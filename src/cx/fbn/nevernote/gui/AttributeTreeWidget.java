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

package cx.fbn.nevernote.gui;

import com.trolltech.qt.core.Qt;
import com.trolltech.qt.core.Qt.ItemDataRole;

import com.trolltech.qt.gui.QAbstractItemView;
import com.trolltech.qt.gui.QIcon;
import com.trolltech.qt.gui.QTreeWidget;
import com.trolltech.qt.gui.QTreeWidgetItem;

import cx.fbn.nevernote.Global;

public class AttributeTreeWidget extends QTreeWidget {
	public enum Attributes {Created, Since, Before, LastModified};
	
	public AttributeTreeWidget() {
    	setHeaderLabel(tr("Attributes"));
    	setSelectionMode(QAbstractItemView.SelectionMode.MultiSelection);
 
    	setHeaderLabel(tr("Attributes"));
    	setSelectionMode(QAbstractItemView.SelectionMode.SingleSelection);
    	
    	// Setup the first attribute tree
    	QTreeWidgetItem created = new QTreeWidgetItem();
    	created.setText(0,tr("Created"));
	created.setData(0, Qt.ItemDataRole.UserRole, Attributes.Created);
    	addTopLevelItem(created);
    	// Created Since List
    	QTreeWidgetItem parent = created;
    	QTreeWidgetItem child;
    	child = new QTreeWidgetItem();
    	child.setText(0,tr("Since"));
	child.setData(0,Qt.ItemDataRole.UserRole, Attributes.Since);
    	parent.addChild(child);    	
    	parent = child;
   		
	// ------------------------(Since=true/Before=false, Created=true/Updated=false)
    	Global.createdBeforeFilter = new DateAttributeFilterTable(false, true);
    	Global.createdSinceFilter = new DateAttributeFilterTable(true, true);
    	Global.changedBeforeFilter = new DateAttributeFilterTable(false, false);
    	Global.changedSinceFilter = new DateAttributeFilterTable(true, false);
    	Global.containsFilter = new ContainsAttributeFilterTable();
    	
		String iconPath = new String("classpath:cx/fbn/nevernote/icons/");
		QIcon icon = new QIcon(iconPath+"attribute.png");
    	for (int i=0; i<Global.createdSinceFilter.size(); i++) {
    		child = new QTreeWidgetItem();
    		child.setIcon(0, icon);
    		child.setText(0, Global.createdSinceFilter.getLabel(i));
    		parent.addChild(child);
    	}
    	
    	
    	// Created Before List
    	parent = created;
    	child = new QTreeWidgetItem();
    	child.setText(0,tr("Before"));
	child.setData(0,Qt.ItemDataRole.UserRole, Attributes.Before);
    	created.addChild(child);
    	parent = child;
    	for (int i=0; i<Global.createdBeforeFilter.size(); i++) {
    		child = new QTreeWidgetItem();
    		child.setIcon(0, icon);
    		child.setText(0, Global.createdBeforeFilter.getLabel(i));
    		parent.addChild(child);
    	}
    	
    	
    	QTreeWidgetItem lastModified = new QTreeWidgetItem();
    	lastModified.setText(0,tr("Last Modified"));
	lastModified.setData(0,Qt.ItemDataRole.UserRole, Attributes.LastModified);
    	addTopLevelItem(lastModified);
 
    	// Changed Since List
    	parent = lastModified;
    	child = new QTreeWidgetItem();
    	child.setText(0,tr("Since"));
	child.setData(0,Qt.ItemDataRole.UserRole, Attributes.Since);
    	lastModified.addChild(child);
    	parent = child;
    	for (int i=0; i<Global.changedSinceFilter.size(); i++) {
    		child = new QTreeWidgetItem();
    		child.setIcon(0, icon);
    		child.setText(0, Global.changedSinceFilter.getLabel(i));
    		parent.addChild(child);
    	}
    	
    	
    	parent = created;
    	child = new QTreeWidgetItem();
    	child.setText(0,tr("Before"));
	child.setData(0,Qt.ItemDataRole.UserRole, Attributes.Before);
    	lastModified.addChild(child);
    	parent = child;
    	for (int i=0; i<Global.changedBeforeFilter.size(); i++) {
    		child = new QTreeWidgetItem();
    		child.setIcon(0, icon);
    		child.setText(0, Global.changedBeforeFilter.getLabel(i));
    		parent.addChild(child);
    	}
    	
    	// Now we are into the other attributes
    	QTreeWidgetItem contains = new QTreeWidgetItem();
    	contains.setText(0,tr("Contains"));
	contains.setData(0,Qt.ItemDataRole.UserRole,"Contains");
    	addTopLevelItem(contains);
    	child = new QTreeWidgetItem();
    	child.setText(0,tr("Images"));
	child.setData(0,Qt.ItemDataRole.UserRole, "Images");
    	child.setIcon(0, icon);
    	contains.addChild(child);
    	
    	child = new QTreeWidgetItem();
    	child.setText(0,tr("Audio"));
	child.setData(0,Qt.ItemDataRole.UserRole, "Audio");
    	child.setIcon(0, icon);
    	contains.addChild(child);
    	
    	child = new QTreeWidgetItem();
    	child.setText(0,tr("Ink"));
	child.setData(0,Qt.ItemDataRole.UserRole, "Ink");
    	child.setIcon(0, icon);
    	contains.addChild(child);
    	
    	child = new QTreeWidgetItem();
    	child.setText(0,tr("Encrypted Text"));
	child.setData(0,Qt.ItemDataRole.UserRole, "Encrypted Text");
    	child.setIcon(0, icon);
    	contains.addChild(child);
    	
    	
    	child = new QTreeWidgetItem();
    	child.setText(0,tr("To-Do Items"));
	child.setData(0,Qt.ItemDataRole.UserRole, "To-Do Items");
    	child.setIcon(0, icon);
    	contains.addChild(child);
    	
    	child = new QTreeWidgetItem();
    	child.setText(0,tr("Unfinished to-do items"));
	child.setData(0, Qt.ItemDataRole.UserRole, "Unfinished to-do items");
    	child.setIcon(0, icon);
    	contains.addChild(child);
    	
    	child = new QTreeWidgetItem();
    	child.setText(0,tr("Finished to-do items"));
	child.setData(0,Qt.ItemDataRole.UserRole, "Finished to-do items");
    	child.setIcon(0, icon);
    	contains.addChild(child);
    	
    	child = new QTreeWidgetItem();
    	child.setText(0,tr("Attachment"));
	child.setData(0,Qt.ItemDataRole.UserRole, "Attachment");
    	child.setIcon(0, icon);
    	contains.addChild(child);
    	
    	child = new QTreeWidgetItem();
    	child.setText(0,tr("PDF"));
	child.setData(0,Qt.ItemDataRole.UserRole, "PDF");
    	child.setIcon(0, icon);
    	contains.addChild(child);
    	
	}
	
}
