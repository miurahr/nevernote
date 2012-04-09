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

import com.trolltech.qt.core.QBuffer;
import com.trolltech.qt.core.QByteArray;
import com.trolltech.qt.core.QIODevice;
import com.trolltech.qt.gui.QIcon;
import com.trolltech.qt.gui.QImage;
import com.trolltech.qt.gui.QPixmap;

import cx.fbn.nevernote.sql.driver.NSqlQuery;
import cx.fbn.nevernote.utilities.ApplicationLogger;

public class SystemIconTable {
	
	private final ApplicationLogger 		logger;
	DatabaseConnection							db;
	
	// Constructor
	public SystemIconTable(ApplicationLogger l, DatabaseConnection d) {
		logger = l;
		db = d;
	}
	// Create the table
	public void createTable() {
		NSqlQuery query = new NSqlQuery(db.getConnection());
        logger.log(logger.HIGH, "Creating table SystemIcon...");
        if (!query.exec("Create table SystemIcon (name varchar, " +
        		"type varchar, " +
        		"primary key(name, type), " +
        		"icon blob)"))	        		
        	logger.log(logger.HIGH, "Table SystemIcon creation FAILED!!!");   
	}
	// Drop the table
	public void dropTable() {
		NSqlQuery query = new NSqlQuery(db.getConnection());
		query.exec("Drop table SystemIcon");
	}

	
	// Get the notebooks custom icon
	public QIcon getIcon(String name, String type) {
		NSqlQuery query = new NSqlQuery(db.getConnection());
		
		if (!query.prepare("Select icon from SystemIcon where name=:name and type=:type"))
			logger.log(logger.EXTREME, "Error preparing system icon select.");
		query.bindValue(":name", name);
		query.bindValue(":type", type);
		if (!query.exec())
			logger.log(logger.EXTREME, "Error finding system icon.");
		if (!query.next() || query.getBlob(0) == null)
			return null;
		
		QByteArray blob = new QByteArray(query.getBlob(0));
		QIcon icon = new QIcon(QPixmap.fromImage(QImage.fromData(blob)));
		return icon;
	}
	
	
	// Get the notebooks custom icon
	public boolean exists(String name, String type) {
		NSqlQuery query = new NSqlQuery(db.getConnection());
		
		if (!query.prepare("Select icon from SystemIcon where name=:name and type=:type"))
			logger.log(logger.EXTREME, "Error preparing system icon select.");
		query.bindValue(":name", name);
		query.bindValue(":type", type);
		if (!query.exec())
			logger.log(logger.EXTREME, "Error finding system icon.");
		if (!query.next() || query.getBlob(0) == null)
			return false;
		return true;
	}
	
	
	// Set the notebooks custom icon
	public void setIcon(String name, String rectype, QIcon icon, String filetype) {
		if (exists(name, rectype))
			updateIcon(name, rectype, icon, filetype);
		else
			addIcon(name, rectype, icon, filetype);
	}
	
	
	
	// Set the notebooks custom icon
	public void addIcon(String name, String rectype, QIcon icon, String filetype) {
		NSqlQuery query = new NSqlQuery(db.getConnection());
		if (icon == null) {
			return;
		} else {
			if (!query.prepare("Insert into SystemIcon (icon, name, type) values (:icon, :name, :type)"))
				logger.log(logger.EXTREME, "Error preparing notebook icon select.");
			QBuffer buffer = new QBuffer();
	        if (!buffer.open(QIODevice.OpenModeFlag.ReadWrite)) {
	        	logger.log(logger.EXTREME, "Failure to open buffer.  Aborting.");
	        	return;
	        }
	        QPixmap p = icon.pixmap(32, 32);
	        QImage i = p.toImage();
	       	i.save(buffer, filetype.toUpperCase());
	       	buffer.close();
	       	QByteArray b = new QByteArray(buffer.buffer());
	       	if (!b.isNull() && !b.isEmpty())
	       		query.bindValue(":icon", b.toByteArray());
	       	else
	       		return;
		}
		query.bindValue(":name", name);
		query.bindValue(":type", rectype);
		if (!query.exec()) 
			logger.log(logger.LOW, "Error setting system icon. " +query.lastError());
	}
	
	
	
	// Set the notebooks custom icon
	public void updateIcon(String name, String rectype, QIcon icon, String filetype) {
		NSqlQuery query = new NSqlQuery(db.getConnection());
		if (icon == null) {
			if (!query.prepare("delete from SystemIcon where name=:name and type=:type"))
				logger.log(logger.EXTREME, "Error preparing notebook icon select.");
		} else {
			if (!query.prepare("update SystemIcon set icon=:icon where name=:name and type=:type"))
				logger.log(logger.EXTREME, "Error preparing notebook icon select.");
			QBuffer buffer = new QBuffer();
	        if (!buffer.open(QIODevice.OpenModeFlag.ReadWrite)) {
	        	logger.log(logger.EXTREME, "Failure to open buffer.  Aborting.");
	        	return;
	        }
	        QPixmap p = icon.pixmap(32, 32);
	        QImage i = p.toImage();
	       	i.save(buffer, filetype.toUpperCase());
	       	buffer.close();
	       	QByteArray b = new QByteArray(buffer.buffer());
	       	if (!b.isNull() && !b.isEmpty())
	       		query.bindValue(":icon", b.toByteArray());
	       	else
	       		return;
		}
		query.bindValue(":name", name);
		query.bindValue(":type", rectype);
		if (!query.exec()) 
			logger.log(logger.LOW, "Error setting system icon. " +query.lastError());
	}

}

