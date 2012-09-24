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

import com.evernote.edam.type.Tag;
import com.trolltech.qt.core.QBuffer;
import com.trolltech.qt.core.QByteArray;
import com.trolltech.qt.core.QIODevice;
import com.trolltech.qt.gui.QIcon;
import com.trolltech.qt.gui.QImage;
import com.trolltech.qt.gui.QPixmap;

import cx.fbn.nevernote.sql.driver.NSqlQuery;
import cx.fbn.nevernote.utilities.ApplicationLogger;

public class TagTable {
	private final ApplicationLogger logger;
	DatabaseConnection db;

	public TagTable (ApplicationLogger l, DatabaseConnection d) {
		logger = l;
		db = d;
	}
	// Create the table
	public void createTable() {
 		
		NSqlQuery query = new NSqlQuery(db.getConnection());
        logger.log(logger.HIGH, "Creating table Tag...");
        if (!query.exec("Create table Tag (guid varchar primary key, " +
        		"parentGuid varchar, sequence integer, hashCode integer, name varchar, isDirty boolean)"))
        	logger.log(logger.HIGH, "Table TAG creation FAILED!!!");  
 		
	}
	// Drop the table
	public void dropTable() {
 		
		NSqlQuery query = new NSqlQuery(db.getConnection());
		query.exec("Drop table Tag");
 		
	}
	// Get tags for a specific notebook
	// get all tags
	public List<Tag> getTagsForNotebook(String notebookGuid) {
 		
		Tag tempTag;
		List<Tag> index = new ArrayList<Tag>();
		boolean check;
						
        NSqlQuery query = new NSqlQuery(db.getConnection());
        				        
		check = query.prepare("Select guid, parentGuid, sequence, name"
				+" from Tag where notebookGuid=:notebookGuid");
		if (!check) {
			logger.log(logger.EXTREME, "Tag SQL prepare getTagsForNotebook has failed.");
			logger.log(logger.EXTREME, query.lastError());
		}
		query.bindValue(":notebookGuid", notebookGuid);
		query.exec();
		while (query.next()) {
			tempTag = new Tag();
			tempTag.setGuid(query.valueString(0));
			if (query.valueString(1) != null)
				tempTag.setParentGuid(query.valueString(1));
			else
				tempTag.setParentGuid(null);
			int sequence = new Integer(query.valueString(2)).intValue();
			tempTag.setUpdateSequenceNum(sequence);
			tempTag.setName(query.valueString(3));
			index.add(tempTag); 
		}	
 		
		return index;
	}
	// get all tags
	public List<Tag> getAll() {
 		
		Tag tempTag;
		List<Tag> index = new ArrayList<Tag>();
		boolean check;
						
        NSqlQuery query = new NSqlQuery(db.getConnection());
        				        
		check = query.exec("Select guid, parentGuid, sequence, name"
				+" from Tag where notebookguid not in (select guid from notebook where archived=true)");
		if (!check) {
			logger.log(logger.EXTREME, "Tag SQL retrieve has failed.");
			logger.log(logger.EXTREME, query.lastError());
		}
		while (query.next()) {
			tempTag = new Tag();
			tempTag.setGuid(query.valueString(0));
			if (query.valueString(1) != null)
				tempTag.setParentGuid(query.valueString(1));
			else
				tempTag.setParentGuid(null);
			int sequence = new Integer(query.valueString(2)).intValue();
			tempTag.setUpdateSequenceNum(sequence);
			tempTag.setName(query.valueString(3));
			index.add(tempTag); 
		}	
 		
		return index;
	}
	public Tag getTag(String guid) {
		Tag tempTag = new Tag();		
 		
        NSqlQuery query = new NSqlQuery(db.getConnection());
	        				        
		if (!query.prepare("Select guid, parentGuid, sequence, name"
				+" from Tag where guid=:guid"))
			logger.log(logger.EXTREME, "Tag select by guid SQL prepare has failed.");

		query.bindValue(":guid", guid);
		if (!query.exec())
			logger.log(logger.EXTREME, "Tag select by guid SQL exec has failed.");
		
		if (!query.next())  {
			return tempTag;
		}
		tempTag.setGuid(query.valueString(0));
		tempTag.setParentGuid(query.valueString(1));
		int sequence = new Integer(query.valueString(2)).intValue();
		tempTag.setUpdateSequenceNum(sequence);
		tempTag.setName(query.valueString(3));
		return tempTag;
	}
	// Update a tag
	public void updateTag(Tag tempTag, boolean isDirty) {
		updateTag(tempTag, isDirty, "");
	}
	// Update a tag
	public void updateTag(Tag tempTag, boolean isDirty, String realName) {
		boolean check;
		
        NSqlQuery query = new NSqlQuery(db.getConnection());
		check = query.prepare("Update Tag set parentGuid=:parentGuid, sequence=:sequence, "+
			"hashCode=:hashCode, name=:name, isDirty=:isDirty "
			+"where guid=:guid");
      
		if (!check) {
			logger.log(logger.EXTREME, "Tag SQL update prepare has failed.");
			logger.log(logger.EXTREME, query.lastError());
		}
		query.bindValue(":parentGuid", tempTag.getParentGuid());
		query.bindValue(":sequence", tempTag.getUpdateSequenceNum());
		query.bindValue(":hashCode", tempTag.hashCode());
		query.bindValue(":name", tempTag.getName());
		query.bindValue(":isDirty", isDirty);
		query.bindValue(":guid", tempTag.getGuid());
		
		check = query.exec();
		if (!check)
			logger.log(logger.MEDIUM, "Tag Table update failed.");
 		
	}
	// Delete a tag
	public void expungeTag(String guid, boolean needsSync) {
		boolean check;
		Tag t = getTag(guid);
		
 		
        NSqlQuery query = new NSqlQuery(db.getConnection());

       	check = query.prepare("delete from Tag "
   				+"where guid=:guid");
		if (!check) {
			logger.log(logger.EXTREME, "Tag SQL delete prepare has failed.");
			logger.log(logger.EXTREME, query.lastError());
		}
		query.bindValue(":guid", guid);
		check = query.exec();
		if (!check)
			logger.log(logger.MEDIUM, "Tag delete failed.");
		
       	check = query.prepare("delete from NoteTags "
   				+"where tagGuid=:guid");
		if (!check) {
			logger.log(logger.EXTREME, "NoteTags SQL delete prepare has failed.");
			logger.log(logger.EXTREME, query.lastError());
		}
		
		query.bindValue(":guid", guid);
		check = query.exec();
		if (!check)
			logger.log(logger.MEDIUM, "NoteTags delete failed.");
		
		// Add the work to the parent queue
		if (needsSync && t!= null && t.getUpdateSequenceNum() > 0) {
			DeletedTable del = new DeletedTable(logger, db);
			del.addDeletedItem(guid, "Tag");
		}
	}
	// Save a tag
	public void addTag(Tag tempTag, boolean isDirty) {
		addTag(tempTag, isDirty, false, "", "");
	}
	// Save a tag
	public void addTag(Tag tempTag, boolean isDirty, boolean isLinked, String realName, String notebookGuid) {
		boolean check;
 		
        NSqlQuery query = new NSqlQuery(db.getConnection());
		check = query.prepare("Insert Into Tag (guid, parentGuid, sequence, hashCode, name, isDirty, linked, realName, notebookGuid)"
				+" Values(:guid, :parentGuid, :sequence, :hashCode, :name, :isDirty, :linked, :realName, :notebookGuid)");
		if (!check) {
			logger.log(logger.EXTREME, "Tag SQL insert prepare has failed.");
			logger.log(logger.EXTREME, query.lastError());
		}
		query.bindValue(":guid", tempTag.getGuid());
		query.bindValue(":parentGuid", tempTag.getParentGuid());
		query.bindValue(":sequence", tempTag.getUpdateSequenceNum());
		query.bindValue(":hashCode", tempTag.hashCode());
		query.bindValue(":name", tempTag.getName());
		query.bindValue(":isDirty", isDirty);
		query.bindValue(":linked", isLinked);
		query.bindValue(":realName", realName);
		query.bindValue(":notebookGuid", notebookGuid);
		
		check = query.exec();
		if (!check) {
			logger.log(logger.MEDIUM, "Tag Table insert failed.");
			logger.log(logger.MEDIUM, query.lastError());
		}
	}
	// Update a tag's parent
	public void updateTagParent(String guid, String parentGuid) {
		boolean check;
 		
        NSqlQuery query = new NSqlQuery(db.getConnection());
		check = query.prepare("Update Tag set parentGuid=:parentGuid where guid=:guid");
		if (!check) {
			logger.log(logger.EXTREME, "Tag SQL tag parent update prepare has failed.");
			logger.log(logger.EXTREME, query.lastError());
		}

		query.bindValue(":parentGuid", parentGuid);
		query.bindValue(":guid", guid);
		
		check = query.exec();
		if (!check) {
			logger.log(logger.MEDIUM, "Tag parent update failed.");
			logger.log(logger.MEDIUM, query.lastError());
		}
	}
	//Save tags from Evernote
	public void saveTags(List<Tag> tags) {
		Tag tempTag;
		for (int i=0; i<tags.size(); i++) {
			tempTag = tags.get(i);
			addTag(tempTag, false);
		}		
	}
	// Update a tag sequence number
	public void updateTagSequence(String guid, int sequence) {
		boolean check;
        NSqlQuery query = new NSqlQuery(db.getConnection());
		check = query.prepare("Update Tag set sequence=:sequence where guid=:guid");
		query.bindValue(":sequence", sequence);
		query.bindValue(":guid", guid);
		
		query.exec();
		if (!check) {
			logger.log(logger.MEDIUM, "Tag sequence update failed.");
			logger.log(logger.MEDIUM, query.lastError());
		}
 		
	}
	// Update a tag sequence number
	public void updateTagGuid(String oldGuid, String newGuid) {
		boolean check;
 		
        NSqlQuery query = new NSqlQuery(db.getConnection());
		check = query.prepare("Update Tag set guid=:newGuid where guid=:oldGuid");
		query.bindValue(":newGuid", newGuid);
		query.bindValue(":oldGuid", oldGuid);
		query.exec();
		if (!check) {
			logger.log(logger.MEDIUM, "Tag guid update failed.");
			logger.log(logger.MEDIUM, query.lastError());
		}
		
		check = query.prepare("Update Tag set parentGuid=:newGuid where parentGuid=:oldGuid");
		query.bindValue(":newGuid", newGuid);
		query.bindValue(":oldGuid", oldGuid);
		query.exec();
		if (!check) {
			logger.log(logger.MEDIUM, "Tag guid update failed.");
			logger.log(logger.MEDIUM, query.lastError());
		}
		
		check = query.prepare("Update NoteTags set tagGuid=:newGuid where tagGuid=:oldGuid");
		query.bindValue(":newGuid", newGuid);
		query.bindValue(":oldGuid", oldGuid);
		query.exec();
		if (!check) {
			logger.log(logger.MEDIUM, "Tag guid update failed for NoteTags.");
			logger.log(logger.MEDIUM, query.lastError());
		}
 		
	}
	// Get dirty tags
	public List<Tag> getDirty() {
		Tag tempTag;
		List<Tag> index = new ArrayList<Tag>();
		boolean check;
						
 		
        NSqlQuery query = new NSqlQuery(db.getConnection());
        				        
		check = query.exec("Select guid, parentGuid, sequence, name"
				+" from Tag where isDirty = true");
		if (!check)
			logger.log(logger.EXTREME, "Tag SQL retrieve has failed.");
		while (query.next()) {
			tempTag = new Tag();
			tempTag.setGuid(query.valueString(0));
			tempTag.setParentGuid(query.valueString(1));
			int sequence = new Integer(query.valueString(2)).intValue();
			tempTag.setUpdateSequenceNum(sequence);
			tempTag.setName(query.valueString(3));
			if (tempTag.getParentGuid() != null && tempTag.getParentGuid().equals(""))
				tempTag.setParentGuid(null);
			index.add(tempTag); 
		}
		return index;
	}
	// Find a guid based upon the name
	public String findTagByName(String name) {
 		
		NSqlQuery query = new NSqlQuery(db.getConnection());
		
		query.prepare("Select guid from tag where name=:name");
		query.bindValue(":name", name);
		if (!query.exec())
			logger.log(logger.EXTREME, "Tag SQL retrieve has failed.");
		String val = null;
		if (query.next())
			val = query.valueString(0);
		return val;
	}
	// Get the linked notebook guid for this tag
	public String getNotebookGuid(String guid) {
 		
		NSqlQuery query = new NSqlQuery(db.getConnection());
		
		query.prepare("Select notebookguid from tag where guid=:guid");
		query.bindValue(":guid", guid);
		if (!query.exec())
			logger.log(logger.EXTREME, "Tag SQL retrieve has failed.");
		String val = null;
		if (query.next())
			val = query.valueString(0);
		return val;
	}
	// given a guid, does the tag exist
	public boolean exists(String guid) {
 		
		NSqlQuery query = new NSqlQuery(db.getConnection());
		
		query.prepare("Select guid from tag where guid=:guid");
		query.bindValue(":guid", guid);
		if (!query.exec())
			logger.log(logger.EXTREME, "Tag SQL retrieve has failed.");
		boolean retval = query.next();
		return retval;
	}
	// This is a convience method to check if a tag exists & update/create based upon it
	public void syncLinkedTag(Tag tag, String notebookGuid, boolean isDirty) {
		if (exists(tag.getGuid())) {
			Tag t = getTag(tag.getGuid());
			String realName = tag.getName();
			tag.setName(t.getName());
			updateTag(tag, isDirty, realName);
		}
		else
			addTag(tag, isDirty, true, tag.getName(), notebookGuid);
	}

