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

import com.evernote.edam.type.Notebook;

import cx.fbn.nevernote.sql.driver.NSqlQuery;
import cx.fbn.nevernote.utilities.ApplicationLogger;
import cx.fbn.nevernote.utilities.Pair;

public class NotebookTable {
	
	private final ApplicationLogger 		logger;
	DatabaseConnection							db;
	
	// Constructor
	public NotebookTable(ApplicationLogger l, DatabaseConnection d) {
		logger = l;
		db = d;
	}
	// Create the table
	public void createTable() {
		NSqlQuery query = new NSqlQuery(db.getConnection());
        logger.log(logger.HIGH, "Creating table Notebook...");
        if (!query.exec("Create table Notebook (guid varchar primary key, " +
        		"sequence integer, name varchar, defaultNotebook varchar, "+
        		"serviceCreated timestamp, serviceUpdated timestamp, published boolean, isDirty boolean, "+
        		"autoEncrypt boolean, local boolean, archived boolean)"))	        		
        	logger.log(logger.HIGH, "Table Notebook creation FAILED!!!");   
        Notebook newnote = new Notebook();
        newnote.setDefaultNotebook(true);
        newnote.setName("My Notebook");
        newnote.setPublished(false);
        newnote.setGuid("1");
        addNotebook(newnote, true, false);
 		
	}
	// Drop the table
	public void dropTable() {
		NSqlQuery query = new NSqlQuery(db.getConnection());
		query.exec("Drop table Notebook");
	}
	// Save an individual notebook
	public void addNotebook(Notebook tempNotebook, boolean isDirty, boolean local) {
		boolean check;
		
		SimpleDateFormat simple = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
        NSqlQuery query = new NSqlQuery(db.getConnection());
		check = query.prepare("Insert Into Notebook (guid, sequence, name, defaultNotebook, "
				+"serviceCreated, serviceUpdated, published, "   
				+ "isDirty, autoEncrypt," 
				+ "local, archived) Values("
				+":guid, :sequence, :name, :defaultNotebook,  "
				+":serviceCreated, :serviceUpdated, :published, "
				+":isDirty, :autoEncrypt, "
				+":local, false)");
		query.bindValue(":guid", tempNotebook.getGuid());
		query.bindValue(":sequence", tempNotebook.getUpdateSequenceNum());
		query.bindValue(":name", tempNotebook.getName());
		query.bindValue(":defaultNotebook", tempNotebook.isDefaultNotebook());
		
		StringBuilder serviceCreated = new StringBuilder(simple.format(tempNotebook.getServiceCreated()));			
		StringBuilder serviceUpdated = new StringBuilder(simple.format(tempNotebook.getServiceUpdated()));
		if (serviceUpdated.toString() == null)
			serviceUpdated = serviceCreated;
		query.bindValue(":serviceCreated", serviceCreated.toString());
		query.bindValue(":serviceUpdated", serviceCreated.toString());
		query.bindValue(":published",tempNotebook.isPublished());
		
		if (isDirty)
			query.bindValue(":isDirty", true);
		else
			query.bindValue(":isDirty", false);
		query.bindValue(":autoEncrypt", false);
		query.bindValue(":local", local);

		check = query.exec();
		if (!check) {
			logger.log(logger.MEDIUM, "Notebook Table insert failed.");
			logger.log(logger.MEDIUM, query.lastError().toString());
		}
	}
	// Delete the notebook based on a guid
	public void expungeNotebook(String guid, boolean needsSync) {
		boolean check;
        NSqlQuery query = new NSqlQuery(db.getConnection());

       	check = query.prepare("delete from Notebook "
   				+"where guid=:guid");
		if (!check) {
			logger.log(logger.EXTREME, "Notebook SQL delete prepare has failed.");
			logger.log(logger.EXTREME, query.lastError().toString());
		}
		query.bindValue(":guid", guid);
		check = query.exec();
		if (!check) 
			logger.log(logger.MEDIUM, "Notebook delete failed.");
		
		// Signal the parent that work needs to be done
		if  (needsSync) {
			DeletedTable deletedTable = new DeletedTable(logger, db);
			deletedTable.addDeletedItem(guid, "Notebook");
		}
	}
	// Update a notebook
	public void updateNotebook(Notebook tempNotebook, boolean isDirty) {
		boolean check;
		
		SimpleDateFormat simple = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
		
        NSqlQuery query = new NSqlQuery(db.getConnection());
       	check = query.prepare("Update Notebook set sequence=:sequence, name=:name, defaultNotebook=:defaultNotebook, " +
       			"serviceCreated=:serviceCreated, serviceUpdated=:serviceUpdated, "+
				"published=:published, isDirty=:isDirty where guid=:guid ");
		query.bindValue(":sequence", tempNotebook.getUpdateSequenceNum());
		query.bindValue(":name", tempNotebook.getName());
		query.bindValue(":defaultNotebook", tempNotebook.isDefaultNotebook());

		StringBuilder serviceCreated = new StringBuilder(simple.format(tempNotebook.getServiceCreated()));			
		StringBuilder serviceUpdated = new StringBuilder(simple.format(tempNotebook.getServiceUpdated()));			
		query.bindValue(":serviceCreated", serviceCreated.toString());
		query.bindValue(":serviceUpdated", serviceUpdated.toString());
		
		query.bindValue(":published", tempNotebook.isPublished());
		query.bindValue(":isDirty", isDirty);
		query.bindValue(":guid", tempNotebook.getGuid());
		
		check = query.exec();
		if (!check) {
			logger.log(logger.MEDIUM, "Notebook Table update failed.");
			logger.log(logger.MEDIUM, query.lastError().toString());
		}
	}
	// Load notebooks from the database
	public List<Notebook> getAll() {
		Notebook tempNotebook;
		List<Notebook> index = new ArrayList<Notebook>();
		boolean check;
					
        NSqlQuery query = new NSqlQuery(db.getConnection());
        				
		check = query.exec("Select guid, sequence, name, defaultNotebook, " +
				"serviceCreated, "+
				"serviceUpdated, "+
				"published, defaultNotebook from Notebook order by name");
		if (!check)
			logger.log(logger.EXTREME, "Notebook SQL retrieve has failed.");
		while (query.next()) {
			tempNotebook = new Notebook();
			tempNotebook.setGuid(query.valueString(0));
			int sequence = new Integer(query.valueString(1)).intValue();
			tempNotebook.setUpdateSequenceNum(sequence);
			tempNotebook.setName(query.valueString(2));
			DateFormat indfm = null;
			try {
				indfm = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
//				indfm = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy");
			} catch (Exception e) {	}
			try {
				tempNotebook.setServiceCreated(indfm.parse(query.valueString(4)).getTime());
				tempNotebook.setServiceUpdated(indfm.parse(query.valueString(5)).getTime());
			} catch (ParseException e) {
				e.printStackTrace();
			}
			tempNotebook.setPublished(new Boolean(query.valueString(6)));
			tempNotebook.setDefaultNotebook(new Boolean(query.valueString(7)));
			index.add(tempNotebook); 
		}	
		return index;
	}	
	public List<Notebook> getAllLocal() {
		Notebook tempNotebook;
		List<Notebook> index = new ArrayList<Notebook>();
		boolean check;

        NSqlQuery query = new NSqlQuery(db.getConnection());
        				
		check = query.exec("Select guid, sequence, name, defaultNotebook, " +
				"serviceCreated, serviceUpdated, published from Notebook where local=true order by name");
		if (!check)
			logger.log(logger.EXTREME, "Notebook SQL retrieve has failed.");
		while (query.next()) {
			tempNotebook = new Notebook();
			tempNotebook.setGuid(query.valueString(0));
			int sequence = new Integer(query.valueString(1)).intValue();
			tempNotebook.setUpdateSequenceNum(sequence);
			tempNotebook.setName(query.valueString(2));
			
			DateFormat indfm = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
//			indfm = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy");
			try {
				tempNotebook.setServiceCreated(indfm.parse(query.valueString(4)).getTime());
				tempNotebook.setServiceUpdated(indfm.parse(query.valueString(5)).getTime());
			} catch (ParseException e) {
				e.printStackTrace();
			}
			index.add(tempNotebook); 
		}	
		return index;
	}
	// Archive or un-archive a notebook
	public void setArchived(String guid, boolean val) {
		boolean check;			
        NSqlQuery query = new NSqlQuery(db.getConnection());     				
		check = query.prepare("Update notebook set archived=:archived where guid=:guid");
		if (!check)
			logger.log(logger.EXTREME, "Notebook SQL archive update has failed.");
		query.bindValue(":guid", guid);
		query.bindValue(":archived", val);
		query.exec();
	}
	// Load non-archived notebooks from the database
	public List<Notebook> getAllArchived() {
		Notebook tempNotebook;
		List<Notebook> index = new ArrayList<Notebook>();
		boolean check;
						
        NSqlQuery query = new NSqlQuery(db.getConnection());
        				
		check = query.exec("Select guid, sequence, name, defaultNotebook, " +
				"serviceCreated, serviceUpdated, published from Notebook where archived=true order by name");
		if (!check)
			logger.log(logger.EXTREME, "Notebook SQL retrieve has failed.");
		while (query.next()) {
			tempNotebook = new Notebook();
			tempNotebook.setGuid(query.valueString(0));
			int sequence = new Integer(query.valueString(1)).intValue();
			tempNotebook.setUpdateSequenceNum(sequence);
			tempNotebook.setName(query.valueString(2));
			
			DateFormat indfm = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
//			indfm = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy");
			try {
				tempNotebook.setServiceCreated(indfm.parse(query.valueString(4)).getTime());
				tempNotebook.setServiceUpdated(indfm.parse(query.valueString(5)).getTime());
			} catch (ParseException e) {
				e.printStackTrace();
			}
			tempNotebook.setPublished(new Boolean(query.valueString(6)));
			index.add(tempNotebook); 
		}	
		return index;
	}	
	// Check for a local/remote notebook
	public boolean isNotebookLocal(String guid) {
        NSqlQuery query = new NSqlQuery(db.getConnection());
		
		query.prepare("Select local from Notebook where guid=:guid");
		query.bindValue(":guid", guid);
		query.exec();
		if (!query.next()) {
			return false;
		}
		boolean returnValue = query.valueBoolean(0, false);
		return returnValue;
	}
	// Update a notebook sequence number
	public void updateNotebookSequence(String guid, int sequence) {
		boolean check;
        NSqlQuery query = new NSqlQuery(db.getConnection());
		check = query.prepare("Update Notebook set sequence=:sequence where guid=:guid");
		query.bindValue(":guid", guid);
		query.bindValue(":sequence", sequence);
		query.exec();
		if (!check) {
			logger.log(logger.MEDIUM, "Notebook sequence update failed.");
			logger.log(logger.MEDIUM, query.lastError());
		} 
	}
	// Update a notebook GUID number
	public void updateNotebookGuid(String oldGuid, String newGuid) {
        NSqlQuery query = new NSqlQuery(db.getConnection());
		query.prepare("Update Notebook set guid=:newGuid where guid=:oldGuid");
		query.bindValue(":oldGuid", oldGuid);
		query.bindValue(":newGuid", newGuid);
		if (!query.exec()) {
			logger.log(logger.MEDIUM, "Notebook guid update failed.");
			logger.log(logger.MEDIUM, query.lastError());
		} 
 		
		// Update any notes containing the notebook guid
		query.prepare("Update Note set notebookGuid=:newGuid where notebookGuid=:oldGuid");
		query.bindValue(":oldGuid", oldGuid);
		query.bindValue(":newGuid", newGuid);
		if (!query.exec()) {
			logger.log(logger.MEDIUM, "Notebook guid update for note failed.");
			logger.log(logger.MEDIUM, query.lastError());
		} 
		
		// Update any watch folders with the new guid
		query = new NSqlQuery(db.getConnection());
		query.prepare("Update WatchFolders set notebook=:newGuid where notebook=:oldGuid");
		query.bindValue(":oldGuid", oldGuid);
		query.bindValue(":newGuid", newGuid);
		if (!query.exec()) {
			logger.log(logger.MEDIUM, "Update WatchFolder notebook failed.");
			logger.log(logger.MEDIUM, query.lastError().toString());
		}		
	}
	// Get a list of notes that need to be updated
	public List <Notebook> getDirty() {
		Notebook tempNotebook;
		List<Notebook> index = new ArrayList<Notebook>();
		boolean check;
						
 		
        NSqlQuery query = new NSqlQuery(db.getConnection());
        				
		check = query.exec("Select guid, sequence, name, defaultNotebook, " +
				"serviceCreated, serviceUpdated, published from Notebook where isDirty = true and local=false");
		if (!check) 
			logger.log(logger.EXTREME, "Notebook SQL retrieve has failed.");
		while (query.next()) {
			tempNotebook = new Notebook();
			tempNotebook.setGuid(query.valueString(0));
			int sequence = new Integer(query.valueString(1)).intValue();
			tempNotebook.setUpdateSequenceNum(sequence);
			tempNotebook.setName(query.valueString(2));
			
			DateFormat indfm = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
//			indfm = new SimpleDateFormat("EEE MMM dd HH:mm:ss yyyy");
			try {
				tempNotebook.setServiceCreated(indfm.parse(query.valueString(4)).getTime());
				tempNotebook.setServiceUpdated(indfm.parse(query.valueString(5)).getTime());
			} catch (ParseException e) {
				e.printStackTrace();
			}
			tempNotebook.setPublished(new Boolean(query.valueString(6)));
			index.add(tempNotebook); 
		}	
		return index;	
	}
	// This is a convience method to check if a tag exists & update/create based upon it
	public void syncNotebook(Notebook notebook, boolean isDirty) {
		if (!exists(notebook.getGuid())) {
			addNotebook(notebook, isDirty, isDirty);
			return;
		}
		updateNotebook(notebook, isDirty);
	}
	// does a record exist?
	private boolean exists(String guid) {
 		
		NSqlQuery query = new NSqlQuery(db.getConnection());
		
		query.prepare("Select guid from notebook where guid=:guid");
		query.bindValue(":guid", guid);
		if (!query.exec())
			logger.log(logger.EXTREME, "notebook SQL retrieve has failed.");
		boolean retval = query.next();
		return retval;
	}
	// Reset the dirty flag.  Typically done after a sync.
	public void  resetDirtyFlag(String guid) {
		NSqlQuery query = new NSqlQuery(db.getConnection());
		
		query.prepare("Update notebook set isdirty='false' where guid=:guid");
		query.bindValue(":guid", guid);
		if (!query.exec())
			logger.log(logger.EXTREME, "Error resetting notebook dirty field.");
	}
	// Set the default notebook
	public void setDefaultNotebook(String guid) {
		NSqlQuery query = new NSqlQuery(db.getConnection());
		
		query.prepare("Update notebook set defaultNotebook=false");
		if (!query.exec())
			logger.log(logger.EXTREME, "Error removing default notebook.");
		query.prepare("Update notebook set defaultNotebook=true where guid = :guid");
		query.bindValue(":guid", guid);
		if (!query.exec())
			logger.log(logger.EXTREME, "Error setting default notebook.");
	}
	
	

	// does a record exist?
	public String findNotebookByName(String newname) {
 		
		NSqlQuery query = new NSqlQuery(db.getConnection());
		
		query.prepare("Select guid from notebook where name=:newname");
		query.bindValue(":newname", newname);
		if (!query.exec())
			logger.log(logger.EXTREME, "notebook SQL retrieve has failed.");
		String val = null;
		if (query.next())
			val = query.valueString(0);
		return val;
	}
	// Get a note tag counts
	public List<Pair<String,Integer>> getNotebookCounts() {
		List<Pair<String,Integer>> counts = new ArrayList<Pair<String,Integer>>();		
		NSqlQuery query = new NSqlQuery(db.getConnection());
		if (!query.exec("select notebookGuid, count(guid) from note where active=1 group by notebookguid;")) {
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

