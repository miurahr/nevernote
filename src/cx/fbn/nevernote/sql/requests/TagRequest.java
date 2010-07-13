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

import com.evernote.edam.type.Tag;

public class TagRequest extends DBRunnerRequest {
	public static int Create_Table     			= 1;
	public static int Drop_Table 				= 2;
	public static int Add_Tag					= 3;
	public static int Delete_Tag				= 4;
	public static int Get_All					= 5;
	public static int Get_Tag					= 6;
	public static int Update_Tag				= 7;
	public static int Expunge_Tag				= 8;
	public static int Update_Parent				= 9;
	public static int Save_Tags					= 10;
	public static int Update_Tag_Sequence 		= 11;
	public static int Update_Tag_Guid			= 12;
	public static int Get_Dirty					= 13;
	public static int Find_Tag_By_Name			= 14;
	public static int Exists					= 15;
	public static int Reset_Dirty_Flag			= 16;
	public static int Sync_Tag					= 17;

	public volatile String			string1;
	public volatile String			string2;	
	public volatile boolean			bool1;
	public volatile int				int1;

	public volatile Tag				tag;
	public volatile List<Tag>		tags;
	
	public volatile List<Tag>		responseTags;
	public volatile Tag				responseTag;
	public volatile String			responseString;
	public volatile boolean			responseBool;
	
	public TagRequest() {
		category = TAG;
	}
	
	public TagRequest copy() {
		TagRequest request = new TagRequest();
		
		request.requestor_id = requestor_id;
		request.type = type;
		request.category = category;
		request.int1 = int1;
		if (string1 != null)
			request.string1 = new String(string1);
		if (string2 != null)
			request.string2 = new String(string2);
		if (tag != null)
			request.tag = tag;
		request.bool1 = bool1;
		if (tags != null && tags.size() > 0) {
			for (int i=0; i<tags.size(); i++)
				request.tags.add(tags.get(i));
		}
		
		responseBool = request.responseBool;
		if (responseString != null) {
			request.responseString = new String(responseString);
		}
		
		if (responseTag != null)
			request.responseTag = responseTag.deepCopy();
		
		if (responseTags != null) {
			request.responseTags = new ArrayList<Tag>();
			for (int i =0; i<responseTags.size(); i++)
				request.responseTags.add(responseTags.get(i).deepCopy());
		}
		
		return request;
	}
	
}