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

import com.evernote.edam.type.Note;
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
import com.trolltech.qt.gui.QMouseEvent;
import com.trolltech.qt.gui.QTreeWidget;
import com.trolltech.qt.gui.QTreeWidgetItem;
import com.trolltech.qt.gui.QTreeWidgetItem.ChildIndicatorPolicy;

import cx.fbn.nevernote.Global;
import cx.fbn.nevernote.filters.NotebookCounter;
import cx.fbn.nevernote.signals.NoteSignal;
import cx.fbn.nevernote.sql.DatabaseConnection;

public class NotebookTreeWidget extends QTreeWidget {
	private QAction 				deleteAction;
	private QAction 				addAction;
	private QAction 				editAction;
	private QAction					iconAction;
	private QAction					stackAction;
	private QAction					publishAction;
	private QAction					shareAction;
	public NoteSignal 				noteSignal;
	public Signal0					selectionSignal;
	private String 					selectedNotebook;
	private HashMap<String, QIcon>	icons;
	private final DatabaseConnection		db;
	private final HashMap<String, QTreeWidgetItem>	stacks;
	private boolean rightButtonClicked;
	
	public void setAddAction(QAction a) {
		addAction = a;
	}
	
	public void setPublishAction(QAction p) {
		publishAction = p;
	}
	
	public void setShareAction(QAction s) {
		shareAction = s;
	}
	
	public void setDeleteAction(QAction d) {
		deleteAction = d;
	}
	
	public void setEditAction(QAction e) {
		editAction = e;
	}
	
	public void setStackAction(QAction e) {
		stackAction = e;
	}
	
	public void setIconAction(QAction e) {
		iconAction = e;
	}
	
	public NotebookTreeWidget(DatabaseConnection db) {
		noteSignal = new NoteSignal();
		this.db = db;
//		setProperty("hideTree", true);
		List<String> labels = new ArrayList<String>();
		labels.add(tr("Notebooks"));
		labels.add("");
		setAcceptDrops(true);
		setDragEnabled(true);
		setColumnCount(2);
		header().setResizeMode(0, QHeaderView.ResizeMode.ResizeToContents);
		header().setResizeMode(1, QHeaderView.ResizeMode.Stretch);
		header().setMovable(false);
		header().setStyleSheet("QHeaderView::section {border: 0.0em;}");
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
		selectionSignal = new Signal0();
		selectedNotebook = "";
		rightButtonClicked = false;
		itemClicked.connect(this, "itemClicked()");
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
    	QIcon orangeIcon = new QIcon(iconPath+"notebook-orange.png");
		
		if (localBooks.contains(guid) && 
			(name.equalsIgnoreCase("Conflicting Changes") ||
			 name.equalsIgnoreCase("Conflicting Changes (Local)")))
				return redIcon;
		if (localBooks.contains(guid)) {
			return yellowIcon;
		}
		if (isPublished)
			return blueIcon;
		
		if (db.getNotebookTable().isLinked(guid))
			return orangeIcon;

		return greenIcon;
	}
	
