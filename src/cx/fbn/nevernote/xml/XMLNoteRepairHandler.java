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

import java.util.Vector;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;


public class XMLNoteRepairHandler extends DefaultHandler {
	
	private final Vector<String> tagQueue;
	private final Vector<String> xml; 
	
	public XMLNoteRepairHandler() {
		xml = new Vector<String>();
		tagQueue = new Vector<String>();
	}
	
	public void setXml(String x) {
		// First, change the <hr> tags.  That is usually a problem
		String val = x.toString().replace("<HR>", "<hr/>");
		val = val.replace("<hr>", "<hr/>");
		val = val.replace("</HR>", "");
		x = val.replace("</hr>", "");
		
		// Another problem is a bad closing bracket
		x = x.replace("//>", "/>");
		
		// Onother oddball is a bad br
		x = x.replace("<br/></br>", "<br/>");
		
		int pos = 0;
		for (; pos > -1;) {
			String line;
			pos = x.indexOf('\n');
			if (pos > -1) {
				line = new String(x.substring(0,pos));
				x = x.substring(pos+1);
			} else
				line = x;
			xml.add(line);
		}
	}
	
	public String getXml() {
		StringBuffer b = new StringBuffer();
		for (int i=0; i<xml.size(); i++) {
			b.append(xml.get(i));
			if (i<xml.size()-1)
				b.append('\n');
		}
		return b.toString();
	}
	
	public void reset() {
		tagQueue.clear();
	}
	
	public void stripAttribute(String attribute, int line, int column) {
	    String repair = new String(xml.get(line-1));
	    
	    int pos = column-1;
	    String fragment = repair.substring(0,pos);
	    int startPos = fragment.lastIndexOf("<");
	    fragment = fragment.substring(startPos);   

    	
    	// Now we have the fragment, remove the attribute
    	int attributeStart = fragment.indexOf(attribute)-1;
    	int endPos = fragment.indexOf("\"", attributeStart+1);
    	endPos = fragment.indexOf("\"", endPos+1)+1;
    	if (endPos == -1) 
    		endPos = fragment.length()-1;
    	
    	String repairedFragment = fragment.substring(0,attributeStart) +fragment.substring(endPos);
    	repair = repair.substring(0,startPos)+ repairedFragment+ repair.substring(column-1);
    	
    	xml.set(line-1, repair);
	}
	
	
	public void renameElement(String element, int line, int column) {
		for (int i=0; i<xml.size(); i++) {
			String value = xml.get(i);
			value = value.replace("<"+element, "<span");
			value = value.replace("</"+element, "</span");
			xml.set(i, value);
		}
	}
	
	public String repair(int line, int column) {
	    StringBuffer repair = new StringBuffer(xml.get(line-1));
	    int pos = column-1;
	    if (pos > 0) {
	    	for (int i=pos; i>=0; i--) {
	    		if (repair.charAt(i) == '<') {
	    			pos = i;
	    			i = -1;
	    		}
	    	}
	    	if (pos > 0) {
	    		repair.insert(pos, "</" +tagQueue.get(tagQueue.size()-1) +">");
	    	}
	    	xml.remove(line-1);
	    	xml.add(line-1, repair.toString());
	    }
		
		
		return getXml();
	}
		
	@Override
	public void startElement(String uri, String localName, String qName, Attributes attrs) throws SAXException {
		tagQueue.add(qName);
	}
	
	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		String endVal = tagQueue.lastElement();
		if (endVal.equals(qName)) {
			tagQueue.remove(tagQueue.size()-1);
		} 
	}


	@Override
	public void error(SAXParseException e)
	  throws EnmlException
	{
		Exception exception = new Exception();
		EnmlException e1 = new EnmlException(e.getMessage(), e.getPublicId(), e.getSystemId(), e.getLineNumber(), e.getColumnNumber(), exception);
		throw e1;
	}

	@Override
	public void warning(SAXParseException e)
	  throws SAXParseException
	{
		System.out.println("---------- WARNING ------------- ");
	  throw e;
	}


}
