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

public class SyncTable {
	ListManager parent;
	private final ApplicationLogger 		logger;
	private final DatabaseConnection		db;

	
	// Constructor
	public SyncTable(ApplicationLogger l, DatabaseConnection d) {
		logger = l;
		db = d;
	}
	// Create the table
	public void createTable() {
		NSqlQuery query = new NSqlQuery(db.getConnection());
		logger.log(logger.HIGH, "Creating table Sync...");
        if (!query.exec("Create table Sync (key varchar primary key, value varchar);"))
           	logger.log(logger.HIGH, "Table Sync creation FAILED!!!"); 
        addRecord("LastSequenceDate","0");
        addRecord("UpdateSequenceNumber", "0");
	}
	// Drop the table
	public void dropTable() {
		NSqlQuery query = new NSqlQuery(db.getConnection());
		query.exec("Drop table Sync");
	}
	// Add an item to the table
	public void addRecord(String key, String value) {
        NSqlQuery query = new NSqlQuery(db.getConnection());
		query.prepare("Insert Into Sync (key,  value) values (:key, :value);");
		query.bindValue(":key", key);
		query.bindValue(":value", value);
		if (!query.exec()) {
			logger.log(logger.MEDIUM, "Add to into Sync failed.");
			logger.log(logger.MEDIUM, query.lastError());
		}
	}
	// Add an item to the table
	public void deleteRecord(String key) {
        NSqlQuery query = new NSqlQuery(db.getConnection());
		query.prepare("Delete From Sync where key=:key");
		query.bindValue(":key", key);
		if (!query.exec()) {
			logger.log(logger.MEDIUM, "Delete from Sync failed.");
			logger.log(logger.MEDIUM, query.lastError());
		}
	}
	// Get a key field
	public String getRecord(String key) {
        NSqlQuery query = new NSqlQuery(db.getConnection());
        query.prepare("Select value from Sync where key=:key");
        query.bindValue(":key", key);
		if (!query.exec()) {
			logger.log(logger.MEDIUM, "getRecord from sync failed.");
			logger.log(logger.MEDIUM, query.lastError());
			return null;
		}
		if (query.next()) {
			return (query.valueString(0));
		}
 		return null;
	}
	// Set a key field
	public void setRecord(String key, String value) {
        NSqlQuery query = new NSqlQuery(db.getConnection());
        query.prepare("Update Sync set value=:value where key=:key");
        query.bindValue(":key", key);
        query.bindValue(":value", value);
		if (!query.exec()) {
			logger.log(logger.MEDIUM, "setRecord from sync failed.");
			logger.log(logger.MEDIUM, query.lastError());
		}
		return;
	}

	// Set the last sequence date
	public void setLastSequenceDate(long date) {
		logger.log(logger.LOW, "Updating Last Sequence Date: " +date);
		long old = getLastSequenceDate();
		logger.log(logger.LOW, "Old Last Sequence Date: " +old);
		if (date < old) 
			logger.log(logger.LOW, "************* SEQUENCE DATE PROBLEM!!! "+(old-date));
		setRecord("LastSequenceDate", new Long(date).toString());
	}
	// Set the last sequence date
	public void setUpdateSequenceNumber(int number) {
		logger.log(logger.LOW, "Updating Last Sequence Number: " +number);
		int old = getUpdateSequenceNumber();
		logger.log(logger.LOW, "Old Last Sequence Number: " +old);
		if (number < old) 
			logger.log(logger.LOW, "************* SEQUENCE NUMBER PROBLEM!!! "+(old-number));
		setRecord("UpdateSequenceNumber", new Integer(number).toString());
	}
	// get last sequence date
	public long getLastSequenceDate() {
		return new Long(getRecord("LastSequenceDate"));
	}
	// Get invalid attributes for a given element
	public int getUpdateSequenceNumber() {
		return new Integer(getRecord("UpdateSequenceNumber"));
	}
	// Get notebooks/tags to ignore
	public List<String> getIgnoreRecords(String type) {
		List<String> values = new ArrayList<String>();
        NSqlQuery query = new NSqlQuery(db.getConnection());
        if (!query.prepare("Select value from Sync where key like :type")) {
			logger.log(logger.MEDIUM, "getIgnoreRecords from sync failed.");
			logger.log(logger.MEDIUM, query.lastError());
			return null;
		}
        query.bindValue(":type", "IGNORE" +type +"-%");
        query.exec();
		while (query.next()) {
			values.add(query.valueString(0));
		}
 		return values;
	}
	// Expunge ignore records
	// Add an item to the table

}
