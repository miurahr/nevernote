package cx.fbn.nevernote.gui;

import java.util.List;

import com.trolltech.qt.core.Qt.Orientation;
import com.trolltech.qt.gui.QAction;
import com.trolltech.qt.gui.QContextMenuEvent;
import com.trolltech.qt.gui.QHeaderView;
import com.trolltech.qt.gui.QMenu;
import com.trolltech.qt.gui.QWidget;

import cx.fbn.nevernote.Global;

public class TableViewHeader extends QHeaderView {
	public QMenu contextMenu;
	public QAction createdDateAction;
	public QAction changedDateAction;
	public QAction subjectDateAction;
	public QAction tagsAction;
	public QAction titleAction;
	public QAction notebookAction;
	public QAction synchronizedAction;
	public QAction authorAction;
	public QAction urlAction;
	public QAction thumbnailAction;
	public QAction guidAction;
	public QAction pinnedAction;
	

	public TableViewHeader(Orientation orientation, QWidget parent) {
		super(orientation, parent);
		
		setSortIndicatorShown(true);
		setClickable(true);
		
		contextMenu = new QMenu();

		titleAction = new QAction(this);
		titleAction.setText(tr("Title"));
		titleAction.setCheckable(true);
		contextMenu.addAction(titleAction);
		
		createdDateAction = new QAction(this);
		createdDateAction.setText(tr("Date Created"));
		createdDateAction.setCheckable(true);
		contextMenu.addAction(createdDateAction);
		
		changedDateAction = new QAction(this);
		changedDateAction.setText(tr("Date Changed"));
		changedDateAction.setCheckable(true);
		contextMenu.addAction(changedDateAction);
		
		subjectDateAction = new QAction(this);
		subjectDateAction.setText(tr("Subject Date"));
		subjectDateAction.setCheckable(true);
		contextMenu.addAction(subjectDateAction);
		
		tagsAction = new QAction(this);
		tagsAction.setText(tr("Tags"));
		tagsAction.setCheckable(true);
		contextMenu.addAction(tagsAction);
		
		notebookAction = new QAction(this);
		notebookAction.setText(tr("Notebook"));
		notebookAction.setCheckable(true);
		contextMenu.addAction(notebookAction);
		
		synchronizedAction = new QAction(this);
		synchronizedAction.setText(tr("Sync"));
		synchronizedAction.setCheckable(true);
		contextMenu.addAction(synchronizedAction);
		
		authorAction = new QAction(this);
		authorAction.setText(tr("Author"));
		authorAction.setCheckable(true);
		contextMenu.addAction(authorAction);
		
		urlAction = new QAction(this);
		urlAction.setText(tr("Source URL"));
		urlAction.setCheckable(true);
		contextMenu.addAction(urlAction);
		
		thumbnailAction = new QAction(this);
		thumbnailAction.setText(tr("Thumbnail"));
		thumbnailAction.setCheckable(true);
		contextMenu.addAction(thumbnailAction);
		
		pinnedAction = new QAction(this);
		pinnedAction.setText(tr("Pinned"));
		pinnedAction.setCheckable(true);
		contextMenu.addAction(pinnedAction);
		
		
		guidAction = new QAction(this);
		guidAction.setText(tr("Guid"));
		guidAction.setCheckable(true);
		setMouseTracking(true);
		sectionEntered.connect(this, "sectionClicked(Integer)");
		
		checkActions();
	}

	private void checkActions() {
		titleAction.setChecked(Global.isColumnVisible("title"));
		createdDateAction.setChecked(Global.isColumnVisible("dateCreated"));
		changedDateAction.setChecked(Global.isColumnVisible("dateChanged"));
		subjectDateAction.setChecked(Global.isColumnVisible("dateSubject"));
		tagsAction.setChecked(Global.isColumnVisible("tags"));
		notebookAction.setChecked(Global.isColumnVisible("notebook"));
		synchronizedAction.setChecked(Global.isColumnVisible("synchronized"));
		authorAction.setChecked(Global.isColumnVisible("author"));
		urlAction.setChecked(Global.isColumnVisible("sourceUrl"));
		thumbnailAction.setChecked(Global.isColumnVisible("thumbnail"));
		pinnedAction.setChecked(Global.isColumnVisible("pinned"));
		guidAction.setChecked(Global.isColumnVisible("guid"));
	}
	
	public void sectionClicked(Integer position) {
		if (position == Global.noteTableThumbnailPosition)
			setClickable(false);
		else
			setClickable(true);
	}
	
	@Override
	public void contextMenuEvent(QContextMenuEvent event) {
		checkActions();
		List<QAction> actions = contextMenu.actions();
		int count = 0;
		for (int i=0; i<actions.size(); i++) {
			actions.get(i).setEnabled(true);
			if (actions.get(i).isChecked()) 
				count++;
		}
		if (count <= 1) {
			for (int i=0; i<actions.size(); i++) {
				if (actions.get(i).isChecked()) 
					actions.get(i).setEnabled(false);
			}
		}
		contextMenu.exec(event.globalPos());
	}
}
