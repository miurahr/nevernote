/*
 * This file is part of NeverNote 
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


package cx.fbn.nevernote.sql;

import java.util.ArrayList;
import java.util.List;

import cx.fbn.nevernote.Global;
import cx.fbn.nevernote.sql.requests.InvalidXMLRequest;
import cx.fbn.nevernote.utilities.ListManager;

public class InvalidXMLTable {
	ListManager parent;
	int id;
	
	// Constructor
	public InvalidXMLTable(int i) {
		id = i;
	}
	// Create the table
	public void createTable() {
		InvalidXMLRequest request = new InvalidXMLRequest();
		request.requestor_id = id;
		request.type = InvalidXMLRequest.Create_Table;
		Global.dbRunner.addWork(request);
	}
	// Drop the table
	public void dropTable() {
		InvalidXMLRequest request = new InvalidXMLRequest();
		request.requestor_id = id;
		request.type = InvalidXMLRequest.Drop_Table;
		Global.dbRunner.addWork(request);
	}
	// Add an invalid XML element to the table
	public void addInvalidElement(String element) {
		InvalidXMLRequest request = new InvalidXMLRequest();
		request.requestor_id = id;
		request.string1 = element;
		request.type = InvalidXMLRequest.Add_Invalid_Element;
		Global.dbRunner.addWork(request);
	}
	// Add an invalid XML attribute to the table
	public void addInvalidAttribute(String element, String attribute) {
		InvalidXMLRequest request = new InvalidXMLRequest();
		request.requestor_id = id;
		request.string1 = element;
		request.string2 = attribute;
		request.type = InvalidXMLRequest.Add_Invalid_Attribute;
		Global.dbRunner.addWork(request);
	}
	// Get invalid attributes for a given element
	public List<String> getInvalidAttributeElements() {
		InvalidXMLRequest request = new InvalidXMLRequest();
		request.requestor_id = id;
		request.type = InvalidXMLRequest.Get_Invalid_Attribute_Elements;
		Global.dbRunner.addWork(request);
		Global.dbClientWait(id);
		InvalidXMLRequest req = Global.dbRunner.invalidXMLResponse.get(id).copy();
		return req.responseList;
	}
	// Get the list of elements which we have invalid attributes for
	public ArrayList<String> getInvalidAttributes(String element) {
		InvalidXMLRequest request = new InvalidXMLRequest();
		request.requestor_id = id;
		request.string1 = element;
		request.type = InvalidXMLRequest.Get_Invalid_Attributes;
		Global.dbRunner.addWork(request);
		Global.dbClientWait(id);
		InvalidXMLRequest req = Global.dbRunner.invalidXMLResponse.get(id).copy();
		return req.responseArrayList;
	}
	// Add an invalid XML attribute to the table
	public List<String> getInvalidElements() {
		InvalidXMLRequest request = new InvalidXMLRequest();
		request.requestor_id = id;
		request.type = InvalidXMLRequest.Get_Invalid_Elements;
		Global.dbRunner.addWork(request);
		Global.dbClientWait(id);
		InvalidXMLRequest req = Global.dbRunner.invalidXMLResponse.get(id).copy();
		return req.responseList;
	}
	
	

}
