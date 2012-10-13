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

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.evernote.edam.type.NoteSortOrder;
import com.evernote.edam.type.Notebook;
import com.evernote.edam.type.Publishing;
import com.trolltech.qt.core.QBuffer;
import com.trolltech.qt.core.QByteArray;
import com.trolltech.qt.core.QIODevice;
import com.trolltech.qt.gui.QIcon;
import com.trolltech.qt.gui.QImage;
import com.trolltech.qt.gui.QPixmap;

import cx.fbn.nevernote.Global;
import cx.fbn.nevernote.sql.driver.NSqlQuery;
import cx.fbn.nevernote.utilities.ApplicationLogger;
import cx.fbn.nevernote.utilities.Pair;

public class NotebookTable {
	
	private final ApplicationLogger 		logger;
	DatabaseConnection						db;
	private final String					dbName;
	NSqlQuery								notebookCountQuery;
	
	// Constructor
	public NotebookTable(ApplicationLogger l, DatabaseConnection d) {
		logger = l;
		db = d;
		dbName = "Notebook";
	}
	// Constructor
	public NotebookTable(ApplicationLogger l, DatabaseConnection d, String name) {
		logger = l;
		db = d;
		dbName = name;
	}

	// Create the table
	public void createTable(boolean addDefaulte) {
		NSqlQuery query = new NSqlQuery(db.getConnection());
        logger.log(logger.HIGH, "Creating table "+dbName+"...");
        if (!query.exec("Create table "+dbName+" (guid varchar primary key, " +
        		"sequence integer, " +
        		"name varchar, "+
        		"defaultNotebook varchar, "+
        		"serviceCreated timestamp, " +
        		"serviceUpdated timestamp, "+
        		"published boolean, "+
        		"isDirty boolean, "+
        		"autoEncrypt boolean, "+
        		"local boolean, "+
        		"archived boolean)"))	        		
        	logger.log(logger.HIGH, "Table "+dbName+" creation FAILED!!!");   
        Notebook newnote = new Notebook();
        newnote.setDefaultNotebook(true);
        newnote.setName("My Notebook");
        newnote.setPublished(false);
        newnote.setGuid("1");
        
        // Setup an initial notebook
		SimpleDateFormat simple = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
        query = new NSqlQuery(db.getConnection());
		query.prepare("Insert Into "+dbName+" (guid, sequence, name, defaultNotebook, "
				+"serviceCreated, serviceUpdated, published, "   
				+ "isDirty, autoEncrypt, " 
				+ "local, archived) Values("
				+":guid, :sequence, :name, :defaultNotebook,  "
				+":serviceCreated, :serviceUpdated, :published, "
				+":isDirty, :autoEncrypt, "
				+":local, false)");
		query.bindValue(":guid", newnote.getGuid());
		query.bindValue(":sequence", newnote.getUpdateSequenceNum());
		query.bindValue(":name", newnote.getName());
		query.bindValue(":defaultNotebook", newnote.isDefaultNotebook());
		
		StringBuilder serviceCreated = new StringBuilder(simple.format(newnote.getServiceCreated()));			
		StringBuilder serviceUpdated = new StringBuilder(simple.format(newnote.getServiceUpdated()));
		if (serviceUpdated.toString() == null)
			serviceUpdated = serviceCreated;
		query.bindValue(":serviceCreated", serviceCreated.toString());
		query.bindValue(":serviceUpdated", serviceCreated.toString());
		query.bindValue(":published",newnote.isPublished());
		
		query.bindValue(":isDirty", true);
		query.bindValue(":autoEncrypt", false);
		query.bindValue(":local", false);

		boolean check = query.exec();
		if (!check) {
			logger.log(logger.MEDIUM, "Initial "+dbName+" Table insert failed.");
			logger.log(logger.MEDIUM, query.lastError().toString());
		}

 		
	}
	// Drop the table
	public void dropTable() {
		NSqlQuery query = new NSqlQuery(db.getConnection());
		query.exec("Drop table "+dbName);
	}
	// Save an individual notebook
	public void addNotebook(Notebook tempNotebook, boolean isDirty, boolean local) {
		addNotebook(tempNotebook, isDirty, local, false, false);
	}
	// Save an individual notebook
	public void addNotebook(Notebook tempNotebook, boolean isDirty, boolean local, boolean linked, boolean readOnly) {
		boolean check;
		
		SimpleDateFormat simple = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
        NSqlQuery query = new NSqlQuery(db.getConnection());
		check = query.prepare("Insert Into "+dbName+" (guid, sequence, name, defaultNotebook, "
				+"serviceCreated, serviceUpdated, published, "   
				+ "publishingUri, publishingOrder, publishingAscending, publishingPublicDescription, "
				+ "isDirty, autoEncrypt, stack, " 
				+ "local, archived, readOnly, linked) Values("
				+":guid, :sequence, :name, :defaultNotebook,  "
				+":serviceCreated, :serviceUpdated, :published, "
				+":publishingUri, :publishingOrder, :publishingAscending, :publishingPublicDescription, "
				+":isDirty, :autoEncrypt, "
				+":stack, :local, false, :readOnly, :linked)");
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
		query.bindValue(":linked", linked);
		query.bindValue(":readOnly", readOnly);
		
		if (tempNotebook.isPublished() && tempNotebook.getPublishing() != null) {
			Publishing p = tempNotebook.getPublishing();
			query.bindValue(":publishingUri", p.getUri());
			query.bindValue(":publishingOrder", p.getOrder().getValue());
			query.bindValue(":publishingAscending", p.isAscending());
			query.bindValue(":publishingPublicDescription", p.getPublicDescription());
		} else {
			query.bindValue(":publishingUri", "");
			query.bindValue(":publishingOrder", 1);
			query.bindValue(":publishingAscending", 1);
			query.bindValue(":publishingPublicDescription", "");
		}
		
		if (isDirty)
			query.bindValue(":isDirty", true);
		else
			query.bindValue(":isDirty", false);
		query.bindValue(":autoEncrypt", false);
		query.bindValue(":local", local);
		query.bindValue(":stack", tempNotebook.getStack());

		check = query.exec();
		if (!check) {
			logger.log(logger.MEDIUM, ""+dbName+" Table insert failed.");
			logger.log(logger.MEDIUM, query.lastError().toString());
		}
	}
	// Delete the notebook based on a guid
	public void expungeNotebook(String guid, boolean needsSync) {
		boolean check;
		Notebook n;
		n = getNotebook(guid);
        NSqlQuery query = new NSqlQuery(db.getConnection());

       	check = query.prepare("delete from "+dbName+" where guid=:guid");
		if (!check) {
			logger.log(logger.EXTREME, dbName+" SQL delete prepare has failed.");
			logger.log(logger.EXTREME, query.lastError().toString());
		}
		query.bindValue(":guid", guid);
		check = query.exec();
		if (!check) 
			logger.log(logger.MEDIUM, dbName+" delete failed.");
		
		// Signal the parent that work needs to be done
		if  (needsSync && n!=null && n.getUpdateSequenceNum() > 0) {
			DeletedTable deletedTable = new DeletedTable(logger, db);
			deletedTable.addDeletedItem(guid, dbName);
		}
	}
	// Update a notebook
	public void updateNotebook(Notebook tempNotebook, boolean isDirty) {
		boolean check;
		
		SimpleDateFormat simple = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
		
        NSqlQuery query = new NSqlQuery(db.getConnection());
       	check = query.prepare("Update "+dbName+" set sequence=:sequence, name=:name, defaultNotebook=:defaultNotebook, " +
       			"serviceCreated=:serviceCreated, serviceUpdated=:serviceUpdated, "+
				"published=:published, isDirty=:isDirty, publishinguri=:uri, "+
				"publishingOrder=:order, " + 
				"publishingAscending=:ascending, " +
				"publishingPublicDescription=:desc, " +
				"stack=:stack " +
				"where guid=:guid ");
       	
		query.bindValue(":sequence", tempNotebook.getUpdateSequenceNum());
		query.bindValue(":name", tempNotebook.getName());
		query.bindValue(":defaultNotebook", tempNotebook.isDefaultNotebook());

		StringBuilder serviceCreated = new StringBuilder(simple.format(tempNotebook.getServiceCreated()));			
		StringBuilder serviceUpdated = new StringBuilder(simple.format(tempNotebook.getServiceUpdated()));			
		query.bindValue(":serviceCreated", serviceCreated.toString());
		query.bindValue(":serviceUpdated", serviceUpdated.toString());
		
		query.bindValue(":published", tempNotebook.isPublished());
		query.bindValue(":isDirty", isDirty);
		
		if (tempNotebook.isPublished()) {
			query.bindValue(":uri", tempNotebook.getPublishing().getUri());
			query.bindValue(":order", tempNotebook.getPublishing().getOrder().getValue());
			query.bindValue(":ascending", tempNotebook.getPublishing().isAscending());
			query.bindValue(":desc", tempNotebook.getPublishing().getPublicDescription());
		} else {
			query.bindValue(":uri", "");
			query.bindValue(":order", NoteSortOrder.CREATED.getValue());
			query.bindValue(":ascending", false);
			query.bindValue(":desc", "");
		}
		
		query.bindValue(":guid", tempNotebook.getGuid());
		query.bindValue(":stack", tempNotebook.getStack());
		
		check = query.exec();
		if (!check) {
			logger.log(logger.MEDIUM, dbName+" Table update failed.");
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
				"published, stack, publishinguri, publishingascending, publishingPublicDescription, "+
				"publishingOrder from "+dbName+" order by name");
		if (!check)
			logger.log(logger.EXTREME, dbName+" SQL retrieve has failed.");
		while (query.next()) {
			tempNotebook = new Notebook();
			tempNotebook.setGuid(query.valueString(0));
			int sequence = new Integer(query.valueString(1)).intValue();
			tempNotebook.setUpdateSequenceNum(sequence);
			tempNotebook.setName(query.valueString(2));
			tempNotebook.setDefaultNotebook(query.valueBoolean(3, false));
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
			tempNotebook.setStack(query.valueString(7));
			if (tempNotebook.isPublished()) {
				Publishing p = new Publishing();
				p.setUri(query.valueString(8));
				p.setAscending(query.valueBoolean(9, false));
				p.setPublicDescription(query.valueString(10));
				p.setOrder(NoteSortOrder.findByValue(query.valueInteger(11)));
				tempNotebook.setPublishing(p);
			}
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
				"serviceCreated, serviceUpdated, published, stack from "+dbName+" where local=true order by name");
		if (!check)
			logger.log(logger.EXTREME, dbName+" SQL retrieve has failed.");
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
			if (query.valueString(7) != null && !query.valueString(7).trim().equals(""))
				tempNotebook.setStack(query.valueString(7));
			index.add(tempNotebook); 
		}	
		return index;
	}
	// Archive or un-archive a notebook
	public void setArchived(String guid, boolean val) {
		boolean check;			
        NSqlQuery query = new NSqlQuery(db.getConnection());     				
		check = query.prepare("Update "+dbName+" set archived=:archived where guid=:guid");
		if (!check)
			logger.log(logger.EXTREME, dbName+" SQL archive update has failed.");
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
				"serviceCreated, serviceUpdated, published, stack, "+
				"publishinguri, publishingascending, publishingPublicDescription, "+
				"publishingOrder " +
				"from "+dbName+" where archived=true order by name");
		if (!check)
			logger.log(logger.EXTREME, dbName+" SQL retrieve has failed.");
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
			if (query.valueString(7) != null && !query.valueString(7).trim().equals(""))
				tempNotebook.setStack(query.valueString(7));
			
			if (tempNotebook.isPublished()) {
				Publishing p = new Publishing();
				p.setUri(query.valueString(8));
				p.setAscending(query.valueBoolean(9, false));
				p.setPublicDescription(query.valueString(10));
				p.setOrder(NoteSortOrder.findByValue(query.valueInteger(11)));
				tempNotebook.setPublishing(p);
			}
			
			index.add(tempNotebook); 
		}	
		return index;
	}	
	// Check for a local/remote notebook
	public boolean isNotebookLocal(String guid) {
        NSqlQuery query = new NSqlQuery(db.getConnection());
		
		query.prepare("Select local from "+dbName+" where guid=:guid");
		query.bindValue(":guid", guid);
		query.exec();
		if (!query.next()) {
			return false;
		}
		boolean returnValue = query.valueBoolean(0, false);
		return returnValue;
	}
	// Check for a local/remote notebook
	public boolean isNotebookLinked(String guid) {
        NSqlQuery query = new NSqlQuery(db.getConnection());
		
		query.prepare("Select linked from "+dbName+" where guid=:guid");
		query.bindValue(":guid", guid);
		query.exec();
		if (!query.next()) {
			return false;
		}
		boolean returnValue = query.valueBoolean(0, false);
		return returnValue;
	}
	public boolean isReadOnly(String guid) {
        NSqlQuery query = new NSqlQuery(db.getConnection());
		
		query.prepare("Select readOnly from "+dbName+" where guid=:guid and readOnly=true");
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
		check = query.prepare("Update "+dbName+" set sequence=:sequence where guid=:guid");
		query.bindValue(":guid", guid);
		query.bindValue(":sequence", sequence);
		query.exec();
		if (!check) {
			logger.log(logger.MEDIUM, dbName+" sequence update failed.");
			logger.log(logger.MEDIUM, query.lastError());
		} 
	}
	// Update a notebook GUID number
	public void updateNotebookGuid(String oldGuid, String newGuid) {
        NSqlQuery query = new NSqlQuery(db.getConnection());
		query.prepare("Update "+dbName+" set guid=:newGuid where guid=:oldGuid");
		query.bindValue(":oldGuid", oldGuid);
		query.bindValue(":newGuid", newGuid);
		if (!query.exec()) {
			logger.log(logger.MEDIUM, dbName+" guid update failed.");
			logger.log(logger.MEDIUM, query.lastError());
		} 
 		
		// Update any notes containing the notebook guid
		query.prepare("Update Note set notebookGuid=:newGuid where notebookGuid=:oldGuid");
		query.bindValue(":oldGuid", oldGuid);
		query.bindValue(":newGuid", newGuid);
		if (!query.exec()) {
			logger.log(logger.MEDIUM, dbName+" guid update for note failed.");
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
				"serviceCreated, serviceUpdated, published, stack, "+
				"publishinguri, publishingascending, publishingPublicDescription, "+
				"publishingOrder " +
				"from "+dbName+" where isDirty=true and local=false and linked=false");
		if (!check) 
			logger.log(logger.EXTREME, dbName+" SQL retrieve has failed.");
		while (query.next()) {
			tempNotebook = new Notebook();
			tempNotebook.setGuid(query.valueString(0));
			int sequence = new Integer(query.valueString(1)).intValue();
			tempNotebook.setUpdateSequenceNum(sequence);
			tempNotebook.setName(query.valueString(2));
			
			DateFormat indfm = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
			try {
				tempNotebook.setServiceCreated(indfm.parse(query.valueString(4)).getTime());
				tempNotebook.setServiceUpdated(indfm.parse(query.valueString(5)).getTime());
			} catch (ParseException e) {
				e.printStackTrace();
			}
			tempNotebook.setPublished(new Boolean(query.valueString(6)));
			if (query.valueString(7) != null && !query.valueString(7).trim().equals(""))
				tempNotebook.setStack(query.valueString(7));
			
			if (tempNotebook.isPublished()) {
				Publishing p = new Publishing();
				p.setUri(query.valueString(8));
				p.setAscending(query.valueBoolean(9, false));
				p.setPublicDescription(query.valueString(10));
				p.setOrder(NoteSortOrder.findByValue(query.valueInteger(11)));
				if (p.getPublicDescription() != null && p.getPublicDescription().trim().equalsIgnoreCase(""))
					p.setPublicDescription(null);
				tempNotebook.setPublishing(p);
			}
			
			index.add(tempNotebook);
		}	
		return index;	
	}
	// Get a list of notes that need to be updated
	public Notebook getNotebook(String guid) {
		Notebook tempNotebook;
		boolean check;
						
        NSqlQuery query = new NSqlQuery(db.getConnection());
        				
		query.prepare("Select guid, sequence, name, defaultNotebook, " +
				"serviceCreated, serviceUpdated, published, stack, "+
				"publishinguri, publishingascending, publishingPublicDescription, "+
				"publishingOrder " +
				"from "+dbName+" where guid=:guid");
		query.bindValue(":guid", guid);
		check  = query.exec();
		if (!check) 
			logger.log(logger.EXTREME, dbName+" SQL retrieve has failed.");
		while (query.next()) {
			tempNotebook = new Notebook();
			tempNotebook.setGuid(query.valueString(0));
			int sequence = new Integer(query.valueString(1)).intValue();
			tempNotebook.setUpdateSequenceNum(sequence);
			tempNotebook.setName(query.valueString(2));
			
			DateFormat indfm = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.S");
			try {
				tempNotebook.setServiceCreated(indfm.parse(query.valueString(4)).getTime());
				tempNotebook.setServiceUpdated(indfm.parse(query.valueString(5)).getTime());
			} catch (ParseException e) {
				e.printStackTrace();
			}
			tempNotebook.setPublished(new Boolean(query.valueString(6)));
			if (query.valueString(7) != null && !query.valueString(7).trim().equals(""))
				tempNotebook.setStack(query.valueString(7));
			
			if (tempNotebook.isPublished()) {
				Publishing p = new Publishing();
				p.setUri(query.valueString(8));
				p.setAscending(query.valueBoolean(9, false));
				p.setPublicDescription(query.valueString(10));
				p.setOrder(NoteSortOrder.findByValue(query.valueInteger(11)));
				if (p.getPublicDescription() != null && p.getPublicDescription().trim().equalsIgnoreCase(""))
					p.setPublicDescription(null);
				tempNotebook.setPublishing(p);
			}
			
			return tempNotebook;
		}	
		return null;	
	}
	// This is a convience method to check if a tag exists & update/create based upon it
	public void syncNotebook(Notebook notebook, boolean isDirty) {
		if (!exists(notebook.getGuid())) {
			addNotebook(notebook, isDirty, isDirty);
			return;
		}
		updateNotebook(notebook, isDirty);
	}
	// This is a convience method to check if a tag exists & update/create based upon it
	public void syncLinkedNotebook(Notebook notebook, boolean isDirty, boolean readOnly) {
		if (!exists(notebook.getGuid())) {
			addNotebook(notebook, isDirty, false, true, readOnly);
			return;
		}
		updateNotebook(notebook, isDirty);
	}
	// does a record exist?
	private boolean exists(String guid) {
 		
		NSqlQuery query = new NSqlQuery(db.getConnection());
		
		query.prepare("Select guid from "+dbName+" where guid=:guid");
		query.bindValue(":guid", guid);
		if (!query.exec())
			logger.log(logger.EXTREME, dbName+" SQL retrieve has failed.");
		boolean retval = query.next();
		return retval;
	}
	// Reset the dirty flag.  Typically done after a sync.
	public void  resetDirtyFlag(String guid) {
		NSqlQuery query = new NSqlQuery(db.getConnection());
		
		query.prepare("Update "+dbName+" set isdirty='false' where guid=:guid");
		query.bindValue(":guid", guid);
		if (!query.exec())
			logger.log(logger.EXTREME, "Error resetting "+dbName+" dirty field.");
	}
	// Set the default notebook
	public void setDefaultNotebook(String guid) {
		NSqlQuery query = new NSqlQuery(db.getConnection());
		
		query.prepare("Update "+dbName+" set defaultNotebook=false, isDirty=true where linked=false and defaultNotebook=true");
		if (!query.exec())
			logger.log(logger.EXTREME, "Error removing default "+dbName+".");
		query.prepare("Update "+dbName+" set defaultNotebook=true, isDirty=true where guid=:guid and linked=false");
		query.bindValue(":guid", guid);
		if (!query.exec())
			logger.log(logger.EXTREME, "Error setting default "+dbName+".");
	}
	
	// Get a list of all icons
	public HashMap<String, QIcon> getAllIcons() {
		HashMap<String, QIcon> values = new HashMap<String, QIcon>();
		NSqlQuery query = new NSqlQuery(db.getConnection());
	
		if (!query.exec("SELECT guid, icon from "+dbName+" where ARCHIVED  != true"))
			logger.log(logger.EXTREME, "Error executing "+dbName+" getAllIcons select.");
		while (query.next()) {
			if (query.getBlob(1) != null) {
				String guid = query.valueString(0);
				QByteArray blob = new QByteArray(query.getBlob(1));
				QIcon icon = new QIcon(QPixmap.fromImage(QImage.fromData(blob)));
				values.put(guid, icon);
			}
		}
		return values;
	}
	
	// Get the notebooks custom icon
	public QIcon getIcon(String guid) {
		NSqlQuery query = new NSqlQuery(db.getConnection());
		
		if (!query.prepare("Select icon from "+dbName+" where guid=:guid"))
			logger.log(logger.EXTREME, "Error preparing "+dbName+" icon select.");
		query.bindValue(":guid", guid);
		if (!query.exec())
			logger.log(logger.EXTREME, "Error finding "+dbName+" icon.");
		if (!query.next() || query.getBlob(0) == null)
			return null;
		
		QByteArray blob = new QByteArray(query.getBlob(0));
		QIcon icon = new QIcon(QPixmap.fromImage(QImage.fromData(blob)));
		return icon;
	}
	// Get the notebooks custom icon
	public QByteArray getIconAsByteArray(String guid) {
		NSqlQuery query = new NSqlQuery(db.getConnection());
		
		if (!query.prepare("Select icon from "+dbName+" where guid=:guid"))
			logger.log(logger.EXTREME, "Error preparing "+dbName+" icon select.");
		query.bindValue(":guid", guid);
		if (!query.exec())
			logger.log(logger.EXTREME, "Error finding "+dbName+" icon.");
		if (!query.next() || query.getBlob(0) == null)
			return null;
		
		QByteArray blob = new QByteArray(query.getBlob(0));
		return blob;
	}
	// Set the notebooks custom icon
	public void setIcon(String guid, QIcon icon, String type) {
		NSqlQuery query = new NSqlQuery(db.getConnection());
		if (icon == null) {
			if (!query.prepare("update "+dbName+" set icon=null where guid=:guid"))
				logger.log(logger.EXTREME, "Error preparing "+dbName+" icon select.");
		} else {
			if (!query.prepare("update "+dbName+" set icon=:icon where guid=:guid"))
				logger.log(logger.EXTREME, "Error preparing "+dbName+" icon select.");
			QBuffer buffer = new QBuffer();
	        if (!buffer.open(QIODevice.OpenModeFlag.ReadWrite)) {
	        	logger.log(logger.EXTREME, "Failure to open buffer.  Aborting.");
	        	return;
	        }
	        QPixmap p = icon.pixmap(32, 32);
	        QImage i = p.toImage();
	       	i.save(buffer, type.toUpperCase());
	       	buffer.close();
	       	QByteArray b = new QByteArray(buffer.buffer());
	       	if (!b.isNull() && !b.isEmpty())
	       		query.bindValue(":icon", b.toByteArray());
	       	else
	       		return;
		}
		query.bindValue(":guid", guid);
		if (!query.exec()) 
			logger.log(logger.LOW, "Error setting "+dbName+" icon. " +query.lastError());
	}
	// Set the notebooks custom icon
	public void setReadOnly(String guid, boolean readOnly) {
		NSqlQuery query = new NSqlQuery(db.getConnection());
		if (!query.prepare("update "+dbName+" set readOnly=:readOnly where guid=:guid"))
			logger.log(logger.EXTREME, "Error preparing "+dbName+" read only.");
		query.bindValue(":guid", guid);
		query.bindValue(":readOnly", readOnly);
		if (!query.exec()) 
			logger.log(logger.LOW, "Error setting "+dbName+" read only. " +query.lastError());
	}

	// does a record exist?
	public String findNotebookByName(String newname) {
 		
		NSqlQuery query = new NSqlQuery(db.getConnection());
		
		query.prepare("Select guid from "+dbName+" where name=:newname");
		query.bindValue(":newname", newname);
		if (!query.exec())
			logger.log(logger.EXTREME, dbName+" SQL retrieve has failed.");
		String val = null;
		if (query.next())
			val = query.valueString(0);
		return val;
	}
	// Get a note tag counts
	public List<Pair<String,Integer>> getNotebookCounts() {
		List<Pair<String,Integer>> counts = new ArrayList<Pair<String,Integer>>();		
		if (notebookCountQuery == null) {
			notebookCountQuery = new NSqlQuery(db.getConnection());
			notebookCountQuery.prepare("select notebookGuid, count(guid) from note where active=1 group by notebookguid;");
		}
		if (!notebookCountQuery.exec()) {
			logger.log(logger.EXTREME, "NoteTags SQL getTagCounts has failed.");
			logger.log(logger.MEDIUM, notebookCountQuery.lastError());
			return null;
		}
		while (notebookCountQuery.next()) {
			Pair<String,Integer> newCount = new Pair<String,Integer>();
			newCount.setFirst(notebookCountQuery.valueString(0));
			newCount.setSecond(notebookCountQuery.valueInteger(1));
			counts.add(newCount);
		}	
		return counts;
	}

	// Get/Set stacks
	public void clearStack(String guid) {
		NSqlQuery query = new NSqlQuery(db.getConnection());
		
		query.prepare("Update "+dbName+" set stack='' where guid=:guid");
		query.bindValue(":guid", guid);
		if (!query.exec())
			logger.log(logger.EXTREME, "Error clearing "+dbName+" stack.");
	}
	// Get/Set stacks
	public void setStack(String guid, String stack) {
		NSqlQuery query = new NSqlQuery(db.getConnection());
		
		query.prepare("Update "+dbName+" set stack=:stack, isDirty=true where guid=:guid");
		query.bindValue(":guid", guid);
		query.bindValue(":stack", stack);
		if (!query.exec())
			logger.log(logger.EXTREME, "Error setting notebook stack.");
	}
	// Get all stack names
	public List<String> getAllStackNames() {
		List<String> stacks = new ArrayList<String>();
		NSqlQuery query = new NSqlQuery(db.getConnection());
		
		if (!query.exec("Select distinct stack from "+dbName)) {
			logger.log(logger.EXTREME, "Error getting all stack names.");
			return null;
		}
		
		while (query.next()) {
			if (query.valueString(0) != null && !query.valueString(0).trim().equals(""))
				stacks.add(query.valueString(0));
		}
		return stacks;
	}
	// Rename a stack
	public void renameStacks(String oldName, String newName) {
		NSqlQuery query = new NSqlQuery(db.getConnection());
		
		if (!query.prepare("update "+dbName+" set stack=:newName where stack=:oldName")) {
			logger.log(logger.EXTREME, "Error preparing in renameStacks.");
			return;
		}
		query.bindValue(":oldName", oldName);
		query.bindValue(":newName", newName);
		if (!query.exec()) {
			logger.log(logger.EXTREME, "Error updating stack names");
			return;
		}
		
		if (!query.prepare("update SystemIcon set name=:newName where name=:oldName and type='STACK'")) {
			logger.log(logger.EXTREME, "Error preparing icon rename in renameStacks.");
			return;
		}
		query.bindValue(":oldName", oldName);
		query.bindValue(":newName", newName);
		if (!query.exec()) {
			logger.log(logger.EXTREME, "Error updating stack names for SystemIcon");
			return;
		}

	}
	// Get/Set stacks
	public boolean stackExists(String stack) {
		NSqlQuery query = new NSqlQuery(db.getConnection());
		
		query.prepare("Select guid from "+dbName+" where stack=:stack limit 1");
		query.bindValue(":stack", stack);
		if (!query.exec())
			logger.log(logger.EXTREME, "Error setting "+dbName+" stack.");
		if (query.next())
			return true;
		else
			return false;
	}
	// Set Publishing
	public void setPublishing(String guid, boolean published, Publishing p) {
		NSqlQuery query = new NSqlQuery(db.getConnection());
		
		
		query.prepare("Update "+dbName+" set publishingPublicDescription=:publishingPublicDescription, " +
				"publishingUri=:publishingUri, publishingOrder=:publishingOrder, published=:published, "+
				"publishingAscending=:publishingAscending, isdirty=true where "+
				"guid=:guid");
		query.bindValue(":publishingPublicDescription", p.getPublicDescription());
		query.bindValue(":publishingUri", p.getUri());
				query.bindValue(":publishingOrder", p.getOrder().getValue());
		query.bindValue(":publishingAscending", p.isAscending());
		query.bindValue(":publishingPublicDescription", p.getPublicDescription());
		query.bindValue(":published", published);
		query.bindValue(":guid", guid);
		if (!query.exec())
			logger.log(logger.EXTREME, "Error setting "+dbName+" stack.");
	}
	// Get a notebook by uri
	public String getNotebookByUri(String uri) {
		boolean check;
					
        NSqlQuery query = new NSqlQuery(db.getConnection());
        				
		check = query.prepare("Select guid " 
				+"from "+dbName+" where publishingUri=:uri");
		query.bindValue(":uri", uri);
		check = query.exec();
		if (!check)
			logger.log(logger.EXTREME, "Notebook SQL retrieve guid by uri has failed.");
		if (query.next()) {
			return query.valueString(0);
		}	
		return null;
	}	
	// Get a notebook's sort order
	public int getSortColumn(String guid) {
		boolean check;
					
        NSqlQuery query = new NSqlQuery(db.getConnection());
        				
        if (Global.getSortOrder() != Global.View_List_Wide)
        	check = query.prepare("Select wide_sort_column " 
				+"from "+dbName+" where guid=:guid");
        else
        	check = query.prepare("Select narrow_sort_column " 
			+"from "+dbName+" where guid=:guid");
		query.bindValue(":guid", guid);
		check = query.exec();
		if (!check) {
			logger.log(logger.EXTREME, "Notebook SQL retrieve sort order has failed.");
			return -1;
		}
		if (query.next()) {
			return query.valueInteger(0);
		}	
		return -1;
	}	

	// Get a notebook's sort order
	public int getSortOrder(String guid) {
		boolean check;
					
        NSqlQuery query = new NSqlQuery(db.getConnection());
        				
        if (Global.getSortOrder() != Global.View_List_Wide)
        	check = query.prepare("Select wide_sort_order " 
				+"from "+dbName+" where guid=:guid");
        else
        	check = query.prepare("Select narrow_sort_order " 
			+"from "+dbName+" where guid=:guid");
		query.bindValue(":guid", guid);
		check = query.exec();
		if (!check) {
			logger.log(logger.EXTREME, "Notebook SQL retrieve sort order has failed.");
			return -1;
		}
		if (query.next()) {
			return query.valueInteger(0);
		}	
		return -1;
	}	
	// Get a notebook's sort order
	public void setSortOrder(String guid, int column, int order) {
		boolean check;
					
        NSqlQuery query = new NSqlQuery(db.getConnection());
        				
        if (Global.getSortOrder() != Global.View_List_Wide)
        	check = query.prepare("Update "+dbName+" set wide_sort_order=:order, wide_sort_column=:column where guid=:guid");
        else
        	check = query.prepare("Update "+dbName+" set narrow_sort_order=:order, narrow_sort_column=:column where guid=:guid");

		query.bindValue(":guid", guid);
		query.bindValue(":order", order);
		query.bindValue(":column", column);
		check = query.exec();
		if (!check)
			logger.log(logger.EXTREME, "Notebook SQL set sort order has failed.");
	}	
	// Is a notebook a linked notebook?
	public boolean isLinked(String guid) {
		boolean check;
		
        NSqlQuery query = new NSqlQuery(db.getConnection());
        				
		check = query.prepare("Select guid " 
				+"from "+dbName+" where guid=:guid and linked=true");
		query.bindValue(":guid", guid);
		check = query.exec();
		if (!check)
			logger.log(logger.EXTREME, "Notebook SQL isLinked failed.");
		if (query.next()) {
			return true;
		}	
		return false;
	}

	// Given a notebook, what tags are valid for it?
	public List<String> getValidLinkedTags(String guid) {
		boolean check;
		List<String> tags = new ArrayList<String>();
		
        NSqlQuery query = new NSqlQuery(db.getConnection());       				
		check = query.prepare("select distinct tagGuid from noteTags " +
				"where noteGuid in " +
				"(SELECT guid from note where notebookguid=:guid)");
		query.bindValue(":guid", guid);
		check = query.exec();
		if (!check)
			logger.log(logger.EXTREME, "Notebook SQL getValidLinedTags failed.");
		while (query.next()) {
			tags.add(query.valueString(0));
		}	
		return tags;
		
		
	}
	// Given a notebook, what tags are valid for it?
	public void deleteLinkedTags(String guid) {
		
        NSqlQuery query = new NSqlQuery(db.getConnection());       				
		query.prepare("select distinct tagguid from noteTags " +
				"where noteGuid in " +
				"(SELECT guid from note where notebookguid=:guid)");
		query.bindValue(":guid", guid);
		boolean check = query.exec();
		if (!check)
			logger.log(logger.EXTREME, "Notebook SQL getValidLinedTags failed.");
		while(query.next()) {
			db.getTagTable().expungeTag(query.valueString(0), false);
		}
		
		
		query.prepare("delete from note " +
				"where notebookguid=:guid");
		query.bindValue(":guid", guid);
		check = query.exec();
		if (!check)
			logger.log(logger.EXTREME, "Notebook SQL getValidLinedTags failed.");

		
		return;
		
		
	}
	
	// Given a notebook, what tags are valid for it?
	public void convertFromSharedNotebook(String guid, boolean local) {
		
        NSqlQuery query = new NSqlQuery(db.getConnection());  
        
        query.prepare("Update Notebook set sequence=0, published=false, isdirty=true, local=:local, publishinguri=''"
				+" where guid=:guid");
		query.bindValue(":guid", guid);
		if (local)
			query.bindValue(":local", true);
		else
			query.bindValue(":local", false);
		
		if (!query.exec())
			logger.log(logger.EXTREME, "NotebookTable.convertToLocalNotebook error.");
		
        query.prepare("Update Note set updatesequencenumber=0, isdirty=true"
				+" where notebookguid=:guid");
		query.bindValue(":guid", guid);
		if (!query.exec())
			logger.log(logger.EXTREME, "NotebookTable.convertToLocalNotebook #2 error.");
			
		return;
		
		
	}
}

