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
package cx.fbn.nevernote.evernote;

//**********************************************
//**********************************************
//* This is used to turn HTML into ENML compliant
//* data.
//**********************************************
//**********************************************

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.w3c.tidy.Tidy;
import org.w3c.tidy.TidyMessage;

import com.trolltech.qt.core.QByteArray;
import com.trolltech.qt.core.QTextCodec;

import cx.fbn.nevernote.Global;
import cx.fbn.nevernote.utilities.ApplicationLogger;
import cx.fbn.nevernote.utilities.Pair;
import cx.fbn.nevernote.xml.XMLCleanup;
import cx.fbn.nevernote.xml.XMLNoteRepair;

public class EnmlConverter {
	private final ApplicationLogger logger;
	private List<String>			resources;
	public boolean saveInvalidXML;
	
	private class TidyListener implements org.w3c.tidy.TidyMessageListener {
		
		ApplicationLogger logger;
		public boolean errorFound; 
		
		public TidyListener(ApplicationLogger logger) {
			this.logger = logger;
			errorFound = false;
		}
		@Override
		public void messageReceived(TidyMessage msg) {
			if (msg.getLevel() == TidyMessage.Level.ERROR) {
				logger.log(logger.LOW, "******* JTIDY ERORR *******");
				logger.log(logger.LOW, "Error Code: " +msg.getErrorCode());
				logger.log(logger.LOW, "Column: " +msg.getColumn());
				logger.log(logger.LOW, "Column: " +msg.getColumn());
				logger.log(logger.LOW, "Line: " +msg.getLine());
				logger.log(logger.LOW, "Message: " +msg.getMessage());
				logger.log(logger.LOW, "***************************");
				errorFound = true;
			} else 
				logger.log(logger.EXTREME, "JTidy Results: "+msg.getMessage());
		}
		
	}
	
	public EnmlConverter(ApplicationLogger l) {
		logger = l;
//		conn = c;
		saveInvalidXML = false;
		resources = new ArrayList<String>();
	}

