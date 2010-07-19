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
package cx.fbn.nevernote;
import java.awt.Desktop;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.SortedMap;
import java.util.Vector;

import org.apache.thrift.TException;

import com.evernote.edam.error.EDAMNotFoundException;
import com.evernote.edam.error.EDAMSystemException;
import com.evernote.edam.error.EDAMUserException;
import com.evernote.edam.notestore.NoteFilter;
import com.evernote.edam.notestore.NoteVersionId;
import com.evernote.edam.type.Data;
import com.evernote.edam.type.Note;
import com.evernote.edam.type.NoteAttributes;
import com.evernote.edam.type.Notebook;
import com.evernote.edam.type.QueryFormat;
import com.evernote.edam.type.Resource;
import com.evernote.edam.type.SavedSearch;
import com.evernote.edam.type.Tag;
import com.evernote.edam.type.User;
import com.trolltech.qt.QThread;
import com.trolltech.qt.core.QByteArray;
import com.trolltech.qt.core.QDataStream;
import com.trolltech.qt.core.QDateTime;
import com.trolltech.qt.core.QDir;
import com.trolltech.qt.core.QFile;
import com.trolltech.qt.core.QFileInfo;
import com.trolltech.qt.core.QFileSystemWatcher;
import com.trolltech.qt.core.QIODevice;
import com.trolltech.qt.core.QModelIndex;
import com.trolltech.qt.core.QSize;
import com.trolltech.qt.core.QTemporaryFile;
import com.trolltech.qt.core.QTextCodec;
import com.trolltech.qt.core.QThreadPool;
import com.trolltech.qt.core.QTimer;
import com.trolltech.qt.core.QUrl;
import com.trolltech.qt.core.Qt;
import com.trolltech.qt.core.QIODevice.OpenModeFlag;
import com.trolltech.qt.core.Qt.SortOrder;
import com.trolltech.qt.core.Qt.WidgetAttribute;
import com.trolltech.qt.gui.QAbstractItemView;
import com.trolltech.qt.gui.QAction;
import com.trolltech.qt.gui.QApplication;
import com.trolltech.qt.gui.QCloseEvent;
import com.trolltech.qt.gui.QColor;
import com.trolltech.qt.gui.QComboBox;
import com.trolltech.qt.gui.QCursor;
import com.trolltech.qt.gui.QDesktopServices;
import com.trolltech.qt.gui.QDialog;
import com.trolltech.qt.gui.QFileDialog;
import com.trolltech.qt.gui.QGridLayout;
import com.trolltech.qt.gui.QHBoxLayout;
import com.trolltech.qt.gui.QIcon;
import com.trolltech.qt.gui.QImage;
import com.trolltech.qt.gui.QLabel;
import com.trolltech.qt.gui.QListWidget;
import com.trolltech.qt.gui.QMainWindow;
import com.trolltech.qt.gui.QMenu;
import com.trolltech.qt.gui.QMessageBox;
import com.trolltech.qt.gui.QPixmap;
import com.trolltech.qt.gui.QPrintDialog;
import com.trolltech.qt.gui.QPrinter;
import com.trolltech.qt.gui.QProgressBar;
import com.trolltech.qt.gui.QSizePolicy;
import com.trolltech.qt.gui.QSpinBox;
import com.trolltech.qt.gui.QSplashScreen;
import com.trolltech.qt.gui.QSplitter;
import com.trolltech.qt.gui.QStatusBar;
import com.trolltech.qt.gui.QSystemTrayIcon;
import com.trolltech.qt.gui.QTableWidgetItem;
import com.trolltech.qt.gui.QTextEdit;
import com.trolltech.qt.gui.QToolBar;
import com.trolltech.qt.gui.QTreeWidgetItem;
import com.trolltech.qt.gui.QAbstractItemView.ScrollHint;
import com.trolltech.qt.gui.QComboBox.InsertPolicy;
import com.trolltech.qt.gui.QFileDialog.AcceptMode;
import com.trolltech.qt.gui.QFileDialog.FileMode;
import com.trolltech.qt.gui.QMessageBox.StandardButton;
import com.trolltech.qt.gui.QSizePolicy.Policy;
import com.trolltech.qt.webkit.QWebSettings;
import com.trolltech.qt.webkit.QWebPage.WebAction;
import com.trolltech.qt.xml.QDomAttr;
import com.trolltech.qt.xml.QDomDocument;
import com.trolltech.qt.xml.QDomElement;
import com.trolltech.qt.xml.QDomNodeList;

import cx.fbn.nevernote.config.InitializationException;
import cx.fbn.nevernote.config.StartupConfig;
import cx.fbn.nevernote.dialog.AccountDialog;
import cx.fbn.nevernote.dialog.ConfigDialog;
import cx.fbn.nevernote.dialog.DatabaseLoginDialog;
import cx.fbn.nevernote.dialog.DatabaseStatus;
import cx.fbn.nevernote.dialog.FindDialog;
import cx.fbn.nevernote.dialog.LoginDialog;
import cx.fbn.nevernote.dialog.NotebookArchive;
import cx.fbn.nevernote.dialog.NotebookEdit;
import cx.fbn.nevernote.dialog.OnlineNoteHistory;
import cx.fbn.nevernote.dialog.SavedSearchEdit;
import cx.fbn.nevernote.dialog.TagEdit;
import cx.fbn.nevernote.dialog.ThumbnailViewer;
import cx.fbn.nevernote.dialog.WatchFolder;
import cx.fbn.nevernote.filters.EnSearch;
import cx.fbn.nevernote.gui.AttributeTreeWidget;
import cx.fbn.nevernote.gui.BrowserWindow;
import cx.fbn.nevernote.gui.DateAttributeFilterTable;
import cx.fbn.nevernote.gui.MainMenuBar;
import cx.fbn.nevernote.gui.NotebookTreeWidget;
import cx.fbn.nevernote.gui.PDFPreview;
import cx.fbn.nevernote.gui.SavedSearchTreeWidget;
import cx.fbn.nevernote.gui.TableView;
import cx.fbn.nevernote.gui.TagTreeWidget;
import cx.fbn.nevernote.gui.Thumbnailer;
import cx.fbn.nevernote.gui.TrashTreeWidget;
import cx.fbn.nevernote.signals.DBRunnerSignal;
import cx.fbn.nevernote.sql.DatabaseConnection;
import cx.fbn.nevernote.sql.runners.WatchFolderRecord;
import cx.fbn.nevernote.threads.DBRunner;
import cx.fbn.nevernote.threads.IndexRunner;
import cx.fbn.nevernote.threads.SyncRunner;
import cx.fbn.nevernote.utilities.AESEncrypter;
import cx.fbn.nevernote.utilities.ApplicationLogger;
import cx.fbn.nevernote.utilities.FileImporter;
import cx.fbn.nevernote.utilities.ListManager;
import cx.fbn.nevernote.utilities.SyncTimes;
import cx.fbn.nevernote.xml.ExportData;
import cx.fbn.nevernote.xml.ImportData;
import cx.fbn.nevernote.xml.XMLInsertHilight;


public class NeverNote extends QMainWindow{
	
	QStatusBar 				statusBar;					// Application status bar
	
	DatabaseConnection		conn;
	
	MainMenuBar				menuBar;					// Main menu bar
	FindDialog				find;						// Text search in note dialog
	List<String>			emitLog;					// Messages displayed in the status bar;
	QSystemTrayIcon			trayIcon;					// little tray icon
	QMenu					trayMenu;					// System tray menu
	QAction					trayExitAction;				// Exit the application
	QAction					trayShowAction;				// toggle the show/hide action		
	QAction					trayAddNoteAction;			// Add a note from the system tray
	
    NotebookTreeWidget 		notebookTree;     			// List of notebooks
    AttributeTreeWidget		attributeTree;				// List of note attributes
    TagTreeWidget 			tagTree;					// list of user created tags
    SavedSearchTreeWidget	savedSearchTree;			// list of saved searches
    TrashTreeWidget			trashTree;					// Trashcan
    TableView	 			noteTableView;				// 	List of notes (the widget).

    public BrowserWindow	browserWindow;				// Window containing browser & labels
    QToolBar 				toolBar;					// The tool bar under the menu
//    QLineEdit 				searchField;				// The search filter bar on the toolbar
    QComboBox				searchField;				// search filter bar on the toolbar;
    boolean					searchPerformed = false;	// Search was done?
    QProgressBar			quotaBar;					// The current quota usage
    
    ApplicationLogger		logger;
    List<String>			selectedNotebookGUIDs;  	// List of notebook GUIDs
    List<String>			selectedTagGUIDs;			// List of selected tag GUIDs
    List<String>			selectedNoteGUIDs;			// List of selected notes
    String					selectedSavedSearchGUID;	// Currently selected saved searches
    
    NoteFilter				filter;						// Note filter
    String					currentNoteGuid;			// GUID of the current note 
    Note 					currentNote;				// The currently viewed note
    boolean					noteDirty;					// Has the note been changed?
    boolean 				inkNote;                    // if this is an ink note, it is read only
  
    QThread					dbThread;					// Thread to handle DB communication
    ListManager				listManager;					// DB runnable task
    
    List<QTemporaryFile>	tempFiles;					// Array of temporary files;
    
    QTimer					indexTimer;					// timer to start the index thread
    IndexRunner				indexRunner;				// thread to index notes
    QThread					indexThread;
    
    QTimer					syncTimer;					// Sync on an interval
    QTimer					syncDelayTimer;				// Sync delay to free up database
    SyncRunner				syncRunner;					// thread to do a sync.
    QThread					syncThread;
    QTimer					saveTimer;					// Timer to save note contents
    
    QTimer					authTimer;					// Refresh authentication
    QTimer					externalFileSaveTimer;		// Save files altered externally
    List<String>			externalFiles;				// External files to save later
    List<String>			importFilesKeep;			// Auto-import files to save later
    List<String>			importFilesDelete;			// Auto-import files to save later
    
    int						indexTime;					// how often to try and index
    boolean					indexRunning;				// Is indexing running?
    boolean					indexDisabled;				// Is indexing disabled?
    
    int						syncThreadsReady;			// number of sync threads that are free
    int						syncTime;					// Sync interval
    boolean					syncRunning;				// Is sync running?
    boolean					automaticSync;				// do sync automatically?
    QTreeWidgetItem			attributeTreeSelected;

    QAction				prevButton;					// Go to the previous item viewed
    QAction				nextButton;					// Go to the next item in the history
    QAction				downButton;					// Go to the next item in the list
    QAction				upButton;					// Go to the prev. item in the list;
    QAction				synchronizeButton;			// Synchronize with Evernote
    List<QIcon>			synchronizeAnimation;		// Synchronize movie
    QTimer				synchronizeAnimationTimer;	// Timer to change animation button
    int					synchronizeFrame;			// Current frame being viewed
    QAction 			printButton;				// Print Button
    QAction				tagButton;					// Tag edit button
    QAction				attributeButton;			// Attribute information button
    QAction 			emailButton;				// Email button
    QAction 			deleteButton;	 			// Delete button
    QAction				newButton;					// new Note Button;
    QSpinBox			zoomSpinner;				// Zoom zoom
    QAction				searchClearButton;			// Clear the search field
    
    QSplitter			mainLeftRightSplitter;		// main splitter for left/right side
    QSplitter 			leftSplitter1;				// first left hand splitter
    QSplitter			browserIndexSplitter;		// splitter between note index & note text
    
    QFileSystemWatcher	importKeepWatcher;			// Watch & keep auto-import
    QFileSystemWatcher	importDeleteWatcher;		// Watch & Delete auto-import
    List<String>		importedFiles;				// History of imported files (so we don't import twice)
    
    OnlineNoteHistory 	historyWindow;				// online history window 
    List<NoteVersionId> versions;					// history versions
    
    QTimer				threadMonitorTimer;			// Timer to watch threads.
    int					dbThreadDeadCount=0;		// number of consecutive dead times for the db thread
    int					syncThreadDeadCount=0;		// number of consecutive dead times for the sync thread
    int					indexThreadDeadCount=0;		// number of consecutive dead times for the index thread
    int					notebookThreadDeadCount=0;	// number of consecutive dead times for the notebook thread
    int					tagDeadCount=0;				// number of consecutive dead times for the tag thread
    int					trashDeadCount=0;			// number of consecutive dead times for the trash thread
    int 				saveThreadDeadCount=0;		// number of consecutive dead times for the save thread
    
    HashMap<String, String>		noteCache;			// Cash of note content	
    List<String>		historyGuids;				// GUIDs of previously viewed items
    int					historyPosition;			// Position within the viewed items
    boolean				fromHistory;				// Is this from the history queue?
    String 				trashNoteGuid;				// Guid to restore / set into or out of trash to save position
    Thumbnailer			preview;					// generate preview image
    ThumbnailViewer		thumbnailViewer;			// View preview thumbnail; 
    
    String iconPath = new String("classpath:cx/fbn/nevernote/icons/");
    	
	
    //***************************************************************
    //***************************************************************
    //** Constructor & main entry point
    //***************************************************************
    //***************************************************************
    // Application Constructor	
	public NeverNote()  {
				
		if (!lockApplication()) {
			System.exit(0);
		}
		thread().setPriority(Thread.MAX_PRIORITY);
		
		logger = new ApplicationLogger("nevernote.log");
		logger.log(logger.HIGH, tr("Starting Application"));
		conn = new DatabaseConnection(logger, Global.mainThreadId);
		conn.dbSetup();
		
		logger.log(logger.EXTREME, tr("Creating index connection"));	
		logger.log(logger.EXTREME, tr("Building DB thread"));
		Global.dbRunnerSignal = new DBRunnerSignal();
		if (Global.getDatabaseUrl().toUpperCase().indexOf("CIPHER=") > -1) {
			boolean goodCheck = false;
			while (!goodCheck) {
				DatabaseLoginDialog dialog = new DatabaseLoginDialog();
				dialog.exec();
				if (!dialog.okPressed())
					System.exit(0);
				Global.cipherPassword = dialog.getPassword();
				goodCheck = databaseCheck(Global.getDatabaseUrl(), Global.getDatabaseUserid(), Global.getDatabaseUserPassword(), Global.cipherPassword);
			}
		}
		Global.dbRunner = new DBRunner(Global.getDatabaseUrl(), Global.getDatabaseUserid(), Global.getDatabaseUserPassword(), Global.cipherPassword);
		logger.log(logger.EXTREME, tr("Starting DB thread"));
		dbThread = new QThread(Global.dbRunner, "Database Thread");
		dbThread.start();
		logger.log(logger.EXTREME, tr("DB Thread has started"));
		Global.dbRunnerSignal.start.emit();
		logger.log(logger.EXTREME, tr("Setting main thread DB connection"));
		logger.log(logger.EXTREME, tr("main DB thread setup complete."));
		conn.checkDatabaseVersion();
		
		// Start building the invalid XML tables
		Global.invalidElements = conn.getInvalidXMLTable().getInvalidElements();
		List<String> elements = conn.getInvalidXMLTable().getInvalidAttributeElements();
		
		for (int i=0; i<elements.size(); i++) {
			Global.invalidAttributes.put(elements.get(i), conn.getInvalidXMLTable().getInvalidAttributes(elements.get(i)));
		}
		
		logger.log(logger.EXTREME, tr("Starting GUI build"));
		Global.originalPalette = QApplication.palette();
		QApplication.setStyle(Global.getStyle());
		if (Global.useStandardPalette())
			QApplication.setPalette(QApplication.style().standardPalette());
        setWindowTitle("NeverNote");
        
        mainLeftRightSplitter = new QSplitter();
        setCentralWidget(mainLeftRightSplitter);
        leftSplitter1 = new QSplitter();
        leftSplitter1.setOrientation(Qt.Orientation.Vertical);
                
        browserIndexSplitter = new QSplitter();
        browserIndexSplitter.setOrientation(Qt.Orientation.Vertical);
        
        //* Setup threads & thread timers
        int indexRunnerCount = Global.getIndexThreads();
        indexRunnerCount = 1;
        QThreadPool.globalInstance().setMaxThreadCount(indexRunnerCount+5);	// increase max thread count

		logger.log(logger.EXTREME, tr("Building list manager"));
        listManager = new ListManager(conn, logger, Global.mainThreadId);
        
		logger.log(logger.EXTREME, tr("Building index runners & timers"));
        indexRunner = new IndexRunner("indexRunner.log");
		indexThread = new QThread(indexRunner, "Index Thread");
		indexThread.start();
		
        synchronizeAnimationTimer = new QTimer();
        synchronizeAnimationTimer.timeout.connect(this, "updateSyncButton()");
        
		indexTimer = new QTimer();
		indexTime = 1000*60*5;     // look for unindexed every 5 minutes
//		indexTime = 1000*5;
		indexTimer.start(indexTime);  // Start indexing timer
		indexTimer.timeout.connect(this, "indexTimer()");
		indexDisabled = false;
		indexRunning = false;
				
		logger.log(logger.EXTREME, tr("Setting sync thread & timers"));
		syncThreadsReady=1;
		syncRunner = new SyncRunner("syncRunner.log");
		syncTime = new SyncTimes().timeValue(Global.getSyncInterval());
		syncTimer = new QTimer();
		syncTimer.timeout.connect(this, "syncTimer()");
        syncRunner.status.message.connect(this, "setMessage(String)");
        syncRunner.syncSignal.finished.connect(this, "syncThreadComplete(Boolean)");
        syncRunner.syncSignal.errorDisconnect.connect(this, "remoteErrorDisconnect()");
        syncRunning = false;	
		if (syncTime > 0) {
			automaticSync = true;
			syncTimer.start(syncTime*60*1000);
		} else {
			automaticSync = false;
			syncTimer.stop();
		}
		syncRunner.setEvernoteUpdateCount(Global.getEvernoteUpdateCount());
		syncThread = new QThread(syncRunner, "Synchronization Thread");
		syncThread.start();
		
		
		logger.log(logger.EXTREME, tr("Starting authentication timer"));
		authTimer = new QTimer();
		authTimer.timeout.connect(this, "authTimer()");
		authTimer.start(1000*60*15);
		syncRunner.syncSignal.authRefreshComplete.connect(this, "authRefreshComplete(boolean)");
		
		logger.log(logger.EXTREME, tr("Setting save note timer"));
		saveTimer = new QTimer();
		saveTimer.timeout.connect(this, "saveNote()");
		if (Global.getAutoSaveInterval() > 0) {
			saveTimer.setInterval(1000*60*Global.getAutoSaveInterval()); 
//			saveTimer.setInterval(1000*10); // auto save every 20 seconds;
			saveTimer.start();
		}
		
//		Global.trace();
		logger.log(logger.EXTREME, tr("Starting external file monitor timer"));
		externalFileSaveTimer = new QTimer();
		externalFileSaveTimer.timeout.connect(this, "externalFileEditedSaver()");
		externalFileSaveTimer.setInterval(1000*5);   // save every 5 seconds;
		externalFiles = new ArrayList<String>();
		importFilesDelete = new ArrayList<String>();
		importFilesKeep = new ArrayList<String>();
		externalFileSaveTimer.start();
		
        notebookTree = new NotebookTreeWidget();
        attributeTree = new AttributeTreeWidget();
        tagTree = new TagTreeWidget(conn);
        savedSearchTree = new SavedSearchTreeWidget();
        trashTree = new TrashTreeWidget();
        noteTableView = new TableView(logger);
        
        QGridLayout leftGrid = new QGridLayout();
        leftSplitter1.setLayout(leftGrid);
        leftGrid.addWidget(notebookTree, 1, 1);
        leftGrid.addWidget(tagTree,2,1);
        leftGrid.addWidget(attributeTree,3,1);
        leftGrid.addWidget(savedSearchTree,4,1);
        leftGrid.addWidget(trashTree, 5, 1);
        
        // Setup the browser window
        noteCache = new HashMap<String,String>();
        browserWindow = new BrowserWindow(conn);

        browserIndexSplitter.addWidget(noteTableView);
        browserIndexSplitter.addWidget(browserWindow);
        
        mainLeftRightSplitter.addWidget(leftSplitter1);
        mainLeftRightSplitter.addWidget(browserIndexSplitter);

        searchField = new QComboBox();
        searchField.setEditable(true);
    	searchField.activatedIndex.connect(this, "searchFieldChanged()");
    	searchField.setDuplicatesEnabled(false);
    	searchField.editTextChanged.connect(this,"searchFieldTextChanged(String)");
        
    	quotaBar = new QProgressBar();
    	
    	// Setup the thumbnail viewer
    	thumbnailViewer = new ThumbnailViewer();
    	thumbnailViewer.upArrow.connect(this, "upAction()");
    	thumbnailViewer.downArrow.connect(this, "downAction()");
    	thumbnailViewer.leftArrow.connect(this, "nextViewedAction()");
    	thumbnailViewer.rightArrow.connect(this, "previousViewedAction()");

    	listManager.loadNotesIndex();
        initializeNotebookTree();
        initializeTagTree();
        initializeSavedSearchTree();
    	attributeTree.itemClicked.connect(this, "attributeTreeClicked(QTreeWidgetItem, Integer)");
    	attributeTreeSelected = null;
        initializeNoteTable();    

		selectedNoteGUIDs = new ArrayList<String>();
		statusBar = new QStatusBar();
		setStatusBar(statusBar);
		menuBar = new MainMenuBar(this);
		emitLog = new ArrayList<String>();
		
		tagTree.setDeleteAction(menuBar.tagDeleteAction);
		tagTree.setEditAction(menuBar.tagEditAction);
		tagTree.setAddAction(menuBar.tagAddAction);
		tagTree.setVisible(Global.isWindowVisible("tagTree"));
		tagTree.noteSignal.tagsAdded.connect(this, "tagsAdded(String, String)");
		menuBar.hideTags.setChecked(Global.isWindowVisible("tagTree"));
		listManager.tagSignal.listChanged.connect(this, "reloadTagTree()");
	
		notebookTree.setDeleteAction(menuBar.notebookDeleteAction);
		notebookTree.setEditAction(menuBar.notebookEditAction);
		notebookTree.setAddAction(menuBar.notebookAddAction);
		notebookTree.setVisible(Global.isWindowVisible("notebookTree"));
		notebookTree.noteSignal.notebookChanged.connect(this, "updateNoteNotebook(String, String)");
		menuBar.hideNotebooks.setChecked(Global.isWindowVisible("notebookTree"));

		savedSearchTree.setAddAction(menuBar.savedSearchAddAction);
		savedSearchTree.setEditAction(menuBar.savedSearchEditAction);
		savedSearchTree.setDeleteAction(menuBar.savedSearchDeleteAction);
		savedSearchTree.itemSelectionChanged.connect(this, "updateSavedSearchSelection()");
		savedSearchTree.setVisible(Global.isWindowVisible("savedSearchTree"));
		menuBar.hideSavedSearches.setChecked(Global.isWindowVisible("savedSearchTree"));
			
		noteTableView.setAddAction(menuBar.noteAdd);
		noteTableView.setDeleteAction(menuBar.noteDelete);
		noteTableView.setRestoreAction(menuBar.noteRestoreAction);
		noteTableView.setNoteDuplicateAction(menuBar.noteDuplicateAction);
		noteTableView.setNoteHistoryAction(menuBar.noteOnlineHistoryAction);
		noteTableView.noteSignal.titleColorChanged.connect(this, "titleColorChanged(Integer)");
		noteTableView.setMergeNotesAction(menuBar.noteMergeAction);
		noteTableView.rowChanged.connect(this, "scrollToGuid(String)");
		noteTableView.resetViewport.connect(this, "scrollToCurrentGuid()");
		noteTableView.doubleClicked.connect(this, "listDoubleClick()");
		listManager.trashSignal.countChanged.connect(trashTree, "updateCounts(Integer)");
		trashTree.load();
        trashTree.itemSelectionChanged.connect(this, "trashTreeSelection()");
		trashTree.setEmptyAction(menuBar.emptyTrashAction);
		trashTree.setVisible(Global.isWindowVisible("trashTree"));
		menuBar.hideTrash.setChecked(Global.isWindowVisible("trashTree"));
		trashTree.updateCounts(listManager.getTrashCount());

		attributeTree.setVisible(Global.isWindowVisible("attributeTree"));
		menuBar.hideAttributes.setChecked(Global.isWindowVisible("attributeTree"));

		noteTableView.setVisible(Global.isWindowVisible("noteList"));
		menuBar.hideNoteList.setChecked(Global.isWindowVisible("noteList"));
		
		if (!Global.isWindowVisible("editorButtonBar"))
			toggleEditorButtonBar();
		if (!Global.isWindowVisible("leftPanel"))
			menuBar.hideLeftSide.setChecked(true);
		
		setMenuBar(menuBar);
		setupToolBar();
		find = new FindDialog();
		find.getOkButton().clicked.connect(this, "doFindText()");
		
		// Setup the tray icon menu bar
		trayShowAction = new QAction("Show/Hide", this);
		trayExitAction = new QAction("Exit", this);
		trayAddNoteAction = new QAction("Add Note", this);
		
		trayExitAction.triggered.connect(this, "close()");
		trayAddNoteAction.triggered.connect(this, "addNote()");
		trayShowAction.triggered.connect(this, "trayToggleVisible()");
		
		trayMenu = new QMenu(this);
		trayMenu.addAction(trayAddNoteAction);
		trayMenu.addAction(trayShowAction);
		trayMenu.addAction(trayExitAction);
		
		
		trayIcon = new QSystemTrayIcon(this);
		trayIcon.setToolTip("NeverNote");
		trayIcon.setContextMenu(trayMenu);
		trayIcon.activated.connect(this, "trayActivated(com.trolltech.qt.gui.QSystemTrayIcon$ActivationReason)");

		currentNoteGuid="";
		currentNoteGuid = Global.getLastViewedNoteGuid();
    	historyGuids = new ArrayList<String>();
    	historyPosition = 0;
    	fromHistory = false;
		noteDirty = false;
		if (!currentNoteGuid.trim().equals("")) {
			currentNote = conn.getNoteTable().getNote(currentNoteGuid, true,true,false,false,true);
		}
		
		noteIndexUpdated(true);
		showColumns();
		menuBar.showEditorBar.setChecked(Global.isWindowVisible("editorButtonBar"));
		if (menuBar.showEditorBar.isChecked())
        	showEditorButtons();
		tagIndexUpdated(true);
		savedSearchIndexUpdated();
		notebookIndexUpdated();
		updateQuotaBar();
        setupSyncSignalListeners();        
        setupBrowserSignalListeners();
        setupIndexListeners();
              
        
        tagTree.tagSignal.listChanged.connect(this, "tagIndexUpdated()");
        tagTree.showAllTags(true);

		QIcon appIcon = new QIcon(iconPath+"nevernote.png");
    	setWindowIcon(appIcon);
    	trayIcon.setIcon(appIcon);
    	if (Global.showTrayIcon())
    		trayIcon.show();
    	else
    		trayIcon.hide();
    	
    	scrollToGuid(currentNoteGuid);
    	if (Global.automaticLogin()) {
    		remoteConnect();
    		if (Global.isConnected)
    			syncTimer();
    	}
    	setupFolderImports();
    	
    	loadStyleSheet();
    	restoreWindowState();
    	
    	if (Global.mimicEvernoteInterface) {
    		notebookTree.selectGuid("");
    	}
    	
    	threadMonitorTimer = new QTimer();
    	threadMonitorTimer.timeout.connect(this, "threadMonitorCheck()");
    	threadMonitorTimer.start(1000*10);  // Check for threads every 10 seconds;	   	
    	
    	historyGuids.add(currentNoteGuid);
    	historyPosition = 1;
    	
    	int sortCol = Global.getSortColumn();
		int sortOrder = Global.getSortOrder();
		noteTableView.sortByColumn(sortCol, SortOrder.resolve(sortOrder));
	}

	
	// Main entry point
	public static void main(String[] args) {
		QApplication.initialize(args);
		QPixmap pixmap = new QPixmap("classpath:cx/fbn/nevernote/icons/splash_logo.png");
		QSplashScreen splash = new QSplashScreen(pixmap);

		try {
		    initializeGlobalSettings(args);
		} catch (InitializationException e) {
		        QMessageBox.critical(null, "Startup error", "Aborting: " + e.getMessage());
		        return;
		}

		boolean showSplash = Global.isWindowVisible("SplashScreen");
		if (showSplash) 
			splash.show();
		NeverNote application = new NeverNote();
		application.setAttribute(WidgetAttribute.WA_DeleteOnClose, true);
		if (Global.wasWindowMaximized())
			application.showMaximized();
		else
			application.show();
		if (showSplash)
			splash.finish(application);
		QApplication.exec();
		System.out.println("Goodbye.");
		QApplication.exit();
	}

