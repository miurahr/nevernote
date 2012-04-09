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

import com.evernote.edam.type.LinkedNotebook;

import cx.fbn.nevernote.sql.driver.NSqlQuery;
import cx.fbn.nevernote.utilities.ApplicationLogger;

public class LinkedNotebookTable {
	
	private final ApplicationLogger 		logger;
	DatabaseConnection							db;
	
	// Constructor
	public LinkedNotebookTable(ApplicationLogger l, DatabaseConnection d) {
		logger = l;
		db = d;
	}
	// Create the table
	public void createTable() {
		NSqlQuery query = new NSqlQuery(db.getConnection());
        logger.log(logger.HIGH, "Creating table LinkedNotebook...");
        if (!query.exec("Create table LinkedNotebook (guid VarChar primary key, " +
        		"shareName VarChar, " +
        		"username VarChar, "+
        		"shardID VarChar, " +
        		"shareKey VarChar, " +
        		"uri VarChar, " +
        		"updateSequenceNumber Long," +
        		"lastSequenceNumber Integer," +
        		"lastSequenceDate Long," +
        		"notebookGuid VarChar," +
        		"isDirty boolean)"))	        		
        	logger.log(logger.HIGH, "Table LinkedNotebook creation FAILED!!!");   
	}
	// Drop the table
	public void dropTable() {
		NSqlQuery query = new NSqlQuery(db.getConnection());
		query.exec("Drop table LinkedNotebook");
	}
	// Save an individual notebook
	public void addNotebook(LinkedNotebook tempNotebook,  boolean isDirty) {
		boolean check;
		
        NSqlQuery query = new NSqlQuery(db.getConnection());
		check = query.prepare("Insert Into LinkedNotebook (guid, shareName, username,  "
				+"shardId, shareKey, uri, updateSequenceNumber, isDirty, lastSequenceNumber, "
				+ "lastSequenceDate, notebookGuid) "   
				+ " Values("
				+":guid, :shareName, :username, "
				+":shardId, :shareKey, :uri,:usn, :isDirty, 0, 0, :notebookGuid)");
		query.bindValue(":guid", tempNotebook.getGuid());
		query.bindValue(":shareName", tempNotebook.getShareName());
		query.bindValue(":username", tempNotebook.getUsername());
		query.bindValue(":shardId", tempNotebook.getShardId());
		query.bindValue(":shareKey", tempNotebook.getShareKey());
		query.bindValue(":usn", tempNotebook.getUpdateSequenceNum());
		query.bindValue(":uri", tempNotebook.getUri());
		query.bindValue(":notebookGuid", "");
		
		if (isDirty)
			query.bindValue(":isDirty", true);
		else
			query.bindValue(":isDirty", false);

		check = query.exec();
		if (!check) {
			logger.log(logger.MEDIUM, "LinkedNotebook Table insert failed.");
			logger.log(logger.MEDIUM, query.lastError().toString());
			return;
		}
	}
	// Delete the notebook based on a guid
	public void expungeNotebook(String id, boolean needsSync) {
		boolean check;
		
		// First, delete any tags associated with this notebook
		String notebookGuid = getNotebookGuid(id);
		db.getNotebookTable().deleteLinkedTags(notebookGuid);
		
		// Now, delete any notes associated with this notebook
		List<String> notes = db.getNoteTable().getNotesByNotebook(notebookGuid);
		for (int i=0; i<notes.size(); i++) {
			db.getNoteTable().expungeNote(notes.get(i), true, needsSync);
		}
		
		// Delete the notebook record
		db.getNotebookTable().expungeNotebook(notebookGuid, needsSync);
		
		// Finally, delete the linked notebook object itself
        NSqlQuery query = new NSqlQuery(db.getConnection());
       	check = query.prepare("delete from LinkedNotebook "
   				+"where guid=:guid");
		if (!check) {
			logger.log(logger.EXTREME, "LinkedNotebook SQL delete prepare has failed.");
			logger.log(logger.EXTREME, query.lastError().toString());
		}
		query.bindValue(":guid", id);
		check = query.exec();
		if (!check) 
			logger.log(logger.MEDIUM, "LinkedNotebook delete failed.");
		
		if  (needsSync) {
			DeletedTable deletedTable = new DeletedTable(logger, db);
			deletedTable.addDeletedItem(new Long(id).toString(), "LinkedNotebook");
		}
	}
	// Check if a notebook exists
	public boolean exists(String id) {
        NSqlQuery query = new NSqlQuery(db.getConnection());
       	boolean check = query.prepare("Select guid from linkednotebook where guid=:guid");
       	query.bindValue(":guid", id);
		check = query.exec();
		if (!check) {
			logger.log(logger.MEDIUM, "LinkedNotebook Table exists check failed.");
			logger.log(logger.MEDIUM, query.lastError().toString());
		}
		if (query.next())
			return true;
		return false;
	}
	// Update a notebook
	public void updateNotebook(LinkedNotebook tempNotebook, boolean isDirty) {
		boolean check;
		if (!exists(tempNotebook.getGuid())) {
			addNotebook(tempNotebook, isDirty);
			return;
		}
		
        NSqlQuery query = new NSqlQuery(db.getConnection());
       	check = query.prepare("Update LinkedNotebook set guid=:guid, shareName=:shareName, " +
       			"username=:username, shardID=:shardID, uri=:uri, updateSequenceNumber=:usn, isDirty=:isDirty "+
       			"where guid=:keyGuid");
		query.bindValue(":guid", tempNotebook.getGuid());
		query.bindValue(":keyGuid", tempNotebook.getGuid());
		query.bindValue(":shareName", tempNotebook.getShareName());
		query.bindValue(":username", tempNotebook.getUsername());
		query.bindValue(":shardID", tempNotebook.getShardId());
		query.bindValue(":shareKey", tempNotebook.getShareKey());
		query.bindValue(":uri", tempNotebook.getUri());
		query.bindValue(":usn", tempNotebook.getUpdateSequenceNum());

		query.bindValue(":isDirty", isDirty);
		
		check = query.exec();
		if (!check) {
			logger.log(logger.MEDIUM, "LinkedNotebook Table update failed.");
			logger.log(logger.MEDIUM, query.lastError().toString());
		}
	}
	// Load notebooks from the database
	public List<LinkedNotebook> getAll() {
		LinkedNotebook tempNotebook;
		List<LinkedNotebook> index = new ArrayList<LinkedNotebook>();
		boolean check;
					
        NSqlQuery query = new NSqlQuery(db.getConnection());
        				
		check = query.exec("Select guid, shareName, username, shardID, shareKey, uri " +
				" from LinkedNotebook");
		if (!check)
			logger.log(logger.EXTREME, "Notebook SQL retrieve has failed.");
		while (query.next()) {
			tempNotebook = new LinkedNotebook();
			tempNotebook.setGuid(query.valueString(0));
			tempNotebook.setShareName(query.valueString(1));
			tempNotebook.setUsername(query.valueString(2));
			tempNotebook.setShardId(query.valueString(3));
			tempNotebook.setShareKey(query.valueString(4));
			tempNotebook.setUri(query.valueString(5));

			index.add(tempNotebook); 
		}	
		return index;
	}			
	// Load notebooks from the database
	public LinkedNotebook getNotebook(String guid) {
		LinkedNotebook tempNotebook;
		boolean check;
					
        NSqlQuery query = new NSqlQuery(db.getConnection());
        				
		check = query.prepare("Select guid, shareName, username, shardID, shareKey, uri " +
				" from LinkedNotebook where guid=:guid");
		if (!check)
			logger.log(logger.EXTREME, "Notebook SQL retrieve notebook prepare has failed.");
		query.bindValue(":guid", guid);
		query.exec();
		while (query.next()) {
			tempNotebook = new LinkedNotebook();
			tempNotebook.setGuid(query.valueString(0));
			tempNotebook.setShareName(query.valueString(1));
			tempNotebook.setUsername(query.valueString(2));
			tempNotebook.setShardId(query.valueString(3));
			tempNotebook.setShareKey(query.valueString(4));
			tempNotebook.setUri(query.valueString(5));
			return tempNotebook;
		}	
		return null;
	}	
	// Load notebooks from the database
	public LinkedNotebook getByNotebookGuid(String guid) {
		LinkedNotebook tempNotebook;
		boolean check;
					
        NSqlQuery query = new NSqlQuery(db.getConnection());
        				
		check = query.prepare("Select guid, shareName, username, shardID, shareKey, uri " +
				" from LinkedNotebook where notebookguid=:guid");
		if (!check)
			logger.log(logger.EXTREME, "Notebook SQL retrieve notebook prepare has failed.");
		query.bindValue(":guid", guid);
		query.exec();
		while (query.next()) {
			tempNotebook = new LinkedNotebook();
			tempNotebook.setGuid(query.valueString(0));
			tempNotebook.setShareName(query.valueString(1));
			tempNotebook.setUsername(query.valueString(2));
			tempNotebook.setShardId(query.valueString(3));
			tempNotebook.setShareKey(query.valueString(4));
			tempNotebook.setUri(query.valueString(5));
			return tempNotebook;
		}	
		return null;
	}
	// Get last sequence date for the notebook
	public long getLastSequenceDate(String guid) {
		boolean check;
					
        NSqlQuery query = new NSqlQuery(db.getConnection());
        				
		check = query.prepare("Select LastSequenceDate " 
				+"from LinkedNotebook where guid=:guid");
		query.bindValue(":guid", guid);
		check = query.exec();
		if (!check)
			logger.log(logger.EXTREME, "LinkedNotebook SQL retrieve last sequence date has failed.");
		if (query.next()) {
			return query.valueLong(0);
		}	
		return 0;
	}			
	// Get a guid by uri
	public String getNotebookGuid(String guid) {
		boolean check;
					
        NSqlQuery query = new NSqlQuery(db.getConnection());
        				
		check = query.prepare("Select notebookGuid " 
				+"from LinkedNotebook where guid=:guid");
		query.bindValue(":guid", guid);
		check = query.exec();
		if (!check)
			logger.log(logger.EXTREME, "LinkedNotebook SQL retrieve of notebookguid by guidhas failed.");
		if (query.next()) {
			return query.valueString(0);
		}	
		return null;
	}	
	// get last sequence numeber
	public int getLastSequenceNumber(String guid) {
		boolean check;
					
        NSqlQuery query = new NSqlQuery(db.getConnection());
        				
		check = query.prepare("Select LastSequenceNumber " 
				+"from LinkedNotebook where guid=:guid");
		query.bindValue(":guid", guid);
		check = query.exec();
		if (!check)
			logger.log(logger.EXTREME, "Notebook SQL retrieve has failed.");
		while (query.next()) {
			return query.valueInteger(0);
		}	
		return 0;
	}		
	
