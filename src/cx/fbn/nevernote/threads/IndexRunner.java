/*
 * This file is part of NeverNote 
 * Copyright 2009 Randy Baumgarte
 * 
 * This file may be licensed under the terms of of the
 * GNU General Public License Version 2 (the ``GPL'').
 *
 * Software distributed under the License is distributed
 * on an ``AS IS'' basis, WITHOUT WARRANTY OF ANY KIND, either
 * express or implied. See the GPL for the specific language
 * governing rights and limitations.
 *
 * You should have received a copy of the GPL along with this
 * program. If not, go to http://www.gnu.org/licenses/gpl.html
 * or write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
*/

package cx.fbn.nevernote.threads;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.TreeSet;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.microsoft.OfficeParser;
import org.apache.tika.parser.microsoft.ooxml.OOXMLParser;
import org.apache.tika.parser.odf.OpenDocumentParser;
import org.apache.tika.parser.pdf.PDFParser;
import org.apache.tika.parser.rtf.RTFParser;
import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import com.evernote.edam.type.Data;
import com.evernote.edam.type.Note;
import com.evernote.edam.type.Resource;
import com.trolltech.qt.core.QByteArray;
import com.trolltech.qt.core.QIODevice.OpenModeFlag;
import com.trolltech.qt.core.QObject;
import com.trolltech.qt.core.QTemporaryFile;
import com.trolltech.qt.xml.QDomDocument;
import com.trolltech.qt.xml.QDomElement;
import com.trolltech.qt.xml.QDomNodeList;

import cx.fbn.nevernote.Global;
import cx.fbn.nevernote.signals.IndexSignal;
import cx.fbn.nevernote.signals.NoteResourceSignal;
import cx.fbn.nevernote.signals.NoteSignal;
import cx.fbn.nevernote.sql.DatabaseConnection;
import cx.fbn.nevernote.utilities.ApplicationLogger;

public class IndexRunner extends QObject implements Runnable {
	
	private final ApplicationLogger 	logger;
	private String 						guid;
	private QByteArray					resourceBinary;
	public volatile NoteSignal 			noteSignal;
	public volatile NoteResourceSignal	resourceSignal;
	private int							indexType;
	public final int					SCAN=1; 
	public final int					REINDEXALL=2;
	public final int					REINDEXNOTE=3;
	public boolean						keepRunning;
	private final QDomDocument			doc;
	private static String				regex = Global.getWordRegex();
	private final DatabaseConnection	conn;
	private volatile LinkedBlockingQueue<String> workQueue;
	private static int MAX_QUEUED_WAITING = 1000;
	public boolean interrupt;
	public boolean idle;
	public boolean indexAttachmentsLocally = true;
	public volatile IndexSignal			signal;
	private final TreeSet<String>		foundWords;

	
	public IndexRunner(String logname, String u, String uid, String pswd, String cpswd) {
		foundWords = new TreeSet<String>();
		logger = new ApplicationLogger(logname);
		conn = new DatabaseConnection(logger, u, uid, pswd, cpswd);
		indexType = SCAN;
		guid = null;
		keepRunning = true;
		doc = new QDomDocument();
		workQueue=new LinkedBlockingQueue<String>(MAX_QUEUED_WAITING);	
	}
	
	public void setIndexType(int t) {
		indexType = t;
	}
	
	
	@Override
	public void run() {
		thread().setPriority(Thread.MIN_PRIORITY);
		noteSignal = new NoteSignal();
		resourceSignal = new NoteResourceSignal();
		signal = new IndexSignal();
		logger.log(logger.EXTREME, "Starting index thread ");
		while (keepRunning) {
			idle=true;
			try {
				String work = workQueue.take();
				idle=false;
				if (work.startsWith("SCAN")) {
					guid=null;
					interrupt = false;
					indexType = SCAN;
				}
				if (work.startsWith("REINDEXALL")) {
					guid = null;
					indexType=REINDEXALL;
				}
				if (work.startsWith("REINDEXNOTE")) {
					work = work.replace("REINDEXNOTE ", "");
					guid = work;
					indexType = REINDEXNOTE;
				}
				if (work.startsWith("STOP")) {
					keepRunning = false;
					guid = null;
				}
				logger.log(logger.EXTREME, "Type:" +indexType);
				if (indexType == SCAN && keepRunning) {
					logger.log(logger.MEDIUM, "Scanning for unindexed notes & resources");
					scanUnindexed();
					setIndexType(0);
				}
				if (indexType == REINDEXALL && keepRunning) {
					logger.log(logger.MEDIUM, "Marking all for reindex");
					reindexAll();
					setIndexType(0);
				}
				if (indexType == REINDEXNOTE && keepRunning) {
					reindexNote();
				}
			} catch (InterruptedException e) {
				logger.log(logger.LOW, "Thread interrupted exception: " +e.getMessage());
			}
		}
		logger.log(logger.EXTREME, "Shutting down database");
		conn.dbShutdown();
		logger.log(logger.EXTREME, "Database shut down.  Exiting thread");
	}
	
