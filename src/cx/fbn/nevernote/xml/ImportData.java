/*
 * This file is part of NixNote 
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

package cx.fbn.nevernote.xml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import com.evernote.edam.type.Data;
import com.evernote.edam.type.LinkedNotebook;
import com.evernote.edam.type.Note;
import com.evernote.edam.type.NoteAttributes;
import com.evernote.edam.type.NoteSortOrder;
import com.evernote.edam.type.Notebook;
import com.evernote.edam.type.Publishing;
import com.evernote.edam.type.Resource;
import com.evernote.edam.type.ResourceAttributes;
import com.evernote.edam.type.SavedSearch;
import com.evernote.edam.type.SharedNotebook;
import com.evernote.edam.type.Tag;
import com.trolltech.qt.core.QByteArray;
import com.trolltech.qt.core.QFile;
import com.trolltech.qt.core.QIODevice;
import com.trolltech.qt.gui.QIcon;
import com.trolltech.qt.gui.QImage;
import com.trolltech.qt.gui.QPixmap;
import com.trolltech.qt.xml.QXmlStreamAttributes;
import com.trolltech.qt.xml.QXmlStreamReader;

import cx.fbn.nevernote.evernote.NoteMetadata;
import cx.fbn.nevernote.sql.DatabaseConnection;
import cx.fbn.nevernote.utilities.ApplicationLogger;

public class ImportData {

	public int							lastError;
	private String						errorMessage;
	private String						fileName;
	DatabaseConnection					conn;
	QXmlStreamReader					reader;
	private Note						note;
	private boolean						noteIsDirty;
	private Notebook					notebook;
	private boolean						notebookIsDirty;
	private boolean						notebookIsLocal;
	private boolean						notebookIsReadOnly;
	private QIcon						notebookIcon;
	private Tag							tag;
	private boolean						tagIsDirty;
//	private final HashMap<String,Integer>		titleColors;
	private SavedSearch					search;
	private boolean						searchIsDirty;
	public int							highUpdateSequenceNumber;
	public long							lastSequenceDate;
	private final ApplicationLogger 	logger;
	private final boolean				backup;
	private String						notebookGuid;
	private boolean						linkedNotebookIsDirty;
	private boolean						sharedNotebookIsDirty;
	private LinkedNotebook				linkedNotebook;
	private SharedNotebook				sharedNotebook;
	public final boolean				importTags = false;
	public final boolean				importNotebooks = false;
	private final HashMap<String,String>		noteMap;
	private final HashMap<String, NoteMetadata> metaData;
	
	public ImportData(DatabaseConnection c, boolean full) {
		logger = new ApplicationLogger("import.log");
		backup = full;
		conn = c;
		metaData = new HashMap<String,NoteMetadata>();
		noteMap = new HashMap<String,String>();
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
				logger.log(logger.LOW, "************************* ERROR READING BACKUP " +reader.errorString());
				lastError = 16;
				return;
			}
			if (reader.name().equalsIgnoreCase("nevernote-export") && reader.isStartElement()) {
				QXmlStreamAttributes attributes = reader.attributes();
				String version = attributes.value("version");
				String type = attributes.value("exportType");
				String application = attributes.value("application");
				if (!version.equalsIgnoreCase("0.85") && !version.equalsIgnoreCase("0.86")
						&& !version.equalsIgnoreCase("0.95")) {
					lastError = 1;
					errorMessage = "Unknown backup version = " +version;
					return;
				}
				if (!application.equalsIgnoreCase("NeverNote")) {
					lastError = 2;
					errorMessage = "This backup is from an unknown application = " +application;
					return;
				}
				if (!type.equalsIgnoreCase("backup") && backup) {
					lastError = 4;
					errorMessage = "This is an export file, not a backup file";
					return;
				} 
				if (type.equalsIgnoreCase("export") && backup) {
					lastError = 5;
					errorMessage = "This is a backup file, not an export file";		
					return;
				}
				
			}
			if (reader.name().equalsIgnoreCase("Synchronization") && reader.isStartElement() && backup) {
				processSynchronizationNode();
				conn.getSyncTable().setLastSequenceDate(lastSequenceDate);
		    	conn.getSyncTable().setUpdateSequenceNumber(highUpdateSequenceNumber);
			}
			if (reader.name().equalsIgnoreCase("note") && reader.isStartElement()) {
				processNoteNode();
				if (backup)
					conn.getNoteTable().addNote(note, noteIsDirty);
				else {
					note.setUpdateSequenceNum(0);
					if (notebookGuid != null) 
						note.setNotebookGuid(notebookGuid);
					for (int i=0; i<note.getResourcesSize(); i++) {
						note.getResources().get(i).setUpdateSequenceNum(0);
					}
					conn.getNoteTable().addNote(note, true);
				}
				if (metaData.containsKey(note.getGuid())) 
					conn.getNoteTable().updateNoteMetadata(metaData.get(note.getGuid()));
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
			if (reader.name().equalsIgnoreCase("savedsearch") && reader.isStartElement() && backup) {
				processSavedSearchNode();
	    		conn.getSavedSearchTable().addSavedSearch(search, searchIsDirty);
			}
			if (reader.name().equalsIgnoreCase("LinkedNotebook") && reader.isStartElement() && backup) {
				processLinkedNotebookNode();
	    		conn.getLinkedNotebookTable().addNotebook(linkedNotebook, linkedNotebookIsDirty);
			}
			if (reader.name().equalsIgnoreCase("SharedNotebook") && reader.isStartElement() && backup) {
				processSharedNotebookNode();
	    		conn.getSharedNotebookTable().addNotebook(sharedNotebook, sharedNotebookIsDirty);
			}
		}
		xmlFile.close();
	}
	
	
	private void processNoteNode() {
		note = new Note();
		note.setResources(new ArrayList<Resource>());

		boolean atEnd = false;
		while(!atEnd) {
			if (reader.isStartElement()) {
				if (reader.name().equalsIgnoreCase("Guid")) { 
					note.setGuid(textValue());
					if (!backup) {
						Random random1 = new Random();
						String newGuid = "IMP" +new Integer(random1.nextInt(1000)).toString();
						newGuid = newGuid+"-"+new Integer(random1.nextInt(1000)).toString();
						newGuid = newGuid+"-"+new Integer(random1.nextInt(1000)).toString();
						newGuid = newGuid+"-"+new Integer(random1.nextInt(1000)).toString();
						noteMap.put(note.getGuid(), newGuid);
						note.setGuid(newGuid);
					} else 
						noteMap.put(note.getGuid(), note.getGuid());
				}
				if (reader.name().equalsIgnoreCase("UpdateSequenceNumber")) 
					note.setUpdateSequenceNum(intValue());
				if (reader.name().equalsIgnoreCase("Title")) 
					note.setTitle(textValue());
				if (reader.name().equalsIgnoreCase("Created")) 
					note.setCreated(longValue());
				if (reader.name().equalsIgnoreCase("Updated")) 
					note.setUpdated(longValue());
				if (reader.name().equalsIgnoreCase("Deleted")) 
					note.setDeleted(longValue());
				if (reader.name().equalsIgnoreCase("Active")) 
					note.setActive(booleanValue());
				if (reader.name().equalsIgnoreCase("NotebookGuid")) 
					note.setNotebookGuid(textValue());
				if (reader.name().equalsIgnoreCase("Content")) 
					note.setContent(textValue());
				if (reader.name().equalsIgnoreCase("NoteTags")) 
					note.setTagGuids(processNoteTagList());
				if (reader.name().equalsIgnoreCase("NoteAttributes")) 
					note.setAttributes(processNoteAttributes());
				if (reader.name().equalsIgnoreCase("NoteResource")) 
					note.getResources().add(processResource());
				if (reader.name().equalsIgnoreCase("Dirty")) {
					if (booleanValue())
						noteIsDirty=true;
				}
				if (reader.name().equalsIgnoreCase("TitleColor")) {
					if (metaData.get(note.getGuid()) == null) {
						NoteMetadata m = new NoteMetadata();
						m.setColor(intValue());
						metaData.put(note.getGuid(), m);
					} else
						metaData.get(note.getGuid()).setColor(intValue());
				}
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
		boolean isDirty = false;
		while(!atEnd) {
			if (reader.isStartElement()) {
				if (reader.name().equalsIgnoreCase("Guid")) 
					resource.setGuid(textValue());
				if (!backup) {
					Random random1 = new Random();
					String newGuid = "IMP" +new Integer(random1.nextInt(1000)).toString();
					newGuid = newGuid+"-"+new Integer(random1.nextInt(1000)).toString();
					newGuid = newGuid+"-"+new Integer(random1.nextInt(1000)).toString();
					newGuid = newGuid+"-"+new Integer(random1.nextInt(1000)).toString();
					resource.setGuid(newGuid);
				}
				if (reader.name().equalsIgnoreCase("NoteGuid")) {
					String tx = textValue();
					resource.setNoteGuid(noteMap.get(textValue()));  
					resource.setNoteGuid(noteMap.get(tx));
				}
				if (reader.name().equalsIgnoreCase("UpdateSequenceNumber")) 
					resource.setUpdateSequenceNum(intValue());
				if (reader.name().equalsIgnoreCase("Active")) 
					resource.setActive(booleanValue());
				if (reader.name().equalsIgnoreCase("Mime")) 
					resource.setMime(textValue());
				if (reader.name().equalsIgnoreCase("Duration")) 
					resource.setDuration(shortValue());
				if (reader.name().equalsIgnoreCase("Height")) 
					resource.setHeight(shortValue());
				if (reader.name().equalsIgnoreCase("Width")) 
					resource.setWidth(shortValue());
				if (reader.name().equalsIgnoreCase("dirty")) 
					isDirty = booleanValue();
				if (reader.name().equalsIgnoreCase("Data")) 
					resource.setData(processData("Data"));
				if (reader.name().equalsIgnoreCase("AlternateData")) 
					resource.setAlternateData(processData("AlternateData"));
				if (reader.name().equalsIgnoreCase("RecognitionData")) 
					resource.setRecognition(processData("RecognitionData"));
				if (reader.name().equalsIgnoreCase("NoteResourceAttribute")) 
					resource.setAttributes(processResourceAttributes());
			}
			reader.readNext();
			if (reader.name().equalsIgnoreCase("noteresource") && reader.isEndElement())
				atEnd = true;
		}
		
		conn.getNoteTable().noteResourceTable.saveNoteResource(resource, isDirty);

		
		return resource;
	}
	
	private Data processData(String nodeName) {
		Data data = new Data();
		boolean atEnd = false;
		while(!atEnd) {
			if (reader.isStartElement()) {
				if (reader.name().equalsIgnoreCase("Size")) 
					data.setSize(intValue());
				if (reader.name().equalsIgnoreCase("Body")) {
					byte[] b = textValue().getBytes();   // data binary
					QByteArray hexData = new QByteArray(b);
					QByteArray binData = new QByteArray(QByteArray.fromHex(hexData));
					data.setBody(binData.toByteArray());
				}
				if (reader.name().equalsIgnoreCase("BodyHash")) {
					byte[] b = textValue().getBytes();   // data binary
					QByteArray hexData = new QByteArray(b);
					QByteArray binData = new QByteArray(QByteArray.fromHex(hexData));
					data.setBodyHash(binData.toByteArray());
				}

				reader.readNext();
				if (reader.name().equalsIgnoreCase("data") && reader.isEndElement())
					atEnd = true;
			}
			reader.readNext();
			if (reader.name().equalsIgnoreCase(nodeName) && reader.isEndElement())
				atEnd = true;
		}
		return data;
	}

	private ResourceAttributes processResourceAttributes() {
		ResourceAttributes attributes = new ResourceAttributes();
		boolean atEnd = false;
		while(!atEnd) {
			if (reader.isStartElement()) {
				if (reader.name().equalsIgnoreCase("CameraMake")) 
					attributes.setCameraMake(textValue());		
				if (reader.name().equalsIgnoreCase("CameraModel")) 
					attributes.setCameraModel(textValue());		
				if (reader.name().equalsIgnoreCase("FileName")) 
					attributes.setFileName(textValue());		
				if (reader.name().equalsIgnoreCase("RecoType")) 
					attributes.setRecoType(textValue());		
				if (reader.name().equalsIgnoreCase("CameraModel")) 
					attributes.setCameraMake(textValue());		
				if (reader.name().equalsIgnoreCase("SourceURL")) 
					attributes.setSourceURL(textValue());		
				if (reader.name().equalsIgnoreCase("Altitude")) 
					attributes.setAltitude(doubleValue());		
				if (reader.name().equalsIgnoreCase("Longitude")) 
					attributes.setLongitude(doubleValue());		
				if (reader.name().equalsIgnoreCase("Latitude")) 
					attributes.setLatitude(doubleValue());		
				if (reader.name().equalsIgnoreCase("Timestamp")) 
					attributes.setTimestamp(longValue());		
				if (reader.name().equalsIgnoreCase("Attachment")) 
					attributes.setAttachment(booleanValue());		
				if (reader.name().equalsIgnoreCase("ClientWillIndex")) 
					attributes.setClientWillIndex(booleanValue());		
			}
			reader.readNext();
			if (reader.name().equalsIgnoreCase("noteresourceattribute") && reader.isEndElement())
				atEnd = true;
		}
		
		return attributes;
	}

	
	private List<String> processNoteTagList() {
		List<String> guidList = new ArrayList<String>();
	

		boolean atEnd = false;
		while(!atEnd) {			
			if (reader.isStartElement()) {
				if (reader.name().equalsIgnoreCase("guid")) 
					guidList.add(textValue());
			}
			reader.readNext();
			if (reader.name().equalsIgnoreCase("notetags") && reader.isEndElement())
				atEnd = true;
		}
			return guidList;
	}



	private NoteAttributes processNoteAttributes() {
	NoteAttributes attributes = new NoteAttributes();
	
	boolean atEnd = false;
	while(!atEnd) {
		if (reader.isStartElement()) {
			if (reader.name().equalsIgnoreCase("Author")) 
				attributes.setAuthor(textValue());
			if (reader.name().equalsIgnoreCase("SourceURL")) 
				attributes.setSourceURL(textValue());
			if (reader.name().equalsIgnoreCase("Source")) 
				attributes.setSource(textValue());
			if (reader.name().equalsIgnoreCase("SourceApplication")) 
				attributes.setSourceApplication(textValue());
			if (reader.name().equalsIgnoreCase("Altitude")) 
				attributes.setAltitude(doubleValue());
			if (reader.name().equalsIgnoreCase("Longitude")) 
				attributes.setLongitude(doubleValue());
			if (reader.name().equalsIgnoreCase("Latitude")) 
				attributes.setLatitude(doubleValue());
			if (reader.name().equalsIgnoreCase("SubjectDate")) 
				attributes.setSubjectDate(longValue());
		}
		reader.readNext();
		if (reader.name().equalsIgnoreCase("noteattributes") && reader.isEndElement())
			atEnd = true;
	}
	
	return attributes;
}

	
	private void processSynchronizationNode() {
		boolean atEnd = false;
		while(!atEnd) {
			if (reader.isStartElement()) {
				if (reader.name().equalsIgnoreCase("UpdateSequenceNumber")) 	
					highUpdateSequenceNumber = intValue();
				if (reader.name().equalsIgnoreCase("LastSequenceDate")) 	
					lastSequenceDate = longValue();
			}
			reader.readNext();
			if (reader.name().equalsIgnoreCase("synchronization") && reader.isEndElement())
				atEnd = true;
		}		
	}
	
	
	private void processSavedSearchNode() {
		search = new SavedSearch();
		searchIsDirty = false;
		
		boolean atEnd = false;
		while(!atEnd) {
			if (reader.isStartElement()) {
				if (reader.name().equalsIgnoreCase("Guid")) 
					search.setGuid(textValue());
				if (reader.name().equalsIgnoreCase("Name")) 
					search.setName(textValue());
				if (reader.name().equalsIgnoreCase("UpdateSequenceNumber")) 
					search.setUpdateSequenceNum(intValue());
				if (reader.name().equalsIgnoreCase("Query")) 
					search.setQuery(textValue());
				if (reader.name().equalsIgnoreCase("Dirty")) {
					if (booleanValue())
						searchIsDirty = true;
				}
			}
			reader.readNext();
			if (reader.name().equalsIgnoreCase("savedsearch") && reader.isEndElement())
				atEnd = true;		}	
		return;
	}
	
	
	private void processLinkedNotebookNode() {
		linkedNotebook = new LinkedNotebook();
		linkedNotebookIsDirty = false;
		
		boolean atEnd = false;
		while(!atEnd) {
			if (reader.isStartElement()) {
				if (reader.name().equalsIgnoreCase("Guid")) 
					linkedNotebook.setGuid(textValue());
				if (reader.name().equalsIgnoreCase("ShardID")) 
					linkedNotebook.setShardId(textValue());
				if (reader.name().equalsIgnoreCase("UpdateSequenceNumber")) 
					linkedNotebook.setUpdateSequenceNum(intValue());
				if (reader.name().equalsIgnoreCase("ShareKey")) 
					linkedNotebook.setShareKey(textValue());
				if (reader.name().equalsIgnoreCase("ShareName")) 
					linkedNotebook.setShareName(textValue());
				if (reader.name().equalsIgnoreCase("Uri")) 
					linkedNotebook.setUri(textValue());
				if (reader.name().equalsIgnoreCase("Username")) 
					linkedNotebook.setUsername(textValue());
				if (reader.name().equalsIgnoreCase("Dirty")) {
					if (booleanValue())
						linkedNotebookIsDirty = true;
				}
			}
			reader.readNext();
			if (reader.name().equalsIgnoreCase("LinkedNotebook") && reader.isEndElement())
				atEnd = true;		}	
		return;
	}


	private void processSharedNotebookNode() {
		sharedNotebook = new SharedNotebook();
		sharedNotebookIsDirty = false;
		
		boolean atEnd = false;
		while(!atEnd) {
			if (reader.isStartElement()) {
				if (reader.name().equalsIgnoreCase("Id")) 
					sharedNotebook.setId(intValue());
				if (reader.name().equalsIgnoreCase("Userid")) 
					sharedNotebook.setUserId(intValue());
				if (reader.name().equalsIgnoreCase("Email")) 
					sharedNotebook.setEmail(textValue());
				if (reader.name().equalsIgnoreCase("NotebookGuid")) 
					sharedNotebook.setNotebookGuid(textValue());
				if (reader.name().equalsIgnoreCase("ShareKey")) 
					sharedNotebook.setShareKey(textValue());
				if (reader.name().equalsIgnoreCase("Username")) 
					sharedNotebook.setUsername(textValue());
				if (reader.name().equalsIgnoreCase("ServiceCreated")) 
					sharedNotebook.setServiceCreated(longValue());
				if (reader.name().equalsIgnoreCase("Dirty")) {
					if (booleanValue())
						sharedNotebookIsDirty = true;
				}
			}
			reader.readNext();
			if (reader.name().equalsIgnoreCase("LinkedNotebook") && reader.isEndElement())
				atEnd = true;		}	
		return;
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
			if (backup || importNotebooks) {
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
			if (backup || importTags) {
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
	private double doubleValue() {
		return new Double(textValue());
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
