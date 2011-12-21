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

//Written by David Flanagan.  Copyright (c) 1996 O'Reilly & Associates.
//You may study, use, modify, and distribute this example for any purpose.
//This example is provided WITHOUT WARRANTY either expressed or implied.

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.evernote.edam.type.Data;
import com.evernote.edam.type.Resource;
import com.evernote.edam.type.ResourceAttributes;
import com.trolltech.qt.core.QUuid;

import cx.fbn.nevernote.sql.DatabaseConnection;

public class ImageFetcher {
	
	private Resource newResource;
	private final ApplicationLogger logger;
	private final DatabaseConnection conn;
	private final String noteGuid;
	
	
	public ImageFetcher(DatabaseConnection c, ApplicationLogger l, String g) {
		newResource = new Resource();
		logger = l;
		conn = c;
		noteGuid = g;
	}
	

 // Get the contents of a URL and return it as an image
 public boolean fetch(String address) 
     throws MalformedURLException, IOException 
 {
	 URL u = new URL(address);
	 URLConnection uc = u.openConnection();
	 String contentType = uc.getContentType();
	 int contentLength = uc.getContentLength();
	 if (contentType.startsWith("text/") || contentLength == -1) {
	     throw new IOException("This is not a binary file.");
	 }
	 InputStream raw = uc.getInputStream();
	 InputStream in = new BufferedInputStream(raw);
	 byte[] data = new byte[contentLength];
	 int bytesRead = 0;
	 int offset = 0;
	 while (offset < contentLength) {
	   bytesRead = in.read(data, offset, data.length - offset);
	   if (bytesRead == -1)
	     break;
	   offset += bytesRead;
	 }
	 in.close();

	 if (offset != contentLength) {
	   throw new IOException("Only read " + offset + " bytes; Expected " + contentLength + " bytes");
	 }
	
	 newResource = createResource(data, address);
	 if (newResource != null)
		 return true;
	 else
		 return false;
 }
 
 
 	// Convert the binary data to a resource
	private Resource createResource(byte[] fileData, String address) {
		logger.log(logger.EXTREME, "Inside create resource");
		//These two lines are added to handle odd characters in the name like #.  Without it
		// toLocalFile() chokes and returns the wrong name.
    	MessageDigest md;
    	try {
    		logger.log(logger.EXTREME, "Generating MD5");
    		md = MessageDigest.getInstance("MD5");
    		md.update(fileData);
    		byte[] hash = md.digest();
  
    		Resource r = new Resource();
    		r.setGuid(QUuid.createUuid().toString().replace("}", "").replace("{", ""));
    		r.setNoteGuid(noteGuid);
    		r.setMime("image/" +address.substring(address.lastIndexOf(".")+1));
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
    		
    		int fileNamePos = address.lastIndexOf(File.separator);
    		if (fileNamePos == -1)
    			fileNamePos = address.lastIndexOf("/");
   			String fileName = address.substring(fileNamePos+1);
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
    		a.setAttachment(false);
    		a.setAttachmentIsSet(true);
    		a.setClientWillIndex(false);
    		a.setClientWillIndexIsSet(true);
    		a.setRecoType("");
    		a.setRecoTypeIsSet(false);
    		a.setSourceURL(fileName);
    		a.setSourceURLIsSet(true);
    		a.setTimestamp(0);
    		a.setTimestampIsSet(false);
    		a.setFileName(fileName);
    		a.setFileNameIsSet(true);
    		r.setAttributes(a);
    		
    		//conn.getNoteTable().noteResourceTable.saveNoteResource(r, true);
    		logger.log(logger.EXTREME, "Resource created");
    		
    		// Now write it out so someone else can look at it
    		
    		return r;
    	} catch (NoSuchAlgorithmException e1) {
    		e1.printStackTrace();
		}
    	return null;
	}

}