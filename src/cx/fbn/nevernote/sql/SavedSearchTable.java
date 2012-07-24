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
import java.util.HashMap;
import java.util.List;

import com.evernote.edam.type.QueryFormat;
import com.evernote.edam.type.SavedSearch;
import com.trolltech.qt.core.QBuffer;
import com.trolltech.qt.core.QByteArray;
import com.trolltech.qt.core.QIODevice;
import com.trolltech.qt.gui.QIcon;
import com.trolltech.qt.gui.QImage;
import com.trolltech.qt.gui.QPixmap;

import cx.fbn.nevernote.sql.driver.NSqlQuery;
import cx.fbn.nevernote.utilities.ApplicationLogger;

public class SavedSearchTable {
	private final ApplicationLogger 		logger;
	private final DatabaseConnection		db;

	
	// Constructor
	public SavedSearchTable(ApplicationLogger l, DatabaseConnection d) {
		logger = l;
		db = d;
	}
	// Create the table
	public void createTable() {
		NSqlQuery query = new NSqlQuery(db.getConnection());
        logger.log(logger.HIGH, "Creating table SavedSearch...");
        if (!query.exec("Create table SavedSearch (guid varchar primary key, " +
        		"name varchar, query varchar, format integer, sequence integer, isDirty boolean)"))
        	logger.log(logger.HIGH, "Table SavedSearch creation FAILED!!!"); 
	}
	// Drop the table
	public void dropTable() {
		NSqlQuery query = new NSqlQuery(db.getConnection());
		query.exec("Drop table SavedSearch");
	}
	// get all tags
	public List<SavedSearch> getAll() {
		SavedSearch tempSearch;
		List<SavedSearch> index = new ArrayList<SavedSearch>();
		boolean check;
						
        NSqlQuery query = new NSqlQuery(db.getConnection());
        				        
		check = query.exec("Select guid, name, query, format, sequence"
				+" from SavedSearch");
		if (!check)
			logger.log(logger.EXTREME, "SavedSearch SQL retrieve has failed in getAll().");
		while (query.next()) {
			tempSearch = new SavedSearch();
			tempSearch.setGuid(query.valueString(0));
			tempSearch.setName(query.valueString(1));
			tempSearch.setQuery(query.valueString(2));
			int fmt = new Integer(query.valueString(3));
			if (fmt == 1)
				tempSearch.setFormat(QueryFormat.USER);
			else
				tempSearch.setFormat(QueryFormat.SEXP);
			int sequence = new Integer(query.valueString(4)).intValue();
			tempSearch.setUpdateSequenceNum(sequence);
			index.add(tempSearch); 
		}	
		return index;
	}
	public SavedSearch getSavedSearch(String guid) {
		SavedSearch tempSearch = null;
		boolean check;
			
        NSqlQuery query = new NSqlQuery(db.getConnection());
        				        
		check = query.prepare("Select guid, name, query, format, sequence"
				+" from SavedSearch where guid=:guid");
		if (!check)
			logger.log(logger.EXTREME, "SavedSearch SQL prepare has failed in getSavedSearch.");
		query.bindValue(":guid", guid);
		query.exec();
		if (!check)
			logger.log(logger.EXTREME, "SavedSearch SQL retrieve has failed in getSavedSearch.");
		if (query.next()) {
			tempSearch = new SavedSearch();
			tempSearch.setGuid(query.valueString(0));
			tempSearch.setName(query.valueString(1));
			tempSearch.setQuery(query.valueString(2));
			int fmt = new Integer(query.valueString(3));
			if (fmt == 1)
				tempSearch.setFormat(QueryFormat.USER);
			else
				tempSearch.setFormat(QueryFormat.SEXP);
			int sequence = new Integer(query.valueInteger(4));
			tempSearch.setUpdateSequenceNum(sequence);
		}
		return tempSearch;
	}
	// Update a tag
	public void updateSavedSearch(SavedSearch search, boolean isDirty) {
		boolean check;
        NSqlQuery query = new NSqlQuery(db.getConnection());
		check = query.prepare("Update SavedSearch set sequence=:sequence, "+
			"name=:name, isDirty=:isDirty, query=:query, format=:format "
			+"where guid=:guid");
       
		if (!check) {
			logger.log(logger.EXTREME, "SavedSearch SQL update prepare has failed.");
			logger.log(logger.EXTREME, query.lastError().toString());
		}
		query.bindValue(":sequence", search.getUpdateSequenceNum());
		query.bindValue(":name", search.getName());
		query.bindValue(":isDirty", isDirty);
		query.bindValue(":query", search.getQuery());
		if (search.getFormat() == QueryFormat.USER)
			query.bindValue(":format", 1);
		else
			query.bindValue(":format", 2);
		
		query.bindValue(":guid", search.getGuid());
		
		check = query.exec();
		if (!check) {
			logger.log(logger.MEDIUM, "Tag Table update failed.");
			logger.log(logger.EXTREME, query.lastError().toString());
		}
	}
	// Delete a tag
	public void expungeSavedSearch(String guid, boolean needsSync) {
		boolean check;
		SavedSearch s = getSavedSearch(guid);
        NSqlQuery query = new NSqlQuery(db.getConnection());

       	check = query.prepare("delete from SavedSearch "
   				+"where guid=:guid");
		if (!check) {
			logger.log(logger.EXTREME, "SavedSearch SQL delete prepare has failed.");
			logger.log(logger.EXTREME, query.lastError().toString());
		}
		query.bindValue(":guid", guid);
		check = query.exec();
		if (!check) {
			logger.log(logger.MEDIUM, "Saved Search delete failed.");
			logger.log(logger.EXTREME, query.lastError().toString());
		}

		// Add the work to the parent queue
		if (needsSync && s != null && s.getUpdateSequenceNum() > 0) {
			DeletedTable del = new DeletedTable(logger, db);
			del.addDeletedItem(guid, "SavedSearch");
		}
	}
	// Save a tag
	public void addSavedSearch(SavedSearch search, boolean isDirty) {
		boolean check;
        NSqlQuery query = new NSqlQuery(db.getConnection());
		check = query.prepare("Insert Into SavedSearch (guid, query, sequence, format, name, isDirty)"
				+" Values(:guid, :query, :sequence, :format, :name, :isDirty)");
		if (!check) {
			logger.log(logger.EXTREME, "Search SQL insert prepare has failed.");
			logger.log(logger.EXTREME, query.lastError().toString());
		}
		query.bindValue(":guid", search.getGuid());
		query.bindValue(":query", search.getQuery());
		query.bindValue(":sequence", search.getUpdateSequenceNum());
		if (search.getFormat() == QueryFormat.USER)
			query.bindValue(":format", 1);
		else
			query.bindValue(":format", 2);
		query.bindValue(":name", search.getName());
		query.bindValue(":isDirty", isDirty);
	
		check = query.exec();
		if  (!check) {
			logger.log(logger.MEDIUM, "Search Table insert failed.");
			logger.log(logger.MEDIUM, query.lastError().toString());
		}
	}
	// Update a tag sequence number
	public void updateSavedSearchSequence(String guid, int sequence) {
		boolean check;
 		;
        NSqlQuery query = new NSqlQuery(db.getConnection());
		check = query.prepare("Update SavedSearch set sequence=:sequence where guid=:guid");
		query.bindValue(":sequence", sequence);
		query.bindValue(":guid", guid);
		query.exec();
		if (!check) {
			logger.log(logger.MEDIUM, "SavedSearch sequence update failed.");
			logger.log(logger.MEDIUM, query.lastError());
		}
	}
	// Update a tag sequence number
	public void updateSavedSearchGuid(String oldGuid, String newGuid) {
		boolean check;
        NSqlQuery query = new NSqlQuery(db.getConnection());
		check = query.prepare("Update SavedSearch set guid=:newGuid where guid=:oldGuid");
		query.bindValue(":newGuid", newGuid);
		query.bindValue(":oldGuid", oldGuid);
		query.exec();
		if (!check) {
			logger.log(logger.MEDIUM, "SavedSearch guid update failed.");
			logger.log(logger.MEDIUM, query.lastError());
		}
	}
	// Get dirty tags
	public List<SavedSearch> getDirty() {
		SavedSearch search;
		List<SavedSearch> index = new ArrayList<SavedSearch>();
		boolean check;
						
        NSqlQuery query = new NSqlQuery(db.getConnection());
        				        
		check = query.exec("Select guid, query, sequence, name, format"
				+" from SavedSearch where isDirty = true");
		if (!check)
			logger.log(logger.EXTREME, "SavedSearch getDirty prepare has failed.");
		while (query.next()) {
			search = new SavedSearch();
			search.setGuid(query.valueString(0));
			search.setQuery(query.valueString(1));
			int sequence = new Integer(query.valueString(2)).intValue();
			search.setUpdateSequenceNum(sequence);
			search.setName(query.valueString(3));
			int fmt = new Integer(query.valueString(4)).intValue();
			if (fmt == 1)
				search.setFormat(QueryFormat.USER);
			else
				search.setFormat(QueryFormat.SEXP);
			index.add(search); 
		}	
		return index;
	}
	// Find a guid based upon the name
	public String findSavedSearchByName(String name) {
		NSqlQuery query = new NSqlQuery(db.getConnection());
		
		query.prepare("Select guid from SavedSearch where name=:name");
		query.bindValue(":name", name);
		if (!query.exec())
			logger.log(logger.EXTREME, "SavedSearch SQL retrieve has failed in findSavedSearchByName().");
		String val = null;
		if (query.next())
			val = query.valueString(0);
		return val;
	}
	// given a guid, does the tag exist
	public boolean exists(String guid) {
		NSqlQuery query = new NSqlQuery(db.getConnection());
		query.prepare("Select guid from SavedSearch where guid=:guid");
		query.bindValue(":guid", guid);
		if (!query.exec())
			logger.log(logger.EXTREME, "SavedSearch SQL retrieve has failed in exists().");
		boolean retval = query.next();
		return retval;
	}
	// This is a convience method to check if a tag exists & update/create based upon it
	public void syncSavedSearch(SavedSearch search, boolean isDirty) {
		if (exists(search.getGuid()))
			updateSavedSearch(search, isDirty);
		else
			addSavedSearch(search, isDirty);
	}
	public void  resetDirtyFlag(String guid) {
		NSqlQuery query = new NSqlQuery(db.getConnection());
		
		query.prepare("Update SavedSearch set isdirty=false where guid=:guid");
		query.bindValue(":guid", guid);
		if (!query.exec())
			logger.log(logger.EXTREME, "Error resetting SavedSearch dirty field in resetDirtyFlag().");
	}

	
	// Get the custom icon
	public QIcon getIcon(String guid) {
		NSqlQuery query = new NSqlQuery(db.getConnection());
		
		if (!query.prepare("Select icon from SavedSearch where guid=:guid"))
			logger.log(logger.EXTREME, "Error preparing saved search icon select.");
		query.bindValue(":guid", guid);
		if (!query.exec())
			logger.log(logger.EXTREME, "Error finding saved search icon.");
		if (!query.next() || query.getBlob(0) == null)
			return null;
		
		QByteArray blob = new QByteArray(query.getBlob(0));
		QIcon icon = new QIcon(QPixmap.fromImage(QImage.fromData(blob)));
		return icon;
	}
	// Set the custom icon
	public void setIcon(String guid, QIcon icon, String type) {
		NSqlQuery query = new NSqlQuery(db.getConnection());
		if (icon == null) {
			if (!query.prepare("update SavedSearch set icon=null where guid=:guid"))
				logger.log(logger.EXTREME, "Error preparing saved search icon set.");
		} else {
			if (!query.prepare("update SavedSearch set icon=:icon where guid=:guid"))
				logger.log(logger.EXTREME, "Error preparing tag icon set.");
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
			logger.log(logger.LOW, "Error setting SavedSearch icon. " +query.lastError());
	}

	// Get a list of all icons
	public HashMap<String, QIcon> getAllIcons() {
		HashMap<String, QIcon> values = new HashMap<String, QIcon>();
		NSqlQuery query = new NSqlQuery(db.getConnection());
	
		if (!query.exec("SELECT guid, icon from SavedSearch"))
			logger.log(logger.EXTREME, "Error executing SavedSearch getAllIcons select.");
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


}
