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
import cx.fbn.nevernote.utilities.ListManager;

public class WatchFolderTable {
	ListManager parent;
	private final ApplicationLogger 		logger;
	private final DatabaseConnection		db;

	
	// Constructor
	public WatchFolderTable(ApplicationLogger l, DatabaseConnection d) {
		logger = l;
		db = d;
	}
	// Create the table
	public void createTable() {
		NSqlQuery query = new NSqlQuery(db.getConnection());
		logger.log(logger.HIGH, "Creating table WatchFolder...");
        if (!query.exec("Create table WatchFolders (folder varchar primary key, notebook varchar," +
        		"keep boolean, depth integer)"));
           	logger.log(logger.HIGH, "Table WatchFolders creation FAILED!!!"); 
	}
	// Drop the table
	public void dropTable() {
		NSqlQuery query = new NSqlQuery(db.getConnection());
		query.exec("Drop table WatchFolders");
	}
	// Add an folder
	public void addWatchFolder(String folder, String notebook, boolean keep, int depth) {
		if (exists(folder))
			expungeWatchFolder(folder);
        NSqlQuery query = new NSqlQuery(db.getConnection());
		query.prepare("Insert Into WatchFolders (folder, notebook, keep, depth) " +
				"values (:folder, :notebook, :keep, :depth)");
		query.bindValue(":folder", folder);
		query.bindValue(":notebook", notebook);
		query.bindValue(":keep", keep);
		query.bindValue(":depth", depth);
		if (!query.exec()) {
			logger.log(logger.MEDIUM, "Insert into WatchFolder failed.");
		}
	}
	// Add an folder
	public boolean exists(String folder) {
        NSqlQuery query = new NSqlQuery(db.getConnection());
		query.prepare("Select folder from WatchFolders where folder=:folder ");
		query.bindValue(":folder", folder);
		query.exec();
		if (!query.next()) 
			return false;
		else
			return true;
	}

	// remove an folder
	public void expungeWatchFolder(String folder) {
        NSqlQuery query = new NSqlQuery(db.getConnection());
		query.prepare("delete from WatchFolders where folder=:folder");
		query.bindValue(":folder", folder);
		if (!query.exec()) {
			logger.log(logger.MEDIUM, "Expunge WatchFolder failed.");
			logger.log(logger.MEDIUM, query.lastError());
		}
	}
	public void expungeAll() {
        NSqlQuery query = new NSqlQuery(db.getConnection());
		if (!query.exec("delete from WatchFolders")) {
			logger.log(logger.MEDIUM, "Expunge all WatchFolder failed.");
			logger.log(logger.MEDIUM, query.lastError());
		}
	}
	public List<WatchFolderRecord> getAll() {
		logger.log(logger.HIGH, "Entering RWatchFolders.getAll");
		
		List<WatchFolderRecord> list = new ArrayList<WatchFolderRecord>();
		NSqlQuery query = new NSqlQuery(db.getConnection());
		query.exec("Select folder, (select name from notebook where guid = notebook), keep, depth from WatchFolders");
		while (query.next()) {
			WatchFolderRecord record = new WatchFolderRecord();
			record.folder = query.valueString(0);
			record.notebook = query.valueString(1);
			record.keep = new Boolean(query.valueString(2));
			record.depth = new Integer(query.valueString(3));
			list.add(record);
		}
		logger.log(logger.HIGH, "Leaving RWatchFolders.getAll");
		return list;

	}
	
	public String getNotebook(String dir) {
		logger.log(logger.HIGH, "Entering RWatchFolders.getNotebook");
		NSqlQuery query = new NSqlQuery(db.getConnection());
		query.prepare("Select notebook from WatchFolders where folder=:dir");
		query.bindValue(":dir", dir);
		query.exec();
		String response = null;
		while (query.next()) {
			response = query.valueString(0);
		}
		logger.log(logger.HIGH, "Leaving RWatchFolders.getNotebook");
		return response;

	}
}
