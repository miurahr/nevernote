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

import java.util.List;
import java.util.SortedMap;

import com.evernote.edam.type.Note;
import com.trolltech.qt.core.QByteArray;
import com.trolltech.qt.core.QModelIndex;
import com.trolltech.qt.core.Qt;
import com.trolltech.qt.core.Qt.SortOrder;
import com.trolltech.qt.gui.QAbstractItemView;
import com.trolltech.qt.gui.QAction;
import com.trolltech.qt.gui.QApplication;
import com.trolltech.qt.gui.QColor;
import com.trolltech.qt.gui.QContextMenuEvent;
import com.trolltech.qt.gui.QDragEnterEvent;
import com.trolltech.qt.gui.QDropEvent;
import com.trolltech.qt.gui.QFontMetrics;
import com.trolltech.qt.gui.QHeaderView;
import com.trolltech.qt.gui.QKeyEvent;
import com.trolltech.qt.gui.QKeySequence.StandardKey;
import com.trolltech.qt.gui.QMenu;
import com.trolltech.qt.gui.QTableView;

import cx.fbn.nevernote.Global;
import cx.fbn.nevernote.filters.NoteSortFilterProxyModel;
import cx.fbn.nevernote.signals.NoteSignal;
import cx.fbn.nevernote.utilities.ApplicationLogger;
import cx.fbn.nevernote.utilities.ListManager;

public class TableView extends QTableView {
	private final ListManager 				runner;
	private final ApplicationLogger 	logger;
    public NoteSortFilterProxyModel 	proxyModel;		// note sort model
    private QAction deleteAction;
    private QAction addAction;
    private QAction restoreAction;
    private QAction noteHistoryAction;
    private QAction duplicateAction;
    private QAction	mergeNotesAction;
    
    // Note title colors
    private QAction	noteTitleColorWhite;
    private QAction	noteTitleColorRed;
    private QAction	noteTitleColorBlue;
    private QAction	noteTitleColorGreen;
    private QAction	noteTitleColorYellow;
    private QAction	noteTitleColorBlack;
    private QAction	noteTitleColorGray;
    private QAction	noteTitleColorCyan;
    private QAction	noteTitleColorMagenta;


    
    public QHeaderView header;
    int fontHeight;
    public Signal1<String> rowChanged;
    public Signal0	resetViewport;
    public NoteSignal noteSignal;
	
	public TableView(ApplicationLogger l, ListManager m) {
		logger = l;
		header = horizontalHeader();
		header.setMovable(true);
		
		noteSignal = new NoteSignal();
		setAcceptDrops(true);
		setDragEnabled(true);
		setDragDropMode(QAbstractItemView.DragDropMode.DragDrop);
		setDropIndicatorShown(false);
		
		runner = m;	
		
        runner.getNoteTableModel().setHeaderData(Global.noteTableCreationPosition, Qt.Orientation.Horizontal, tr("Date Created"), Qt.ItemDataRole.DisplayRole);
        runner.getNoteTableModel().setHeaderData(Global.noteTableTagPosition, Qt.Orientation.Horizontal, tr("Tags"), Qt.ItemDataRole.DisplayRole);
        runner.getNoteTableModel().setHeaderData(Global.noteTableGuidPosition, Qt.Orientation.Horizontal, tr("Guid"), Qt.ItemDataRole.DisplayRole);
        runner.getNoteTableModel().setHeaderData(Global.noteTableNotebookPosition, Qt.Orientation.Horizontal, tr("Notebook"), Qt.ItemDataRole.DisplayRole);
        runner.getNoteTableModel().setHeaderData(Global.noteTableTitlePosition, Qt.Orientation.Horizontal, tr("Title"), Qt.ItemDataRole.DisplayRole);
        runner.getNoteTableModel().setHeaderData(Global.noteTableChangedPosition, Qt.Orientation.Horizontal, tr("Date Changed"), Qt.ItemDataRole.DisplayRole);
        runner.getNoteTableModel().setHeaderData(Global.noteTableAuthorPosition, Qt.Orientation.Horizontal, tr("Author"), Qt.ItemDataRole.DisplayRole);
        runner.getNoteTableModel().setHeaderData(Global.noteTableSourceUrlPosition, Qt.Orientation.Horizontal, tr("Source Url"), Qt.ItemDataRole.DisplayRole);
        runner.getNoteTableModel().setHeaderData(Global.noteTableSubjectDatePosition, Qt.Orientation.Horizontal, tr("Subject Date"), Qt.ItemDataRole.DisplayRole);
        runner.getNoteTableModel().setHeaderData(Global.noteTableSynchronizedPosition, Qt.Orientation.Horizontal, tr("Synchronized"), Qt.ItemDataRole.DisplayRole);
        header.sortIndicatorChanged.connect(this, "resetViewport()");
       
        proxyModel = new NoteSortFilterProxyModel(this);
        proxyModel.setSourceModel(runner.getNoteTableModel());
        setAlternatingRowColors(false);
        setModel(proxyModel);
//        setModel(runner.getNoteTableModel());
        runner.getNoteTableModel().setSortProxyModel(proxyModel);
               
        setSortingEnabled(true);
        int sortCol = proxyModel.sortColumn();
		SortOrder sortOrder = proxyModel.sortOrder();
		sortByColumn(sortCol, sortOrder);

		setSelectionBehavior(SelectionBehavior.SelectRows);
		setSelectionMode(SelectionMode.SingleSelection);
		verticalHeader().setVisible(false);
		hideColumn(Global.noteTableGuidPosition);  // Hide the guid column
		setShowGrid(false);
		setEditTriggers(QAbstractItemView.EditTrigger.NoEditTriggers);
		
		QFontMetrics f = QApplication.fontMetrics();
		fontHeight = f.height();
		rowChanged = new Signal1<String>();
		resetViewport = new Signal0();
	}
	
