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

import com.evernote.edam.type.Resource;

import cx.fbn.nevernote.Global;

public class HtmlTagModifier {
	StringBuffer html;

	public HtmlTagModifier() {
		html = null;
	}
	
	public HtmlTagModifier(String data) {
		html = new StringBuffer(data);
	}
	
	public void setHtml(String data) {
		html = new StringBuffer(data);
	}
	
	public String getHtml() {
		return html.toString();
	}
	
	public void modifyLatexTagHash(Resource res) {
		
		int position = 0;
		for (; position<html.length();) {
			position = html.indexOf("<img", position);
			if (position > 0) {
				if (matchesGuid(position, res.getGuid())) {
					replaceValue(position, "height", new Integer(res.getHeight()).toString());
					replaceValue(position, "width", new Integer(res.getWidth()).toString());
					replaceValue(position, "hash", Global.byteArrayToHexString(res.getData().getBodyHash()));
					return;
				} 
			}
			position = position+1;
		}
		return;
	}
	
	
	private boolean matchesGuid(int position, String guid) {
		int endPosition = html.indexOf(">", position);
		if (endPosition < 0)
			return false;
		
		int guidPos = html.indexOf(guid, position);
		if (guidPos > endPosition) 
			return false;
		else
			return true;
	}
	
	public void replaceValue(int position, String attribute, String newValue) {

		int endPosition = html.indexOf(">", position);
		if (endPosition < 0)
			return;
		
		int attributeStart = html.indexOf(attribute, position);
		if (attributeStart < 0 || attributeStart > endPosition)
			return;
		
		int attributeEnd = html.indexOf(" ", attributeStart);
		if (attributeEnd < 0 || endPosition < attributeEnd) 
			attributeEnd = endPosition-1;
				
		attributeStart = attributeStart+2+attribute.length();
		html = html.delete(attributeStart, endPosition-1);
		html = html.insert(attributeStart, newValue);
		
	}
}
