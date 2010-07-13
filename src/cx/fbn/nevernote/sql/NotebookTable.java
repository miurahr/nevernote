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

import com.evernote.edam.type.Notebook;

import cx.fbn.nevernote.Global;
import cx.fbn.nevernote.sql.requests.NotebookRequest;
import cx.fbn.nevernote.utilities.Pair;

public class NotebookTable {
	int id;
	
	public NotebookTable(int i) {
		id = i;
	}
	
	// Create the table
	public void createTable() {
		NotebookRequest request = new NotebookRequest();
		request.requestor_id = id;
		request.type = NotebookRequest.Create_Table;
		Global.dbRunner.addWork(request);
	}
	// Drop the table
	public void dropTable() {
		NotebookRequest request = new NotebookRequest();
		request.requestor_id = id;
		request.type = NotebookRequest.Drop_Table;
		Global.dbRunner.addWork(request);
	}
	// Save an individual notebook
	public void addNotebook(Notebook tempNotebook, boolean isDirty, boolean local) {
		NotebookRequest request = new NotebookRequest();
		request.requestor_id = id;
		request.type = NotebookRequest.Add_Notebook;
		request.notebook = tempNotebook;
		request.bool1 = isDirty;
		request.bool2 = local;
		Global.dbRunner.addWork(request);
	}
	// Delete the notebook based on a guid
	public void expungeNotebook(String guid, boolean needsSync) {
		NotebookRequest request = new NotebookRequest();
		request.requestor_id = id;
		request.type = NotebookRequest.Expunge_Notebook;
		request.string1 = guid;
		Global.dbRunner.addWork(request);
	}
	// Update a notebook
	public void updateNotebook(Notebook tempNotebook, boolean isDirty) {
		NotebookRequest request = new NotebookRequest();
		request.requestor_id = id;
		request.type = NotebookRequest.Update_Notebook;
		request.notebook = tempNotebook;
		request.bool1 = isDirty;
		Global.dbRunner.addWork(request);
	}
	// Load notebooks from the database
	public List<Notebook> getAll() {
		NotebookRequest request = new NotebookRequest();
		request.requestor_id = id;
		request.type = NotebookRequest.Get_All;
		Global.dbRunner.addWork(request);
		Global.dbClientWait(id);
		NotebookRequest req = Global.dbRunner.notebookResponse.get(id).copy();
		return req.responseNotebooks;
		
	}	
	public List<Notebook> getAllLocal() {
		NotebookRequest request = new NotebookRequest();
		request.requestor_id = id;
		request.type = NotebookRequest.Get_All_Local;
		Global.dbRunner.addWork(request);
		Global.dbClientWait(id);
		NotebookRequest req = Global.dbRunner.notebookResponse.get(id).copy();
		return req.responseNotebooks;
	}
	// Archive or un-archive a notebook
	public void setArchived(String guid, boolean isDirty) {
		NotebookRequest request = new NotebookRequest();
		request.requestor_id = id;
		request.type = NotebookRequest.Set_Archived;
		request.string1 = guid;
		request.bool1 = isDirty;
		Global.dbRunner.addWork(request);
	}
	// Load non-archived notebooks from the database
	public List<Notebook> getAllArchived() {
		NotebookRequest request = new NotebookRequest();
		request.requestor_id = id;
		request.type = NotebookRequest.Get_All_Archived;
		Global.dbRunner.addWork(request);
		Global.dbClientWait(id);
		NotebookRequest req = Global.dbRunner.notebookResponse.get(id).copy();
		return req.responseNotebooks;
	}	
	// Check for a local/remote notebook
	public boolean isNotebookLocal(String guid) {
		NotebookRequest request = new NotebookRequest();
		request.requestor_id = id;
		request.type = NotebookRequest.Is_Notebook_Local;
		request.string1 = guid;
		Global.dbRunner.addWork(request);
		Global.dbClientWait(id);
		NotebookRequest req =(Global.dbRunner.notebookResponse.get(id)).copy();
		return req.responseBoolean;
	}
	// Update a notebook sequence number
	public void updateNotebookSequence(String guid, int sequence) {
		NotebookRequest request = new NotebookRequest();
		request.requestor_id = id;
		request.type = NotebookRequest.Update_Notebook_Sequence;
		request.int1 = sequence;
		Global.dbRunner.addWork(request);
	}
	// Update a notebook GUID number
	public void updateNotebookGuid(String oldGuid, String newGuid) {
		NotebookRequest request = new NotebookRequest();
		request.requestor_id = id;
		request.type = NotebookRequest.Update_Notebook_Guid;
		request.string1 = oldGuid;
		request.string2 = newGuid;
		Global.dbRunner.addWork(request);
	}
	// Get a list of notes that need to be updated
	public List <Notebook> getDirty() {
		NotebookRequest request = new NotebookRequest();
		request.requestor_id = id;
		request.type = NotebookRequest.Get_Dirty;
		Global.dbRunner.addWork(request);
		Global.dbClientWait(id);
		NotebookRequest req = Global.dbRunner.notebookResponse.get(id).copy();
		return req.responseNotebooks;
	}

	// This is a convience method to check if a tag exists & update/create based upon it
	public void syncNotebook(Notebook notebook, boolean isDirty) {
		NotebookRequest request = new NotebookRequest();
		request.requestor_id = id;
		request.type = NotebookRequest.Sync_Notebook;
		request.notebook = notebook;
		request.bool1 = isDirty;
		Global.dbRunner.addWork(request);
	}
	// Reset the dirty flag.  Typically done after a sync.
	public void  resetDirtyFlag(String guid) {
		NotebookRequest request = new NotebookRequest();
		request.requestor_id = id;
		request.type = NotebookRequest.Reset_Dirty;
		request.string1 = guid;
		Global.dbRunner.addWork(request);
	}
	// does a record exist?
	public String findNotebookByName(String newname) {
		NotebookRequest request = new NotebookRequest();
		request.requestor_id = id;
		request.type = NotebookRequest.Find_Note_By_Name;
		request.string1 = newname;
		Global.dbRunner.addWork(request);
		Global.dbClientWait(id);
		NotebookRequest req = Global.dbRunner.notebookResponse.get(id).copy();
		return req.responseString;
	}
	// Get Notebook counts
	public List<Pair<String,Integer>> getNotebookCounts() {
		NotebookRequest request = new NotebookRequest();
		request.requestor_id = id;
		request.type = NotebookRequest.Notebook_Counts;
		Global.dbRunner.addWork(request);
		Global.dbClientWait(id);
		NotebookRequest req = Global.dbRunner.notebookResponse.get(id).copy();
		return req.responseCounts;
		
	}
}

