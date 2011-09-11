/*
 * This file is part of NixNote 
 * Copyright 2011 Randy Baumgarte
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

package cx.fbn.nevernote.xml;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import com.evernote.edam.type.Data;
import com.evernote.edam.type.Note;
import com.evernote.edam.type.NoteAttributes;
import com.evernote.edam.type.NoteSortOrder;
import com.evernote.edam.type.Notebook;
import com.evernote.edam.type.Publishing;
import com.evernote.edam.type.Resource;
import com.evernote.edam.type.ResourceAttributes;
import com.evernote.edam.type.Tag;
import com.trolltech.qt.core.QByteArray;
import com.trolltech.qt.core.QFile;
import com.trolltech.qt.core.QIODevice;
import com.trolltech.qt.core.QUuid;
import com.trolltech.qt.gui.QIcon;
import com.trolltech.qt.gui.QImage;
import com.trolltech.qt.gui.QPixmap;
import com.trolltech.qt.xml.QXmlStreamReader;

import cx.fbn.nevernote.sql.DatabaseConnection;
import cx.fbn.nevernote.utilities.ApplicationLogger;

public class ImportEnex {

	public int							lastError;
	private String						errorMessage;
	private String						fileName;
	DatabaseConnection					conn;
	QXmlStreamReader					reader;
	private Note						note;
	private Notebook					notebook;
	private boolean						notebookIsDirty;
	private boolean						notebookIsLocal;
	private boolean						notebookIsReadOnly;
	private QIcon						notebookIcon;
	private Tag							tag;
	private boolean						tagIsDirty;
	public int							highUpdateSequenceNumber;
	public long							lastSequenceDate;
	private final ApplicationLogger 	logger;
	private final boolean				backup;
	private String						notebookGuid;
	public final boolean				importTags = false;
	public final boolean				importNotebooks = false;
	private String newGuid;
	
	public ImportEnex(DatabaseConnection c, boolean full) {
		logger = new ApplicationLogger("import.log");
		backup = full;
		conn = c;
	}
	
	public void importData(String f) {
		fileName = f;
		errorMessage = "";
				
		lastError = 0;
		errorMessage = "";
		QFile xmlFile = new QFile(fileName);
		if (!xmlFile.open(QIODevice.OpenModeFlag.ReadOnly)) {
			lastError = 16;
			errorMessage = "Cannot open file.";
		}
			
		reader = new QXmlStreamReader(xmlFile);	
		while (!reader.atEnd()) {
			reader.readNext();
			if (reader.hasError()) {
				errorMessage = reader.errorString();
				logger.log(logger.LOW, "************************* ERROR READING FILE " +reader.errorString());
				lastError = 16;
				return;
			}
			if (reader.name().equalsIgnoreCase("note") && reader.isStartElement()) {
				processNoteNode();
				note.setUpdateSequenceNum(0);
				if (notebookGuid != null) 
					note.setNotebookGuid(notebookGuid);
				for (int i=0; i<note.getResourcesSize(); i++) {
					note.getResources().get(i).setUpdateSequenceNum(0);
				}
				note.setActive(true);
				conn.getNoteTable().addNote(note, true);
			}
			if (reader.name().equalsIgnoreCase("notebook") && reader.isStartElement() && (backup || importNotebooks)) {
				processNotebookNode();
	    		String existingGuid = conn.getNotebookTable().findNotebookByName(notebook.getName());
	    		if (existingGuid == null) {
	    			conn.getNotebookTable().addNotebook(notebook, notebookIsDirty, notebookIsLocal);
	    		} else {
	    			conn.getNotebookTable().updateNotebookGuid(existingGuid, notebook.getGuid());
	    			conn.getNotebookTable().updateNotebook(notebook, notebookIsDirty);
	    		}
	    		conn.getNotebookTable().setIcon(notebook.getGuid(), notebookIcon, "PNG");
	    		conn.getNotebookTable().setReadOnly(notebook.getGuid(), notebookIsReadOnly);
			}
			if (reader.name().equalsIgnoreCase("tag") && reader.isStartElement() && (backup || importTags)) {
				processTagNode();
		   		String testGuid = conn.getTagTable().findTagByName(tag.getName());
	    		if (testGuid == null)
	    			conn.getTagTable().addTag(tag, tagIsDirty);
	    		else {
	    			conn.getTagTable().updateTagGuid(testGuid, tag.getGuid());
	    			conn.getTagTable().updateTag(tag,tagIsDirty);
	    		}
			}
		}
		xmlFile.close();
	}
	
	
	private void processNoteNode() {
		note = new Note();
		newGuid = QUuid.createUuid().toString().replace("{", "").replace("}", "");
		note.setGuid(newGuid);
		note.setResources(new ArrayList<Resource>());
		
		boolean atEnd = false;
		while(!atEnd) {
			if (reader.name().equalsIgnoreCase("title")) 
				note.setTitle(textValue());
			if (reader.name().equalsIgnoreCase("Created")) 
				note.setCreated(datetimeValue());
			if (reader.name().equalsIgnoreCase("Content")) 
				note.setContent(textValue());
			if (reader.name().equalsIgnoreCase("note-attributes")) 
				note.setAttributes(processNoteAttributes());
			if (reader.name().equalsIgnoreCase("resource")) {
				note.getResources().add(processResource());
			}
			reader.readNext();
			if (reader.name().equalsIgnoreCase("note") && reader.isEndElement())
				atEnd = true;
		}
		return;
	}	
	private Resource processResource() {
		Resource resource = new Resource();
		boolean atEnd = false;
		while(!atEnd) {
			if (reader.isStartElement()) {
				String newResGuid = QUuid.createUuid().toString().replace("{", "").replace("}", "");
				resource.setGuid(newResGuid);
				resource.setNoteGuid(this.newGuid);
			}
			if (reader.name().equalsIgnoreCase("mime")) 
				resource.setMime(textValue());
			if (reader.name().equalsIgnoreCase("height")) 
				resource.setHeight(shortValue());
			if (reader.name().equalsIgnoreCase("width")) 
				resource.setWidth(shortValue());
			if (reader.name().equalsIgnoreCase("data")) 
				resource.setData(processData("data"));
//				if (reader.name().equalsIgnoreCase("recognition")) 
//					resource.setRecognition(processData("NoteResourceAttribute"));
			reader.readNext();
			if (reader.name().equalsIgnoreCase("resource") && reader.isEndElement())
				atEnd = true;
		}
		resource.setAttributes(new ResourceAttributes());
		resource.getAttributes().setSourceURL("");
		conn.getNoteTable().noteResourceTable.saveNoteResource(resource, true);

		
		return resource;
	}
	
	private Data processData(String nodeName) {
		Data data = new Data();
		boolean atEnd = false;
		while(!atEnd) {
			if (reader.isStartElement()) {
				byte[] b = textValue().getBytes();   // data binary
				if (b.length > 0) {
					QByteArray hexData = new QByteArray(b);
					QByteArray binData = new QByteArray(QByteArray.fromHex(hexData));
					data.setBody(binData.toByteArray());
					MessageDigest md;
					try {
						md = MessageDigest.getInstance("MD5");
						md.update(b);
						data.setBodyHash(md.digest());
					} catch (NoSuchAlgorithmException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			if (reader.name().equalsIgnoreCase(nodeName) && reader.isEndElement())
				atEnd = true;
			else 
				reader.readNext();
		}
		return data;
	}

	
	private NoteAttributes processNoteAttributes() {
	NoteAttributes attributes = new NoteAttributes();
	
	boolean atEnd = false;
	while(!atEnd) {
		if (reader.isStartElement()) {
			if (reader.name().equalsIgnoreCase("source-url")) 
				attributes.setSourceURL(textValue());
			if (reader.name().equalsIgnoreCase("source")) 
				attributes.setSource(textValue());
		}
		reader.readNext();
		if (reader.name().equalsIgnoreCase("note-attributes") && reader.isEndElement())
			atEnd = true;
	}
	
	return attributes;
}

		
	

	

	
	private void processNotebookNode() {
		notebook = new Notebook();
		Publishing p = new Publishing();
		notebook.setPublishing(p);
		notebookIsDirty = false;
		notebookIsLocal = false;
		notebookIsReadOnly = false;
		notebookIcon = null;
		boolean atEnd = false;
		while(!atEnd) {
			if (reader.isStartElement()) {
				if (reader.name().equalsIgnoreCase("Guid")) 
					notebook.setGuid(textValue());
				if (reader.name().equalsIgnoreCase("Name")) 
					notebook.setName(textValue());
				if (reader.name().equalsIgnoreCase("UpdateSequenceNumber")) 
					notebook.setUpdateSequenceNum(intValue());
				if (reader.name().equalsIgnoreCase("ServiceCreated")) 
					notebook.setServiceCreated(longValue());
				if (reader.name().equalsIgnoreCase("ServiceUpdated")) 
					notebook.setServiceUpdated(longValue());
				if (reader.name().equalsIgnoreCase("DefaultNotebook")) {
					notebook.setDefaultNotebook(booleanValue());
				}
				if (reader.name().equalsIgnoreCase("Dirty")) {
					if (booleanValue())
						notebookIsDirty = true;
				}
				if (reader.name().equalsIgnoreCase("LocalNotebook")) {
					if (booleanValue())
						notebookIsLocal = true;
				}
				if (reader.name().equalsIgnoreCase("ReadOnly")) {
					if (booleanValue())
						notebookIsReadOnly = true;
				}
				if (reader.name().equalsIgnoreCase("PublishingPublicDescription")) {
					notebook.getPublishing().setPublicDescription(textValue());
				}
				if (reader.name().equalsIgnoreCase("PublishingUri")) {
					notebook.getPublishing().setUri(textValue());
				}
				if (reader.name().equalsIgnoreCase("PublishingOrder")) {
					notebook.getPublishing().setOrder(NoteSortOrder.findByValue(intValue()));
				}
				if (reader.name().equalsIgnoreCase("ReadOnly")) {
					if (booleanValue())
						notebookIsReadOnly = true;
				}
				if (reader.name().equalsIgnoreCase("PublishingAscending")) {
					if (booleanValue())
						notebook.getPublishing().setAscending(true);
					else
						notebook.getPublishing().setAscending(false);
				}		
				if (reader.name().equalsIgnoreCase("Icon")) {
					byte[] b = textValue().getBytes();   // data binary
					QByteArray hexData = new QByteArray(b);
					QByteArray binData = new QByteArray(QByteArray.fromHex(hexData));
					notebookIcon = new QIcon(QPixmap.fromImage(QImage.fromData(binData)));
				}
				if (reader.name().equalsIgnoreCase("Stack"))
					notebook.setStack(textValue());
			}
			reader.readNext();
			if (reader.name().equalsIgnoreCase("notebook") && reader.isEndElement())
				atEnd = true;
		}
		return;
	}

	
	
	private void processTagNode() {
		tag = new Tag();
		tagIsDirty = false;
		boolean atEnd = false;
		while(!atEnd) {
			if (reader.isStartElement()) {			
				if (reader.name().equalsIgnoreCase("Guid")) 
					tag.setGuid(textValue());
				if (reader.name().equalsIgnoreCase("Name")) 
					tag.setName(textValue());
				if (reader.name().equalsIgnoreCase("UpdateSequenceNumber")) 
					tag.setUpdateSequenceNum(intValue());
				if (reader.name().equalsIgnoreCase("ParentGuid")) 
					tag.setParentGuid(textValue());
				if (reader.name().equalsIgnoreCase("Dirty")) {
					if (booleanValue())
						tagIsDirty = true;
				}
			}
			reader.readNext();
			if (reader.name().equalsIgnoreCase("tag") && reader.isEndElement())
				atEnd = true;
		}
		return;
	}
	
	
	
	
	private String textValue() {
		return reader.readElementText();
	}
	private int intValue() {
		return new Integer(textValue());
	}
	private long longValue() {
		return new Long(textValue());
	}
	private long datetimeValue() {
		Date d;
		String time = textValue();
		String year = time.substring(0,4);
		String month = time.substring(4,6);
		String day = time.substring(6,8);
		String hour = time.substring(9,11);
		String minute = time.substring(11,13);
		String second = time.substring(13,15);
		DateFormat dfm = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		try {
			d = dfm.parse(year +"-" +month +"-" +day +" " +hour +":" +minute +":" +second);
			return d.getTime();
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 0;
	}

	private boolean booleanValue() {
		String value = textValue();
		if (value.equalsIgnoreCase("true"))
			return true;
		else
			return false;
	}
	private short shortValue() {
		return new Short(textValue());
	}
	
	public void setNotebookGuid(String g) {
		notebookGuid = g;
	}
	
	public String getErrorMessage() {
		return errorMessage;
	}
}