	// This should rescroll to the current item in the list when a column is 
	// sorted.  Somehow I can't get this to work, but this part is correct.
	@SuppressWarnings("unused")
	private void resetViewport() {
//		if (currentIndex() == null) 
//			return;
		
//		resetViewport.emit();
	}
	
		
	public void load(boolean reload) {
		proxyModel.clear();
		setSortingEnabled(false);
		QFontMetrics f = QApplication.fontMetrics();
		verticalHeader().setDefaultSectionSize(f.height());
		for (int i=0; i<runner.getNoteIndex().size(); i++) {
			if (Global.showDeleted == true && !runner.getNoteIndex().get(i).isActive())
				proxyModel.addGuid(runner.getNoteIndex().get(i).getGuid());
			if (!Global.showDeleted == true && runner.getNoteIndex().get(i).isActive())			
				proxyModel.addGuid(runner.getNoteIndex().get(i).getGuid());
		}

		if (!reload) {
			logger.log(logger.EXTREME, "TableView.load() reload starting.");
			proxyModel.filter();
			logger.log(logger.EXTREME, "TableView.load() leaving reload.");
			return;
		}
		logger.log(logger.EXTREME, "TableView.load() Filling table data from scratch");
	
		for (int i=0; i<runner.getMasterNoteIndex().size(); i++) {
			if (runner.getMasterNoteIndex().get(i) != null) {	
				insertRow(runner.getMasterNoteIndex().get(i), false, i);							
			}
		} 
		proxyModel.invalidate();
		
		int width;
		width = Global.getColumnWidth("noteTableCreationPosition");
		if (width>0) setColumnWidth(Global.noteTableCreationPosition, width);
		width = Global.getColumnWidth("noteTableChangedPosition");
		if (width>0) setColumnWidth(Global.noteTableChangedPosition, width);
		width = Global.getColumnWidth("noteTableTitlePosition");
		if (width>0) setColumnWidth(Global.noteTableTitlePosition, width);
		width = Global.getColumnWidth("noteTableTagPosition");
		if (width>0) setColumnWidth(Global.noteTableTagPosition, width);
		width = Global.getColumnWidth("noteTableGuidPosition");
		if (width>0) setColumnWidth(Global.noteTableGuidPosition, width);
		width = Global.getColumnWidth("noteTableNotebookPosition");
		if (width>0) setColumnWidth(Global.noteTableNotebookPosition, width);
		width = Global.getColumnWidth("noteTableSourceUrlPosition");
		if (width>0) setColumnWidth(Global.noteTableSourceUrlPosition, width);
		width = Global.getColumnWidth("noteTableAuthorPosition");
		if (width>0) setColumnWidth(Global.noteTableAuthorPosition, width);
		width = Global.getColumnWidth("noteTableSubjectDatePosition");
		if (width>0) setColumnWidth(Global.noteTableSubjectDatePosition, width);
		width = Global.getColumnWidth("noteTableSynchronizedPosition");
		if (width>0) setColumnWidth(Global.noteTableSynchronizedPosition, width);
		
		int from = header.visualIndex(Global.noteTableCreationPosition);
		int to = Global.getColumnPosition("noteTableCreationPosition");
		if (to>=0) header.moveSection(from, to);

		from = header.visualIndex(Global.noteTableTitlePosition);
		to = Global.getColumnPosition("noteTableTitlePosition");
		if (to>=0) header.moveSection(from, to);
		
		from = header.visualIndex(Global.noteTableTagPosition);
		to = Global.getColumnPosition("noteTableTagPosition");
		if (to>=0) header.moveSection(from, to);
		
		from = header.visualIndex(Global.noteTableNotebookPosition);
		to = Global.getColumnPosition("noteTableNotebookPosition");
		if (to>=0) header.moveSection(from, to);
		
		from = header.visualIndex(Global.noteTableChangedPosition);
		to = Global.getColumnPosition("noteTableChangedPosition");
		if (to>=0) header.moveSection(from, to);
		
		from = header.visualIndex(Global.noteTableSourceUrlPosition);
		to = Global.getColumnPosition("noteTableSourceUrlPosition");
		if (to>=0) header.moveSection(from, to);
		
		from = header.visualIndex(Global.noteTableAuthorPosition);
		to = Global.getColumnPosition("noteTableAuthorPosition");
		if (to>=0) header.moveSection(from, to);
		
		from = header.visualIndex(Global.noteTableSubjectDatePosition);
		to = Global.getColumnPosition("noteTableSubjectDatePosition");
		if (to>=0) header.moveSection(from, to);
		
		from = header.visualIndex(Global.noteTableSynchronizedPosition);
		to = Global.getColumnPosition("noteTableSynchronizedPosition");
		if (to>=0) header.moveSection(from, to);

		proxyModel.filter();
		
		setSortingEnabled(true);
		resetViewport.emit();
	}

