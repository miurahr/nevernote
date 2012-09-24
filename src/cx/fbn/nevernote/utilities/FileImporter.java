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

package cx.fbn.nevernote.utilities;

import java.io.File;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

import com.evernote.edam.type.Data;
import com.evernote.edam.type.Note;
import com.evernote.edam.type.NoteAttributes;
import com.evernote.edam.type.Resource;
import com.evernote.edam.type.ResourceAttributes;
import com.trolltech.qt.core.QByteArray;
import com.trolltech.qt.core.QFile;
import com.trolltech.qt.core.QFileInfo;
import com.trolltech.qt.core.QIODevice;
import com.trolltech.qt.webkit.QWebPage;

import cx.fbn.nevernote.Global;
import cx.fbn.nevernote.NeverNote;
import cx.fbn.nevernote.sql.DatabaseConnection;



public class FileImporter  {

	private final ApplicationLogger	logger;
	private String				fileName;
	private String 				content;
	private QFileInfo 			fileInfo;
	private Note				newNote;
	private String				noteString;
	private final DatabaseConnection	conn;
	
	
	public FileImporter(ApplicationLogger l, DatabaseConnection c) {
		logger = l;
		conn = c;
	}
	
	
	//********************************************************
	//* Begin to import the file
	//********************************************************
	public boolean importFile() {
		if (fileInfo.isFile()) {
			if (fileInfo.completeSuffix().equalsIgnoreCase("txt"))
				return importTextFile();
			return importAttachment();
		}
		return false;
	}
	
	//********************************************************
	//* Import text files 
	//********************************************************
	public boolean importTextFile() {
		QFile file = new QFile(fileName);
		if (!file.open(new QIODevice.OpenMode(QIODevice.OpenModeFlag.ReadOnly))) {
			// If we can't get to the file, it is probably locked.  We'll try again later.
			logger.log(logger.LOW, "Unable to save externally edited file - locked.  Saving for later.");
			return false;
		}
		QByteArray binData = file.readAll();
		file.close();
		if (binData.size() == 0) {
			// If we can't get to the file, it is probably locked.  We'll try again later.
			logger.log(logger.LOW, "Unable to save externally edited file - zero size.  Saving for later.");
			return false;
		}
	
		String fileData =binData.toString();
		
		// Fix \r\n & \n and turn inte HTML
		QWebPage page = new QWebPage();
		page.mainFrame().setContent(binData);
		fileData = page.mainFrame().toHtml();
		fileData = fileData.replace("\n", "<br>\n");

		noteString = new String("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
				"<!DOCTYPE en-note SYSTEM \"http://xml.evernote.com/pub/enml2.dtd\">\n" +
				"<en-note>\n<br clear=\"none\" />"+fileData+"</en-note>");

		noteString = fileData;
		buildEmptyNote();
    	newNote.setAttributes(new NoteAttributes());
	    		
	    return true;
	}