	// Reindex a note
	public void indexNoteContent() {
		foundWords.clear();
		
		logger.log(logger.EXTREME, "Entering indexRunner.indexNoteContent()");
		
		logger.log(logger.EXTREME, "Getting note content");
		Note n = conn.getNoteTable().getNote(guid,true,false,true,true, true);
		String data = n.getContent();
		data = conn.getNoteTable().getNoteContentNoUTFConversion(n.getGuid());
		
		logger.log(logger.EXTREME, "Removing any encrypted data");
		data = removeEnCrypt(data.toString());
		logger.log(logger.EXTREME, "Removing xml markups");
		String text =  removeTags(StringEscapeUtils.unescapeHtml(data) +" "+
		n.getTitle());
				
		logger.log(logger.EXTREME, "Splitting words");
		String[] result = text.toString().split(regex);
		logger.log(logger.EXTREME, "Deleting existing words for note from index");
		conn.getWordsTable().expungeFromWordIndex(guid, "CONTENT");
		
		logger.log(logger.EXTREME, "Number of words found: " +result.length);
		for (int j=0; j<result.length && keepRunning; j++) {
			if (!result[j].trim().equals("")) {
				logger.log(logger.EXTREME, "Result word: " +result[j].trim());
				addToIndex(guid, result[j], "CONTENT");
			}
		}
		// If we were interrupted, we will reindex this note next time
		if (Global.keepRunning) {
			logger.log(logger.EXTREME, "Resetting note guid needed");
			conn.getNoteTable().setIndexNeeded(guid, false);
		}
		logger.log(logger.EXTREME, "Leaving indexRunner.indexNoteContent()");
	}
	
	
	private String removeTags(String text) {
		StringBuffer buffer = new StringBuffer(text);
		boolean inTag = false;
		for (int i=buffer.length()-1; i>=0; i--) {
			if (buffer.charAt(i) == '>')
				inTag = true;
			if (buffer.charAt(i) == '<')
				inTag = false;
			if (inTag || buffer.charAt(i) == '<')
				buffer.deleteCharAt(i);
		}
		
		return buffer.toString();
	}

	
	public synchronized boolean addWork(String request) {
		if (workQueue.size() == 0) {
			workQueue.offer(request);
			return true;
		}
		return false;
	}
	
	public synchronized int getWorkQueueSize() {
		return workQueue.size();
	}
	
	public void indexResource() {
		
		if (guid == null)
			return;
		foundWords.clear();
		Resource r = conn.getNoteTable().noteResourceTable.getNoteResourceRecognition(guid);
		if (r == null || r.getRecognition() == null || r.getRecognition().getBody() == null || r.getRecognition().getBody().length == 0) 
			resourceBinary = new QByteArray(" ");
		else
			resourceBinary = new QByteArray(r.getRecognition().getBody());
		
		conn.getWordsTable().expungeFromWordIndex(r.getNoteGuid(), "RESOURCE");
		// This is due to an old bug & can be removed at some point in the future 11/23/2010
		conn.getWordsTable().expungeFromWordIndex(guid, "RESOURCE");   
			
		doc.setContent(resourceBinary);
		QDomElement docElem = doc.documentElement();
			
		// look for text tags
		QDomNodeList anchors = docElem.elementsByTagName("t");
		for (int i=0; i<anchors.length() && keepRunning; i++) {
			QDomElement enmedia = anchors.at(i).toElement();
			String weight = new String(enmedia.attribute("w"));
			String text = new String(enmedia.text()).toLowerCase();
			if (!text.equals("")) {
				conn.getWordsTable().addWordToNoteIndex(r.getNoteGuid(), text, "RESOURCE", new Integer(weight));
			}
		}
		
		if (Global.keepRunning && indexAttachmentsLocally) {
			indexResourceContent(guid);
		}
				
		if (Global.keepRunning)
			conn.getNoteTable().noteResourceTable.setIndexNeeded(guid,false);
	}
	
