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

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import com.evernote.edam.type.SharedNotebook;

import cx.fbn.nevernote.sql.driver.NSqlQuery;
import cx.fbn.nevernote.utilities.ApplicationLogger;

public class SharedNotebookTable {
	
	private final ApplicationLogger 		logger;
	DatabaseConnection							db;
	
	// Constructor
	public SharedNotebookTable(ApplicationLogger l, DatabaseConnection d) {
		logger = l;
		db = d;
	}
	// Create the table
	public void createTable() {
		NSqlQuery query = new NSqlQuery(db.getConnection());
        logger.log(logger.HIGH, "Creating table SharedNotebook...");
        if (!query.exec("Create table SharedNotebook (id long primary key, " +
        		"userid Integer, " +
        		"notebookGuid VarChar, "+
        		"email VarChar, "+
        		"notebookModifiable boolean, " +
        		"requireLogin boolean, "+
        		"serviceCreated timestamp, "+
        		"shareKey VarChar,  "+
        		"username VarChar, "+
        		"icon blob, " +
        		"isDirty boolean)"))	        		
        	logger.log(logger.HIGH, "Table SharedNotebook creation FAILED!!!");   
	}
	// Drop the table
	public void dropTable() {
		NSqlQuery query = new NSqlQuery(db.getConnection());
		query.exec("Drop table SharedNotebook");
	}
	// Save an individual notebook
	public void addNotebook(SharedNotebook tempNotebook, boolean isDirty) {
		boolean check;
		
		SimpleDateFormat simple = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
        NSqlQuery query = new NSqlQuery(db.getConnection());
		check = query.prepare("Insert Into SharedNotebook (id, userid, notebookGuid, email,  "
<<<<<<< HEAD
				+"notebookModifiable, requireLogin, serviceCreated, shareKey, username, isDirty) "   
				+ " Values("
				+":id, :userid, :notebookGuid, :email, "
				+":notebookModifiable, :requireLogin, :serviceCreated, "
				+":shareKey, :username, :isDirty)");
=======
				+"notebookModifiable, requireLogin, serviceCreated, shareKey, username) "   
				+ " Values("
				+":id, :userid, :notebookGuid, :email, "
				+":notebookModifiable, :requireLogin, :serviceCreated, "
				+":shareKey, :username)");
>>>>>>> Add linked & shared notebook tables
		query.bindValue(":id", tempNotebook.getId());
		query.bindValue(":userid", tempNotebook.getUserId());
		query.bindValue(":notebookGuid", tempNotebook.getNotebookGuid());
		query.bindValue(":email", tempNotebook.getEmail());
		query.bindValue(":notebookModifiable", tempNotebook.isNotebookModifiable());
		query.bindValue(":requireLogin", tempNotebook.isRequireLogin());

		StringBuilder serviceCreated = new StringBuilder(simple.format(tempNotebook.getServiceCreated()));			
		query.bindValue(":serviceCreated", serviceCreated.toString());
		
		query.bindValue(":shareKey", tempNotebook.getShareKey());
		query.bindValue(":username", tempNotebook.getUsername());
		
		if (isDirty)
			query.bindValue(":isDirty", true);
		else
			query.bindValue(":isDirty", false);

		check = query.exec();
		if (!check) {
			logger.log(logger.MEDIUM, "SharedNotebook Table insert failed.");
			logger.log(logger.MEDIUM, query.lastError().toString());
		}
	}
<<<<<<< HEAD
	// Check if a notebook exists
	public boolean exists(long id) {
        NSqlQuery query = new NSqlQuery(db.getConnection());
       	boolean check = query.prepare("Select id from sharednotebook where id=:id");
       	query.bindValue(":id", id);
		check = query.exec();
		if (!check) {
			logger.log(logger.MEDIUM, "SharedNotebook Table exists check failed.");
			logger.log(logger.MEDIUM, query.lastError().toString());
		}
		if (query.next())
			return true;
		return false;
	}
	// Delete the notebook based on a id
=======
	// Delete the notebook based on a guid
