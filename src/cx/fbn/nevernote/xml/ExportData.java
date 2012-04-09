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

import com.evernote.edam.type.Data;
import com.evernote.edam.type.LinkedNotebook;
import com.evernote.edam.type.Note;
import com.evernote.edam.type.NoteAttributes;
import com.evernote.edam.type.Notebook;
import com.evernote.edam.type.Publishing;
import com.evernote.edam.type.Resource;
import com.evernote.edam.type.SavedSearch;
import com.evernote.edam.type.SharedNotebook;
import com.evernote.edam.type.Tag;
import com.trolltech.qt.core.QByteArray;
import com.trolltech.qt.core.QFile;
import com.trolltech.qt.core.QIODevice;
import com.trolltech.qt.xml.QXmlStreamWriter;

import cx.fbn.nevernote.Global;
import cx.fbn.nevernote.evernote.NoteMetadata;
import cx.fbn.nevernote.sql.DatabaseConnection;
import cx.fbn.nevernote.utilities.ApplicationLogger;

public class ExportData {
	
	private List<Notebook> 						notebooks;
	private final HashMap<String,String>		localNotebooks;
	private final HashMap<String,String>		dirtyNotebooks;
	private final ApplicationLogger				logger;
	
	private List<SavedSearch> 					searches;
	private List<LinkedNotebook>				linkedNotebooks;
	private List<SharedNotebook>				sharedNotebooks;
	private final HashMap<String,String> 		dirtySearches;

	private List<Tag> 							tags;
	private final HashMap<String,String>		dirtyTags;

	private final HashMap<String, String>		exportableNotebooks;
	private final HashMap<String, String>		exportableTags;
	private List<Note>							notes;
	private final HashMap<String,String>		dirtyNotes;
	private final HashMap<String,String>		dirtyLinkedNotebooks;
	private final HashMap<Long,String>			dirtySharedNotebooks;
	private HashMap<String, NoteMetadata>			noteMeta;
	private final boolean 						fullBackup;
	private final DatabaseConnection 			conn;
	private QXmlStreamWriter					writer;		
	
	private String								errorMessage;
	public int									lastError;
	
