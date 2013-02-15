/*
 * This file is part of NixNote 
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

import java.util.ArrayList;
import java.util.List;

import cx.fbn.nevernote.sql.driver.NSqlQuery;
import cx.fbn.nevernote.utilities.ApplicationLogger;
import cx.fbn.nevernote.utilities.Pair;

public class NoteTagsTable {
	private final ApplicationLogger 		logger;
	DatabaseConnection						db;
	NSqlQuery								getNoteTagsQuery;

	
	// Constructor
	public NoteTagsTable(ApplicationLogger l,DatabaseConnection d) {
		logger = l;
		db = d;
	}
	// Create the table
	public void createTable() {
		NSqlQuery query = new NSqlQuery(db.getConnection());
        // Create the NoteTag table
        logger.log(logger.HIGH, "Creating table NoteTags...");
        if (!query.exec("Create table NoteTags (noteGuid varchar, " +
        		"tagGuid varchar, primary key(noteGuid, tagGuid))"))
        	logger.log(logger.HIGH, "Table NoteTags creation FAILED!!!"); 
	}
	// Drop the table
	public void dropTable() {
		NSqlQuery query = new NSqlQuery(db.getConnection());
		query.exec("drop table NoteTags");
	}
	// Get a note tags by the note's Guid
	public List<String> getNoteTags(String noteGuid) {
		if (noteGuid == null)
			return null;
		List<String> tags = new ArrayList<String>();
		
		if (getNoteTagsQuery == null)
			prepareGetNoteTagsQuery();
		
		getNoteTagsQuery.bindValue(":guid", noteGuid);
		if (!getNoteTagsQuery.exec()) {
			logger.log(logger.EXTREME, "NoteTags SQL select has failed.");
			logger.log(logger.MEDIUM, getNoteTagsQuery.lastError());
			return null;
		}
		while (getNoteTagsQuery.next()) {
			tags.add(getNoteTagsQuery.valueString(0));
		}	
		return tags;
	}
	// Get a list of notes by the tag guid
	public List<String> getTagNotes(String tagGuid) {
		if (tagGuid == null)
			return null;
		List<String> notes = new ArrayList<String>();
		
		NSqlQuery query = new NSqlQuery(db.getConnection());
		query.prepare("Select NoteGuid from NoteTags where tagGuid = :guid");
		
		query.bindValue(":guid", tagGuid);
		if (!query.exec()) {
			logger.log(logger.EXTREME, "getTagNotes SQL select has failed.");
			logger.log(logger.MEDIUM, query.lastError());
			return notes;
		}
		while (query.next()) {
			notes.add(query.valueString(0));
		}	
		return notes;
	}
	void prepareGetNoteTagsQuery() {
		getNoteTagsQuery = new NSqlQuery(db.getConnection());
		getNoteTagsQuery.prepare("Select TagGuid from NoteTags where noteGuid = :guid");
	}
	// Get a note tags by the note's Guid
	public List<NoteTagsRecord> getAllNoteTags() {
		List<NoteTagsRecord> tags = new ArrayList<NoteTagsRecord>();
		
		NSqlQuery query = new NSqlQuery(db.getConnection());
		if (!query.exec("Select TagGuid, NoteGuid from NoteTags")) {
			logger.log(logger.EXTREME, "NoteTags SQL select has failed.");
			logger.log(logger.MEDIUM, query.lastError());
			return null;
		}
		while (query.next()) {
			NoteTagsRecord record = new NoteTagsRecord();
			record.tagGuid = query.valueString(0);
			record.noteGuid = query.valueString(1);
			tags.add(record);
		}	
		return tags;
	}
	// Check if a note has a specific tag already
	public boolean checkNoteNoteTags(String noteGuid, String tagGuid) {
		if (noteGuid == null || tagGuid == null)
			return false;
		boolean check;
		NSqlQuery query = new NSqlQuery(db.getConnection());
		check = query.prepare("Select "
				+"NoteGuid, TagGuid from NoteTags where noteGuid = :noteGuid and tagGuid = :tagGuid");
		if (!check)
			logger.log(logger.EXTREME, "checkNoteTags SQL prepare has failed.");
		
		query.bindValue(":noteGuid", noteGuid);
		query.bindValue(":tagGuid", tagGuid);
		query.exec();
		
		if (!check) {
			logger.log(logger.EXTREME, "checkNoteTags SQL select has failed.");
			logger.log(logger.MEDIUM, query.lastError());
			return false;
		}
		
		if (query.next()) {
			return true;
		}	
		return false;
	}
	// Save Note Tags
	public void saveNoteTag(String noteGuid, String tagGuid, boolean isDirty) {
		boolean check;
		NSqlQuery query = new NSqlQuery(db.getConnection());

		check = query.prepare("Insert Into NoteTags (noteGuid, tagGuid) "
				+"Values("
				+":noteGuid, :tagGuid)");
		if (!check)
			logger.log(logger.EXTREME, "Note SQL insert prepare has failed.");
	
		query.bindValue(":noteGuid", noteGuid);
		query.bindValue(":tagGuid", tagGuid);
						
		check = query.exec();
		if (!check) {
			logger.log(logger.MEDIUM, "NoteTags Table insert failed.");		
			logger.log(logger.MEDIUM, query.lastError());
		}
		check = query.prepare("Update Note set isDirty=:isDirty where guid=:guid");
		if (!check)
			logger.log(logger.EXTREME, "RNoteTagsTable.saveNoteTag prepare has failed.");
		query.bindValue(":isDirty", isDirty);
		query.bindValue(":guid", noteGuid);
		query.exec();
		if (!check) {
			logger.log(logger.MEDIUM, "RNoteTagsTable.saveNoteTag has failed to set note as dirty.");		
			logger.log(logger.MEDIUM, query.lastError());
		}
	}
	// Delete a note's tags
	public void deleteNoteTag(String noteGuid) {
		boolean check;
		NSqlQuery query = new NSqlQuery(db.getConnection());
		check = query.prepare("Delete from NoteTags where noteGuid = :noteGuid");
		if (!check)
			logger.log(logger.EXTREME, "Note SQL delete prepare has failed.");
	
		query.bindValue(":noteGuid", noteGuid);
		check = query.exec();
		if (!check) {
			logger.log(logger.MEDIUM, "NoteTags Table delete failed.");		
			logger.log(logger.MEDIUM, query.lastError());
		}

	}
	// Get a note tag counts
	public List<Pair<String,Integer>> getTagCounts() {
		List<Pair<String,Integer>> counts = new ArrayList<Pair<String,Integer>>();		
		NSqlQuery query = new NSqlQuery(db.getConnection());
		if (!query.exec("select tagguid, count(noteguid) from notetags group by tagguid;")) {
			logger.log(logger.EXTREME, "NoteTags SQL getTagCounts has failed.");
			logger.log(logger.MEDIUM, query.lastError());
			return null;
		}
		while (query.next()) {
			Pair<String,Integer> newCount = new Pair<String,Integer>();
			newCount.setFirst(query.valueString(0));
			newCount.setSecond(query.valueInteger(1));
			counts.add(newCount);
		}	
		return counts;
	}
}
