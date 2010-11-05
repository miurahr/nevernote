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

import java.text.SimpleDateFormat;
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
        if (!query.exec("Create table LinkedNotebook (id long primary key, " +
        		"shareName VarChar, " +
        		"username VarChar, "+
        		"shardID VarChar, " +
        		"shareKey VarChar, " +
        		"url VarChar, " +
        		"icon blob, " +
        		"isDirty boolean)"))	        		
        	logger.log(logger.HIGH, "Table LinkedNotebook creation FAILED!!!");   
	}
	// Drop the table
	public void dropTable() {
		NSqlQuery query = new NSqlQuery(db.getConnection());
		query.exec("Drop table LinkedNotebook");
	}
	// Save an individual notebook
	public void addNotebook(LinkedNotebook tempNotebook, boolean isDirty) {
		boolean check;
		
		SimpleDateFormat simple = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
        NSqlQuery query = new NSqlQuery(db.getConnection());
		check = query.prepare("Insert Into LinkedNotebook (id, shareName, username,  "
				+"shardId, shareKey, url, isDirty) "   
				+ " Values("
				+":id, :shareName, :username, "
				+":shardId, :shareKey, :url, :isDirty)");
		query.bindValue(":id", tempNotebook.getId());
		query.bindValue(":shareName", tempNotebook.getShareName());
		query.bindValue(":username", tempNotebook.getUsername());
		query.bindValue(":shardId", tempNotebook.getShardId());
		query.bindValue(":shareKey", tempNotebook.getShareKey());
		query.bindValue(":url", tempNotebook.getUri());
		
		if (isDirty)
			query.bindValue(":isDirty", true);
		else
			query.bindValue(":isDirty", false);

		check = query.exec();
		if (!check) {
			logger.log(logger.MEDIUM, "LinkedNotebook Table insert failed.");
			logger.log(logger.MEDIUM, query.lastError().toString());
		}
	}
	// Delete the notebook based on a guid
	public void expungeNotebook(long id, boolean needsSync) {
		boolean check;
        NSqlQuery query = new NSqlQuery(db.getConnection());

       	check = query.prepare("delete from LinkedNotebook "
   				+"where id=:id");
		if (!check) {
			logger.log(logger.EXTREME, "LinkedNotebook SQL delete prepare has failed.");
			logger.log(logger.EXTREME, query.lastError().toString());
		}
		query.bindValue(":id", id);
		check = query.exec();
		if (!check) 
			logger.log(logger.MEDIUM, "LinkedNotebook delete failed.");
		
		// Signal the parent that work needs to be done
		if  (needsSync) {
			DeletedTable deletedTable = new DeletedTable(logger, db);
			deletedTable.addDeletedItem(new Long(id).toString(), "LinkedNotebook");
		}
	}
	// Update a notebook
	public void updateNotebook(LinkedNotebook tempNotebook, boolean isDirty) {
		boolean check;
		
		SimpleDateFormat simple = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
		
        NSqlQuery query = new NSqlQuery(db.getConnection());
       	check = query.prepare("Update LinkedNotebook set id=:id, shareName=:shareName, " +
       			"username=:username, shardID=:shardID, shareKey=:shareKey, url=:url, isDirty=:isDirty");
		query.bindValue(":id", tempNotebook.getId());
		query.bindValue(":shareName", tempNotebook.getShareName());
		query.bindValue(":username", tempNotebook.getUsername());
		query.bindValue(":shardID", tempNotebook.getShardId());
		query.bindValue(":shareKey", tempNotebook.getShareKey());
		query.bindValue(":url", tempNotebook.getUri());

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
        				
		check = query.exec("Select id, shareName, username, shardID, shareKey url, " +
				" from LinkedNotebook");
		if (!check)
			logger.log(logger.EXTREME, "Notebook SQL retrieve has failed.");
		while (query.next()) {
			tempNotebook = new LinkedNotebook();
			tempNotebook.setId(query.valueLong(0));
			tempNotebook.setUsername(query.valueString(1));
			tempNotebook.setShardId(query.valueString(2));
			tempNotebook.setShareKey(query.valueString(3));
			tempNotebook.setUri(query.valueString(4));

			index.add(tempNotebook); 
		}	
		return index;
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


}