	private void indexResourceContent(String guid) {
		Resource r = conn.getNoteTable().noteResourceTable.getNoteResource(guid, true);
		if (r.getMime().equalsIgnoreCase("application/pdf")) {
			indexResourcePDF(r);
			return;
		}
		if (r.getMime().equalsIgnoreCase("application/docx") || 
			r.getMime().equalsIgnoreCase("application/xlsx") || 
			r.getMime().equalsIgnoreCase("application/pptx")) {
			indexResourceOOXML(r);
			return;
		}
		if (r.getMime().equalsIgnoreCase("application/vsd") ||
			r.getMime().equalsIgnoreCase("application/ppt") ||
			r.getMime().equalsIgnoreCase("application/xls") ||
			r.getMime().equalsIgnoreCase("application/msg") ||
			r.getMime().equalsIgnoreCase("application/doc")) {
				indexResourceOffice(r);
				return;
		}
		if (r.getMime().equalsIgnoreCase("application/rtf")) {
					indexResourceRTF(r);
					return;
		}
		if (r.getMime().equalsIgnoreCase("application/odf") ||
			r.getMime().equalsIgnoreCase("application/odt") ||
			r.getMime().equalsIgnoreCase("application/odp") ||
			r.getMime().equalsIgnoreCase("application/odg") ||
			r.getMime().equalsIgnoreCase("application/odb") ||
			r.getMime().equalsIgnoreCase("application/ods")) {
			indexResourceODF(r);
			return;
		}
	}


	private void indexResourceRTF(Resource r) {

		QTemporaryFile f = writeResource(r.getData());
		if (!keepRunning) {
			return;
		}
		
		InputStream input;
		try {
			input = new FileInputStream(new File(f.fileName()));
			ContentHandler textHandler = new BodyContentHandler(-1);
			Metadata metadata = new Metadata();
			RTFParser parser = new RTFParser();	
			ParseContext context = new ParseContext();
			parser.parse(input, textHandler, metadata, context);
			String[] result = textHandler.toString().split(regex);
			for (int i=0; i<result.length && keepRunning; i++) {
				addToIndex(r.getNoteGuid(), result[i], "RESOURCE");
			}
			input.close();
		
			f.close();
		} catch (java.lang.ClassCastException e) {
			logger.log(logger.LOW, "Cast exception: " +e.getMessage());
		} catch (FileNotFoundException e) {
			logger.log(logger.LOW, "FileNotFound  exception: " +e.getMessage());
		} catch (IOException e) {
			logger.log(logger.LOW, "IO  exception: " +e.getMessage());
		} catch (SAXException e) {
			logger.log(logger.LOW, "SAX  exception: " +e.getMessage());
		} catch (TikaException e) {
			logger.log(logger.LOW, "Tika  exception: " +e.getMessage());
		} catch (Exception e) {
			logger.log(logger.LOW, "Unknown  exception: " +e.getMessage());
		} catch (java.lang.NoSuchMethodError e) {
			logger.log(logger.LOW, "NoSuchMethod error: " +e.getMessage());
		} catch (Error e) {
			logger.log(logger.LOW, "Unknown error: " +e.getMessage());
		}
	}

	
	private void indexResourceODF(Resource r) {

		QTemporaryFile f = writeResource(r.getData());
		if (!keepRunning) {
			return;
		}
		
		InputStream input;
		try {
			input = new FileInputStream(new File(f.fileName()));
			ContentHandler textHandler = new BodyContentHandler(-1);
			Metadata metadata = new Metadata();
			OpenDocumentParser parser = new OpenDocumentParser();	
			ParseContext context = new ParseContext();
			parser.parse(input, textHandler, metadata, context);
			String[] result = textHandler.toString().split(regex);
			for (int i=0; i<result.length && keepRunning; i++) {
				addToIndex(r.getNoteGuid(), result[i], "RESOURCE");
			}
			input.close();
		
			f.close();
		} catch (java.lang.ClassCastException e) {
			logger.log(logger.LOW, "Cast exception: " +e.getMessage());
		} catch (FileNotFoundException e) {
			logger.log(logger.LOW, "FileNotFound  exception: " +e.getMessage());
		} catch (IOException e) {
			logger.log(logger.LOW, "IO  exception: " +e.getMessage());
		} catch (SAXException e) {
			logger.log(logger.LOW, "SAX  exception: " +e.getMessage());
		} catch (TikaException e) {
			logger.log(logger.LOW, "Tika  exception: " +e.getMessage());
		} catch (Exception e) {
			logger.log(logger.LOW, "Unknown  exception: " +e.getMessage());
		} catch (java.lang.NoSuchMethodError e) {
			logger.log(logger.LOW, "NoSuchMethod error: " +e.getMessage());
		} catch (Error e) {
			logger.log(logger.LOW, "Unknown error: " +e.getMessage());
		}
	}

	
	private void indexResourceOffice(Resource r) {

		QTemporaryFile f = writeResource(r.getData());
		if (!keepRunning) {
			return;
		}
		
		InputStream input;
		try {
			input = new FileInputStream(new File(f.fileName()));
			ContentHandler textHandler = new BodyContentHandler(-1);
			Metadata metadata = new Metadata();
			OfficeParser parser = new OfficeParser();	
			ParseContext context = new ParseContext();
			parser.parse(input, textHandler, metadata, context);
			String[] result = textHandler.toString().split(regex);
			for (int i=0; i<result.length && keepRunning; i++) {
				addToIndex(r.getNoteGuid(), result[i], "RESOURCE");
			}
			input.close();
		
			f.close();
		} catch (java.lang.ClassCastException e) {
			logger.log(logger.LOW, "Cast exception: " +e.getMessage());
		} catch (FileNotFoundException e) {
			logger.log(logger.LOW, "FileNotFound  exception: " +e.getMessage());
		} catch (IOException e) {
			logger.log(logger.LOW, "IO  exception: " +e.getMessage());
		} catch (SAXException e) {
			logger.log(logger.LOW, "SAX  exception: " +e.getMessage());
		} catch (TikaException e) {
			logger.log(logger.LOW, "Tika  exception: " +e.getMessage());
		} catch (Exception e) {
			logger.log(logger.LOW, "Unknown  exception: " +e.getMessage());
		} catch (java.lang.NoSuchMethodError e) {
			logger.log(logger.LOW, "NoSuchMethod error: " +e.getMessage());
		} catch (Error e) {
			logger.log(logger.LOW, "Unknown error: " +e.getMessage());
		}
	}

	
	