>>>>>>> Add linked & shared notebook tables
	public void expungeNotebook(long id, boolean needsSync) {
		boolean check;
        NSqlQuery query = new NSqlQuery(db.getConnection());

       	check = query.prepare("delete from SharedNotebook "
   				+"where id=:id");
		if (!check) {
			logger.log(logger.EXTREME, "SharedNotebook SQL delete prepare has failed.");
			logger.log(logger.EXTREME, query.lastError().toString());
		}
		query.bindValue(":id", id);
		check = query.exec();
		if (!check) 
			logger.log(logger.MEDIUM, "SharedNotebook delete failed.");
		
		// Signal the parent that work needs to be done
		if  (needsSync) {
			DeletedTable deletedTable = new DeletedTable(logger, db);
			deletedTable.addDeletedItem(new Long(id).toString(), "SharedNotebook");
		}
	}
<<<<<<< HEAD
	// Delete the notebook based on a id
	public void expungeNotebookByGuid(String id, boolean needsSync) {
		boolean check;
        NSqlQuery query = new NSqlQuery(db.getConnection());

       	check = query.prepare("delete from SharedNotebook "
   				+"where guid=:id");
		if (!check) {
			logger.log(logger.EXTREME, "SharedNotebook SQL delete by notebook guid prepare has failed.");
			logger.log(logger.EXTREME, query.lastError().toString());
		}
		query.bindValue(":id", id);
		check = query.exec();
		if (!check) 
			logger.log(logger.MEDIUM, "SharedNotebook delete by notebook guid failed.");
		
		// Signal the parent that work needs to be done
		if  (needsSync) {
			DeletedTable deletedTable = new DeletedTable(logger, db);
			deletedTable.addDeletedItem(new Long(id).toString(), "SharedNotebook");
		}
	}

	
	// Update a notebook
	public void updateNotebook(SharedNotebook tempNotebook, boolean isDirty) {
		boolean check;
		if (!exists(tempNotebook.getId())) {
			addNotebook(tempNotebook, isDirty);
			return;
		}
		
		SimpleDateFormat simple = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
		
		StringBuilder serviceCreated = new StringBuilder(simple.format(tempNotebook.getServiceCreated()));						
        NSqlQuery query = new NSqlQuery(db.getConnection());
       	check = query.prepare("Update SharedNotebook set id=:id, userid=:userid, notebookGuid=:notebook, "
       			+ "email=:email, notebookModifiable=:mod, requireLogin=:rlogin, serviceCreated=:serviceCreated, "
=======
	// Update a notebook
	public void updateNotebook(SharedNotebook tempNotebook, boolean isDirty) {
		boolean check;
		
		SimpleDateFormat simple = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
		
        NSqlQuery query = new NSqlQuery(db.getConnection());
       	check = query.prepare("Update SharedNotebook set id=:id, userid=:userid, notebookGuid=:notebook, "
       			+ "email=:email, notebookModifiable=:mod, requireLogin=:rlogin, serviceCreated=:created "
>>>>>>> Add linked & shared notebook tables
       			+ "shareKey=:shareKey, username=:username, isDirty=:isdirty");
		query.bindValue(":id", tempNotebook.getId());
		query.bindValue(":userid", tempNotebook.getUserId());
		query.bindValue(":notebook", tempNotebook.getNotebookGuid());
		query.bindValue(":email", tempNotebook.getEmail());
		query.bindValue(":mod", tempNotebook.isNotebookModifiable());
		query.bindValue(":rlogin", tempNotebook.isRequireLogin());
<<<<<<< HEAD
		query.bindValue(":serviceCreated", serviceCreated.toString());
		query.bindValue(":shareKey", tempNotebook.getShareKey());
		query.bindValue(":username", tempNotebook.getUsername());
=======
		query.bindValue(":shareKey", tempNotebook.getShareKey());
		query.bindValue(":username", tempNotebook.getUsername());

		StringBuilder serviceCreated = new StringBuilder(simple.format(tempNotebook.getServiceCreated()));						
		query.bindValue(":serviceCreated", serviceCreated.toString());
>>>>>>> Add linked & shared notebook tables
		
		query.bindValue(":isDirty", isDirty);
		
		check = query.exec();
		if (!check) {
			logger.log(logger.MEDIUM, "SharedNotebook Table update failed.");
			logger.log(logger.MEDIUM, query.lastError().toString());
		}
	}
	// Load notebooks from the database
	public List<SharedNotebook> getAll() {
		SharedNotebook tempNotebook;
		List<SharedNotebook> index = new ArrayList<SharedNotebook>();
		boolean check;
					
        NSqlQuery query = new NSqlQuery(db.getConnection());
        				
		check = query.exec("Select id, userid, notebookGuid, email, notebookModifiable, requireLogin, " +
				"serviceCreated, "+
				"shareKey, username from SharedNotebook");
		if (!check)
			logger.log(logger.EXTREME, "Notebook SQL retrieve has failed.");
		while (query.next()) {
			tempNotebook = new SharedNotebook();
			tempNotebook.setId(query.valueLong(0));
			tempNotebook.setUserId(query.valueInteger(1));
			tempNotebook.setNotebookGuid(query.valueString(2));
			tempNotebook.setEmail(query.valueString(3));
			tempNotebook.setNotebookModifiable(query.valueBoolean(4,false));
			tempNotebook.setRequireLogin(query.valueBoolean(5,true));
			DateFormat indfm = null;
			try {
				indfm = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
			} catch (Exception e) {	}
			try {
				tempNotebook.setServiceCreated(indfm.parse(query.valueString(6)).getTime());
			} catch (ParseException e) {
				e.printStackTrace();
			}
			tempNotebook.setShareKey(query.valueString(7));
			tempNotebook.setUsername(query.valueString(8));

			index.add(tempNotebook); 
		}	
		return index;
	}			

<<<<<<< HEAD
	// Load notebooks from the database
	public List<SharedNotebook> getForNotebook(String guid) {
		SharedNotebook tempNotebook;
		List<SharedNotebook> index = new ArrayList<SharedNotebook>();
		boolean check;
					
        NSqlQuery query = new NSqlQuery(db.getConnection());
        				
		check = query.prepare("Select id, userid, notebookGuid, email, notebookModifiable, requireLogin, " +
				"serviceCreated, "+
				"shareKey, username from SharedNotebook where notebookGuid=:notebookGuid ");
		if (!check)
			logger.log(logger.EXTREME, "SharedNotebook getForNotebook SQL prepare has failed.");
		query.bindValue(":notebookGuid", guid);
		check = query.exec();
		if (!check)
			logger.log(logger.EXTREME, "SharedNotebook getForNotebook SQL exec has failed.");
		
		while (query.next()) {
			tempNotebook = new SharedNotebook();
			tempNotebook.setId(query.valueLong(0));
			tempNotebook.setUserId(query.valueInteger(1));
			tempNotebook.setNotebookGuid(query.valueString(2));
			tempNotebook.setEmail(query.valueString(3));
			tempNotebook.setNotebookModifiable(query.valueBoolean(4,false));
			tempNotebook.setRequireLogin(query.valueBoolean(5,true));
			DateFormat indfm = null;
			try {
				indfm = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
			} catch (Exception e) {	}
			try {
				tempNotebook.setServiceCreated(indfm.parse(query.valueString(6)).getTime());
			} catch (ParseException e) {
				e.printStackTrace();
			}
			tempNotebook.setShareKey(query.valueString(7));
			tempNotebook.setUsername(query.valueString(8));

			index.add(tempNotebook); 
		}	
		return index;
	}			

	
	// Get a list of shared notebooks that need to be updated
	public List <Long> getDirtyIds() {
		List<Long> index = new ArrayList<Long>();
		boolean check;	
        NSqlQuery query = new NSqlQuery(db.getConnection());
        				
		check = query.exec("Select id from SharedNotebook where isDirty = true");
		if (!check) 
			logger.log(logger.EXTREME, "SharedNotebook SQL retrieve has failed in getdirtyIds.");
		while (query.next()) {
			index.add(query.valueLong(0));
		}	
		return index;	
	}

=======
	// does a record exist?
	public String findNotebookByName(String newname) {
 		
		NSqlQuery query = new NSqlQuery(db.getConnection());
		
		query.prepare("Select guid from sharednotebook where name=:newname");
		query.bindValue(":newname", newname);
		if (!query.exec())
			logger.log(logger.EXTREME, "notebook SQL retrieve has failed.");
		String val = null;
		if (query.next())
			val = query.valueString(0);
		return val;
	}


>>>>>>> Add linked & shared notebook tables
}

