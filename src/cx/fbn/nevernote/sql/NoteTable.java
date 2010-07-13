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

import com.evernote.edam.type.Note;
import com.trolltech.qt.core.QByteArray;
import com.trolltech.qt.core.QDateTime;

import cx.fbn.nevernote.Global;
import cx.fbn.nevernote.sql.requests.NoteRequest;
import cx.fbn.nevernote.utilities.Pair;

public class NoteTable {
	public NoteResourceTable				noteResourceTable;
	public NoteTagsTable					noteTagsTable;
	
	int id;
	
	// Constructor
	public NoteTable(int i) {
		id = i;
		noteTagsTable = new NoteTagsTable(id);
		noteResourceTable = new NoteResourceTable(id);
	}
	// Create the table
	public void createTable() {
		NoteRequest request = new NoteRequest();
		request.requestor_id = id;
		request.type = NoteRequest.Create_Table;
		Global.dbRunner.addWork(request);
	}
	// Drop the table
	public void dropTable() {
		NoteRequest request = new NoteRequest();
		request.requestor_id = id;
		request.type = NoteRequest.Drop_Table;
		Global.dbRunner.addWork(request);
	}
	// Save Note List from Evernote 
	public void addNote(Note n, boolean isDirty) {
		NoteRequest request = new NoteRequest();
		request.requestor_id = id;
		request.note = n.deepCopy();
		request.bool1 = isDirty;
		request.type = NoteRequest.Add_Note;
		Global.dbRunner.addWork(request);
	} 
	// Get a note by Guid
	public Note getNote(String noteGuid, boolean loadContent, boolean loadResources, boolean loadRecognition, boolean loadBinary, boolean loadTags) {
		NoteRequest request = new NoteRequest();
		request.requestor_id = id;
		request.type = NoteRequest.Get_Note;
		request.string1 = new String(noteGuid);
		request.bool1 = loadContent;
		request.bool2 = loadResources;
		request.bool3 = loadRecognition;
		request.bool4 = loadBinary;
		request.bool5 = loadTags;
		Global.dbRunner.addWork(request);
		Global.dbClientWait(id);
		NoteRequest req = Global.dbRunner.noteResponse.get(id).copy();
		return req.responseNote;
	}
	// Update a note's title
	public void updateNoteTitle(String guid, String title) {
		NoteRequest request = new NoteRequest();
		request.requestor_id = id;
		request.type = NoteRequest.Update_Note_Title;
		request.string1 = new String(guid);
		request.string2 = new String(title);
		Global.dbRunner.addWork(request);
	}
	// Update a note's creation date
	public void updateNoteCreatedDate(String guid, QDateTime date) {
		NoteRequest request = new NoteRequest();
		request.requestor_id = id;
		request.type = NoteRequest.Update_Note_Creation_Date;
		request.string1 = new String(guid);
		request.date = new QDateTime(date);
		Global.dbRunner.addWork(request);
	}
	// Update a note's creation date
	public void updateNoteAlteredDate(String guid, QDateTime date) {
		NoteRequest request = new NoteRequest();
		request.requestor_id = id;
		request.type = NoteRequest.Update_Note_Altered_Date;
		request.string1 = new String(guid);
		request.date = new QDateTime(date);
		Global.dbRunner.addWork(request);
	}
	// Update a note's creation date
	public void updateNoteSubjectDate(String guid, QDateTime date) {
		NoteRequest request = new NoteRequest();
		request.requestor_id = id;
		request.type = NoteRequest.Update_Note_Subject_Date;
		request.string1 = new String(guid);
		request.date = new QDateTime(date);
		Global.dbRunner.addWork(request);
	}
	// Update a note's author
	public void updateNoteAuthor(String guid, String author) {
		NoteRequest request = new NoteRequest();
		request.requestor_id = id;
		request.type = NoteRequest.Update_Note_Author;
		request.string1 = new String(guid);
		request.string2 = new String(author);
		Global.dbRunner.addWork(request);
	}
	// Update a note's creation date
	public void updateNoteSourceUrl(String guid, String url) {
		NoteRequest request = new NoteRequest();
		request.requestor_id = id;
		request.type = NoteRequest.Update_Note_Source_Url;
		request.string1 = new String(guid);
		request.string2 = new String(url);
		Global.dbRunner.addWork(request);
	}
	// Update the notebook that a note is assigned to
	public void updateNoteNotebook(String guid, String notebookGuid, boolean expungeFromRemote) {
		NoteRequest request = new NoteRequest();
		request.requestor_id = id;
		request.type = NoteRequest.Update_Note_Notebook;
		request.string1 = new String(guid);
		request.string2 = new String(notebookGuid);
		request.bool1 = expungeFromRemote;
		Global.dbRunner.addWork(request);
	}
	// Update a note's content
	public void updateNoteContent(String guid, String content) {
		NoteRequest request = new NoteRequest();
		request.requestor_id = id;
		request.type = NoteRequest.Update_Note_Content;
		request.string1 = new String(guid);
		request.string2 = new String(content);
		Global.dbRunner.addWork(request);
	}
	// Get a note's contents as a binary string (useful for unicode on sync)
	public String getNoteContentBinary(String guid) {
		NoteRequest request = new NoteRequest();
		request.requestor_id = id;
		request.type = NoteRequest.Get_Note_Content_Binary;
		request.string1 = new String(guid);
		Global.dbRunner.addWork(request);
		Global.dbClientWait(id);
		NoteRequest req = Global.dbRunner.noteResponse.get(id).copy();
		return req.responseString;
	}
	// Check a note to see if it passes the attribute selection criteria
	public boolean checkAttributeSelection(Note n) {
		if (Global.createdSinceFilter.check(n) &&
			Global.createdBeforeFilter.check(n) && 
			Global.changedSinceFilter.check(n) &&
			Global.changedBeforeFilter.check(n) &&
			Global.containsFilter.check(this, n))
				return true;
		
		return false;
	}
	// Delete a note
	public void deleteNote(String guid) {
		NoteRequest request = new NoteRequest();
		request.requestor_id = id;
		request.type = NoteRequest.Delete_Note;
		request.string1 = new String(guid);
		Global.dbRunner.addWork(request);
	}
	public void restoreNote(String guid) {
		NoteRequest request = new NoteRequest();
		request.requestor_id = id;
		request.type = NoteRequest.Restore_Note;
		request.string1 = new String(guid);
		Global.dbRunner.addWork(request);
	}
	// Purge a note (actually delete it instead of just marking it deleted)
	public void expungeNote(String guid, boolean permanentExpunge, boolean needsSync) {
		NoteRequest request = new NoteRequest();
		request.requestor_id = id;
		request.type = NoteRequest.Expunge_Note;
		request.string1 = new String(guid);
		request.bool1 = permanentExpunge;
		request.bool2 = needsSync;
		Global.dbRunner.addWork(request);
	}
	// Purge all deleted notes;
	public void expungeAllDeletedNotes() {
		NoteRequest request = new NoteRequest();
		request.requestor_id = id;
		request.type = NoteRequest.Expunge_All_Deleted_Notes;
		Global.dbRunner.addWork(request);
	}
	// Update the note sequence number
	public void updateNoteSequence(String guid, int sequence) {
		NoteRequest request = new NoteRequest();
		request.requestor_id = id;
		request.type = NoteRequest.Update_Note_Sequence;
		request.string1 = new String(guid);
		request.int1 = sequence;
		Global.dbRunner.addWork(request);
	}
	// Update the note Guid
	public void updateNoteGuid(String oldGuid, String newGuid) {
		NoteRequest request = new NoteRequest();
		request.requestor_id = id;
		request.type = NoteRequest.Update_Note_Guid;
		request.string1 = new String(oldGuid);
		request.string2 = new String(newGuid);
		Global.dbRunner.addWork(request);
	}
	// Update a note
	public void updateNote(Note n, boolean isNew) {
		NoteRequest request = new NoteRequest();
		request.requestor_id = id;
		request.type = NoteRequest.Update_Note;
		request.note = n.deepCopy();
		request.bool1 = isNew;
		Global.dbRunner.addWork(request);
	}
	// Does a note exist?
	public boolean exists(String guid) {
		NoteRequest request = new NoteRequest();
		request.requestor_id = id;
		request.type = NoteRequest.Exists;
		request.string1 = new String(guid);
		Global.dbRunner.addWork(request);
		Global.dbClientWait(id);
		NoteRequest req = Global.dbRunner.noteResponse.get(id).copy();
		return req.responseBoolean;
	}
	// This is a convience method to check if a tag exists & update/create based upon it
	public void syncNote(Note note, boolean isDirty) {
		NoteRequest request = new NoteRequest();
		request.requestor_id = id;
		request.type = NoteRequest.Sync_Note;
		request.note = note.deepCopy();
		request.bool1 = isDirty;
		Global.dbRunner.addWork(request);
	}
	// Get a list of notes that need to be updated
	public List <Note> getDirty() {
		NoteRequest request = new NoteRequest();
		request.requestor_id = id;
		request.type = NoteRequest.Get_Dirty;
		Global.dbRunner.addWork(request);
		Global.dbClientWait(id);
		NoteRequest req = Global.dbRunner.noteResponse.get(id).copy();
		return req.responseNotes;
	}
	// Get a list of notes that need to be updated
	public List <String> getUnsynchronizedGUIDs() {
		NoteRequest request = new NoteRequest();
		request.requestor_id = id;
		request.type = NoteRequest.Get_Unsynchronized_Guids;
		Global.dbRunner.addWork(request);
		Global.dbClientWait(id);
		NoteRequest req = Global.dbRunner.noteResponse.get(id).copy();
		return req.responseStrings;
	}
	public boolean isNoteDirty(String guid) {
		NoteRequest request = new NoteRequest();
		request.requestor_id = id;
		request.type = NoteRequest.Is_Note_Dirty;
		request.string1 = new String(guid);
		Global.dbRunner.addWork(request);
		Global.dbClientWait(id);
		NoteRequest req = Global.dbRunner.noteResponse.get(id).copy();
		return req.responseBoolean;
	}
	// Reset the dirty bit
	public void  resetDirtyFlag(String guid) {
		NoteRequest request = new NoteRequest();
		request.requestor_id = id;
		request.type = NoteRequest.Reset_Dirty_Flag;
		request.string1 = new String(guid);
		Global.dbRunner.addWork(request);
	}
	// Get all notes
	public List<String> getAllGuids() {
		NoteRequest request = new NoteRequest();
		request.requestor_id = id;
		request.type = NoteRequest.Get_All_Guids;
		Global.dbRunner.addWork(request);
		Global.dbClientWait(id);
		NoteRequest req = Global.dbRunner.noteResponse.get(id).copy();
		return req.responseStrings;
	}
	// Get all notes
	public List<Note> getAllNotes() {
		NoteRequest request = new NoteRequest();
		request.requestor_id = id;
		request.type = NoteRequest.Get_All_Notes;
		Global.dbRunner.addWork(request);
		Global.dbClientWait(id);
		NoteRequest req = Global.dbRunner.noteResponse.get(id).copy();
		return req.responseNotes;
	}
	// Count unindexed notes
	public int getUnindexedCount() {
		NoteRequest request = new NoteRequest();
		request.requestor_id = id;
		request.type = NoteRequest.Get_Unindexed_Count;
		Global.dbRunner.addWork(request);
		Global.dbClientWait(id);
		NoteRequest req = Global.dbRunner.noteResponse.get(id).copy();
		return req.responseInt;
	}
	// Count unsynchronized count
	public int getDirtyCount() {
		NoteRequest request = new NoteRequest();
		request.requestor_id = id;
		request.type = NoteRequest.Get_Dirty_Count;
		Global.dbRunner.addWork(request);
		Global.dbClientWait(id);
		NoteRequest req = Global.dbRunner.noteResponse.get(id).copy();
		return req.responseInt;
	}
	// Count unindexed notes
	public int getNoteCount() {
		NoteRequest request = new NoteRequest();
		request.requestor_id = id;
		request.type = NoteRequest.Get_Note_Count;
		Global.dbRunner.addWork(request);
		Global.dbClientWait(id);
		NoteRequest req = Global.dbRunner.noteResponse.get(id).copy();
		return req.responseInt;
	}
	// Count deleted notes
	public int getDeletedCount() {
		NoteRequest request = new NoteRequest();
		request.requestor_id = id;
		request.type = NoteRequest.Get_Deleted_Count;
		Global.dbRunner.addWork(request);
		Global.dbClientWait(id);
		NoteRequest req = Global.dbRunner.noteResponse.get(id).copy();
		return req.responseInt;
	}
	// Reset a note's sequence count to zero.  This is useful when moving a conflicting note
	public void resetSequenceNumber(String guid) {
		NoteRequest request = new NoteRequest();
		request.requestor_id = id;
		request.string1 = new String(guid);
		request.type = NoteRequest.Reset_Note_Sequence;
		Global.dbRunner.addWork(request);
	}
	