	// get the "true" notebook guid and not the shared notebook guid
	public String getLocalNotebookGuid(String guid) {
		boolean check;
					
        NSqlQuery query = new NSqlQuery(db.getConnection());
        				
		check = query.prepare("Select notebookGuid " 
				+"from LinkedNotebook where guid=:guid");
		query.bindValue(":guid", guid);
		check = query.exec();
		if (!check)
			logger.log(logger.EXTREME, "Notebook SQL retrieve has failed.");
		while (query.next()) {
			return query.valueString(0);
		}	
		return null;
	}

	// does a record exist?
	public String findNotebookByShareName(String name) {
 		
		NSqlQuery query = new NSqlQuery(db.getConnection());
		
		query.prepare("Select guid from Linkednotebook where shareName=:name");
		query.bindValue(":name", name);
		if (!query.exec())
			logger.log(logger.EXTREME, "Linked notebook SQL retrieve by share name has failed.");
		String val = null;
		if (query.next())
			val = query.valueString(0);
		return val;
	}

	// does a record exist?
	public String setNotebookGuid(String shareKey, String notebookGuid) {
 		
		NSqlQuery query = new NSqlQuery(db.getConnection());
		
		query.prepare("Update LinkedNotebook set notebookGuid=:notebookGuid where shareKey=:shareKey");
		query.bindValue(":notebookGuid", notebookGuid);
		query.bindValue(":shareKey", shareKey);
		if (!query.exec())
			logger.log(logger.EXTREME, "Linked notebook SQL retrieve by share name has failed.");
		String val = null;
		if (query.next())
			val = query.valueString(0);
		return val;
	}
	// set sync date
	public String setLastSequenceDate(String guid, long date) {
 		
		NSqlQuery query = new NSqlQuery(db.getConnection());
		
		query.prepare("Update LinkedNotebook set lastsequencedate=:date where guid=:guid");
		query.bindValue(":date", date);
		query.bindValue(":guid", guid);
		if (!query.exec())
			logger.log(logger.EXTREME, "Linked notebook SQL retrieve by share name has failed.");
		String val = null;
		if (query.next())
			val = query.valueString(0);
		return val;
	}
	// set sync number
	public String setLastSequenceNumber(String guid, int number) {
 		
		NSqlQuery query = new NSqlQuery(db.getConnection());
		
		query.prepare("Update LinkedNotebook set lastsequencenumber=:number where guid=:guid");
		query.bindValue(":number", number);
		query.bindValue(":guid", guid);
		if (!query.exec())
			logger.log(logger.EXTREME, "Linked notebook SQL retrieve by share name has failed.");
		String val = null;
		if (query.next())
			val = query.valueString(0);
		return val;
	}
	
	// Get a list of linked notebooks that need to be updated
	public List<String> getDirtyGuids() {
		List<String> index = new ArrayList<String>();
		boolean check;	
        NSqlQuery query = new NSqlQuery(db.getConnection());
        				
		check = query.exec("Select guid from LinkedNotebook where isDirty = true");
		if (!check) 
			logger.log(logger.EXTREME, "LinkedNotebook SQL retrieve has failed in getdirtyIds.");
		while (query.next()) {
			index.add(query.valueString(0));
		}	
		return index;	
	}

}

