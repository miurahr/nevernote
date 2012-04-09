/*
 * This file is part of NixNote 
 * Copyright 2009,2010 Randy Baumgarte
 * Copyright 2010 Hiroshi Miura
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
    	header().setStyleSheet("QHeaderView::section {border: 0.0em;}");
    	
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
   		
		// -Since=true/Before=false, Created=true/Updated=false)
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

        for (int i=0; i<Global.containsFilter.size(); i++) {
	    	child = new QTreeWidgetItem();
    		child.setText(0,Global.containsFilter.getLabel(i));
    		child.setIcon(0, icon);
    		contains.addChild(child);
	    }
    	
	}
	
}
