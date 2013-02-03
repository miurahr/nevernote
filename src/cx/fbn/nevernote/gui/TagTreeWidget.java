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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.evernote.edam.type.Note;
import com.evernote.edam.type.Tag;
import com.trolltech.qt.core.QByteArray;
import com.trolltech.qt.core.QMimeData;
import com.trolltech.qt.core.Qt;
import com.trolltech.qt.core.Qt.MatchFlag;
import com.trolltech.qt.core.Qt.MatchFlags;
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

import cx.fbn.nevernote.Global;
import cx.fbn.nevernote.filters.TagCounter;
import cx.fbn.nevernote.signals.NoteSignal;
import cx.fbn.nevernote.signals.TagSignal;
import cx.fbn.nevernote.sql.DatabaseConnection;

public class TagTreeWidget extends QTreeWidget {
	private QAction editAction;
	private QAction deleteAction;
	private QAction addAction;
	private QAction iconAction;
	private QAction mergeAction;
	public TagSignal tagSignal;
	public NoteSignal noteSignal;
	private boolean showAllTags;
	private final DatabaseConnection db;
	private HashMap<String, QIcon>	icons;
	public Signal0 selectionSignal;
	public String selectedTag;
	private boolean rightButtonClicked;
	private List<TagCounter> lastCount;
	
	
	public TagTreeWidget(DatabaseConnection d) {
		List<String> headers = new ArrayList<String>();
		headers.add(tr("Tags"));
		headers.add("");
		showAllTags = true;
		setAcceptDrops(true);
		setDragEnabled(true);
		setColumnCount(2);
		header().setResizeMode(0, QHeaderView.ResizeMode.ResizeToContents);
		header().setResizeMode(1, QHeaderView.ResizeMode.Stretch);
		header().setMovable(false);
		header().setStyleSheet("QHeaderView::section {border: 0.0em;}");
		db = d;
		selectionSignal = new Signal0();
		tagSignal = new TagSignal();
		noteSignal = new NoteSignal();
		setDragDropMode(QAbstractItemView.DragDropMode.DragDrop);
    	setHeaderLabels(headers);

//    	setSelectionMode(QAbstractItemView.SelectionMode.MultiSelection);
    	setSelectionMode(QAbstractItemView.SelectionMode.ExtendedSelection);
    	
    	selectedTag = "";
    	itemClicked.connect(this, "itemClicked()");
		int width = Global.getColumnWidth("tagTreeName");
		if (width>0)
			setColumnWidth(0, width);

		
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
	public void setIconAction(QAction i) {
		iconAction = i;
	}
	public void setMergeAction(QAction i) { 
		mergeAction = i; }
	
	// Insert a new tag into the tree.  This is used when we dynamically add a 
	// new tag after the full tag tree has been built.  It only adds to the
	// top level.
	public void insertTag(String name, String guid) {
    	String iconPath = new String("classpath:cx/fbn/nevernote/icons/");
		QIcon icon = new QIcon(iconPath+"tag.png");
		NTreeWidgetItem child;
		Qt.Alignment ra = new Qt.Alignment(Qt.AlignmentFlag.AlignRight);
		
		// Build new tag & add it
		child = new NTreeWidgetItem();
		child.setText(0, name);
		child.setIcon(0,icon);
		child.setText(2, guid);
		child.setTextAlignment(1, ra.value());
		addTopLevelItem(child);
		
		// Resort the list
		resizeColumnToContents(0);
    	resizeColumnToContents(1);
    	sortItems(0, SortOrder.AscendingOrder);
	}
	
	private QIcon findDefaultIcon(String guid) {
    	String iconPath = new String("classpath:cx/fbn/nevernote/icons/");
		QIcon icon = new QIcon(iconPath+"tag.png");
		QIcon linkedIcon = new QIcon(iconPath+"tag-orange.png");

		if (db.getTagTable().getNotebookGuid(guid) == null || 
				db.getTagTable().getNotebookGuid(guid).equals(""))
			return icon;
		else
			return linkedIcon;
	}
	
	List<String> findExpandedTags(QTreeWidgetItem item) {
		List<String> list = new ArrayList<String>();
		if (item.isExpanded()) 
			list.add(item.text(0));
		for (int i=0; i<item.childCount(); i++) {
			List<String> childrenList = findExpandedTags(item.child(i));
			for (int j=0; j<childrenList.size(); j++) {
				list.add(childrenList.get(j));
			}
		}
		
		return list;
	}
	
	void expandTags(QTreeWidgetItem item, List<String> expandedTags) {
		for (int i=0; i<item.childCount(); i++) {
			expandTags(item.child(i), expandedTags);
		}
		
		for (int i=0; i<expandedTags.size(); i++) {
			if (expandedTags.get(i).equalsIgnoreCase(item.text(0))) {
				expandItem(item);
				i=expandedTags.size();
			}
		}
	}
	
	public void load(List<Tag> tags) {
    	Tag tag;
    	List<NTreeWidgetItem> index = new ArrayList<NTreeWidgetItem>();
    	NTreeWidgetItem child;
    	  	   	
    	/* First, let's find out which stacks are expanded */
    	QTreeWidgetItem root = 	invisibleRootItem();
    	List<String> expandedTags = findExpandedTags(root);


    	
    	//Clear out the tree & reload
    	clear();
    	String iconPath = new String("classpath:cx/fbn/nevernote/icons/");
		QIcon icon = new QIcon(iconPath+"tag.png");
    	
		Qt.Alignment ra = new Qt.Alignment(Qt.AlignmentFlag.AlignRight);
	
		// Create a copy.  We delete them out as they are found
		List<Tag> tempList = new ArrayList<Tag>();
		for (int i=0; i<tags.size(); i++) {
			tempList.add(tags.get(i));
		}
		
    	while (tempList.size() > 0) {
    		for (int i=0; i<tempList.size(); i++) {
    			tag = tempList.get(i);
    			if (tag.getParentGuid()==null || tag.getParentGuid().equals("")) {
    				child = new NTreeWidgetItem();
    				child.setText(0, tag.getName());
   		    		if (icons != null && !icons.containsKey(tag.getGuid())) {
   		    			child.setIcon(0, findDefaultIcon(tag.getGuid()));
   		    		} else {
   		    			child.setIcon(0, icons.get(tag.getGuid()));
   		    		}

    				child.setText(2, tag.getGuid());
    				child.setTextAlignment(1, ra.value());
    				index.add(child);
    				addTopLevelItem(child);
    				tempList.remove(i);
    			} else {
    				// We need to find the parent
    				for (int j=0; j<index.size(); j++) {
    					if (index.get(j).text(2).equals(tag.getParentGuid())) {
    	    				child = new NTreeWidgetItem();
    	    				child.setText(0, tag.getName());
    	    				child.setIcon(0, icon);
    	    				child.setText(2, tag.getGuid());
    	    				child.setTextAlignment(1, ra.value());
    	   		    		if (icons != null && !icons.containsKey(tag.getGuid())) {
    	   		    			child.setIcon(0, findDefaultIcon(tag.getGuid()));
    	   		    		} else {
    	   		    			child.setIcon(0, icons.get(tag.getGuid()));
    	   		    		}
    	    				tempList.remove(i);
    	    				index.add(child);    						
    	    				index.get(j).addChild(child);
    					}
    				}
    			}
    		} 
    	}
    	resizeColumnToContents(0);
    	resizeColumnToContents(1);
    	sortItems(0, SortOrder.AscendingOrder);
    	
    	expandTags(invisibleRootItem(), expandedTags);
    	if (lastCount != null)
    		updateCounts(lastCount);
	}
	// Show (unhide) all tags
	public void showAllTags(boolean value) {
		showAllTags = value;
	}

	// update the display with the current number of notes
	public void updateCounts(List<TagCounter> counts) {
				
		lastCount = counts;
		MatchFlags flags = new MatchFlags();
		flags.set(MatchFlag.MatchWildcard);
		flags.set(MatchFlag.MatchRecursive);
//		List<QTreeWidgetItem> children = new ArrayList<QTreeWidgetItem>();
		List <QTreeWidgetItem>	children = findItems("*", flags);
		
		QBrush black = new QBrush();
		black.setColor(QColor.black);
		QBrush blue = new QBrush();
		blue.setColor(QColor.blue);
		if (!Global.tagBehavior().equalsIgnoreCase("ColorActive"))
			blue.setColor(QColor.black);
		
		for (int i=0; i<children.size(); i++) {
			children.get(i).setText(1,"0");
			children.get(i).setForeground(0, black);			
			children.get(i).setForeground(1, black);
			if (!showAllTags && (Global.tagBehavior().equalsIgnoreCase("HideInactiveCount") || Global.tagBehavior().equalsIgnoreCase("NoHideInactiveCount")))
				children.get(i).setHidden(true);
			else
				children.get(i).setHidden(false);
			if (children.get(i).isSelected())
				children.get(i).setHidden(false);
		}
		for (int i=0; i<counts.size(); i++) {
			for (int j=0; j<children.size(); j++) {
				String guid = children.get(j).text(2);
				if (counts.get(i).getGuid().equals(guid)) {
					children.get(j).setText(1, new Integer(counts.get(i).getCount()).toString());
					if (counts.get(i).getCount() > 0 || children.get(j).isSelected()) {
						children.get(j).setForeground(0, blue);			
						children.get(j).setForeground(1, blue);
						QTreeWidgetItem parent = children.get(j);
						while (parent != null) {
							parent.setForeground(0, blue);			
							parent.setForeground(1, blue);
							parent.setHidden(false);
							parent = parent.parent();
						}
					}
				}
			}
		}
	}

	
	public boolean selectGuid(String guid) {
		MatchFlags flags = new MatchFlags();
		flags.set(MatchFlag.MatchWildcard);
		flags.set(MatchFlag.MatchRecursive);
//		List<QTreeWidgetItem> children = new ArrayList<QTreeWidgetItem>();
		List <QTreeWidgetItem>	children = findItems("*", flags);

		for (int i=0; i<children.size(); i++) {
			if (children.get(i).text(2).equals(guid)) {
				children.get(i).setSelected(true);
				return true;
			}
		}
		return false;
	}
	
	 @Override
	 protected void dragMoveEvent(QDragMoveEvent event) {
		if (event.mimeData().hasFormat("application/x-nevernote-note")) {
			if (event.answerRect().intersects(childrenRect()))
				event.acceptProposedAction();
			return;
		}
	 }

	
	@Override
	public void dragEnterEvent(QDragEnterEvent event) {
		if (event.mimeData().hasFormat("application/x-nevernote-note")) {
			event.accept();
			return;
		}
		if (event.source() == this) {
			if (Global.tagBehavior().equals("HideInactiveCount")) {
				event.ignore();
				return;
			}
			event.mimeData().setData("application/x-nevernote-tag", new QByteArray(currentItem().text(2)));
			event.accept();
			return;
		}
		event.ignore();
	}

	@Override
	public boolean dropMimeData(QTreeWidgetItem parent, int index, QMimeData data, Qt.DropAction action) {
		if (data.hasFormat("application/x-nevernote-tag")) {
			QByteArray d = data.data("application/x-nevernote-tag");
			String current = d.toString();
			
			// Check we don't do a dumb thing like move a parent to a child of itself
			if (!checkParent(parent, current))
				return false;
			QTreeWidgetItem newChild;
			if (parent == null) {
//				tagSignal.changeParent.emit(current, "");
				db.getTagTable().updateTagParent(current, "");
				newChild = new QTreeWidgetItem(this);
			} else {
//				tagSignal.changeParent.emit(current, parent.text(2));
				db.getTagTable().updateTagParent(current, parent.text(2));
				newChild = new QTreeWidgetItem(parent);
			}
			copyTreeItem(currentItem(), newChild);
			currentItem().setHidden(true);
			sortItems(0, SortOrder.AscendingOrder);
			return true;
		}
		
		// If we are dropping a note
		if (data.hasFormat("application/x-nevernote-note")) {
			String notebookGuid = db.getTagTable().getNotebookGuid(parent.text(2));
			QByteArray d = data.data("application/x-nevernote-note");
			String s = d.toString();
			String noteGuidArray[] = s.split(" ");
			for (String element : noteGuidArray) {
				Note n = db.getNoteTable().getNote(element.trim(), false, false, false, false, false);
				
				// Check that...
				// 1.) Check that tag isn't already assigned to that note
				// 2.) Check that that tag is valid for that notebook or the tag isn't notebook specific
				// 3.) Check that the notebook isn't read only.
				if (!db.getNoteTable().noteTagsTable.checkNoteNoteTags(element.trim(), parent.text(2)) &&
						(notebookGuid == null || n.getNotebookGuid().equalsIgnoreCase(notebookGuid) || notebookGuid.equals("")) &&
						!db.getNotebookTable().isReadOnly(n.getNotebookGuid())) {
					db.getNoteTable().noteTagsTable.saveNoteTag(element.trim(), parent.text(2), true);
					noteSignal.tagsAdded.emit(element.trim(), parent.text(2));
				}
			}
			//tagSignal.listChanged.emit();
			
			return true;
		}
		return false;
	}
	
	@Override
	public void contextMenuEvent(QContextMenuEvent event) {
		QMenu menu = new QMenu(this);
		menu.addAction(addAction);
		menu.addAction(editAction);
		menu.addAction(deleteAction);
		menu.addAction(mergeAction);
		menu.addSeparator();
		menu.addAction(iconAction);
		menu.exec(event.globalPos());
	}
	
	public void setIcons(HashMap<String, QIcon> i) {
		icons = i;
	}
	
	// Copy an individual item within the tree.  I need to do this because
	// Qt doesn't call the dropMimeData on a move, just a copy.
	private void copyTreeItem(QTreeWidgetItem source, QTreeWidgetItem target) {
		target.setText(0, source.text(0));
		target.setIcon(0, source.icon(0));
		target.setText(1, source.text(1));
		target.setText(2, source.text(2));
		Qt.Alignment ra = new Qt.Alignment(Qt.AlignmentFlag.AlignRight);
		target.setTextAlignment(1, ra.value());
		
		for (int i=0; i<source.childCount(); i++) {
			QTreeWidgetItem newChild = new QTreeWidgetItem(target);
			copyTreeItem(source.child(i), newChild);
			source.child(i).setHidden(true);
		}
		return;
	}
	
	// Check that we don't copy a parent as a child of a current child.
	private boolean checkParent(QTreeWidgetItem parent, String child) {
		if (parent != null)
			if (parent.text(2).equals(child))
				return false;
		if (parent == null)
			return true;
		return checkParent(parent.parent(), child);
	}


	public void selectTag(QTreeWidgetItem item) {
		MatchFlags flags = new MatchFlags();
		flags.set(MatchFlag.MatchWildcard);
		flags.set(MatchFlag.MatchRecursive);
		List <QTreeWidgetItem>	children = findItems("*", flags);
		
		for (int j=0; j<children.size(); j++) {
			String guid = children.get(j).text(2);
			if (item.text(2).equals(guid)) {
				children.get(j).setSelected(true);
			}
		}
	}

	@SuppressWarnings("unused")
	private void itemClicked() {
		
		List<QTreeWidgetItem> selectedItem = selectedItems();
		if (selectedItem.size() == 1) {
			if (selectedItem.get(0).text(0).equalsIgnoreCase(selectedTag) && !rightButtonClicked) {
				selectedTag = "";
				clearSelection();
			} else {
				selectedTag = selectedItem.get(0).text(0);
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