	public List<String> getResources() {
		return resources;
	}
	public String convert(String noteGuid, String content) {
		logger.log(logger.HIGH, "Entering DBRunner.convertToEnml");
		logger.log(logger.EXTREME, "Note Text:" +content);
		
		// Replace the en-note tags with body tags in case we came from 
		// someplace other than the editor (for example, if we are merging notes).
		content = content.replace("<en-note>", "<body>");
		content = content.replace("</en-note>", "</body>");
		// Start removing stuff we don't need or want
		int br = content.lastIndexOf("</body>");
		if (br > 0)
			content = new String(content.substring(0,br));
		String newContent;
		int k = content.indexOf("<body");
		if (k>-1)
			newContent = new String(content.substring(k));
		else
			newContent = "<body>"+content;

		
		// Check that we have a vaild header.  Normally we should not
		// but sometimes it seems that we can.  I don't see how, but it is
		// easy enough to check.
		if (!newContent.startsWith("<?xml"))
			newContent = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" 
				+"<!DOCTYPE en-note SYSTEM \"http://xml.evernote.com/pub/enml2.dtd\">\n"
				+newContent 
				+"</body>";
		

		// Fix the more common XML problems that Webkit creates, but are not considered 
		// valid XML.
		newContent = fixStupidXMLProblems(newContent);
		
		
		// Change the contents to have enml instead of body tags or
		// we'll fail validation later.
		newContent = newContent.replace("<body", "<en-note");
		newContent = newContent.replace("</body>", "</en-note>");
		
		// First pass through the data.  The goal of this pass is to 
		// validate that we have a good XML document and to repair
		// any problems found.
		
		XMLNoteRepair repair = new XMLNoteRepair();
//		logger.log(logger.HIGH, "Checking XML Structure");
//		newContent = repair.parse(newContent, false);
//		logger.log(logger.HIGH, "Check complete");
	
        logger.log(logger.HIGH, "Fixing encryption tags");
        newContent = fixEncryptionTags(newContent);
		
		Tidy tidy = new Tidy();
		TidyListener tidyListener = new TidyListener(logger);
		tidy.setMessageListener(tidyListener);
		tidy.getStderr().close();  // the listener will capture messages
		tidy.setXmlTags(true);
		tidy.setXHTML(true);
		
		QTextCodec codec;
		codec = QTextCodec.codecForName("UTF-8");
        QByteArray unicode =  codec.fromUnicode(newContent);
        
//		byte html[] = newContent.getBytes();
//		ByteArrayInputStream is = new ByteArrayInputStream(html);
        logger.log(logger.HIGH, "Starting JTidy check");
        logger.log(logger.EXTREME, "Start of JTidy Input");
        logger.log(logger.EXTREME, newContent);
        logger.log(logger.EXTREME, "End Of JTidy Input");
		ByteArrayInputStream is = new ByteArrayInputStream(unicode.toByteArray());
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        tidy.setInputEncoding("UTF-8");
		tidy.parse(is, os);
		String tidyContent = os.toString();
		if (tidyListener.errorFound) {
			logger.log(logger.LOW, "Note Contents Begin");
			logger.log(logger.LOW, content);
			logger.log(logger.LOW, "Note Contents End");
			tidyContent = null;
		} else {
			if (newContent.trim().equals(""))
				tidyContent = null;
		}

		// If the repair above returned null, then the XML is foobar.
		// We are done here.
		if (tidyContent != null) {
			newContent = tidyContent;
		} else {
			// Houston, we've had a problem.  Fall back to old method
			logger.log(logger.HIGH, "Error converting to JTidy.  Falling back to old method");
			String repairedContent = repair.parse(newContent, false);
			if (repairedContent == null) {
				logger.log(logger.EXTREME, "Null returned from repair.parse()");
				logger.log(logger.LOW, "Parse error when converting to ENML. Aborting save");
				return null;
			}
			newContent = repairedContent;
			logger.log(logger.EXTREME, "Start of repaired content");
			logger.log(logger.EXTREME, repairedContent);
			logger.log(logger.EXTREME, "End of repaired content");
		}
		
		// Second pass through the data.  The goal of this pass is to 
		// remove any things we added in NixNote that do not match
		// the ENML schema
		XMLCleanup v = new XMLCleanup();
		v.setValue(newContent);
		logger.log(logger.HIGH, "Beginning ENML Cleanup");
		v.validate();
		logger.log(logger.HIGH, "Cleanup complete.");
		
	
			
		// Final pass through the data.  In this one we
		// remove any invalid attributes and to save the
		// new resources.
		logger.log(logger.EXTREME, "Rebuilt ENML:");
		logger.log(logger.EXTREME, v.getValue());	
		logger.log(logger.EXTREME, "End Of Rebuilt ENML:");
		resources = v.getResources();

		
		// The XML has the dtd to validate set against Evernote's web
		// address.  We change it to a local one because otherwise it would
		// fail if the user doesn't have internet connectivity.  The local copy
		// also contains the 3 other PUBLIC definitions at the beginning of the dtd.
		newContent = v.getValue();
		File dtdFile = Global.getFileManager().getXMLDirFile("enml2.dtd");
		String dtd = dtdFile.toURI().toString();
		newContent = newContent.replace("<!DOCTYPE en-note SYSTEM \'http://xml.evernote.com/pub/enml2.dtd'>", 
				"<!DOCTYPE en-note SYSTEM \"" +dtd +"\">");
		
		logger.log(logger.HIGH, "Validating ENML");
		String repairedContent = repair.parse(newContent, true);
		if (repairedContent == null)
			logger.log(logger.EXTREME, "Null returned from repair.parse()");
		else
			newContent = repairedContent;
		logger.log(logger.HIGH, "Validation complete");
		saveInvalidXML = repair.saveInvalidXML;
		
		// Restore the correct XML header.
		newContent = newContent.replace("<!DOCTYPE en-note SYSTEM \"" +dtd +"\">", 
				"<!DOCTYPE en-note SYSTEM 'http://xml.evernote.com/pub/enml2.dtd'>");
		
		
		logger.log(logger.EXTREME, "Leaving ENMLConverter.convert()");
		return newContent;
	}

	
	private String fixEncryptionTags(String content) {
		// Fix the problem that the document body isn't properly closed
		String newContent = new String(content);
		logger.log(logger.MEDIUM, "Inside EnmlConverter.fixEncryptionTags");
		logger.log(logger.EXTREME, content);
		
		// Fix the problem that the img tag isn't properly closed
		int endPos, startPos, endData,slotStart, slotEnd;
		logger.log(logger.MEDIUM, "Checking table encryption tags");
		String eTag = "<table class=\"en-crypt-temp\"";
		for (int i=newContent.indexOf(eTag); i>0; i = newContent.indexOf(eTag,i+1)) {
			slotStart = newContent.indexOf("slot", i+1)+6;
			slotEnd = newContent.indexOf("\"",slotStart);
			String slot = newContent.substring(slotStart, slotEnd);
			startPos = newContent.indexOf("<td>", i+1)+4;
			endData = newContent.indexOf("</td>",startPos);
			String text = newContent.substring(startPos,endData);
			endPos = newContent.indexOf("</table>",i+1)+8;
			// Encrypt the text
			Pair<String,String> pair = Global.passwordSafe.get(slot);
			String password = pair.getFirst();
			String hint = pair.getSecond();
			EnCrypt crypt = new EnCrypt(); 
			String encrypted = crypt.encrypt(text, password, 64); 

			// replace the table with an en-crypt tag.
			newContent = newContent.substring(0,i-1) + 
				"<en-crypt-temp cipher=\"RC2\" length=\"64\" hint=\""+
				hint +"\" value=\""+
				encrypted +
				"\" />" +
				newContent.substring(endPos);
		}
		
		return newContent;
	}
	
