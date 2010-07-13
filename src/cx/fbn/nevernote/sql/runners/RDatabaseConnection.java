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
package cx.fbn.nevernote.sql.runners;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import com.trolltech.qt.sql.QJdbc;

import cx.fbn.nevernote.Global;
import cx.fbn.nevernote.sql.driver.NSqlQuery;
import cx.fbn.nevernote.utilities.ApplicationLogger;

public class RDatabaseConnection {
	// Table helpers
	private RTagTable					tagTable;
	private RNotebookTable				notebookTable;
	private RNoteTable					noteTable;
	private RDeletedTable				deletedTable;
	private RSavedSearchTable			searchTable;
	private RWatchFolderTable			watchFolderTable;
	private RInvalidXMLTable			invalidXMLTable;
	private final ApplicationLogger 	logger;
	private Connection					conn;
	private final String				databaseName;
	private RWordsTable					wordsTable;
	private RSyncTable					syncTable;

	
	public RDatabaseConnection(ApplicationLogger l, String c) {
		logger = l;
		databaseName = Global.databaseName;
	}
	
	private void setupTables() {
		tagTable = new RTagTable(logger, this);
		notebookTable = new RNotebookTable(logger, this);
		noteTable = new RNoteTable(logger, this);
		deletedTable = new RDeletedTable(logger, this);
		searchTable = new RSavedSearchTable(logger, this);	
		watchFolderTable = new RWatchFolderTable(logger, this);
		invalidXMLTable = new RInvalidXMLTable(logger, this);
		wordsTable = new RWordsTable(logger, this);
		syncTable = new RSyncTable(logger, this);
	}
	
    //***************************************************************
    //***************************************************************
    //** These functions deal starting & stopping the database
    //***************************************************************
    //***************************************************************
	// Initialize the database connection
	public void dbSetup(String url,String userid, String userPassword, String cypherPassword) {
		logger.log(logger.HIGH, "Entering RDatabaseConnection.dbSetup");
		
		// This thread cleans things up if we crash
//		ShutdownRunner shutdownRunner = new ShutdownRunner(this);
//		Runtime.getRuntime().addShutdownHook(shutdownRunner);

/*		if (QSqlDatabase.isDriverAvailable("QSQLITE"))
			logger.log(logger.MEDIUM, "SQL Driver check was successful.");
			else {
				logger.log(logger.MEDIUM, "SQL Driver check has failed.");
				System.err.println("Database failure");
				System.exit(16);
			}
*/

		try {
			Class.forName("org.h2.Driver");
		} catch (ClassNotFoundException e1) {
			e1.printStackTrace();
			System.exit(16);
		}
		
		QJdbc.initialize();
//		db = QSqlDatabase.addDatabase("QSQLITE", connectionName); 		
//		db = QSqlDatabase.addDatabase("QJDBC", connectionName); 
//		db.setDatabaseName("jdbc:h2:"+Global.getDirectoryPath() +File.separator +"db" +File.separator +databaseName);
		
		setupTables();
		
		File f = new File(Global.getDirectoryPath() +File.separator +"db" +File.separator +databaseName +".h2.db");
		boolean dbExists = f.exists(); 

		try {
			String passwordString = null;
			if (cypherPassword==null || cypherPassword.trim().equals(""))
				passwordString = userPassword;
			else
				passwordString = cypherPassword+" "+userPassword;
			conn = DriverManager.getConnection(url,userid,passwordString);
		} catch (SQLException e) {
			e.printStackTrace();
			return;
		}

		if (!dbExists)
			createTables();
		
		logger.log(logger.HIGH, "Leaving RDatabaseConnection.dbSetup");
	}
	// Tear down the database connections
	public void dbShutdown() {
		logger.log(logger.HIGH, "Entering RDatabaseConnection.dbShutdown");
		try {
			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		logger.log(logger.HIGH, "Leaving RDatabaseConnection.dbShutdown");
	}
	
	public void backupDatabase() {
	}
	
	public void executeSql(String sql) {
		NSqlQuery query = new NSqlQuery(conn);
		query.exec(sql);	
	}
	
	// Create note tables
	public void dropTables() { 
		logger.log(logger.HIGH, "Entering RDatabaseConnection.dropTables");
        getTagTable().dropTable();
        getNotebookTable().dropTable();
        getNoteTable().dropTable();
        getDeletedTable().dropTable();
        getSavedSearchTable().dropTable();
		logger.log(logger.HIGH, "Leaving RDatabaseConnection.dropTables");
	}
	public void createTables() {
		logger.log(logger.HIGH, "Entering RDatabaseConnection.createTables");
		getTagTable().createTable();
		notebookTable.createTable();
		noteTable.createTable();
		deletedTable.createTable();		
		searchTable.createTable();
		watchFolderTable.createTable();
		invalidXMLTable.createTable();
		wordsTable.createTable();
		syncTable.createTable();
		logger.log(logger.HIGH, "Leaving RDatabaseConnection.createTables");
	}
	

	public void compactDatabase() {
		logger.log(logger.HIGH, "Entering RDatabaseConnection.compactDatabase");
		NSqlQuery query = new NSqlQuery(conn);
		query.exec("vacuum");
		logger.log(logger.HIGH, "Leaving RDatabaseConnection.PragmaSettings");
	}
	
	
	public Connection getConnection() {
		return conn;
	}
	
	//***************************************************************
	//* Table get methods
	//***************************************************************
	public RDeletedTable getDeletedTable() {
		return deletedTable;
	}
	public RTagTable getTagTable() {
		return tagTable;
	}
	public RNoteTable getNoteTable() {
		return noteTable;
	}
	public RNotebookTable getNotebookTable() {
		return notebookTable;
	}
	public RSavedSearchTable getSavedSearchTable() {
		return searchTable;
	}
	public RWatchFolderTable getWatchFolderTable() {
		return watchFolderTable;
	}
	public RInvalidXMLTable getInvalidXMLTable() {
		return invalidXMLTable;
	}
	public RWordsTable		getWordsTable() {
		return wordsTable;
	}
	public RSyncTable		getSyncTable() {
		return syncTable;
	}
}
