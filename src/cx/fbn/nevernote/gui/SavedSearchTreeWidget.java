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

package cx.fbn.nevernote.gui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.evernote.edam.type.SavedSearch;
import com.trolltech.qt.core.Qt.SortOrder;
import com.trolltech.qt.gui.QAbstractItemView;
import com.trolltech.qt.gui.QAction;
import com.trolltech.qt.gui.QContextMenuEvent;
import com.trolltech.qt.gui.QIcon;
import com.trolltech.qt.gui.QMenu;
import com.trolltech.qt.gui.QTreeWidget;
import com.trolltech.qt.gui.QTreeWidgetItem;

public class SavedSearchTreeWidget extends QTreeWidget {
	private QAction editAction;
	private QAction deleteAction;
	private QAction addAction;
	private QAction iconAction;
	private HashMap<String, QIcon>	icons;
	
	
	public SavedSearchTreeWidget() {
//		setAcceptDrops(true);
//		setDragEnabled(true);
		setProperty("hideTree", true);
		header().setStyleSheet("QHeaderView::section {border: 0.0em;}");
		setAcceptDrops(false);
		setDragEnabled(false);
//		setDragDropMode(QAbstractItemView.DragDropMode.DragDrop);
    	setHeaderLabel(tr("Saved Searches"));
    	setSelectionMode(QAbstractItemView.SelectionMode.MultiSelection);
	}
	
	public void setEditAction(QAction e) {
		editAction = e;
	}
	public void setDeleteAction(QAction d) {
		deleteAction = d;
	}
	public void setAddAction(QAction a) {
		addAction = a;
	}
	public void setIconAction(QAction a) {
		iconAction = a;
	}
	public void setIcons(HashMap<String, QIcon> i) {
		icons = i;
	}
	
	public void load(List<SavedSearch> tempList) {
    	SavedSearch search;
    	List<NTreeWidgetItem> index = new ArrayList<NTreeWidgetItem>();
    	  	
    	//Clear out the tree & reload
    	clear();
    	String iconPath = new String("classpath:cx/fbn/nevernote/icons/");
		QIcon icon = new QIcon(iconPath+"search.png");
    	
   		for (int i=0; i<tempList.size(); i++) {
   			search = tempList.get(i);
   			NTreeWidgetItem child = new NTreeWidgetItem();
			child.setText(0, search.getName());
	    	if (icons != null && !icons.containsKey(search.getGuid())) {
		    	child.setIcon(0, icon);
		    } else {
		    	child.setIcon(0, icons.get(search.getGuid()));
		   	}
			child.setText(1, search.getGuid());
			index.add(child);
			addTopLevelItem(child);
 		} 
    	sortItems(0, SortOrder.AscendingOrder);
	}

	
	public boolean selectGuid(String guid) {
		QTreeWidgetItem root = invisibleRootItem();
		QTreeWidgetItem child;

		for (int i=0; i<root.childCount(); i++) {
			child = root.child(i);
			if (child.text(1).equals(guid)) {
				child.setSelected(true);
				return true;
			}
		}
		return false;
	}
	
	
	@Override
	public void contextMenuEvent(QContextMenuEvent event) {
		QMenu menu = new QMenu(this);
		menu.addAction(addAction);
		menu.addAction(editAction);
		menu.addAction(deleteAction);
		menu.addSeparator();
		menu.addAction(iconAction);
		menu.exec(event.globalPos());
	}
	
	
	public void selectSavedSearch(QTreeWidgetItem item) {
		QTreeWidgetItem root = invisibleRootItem();
		QTreeWidgetItem child;
		
		for (int i=0; i<root.childCount(); i++) {
			child = root.child(i); 
			if (child.text(1).equals(item.text(1))) {
				child.setSelected(true);
				return;
			}
		}
	}
}
