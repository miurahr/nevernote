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

import com.evernote.edam.type.Tag;

import cx.fbn.nevernote.Global;
import cx.fbn.nevernote.sql.requests.DeletedItemRequest;
import cx.fbn.nevernote.sql.requests.TagRequest;

public class TagTable {
	int id;
	
	public TagTable (int i) {
		id = i;
	}
	// Create the table
	public void createTable() {
		TagRequest request = new TagRequest();
		request.requestor_id = id;
		request.type = TagRequest.Create_Table;
		Global.dbRunner.addWork(request);
 	}
	// Drop the table
	public void dropTable() {
		TagRequest request = new TagRequest();
		request.requestor_id = id;
		request.type = DeletedItemRequest.Drop_Table;
		Global.dbRunner.addWork(request);
	}
	// get all tags
	public List<Tag> getAll() {
		TagRequest request = new TagRequest();
		request.requestor_id = id;
		request.type = DeletedItemRequest.Get_All;
		Global.dbRunner.addWork(request);
		Global.dbClientWait(id);
		TagRequest req = Global.dbRunner.tagResponse.get(id).copy();
		return req.responseTags;
	}
	public Tag getTag(String guid) {
		TagRequest request = new TagRequest();
		request.requestor_id = id;
		request.string1 = new String(guid);
		request.type = TagRequest.Get_Tag;
		Global.dbRunner.addWork(request);
		Global.dbClientWait(id);
		TagRequest req = Global.dbRunner.tagResponse.get(id).copy();
		return req.responseTag;
	}
	// Update a tag
	public void updateTag(Tag tempTag, boolean isDirty) {
		TagRequest request = new TagRequest();
		request.requestor_id = id;
		request.type = TagRequest.Update_Tag;
		request.tag = tempTag.deepCopy();
		request.bool1 = isDirty;
		Global.dbRunner.addWork(request);

	}
	// Delete a tag
	public void expungeTag(String guid, boolean needsSync) {
		TagRequest request = new TagRequest();
		request.requestor_id = id;
		request.string1 = new String(guid);
		request.bool1 = needsSync;
		request.type = TagRequest.Expunge_Tag;
		Global.dbRunner.addWork(request);
	}
	// Save a tag
	public void addTag(Tag tempTag, boolean isDirty) {
		TagRequest request = new TagRequest();
		request.requestor_id = id;
		request.type = TagRequest.Add_Tag;
		request.tag = tempTag.deepCopy();
		request.bool1 = isDirty;
		Global.dbRunner.addWork(request);
	}
	// Update a tag's parent
	public void updateTagParent(String guid, String parentGuid) {
		TagRequest request = new TagRequest();
		request.requestor_id = id;
		request.type = TagRequest.Update_Parent;
		request.string1 = new String(guid);
		request.string2 = new String(parentGuid);
		Global.dbRunner.addWork(request);
	}
	//Save tags from Evernote
	public void saveTags(List<Tag> tags) {
		TagRequest request = new TagRequest();
		request.requestor_id = id;
		request.type = TagRequest.Save_Tags;
		for (int i=0; i<tags.size(); i++) 
			request.tags.add(tags.get(i).deepCopy());
		Global.dbRunner.addWork(request);
	}
	// Update a tag sequence number
	public void updateTagSequence(String guid, int sequence) {
		TagRequest request = new TagRequest();
		request.requestor_id = id;
		request.type = TagRequest.Update_Tag_Sequence;
		request.string1 = new String(guid);
		request.int1 = sequence;
		Global.dbRunner.addWork(request);
	}
	// Update a tag sequence number
	public void updateTagGuid(String oldGuid, String newGuid) {
		TagRequest request = new TagRequest();
		request.requestor_id = id;
		request.string1 = new String(oldGuid);
		request.string2 = new String(newGuid);
		request.type = TagRequest.Update_Tag_Guid;
		Global.dbRunner.addWork(request);
	}
	// Get dirty tags
	public List<Tag> getDirty() {
		TagRequest request = new TagRequest();
		request.requestor_id = id;
		request.type = TagRequest.Get_Dirty;
		Global.dbRunner.addWork(request);
		Global.dbClientWait(id);
		TagRequest req = Global.dbRunner.tagResponse.get(id).copy();
		return req.responseTags;
	}
	// Find a guid based upon the name
	public String findTagByName(String name) {
		TagRequest request = new TagRequest();
		request.requestor_id = id;
		request.type = TagRequest.Find_Tag_By_Name;
		request.string1 = new String(name);
		Global.dbRunner.addWork(request);
		Global.dbClientWait(id);
		TagRequest req = Global.dbRunner.tagResponse.get(id).copy();
		return req.responseString;
	}
	// given a guid, does the tag exist
	public boolean exists(String guid) {
		TagRequest request = new TagRequest();
		request.requestor_id = id;
		request.string1 = new String(guid);
		request.type = TagRequest.Exists;
		Global.dbRunner.addWork(request);
		Global.dbClientWait(id);
		TagRequest req = Global.dbRunner.tagResponse.get(id).copy();
		return req.responseBool;

	}
	// This is a convience method to check if a tag exists & update/create based upon it
	public void syncTag(Tag tag, boolean isDirty) {
		TagRequest request = new TagRequest();
		request.requestor_id = id;
		request.tag = tag.deepCopy();
		request.bool1 = isDirty;
		request.type = TagRequest.Sync_Tag;
		Global.dbRunner.addWork(request);
	}
	public void  resetDirtyFlag(String guid) {
		TagRequest request = new TagRequest();
		request.requestor_id = id;
		request.type = TagRequest.Reset_Dirty_Flag;
		request.string1 = new String(guid);
		Global.dbRunner.addWork(request);
	}
}