	// Update a note resource by the hash
	public void updateNoteResourceGuidbyHash(String noteGuid, String resGuid, String hash) {
		NoteRequest request = new NoteRequest();
		request.requestor_id = id;
		request.type = NoteRequest.Update_Resource_Guid_By_Hash;
		request.string1 = new String(noteGuid);
		request.string2 = new String(resGuid);
		request.string3 = new String(hash);
		Global.dbRunner.addWork(request);
	}
	
	// Get the title color of notes
	public List<Pair<String,Integer>> getNoteTitleColors() {
		NoteRequest request = new NoteRequest();
		request.requestor_id = id;
		request.type = NoteRequest.Get_Title_Colors;
		Global.dbRunner.addWork(request);
		Global.dbClientWait(id);
		NoteRequest req = Global.dbRunner.noteResponse.get(id).copy();
		return req.responsePair;
	}
	
	// Get the title color of notes
	public void setNoteTitleColor(String guid, int color) {
		NoteRequest request = new NoteRequest();
		request.requestor_id = id;
		request.string1 = new String(guid);
		request.int1 = color;
		request.type = NoteRequest.Set_Title_Colors;
		Global.dbRunner.addWork(request);	
	}
	
	
	//********************************************************************************
	//********************************************************************************
	//* Indexing Functions
	//********************************************************************************
	//********************************************************************************
	// set/unset a note to be reindexed
	public void setIndexNeeded(String guid, Boolean flag) {
		NoteRequest request = new NoteRequest();
		request.requestor_id = id;
		request.type = NoteRequest.Set_Index_Needed;
		request.string1 = new String(guid);
		request.bool1 = flag;
		Global.dbRunner.addWork(request);
	}
	// Set all notes to be reindexed
	public void reindexAllNotes() {
		NoteRequest request = new NoteRequest();
		request.requestor_id = id;
		request.type = NoteRequest.Reindex_All_Notes;
		Global.dbRunner.addWork(request);
	}
	// Get all unindexed notes
	public List <String> getUnindexed() {
		NoteRequest request = new NoteRequest();
		request.requestor_id = id;
		request.type = NoteRequest.Get_Unindexed;
		Global.dbRunner.addWork(request);
		Global.dbClientWait(id);
		NoteRequest req = Global.dbRunner.noteResponse.get(id).copy();
		return req.responseStrings;
	}
	public List<String> getNextUnindexed(int limit) {
		NoteRequest request = new NoteRequest();
		request.requestor_id = id;
		request.type = NoteRequest.Get_Next_Unindexed;
		request.int1 = limit;
		Global.dbRunner.addWork(request);
		Global.dbClientWait(id);
		NoteRequest req = Global.dbRunner.noteResponse.get(id).copy();
		return req.responseStrings;
	}
	
	
	//*********************************************************************************
	//* Thumbnail Functions
	//*********************************************************************************
	// Set if a new thumbnail is needed
	public void setThumbnailNeeded(String guid, boolean needed) {
		NoteRequest request = new NoteRequest();
		request.requestor_id = id;
		request.type = NoteRequest.Set_Thumbnail_Needed;
		request.string1 = new String(guid);
		request.bool1 = needed;
		Global.dbRunner.addWork(request);
	}
	// Is a thumbail needed for this guid?
	public boolean isThumbnailNeeded(String guid) {
		NoteRequest request = new NoteRequest();
		request.requestor_id = id;
		request.type = NoteRequest.Is_Thumbail_Needed;
		request.string1 = new String(guid);
		Global.dbRunner.addWork(request);
		Global.dbClientWait(id);
		NoteRequest req = Global.dbRunner.noteResponse.get(id).copy();
		return req.responseBoolean;
	}
	// Set if a new thumbnail is needed
	public void setThumbnail(String guid, QByteArray thumbnail) {
		NoteRequest request = new NoteRequest();
		request.requestor_id = id;
		request.type = NoteRequest.Set_Thumbnail;
		request.string1 = new String(guid);
		request.bytes = thumbnail;
		Global.dbRunner.addWork(request);
	}
	// Set if a new thumbnail is needed
	public QByteArray getThumbnail(String guid) {
		NoteRequest request = new NoteRequest();
		request.requestor_id = id;
		request.type = NoteRequest.Get_Thumbnail;
		request.string1 = new String(guid);
		Global.dbRunner.addWork(request);
		Global.dbClientWait(id);
		NoteRequest req = Global.dbRunner.noteResponse.get(id).copy();
		return req.responseBytes;
	}
	

	
	// Update a note content's hash.  This happens if a resource is edited outside of NN
	public void updateResourceContentHash(String guid, String oldHash, String newHash) {
		NoteRequest request = new NoteRequest();
		request.requestor_id = id;
		request.type = NoteRequest.Update_Resource_Content_Hash;
		request.string1 = new String(guid);
		request.string2 = new String(oldHash);
		request.string3 = new String(newHash);
		Global.dbRunner.addWork(request);
	}
}	