	public ExportData(DatabaseConnection conn2, boolean full) {
		conn = conn2;
		logger = new ApplicationLogger("export.log");
		notebooks = new ArrayList<Notebook>();
		tags = new ArrayList<Tag>();
		notes = new ArrayList<Note>();
		sharedNotebooks = new ArrayList<SharedNotebook>();
		linkedNotebooks = new ArrayList<LinkedNotebook>();
		dirtyNotebooks = new HashMap<String,String>();
		localNotebooks = new HashMap<String,String>();
		dirtyLinkedNotebooks = new HashMap<String,String>();
		dirtySharedNotebooks = new HashMap<Long,String>();
		dirtyTags = new HashMap<String,String>();
		fullBackup = full;
		
		dirtyNotes = new HashMap<String, String>();
		dirtySearches = new HashMap<String, String>();
		searches = new ArrayList<SavedSearch>();
		
		exportableNotebooks = new HashMap<String, String>();
		exportableTags = new HashMap<String, String>();
	}
	
	
	public ExportData(DatabaseConnection conn2, boolean full, List<String> guids) {
		conn = conn2;
		logger = new ApplicationLogger("export.log");
		notebooks = new ArrayList<Notebook>();
		tags = new ArrayList<Tag>();
		notes = new ArrayList<Note>();
		for (int i=0; i<guids.size(); i++) {
			notes.add(conn.getNoteTable().getNote(guids.get(i), true, true, true, true, true));
		}
		dirtyNotebooks = new HashMap<String,String>();
		localNotebooks = new HashMap<String,String>();
		dirtyTags = new HashMap<String,String>();
		fullBackup = full;
		
		dirtyNotes = new HashMap<String, String>();
		dirtyLinkedNotebooks = new HashMap<String,String>();
		dirtySharedNotebooks = new HashMap<Long,String>();
		dirtySearches = new HashMap<String, String>();
		searches = new ArrayList<SavedSearch>();
		
		exportableNotebooks = new HashMap<String, String>();
		exportableTags = new HashMap<String, String>();
	}
	
	
	public void exportData(String filename) {
		
    	notebooks = conn.getNotebookTable().getAll();   	
    	tags = conn.getTagTable().getAll();
    	List<Notebook> books = conn.getNotebookTable().getAllLocal();
    	for (int i=0; i<books.size(); i++) {
    		localNotebooks.put(books.get(i).getGuid(), "");
    	}
    	
    	books = conn.getNotebookTable().getDirty();
    	for (int i=0; i<books.size(); i++) {
    		dirtyNotebooks.put(books.get(i).getGuid(), "");
    	}

    	List<Tag> d= conn.getTagTable().getDirty();
    	for (int i=0; i<d.size(); i++) {
    		dirtyTags.put(d.get(i).getGuid(), "");
    	}
    	
    	if (fullBackup)
    		notes = conn.getNoteTable().getAllNotes();
    	
    	List<Note> dn = conn.getNoteTable().getDirty();
    	for (int i=0; i<dn.size(); i++) {
    		dirtyNotes.put(dn.get(i).getGuid(), "");
    	}
    	
    	noteMeta = conn.getNoteTable().getNotesMetaInformation();

    	
    	searches = conn.getSavedSearchTable().getAll();
    	
    	List<SavedSearch> ds = conn.getSavedSearchTable().getDirty();
    	for (int i=0; i<ds.size(); i++) {
    		dirtySearches.put(ds.get(i).getGuid(), "");
    	}
		
    	linkedNotebooks = conn.getLinkedNotebookTable().getAll();
    	List<String> dln = conn.getLinkedNotebookTable().getDirtyGuids();
    	for (int i=0; i<dln.size(); i++) {
    		dirtyLinkedNotebooks.put(dln.get(i), "");
    	}
    	
    	sharedNotebooks = conn.getSharedNotebookTable().getAll();
    	List<Long> dsn = conn.getSharedNotebookTable().getDirtyIds();
    	for (int i=0; i<dsn.size(); i++) {
    		dirtySharedNotebooks.put(dsn.get(i), "");
    	}
		
		lastError = 0;
		errorMessage = "";
		QFile xmlFile = new QFile(filename);
		if (!xmlFile.open(QIODevice.OpenModeFlag.WriteOnly, QIODevice.OpenModeFlag.Truncate)) {
			lastError = 16;
			errorMessage = "Cannot open file.";
		}
			
		writer = new QXmlStreamWriter(xmlFile);	
		writer.setAutoFormatting(true);
		writer.setCodec("UTF-8");
		writer.writeStartDocument();
		writer.writeDTD("<!DOCTYPE NeverNote-Export>");
		writer.writeStartElement("nevernote-export");
		writer.writeAttribute("version", "0.95");
		if (fullBackup)
			writer.writeAttribute("exportType", "backup");
		else
			writer.writeAttribute("exportType", "export");			
		writer.writeAttribute("application", "NeverNote");
		writer.writeAttribute("applicationVersion", Global.version);
		if (fullBackup) {
			writer.writeStartElement("Synchronization");
			long sequenceDate = conn.getSyncTable().getLastSequenceDate();
			int number = conn.getSyncTable().getUpdateSequenceNumber();
			createTextNode("UpdateSequenceNumber", new Long(number).toString());
			createTextNode("LastSequenceDate", new Long(sequenceDate).toString());
			writer.writeEndElement();
		}
		
		for (int i=0; i<notes.size(); i++) {
			String guid = notes.get(i).getGuid();
			logger.log(logger.EXTREME, "Getting note " +guid +" : " +notes.get(i).getTitle());
			Note note = conn.getNoteTable().getNote(guid, true, true, true, true, true);
			logger.log(logger.EXTREME, "Writing note XML");
			writeNote(note);
		}
		
		writeNotebooks();
		writeTags();
		writeSavedSearches();
		writeLinkedNotebooks();
		writeSharedNotebooks();

		
		writer.writeEndElement();
		writer.writeEndDocument();

		
		
		writer.dispose();

		
		xmlFile.close();
		xmlFile.dispose();

	}
	
	
	private void writeSavedSearches() {
		if (!fullBackup)
			return;
		for (int i=0; i<searches.size(); i++) {
			writer.writeStartElement("SavedSearch");
			createTextNode("Guid", searches.get(i).getGuid());
			createTextNode("Name", searches.get(i).getName());
			createTextNode("Query", searches.get(i).getQuery());
			createTextNode("Format", new Integer(searches.get(i).getFormat().getValue()).toString());
			if (dirtySearches.containsKey(searches.get(i).getGuid()))
				createTextNode("Dirty","true");
			else
				createTextNode("Dirty","false");
			writer.writeEndElement();
		}
	}
	
	
	private void writeLinkedNotebooks() {
		if (!fullBackup)
			return;
		for (int i=0; i<linkedNotebooks.size(); i++) {
			writer.writeStartElement("LinkedNotebook");
			createTextNode("Guid", linkedNotebooks.get(i).getGuid());
			createTextNode("ShardID", linkedNotebooks.get(i).getShardId());
			createTextNode("ShareKey", linkedNotebooks.get(i).getShareKey());
			createTextNode("ShareName", linkedNotebooks.get(i).getShareName());
			createTextNode("Uri", linkedNotebooks.get(i).getUri());
			createTextNode("Username", linkedNotebooks.get(i).getUsername());
			createTextNode("UpdateSequenceNumber", new Long(linkedNotebooks.get(i).getUpdateSequenceNum()).toString());
			if (dirtyLinkedNotebooks.containsKey(linkedNotebooks.get(i).getGuid()))
				createTextNode("Dirty", "true");
			else
				createTextNode("Dirty", "false");
			writer.writeEndElement();
		}
	}

		
	
