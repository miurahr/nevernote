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
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import com.trolltech.qt.sql.QJdbc;

import cx.fbn.nevernote.Global;
import cx.fbn.nevernote.sql.driver.NSqlQuery;
import cx.fbn.nevernote.utilities.ApplicationLogger;


public class DatabaseConnection {
	// Table helpers
	private WordsTable					wordsTable;
	private TagTable					tagTable;
	private NotebookTable				notebookTable;
	private NoteTable					noteTable;
	private DeletedTable				deletedTable;
	private SavedSearchTable			searchTable;
	private WatchFolderTable			watchFolderTable;
	private InvalidXMLTable				invalidXMLTable;
	private LinkedNotebookTable			linkedNotebookTable;
	private SharedNotebookTable			sharedNotebookTable;
	private SyncTable					syncTable;
	private final ApplicationLogger		logger;
	private Connection					conn;
	int id;

	
	public DatabaseConnection(ApplicationLogger l, String url, String userid, String password, String cypherPassword) {
		logger = l;
		dbSetup(url, userid, password, cypherPassword);
	}
	
	private void setupTables() {
		tagTable = new TagTable(logger, this);
		notebookTable = new NotebookTable(logger, this);
		noteTable = new NoteTable(logger, this);
		deletedTable = new DeletedTable(logger, this);
		searchTable = new SavedSearchTable(logger, this);	
		watchFolderTable = new WatchFolderTable(logger, this);
		invalidXMLTable = new InvalidXMLTable(logger, this);
		wordsTable = new WordsTable(logger, this);
		syncTable = new SyncTable(logger, this);
		linkedNotebookTable = new LinkedNotebookTable(logger, this);
		sharedNotebookTable = new SharedNotebookTable(logger, this);
	}
	
	
	// Compact the database
	public void compactDatabase() {
		
	}
	
	// Initialize the database connection
	public void dbSetup(String url,String userid, String userPassword, String cypherPassword) {
		logger.log(logger.HIGH, "Entering DatabaseConnection.dbSetup " +id);

		
		try {
			Class.forName("org.h2.Driver");
		} catch (ClassNotFoundException e1) {
			e1.printStackTrace();
			System.exit(16);
		}
		
		QJdbc.initialize();
		
		setupTables();
		
		File f = Global.getFileManager().getDbDirFile(Global.databaseName + ".h2.db");
		boolean dbExists = f.exists(); 
		
		logger.log(logger.HIGH, "Entering RDatabaseConnection.dbSetup");
		

		try {
			String passwordString = null;
			if (cypherPassword==null || cypherPassword.trim().equals(""))
				passwordString = userPassword;
			else
				passwordString = cypherPassword+" "+userPassword;
			conn = DriverManager.getConnection(url,userid,passwordString);
//			conn = DriverManager.getConnection(url+";AUTO_SERVER=TRUE",userid,passwordString);
		} catch (SQLException e) {
			e.printStackTrace();
			return;
		}
		
		// If it doesn't exist and we are the main thread, then we need to create stuff.
		if (!dbExists)  {
			createTables();
			Global.setAutomaticLogin(false);
		}		
		
		logger.log(logger.HIGH, "Leaving DatabaseConnection.dbSetup" +id);
	}
	
	
	public void dbShutdown() {
		logger.log(logger.HIGH, "Entering RDatabaseConnection.dbShutdown");
		try {
			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		logger.log(logger.HIGH, "Leaving RDatabaseConnection.dbShutdown");
	}
	
	public void upgradeDb(String version) {
		if (version.equals("0.85")) {
			executeSql("alter table note add column titleColor integer");
			executeSql("alter table note add column thumbnail blob");
			executeSql("alter table note add column thumbnailneeded boolean");
			executeSql("Update note set thumbnailneeded = true;");
			executeSql("create index NOTE_NOTEBOOK_INDEX on note (notebookguid, guid);");
			executeSql("create index NOTETAGS_TAG_INDEX on notetags (tagguid, noteguid);");
			version = "0.86";
			Global.setDatabaseVersion(version);
		} 
		if (version.equals("0.86")) {
/*			sharedNotebookTable.dropTable();
			linkedNotebookTable.dropTable();
			
			executeSql("alter table notebook drop column publishingUri");
			executeSql("alter table notebook drop column publishingOrder");
			executeSql("alter table notebook drop column publishingAscending");
			executeSql("alter table notebook drop column publishingPublicDescription");
			executeSql("alter table notebook drop column stack");
			executeSql("alter table notebook drop column icon");
			executeSql("alter table tag drop column icon");
			executeSql("alter table SavedSearch drop column icon");
			
			executeSql("drop index NOTE_THUMBNAIL_INDEX;");
			executeSql("drop index NOTE_EXPUNGED_INDEX;");
			executeSql("drop index NOTE_DUEDATE_INDEX;");
			executeSql("drop index RESOURCES_GUID_INDEX;");
*/		
			executeSql("alter table notebook add column publishingUri VarChar");
			executeSql("alter table notebook add column publishingOrder Integer");
			executeSql("alter table notebook add column publishingAscending VarChar");
			executeSql("alter table notebook add column publishingPublicDescription varchar");
			executeSql("alter table notebook add column stack varchar");
			executeSql("alter table notebook add column icon blob");
			executeSql("alter table tag add column icon blob");
			executeSql("alter table SavedSearch add column icon blob");

			executeSql("create index NOTE_THUMBNAIL_INDEX on note (thumbnailneeded, guid);");
			executeSql("create index NOTE_EXPUNGED_INDEX on note (isExpunged, guid);");
			executeSql("create index NOTE_DUEDATE_INDEX on note (attributeSubjectDate, guid);");
			executeSql("create index RESOURCES_GUID_INDEX on noteresources (noteGuid, guid);");
			executeSql("update note set thumbnailneeded=true, thumbnail=null;");
			
			sharedNotebookTable.createTable();
			linkedNotebookTable.createTable();
			
			version = "0.95";
			executeSql("Insert into Sync (key, value) values ('FullLinkedNotebookSync', 'true')");
			executeSql("Insert into Sync (key, value) values ('FullSharedNotebookSync', 'true')");
			Global.setDatabaseVersion(version);
		} 
	}
	
	public void executeSql(String sql) {
		NSqlQuery query = new NSqlQuery(conn);
		query.exec(sql);	
	}
	
	public void checkDatabaseVersion() {
		if (!Global.getDatabaseVersion().equals("0.86")) {
			upgradeDb(Global.getDatabaseVersion());
		}
		if (!Global.getDatabaseVersion().equals("0.95")) {
			upgradeDb(Global.getDatabaseVersion());
		}
	}
	

	public void backupDatabase(int highSequence, long date) {
		
	}
	
	
	public void createTables() {
		Global.setDatabaseVersion("0.85");
//		Global.setDatabaseVersion("0.95");
		Global.setAutomaticLogin(false);
		Global.saveCurrentNoteGuid("");
		Global.saveUploadAmount(0);
		
		getTagTable().createTable();
		notebookTable.createTable();
		noteTable.createTable();
		deletedTable.createTable();		
		searchTable.createTable();
		watchFolderTable.createTable();
		invalidXMLTable.createTable();
		wordsTable.createTable();
		syncTable.createTable();
		
	}
	
	public Connection getConnection() {
		return conn;
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
	public LinkedNotebookTable getLinkedNotebookTable() {
		return linkedNotebookTable;
	}
	public SharedNotebookTable getSharedNotebookTable() {
		return sharedNotebookTable;
	}
}