	// This is a convience method to check if a tag exists & update/create based upon it
	public void syncTag(Tag tag, boolean isDirty) {
		if (exists(tag.getGuid()))
			updateTag(tag, isDirty);
		else
			addTag(tag, isDirty);
	}
	public void  resetDirtyFlag(String guid) {
 		
		NSqlQuery query = new NSqlQuery(db.getConnection());
		
		query.prepare("Update tag set isdirty=false where guid=:guid");
		query.bindValue(":guid", guid);
		if (!query.exec())
			logger.log(logger.EXTREME, "Error resetting tag dirty field.");
	}
	
	
	// Get the custom icon
	public QIcon getIcon(String guid) {
		NSqlQuery query = new NSqlQuery(db.getConnection());
		
		if (!query.prepare("Select icon from tag where guid=:guid"))
			logger.log(logger.EXTREME, "Error preparing tag icon select.");
		query.bindValue(":guid", guid);
		if (!query.exec())
			logger.log(logger.EXTREME, "Error finding tag icon.");
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
			if (!query.prepare("update tag set icon=null where guid=:guid"))
				logger.log(logger.EXTREME, "Error preparing tag icon update.");
		} else {
			if (!query.prepare("update tag set icon=:icon where guid=:guid"))
				logger.log(logger.EXTREME, "Error preparing tag icon update.");
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
			logger.log(logger.LOW, "Error setting tag icon. " +query.lastError());
	}

