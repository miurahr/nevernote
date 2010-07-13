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

import com.evernote.edam.type.SavedSearch;

public class SavedSearchRequest extends DBRunnerRequest {
	public static int Create_Table     			= 1;
	public static int Drop_Table 				= 2;
	public static int Get_All					= 3;
	public static int Update_Saved_Search 		= 4;
	public static int Expunge_Saved_Search		= 5;
	public static int Get_Saved_Search			= 6;
	public static int Add_Saved_Search			= 7;
	public static int Update_Saved_Search_Sequence = 8;
	public static int Get_Dirty					= 9;
	public static int Find_Saved_Search_By_Name = 10;
	public static int Exists					= 11;
	public static int Sync_Saved_Search			= 12;
	public static int Reset_Dirty_Flag			= 13;
	

	public volatile String					string1;
	public volatile String					string2;
	public volatile int						int1;
	public volatile boolean					bool1;
	public volatile SavedSearch				savedSearch;
	public volatile List<SavedSearch> 		savedSearches;
	
	public volatile List<SavedSearch>		responseSavedSearches;
	public volatile SavedSearch				responseSavedSearch;
	public volatile String 					responseString;
	public volatile boolean					responseBoolean;
	
	public SavedSearchRequest() {
		category = SAVED_SEARCH;
	}
	
	public SavedSearchRequest copy() {
		SavedSearchRequest request = new SavedSearchRequest();
		
		request.requestor_id = requestor_id;
		request.type = type;
		request.category = category;
		if (string1 != null)
			request.string1 = new String(string1);
		if (string2 != null)
			request.string2 = new String(string2);
		request.bool1 = bool1;
		
		if (responseString != null)
			request.responseString = new String(responseString);
		
		if (savedSearch != null)
			request.savedSearch = savedSearch.deepCopy();
		if (savedSearches != null)
			for (int i=0; i<savedSearches.size(); i++)
				request.savedSearches.add(savedSearches.get(i).deepCopy());
		
		request.responseBoolean = responseBoolean;
		
		if (responseSavedSearches != null) {
			request.responseSavedSearches = new ArrayList<SavedSearch>();
			for (int i=0; i<responseSavedSearches.size(); i++) 
				request.responseSavedSearches.add(responseSavedSearches.get(i).deepCopy());	
		}
		
		if (responseSavedSearch != null) {
			request.responseSavedSearch = responseSavedSearch.deepCopy();
		}

		return request;
	}
	
}