	//********************************************************
	//* Import an image
	//********************************************************
	public boolean importAttachment() {
		QFile file = new QFile(fileName);
		if (!file.open(new QIODevice.OpenMode(QIODevice.OpenModeFlag.ReadOnly))) {
			// If we can't get to the file, it is probably locked.  We'll try again later.
			logger.log(logger.LOW, "Unable to save externally edited file - locked.  Saving for later.");
			return false;
		}
		QByteArray binData = file.readAll();
		file.close();
		if (binData.size() == 0) {
			// If we can't get to the file, it is probably locked.  We'll try again later.
			logger.log(logger.LOW, "Unable to save externally edited file - zero size.  Saving for later.");
			return false;
		}
			
		buildEmptyNote();
    	newNote.setAttributes(new NoteAttributes());
    
    	// Start building the resource itself
    	StringBuffer buffer = new StringBuffer();
    	buffer.append("<html><head></head><body>\n");
    	
    	Resource newRes;
    	
    	// If we have an image
    	if (isImage(fileInfo.completeSuffix())) {
        	String mimeType = "image/" +fileInfo.completeSuffix();
        	if (mimeType.equals("image/jpg"))
        		mimeType = "image/jpeg";
        	newRes = createResource(mimeType, false);
        	
    		buffer.append("<img src=\"" +fileName);
    		buffer.append("\" en-tag=\"en-media\" type=\"" + mimeType +"\""
    				+" hash=\""+Global.byteArrayToHexString(newRes.getData().getBodyHash()) +"\""
    				+" guid=\"" +newRes.getGuid() +"\">");
    	} else {
    		// We have an attachment, not an image.
    		
    		
        	String mimeType = "application/" +fileInfo.completeSuffix();
        	newRes = createResource(mimeType, false);
        	newRes.getAttributes().setFileName(fileInfo.fileName());
    		
        	String icon = findIcon(fileInfo.completeSuffix());
		String imageURL = FileUtils.toFileURLString(Global.getFileManager().getImageDirFile(icon));
    		buffer.append("<a en-tag=\"en-media\" guid=\"" +newRes.getGuid()+"\" ");
			buffer.append("type=\"" + mimeType + "\" href=\"nnres://" + fileName +"\" hash=\""+Global.byteArrayToHexString(newRes.getData().getBodyHash()) +"\" >");
			buffer.append("<img src=\"" + imageURL +"\" title=\"" +newRes.getAttributes().getFileName());
			buffer.append("\">");
			buffer.append("</a>");
    	}
		buffer.append("</body></html>");
		
		content = buffer.toString();
		noteString = buffer.toString();
		newNote.setContent(content);
		newNote.setResources(new ArrayList<Resource>());
		newNote.getResources().add(newRes);
		newNote.setResourcesIsSet(true);
    	
    	return true;
	}
	
	
	//********************************************************
	//* Check if we found an image
	//********************************************************
	// Check if the account supports this type of attachment
	private boolean isImage(String type) {
		if (type.equalsIgnoreCase("JPG"))
			return true;
		if (type.equalsIgnoreCase("JPEG"))
			return true;
		if (type.equalsIgnoreCase("PNG"))
			return true;
		if (type.equalsIgnoreCase("GIF"))
			return true;
		if (!Global.isPremium()) 
			return false;
		if (type.equalsIgnoreCase("XPM"))
			return true;
		if (type.equalsIgnoreCase("BMP"))
			return true;

		return false;
	}

	//********************************************************
	//* Build an empty note
	//********************************************************
	private void buildEmptyNote() {
		Calendar currentTime = new GregorianCalendar();
		String baseNote = new String("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
				"<!DOCTYPE en-note SYSTEM \"http://xml.evernote.com/pub/enml2.dtd\">\n" +
				"<en-note>\n<br clear=\"none\" /></en-note>");
		Long l = new Long(currentTime.getTimeInMillis());
		long prevTime = l;
		while (prevTime==l) {
			currentTime = new GregorianCalendar();
			l=currentTime.getTimeInMillis();
		}
		
		String randint = new String(Long.toString(l));
		
    	newNote = new Note();
    	newNote.setUpdateSequenceNum(0);
    	newNote.setGuid(randint);
    	newNote.setTitle(fileName);
    	newNote.setContent(baseNote);
    	newNote.setDeleted(0);
    	int cdate = fileInfo.created().toTime_t();
    	int mdate = fileInfo.lastModified().toTime_t();
    	newNote.setCreated((long)cdate*1000);
    	newNote.setUpdated((long)mdate*1000);
    	newNote.setActive(true);
    	NoteAttributes na = new NoteAttributes();
    	na.setLatitude(0.0);
    	na.setLongitude(0.0);
    	na.setAltitude(0.0);
	}
	//********************************************************
	//* Check valid file types
	//********************************************************
	public boolean isValidType() {
		if (!checkFileAttachmentSize(fileName))
			return false;
		return true;
	}
	//**************************************************************
	//* Check the file attachment to be sure it isn't over 25 mb
	//**************************************************************
	private boolean checkFileAttachmentSize(String url) {
		QFile resourceFile = new QFile(fileName);
		resourceFile.open(new QIODevice.OpenMode(
				QIODevice.OpenModeFlag.ReadOnly));
		long size = resourceFile.size();
		resourceFile.close();
		size = size / 1024 / 1024;
		if (size < 50 && Global.isPremium())
			return true;
		if (size < 25)
			return true;
		return false;
	}