	// Get a list of all icons
	public HashMap<String, QIcon> getAllIcons() {
		HashMap<String, QIcon> values = new HashMap<String, QIcon>();
		NSqlQuery query = new NSqlQuery(db.getConnection());
	
		if (!query.exec("SELECT guid, icon from tag"))
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

	// Remove unused tags that are linked tags
	public void removeUnusedLinkedTags() {
		NSqlQuery query = new NSqlQuery(db.getConnection());
		
		query.exec("Delete from tag where linked=true and guid not in (select distinct tagguid from notetags);");
	}
	
	public void cleanupTags() {
		NSqlQuery query = new NSqlQuery(db.getConnection());
		
		query.exec("Update tag set parentguid=null where parentguid not in (select distinct guid from tag);");	
	}

	
	public List<String> findChildren(String guid, List<Tag> tagList) {
		List<String> returnValue = new ArrayList<String>();
		
		for (int i=0; i<tagList.size(); i++) {
			if (tagList.get(i).getParentGuid().equalsIgnoreCase(guid)) {
				returnValue.add(tagList.get(i).getName());
				List<String> childMatch = findChildren(tagList.get(i).getGuid(), tagList);
				for (int j=0; j<childMatch.size(); j++) {
					returnValue.add(childMatch.get(j));
				}
			}
		}
		return returnValue;
	}
}
