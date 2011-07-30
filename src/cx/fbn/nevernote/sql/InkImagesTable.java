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

import com.trolltech.qt.core.QByteArray;

import cx.fbn.nevernote.sql.driver.NSqlQuery;
import cx.fbn.nevernote.utilities.ApplicationLogger;

public class InkImagesTable {
	private final ApplicationLogger 		logger;
	DatabaseConnection						db;

	// Constructor
	public InkImagesTable(ApplicationLogger l,DatabaseConnection d) {
		logger = l;
		db = d;
	}
	// Create the table
	public void createTable() {
		NSqlQuery query = new NSqlQuery(db.getConnection());
        // Create the NoteTag table
        logger.log(logger.HIGH, "Creating table InkImage...");
        if (!query.exec("Create table InkImages (guid varchar, " +
        		"slice integer, primary key(guid, slice), image blob)"))
        	logger.log(logger.HIGH, "Table InkImage creation FAILED!!!"); 
	}
	// Drop the table
	public void dropTable() {
		NSqlQuery query = new NSqlQuery(db.getConnection());
		query.exec("drop table InkImages");
	}
	// Delete an image
	public void expungeImage(String guid) {
		NSqlQuery query = new NSqlQuery(db.getConnection());
		if (!query.prepare("Delete from InkImages where guid=:guid ")) {
			logger.log(logger.EXTREME, "InkImage SQL prepare has failed.");
			logger.log(logger.MEDIUM, query.lastError());
			return;
		}
		query.bindValue(":guid", guid);
		if (!query.exec()) {
			logger.log(logger.EXTREME, "InkImage SQL delete has failed.");
			logger.log(logger.MEDIUM, query.lastError());
			return;
		}
		return;		
	}
	// Get a note tags by the note's Guid
	public List<QByteArray> getImage(String guid) {
		List<QByteArray> data = new ArrayList<QByteArray>();
		NSqlQuery query = new NSqlQuery(db.getConnection());
		if (!query.prepare("Select image from InkImages where guid = :guid order by slice")) {
			logger.log(logger.EXTREME, "InkImage SQL prepare has failed.");
			logger.log(logger.MEDIUM, query.lastError());
			return null;
		}
		query.bindValue(":guid", guid);
		if (!query.exec()) {
			logger.log(logger.EXTREME, "InkImage SQL exec has failed.");
			logger.log(logger.MEDIUM, query.lastError());
			return null;
		}
		while (query.next()) {
			data.add(new QByteArray(query.getBlob(0)));	
		}	
		return data;
	}
	// Save an ink note image
	public void saveImage(String guid, int slice, QByteArray data) {
		logger.log(logger.HIGH, "Entering inkImageTable.saveImage");
		boolean check;
		NSqlQuery query = new NSqlQuery(db.getConnection());
		check = query.prepare("Insert Into InkImages ("
				+"guid, slice, image) Values("
				+":guid, :slice, :data)");
		if (!check) {
			logger.log(logger.EXTREME, "InkImages SQL insert prepare has failed.");
			logger.log(logger.MEDIUM, query.lastError());
			return;
		}
		query.bindValue(":guid", guid);
		query.bindValue(":slice", slice);
		query.bindBlob(":data", data.toByteArray());						
		check = query.exec();
		if (!check) {
			logger.log(logger.MEDIUM, "*** InkImages Table insert failed.");		
			logger.log(logger.MEDIUM, query.lastError());
		}			
		logger.log(logger.HIGH, "Leaving InkImages.saveImage");
	}

}