	private static void initializeGlobalSettings(String[] args) throws InitializationException {
                StartupConfig startupConfig = new StartupConfig();

                for (String arg : args) {
                        String lower = arg.toLowerCase();
                        if (lower.startsWith("--name="))
                                startupConfig.setName(arg.substring(arg.indexOf('=') + 1));
                        if (lower.startsWith("--home="))
                                startupConfig.setHomeDirPath(arg.substring(arg.indexOf('=') + 1));
                        if (lower.startsWith("--disable-viewing"))
                                startupConfig.setDisableViewing(true);
                }

                Global.setup(startupConfig);
        }

    // Exit point
	@Override
	public void closeEvent(QCloseEvent event) {	
		logger.log(logger.HIGH, "Entering NeverNote.closeEvent");
		waitCursor(true);
		
		if (currentNote!= null & browserWindow!=null) {
			if (!currentNote.getTitle().equals(browserWindow.getTitle()))
				conn.getNoteTable().updateNoteTitle(currentNote.getGuid(), browserWindow.getTitle());
		}
		saveNote();
		setMessage("Beginning shutdown.");

		externalFileEditedSaver();
		if (Global.isConnected && Global.synchronizeOnClose()) {
			setMessage("Performing synchronization before closing.");
			syncRunner.addWork("SYNC");
		}
		setMessage("Closing Program.");
		threadMonitorTimer.stop();

		syncRunner.addWork("STOP");
		indexRunner.addWork("STOP");
		saveNote();
		listManager.stop();
		saveWindowState();

		if (tempFiles != null)
			tempFiles.clear();

		browserWindow.noteSignal.tagsChanged.disconnect();
		browserWindow.noteSignal.titleChanged.disconnect();
		browserWindow.noteSignal.noteChanged.disconnect();
		browserWindow.noteSignal.notebookChanged.disconnect();
		browserWindow.noteSignal.createdDateChanged.disconnect();
		browserWindow.noteSignal.alteredDateChanged.disconnect();
		syncRunner.searchSignal.listChanged.disconnect();
		syncRunner.tagSignal.listChanged.disconnect();
        syncRunner.notebookSignal.listChanged.disconnect();
        syncRunner.noteIndexSignal.listChanged.disconnect();


		int position = noteTableView.header.visualIndex(Global.noteTableCreationPosition);
		Global.setColumnPosition("noteTableCreationPosition", position);
		position = noteTableView.header.visualIndex(Global.noteTableTagPosition);
		Global.setColumnPosition("noteTableTagPosition", position);
		position = noteTableView.header.visualIndex(Global.noteTableNotebookPosition);
		Global.setColumnPosition("noteTableNotebookPosition", position);
		position = noteTableView.header.visualIndex(Global.noteTableChangedPosition);
		Global.setColumnPosition("noteTableChangedPosition", position);
		position = noteTableView.header.visualIndex(Global.noteTableAuthorPosition);
		Global.setColumnPosition("noteTableAuthorPosition", position);
		position = noteTableView.header.visualIndex(Global.noteTableSourceUrlPosition);
		Global.setColumnPosition("noteTableSourceUrlPosition", position);
		position = noteTableView.header.visualIndex(Global.noteTableSubjectDatePosition);
		Global.setColumnPosition("noteTableSubjectDatePosition", position);
		position = noteTableView.header.visualIndex(Global.noteTableTitlePosition);
		Global.setColumnPosition("noteTableTitlePosition", position);
		position = noteTableView.header.visualIndex(Global.noteTableSynchronizedPosition);
		Global.setColumnPosition("noteTableSynchronizedPosition", position);
		
		saveNoteIndexWidth();
		
		int width = notebookTree.columnWidth(0);
		Global.setColumnWidth("notebookTreeName", width);
		width = tagTree.columnWidth(0);
		Global.setColumnWidth("tagTreeName", width);
		
		Global.saveWindowMaximized(isMaximized());
		Global.saveCurrentNoteGuid(currentNoteGuid);
			
		int sortCol = noteTableView.proxyModel.sortColumn();
		int sortOrder = noteTableView.proxyModel.sortOrder().value();
		Global.setSortColumn(sortCol);
		Global.setSortOrder(sortOrder);
		
		hide();
		trayIcon.hide();
		Global.keepRunning = false;
		try {
			logger.log(logger.MEDIUM, "Waiting for indexThread to stop");
			indexRunner.thread().join(50);
			logger.log(logger.MEDIUM, "Index thread has stopped");
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		if (!syncRunner.isIdle()) {
			try {
				logger.log(logger.MEDIUM, "Waiting for syncThread to stop");
				syncThread.join();
				logger.log(logger.MEDIUM, "Sync thread has stopped");
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
		}

		logger.log(logger.EXTREME, "Shutting down database");
		conn.dbShutdown();
		logger.log(logger.EXTREME, "Waiting for DB thread to terminate");
		try {
			dbThread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		logger.log(logger.EXTREME, "DB Thread has terminated");
		unlockApplication();
		logger.log(logger.HIGH, "Leaving NeverNote.closeEvent");
	}

	public void setMessage(String s) {
		logger.log(logger.HIGH, "Entering NeverNote.setMessage");
		logger.log(logger.HIGH, "Message: " +s);
		statusBar.showMessage(s);
		emitLog.add(s);
		logger.log(logger.HIGH, "Leaving NeverNote.setMessage");
	}
		
	private void waitCursor(boolean wait) {
		if (wait)
			QApplication.setOverrideCursor(new QCursor(Qt.CursorShape.WaitCursor));
		else
			QApplication.restoreOverrideCursor();
	}
	
	private void setupIndexListeners() {
		indexRunner.noteSignal.noteIndexed.connect(this, "indexThreadComplete(String)");
		indexRunner.resourceSignal.resourceIndexed.connect(this, "indexThreadComplete(String)");
//			indexRunner.threadSignal.indexNeeded.connect(listManager, "setIndexNeeded(String, String, Boolean)");
	}
	private void setupSyncSignalListeners() {
		syncRunner.tagSignal.listChanged.connect(this, "tagIndexUpdated()");
        syncRunner.searchSignal.listChanged.connect(this, "savedSearchIndexUpdated()");
        syncRunner.notebookSignal.listChanged.connect(this, "notebookIndexUpdated()");
        syncRunner.noteIndexSignal.listChanged.connect(this, "noteIndexUpdated(boolean)");
        syncRunner.noteSignal.quotaChanged.connect(this, "updateQuotaBar()");
        
//		syncRunner.syncSignal.setSequenceDate.connect(this,"setSequenceDate(long)");
		syncRunner.syncSignal.saveUploadAmount.connect(this,"saveUploadAmount(long)");
//		syncRunner.syncSignal.setUpdateSequenceNumber.connect(this,"setUpdateSequenceNumber(int)");
		syncRunner.syncSignal.saveUserInformation.connect(this,"saveUserInformation(User)");
		syncRunner.syncSignal.saveEvernoteUpdateCount.connect(this,"saveEvernoteUpdateCount(int)");
		
		syncRunner.noteSignal.guidChanged.connect(this, "noteGuidChanged(String, String)");
		syncRunner.noteSignal.noteChanged.connect(this, "invalidateNoteCache(String, String)");
		syncRunner.resourceSignal.resourceGuidChanged.connect(this, "noteResourceGuidChanged(String,String,String)");
		
		syncRunner.syncSignal.refreshLists.connect(this, "refreshLists()");
	}
	
	private void setupBrowserSignalListeners() {
		
		browserWindow.fileWatcher.fileChanged.connect(this, "externalFileEdited(String)");
		browserWindow.noteSignal.tagsChanged.connect(this, "updateNoteTags(String, List)");
	    browserWindow.noteSignal.tagsChanged.connect(this, "updateListTags(String, List)");
		browserWindow.noteSignal.noteChanged.connect(this, "invalidateNoteCache(String, String)");
	    browserWindow.noteSignal.noteChanged.connect(this, "setNoteDirty()");
	    browserWindow.noteSignal.titleChanged.connect(listManager, "updateNoteTitle(String, String)");
	    browserWindow.noteSignal.titleChanged.connect(this, "updateListTitle(String, String)");
	    browserWindow.noteSignal.notebookChanged.connect(this, "updateNoteNotebook(String, String)");
	    browserWindow.noteSignal.createdDateChanged.connect(listManager, "updateNoteCreatedDate(String, QDateTime)");
	    browserWindow.noteSignal.createdDateChanged.connect(this, "updateListDateCreated(String, QDateTime)");
	    browserWindow.noteSignal.alteredDateChanged.connect(listManager, "updateNoteAlteredDate(String, QDateTime)");
	    browserWindow.noteSignal.alteredDateChanged.connect(this, "updateListDateChanged(String, QDateTime)");
	    browserWindow.noteSignal.subjectDateChanged.connect(listManager, "updateNoteSubjectDate(String, QDateTime)");
	    browserWindow.noteSignal.subjectDateChanged.connect(this, "updateListDateSubject(String, QDateTime)");
	    browserWindow.noteSignal.authorChanged.connect(listManager, "updateNoteAuthor(String, String)");
	    browserWindow.noteSignal.authorChanged.connect(this, "updateListAuthor(String, String)");
	    browserWindow.noteSignal.sourceUrlChanged.connect(listManager, "updateNoteSourceUrl(String, String)");
	    browserWindow.noteSignal.sourceUrlChanged.connect(this, "updateListSourceUrl(String, String)");
	    browserWindow.focusLost.connect(this, "saveNote()");
	    browserWindow.resourceSignal.contentChanged.connect(this, "externalFileEdited(String)");
//	    browserWindow.resourceSignal.externalFileEdit.connect(this, "saveResourceLater(String, String)");
	}
	private boolean lockApplication() {
				
	        // NFC TODO: who creates this - H2? should it be parameterized with databaseName like in RDatabaseConnection? 
		String fileName = Global.getFileManager().getDbDirPath("NeverNote.lock.db");
//		QFile.remove(fileName);
		if (QFile.exists(fileName)) {
			QMessageBox.question(this, "Lock File Detected",
					"While starting I've found a database lock file.\n" +
					"to prevent multiple instances from accessing the database \n"+
					"at the same time.  Please stop any other program, or (if you\n" +
					"are sure nothing else is using the database) remove the file\n" +
					fileName +".");
			return false;
			
		}
		return true;
/*		String fileName = Global.currentDir +"nevernote.lock";

		
		if (QFile.exists(fileName)) {
			if (QMessageBox.question(this, "Confirmation",
				"While starting I've found a lock file.  This file is used to prevent multiple "+
				"instances of this program running at once.  If NeverNote has crashed this " +
				"is just a file that wasn't cleaned up and you can safely, "+
				"continue, but if there is another instance of this running you are " +
				"running the risk of creating problems.\n\n" +
				"Are you sure you want to continue?",
				QMessageBox.StandardButton.Yes, 
				QMessageBox.StandardButton.No)==StandardButton.No.value()) {
					return false;
				}
		}
		
		QFile file = new QFile(fileName);
		file.open(OpenModeFlag.WriteOnly);
		file.write(new QByteArray("This file is used to prevent multiple instances " +
				"of NeverNote running more than once.  " +
				"It should be deleted when NeverNote ends"));
		file.close();
		return true;
*/
	}
	private void unlockApplication() {
	        // NFC TODO: should this be removed? Looks like H2 now handles the locking, which it will clean up itself. See #lockApplication.
		String fileName = Global.getFileManager().getHomeDirPath("nevernote.lock");
		if (QFile.exists(fileName)) {
			QFile.remove(fileName);
		}
	}
	

	//***************************************************************
	//***************************************************************
	//* Settings and look & feel
	//***************************************************************
	//***************************************************************
	@SuppressWarnings("unused")
	private void settings() {
		logger.log(logger.HIGH, "Entering NeverNote.settings");
        ConfigDialog settings = new ConfigDialog(this);
        String dateFormat = Global.getDateFormat();
        String timeFormat = Global.getTimeFormat();
        
        settings.exec();
        if (Global.showTrayIcon())
        	trayIcon.show();
        else
        	trayIcon.hide();
        showColumns();
        if (menuBar.showEditorBar.isChecked())
        	showEditorButtons();
        
        // Reset the save timer
        if (Global.getAutoSaveInterval() > 0)
			saveTimer.setInterval(1000*60*Global.getAutoSaveInterval());
        else
        	saveTimer.stop();
        
        // This is a hack to force a reload of the index in case the date or time changed.
//        if (!dateFormat.equals(Global.getDateFormat()) ||
//        		!timeFormat.equals(Global.getTimeFormat())) {
        	noteCache.clear();
        	noteIndexUpdated(true);
//        }
        
        logger.log(logger.HIGH, "Leaving NeverNote.settings");
	}
	// Restore things to the way they were
	private void restoreWindowState() {
		// We need to name things or this doesn't work.
		setObjectName("NeverNote");
		mainLeftRightSplitter.setObjectName("mainLeftRightSplitter");
		browserIndexSplitter.setObjectName("browserIndexSplitter");
		leftSplitter1.setObjectName("leftSplitter1");	
		
		// Restore the actual positions.
		restoreGeometry(Global.restoreGeometry(objectName()));
        mainLeftRightSplitter.restoreState(Global.restoreState(mainLeftRightSplitter.objectName()));
        browserIndexSplitter.restoreState(Global.restoreState(browserIndexSplitter.objectName()));
        leftSplitter1.restoreState(Global.restoreState(leftSplitter1.objectName()));
       
	}
	// Save window positions for the next start
	private void saveWindowState() {
		Global.saveGeometry(objectName(), saveGeometry());
		Global.saveState(mainLeftRightSplitter.objectName(), mainLeftRightSplitter.saveState());
		Global.saveState(browserIndexSplitter.objectName(), browserIndexSplitter.saveState());
		Global.saveState(leftSplitter1.objectName(), leftSplitter1.saveState());
	}    
	// Load the style sheet
	private void loadStyleSheet() {
		String fileName = Global.currentDir +"qss"+System.getProperty("file.separator")+ "default.qss";
		QFile file = new QFile(fileName);
		file.open(OpenModeFlag.ReadOnly);
		String styleSheet = file.readAll().toString();
		file.close();
		setStyleSheet(styleSheet);
	}
	// Save column widths for the next time
	private void saveNoteIndexWidth() {
		int width;
        width = noteTableView.getColumnWidth(Global.noteTableCreationPosition);
        Global.setColumnWidth("noteTableCreationPosition", width);
		width = noteTableView.getColumnWidth(Global.noteTableChangedPosition);
		Global.setColumnWidth("noteTableChangedPosition", width);
		width = noteTableView.getColumnWidth(Global.noteTableGuidPosition);
		Global.setColumnWidth("noteTableGuidPosition", width);
		width = noteTableView.getColumnWidth(Global.noteTableNotebookPosition);
		Global.setColumnWidth("noteTableNotebookPosition", width);
		width = noteTableView.getColumnWidth(Global.noteTableTagPosition);
		Global.setColumnWidth("noteTableTagPosition", width);
		width = noteTableView.getColumnWidth(Global.noteTableTitlePosition);
		Global.setColumnWidth("noteTableTitlePosition", width);
		width = noteTableView.getColumnWidth(Global.noteTableSourceUrlPosition);
		Global.setColumnWidth("noteTableSourceUrlPosition", width);
		width = noteTableView.getColumnWidth(Global.noteTableAuthorPosition);
		Global.setColumnWidth("noteTableAuthorPosition", width);
		width = noteTableView.getColumnWidth(Global.noteTableSubjectDatePosition);
		Global.setColumnWidth("noteTableSubjectDatePosition", width);
		width = noteTableView.getColumnWidth(Global.noteTableSynchronizedPosition);
		Global.setColumnWidth("noteTableSynchronizedPosition", width);
	}
	
	
    //***************************************************************
    //***************************************************************
    //** These functions deal with Notebook menu items
    //***************************************************************
    //***************************************************************
    // Setup the tree containing the user's notebooks.
    private void initializeNotebookTree() {       
    	logger.log(logger.HIGH, "Entering NeverNote.initializeNotebookTree");
    	notebookTree.itemSelectionChanged.connect(this, "notebookTreeSelection()");
    	listManager.notebookSignal.refreshNotebookTreeCounts.connect(notebookTree, "updateCounts(List, List)");
 //   	notebookTree.resize(Global.getSize("notebookTree"));
    	logger.log(logger.HIGH, "Leaving NeverNote.initializeNotebookTree");
    }   
    // Listener when a notebook is selected
    @SuppressWarnings("unused")
	private void notebookTreeSelection() {
		logger.log(logger.HIGH, "Entering NeverNote.notebookTreeSelection");

		clearTrashFilter();
		clearAttributeFilter();
		clearSavedSearchFilter();
		if (Global.mimicEvernoteInterface) {
			clearTagFilter();
			searchField.clear();
		}
		
		menuBar.noteRestoreAction.setVisible(false);		
    	menuBar.notebookEditAction.setEnabled(true);
    	menuBar.notebookDeleteAction.setEnabled(true);
    	List<QTreeWidgetItem> selections = notebookTree.selectedItems();
    	QTreeWidgetItem currentSelection;
    	selectedNotebookGUIDs.clear();
    	if (!Global.mimicEvernoteInterface) {
    		for (int i=0; i<selections.size(); i++) {
    			currentSelection = selections.get(i);
    			selectedNotebookGUIDs.add(currentSelection.text(2));
    		}
    	
    	   	
    		// There is the potential for no notebooks to be selected if this 
    		// happens then we make it look like all notebooks were selecetd.
    		// If that happens, just select the "all notebooks"
    		selections = notebookTree.selectedItems();
    		if (selections.size()==0) {
    			selectedNotebookGUIDs.clear();
    			menuBar.notebookEditAction.setEnabled(false);
    			menuBar.notebookDeleteAction.setEnabled(false);
    		}
    	} else {
    		String guid = "";
    		if (selections.size() > 0)
    			guid = (selections.get(0).text(2));
    		if (!guid.equals(""))
    			selectedNotebookGUIDs.add(guid);
    	}
    	listManager.setSelectedNotebooks(selectedNotebookGUIDs);
    	listManager.loadNotesIndex();
    	noteIndexUpdated(false);
		logger.log(logger.HIGH, "Leaving NeverNote.notebookTreeSelection");

    }
    private void clearNotebookFilter() {
    	notebookTree.blockSignals(true);
    	notebookTree.clearSelection();
		menuBar.noteRestoreAction.setVisible(false);
    	menuBar.notebookEditAction.setEnabled(false);
    	menuBar.notebookDeleteAction.setEnabled(false);
    	selectedNotebookGUIDs.clear();
    	listManager.setSelectedNotebooks(selectedNotebookGUIDs);
    	notebookTree.blockSignals(false);
    }
	// Triggered when the notebook DB has been updated
	private void notebookIndexUpdated() {
		logger.log(logger.HIGH, "Entering NeverNote.notebookIndexUpdated");
		if (selectedNotebookGUIDs == null)
			selectedNotebookGUIDs = new ArrayList<String>();
		List<Notebook> books = conn.getNotebookTable().getAll();
		for (int i=books.size()-1; i>=0; i--) {
			for (int j=0; j<listManager.getArchiveNotebookIndex().size(); j++) {
				if (listManager.getArchiveNotebookIndex().get(j).getGuid().equals(books.get(i).getGuid())) {
					books.remove(i);
					j=listManager.getArchiveNotebookIndex().size();
				}
			}
		}
		
		
		listManager.countNotebookResults(listManager.getNoteIndex());
		notebookTree.blockSignals(true);
    	notebookTree.load(books, listManager.getLocalNotebooks());
    	for (int i=selectedNotebookGUIDs.size()-1; i>=0; i--) {
    		boolean found = notebookTree.selectGuid(selectedNotebookGUIDs.get(i));
    		if (!found)
    			selectedNotebookGUIDs.remove(i);
    	}
    	notebookTree.blockSignals(false);
    	
		logger.log(logger.HIGH, "Leaving NeverNote.notebookIndexUpdated");
    }
    // Show/Hide note information
	private void toggleNotebookWindow() {
		logger.log(logger.HIGH, "Entering NeverNote.toggleNotebookWindow");
    	if (notebookTree.isVisible())
    		notebookTree.hide();
    	else
    		notebookTree.show();
    	menuBar.hideNotebooks.setChecked(notebookTree.isVisible());
    	Global.saveWindowVisible("notebookTree", notebookTree.isVisible());
    	logger.log(logger.HIGH, "Leaving NeverNote.toggleNotebookWindow");
    }	
	// Add a new notebook
	@SuppressWarnings("unused")
	private void addNotebook() {
		logger.log(logger.HIGH, "Inside NeverNote.addNotebook");
		NotebookEdit edit = new NotebookEdit();
		edit.setNotebooks(listManager.getNotebookIndex());
		edit.exec();
	
		if (!edit.okPressed())
			return;
        
		Calendar currentTime = new GregorianCalendar();
		Long l = new Long(currentTime.getTimeInMillis());
		String randint = new String(Long.toString(l));
	
		Notebook newBook = new Notebook();
		newBook.setUpdateSequenceNum(0);
		newBook.setGuid(randint);
		newBook.setName(edit.getNotebook());
		newBook.setServiceCreated(new Date().getTime());
		newBook.setServiceUpdated(new Date().getTime());
		newBook.setDefaultNotebook(false);
		newBook.setPublished(false);
		
		listManager.getNotebookIndex().add(newBook);
		if (edit.isLocal())
			listManager.getLocalNotebooks().add(newBook.getGuid());
		conn.getNotebookTable().addNotebook(newBook, true, edit.isLocal());
		notebookIndexUpdated();
		listManager.countNotebookResults(listManager.getNoteIndex());
//		notebookTree.updateCounts(listManager.getNotebookIndex(), listManager.getNotebookCounter());
		logger.log(logger.HIGH, "Leaving NeverNote.addNotebook");
	}
	// Edit an existing notebook
	@SuppressWarnings("unused")
	private void editNotebook() {
		logger.log(logger.HIGH, "Entering NeverNote.editNotebook");
		NotebookEdit edit = new NotebookEdit();
		edit.setTitle("Edit Notebook");
		edit.setLocalCheckboxEnabled(false);
		List<QTreeWidgetItem> selections = notebookTree.selectedItems();
		QTreeWidgetItem currentSelection;
		currentSelection = selections.get(0);
		edit.setNotebook(currentSelection.text(0));
		edit.setNotebooks(listManager.getNotebookIndex());
		edit.exec();
	
		if (!edit.okPressed())
			return;
        
		String guid = currentSelection.text(2);
		updateListNotebookName(currentSelection.text(0), edit.getNotebook());
		currentSelection.setText(0, edit.getNotebook());
		
		for (int i=0; i<listManager.getNotebookIndex().size(); i++) {
			if (listManager.getNotebookIndex().get(i).getGuid().equals(guid)) {
				listManager.getNotebookIndex().get(i).setName(edit.getNotebook());
				conn.getNotebookTable().updateNotebook(listManager.getNotebookIndex().get(i), true);
				i=listManager.getNotebookIndex().size();
			}
		}
		
		// Build a list of non-closed notebooks
		List<Notebook> nbooks = new ArrayList<Notebook>();
		for (int i=0; i<listManager.getNotebookIndex().size(); i++) {
			boolean found=false;
			for (int j=0; j<listManager.getArchiveNotebookIndex().size(); j++) {
				if (listManager.getArchiveNotebookIndex().get(j).getGuid().equals(listManager.getNotebookIndex().get(i).getGuid()))
					found = true;
			}
			if (!found)
				nbooks.add(listManager.getNotebookIndex().get(i));
		}
		
		browserWindow.setNotebookList(nbooks);
		logger.log(logger.HIGH, "Leaving NeverNote.editNotebook");
	}
	// Delete an existing notebook
	@SuppressWarnings("unused")
	private void deleteNotebook() {
		logger.log(logger.HIGH, "Entering NeverNote.deleteNotebook");
		boolean assigned = false;
		// Check if any notes have this notebook
		List<QTreeWidgetItem> selections = notebookTree.selectedItems();
        for (int i=0; i<selections.size(); i++) {
        	QTreeWidgetItem currentSelection;
    		currentSelection = selections.get(i);
    		String guid = currentSelection.text(2);
    		for (int j=0; j<listManager.getNoteIndex().size(); j++) {
    			String noteGuid = listManager.getNoteIndex().get(j).getNotebookGuid();
    			if (noteGuid.equals(guid)) {
    				assigned = true;
    				j=listManager.getNoteIndex().size();
    				i=selections.size();
    			}
    		}
        }
		if (assigned) {
			QMessageBox.information(this, "Unable to Delete", "Some of the selected notebook(s) contain notes.\n"+
					"Please delete the notes or move them to another notebook before deleting any notebooks.");
			return;
		}
		
		if (conn.getNotebookTable().getAll().size() == 1) {
			QMessageBox.information(this, "Unable to Delete", "You must have at least one notebook.");
			return;
		}
        
        // If all notebooks are clear, verify the delete
		if (QMessageBox.question(this, "Confirmation", "Delete the selected notebooks?",
			QMessageBox.StandardButton.Yes, 
			QMessageBox.StandardButton.No)==StandardButton.No.value()) {
			return;
		}
		
		// If confirmed, delete the notebook
        for (int i=selections.size()-1; i>=0; i--) {
        	QTreeWidgetItem currentSelection;
    		currentSelection = selections.get(i);
    		String guid = currentSelection.text(2);
    		conn.getNotebookTable().expungeNotebook(guid, true);
    		listManager.deleteNotebook(guid);
        }
//        for (int i=<dbRunner.getLocalNotebooks().size()-1; i>=0; i--) {
 //       	if (dbRunner.getLocalNotebooks().get(i).equals(arg0))
 //       }
        notebookTree.load(listManager.getNotebookIndex(), listManager.getLocalNotebooks());
        listManager.countNotebookResults(listManager.getNoteIndex());
//		notebookTree.updateCounts(listManager.getNotebookIndex(), listManager.getNotebookCounter());
        logger.log(logger.HIGH, "Entering NeverNote.deleteNotebook");
	}
	// A note's notebook has been updated
	@SuppressWarnings("unused")
	private void updateNoteNotebook(String guid, String notebookGuid) {
		
		// Update the list manager
		listManager.updateNoteNotebook(guid, notebookGuid);
		listManager.countNotebookResults(listManager.getNoteIndex());
//		notebookTree.updateCounts(listManager.getNotebookIndex(), listManager.getNotebookCounter());	
		
		// Find the name of the notebook
		String notebookName = null;
		for (int i=0; i<listManager.getNotebookIndex().size(); i++) {
			if (listManager.getNotebookIndex().get(i).getGuid().equals(notebookGuid)) {
				notebookName = listManager.getNotebookIndex().get(i).getName();
				i=listManager.getNotebookIndex().size();
			}
		}
		
		// If we found the name, update the browser window
		if (notebookName != null) {
			updateListNoteNotebook(guid, notebookName);
			if (guid.equals(currentNoteGuid)) {
				int pos =  browserWindow.notebookBox.findText(notebookName);
				if (pos >=0)
					browserWindow.notebookBox.setCurrentIndex(pos);
			}
		}
		
		// If we're dealing with the current note, then we need to be sure and update the notebook there
		if (guid.equals(currentNoteGuid)) {
			if (currentNote != null) {
				currentNote.setNotebookGuid(notebookGuid);
			}
		}
	}
	// Open/close notebooks
	@SuppressWarnings("unused")
	private void closeNotebooks() {
		NotebookArchive na = new NotebookArchive(listManager.getNotebookIndex(), listManager.getArchiveNotebookIndex());
		na.exec();
		if (!na.okClicked())
			return;
		
		waitCursor(true);
		listManager.getArchiveNotebookIndex().clear();
		
		for (int i=na.getClosedBookList().count()-1; i>=0; i--) {
			String text = na.getClosedBookList().takeItem(i).text();
			for (int j=0; j<listManager.getNotebookIndex().size(); j++) {
				if (listManager.getNotebookIndex().get(j).getName().equalsIgnoreCase(text)) {
					Notebook n = listManager.getNotebookIndex().get(j);
					conn.getNotebookTable().setArchived(n.getGuid(),true);
					listManager.getArchiveNotebookIndex().add(n);
					j=listManager.getNotebookIndex().size();
				}
			}
		}
		
		for (int i=na.getOpenBookList().count()-1; i>=0; i--) {
			String text = na.getOpenBookList().takeItem(i).text();
			for (int j=0; j<listManager.getNotebookIndex().size(); j++) {
				if (listManager.getNotebookIndex().get(j).getName().equalsIgnoreCase(text)) {
					Notebook n = listManager.getNotebookIndex().get(j);
					conn.getNotebookTable().setArchived(n.getGuid(),false);
					j=listManager.getNotebookIndex().size();
				}
			}
		}
		
		listManager.loadNotesIndex();
		notebookIndexUpdated();
		noteIndexUpdated(true);
//		noteIndexUpdated(false);
		
		// Build a list of non-closed notebooks
		List<Notebook> nbooks = new ArrayList<Notebook>();
		for (int i=0; i<listManager.getNotebookIndex().size(); i++) {
			boolean found=false;
			for (int j=0; j<listManager.getArchiveNotebookIndex().size(); j++) {
				if (listManager.getArchiveNotebookIndex().get(j).getGuid().equals(listManager.getNotebookIndex().get(i).getGuid()))
					found = true;
			}
			if (!found)
				nbooks.add(listManager.getNotebookIndex().get(i));
		}
		waitCursor(false);
		browserWindow.setNotebookList(nbooks);
	}

	
	
	
	
    //***************************************************************
    //***************************************************************
    //** These functions deal with Tag menu items
    //***************************************************************
    //***************************************************************
	// Add a new notebook
	@SuppressWarnings("unused")
	private void addTag() {
		logger.log(logger.HIGH, "Inside NeverNote.addTag");
		TagEdit edit = new TagEdit();
		edit.setTagList(listManager.getTagIndex());
		edit.exec();
	
		if (!edit.okPressed())
			return;
        
		Calendar currentTime = new GregorianCalendar();
		Long l = new Long(currentTime.getTimeInMillis());
		String randint = new String(Long.toString(l));
	
		Tag newTag = new Tag();
		newTag.setUpdateSequenceNum(0);
		newTag.setGuid(randint);
		newTag.setName(edit.getTag());
		conn.getTagTable().addTag(newTag, true);
		listManager.getTagIndex().add(newTag);
		reloadTagTree();
		
		logger.log(logger.HIGH, "Leaving NeverNote.addTag");
	}
	private void reloadTagTree() {
		logger.log(logger.HIGH, "Entering NeverNote.reloadTagTree");
		tagIndexUpdated(false);
		boolean filter = false;
		listManager.countTagResults(listManager.getNoteIndex());
		if (notebookTree.selectedItems().size() > 0 
						  && !notebookTree.selectedItems().get(0).text(0).equalsIgnoreCase("All Notebooks"))
						  filter = true;
		if (tagTree.selectedItems().size() > 0)
			filter = true;
		tagTree.showAllTags(!filter);
		logger.log(logger.HIGH, "Leaving NeverNote.reloadTagTree");
	}
	// Edit an existing tag
	@SuppressWarnings("unused")
	private void editTag() {
		logger.log(logger.HIGH, "Entering NeverNote.editTag");
		TagEdit edit = new TagEdit();
		edit.setTitle("Edit Tag");
		List<QTreeWidgetItem> selections = tagTree.selectedItems();
		QTreeWidgetItem currentSelection;
		currentSelection = selections.get(0);
		edit.setTag(currentSelection.text(0));
		edit.setTagList(listManager.getTagIndex());
		edit.exec();
	
		if (!edit.okPressed())
			return;
        
		String guid = currentSelection.text(2);
		currentSelection.setText(0,edit.getTag());
		
		for (int i=0; i<listManager.getTagIndex().size(); i++) {
			if (listManager.getTagIndex().get(i).getGuid().equals(guid)) {
				listManager.getTagIndex().get(i).setName(edit.getTag());
				conn.getTagTable().updateTag(listManager.getTagIndex().get(i), true);
				updateListTagName(guid);
				if (currentNote != null && currentNote.getTagGuids().contains(guid))
					browserWindow.setTag(getTagNamesForNote(currentNote));
				logger.log(logger.HIGH, "Leaving NeverNote.editTag");
				return;
			}
		}
		browserWindow.setTag(getTagNamesForNote(currentNote));
		logger.log(logger.HIGH, "Leaving NeverNote.editTag...");
	}
	// Delete an existing tag
	@SuppressWarnings("unused")
	private void deleteTag() {
		logger.log(logger.HIGH, "Entering NeverNote.deleteTag");
		
		if (QMessageBox.question(this, "Confirmation", "Delete the selected tags?",
			QMessageBox.StandardButton.Yes, 
			QMessageBox.StandardButton.No)==StandardButton.No.value()) {
							return;
		}
		
		List<QTreeWidgetItem> selections = tagTree.selectedItems();
        for (int i=selections.size()-1; i>=0; i--) {
        	QTreeWidgetItem currentSelection;
    		currentSelection = selections.get(i);    		
    		removeTagItem(currentSelection.text(2));
        }
        tagIndexUpdated(true);
        listManager.countTagResults(listManager.getNoteIndex());
//		tagTree.updateCounts(listManager.getTagCounter());
        logger.log(logger.HIGH, "Leaving NeverNote.deleteTag");
	}
	// Remove a tag tree item.  Go recursively down & remove the children too
	private void removeTagItem(String guid) {
    	for (int j=listManager.getTagIndex().size()-1; j>=0; j--) {    		
    		String parent = listManager.getTagIndex().get(j).getParentGuid();
    		if (parent != null && parent.equals(guid)) {  		
    			//Remove this tag's children
    			removeTagItem(listManager.getTagIndex().get(j).getGuid());
    		}
    	}
    	//Now, remove this tag
    	removeListTagName(guid);
    	conn.getTagTable().expungeTag(guid, true);    			
    	for (int a=0; a<listManager.getTagIndex().size(); a++) {
    		if (listManager.getTagIndex().get(a).getGuid().equals(guid)) {
    			listManager.getTagIndex().remove(a);
    			return;
    		}
    	}
	}
	// Setup the tree containing the user's tags
    private void initializeTagTree() {
    	logger.log(logger.HIGH, "Entering NeverNote.initializeTagTree");
    	tagTree.itemSelectionChanged.connect(this, "tagTreeSelection()");
    	listManager.tagSignal.refreshTagTreeCounts.connect(tagTree, "updateCounts(List)");
    	logger.log(logger.HIGH, "Leaving NeverNote.initializeTagTree");
    }
    // Listener when a tag is selected
    @SuppressWarnings("unused")
	private void tagTreeSelection() {
    	logger.log(logger.HIGH, "Entering NeverNote.tagTreeSelection");
    	
    	List<QTreeWidgetItem> x = tagTree.selectedItems();
    	for (int i=0; i<x.size(); i++) {
    	}
    	
    	clearTrashFilter();
    	clearAttributeFilter();
    	clearSavedSearchFilter();
    	
		menuBar.noteRestoreAction.setVisible(false);
		
    	List<QTreeWidgetItem> selections = tagTree.selectedItems();
    	QTreeWidgetItem currentSelection;
    	selectedTagGUIDs.clear();
    	for (int i=0; i<selections.size(); i++) {
    		currentSelection = selections.get(i);
    		selectedTagGUIDs.add(currentSelection.text(2));
    	}
    	if (selections.size() > 0) {
    		menuBar.tagEditAction.setEnabled(true);
    		menuBar.tagDeleteAction.setEnabled(true);
    	}
    	else {
    		menuBar.tagEditAction.setEnabled(false);
    		menuBar.tagDeleteAction.setEnabled(false);
    	}
    	listManager.setSelectedTags(selectedTagGUIDs);
    	listManager.loadNotesIndex();
    	noteIndexUpdated(false);
    	logger.log(logger.HIGH, "Leaving NeverNote.tagTreeSelection");
    }
    // trigger the tag index to be refreshed
    @SuppressWarnings("unused")
	private void tagIndexUpdated() {
    	tagIndexUpdated(true);
    }
    private void tagIndexUpdated(boolean reload) {
    	logger.log(logger.HIGH, "Entering NeverNote.tagIndexUpdated");
		if (selectedTagGUIDs == null)
			selectedTagGUIDs = new ArrayList<String>();
//		selectedTagGUIDs.clear();  // clear out old entries

		tagTree.blockSignals(true);
		if (reload)
			tagTree.load(listManager.getTagIndex());
    	for (int i=selectedTagGUIDs.size()-1; i>=0; i--) {
    		boolean found = tagTree.selectGuid(selectedTagGUIDs.get(i));
    		if (!found)
    			selectedTagGUIDs.remove(i);
    	}
    	tagTree.blockSignals(false);
    	
		browserWindow.setTag(getTagNamesForNote(currentNote));
    	logger.log(logger.HIGH, "Leaving NeverNote.tagIndexUpdated");
    }	
    // Show/Hide note information
	private void toggleTagWindow() {
		logger.log(logger.HIGH, "Entering NeverNote.toggleTagWindow");
    	if (tagTree.isVisible())
    		tagTree.hide();
    	else
    		tagTree.show();
    	menuBar.hideTags.setChecked(tagTree.isVisible());
    	Global.saveWindowVisible("tagTree", tagTree.isVisible());
    	logger.log(logger.HIGH, "Leaving NeverNote.toggleTagWindow");
    }   
	// A note's tags have been updated
	@SuppressWarnings("unused")
	private void updateNoteTags(String guid, List<String> tags) {
		listManager.saveNoteTags(guid, tags);
		listManager.countTagResults(listManager.getNoteIndex());
		StringBuffer names = new StringBuffer("");
		for (int i=0; i<tags.size(); i++) {
			names = names.append(tags.get(i));
			if (i<tags.size()-1) {
				names.append(Global.tagDelimeter + " ");
			}
		}
		browserWindow.setTag(names.toString());
		noteDirty = true;
//		tagTree.updateCounts(listManager.getTagCounter());
	}
	// Get a string containing all tag names for a note
	private String getTagNamesForNote(Note n) {
		logger.log(logger.HIGH, "Entering NeverNote.getTagNamesForNote");
		if (n==null || n.getGuid() == null || n.getGuid().equals(""))
			return "";
		StringBuffer buffer = new StringBuffer(100);
		Vector<String> v = new Vector<String>();
		List<String> guids = n.getTagGuids();
		
		if (guids == null) 
			return "";
		
		for (int i=0; i<guids.size(); i++) {
			v.add(listManager.getTagNameByGuid(guids.get(i)));
		}
		Comparator<String> comparator = Collections.reverseOrder();
		Collections.sort(v,comparator);
		Collections.reverse(v);
		
		for (int i = 0; i<v.size(); i++) {
			if (i>0) 
				buffer.append(", ");
			buffer.append(v.get(i));
		}
		
		logger.log(logger.HIGH, "Leaving NeverNote.getTagNamesForNote");
		return buffer.toString();
	}	
	// Tags were added via dropping notes from the note list
	@SuppressWarnings("unused")
	private void tagsAdded(String noteGuid, String tagGuid) {
		String tagName = null;
		for (int i=0; i<listManager.getTagIndex().size(); i++) {
			if (listManager.getTagIndex().get(i).getGuid().equals(tagGuid)) {
				tagName = listManager.getTagIndex().get(i).getName();
				i=listManager.getTagIndex().size();
			}
		}
		if (tagName == null)
			return;
		
		for (int i=0; i<noteTableView.model.rowCount(); i++) {
			QModelIndex modelIndex =  noteTableView.model.index(i, Global.noteTableGuidPosition);
			if (modelIndex != null) {
				SortedMap<Integer, Object> ix = noteTableView.model.itemData(modelIndex);
				String titleGuid = (String)ix.values().toArray()[0];
				if (titleGuid.equals(noteGuid)) {
					String text = (String)noteTableView.model.data(i, Global.noteTableTagPosition);
					if (!text.trim().equals(""))
						text = text + Global.tagDelimeter + " " +tagName;
					else
						text = tagName;
					noteTableView.model.setData(i, Global.noteTableTagPosition, text);
					noteTableView.model.setData(i, Global.noteTableSynchronizedPosition, "false");
					if (noteGuid.equals(currentNoteGuid))
						browserWindow.setTag(text);
					i=noteTableView.model.rowCount();
				}
			}
		}
	}
	private void clearTagFilter() {
		tagTree.blockSignals(true);
		tagTree.clearSelection();
		menuBar.noteRestoreAction.setVisible(false);
		menuBar.tagEditAction.setEnabled(false);
		menuBar.tagDeleteAction.setEnabled(false);
		selectedTagGUIDs.clear();
    	listManager.setSelectedTags(selectedTagGUIDs);
    	tagTree.blockSignals(false);
	}
	
	
    //***************************************************************
    //***************************************************************
    //** These functions deal with Saved Search menu items
    //***************************************************************
    //***************************************************************
	// Add a new notebook
	@SuppressWarnings("unused")
	private void addSavedSearch() {
		logger.log(logger.HIGH, "Inside NeverNote.addSavedSearch");
		SavedSearchEdit edit = new SavedSearchEdit();
		edit.setSearchList(listManager.getSavedSearchIndex());
		edit.exec();
	
		if (!edit.okPressed())
			return;
        
		Calendar currentTime = new GregorianCalendar();		
	 	Long l = new Long(currentTime.getTimeInMillis());
		String randint = new String(Long.toString(l));
	
		SavedSearch search = new SavedSearch();
		search.setUpdateSequenceNum(0);
		search.setGuid(randint);
		search.setName(edit.getName());
		search.setQuery(edit.getQuery());
		search.setFormat(QueryFormat.USER);
		listManager.getSavedSearchIndex().add(search);
		conn.getSavedSearchTable().addSavedSearch(search, true);
		savedSearchIndexUpdated();
		logger.log(logger.HIGH, "Leaving NeverNote.addSavedSearch");
	}
	// Edit an existing tag
	@SuppressWarnings("unused")
	private void editSavedSearch() {
		logger.log(logger.HIGH, "Entering NeverNote.editSavedSearch");
		SavedSearchEdit edit = new SavedSearchEdit();
		edit.setTitle("Edit Search");
		List<QTreeWidgetItem> selections = savedSearchTree.selectedItems();
		QTreeWidgetItem currentSelection;
		currentSelection = selections.get(0);
		String guid = currentSelection.text(1);
		SavedSearch s = conn.getSavedSearchTable().getSavedSearch(guid);
		edit.setName(currentSelection.text(0));
		edit.setQuery(s.getQuery());
		edit.setSearchList(listManager.getSavedSearchIndex());
		edit.exec();
	
		if (!edit.okPressed())
			return;
        
		List<SavedSearch> list = listManager.getSavedSearchIndex();
		SavedSearch search = null;
		boolean found = false;
		for (int i=0; i<list.size(); i++) {
			search = list.get(i);
			if (search.getGuid().equals(guid)) {
				i=list.size();
				found = true;
			}
		}
		if (!found)
			return;
		search.setName(edit.getName());
		search.setQuery(edit.getQuery());
		conn.getSavedSearchTable().updateSavedSearch(search, true);
		savedSearchIndexUpdated();
		logger.log(logger.HIGH, "Leaving NeverNote.editSavedSearch");
	}
	// Delete an existing tag
	@SuppressWarnings("unused")
	private void deleteSavedSearch() {
		logger.log(logger.HIGH, "Entering NeverNote.deleteSavedSearch");
		
		if (QMessageBox.question(this, "Confirmation", "Delete the selected search?",
			QMessageBox.StandardButton.Yes, 
			QMessageBox.StandardButton.No)==StandardButton.No.value()) {
							return;
		}
		
		List<QTreeWidgetItem> selections = savedSearchTree.selectedItems();
        for (int i=selections.size()-1; i>=0; i--) {
        	QTreeWidgetItem currentSelection;
    		currentSelection = selections.get(i);
        	for (int j=0; j<listManager.getSavedSearchIndex().size(); j++) {
        		if (listManager.getSavedSearchIndex().get(j).getGuid().equals(currentSelection.text(1))) {
        			conn.getSavedSearchTable().expungeSavedSearch(listManager.getSavedSearchIndex().get(j).getGuid(), true);
        			listManager.getSavedSearchIndex().remove(j);
        			j=listManager.getSavedSearchIndex().size()+1;
        		}
        	}
        	selections.remove(i);
        }
        savedSearchIndexUpdated();
        logger.log(logger.HIGH, "Leaving NeverNote.deleteSavedSearch");
	}
    // Setup the tree containing the user's tags
    private void initializeSavedSearchTree() {
    	logger.log(logger.HIGH, "Entering NeverNote.initializeSavedSearchTree");
    	savedSearchTree.itemSelectionChanged.connect(this, "savedSearchTreeSelection()");
    	logger.log(logger.HIGH, "Leaving NeverNote.initializeSavedSearchTree");
    }
    // Listener when a tag is selected
    @SuppressWarnings("unused")
	private void savedSearchTreeSelection() {
    	logger.log(logger.HIGH, "Entering NeverNote.savedSearchTreeSelection");

    	clearNotebookFilter();
    	clearTagFilter();
    	clearTrashFilter();
    	clearAttributeFilter();
    	
    	String currentGuid = selectedSavedSearchGUID;
    	menuBar.savedSearchEditAction.setEnabled(true);
    	menuBar.savedSearchDeleteAction.setEnabled(true);
    	List<QTreeWidgetItem> selections = savedSearchTree.selectedItems();
    	QTreeWidgetItem currentSelection;
    	selectedSavedSearchGUID = "";
    	for (int i=0; i<selections.size(); i++) {
    		currentSelection = selections.get(i);
    		if (currentSelection.text(1).equals(currentGuid)) {
    			currentSelection.setSelected(false);
    		} else {
    			selectedSavedSearchGUID = currentSelection.text(1);
    		}
//    		i = selections.size() +1;
    	}
    	
    	// There is the potential for no notebooks to be selected if this 
    	// happens then we make it look like all notebooks were selecetd.
    	// If that happens, just select the "all notebooks"
    	if (selections.size()==0) {
    		clearSavedSearchFilter();
    	}
    	listManager.setSelectedSavedSearch(selectedSavedSearchGUID);
    	
    	logger.log(logger.HIGH, "Leaving NeverNote.savedSearchTreeSelection");
    }
    private void clearSavedSearchFilter() {
    	menuBar.savedSearchEditAction.setEnabled(false);
    	menuBar.savedSearchDeleteAction.setEnabled(false);
    	savedSearchTree.blockSignals(true);
    	savedSearchTree.clearSelection();
    	savedSearchTree.blockSignals(false);
    	selectedSavedSearchGUID = "";
    	searchField.setEditText("");
    	searchPerformed = false;
    	listManager.setSelectedSavedSearch(selectedSavedSearchGUID);
    }
    // trigger the tag index to be refreshed
	private void savedSearchIndexUpdated() { 
		if (selectedSavedSearchGUID == null)
			selectedSavedSearchGUID = new String();
		savedSearchTree.blockSignals(true);
    	savedSearchTree.load(listManager.getSavedSearchIndex());
    	savedSearchTree.selectGuid(selectedSavedSearchGUID);
    	savedSearchTree.blockSignals(false);
    }
    // trigger when the saved search selection changes
    @SuppressWarnings("unused")
	private void updateSavedSearchSelection() {
		logger.log(logger.HIGH, "Entering NeverNote.updateSavedSearchSelection()");
		
    	menuBar.savedSearchEditAction.setEnabled(true);
    	menuBar.savedSearchDeleteAction.setEnabled(true);
    	List<QTreeWidgetItem> selections = savedSearchTree.selectedItems();

    	if (selections.size() > 0) {
    		menuBar.savedSearchEditAction.setEnabled(true);
    		menuBar.savedSearchDeleteAction.setEnabled(true);
    		selectedSavedSearchGUID = selections.get(0).text(1);
    		SavedSearch s = conn.getSavedSearchTable().getSavedSearch(selectedSavedSearchGUID);
    		searchField.setEditText(s.getQuery());
    	} else { 
        	menuBar.savedSearchEditAction.setEnabled(false);
        	menuBar.savedSearchDeleteAction.setEnabled(false);
        	selectedSavedSearchGUID = "";
        	searchField.setEditText("");
    	}
    	searchFieldChanged();
    	
		logger.log(logger.HIGH, "Leaving NeverNote.updateSavedSearchSelection()");

    	
    }
    // Show/Hide note information
	private void toggleSavedSearchWindow() {
		logger.log(logger.HIGH, "Entering NeverNote.toggleSavedSearchWindow");
    	if (savedSearchTree.isVisible())
    		savedSearchTree.hide();
    	else
    		savedSearchTree.show();
    	menuBar.hideSavedSearches.setChecked(savedSearchTree.isVisible());
				
		Global.saveWindowVisible("savedSearchTree", savedSearchTree.isVisible());
    	logger.log(logger.HIGH, "Leaving NeverNote.toggleSavedSearchWindow");
    }
    	
	
	
	
    //***************************************************************
    //***************************************************************
    //** These functions deal with Help menu & tool menu items
    //***************************************************************
    //***************************************************************
	// Show database status
	@SuppressWarnings("unused")
	private void databaseStatus() {
		waitCursor(true);
		int dirty = conn.getNoteTable().getDirtyCount();
		int unindexed = conn.getNoteTable().getUnindexedCount();
		DatabaseStatus status = new DatabaseStatus();
		status.setUnsynchronized(dirty);
		status.setUnindexed(unindexed);
		status.setNoteCount(conn.getNoteTable().getNoteCount());
		status.setNotebookCount(listManager.getNotebookIndex().size());
		status.setSavedSearchCount(listManager.getSavedSearchIndex().size());
		status.setTagCount(listManager.getTagIndex().size());
		status.setResourceCount(conn.getNoteTable().noteResourceTable.getResourceCount());
		status.setWordCount(conn.getWordsTable().getWordCount());
		waitCursor(false);
		status.exec();
	}
	// Compact the database
	@SuppressWarnings("unused")
	private void compactDatabase() {
    	logger.log(logger.HIGH, "Entering NeverNote.compactDatabase");
   		if (QMessageBox.question(this, "Confirmation", "This will free unused space in the database, "+
   				"but please be aware that depending upon the size of your database this can be time consuming " +
   				"and NeverNote will be unresponsive until it is complete.  Do you wish to continue?",
   				QMessageBox.StandardButton.Yes, 
				QMessageBox.StandardButton.No)==StandardButton.No.value() && Global.verifyDelete() == true) {
							return;
   		}
   		setMessage("Compacting database.");
   		waitCursor(true);
   		listManager.compactDatabase();
   		waitCursor(false);
   		setMessage("Database compact is complete.");    	
    	logger.log(logger.HIGH, "Leaving NeverNote.compactDatabase");
    }
	@SuppressWarnings("unused")
	private void accountInformation() {
		logger.log(logger.HIGH, "Entering NeverNote.accountInformation");
		AccountDialog dialog = new AccountDialog();
		dialog.show();
		logger.log(logger.HIGH, "Leaving NeverNote.accountInformation");
	}
	@SuppressWarnings("unused")
	private void releaseNotes() {
		logger.log(logger.HIGH, "Entering NeverNote.releaseNotes");
		QDialog dialog = new QDialog(this);
		QHBoxLayout layout = new QHBoxLayout();
		QTextEdit textBox = new QTextEdit();
		layout.addWidget(textBox);
		textBox.setReadOnly(true);
		QFile file = new QFile(Global.getFileManager().getHomeDirPath("release.txt"));
		if (!file.open(new QIODevice.OpenMode(QIODevice.OpenModeFlag.ReadOnly,
                QIODevice.OpenModeFlag.Text)))
			return;
		textBox.setText(file.readAll().toString());
		file.close();
		dialog.setWindowTitle("Release Notes");
		dialog.setLayout(layout);
		dialog.show();
		logger.log(logger.HIGH, "Leaving NeverNote.releaseNotes");
	}
	// Called when user picks Log from the help menu
	@SuppressWarnings("unused")
	private void logger() {
		logger.log(logger.HIGH, "Entering NeverNote.logger");
		QDialog dialog = new QDialog(this);
		QHBoxLayout layout = new QHBoxLayout();
		QListWidget textBox = new QListWidget();
		layout.addWidget(textBox);
		textBox.addItems(emitLog);
		
		dialog.setLayout(layout);
		dialog.setWindowTitle("Mesasge Log");
		dialog.show();
		logger.log(logger.HIGH, "Leaving NeverNote.logger");
	}
	// Menu option "help/about" was selected
	@SuppressWarnings("unused")
	private void about() {
		logger.log(logger.HIGH, "Entering NeverNote.about");
		QMessageBox.about(this, 
						tr("About NeverNote"),
						tr("<h4><center><b>NeverNote</b></center></h4><hr><center>Version "+Global.version+"<hr></center>Evernote"
								+" Generic client.<br><br>" 
								+"Licensed under GPL v2.  <br><hr><br>"
								+"Evernote is copyright 2001-2010 by Evernote Corporation<br>"
								+"Jambi and QT are the licensed trademark of Nokia Corporation<br>"
								+"PDFRenderer is licened under the LGPL<br>"
								+"Jazzy is licened under the LGPL<br>"
								+"Java is a registered trademark of Sun Microsystems.<br><hr>"));	
		logger.log(logger.HIGH, "Leaving NeverNote.about");
	}
	// Hide the entire left hand side
	@SuppressWarnings("unused")
	private void toggleLeftSide() {
		boolean hidden;
		
		hidden = !menuBar.hideLeftSide.isChecked();
		menuBar.hideLeftSide.setChecked(!hidden);
		
		if (notebookTree.isVisible() != hidden)
			toggleNotebookWindow();
		if (savedSearchTree.isVisible() != hidden)
			toggleSavedSearchWindow();
		if (tagTree.isVisible() != hidden)
			toggleTagWindow();
		if (attributeTree.isVisible() != hidden)
			toggleAttributesWindow();
		if (trashTree.isVisible() != hidden)
			toggleTrashWindow();
		
		Global.saveWindowVisible("leftPanel", hidden);
		
	}
			
	
    //***************************************************************
    //***************************************************************
    //** These functions deal with the Toolbar
    //***************************************************************
    //***************************************************************  
	// Text in the search bar has been cleared
	private void searchFieldCleared() {
		searchField.setEditText("");
		saveNoteIndexWidth();
	}
	// text in the search bar changed.  We only use this to tell if it was cleared, 
	// otherwise we trigger off searchFieldChanged.
	@SuppressWarnings("unused")
	private void searchFieldTextChanged(String text) {
		if (text.trim().equals("")) {
			searchFieldCleared();
			if (searchPerformed) {
				noteCache.clear();
				listManager.setEnSearch("");
/////				listManager.clearNoteIndexSearch();
				//noteIndexUpdated(true);
				listManager.loadNotesIndex();
				refreshEvernoteNote(true);
				noteIndexUpdated(false);
			}
			searchPerformed = false;
		}
	}
    // Text in the toolbar has changed
    private void searchFieldChanged() {
    	logger.log(logger.HIGH, "Entering NeverNote.searchFieldChanged");
    	noteCache.clear();
    	saveNoteIndexWidth();
    	String text = searchField.currentText();
    	listManager.setEnSearch(text.trim());
    	listManager.loadNotesIndex();
//--->>>    	noteIndexUpdated(true);
    	noteIndexUpdated(false);
    	refreshEvernoteNote(true);
    	searchPerformed = true;
    	logger.log(logger.HIGH, "Leaving NeverNote.searchFieldChanged");
    }
    // Build the window tool bar
    private void setupToolBar() {
    	logger.log(logger.HIGH, "Entering NeverNote.setupToolBar");
    	toolBar = addToolBar(tr("toolBar"));	

    	prevButton = toolBar.addAction("Previous");
    	QIcon prevIcon = new QIcon(iconPath+"back.png");
    	prevButton.setIcon(prevIcon);
    	prevButton.triggered.connect(this, "previousViewedAction()");  	
    	
    	nextButton = toolBar.addAction("Next");
    	QIcon nextIcon = new QIcon(iconPath+"forward.png");
    	nextButton.setIcon(nextIcon);
    	nextButton.triggered.connect(this, "nextViewedAction()");  	
    	
    	upButton = toolBar.addAction("Up");
    	QIcon upIcon = new QIcon(iconPath+"up.png");
    	upButton.setIcon(upIcon);
    	upButton.triggered.connect(this, "upAction()");  	
    	
    	downButton = toolBar.addAction("Down");
    	QIcon downIcon = new QIcon(iconPath+"down.png");
    	downButton.setIcon(downIcon);
    	downButton.triggered.connect(this, "downAction()");
    	
    	synchronizeButton = toolBar.addAction("Synchronize");
    	synchronizeAnimation = new ArrayList<QIcon>();
    	synchronizeAnimation.add(new QIcon(iconPath+"synchronize-0.png"));
    	synchronizeAnimation.add(new QIcon(iconPath+"synchronize-1.png"));
    	synchronizeAnimation.add(new QIcon(iconPath+"synchronize-2.png"));
    	synchronizeAnimation.add(new QIcon(iconPath+"synchronize-3.png"));
    	synchronizeButton.setIcon(synchronizeAnimation.get(0));
    	synchronizeFrame = 0;
    	synchronizeButton.triggered.connect(this, "evernoteSync()");
    	
    	printButton = toolBar.addAction("Print");
    	QIcon printIcon = new QIcon(iconPath+"print.png");
    	printButton.setIcon(printIcon);
    	printButton.triggered.connect(this, "printNote()");
    	
    	tagButton = toolBar.addAction("Tag"); 
    	QIcon tagIcon = new QIcon(iconPath+"tag.png");
    	tagButton.setIcon(tagIcon);
    	tagButton.triggered.connect(browserWindow, "modifyTags()");
    	
    	attributeButton = toolBar.addAction("Attributes"); 
    	QIcon attributeIcon = new QIcon(iconPath+"attribute.png");
    	attributeButton.setIcon(attributeIcon);
    	attributeButton.triggered.connect(this, "toggleNoteInformation()");
    	    	
    	emailButton = toolBar.addAction("Email");
    	QIcon emailIcon = new QIcon(iconPath+"email.png");
    	emailButton.setIcon(emailIcon);
    	emailButton.triggered.connect(this, "emailNote()");
    	
    	deleteButton = toolBar.addAction("Delete");   	
    	QIcon deleteIcon = new QIcon(iconPath+"delete.png");
    	deleteButton.setIcon(deleteIcon);
    	deleteButton.triggered.connect(this, "deleteNote()");
    	 	
    	newButton = toolBar.addAction("New");
    	QIcon newIcon = new QIcon(iconPath+"new.png");
    	newButton.triggered.connect(this, "addNote()");
    	newButton.setIcon(newIcon);
     	toolBar.addSeparator();
      	toolBar.addWidget(new QLabel("Quota:"));
    	toolBar.addWidget(quotaBar);
    	//quotaBar.setSizePolicy(Policy.Minimum, Policy.Minimum);
    	updateQuotaBar();
    	
    	// Setup the zoom
    	zoomSpinner = new QSpinBox();
    	zoomSpinner.setMinimum(10);
    	zoomSpinner.setMaximum(1000);
    	zoomSpinner.setAccelerated(true);
    	zoomSpinner.setSingleStep(10);
    	zoomSpinner.setValue(100);
    	zoomSpinner.valueChanged.connect(this, "zoomChanged()");
    	toolBar.addWidget(new QLabel("Zoom"));
    	toolBar.addWidget(zoomSpinner);
    	
    	//toolBar.addWidget(new QLabel("                    "));
    	toolBar.addSeparator();
    	toolBar.addWidget(new QLabel("  Search:"));
    	toolBar.addWidget(searchField);
    	QSizePolicy sizePolicy = new QSizePolicy();
    	sizePolicy.setHorizontalPolicy(Policy.MinimumExpanding);
    	searchField.setSizePolicy(sizePolicy);
    	searchField.setInsertPolicy(InsertPolicy.InsertAtTop);

    	searchClearButton = toolBar.addAction("Search Clear");
    	QIcon searchClearIcon = new QIcon(iconPath+"searchclear.png");
    	searchClearButton.setIcon(searchClearIcon);
    	searchClearButton.triggered.connect(this, "searchFieldCleared()");
    	
    	logger.log(logger.HIGH, "Leaving NeverNote.setupToolBar");
    }
    // Update the sychronize button picture
    @SuppressWarnings("unused")
	private void updateSyncButton() {
    	synchronizeFrame++;
    	if (synchronizeFrame == 4) 
    		synchronizeFrame = 0;
    	synchronizeButton.setIcon(synchronizeAnimation.get(synchronizeFrame));
    }
    // Synchronize with Evernote
	@SuppressWarnings("unused")
	private void evernoteSync() {
    	logger.log(logger.HIGH, "Entering NeverNote.evernoteSync");
    	if (!Global.isConnected)
    		remoteConnect();
    	if (Global.isConnected)
    		synchronizeAnimationTimer.start(200);
    	syncTimer();
    	logger.log(logger.HIGH, "Leaving NeverNote.evernoteSync");
    }
    private void updateQuotaBar() {
    	long limit = Global.getUploadLimit();
    	long amount = Global.getUploadAmount();
    	if (amount>0 && limit>0) {
    		int percent =(int)(amount*100/limit);
    		quotaBar.setValue(percent);
    	} else 
    		quotaBar.setValue(0);
    }
	// Zoom changed
    @SuppressWarnings("unused")
	private void zoomChanged() {
    	browserWindow.getBrowser().setZoomFactor(new Double(zoomSpinner.value())/100);
    }

    //****************************************************************
    //****************************************************************
    //* System Tray functions
    //****************************************************************
    //****************************************************************
	private void trayToggleVisible() {
    	if (isVisible()) {
    		hide();
    	} else {
    		show();
    		raise();
    	}
    }
    @SuppressWarnings("unused")
	private void trayActivated(QSystemTrayIcon.ActivationReason reason) {
    	if (reason == QSystemTrayIcon.ActivationReason.DoubleClick) {
    		String name = QSystemTrayIcon.MessageIcon.resolve(reason.value()).name();
    		trayToggleVisible();
    	}
    }
    
    
    //***************************************************************
    //***************************************************************
    //** These functions deal with the trash tree
    //***************************************************************
    //***************************************************************    
    // Setup the tree containing the trash.
    @SuppressWarnings("unused")
	private void trashTreeSelection() {     
    	logger.log(logger.HIGH, "Entering NeverNote.trashTreeSelection");
    	
    	clearNotebookFilter();
    	clearTagFilter();
    	clearAttributeFilter();
    	clearSavedSearchFilter();
    	
    	String tempGuid = currentNoteGuid;
    	
//    	currentNoteGuid = "";
    	currentNote = new Note();
    	selectedNoteGUIDs.clear();
    	listManager.getSelectedNotebooks().clear();
    	listManager.getSelectedTags().clear();
    	listManager.setSelectedSavedSearch("");
    	browserWindow.clear();
    
    	// toggle the add buttons
    	newButton.setEnabled(!newButton.isEnabled());
    	menuBar.noteAdd.setEnabled(newButton.isEnabled());
    	menuBar.noteAdd.setVisible(true);
    	
    	List<QTreeWidgetItem> selections = trashTree.selectedItems();
    	if (selections.size() == 0) {
    		currentNoteGuid = trashNoteGuid;
   			trashNoteGuid = tempGuid;
    		Global.showDeleted = false;
    		menuBar.noteRestoreAction.setEnabled(false);
    		menuBar.noteRestoreAction.setVisible(false);
    	}
    	else {
    		currentNoteGuid = trashNoteGuid;
   			trashNoteGuid = tempGuid;
    		menuBar.noteRestoreAction.setEnabled(true);
    		menuBar.noteRestoreAction.setVisible(true);
    		Global.showDeleted = true;
    	}
    	listManager.loadNotesIndex();
    	noteIndexUpdated(false);
////    	browserWindow.setEnabled(newButton.isEnabled());
    	browserWindow.setReadOnly(!newButton.isEnabled());
    	logger.log(logger.HIGH, "Leaving NeverNote.trashTreeSelection");
    }
    // Empty the trash file
    @SuppressWarnings("unused")
	private void emptyTrash() {
//    	browserWindow.clear();
    	listManager.emptyTrash();
    	if (trashTree.selectedItems().size() > 0) {
    		listManager.getSelectedNotebooks().clear();
        	listManager.getSelectedTags().clear();
        	listManager.setSelectedSavedSearch("");
        	newButton.setEnabled(!newButton.isEnabled());
        	menuBar.noteAdd.setEnabled(newButton.isEnabled());
        	menuBar.noteAdd.setVisible(true);
        	browserWindow.clear();
        	
        	clearTagFilter();
        	clearNotebookFilter();
        	clearSavedSearchFilter();
        	clearAttributeFilter();
        	       	
        	Global.showDeleted = false;
    		menuBar.noteRestoreAction.setEnabled(false);
    		menuBar.noteRestoreAction.setVisible(false);
        	
        	listManager.loadNotesIndex();
//--->>>        	noteIndexUpdated(true);
        	noteIndexUpdated(false);
    	}  	
   }
    // Show/Hide trash window
	private void toggleTrashWindow() {
		logger.log(logger.HIGH, "Entering NeverNote.toggleTrashWindow");
    	if (trashTree.isVisible())
    		trashTree.hide();
    	else
    		trashTree.show();
    	menuBar.hideTrash.setChecked(trashTree.isVisible());
    	
		Global.saveWindowVisible("trashTree", trashTree.isVisible());
    	logger.log(logger.HIGH, "Leaving NeverNote.trashWindow");
    }    
	private void clearTrashFilter() {
		Global.showDeleted = false;
    	newButton.setEnabled(true);
    	menuBar.noteAdd.setEnabled(true);
    	menuBar.noteAdd.setVisible(true);
		trashTree.blockSignals(true);
		trashTree.clearSelection();
		trashTree.blockSignals(false);
		
	}
    
   
    //***************************************************************
    //***************************************************************
    //** These functions deal with connection settings
    //***************************************************************
    //***************************************************************
	// SyncRunner had a problem and things are disconnected
	@SuppressWarnings("unused")
	private void remoteErrorDisconnect() {
		menuBar.connectAction.setText("Connect");
		menuBar.connectAction.setToolTip("Connect to Evernote");
		menuBar.synchronizeAction.setEnabled(false);
		synchronizeAnimationTimer.stop();
		return;
	}
	// Do a manual connect/disconnect
    private void remoteConnect() {
    	logger.log(logger.HIGH, "Entering NeverNote.remoteConnect");

    	if (Global.isConnected) {
    		Global.isConnected = false;
    		syncRunner.enDisconnect();
    		setupConnectMenuOptions();
    		setupOnlineMenu();
    		return;
    	}
    	
    	AESEncrypter aes = new AESEncrypter();
    	try {
			aes.decrypt(new FileInputStream(Global.getFileManager().getHomeDirFile("secure.txt")));
		} catch (FileNotFoundException e) {
			// File not found, so we'll just get empty strings anyway. 
		}
		String userid = aes.getUserid();
		String password = aes.getPassword();
		if (!userid.equals("") && !password.equals("")) {
    		Global.username = userid;
    		Global.password = password;
		}		

        // Show the login dialog box
		if (!Global.automaticLogin() || userid.equals("")|| password.equals("")) {
			LoginDialog login = new LoginDialog();
			login.exec();
		
			if (!login.okPressed()) {
				return;
			}
        
			Global.username = login.getUserid();
			Global.password = login.getPassword();
		}
		syncRunner.username = Global.username;
		syncRunner.password = Global.password;
		syncRunner.userStoreUrl = Global.userStoreUrl;
		syncRunner.noteStoreUrl = Global.noteStoreUrl;
		syncRunner.noteStoreUrlBase = Global.noteStoreUrlBase;
		syncRunner.enConnect();
		Global.isConnected = syncRunner.isConnected;
		setupOnlineMenu();
		setupConnectMenuOptions();
		logger.log(logger.HIGH, "Leaving NeverNote.remoteConnect");
    }
    private void setupConnectMenuOptions() {
    	logger.log(logger.HIGH, "entering NeverNote.setupConnectMenuOptions");
		if (!Global.isConnected) {
			menuBar.connectAction.setText("Connect");
			menuBar.connectAction.setToolTip("Connect to Evernote");
			menuBar.synchronizeAction.setEnabled(false);
		} else {
			menuBar.connectAction.setText("Disconnect");
			menuBar.connectAction.setToolTip("Disconnect from Evernote");
			menuBar.synchronizeAction.setEnabled(true);
		}
		logger.log(logger.HIGH, "Leaving NeverNote.setupConnectionMenuOptions");
    }
    
    
    
    //***************************************************************
    //***************************************************************
    //** These functions deal with the GUI Attribute tree
    //***************************************************************
    //***************************************************************    
    @SuppressWarnings("unused")
	private void attributeTreeClicked(QTreeWidgetItem item, Integer integer) {
 	
    	clearTagFilter();
    	clearNotebookFilter();
    	clearTrashFilter();
    	clearSavedSearchFilter();

    	if (attributeTreeSelected == null || item.nativeId() != attributeTreeSelected.nativeId()) {
    		if (item.childCount() > 0) {
    			item.setSelected(false);
    		} else {
    	       	Global.createdBeforeFilter.reset();
    	       	Global.createdSinceFilter.reset();
    	       	Global.changedBeforeFilter.reset();
    	       	Global.changedSinceFilter.reset();
    	       	Global.containsFilter.reset();
    			attributeTreeSelected = item;
    			DateAttributeFilterTable f = null;
    			f = findDateAttributeFilterTable(item.parent());
    			if (f!=null)
    				f.select(item.text(0));
    			else {
    				String text = item.text(0);
    				Global.containsFilter.select(text);
    			}
    		}
    		listManager.loadNotesIndex();
    		noteIndexUpdated(false);
    		return;
    	}
   		attributeTreeSelected = null;
   		item.setSelected(false);
       	Global.createdBeforeFilter.reset();
       	Global.createdSinceFilter.reset();
       	Global.changedBeforeFilter.reset();
       	Global.changedSinceFilter.reset();
       	Global.containsFilter.reset();
       	listManager.loadNotesIndex();
   		noteIndexUpdated(false); 
    }
    // This determines what attribute filter we need, depending upon the selection
    private DateAttributeFilterTable findDateAttributeFilterTable(QTreeWidgetItem w) {
		if (w.parent() != null && w.childCount() > 0) {
			QTreeWidgetItem parent = w.parent();
			if (parent.text(0).equalsIgnoreCase("created") && 
				w.text(0).equalsIgnoreCase("since"))
					return Global.createdSinceFilter;
			if (parent.text(0).equalsIgnoreCase("created") && 
    			w.text(0).equalsIgnoreCase("before"))
    					return Global.createdBeforeFilter;
			if (parent.text(0).equalsIgnoreCase("last modified") && 
    			w.text(0).equalsIgnoreCase("since"))
    					return Global.changedSinceFilter;
    		if (parent.text(0).equalsIgnoreCase("last modified") && 
        		w.text(0).equalsIgnoreCase("before"))
        					return Global.changedBeforeFilter;
		}
		return null;
    }
    // Show/Hide attribute search window
	private void toggleAttributesWindow() {
		logger.log(logger.HIGH, "Entering NeverNote.toggleAttributesWindow");
    	if (attributeTree.isVisible())
    		attributeTree.hide();
    	else
    		attributeTree.show();
    	menuBar.hideAttributes.setChecked(attributeTree.isVisible());
    	
		Global.saveWindowVisible("attributeTree", attributeTree.isVisible());
    	logger.log(logger.HIGH, "Leaving NeverNote.toggleAttributeWindow");
    }    
	private void clearAttributeFilter() {
       	Global.createdBeforeFilter.reset();
       	Global.createdSinceFilter.reset();
       	Global.changedBeforeFilter.reset();
       	Global.changedSinceFilter.reset();
       	Global.containsFilter.reset();
       	attributeTreeSelected = null;
		attributeTree.blockSignals(true);
		attributeTree.clearSelection();
		attributeTree.blockSignals(false);
	}
    
	
    //***************************************************************
    //***************************************************************
    //** These functions deal with the GUI Note index table
    //***************************************************************
    //***************************************************************    
    // Initialize the note list table
	private void initializeNoteTable() {
		logger.log(logger.HIGH, "Entering NeverNote.initializeNoteTable");
		noteTableView.setSelectionMode(QAbstractItemView.SelectionMode.ExtendedSelection);
		noteTableView.selectionModel().selectionChanged.connect(this, "noteTableSelection()");
		logger.log(logger.HIGH, "Leaving NeverNote.initializeNoteTable");
	}	
    // Show/Hide trash window
	@SuppressWarnings("unused")
	private void toggleNoteListWindow() {
		logger.log(logger.HIGH, "Entering NeverNote.toggleNoteListWindow");
    	if (noteTableView.isVisible())
    		noteTableView.hide();
    	else
    		noteTableView.show();
    	menuBar.hideNoteList.setChecked(noteTableView.isVisible());
    	
		Global.saveWindowVisible("noteList", noteTableView.isVisible());
    	logger.log(logger.HIGH, "Leaving NeverNote.toggleNoteListWindow");
    }   
	// Handle the event that a user selects a note from the table
    @SuppressWarnings("unused")
	private void noteTableSelection() {
		logger.log(logger.HIGH, "Entering NeverNote.noteTableSelection");
		saveNote();
		if (historyGuids.size() == 0) {
			historyGuids.add(currentNoteGuid);
			historyPosition = 1;
		}
    	noteTableView.showColumn(Global.noteTableGuidPosition);
    	
    	List<QModelIndex> selections = noteTableView.selectionModel().selectedRows();
    	noteTableView.hideColumn(Global.noteTableGuidPosition);
    	
    	if (selections.size() > 0) {
    		QModelIndex index;
    		menuBar.noteDuplicateAction.setEnabled(true);
    		menuBar.noteOnlineHistoryAction.setEnabled(true);
    		menuBar.noteMergeAction.setEnabled(true);
    		selectedNoteGUIDs.clear();
    		if (selections.size() != 1 || Global.showDeleted) {
    			menuBar.noteDuplicateAction.setEnabled(false);
    		}
    		if (selections.size() != 1 || !Global.isConnected) {
    			menuBar.noteOnlineHistoryAction.setEnabled(false);
    		}
    		if (selections.size() == 1) {
    			menuBar.noteMergeAction.setEnabled(false);
    		}
    		for (int i=0; i<selections.size(); i++) {
    			int row = selections.get(i).row();
    			if (row == 0) 
    				upButton.setEnabled(false);
    			else
    				upButton.setEnabled(true);
    			if (row < noteTableView.model.rowCount()-1)
    				downButton.setEnabled(true);
    			else
    				downButton.setEnabled(false);
    			index = noteTableView.proxyModel.index(row, Global.noteTableGuidPosition);
    			SortedMap<Integer, Object> ix = noteTableView.proxyModel.itemData(index);
        		currentNoteGuid = (String)ix.values().toArray()[0];
        		selectedNoteGUIDs.add(currentNoteGuid);
    		}
    	}
    	
    	nextButton.setEnabled(true);
   		prevButton.setEnabled(true);
    	if (!fromHistory) {
    		int endPosition = historyGuids.size()-1;
    		for (int j=historyPosition; j<=endPosition; j++) {
    			historyGuids.remove(historyGuids.size()-1);
    		}
    		historyGuids.add(currentNoteGuid);
    		historyPosition = historyGuids.size();
    	} 
    	if (historyPosition <= 1)
    		prevButton.setEnabled(false);
    	if (historyPosition == historyGuids.size())
    		nextButton.setEnabled(false);
    	    	
    	fromHistory = false;
    	scrollToGuid(currentNoteGuid);
    	refreshEvernoteNote(true);
		logger.log(logger.HIGH, "Leaving NeverNote.noteTableSelection");
    }    
	// Trigger a refresh when the note db has been updated
	private void noteIndexUpdated(boolean reload) {
		logger.log(logger.HIGH, "Entering NeverNote.noteIndexUpdated");
		Global.traceReset();  
		saveNote();
    	refreshEvernoteNoteList();
    	logger.log(logger.HIGH, "Calling note table reload in NeverNote.noteIndexUpdated() - "+reload);
    	noteTableView.load(listManager, reload);
    	scrollToGuid(currentNoteGuid);
		logger.log(logger.HIGH, "Leaving NeverNote.noteIndexUpdated");
    }
	// Called when the list of notes is updated
    private void refreshEvernoteNoteList() {
    	logger.log(logger.HIGH, "Entering NeverNote.refreshEvernoteNoteList");
    	browserWindow.setDisabled(false);
		if (selectedNoteGUIDs == null)
			selectedNoteGUIDs = new ArrayList<String>();
		selectedNoteGUIDs.clear();  // clear out old entries
		
		String saveCurrentNoteGuid = new String();
		String tempNoteGuid = new String();
				
		historyGuids.clear();
		historyPosition = 0;
		prevButton.setEnabled(false);
		nextButton.setEnabled(false);
		
		if (currentNoteGuid == null) 
			currentNoteGuid = new String();
		
		for (Note note : listManager.getNoteIndex()) {
			tempNoteGuid = note.getGuid();
			if (currentNoteGuid.equals(tempNoteGuid)) {
				saveCurrentNoteGuid = new String(tempNoteGuid);
			}
		}
		
		if (listManager.getNoteIndex().size() == 0) {
			currentNoteGuid = "";
			currentNote = null;
			browserWindow.clear();
			browserWindow.setDisabled(true);
		} 
		
		if (saveCurrentNoteGuid.equals("") && listManager.getNoteIndex().size() >0) {
			currentNoteGuid = listManager.getNoteIndex().get(listManager.getNoteIndex().size()-1).getGuid();
			currentNote = listManager.getNoteIndex().get(listManager.getNoteIndex().size()-1);
			refreshEvernoteNote(true);
		} else {
			refreshEvernoteNote(false);
		}
		reloadTagTree();

		logger.log(logger.HIGH, "Leaving NeverNote.refreshEvernoteNoteList");
	} 
    // Called when the previous arrow button is clicked 
    @SuppressWarnings("unused")
	private void previousViewedAction() {
    	if (!prevButton.isEnabled())
    		return;
    	if (historyPosition == 0)
    		return;
   		historyPosition--;
    	if (historyPosition <= 0)
    		return;
        String historyGuid = historyGuids.get(historyPosition-1);
        fromHistory = true;
    	for (int i=0; i<noteTableView.model().rowCount(); i++) {
    		QModelIndex modelIndex =  noteTableView.model().index(i, Global.noteTableGuidPosition);
    		if (modelIndex != null) {
    			SortedMap<Integer, Object> ix = noteTableView.model().itemData(modelIndex);
    			String tableGuid =  (String)ix.values().toArray()[0];
    			if (tableGuid.equals(historyGuid)) {
    				noteTableView.selectRow(i);
    				return;
    			}	
    		}
    	}
    }
    @SuppressWarnings("unused")
	private void nextViewedAction() {
    	if (!nextButton.isEnabled())
    		return;
        String historyGuid = historyGuids.get(historyPosition);
        historyPosition++;
        fromHistory = true;
    	for (int i=0; i<noteTableView.model().rowCount(); i++) {
    		QModelIndex modelIndex =  noteTableView.model().index(i, Global.noteTableGuidPosition);
    		if (modelIndex != null) {
    			SortedMap<Integer, Object> ix = noteTableView.model().itemData(modelIndex);
    			String tableGuid =  (String)ix.values().toArray()[0];
    			if (tableGuid.equals(historyGuid)) {
    				noteTableView.selectRow(i);
    				return;
    			}	
    		}
    	}    	
    }
    // Called when the up arrow is clicked 
    @SuppressWarnings("unused")
	private void upAction() {
    	List<QModelIndex> selections = noteTableView.selectionModel().selectedRows();
    	int row = selections.get(0).row();
    	if (row > 0) {
    		noteTableView.selectRow(row-1);
    	}
    }
    // Called when the down arrow is clicked 
    @SuppressWarnings("unused")
	private void downAction() {
    	List<QModelIndex> selections = noteTableView.selectionModel().selectedRows();
    	int row = selections.get(0).row();
    	int max = noteTableView.model.rowCount();
    	if (row < max-1) {
    		noteTableView.selectRow(row+1);
    	}
    }
    // Update a tag string for a specific note in the list
    @SuppressWarnings("unused")
	private void updateListTags(String guid, List<String> tags) {
    	logger.log(logger.HIGH, "Entering NeverNote.updateListTags");
    	StringBuffer tagBuffer = new StringBuffer();
    	for (int i=0; i<tags.size(); i++) {
    		tagBuffer.append(tags.get(i));
    		if (i<tags.size()-1)
    			tagBuffer.append(", ");
    	}
    	
    	for (int i=0; i<noteTableView.model.rowCount(); i++) {
    		QModelIndex modelIndex =  noteTableView.model.index(i, Global.noteTableGuidPosition);
    		if (modelIndex != null) {
    			SortedMap<Integer, Object> ix = noteTableView.model.itemData(modelIndex);
    			String tableGuid =  (String)ix.values().toArray()[0];
    			if (tableGuid.equals(guid)) {
    				noteTableView.model.setData(i, Global.noteTableTagPosition,tagBuffer.toString());
    				noteTableView.model.setData(i, Global.noteTableSynchronizedPosition, "false");
    				return;
    			}
    		}
    	}
    	logger.log(logger.HIGH, "Leaving NeverNote.updateListTags");
    }
    // Update a title for a specific note in the list
    @SuppressWarnings("unused")
	private void updateListTitle(String guid, String title) {
    	logger.log(logger.HIGH, "Entering NeverNote.updateListTitle");

    	for (int i=0; i<noteTableView.model.rowCount(); i++) {
    		//QModelIndex modelIndex =  noteTableView.proxyModel.index(i, Global.noteTableGuidPosition);
    		QModelIndex modelIndex =  noteTableView.model.index(i, Global.noteTableGuidPosition);
    		if (modelIndex != null) {
//    			SortedMap<Integer, Object> ix = noteTableView.proxyModel.itemData(modelIndex);
    			SortedMap<Integer, Object> ix = noteTableView.model.itemData(modelIndex);
    			String tableGuid =  (String)ix.values().toArray()[0];
    			if (tableGuid.equals(guid)) {
    				noteTableView.model.setData(i, Global.noteTableTitlePosition,title);
    				noteTableView.model.setData(i, Global.noteTableSynchronizedPosition, "false");
    				return;
    			}	
    		}
    	}
    	logger.log(logger.HIGH, "Leaving NeverNote.updateListTitle");
    }
    // Update a title for a specific note in the list
    @SuppressWarnings("unused")
	private void updateListAuthor(String guid, String author) {
    	logger.log(logger.HIGH, "Entering NeverNote.updateListAuthor");

    	for (int i=0; i<noteTableView.model.rowCount(); i++) {
    		//QModelIndex modelIndex =  noteTableView.proxyModel.index(i, Global.noteTableGuidPosition);
    		QModelIndex modelIndex =  noteTableView.model.index(i, Global.noteTableGuidPosition);
    		if (modelIndex != null) {
//    			SortedMap<Integer, Object> ix = noteTableView.proxyModel.itemData(modelIndex);
    			SortedMap<Integer, Object> ix = noteTableView.model.itemData(modelIndex);
    			String tableGuid =  (String)ix.values().toArray()[0];
    			if (tableGuid.equals(guid)) {
    				noteTableView.model.setData(i, Global.noteTableAuthorPosition,author);
    				noteTableView.model.setData(i, Global.noteTableSynchronizedPosition, "false");
    				return;
    			}	
    		}
    	}
    	logger.log(logger.HIGH, "Leaving NeverNote.updateListAuthor");
    }
	private void updateListNoteNotebook(String guid, String notebook) {
    	logger.log(logger.HIGH, "Entering NeverNote.updateListAuthor");

    	for (int i=0; i<noteTableView.model.rowCount(); i++) {
    		//QModelIndex modelIndex =  noteTableView.proxyModel.index(i, Global.noteTableGuidPosition);
    		QModelIndex modelIndex =  noteTableView.model.index(i, Global.noteTableGuidPosition);
    		if (modelIndex != null) {
//    			SortedMap<Integer, Object> ix = noteTableView.proxyModel.itemData(modelIndex);
    			SortedMap<Integer, Object> ix = noteTableView.model.itemData(modelIndex);
    			String tableGuid =  (String)ix.values().toArray()[0];
    			if (tableGuid.equals(guid)) {
    				noteTableView.model.setData(i, Global.noteTableNotebookPosition,notebook);
    				noteTableView.model.setData(i, Global.noteTableSynchronizedPosition, "false");
    				return;
    			}	
    		}
    	}
    	logger.log(logger.HIGH, "Leaving NeverNote.updateListAuthor");
    }
    // Update a title for a specific note in the list
    @SuppressWarnings("unused")
	private void updateListSourceUrl(String guid, String url) {
    	logger.log(logger.HIGH, "Entering NeverNote.updateListAuthor");

    	for (int i=0; i<noteTableView.model.rowCount(); i++) {
    		//QModelIndex modelIndex =  noteTableView.proxyModel.index(i, Global.noteTableGuidPosition);
    		QModelIndex modelIndex =  noteTableView.model.index(i, Global.noteTableGuidPosition);
    		if (modelIndex != null) {
//    			SortedMap<Integer, Object> ix = noteTableView.proxyModel.itemData(modelIndex);
    			SortedMap<Integer, Object> ix = noteTableView.model.itemData(modelIndex);
    			String tableGuid =  (String)ix.values().toArray()[0];
    			if (tableGuid.equals(guid)) {
    				noteTableView.model.setData(i, Global.noteTableSynchronizedPosition, "false");
    				noteTableView.model.setData(i, Global.noteTableSourceUrlPosition,url);
    				return;
    			}	
    		}
    	}
    	logger.log(logger.HIGH, "Leaving NeverNote.updateListAuthor");
    }
	private void updateListGuid(String oldGuid, String newGuid) {
    	logger.log(logger.HIGH, "Entering NeverNote.updateListTitle");

    	for (int i=0; i<noteTableView.model.rowCount(); i++) {
    		QModelIndex modelIndex =  noteTableView.model.index(i, Global.noteTableGuidPosition);
    		if (modelIndex != null) {
    			SortedMap<Integer, Object> ix = noteTableView.model.itemData(modelIndex);
    			String tableGuid =  (String)ix.values().toArray()[0];
    			if (tableGuid.equals(oldGuid)) {
    				noteTableView.model.setData(i, Global.noteTableGuidPosition,newGuid);
    				//noteTableView.model.setData(i, Global.noteTableSynchronizedPosition, "false");
    				return;
    			}	
    		}
    	}
    	logger.log(logger.HIGH, "Leaving NeverNote.updateListTitle");
    }
	private void updateListTagName(String guid) {
    	logger.log(logger.HIGH, "Entering NeverNote.updateTagName");
		
		for (int j=0; j<listManager.getNoteIndex().size(); j++) {
			if (listManager.getNoteIndex().get(j).getTagGuids().contains(guid)) {
				String newName = listManager.getTagNamesForNote(listManager.getNoteIndex().get(j));

				for (int i=0; i<noteTableView.model.rowCount(); i++) {
					QModelIndex modelIndex =  noteTableView.model.index(i, Global.noteTableGuidPosition);
					if (modelIndex != null) {
						SortedMap<Integer, Object> ix = noteTableView.model.itemData(modelIndex);
						String noteGuid = (String)ix.values().toArray()[0];
						if (noteGuid.equalsIgnoreCase(listManager.getNoteIndex().get(j).getGuid())) {
							noteTableView.model.setData(i, Global.noteTableTagPosition, newName);
							//noteTableView.model.setData(i, Global.noteTableSynchronizedPosition, "false");
							i=noteTableView.model.rowCount();
						}
					}
				}
			}
		}	
    	logger.log(logger.HIGH, "Leaving NeverNote.updateListNotebook");
    }
	private void removeListTagName(String guid) {
    	logger.log(logger.HIGH, "Entering NeverNote.updateTagName");
		
		for (int j=0; j<listManager.getNoteIndex().size(); j++) {
			if (listManager.getNoteIndex().get(j).getTagGuids().contains(guid)) {
				for (int i=listManager.getNoteIndex().get(j).getTagGuids().size()-1; i>=0; i--) {
					if (listManager.getNoteIndex().get(j).getTagGuids().get(i).equals(guid))
						listManager.getNoteIndex().get(j).getTagGuids().remove(i);
				}
				
				String newName = listManager.getTagNamesForNote(listManager.getNoteIndex().get(j));
				for (int i=0; i<noteTableView.model.rowCount(); i++) {
					QModelIndex modelIndex =  noteTableView.model.index(i, Global.noteTableGuidPosition);
					if (modelIndex != null) {
						SortedMap<Integer, Object> ix = noteTableView.model.itemData(modelIndex);
						String noteGuid = (String)ix.values().toArray()[0];
						if (noteGuid.equalsIgnoreCase(listManager.getNoteIndex().get(j).getGuid())) {
							noteTableView.model.setData(i, Global.noteTableTagPosition, newName);
//							noteTableView.model.setData(i, Global.noteTableSynchronizedPosition, "false");
							i=noteTableView.model.rowCount();
						}
					}
				}
			}
		}	
    	logger.log(logger.HIGH, "Leaving NeverNote.updateListNotebook");
    }
    private void updateListNotebookName(String oldName, String newName) {
    	logger.log(logger.HIGH, "Entering NeverNote.updateListNotebookName");

    	for (int i=0; i<noteTableView.model.rowCount(); i++) {
    		QModelIndex modelIndex =  noteTableView.model.index(i, Global.noteTableNotebookPosition); 
    		if (modelIndex != null) {
    			SortedMap<Integer, Object> ix = noteTableView.model.itemData(modelIndex);
    			String tableName =  (String)ix.values().toArray()[0];
    			if (tableName.equalsIgnoreCase(oldName)) {
//    				noteTableView.model.setData(i, Global.noteTableSynchronizedPosition, "false");
    				noteTableView.model.setData(i, Global.noteTableNotebookPosition, newName);
    			}
    		}
    	}
    	logger.log(logger.HIGH, "Leaving NeverNote.updateListNotebookName");
    }
    @SuppressWarnings("unused")
	private void updateListDateCreated(String guid, QDateTime date) {
    	logger.log(logger.HIGH, "Entering NeverNote.updateListDateCreated");

    	for (int i=0; i<noteTableView.model.rowCount(); i++) {
    		QModelIndex modelIndex =  noteTableView.model.index(i, Global.noteTableGuidPosition);
    		if (modelIndex != null) {
    			SortedMap<Integer, Object> ix = noteTableView.model.itemData(modelIndex);
    			String tableGuid =  (String)ix.values().toArray()[0];
    			if (tableGuid.equals(guid)) {
    				noteTableView.model.setData(i, Global.noteTableCreationPosition, date.toString(Global.getDateFormat()+" " +Global.getTimeFormat()));
    				return;
    			}
    		}
    	}
    	logger.log(logger.HIGH, "Leaving NeverNote.updateListDateCreated");
    }
    @SuppressWarnings("unused")
	private void updateListDateSubject(String guid, QDateTime date) {
    	logger.log(logger.HIGH, "Entering NeverNote.updateListDateSubject");

    	for (int i=0; i<noteTableView.model.rowCount(); i++) {
    		QModelIndex modelIndex =  noteTableView.model.index(i, Global.noteTableGuidPosition);
    		if (modelIndex != null) {
    			SortedMap<Integer, Object> ix = noteTableView.model.itemData(modelIndex);
    			String tableGuid =  (String)ix.values().toArray()[0];
    			if (tableGuid.equals(guid)) {
    				noteTableView.model.setData(i, Global.noteTableSynchronizedPosition, "false");
    				noteTableView.model.setData(i, Global.noteTableSubjectDatePosition, date.toString(Global.getDateFormat()+" " +Global.getTimeFormat()));
    				return;
    			}
    		}
    	}
    	logger.log(logger.HIGH, "Leaving NeverNote.updateListDateCreated");
    }
    @SuppressWarnings("unused")
	private void updateListDateChanged(String guid, QDateTime date) {
    	logger.log(logger.HIGH, "Entering NeverNote.updateListDateChanged");

    	for (int i=0; i<noteTableView.model.rowCount(); i++) {
    		QModelIndex modelIndex =  noteTableView.model.index(i, Global.noteTableGuidPosition);
    		if (modelIndex != null) {
    			SortedMap<Integer, Object> ix = noteTableView.model.itemData(modelIndex);
    			String tableGuid =  (String)ix.values().toArray()[0];
    			if (tableGuid.equals(guid)) {
    				noteTableView.model.setData(i, Global.noteTableSynchronizedPosition, "false");
    				noteTableView.model.setData(i, Global.noteTableChangedPosition, date.toString(Global.getDateFormat()+" " +Global.getTimeFormat()));
    				return;
    			}
    		}
    	}
    	logger.log(logger.HIGH, "Leaving NeverNote.updateListDateChanged");
    }
    private void updateListDateChanged() {
    	logger.log(logger.HIGH, "Entering NeverNote.updateListDateChanged");
   	QDateTime date = new QDateTime(QDateTime.currentDateTime());
    	for (int i=0; i<noteTableView.model.rowCount(); i++) {
    		QModelIndex modelIndex =  noteTableView.model.index(i, Global.noteTableGuidPosition);
    		if (modelIndex != null) {
    			SortedMap<Integer, Object> ix = noteTableView.model.itemData(modelIndex);
    			String tableGuid =  (String)ix.values().toArray()[0];
    			if (tableGuid.equals(currentNoteGuid)) {
    				noteTableView.model.setData(i, Global.noteTableSynchronizedPosition, "false");
    				noteTableView.model.setData(i, Global.noteTableChangedPosition, date.toString(Global.getDateFormat()+" " +Global.getTimeFormat()));
    				return;
    			}
    		}
    	}
    	logger.log(logger.HIGH, "Leaving NeverNote.updateListDateChanged");
    }  
    // Redo scroll
    @SuppressWarnings("unused")
	private void scrollToCurrentGuid() {
    	//scrollToGuid(currentNoteGuid);
    	List<QModelIndex> selections = noteTableView.selectionModel().selectedRows();
    	if (selections.size() == 0)
    		return;
    	QModelIndex index = selections.get(0);
    	int row = selections.get(0).row();
    	String guid = (String)index.model().index(row, Global.noteTableGuidPosition).data();
    	scrollToGuid(guid);
    }
    // Scroll to a particular index item
    private void scrollToGuid(String guid) {
    	if (currentNote == null || guid == null) 
    		return;
    	if (currentNote.isActive() && Global.showDeleted) {
    		for (int i=0; i<listManager.getNoteIndex().size(); i++) {
    			if (!listManager.getNoteIndex().get(i).isActive()) {
    				currentNote = listManager.getNoteIndex().get(i);
    				currentNoteGuid =  currentNote.getGuid();
    				i = listManager.getNoteIndex().size();
    			}
    		}
    	}
    	
    	if (!currentNote.isActive() && !Global.showDeleted) {
    		for (int i=0; i<listManager.getNoteIndex().size(); i++) {
    			if (listManager.getNoteIndex().get(i).isActive()) {
    				currentNote = listManager.getNoteIndex().get(i);
    				currentNoteGuid =  currentNote.getGuid();
    				i = listManager.getNoteIndex().size();
    			}
    		}
    	}
    	
    	QModelIndex index; 
    	for (int i=0; i<noteTableView.model().rowCount(); i++) {
    		index = noteTableView.model().index(i, Global.noteTableGuidPosition);
    		if (currentNoteGuid.equals(index.data())) {
//    			noteTableView.setCurrentIndex(index);
       			noteTableView.selectRow(i);
    			noteTableView.scrollTo(index, ScrollHint.EnsureVisible);  // This should work, but it doesn't
   	  			i=noteTableView.model.rowCount();
     		}
      	}
    }
    // Show/Hide columns
    private void showColumns() {
   		noteTableView.setColumnHidden(Global.noteTableCreationPosition, !Global.isColumnVisible("dateCreated"));
   		noteTableView.setColumnHidden(Global.noteTableChangedPosition, !Global.isColumnVisible("dateChanged"));
   		noteTableView.setColumnHidden(Global.noteTableSubjectDatePosition, !Global.isColumnVisible("dateSubject"));
   		noteTableView.setColumnHidden(Global.noteTableAuthorPosition, !Global.isColumnVisible("author"));
   		noteTableView.setColumnHidden(Global.noteTableSourceUrlPosition, !Global.isColumnVisible("sourceUrl"));
   		noteTableView.setColumnHidden(Global.noteTableTagPosition, !Global.isColumnVisible("tags"));
   		noteTableView.setColumnHidden(Global.noteTableNotebookPosition, !Global.isColumnVisible("notebook"));
   		noteTableView.setColumnHidden(Global.noteTableSynchronizedPosition, !Global.isColumnVisible("synchronized"));
    }
    // Open a separate window
    @SuppressWarnings("unused")
	private void listDoubleClick() {

    }
    // Title color has changed
    @SuppressWarnings("unused")
	private void titleColorChanged(Integer color) {
    	logger.log(logger.HIGH, "Entering NeverNote.updateListAuthor");

    	QColor backgroundColor = new QColor();
		QColor foregroundColor = new QColor(QColor.black);
		backgroundColor.setRgb(color);
		
		if (backgroundColor.rgb() == QColor.black.rgb() || backgroundColor.rgb() == QColor.blue.rgb())
			foregroundColor.setRgb(QColor.white.rgb());
    	
		if (selectedNoteGUIDs.size() == 0)
			selectedNoteGUIDs.add(currentNoteGuid);
		
    	for (int j=0; j<selectedNoteGUIDs.size(); j++) {
    		for (int i=0; i<noteTableView.model.rowCount(); i++) {
    			QModelIndex modelIndex =  noteTableView.model.index(i, Global.noteTableGuidPosition);
    			if (modelIndex != null) {
    				SortedMap<Integer, Object> ix = noteTableView.model.itemData(modelIndex);
    				String tableGuid =  (String)ix.values().toArray()[0];
    				if (tableGuid.equals(selectedNoteGUIDs.get(j))) {
    					for (int k=0; k<Global.noteTableColumnCount; k++) {
    						noteTableView.model.setData(i, k, backgroundColor, Qt.ItemDataRole.BackgroundRole);
    						noteTableView.model.setData(i, k, foregroundColor, Qt.ItemDataRole.ForegroundRole);
    						listManager.updateNoteTitleColor(selectedNoteGUIDs.get(j), backgroundColor.rgb());
    					}
    					i=noteTableView.model.rowCount();
    				}
    			}
    		}
    	}
    	logger.log(logger.HIGH, "Leaving NeverNote.updateListAuthor");
    }
    
    
    //***************************************************************
    //***************************************************************
    //** These functions deal with Note specific things
    //***************************************************************
    //***************************************************************    
    @SuppressWarnings("unused")
	private void setNoteDirty() {
		logger.log(logger.EXTREME, "Entering NeverNote.setNoteDirty()");
    	noteDirty = true;

    	listManager.getUnsynchronizedNotes().add(currentNoteGuid);
    	for (int i=0; i<noteTableView.model.rowCount(); i++) {
    		QModelIndex modelIndex =  noteTableView.model.index(i, Global.noteTableGuidPosition);
    		if (modelIndex != null) {
    			SortedMap<Integer, Object> ix = noteTableView.model.itemData(modelIndex);
    			String tableGuid =  (String)ix.values().toArray()[0];
    			if (tableGuid.equals(currentNoteGuid)) {
    				noteTableView.model.setData(i, Global.noteTableSynchronizedPosition, "false");
    				return;
    			}
    		}
    	}
		logger.log(logger.EXTREME, "Leaving NeverNote.setNoteDirty()");
    }
    private void saveNote() {
		logger.log(logger.EXTREME, "Inside NeverNote.saveNote()");
    	if (noteDirty) {
			logger.log(logger.EXTREME, "Note is dirty.");
    		waitCursor(true);
  		
			preview = new Thumbnailer(currentNoteGuid, new QSize(1024,768));
			preview.finished.connect(this, "saveThumbnail(String)");
			preview.setContent(browserWindow.getContent());
    		
			logger.log(logger.EXTREME, "Saving to cache");
			QTextCodec codec = QTextCodec.codecForLocale();
//	        QTextDecoder decoder = codec.makeDecoder();
			codec = QTextCodec.codecForName("UTF-8");
	        QByteArray unicode =  codec.fromUnicode(browserWindow.getContent());
    		noteCache.put(currentNoteGuid, unicode.toString());
			
    		logger.log(logger.EXTREME, "updating list manager");
    		listManager.updateNoteContent(currentNoteGuid, browserWindow.getContent());
			logger.log(logger.EXTREME, "Updating title");
    		listManager.updateNoteTitle(currentNoteGuid, browserWindow.getTitle());
    		updateListDateChanged();

			logger.log(logger.EXTREME, "Looking through note index for refreshed note");
    		for (int i=0; i<listManager.getNoteIndex().size(); i++) {
    			if (listManager.getNoteIndex().get(i).getGuid().equals(currentNoteGuid)) {
    				currentNote = listManager.getNoteIndex().get(i);
    				i = listManager.getNoteIndex().size();
    			}
    		}
    		noteDirty = false;
    		waitCursor(false);
    	}
    }
    // Get a note from Evernote (and put it in the browser)
	private void refreshEvernoteNote(boolean reload) {
		logger.log(logger.HIGH, "Entering NeverNote.refreshEvernoteNote");
		if (Global.getDisableViewing()) {
			browserWindow.setEnabled(false);
			return;
		}
		inkNote = false;
		if (!Global.showDeleted)
			browserWindow.setReadOnly(false);
		Global.cryptCounter =0;
		if (currentNoteGuid.equals("")) {
			browserWindow.setReadOnly(true);
			return;
		}
		if (!reload)
			return;
		
		waitCursor(true);
		browserWindow.loadingData(true);

		currentNote = conn.getNoteTable().getNote(currentNoteGuid, true,true,false,false,true);
		if (currentNote == null) 
			return;

		if (!noteCache.containsKey(currentNoteGuid) || conn.getNoteTable().isThumbnailNeeded(currentNoteGuid)) {
			QByteArray js = new QByteArray();
			// We need to prepend the note with <HEAD></HEAD> or encoded characters are ugly 
			js.append("<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">");		
			js.append("<style type=\"text/css\">en-crypt-temp { border-style:solid; border-color:blue; padding:0.5mm 0.5mm 0.5mm 0.5mm; }</style>");
			js.append("<style type=\"text/css\">en-hilight { background-color: rgb(255,255,0) }</style>");
			js.append("<style type=\"text/css\">en-spell { text-decoration: none; border-bottom: dotted 1px #cc0000; }</style>");
			js.append("</head>");
			js.append(rebuildNoteHTML(currentNoteGuid, currentNote.getContent()));
			js.append("</HTML>");
			js.replace("<!DOCTYPE en-note SYSTEM 'http://xml.evernote.com/pub/enml.dtd'>", "");
			js.replace("<!DOCTYPE en-note SYSTEM 'http://xml.evernote.com/pub/enml2.dtd'>", "");
			js.replace("<?xml version='1.0' encoding='UTF-8'?>", "");
			browserWindow.getBrowser().setContent(js);
			noteCache.put(currentNoteGuid, js.toString());
			if (conn.getNoteTable().isThumbnailNeeded(currentNoteGuid)) {
				preview = new Thumbnailer(currentNoteGuid, new QSize(1024,768));
				preview.finished.connect(this, "saveThumbnail(String)");
				preview.setContent(js.toString());
			}
		} else {
			logger.log(logger.HIGH, "Note content is being pulled from the cache");
			String cachedContent = modifyCachedTodoTags(noteCache.get(currentNoteGuid));
			browserWindow.getBrowser().setContent(new QByteArray(cachedContent));
		}
		
		browserWindow.getBrowser().page().setContentEditable(!inkNote);  // We don't allow editing of ink notes
		browserWindow.setNote(currentNote);
		
		// Build a list of non-closed notebooks
		List<Notebook> nbooks = new ArrayList<Notebook>();
		for (int i=0; i<listManager.getNotebookIndex().size(); i++) {
			boolean found=false;
			for (int j=0; j<listManager.getArchiveNotebookIndex().size(); j++) {
				if (listManager.getArchiveNotebookIndex().get(j).getGuid().equals(listManager.getNotebookIndex().get(i).getGuid())) 
					found = true;
			}
			if (!found)
				nbooks.add(listManager.getNotebookIndex().get(i));
		}
		
		browserWindow.setNotebookList(nbooks);
		browserWindow.setTitle(currentNote.getTitle());
		browserWindow.setTag(getTagNamesForNote(currentNote));
		browserWindow.setAuthor(currentNote.getAttributes().getAuthor());
		
		browserWindow.setAltered(currentNote.getUpdated());
		browserWindow.setCreation(currentNote.getCreated());
		if (currentNote.getAttributes().getSubjectDate() > 0)
			browserWindow.setSubjectDate(currentNote.getAttributes().getSubjectDate());
		else
			browserWindow.setSubjectDate(currentNote.getCreated());
		browserWindow.setUrl(currentNote.getAttributes().getSourceURL());
		browserWindow.setAllTags(listManager.getTagIndex());
		browserWindow.setCurrentTags(currentNote.getTagNames());
		noteDirty = false;
		scrollToGuid(currentNoteGuid);
		
		browserWindow.loadingData(false);
		if (thumbnailViewer.isActiveWindow())
			thumbnailView();
		waitCursor(false);
		logger.log(logger.HIGH, "Leaving NeverNote.refreshEvernoteNote");
	}
	// Save a generated thumbnail
	@SuppressWarnings("unused")
	private void saveThumbnail(String guid) {
		QFile tFile = new QFile(Global.currentDir+"res/thumbnail-"+guid+".png");
		tFile.open(OpenModeFlag.ReadOnly);
		QByteArray imgBytes = tFile.readAll();
		tFile.close();
		conn.getNoteTable().setThumbnail(guid, imgBytes);
		conn.getNoteTable().setThumbnailNeeded(guid, false);
		thumbnailViewer.setThumbnail(QImage.fromData(imgBytes));
		if (thumbnailViewer.isVisible()) 
			thumbnailViewer.showFullScreen();
		
		/*		
  		QByteArray img2 = new QByteArray(conn.getNoteTable().getThumbnail(guid));
		QFile file = new QFile(Global.currentDir+"res/aaaa.png");
		file.open(OpenModeFlag.WriteOnly);
		file.write(img2);
		file.close(); 
		*/
	}
    // Show/Hide note information
	@SuppressWarnings("unused")
	private void toggleNoteInformation() {
		logger.log(logger.HIGH, "Entering NeverNote.toggleNoteInformation");
    	browserWindow.toggleInformation();
    	menuBar.noteAttributes.setChecked(browserWindow.isExtended());
    	logger.log(logger.HIGH, "Leaving NeverNote.toggleNoteInformation");
    }
	// Listener triggered when a print button is pressed
    @SuppressWarnings("unused")
	private void printNote() {
		logger.log(logger.HIGH, "Entering NeverNote.printNote");

    	QPrintDialog dialog = new QPrintDialog();
    	if (dialog.exec() == QDialog.DialogCode.Accepted.value()) {
    		QPrinter printer = dialog.printer();
    		browserWindow.getBrowser().print(printer);
    	}
		logger.log(logger.HIGH, "Leaving NeverNote.printNote");

    }
    // Listener triggered when the email button is pressed
    @SuppressWarnings("unused")
	private void emailNote() {
    	logger.log(logger.HIGH, "Entering NeverNote.emailNote");
    	
        if (Desktop.isDesktopSupported()) {
            Desktop desktop = Desktop.getDesktop();
            
            String text2 = browserWindow.getContentsToEmail();
            QUrl url = new QUrl("mailto:");
            url.addQueryItem("subject", currentNote.getTitle());
            url.addQueryItem("body", QUrl.toPercentEncoding(text2).toString());
            QDesktopServices.openUrl(url);
        }
/*            
            
            if (desktop.isSupported(Desktop.Action.MAIL)) {
            	URI uriMailTo = null;
            	try {
            		//String text = browserWindow.getBrowser().page().currentFrame().toPlainText();
            		String text = browserWindow.getContentsToEmail();
            		//text = "<b>" +text +"</b>";
					uriMailTo = new URI("mailto", "&SUBJECT="+currentNote.getTitle()
							+"&BODY=" +text, null);
					uriMailTo = new URI("mailto", "&SUBJECT="+currentNote.getTitle()
							+"&ATTACHMENT=d:/test.pdf", null);
					desktop.mail(uriMailTo);
				} catch (URISyntaxException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}

            }

        }     
 */   	
    	logger.log(logger.HIGH, "Leaving NeverNote.emailNote");
    }
	// Reindex all notes
    @SuppressWarnings("unused")
	private void fullReindex() {
    	logger.log(logger.HIGH, "Entering NeverNote.fullReindex");
    	// If we are deleting non-trash notes
    	if (currentNote.getDeleted() == 0) { 
    		if (QMessageBox.question(this, "Confirmation", "This will cause all notes & attachments to be reindexed, "+
    				"but please be aware that depending upon the size of your database updating all these records " +
    				"can be time consuming and NeverNote will be unresponsive until it is complete.  Do you wish to continue?",
    				QMessageBox.StandardButton.Yes, 
					QMessageBox.StandardButton.No)==StandardButton.No.value() && Global.verifyDelete() == true) {
								return;
    		}
    	}
    	waitCursor(true);
    	setMessage("Marking notes for reindex.");
    	conn.getNoteTable().reindexAllNotes();
    	conn.getNoteTable().noteResourceTable.reindexAll(); 
    	setMessage("Database will be reindexed.");
    	waitCursor(false);
    	logger.log(logger.HIGH, "Leaving NeverNote.fullRefresh");
    }
    // Listener when a user wants to reindex a specific note
    @SuppressWarnings("unused")
	private void reindexNote() {
    	logger.log(logger.HIGH, "Entering NeverNote.reindexNote");
		for (int i=0; i<selectedNoteGUIDs.size(); i++) {
			conn.getNoteTable().setIndexNeeded(selectedNoteGUIDs.get(i), true);
		}
		if (selectedNotebookGUIDs.size() > 1)
			setMessage("Notes will be reindexed.");
		else
			setMessage("Note will be reindexed.");
    	logger.log(logger.HIGH, "Leaving NeverNote.reindexNote");
    }
    // Delete the note
    @SuppressWarnings("unused")
	private void deleteNote() {
    	logger.log(logger.HIGH, "Entering NeverNote.deleteNote");
    	if (currentNote == null) 
    		return;
    	if (currentNoteGuid.equals(""))
    		return;
    	
    	// If we are deleting non-trash notes
    	if (currentNote.isActive()) { 
    		if (Global.verifyDelete()) {
    			if (QMessageBox.question(this, "Confirmation", "Delete selected note(s)?",
    					QMessageBox.StandardButton.Yes, 
    					QMessageBox.StandardButton.No)==StandardButton.No.value() && Global.verifyDelete() == true) {
    					return;
    			}
    		}
    		if (selectedNoteGUIDs.size() == 0 && !currentNoteGuid.equals("")) 
    			selectedNoteGUIDs.add(currentNoteGuid);
    		for (int i=0; i<selectedNoteGUIDs.size(); i++) {
    			listManager.deleteNote(selectedNoteGUIDs.get(i));
    		}
    	} else { 
    		// If we are deleting from the trash.
    		if (Global.verifyDelete()) {
    			if (QMessageBox.question(this, "Confirmation", "Permanently delete selected note(s)?",
    				QMessageBox.StandardButton.Yes, 
					QMessageBox.StandardButton.No)==StandardButton.No.value()) {
    					return;
    			}
    		}
    		if (selectedNoteGUIDs.size() == 0 && !currentNoteGuid.equals("")) 
    			selectedNoteGUIDs.add(currentNoteGuid);
    		for (int i=selectedNoteGUIDs.size()-1; i>=0; i--) {
    			for (int j=noteTableView.model.rowCount()-1; j>=0; j--) {
    	    		QModelIndex modelIndex =  noteTableView.model.index(j, Global.noteTableGuidPosition);
    	    		if (modelIndex != null) {
    	    			SortedMap<Integer, Object> ix = noteTableView.model.itemData(modelIndex);
    	    			String tableGuid =  (String)ix.values().toArray()[0];
    	    			if (tableGuid.equals(selectedNoteGUIDs.get(i))) {
    	    				noteTableView.model.removeRow(j);
    	    				j=-1;
    	    			}
    	    		}
    	    	}
    			listManager.expungeNote(selectedNoteGUIDs.get(i));
    		}
    	}
    	currentNoteGuid = "";
    	listManager.loadNotesIndex();
    	noteIndexUpdated(false);
    	refreshEvernoteNote(true);
    	scrollToGuid(currentNoteGuid);
    	logger.log(logger.HIGH, "Leaving NeverNote.deleteNote");
    }
    // Add a new note
    @SuppressWarnings("unused")
	private void addNote() {
    	logger.log(logger.HIGH, "Inside NeverNote.addNote");
//    	browserWindow.setEnabled(true);
    	browserWindow.setReadOnly(false);
    	saveNote();
    	Calendar currentTime = new GregorianCalendar();
     	String noteString = new String("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
     		"<!DOCTYPE en-note SYSTEM \"http://xml.evernote.com/pub/enml2.dtd\">\n" +
     		"<en-note>\n<br clear=\"none\" /></en-note>");
	
    	Long l = new Long(currentTime.getTimeInMillis());
    	String randint = new String(Long.toString(l));
    	
    	// Find a notebook.  We first look for a selected notebook (the "All Notebooks" one doesn't count).  
    	// Then we look
    	// for the first non-archived notebook.  Finally, if nothing else we 
    	// pick the first notebook in the list.
    	String notebook = null;
    	listManager.getNotebookIndex().get(0).getGuid();
    	List<QTreeWidgetItem> selectedNotebook = notebookTree.selectedItems();
    	if (selectedNotebook.size() > 0 && !selectedNotebook.get(0).text(0).equalsIgnoreCase("All Notebooks")) {
    		QTreeWidgetItem currentSelectedNotebook = selectedNotebook.get(0);
    		notebook = currentSelectedNotebook.text(2);
    	} else {
    		boolean found = false;
    		List<Notebook> goodNotebooks = new ArrayList<Notebook>();
        	for (int i=0; i<listManager.getNotebookIndex().size(); i++) {
        		boolean match = false;
        		for (int j=0; j<listManager.getArchiveNotebookIndex().size(); j++) {
        			if (listManager.getArchiveNotebookIndex().get(j).getGuid().equals(listManager.getNotebookIndex().get(i).getGuid())) {
        				match = true;
        				j = listManager.getArchiveNotebookIndex().size();
        			}
        		}
        		if (!match)
    				goodNotebooks.add(listManager.getNotebookIndex().get(i).deepCopy());
        	}
      		// Now we have a list of good notebooks, so we can look for the default
       		found = false;
       		for (int i=0; i<goodNotebooks.size(); i++) {
       			if (goodNotebooks.get(i).isDefaultNotebook()) {
       				notebook = goodNotebooks.get(i).getGuid();
       				found = true;
       				i = goodNotebooks.size();
       			}
       		}
       		
       		if (goodNotebooks.size() > 0 && !found)
       			notebook = goodNotebooks.get(0).getGuid();
     
        	if (notebook==null)
        		notebook = listManager.getNotebookIndex().get(0).getGuid();    		
    	}
    	
    	Note newNote = new Note();
    	newNote.setUpdateSequenceNum(0);
    	newNote.setGuid(randint);
    	newNote.setNotebookGuid(notebook);
    	newNote.setTitle("");
    	newNote.setContent(noteString);
    	newNote.setDeleted(0);
    	newNote.setCreated(System.currentTimeMillis());
    	newNote.setUpdated(System.currentTimeMillis());
    	newNote.setActive(true);
    	NoteAttributes na = new NoteAttributes();
    	na.setLatitude(0.0);
    	na.setLongitude(0.0);
    	na.setAltitude(0.0);
    	newNote.setAttributes(new NoteAttributes());
    	conn.getNoteTable().addNote(newNote, true);
    	listManager.getUnsynchronizedNotes().add(newNote.getGuid());
    	noteTableView.insertRow(listManager, newNote, true, -1);
    	
    	currentNote = newNote;
    	currentNoteGuid = currentNote.getGuid();
    	listManager.addNote(newNote);
    	refreshEvernoteNote(true);
    	listManager.countNotebookResults(listManager.getNoteIndex());
    	browserWindow.titleLabel.setFocus();
    	browserWindow.titleLabel.selectAll();
//    	notebookTree.updateCounts(listManager.getNotebookIndex(), listManager.getNotebookCounter());	
    	logger.log(logger.HIGH, "Leaving NeverNote.addNote");
    }
    // Restore a note from the trash;
    @SuppressWarnings("unused")
	private void restoreNote() {
    	waitCursor(true);
		if (selectedNoteGUIDs.size() == 0 && !currentNoteGuid.equals("")) 
			selectedNoteGUIDs.add(currentNoteGuid);
		for (int i=0; i<selectedNoteGUIDs.size(); i++) {
			listManager.restoreNote(selectedNoteGUIDs.get(i));
		}
    	currentNoteGuid = "";
    	listManager.loadNotesIndex();
    	noteIndexUpdated(false);
    	waitCursor(false);
    }
    // Search a note for specific txt
    @SuppressWarnings("unused")
	private void findText() {
    	find.show();
    	find.setFocusOnTextField();
    }
    @SuppressWarnings("unused")
	private void doFindText() {
    	browserWindow.getBrowser().page().findText(find.getText(), find.getFlags());
    	find.setFocus();
    }
    // Signal received that note content has changed.  Normally we just need the guid to remove
    // it from the cache.
    @SuppressWarnings("unused")
	private void invalidateNoteCache(String guid, String content) {
    	String v = noteCache.remove(guid);
    	if (content != null) {
    		v = noteCache.put(guid, content);
    	}
    }
    // Signal received that a note guid has changed
    @SuppressWarnings("unused")
	private void noteGuidChanged(String oldGuid, String newGuid) {
    	if (noteCache.containsKey(oldGuid)) {
    		String cache = noteCache.get(oldGuid);
    		noteCache.put(newGuid, cache);
    		noteCache.remove(oldGuid);
    	}
    	listManager.updateNoteGuid(oldGuid, newGuid, false);
    	if (currentNoteGuid.equals(oldGuid)) {
    		if (currentNote != null)
    			currentNote.setGuid(newGuid);
    		currentNoteGuid = newGuid;
    	}
    	for (int i=0; i<listManager.getNoteIndex().size(); i++) {
    		if (listManager.getNoteIndex().get(i).getGuid().equals(newGuid)) {
    			noteTableView.proxyModel.addGuid(newGuid);
    			i=listManager.getNoteIndex().size();
    		}
    	}
   	 	updateListGuid(oldGuid, newGuid);
    }
    // Toggle the note editor button bar
    private void toggleEditorButtonBar() {
    	if (browserWindow.buttonsVisible) {
    		browserWindow.hideButtons();
    		menuBar.showEditorBar.setChecked(browserWindow.buttonsVisible);
//    		Global.saveWindowVisible("editorButtonBar", browserWindow.buttonsVisible);
    	} else {
    		browserWindow.buttonsVisible = true;
    		showEditorButtons();
    	}
    	Global.saveWindowVisible("editorButtonBar", browserWindow.buttonsVisible);
    }
    // Show editor buttons
    private void showEditorButtons() {
   		browserWindow.undoButton.setVisible(false);
   		browserWindow.redoButton.setVisible(false);
   		browserWindow.cutButton.setVisible(false);
   		browserWindow.copyButton.setVisible(false);
   		browserWindow.pasteButton.setVisible(false);
   		browserWindow.strikethroughButton.setVisible(false);
   		browserWindow.underlineButton.setVisible(false);
   		browserWindow.boldButton.setVisible(false);
   		browserWindow.italicButton.setVisible(false);
   		browserWindow.hlineButton.setVisible(false);
   		browserWindow.indentButton.setVisible(false);
   		browserWindow.outdentButton.setVisible(false);
   		browserWindow.fontList.setVisible(false);
   		browserWindow.fontSize.setVisible(false);
   		browserWindow.fontColor.setVisible(false);
   		browserWindow.fontHilight.setVisible(false);
   		browserWindow.leftAlignButton.setVisible(false);
   		browserWindow.centerAlignButton.setVisible(false);
   		browserWindow.rightAlignButton.setVisible(false);
   		browserWindow.indentButton.setVisible(false);
   		browserWindow.outdentButton.setVisible(false);

   		browserWindow.undoButton.setVisible(Global.isEditorButtonVisible("undo"));
   		browserWindow.redoButton.setVisible(Global.isEditorButtonVisible("redo"));
   		browserWindow.cutButton.setVisible(Global.isEditorButtonVisible("cut"));
   		browserWindow.copyButton.setVisible(Global.isEditorButtonVisible("copy"));
   		browserWindow.pasteButton.setVisible(Global.isEditorButtonVisible("paste"));
   		browserWindow.strikethroughButton.setVisible(Global.isEditorButtonVisible("strikethrough"));
   		browserWindow.underlineButton.setVisible(Global.isEditorButtonVisible("underline"));
   		browserWindow.boldButton.setVisible(Global.isEditorButtonVisible("bold"));
   		browserWindow.italicButton.setVisible(Global.isEditorButtonVisible("italic"));
   		browserWindow.hlineButton.setVisible(Global.isEditorButtonVisible("hline"));
   		browserWindow.indentButton.setVisible(Global.isEditorButtonVisible("indent"));
   		browserWindow.outdentButton.setVisible(Global.isEditorButtonVisible("outdent"));
   		browserWindow.bulletListButton.setVisible(Global.isEditorButtonVisible("bulletList"));
   		browserWindow.numberListButton.setVisible(Global.isEditorButtonVisible("numberList"));
   		browserWindow.fontList.setVisible(Global.isEditorButtonVisible("font"));
   		browserWindow.fontSize.setVisible(Global.isEditorButtonVisible("fontSize"));
   		browserWindow.fontColor.setVisible(Global.isEditorButtonVisible("fontColor"));
   		browserWindow.fontHilight.setVisible(Global.isEditorButtonVisible("fontHilight"));
   		browserWindow.leftAlignButton.setVisible(Global.isEditorButtonVisible("alignLeft"));
   		browserWindow.centerAlignButton.setVisible(Global.isEditorButtonVisible("alignCenter"));
   		browserWindow.rightAlignButton.setVisible(Global.isEditorButtonVisible("alignRight"));
    }
    private void duplicateNote(String guid) {
		
		Calendar currentTime = new GregorianCalendar();
		Long l = new Long(currentTime.getTimeInMillis());
		String newGuid = new String(Long.toString(l));
					
		Note oldNote = conn.getNoteTable().getNote(guid, true, true, false, false, false);
		Note newNote = oldNote.deepCopy();
		newNote.setGuid(newGuid);
		List<Resource> resList = conn.getNoteTable().noteResourceTable.getNoteResources(guid, true);
		oldNote.setResources(resList);
		duplicateNote(oldNote);
	}
	private void duplicateNote(Note oldNote) {
		waitCursor(true);
		// Now that we have a good notebook guid, we need to move the conflicting note
		// to the local notebook
		Calendar currentTime = new GregorianCalendar();
		Long l = new Long(currentTime.getTimeInMillis());
		String newGuid = new String(Long.toString(l));
					
		Note newNote = oldNote.deepCopy();
		newNote.setUpdateSequenceNum(0);
		newNote.setGuid(newGuid);
		newNote.setDeleted(0);
		newNote.setActive(true);
		List<Resource> resList = oldNote.getResources();
		if (resList == null)
			resList = new ArrayList<Resource>();
		long prevGuid = 0;
		for (int i=0; i<resList.size(); i++) {
			l = prevGuid;
			while (l == prevGuid) {
				currentTime = new GregorianCalendar();
				l = new Long(currentTime.getTimeInMillis());
			}
			prevGuid = l;
			String newResGuid = new String(Long.toString(l));
			resList.get(i).setNoteGuid(newGuid);
			resList.get(i).setGuid(newResGuid);
			resList.get(i).setUpdateSequenceNum(0);
			resList.get(i).setActive(true);
			conn.getNoteTable().noteResourceTable.saveNoteResource(new Resource(resList.get(i).deepCopy()), true);
		}
		newNote.setResources(resList);
		listManager.addNote(newNote);
		conn.getNoteTable().addNote(newNote, true);
		listManager.getUnsynchronizedNotes().add(newNote.getGuid());
		noteTableView.insertRow(listManager, newNote, true, -1);
		listManager.countNotebookResults(listManager.getNoteIndex());
		waitCursor(false);
	}
	// Merge notes
	@SuppressWarnings("unused")
	private void mergeNotes() {
		logger.log(logger.HIGH, "Merging notes");
		waitCursor(true);
		saveNote();
		String masterGuid = null;
		List<String> sources = new ArrayList<String>();
		QModelIndex index;
		for (int i=0; i<noteTableView.selectionModel().selectedRows().size(); i++) {
			int r = noteTableView.selectionModel().selectedRows().get(i).row();
			index = noteTableView.proxyModel.index(r, Global.noteTableGuidPosition);
			SortedMap<Integer, Object> ix = noteTableView.proxyModel.itemData(index);
        	if (i == 0) 
        		masterGuid = (String)ix.values().toArray()[0];
        	else 
        		sources.add((String)ix.values().toArray()[0]);	
		}
		
		logger.log(logger.EXTREME, "Master guid=" +masterGuid);
		logger.log(logger.EXTREME, "Children count: "+sources.size());
		mergeNoteContents(masterGuid, sources);
		currentNoteGuid = masterGuid;
		noteIndexUpdated(false);
		refreshEvernoteNote(true);
		waitCursor(false);
	}
	private void mergeNoteContents(String targetGuid, List<String> sources) {
		Note target = conn.getNoteTable().getNote(targetGuid, true, false, false, false, false);
		String newContent = target.getContent();
		newContent = newContent.replace("</en-note>", "<br></br>");
		
		for (int i=0; i<sources.size(); i++) {
			Note source = conn.getNoteTable().getNote(sources.get(i), true, true, false, false, false);
			if (source.isSetTitle()) {
				newContent = newContent +("<table bgcolor=\"lightgrey\"><tr><td><font size=\"6\"><b>" +source.getTitle() +"</b></font></td></tr></table>");
			}
			String sourceContent = source.getContent();
			logger.log(logger.EXTREME, "Merging contents into note");
			logger.log(logger.EXTREME, sourceContent);
			logger.log(logger.EXTREME, "End of content");
			int startOfNote = sourceContent.indexOf("<en-note>");
			sourceContent = sourceContent.substring(startOfNote+9);
			int endOfNote = sourceContent.indexOf("</en-note>");
			sourceContent = sourceContent.substring(0,endOfNote);
			newContent = newContent + sourceContent;
			logger.log(logger.EXTREME, "New note content");
			logger.log(logger.EXTREME, newContent);
			logger.log(logger.EXTREME, "End of content");
			for (int j=0; j<source.getResourcesSize(); j++) {
				logger.log(logger.EXTREME, "Reassigning resource: "+source.getResources().get(j).getGuid());
				Resource r = source.getResources().get(j);
				Resource newRes = conn.getNoteTable().noteResourceTable.getNoteResource(r.getGuid(), true);
				
				Calendar currentTime = new GregorianCalendar();
				Long l = new Long(currentTime.getTimeInMillis());
							
				long prevGuid = 0;
				l = prevGuid;
				while (l == prevGuid) {
					currentTime = new GregorianCalendar();
					l = new Long(currentTime.getTimeInMillis());
				}
				String newResGuid = new String(Long.toString(l));
				newRes.setNoteGuid(targetGuid);
				newRes.setGuid(newResGuid);
				newRes.setUpdateSequenceNum(0);
				newRes.setActive(true);
				conn.getNoteTable().noteResourceTable.saveNoteResource(newRes, true);
			}
		}
		logger.log(logger.EXTREME, "Updating note");
		conn.getNoteTable().updateNoteContent(targetGuid, newContent +"</en-note>");
		for (int i=0; i<sources.size(); i++) {
			logger.log(logger.EXTREME, "Deleting note " +sources.get(i));
			listManager.deleteNote(sources.get(i));
		}
		logger.log(logger.EXTREME, "Exiting merge note");
	}
	// A resource within a note has had a guid change 
	@SuppressWarnings("unused")
	private void noteResourceGuidChanged(String noteGuid, String oldGuid, String newGuid) {
		if (!oldGuid.equals(newGuid))
			Global.resourceMap.put(oldGuid, newGuid);
	}
	// View a thumbnail of the note
	public void thumbnailView() {
		
		String thumbnailName = Global.currentDir+"res/thumbnail-"+currentNoteGuid+".png";
		QFile thumbnail = new QFile(thumbnailName);
		if (!thumbnail.exists()) {
			
			QImage img = new QImage();
			img.loadFromData(conn.getNoteTable().getThumbnail(currentNoteGuid));
			thumbnailViewer.setThumbnail(img);
		} else
			thumbnailViewer.setThumbnail(thumbnailName);
		if (!thumbnailViewer.isVisible()) 
			thumbnailViewer.showFullScreen();
	}

	//**********************************************************
    //**********************************************************
    //* Online user actions
    //**********************************************************
    //**********************************************************
    private void setupOnlineMenu() {
    	if (!Global.isConnected) {
    		menuBar.noteOnlineHistoryAction.setEnabled(false);
    		return;
    	} else {
    		menuBar.noteOnlineHistoryAction.setEnabled(true);
    	}
    }
    @SuppressWarnings("unused")
	private void viewNoteHistory() {
    	if (currentNoteGuid == null || currentNoteGuid.equals("")) 
    		return;
    	if (currentNote.getUpdateSequenceNum() == 0) {
    		setMessage("Note has never been synchronized.");
			QMessageBox.information(this, "Error", "This note has never been sent to Evernote, so there is no history.");
			return;
    	}
    	
    	setMessage("Getting Note History");
    	waitCursor(true);
    	Note currentOnlineNote = null;
    	versions = null;
    	try {
    		if (Global.isPremium())
    			versions = syncRunner.noteStore.listNoteVersions(syncRunner.authToken, currentNoteGuid);
    		else
    			versions = new ArrayList<NoteVersionId>();
    		currentOnlineNote = syncRunner.noteStore.getNote(syncRunner.authToken, currentNoteGuid, true, true, false, false);
		} catch (EDAMUserException e) {
			setMessage("EDAMUserException: " +e.getMessage());
			return;
		} catch (EDAMSystemException e) {
			setMessage("EDAMSystemException: " +e.getMessage());
			return;
		} catch (EDAMNotFoundException e) {
			setMessage("Note not found on server.");
			QMessageBox.information(this, "Error", "This note could not be found on Evernote's servers.");
			return;
		} catch (TException e) {
			setMessage("EDAMTransactionException: " +e.getMessage());
			return;
		}
		
		// If we've gotten this far, we have a good note.
		if (historyWindow == null) {
			historyWindow = new OnlineNoteHistory(conn);
			historyWindow.historyCombo.activated.connect(this, "reloadHistoryWindow(String)");
			historyWindow.restoreAsNew.clicked.connect(this, "restoreHistoryNoteAsNew()");
			historyWindow.restore.clicked.connect(this, "restoreHistoryNote()");
		} else {
			historyWindow.historyCombo.clear();
		}
		boolean isDirty = conn.getNoteTable().isNoteDirty(currentNoteGuid);
		if (currentNote.getUpdateSequenceNum() != currentOnlineNote.getUpdateSequenceNum())
			isDirty = true;
		historyWindow.setCurrent(isDirty);
		
		loadHistoryWindowContent(currentOnlineNote);
		historyWindow.load(versions);
		setMessage("History retrieved");
		waitCursor(false);
		historyWindow.exec();
    }
    private Note reloadHistoryWindow(String selection) {
    	waitCursor(true);
		String fmt = Global.getDateFormat() + " " + Global.getTimeFormat();
		String dateTimeFormat = new String(fmt);
		SimpleDateFormat simple = new SimpleDateFormat(dateTimeFormat);
		int index = -1;
		int usn = 0;
		
		for (int i=0; i<versions.size(); i++) {
			StringBuilder versionDate = new StringBuilder(simple.format(versions.get(i).getServiceUpdated()));
			if (versionDate.toString().equals(selection))
				index = i;
		}
		
		if (index > -1 || selection.indexOf("Current") > -1) {
			Note historyNote = null;
			try {
				if (index > -1) {
					usn = versions.get(index).getUpdateSequenceNum();
					historyNote = syncRunner.noteStore.getNoteVersion(syncRunner.authToken, currentNoteGuid, usn, true, true, true);
				} else
					historyNote = syncRunner.noteStore.getNote(syncRunner.authToken, currentNoteGuid, true,true,true,true);
			} catch (EDAMUserException e) {
				setMessage("EDAMUserException: " +e.getMessage());
				waitCursor(false);
				return null;
			} catch (EDAMSystemException e) {
				setMessage("EDAMSystemException: " +e.getMessage());
				waitCursor(false);
				return null;
			} catch (EDAMNotFoundException e) {
				setMessage("EDAMNotFoundException: " +e.getMessage());
				waitCursor(false);
				return null;
			} catch (TException e) {
				setMessage("EDAMTransactionException: " +e.getMessage());
				waitCursor(false);
				return null;
			}
			
			waitCursor(false);
			if (historyNote != null) 
				historyWindow.setContent(historyNote);
			return historyNote;
		}
		waitCursor(false);
		return null;
    }
    private void loadHistoryWindowContent(Note note) {
    	note.setUpdateSequenceNum(0);
		historyWindow.setContent(note);	
    }
    @SuppressWarnings("unused")
	private void restoreHistoryNoteAsNew() {
    	setMessage("Restoring as new note.");
    	duplicateNote(reloadHistoryWindow(historyWindow.historyCombo.currentText()));
    	setMessage("Note has been restored as a new note.");
    }
    @SuppressWarnings("unused")
	private void restoreHistoryNote() {
    	setMessage("Restoring note.");
    	Note n = reloadHistoryWindow(historyWindow.historyCombo.currentText());
    	conn.getNoteTable().expungeNote(n.getGuid(), true, false);
    	n.setActive(true);
    	n.setDeleted(0);
		for (int i=0; i<n.getResourcesSize(); i++) {
			n.getResources().get(i).setActive(true);
			conn.getNoteTable().noteResourceTable.saveNoteResource(n.getResources().get(i), true);
		}
    	listManager.addNote(n);
    	conn.getNoteTable().addNote(n, true);
    	refreshEvernoteNote(true);
    	setMessage("Note has been restored.");
    }
    
    
    
	//**********************************************************
	//**********************************************************
	//* XML Modifying methods
	//**********************************************************
	//**********************************************************
    // find the appropriate icon for an attachment
    private String findIcon(String appl) {
    	logger.log(logger.HIGH, "Entering NeverNote.findIcon");
    	appl = appl.toLowerCase();
        String relativePath = appl + ".png";
        File f = Global.getFileManager().getImageDirFile(relativePath);
        if (f.exists()) {
            return relativePath;
        }
    	logger.log(logger.HIGH, "Leaving NeverNote.findIcon");
    	return "attachment.png";
    }
    // Modify the en-media tag into an attachment
    private void modifyApplicationTags(QDomDocument doc, QDomElement docElem, QDomElement enmedia, QDomAttr hash, String appl) {
    	logger.log(logger.HIGH, "Entering NeverNote.modifyApplicationTags");
    	if (appl.equalsIgnoreCase("vnd.evernote.ink"))
    		inkNote = true;
    	String resGuid = conn.getNoteTable().noteResourceTable.getNoteResourceGuidByHashHex(currentNote.getGuid(), hash.value());
    	Resource r = conn.getNoteTable().noteResourceTable.getNoteResource(resGuid, false);
    	if (r == null || r.getData() == null) 
    		resourceErrorMessage();
		if (r!= null) {
			if (r.getData()!=null) {
				// Did we get a generic applicaiton?  Then look at the file name to 
				// try and find a good application type for the icon
				if (appl.equalsIgnoreCase("octet-stream")) {
					if (r.getAttributes() != null && r.getAttributes().getFileName() != null) {
						String fn = r.getAttributes().getFileName();
						int pos = fn.lastIndexOf(".");
						if (pos > -1) {
							appl = fn.substring(pos+1);
						}
					}
				}
				
				String fileDetails = null;
				if (r.getAttributes() != null && r.getAttributes().getFileName() != null && !r.getAttributes().getFileName().equals(""))
					fileDetails = r.getAttributes().getFileName();
				String contextFileName;
				String pathPrefix = Global.currentDir;
				pathPrefix = Global.getDirectoryPath()+"res/";
				if (fileDetails != null && !fileDetails.equals("")) {
					enmedia.setAttribute("href", "nnres://" +r.getGuid() +Global.attachmentNameDelimeter +fileDetails);
					contextFileName = Global.currentDir +"res/" +r.getGuid() +Global.attachmentNameDelimeter +fileDetails;
				} else { 
					enmedia.setAttribute("href", "nnres://" +r.getGuid() +Global.attachmentNameDelimeter +appl);
					contextFileName = pathPrefix+r.getGuid() +Global.attachmentNameDelimeter +appl;
				}
				contextFileName = contextFileName.replace("\\", "/");
				enmedia.setAttribute("onContextMenu", "window.jambi.resourceContextMenu('" +contextFileName +"');");
				if (fileDetails == null || fileDetails.equals(""))
					fileDetails = "";
				enmedia.setAttribute("en-tag", "en-media");
				enmedia.setAttribute("guid", r.getGuid());
				enmedia.setTagName("a");
				QDomElement newText = doc.createElement("img");
				boolean goodPreview = false;
				String filePath = "";
				if (appl.equalsIgnoreCase("pdf") && Global.pdfPreview()) {
					String fileName;
					Resource res = conn.getNoteTable().noteResourceTable.getNoteResource(r.getGuid(), true);
					if (res.getAttributes() != null && 
							res.getAttributes().getFileName() != null && 
							!res.getAttributes().getFileName().trim().equals(""))
						fileName = res.getGuid()+Global.attachmentNameDelimeter+res.getAttributes().getFileName();
					else
						fileName = res.getGuid()+".pdf";
					QFile file = new QFile(Global.getDirectoryPath() +"res/"+fileName);
			        QFile.OpenMode mode = new QFile.OpenMode();
			        mode.set(QFile.OpenModeFlag.WriteOnly);
			        file.open(mode);
			        QDataStream out = new QDataStream(file);
			        Resource resBinary = conn.getNoteTable().noteResourceTable.getNoteResource(res.getGuid(), true);
					QByteArray binData = new QByteArray(resBinary.getData().getBody());
					resBinary = null;
			        out.writeBytes(binData.toByteArray());
			        file.close();
			        PDFPreview pdfPreview = new PDFPreview();
					goodPreview = pdfPreview.setupPreview(file.fileName(), appl,0);
					if (goodPreview) {
						QDomElement span = doc.createElement("span");
						QDomElement table = doc.createElement("table");
						span.setAttribute("pdfNavigationTable", "true");
						QDomElement tr = doc.createElement("tr");
						QDomElement td = doc.createElement("td");
						QDomElement left = doc.createElement("img");
						left.setAttribute("onMouseDown", "window.jambi.nextPage('" +file.fileName() +"')");
						left.setAttribute("onMouseDown", "window.jambi.nextPage('" +file.fileName() +"')");
						left.setAttribute("onMouseOver", "style.cursor='hand'");
						QDomElement right = doc.createElement("img");
						right.setAttribute("onMouseDown", "window.jambi.nextPage('" +file.fileName() +"')");
						left.setAttribute("onMouseDown", "window.jambi.previousPage('" +file.fileName() +"')");
						// NFC TODO: should these be file:// URLs?
						left.setAttribute("src", Global.getFileManager().getImageDirPath("small_left.png"));
						right.setAttribute("src", Global.getFileManager().getImageDirPath("small_right.png"));
						right.setAttribute("onMouseOver", "style.cursor='hand'");
						
						table.appendChild(tr);
						tr.appendChild(td);
						td.appendChild(left);
						td.appendChild(right);
						span.appendChild(table);
						enmedia.parentNode().insertBefore(span, enmedia);
					} 
					filePath = fileName+".png";
				}
				String icon = findIcon(appl);
				if (icon.equals("attachment.png"))
					icon = findIcon(fileDetails.substring(fileDetails.indexOf(".")+1));
				// NFC TODO: should this be a 'file://' URL?
				newText.setAttribute("src", Global.getFileManager().getImageDirPath(icon));
				if (goodPreview) {
					newText.setAttribute("src", Global.getDirectoryPath()+"res/"+filePath);
					newText.setAttribute("style", "border-style:solid; border-color:green; padding:0.5mm 0.5mm 0.5mm 0.5mm;");
				}
				newText.setAttribute("title", fileDetails);
				enmedia.removeChild(enmedia.firstChild());
				
				enmedia.appendChild(newText);
			}
		}
		logger.log(logger.HIGH, "Leaving NeverNote.modifyApplicationTags");
    }
    // Modify the en-to tag into an input field
    private void modifyTodoTags(QDomElement todo) {
    	logger.log(logger.HIGH, "Entering NeverNote.modifyTodoTags");
		todo.setAttribute("type", "checkbox");
		String checked = todo.attribute("checked");
		todo.removeAttribute("checked");
		if (checked.equalsIgnoreCase("true"))
			todo.setAttribute("checked", "");
		else
			todo.setAttribute("unchecked","");
		todo.setAttribute("value", checked);
		todo.setAttribute("onClick", "value=checked;window.jambi.contentChanged(); ");
		todo.setTagName("input");
		logger.log(logger.HIGH, "Leaving NeverNote.modifyTodoTags");
    }
    // Modify any cached todo tags that may have changed
    private String modifyCachedTodoTags(String note) {
    	logger.log(logger.HIGH, "Entering NeverNote.modifyCachedTodoTags");
    	StringBuffer html = new StringBuffer(note);
		for (int i=html.indexOf("<input", 0); i>-1; i=html.indexOf("<input", i)) {
			int endPos =html.indexOf(">",i+1);
			String input = html.substring(i,endPos);
			if (input.indexOf("value=\"true\"") > 0) 
				input = input.replace("unchecked=\"\"", "checked=\"\"");
			else
				input = input.replace("checked=\"\"", "unchecked=\"\"");
			html.replace(i, endPos, input);
			i++;
		}
		logger.log(logger.HIGH, "Leaving NeverNote.modifyCachedTodoTags");
		return html.toString();
    }
    // Modify the en-media tag into an image tag so it can be displayed.
    private void modifyImageTags(QDomElement docElem, QDomElement enmedia, QDomAttr hash) {
    	logger.log(logger.HIGH, "Entering NeverNote.modifyImageTags");
    	String type = enmedia.attribute("type");
    	if (type.startsWith("image/"))
    		type = "."+type.substring(6);
    	else
    		type="";
    	
    	String resGuid = conn.getNoteTable().noteResourceTable.getNoteResourceGuidByHashHex(currentNoteGuid, hash.value());
    	QFile tfile = new QFile(Global.getDirectoryPath()+"res"+File.separator +resGuid+type);
    	if (!tfile.exists()) {
    		Resource r = null;
    		if (resGuid != null)
    			r = conn.getNoteTable().noteResourceTable.getNoteResource(resGuid,true);
   			if (r==null || r.getData() == null || r.getData().getBody().length == 0)
   				resourceErrorMessage();
   			if (r!= null && r.getData() != null && r.getData().getBody().length > 0) {
 				tfile.open(new QIODevice.OpenMode(QIODevice.OpenModeFlag.WriteOnly));
 				QByteArray binData = new QByteArray(r.getData().getBody());
				tfile.write(binData);
 				tfile.close();
				enmedia.setAttribute("src", QUrl.fromLocalFile(tfile.fileName()).toString());
  				enmedia.setAttribute("en-tag", "en-media");
  				enmedia.setNodeValue("");
    			enmedia.setAttribute("guid", r.getGuid());
    			enmedia.setTagName("img");
    		}
    	}
		enmedia.setAttribute("src", QUrl.fromLocalFile(tfile.fileName()).toString());
		enmedia.setAttribute("en-tag", "en-media");
		enmedia.setAttribute("onContextMenu", "window.jambi.imageContextMenu('" +tfile.fileName()  +"');");
		enmedia.setNodeValue("");
		enmedia.setAttribute("guid", resGuid);
		enmedia.setTagName("img");

		logger.log(logger.HIGH, "Leaving NeverNote.modifyImageTags");
    }
	// Modify tags from Evernote specific things to XHTML tags.
	private QDomDocument modifyTags(QDomDocument doc) {
		logger.log(logger.HIGH, "Entering NeverNote.modifyTags");
		if (tempFiles == null)
			tempFiles = new ArrayList<QTemporaryFile>();
		tempFiles.clear();
		QDomElement docElem = doc.documentElement();
		
		// Modify en-media tags
		QDomNodeList anchors = docElem.elementsByTagName("en-media");
		int enMediaCount = anchors.length();
		for (int i=enMediaCount-1; i>=0; i--) {
			QDomElement enmedia = anchors.at(i).toElement();
			if (enmedia.hasAttribute("type")) {
				QDomAttr attr = enmedia.attributeNode("type");
				QDomAttr hash = enmedia.attributeNode("hash");
				String[] type = attr.nodeValue().split("/");
				String appl = type[1];
				
				if (type[0] != null) {
					if (type[0].equals("image")) {
						modifyImageTags(docElem, enmedia, hash);
					}
					if (!type[0].equals("image")) {
						modifyApplicationTags(doc, docElem, enmedia, hash, appl);
					}
				}
			}
		}
		
		// Modify todo tags
		anchors = docElem.elementsByTagName("en-todo");
		int enTodoCount = anchors.length();
		for (int i=enTodoCount-1; i>=0; i--) {
			QDomElement enmedia = anchors.at(i).toElement();
			modifyTodoTags(enmedia);
		}
		
		// Modify en-crypt tags
		anchors = docElem.elementsByTagName("en-crypt");
		int enCryptLen = anchors.length();
		for (int i=enCryptLen-1; i>=0; i--) {
			QDomElement enmedia = anchors.at(i).toElement();
			enmedia.setAttribute("contentEditable","false");
			enmedia.setAttribute("src", Global.getFileManager().getImageDirPath("encrypt.png"));
			enmedia.setAttribute("en-tag","en-crypt");
			enmedia.setAttribute("alt", enmedia.text());
			Global.cryptCounter++;
			enmedia.setAttribute("id", "crypt"+Global.cryptCounter.toString());
			String encryptedText = enmedia.text();
			
			// If the encryption string contains crlf at the end, remove them because they mess up the javascript.
			if (encryptedText.endsWith("\n"))
				encryptedText = encryptedText.substring(0,encryptedText.length()-1);
			if (encryptedText.endsWith("\r"))
				encryptedText = encryptedText.substring(0,encryptedText.length()-1);
			
			// Add the commands
			String hint = enmedia.attribute("hint");
			hint = hint.replace("'","&apos;");
			enmedia.setAttribute("onClick", "window.jambi.decryptText('crypt"+Global.cryptCounter.toString()+"', '"+encryptedText+"', '"+hint+"');");
			enmedia.setAttribute("onMouseOver", "style.cursor='hand'");
			enmedia.setTagName("img");
			enmedia.removeChild(enmedia.firstChild());   // Remove the actual encrypted text
		}

		logger.log(logger.HIGH, "Leaving NeverNote.modifyTags");
		return doc;
	}
	// Rebuild the note HTML to something usable
	private String rebuildNoteHTML(String noteGuid, String note) {
	 	logger.log(logger.HIGH, "Entering NeverNote.rebuildNoteHTML");
		logger.log(logger.EXTREME, "Note guid: " +noteGuid);
		logger.log(logger.EXTREME, "Note Text:" +note);
		QDomDocument doc = new QDomDocument();
		QDomDocument.Result result = doc.setContent(note);
		if (!result.success) {
			logger.log(logger.MEDIUM, tr("Parse error when rebuilding HTML"));
			logger.log(logger.MEDIUM, tr("Note guid: " +noteGuid));
			logger.log(logger.EXTREME, tr("Start of unmodified note HTML"));
			logger.log(logger.EXTREME, note);
			logger.log(logger.EXTREME, tr("End of unmodified note HTML"));
			return note;
		}

		if (tempFiles == null)
			tempFiles = new ArrayList<QTemporaryFile>();
		tempFiles.clear();
		
		doc = modifyTags(doc);
		doc = addHilight(doc);
		QDomElement docElem = doc.documentElement();
		docElem.setTagName("Body");
//		docElem.setAttribute("bgcolor", "green");
		logger.log(logger.EXTREME, "Rebuilt HTML:");
		logger.log(logger.EXTREME, doc.toString());	
		logger.log(logger.HIGH, "Leaving NeverNote.rebuildNoteHTML");
		// Fix the stupid problem where inserting an <img> tag after an <a> tag (which is done
		// to get the <en-media> application tag to work properly) causes spaces to be inserted
		// between the <a> & <img>.  This messes things up later.  This is an ugly hack.
		StringBuffer html = new StringBuffer(doc.toString());
		for (int i=html.indexOf("<a en-tag=\"en-media\" ", 0); i>-1; i=html.indexOf("<a en-tag=\"en-media\" ", i)) {
			i=html.indexOf(">\n",i+1);
			int z = html.indexOf("<img",i);
			for (int j=z-1; j>i; j--) 
				html.deleteCharAt(j);
			i=html.indexOf("/>", z+1);
			z = html.indexOf("</a>",i);
			for (int j=z-1; j>i+1; j--) 
				html.deleteCharAt(j);
		} 
		return html.toString();
	}	
	// Scan and do hilighting of words
	private QDomDocument addHilight(QDomDocument doc) {
		EnSearch e = listManager.getEnSearch();
		if (e.hilightWords == null || e.hilightWords.size() == 0)
			return doc;
		XMLInsertHilight hilight = new XMLInsertHilight(doc, listManager.getEnSearch().hilightWords);
		return hilight.getDoc();
	}

	// An error has happended fetching a resource.  let the user know
	private void resourceErrorMessage() {
		if (inkNote)
			return;
		QMessageBox.information(this, tr("DOUGH!!!"), tr("Well, this is embarrassing."+
		"\n\nSome attachments or images for this note appear to be missing from my database.\n"+
		"In a perfect world this wouldn't happen, but it has.\n" +
		"It is embarasing when a program like me, designed to save all your\n"+
		"precious data, has a problem finding data.\n\n" +
		"I guess life isn't fair, but I'll survive.  Somehow...\n\n" +
		"In the mean time, I'm not going to let you make changes to this note.\n" +
		"Don't get angry.  I'm doing it to prevent you from messing up\n"+
		"this note on the Evernote servers.  Sorry."+
		"\n\nP.S. You might want to re-synchronize to see if it corrects this problem.\nWho knows, you might get lucky."));
		inkNote = true;
////		browserWindow.setEnabled(false);
		browserWindow.setReadOnly(true);
	}

	
	
	
	//**********************************************************
	//**********************************************************
	//* Timer functions
	//**********************************************************
	//**********************************************************
	// We should now do a sync with Evernote
	private void syncTimer() {
		logger.log(logger.EXTREME, "Entering NeverNote.syncTimer()");
		syncRunner.syncNeeded = true;
		syncRunner.disableUploads = Global.disableUploads;
		syncStart();
		logger.log(logger.EXTREME, "Leaving NeverNote.syncTimer()");
	}
	private void syncStart() {
		logger.log(logger.EXTREME, "Entering NeverNote.syncStart()");
		saveNote();
		if (!syncRunning && Global.isConnected) {
			syncRunner.setConnected(true);
			syncRunner.setKeepRunning(Global.keepRunning);
			syncRunner.syncDeletedContent = Global.synchronizeDeletedContent();
			
			if (syncThreadsReady > 0) {
				saveNoteIndexWidth();
				if (syncRunner.addWork("SYNC")) {
					syncRunning = true;
					syncRunner.syncNeeded = true;
					syncThreadsReady--;
				}				
			}
		}
		logger.log(logger.EXTREME, "Leaving NeverNote.syncStart");
	}
	@SuppressWarnings("unused")
	private void syncThreadComplete(Boolean refreshNeeded) {
		setMessage("Finalizing Synchronization");
		syncThreadsReady++;
		syncRunning = false;
		syncRunner.syncNeeded = false;
		synchronizeAnimationTimer.stop();
		noteIndexUpdated(true);
		synchronizeButton.setIcon(synchronizeAnimation.get(0));
		saveNote();
//		noteTableView.selectionModel().selectionChanged.disconnect(this, "noteTableSelection()");
		noteTableView.selectionModel().blockSignals(true);
		scrollToGuid(currentNoteGuid);
		noteTableView.selectionModel().blockSignals(false);
//		noteTableView.selectionModel().selectionChanged.connect(this, "noteTableSelection()");
//		indexRunner.setKeepRunning(Global.keepRunning);
		
		// Reload the unindexed table  If the dbthread is dead, we are probably shutting down.
		if (!dbThread.isAlive())
			return;
		listManager.setUnsynchronizedNotes(conn.getNoteTable().getUnsynchronizedGUIDs());
		for (int i=0; i<noteTableView.model.rowCount(); i++) {
			QModelIndex modelIndex =  noteTableView.model.index(i, Global.noteTableGuidPosition);
			if (modelIndex != null) {
			SortedMap<Integer, Object> ix = noteTableView.model.itemData(modelIndex);
				String tableGuid =  (String)ix.values().toArray()[0];
				String synch = "true";
				for (int j=0; j<listManager.getUnsynchronizedNotes().size(); j++) {
					if (listManager.getUnsynchronizedNotes().get(j).equalsIgnoreCase(tableGuid)) {
						synch = "false";
						j = listManager.getUnsynchronizedNotes().size();
					}
				}
				noteTableView.model.setData(i, Global.noteTableSynchronizedPosition, synch);
			}
		}	
		refreshEvernoteNote(false);
		scrollToGuid(currentNoteGuid);
		setMessage("Synchronization Complete");
		logger.log(logger.MEDIUM, "Sync complete.");
	}   
//	public void setSequenceDate(long t) {
//		Global.setSequenceDate(t);
//	}
	public void saveUploadAmount(long t) {
		Global.saveUploadAmount(t);
	}
//	public void setUpdateSequenceNumber(int t) {
//		Global.setUpdateSequenceNumber(t);
//	}
	public void saveUserInformation(User user) {
		Global.saveUserInformation(user);
	}
	public void saveEvernoteUpdateCount(int i) {
		Global.saveEvernoteUpdateCount(i);
	}
	public void refreshLists() {
		logger.log(logger.EXTREME, "Entering NeverNote.refreshLists");
		updateQuotaBar();
		listManager.refreshLists(currentNote, noteDirty, browserWindow.getContent());
		tagIndexUpdated(true);
		notebookIndexUpdated();
		savedSearchIndexUpdated();
		listManager.loadNotesIndex();

		noteTableView.selectionModel().blockSignals(true);
    	noteIndexUpdated(true);
		noteTableView.selectionModel().blockSignals(false);
		logger.log(logger.EXTREME, "Leaving NeverNote.refreshLists");
	}

	
	@SuppressWarnings("unused")
	private void authTimer() {
        Calendar cal = Calendar.getInstance();
		
        // If we are not connected let's get out of here
        if (!Global.isConnected)
        	return;
                
   		// If this is the first time through, then we need to set this
 //  		if (syncRunner.authRefreshTime == 0 || cal.getTimeInMillis() > syncRunner.authRefreshTime) 
//   			syncRunner.authRefreshTime = cal.getTimeInMillis();
   		
//   		long now = new Date().getTime();
//		if (now > Global.authRefreshTime && Global.isConnected) {
			syncRunner.authRefreshNeeded = true;
			syncStart();
//		}
	}
	@SuppressWarnings("unused")
	private void authRefreshComplete(boolean goodSync) {
		logger.log(logger.EXTREME, "Entering NeverNote.authRefreshComplete");
		Global.isConnected = syncRunner.isConnected;
		if (goodSync) {
//			authTimer.start((int)syncRunner.authTimeRemaining/4);
			authTimer.start(1000*60*15);
			logger.log(logger.LOW, "Authentication token has been renewed");
//			setMessage("Authentication token has been renewed.");
		} else {
			authTimer.start(1000*60*5);
			logger.log(logger.LOW, "Authentication token renew has failed - retry in 5 minutes.");
//			setMessage("Authentication token renew has failed - retry in 5 minutes.");
		}
		logger.log(logger.EXTREME, "Leaving NeverNote.authRefreshComplete");
	}
	
	
	@SuppressWarnings("unused")
	private synchronized void indexTimer() {
		logger.log(logger.EXTREME, "Index timer activated.  Sync running="+syncRunning);
		if (syncRunning) 
			return;
		// Look for any unindexed notes.  We only refresh occasionally 
		// and do one at a time to keep overhead down.
		if (!indexDisabled && indexRunner.getWorkQueueSize() == 0) { 
			List<String> notes = conn.getNoteTable().getNextUnindexed(1);
			String unindexedNote = null;
			if (notes.size() > 0)
				unindexedNote = notes.get(0);
			if (unindexedNote != null && Global.keepRunning) {
				indexNoteContent(unindexedNote);
			}
			if (notes.size()>0) {
				indexTimer.setInterval(100);
				return;
			}
			List<String> unindexedResources = conn.getNoteTable().noteResourceTable.getNextUnindexed(1);
			if (unindexedResources.size() > 0 && indexRunner.getWorkQueueSize() == 0) {
				String unindexedResource = unindexedResources.get(0);
				if (unindexedResource != null && Global.keepRunning) {
					indexNoteResource(unindexedResource);
				}
			}
			if (unindexedResources.size() > 0) {
				indexTimer.setInterval(100);
				return;
			} else {
				indexTimer.setInterval(indexTime);
			}
			if (indexRunning) {
				setMessage("Index completed.");
				logger.log(logger.LOW, "Indexing has completed.");
				indexRunning = false;
				indexTimer.setInterval(indexTime);
			}
		}
		logger.log(logger.EXTREME, "Leaving neverNote index timer");
	}
	private synchronized void indexNoteContent(String unindexedNote) {
		logger.log(logger.EXTREME, "Entering NeverNote.indexNoteContent()");
		logger.log(logger.MEDIUM, "Unindexed Note found: "+unindexedNote);
		indexRunner.setIndexType(indexRunner.CONTENT);
		indexRunner.addWork("CONTENT "+unindexedNote);
		if (!indexRunning) {
			setMessage("Indexing notes.");
			logger.log(logger.LOW, "Beginning to index note contents.");
			indexRunning = true;
		}
		logger.log(logger.EXTREME, "Leaving NeverNote.indexNoteContent()");
	}
	private synchronized void indexNoteResource(String unindexedResource) {
		logger.log(logger.EXTREME, "Leaving NeverNote.indexNoteResource()");
		indexRunner.addWork(new String("RESOURCE "+unindexedResource));
		if (!indexRunning) {
			setMessage("Indexing notes.");
			indexRunning = true;
		}
		logger.log(logger.EXTREME, "Leaving NeverNote.indexNoteResource()");
	}
	@SuppressWarnings("unused")
	private void indexThreadComplete(String guid) {
		logger.log(logger.MEDIUM, "Index complete for "+guid);
	}
	@SuppressWarnings("unused")
	private synchronized void toggleNoteIndexing() {
		logger.log(logger.HIGH, "Entering NeverNote.toggleIndexing");
		indexDisabled = !indexDisabled;
		if (!indexDisabled)
			setMessage("Indexing is now enabled.");
		else
			setMessage("Indexing is now disabled.");
		menuBar.disableIndexing.setChecked(indexDisabled);
    	logger.log(logger.HIGH, "Leaving NeverNote.toggleIndexing");
    }  
	
	@SuppressWarnings("unused")
	private void threadMonitorCheck() {
		int MAX=3;
		
		
		boolean alive;
		alive = listManager.threadCheck(Global.tagCounterThreadId);
		if (!alive) {
			tagDeadCount++;
			if (tagDeadCount > MAX)
				QMessageBox.information(this, "A thread his died.", "It appears as the tag counter thread has died.  I recommend "+
				"checking stopping NeverNote, saving the logs for later viewing, and restarting.  Sorry.");
		} else
			tagDeadCount=0;
		
		alive = listManager.threadCheck(Global.notebookCounterThreadId);
		if (!alive) {
			notebookThreadDeadCount++;
			QMessageBox.information(this, "A thread his died.", "It appears as the notebook counter thread has died.  I recommend "+
			"checking stopping NeverNote, saving the logs for later viewing, and restarting.  Sorry.");
		} else
			notebookThreadDeadCount=0;
		
		alive = listManager.threadCheck(Global.trashCounterThreadId);
		if (!alive) {
			trashDeadCount++;
			QMessageBox.information(this, "A thread his died.", "It appears as the trash counter thread has died.  I recommend "+
			"checking stopping NeverNote, saving the logs for later viewing, and restarting.  Sorry.");
		} else
			trashDeadCount = 0;

		alive = listManager.threadCheck(Global.saveThreadId);
		if (!alive) {
			saveThreadDeadCount++;
			QMessageBox.information(this, "A thread his died.", "It appears as the note saver thread has died.  I recommend "+
			"checking stopping NeverNote, saving the logs for later viewing, and restarting.  Sorry.");
		} else
			saveThreadDeadCount=0;

		if (!dbThread.isAlive()) {
			dbThreadDeadCount++;
			QMessageBox.information(this, "A thread his died.", "It appears as the database thread has died.  I recommend "+
			"checking stopping NeverNote, saving the logs for later viewing, and restarting.  Sorry.");
		} else
			dbThreadDeadCount=0;

		if (!syncThread.isAlive()) {
			syncThreadDeadCount++;
			QMessageBox.information(this, "A thread his died.", "It appears as the synchronization thread has died.  I recommend "+
			"checking stopping NeverNote, saving the logs for later viewing, and restarting.  Sorry.");
		} else
			syncThreadDeadCount=0;

		if (!indexThread.isAlive()) {
			indexThreadDeadCount++;
			QMessageBox.information(this, "A thread his died.", "It appears as the index thread has died.  I recommend "+
			"checking stopping NeverNote, saving the logs for later viewing, and restarting.  Sorry.");
		} else
			indexThreadDeadCount=0;

		
	}

	
	
	//**************************************************
	//* Backup & Restore
	//**************************************************
	@SuppressWarnings("unused")
	private void databaseBackup() {
		QFileDialog fd = new QFileDialog(this);
		fd.setFileMode(FileMode.AnyFile);
		fd.setConfirmOverwrite(true);
		fd.setWindowTitle("Backup Database");
		fd.setFilter(tr("NeverNote Export (*.nnex);;All Files (*.*)"));
		fd.setAcceptMode(AcceptMode.AcceptSave);
		fd.setDirectory(System.getProperty("user.home"));
		if (fd.exec() == 0 || fd.selectedFiles().size() == 0) {
			return;
		}
		
		
    	waitCursor(true);
    	setMessage("Backing up database");
    	saveNote();
//    	conn.backupDatabase(Global.getUpdateSequenceNumber(), Global.getSequenceDate());
    	
    	ExportData noteWriter = new ExportData(conn, true);
    	String fileName = fd.selectedFiles().get(0);

    	if (!fileName.endsWith(".nnex"))
    		fileName = fileName +".nnex";
    	noteWriter.exportData(fileName);
    	setMessage("Database backup completed.");
 

    	waitCursor(false);
	}
	@SuppressWarnings("unused")
	private void databaseRestore() {
		if (QMessageBox.question(this, "Confirmation",
				"This is used to restore a database from backups.\n" +
				"It is HIGHLY recommened that this only be used to populate\n" +
				"an empty database.  Restoring into a database that\n already has data" +
				" can cause problems.\n\nAre you sure you want to continue?",
				QMessageBox.StandardButton.Yes, 
				QMessageBox.StandardButton.No)==StandardButton.No.value()) {
					return;
				}
		
		
		QFileDialog fd = new QFileDialog(this);
		fd.setFileMode(FileMode.ExistingFile);
		fd.setConfirmOverwrite(true);
		fd.setWindowTitle("Restore Database");
		fd.setFilter(tr("NeverNote Export (*.nnex);;All Files (*.*)"));
		fd.setAcceptMode(AcceptMode.AcceptOpen);
		fd.setDirectory(System.getProperty("user.home"));
		if (fd.exec() == 0 || fd.selectedFiles().size() == 0) {
			return;
		}
		
		
		waitCursor(true);
		setMessage("Restoring database");
    	ImportData noteReader = new ImportData(conn, true);
    	noteReader.importData(fd.selectedFiles().get(0));
    	
    	if (noteReader.lastError != 0) {
    		setMessage(noteReader.getErrorMessage());
    		logger.log(logger.LOW, "Restore problem: " +noteReader.lastError);
    		waitCursor(false);
    		return;
    	}
    	
    	listManager.loadNoteTitleColors();
    	refreshLists();
    	refreshEvernoteNote(true);
    	setMessage("Database has been restored.");
    	waitCursor(false);
	}
	@SuppressWarnings("unused")
	private void exportNotes() {
		QFileDialog fd = new QFileDialog(this);
		fd.setFileMode(FileMode.AnyFile);
		fd.setConfirmOverwrite(true);
		fd.setWindowTitle("Backup Database");
		fd.setFilter(tr("NeverNote Export (*.nnex);;All Files (*.*)"));
		fd.setAcceptMode(AcceptMode.AcceptSave);
		fd.setDirectory(System.getProperty("user.home"));
		if (fd.exec() == 0 || fd.selectedFiles().size() == 0) {
			return;
		}
		
		
    	waitCursor(true);
    	setMessage("Exporting Notes");
    	saveNote();
    	
		if (selectedNoteGUIDs.size() == 0 && !currentNoteGuid.equals("")) 
			selectedNoteGUIDs.add(currentNoteGuid);
		
    	ExportData noteWriter = new ExportData(conn, false, selectedNoteGUIDs);
    	String fileName = fd.selectedFiles().get(0);

    	if (!fileName.endsWith(".nnex"))
    		fileName = fileName +".nnex";
    	noteWriter.exportData(fileName);
    	setMessage("Export completed.");
 

    	waitCursor(false);
		
	}
	@SuppressWarnings("unused")
	private void importNotes() {
		QFileDialog fd = new QFileDialog(this);
		fd.setFileMode(FileMode.ExistingFile);
		fd.setConfirmOverwrite(true);
		fd.setWindowTitle("Import Notes");
		fd.setFilter(tr("NeverNote Export (*.nnex);;All Files (*.*)"));
		fd.setAcceptMode(AcceptMode.AcceptOpen);
		fd.setDirectory(System.getProperty("user.home"));
		if (fd.exec() == 0 || fd.selectedFiles().size() == 0) {
			return;
		}
		
		
    	waitCursor(true);
    	setMessage("Importing Notes");
    	saveNote();
    	
		if (selectedNoteGUIDs.size() == 0 && !currentNoteGuid.equals("")) 
			selectedNoteGUIDs.add(currentNoteGuid);
		
    	ImportData noteReader = new ImportData(conn, false);
    	String fileName = fd.selectedFiles().get(0);

    	if (!fileName.endsWith(".nnex"))
    		fileName = fileName +".nnex";
    	if (selectedNotebookGUIDs != null && selectedNotebookGUIDs.size() > 0) 
    		noteReader.setNotebookGuid(selectedNotebookGUIDs.get(0));
    	else
    		noteReader.setNotebookGuid(listManager.getNotebookIndex().get(0).getGuid());
  
    	noteReader.importData(fileName);
    	
    	if (noteReader.lastError != 0) {
    		setMessage(noteReader.getErrorMessage());
    		logger.log(logger.LOW, "Import problem: " +noteReader.lastError);
    		waitCursor(false);
    		return;
    	}
    	
    	listManager.loadNoteTitleColors();
    	refreshLists();
    	refreshEvernoteNote(false);
    	setMessage("Notes have been imported.");
    	waitCursor(false);
    	
    	setMessage("Import completed.");
 

    	waitCursor(false);
		
	}
	
	//**************************************************
	//* Duplicate a note 
	//**************************************************
	@SuppressWarnings("unused")
	private void duplicateNote() {
		saveNote();
		duplicateNote(currentNoteGuid);
	}

	
	
	//**************************************************
	//* Folder Imports
	//**************************************************
	public void setupFolderImports() {
		List<WatchFolderRecord> records = conn.getWatchFolderTable().getAll();
		
		if (importKeepWatcher == null)
			importKeepWatcher = new QFileSystemWatcher();
		if (importDeleteWatcher == null) {
			importDeleteWatcher = new QFileSystemWatcher();
			for (int i=0; i<records.size(); i++) {
				if (!records.get(i).keep)
					folderImportDelete(records.get(i).folder); 
			}
		}

				
		
//		importKeepWatcher.addPath(records.get(i).folder.replace('\\', '/'));
		for (int i=0; i<records.size(); i++) {
			if (records.get(i).keep) 
				importKeepWatcher.addPath(records.get(i).folder);
			else
				importDeleteWatcher.addPath(records.get(i).folder);
		}
		
		importKeepWatcher.directoryChanged.connect(this, "folderImportKeep(String)");
		importDeleteWatcher.directoryChanged.connect(this, "folderImportDelete(String)");
		
		// Look at the files already there so we don't import them again if a new file is created
		if (importedFiles == null) {
			importedFiles = new ArrayList<String>();
			for (int j=0; j<records.size(); j++) {
				QDir dir = new QDir(records.get(j).folder);
				List<QFileInfo> list = dir.entryInfoList();
				for (int k=0; k<list.size(); k++) {
					if (list.get(k).isFile())
						importedFiles.add(list.get(k).absoluteFilePath());
				}
			}
		}
	}
	public void folderImport() {
		List<WatchFolderRecord> recs = conn.getWatchFolderTable().getAll();
		WatchFolder dialog = new WatchFolder(recs, listManager.getNotebookIndex());
		dialog.exec();
		if (!dialog.okClicked())
			return;
		
		// We have some sort of update.
		if (importKeepWatcher.directories().size() > 0)
			importKeepWatcher.removePaths(importKeepWatcher.directories());
		if (importDeleteWatcher.directories().size() > 0)
			importDeleteWatcher.removePaths(importDeleteWatcher.directories());
		
		conn.getWatchFolderTable().expungeAll();
		// Start building from the table
		for (int i=0; i<dialog.table.rowCount(); i++) {
			QTableWidgetItem item = dialog.table.item(i, 0);
			String dir = item.text();
			item = dialog.table.item(i, 1);
			String notebook = item.text();
			item = dialog.table.item(i, 2);
			boolean keep;
			if (item.text().equalsIgnoreCase("Keep"))
				keep = true;
			else
				keep = false;
			
			String guid = conn.getNotebookTable().findNotebookByName(notebook);
			conn.getWatchFolderTable().addWatchFolder(dir, guid, keep, 0);
		}
		setupFolderImports();
	}
	
	public void folderImportKeep(String dirName) throws NoSuchAlgorithmException {
		
		String whichOS = System.getProperty("os.name");
		if (whichOS.contains("Windows")) 
			dirName = dirName.replace('/','\\');
		
		FileImporter importer = new FileImporter(logger, conn);
		
		QDir dir = new QDir(dirName);
		List<QFileInfo> list = dir.entryInfoList();
		String notebook = conn.getWatchFolderTable().getNotebook(dirName);

		for (int i=0; i<list.size(); i++){
			
			boolean redundant = false;
			// Check if we've already imported this one or if it existed before
			for (int j=0; j<importedFiles.size(); j++) {
				if (importedFiles.get(j).equals(list.get(i).absoluteFilePath()))
					redundant = true;
			}
			
			if (!redundant) {
				importer.setFileInfo(list.get(i));
				importer.setFileName(list.get(i).absoluteFilePath());
			
			
				if (list.get(i).isFile() && importer.isValidType()) {
			
					if (!importer.importFile()) {
						// If we can't get to the file, it is probably locked.  We'll try again later.
						logger.log(logger.LOW, "Unable to save externally edited file.  Saving for later.");
						importFilesKeep.add(list.get(i).absoluteFilePath());
						return;
					}

					Note newNote = importer.getNote();
					newNote.setNotebookGuid(notebook);
					newNote.setTitle(dir.at(i));
					listManager.addNote(newNote);
					conn.getNoteTable().addNote(newNote, true);
					listManager.getUnsynchronizedNotes().add(newNote.getGuid());
					noteTableView.insertRow(listManager, newNote, true, -1);
					listManager.updateNoteContent(newNote.getGuid(), importer.getNoteContent());
					listManager.countNotebookResults(listManager.getNoteIndex());
					importedFiles.add(list.get(i).absoluteFilePath());
				}
			}
		}
        
        
	}
	
	public void folderImportDelete(String dirName) {
		
		String whichOS = System.getProperty("os.name");
		if (whichOS.contains("Windows")) 
			dirName = dirName.replace('/','\\');
		
		FileImporter importer = new FileImporter(logger, conn);
		QDir dir = new QDir(dirName);
		List<QFileInfo> list = dir.entryInfoList();
		String notebook = conn.getWatchFolderTable().getNotebook(dirName);
		
		for (int i=0; i<list.size(); i++){
			importer.setFileInfo(list.get(i));
			importer.setFileName(list.get(i).absoluteFilePath());
			
			if (list.get(i).isFile() && importer.isValidType()) {
		
				if (!importer.importFile()) {
					// If we can't get to the file, it is probably locked.  We'll try again later.
					logger.log(logger.LOW, "Unable to save externally edited file.  Saving for later.");
					importFilesKeep.add(list.get(i).absoluteFilePath());
					return;
				}
		
				Note newNote = importer.getNote();
				newNote.setNotebookGuid(notebook);
				newNote.setTitle(dir.at(i));
				listManager.addNote(newNote);
				conn.getNoteTable().addNote(newNote, true);
				listManager.getUnsynchronizedNotes().add(newNote.getGuid());
				noteTableView.insertRow(listManager, newNote, true, -1);
				listManager.updateNoteContent(newNote.getGuid(), importer.getNoteContent());
				listManager.countNotebookResults(listManager.getNoteIndex());
				dir.remove(dir.at(i));
			}
		}
	}
	
	
	//**************************************************
	//* External events
	//**************************************************
	private void externalFileEdited(String fileName) throws NoSuchAlgorithmException {
		logger.log(logger.HIGH, "Entering exernalFileEdited");
		
		String dPath = Global.getDirectoryPath() + "res/";
		dPath = dPath.replace('\\', '/');
		String name = fileName.replace(dPath, "");
		int pos = name.lastIndexOf('.');
		String guid = name;
		if (pos > -1) {
			guid = guid.substring(0,pos);
		}
		pos = name.lastIndexOf(Global.attachmentNameDelimeter);
		if (pos > -1) {
			guid = name.substring(0, pos);
		}
		
		QFile file = new QFile(fileName);
        if (!file.open(new QIODevice.OpenMode(QIODevice.OpenModeFlag.ReadOnly))) {
        	// If we can't get to the file, it is probably locked.  We'll try again later.
        	logger.log(logger.LOW, "Unable to save externally edited file.  Saving for later.");
        	externalFiles.add(fileName);
        	return;
		}
		QByteArray binData = file.readAll();
        file.close();
        if (binData.size() == 0) {
        	// If we can't get to the file, it is probably locked.  We'll try again later.
        	logger.log(logger.LOW, "Unable to save externally edited file.  Saving for later.");
        	externalFiles.add(fileName);
        	return;
        }
        
        Resource r = conn.getNoteTable().noteResourceTable.getNoteResource(guid, true);
        if (r==null)
        	r = conn.getNoteTable().noteResourceTable.getNoteResource(Global.resourceMap.get(guid), true);
        if (r == null || r.getData() == null || r.getData().getBody() == null)
        	return;
        String oldHash = Global.byteArrayToHexString(r.getData().getBodyHash());
        MessageDigest md = MessageDigest.getInstance("MD5");
		md.update(binData.toByteArray());
		byte[] hash = md.digest();
        String newHash = Global.byteArrayToHexString(hash);
        if (r.getNoteGuid().equalsIgnoreCase(currentNoteGuid)) {
        	updateResourceContentHash(r.getGuid(), oldHash, newHash);
        }
        conn.getNoteTable().updateResourceContentHash(r.getNoteGuid(), oldHash, newHash);
        Data data = r.getData();
        data.setBody(binData.toByteArray());
        data.setBodyHash(hash);
        logger.log(logger.LOW, "externalFileEdited: " +data.getSize() +" bytes");
        r.setData(data);
        conn.getNoteTable().noteResourceTable.updateNoteResource(r,true);
        
        if (r.getNoteGuid().equals(currentNoteGuid)) {
			QWebSettings.setMaximumPagesInCache(0);
			QWebSettings.setObjectCacheCapacities(0, 0, 0);
			refreshEvernoteNote(true);
			browserWindow.getBrowser().triggerPageAction(WebAction.Reload);
        }
        
		logger.log(logger.HIGH, "Exiting externalFielEdited");
	}
	// This is a timer event that tries to save any external files that were edited.  This
	// is only needed if we couldn't save a file earlier.
	public void externalFileEditedSaver() {
		for (int i=externalFiles.size()-1; i>=0; i--) {
			try {
				logger.log(logger.MEDIUM, "Trying to save " +externalFiles.get(i));
				externalFileEdited(externalFiles.get(i));
				externalFiles.remove(i);
			} catch (NoSuchAlgorithmException e) {e.printStackTrace();}
		}
		for (int i=0; i<importFilesKeep.size(); i++) {
			try {
				logger.log(logger.MEDIUM, "Trying to save " +importFilesKeep.get(i));
				folderImportKeep(importFilesKeep.get(i));
				importFilesKeep.remove(i);
			} catch (NoSuchAlgorithmException e) {e.printStackTrace();}
		}
		for (int i=0; i<importFilesDelete.size(); i++) {
			logger.log(logger.MEDIUM, "Trying to save " +importFilesDelete.get(i));
			folderImportDelete(importFilesDelete.get(i));
			importFilesDelete.remove(i);
		}
	}
	
	
	
	
	// If an attachment on the current note was edited, we need to update the current notes's hash
	// Update a note content's hash.  This happens if a resource is edited outside of NN
	public void updateResourceContentHash(String guid, String oldHash, String newHash) {
		int position = browserWindow.getContent().indexOf("en-tag=\"en-media\" guid=\""+guid+"\" type=");
		int endPos;
		for (;position>-1;) {
			endPos = browserWindow.getContent().indexOf(">", position+1);
			String oldSegment = browserWindow.getContent().substring(position,endPos);
			int hashPos = oldSegment.indexOf("hash=\"");
			int hashEnd = oldSegment.indexOf("\"", hashPos+7);
			String hash = oldSegment.substring(hashPos+6, hashEnd);
			if (hash.equalsIgnoreCase(oldHash)) {
				String newSegment = oldSegment.replace(oldHash, newHash);
				String content = browserWindow.getContent().substring(0,position) +
				                 newSegment +
				                 browserWindow.getContent().substring(endPos);
				browserWindow.getBrowser().setContent(new QByteArray(content));;
			}
			
			position = browserWindow.getContent().indexOf("en-tag=\"en-media\" guid=\""+guid+"\" type=", position+1);
		}
	}


	
	
	//*************************************************
	//* Check database userid & passwords
	//*************************************************
	public boolean databaseCheck(String url,String userid, String userPassword, String cypherPassword) {
			Connection connection;
			
			try {
				Class.forName("org.h2.Driver");
			} catch (ClassNotFoundException e1) {
				e1.printStackTrace();
				System.exit(16);
			}

			try {
				String passwordString = null;
				if (cypherPassword==null || cypherPassword.trim().equals(""))
					passwordString = userPassword;
				else
					passwordString = cypherPassword+" "+userPassword;
				connection = DriverManager.getConnection(url,userid,passwordString);
			} catch (SQLException e) {
				return false;
			}
			try {
				connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			return true;
	}

}
