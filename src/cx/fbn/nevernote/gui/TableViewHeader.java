package cx.fbn.nevernote.gui;

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
	public QAction notebookAction;
	public QAction synchronizedAction;
	public QAction authorAction;
	public QAction urlAction;
	

	public TableViewHeader(Orientation orientation, QWidget parent) {
		super(orientation, parent);
		
		contextMenu = new QMenu();

		createdDateAction = new QAction(this);
		createdDateAction.setText(tr("Date Created"));
		createdDateAction.setCheckable(true);
		createdDateAction.setChecked(Global.isColumnVisible("dateCreated"));
		contextMenu.addAction(createdDateAction);
		
		changedDateAction = new QAction(this);
		changedDateAction.setText(tr("Date Changed"));
		changedDateAction.setCheckable(true);
		changedDateAction.setChecked(Global.isColumnVisible("dateChanged"));
		contextMenu.addAction(changedDateAction);
		
		subjectDateAction = new QAction(this);
		subjectDateAction.setText(tr("Subject Date"));
		subjectDateAction.setCheckable(true);
		subjectDateAction.setChecked(Global.isColumnVisible("dateSubject"));
		contextMenu.addAction(subjectDateAction);
		
		tagsAction = new QAction(this);
		tagsAction.setText(tr("Tags"));
		tagsAction.setCheckable(true);
		tagsAction.setChecked(Global.isColumnVisible("tags"));
		contextMenu.addAction(tagsAction);
		
		notebookAction = new QAction(this);
		notebookAction.setText(tr("Notebook"));
		notebookAction.setCheckable(true);
		notebookAction.setChecked(Global.isColumnVisible("notebook"));
		contextMenu.addAction(notebookAction);
		
		synchronizedAction = new QAction(this);
		synchronizedAction.setText(tr("Synchronized"));
		synchronizedAction.setCheckable(true);
		synchronizedAction.setChecked(Global.isColumnVisible("synchronized"));
		contextMenu.addAction(synchronizedAction);
		
		authorAction = new QAction(this);
		authorAction.setText(tr("Author"));
		authorAction.setCheckable(true);
		authorAction.setChecked(Global.isColumnVisible("author"));
		contextMenu.addAction(authorAction);
		
		urlAction = new QAction(this);
		urlAction.setText(tr("Source URL"));
		urlAction.setCheckable(true);
		urlAction.setChecked(Global.isColumnVisible("sourceUrl"));
		contextMenu.addAction(urlAction);
		
	}
	
	@Override
	public void contextMenuEvent(QContextMenuEvent event) {
		contextMenu.exec(event.globalPos());
	}
}
