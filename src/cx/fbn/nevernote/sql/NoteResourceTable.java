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

import com.evernote.edam.type.Resource;

import cx.fbn.nevernote.Global;
import cx.fbn.nevernote.sql.requests.ResourceRequest;


public class NoteResourceTable {
	private final int id;
	
	// Constructor
	public NoteResourceTable(int i) {
		id = i;
	}
	// Create the table
	public void createTable() {
		ResourceRequest request = new ResourceRequest();
		request.requestor_id = id;
		request.type = ResourceRequest.Create_Table;
		Global.dbRunner.addWork(request);
	}
	// Drop the table
	public void dropTable() {		
		ResourceRequest request = new ResourceRequest();
		request.requestor_id = id;
		request.type = ResourceRequest.Drop_Table;
		Global.dbRunner.addWork(request);
	}
	// Reset the dirty flag
	public void  resetDirtyFlag(String guid) {
		ResourceRequest request = new ResourceRequest();
		request.requestor_id = id;
		request.type = ResourceRequest.Reset_Dirty_Flag;
		request.string1 = new String(guid);
		Global.dbRunner.addWork(request);
	}
	// Set if the resource should be indexed
	public void  setIndexNeeded(String guid, Boolean indexNeeded) {
		ResourceRequest request = new ResourceRequest();
		request.requestor_id = id;
		request.type = ResourceRequest.Set_Index_Needed;
		request.string1 = new String(guid);
		request.bool1 = indexNeeded;
		Global.dbRunner.addWork(request);
	}
	// get any unindexed resource
	public List<String> getNextUnindexed(int limit) {
		ResourceRequest request = new ResourceRequest();
		request.requestor_id = id;
		request.type = ResourceRequest.Get_Next_Unindexed;
		request.int1 = limit;
		Global.dbRunner.addWork(request);
		Global.dbClientWait(id);
		ResourceRequest req = Global.dbRunner.resourceResponse.get(id).copy();
		return req.responseStrings;
	}
	public void saveNoteResource(Resource r, boolean isDirty) {
		ResourceRequest request = new ResourceRequest();
		request.requestor_id = id;
		request.type = ResourceRequest.Save_Note_Resource;
		request.resource = r.deepCopy();
		request.bool1 = isDirty;
		Global.dbRunner.addWork(request);
//		Global.dbClientWait(id);
	}
	// delete an old resource
	public void expungeNoteResource(String guid) {
		ResourceRequest request = new ResourceRequest();
		request.requestor_id = id;
		request.type = ResourceRequest.Expunge_Note_Resource;
		request.string1 = new String(guid);
		Global.dbRunner.addWork(request);
	}

	// Get a note resource from the database by it's hash value
	public String getNoteResourceGuidByHashHex(String noteGuid, String hash) {
		ResourceRequest request = new ResourceRequest();
		request.requestor_id = id;
		request.type = ResourceRequest.Get_Note_Resource_Guid_By_Hash_Hex;
		request.string1 = new String(noteGuid);
		request.string2 = new String(hash);
		Global.dbRunner.addWork(request);
		Global.dbClientWait(id);
		ResourceRequest req = Global.dbRunner.resourceResponse.get(id).copy();
		return req.responseString;	
	}
	// Get a note resource from the database by it's hash value
	public Resource getNoteResourceDataBodyByHashHex(String noteGuid, String hash) {
		ResourceRequest request = new ResourceRequest();
		request.requestor_id = id;
		request.type = ResourceRequest.Get_Note_Resource_Data_Body_By_Hash_Hex;
		request.string1 = new String(noteGuid);
		request.string2 = new String(hash);
		Global.dbRunner.addWork(request);
		Global.dbClientWait(id);
		ResourceRequest req = Global.dbRunner.resourceResponse.get(id).copy();
		return req.responseResource;
	}

