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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.evernote.edam.type.Notebook;
import com.trolltech.qt.core.QByteArray;
import com.trolltech.qt.core.QMimeData;
import com.trolltech.qt.core.Qt;
import com.trolltech.qt.core.Qt.SortOrder;
import com.trolltech.qt.gui.QAbstractItemView;
import com.trolltech.qt.gui.QAction;
import com.trolltech.qt.gui.QBrush;
import com.trolltech.qt.gui.QColor;
import com.trolltech.qt.gui.QContextMenuEvent;
import com.trolltech.qt.gui.QDragEnterEvent;
import com.trolltech.qt.gui.QDragMoveEvent;
import com.trolltech.qt.gui.QHeaderView;
import com.trolltech.qt.gui.QIcon;
import com.trolltech.qt.gui.QMenu;
import com.trolltech.qt.gui.QTreeWidget;
import com.trolltech.qt.gui.QTreeWidgetItem;
import com.trolltech.qt.gui.QTreeWidgetItem.ChildIndicatorPolicy;

import cx.fbn.nevernote.Global;
import cx.fbn.nevernote.filters.NotebookCounter;
import cx.fbn.nevernote.signals.NoteSignal;

public class NotebookTreeWidget extends QTreeWidget {
	private QAction 				deleteAction;
	private QAction 				addAction;
	private QAction 				editAction;
	private QAction					iconAction;
	public NoteSignal 				noteSignal;
	private HashMap<String, QIcon>	icons;
	private final HashMap<String, QTreeWidgetItem>	stacks;
//	private final QTreeWidgetItem			previousMouseOver;
//	private boolean					previousMouseOverWasSelected;
	
	public void setAddAction(QAction a) {
		addAction = a;
	}
	
	public void setDeleteAction(QAction d) {
		deleteAction = d;
	}
	
	public void setEditAction(QAction e) {
		editAction = e;
	}
	
	public void setIconAction(QAction e) {
		iconAction = e;
	}
	
	public NotebookTreeWidget() {
		noteSignal = new NoteSignal();
//		setProperty("hideTree", true);
		List<String> labels = new ArrayList<String>();
		labels.add("Notebooks");
		labels.add("");
		setAcceptDrops(true);
		setDragEnabled(true);
		setColumnCount(2);
		header().setResizeMode(0, QHeaderView.ResizeMode.ResizeToContents);
		header().setResizeMode(1, QHeaderView.ResizeMode.Stretch);
		header().setMovable(false);
		setHeaderLabels(labels);
		setDragDropMode(QAbstractItemView.DragDropMode.DragDrop);
		// If we want to mimic Evernote's notebook selection
		if (Global.mimicEvernoteInterface) {
			setSelectionMode(QAbstractItemView.SelectionMode.SingleSelection);
		} else
			setSelectionMode(QAbstractItemView.SelectionMode.ExtendedSelection);

		stacks = new HashMap<String, QTreeWidgetItem>();
//    	int width = Global.getColumnWidth("notebookTreeName");
//		if (width>0)
//			setColumnWidth(0, width);
//		previousMouseOver = new QTreeWidgetItem();
	}
	
	public void selectNotebook(QTreeWidgetItem item) {
		QTreeWidgetItem root = invisibleRootItem();
		QTreeWidgetItem child;
		
		for (int i=0; i<root.childCount(); i++) {
			child = root.child(i); 
			if (child.text(2).equals(item.text(2))) {
				child.setSelected(true);
				return;
			}
		}
		
	}
	
	public boolean selectGuid(String guid) {
		QTreeWidgetItem root = invisibleRootItem();
		QTreeWidgetItem child;

		for (int i=0; i<root.childCount(); i++) {
			child = root.child(i);
			if (child.text(2).equals(guid)) {
				child.setSelected(true);
				return true;
			}
		}
		return false;
	}
	
	public void setIcons(HashMap<String, QIcon> i) {
		icons = i;
	}
	
