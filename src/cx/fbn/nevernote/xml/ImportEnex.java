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
import java.util.List;

import javax.xml.bind.DatatypeConverter;

import com.evernote.edam.type.Data;
import com.evernote.edam.type.Note;
import com.evernote.edam.type.NoteAttributes;
import com.evernote.edam.type.Resource;
import com.evernote.edam.type.ResourceAttributes;
import com.evernote.edam.type.Tag;
import com.trolltech.qt.core.QByteArray;
import com.trolltech.qt.core.QFile;
import com.trolltech.qt.core.QIODevice;
import com.trolltech.qt.core.QUuid;
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
	public int							highUpdateSequenceNumber;
	public long							lastSequenceDate;
	private final ApplicationLogger 	logger;
	private String						notebookGuid;
	public final boolean				importTags = false;
	public final boolean				importNotebooks = false;
	private String newGuid;
	List<Tag> tags;
	public boolean createNewTags;
	
	public ImportEnex(DatabaseConnection c, boolean full) {
		logger = new ApplicationLogger("import.log");
		conn = c;
		tags = conn.getTagTable().getAll();
		createNewTags = true;
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
				if (note.getUpdated() == 0) {
					note.setUpdated(note.getCreated());
				}
				conn.getNoteTable().addNote(note, true);
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
			if (reader.name().equalsIgnoreCase("updated")) 
				note.setCreated(datetimeValue());
			if (reader.name().equalsIgnoreCase("Content")) 
				note.setContent(textValue());
			if (reader.name().equalsIgnoreCase("tag") && createNewTags) {
				String tag = textValue();
				Tag noteTag = null;
				boolean found=false;
				for (int i=0; i<tags.size(); i++) {
					if (tags.get(i).getName().equalsIgnoreCase(tag)) {
						found=true;
						noteTag = tags.get(i);
						i=tags.size();
					}
				}
				
				if (!found) {
					noteTag = new Tag();
					noteTag.setName(tag);
					String tagGuid = QUuid.createUuid().toString().replace("{", "").replace("}", "");
					noteTag.setGuid(tagGuid);
					noteTag.setName(tag);
					tags.add(noteTag);
					conn.getTagTable().addTag(noteTag, true);
				}
				note.addToTagNames(noteTag.getName());
				note.addToTagGuids(noteTag.getGuid());
			}
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
			if (reader.isStartElement() && reader.name().equalsIgnoreCase("resource")) {
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
			if (reader.name().equalsIgnoreCase("resource-attributes")) 
				resource.setAttributes(processResourceAttributes());
			if (reader.name().equalsIgnoreCase("recognition")) 
				resource.setRecognition(processRecognition());
			reader.readNext();
			if (reader.name().equalsIgnoreCase("resource") && reader.isEndElement())
				atEnd = true;
		}
		if (resource.getAttributes() == null) 
			resource.setAttributes(new ResourceAttributes());
		conn.getNoteTable().noteResourceTable.updateNoteResource(resource, true);
		return resource;
	}
	
	private Data processData(String nodeName) {
		Data data = new Data();
		boolean atEnd = false;
		while(!atEnd) {
			if (reader.isStartElement()) {
				try {
				byte[] b = textValue().getBytes();   // data binary
				if (b.length > 0) {
					QByteArray hexData = new QByteArray(b);
					String hexString = hexData.toString();
					data.setBody(DatatypeConverter.parseBase64Binary(hexString));
					MessageDigest md;
					try {
						md = MessageDigest.getInstance("MD5");
						md.update(data.getBody());
						data.setBodyHash(md.digest());
					} catch (NoSuchAlgorithmException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					
				}}
				catch (Exception e) {};
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
				if (reader.name().equalsIgnoreCase("longitude")) 
					attributes.setLongitude(doubleValue());
				if (reader.name().equalsIgnoreCase("latitude")) 
					attributes.setLatitude(doubleValue());
				if (reader.name().equalsIgnoreCase("altitude")) 
					attributes.setAltitude(doubleValue());
				if (reader.name().equalsIgnoreCase("author")) 
					attributes.setAuthor(textValue());
				if (reader.name().equalsIgnoreCase("subject-date")) 
					attributes.setSubjectDate(datetimeValue());
			}
			reader.readNext();
			if (reader.name().equalsIgnoreCase("note-attributes") && reader.isEndElement())
			atEnd = true;
		}
	
		return attributes;
	}

		
	
	private Data processRecognition() {
		Data reco = new Data();
		reco.setBody(textValue().getBytes());
		MessageDigest md;
		try {
			md = MessageDigest.getInstance("MD5");
			md.update(reco.getBody());
			reco.setBodyHash(md.digest());
			reco.setSize(reco.getBody().length);
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return reco;
	}
	
	private ResourceAttributes processResourceAttributes() {
		ResourceAttributes attributes = new ResourceAttributes();
		boolean atEnd = false;
		while(!atEnd) {
			if (reader.isStartElement()) {
				if (reader.name().equalsIgnoreCase("camera-model")) 
					attributes.setCameraModel(textValue());		
				if (reader.name().equalsIgnoreCase("file-name")) 
					attributes.setFileName(textValue());		
				if (reader.name().equalsIgnoreCase("reco-type")) 
					attributes.setRecoType(textValue());		
				if (reader.name().equalsIgnoreCase("camera-make")) 
					attributes.setCameraMake(textValue());		
				if (reader.name().equalsIgnoreCase("source-url")) 
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
			if (reader.name().equalsIgnoreCase("resource-attributes") && reader.isEndElement())
				atEnd = true;
		}
		
		return attributes;
	}
	
	
	private String textValue() {
		return reader.readElementText();
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

	private double doubleValue() {
		return new Double(textValue());
	}
}
