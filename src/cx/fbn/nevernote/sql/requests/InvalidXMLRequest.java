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

package cx.fbn.nevernote.sql.requests;

import java.util.ArrayList;
import java.util.List;


public class InvalidXMLRequest extends DBRunnerRequest {
	public static int Create_Table     						= 1;
	public static int Drop_Table 							= 2;
	public static int Get_Invalid_Elements					= 3;
	public static int Get_Invalid_Attributes  				= 4;
	public static int Get_Invalid_Attribute_Elements 		= 5;
	public static int Add_Invalid_Element					= 6;
	public static int Add_Invalid_Attribute					= 7;


	public volatile String		string1;
	public volatile String		string2;
	public volatile String		string3;
	
	public String responseString1;
	public List<String> responseList;
	public ArrayList<String> responseArrayList;
	
	public InvalidXMLRequest() {
		category = Invalid_XML;
	}
	
	public InvalidXMLRequest copy() {
		InvalidXMLRequest req = new InvalidXMLRequest();
		req.type = type;
		req.category = category;
		req.requestor_id = requestor_id;
		
		if (string1  != null)
			req.string1 = new String(string1);
		if (string2  != null)
			req.string2 = new String(string2);
		if (string3  != null)
			req.string3 = new String(string3);
		if (responseString1  != null)
			req.responseString1 = new String(responseString1);
		
		if (responseList != null) {
			req.responseList = new ArrayList<String>();
			for (int i=0; i<responseList.size(); i++) {
				req.responseList.add(new String(responseList.get(i)));
			}
		}
		
		if (responseArrayList != null) {
			req.responseArrayList = new ArrayList<String>();
			for (int i=0; i<responseArrayList.size(); i++) {
				req.responseArrayList.add(new String(responseArrayList.get(i)));
			}
		}

		return req;
	}
	
}