	public QIcon findDefaultIcon(String guid, String name, List<String> localBooks, boolean isPublished) {
    	String iconPath = new String("classpath:cx/fbn/nevernote/icons/");
    	QIcon blueIcon = new QIcon(iconPath+"notebook-blue.png");
    	QIcon greenIcon = new QIcon(iconPath+"notebook-green.png");
    	QIcon redIcon = new QIcon(iconPath+"notebook-red.png");
    	QIcon yellowIcon = new QIcon(iconPath+"notebook-yellow.png");

		if (localBooks.contains(guid)) {
			return yellowIcon;
		}
		
		if (localBooks.contains(guid) && 
			(name.equalsIgnoreCase("Conflicting Changes") ||
			 name.equalsIgnoreCase("Conflicting Changes (Local)")))
				return redIcon;
		if (isPublished)
			return blueIcon;

		return greenIcon;
	}
	
	public void load(List<Notebook> books, List<String> localBooks) {
    	Notebook book;
    	QTreeWidgetItem child;
    	clear();
    	
    	if (books == null)
    		return;
    	Qt.Alignment ra = new Qt.Alignment(Qt.AlignmentFlag.AlignRight);
    	for (int i=0; i<books.size(); i++) {
			book = books.get(i);
			child = new QTreeWidgetItem();
			child.setChildIndicatorPolicy(ChildIndicatorPolicy.DontShowIndicatorWhenChildless);
			child.setText(0, book.getName());
    		if (icons != null && !icons.containsKey(book.getGuid())) {
    			QIcon icon = findDefaultIcon(book.getGuid(), book.getName(), localBooks, book.isPublished());
    			child.setIcon(0, icon);
    		} else {
    			child.setIcon(0, icons.get(book.getGuid()));
    		}
    		child.setTextAlignment(1, ra.value());
    		child.setText(2, book.getGuid());
    		if (book.getStack() == null || book.getStack().equalsIgnoreCase(""))
    			addTopLevelItem(child); 
    		else {
    			String stackName = book.getStack();
    			QTreeWidgetItem parent;
    			if (!stacks.containsKey(stackName)) {
    				String iconPath = new String("classpath:cx/fbn/nevernote/icons/");
    		    	QIcon stackIcon = new QIcon(iconPath+"books2.png");
    				parent = new QTreeWidgetItem();
    				stacks.put(stackName, parent);
    				parent.setText(0, stackName);
    				parent.setIcon(0, stackIcon);
    				parent.setText(2, "STACK");
    				parent.setTextAlignment(1, ra.value());
    				addTopLevelItem(parent);
    			} else
    				parent = stacks.get(stackName);
    			parent.addChild(child);
    			
    		}
    	}

    	sortItems(0, SortOrder.AscendingOrder); 
    	if (Global.mimicEvernoteInterface) {
        	String iconPath = new String("classpath:cx/fbn/nevernote/icons/");
        	QIcon greenIcon = new QIcon(iconPath+"notebook-green.png");
        	
    		child = new QTreeWidgetItem();
    		child.setIcon(0, greenIcon);
    		child.setText(0, "All Notebooks");
    		child.setText(2, "");
    		child.setTextAlignment(1, ra.value());
    		insertTopLevelItem(0,child);
    	}
    	resizeColumnToContents(0);
    	resizeColumnToContents(1);
	}