	//********************************************************
	//* Build a new resource
	//********************************************************
	private Resource createResource(String mime, boolean attachment) {
		QFile resourceFile;
    	resourceFile = new QFile(fileName); 
    	resourceFile.open(new QIODevice.OpenMode(QIODevice.OpenModeFlag.ReadOnly));
    	byte[] fileData = resourceFile.readAll().toByteArray();
    	resourceFile.close();
    	if (fileData.length == 0)
    		return null;
    	MessageDigest md;
    	try {
    		md = MessageDigest.getInstance("MD5");
    		md.update(fileData);
    		byte[] hash = md.digest();
  
    		Resource r = new Resource();
    		Calendar time = new GregorianCalendar();
    		Long l = time.getTimeInMillis();
    		long prevTime = l;
    		while (l==prevTime) {
    			time = new GregorianCalendar();
    			l=time.getTimeInMillis();
    		}
    		r.setGuid(new Long(l).toString());
    		r.setNoteGuid(newNote.getGuid());
    		r.setMime(mime);
    		r.setActive(true);
    		r.setUpdateSequenceNum(0);
    		r.setWidth((short) 0);
    		r.setHeight((short) 0);
    		r.setDuration((short) 0);
    		  		
    		Data d = new Data();
    		d.setBody(fileData);
    		d.setBodyIsSet(true);
    		d.setBodyHash(hash);
    		d.setBodyHashIsSet(true);
    		r.setData(d);
    		d.setSize(fileData.length);
    		
    		ResourceAttributes a = new ResourceAttributes();
    		a.setAltitude(0);
    		a.setAltitudeIsSet(false);
    		a.setLongitude(0);
    		a.setLongitudeIsSet(false);
    		a.setLatitude(0);
    		a.setLatitudeIsSet(false);
    		a.setCameraMake("");
    		a.setCameraMakeIsSet(false);
    		a.setCameraModel("");
    		a.setCameraModelIsSet(false);
    		a.setAttachment(attachment);
    		a.setAttachmentIsSet(true);
    		a.setClientWillIndex(false);
    		a.setClientWillIndexIsSet(true);
    		a.setRecoType("");
    		a.setRecoTypeIsSet(false);
    		a.setSourceURLIsSet(false);
    		a.setTimestamp(0);
    		a.setTimestampIsSet(false);
    		a.setFileName(fileInfo.fileName());
    		a.setFileNameIsSet(true);
    		r.setAttributes(a);
    		
    		conn.getNoteTable().noteResourceTable.saveNoteResource(r, true);
    		return r;
    	} catch (NoSuchAlgorithmException e1) {
    		e1.printStackTrace();
		}
    	return null;
	}
	
	//********************************************************
	//* set file name
	//********************************************************
	public void setFileInfo(QFileInfo f) {
		fileInfo = f;		
	}
	//********************************************************
	//* Set file name
	//********************************************************
	public void setFileName(String f) {
		fileName = f;
	}
	//********************************************************
	//* Get file contents
	//********************************************************
	public String getContent() {
		return content;
	}
	//********************************************************
	//* Return the new note
	//********************************************************
	public Note getNote() {
		return newNote;
	}
	//********************************************************
	//* Get just the content of the new note
	//********************************************************
	public String getNoteContent() {
		return noteString;
	}

    /**
     * find the appropriate icon for an attachment
     * NFC TODO: duplicate of {@link NeverNote#findIcon(String)}
     */
    private String findIcon(String appl) {
        appl = appl.toLowerCase();
        String relativePath = appl + ".png";
        File f = Global.getFileManager().getImageDirFile(relativePath);
        if (f.exists()) {
            return relativePath;
        }
        return "attachment.png";
    }

}
