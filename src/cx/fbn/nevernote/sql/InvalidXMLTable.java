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

public class InvalidXMLTable {
	ListManager parent;
	private final ApplicationLogger 		logger;
	private final DatabaseConnection		db;

	
	// Constructor
	public InvalidXMLTable(ApplicationLogger l, DatabaseConnection d) {
		logger = l;
		db = d;
	}
	// Create the table
	public void createTable() {
		NSqlQuery query = new NSqlQuery(db.getConnection());
//		query.exec("drop table InvalidXML");
		logger.log(logger.HIGH, "Creating table InvalidXML...");
        if (!query.exec("Create table InvalidXML (type varchar, element varchar, attribute varchar,primary key(type, element,attribute) );"))
           	logger.log(logger.HIGH, "Table InvalidXML creation FAILED!!!"); 
//        query.clear();
        
        query.exec("Insert into InvalidXML (type, element, attribute) values ('ELEMENT', 'button', '');");
        query.exec("Insert into InvalidXML (type, element, attribute) values ('ELEMENT', 'embed', '');");
        query.exec("Insert into InvalidXML (type, element, attribute) values ('ELEMENT', 'fieldset', '');");
        query.exec("Insert into InvalidXML (type, element, attribute) values ('ELEMENT', 'form', '');");
        query.exec("Insert into InvalidXML (type, element, attribute) values ('ELEMENT', 'input', '');");
        query.exec("Insert into InvalidXML (type, element, attribute) values ('ELEMENT', 'label', '');");
        query.exec("Insert into InvalidXML (type, element, attribute) values ('ELEMENT', 'legend', '');");
        query.exec("Insert into InvalidXML (type, element, attribute) values ('ELEMENT', 'o:p', '')");
        query.exec("Insert into InvalidXML (type, element, attribute) values ('ELEMENT', 'option', '')");        
        query.exec("Insert into InvalidXML (type, element, attribute) values ('ELEMENT', 'script', '')");
        query.exec("Insert into InvalidXML (type, element, attribute) values ('ELEMENT', 'select', '')");
        query.exec("Insert into InvalidXML (type, element, attribute) values ('ELEMENT', 'wbr', '')");
        
        query.exec("Insert into InvalidXML (type, element, attribute) values ('ATTRIBUTE', 'a', 'class')");
        query.exec("Insert into InvalidXML (type, element, attribute) values ('ATTRIBUTE', 'a', 'done')");
        query.exec("Insert into InvalidXML (type, element, attribute) values ('ATTRIBUTE', 'a', 'id')");
        query.exec("Insert into InvalidXML (type, element, attribute) values ('ATTRIBUTE', 'a', 'onclick')");
        query.exec("Insert into InvalidXML (type, element, attribute) values ('ATTRIBUTE', 'a', 'onmousedown')");
        query.exec("Insert into InvalidXML (type, element, attribute) values ('ATTRIBUTE', 'div', 'id')");
        query.exec("Insert into InvalidXML (type, element, attribute) values ('ATTRIBUTE', 'dl', 'class')");
        query.exec("Insert into InvalidXML (type, element, attribute) values ('ATTRIBUTE', 'dl', 'id')");
        query.exec("Insert into InvalidXML (type, element, attribute) values ('ATTRIBUTE', 'dt', 'class')");
        query.exec("Insert into InvalidXML (type, element, attribute) values ('ATTRIBUTE', 'h1', 'class')");
        query.exec("Insert into InvalidXML (type, element, attribute) values ('ATTRIBUTE', 'h2', 'class')");
        query.exec("Insert into InvalidXML (type, element, attribute) values ('ATTRIBUTE', 'h3', 'class')");
        query.exec("Insert into InvalidXML (type, element, attribute) values ('ATTRIBUTE', 'h4', 'class')");
        query.exec("Insert into InvalidXML (type, element, attribute) values ('ATTRIBUTE', 'h5', 'class')");
        query.exec("Insert into InvalidXML (type, element, attribute) values ('ATTRIBUTE', 'img', 'gptag')");
        query.exec("Insert into InvalidXML (type, element, attribute) values ('ATTRIBUTE', 'li', 'class')");
        query.exec("Insert into InvalidXML (type, element, attribute) values ('ATTRIBUTE', 'ol', 'class')");
        query.exec("Insert into InvalidXML (type, element, attribute) values ('ATTRIBUTE', 'ol', 'id')");
        query.exec("Insert into InvalidXML (type, element, attribute) values ('ATTRIBUTE', 'p', 'class')");
        query.exec("Insert into InvalidXML (type, element, attribute) values ('ATTRIBUTE', 'p', 'id')");
        query.exec("Insert into InvalidXML (type, element, attribute) values ('ATTRIBUTE', 'p', 'span')");
        query.exec("Insert into InvalidXML (type, element, attribute) values ('ATTRIBUTE', 'span', 'accesskey')");
        query.exec("Insert into InvalidXML (type, element, attribute) values ('ATTRIBUTE', 'span', 'action')");
        query.exec("Insert into InvalidXML (type, element, attribute) values ('ATTRIBUTE', 'span', 'alt')");
        query.exec("Insert into InvalidXML (type, element, attribute) values ('ATTRIBUTE', 'span', 'bgcolor')");
        query.exec("Insert into InvalidXML (type, element, attribute) values ('ATTRIBUTE', 'span', 'checked')");
        query.exec("Insert into InvalidXML (type, element, attribute) values ('ATTRIBUTE', 'span', 'class')");
        query.exec("Insert into InvalidXML (type, element, attribute) values ('ATTRIBUTE', 'span', 'flashvars')");
        query.exec("Insert into InvalidXML (type, element, attribute) values ('ATTRIBUTE', 'span', 'for')");
        query.exec("Insert into InvalidXML (type, element, attribute) values ('ATTRIBUTE', 'span', 'height')");
        query.exec("Insert into InvalidXML (type, element, attribute) values ('ATTRIBUTE', 'span', 'id')");
        query.exec("Insert into InvalidXML (type, element, attribute) values ('ATTRIBUTE', 'span', 'maxlength')");
        query.exec("Insert into InvalidXML (type, element, attribute) values ('ATTRIBUTE', 'span', 'method')");
        query.exec("Insert into InvalidXML (type, element, attribute) values ('ATTRIBUTE', 'span', 'name')");
        query.exec("Insert into InvalidXML (type, element, attribute) values ('ATTRIBUTE', 'span', 'onblur')");
        query.exec("Insert into InvalidXML (type, element, attribute) values ('ATTRIBUTE', 'span', 'onchange')");
        query.exec("Insert into InvalidXML (type, element, attribute) values ('ATTRIBUTE', 'span', 'aclick')");
        query.exec("Insert into InvalidXML (type, element, attribute) values ('ATTRIBUTE', 'span', 'onsubmit')");
        query.exec("Insert into InvalidXML (type, element, attribute) values ('ATTRIBUTE', 'span', 'quality')");
        query.exec("Insert into InvalidXML (type, element, attribute) values ('ATTRIBUTE', 'span', 'selected')");
        query.exec("Insert into InvalidXML (type, element, attribute) values ('ATTRIBUTE', 'span', 'src')");
        query.exec("Insert into InvalidXML (type, element, attribute) values ('ATTRIBUTE', 'span', 'target')");
        query.exec("Insert into InvalidXML (type, element, attribute) values ('ATTRIBUTE', 'span', 'type')");
        query.exec("Insert into InvalidXML (type, element, attribute) values ('ATTRIBUTE', 'span', 'value')");
        query.exec("Insert into InvalidXML (type, element, attribute) values ('ATTRIBUTE', 'span', 'width')");
        query.exec("Insert into InvalidXML (type, element, attribute) values ('ATTRIBUTE', 'span', 'wmode')");
        query.exec("Insert into InvalidXML (type, element, attribute) values ('ATTRIBUTE', 'table', 'class')");
        query.exec("Insert into InvalidXML (type, element, attribute) values ('ATTRIBUTE', 'td', 'class')");
        query.exec("Insert into InvalidXML (type, element, attribute) values ('ATTRIBUTE', 'tr', 'class')");
        query.exec("Insert into InvalidXML (type, element, attribute) values ('ATTRIBUTE', 'ul', 'class')");

	}
	// Drop the table
	public void dropTable() {
		NSqlQuery query = new NSqlQuery(db.getConnection());
		query.exec("Drop table InvalidXML");
	}
	// Add an item to the table
	public void addAttribute(String element, String attribute) {
		if (attributeExists(element,attribute))
			return;
        NSqlQuery query = new NSqlQuery(db.getConnection());
		query.prepare("Insert Into InvalidXML (type, element, attribute) Values('ATTRIBUTE', :element, :attribute)");
		query.bindValue(":element", element);
		query.bindValue(":attribute", attribute);
		if (!query.exec()) {
			logger.log(logger.MEDIUM, "Insert Attribute into invalidXML failed.");
			logger.log(logger.MEDIUM, query.lastError());
		}
	}
	// Add an item to the table
	public void addElement(String element) {
		if (elementExists(element))
			return;
        NSqlQuery query = new NSqlQuery(db.getConnection());
		query.prepare("Insert Into InvalidXML (type, element) Values('ELEMENT', :element)");
		query.bindValue(":element", element);
		if (!query.exec()) {
			logger.log(logger.MEDIUM, "Insert Element into invalidXML failed.");
			logger.log(logger.MEDIUM, query.lastError());
		}
	}
	// get invalid elements
	public List<String> getInvalidElements() {
        NSqlQuery query = new NSqlQuery(db.getConnection());
		if (!query.exec("Select element from InvalidXML where type = 'ELEMENT'")) {
			logger.log(logger.MEDIUM, "getInvalidElement from invalidXML failed.");
			logger.log(logger.MEDIUM, query.lastError());
			return null;
		}
		List<String> elements = new ArrayList<String>();
		while (query.next()) {
			elements.add(query.valueString(0));
		}
 		return elements;
	}
	
