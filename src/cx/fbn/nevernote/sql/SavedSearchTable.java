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

import java.util.List;

import com.evernote.edam.type.SavedSearch;

import cx.fbn.nevernote.Global;
import cx.fbn.nevernote.sql.requests.SavedSearchRequest;

public class SavedSearchTable {
	int id;
	
	// Constructor
	public SavedSearchTable(int i) {
		id = i;
	}
	// Create the table
	public void createTable() {
		SavedSearchRequest request = new SavedSearchRequest();
		request.requestor_id = id;
		request.type = SavedSearchRequest.Create_Table;
		Global.dbRunner.addWork(request);
	}
	// Drop the table
	public void dropTable() {
		SavedSearchRequest request = new SavedSearchRequest();
		request.requestor_id = id;
		request.type = SavedSearchRequest.Drop_Table;
		Global.dbRunner.addWork(request);
 	}
	// get all tags
	public List<SavedSearch> getAll() {
		SavedSearchRequest request = new SavedSearchRequest();
		request.requestor_id = id;
		request.type = SavedSearchRequest.Get_All;
		Global.dbRunner.addWork(request);
		Global.dbClientWait(id);
		SavedSearchRequest req = Global.dbRunner.savedSearchResponse.get(id).copy();
		return req.responseSavedSearches;
	}
	public SavedSearch getSavedSearch(String guid) {
		SavedSearchRequest request = new SavedSearchRequest();
		request.requestor_id = id;
		request.type = SavedSearchRequest.Get_Saved_Search;
		request.string1 = new String(guid);
		Global.dbRunner.addWork(request);
		Global.dbClientWait(id);
		SavedSearchRequest req = Global.dbRunner.savedSearchResponse.get(id).copy();
		return req.responseSavedSearch;
	}
	// Update a saved search
	public void updateSavedSearch(SavedSearch search, boolean isDirty) {
		SavedSearchRequest request = new SavedSearchRequest();
		request.requestor_id = id;
		request.type = SavedSearchRequest.Update_Saved_Search;
		request.savedSearch = search.deepCopy();
		request.bool1 = isDirty;
		Global.dbRunner.addWork(request);
	}
	// Delete a saved search
	public void expungeSavedSearch(String guid, boolean needsSync) {
		SavedSearchRequest request = new SavedSearchRequest();
		request.requestor_id = id;
		request.type = SavedSearchRequest.Expunge_Saved_Search;
		request.string1 = new String(guid);
		request.bool1 = needsSync;
		Global.dbRunner.addWork(request);
	}
	// Save a saved search
	public void addSavedSearch(SavedSearch search, boolean isDirty) {
		SavedSearchRequest request = new SavedSearchRequest();
		request.requestor_id = id;
		request.type = SavedSearchRequest.Add_Saved_Search;
		request.savedSearch = search.deepCopy();
		request.bool1 = isDirty;
		Global.dbRunner.addWork(request);
	}
	// Update sequence number
	public void updateSavedSearchSequence(String guid, int sequence) {
		SavedSearchRequest request = new SavedSearchRequest();
		request.requestor_id = id;
		request.type = SavedSearchRequest.Update_Saved_Search_Sequence;
		request.string1 = new String(guid);
		request.int1 = sequence;
		Global.dbRunner.addWork(request);
	}
	// Update saved search guid
	public void updateSavedSearchGuid(String oldGuid, String newGuid) {
		SavedSearchRequest request = new SavedSearchRequest();
		request.requestor_id = id;
		request.type = SavedSearchRequest.Get_Saved_Search;
		request.string1 = new String(oldGuid);
		request.string2 = new String(newGuid);
		Global.dbRunner.addWork(request);
	}
	public void resetDirtyFlag(String guid) {
		SavedSearchRequest request = new SavedSearchRequest();
		request.requestor_id = id;
		request.type = SavedSearchRequest.Reset_Dirty_Flag;
		request.string1 = new String(guid);
		Global.dbRunner.addWork(request);
	}
	// Get dirty records
	public List<SavedSearch> getDirty() {
		SavedSearchRequest request = new SavedSearchRequest();
		request.requestor_id = id;
		request.type = SavedSearchRequest.Get_Dirty;
		Global.dbRunner.addWork(request);
		Global.dbClientWait(id);
		SavedSearchRequest req = Global.dbRunner.savedSearchResponse.get(id).copy();
		return req.responseSavedSearches;
	}
	// Find a guid based upon the name
	public String findSavedSearchByName(String name) {
		SavedSearchRequest request = new SavedSearchRequest();
		request.requestor_id = id;
		request.type = SavedSearchRequest.Find_Saved_Search_By_Name;
		request.string1 = new String(name);
		Global.dbRunner.addWork(request);
		Global.dbClientWait(id);
		SavedSearchRequest req = Global.dbRunner.savedSearchResponse.get(id).copy();
		return req.string1;
	}
	// given a guid, does the saved search exist
	public boolean exists(String guid) {
		SavedSearchRequest request = new SavedSearchRequest();
		request.requestor_id = id;
		request.type = SavedSearchRequest.Exists;
		request.string1 = new String(guid);
		Global.dbRunner.addWork(request);
		Global.dbClientWait(id);
		SavedSearchRequest req = Global.dbRunner.savedSearchResponse.get(id).copy();
		return req.bool1;
	}
	// This is a convience method to check if a tag exists & update/create based upon it
	public void syncSavedSearch(SavedSearch search, boolean isDirty) {
		SavedSearchRequest request = new SavedSearchRequest();
		request.requestor_id = id;
		request.type = SavedSearchRequest.Sync_Saved_Search;
		request.savedSearch = search.deepCopy();
		request.bool1 = isDirty;
		Global.dbRunner.addWork(request);
	}
}
