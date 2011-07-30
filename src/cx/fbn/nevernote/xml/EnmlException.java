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

import org.xml.sax.SAXParseException;

public class EnmlException extends SAXParseException {

	public EnmlException(String message, String publicId, String systemId,
			int lineNumber, int columnNumber, Exception e) {
		super(message, publicId, systemId, lineNumber, columnNumber, e);
		// TODO Auto-generated constructor stub
	}
	public String attribute = null;
	/**
	 * 
	 */
	private static final long serialVersionUID = 6728529537176421409L;

	

}
