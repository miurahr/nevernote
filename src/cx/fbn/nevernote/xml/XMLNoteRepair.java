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

import java.io.File;
import java.io.IOException;
import java.io.StringReader;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import cx.fbn.nevernote.Global;

public class XMLNoteRepair {
	public boolean saveInvalidXML;
	
	public String parse(String xmlData, boolean validate) {
		saveInvalidXML = false;
		InputSource is = new InputSource();
		is.setCharacterStream(new StringReader(xmlData));
		XMLNoteRepairHandler handler = new XMLNoteRepairHandler();
		
		// Replace DTD with local copy in case we are not connected
		File dtdFile = Global.getFileManager().getXMLDirFile("enml2.dtd");
		String dtd = dtdFile.toURI().toString();
		xmlData = xmlData.replace("<!DOCTYPE en-note SYSTEM \'http://xml.evernote.com/pub/enml2.dtd'>", 
				"<!DOCTYPE en-note SYSTEM \"" +dtd +"\">");
		xmlData = xmlData.replace("<!DOCTYPE en-note SYSTEM \"http://xml.evernote.com/pub/enml2.dtd\">", 
				"<!DOCTYPE en-note SYSTEM \"" +dtd +"\">");
		
		handler.setXml(xmlData);
		is.setCharacterStream(new StringReader(handler.getXml()));
        
		boolean fixed = false;
		int i=0;
		int max = 10;
		if (validate)
			max = 10000;
		while (!fixed && i<max) {
			try {
				i++;
				SAXParserFactory factory = SAXParserFactory.newInstance();
				factory.setValidating(validate);
				SAXParser parser = factory.newSAXParser();
				parser.parse(is, handler);
				fixed = true;
			} catch (EnmlException e) { 
				String message = e.getMessage();
				saveInvalidXML = true;
				//System.out.println("ENML Exception: " +message);
				boolean found = false;
				int endAttribute = message.indexOf(" must be declared for element type ");
				if (message.startsWith("Attribute ") && endAttribute > -1) {
					String attribute = message.substring(11, endAttribute-1);
					String element = message.substring(message.indexOf("\"", endAttribute+3));
					element = element.replace("\"", "");
					element = element.substring(0,element.length()-1);
					Global.addInvalidAttribute(element, attribute);
					handler.stripAttribute(attribute, e.getLineNumber(), e.getColumnNumber());
					is.setCharacterStream(new StringReader(handler.getXml()));
					found = true;
				}
				int endElement = message.indexOf(" must be declared.");
				if (message.startsWith("Element type") && endElement > -1) {
					String element = message.substring(14,endElement-1);
					Global.addInvalidElement(element);
					handler.renameElement(element, e.getLineNumber(), e.getColumnNumber());
					is.setCharacterStream(new StringReader(handler.getXml()));
					found = true;
				}
				if (!found)
					System.err.println("New enml validation error: " +e.getMessage() +" Line:" +e.getLineNumber() +" Column:" +e.getColumnNumber());
			} catch (SAXParseException e) {
				System.err.println("SAXParse Exception - Attempt #"+i +" "+e.getMessage());
				handler.repair(e.getLineNumber(), e.getColumnNumber());
				is.setCharacterStream(new StringReader(handler.getXml()));
				if (validate) {
					System.err.println("Error validating ENML2 DTD");
					return null;
				}
			} catch (SAXException e) {
				System.err.append("SAXException");
				e.printStackTrace();
			} catch (ParserConfigurationException e) {
				System.err.println("Parser Config Error");
				e.printStackTrace();
			} catch (IOException e) {
				System.err.println("IO Exception");
				e.printStackTrace();
			}
		}
		if (!fixed)
			return null;
		else {
			// Replace DTD with online copy
			xmlData = handler.getXml();
			xmlData = xmlData.replace("<!DOCTYPE en-note SYSTEM \"" +dtd +"\">", "<!DOCTYPE en-note SYSTEM \'http://xml.evernote.com/pub/enml2.dtd'>");
			return xmlData;			
		}
	}
}