	// Fix XML problems that Qt can't deal with
	public String fixStupidXMLProblems(String content) {
		logger.log(logger.HIGH, "Entering DBRunner.fixStupidXMLProblems");

		// Fix the problem that the document body isn't properly closed
		String newContent = new String(content);
		logger.log(logger.MEDIUM, "Inside fixStupidXMLProblems.  Old content:");
		logger.log(logger.EXTREME, content);
		
		// Fix the problem that the img tag isn't properly closed
		int endPos;
		logger.log(logger.MEDIUM, "Checking img tags");
		for (int i=newContent.indexOf("<img"); i>0; i = newContent.indexOf("<img",i+1)) {
			endPos = newContent.indexOf(">",i+1);
			String end = newContent.substring(endPos+1);
			newContent = newContent.subSequence(0,endPos) +"/>"+end;
		}
		
		// Fix the problem that the input tag isn't properly closed
		logger.log(logger.MEDIUM, "Checking input tags");
		for (int i=newContent.indexOf("<input"); i>0; i = newContent.indexOf("<input",i+1)) {
			endPos = newContent.indexOf(">",i+1);
			String end = newContent.substring(endPos+1);
			newContent = newContent.subSequence(0,endPos) +"/>"+end;
		}
		
		
		// Fix the problem that the <br> tag isn't properly closed
		logger.log(logger.MEDIUM, "Checking br tags");
		for (int i=newContent.indexOf("<br"); i>0; i = newContent.indexOf("<br",i+1)) {
			endPos = newContent.indexOf(">",i+1);
			String end = newContent.substring(endPos+1);
			newContent = newContent.subSequence(0,endPos) +"/>"+end;
		}
			
		// Fix the problem that the <hr> tag isn't properly closed
		logger.log(logger.MEDIUM, "Checking hr tags");
		for (int i=newContent.indexOf("<hr"); i>0; i = newContent.indexOf("<hr",i+1)) {
			endPos = newContent.indexOf(">",i+1);
			String end = newContent.substring(endPos+1);
			newContent = newContent.subSequence(0,endPos) +"/>"+end;
		}
		
		// Fix the problem that the <meta> tag isn't properly closed
		logger.log(logger.MEDIUM, "Checking meta tags");
		for (int i=newContent.indexOf("<meta"); i>0; i = newContent.indexOf("<meta",i+1)) {
			endPos = newContent.indexOf(">",i+1);
			String end = newContent.substring(endPos+1);
			newContent = newContent.subSequence(0,endPos) +"/>"+end;
		}
		
		logger.log(logger.MEDIUM, "Leaving fixStupidXMLProblems");
		logger.log(logger.HIGH, "Leaving DBRunner.fixStupidXMLProblems");
		return newContent.toString();
	}


	// Fix XML that Evernote thinks is invalid
	public String fixEnXMLCrap(String note) {
		logger.log(logger.EXTREME, "Entering EnmlConverter.fixEnXMLCrap");
		if (note == null)
			return null;
		
		int pos;
		StringBuffer buffer = new StringBuffer(note);
		
		logger.log(logger.EXTREME, "Converting <b/>");
		// change all <b/> to <b></b> because Evernote hates them if they happen in <span>
		pos = buffer.indexOf("<b/>");
		for (; pos>-1; ) {
			buffer.replace(pos, pos+4, "<b></b>");
			pos = buffer.indexOf("<b/>",pos);
		}
		// change all <br/> to <br></br> because Evernote hates them if they happen in <span>
		logger.log(logger.EXTREME, "converting <br/>");
		pos = buffer.indexOf("<br/>");
		for (; pos>-1; ) {
			buffer.replace(pos, pos+5, "<br></br>");
			pos = buffer.indexOf("<br/>",pos);
		}
		
		// change all <span> elements in lists because Evernote hates them if they happen 
		int endPos = 0;
		int spanPos;
		pos = buffer.indexOf("<li>");
		spanPos = buffer.indexOf("<span>");
		// Get rid of empty spans in <li> elements
		pos = buffer.indexOf("<li>");
		spanPos = buffer.indexOf("<span/>");
		for (; pos>-1 && spanPos >-1;) {
			endPos = buffer.indexOf("</li>",pos);
			if (spanPos > pos && spanPos < endPos) {
				buffer.replace(spanPos,spanPos+7,"");
			}
			pos=buffer.indexOf("<li>",pos+1);
			spanPos = buffer.indexOf("<span/>",spanPos);
		}
		
		logger.log(logger.EXTREME, "Leaving EnmlConverter.fixEnXMLCrap");
		return buffer.toString();
	}
	
 	// Fix stupid en-media problems
	public String fixEnMediaCrap(String note) {
		if (note == null)
			return null;
		
		StringBuffer buffer = new StringBuffer(note);
		// get rid of any </en-media> tags since they shouldn't exist.
		int pos = buffer.indexOf("</en-media>");
		for (; pos>-1; ) {
			buffer.replace(pos, pos+11, "");
			pos = buffer.indexOf("</en-media>",pos);
		}
		
		
		// Make sure we have a proper /> ending the en-media tag
		pos = buffer.indexOf("<en-media");
		for (; pos>-1; ) {
			pos=buffer.indexOf(">", pos);
			if (!buffer.substring(pos-1,pos).equals("/"))
			buffer.replace(pos, pos+1, " />");
			pos = buffer.indexOf("<en-media",pos);
		}
		
		return buffer.toString();
	}
}