	public void load(List<Notebook> books, List<String> localBooks) {
    	Notebook book;
    	NTreeWidgetItem child;
    	
    	/* First, let's find out which stacks are expanded */
    	QTreeWidgetItem root = 	invisibleRootItem();
    	List<String> expandedStacks = new ArrayList<String>();
    	for (int i=0; i<root.childCount(); i++) {
    		if (root.child(i).isExpanded())
    			expandedStacks.add(root.child(i).text(0));
    	}
    	
    	clear();
    	stacks.clear();
    	
    	if (books == null)
    		return;
    	Qt.Alignment ra = new Qt.Alignment(Qt.AlignmentFlag.AlignRight);
    	for (int i=0; i<books.size(); i++) {
			book = books.get(i);
			child = new NTreeWidgetItem();
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
    				parent = createStackIcon(stackName, ra);
    				addTopLevelItem(parent);
    				stacks.put(stackName, parent);
    			} else
    				parent = stacks.get(stackName);
    			parent.addChild(child);
    			
    		}
    	}

    	sortItems(0, SortOrder.AscendingOrder); 
    	if (Global.mimicEvernoteInterface) {
        	String iconPath = new String("classpath:cx/fbn/nevernote/icons/");
        	QIcon allIcon = db.getSystemIconTable().getIcon("All Notebooks", "ALLNOTEBOOK");
        	
        	if (allIcon == null)
        		allIcon = new QIcon(iconPath+"notebook-green.png");
        	
    		child = new NTreeWidgetItem();
    		child.setIcon(0, allIcon);
    		child.setText(0, tr("All Notebooks"));
    		child.setText(2, "");
    		child.setTextAlignment(1, ra.value());
    		insertTopLevelItem(0,child);
    	}
    	resizeColumnToContents(0);
    	resizeColumnToContents(1);
    	
    	// Finally, expand the stacks back out
    	root = invisibleRootItem();
    	for (int i=0; i<root.childCount(); i++) {
    		for (int j=0; j<expandedStacks.size(); j++) {
    			if (root.child(i).text(0).equalsIgnoreCase(expandedStacks.get(j))) {
    				expandItem(root.child(i));
    				j=expandedStacks.size();
    			}
    		}
    	}
	}


	// update the display with the current number of notes
	public void updateCounts(List<Notebook> books, List<NotebookCounter> counts) {
		QTreeWidgetItem root = invisibleRootItem();
		QTreeWidgetItem child;
		
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
		menu.addAction(stackAction);
		menu.addSeparator();
		menu.addAction(publishAction);
		menu.addAction(shareAction);
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
		if (event.source() == this) {
			event.mimeData().setData("application/x-nevernote-notebook", new QByteArray(currentItem().text(2)));
			List<QTreeWidgetItem> selected = selectedItems();
			for (int i=0; i<selected.size(); i++) {
				if (selected.get(i).text(2).equalsIgnoreCase("STACK") || 
					selected.get(i).text(2).equals("")) {
						event.ignore();
						return;
					}
			}
			event.accept();
			return;
		}
		event.ignore();
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
		if (data.hasFormat("application/x-nevernote-notebook")) {
			return false;
		}
		
		// This is really dead code.  it is the beginning of logic to create stacks by
		// dragging.
		if (data.hasFormat("application/x-nevernote-notebook")) {
			QByteArray d = data.data("application/x-nevernote-notebook");
			String current = d.toString();
			
			// If dropping to the top level, then remove the stack
			if (parent == null) {
				db.getNotebookTable().clearStack(current);
				return true;
			} 
			
			// If trying to drop under the "All notebooks" then ignore
			if (parent.text(2).equals(""))
				return false;
			
			
			// If we are NOT droping directly onto the stack icon
			// we need to find the stack widget
			String stackName;
			QTreeWidgetItem stackItem;
			List<QTreeWidgetItem> currentItems = selectedItems();
			if (!parent.text(2).equalsIgnoreCase("STACK")) {
				
				// If a parent stack exists, then use it.
				if (parent.parent() != null) {
					stackName = parent.parent().text(0);
					stackItem = parent.parent();
				} else {
					
					currentItems.add(parent);
					// If a stack doesn't exist, then we need to create one
					stackName = "New Stack";	
					// Find a new stack name that isn't in use
					for (int i=1; i<101; i++) {
						if (stacks.containsKey(stackName))
							stackName = "New Stack(" +new Integer(i).toString() + ")";
						else
							break;
					}
					db.getNotebookTable().setStack(parent.text(2), stackName);
					Qt.Alignment ra = new Qt.Alignment(Qt.AlignmentFlag.AlignRight);
					stackItem = createStackIcon(stackName, ra);
					addTopLevelItem(stackItem);
				}
			} else {
				stackName = parent.text(0);
				stackItem = parent;
			}
			
			List<QTreeWidgetItem> newItems = new ArrayList<QTreeWidgetItem>();
			for (int i=0; i<currentItems.size(); i++) {
				newItems.add(copyTreeItem(currentItems.get(i)));
				currentItems.get(i).setHidden(true);
			}
			db.getNotebookTable().setStack(current, stackName);		
			stackItem.addChildren(newItems);
			
			return true;
		}
		
		
		// If we are dropping a note onto a notebook
		if (data.hasFormat("application/x-nevernote-note")) {
			// If we are dropping onto a read-only notebook, we are done.
			if (db.getNotebookTable().isReadOnly(parent.text(2)))
					return false;
			
			QByteArray d = data.data("application/x-nevernote-note");
			String s = d.toString();
			String noteGuidArray[] = s.split(" ");
			for (String element : noteGuidArray) {
				Note n = db.getNoteTable().getNote(element.trim(), false, false, false, false, true);
				
				// We  need to be sure that...
				// 1.) We are not dropping onto the "All Notebooks" stack
				// 2.) We are not dropping onto a stack
				// 3.) We are actually dropping onto a different notebook.
				if (!parent.text(2).equalsIgnoreCase("") && 
						!parent.text(2).equalsIgnoreCase(tr("STACK")) &&
						!(n.getNotebookGuid().equalsIgnoreCase(parent.text(2))
					)) {
					noteSignal.notebookChanged.emit(element.trim(), parent.text(2));
					if (db.getNotebookTable().isLinked(parent.text(2))) {
						noteSignal.tagsChanged.emit(element.trim(), new ArrayList<String>());
					}
				}
			}
			return true;
		}
		return false;
	}
	

	private QTreeWidgetItem createStackIcon(String stackName, Qt.Alignment ra) {
		String iconPath = new String("classpath:cx/fbn/nevernote/icons/");
		QIcon stackIcon;
		stackIcon = db.getSystemIconTable().getIcon(stackName, "STACK");
		
		if (stackIcon == null)
			stackIcon = new QIcon(iconPath+"books2.png");
		QTreeWidgetItem parent = new QTreeWidgetItem();
		stacks.put(stackName, parent);
		parent.setText(0, stackName);
		parent.setIcon(0, stackIcon);
		parent.setText(2, "STACK");
		parent.setTextAlignment(1, ra.value());
		return parent;
	}

	
	
	// Copy an individual item within the tree.  I need to do this because
	// Qt doesn't call the dropMimeData on a move, just a copy.
	private QTreeWidgetItem copyTreeItem(QTreeWidgetItem source) {
		QTreeWidgetItem target = new QTreeWidgetItem(this);
		target.setText(0, source.text(0));
		target.setIcon(0, source.icon(0));
		target.setText(1, source.text(1));
		target.setText(2, source.text(2));
		Qt.Alignment ra = new Qt.Alignment(Qt.AlignmentFlag.AlignRight);
		target.setTextAlignment(1, ra.value());
		source.setHidden(true);

		return target;
	}

	
	@SuppressWarnings("unused")
	private void itemClicked() {
		List<QTreeWidgetItem> selectedItem = selectedItems();
		if (selectedItem.size() == 1) {
			if (selectedItem.get(0).text(0).equalsIgnoreCase(selectedNotebook) && 
					!Global.mimicEvernoteInterface && !rightButtonClicked) {
				selectedNotebook = "";
				clearSelection();
			} else {
				selectedNotebook = selectedItem.get(0).text(0);
			}
			
		}
		selectionSignal.emit();
	}

	
	@Override
	public void mousePressEvent(QMouseEvent e) {
		if (e.button() == Qt.MouseButton.RightButton)
			rightButtonClicked = true;
		else
			rightButtonClicked = false;
		super.mousePressEvent(e);
	}
}
