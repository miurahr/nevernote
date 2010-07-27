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

import com.trolltech.qt.gui.QAbstractItemView;
import com.trolltech.qt.gui.QIcon;
import com.trolltech.qt.gui.QTreeWidget;
import com.trolltech.qt.gui.QTreeWidgetItem;

import cx.fbn.nevernote.Global;

public class AttributeTreeWidget extends QTreeWidget {
	
	public AttributeTreeWidget() {
    	setHeaderLabel(tr("Attributes"));
    	setSelectionMode(QAbstractItemView.SelectionMode.MultiSelection);
 
    	setHeaderLabel(tr("Attributes"));
    	setSelectionMode(QAbstractItemView.SelectionMode.SingleSelection);
    	
    	// Setup the first attribute tree
    	QTreeWidgetItem created = new QTreeWidgetItem();
    	created.setText(0,tr("Created"));
    	addTopLevelItem(created);
    	// Created Since List
    	QTreeWidgetItem parent = created;
    	QTreeWidgetItem child;
    	child = new QTreeWidgetItem();
    	child.setText(0,tr("Since"));
    	parent.addChild(child);    	
    	parent = child;
    	
    	Global.createdBeforeFilter = new DateAttributeFilterTable();
		Global.createdSinceFilter = new DateAttributeFilterTable();
		Global.changedBeforeFilter = new DateAttributeFilterTable();
		Global.changedSinceFilter = new DateAttributeFilterTable();
		Global.containsFilter = new ContainsAttributeFilterTable();
    	
		Global.createdBeforeFilter.setBefore();
		Global.createdBeforeFilter.setCreated();
		Global.createdSinceFilter.setSince();
		Global.createdSinceFilter.setCreated();
		Global.changedBeforeFilter.setBefore();
		Global.changedBeforeFilter.setUpdated();
		Global.changedSinceFilter.setSince();
		Global.changedBeforeFilter.setUpdated();
		
		String iconPath = new String("classpath:cx/fbn/nevernote/icons/");
		QIcon icon = new QIcon(iconPath+"attribute.png");
    	for (int i=0; i<Global.createdSinceFilter.size(); i++) {
    		child = new QTreeWidgetItem();
    		child.setIcon(0, icon);
    		child.setText(0,Global.createdSinceFilter.getName(i));
    		parent.addChild(child);
    	}
    	
    	
    	// Created Before List
    	parent = created;
    	child = new QTreeWidgetItem();
    	child.setText(0,tr("Before"));
    	created.addChild(child);
    	parent = child;
    	for (int i=0; i<Global.createdBeforeFilter.size(); i++) {
    		child = new QTreeWidgetItem();
    		child.setIcon(0, icon);
    		child.setText(0,Global.createdBeforeFilter.getName(i));
    		parent.addChild(child);
    	}
    	
    	
    	QTreeWidgetItem lastModified = new QTreeWidgetItem();
    	lastModified.setText(0,tr("Last Modified"));
    	addTopLevelItem(lastModified);
 
    	// Changed Since List
    	parent = lastModified;
    	child = new QTreeWidgetItem();
    	child.setText(0,tr("Since"));
    	lastModified.addChild(child);
    	parent = child;
    	for (int i=0; i<Global.changedSinceFilter.size(); i++) {
    		child = new QTreeWidgetItem();
    		child.setIcon(0, icon);
    		child.setText(0,Global.changedSinceFilter.getName(i));
    		parent.addChild(child);
    	}
    	
    	
    	parent = created;
    	child = new QTreeWidgetItem();
    	child.setText(0,tr("Before"));
    	lastModified.addChild(child);
    	parent = child;
    	for (int i=0; i<Global.changedBeforeFilter.size(); i++) {
    		child = new QTreeWidgetItem();
    		child.setIcon(0, icon);
    		child.setText(0,Global.changedBeforeFilter.getName(i));
    		parent.addChild(child);
    	}
    	
    	// Now we are into the other attributes
    	QTreeWidgetItem contains = new QTreeWidgetItem();
    	contains.setText(0,tr("Contains"));
    	addTopLevelItem(contains);
    	child = new QTreeWidgetItem();
    	child.setText(0,tr("Images"));
    	child.setIcon(0, icon);
    	contains.addChild(child);
    	
    	child = new QTreeWidgetItem();
    	child.setText(0,tr("Audio"));
    	child.setIcon(0, icon);
    	contains.addChild(child);
    	
    	child = new QTreeWidgetItem();
    	child.setText(0,tr("Ink"));
    	child.setIcon(0, icon);
    	contains.addChild(child);
    	
    	child = new QTreeWidgetItem();
    	child.setText(0,tr("Encrypted Text"));
    	child.setIcon(0, icon);
    	contains.addChild(child);
    	
    	
    	child = new QTreeWidgetItem();
    	child.setText(0,tr("To-Do Items"));
    	child.setIcon(0, icon);
    	contains.addChild(child);
    	
    	child = new QTreeWidgetItem();
    	child.setText(0,tr("Unfinished to-do items"));
    	child.setIcon(0, icon);
    	contains.addChild(child);
    	
    	child = new QTreeWidgetItem();
    	child.setText(0,tr("Finished to-do items"));
    	child.setIcon(0, icon);
    	contains.addChild(child);
    	
    	child = new QTreeWidgetItem();
    	child.setText(0,tr("Attachment"));
    	child.setIcon(0, icon);
    	contains.addChild(child);
    	
    	child = new QTreeWidgetItem();
    	child.setText(0,tr("PDF"));
    	child.setIcon(0, icon);
    	contains.addChild(child);
    	
	}
	
}
