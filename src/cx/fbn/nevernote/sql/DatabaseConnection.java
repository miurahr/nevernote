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

import java.io.File;

import cx.fbn.nevernote.Global;
import cx.fbn.nevernote.sql.requests.DatabaseRequest;
import cx.fbn.nevernote.utilities.ApplicationLogger;


public class DatabaseConnection {
	// Table helpers
	private final WordsTable				wordsTable;
	private final TagTable					tagTable;
	private final NotebookTable				notebookTable;
	private final NoteTable					noteTable;
	private final DeletedTable				deletedTable;
	private final SavedSearchTable			searchTable;
	private final WatchFolderTable			watchFolderTable;
	private final InvalidXMLTable			invalidXMLTable;
	private final SyncTable					syncTable;
	private final ApplicationLogger			logger;
	int id;

	
	public DatabaseConnection(ApplicationLogger l, int i) {
		id = i;
		tagTable = new TagTable(id);
		notebookTable = new NotebookTable(id);
		noteTable = new NoteTable(id);
		deletedTable = new DeletedTable(id);
		searchTable = new SavedSearchTable(id);
		watchFolderTable = new WatchFolderTable(id);
		wordsTable = new WordsTable(id);
		invalidXMLTable = new InvalidXMLTable(id);
		syncTable = new SyncTable(id);
		logger = l;

	}
	
	
	// Initialize the database connection
	public void dbSetup() {
		logger.log(logger.HIGH, "Entering DatabaseConnection.dbSetup " +id);

		File f = new File(Global.getDirectoryPath() +File.separator +"db" +File.separator +"NeverNote.h2.db");
		boolean dbExists = f.exists(); 
		
		// If it doesn't exist and we are the main thread, then we need to create stuff.
		if (!dbExists && id  == 0)  {
			createTables();
			Global.setAutomaticLogin(false);
		}
		
		logger.log(logger.HIGH, "Leaving DatabaseConnection.dbSetup" +id);
	}
	
	
	public void dbShutdown() {
		DatabaseRequest req = new DatabaseRequest();
		req.type = DatabaseRequest.Shutdown;
		Global.dbRunner.addWork(req);
	}
	
	public void upgradeDb(String version) {
		if (version.equals("0.85")) {
			DatabaseRequest req = new DatabaseRequest();
			req.type = DatabaseRequest.Execute_Sql;
			req.string1 = new String("alter table note add column titleColor integer");
			Global.dbRunner.addWork(req);
			Global.dbClientWait(id);
			req.type = DatabaseRequest.Execute_Sql;
			req.string1 = new String("update note set titlecolor=-1");
			Global.dbRunner.addWork(req);
			Global.dbClientWait(id);
			req.type = DatabaseRequest.Execute_Sql;
			req.string1 = new String("alter table note add column thumbnail blob");
			Global.dbRunner.addWork(req);
			Global.dbClientWait(id);
			req.string1 = new String("alter table note add column thumbnailneeded boolean");
			Global.dbRunner.addWork(req);
			Global.dbClientWait(id);
			req.string1 = new String("Update note set thumbnailneeded = true;");
			Global.dbRunner.addWork(req);
			Global.dbClientWait(id);
			req.string1 = new String("create index NOTE_NOTEBOOK_INDEX on note (notebookguid, guid);");
			Global.dbRunner.addWork(req);
			Global.dbClientWait(id);
			req.string1 = new String("create index NOTETAGS_TAG_INDEX on notetags (tagguid, noteguid);");
			Global.dbRunner.addWork(req);
			Global.dbClientWait(id);
			version = "0.86";
			Global.setDatabaseVersion(version);
		} 
	}
	
	public void checkDatabaseVersion() {
		if (!Global.getDatabaseVersion().equals("0.86")) {
			upgradeDb(Global.getDatabaseVersion());
		}
	}
	
	public void compactDatabase() {
		DatabaseRequest request = new DatabaseRequest();
		request.requestor_id = id;
		request.type = DatabaseRequest.Compact;
		Global.dbRunner.addWork(request);
		Global.dbClientWait(id);
	}

	public void backupDatabase(int highSequence, long date) {
		DatabaseRequest request = new DatabaseRequest();
		request.requestor_id = id;
		request.int1 = highSequence;
		request.long1 = date;
		request.type = DatabaseRequest.Backup_Database;
		Global.dbRunner.addWork(request);
		Global.dbClientWait(id);
	}
	
	
	public void createTables() {
		Global.setDatabaseVersion("0.85");
//		Global.setUpdateSequenceNumber(0);
		Global.setAutomaticLogin(false);
		Global.saveCurrentNoteGuid("");
		Global.saveUploadAmount(0);
		
	}
	
	//***************************************************************
	//* Table get methods
	//***************************************************************
	public DeletedTable getDeletedTable() {
		return deletedTable;
	}
	public TagTable getTagTable() {
		return tagTable;
	}
	public NoteTable getNoteTable() {
		return noteTable;
	}
	public NotebookTable getNotebookTable() {
		return notebookTable;
	}
	public SavedSearchTable getSavedSearchTable() {
		return searchTable;
	}
	public WatchFolderTable getWatchFolderTable() {
		return watchFolderTable;
	}
	public WordsTable getWordsTable() {
		return wordsTable;
	}
	public InvalidXMLTable getInvalidXMLTable() {
		return invalidXMLTable;
	}
	public SyncTable getSyncTable() {
		return syncTable;
	}
}
