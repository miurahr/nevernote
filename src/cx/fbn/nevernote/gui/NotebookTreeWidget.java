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

import cx.fbn.nevernote.Global;
import cx.fbn.nevernote.filters.NotebookCounter;
import cx.fbn.nevernote.signals.NoteSignal;

public class NotebookTreeWidget extends QTreeWidget {
	private QAction 				deleteAction;
	private QAction 				addAction;
	private QAction 				editAction;
	public NoteSignal 				noteSignal;
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
	
	public NotebookTreeWidget() {
		noteSignal = new NoteSignal();
		setProperty("hideTree", true);
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
	
	
	public void load(List<Notebook> books, List<String> localBooks) {
    	Notebook book;
    	QTreeWidgetItem child;
    	clear();
    	String iconPath = new String("classpath:cx/fbn/nevernote/icons/");
    	QIcon blueIcon = new QIcon(iconPath+"notebook-blue.png");
    	QIcon greenIcon = new QIcon(iconPath+"notebook-green.png");
    	QIcon redIcon = new QIcon(iconPath+"notebook-red.png");
    	QIcon yellowIcon = new QIcon(iconPath+"notebook-yellow.png");
    	
    	if (books == null)
    		return;
    	Qt.Alignment ra = new Qt.Alignment(Qt.AlignmentFlag.AlignRight);
    	for (int i=0; i<books.size(); i++) {
    		book = books.get(i);
    		child = new QTreeWidgetItem();
    		child.setText(0, book.getName());
    		child.setIcon(0, greenIcon);
    		if (localBooks.contains(book.getGuid()))
    			child.setIcon(0, yellowIcon);
    		if (localBooks.contains(book.getGuid()) && 
    				(book.getName().equalsIgnoreCase("Conflicting Changes") ||
    				 book.getName().equalsIgnoreCase("Conflicting Changes (Local)")))
    					child.setIcon(0, redIcon);
    		if (book.isPublished())
    			child.setIcon(0, blueIcon);
    		child.setTextAlignment(1, ra.value());
    		child.setText(2, book.getGuid());
    		addTopLevelItem(child);
    	}

    	sortItems(0, SortOrder.AscendingOrder); 
    	if (Global.mimicEvernoteInterface) {
    		child = new QTreeWidgetItem();
    		child.setIcon(0, greenIcon);
    		child.setText(0, "All Notebooks");
//    		child.setText(1, "0");
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
		
		QBrush blue = new QBrush();
		QBrush black = new QBrush();
		black.setColor(QColor.black);
		if (Global.tagBehavior().equalsIgnoreCase("ColorActive") && !Global.mimicEvernoteInterface)
			blue.setColor(QColor.blue);
		else
			blue.setColor(QColor.black);
		int total=0;
		
//		for (int i=0; i<counts.size(); i++) {
//			total=total+counts.get(i).getCount();
//		}
		
		int size = books.size();
		if (Global.mimicEvernoteInterface)
			size++;
		
		for (int i=0; i<size; i++) {
			child = root.child(i); 
			if (child != null) {
				String guid = child.text(2);
				child.setText(1,"0");
				child.setForeground(0, black);
				child.setForeground(1, black);
				for (int j=0; j<counts.size(); j++) {
					if (counts.get(j).getGuid().equals(guid)) {
						child.setText(1, new Integer(counts.get(j).getCount()).toString());
						total = total+counts.get(j).getCount();
						if (counts.get(j).getCount() > 0) {
							child.setForeground(0, blue);
							child.setForeground(1, blue);
						}
					}
//					if (guid.equals("") && Global.mimicEvernoteInterface) {
//						child.setText(1, new Integer(total).toString());
//					}
				}
			}
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