	private void writeSharedNotebooks() {
		if (!fullBackup)
			return;
		for (int i=0; i<linkedNotebooks.size(); i++) {
			writer.writeStartElement("SharedNotebook");
			createTextNode("Id", new Long(sharedNotebooks.get(i).getId()).toString());
			createTextNode("Userid", new Integer(sharedNotebooks.get(i).getUserId()).toString());
			createTextNode("Email", sharedNotebooks.get(i).getEmail());
			createTextNode("NotebookGuid", sharedNotebooks.get(i).getNotebookGuid());
			createTextNode("ShareKey", sharedNotebooks.get(i).getShareKey());
			createTextNode("Username", sharedNotebooks.get(i).getUsername());
			createTextNode("ServiceCreated", new Long(sharedNotebooks.get(i).getServiceCreated()).toString());
			if (dirtySharedNotebooks.containsKey(sharedNotebooks.get(i).getId()))
				createTextNode("Dirty", "true");
			else
				createTextNode("Dirty", "false");
			writer.writeEndElement();
		}
	}


	
	private void writeNote(Note note) {
		
		writer.writeStartElement("Note");
		createTextNode("Guid", note.getGuid());
		createTextNode("UpdateSequenceNumber", new Long(note.getUpdateSequenceNum()).toString());
		createTextNode("Title", note.getTitle());
		createTextNode("Created", new Long(note.getCreated()).toString());
		createTextNode("Updated", new Long(note.getUpdated()).toString());
		createTextNode("Deleted", new Long(note.getDeleted()).toString());
		createTextNode("Active", new Boolean(note.isActive()).toString());
		createTextNode("NotebookGuid", note.getNotebookGuid());
		if (dirtyNotes.containsKey(note.getGuid()))
			createTextNode("Dirty", "true");
		else
			createTextNode("Dirty", "false");
		if (noteMeta.containsKey(note.getGuid())) {
			Integer color = new Integer(noteMeta.get(note.getGuid()).getColor());
			createTextNode("TitleColor", color.toString());
		}
		exportableNotebooks.put(note.getNotebookGuid(), "");
		
		if (note.getTagGuidsSize() > 0) {
			writer.writeStartElement("NoteTags");
			for (int i=0; i<note.getTagGuidsSize(); i++) {
				createTextNode("Guid", note.getTagGuids().get(i));
				exportableTags.put(note.getTagGuids().get(i), "");
			}
			writer.writeEndElement();
		}
		
		NoteAttributes noteAttributes = note.getAttributes();
		if (noteAttributes != null) {
			writer.writeStartElement("NoteAttributes");
			createTextNode("Author", noteAttributes.getAuthor());
			createTextNode("Source", noteAttributes.getSource());
			createTextNode("SourceApplication", noteAttributes.getSourceApplication());
			createTextNode("SourceURL", noteAttributes.getSourceURL());
			createTextNode("Altitude", new Double(noteAttributes.getAltitude()).toString());
			createTextNode("Longitude", new Double(noteAttributes.getLongitude()).toString());
			createTextNode("Latitude", new Double(noteAttributes.getLatitude()).toString());
			createTextNode("SubjectDate", new Long(noteAttributes.getSubjectDate()).toString());
			writer.writeEndElement();
		}
		
//		writeResources(conn.getNoteTable().noteResourceTable.getNoteResources(note.getGuid(), true));
		writeResources(note.getResources());
		
		logger.log(logger.EXTREME, "Writing content");
		writer.writeStartElement("Content");
		writer.writeCDATA(conn.getNoteTable().getNoteContentNoUTFConversion(note.getGuid()));
		writer.writeEndElement();
		writer.writeEndElement();
	}

	
	private void writeResources(List<Resource> resourceTable) {
		Resource resource;
		if (resourceTable.size() == 0)
			return;
		for (int i=0; i<resourceTable.size(); i++) {
			resource = resourceTable.get(i);
			writer.writeStartElement("NoteResource");
			createTextNode("Guid", resource.getGuid());
			createTextNode("NoteGuid", resource.getNoteGuid());
			createTextNode("UpdateSequenceNumber", new Integer(resource.getUpdateSequenceNum()).toString());
			createTextNode("Mime", resource.getMime());
			createTextNode("Duration", new Integer(resource.getDuration()).toString());
			createTextNode("Height", new Integer(resource.getHeight()).toString());
			createTextNode("Width", new Integer(resource.getWidth()).toString());
			logger.log(logger.EXTREME, "Checking for data node");
			if (resource.getData() != null)
				writeDataNode("Data", resource.getData());
			logger.log(logger.EXTREME, "Checking for alternate data node");
			if (resource.getAlternateData() != null)
				writeDataNode("AlternateData", resource.getAlternateData());
			logger.log(logger.EXTREME, "Checking for recognition");
			if (resource.getRecognition() != null)
				writeRecognitionNode("Recognition", resource.getRecognition());
			if (resource.isActive())
				createTextNode("Active", "true");
			else
				createTextNode("Active", "false");
				logger.log(logger.EXTREME, "Checking resource attributes");
			if (resource.getAttributes() != null) {
				writer.writeStartElement("NoteResourceAttribute");
				createTextNode("CameraMake", resource.getAttributes().getCameraMake());
				createTextNode("CameraModel", resource.getAttributes().getCameraModel());
				createTextNode("FileName", resource.getAttributes().getFileName());
				createTextNode("RecoType", resource.getAttributes().getRecoType());
				createTextNode("SourceURL", resource.getAttributes().getSourceURL());
				createTextNode("Altitude", new Double(resource.getAttributes().getAltitude()).toString());
				createTextNode("Longitude", new Double(resource.getAttributes().getLongitude()).toString());
				createTextNode("Latitude", new Double(resource.getAttributes().getLatitude()).toString());
				createTextNode("Timestamp", new Long(resource.getAttributes().getTimestamp()).toString());
				if (resource.getAttributes().isAttachment())
					createTextNode("Attachment", "true");
				else	
					createTextNode("Attachment", "false");
				if (resource.getAttributes().isClientWillIndex())
					createTextNode("ClientWillIndex", "true");
				else
					createTextNode("ClientWillIndex", "false");
				writer.writeEndElement();
			}
			writer.writeEndElement();
		}
		logger.log(logger.EXTREME, "Ending resource node");
//		writer.writeEndElement();
	}
	
	
	private void writeDataNode(String name, Data data) {
		writer.writeStartElement(name);
		createTextNode("Size", new Integer(data.getSize()).toString());
		if (data.getBody() != null && data.getBody().length > 0)
			createBinaryNode("Body", new QByteArray(data.getBody()).toHex().toString());
		else
			createBinaryNode("Body", "");
		if (data.getBodyHash() != null && data.getBodyHash().length > 0)
			createTextNode("BodyHash", new QByteArray(data.getBodyHash()).toHex().toString());
		else
			createTextNode("BodyHash", "");
		writer.writeEndElement();		
	}

	
	
	
	private void writeRecognitionNode(String name, Data data) {
		writer.writeStartElement(name);
		createTextNode("Size", new Integer(data.getSize()).toString());
		if (data.getBody() != null && data.getBody().length > 0) {
			writer.writeStartElement("Body");
			writer.writeCDATA(new QByteArray(data.getBody()).toString());
//			writer.writeCDATA(new QByteArray(data.getBody()).toHex().toString());
			writer.writeEndElement();
		}  else
			createBinaryNode("Body", "");
		
		if (data.getBodyHash() != null && data.getBodyHash().length > 0)
			createTextNode("BodyHash", new QByteArray(data.getBodyHash()).toHex().toString());
		else
			createTextNode("BodyHash", "");
		writer.writeEndElement();		
	}

	
	private void writeNotebooks() {
		for (int i=0; i<notebooks.size(); i++) {
			if (exportableNotebooks.containsKey(notebooks.get(i).getGuid()) || fullBackup) {
				writer.writeStartElement("Notebook");
				createTextNode("Guid", notebooks.get(i).getGuid());
				createTextNode("Name", notebooks.get(i).getName());
				createTextNode("UpdateSequenceNumber", new Long(notebooks.get(i).getUpdateSequenceNum()).toString());
				if (notebooks.get(i).isDefaultNotebook())
					createTextNode("DefaultNotebook", "true");
				else
					createTextNode("DefaultNotebook", "false");
				createTextNode("ServiceCreated", new Long(notebooks.get(i).getServiceCreated()).toString());
				createTextNode("ServiceUpdated", new Long(notebooks.get(i).getServiceUpdated()).toString());
				if (localNotebooks.containsKey(notebooks.get(i).getGuid()))
					createTextNode("Local","true");
				else
					createTextNode("Local","false");
				if (dirtyNotebooks.containsKey(notebooks.get(i).getGuid()))
					createTextNode("Dirty","true");
				else
					createTextNode("Dirty","false");
				if (conn.getNotebookTable().isReadOnly(notebooks.get(i).getGuid()))
					createTextNode("ReadOnly", "true");
				else
					createTextNode("ReadOnly", "false");
				if (notebooks.get(i).getPublishing() != null) {
					Publishing p = notebooks.get(i).getPublishing();
					createTextNode("PublishingPublicDescription", p.getPublicDescription());
					createTextNode("PublishingUri", p.getUri());
					createTextNode("PublishingOrder", new Integer(p.getOrder().getValue()).toString());
					if (p.isAscending())
						createTextNode("PublishingAscending", "true");
					else
						createTextNode("PublishingAscending", "false");
				}
				QByteArray b = conn.getNotebookTable().getIconAsByteArray(notebooks.get(i).getGuid());
				if (b != null) 
					createBinaryNode("Icon", b.toHex().toString());
				if (notebooks.get(i).getStack() != null && !notebooks.get(i).getStack().trim().equals(""))
					createTextNode("Stack", notebooks.get(i).getStack());
				writer.writeEndElement();	
			}
		}
	}


