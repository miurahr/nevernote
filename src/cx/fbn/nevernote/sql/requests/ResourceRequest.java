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

import com.evernote.edam.type.Resource;

public class ResourceRequest extends DBRunnerRequest {
	public static int Create_Table     			= 1;
	public static int Drop_Table 				= 2;
	public static int Reset_Dirty_Flag 			= 3;
	public static int Get_Next_Unindexed		= 4;
	public static int Set_Index_Needed			= 5;
	public static int Save_Note_Resource		= 6;
	public static int Expunge_Note_Resource		= 7;
	public static int Get_Note_Resource_Guid_By_Hash_Hex 	= 8;
	public static int Get_Note_Resource_Data_Body_By_Hash_Hex = 9;
	public static int Get_Note_Resource			= 10;
	public static int Get_Note_Resources		= 11;
	public static int Get_Note_Resources_Recognition = 12;
	public static int Get_Note_Resource_Recognition = 13;
	public static int Update_Note_Resource		= 14;
	public static int Reindex_All				= 15;
	public static int Get_Resource_Count		= 16;
	public static int Update_Note_Resource_Guid = 17;
	public static int Reset_Update_Sequence_Number = 18;
	


	public volatile String			string1;
	public volatile String			string2;
	public volatile Resource		resource;
	public volatile boolean			bool1;
	public volatile int				int1;
	
	public volatile List<String>	responseStrings;
	public volatile String			responseString;
	public volatile List<Resource>	responseResources;
	public volatile Resource		responseResource;
	public volatile int				responseInteger;
	
	public ResourceRequest() {
		category = RESOURCE;
	}
	
	public ResourceRequest copy() {
		ResourceRequest request = new ResourceRequest();
		
		request.requestor_id = requestor_id;
		request.type = type;
		request.category = category;
		if (string1 != null)
			request.string1 = new String(string1);
		if (string2 != null)
			request.string2 = new String(string2);
		if (resource != null)
			request.resource = resource.deepCopy();
		request.bool1 = bool1;
		request.int1 = int1;
		
		if (responseStrings != null) {
			request.responseStrings = new ArrayList<String>();
			for (int i=0; i<responseStrings.size(); i++) 
				request.responseStrings.add(new String(responseStrings.get(i)));	
		}
		
		if (responseResource != null) {
			request.responseResource = responseResource.deepCopy();
		}
		
		if (responseString != null) 
			request.responseString = new String(responseString);
		
		if (responseResources != null) {
			request.responseResources = new ArrayList<Resource>();
			for (int i=0; i<responseResources.size(); i++) 
				request.responseResources.add(responseResources.get(i).deepCopy());	
		}
		
		request.responseInteger = responseInteger;
		return request;
	}
	
}