	public void insertRow(Note tempNote, boolean newNote, int row) {
		if (newNote)
			proxyModel.addGuid(tempNote.getGuid());
		
		if (row > runner.getNoteTableModel().rowCount())
			runner.getNoteTableModel().insertRow(0);
		
		if (row < 0) {
			row  = runner.getNoteTableModel().rowCount();
			runner.getNoteTableModel().insertRow(row);
		}
		
		if (newNote) {
			QFontMetrics f = QApplication.fontMetrics();
			fontHeight = f.height();
			for (int i=0; i<runner.getNoteTableModel().rowCount(); i++)
				setRowHeight(i, fontHeight);
		}
	}
	protected boolean filterAcceptsRow(int sourceRow, QModelIndex sourceParent) {
		return true;
	}

	public void setAddAction(QAction a) {
		addAction = a;
	}
	
	public void setMergeNotesAction(QAction a) {
		mergeNotesAction = a;
	}
	
	public void setNoteHistoryAction(QAction a) {
		noteHistoryAction = a;
	}
	
	public void setDeleteAction(QAction d) {
		deleteAction = d;
	}
	 
	public void setRestoreAction(QAction r) {
		restoreAction = r;
	}
	public void setNoteDuplicateAction(QAction d) {
		duplicateAction = d;
	}
	
	@Override
	public void keyPressEvent(QKeyEvent e) {
		if (e.matches(StandardKey.MoveToStartOfDocument)) {
			if (runner.getNoteTableModel().rowCount() > 0) {
				clearSelection();
				selectRow(0);
			}
		}
		if (e.matches(StandardKey.MoveToEndOfDocument)) {
			if (runner.getNoteTableModel().rowCount() > 0) {
				clearSelection();
				selectRow(model().rowCount()-1);
			}
		}
		super.keyPressEvent(e);
	}
	