	// update the display with the current number of notes
	public void updateCounts(List<Notebook> books, List<NotebookCounter> counts) {
		QTreeWidgetItem root = invisibleRootItem();
		QTreeWidgetItem child;
		HashMap<String, Integer> stackCounts = new HashMap<String, Integer>();
		
		QBrush blue = new QBrush();
		QBrush black = new QBrush();
		black.setColor(QColor.black);
		if (Global.tagBehavior().equalsIgnoreCase("ColorActive") && !Global.mimicEvernoteInterface)
			blue.setColor(QColor.blue);
		else
			blue.setColor(QColor.black);
		int total=0;
		
		
		int size = books.size();
		if (Global.mimicEvernoteInterface)
			size++;
		
		for (int i=0; i<size; i++) {
			child = root.child(i);
			if (child != null && child.childCount() > 0) {
				int count = child.childCount();
				QTreeWidgetItem parent = child;
				int localTotal = 0;
				for (int j=0; j<count; j++) {
					child = parent.child(j);
					int childCount = updateCounts(child, books, counts, blue, black);
					total = total+childCount;
					localTotal = localTotal+childCount;
				}
				parent.setText(1, new Integer(localTotal).toString());
			} else
				total = total+updateCounts(child, books, counts, blue, black);
		}
		
		for (int i=0; i<size; i++) {
			child = root.child(i); 
			if (child != null) {
				String guid = child.text(2);
				if (guid.equals("") && Global.mimicEvernoteInterface) 
					child.setText(1, new Integer(total).toString());
			}
		}
	}
	
	private int updateCounts(QTreeWidgetItem child, List<Notebook> books, List<NotebookCounter> counts, QBrush blue, QBrush black) {
		int total=0;
		if (child != null) {
			String guid = child.text(2);
			child.setText(1,"0");
			child.setForeground(0, black);
			child.setForeground(1, black);
			for (int j=0; j<counts.size(); j++) {
				if (counts.get(j).getGuid().equals(guid)) {
					child.setText(1, new Integer(counts.get(j).getCount()).toString());
					total = counts.get(j).getCount();
					if (counts.get(j).getCount() > 0) {
						child.setForeground(0, blue);
						child.setForeground(1, blue);
					}
				}
			}
		}
		return total;
	}
	
	// Return a list of the notebook guids, ordered by the current display order.
	public List<String> getNotebookGuids() {
		List<String> names = new ArrayList<String>();
		QTreeWidgetItem root = invisibleRootItem();
		QTreeWidgetItem child;
		for (int i=0; i<root.childCount(); i++) {
			child = root.child(i);
			String text = child.text(2);
			names.add(text);
		}
		return names;
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
	
	
	@Override
	public void dragEnterEvent(QDragEnterEvent event) {
		if (event.mimeData().hasFormat("application/x-nevernote-note")) {
			event.accept();
			return;
		}
	}
	
	
	 @Override
	protected void dragMoveEvent(QDragMoveEvent event) {
	       // if (event.mimeData().hasFormat("text/plain") &&
	             //event.answerRect().intersects(dropFrame.geometry()))
		 	QTreeWidgetItem treeItem = itemAt(event.pos().x(), event.pos().y());
		 	if (treeItem != null) {
/*		 		if (!previousMouseOver.text(0).equalsIgnoreCase(treeItem.text(0))) {
		 			previousMouseOver.setSelected(previousMouseOverWasSelected);
		 			previousMouseOverWasSelected = treeItem.isSelected();
		 			previousMouseOver = treeItem;
		 			blockSignals(true);
		 			treeItem.setSelected(true);
		 			blockSignals(false);
		 		}
*/		 		
		 	}
			if (event.mimeData().hasFormat("application/x-nevernote-note")) {
				if (event.answerRect().intersects(childrenRect()))
					event.acceptProposedAction();
				return;
			}
	    }

	
	@Override
	public boolean dropMimeData(QTreeWidgetItem parent, int index, QMimeData data, Qt.DropAction action) {
		if (data.hasFormat("application/x-nevernote-note")) {
			QByteArray d = data.data("application/x-nevernote-note");
			String s = d.toString();
			String noteGuidArray[] = s.split(" ");
			for (String element : noteGuidArray) {
				if (!parent.text(0).equalsIgnoreCase("All Notebooks"))
					noteSignal.notebookChanged.emit(element.trim(), parent.text(2));
			}
			return true;
		}
		return false;
	}
}