	private void writeTags() {
		for (int i=0; i<tags.size(); i++) {
			if (exportableTags.containsKey(tags.get(i).getGuid()) || fullBackup) {
				writer.writeStartElement("Tag");
				createTextNode("Guid", tags.get(i).getGuid());
				createTextNode("Name", tags.get(i).getName());
				createTextNode("ParentGuid", tags.get(i).getParentGuid());
				createTextNode("UpdateSequenceNumber", new Long(tags.get(i).getUpdateSequenceNum()).toString());
				if (dirtyTags.containsKey(tags.get(i).getGuid()))
					createTextNode("Dirty","true");
				else
					createTextNode("Dirty","false");
				writer.writeEndElement();	
			}
		}
	}
	
	
	private void createTextNode(String nodeName, String value) {
		if (value == null)
			value = "";
		writer.writeTextElement(nodeName, value);
		return;
	}

	private void createBinaryNode(String nodeName, String value) {
		if (value == null)
			value = "";
		logger.log(logger.EXTREME, "Writing binary node");
		writer.writeStartElement(nodeName);
/*		int i=0;
		for (; i<value.length(); i+=80) 
		{
			writer.writeCharacters("\n"+value.substring(i,i+80));
		}
		writer.writeCharacters("\n"+value.substring(i)+"\n");
*/		writer.writeCharacters(value);
		writer.writeEndElement();
		return;
	}

	
	public String getErrorMessage() {
		return errorMessage;
	}
	
	
}