	// Update a note resource guid
	public void updateNoteResourceGuid(String oldGuid, String newGuid, boolean isDirty) {
		ResourceRequest request = new ResourceRequest();
		request.requestor_id = id;
		request.type = ResourceRequest.Update_Note_Resource_Guid;
		request.string1 = new String(oldGuid);
		request.string2 = new String(newGuid);
		request.bool1 = isDirty;
		Global.dbRunner.addWork(request);
//		Global.dbClientWait(id);
	}
	

	// Reset update sequence number to zero
	public void resetUpdateSequenceNumber(String guid, boolean isDirty) {
		ResourceRequest request = new ResourceRequest();
		request.requestor_id = id;
		request.type = ResourceRequest.Reset_Update_Sequence_Number;
		request.string1 = new String(guid);
		request.bool1 = isDirty;
		Global.dbRunner.addWork(request);
	}
	
	// Get a note's resourcesby Guid
	public Resource getNoteResource(String guid, boolean withBinary) {
		if (guid == null) 
			return null;
		ResourceRequest request = new ResourceRequest();
		request.requestor_id = id;
		request.type = ResourceRequest.Get_Note_Resource;
		request.string1 = new String(guid);
		request.bool1 = withBinary;
		Global.dbRunner.addWork(request);
		Global.dbClientWait(id);
		ResourceRequest req = Global.dbRunner.resourceResponse.get(id).copy();
		return req.responseResource;
	}
	
	
	// Get a note's resourcesby Guid
	public List<Resource> getNoteResources(String noteGuid, boolean withBinary) {
		ResourceRequest request = new ResourceRequest();
		request.requestor_id = id;
		request.type = ResourceRequest.Get_Note_Resources;
		request.string1 = new String(noteGuid);
		request.bool1 = withBinary;
		Global.dbRunner.addWork(request);
		Global.dbClientWait(id);
		ResourceRequest req = Global.dbRunner.resourceResponse.get(id).copy();
		return req.responseResources;
	}
	
	
	// Get all of a note's recognition data by the note guid
	public List<Resource> getNoteResourcesRecognition(String noteGuid) {
		ResourceRequest request = new ResourceRequest();
		request.requestor_id = id;
		request.type = ResourceRequest.Get_Note_Resources_Recognition;
		request.string1 = new String(noteGuid);
		Global.dbRunner.addWork(request);
		Global.dbClientWait(id);
		ResourceRequest req = Global.dbRunner.resourceResponse.get(id).copy();
		return req.responseResources;
	}
	
	// Get a note's recognition data by it's guid.
	public Resource getNoteResourceRecognition(String guid) {
		ResourceRequest request = new ResourceRequest();
		request.requestor_id = id;
		request.type = ResourceRequest.Get_Note_Resource_Recognition;
		request.string1 = new String(guid);
		Global.dbRunner.addWork(request);	
		Global.dbClientWait(id);
		ResourceRequest req = Global.dbRunner.resourceResponse.get(id).copy();
		return req.responseResource;
	}
	// Save Note Resource
	public void updateNoteResource(Resource r, boolean isDirty) {
		ResourceRequest request = new ResourceRequest();
		request.requestor_id = id;
		request.type = ResourceRequest.Update_Note_Resource;
		request.resource = r.deepCopy();
		request.bool1 = isDirty;
		Global.dbRunner.addWork(request);
	}
	
	// Drop the table
	public void reindexAll() {		
		ResourceRequest request = new ResourceRequest();
		request.requestor_id = id;
		request.type = ResourceRequest.Reindex_All;
		Global.dbRunner.addWork(request);
	}
	// Count unindexed notes
	public int getResourceCount() {
		ResourceRequest request = new ResourceRequest();
		request.requestor_id = id;
		request.type = ResourceRequest.Get_Resource_Count;
		Global.dbRunner.addWork(request);
		Global.dbClientWait(id);
		ResourceRequest req = Global.dbRunner.resourceResponse.get(id).copy();
		return req.responseInteger;
	}
}