	private void indexResourcePDF(Resource r) {

		QTemporaryFile f = writeResource(r.getData());
		if (!keepRunning) {
			return;
		}
		
		InputStream input;
		try {			
			input = new FileInputStream(new File(f.fileName()));
			ContentHandler textHandler = new BodyContentHandler(-1);
			Metadata metadata = new Metadata();
			PDFParser parser = new PDFParser();	
			ParseContext context = new ParseContext();
			parser.parse(input, textHandler, metadata, context);
			String[] result = textHandler.toString().split(regex);
			for (int i=0; i<result.length && keepRunning; i++) {
				addToIndex(r.getNoteGuid(), result[i], "RESOURCE");
			}
			input.close();
		
			f.close();
		} catch (java.lang.ClassCastException e) {
			logger.log(logger.LOW, "Cast exception: " +e.getMessage());
		} catch (FileNotFoundException e) {
			logger.log(logger.LOW, "FileNotFound  exception: " +e.getMessage());
		} catch (IOException e) {
			logger.log(logger.LOW, "IO  exception: " +e.getMessage());
		} catch (SAXException e) {
			logger.log(logger.LOW, "SAX  exception: " +e.getMessage());
		} catch (TikaException e) {
			logger.log(logger.LOW, "Tika  exception: " +e.getMessage());
		} catch (Exception e) {
			logger.log(logger.LOW, "Unknown  exception: " +e.getMessage());
		} catch (java.lang.NoSuchMethodError e) {
			logger.log(logger.LOW, "NoSuchMethod error: " +e.getMessage());
		} catch (Error e) {
			logger.log(logger.LOW, "Unknown error: " +e.getMessage());
		}
	}
	
	
	private void indexResourceOOXML(Resource r) {

		QTemporaryFile f = writeResource(r.getData());
		if (!keepRunning) {
			return;
		}
		
		InputStream input;
		try {
			input = new FileInputStream(new File(f.fileName()));
			ContentHandler textHandler = new BodyContentHandler(-1);
			Metadata metadata = new Metadata();
			OOXMLParser parser = new OOXMLParser();	
			ParseContext context = new ParseContext();
			parser.parse(input, textHandler, metadata, context);
			String[] result = textHandler.toString().split(regex);
			for (int i=0; i<result.length && keepRunning; i++) {
				addToIndex(r.getNoteGuid(), result[i], "RESOURCE");
			}
			input.close();
		
			f.close();
		} catch (java.lang.ClassCastException e) {
			logger.log(logger.LOW, "Cast exception: " +e.getMessage());
		} catch (FileNotFoundException e) {
			logger.log(logger.LOW, "FileNotFound  exception: " +e.getMessage());
		} catch (IOException e) {
			logger.log(logger.LOW, "IO  exception: " +e.getMessage());
		} catch (SAXException e) {
			logger.log(logger.LOW, "SAX  exception: " +e.getMessage());
		} catch (TikaException e) {
			logger.log(logger.LOW, "Tika  exception: " +e.getMessage());
		} catch (Exception e) {
			logger.log(logger.LOW, "Unknown  exception: " +e.getMessage());
		} catch (java.lang.NoSuchMethodError e) {
			logger.log(logger.LOW, "NoSuchMethod error: " +e.getMessage());
		} catch (Error e) {
			logger.log(logger.LOW, "Unknown error: " +e.getMessage());
		}
	}
	

	
	private QTemporaryFile writeResource(Data d) {
		QTemporaryFile newFile = new QTemporaryFile();
		newFile.open(OpenModeFlag.WriteOnly);
		newFile.write(d.getBody());
		newFile.close();
		return newFile;
	} 

	
	private String removeEnCrypt(String content) {
		int index = content.indexOf("<en-crypt");
		int endPos;
		boolean tagFound = true;
		while (tagFound && keepRunning) {
			endPos = content.indexOf("</en-crypt>", index)+11;
			if (endPos > -1 && index > -1) {
				content = content.substring(0,index)+content.substring(endPos);
				index = content.indexOf("<en-crypt");
			} else {
				tagFound = false;
			}
		}
		return content;
	}

	
	private void addToIndex(String guid, String word, String type) {
		if (!foundWords.contains(word))
			foundWords.add(word);
		StringBuffer buffer = new StringBuffer(word.toLowerCase());
		for (int i=buffer.length()-1; i>=0; i--) {
			if (!Character.isLetterOrDigit(buffer.charAt(i)))
				buffer.deleteCharAt(i);
			else
				break;
		}
		buffer = buffer.reverse();
		for (int i=buffer.length()-1; i>=0; i--) {
			if (!Character.isLetterOrDigit(buffer.charAt(i)))
				buffer.deleteCharAt(i);
			else
				break;
		}
		buffer = buffer.reverse();
		if (buffer.length() > 0) {
			// We have a good word, now let's trim off junk at the beginning or end
			if (!foundWords.contains(buffer.toString())) {
				foundWords.add(buffer.toString());
				conn.getWordsTable().addWordToNoteIndex(guid, buffer.toString(), type, 100);
			}
		}
		return;
	}
	
	private void scanUnindexed() {
		List<String> notes = conn.getNoteTable().getUnindexed();
		guid = null;
		boolean started = false;
		if (notes.size() > 0) {
			signal.indexStarted.emit();
			started = true;
		}
		for (int i=0; i<notes.size() && !interrupt && keepRunning; i++) {
			guid = notes.get(i);
			if (guid != null && keepRunning) {
				indexNoteContent();
			}
		}
		
		List<String> unindexedResources = conn.getNoteTable().noteResourceTable.getUnindexed();
		if (unindexedResources.size() > 0 && !started) {
			signal.indexStarted.emit();
			started = true;
		}
		for (int i=0; i<unindexedResources.size()&& !interrupt && keepRunning; i++) {
			guid = unindexedResources.get(i);
			if (keepRunning) {
				indexResource();
			}
		}
		if (started && keepRunning && !interrupt) 
			signal.indexFinished.emit();
	}
	
	private void reindexNote() {
		if (guid == null)
			return;
		conn.getNoteTable().setIndexNeeded(guid, true);
	}
	
	private void reindexAll() {
		conn.getNoteTable().reindexAllNotes();
		conn.getNoteTable().noteResourceTable.reindexAll(); 
	}

}