	@Override
	public void contextMenuEvent(QContextMenuEvent event) {
		QMenu menu = new QMenu(this);
		if (Global.showDeleted) {
			menu.addAction(restoreAction);
		} else {
			menu.addAction(addAction);
		}
		menu.addAction(deleteAction);
		menu.addSeparator();
		menu.addAction(duplicateAction);
		menu.addAction(noteHistoryAction);
		menu.addAction(mergeNotesAction);
		
		QMenu titleColorMenu = new QMenu();
		titleColorMenu.setTitle("Title Color");
		menu.addMenu(titleColorMenu);
		noteTitleColorWhite = new QAction(titleColorMenu);
	    noteTitleColorRed = new QAction(titleColorMenu);
	    noteTitleColorBlue = new QAction(titleColorMenu);
	    noteTitleColorGreen = new QAction(titleColorMenu);
	    noteTitleColorYellow = new QAction(titleColorMenu);
	    noteTitleColorBlack = new QAction(titleColorMenu);
	    noteTitleColorGray = new QAction(titleColorMenu);
	    noteTitleColorCyan = new QAction(titleColorMenu);
	    noteTitleColorMagenta = new QAction(titleColorMenu);
    
	    noteTitleColorWhite.setText(tr("White"));
	    noteTitleColorRed.setText(tr("Red"));
	    noteTitleColorBlue.setText(tr("Blue"));
	    noteTitleColorGreen.setText(tr("Green"));
	    noteTitleColorYellow.setText(tr("Yellow"));
	    noteTitleColorBlack.setText(tr("Black"));
	    noteTitleColorGray.setText(tr("Gray"));
	    noteTitleColorCyan.setText(tr("Cyan"));
	    noteTitleColorMagenta.setText(tr("Magenta"));
	    
	    titleColorMenu.addAction(noteTitleColorWhite);
	    titleColorMenu.addAction(noteTitleColorRed);
	    titleColorMenu.addAction(noteTitleColorBlue);
	    titleColorMenu.addAction(noteTitleColorGreen);
	    titleColorMenu.addAction(noteTitleColorYellow);
	    titleColorMenu.addAction(noteTitleColorBlack);
	    titleColorMenu.addAction(noteTitleColorGray);
	    titleColorMenu.addAction(noteTitleColorCyan);
	    titleColorMenu.addAction(noteTitleColorMagenta);
	    
	    noteTitleColorWhite.triggered.connect(this, "titleColorWhite()");
	    
	    noteTitleColorWhite.triggered.connect(this, "titleColorWhite()");
	    noteTitleColorRed.triggered.connect(this, "titleColorRed()");
	    noteTitleColorBlue.triggered.connect(this, "titleColorBlue()");
	    noteTitleColorGreen.triggered.connect(this, "titleColorGreen()");
	    noteTitleColorYellow.triggered.connect(this, "titleColorYellow()");
	    noteTitleColorBlack.triggered.connect(this, "titleColorBlack()");
	    noteTitleColorGray.triggered.connect(this, "titleColorGray()");
	    noteTitleColorCyan.triggered.connect(this, "titleColorCyan()");
	    noteTitleColorMagenta.triggered.connect(this, "titleColorMagenta()");
	    
		menu.exec(event.globalPos());
	}
	
	
    @SuppressWarnings("unused")
	private void titleColorWhite() {noteSignal.titleColorChanged.emit(QColor.white.rgb());}
    @SuppressWarnings("unused")
	private void titleColorRed() {noteSignal.titleColorChanged.emit(QColor.red.rgb());}
    @SuppressWarnings("unused")
	private void titleColorBlue() {noteSignal.titleColorChanged.emit(QColor.blue.rgb());}
    @SuppressWarnings("unused")
	private void titleColorGreen() {noteSignal.titleColorChanged.emit(QColor.green.rgb());}
    @SuppressWarnings("unused")
	private void titleColorYellow(){noteSignal.titleColorChanged.emit(QColor.yellow.rgb());}
    @SuppressWarnings("unused")
	private void titleColorBlack() {noteSignal.titleColorChanged.emit(QColor.black.rgb());}
    @SuppressWarnings("unused")
	private void titleColorGray() {noteSignal.titleColorChanged.emit(QColor.gray.rgb());}
    @SuppressWarnings("unused")
	private void titleColorCyan() {noteSignal.titleColorChanged.emit(QColor.cyan.rgb());}
    @SuppressWarnings("unused")
	private void titleColorMagenta() {noteSignal.titleColorChanged.emit(QColor.magenta.rgb());}
	
	

	@Override
	public void dragEnterEvent(QDragEnterEvent event) {
		StringBuffer guid = new StringBuffer(1000);
		
		showColumn(Global.noteTableGuidPosition);
		List<QModelIndex> selections = selectionModel().selectedRows();
		hideColumn(Global.noteTableGuidPosition);
		
    	if (selections.size() > 0) {
    		QModelIndex index;
    		for (int i=0; i<selections.size(); i++) {
    			int row = selections.get(i).row();
    			index = proxyModel.index(row, Global.noteTableGuidPosition);
    			SortedMap<Integer, Object> ix = proxyModel.itemData(index);
        		guid.append((String)ix.values().toArray()[0]);
        		guid.append(" ");
    		}
    	}
		event.mimeData().setData("application/x-nevernote-note", new QByteArray(guid.toString()));
		event.accept();
				
	}
	
	@Override
	public void dropEvent(QDropEvent event) {
		if (event.source() == this)
			event.ignore();
	}
	
	// Return a column width
	public int getColumnWidth(int col) {
		return columnWidth(col);
	}

/*
    @Override
	public void scrollTo(final QModelIndex index, ScrollHint hint) {
        QRect area = viewport().rect();
        QRect rect = visualRect(index);

        if (rect.top() < area.top())
            verticalScrollBar().setValue(
                verticalScrollBar().value() + rect.top() - area.top());
        else if (rect.bottom() > area.bottom())
            verticalScrollBar().setValue(
                verticalScrollBar().value() + Math.min(
                    rect.bottom() - area.bottom(), rect.top() - area.top()));
        update();
    }
    
    @Override
	protected void updateGeometries() {
        verticalScrollBar().setPageStep(viewport().height());
        verticalScrollBar().setRange(0, Math.max(0, viewport().height()));
    }
    @Override
    protected int verticalOffset() {
        return verticalScrollBar().value();
    }
*/
}
