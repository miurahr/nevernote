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

import cx.fbn.nevernote.Global;
import cx.fbn.nevernote.sql.requests.NoteTagsRequest;
import cx.fbn.nevernote.sql.runners.NoteTagsRecord;
import cx.fbn.nevernote.utilities.Pair;

public class NoteTagsTable {
	private final int id;
	
	// Constructor
	public NoteTagsTable(int i) {
		id = i;
	}
	// Create the table
	public void createTable() {
		NoteTagsRequest request = new NoteTagsRequest();
		request.requestor_id = id;
		request.type = NoteTagsRequest.Create_Table;
		Global.dbRunner.addWork(request);
	}
	// Drop the table
	public void dropTable() {
		NoteTagsRequest request = new NoteTagsRequest();
		request.requestor_id = id;
		request.type = NoteTagsRequest.Drop_Table;
		Global.dbRunner.addWork(request);	}
	// Get a note tags by the note's Guid
	public List<String> getNoteTags(String noteGuid) {
		NoteTagsRequest request = new NoteTagsRequest();
		request.requestor_id = id;
		request.type = NoteTagsRequest.Get_Note_Tags;
		request.string1 = new String(noteGuid);
		Global.dbRunner.addWork(request);
		Global.dbClientWait(id);
		NoteTagsRequest req = Global.dbRunner.noteTagsResponse.get(id).copy();
		return req.responseStrings;
	}
	// Get a note tags by the note's Guid
	public List<NoteTagsRecord> getAllNoteTags() {
		NoteTagsRequest request = new NoteTagsRequest();
		request.requestor_id = id;
		request.type = NoteTagsRequest.Get_All_Note_Tags;
		Global.dbRunner.addWork(request);
		Global.dbClientWait(id);
		NoteTagsRequest req = Global.dbRunner.noteTagsResponse.get(id).copy();
		return req.responseNoteTagsRecord;
	}
	// Check if a note has a specific tag already
	public boolean checkNoteNoteTags(String noteGuid, String tagGuid) {
		NoteTagsRequest request = new NoteTagsRequest();
		request.requestor_id = id;
		request.type = NoteTagsRequest.Check_Note_Note_Tags;
		request.string1 = new String(noteGuid);
		request.string2 = new String(tagGuid);
		Global.dbRunner.addWork(request);
		Global.dbClientWait(id);
		NoteTagsRequest req = Global.dbRunner.noteTagsResponse.get(id).copy();
		return req.responseBoolean;
	}
	// Save Note Tags
	public void saveNoteTag(String noteGuid, String tagGuid) {
		NoteTagsRequest request = new NoteTagsRequest();
		request.requestor_id = id;
		request.type = NoteTagsRequest.Save_Note_Tag;
		request.string1 = new String(noteGuid);
		request.string2 = new String(tagGuid);
		Global.dbRunner.addWork(request);
	}
	// Delete a note's tags
	public void deleteNoteTag(String noteGuid) {
		NoteTagsRequest request = new NoteTagsRequest();
		request.requestor_id = id;
		request.type = NoteTagsRequest.Delete_Note_Tag;
		request.string1 = new String(noteGuid);
		Global.dbRunner.addWork(request);
	}
	// Get tag counts
	public List<Pair<String,Integer>> getTagCounts() {
		NoteTagsRequest request = new NoteTagsRequest();
		request.requestor_id = id;
		request.type = NoteTagsRequest.Tag_Counts;
		Global.dbRunner.addWork(request);
		Global.dbClientWait(id);
		NoteTagsRequest req = Global.dbRunner.noteTagsResponse.get(id).copy();
		return req.responseCounts;
		
	}
}
