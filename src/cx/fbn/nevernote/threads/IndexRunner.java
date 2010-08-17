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

import java.util.concurrent.LinkedBlockingQueue;

import org.apache.commons.lang.StringEscapeUtils;

import com.evernote.edam.type.Note;
import com.evernote.edam.type.Resource;
import com.trolltech.qt.core.QByteArray;
import com.trolltech.qt.core.QObject;
import com.trolltech.qt.xml.QDomDocument;
import com.trolltech.qt.xml.QDomElement;
import com.trolltech.qt.xml.QDomNodeList;

import cx.fbn.nevernote.Global;
import cx.fbn.nevernote.signals.NoteResourceSignal;
import cx.fbn.nevernote.signals.NoteSignal;
import cx.fbn.nevernote.sql.DatabaseConnection;
import cx.fbn.nevernote.utilities.ApplicationLogger;

//public class IndexRunner implements QRunnable {
public class IndexRunner extends QObject implements Runnable {
	
	private final ApplicationLogger 	logger;
	private String 						guid;
	private QByteArray					resourceBinary;
	public volatile NoteSignal 			noteSignal;
	public volatile NoteResourceSignal	resourceSignal;
	private int							indexType;
	public final int					CONTENT=1; 
	public final int					RESOURCE=2;
	private boolean						keepRunning;
//	public volatile int					ID;
	private final QDomDocument			doc;
	private static String				regex = Global.getWordRegex();
	private final DatabaseConnection	conn;
	private volatile LinkedBlockingQueue<String> workQueue;
//	private static int MAX_EMPTY_QUEUE_COUNT = 1;
	private static int MAX_QUEUED_WAITING = 1000;

	

	
	public IndexRunner(String logname, String u, String uid, String pswd, String cpswd) {
		logger = new ApplicationLogger(logname);
		conn = new DatabaseConnection(logger, u, uid, pswd, cpswd);
		noteSignal = new NoteSignal();
		resourceSignal = new NoteResourceSignal();
//		threadSignal = new ThreadSignal();
		indexType = CONTENT;
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
		logger.log(logger.EXTREME, "Starting index thread ");
		while (keepRunning) {
			try {
				String work = workQueue.take();
				if (work.startsWith("CONTENT")) {
					work = work.replace("CONTENT ", "");
					guid = work;
					indexType = CONTENT;
				}
				if (work.startsWith("RESOURCE")) {
					work = work.replace("RESOURCE ", "");
					guid = work;
					indexType = RESOURCE;
				}
				if (work.startsWith("STOP")) {
					keepRunning = false;
					guid = work;
				}
				if (guid == null || guid.trim().equals("")) {
					setIndexType(0);
					resourceSignal.resourceIndexed.emit("null or empty guid");
				}
				logger.log(logger.EXTREME, "Type:" +indexType);
				if (indexType == CONTENT && keepRunning) {
					logger.log(logger.MEDIUM, "Indexing note: "+guid);
					indexNoteContent();
					setIndexType(0);
				}
				if (indexType == RESOURCE && keepRunning) {
					logger.log(logger.MEDIUM, "Indexing resource: "+guid);
					indexResource();
					setIndexType(0);
				}
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		conn.dbShutdown();
	}
	
	// Reindex a note
	public void indexNoteContent() {
		logger.log(logger.EXTREME, "Entering indexRunner.indexNoteContent()");
		
		logger.log(logger.EXTREME, "Getting note content");
		Note n = conn.getNoteTable().getNote(guid,true,false,true,true, true);
		String data = n.getContent();
		
		logger.log(logger.EXTREME, "Removing any encrypted data");
		data = removeEnCrypt(data);
		logger.log(logger.EXTREME, "Removing xml markups");
		String text = StringEscapeUtils.unescapeHtml(data.replaceAll("\\<.*?\\>", ""));
		
		logger.log(logger.EXTREME, "Splitting words");
		String[] result = text.toString().split(regex);
		logger.log(logger.EXTREME, "Deleting existing words for note from index");
		conn.getWordsTable().expungeFromWordIndex(guid, "CONTENT");
		
		logger.log(logger.EXTREME, "Number of words found: " +result.length);
		for (int j=0; j<result.length && keepRunning; j++) {
			logger.log(logger.EXTREME, "Result word: " +result[j]);
			if (result[j].length() > 0) {
				if (Character.isLetterOrDigit(result[j].charAt(0))) {
					int len = result[j].length();
					StringBuffer buffer = new StringBuffer(result[j].toLowerCase());
					logger.log(logger.EXTREME, "Processing " +buffer);
					for (int k=len-1; k>=0 && keepRunning; k--) {
						if (!Character.isLetterOrDigit(result[j].charAt(k)))
							buffer.deleteCharAt(k);
						else
							k=-1;
					}

					if (buffer.length()>=Global.minimumWordCount) {
						logger.log(logger.EXTREME, "Adding " +buffer);
						conn.getWordsTable().addWordToNoteIndex(guid, buffer.toString(), "CONTENT", 100);
					}
				}
			}
		}
		// If we were interrupted, we will reindex this note next time
		if (Global.keepRunning) {
			logger.log(logger.EXTREME, "Resetting note guid needed");
			conn.getNoteTable().setIndexNeeded(guid, false);
		}
		logger.log(logger.EXTREME, "Leaving indexRunner.indexNoteContent()");
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
		
		Resource r = conn.getNoteTable().noteResourceTable.getNoteResourceRecognition(guid);
		if (r == null || r.getRecognition() == null || r.getRecognition().getBody() == null || r.getRecognition().getBody().length == 0) 
			resourceBinary = new QByteArray(" ");
		else
			resourceBinary = new QByteArray(r.getRecognition().getBody());
		
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
				conn.getWordsTable().addWordToNoteIndex(guid, text, "RESOURCE", new Integer(weight));
			}
		}
		if (Global.keepRunning)
			conn.getNoteTable().noteResourceTable.setIndexNeeded(guid,false);
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

	
	
	

}