	// get invalid elements
	public List<String> getInvalidAttributeElements() {
        NSqlQuery query = new NSqlQuery(db.getConnection());
		if (!query.exec("Select distinct element from InvalidXML where type = 'ATTRIBUTE'")) {
			logger.log(logger.MEDIUM, "getInvalidElement from invalidXML failed.");
			logger.log(logger.MEDIUM, query.lastError());
			return null;
		}
		List<String> elements = new ArrayList<String>();
		while (query.next()) {
			elements.add(query.valueString(0));
		}
 		return elements;
	}
	// get invalid attributes for a given element
	public ArrayList<String> getInvalidAttributes(String element) {
        NSqlQuery query = new NSqlQuery(db.getConnection());
		query.prepare("Select attribute from InvalidXML where type = 'ATTRIBUTE' and element = :element");
		query.bindValue(":element", element);
		if (!query.exec()) {
			logger.log(logger.MEDIUM, "getInvalidElement from invalidXML failed.");
			logger.log(logger.MEDIUM, query.lastError());
			return null;
		}
		ArrayList<String> elements = new ArrayList<String>();
		while (query.next()) {
			elements.add(query.valueString(0));
		}
 		return elements;
	}

	// Determine if an element already is in the table
	public boolean elementExists(String element) {
        NSqlQuery query = new NSqlQuery(db.getConnection());
		query.prepare("Select element from InvalidXML where type='ELEMENT' and element=:element");
		query.bindValue(":element", element);
		if (!query.exec()) {
			logger.log(logger.MEDIUM, "elementExists in invalidXML failed.");
			logger.log(logger.MEDIUM, query.lastError());
		}
		if (query.next())
			return true;
		else
			return false;
	}
	
	// Determine if an element already is in the table
	public boolean attributeExists(String element, String attribute) {
        NSqlQuery query = new NSqlQuery(db.getConnection());
		query.prepare("Select element from InvalidXML where type='ATTRIBUTE' and element=:element and attribute=:attribute");
		query.bindValue(":element", element);
		query.bindValue(":attribute", attribute);
		if (!query.exec()) {
			logger.log(logger.MEDIUM, "attributeExists in invalidXML failed.");
			logger.log(logger.MEDIUM, query.lastError());
		}
		if (query.next())
			return true;
		else
			return false;
	}
}
