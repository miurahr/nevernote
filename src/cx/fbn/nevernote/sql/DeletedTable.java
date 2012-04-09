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

public class DeletedTable {
	ListManager parent;
	private final ApplicationLogger 		logger;
	private final DatabaseConnection		db;

	
	// Constructor
	public DeletedTable(ApplicationLogger l,DatabaseConnection d) {
		logger = l;
		db = d;
	}
	// Create the table
	public void createTable() {
		NSqlQuery query = new NSqlQuery(db.getConnection());
		logger.log(logger.HIGH, "Creating table DeletedItems...");
        if (!query.exec("Create table DeletedItems (guid varchar primary key, type varchar)"))
           	logger.log(logger.HIGH, "Table DeletedItems creation FAILED!!!"); 
	}
	// Drop the table
	public void dropTable() {
		NSqlQuery query = new NSqlQuery(db.getConnection());
		query.exec("Drop table DeletedItems");
	}
	// Add an item to the deleted table
	public void addDeletedItem(String guid, String type) {
		if (exists(guid,type))
			return;
        NSqlQuery query = new NSqlQuery(db.getConnection());
		query.prepare("Insert Into DeletedItems (guid, type) Values(:guid, :type)");
		query.bindValue(":guid", guid);
		query.bindValue(":type", type);
		if (!query.exec()) {
			logger.log(logger.MEDIUM, "Insert into deleted items failed.");
			logger.log(logger.MEDIUM, query.lastError());
		}
	}
	// Check if a record exists
	public boolean exists(String guid, String type) {
        NSqlQuery query = new NSqlQuery(db.getConnection());
		query.prepare("Select guid, type from DeletedItems where guid=:guid and type=:type");
		query.bindValue(":guid", guid);
		query.bindValue(":type", type);
		query.exec();
		if (!query.next()) {
			return false;
		}
		return true;
	}
	// Add an item to the deleted table
	public void expungeDeletedItem(String guid, String type) {
        NSqlQuery query = new NSqlQuery(db.getConnection());
		query.prepare("delete from DeletedItems where guid=:guid and type=:type");
		query.bindValue(":guid", guid);
		query.bindValue(":type", type);
		if (!query.exec()) {
			logger.log(logger.MEDIUM, "Expunge deleted items failed.");
			logger.log(logger.MEDIUM, query.lastError());
		}
	}
	public List<DeletedItemRecord> getAllDeleted() {
		logger.log(logger.HIGH, "Entering DeletedTable.getAllDeleted");
		List<DeletedItemRecord> list = new ArrayList<DeletedItemRecord>();
		NSqlQuery query = new NSqlQuery(db.getConnection());
		query.exec("Select guid, type from DeletedItems");
		while (query.next()) {
			DeletedItemRecord record = new DeletedItemRecord();
			record.guid = query.valueString(0);
			record.type = query.valueString(1);
			list.add(record);
		}
		logger.log(logger.HIGH, "Leaving DeletedTable.getAllDeleted");
		return list;

	}
	public void expungeAllDeletedRecords() {
		NSqlQuery query = new NSqlQuery(db.getConnection());
		query.exec("delete from DeletedItems");
	}

}
