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
package cx.fbn.nevernote;
import java.awt.Desktop;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.SortedMap;
import java.util.Vector;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.thrift.TException;
import org.h2.tools.ChangeFileEncryption;

import com.evernote.edam.error.EDAMNotFoundException;
import com.evernote.edam.error.EDAMSystemException;
import com.evernote.edam.error.EDAMUserException;
import com.evernote.edam.notestore.NoteFilter;
import com.evernote.edam.notestore.NoteVersionId;
import com.evernote.edam.type.Data;
import com.evernote.edam.type.LinkedNotebook;
import com.evernote.edam.type.Note;
import com.evernote.edam.type.NoteAttributes;
import com.evernote.edam.type.Notebook;
import com.evernote.edam.type.Publishing;
import com.evernote.edam.type.QueryFormat;
import com.evernote.edam.type.Resource;
import com.evernote.edam.type.SavedSearch;
import com.evernote.edam.type.Tag;
import com.evernote.edam.type.User;
import com.trolltech.qt.QThread;
import com.trolltech.qt.core.QByteArray;
import com.trolltech.qt.core.QDateTime;
import com.trolltech.qt.core.QDir;
import com.trolltech.qt.core.QEvent;
import com.trolltech.qt.core.QFile;
import com.trolltech.qt.core.QFileInfo;
import com.trolltech.qt.core.QFileSystemWatcher;
import com.trolltech.qt.core.QIODevice;
import com.trolltech.qt.core.QIODevice.OpenModeFlag;
import com.trolltech.qt.core.QLocale;
import com.trolltech.qt.core.QMimeData;
import com.trolltech.qt.core.QModelIndex;
import com.trolltech.qt.core.QSize;
import com.trolltech.qt.core.QTemporaryFile;
import com.trolltech.qt.core.QTextCodec;
import com.trolltech.qt.core.QThreadPool;
import com.trolltech.qt.core.QTimer;
import com.trolltech.qt.core.QTranslator;
import com.trolltech.qt.core.QUrl;
import com.trolltech.qt.core.Qt;
import com.trolltech.qt.core.Qt.BGMode;
import com.trolltech.qt.core.Qt.ItemDataRole;
import com.trolltech.qt.core.Qt.KeyboardModifier;
import com.trolltech.qt.core.Qt.MouseButton;
import com.trolltech.qt.core.Qt.SortOrder;
import com.trolltech.qt.core.Qt.WidgetAttribute;
import com.trolltech.qt.gui.QAbstractItemView;
import com.trolltech.qt.gui.QAbstractItemView.ScrollHint;
import com.trolltech.qt.gui.QAction;
import com.trolltech.qt.gui.QApplication;
import com.trolltech.qt.gui.QClipboard;
import com.trolltech.qt.gui.QCloseEvent;
import com.trolltech.qt.gui.QColor;
import com.trolltech.qt.gui.QComboBox;
import com.trolltech.qt.gui.QCursor;
import com.trolltech.qt.gui.QDesktopServices;
import com.trolltech.qt.gui.QDialog;
import com.trolltech.qt.gui.QFileDialog;
import com.trolltech.qt.gui.QFileDialog.AcceptMode;
import com.trolltech.qt.gui.QFileDialog.FileMode;
import com.trolltech.qt.gui.QGridLayout;
import com.trolltech.qt.gui.QHBoxLayout;
import com.trolltech.qt.gui.QIcon;
import com.trolltech.qt.gui.QImage;
import com.trolltech.qt.gui.QKeySequence;
import com.trolltech.qt.gui.QLabel;
import com.trolltech.qt.gui.QMainWindow;
import com.trolltech.qt.gui.QMenu;
import com.trolltech.qt.gui.QMessageBox;
import com.trolltech.qt.gui.QMessageBox.StandardButton;
import com.trolltech.qt.gui.QPainter;
import com.trolltech.qt.gui.QPalette.ColorRole;
import com.trolltech.qt.gui.QPixmap;
import com.trolltech.qt.gui.QPrintDialog;
import com.trolltech.qt.gui.QPrinter;
import com.trolltech.qt.gui.QShortcut;
import com.trolltech.qt.gui.QSizePolicy;
import com.trolltech.qt.gui.QSizePolicy.Policy;
import com.trolltech.qt.gui.QSpinBox;
import com.trolltech.qt.gui.QSplashScreen;
import com.trolltech.qt.gui.QSplitter;
import com.trolltech.qt.gui.QStatusBar;
import com.trolltech.qt.gui.QSystemTrayIcon;
import com.trolltech.qt.gui.QTableWidgetItem;
import com.trolltech.qt.gui.QTextEdit;
import com.trolltech.qt.gui.QToolBar;
import com.trolltech.qt.gui.QTreeWidgetItem;
import com.trolltech.qt.network.QNetworkAccessManager;
import com.trolltech.qt.network.QNetworkProxy;
import com.trolltech.qt.network.QNetworkProxy.ProxyType;
import com.trolltech.qt.network.QNetworkReply;
import com.trolltech.qt.network.QNetworkRequest;
import com.trolltech.qt.webkit.QWebPage.WebAction;
import com.trolltech.qt.webkit.QWebSettings;

import cx.fbn.nevernote.config.InitializationException;
import cx.fbn.nevernote.config.StartupConfig;
import cx.fbn.nevernote.dialog.AccountDialog;
import cx.fbn.nevernote.dialog.ConfigDialog;
import cx.fbn.nevernote.dialog.DBEncryptDialog;
import cx.fbn.nevernote.dialog.DatabaseLoginDialog;
import cx.fbn.nevernote.dialog.DatabaseStatus;
import cx.fbn.nevernote.dialog.FindDialog;
import cx.fbn.nevernote.dialog.IgnoreSync;
import cx.fbn.nevernote.dialog.LogFileDialog;
import cx.fbn.nevernote.dialog.NotebookArchive;
import cx.fbn.nevernote.dialog.NotebookEdit;
import cx.fbn.nevernote.dialog.OnlineNoteHistory;
import cx.fbn.nevernote.dialog.PublishNotebook;
import cx.fbn.nevernote.dialog.SavedSearchEdit;
import cx.fbn.nevernote.dialog.SetIcon;
import cx.fbn.nevernote.dialog.ShareNotebook;
import cx.fbn.nevernote.dialog.SharedNotebookSyncError;
import cx.fbn.nevernote.dialog.StackNotebook;
import cx.fbn.nevernote.dialog.SynchronizationRequiredWarning;
import cx.fbn.nevernote.dialog.TagEdit;
import cx.fbn.nevernote.dialog.TagMerge;
import cx.fbn.nevernote.dialog.ThumbnailViewer;
import cx.fbn.nevernote.dialog.UpgradeAvailableDialog;
import cx.fbn.nevernote.dialog.WatchFolder;
import cx.fbn.nevernote.evernote.NoteMetadata;
import cx.fbn.nevernote.filters.FilterEditorNotebooks;
import cx.fbn.nevernote.filters.FilterEditorTags;
import cx.fbn.nevernote.gui.AttributeTreeWidget;
import cx.fbn.nevernote.gui.BrowserWindow;
import cx.fbn.nevernote.gui.DateAttributeFilterTable;
import cx.fbn.nevernote.gui.ExternalBrowse;
import cx.fbn.nevernote.gui.MainMenuBar;
import cx.fbn.nevernote.gui.NotebookTreeWidget;
import cx.fbn.nevernote.gui.SavedSearchTreeWidget;
import cx.fbn.nevernote.gui.SearchPanel;
import cx.fbn.nevernote.gui.TableView;
import cx.fbn.nevernote.gui.TagTreeWidget;
import cx.fbn.nevernote.gui.Thumbnailer;
import cx.fbn.nevernote.gui.TrashTreeWidget;
import cx.fbn.nevernote.gui.controls.QuotaProgressBar;
import cx.fbn.nevernote.oauth.OAuthTokenizer;
import cx.fbn.nevernote.oauth.OAuthWindow;
import cx.fbn.nevernote.sql.DatabaseConnection;
import cx.fbn.nevernote.sql.WatchFolderRecord;
import cx.fbn.nevernote.threads.IndexRunner;
import cx.fbn.nevernote.threads.SyncRunner;
import cx.fbn.nevernote.threads.ThumbnailRunner;
import cx.fbn.nevernote.utilities.AESEncrypter;
import cx.fbn.nevernote.utilities.ApplicationLogger;
import cx.fbn.nevernote.utilities.FileImporter;
import cx.fbn.nevernote.utilities.FileUtils;
import cx.fbn.nevernote.utilities.ListManager;
import cx.fbn.nevernote.utilities.SyncTimes;
import cx.fbn.nevernote.xml.ExportData;
import cx.fbn.nevernote.xml.ImportData;
import cx.fbn.nevernote.xml.ImportEnex;
import cx.fbn.nevernote.xml.NoteFormatter;


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
	QNetworkAccessManager	versionChecker;				// Used when checking for new versions
	
    NotebookTreeWidget 		notebookTree;     			// List of notebooks
    AttributeTreeWidget		attributeTree;				// List of note attributes
    TagTreeWidget 			tagTree;					// list of user created tags
    SavedSearchTreeWidget	savedSearchTree;			// list of saved searches
    TrashTreeWidget			trashTree;					// Trashcan
    TableView	 			noteTableView;				// 	List of notes (the widget).

    public BrowserWindow	browserWindow;				// Window containing browser & labels
    public QToolBar 		toolBar;					// The tool bar under the menu
    QComboBox				searchField;				// search filter bar on the toolbar;
    QShortcut				searchShortcut;				// Shortcut to search bar
    boolean					searchPerformed = false;	// Search was done?
    QuotaProgressBar		quotaBar;					// The current quota usage
    
    ApplicationLogger		logger;
    List<String>			selectedNotebookGUIDs;  	// List of notebook GUIDs
    List<String>			selectedTagGUIDs;			// List of selected tag GUIDs
    List<String>			selectedNoteGUIDs;			// List of selected notes
    String					selectedSavedSearchGUID;	// Currently selected saved searches
    private final HashMap<String, ExternalBrowse>	externalWindows;	// Notes being edited by an external window;
    
    NoteFilter				filter;						// Note filter
    String					currentNoteGuid;			// GUID of the current note 
    Note 					currentNote;				// The currently viewed note
    boolean					noteDirty;					// Has the note been changed?
    boolean 				inkNote;                   // if this is an ink note, it is read only
    boolean					readOnly;					// Is this note read-only?
	
  
    ListManager				listManager;					// DB runnable task
    
    List<QTemporaryFile>	tempFiles;					// Array of temporary files;
    
    QTimer					indexTimer;					// timer to start the index thread
    IndexRunner				indexRunner;				// thread to index notes
    QThread					indexThread;
    
    QTimer					syncTimer;					// Sync on an interval
    QTimer					syncDelayTimer;				// Sync delay to free up database
    SyncRunner				syncRunner;					// thread to do a sync.
    QThread					syncThread;					// Thread which talks to evernote
    ThumbnailRunner			thumbnailRunner;			// Runner for thumbnail thread
    QThread					thumbnailThread;			// Thread that generates pretty pictures
    QTimer					saveTimer;					// Timer to save note contents
    
    QTimer					authTimer;					// Refresh authentication
    QTimer					externalFileSaveTimer;		// Save files altered externally
    QTimer					thumbnailTimer;				// Wakeup & scan for thumbnails
    QTimer					debugTimer;
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
    QAction				allNotesButton;				// Reset & view all notes
    QTimer				synchronizeAnimationTimer;	// Timer to change animation button
    int					synchronizeIconAngle;		// Used to rotate sync icon
    QAction 			printButton;				// Print Button
    QAction				tagButton;					// Tag edit button
    QAction				attributeButton;			// Attribute information button
    QAction 			emailButton;				// Email button
    QAction 			deleteButton;	 			// Delete button
    QAction				newButton;					// new Note Button;
    QSpinBox			zoomSpinner;				// Zoom zoom
    QAction				searchClearButton;			// Clear the search field
    
    SearchPanel			searchLayout;				// Widget to hold search field, zoom, & quota
    
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
    boolean				disableTagThreadCheck=false;
    boolean				disableNotebookThreadCheck=false;
    boolean				disableTrashThreadCheck=false;
    boolean				disableSaveThreadCheck=false;
    boolean				disableSyncThreadCheck=false;
    boolean				disableIndexThreadCheck=false;
    
    HashMap<String, String>		noteCache;			// Cash of note content	
    HashMap<String, Boolean>	readOnlyCache;		// List of cashe notes that are read-only
    HashMap<String, Boolean>	inkNoteCache;		// List of cache notes that are ink notes 
    List<String>		historyGuids;				// GUIDs of previously viewed items
    int					historyPosition;			// Position within the viewed items
    boolean				fromHistory;				// Is this from the history queue?
    String 				trashNoteGuid;				// Guid to restore / set into or out of trash to save position
    List<Thumbnailer>	thumbGenerators;				// generate preview image
    ThumbnailViewer		thumbnailViewer;			// View preview thumbnail; 
    boolean				encryptOnShutdown;			// should I encrypt when I close?
    boolean				decryptOnShutdown;			// should I decrypt on shutdown;
    String				encryptCipher;				// What cipher should I use?
    //Signal0 			minimizeToTray;
    boolean				windowMaximized = false;	// Keep track of the window state for restores
    List<String>		pdfReadyQueue;				// Queue of PDFs that are ready to be rendered.
    List<QPixmap>		syncIcons;					// Array of icons used in sync animation
    private boolean		closeAction = false;		// Used to say when to close or when to minimize
    private static Logger log = Logger.getLogger(NeverNote.class); 
    private String 		saveLastPath;				// last path we used
    private final QTimer		messageTimer;				// Timer to clear the status message.
    private QTimer		blockTimer;
    BrowserWindow		blockingWindow;
    
    String iconPath = new String("classpath:cx/fbn/nevernote/icons/");
    	
	
    //***************************************************************
    //***************************************************************
    //** Constructor & main entry point
    //***************************************************************
    //***************************************************************
    // Application Constructor	
	@SuppressWarnings("static-access")
	public NeverNote(DatabaseConnection dbConn)  {
		conn = dbConn;		
		if (conn.getConnection() == null) {
			String msg = new String(tr("Unable to connect to the database.\n\nThe most probable reason is that some other process\n" +
				"is accessing the database or NixNote is already running.\n\n" +
				"Please end any other process or shutdown the other NixNote before starting.\n\nExiting program."));
			
            QMessageBox.critical(null, tr("Database Connection Error") ,msg);
			System.exit(16);
		}
		setObjectName("mainWindow");
//		thread().setPriority(Thread.MAX_PRIORITY);
		
		logger = new ApplicationLogger("nevernote.log");
		logger.log(logger.HIGH, "Starting Application");
		
		decryptOnShutdown = false;
		encryptOnShutdown = false;
		conn.checkDatabaseVersion();
		
		
		
		// Start building the invalid XML tables
		Global.invalidElements = conn.getInvalidXMLTable().getInvalidElements();
		List<String> elements = conn.getInvalidXMLTable().getInvalidAttributeElements();
		
		for (int i=0; i<elements.size(); i++) {
			Global.invalidAttributes.put(elements.get(i), conn.getInvalidXMLTable().getInvalidAttributes(elements.get(i)));
		}
		
		logger.log(logger.EXTREME, "Starting GUI build");

		QTranslator nevernoteTranslator = new QTranslator();
		nevernoteTranslator.load(Global.getFileManager().getTranslateFilePath("nevernote_" + QLocale.system().name() + ".qm"));
		QApplication.instance().installTranslator(nevernoteTranslator);

		Global.originalPalette = QApplication.palette();
		QApplication.setStyle(Global.getStyle());
		if (Global.useStandardPalette())
			QApplication.setPalette(QApplication.style().standardPalette());
        setWindowTitle(tr("NixNote"));

        mainLeftRightSplitter = new QSplitter();
        setCentralWidget(mainLeftRightSplitter);
        leftSplitter1 = new QSplitter();
        leftSplitter1.setOrientation(Qt.Orientation.Vertical);
                
        browserIndexSplitter = new QSplitter();
        browserIndexSplitter.setOrientation(Qt.Orientation.Vertical);
        
        //* Setup threads & thread timers
//        int indexRunnerCount = Global.getIndexThreads();
//       indexRunnerCount = 1;
        QThreadPool.globalInstance().setMaxThreadCount(Global.threadCount);	// increase max thread count

		logger.log(logger.EXTREME, "Building list manager");
        listManager = new ListManager(conn, logger);
        
		logger.log(logger.EXTREME, "Building index runners & timers");
        indexRunner = new IndexRunner("indexRunner.log", 
        		Global.getDatabaseUrl(), Global.getIndexDatabaseUrl(), Global.getResourceDatabaseUrl(),
        		Global.getDatabaseUserid(), Global.getDatabaseUserPassword(), Global.cipherPassword);
		indexThread = new QThread(indexRunner, "Index Thread");
        indexRunner.indexAttachmentsLocally = Global.indexAttachmentsLocally();
        indexRunner.indexImageRecognition = Global.indexImageRecognition();
        indexRunner.indexNoteBody = Global.indexNoteBody();
        indexRunner.indexNoteTitle = Global.indexNoteTitle();
        indexRunner.specialIndexCharacters = Global.getSpecialIndexCharacters();
		indexThread.start();
		
        synchronizeAnimationTimer = new QTimer();
        synchronizeAnimationTimer.timeout.connect(this, "updateSyncButton()");
        
		indexTimer = new QTimer();
		indexTime = 1000*Global.getIndexThreadSleepInterval();  
		indexTimer.start(indexTime);  // Start indexing timer
		indexTimer.timeout.connect(this, "indexTimer()");
		indexDisabled = false;
		indexRunning = false;
				
		logger.log(logger.EXTREME, "Setting sync thread & timers");
		syncThreadsReady=1;
		syncRunner = new SyncRunner("syncRunner.log", 
				Global.getDatabaseUrl(), Global.getIndexDatabaseUrl(), Global.getResourceDatabaseUrl(),
				Global.getDatabaseUserid(), Global.getDatabaseUserPassword(), Global.cipherPassword);
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
		
		
		logger.log(logger.EXTREME, "Starting thumnail thread");
		pdfReadyQueue = new ArrayList<String>();
		thumbnailRunner = new ThumbnailRunner("thumbnailRunner.log", 
				Global.getDatabaseUrl(), Global.getIndexDatabaseUrl(), Global.getResourceDatabaseUrl(),
				Global.getDatabaseUserid(), Global.getDatabaseUserPassword(), Global.cipherPassword);
		thumbnailThread = new QThread(thumbnailRunner, "Thumbnail Thread");
		thumbnailRunner.noteSignal.thumbnailPageReady.connect(this, "thumbnailHTMLReady(String,QByteArray,Integer)");
		thumbnailThread.start();
		thumbGenerators = new ArrayList<Thumbnailer>();
		thumbnailTimer = new QTimer();
		thumbnailTimer.timeout.connect(this, "thumbnailTimer()");
		thumbnailTimer();
		thumbnailTimer.setInterval(500*1000);  // Thumbnail every minute
		thumbnailTimer.start();
		
//		debugTimer = new QTimer();
//		debugTimer.timeout.connect(this, "debugDirty()");
//		debugTimer.start(1000*60);
		
		logger.log(logger.EXTREME, "Starting authentication timer");
		authTimer = new QTimer();
		authTimer.timeout.connect(this, "authTimer()");
		authTimer.start(1000*60*15);
		syncRunner.syncSignal.authRefreshComplete.connect(this, "authRefreshComplete(boolean)");
		
		logger.log(logger.EXTREME, "Setting save note timer");
		saveTimer = new QTimer();
		saveTimer.timeout.connect(this, "saveNote()");
		if (Global.getAutoSaveInterval() > 0) {
			saveTimer.setInterval(1000*60*Global.getAutoSaveInterval()); 
			saveTimer.start();
		}
		listManager.saveRunner.noteSignals.noteSaveRunnerError.connect(this, "saveRunnerError(String, String)");
		
		logger.log(logger.EXTREME, "Starting external file monitor timer");
		externalFileSaveTimer = new QTimer();
		externalFileSaveTimer.timeout.connect(this, "externalFileEditedSaver()");
		externalFileSaveTimer.setInterval(1000*5);   // save every 5 seconds;
		externalFiles = new ArrayList<String>();
		importFilesDelete = new ArrayList<String>();
		importFilesKeep = new ArrayList<String>();
		externalFileSaveTimer.start();
		
        notebookTree = new NotebookTreeWidget(conn);
        attributeTree = new AttributeTreeWidget();
        tagTree = new TagTreeWidget(conn);
        savedSearchTree = new SavedSearchTreeWidget();
        trashTree = new TrashTreeWidget();
        noteTableView = new TableView(logger, listManager);        
        
        searchField = new QComboBox();
        searchField.setObjectName("searchField");
        //setStyleSheet("QComboBox#searchField { background-color: yellow }");
        searchField.setEditable(true);
    	searchField.activatedIndex.connect(this, "searchFieldChanged()");
    	searchField.setDuplicatesEnabled(false);
    	searchField.editTextChanged.connect(this,"searchFieldTextChanged(String)");
    	searchShortcut = new QShortcut(this);
    	setupShortcut(searchShortcut, "Focus_Search");
    	searchShortcut.activated.connect(this, "focusSearch()");
        
    	quotaBar = new QuotaProgressBar();
    	// Setup the zoom
    	zoomSpinner = new QSpinBox();
    	zoomSpinner.setMinimum(10);
    	zoomSpinner.setMaximum(1000);
    	zoomSpinner.setAccelerated(true);
    	zoomSpinner.setSingleStep(10);
    	zoomSpinner.setValue(100);
    	zoomSpinner.valueChanged.connect(this, "zoomChanged()");
    	
    	searchLayout = new SearchPanel(searchField, quotaBar, notebookTree, zoomSpinner);
        
        
        QGridLayout leftGrid = new QGridLayout();
        leftSplitter1.setContentsMargins(5, 0, 0, 7);
        leftSplitter1.setLayout(leftGrid);
    	leftGrid.addWidget(searchLayout,1,1);
        leftGrid.addWidget(tagTree,2,1);
        leftGrid.addWidget(attributeTree,3,1);
        leftGrid.addWidget(savedSearchTree,4,1);
        leftGrid.addWidget(trashTree,5, 1);
        
        // Setup the browser window
        noteCache = new HashMap<String,String>();
        readOnlyCache = new HashMap<String, Boolean>();
        inkNoteCache = new HashMap<String, Boolean>();
        browserWindow = new BrowserWindow(conn);

        mainLeftRightSplitter.addWidget(leftSplitter1);
        mainLeftRightSplitter.addWidget(browserIndexSplitter);
        
        if (Global.getListView() == Global.View_List_Wide) {
        	browserIndexSplitter.addWidget(noteTableView);
        	browserIndexSplitter.addWidget(browserWindow); 
        } else {
        	mainLeftRightSplitter.addWidget(noteTableView);
        	mainLeftRightSplitter.addWidget(browserWindow); 
        }
    	
    	// Setup the thumbnail viewer
    	thumbnailViewer = new ThumbnailViewer();
    	thumbnailViewer.upArrow.connect(this, "upAction()");
    	thumbnailViewer.downArrow.connect(this, "downAction()");
    	thumbnailViewer.leftArrow.connect(this, "nextViewedAction()");
    	thumbnailViewer.rightArrow.connect(this, "previousViewedAction()");
    	
    	//Setup external browser manager
    	externalWindows = new HashMap<String, ExternalBrowse>();

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
		tagTree.setMergeAction(menuBar.tagMergeAction);
		tagTree.setEditAction(menuBar.tagEditAction);
		tagTree.setAddAction(menuBar.tagAddAction);
		tagTree.setIconAction(menuBar.tagIconAction);
		tagTree.setVisible(Global.isWindowVisible("tagTree"));
		leftSplitter1.setVisible(Global.isWindowVisible("leftPanel"));
		tagTree.noteSignal.tagsAdded.connect(this, "tagsAdded(String, String)");
		menuBar.hideTags.setChecked(Global.isWindowVisible("tagTree"));
		listManager.tagSignal.listChanged.connect(this, "reloadTagTree()");
		
		if (!Global.isWindowVisible("zoom")) {
			searchLayout.hideZoom();
			menuBar.hideZoom.setChecked(false);
		} 
	
		notebookTree.setDeleteAction(menuBar.notebookDeleteAction);
		notebookTree.setEditAction(menuBar.notebookEditAction);
		notebookTree.setAddAction(menuBar.notebookAddAction);
		notebookTree.setIconAction(menuBar.notebookIconAction);
		notebookTree.setStackAction(menuBar.notebookStackAction);
		notebookTree.setPublishAction(menuBar.notebookPublishAction);
		notebookTree.setShareAction(menuBar.notebookShareAction);
		notebookTree.setVisible(Global.isWindowVisible("notebookTree"));
		notebookTree.noteSignal.notebookChanged.connect(this, "updateNoteNotebook(String, String)");
		notebookTree.noteSignal.tagsChanged.connect(this, "updateNoteTags(String, List)");
	    notebookTree.noteSignal.tagsChanged.connect(this, "updateListTags(String, List)");
		menuBar.hideNotebooks.setChecked(Global.isWindowVisible("notebookTree"));

		savedSearchTree.setAddAction(menuBar.savedSearchAddAction);
		savedSearchTree.setEditAction(menuBar.savedSearchEditAction);
		savedSearchTree.setDeleteAction(menuBar.savedSearchDeleteAction);
		savedSearchTree.setIconAction(menuBar.savedSearchIconAction);
		savedSearchTree.itemSelectionChanged.connect(this, "updateSavedSearchSelection()");
		savedSearchTree.setVisible(Global.isWindowVisible("savedSearchTree"));
		menuBar.hideSavedSearches.setChecked(Global.isWindowVisible("savedSearchTree"));
			
		noteTableView.setAddAction(menuBar.noteAdd);
		noteTableView.setDeleteAction(menuBar.noteDelete);
		noteTableView.setRestoreAction(menuBar.noteRestoreAction);
		noteTableView.setNoteDuplicateAction(menuBar.noteDuplicateAction);
		noteTableView.setNoteHistoryAction(menuBar.noteOnlineHistoryAction);
		noteTableView.noteSignal.titleColorChanged.connect(this, "titleColorChanged(Integer)");
		noteTableView.noteSignal.notePinned.connect(this, "notePinned()");
		noteTableView.setMergeNotesAction(menuBar.noteMergeAction);
		noteTableView.setCopyAsUrlAction(menuBar.noteCopyAsUrlAction);
		noteTableView.doubleClicked.connect(this, "listDoubleClick()");
		listManager.trashSignal.countChanged.connect(trashTree, "updateCounts(Integer)");
		
		quotaBar.setMouseClickAction(menuBar.accountAction);
		
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
		if (Global.isWindowVisible("noteInformation"))
			toggleNoteInformation();
		quotaBar.setVisible(Global.isWindowVisible("quota"));
		if (!quotaBar.isVisible())
			menuBar.hideQuota.setChecked(false);
		searchField.setVisible(Global.isWindowVisible("searchField"));
		if (!searchField.isVisible())
			menuBar.hideSearch.setChecked(false);
		
		if (searchField.isHidden() && quotaBar.isHidden() && zoomSpinner.isHidden() && notebookTree.isHidden())
			searchLayout.hide();
		
		setMenuBar(menuBar);
		setupToolBar();
		find = new FindDialog();
		find.getOkButton().clicked.connect(this, "doFindText()");
		
		// Setup the tray icon menu bar
		trayShowAction = new QAction(tr("Show/Hide"), this);
		trayExitAction = new QAction(tr("Exit"), this);
		trayAddNoteAction = new QAction(tr("Add Note"), this);
		
		trayExitAction.triggered.connect(this, "closeNeverNote()");
		trayAddNoteAction.triggered.connect(this, "addNote()");
		trayShowAction.triggered.connect(this, "trayToggleVisible()");
		
		trayMenu = new QMenu(this);
		trayMenu.addAction(trayAddNoteAction);
		trayMenu.addAction(trayShowAction);
		trayMenu.addAction(trayExitAction);
		
		
		trayIcon = new QSystemTrayIcon(this);
		trayIcon.setToolTip(tr("NixNote"));
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
        	showEditorButtons(browserWindow);
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
		if (QSystemTrayIcon.isSystemTrayAvailable()) {
			setWindowIcon(appIcon);
			trayIcon.setIcon(appIcon);
			if (Global.showTrayIcon() || Global.minimizeOnClose())
				trayIcon.show();
			else
				trayIcon.hide();
		}
    	
    	scrollToGuid(currentNoteGuid);
    	if (Global.automaticLogin()) {
    		remoteConnect();
    		if (Global.isConnected)
    			syncTimer();
    	}
    	setupFolderImports();
    	
    	loadStyleSheet();
    	restoreWindowState(true);
    	
    	if (Global.mimicEvernoteInterface) {
    		notebookTree.selectGuid("");
    	}
    	
    	threadMonitorTimer = new QTimer();
    	threadMonitorTimer.timeout.connect(this, "threadMonitorCheck()");
    	threadMonitorTimer.start(1000*10);  // Check for threads every 10 seconds;	   	
    	
    	historyGuids.add(currentNoteGuid);
    	historyPosition = 1;
    	
    	menuBar.blockSignals(true);
    	menuBar.narrowListView.blockSignals(true);
    	menuBar.wideListView.blockSignals(true);
        if (Global.getListView() == Global.View_List_Narrow) { 
        	menuBar.narrowListView.setChecked(true);
        }
        else{ 
        	menuBar.wideListView.setChecked(true);
        }
        menuBar.blockSignals(false);
    	menuBar.narrowListView.blockSignals(false);
    	menuBar.wideListView.blockSignals(false);

        if (Global.getListView() == Global.View_List_Wide) {
        	browserIndexSplitter.addWidget(noteTableView);
        	browserIndexSplitter.addWidget(browserWindow); 
        } else {
        	mainLeftRightSplitter.addWidget(noteTableView);
        	mainLeftRightSplitter.addWidget(browserWindow); 
        }
        
		messageTimer = new QTimer();
		messageTimer.timeout.connect(this, "clearMessage()");
		messageTimer.setInterval(1000*15);
		clearMessage();
        
    	int sortCol = Global.getSortColumn();
		int sortOrder = Global.getSortOrder();
		noteTableView.proxyModel.blocked = true;
		// We sort the table twice to fix a bug.  For some reaosn the table won't sort properly if it is in narrow
		// list view and sorted descending on the date  created.  By sorting it twice it forces the proper sort.  Ugly.
		if (sortCol == 0 && sortOrder == 1 && Global.getListView() == Global.View_List_Narrow) 
			noteTableView.sortByColumn(sortCol, SortOrder.resolve(0));   
		noteTableView.sortByColumn(sortCol, SortOrder.resolve(sortOrder));
		noteTableView.proxyModel.blocked = false;
		noteTableView.proxyModel.sortChanged.connect(this, "tableSortOrderChanged(Integer,Integer)");
		
		// Set the startup notebook
    	String defaultNotebook = Global.getStartupNotebook();
    	if (!defaultNotebook.equals("AllNotebooks") && !defaultNotebook.equals("")) {
    		for (int k=0; k<listManager.getNotebookIndex().size(); k++) {
    			if (listManager.getNotebookIndex().get(k).isDefaultNotebook()) {
    				notebookTree.clearSelection();
    				notebookTree.selectGuid(listManager.getNotebookIndex().get(k).getGuid());
    				notebookTree.selectionSignal.emit();
    			}
    		}
    	}
		
   
			
			
		if (Global.checkVersionUpgrade())
			checkForUpdates();
	}
	
	
	public void debugDirty() {
		List<Note> dirty = conn.getNoteTable().getDirty();
		logger.log(logger.LOW, "------ Dirty Notes List Begin ------");
		for (int i=0; i<dirty.size(); i++) {
			logger.log(logger.LOW, "GUID: " +dirty.get(i).getGuid() + " Title:" + dirty.get(i).getTitle());
		}
		logger.log(logger.LOW, "------ Dirty Notes List End ------");
	}
		
	// Main entry point
	public static void main(String[] args) {
		log.setLevel(Level.FATAL);
		QApplication.initialize(args);
		QPixmap pixmap = new QPixmap("classpath:cx/fbn/nevernote/icons/splash_logo.png");
		QSplashScreen splash = new QSplashScreen(pixmap);
		boolean showSplash;
		
		DatabaseConnection dbConn;

        try {
            initializeGlobalSettings(args);

            showSplash = Global.isWindowVisible("SplashScreen");
            if (showSplash)
                splash.show();

            dbConn = setupDatabaseConnection();

            // Must be last stage of setup - only safe once DB is open hence we know we are the only instance running
            Global.getFileManager().purgeResDirectory(true);

        } catch (InitializationException e) {
            // Fatal
            e.printStackTrace();
            QMessageBox.critical(null, "Startup error", "Aborting: " + e.getMessage());
            return;
        }
        
		// Setup proxy crap
		String proxyUrl = Global.getProxyValue("url");
		String proxyPort = Global.getProxyValue("port");
		String proxyUserid = Global.getProxyValue("userid");
		String proxyPassword = Global.getProxyValue("password");
		boolean proxySet = false;
		QNetworkProxy proxy = new QNetworkProxy();
		proxy.setType(ProxyType.HttpProxy);
		if (!proxyUrl.trim().equals("")) {
			System.out.println("Proxy URL found: " +proxyUrl);
			proxySet = true;
			proxy.setHostName(proxyUrl);
		}
		if (!proxyPort.trim().equals("")) {
			System.out.println("Proxy Port found: " +proxyPort);
			proxySet = true;
			proxy.setPort(Integer.parseInt(proxyPort));
		}
		if (!proxyUserid.trim().equals("")) {
			System.out.println("Proxy Userid found: " +proxyUserid);
			proxySet = true;
			proxy.setUser(proxyUserid);
		}
		if (!proxyPassword.trim().equals("")) {
			System.out.println("Proxy URL found: " +proxyPassword);
			proxySet = true;
			proxy.setPassword(proxyPassword);
		}
		if (proxySet) {
			QNetworkProxy.setApplicationProxy(proxy);
		}
			

        NeverNote application = new NeverNote(dbConn);
		if (Global.syncOnly) {
			System.out.println("Performing synchronization only.");
			application.remoteConnect();
			if (Global.isConnected) {
				application.syncRunner.syncNeeded = true;
				application.syncRunner.addWork("SYNC");
				application.syncRunner.addWork("STOP");
				while(!application.syncRunner.isIdle());
				application.closeNeverNote();
			}
			return;
		}

		application.setAttribute(WidgetAttribute.WA_DeleteOnClose, true);
		if (Global.startMinimized()) 
			application.showMinimized();
		else {
			if (Global.wasWindowMaximized())
				application.showMaximized();
			else
				application.show();
		}
		
		if (showSplash)
			splash.finish(application);
		QApplication.exec();
		System.out.println("Goodbye.");
		QApplication.exit();
	}

    /**
     * Open the internal database, or create if not present
     *
     * @throws InitializationException when opening the database fails, e.g. because another process has it locked
     */
    private static DatabaseConnection setupDatabaseConnection() throws InitializationException {
    	ApplicationLogger logger = new ApplicationLogger("nevernote-database.log");
    	
    	File f = Global.getFileManager().getDbDirFile(Global.databaseName + ".h2.db");
    	File fr = Global.getFileManager().getDbDirFile(Global.resourceDatabaseName + ".h2.db");
    	File fi = Global.getFileManager().getDbDirFile(Global.resourceDatabaseName + ".h2.db");
		if (!f.exists())
			Global.setDatabaseUrl("");
		if (!fr.exists())
			Global.setResourceDatabaseUrl("");		
		if (!fi.exists())
			Global.setIndexDatabaseUrl("");	
    	
        if (Global.getDatabaseUrl().toUpperCase().indexOf("CIPHER=") > -1) {
            boolean goodCheck = false;
            while (!goodCheck) {
                DatabaseLoginDialog dialog = new DatabaseLoginDialog();
                dialog.exec();
                if (!dialog.okPressed())
                    System.exit(0);
                Global.cipherPassword = dialog.getPassword();
                goodCheck = databaseCheck(Global.getDatabaseUrl(), Global.getDatabaseUserid(),
                        Global.getDatabaseUserPassword(), Global.cipherPassword);
            }
        }
       	DatabaseConnection dbConn = new DatabaseConnection(logger,Global.getDatabaseUrl(), 
       			Global.getIndexDatabaseUrl(), Global.getResourceDatabaseUrl(),
       			Global.getDatabaseUserid(), Global.getDatabaseUserPassword(), Global.cipherPassword, 0);
       return dbConn;
    }
    
    // Encrypt the database upon shutdown
    private void encryptOnShutdown() {
        String dbPath= Global.getFileManager().getDbDirPath("");
        try {
        	
        	Statement st = conn.getConnection().createStatement();	
        	st.execute("shutdown");
        	st = conn.getResourceConnection().createStatement();
        	st.execute("shutdown");
        	st = conn.getIndexConnection().createStatement();
        	st.execute("shutdown");
        	if (QMessageBox.question(this, tr("Are you sure"), 
        			tr("Are you sure you wish to encrypt the database?"),
        			QMessageBox.StandardButton.Yes, 
    				QMessageBox.StandardButton.No) == StandardButton.Yes.value()) {
        		ChangeFileEncryption.execute(dbPath, "NeverNote", encryptCipher, null, Global.cipherPassword.toCharArray(), true);
        		ChangeFileEncryption.execute(dbPath, "Resources", encryptCipher, null, Global.cipherPassword.toCharArray(), true);
        		ChangeFileEncryption.execute(dbPath, "Index", encryptCipher, null, Global.cipherPassword.toCharArray(), true);
        		Global.setDatabaseUrl(Global.getDatabaseUrl() + ";CIPHER="+encryptCipher);
        		Global.setResourceDatabaseUrl(Global.getResourceDatabaseUrl() + ";CIPHER="+encryptCipher);
        		Global.setIndexDatabaseUrl(Global.getIndexDatabaseUrl() + ";CIPHER="+encryptCipher);

        		QMessageBox.information(this, tr("Encryption Complete"), tr("Encryption is complete"));
        	}
        } catch (SQLException e) {
			e.printStackTrace();
		}    	
    }
    
    // Decrypt the database upon shutdown
    private void decryptOnShutdown() {
        String dbPath= Global.getFileManager().getDbDirPath("");
        String dbName = "NeverNote";
        try {
        	Statement st = conn.getConnection().createStatement();	
        	st.execute("shutdown");
        	if (Global.getDatabaseUrl().toUpperCase().indexOf(";CIPHER=AES") > -1)
        		encryptCipher = "AES";
        	else
        		encryptCipher = "XTEA";
        	if (QMessageBox.question(this, tr("Confirmation"), tr("Are you sure", 
        			"Are you sure you wish to decrypt the database?"),
        			QMessageBox.StandardButton.Yes, 
    				QMessageBox.StandardButton.No) == StandardButton.Yes.value()) {

        		ChangeFileEncryption.execute(dbPath, dbName, encryptCipher, Global.cipherPassword.toCharArray(), null, true);
        		Global.setDatabaseUrl("");
        		Global.setResourceDatabaseUrl("");
        		Global.setIndexDatabaseUrl("");
        		QMessageBox.information(this, tr("Decryption Complete"), tr("Decryption is complete"));
        	}
		} catch (SQLException e) {
			e.printStackTrace();
		}    	
    }
    /**
     * Encrypt/Decrypt the local database
     **/
    public void doDatabaseEncrypt() {
    	// The database is not currently encrypted
        if (Global.getDatabaseUrl().toUpperCase().indexOf("CIPHER=") == -1) {
        	if (QMessageBox.question(this, tr("Confirmation"), tr("Encrypting the database is used" +
    				"to enhance security and is performed\nupon shutdown, but please be aware that if"+
    				" you lose the password your\nis lost forever.\n\nIt is highly recommended you " +
    				"perform a backup and/or fully synchronize\n prior to executing this funtction.\n\n" +
    				"Do you wish to proceed?"),
    				QMessageBox.StandardButton.Yes, 
    				QMessageBox.StandardButton.No)==StandardButton.No.value()) {
    				return;
    		}
        	DBEncryptDialog dialog = new DBEncryptDialog();
        	dialog.exec();
        	if (dialog.okPressed()) {
           		Global.cipherPassword = dialog.getPassword();
           		encryptOnShutdown  = true;
           		encryptCipher = dialog.getEncryptionMethod();
        	}
        } else {
            DBEncryptDialog dialog = new DBEncryptDialog();
            dialog.setWindowTitle(tr("Database Decryption"));
            dialog.hideEncryption();
            dialog.exec();
            if (dialog.okPressed()) {
            	if (!dialog.getPassword().equals(Global.cipherPassword)) {
            		QMessageBox.critical(null, tr("Incorrect Password"), tr("Incorrect Password"));
            		return;
            	}
            	decryptOnShutdown  = true;
            	encryptCipher = "";
            }
        }
        return;
    }

	private static void initializeGlobalSettings(String[] args) throws InitializationException {
		StartupConfig	startupConfig = new StartupConfig();

        for (String arg : args) {
            String lower = arg.toLowerCase();
            if (lower.startsWith("--name="))
               startupConfig.setName(arg.substring(arg.indexOf('=') + 1));
            if (lower.startsWith("--home="))
               startupConfig.setHomeDirPath(arg.substring(arg.indexOf('=') + 1));
            if (lower.startsWith("--disable-viewing"))
               startupConfig.setDisableViewing(true);
            if (lower.startsWith("--sync-only=true"))
                startupConfig.setSyncOnly(true);
        }
        Global.setup(startupConfig);
        
    }

    // Exit point
	@Override
	public void closeEvent(QCloseEvent event) {	
		if (Global.minimizeOnClose() && !closeAction) {
			event.ignore();
			hide();
			return;
		}
		logger.log(logger.HIGH, "Entering NeverNote.closeEvent");
		waitCursor(true);
		
		if (currentNote!= null & browserWindow!=null) {
			if (currentNote.getTitle() != null && browserWindow != null 
					&& !currentNote.getTitle().equals(browserWindow.getTitle()))
				conn.getNoteTable().updateNoteTitle(currentNote.getGuid(), browserWindow.getTitle());
		}
		saveNote();
		setMessage(tr("Beginning shutdown."));
		
		// Close down external windows
		Collection<ExternalBrowse> windows = externalWindows.values();
		Iterator<ExternalBrowse> iterator = windows.iterator();
		while (iterator.hasNext()) {
			ExternalBrowse browser = iterator.next();
			browser.windowClosing.disconnect();
			browser.close();
		}
		
		
		externalFileEditedSaver();
		if (Global.isConnected && Global.synchronizeOnClose()) {
			setMessage(tr("Performing synchronization before closing."));
			syncRunner.syncNeeded = true;
			syncRunner.addWork("SYNC");
		} else {
			syncRunner.keepRunning = false;
		}
		syncRunner.addWork("STOP");
		setMessage("Closing Program.");
		threadMonitorTimer.stop();

		thumbnailRunner.addWork("STOP");
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

		if (isVisible())
			Global.saveWindowVisible("toolBar", toolBar.isVisible());
		saveNoteColumnPositions();
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
			if (indexRunner.thread().isAlive())
				indexRunner.thread().join(50);
			if (!indexRunner.thread().isAlive())
				logger.log(logger.MEDIUM, "Index thread has stopped");
			else {
				logger.log(logger.MEDIUM, "Index thread still running - interrupting");
				indexRunner.thread().interrupt();
			}
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		
		if (!syncRunner.thread().isAlive()) {
			logger.log(logger.MEDIUM, "Waiting for syncThread to stop");
			if (syncRunner.thread().isAlive()) {
				System.out.println(tr("Synchronizing.  Please be patient."));
				for(;syncRunner.thread().isAlive();) {
					try {
						wait(10);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
			logger.log(logger.MEDIUM, "Sync thread has stopped");
		}

		if (encryptOnShutdown) {
			encryptOnShutdown();
		}
		if (decryptOnShutdown) {
			decryptOnShutdown();
		}
		try {
			Global.getFileManager().purgeResDirectory(false);
		} catch (InitializationException e) {
			System.out.println(tr("Empty res directory purge failed"));
			e.printStackTrace();
		}
		logger.log(logger.HIGH, "Leaving NeverNote.closeEvent");
	}


	private void closeNeverNote() {
		closeAction = true;
		close();
	}
	public void setMessage(String s) {
		if (logger != null) 
			logger.log(logger.HIGH, "Entering NeverNote.setMessage");
		else
			System.out.println("*** ERROR *** " +s);
		
		if (statusBar != null) {
			statusBar.show();
			if (logger != null) 
				logger.log(logger.HIGH, "Message: " +s);
			statusBar.showMessage(s);
			if (emitLog != null)
				emitLog.add(s);
		
			if (messageTimer != null) {
				messageTimer.stop();
				messageTimer.setSingleShot(true);
				messageTimer.start();
			}
		}
			
		if (logger != null) 
			logger.log(logger.HIGH, "Leaving NeverNote.setMessage");
	}
	
	private void clearMessage() {
		statusBar.clearMessage();
		statusBar.hide();
	}
		
	private void waitCursor(boolean wait) {
		if (wait) {
			if (QApplication.overrideCursor() == null)
				QApplication.setOverrideCursor(new QCursor(Qt.CursorShape.WaitCursor));
		}
		else {
			while (QApplication.overrideCursor() != null)
				QApplication.restoreOverrideCursor();
		}
		listManager.refreshCounters();
	}
	
	private void setupIndexListeners() {
//		indexRunner.noteSignal.noteIndexed.connect(this, "indexThreadComplete(String)");
//		indexRunner.resourceSignal.resourceIndexed.connect(this, "indexThreadComplete(String)");
		indexRunner.signal.indexStarted.connect(this, "indexStarted()");
		indexRunner.signal.indexFinished.connect(this, "indexComplete()");
	}
	private void setupSyncSignalListeners() {
		syncRunner.tagSignal.listChanged.connect(this, "tagIndexUpdated()");
        syncRunner.searchSignal.listChanged.connect(this, "savedSearchIndexUpdated()");
        syncRunner.notebookSignal.listChanged.connect(this, "notebookIndexUpdated()");
        syncRunner.noteIndexSignal.listChanged.connect(this, "noteIndexUpdated(boolean)");
        syncRunner.noteSignal.quotaChanged.connect(this, "updateQuotaBar()");
        
		syncRunner.syncSignal.saveUploadAmount.connect(this,"saveUploadAmount(long)");
		syncRunner.syncSignal.saveUserInformation.connect(this,"saveUserInformation(User)");
		syncRunner.syncSignal.saveEvernoteUpdateCount.connect(this,"saveEvernoteUpdateCount(int)");
		
		syncRunner.noteSignal.guidChanged.connect(this, "noteGuidChanged(String, String)");
		syncRunner.noteSignal.noteChanged.connect(this, "invalidateNoteCache(String, String)");
		syncRunner.resourceSignal.resourceGuidChanged.connect(this, "noteResourceGuidChanged(String,String,String)");
		syncRunner.noteSignal.noteDownloaded.connect(listManager, "noteDownloaded(Note)");
		syncRunner.noteSignal.notebookChanged.connect(this, "updateNoteNotebook(String, String)");
		
		syncRunner.syncSignal.refreshLists.connect(this, "refreshLists()");
	}
	
	private void setupBrowserSignalListeners() {
		setupBrowserWindowListeners(browserWindow, true);
	}

	private void setupBrowserWindowListeners(BrowserWindow browser, boolean master) {
		browser.fileWatcher.fileChanged.connect(this, "externalFileEdited(String)");
		browser.noteSignal.tagsChanged.connect(this, "updateNoteTags(String, List)");
	    browser.noteSignal.tagsChanged.connect(this, "updateListTags(String, List)");
	    if (master) browser.noteSignal.noteChanged.connect(this, "setNoteDirty()");
	    browser.noteSignal.titleChanged.connect(listManager, "updateNoteTitle(String, String)");
	    browser.noteSignal.titleChanged.connect(this, "updateNoteTitle(String, String)");
	    browser.noteSignal.notebookChanged.connect(this, "updateNoteNotebook(String, String)");
	    browser.noteSignal.createdDateChanged.connect(listManager, "updateNoteCreatedDate(String, QDateTime)");
	    browser.noteSignal.alteredDateChanged.connect(listManager, "updateNoteAlteredDate(String, QDateTime)");
	    browser.noteSignal.subjectDateChanged.connect(listManager, "updateNoteSubjectDate(String, QDateTime)");
	    browser.noteSignal.authorChanged.connect(listManager, "updateNoteAuthor(String, String)");
	    browser.noteSignal.geoChanged.connect(listManager, "updateNoteGeoTag(String, Double,Double,Double)");
	    browser.noteSignal.geoChanged.connect(this, "setNoteDirty()");
	    browser.noteSignal.sourceUrlChanged.connect(listManager, "updateNoteSourceUrl(String, String)");
    	browser.blockApplication.connect(this, "blockApplication(BrowserWindow)");
    	browser.unblockApplication.connect(this, "unblockApplication()");
	    if (master) browser.focusLost.connect(this, "saveNote()");
	    browser.resourceSignal.contentChanged.connect(this, "externalFileEdited(String)");
	    browser.evernoteLinkClicked.connect(this, "evernoteLinkClick(String, String)");
	}

	//**************************************************
	//* Setup shortcuts
	//**************************************************
	private void setupShortcut(QShortcut action, String text) {
		if (!Global.shortcutKeys.containsAction(text))
			return;
		action.setKey(new QKeySequence(Global.shortcutKeys.getShortcut(text)));
	}
	
	//***************************************************************
	//***************************************************************
	//* Settings and look & feel
	//***************************************************************
	//***************************************************************
	@SuppressWarnings("unused")
	private void settings() {
		logger.log(logger.HIGH, "Entering NeverNote.settings");
		saveNoteColumnPositions();
		saveNoteIndexWidth();
		showColumns();
        ConfigDialog settings = new ConfigDialog(this);
        String dateFormat = Global.getDateFormat();
        String timeFormat = Global.getTimeFormat();
        
		indexTime = 1000*Global.getIndexThreadSleepInterval();  
		indexTimer.start(indexTime);  // reset indexing timer
        
        settings.exec();
        indexRunner.indexAttachmentsLocally = Global.indexAttachmentsLocally();
        indexRunner.indexNoteBody = Global.indexNoteBody();
        indexRunner.indexNoteTitle = Global.indexNoteTitle();
        indexRunner.specialIndexCharacters = Global.getSpecialIndexCharacters();
        indexRunner.indexImageRecognition = Global.indexImageRecognition();
        if (Global.showTrayIcon() || Global.minimizeOnClose())
        	trayIcon.show();
        else
        	trayIcon.hide();
        showColumns();
        if (menuBar.showEditorBar.isChecked())
        	showEditorButtons(browserWindow);
        
        // Reset the save timer
        if (Global.getAutoSaveInterval() > 0)
			saveTimer.setInterval(1000*60*Global.getAutoSaveInterval());
        else
        	saveTimer.stop();
        
        
        // Set special reloads
        if (settings.getDebugPage().reloadSharedNotebooksClicked()) {
        	conn.executeSql("Delete from LinkedNotebook");
        	conn.executeSql("delete from SharedNotebook");
        	conn.executeSql("Delete from Notebook where linked=true");
        	conn.executeSql("Insert into Sync (key, value) values ('FullLinkedNotebookSync', 'true')");
        	conn.executeSql("Insert into Sync (key, value) values ('FullSharedNotebookSync', 'true')");
        }

        // Reload user data
        noteCache.clear();
        readOnlyCache.clear();
        inkNoteCache.clear();
        noteIndexUpdated(true);
        	
        logger.log(logger.HIGH, "Leaving NeverNote.settings");
	}
	// Restore things to the way they were
	private void restoreWindowState(boolean mainWindow) {
		// We need to name things or this doesn't work.
		setObjectName("NeverNote");
        restoreState(Global.restoreState(objectName()));
		mainLeftRightSplitter.setObjectName("mainLeftRightSplitter");
		browserIndexSplitter.setObjectName("browserIndexSplitter");
		leftSplitter1.setObjectName("leftSplitter1");	
		
		// Restore the actual positions.
		if (mainWindow)
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
		Global.saveState(objectName(), saveState());
	}    
	// Load the style sheet
	private void loadStyleSheet() {
		String styleSheetName = "default.qss";
		if (Global.getStyle().equalsIgnoreCase("cleanlooks"))
				styleSheetName = "default-cleanlooks.qss";
		String fileName = Global.getFileManager().getQssDirPathUser("default.qss");
		QFile file = new QFile(fileName);
		
		// If a user default.qss doesn't exist, we use the one shipped with NeverNote
		if (!file.exists()) {
			fileName = Global.getFileManager().getQssDirPath(styleSheetName);
			file = new QFile(fileName);
		}
		file.open(OpenModeFlag.ReadOnly);
		String styleSheet = file.readAll().toString();
		file.close();
		setStyleSheet(styleSheet);
	}
	// Save column positions for the next time
	private void saveNoteColumnPositions() {
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
		position = noteTableView.header.visualIndex(Global.noteTableGuidPosition);
		Global.setColumnPosition("noteTableGuidPosition", position);
		position = noteTableView.header.visualIndex(Global.noteTableThumbnailPosition);
		Global.setColumnPosition("noteTableThumbnailPosition", position);
		position = noteTableView.header.visualIndex(Global.noteTablePinnedPosition);
		Global.setColumnPosition("noteTablePinnedPosition", position);

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
		width = noteTableView.getColumnWidth(Global.noteTableThumbnailPosition);
		Global.setColumnWidth("noteTableThumbnailPosition", width);
		width = noteTableView.getColumnWidth(Global.noteTableGuidPosition);
		Global.setColumnWidth("noteTableGuidPosition", width);
		width = noteTableView.getColumnWidth(Global.noteTablePinnedPosition);
		Global.setColumnWidth("noteTablePinnedPosition", width);
	}
	
	@SuppressWarnings("unused")
	private void toggleSearchWindow() {
		logger.log(logger.HIGH, "Entering NeverNote.toggleSearchWindow");
    	searchLayout.toggleSearchField();
    	menuBar.hideSearch.setChecked(searchField.isVisible());
    	Global.saveWindowVisible("searchField", searchField.isVisible());
    	logger.log(logger.HIGH, "Leaving NeverNote.toggleSearchWindow");
    }	
	@SuppressWarnings("unused")
	private void toggleQuotaWindow() {
		logger.log(logger.HIGH, "Entering NeverNote.toggleQuotaWindow");
    	searchLayout.toggleQuotaBar();
    	menuBar.hideQuota.setChecked(quotaBar.isVisible());
    	Global.saveWindowVisible("quota", quotaBar.isVisible());
    	logger.log(logger.HIGH, "Leaving NeverNote.toggleQuotaWindow");
    }	
	@SuppressWarnings("unused")
	private void toggleZoomWindow() {
		logger.log(logger.HIGH, "Entering NeverNote.toggleZoomWindow");
    	searchLayout.toggleZoom();
    	menuBar.hideZoom.setChecked(zoomSpinner.isVisible());
    	Global.saveWindowVisible("zoom", zoomSpinner.isVisible());
    	logger.log(logger.HIGH, "Leaving NeverNote.toggleZoomWindow");
    }	
	
	
	
    //***************************************************************
    //***************************************************************
    //** These functions deal with Notebook menu items
    //***************************************************************
    //***************************************************************
    // Setup the tree containing the user's notebooks.
    private void initializeNotebookTree() {       
    	logger.log(logger.HIGH, "Entering NeverNote.initializeNotebookTree");
//    	notebookTree.itemClicked.connect(this, "notebookTreeSelection()");
    	notebookTree.selectionSignal.connect(this, "notebookTreeSelection()");
    	listManager.notebookSignal.refreshNotebookTreeCounts.connect(notebookTree, "updateCounts(List, List)");
    	logger.log(logger.HIGH, "Leaving NeverNote.initializeNotebookTree");
    }   
    // Listener when a notebook is selected
	private void notebookTreeSelection() {
		logger.log(logger.HIGH, "Entering NeverNote.notebookTreeSelection");
		noteTableView.proxyModel.blocked = true;
		
		clearTrashFilter();
		clearAttributeFilter();
		clearSavedSearchFilter();
		if (Global.mimicEvernoteInterface) {
			clearTagFilter();
			//searchField.clear();
			searchField.clearEditText();
		}
		menuBar.noteRestoreAction.setVisible(false);		
    	menuBar.notebookEditAction.setEnabled(true);
    	menuBar.notebookDeleteAction.setEnabled(true);
    	menuBar.notebookPublishAction.setEnabled(true);
    	menuBar.notebookShareAction.setEnabled(true);
    	menuBar.notebookIconAction.setEnabled(true);
    	menuBar.notebookStackAction.setEnabled(true);
    	List<QTreeWidgetItem> selections = notebookTree.selectedItems();
    	selectedNotebookGUIDs.clear();
   		String guid = "";
   		String stackName = "";
   		if (selections.size() > 0) {
    		guid = (selections.get(0).text(2));
    		stackName = selections.get(0).text(0);
    	}
   		if (!Global.mimicEvernoteInterface) {
   			// If no notebooks are selected, we make it look like the "all notebooks" one was selected
   			if (selections.size()==0) {
   				selectedNotebookGUIDs.clear();
   				for (int i=0; i < listManager.getNotebookIndex().size(); i++) {
   					selectedNotebookGUIDs.add(listManager.getNotebookIndex().get(i).getGuid());
   				}
   				menuBar.notebookEditAction.setEnabled(false);
   				menuBar.notebookDeleteAction.setEnabled(false);
   				menuBar.notebookStackAction.setEnabled(false);
   				menuBar.notebookIconAction.setEnabled(false);
   			}
   		}
    	if (!guid.equals("") && !guid.equals("STACK")) {
    		selectedNotebookGUIDs.add(guid);
    		menuBar.notebookIconAction.setEnabled(true);
    	} else {
    		menuBar.notebookIconAction.setEnabled(true);
			for (int j=0; j<listManager.getNotebookIndex().size(); j++) {
				Notebook book = listManager.getNotebookIndex().get(j);
				if (book.getStack() != null && book.getStack().equalsIgnoreCase(stackName))
					selectedNotebookGUIDs.add(book.getGuid());
			}
    	}
    	listManager.setSelectedNotebooks(selectedNotebookGUIDs);
    	listManager.loadNotesIndex();
    	noteIndexUpdated(false);
    	refreshEvernoteNote(true);
    	listManager.refreshCounters = true;
    	listManager.refreshCounters();
    	if (selectedNotebookGUIDs.size() == 1) {
    		int col = conn.getNotebookTable().getSortColumn(selectedNotebookGUIDs.get(0));
    		int order = conn.getNotebookTable().getSortOrder(selectedNotebookGUIDs.get(0));
    		if (col != -1) {
    			noteTableView.proxyModel.blocked = true;
    			if (order == 1)
    				noteTableView.sortByColumn(col, Qt.SortOrder.DescendingOrder);
    			else
    				noteTableView.sortByColumn(col, Qt.SortOrder.AscendingOrder);
    		}
    	}
    	noteTableView.proxyModel.blocked = false;
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
    	
		// Get the possible icons
		HashMap<String, QIcon> icons = conn.getNotebookTable().getAllIcons();
    	notebookTree.setIcons(icons);
    	
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
    	listManager.refreshCounters = true;
    	listManager.refreshCounters();
    	notebookTree.blockSignals(false);
    	
		logger.log(logger.HIGH, "Leaving NeverNote.notebookIndexUpdated");
    }
    // Show/Hide note information
	@SuppressWarnings("unused")
	private void toggleNotebookWindow() {
		logger.log(logger.HIGH, "Entering NeverNote.toggleNotebookWindow");
		searchLayout.toggleNotebook();
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
	private void stackNotebook() {
		logger.log(logger.HIGH, "Entering NeverNote.stackNotebook");
		StackNotebook edit = new StackNotebook();
		
		List<QTreeWidgetItem> selections = notebookTree.selectedItems();
		QTreeWidgetItem currentSelection;
		for (int i=0; i<selections.size(); i++) {
			currentSelection = selections.get(0);
			String guid = currentSelection.text(2);
			if (guid.equalsIgnoreCase("")) {
				 QMessageBox.critical(this, tr("Unable To Stack") ,tr("You can't stack the \"All Notebooks\" item."));
				 return;
			}
			if (guid.equalsIgnoreCase("STACK")) {
				 QMessageBox.critical(this, tr("Unable To Stack") ,tr("You can't stack a stack."));
				 return;
			}
		}

		edit.setStackNames(conn.getNotebookTable().getAllStackNames());

		
		edit.exec();
	
		if (!edit.okPressed())
			return;
        
		String stack = edit.getStackName();
		
		for (int i=0; i<selections.size(); i++) {
			currentSelection = selections.get(i);
			String guid = currentSelection.text(2);
			listManager.updateNotebookStack(guid, stack);
		}
		notebookIndexUpdated();
		logger.log(logger.HIGH, "Leaving NeverNote.stackNotebook");
	}
	// Edit an existing notebook
	@SuppressWarnings("unused")
	private void editNotebook() {
		logger.log(logger.HIGH, "Entering NeverNote.editNotebook");
		NotebookEdit edit = new NotebookEdit();
		
		List<QTreeWidgetItem> selections = notebookTree.selectedItems();
		QTreeWidgetItem currentSelection;
		currentSelection = selections.get(0);
		edit.setNotebook(currentSelection.text(0));
		
		String guid = currentSelection.text(2);
		if (!guid.equalsIgnoreCase("STACK")) {
			edit.setTitle(tr("Edit Notebook"));
			edit.setNotebooks(listManager.getNotebookIndex());
			edit.setLocalCheckboxEnabled(false);
			for (int i=0; i<listManager.getNotebookIndex().size(); i++) {
				if (listManager.getNotebookIndex().get(i).getGuid().equals(guid)) {
					edit.setDefaultNotebook(listManager.getNotebookIndex().get(i).isDefaultNotebook());
					i=listManager.getNotebookIndex().size();
				}
			}
		} else {
			edit.setTitle(tr("Edit Stack"));
			edit.setStacks(conn.getNotebookTable().getAllStackNames());
			edit.hideLocalCheckbox();
			edit.hideDefaultCheckbox();
		}
		
		edit.exec();
	
		if (!edit.okPressed())
			return;
        
		
		if (guid.equalsIgnoreCase("STACK")) {
			conn.getNotebookTable().renameStacks(currentSelection.text(0), edit.getNotebook());
			for (int j=0; j<listManager.getNotebookIndex().size(); j++) {
				if (listManager.getNotebookIndex().get(j).getStack() != null && 
					listManager.getNotebookIndex().get(j).getStack().equalsIgnoreCase(currentSelection.text(0)))
						listManager.getNotebookIndex().get(j).setStack(edit.getNotebook());
			}
			conn.getNotebookTable().renameStacks(currentSelection.text(0), edit.getNotebook());
			currentSelection.setText(0, edit.getNotebook());
			return;
		}
		
		updateListNotebookName(currentSelection.text(0), edit.getNotebook());
		currentSelection.setText(0, edit.getNotebook());
		
		for (int i=0; i<listManager.getNotebookIndex().size(); i++) {
			if (listManager.getNotebookIndex().get(i).getGuid().equals(guid)) {
				listManager.getNotebookIndex().get(i).setName(edit.getNotebook());
				if (!listManager.getNotebookIndex().get(i).isDefaultNotebook() && edit.isDefaultNotebook()) {
					for (int j=0; j<listManager.getNotebookIndex().size(); j++)
						listManager.getNotebookIndex().get(j).setDefaultNotebook(false);
					listManager.getNotebookIndex().get(i).setDefaultNotebook(true);
					conn.getNotebookTable().setDefaultNotebook(listManager.getNotebookIndex().get(i).getGuid());
				}
				conn.getNotebookTable().updateNotebook(listManager.getNotebookIndex().get(i), true);
				if (conn.getNotebookTable().isLinked(listManager.getNotebookIndex().get(i).getGuid())) {
					LinkedNotebook linkedNotebook = conn.getLinkedNotebookTable().getByNotebookGuid(listManager.getNotebookIndex().get(i).getGuid());
					linkedNotebook.setShareName(edit.getNotebook());
					conn.getLinkedNotebookTable().updateNotebook(linkedNotebook, true);
				}
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
		
		
		FilterEditorNotebooks notebookFilter = new FilterEditorNotebooks(conn, logger);
		List<Notebook> filteredBooks = notebookFilter.getValidNotebooks(currentNote, listManager.getNotebookIndex());
		browserWindow.setNotebookList(filteredBooks);
		Iterator<String> set = externalWindows.keySet().iterator();
		while(set.hasNext())
			externalWindows.get(set.next()).getBrowserWindow().setNotebookList(filteredBooks);
		logger.log(logger.HIGH, "Leaving NeverNote.editNotebook");
	}
	// Publish a notebook
	@SuppressWarnings("unused")
	private void publishNotebook() {
		List<QTreeWidgetItem> selections = notebookTree.selectedItems();
		QTreeWidgetItem currentSelection;
		currentSelection = selections.get(0);
		String guid = currentSelection.text(2);

		if (guid.equalsIgnoreCase("STACK") || guid.equalsIgnoreCase(""))
			return;
		
		Notebook n = null;
		int position = 0;
		for (int i=0; i<listManager.getNotebookIndex().size(); i++) {
			if (guid.equals(listManager.getNotebookIndex().get(i).getGuid())) {
				n = listManager.getNotebookIndex().get(i);
				position = i;
				i = listManager.getNotebookIndex().size();
			}
		}
		if (n == null)
			return;
		
		PublishNotebook publish = new PublishNotebook(Global.username, Global.getServer(), n);
		publish.exec();
		
		if (!publish.okClicked()) 
			return;
		
		Publishing p = publish.getPublishing();
		boolean isPublished = !publish.isStopPressed();
		conn.getNotebookTable().setPublishing(n.getGuid(), isPublished, p);
		n.setPublished(isPublished);
		n.setPublishing(p);
		listManager.getNotebookIndex().set(position, n);
		notebookIndexUpdated();
	}
	// Publish a notebook
	@SuppressWarnings("unused")
	private void shareNotebook() {
		List<QTreeWidgetItem> selections = notebookTree.selectedItems();
		QTreeWidgetItem currentSelection;
		currentSelection = selections.get(0);
		String guid = currentSelection.text(2);

		if (guid.equalsIgnoreCase("STACK") || guid.equalsIgnoreCase(""))
			return;
		
		Notebook n = null;;
		for (int i=0; i<listManager.getNotebookIndex().size(); i++) {
			if (guid.equals(listManager.getNotebookIndex().get(i).getGuid())) {
				n = listManager.getNotebookIndex().get(i);
				i = listManager.getNotebookIndex().size();
			}
		}
				
		String authToken = null;
		if (syncRunner.isConnected)
			authToken = syncRunner.authToken;
		ShareNotebook share = new ShareNotebook(n.getName(), conn, n, syncRunner);
		share.exec();
		
	}

	// Delete an existing notebook
	@SuppressWarnings("unused")
	private void deleteNotebook() {
		logger.log(logger.HIGH, "Entering NeverNote.deleteNotebook");
		boolean stacksFound = false;
		boolean notebooksFound = false;
		boolean assigned = false;
		// Check if any notes have this notebook
		List<QTreeWidgetItem> selections = notebookTree.selectedItems();
        for (int i=0; i<selections.size(); i++) {
        	QTreeWidgetItem currentSelection;
    		currentSelection = selections.get(i);
    		String guid = currentSelection.text(2);
    		if (!guid.equalsIgnoreCase("STACK")) {
    			notebooksFound = true;
    			for (int j=0; j<listManager.getNoteIndex().size(); j++) {
    				String noteGuid = listManager.getNoteIndex().get(j).getNotebookGuid();
    				if (noteGuid.equals(guid)) {
    					assigned = true;
    					j=listManager.getNoteIndex().size();
    					i=selections.size();
    				}
    			}
    		} else {
    			stacksFound = true;
    		}
        }
		if (assigned) {
			QMessageBox.information(this, tr("Unable to Delete"), tr("Some of the selected notebook(s) contain notes.\n"+
					"Please delete the notes or move them to another notebook before deleting any notebooks."));
			return;
		}
		
		if (conn.getNotebookTable().getAll().size() == 1) {
			QMessageBox.information(this, tr("Unable to Delete"), tr("You must have at least one notebook."));
			return;
		}
        
        // If all notebooks are clear, verify the delete
		String msg1 = new String(tr("Delete selected notebooks?"));
		String msg2 = new String(tr("Remove selected stacks (notebooks will not be deleted)?"));
		String msg3 = new String(tr("Delete selected notebooks & remove stacks? Notebooks under the stacks are" +
				" not deleted unless selected?"));
		String msg = "";
		if (stacksFound && notebooksFound)
			msg = msg3;
		if (!stacksFound && notebooksFound)
			msg = msg1;
		if (stacksFound && !notebooksFound)
			msg = msg2;
		if (QMessageBox.question(this, tr("Confirmation"), msg,
			QMessageBox.StandardButton.Yes, 
			QMessageBox.StandardButton.No)==StandardButton.No.value()) {
			return;
		}
		
		// If confirmed, delete the notebook
        for (int i=selections.size()-1; i>=0; i--) {
        	QTreeWidgetItem currentSelection;
    		currentSelection = selections.get(i);
    		String guid = currentSelection.text(2);
    		if (currentSelection.text(2).equalsIgnoreCase("STACK")) {
       			conn.getNotebookTable().renameStacks(currentSelection.text(0), "");
       			listManager.renameStack(currentSelection.text(0), "");
    		} else {
    			conn.getNotebookTable().expungeNotebook(guid, true);
    			listManager.deleteNotebook(guid);
    		}
        }

		notebookIndexUpdated();
//        notebookTreeSelection();
//        notebookTree.load(listManager.getNotebookIndex(), listManager.getLocalNotebooks());
//        listManager.countNotebookResults(listManager.getNoteIndex());
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
				break;
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
		notebookTreeSelection();
		listManager.loadNotesIndex();
		notebookIndexUpdated();
		noteIndexUpdated(false);
		reloadTagTree(true);
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
		
		FilterEditorNotebooks notebookFilter = new FilterEditorNotebooks(conn, logger);
		List<Notebook> filteredBooks = notebookFilter.getValidNotebooks(currentNote, listManager.getNotebookIndex());
		browserWindow.setNotebookList(filteredBooks);
		
		// Update any external windows
		Iterator<String> set = externalWindows.keySet().iterator();
		while(set.hasNext())
			externalWindows.get(set.next()).getBrowserWindow().setNotebookList(filteredBooks);
		
		waitCursor(false);
	}
	// Change the notebook's icon
	@SuppressWarnings("unused")
	private void setNotebookIcon() {
		boolean stackSelected = false;
		boolean allNotebookSelected = false;
		
		QTreeWidgetItem currentSelection;
		List<QTreeWidgetItem> selections = notebookTree.selectedItems();
		if (selections.size() == 0)
			return;
		
		currentSelection = selections.get(0);	
		String guid = currentSelection.text(2);
		if (guid.equalsIgnoreCase(""))
			allNotebookSelected = true;
		if (guid.equalsIgnoreCase("STACK"))
			stackSelected = true;

		QIcon currentIcon = currentSelection.icon(0);
		QIcon icon;
		SetIcon dialog;
		
		if (!stackSelected && !allNotebookSelected) {
			icon = conn.getNotebookTable().getIcon(guid);
			if (icon == null) {
				dialog = new SetIcon(currentIcon, saveLastPath);
				dialog.setUseDefaultIcon(true);
			} else {
				dialog = new SetIcon(icon, saveLastPath);
				dialog.setUseDefaultIcon(false);
			}
		} else {
			if (stackSelected) {
				icon = conn.getSystemIconTable().getIcon(currentSelection.text(0), "STACK");
			} else {
				icon = conn.getSystemIconTable().getIcon(currentSelection.text(0), "ALLNOTEBOOK");				
			}
			if (icon == null) {
				dialog = new SetIcon(currentIcon, saveLastPath);
				dialog.setUseDefaultIcon(true);
			} else {
				dialog = new SetIcon(icon, saveLastPath);
				dialog.setUseDefaultIcon(false);
			}
		}
		dialog.exec();
		if (dialog.okPressed()) {
	    	saveLastPath = dialog.getPath();

			QIcon newIcon = dialog.getIcon();
			if (stackSelected) {
				conn.getSystemIconTable().setIcon(currentSelection.text(0), "STACK", newIcon, dialog.getFileType());
				if (newIcon == null) {
					newIcon = new QIcon(iconPath+"books2.png");
				}
				currentSelection.setIcon(0,newIcon);
				return;
			}
			if (allNotebookSelected) {
				conn.getSystemIconTable().setIcon(currentSelection.text(0), "ALLNOTEBOOK", newIcon, dialog.getFileType());
				if (newIcon == null) {
					newIcon = new QIcon(iconPath+"notebook-green.png");
				}
				currentSelection.setIcon(0,newIcon);
				return;
			}
			conn.getNotebookTable().setIcon(guid, newIcon, dialog.getFileType());
			if (newIcon == null) {
				boolean isPublished = false;;
				boolean found = false;
				for (int i=0; i<listManager.getNotebookIndex().size() && !found; i++) {
					if (listManager.getNotebookIndex().get(i).getGuid().equals(guid)) {
						isPublished = listManager.getNotebookIndex().get(i).isPublished();
						found = true;
					}
				}
				newIcon = notebookTree.findDefaultIcon(guid, currentSelection.text(1), listManager.getLocalNotebooks(), isPublished);
			}
			currentSelection.setIcon(0, newIcon);
		}
	
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

		List<QTreeWidgetItem> selections = tagTree.selectedItems();
		QTreeWidgetItem currentSelection = null;
		if (selections.size() > 0) {
			currentSelection = selections.get(0);
			edit.setParentTag(currentSelection.text(0));
		}

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
		if (edit.getParentTag().isChecked()) {
			newTag.setParentGuid(currentSelection.text(2));
			newTag.setParentGuidIsSet(true);
			currentSelection.setExpanded(true);
		}
		conn.getTagTable().addTag(newTag, true);
		listManager.getTagIndex().add(newTag);
		reloadTagTree(true);
		
		logger.log(logger.HIGH, "Leaving NeverNote.addTag");
	}
	@SuppressWarnings("unused")
	private void reloadTagTree() {
		reloadTagTree(false);
	}
	private void reloadTagTree(boolean reload) {
		logger.log(logger.HIGH, "Entering NeverNote.reloadTagTree");
		tagIndexUpdated(reload);
		boolean filter = false;
		if (reload)
			listManager.countTagResults(listManager.getNoteIndex());
		if (notebookTree.selectedItems().size() > 0 
						  && !notebookTree.selectedItems().get(0).text(0).equalsIgnoreCase("All Notebooks"))
						  filter = true;
		if (tagTree.selectedItems().size() > 0)
			filter = true;
		tagTree.showAllTags(!filter);
		tagIndexUpdated(false);
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
				//return;
			}
		}
		listManager.reloadNoteTagNames(guid, edit.getTag());
		noteIndexUpdated(true);
		refreshEvernoteNote(true);
		browserWindow.setTag(getTagNamesForNote(currentNote));
		logger.log(logger.HIGH, "Leaving NeverNote.editTag...");
	}
	// Delete an existing tag
	@SuppressWarnings("unused")
	private void deleteTag() {
		logger.log(logger.HIGH, "Entering NeverNote.deleteTag");
		
		if (QMessageBox.question(this, tr("Confirmation"), tr("Delete the selected tags?"),
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
        tagTreeSelection();
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
//    	tagTree.itemSelectionChanged.connect(this, "tagTreeSelection()");
//    	tagTree.itemClicked.connect(this, "tagTreeSelection()");
    	tagTree.selectionSignal.connect(this, "tagTreeSelection()");
    	listManager.tagSignal.refreshTagTreeCounts.connect(tagTree, "updateCounts(List)");
    	logger.log(logger.HIGH, "Leaving NeverNote.initializeTagTree");
    }
    // Listener when a tag is selected
	private void tagTreeSelection() {
    	logger.log(logger.HIGH, "Entering NeverNote.tagTreeSelection");
    	    	
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
    		menuBar.tagIconAction.setEnabled(true);
    	}
    	else {
    		menuBar.tagEditAction.setEnabled(false);
    		menuBar.tagDeleteAction.setEnabled(false);
    		menuBar.tagIconAction.setEnabled(true);
    	}
    	if (selections.size() > 1)
    		menuBar.tagMergeAction.setEnabled(true);
    	else
    		menuBar.tagMergeAction.setEnabled(false);
    	listManager.setSelectedTags(selectedTagGUIDs);
    	listManager.loadNotesIndex();
    	noteIndexUpdated(false);
    	refreshEvernoteNote(true);
    	listManager.refreshCounters = true;
    	listManager.refreshCounters();
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
		if (reload)
			listManager.reloadTagIndex();

		tagTree.blockSignals(true);
		if (reload) {
			tagTree.setIcons(conn.getTagTable().getAllIcons());
			tagTree.load(listManager.getTagIndex());
		}

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
	@SuppressWarnings("unused")
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
		// Save any new tags.  We'll need them later.
		List<String> newTags = new ArrayList<String>();
		for (int i=0; i<tags.size(); i++) {
			if (conn.getTagTable().findTagByName(tags.get(i))==null) 
				newTags.add(tags.get(i));
		}
		
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
		
		// Now, we need to add any new tags to the tag tree
		for (int i=0; i<newTags.size(); i++) 
			tagTree.insertTag(newTags.get(i), conn.getTagTable().findTagByName(newTags.get(i)));
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
		
		for (int i=0; i<listManager.getMasterNoteIndex().size(); i++) {
			if (listManager.getMasterNoteIndex().get(i).getGuid().equals(noteGuid)) {
				List<String> tagNames = new ArrayList<String>();
				tagNames.add(new String(tagName));
				Note n = listManager.getMasterNoteIndex().get(i);
				for (int j=0; j<n.getTagNames().size(); j++) {
					tagNames.add(new String(n.getTagNames().get(j)));
				}
				listManager.getNoteTableModel().updateNoteTags(noteGuid, n.getTagGuids(), tagNames);
				if (n.getGuid().equals(currentNoteGuid)) {
					Collections.sort(tagNames);
					String display = "";
					for (int j=0; j<tagNames.size(); j++) {
						display = display+tagNames.get(j);
						if (j+2<tagNames.size()) 
							display = display+Global.tagDelimeter+" ";
					}
					browserWindow.setTag(display);
				}
				i=listManager.getMasterNoteIndex().size();
			}
		}
		
		
		listManager.getNoteTableModel().updateNoteSyncStatus(noteGuid, false);
	}
	private void clearTagFilter() {
		tagTree.blockSignals(true);
		tagTree.clearSelection();
		menuBar.noteRestoreAction.setVisible(false);
		menuBar.tagEditAction.setEnabled(false);
		menuBar.tagMergeAction.setEnabled(false);
		menuBar.tagDeleteAction.setEnabled(false);
		menuBar.tagIconAction.setEnabled(false);
		selectedTagGUIDs.clear();
    	listManager.setSelectedTags(selectedTagGUIDs);
    	tagTree.blockSignals(false);
	}
	// Change the icon for a tag
	@SuppressWarnings("unused")
	private void setTagIcon() {
		QTreeWidgetItem currentSelection;
		List<QTreeWidgetItem> selections = tagTree.selectedItems();
		if (selections.size() == 0)
			return;
		
		currentSelection = selections.get(0);	
		String guid = currentSelection.text(2);

		QIcon currentIcon = currentSelection.icon(0);
		QIcon icon = conn.getTagTable().getIcon(guid);
		SetIcon dialog;
		if (icon == null) {
			dialog = new SetIcon(currentIcon, saveLastPath);
			dialog.setUseDefaultIcon(true);
		} else {
			dialog = new SetIcon(icon, saveLastPath);
			dialog.setUseDefaultIcon(false);
		}
		dialog.exec();
		if (dialog.okPressed()) {
	    	saveLastPath = dialog.getPath();
			QIcon newIcon = dialog.getIcon();
			conn.getTagTable().setIcon(guid, newIcon, dialog.getFileType());
			if (newIcon == null) 
				newIcon = new QIcon(iconPath+"tag.png");
			currentSelection.setIcon(0, newIcon);
		}
	
	}
	// Merge tags
	@SuppressWarnings("unused")
	private void mergeTags() {
		List<Tag> tags = new ArrayList<Tag>();
		List<QTreeWidgetItem> selections = tagTree.selectedItems();
		for (int i=0; i<selections.size(); i++) {
			Tag record = new Tag();
			record.setGuid(selections.get(i).text(2));
			record.setName(selections.get(i).text(0));
			tags.add(record);
		}

		TagMerge mergeDialog = new TagMerge(tags);
		mergeDialog.exec();
		if (!mergeDialog.okClicked())
			return;
		String newGuid = mergeDialog.getNewTagGuid();
		
		for (int i=0; i<tags.size(); i++) {
			if (!tags.get(i).getGuid().equals(newGuid)) {
				List<String> noteGuids = conn.getNoteTable().noteTagsTable.getTagNotes(tags.get(i).getGuid());
				for (int j=0; j<noteGuids.size(); j++) {
					String noteGuid = noteGuids.get(j);
					conn.getNoteTable().noteTagsTable.deleteNoteTag(noteGuid);
					if (!conn.getNoteTable().noteTagsTable.checkNoteNoteTags(noteGuid, newGuid))
						conn.getNoteTable().noteTagsTable.saveNoteTag(noteGuid, newGuid);
				}
			}
		}
		listManager.reloadIndexes();
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
		edit.setTitle(tr("Edit Search"));
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
		
		if (QMessageBox.question(this, tr("Confirmation"), tr("Delete the selected search?"),
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
    	menuBar.savedSearchIconAction.setEnabled(true);
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
    	menuBar.savedSearchIconAction.setEnabled(false);
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
		savedSearchTree.setIcons(conn.getSavedSearchTable().getAllIcons());
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
    	menuBar.savedSearchIconAction.setEnabled(true);
    	List<QTreeWidgetItem> selections = savedSearchTree.selectedItems();

    	if (selections.size() > 0) {
    		menuBar.savedSearchEditAction.setEnabled(true);
    		menuBar.savedSearchDeleteAction.setEnabled(true);
    		menuBar.savedSearchIconAction.setEnabled(true);
    		selectedSavedSearchGUID = selections.get(0).text(1);
    		SavedSearch s = conn.getSavedSearchTable().getSavedSearch(selectedSavedSearchGUID);
    		searchField.setEditText(s.getQuery());
    	} else { 
        	menuBar.savedSearchEditAction.setEnabled(false);
        	menuBar.savedSearchDeleteAction.setEnabled(false);
        	menuBar.savedSearchIconAction.setEnabled(false);
        	selectedSavedSearchGUID = "";
        	searchField.setEditText("");
    	}
    	searchFieldChanged();
    	
		logger.log(logger.HIGH, "Leaving NeverNote.updateSavedSearchSelection()");

    	
    }
    // Show/Hide note information
	@SuppressWarnings("unused")
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
	// Change the icon for a saved search
	@SuppressWarnings("unused")
	private void setSavedSearchIcon() {
		QTreeWidgetItem currentSelection;
		List<QTreeWidgetItem> selections = savedSearchTree.selectedItems();
		if (selections.size() == 0)
			return;
		
		currentSelection = selections.get(0);	
		String guid = currentSelection.text(1);

		QIcon currentIcon = currentSelection.icon(0);
		QIcon icon = conn.getSavedSearchTable().getIcon(guid);
		SetIcon dialog;
		if (icon == null) {
			dialog = new SetIcon(currentIcon, saveLastPath);
			dialog.setUseDefaultIcon(true);
		} else {
			dialog = new SetIcon(icon, saveLastPath);
			dialog.setUseDefaultIcon(false);
		}
		dialog.exec();
		if (dialog.okPressed()) {
	    	saveLastPath = dialog.getPath();
			QIcon newIcon = dialog.getIcon();
			conn.getSavedSearchTable().setIcon(guid, newIcon, dialog.getFileType());
			if (newIcon == null) 
				newIcon = new QIcon(iconPath+"search.png");
			currentSelection.setIcon(0, newIcon);
		}
	
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
		indexRunner.interrupt = true;
		int dirty = conn.getNoteTable().getDirtyCount();
		int unindexed = conn.getNoteTable().getUnindexedCount();
		DatabaseStatus status = new DatabaseStatus();
		status.setUnsynchronized(dirty);
		status.setUnindexed(unindexed);
		status.setNoteCount(conn.getNoteTable().getNoteCount());
		status.setNotebookCount(listManager.getNotebookIndex().size());
		status.setUnindexedResourceCount(conn.getNoteTable().noteResourceTable.getUnindexedCount());
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
   		if (QMessageBox.question(this, tr("Confirmation"), tr("This will free unused space in the database, "+
   				"but please be aware that depending upon the size of your database this can be time consuming " +
   				"and NixNote will be unresponsive until it is complete.  Do you wish to continue?"),
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
		QFile file = new QFile(Global.getFileManager().getProgramDirPath("release.txt"));
		if (!file.open(new QIODevice.OpenMode(QIODevice.OpenModeFlag.ReadOnly,
                QIODevice.OpenModeFlag.Text)))
			return;
		textBox.setText(file.readAll().toString());
		file.close();
		dialog.setWindowTitle(tr("Release Notes"));
		dialog.setLayout(layout);
		dialog.show();
		logger.log(logger.HIGH, "Leaving NeverNote.releaseNotes");
	}
	// Called when user picks Log from the help menu
	@SuppressWarnings("unused")
	private void logger() {
		logger.log(logger.HIGH, "Entering NeverNote.logger");
		LogFileDialog dialog = new LogFileDialog(emitLog);
		dialog.exec();
		logger.log(logger.HIGH, "Leaving NeverNote.logger");
	}
	// Menu option "help/about" was selected
	@SuppressWarnings("unused")
	private void about() {
		logger.log(logger.HIGH, "Entering NeverNote.about");
		QMessageBox.about(this, 
						tr("About NixNote"),
						tr("<h4><center><b>NixNote</b></center></h4><hr><center>Version ")
						+Global.version
						//+"1.2.120724"
						+tr("<hr>"
								+"Open Source Evernote Client.<br><br>" 
								+"Licensed under GPL v2.  <br><hr><br>"
								+"</center>Evernote is copyright 2001-2012 by Evernote Corporation<br>"
								+"Jambi and QT are the licensed trademark of Nokia Corporation<br>"
								+"PDFRenderer is licened under the LGPL<br>"
								+"JTidy is copyrighted under the World Wide Web Consortium<br>"
								+"Apache Common Utilities licensed under the Apache License Version 2.0<br>"
								+"Jazzy is licened under the LGPL<br>"
								+"Java is a registered trademark of Oracle Corporation.<br><hr>"
								+"Special thanks to:<br>BitRock InstallBuilder for the Windows installer"
								+"<br>CodeCogs (www.codecogs.com) for the LaTeX image rendering."));
		logger.log(logger.HIGH, "Leaving NeverNote.about");
	}
	// Hide the entire left hand side
	@SuppressWarnings("unused")
	private void toggleLeftSide() {
		boolean hidden;
		
		hidden = !menuBar.hideLeftSide.isChecked();
		menuBar.hideLeftSide.setChecked(!hidden);
		
		if (!hidden) 
			leftSplitter1.setHidden(true);
		else
			leftSplitter1.setHidden(false);
		
		Global.saveWindowVisible("leftPanel", hidden);
		
	}
	public void checkForUpdates() {
		// Send off thread to check for a new version
		versionChecker = new QNetworkAccessManager(this);
		versionChecker.finished.connect(this, "upgradeFileRead(QNetworkReply)");
		QNetworkRequest request = new QNetworkRequest();
		request.setUrl(new QUrl(Global.getUpdatesAvailableUrl()));
		versionChecker.get(request);
	}
	@SuppressWarnings("unused")
	private void upgradeFileRead(QNetworkReply reply) {
		if (!reply.isReadable())
			return;
		
		String winVersion = Global.version;
		String osxVersion = Global.version;
		String linuxVersion = Global.version;
		String linux64Version = Global.version;
		String version = Global.version;
		
		// Determine the versions available
		QByteArray data = reply.readLine();
		while (data != null && !reply.atEnd()) {
			String line = data.toString();
			String lineVersion;
			if (line.contains(":")) 
				lineVersion = line.substring(line.indexOf(":")+1).replace(" ", "").replace("\n", "");
			else
				lineVersion = "";
			if (line.toLowerCase().contains("windows")) 
				winVersion = lineVersion;
			else if (line.toLowerCase().contains("os-x")) 
				osxVersion = lineVersion;
			else if (line.toLowerCase().contains("linux amd64")) 
				linux64Version = lineVersion;
			else if (line.toLowerCase().contains("linux i386")) 
				linuxVersion = lineVersion;
			else if (line.toLowerCase().contains("default")) 
				version = lineVersion;
			
			// Read the next line
			data = reply.readLine();
		}
		
		// Now we need to determine what system we are on.
		if (System.getProperty("os.name").toLowerCase().contains("windows"))
			version = winVersion;
		if (System.getProperty("os.name").toLowerCase().contains("mac os"))
			version = osxVersion;
		if (System.getProperty("os.name").toLowerCase().contains("Linux")) {
			if (System.getProperty("os.arch").contains("amd64") ||
				System.getProperty("os.arch").contains("x86_64"))
					version = linux64Version;
			else
				version = linuxVersion;
		}
		
		
		for (String validVersion : Global.validVersions) {
			if (version.equals(validVersion))
				return;
		}
		
		UpgradeAvailableDialog dialog = new UpgradeAvailableDialog();
		dialog.exec();
		if (dialog.remindMe())
			Global.setCheckVersionUpgrade(true);
		else
			Global.setCheckVersionUpgrade(false);
	}
		
	
    //***************************************************************
    //***************************************************************
    //** These functions deal with the Toolbar
    //***************************************************************
    //*************************************************************** 
	@SuppressWarnings("unused")
	private void focusSearch() {
		searchField.setFocus();
	}

	// Text in the search bar has been cleared
	private void searchFieldCleared() {
		saveNote();
		
		// This is done because we want to force a reload of
		// images.  Some images we may want to highlight the text.
		readOnlyCache.clear();
		inkNoteCache.clear();
		noteCache.clear();
		QWebSettings.setMaximumPagesInCache(0);
		QWebSettings.setObjectCacheCapacities(0, 0, 0);
        
		searchField.setEditText("");
		saveNoteColumnPositions();
		saveNoteIndexWidth();
		noteIndexUpdated(true);
		if (currentNote == null && listManager.getNoteIndex().size() > 0) {
			currentNote = listManager.getNoteIndex().get(0);
			currentNoteGuid = currentNote.getGuid();
		}
		refreshEvernoteNote(true);
		if (currentNote != null)
			loadNoteBrowserInformation(browserWindow, currentNoteGuid, currentNote);
	}
	// text in the search bar changed.  We only use this to tell if it was cleared, 
	// otherwise we trigger off searchFieldChanged.
	@SuppressWarnings("unused")
	private void searchFieldTextChanged(String text) {
		QWebSettings.setMaximumPagesInCache(0);
		QWebSettings.setObjectCacheCapacities(0, 0, 0);

		if (text.trim().equals("")) {
			searchFieldCleared();
			if (searchPerformed) {

				// This is done because we want to force a reload of
				// images.  Some images we may want to highlight the text.
				noteCache.clear();
				readOnlyCache.clear();
				inkNoteCache.clear();
				
				listManager.setEnSearch("");
				listManager.loadNotesIndex();
				refreshEvernoteNote(true);
				noteIndexUpdated(false);
				refreshEvernoteNote(true);
			}
			searchPerformed = false;
		}
	}
    // Text in the toolbar has changed
    private void searchFieldChanged() {
    	logger.log(logger.HIGH, "Entering NeverNote.searchFieldChanged");
    	noteCache.clear();
    	readOnlyCache.clear();
    	inkNoteCache.clear();
    	saveNoteColumnPositions();
    	saveNoteIndexWidth();
    	String text = searchField.currentText();
    	listManager.setEnSearch(text.trim());
    	listManager.loadNotesIndex();
    	noteIndexUpdated(false);

    	refreshEvernoteNote(true);
    	searchPerformed = true;
    	waitCursor(false);
    	logger.log(logger.HIGH, "Leaving NeverNote.searchFieldChanged");
    }

    // Build the window tool bar
    private void setupToolBar() {
    	logger.log(logger.HIGH, "Entering NeverNote.setupToolBar");
    	toolBar = addToolBar(tr("Tool Bar"));	
    	toolBar.setObjectName("toolBar");
    	menuBar.setupToolBarVisible();
    	if (!Global.isWindowVisible("toolBar"))
    		toolBar.setVisible(false);
    	else
    		toolBar.setVisible(true);

//    	toolBar.addWidget(menuBar);
//    	menuBar.setSizePolicy(Policy.Minimum, Policy.Minimum);
//    	toolBar.addSeparator();
    	prevButton = toolBar.addAction(tr("Previous"));
    	QIcon prevIcon = new QIcon(iconPath+"back.png");
    	prevButton.setIcon(prevIcon);
    	prevButton.triggered.connect(this, "previousViewedAction()");  	
    	togglePrevArrowButton(Global.isToolbarButtonVisible("prevArrow"));
    	
    	nextButton = toolBar.addAction(tr("Next"));
    	QIcon nextIcon = new QIcon(iconPath+"forward.png");
    	nextButton.setIcon(nextIcon);
    	nextButton.triggered.connect(this, "nextViewedAction()");  	
    	toggleNextArrowButton(Global.isToolbarButtonVisible("nextArrow"));
    	
    	upButton = toolBar.addAction(tr("Up"));
    	QIcon upIcon = new QIcon(iconPath+"up.png");
    	upButton.setIcon(upIcon);
    	upButton.triggered.connect(this, "upAction()");  	
    	toggleUpArrowButton(Global.isToolbarButtonVisible("upArrow"));

    	
    	downButton = toolBar.addAction(tr("Down"));
    	QIcon downIcon = new QIcon(iconPath+"down.png");
    	downButton.setIcon(downIcon);
    	downButton.triggered.connect(this, "downAction()");
    	toggleDownArrowButton(Global.isToolbarButtonVisible("downArrow"));
    	
    	synchronizeButton = toolBar.addAction(tr("Synchronize"));
    	synchronizeButton.setIcon(new QIcon(iconPath+"synchronize.png"));
    	synchronizeIconAngle = 0;
    	synchronizeButton.triggered.connect(this, "evernoteSync()");
    	toggleSynchronizeButton(Global.isToolbarButtonVisible("synchronize"));
    	
    	printButton = toolBar.addAction(tr("Print"));
    	QIcon printIcon = new QIcon(iconPath+"print.png");
    	printButton.setIcon(printIcon);
    	printButton.triggered.connect(this, "printNote()");
    	togglePrintButton(Global.isToolbarButtonVisible("print"));

    	tagButton = toolBar.addAction(tr("Tag")); 
    	QIcon tagIcon = new QIcon(iconPath+"tag.png");
    	tagButton.setIcon(tagIcon);
    	tagButton.triggered.connect(browserWindow, "modifyTags()");
    	toggleTagButton(Global.isToolbarButtonVisible("tag"));

    	attributeButton = toolBar.addAction(tr("Attributes")); 
    	QIcon attributeIcon = new QIcon(iconPath+"attribute.png");
    	attributeButton.setIcon(attributeIcon);
    	attributeButton.triggered.connect(this, "toggleNoteInformation()");
    	toggleAttributeButton(Global.isToolbarButtonVisible("attribute"));
    	    	
    	emailButton = toolBar.addAction(tr("Email"));
    	QIcon emailIcon = new QIcon(iconPath+"email.png");
    	emailButton.setIcon(emailIcon);
    	emailButton.triggered.connect(this, "emailNote()");
    	toggleEmailButton(Global.isToolbarButtonVisible("email"));

    	deleteButton = toolBar.addAction(tr("Delete"));   	
    	QIcon deleteIcon = new QIcon(iconPath+"delete.png");
    	deleteButton.setIcon(deleteIcon);
    	deleteButton.triggered.connect(this, "deleteNote()");
    	toggleDeleteButton(Global.isToolbarButtonVisible("delete"));

    	newButton = toolBar.addAction(tr("New"));
    	QIcon newIcon = new QIcon(iconPath+"new.png");
    	newButton.triggered.connect(this, "addNote()");
    	newButton.setIcon(newIcon);
    	toggleNewButton(Global.isToolbarButtonVisible("new"));
    	
    	allNotesButton = toolBar.addAction(tr("All Notes"));
    	QIcon allIcon = new QIcon(iconPath+"books.png");
    	allNotesButton.triggered.connect(this, "allNotes()");
    	allNotesButton.setIcon(allIcon);
    	toggleAllNotesButton(Global.isToolbarButtonVisible("allNotes"));
    	
     	//toolBar.addSeparator();
      	//toolBar.addWidget(new QLabel(tr("Quota:")));
    	//toolBar.addWidget(quotaBar);
    	//quotaBar.setSizePolicy(Policy.Minimum, Policy.Minimum);
    	updateQuotaBar();
    	//toolBar.addSeparator();
    	
    	//toolBar.addWidget(new QLabel(tr("Zoom")));
    	//toolBar.addWidget(zoomSpinner);
    	
    	//toolBar.addWidget(new QLabel("                    "));
    	//toolBar.addSeparator();
    	//toolBar.addWidget(new QLabel(tr("  Search:")));
    	//toolBar.addWidget(searchField);
    	QSizePolicy sizePolicy = new QSizePolicy();
    	sizePolicy.setHorizontalPolicy(Policy.MinimumExpanding);
    	QLabel spacer = new QLabel("");
    	spacer.setSizePolicy(sizePolicy);
    	toolBar.addWidget(spacer);
    	//searchField.setInsertPolicy(InsertPolicy.InsertAtTop);

    	//searchClearButton = toolBar.addAction("Search Clear");
    	//QIcon searchClearIcon = new QIcon(iconPath+"searchclear.png");
    	//searchClearButton.setIcon(searchClearIcon);
    	//searchClearButton.triggered.connect(this, "searchFieldCleared()");
    	//toggleSearchClearButton(Global.isToolbarButtonVisible("searchClear"));

    	logger.log(logger.HIGH, "Leaving NeverNote.setupToolBar");
    }
    // Update the sychronize button picture
    @Override
	public QMenu createPopupMenu() {
    	QMenu contextMenu = super.createPopupMenu();
    	
    	contextMenu.addSeparator();
    	QAction prevAction = addContextAction("prevArrow", tr("Previous Arrow"));
    	contextMenu.addAction(prevAction);
    	prevAction.triggered.connect(this, "togglePrevArrowButton(Boolean)");

    	QAction nextAction = addContextAction("nextArrow", tr("Next Arrow"));
    	contextMenu.addAction(nextAction);
    	nextAction.triggered.connect(this, "toggleNextArrowButton(Boolean)");

    	QAction upAction = addContextAction("upArrow", tr("Up Arrow"));
    	contextMenu.addAction(upAction);
    	upAction.triggered.connect(this, "toggleUpArrowButton(Boolean)");

    	QAction downAction = addContextAction("downArrow", tr("Down Arrow"));
    	contextMenu.addAction(downAction);
    	downAction.triggered.connect(this, "toggleDownArrowButton(Boolean)");

    	QAction synchronizeAction = addContextAction("synchronize", tr("Synchronize"));
    	contextMenu.addAction(synchronizeAction);
    	synchronizeAction.triggered.connect(this, "toggleSynchronizeButton(Boolean)");

    	QAction printAction = addContextAction("print", tr("Print"));
    	contextMenu.addAction(printAction);
    	printAction.triggered.connect(this, "togglePrintButton(Boolean)");

    	QAction tagAction = addContextAction("tag", tr("Tag"));
    	contextMenu.addAction(tagAction);
    	tagAction.triggered.connect(this, "toggleTagButton(Boolean)");
    	
    	QAction attributeAction = addContextAction("attribute", tr("Attribute"));
    	contextMenu.addAction(attributeAction);
    	attributeAction.triggered.connect(this, "toggleAttributeButton(Boolean)");
    	
    	QAction emailAction = addContextAction("email", tr("Email"));
    	contextMenu.addAction(emailAction);
    	emailAction.triggered.connect(this, "toggleEmailButton(Boolean)");

    	QAction deleteAction = addContextAction("delete", tr("Delete"));
    	contextMenu.addAction(deleteAction);
    	deleteAction.triggered.connect(this, "toggleDeleteButton(Boolean)");

    	QAction newAction = addContextAction("new", tr("Add"));
    	contextMenu.addAction(newAction);
    	newAction.triggered.connect(this, "toggleNewButton(Boolean)");

    	QAction allNotesAction = addContextAction("allNotes", tr("All Notes"));
    	contextMenu.addAction(allNotesAction);
    	allNotesAction.triggered.connect(this, "toggleAllNotesButton(Boolean)");
    	
    	QAction searchClearAction = addContextAction("searchClear", tr("Search Clear"));
    	contextMenu.addAction(searchClearAction);
    	searchClearAction.triggered.connect(this, "toggleSearchClearButton(Boolean)");
    	
    	return contextMenu;
    	
    }
    private QAction addContextAction(String config, String name) {
    	QAction newAction = new QAction(this);
		newAction.setText(name);
		newAction.setCheckable(true);
		newAction.setChecked(Global.isToolbarButtonVisible(config));
		return newAction;
    }
    private void togglePrevArrowButton(Boolean toggle) {
		prevButton.setVisible(toggle);
		Global.saveToolbarButtonsVisible("prevArrow", toggle);
    }
    private void toggleNextArrowButton(Boolean toggle) {
		nextButton.setVisible(toggle);
		Global.saveToolbarButtonsVisible("nextArrow", toggle);
    }
    private void toggleUpArrowButton(Boolean toggle) {
		upButton.setVisible(toggle);
		Global.saveToolbarButtonsVisible("upArrow", toggle);
    }
    private void toggleDownArrowButton(Boolean toggle) {
		downButton.setVisible(toggle);
		Global.saveToolbarButtonsVisible("downArrow", toggle);
    }
    private void toggleSynchronizeButton(Boolean toggle) {
		synchronizeButton.setVisible(toggle);
		Global.saveToolbarButtonsVisible("synchronize", toggle);
    }
    private void togglePrintButton(Boolean toggle) {
		printButton.setVisible(toggle);
		Global.saveToolbarButtonsVisible("print", toggle);
    }
    private void toggleTagButton(Boolean toggle) {
		tagButton.setVisible(toggle);
		Global.saveToolbarButtonsVisible("tag", toggle);
    }
    private void toggleAttributeButton(Boolean toggle) {
		attributeButton.setVisible(toggle);
		Global.saveToolbarButtonsVisible("attribute", toggle);
    }
    private void toggleEmailButton(Boolean toggle) {
		emailButton.setVisible(toggle);
		Global.saveToolbarButtonsVisible("email", toggle);
    }
    private void toggleDeleteButton(Boolean toggle) {
		deleteButton.setVisible(toggle);
		Global.saveToolbarButtonsVisible("delete", toggle);
    }
    private void toggleNewButton(Boolean toggle) {
		newButton.setVisible(toggle);
		Global.saveToolbarButtonsVisible("new", toggle);
    }
    private void toggleAllNotesButton(Boolean toggle) {
		allNotesButton.setVisible(toggle);
		Global.saveToolbarButtonsVisible("allNotes", toggle);
    }
    @SuppressWarnings("unused")
	private void toggleSearchClearButton(Boolean toggle) {
		searchClearButton.setVisible(toggle);
		Global.saveToolbarButtonsVisible("searchClear", toggle);
    }





    @SuppressWarnings("unused")
	private void updateSyncButton() {
    	    	
    	if (syncIcons == null) {
    		syncIcons = new ArrayList<QPixmap>();
    		double angle = 0.0;
    		synchronizeIconAngle = 0;
        	QPixmap pix = new QPixmap(iconPath+"synchronize.png");
    		syncIcons.add(pix);
    		for (int i=0; i<=360; i++) {
    			QPixmap rotatedPix = new QPixmap(pix.size());
    			QPainter p = new QPainter(rotatedPix);
    	    	rotatedPix.fill(toolBar.palette().color(ColorRole.Button));
    	    	QSize size = pix.size();
    	    	p.translate(size.width()/2, size.height()/2);
    	    	angle = angle+1.0;
    	    	p.rotate(angle);
    	    	p.setBackgroundMode(BGMode.OpaqueMode);
    	    	p.translate(-size.width()/2, -size.height()/2);
    	    	p.drawPixmap(0,0, pix);
    	    	p.end();
    	    	syncIcons.add(rotatedPix);
    		}
    	}

    	synchronizeIconAngle++;
    	if (synchronizeIconAngle > 359)
    		synchronizeIconAngle=0;
    	synchronizeButton.setIcon(syncIcons.get(synchronizeIconAngle));
    	
    }
    // Synchronize with Evernote

	private void evernoteSync() {
    	logger.log(logger.HIGH, "Entering NeverNote.evernoteSync");
    	if (!Global.isConnected)
    		remoteConnect();
    	if (Global.isConnected)
    		synchronizeAnimationTimer.start(5);
//			synchronizeAnimationTimer.start(200);
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
    		if (windowMaximized)
    			showMaximized();
    		else
    			showNormal();
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
    		trashNoteGuid = tempGuid;
    		currentNoteGuid = trashNoteGuid;
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
    	logger.log(logger.EXTREME, "Emptying Trash");
    	listManager.emptyTrash();
    	logger.log(logger.EXTREME, "Resetting view after trash empty");
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
        	noteIndexUpdated(false);
    	}  	
   }
    // Show/Hide trash window
	@SuppressWarnings("unused")
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
		menuBar.connectAction.setText(tr("Connect"));
		menuBar.connectAction.setToolTip(tr("Connect to Evernote"));
		menuBar.synchronizeAction.setEnabled(false);
		Global.isConnected = false;
		synchronizeAnimationTimer.stop();
		return;
	}
	// Do a manual connect/disconnect
    private void remoteConnect() {
    	
    	logger.log(logger.HIGH, "Entering NeverNote.remoteConnect");

    	// If we are already connected, we just disconnect
    	if (Global.isConnected) {
    		Global.isConnected = false;
    		syncRunner.enDisconnect();
    		setupConnectMenuOptions();
    		setupOnlineMenu();
    		return;
    	}
    	
    	OAuthTokenizer tokenizer = new OAuthTokenizer();
    	AESEncrypter aes = new AESEncrypter();
    	try {
			aes.decrypt(new FileInputStream(Global.getFileManager().getHomeDirFile("oauth.txt")));
		} catch (FileNotFoundException e) {
			// File not found, so we'll just get empty strings anyway. 
		}
    	
    	   	
		if (Global.getProxyValue("url").equals("")) {
			System.setProperty("http.proxyHost","") ;
			System.setProperty("http.proxyPort", "") ;
			System.setProperty("https.proxyHost","") ;
			System.setProperty("https.proxyPort", "") ;	    
		} else {
			// PROXY
			System.setProperty("http.proxyHost",Global.getProxyValue("url")) ;
			System.setProperty("http.proxyPort", Global.getProxyValue("port")) ;
			System.setProperty("https.proxyHost",Global.getProxyValue("url")) ;
			System.setProperty("https.proxyPort", Global.getProxyValue("port")) ;
 
			if (Global.getProxyValue("userid").equals("")) {
				Authenticator.setDefault(new Authenticator() {
    			@Override
    			protected PasswordAuthentication getPasswordAuthentication() {
    				return new
    				PasswordAuthentication(Global.getProxyValue("userid"),Global.getProxyValue("password").toCharArray());
    				}
    			});
    		}
    	}

		syncRunner.userStoreUrl = Global.userStoreUrl;
		syncRunner.noteStoreUrl = Global.noteStoreUrl;
		syncRunner.noteStoreUrlBase = Global.noteStoreUrlBase;
		
		
		
		String authString = aes.getString();
		if (!authString.equals("")) {
			tokenizer.tokenize(authString);
			syncRunner.authToken = tokenizer.oauth_token;
    		syncRunner.enConnect();
		}		

		Global.isConnected = syncRunner.isConnected;
		
		if (!Global.isConnected) {
	    	OAuthWindow window = new OAuthWindow(logger);
	    	if (window.error) {
	    		setMessage(window.errorMessage);
	    		return;
	    	}
	    	window.exec();
	    	if (window.error) {
	    		setMessage(window.errorMessage);
	    		return;
			}
	    	tokenizer.tokenize(window.response);
	    	if (tokenizer.oauth_token.equals("")) {
	    		setMessage(tr("Invalid authorization token received."));
	    		return;
	    	}
	    	aes.setString(window.response);
	    	try {
				aes.encrypt(new FileOutputStream(Global.getFileManager().getHomeDirFile("oauth.txt")));
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    	syncRunner.authToken = tokenizer.oauth_token;
			syncRunner.enConnect();
			Global.isConnected = syncRunner.isConnected;
		}
//		Global.username = syncRunner.username;
		    	
		if (!Global.isConnected)
			return;
		setupOnlineMenu();
		setupConnectMenuOptions();
		logger.log(logger.HIGH, "Leaving NeverNote.remoteConnect");


    }
    private void setupConnectMenuOptions() {
    	logger.log(logger.HIGH, "entering NeverNote.setupConnectMenuOptions");
		if (!Global.isConnected) {
			menuBar.connectAction.setText(tr("Connect"));
			menuBar.connectAction.setToolTip(tr("Connect to Evernote"));
			menuBar.synchronizeAction.setEnabled(false);
		} else {
			menuBar.connectAction.setText(tr("Disconnect"));
			menuBar.connectAction.setToolTip(tr("Disconnect from Evernote"));
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
 	
//    	clearTagFilter();
//    	clearNotebookFilter();
    	clearTrashFilter();
//    	clearSavedSearchFilter();

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
    				f.select(item.parent().indexOfChild(item));
    			else {
    				Global.containsFilter.select(item.parent().indexOfChild(item));
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
			if (parent.data(0,ItemDataRole.UserRole)==AttributeTreeWidget.Attributes.Created && 
				w.data(0,ItemDataRole.UserRole)==AttributeTreeWidget.Attributes.Since)
					return Global.createdSinceFilter;
			if (parent.data(0,ItemDataRole.UserRole)==AttributeTreeWidget.Attributes.Created && 
    			w.data(0,ItemDataRole.UserRole)==AttributeTreeWidget.Attributes.Before)
    					return Global.createdBeforeFilter;
			if (parent.data(0,ItemDataRole.UserRole)==AttributeTreeWidget.Attributes.LastModified && 
    			w.data(0,ItemDataRole.UserRole)==AttributeTreeWidget.Attributes.Since)
    					return Global.changedSinceFilter;
    		if (parent.data(0,ItemDataRole.UserRole)==AttributeTreeWidget.Attributes.LastModified && 
        		w.data(0,ItemDataRole.UserRole)==AttributeTreeWidget.Attributes.Before)
        					return Global.changedBeforeFilter;
		}
		return null;
    }

    // Show/Hide attribute search window
	@SuppressWarnings("unused")
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
		
		// If we have more than one selection, then set the merge note action to true.
    	List<QModelIndex> selections = noteTableView.selectionModel().selectedRows();
		if (selections.size() > 1) 
    		menuBar.noteMergeAction.setEnabled(true);
		else
			menuBar.noteMergeAction.setEnabled(false);

		// If the ctrl key is pressed, then they are selecting multiple 
		// entries and we don't want to change the currently viewed note.
		if (QApplication.keyboardModifiers().isSet(KeyboardModifier.ControlModifier) &&
				QApplication.mouseButtons().isSet(MouseButton.LeftButton)) 
			return;

		if (historyGuids.size() == 0) {
			historyGuids.add(currentNoteGuid);
			historyPosition = 1;
		}
    	noteTableView.showColumn(Global.noteTableGuidPosition);
    	
    	if (!Global.isColumnVisible("guid"))
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
    			if (row < listManager.getNoteTableModel().rowCount()-1)
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
		saveNote();
    	refreshEvernoteNoteList();
    	logger.log(logger.HIGH, "Calling note table reload in NeverNote.noteIndexUpdated() - "+reload);
    	noteTableView.load(reload);
    	if (currentNoteGuid == null || currentNoteGuid.equals("")) {
    		int pos;
    		if (noteTableView.proxyModel.sortOrder() == SortOrder.AscendingOrder)
    			pos = noteTableView.proxyModel.rowCount();
    		else 
    			pos = 1;
    		if (noteTableView.proxyModel.rowCount() == 0)
    			pos = 0;
    		if (pos>0)	{
    			QModelIndex i = noteTableView.proxyModel.index(pos-1, Global.noteTableGuidPosition);
    			if (i!=null) {
    				currentNoteGuid = (String)i.data();
    			}
    		}
    	}		
		if (!noteTableView.isColumnHidden(Global.noteTableGuidPosition))
			showColumns();
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
		
		//determine current note guid
		for (Note note : listManager.getNoteIndex()) {
			tempNoteGuid = note.getGuid();
			if (currentNoteGuid.equals(tempNoteGuid)) {
				saveCurrentNoteGuid = tempNoteGuid;
			}
		}
		
		if (listManager.getNoteIndex().size() == 0) {
			currentNoteGuid = "";
			currentNote = null;
			browserWindow.clear();
			browserWindow.setDisabled(true);
		} 
		
		if (Global.showDeleted && listManager.getNotebookIndex().size() > 0 && saveCurrentNoteGuid.equals("")) {
			currentNoteGuid = listManager.getNoteIndex().get(0).getGuid();
			saveCurrentNoteGuid = currentNoteGuid;
			refreshEvernoteNote(true);
		}
		
		if (!saveCurrentNoteGuid.equals("")) {
			refreshEvernoteNote(false);
		} else {
				currentNoteGuid = "";
		}
		reloadTagTree(false);

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
    	int max = listManager.getNoteTableModel().rowCount();
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
    	
    	for (int i=0; i<listManager.getNoteTableModel().rowCount(); i++) {
    		QModelIndex modelIndex =  listManager.getNoteTableModel().index(i, Global.noteTableGuidPosition);
    		if (modelIndex != null) {
    			SortedMap<Integer, Object> ix = listManager.getNoteTableModel().itemData(modelIndex);
    			String tableGuid =  (String)ix.values().toArray()[0];
    			if (tableGuid.equals(guid)) {
    				listManager.getNoteTableModel().setData(i, Global.noteTableTagPosition,tagBuffer.toString());
    				listManager.getNoteTableModel().setData(i, Global.noteTableSynchronizedPosition, "false");
    				noteTableView.proxyModel.invalidate();
    				return;
    			}
    		}
    	}
    	logger.log(logger.HIGH, "Leaving NeverNote.updateListTags");
    }
    // Update a title for a specific note in the list
    @SuppressWarnings("unused")
	private void updateListAuthor(String guid, String author) {
    	logger.log(logger.HIGH, "Entering NeverNote.updateListAuthor");

    	for (int i=0; i<listManager.getNoteTableModel().rowCount(); i++) {
    		//QModelIndex modelIndex =  noteTableView.proxyModel.index(i, Global.noteTableGuidPosition);
    		QModelIndex modelIndex =  listManager.getNoteTableModel().index(i, Global.noteTableGuidPosition);
    		if (modelIndex != null) {
    			SortedMap<Integer, Object> ix = listManager.getNoteTableModel().itemData(modelIndex);
    			String tableGuid =  (String)ix.values().toArray()[0];
    			if (tableGuid.equals(guid)) {
    				listManager.getNoteTableModel().setData(i, Global.noteTableAuthorPosition,author);
    				listManager.getNoteTableModel().setData(i, Global.noteTableSynchronizedPosition, "false");
    				noteTableView.proxyModel.invalidate();
    				return;
    			}	
    		}
    	}
    	
    	logger.log(logger.HIGH, "Leaving NeverNote.updateListAuthor");
    }
	private void updateListNoteNotebook(String guid, String notebook) {
    	logger.log(logger.HIGH, "Entering NeverNote.updateListNoteNotebook");
    	listManager.getNoteTableModel().updateNoteSyncStatus(guid, false);
    	logger.log(logger.HIGH, "Leaving NeverNote.updateListNoteNotebook");
    }
    // Update a title for a specific note in the list
    @SuppressWarnings("unused")
	private void updateListSourceUrl(String guid, String url) {
    	logger.log(logger.HIGH, "Entering NeverNote.updateListAuthor");

    	for (int i=0; i<listManager.getNoteTableModel().rowCount(); i++) {
    		//QModelIndex modelIndex =  noteTableView.proxyModel.index(i, Global.noteTableGuidPosition);
    		QModelIndex modelIndex =  listManager.getNoteTableModel().index(i, Global.noteTableGuidPosition);
    		if (modelIndex != null) {
//    			SortedMap<Integer, Object> ix = noteTableView.proxyModel.itemData(modelIndex);
    			SortedMap<Integer, Object> ix = listManager.getNoteTableModel().itemData(modelIndex);
    			String tableGuid =  (String)ix.values().toArray()[0];
    			if (tableGuid.equals(guid)) {
    				listManager.getNoteTableModel().setData(i, Global.noteTableSynchronizedPosition, "false");
    				listManager.getNoteTableModel().setData(i, Global.noteTableSourceUrlPosition,url);
    				noteTableView.proxyModel.invalidate();
    				return;
    			}	
    		}
    	}
    	logger.log(logger.HIGH, "Leaving NeverNote.updateListAuthor");
    }
	@SuppressWarnings("unused")
	private void updateListGuid(String oldGuid, String newGuid) {
    	logger.log(logger.HIGH, "Entering NeverNote.updateListTitle");

    	for (int i=0; i<listManager.getNoteTableModel().rowCount(); i++) {
    		QModelIndex modelIndex =  listManager.getNoteTableModel().index(i, Global.noteTableGuidPosition);
    		if (modelIndex != null) {
    			SortedMap<Integer, Object> ix = listManager.getNoteTableModel().itemData(modelIndex);
    			String tableGuid =  (String)ix.values().toArray()[0];
    			if (tableGuid.equals(oldGuid)) {
    				listManager.getNoteTableModel().setData(i, Global.noteTableGuidPosition,newGuid);
    				//listManager.getNoteTableModel().setData(i, Global.noteTableSynchronizedPosition, "false");
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

				for (int i=0; i<listManager.getNoteTableModel().rowCount(); i++) {
					QModelIndex modelIndex =  listManager.getNoteTableModel().index(i, Global.noteTableGuidPosition);
					if (modelIndex != null) {
						SortedMap<Integer, Object> ix = listManager.getNoteTableModel().itemData(modelIndex);
						String noteGuid = (String)ix.values().toArray()[0];
						if (noteGuid.equalsIgnoreCase(listManager.getNoteIndex().get(j).getGuid())) {
							listManager.getNoteTableModel().setData(i, Global.noteTableTagPosition, newName);
							i=listManager.getNoteTableModel().rowCount();
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
				for (int i=0; i<listManager.getNoteTableModel().rowCount(); i++) {
					QModelIndex modelIndex =  listManager.getNoteTableModel().index(i, Global.noteTableGuidPosition);
					if (modelIndex != null) {
						SortedMap<Integer, Object> ix = listManager.getNoteTableModel().itemData(modelIndex);
						String noteGuid = (String)ix.values().toArray()[0];
						if (noteGuid.equalsIgnoreCase(listManager.getNoteIndex().get(j).getGuid())) {
							listManager.getNoteTableModel().setData(i, Global.noteTableTagPosition, newName);
							i=listManager.getNoteTableModel().rowCount();
						}
					}
				}
			}
		}	
    	logger.log(logger.HIGH, "Leaving NeverNote.updateListNotebook");
    }
    private void updateListNotebookName(String oldName, String newName) {
    	logger.log(logger.HIGH, "Entering NeverNote.updateListNotebookName");

    	for (int i=0; i<listManager.getNoteTableModel().rowCount(); i++) {
    		QModelIndex modelIndex =  listManager.getNoteTableModel().index(i, Global.noteTableNotebookPosition); 
    		if (modelIndex != null) {
    			SortedMap<Integer, Object> ix = listManager.getNoteTableModel().itemData(modelIndex);
    			String tableName =  (String)ix.values().toArray()[0];
    			if (tableName.equalsIgnoreCase(oldName)) {
    				listManager.getNoteTableModel().setData(i, Global.noteTableNotebookPosition, newName);
    			}
    		}
    	}
    	logger.log(logger.HIGH, "Leaving NeverNote.updateListNotebookName");
    }
    @SuppressWarnings("unused")
	private void updateListDateCreated(String guid, QDateTime date) {
    	logger.log(logger.HIGH, "Entering NeverNote.updateListDateCreated");

    	for (int i=0; i<listManager.getNoteTableModel().rowCount(); i++) {
    		QModelIndex modelIndex =  listManager.getNoteTableModel().index(i, Global.noteTableGuidPosition);
    		if (modelIndex != null) {
    			SortedMap<Integer, Object> ix = listManager.getNoteTableModel().itemData(modelIndex);
    			String tableGuid =  (String)ix.values().toArray()[0];
    			if (tableGuid.equals(guid)) {
    				listManager.getNoteTableModel().setData(i, Global.noteTableCreationPosition, date.toString(Global.getDateFormat()+" " +Global.getTimeFormat()));
    				noteTableView.proxyModel.invalidate();
    				return;
    			}
    		}
    	}
    	logger.log(logger.HIGH, "Leaving NeverNote.updateListDateCreated");
    }
    @SuppressWarnings("unused")
	private void updateListDateSubject(String guid, QDateTime date) {
    	logger.log(logger.HIGH, "Entering NeverNote.updateListDateSubject");

    	for (int i=0; i<listManager.getNoteTableModel().rowCount(); i++) {
    		QModelIndex modelIndex =  listManager.getNoteTableModel().index(i, Global.noteTableGuidPosition);
    		if (modelIndex != null) {
    			SortedMap<Integer, Object> ix = listManager.getNoteTableModel().itemData(modelIndex);
    			String tableGuid =  (String)ix.values().toArray()[0];
    			if (tableGuid.equals(guid)) {
    				listManager.getNoteTableModel().setData(i, Global.noteTableSynchronizedPosition, "false");
    				listManager.getNoteTableModel().setData(i, Global.noteTableSubjectDatePosition, date.toString(Global.getDateFormat()+" " +Global.getTimeFormat()));
    				noteTableView.proxyModel.invalidate();
    				return;
    			}
    		}
    	}
    	logger.log(logger.HIGH, "Leaving NeverNote.updateListDateCreated");
    }
	private void updateListDateChanged(String guid, QDateTime date) {
    	logger.log(logger.HIGH, "Entering NeverNote.updateListDateChanged");

    	for (int i=0; i<listManager.getNoteTableModel().rowCount(); i++) {
    		QModelIndex modelIndex =  listManager.getNoteTableModel().index(i, Global.noteTableGuidPosition);
    		if (modelIndex != null) {
    			SortedMap<Integer, Object> ix = listManager.getNoteTableModel().itemData(modelIndex);
    			String tableGuid =  (String)ix.values().toArray()[0];
    			if (tableGuid.equals(guid)) {
    				listManager.getNoteTableModel().setData(i, Global.noteTableSynchronizedPosition, "false");
    				listManager.getNoteTableModel().setData(i, Global.noteTableChangedPosition, date.toString(Global.getDateFormat()+" " +Global.getTimeFormat()));
    				return;
    			}
    		}
    	}
    	logger.log(logger.HIGH, "Leaving NeverNote.updateListDateChanged");
    }
    private void updateListDateChanged() {
    	logger.log(logger.HIGH, "Entering NeverNote.updateListDateChanged");
    	QDateTime date = new QDateTime(QDateTime.currentDateTime());
    	updateListDateChanged(currentNoteGuid, date);
    	logger.log(logger.HIGH, "Leaving NeverNote.updateListDateChanged");
    }  
    // Redo scroll
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
	// Scroll to the current GUID in tthe list.
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
//    			noteTableView.selectionModel().blockSignals(true);
       			noteTableView.selectRow(i);
//       			noteTableView.selectionModel().blockSignals(false);
    			noteTableView.scrollTo(index, ScrollHint.EnsureVisible);  // This should work, but it doesn't
   	  			i=listManager.getNoteTableModel().rowCount();
     		}
      	}
    	noteTableView.repaint();
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
   		noteTableView.setColumnHidden(Global.noteTableGuidPosition, !Global.isColumnVisible("guid"));
   		noteTableView.setColumnHidden(Global.noteTableThumbnailPosition, !Global.isColumnVisible("thumbnail"));
   		noteTableView.setColumnHidden(Global.noteTableTitlePosition, !Global.isColumnVisible("title"));   	
   		noteTableView.setColumnHidden(Global.noteTablePinnedPosition, !Global.isColumnVisible("pinned")); 
    }
    // Title color has changed
    @SuppressWarnings("unused")
	private void titleColorChanged(Integer color) {
    	logger.log(logger.HIGH, "Entering NeverNote.titleColorChanged");

    	setNoteDirty();
    	QColor backgroundColor = new QColor();
		QColor foregroundColor = new QColor(QColor.black);
		backgroundColor.setRgb(color);
		
		if (backgroundColor.rgb() == QColor.black.rgb() || backgroundColor.rgb() == QColor.blue.rgb())
			foregroundColor.setRgb(QColor.white.rgb());
    	
		if (selectedNoteGUIDs.size() == 0)
			selectedNoteGUIDs.add(currentNoteGuid);
		
    	for (int j=0; j<selectedNoteGUIDs.size(); j++) {
    		for (int i=0; i<listManager.getNoteTableModel().rowCount(); i++) {
    			QModelIndex modelIndex =  listManager.getNoteTableModel().index(i, Global.noteTableGuidPosition);
    			if (modelIndex != null) {
    				SortedMap<Integer, Object> ix = listManager.getNoteTableModel().itemData(modelIndex);
    				String tableGuid =  (String)ix.values().toArray()[0];
    				if (tableGuid.equals(selectedNoteGUIDs.get(j))) {
    					for (int k=0; k<Global.noteTableColumnCount; k++) {
    						listManager.getNoteTableModel().setData(i, k, backgroundColor, Qt.ItemDataRole.BackgroundRole);
    						listManager.getNoteTableModel().setData(i, k, foregroundColor, Qt.ItemDataRole.ForegroundRole);
    						listManager.updateNoteTitleColor(selectedNoteGUIDs.get(j), backgroundColor.rgb());
    					}
    					i=listManager.getNoteTableModel().rowCount();
    				}
    			}
    		}
    	}
    	logger.log(logger.HIGH, "Leaving NeverNote.titleColorChanged");
    }
    // A note has been pinned or unpinned
	@SuppressWarnings("unused")
	private void notePinned() {
		logger.log(logger.EXTREME, "Entering NeverNote.notePinned()");
		setNoteDirty();

    	for (int j=0; j<selectedNoteGUIDs.size(); j++) {
    		NoteMetadata meta = listManager.getNoteMetadata().get(selectedNoteGUIDs.get(j));
    		boolean pinned = !meta.isPinned();
    		meta.setPinned(pinned);   // Toggle the pinned/unpinned 
    		
    		// Update the list & table
    		listManager.updateNoteMetadata(meta);	
    		noteTableView.proxyModel.addGuid(selectedNoteGUIDs.get(j), meta);
    	}
   	
		logger.log(logger.EXTREME, "Leaving NeverNote.setNoteDirty()");
    }
    // Wide list was chosen
    public void narrowListView() {
    	saveNoteColumnPositions();
    	saveNoteIndexWidth();
    	saveWindowState();
		int sortCol = noteTableView.proxyModel.sortColumn();
		int sortOrder = noteTableView.proxyModel.sortOrder().value();
		Global.setSortColumn(sortCol);
		Global.setSortOrder(sortOrder);

		Global.setListView(Global.View_List_Narrow);
    	
    	menuBar.wideListView.blockSignals(true);
    	menuBar.narrowListView.blockSignals(true);
    	
    	menuBar.wideListView.setChecked(false);
    	menuBar.narrowListView.setChecked(true);
    	
    	menuBar.wideListView.blockSignals(false);
    	menuBar.narrowListView.blockSignals(false);
    	
    	mainLeftRightSplitter.addWidget(noteTableView);
    	mainLeftRightSplitter.addWidget(browserWindow);
    	restoreWindowState(false);
    	noteTableView.repositionColumns();
    	noteTableView.resizeColumnWidths();
    	noteTableView.resizeRowHeights();
    	
    	sortCol = Global.getSortColumn();
		sortOrder = Global.getSortOrder();
		noteTableView.proxyModel.blocked = true;
		noteTableView.sortByColumn(sortCol, SortOrder.resolve(sortOrder));
		noteTableView.proxyModel.blocked = false;

		
    	showColumns();
    	noteTableView.load(false);
    	refreshEvernoteNote(true);
    	scrollToCurrentGuid();
    }
    public void wideListView() {
		int sortCol = noteTableView.proxyModel.sortColumn();
		int sortOrder = noteTableView.proxyModel.sortOrder().value();
		Global.setSortColumn(sortCol);
		Global.setSortOrder(sortOrder);

		saveWindowState();
    	saveNoteColumnPositions();
    	saveNoteIndexWidth();
    	Global.setListView(Global.View_List_Wide);

    	menuBar.wideListView.blockSignals(true);
    	menuBar.narrowListView.blockSignals(true);
    	
    	menuBar.wideListView.setChecked(true);
    	menuBar.narrowListView.setChecked(false);

    	menuBar.wideListView.blockSignals(false);
    	menuBar.narrowListView.blockSignals(false);
    	browserIndexSplitter.setVisible(true);
        browserIndexSplitter.addWidget(noteTableView);
        browserIndexSplitter.addWidget(browserWindow);
        restoreWindowState(false);
    	noteTableView.repositionColumns();
    	noteTableView.resizeColumnWidths();
    	noteTableView.resizeRowHeights();
    	
    	sortCol = Global.getSortColumn();
		sortOrder = Global.getSortOrder();
		noteTableView.proxyModel.blocked = true;
		noteTableView.sortByColumn(sortCol, SortOrder.resolve(sortOrder));
		noteTableView.proxyModel.blocked = false;

    	showColumns();
    	noteTableView.load(false);
    	scrollToCurrentGuid();
    }
    // Sort order for the notebook has changed   
    public void tableSortOrderChanged(Integer column, Integer order) {
    	
    	// Find what notebook (if any) is selected.  We ignore stacks & the "All Notebooks".
    	List<QTreeWidgetItem> selectedNotebook = notebookTree.selectedItems();
    	if (selectedNotebook.size() > 0 && !selectedNotebook.get(0).text(0).equalsIgnoreCase("All Notebooks") && !selectedNotebook.get(0).text(2).equalsIgnoreCase("STACK")) {
    		QTreeWidgetItem currentSelectedNotebook = selectedNotebook.get(0);
    		String notebook;
    		notebook = currentSelectedNotebook.text(2);
    		conn.getNotebookTable().setSortOrder(notebook, column, order);
    	}    	
    }
    
    //***************************************************************
    @SuppressWarnings("unused")
	private void evernoteLinkClick(String syncGuid, String locGuid) {
    	String guid = null;
    	if (conn.getNoteTable().guidExists(syncGuid)) {
    		guid = syncGuid;
    	} else {
    		// If we didn't find it via the synchronized guid, look under the local guid
    		// Iwe don't find it there, look to see if the GUID is posted under the local GUID, but was 
    		// later synchronized (that causes the guid to change so we need to find the new one).
    		if (conn.getNoteTable().guidExists(locGuid)) 
        		guid = locGuid;
        	else
        		guid = conn.getNoteTable().findAlternateGuid(locGuid);
    	}
		if (guid != null) {
			openExternalEditor(guid);
			return;
		}
    	
    	//If we've gotten this far, we can't find the note
    	QMessageBox.information(this, tr("Note Not Found"), tr("Sorry, but I can't"+
    			" seem to find that note."));
    }
    //***************************************************************
    //***************************************************************
    //** External editor window functions                    
    //***************************************************************
    //***************************************************************
	private void listDoubleClick() {
		saveNote();
    	openExternalEditor(currentNoteGuid);
    }
    private void openExternalEditor(String guid) {
    	
    	if (externalWindows.containsKey(guid)) {
    		externalWindows.get(guid).raise();
    		return;
    	}
    	Note note = conn.getNoteTable().getNote(guid, true, true, false, true, true);
    	// We have a new external editor to create
    	QIcon appIcon = new QIcon(iconPath+"nevernote.png");
    	ExternalBrowse newBrowser = new ExternalBrowse(conn);
    	newBrowser.setWindowIcon(appIcon);
    	externalWindows.put(guid, newBrowser);
    	showEditorButtons(newBrowser.getBrowserWindow());
    	loadNoteBrowserInformation(newBrowser.getBrowserWindow(), guid, note);
    	setupBrowserWindowListeners(newBrowser.getBrowserWindow(), false);
    	newBrowser.windowClosing.connect(this, "externalWindowClosing(String)");
    	//newBrowser.getBrowserWindow().noteSignal.titleChanged.connect(this, "externalWindowTitleEdited(String, String)");
    	newBrowser.getBrowserWindow().noteSignal.tagsChanged.connect(this, "externalWindowTagsEdited(String, List)");
    	newBrowser.contentsChanged.connect(this, "saveNoteExternalBrowser(String, String, Boolean, BrowserWindow)");
    	newBrowser.getBrowserWindow().blockApplication.connect(this, "blockApplication(BrowserWindow)");
    	newBrowser.getBrowserWindow().unblockApplication.connect(this, "unblockApplication()");

    	browserWindow.noteSignal.tagsChanged.connect(newBrowser, "updateTags(String, List)");
    	browserWindow.noteSignal.titleChanged.connect(newBrowser, "updateTitle(String, String)");
    	browserWindow.noteSignal.notebookChanged.connect(newBrowser, "updateNotebook(String, String)");
    	
    	newBrowser.show();
    }
    @SuppressWarnings({ "rawtypes", "unused" })
	private void externalWindowTagsEdited(String guid, List values) {
    	StringBuffer line = new StringBuffer(100);
    	for (int i=0; i<values.size(); i++) {
    		if (i>0) 
    			line.append(Global.tagDelimeter+" ");
    		line.append(values.get(i));
    	}
    	if (guid.equals(currentNoteGuid)) {
    		browserWindow.setTag(line.toString());
    	}
    }
    @SuppressWarnings("unused")
	private void externalWindowClosing(String guid) {
   		externalWindows.remove(guid);
    }

    
    
    //***************************************************************
    //***************************************************************
    //** These functions deal with Note specific things
    //***************************************************************
    //***************************************************************    
	private void setNoteDirty() {
		logger.log(logger.EXTREME, "Entering NeverNote.setNoteDirty()");
		
		// Find if the note is being edited externally.  If it is, update it.
		if (externalWindows.containsKey(currentNoteGuid)) {
			QTextCodec codec = QTextCodec.codecForName("UTF-8");
	        QByteArray unicode =  codec.fromUnicode(browserWindow.getContent());
			ExternalBrowse window = externalWindows.get(currentNoteGuid);
    		window.getBrowserWindow().setContent(unicode);
		}
		
		// If the note is dirty, then it is unsynchronized by default.
		if (noteDirty) 
			return;
		
		// Set the note as dirty and check if its status is synchronized in the display table
		noteDirty = true;
		if (listManager.getNoteMetadata().containsKey(currentNoteGuid) && 
				listManager.getNoteMetadata().get(currentNoteGuid).isDirty()) {
				return;
		}
		
		// If this wasn't already marked as unsynchronized, then we need to update the table
		listManager.getNoteTableModel().updateNoteSyncStatus(currentNoteGuid, false);
//    	listManager.getUnsynchronizedNotes().add(currentNoteGuid);
    	for (int i=0; i<listManager.getNoteTableModel().rowCount(); i++) {
    		QModelIndex modelIndex =  listManager.getNoteTableModel().index(i, Global.noteTableGuidPosition);
    		if (modelIndex != null) {
    			SortedMap<Integer, Object> ix = listManager.getNoteTableModel().itemData(modelIndex);
    			String tableGuid =  (String)ix.values().toArray()[0];
    			if (tableGuid.equals(currentNoteGuid)) {
    				listManager.getNoteTableModel().proxyModel.setData(i, Global.noteTableSynchronizedPosition, "false");
    				return;
    			}
    		}
    	}
   	
		logger.log(logger.EXTREME, "Leaving NeverNote.setNoteDirty()");
    }
    @SuppressWarnings("unused")
	private void saveNoteExternalBrowser(String guid, String content, Boolean save, BrowserWindow browser) {
		QTextCodec codec = QTextCodec.codecForName("UTF-8");
        QByteArray unicode =  codec.fromUnicode(content);
    	noteCache.remove(guid);
		noteCache.put(guid, unicode.toString());
    	if (guid.equals(currentNoteGuid)) {
    		noteDirty = true;
    		browserWindow.setContent(unicode);
    	} 
    	if (save) {
    		thumbnailRunner.addWork("GENERATE "+ guid);
    		saveNote(guid, browser);
    	}
    	
    }
    private void saveNote() {
    	if (noteDirty) {
    		saveNote(currentNoteGuid, browserWindow);
    		thumbnailRunner.addWork("GENERATE "+ currentNoteGuid);
    		noteDirty = false;
    	} 
    }
    private void saveNote(String guid, BrowserWindow window) {
		logger.log(logger.EXTREME, "Inside NeverNote.saveNote()");
   		waitCursor(true);
    		
		logger.log(logger.EXTREME, "Saving to cache");
		QTextCodec codec = QTextCodec.codecForLocale();
//	        QTextDecoder decoder = codec.makeDecoder();
		codec = QTextCodec.codecForName("UTF-8");
        QByteArray unicode =  codec.fromUnicode(window.getContent());
   		noteCache.put(guid, unicode.toString());
			
   		logger.log(logger.EXTREME, "updating list manager");
   		listManager.updateNoteContent(guid, window.getContent());
		logger.log(logger.EXTREME, "Updating title");
   		listManager.updateNoteTitle(guid, window.getTitle());
   		updateListDateChanged();

   		logger.log(logger.EXTREME, "Looking through note index for refreshed note");
   		for (int i=0; i<listManager.getNoteIndex().size(); i++) {
    		if (listManager.getNoteIndex().get(i).getGuid().equals(guid)) {
    			currentNote = listManager.getNoteIndex().get(i);
    			i = listManager.getNoteIndex().size();
    		}
    	}
    	waitCursor(false);
    }
    // Get a note from Evernote (and put it in the browser)
	private void refreshEvernoteNote(boolean reload) {
		logger.log(logger.HIGH, "Entering NeverNote.refreshEvernoteNote");
		
		if (Global.disableViewing) {
			browserWindow.setEnabled(false);
			return;
		}
		inkNote = false;
		readOnly = false;
		if (Global.showDeleted || currentNoteGuid == null || currentNoteGuid.equals(""))
			readOnly = true;
		Global.cryptCounter =0;
		if (readOnly) {
			browserWindow.setReadOnly(true);
		}
		
		if (!reload)
			return;
		
		waitCursor(true);
		browserWindow.loadingData(true);

		currentNote = conn.getNoteTable().getNote(currentNoteGuid, true,true,false,false,true);
		if (currentNote == null) 
			return;
		loadNoteBrowserInformation(browserWindow, currentNoteGuid, currentNote);
	}

	private void loadNoteBrowserInformation(BrowserWindow browser, String guid, Note note) {
		NoteFormatter	formatter = new NoteFormatter(logger, conn, tempFiles);
		formatter.setNote(note, Global.pdfPreview());
		formatter.setHighlight(listManager.getEnSearch());
		QByteArray js;
		if (!noteCache.containsKey(guid)) {
			js = new QByteArray();
			// We need to prepend the note with <HEAD></HEAD> or encoded characters are ugly 
			js.append("<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">");		
			js.append("<style type=\"text/css\">.en-crypt-temp { border-collapse:collapse; border-style:solid; border-color:blue; padding:0.0mm 0.0mm 0.0mm 0.0mm; }</style>");
			js.append("<style type=\"text/css\">en-hilight { background-color: rgb(255,255,0) }</style>");
			js.append("<style> img { height:auto; width:auto; max-height:auto; max-width:100%; }</style>");
			if (Global.displayRightToLeft())
				js.append("<style> body { direction:rtl; }</style>");
			js.append("<style type=\"text/css\">en-spell { text-decoration: none; border-bottom: dotted 1px #cc0000; }</style>");
			js.append("</head>");
			formatter.setNote(note, Global.pdfPreview());
			js.append(formatter.rebuildNoteHTML());
			js.append("</HTML>");
			js.replace("<!DOCTYPE en-note SYSTEM 'http://xml.evernote.com/pub/enml.dtd'>", "");
			js.replace("<!DOCTYPE en-note SYSTEM 'http://xml.evernote.com/pub/enml2.dtd'>", "");
			js.replace("<?xml version='1.0' encoding='UTF-8'?>", "");
//	        if (Global.enableHTMLEntitiesFix) {
//	        	browser.getBrowser().setContent(new QByteArray(StringEscapeUtils.unescapeHtml(js.toString())));
//	        } else
	        	browser.setContent(js);
			noteCache.put(guid, js.toString());

			if (formatter.resourceError)
				resourceErrorMessage();
			if (formatter.formatError) {
				waitCursor(false);
			     QMessageBox.information(this, tr("Error"),
						tr("NixNote had issues formatting this note." +
						" To protect your data this note is being marked as read-only."));	
			     waitCursor(true);
			}
			readOnly = formatter.readOnly;
			inkNote = formatter.inkNote;
			if (readOnly)
				readOnlyCache.put(guid, true);
			if (inkNote)
				inkNoteCache.put(guid, true);
		} else {
			logger.log(logger.HIGH, "Note content is being pulled from the cache");
			String cachedContent = formatter.modifyCachedTodoTags(noteCache.get(guid));
			js = new QByteArray(cachedContent);
			browser.setContent(js);
			if (readOnlyCache.containsKey(guid))
					readOnly = true;
			if (inkNoteCache.containsKey(guid))
					inkNote = true;
		}
		if (conn.getNoteTable().isThumbnailNeeded(guid)) {
			thumbnailHTMLReady(guid, js, Global.calculateThumbnailZoom(js.toString()));
		}
		if (readOnly || inkNote || 
				(note.getAttributes() != null && note.getAttributes().getContentClass() != null && note.getAttributes().getContentClass() != ""))
			browser.getBrowser().page().setContentEditable(false);  // We don't allow editing of ink notes
		else
			browser.getBrowser().page().setContentEditable(true);
		browser.setReadOnly(readOnly);
		deleteButton.setEnabled(!readOnly);
		tagButton.setEnabled(!readOnly);
		menuBar.noteDelete.setEnabled(!readOnly);
		menuBar.noteTags.setEnabled(!readOnly);
		browser.setNote(note);
		
		if (note != null && note.getNotebookGuid() != null && 
				conn.getNotebookTable().isLinked(note.getNotebookGuid())) {
			deleteButton.setEnabled(false);
			menuBar.notebookDeleteAction.setEnabled(false);
		} else {
			deleteButton.setEnabled(true);
			menuBar.notebookDeleteAction.setEnabled(true);
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
		
		browser.setTitle(note.getTitle());
		browser.setTag(getTagNamesForNote(note));
		browser.setAuthor(note.getAttributes().getAuthor());

		browser.setAltered(note.getUpdated());
		browser.setCreation(note.getCreated());
		if (note.getAttributes().getSubjectDate() > 0)
			browser.setSubjectDate(note.getAttributes().getSubjectDate());
		else
			browser.setSubjectDate(note.getCreated());
		browser.setUrl(note.getAttributes().getSourceURL());
		
		FilterEditorTags tagFilter = new FilterEditorTags(conn, logger);
		List<Tag> tagList = tagFilter.getValidTags(note);
		browser.setAllTags(tagList);
		
		browser.setCurrentTags(note.getTagNames());
		noteDirty = false;
		scrollToGuid(guid);
		
		browser.loadingData(false);
		if (thumbnailViewer.isActiveWindow())
			thumbnailView();
		
		FilterEditorNotebooks notebookFilter = new FilterEditorNotebooks(conn, logger);
		browser.setNotebookList(notebookFilter.getValidNotebooks(note, listManager.getNotebookIndex()));

		waitCursor(false);
		logger.log(logger.HIGH, "Leaving NeverNote.refreshEvernoteNote");
	}
	// Save a generated thumbnail
	private void toggleNoteInformation() {
		logger.log(logger.HIGH, "Entering NeverNote.toggleNoteInformation");
    	browserWindow.toggleInformation();
    	menuBar.noteAttributes.setChecked(browserWindow.isExtended());
    	Global.saveWindowVisible("noteInformation", browserWindow.isExtended());
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
//            url.addQueryItem("body", QUrl.toPercentEncoding(text2).toString());
            url.addQueryItem("body", text2);
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
    	indexRunner.addWork("REINDEXALL");
    	setMessage(tr("Database will be reindexed."));
    	logger.log(logger.HIGH, "Leaving NeverNote.fullReindex");
    }
    // Listener when a user wants to reindex a specific note
    @SuppressWarnings("unused")
	private void reindexNote() {
    	logger.log(logger.HIGH, "Entering NeverNote.reindexNote");
		for (int i=0; i<selectedNoteGUIDs.size(); i++) {
			indexRunner.addWork("REINDEXNOTE "+selectedNoteGUIDs.get(i));
		}
		if (selectedNotebookGUIDs.size() > 1)
			setMessage(tr("Notes will be reindexed."));
		else
			setMessage(tr("Note will be reindexed."));
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
    	String title = null;
    	if (selectedNoteGUIDs.size() == 1)
    		title = conn.getNoteTable().getNote(selectedNoteGUIDs.get(0),false,false,false,false,false).getTitle();

    	// If we are deleting non-trash notes
    	if (currentNote.isActive()) { 
    		if (Global.verifyDelete()) {
    			String msg;
    			if (selectedNoteGUIDs.size() > 1) {
    				msg = new String(tr("Delete ") +selectedNoteGUIDs.size() +" notes?");
    			} else {
    				if (title != null)
    					msg = new String(tr("Delete note \"") +title +"\"?");
    				else  				
    					msg = new String(tr("Delete note selected note?"));
    			}
    			if (QMessageBox.question(this, tr("Confirmation"), msg,
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
    			String msg;
    			if (selectedNoteGUIDs.size() > 1) {
    				msg = new String(tr("Permanently delete ") +selectedNoteGUIDs.size() +" notes?");
    			} else {
    				if (title != null)
     	    			msg = new String(tr("Permanently delete note \"") +title +"\"?");
    				else
    					msg = new String(tr("Permanently delete note selected note?"));
    			}
    			if (QMessageBox.question(this, "Confirmation", msg,
    				QMessageBox.StandardButton.Yes, 
					QMessageBox.StandardButton.No)==StandardButton.No.value()) {
    					return;
    			}
    		}
    		if (selectedNoteGUIDs.size() == 0 && !currentNoteGuid.equals("")) 
    			selectedNoteGUIDs.add(currentNoteGuid);
    		for (int i=selectedNoteGUIDs.size()-1; i>=0; i--) {
    			for (int j=listManager.getNoteTableModel().rowCount()-1; j>=0; j--) {
    	    		QModelIndex modelIndex =  listManager.getNoteTableModel().index(j, Global.noteTableGuidPosition);
    	    		if (modelIndex != null) {
    	    			SortedMap<Integer, Object> ix = listManager.getNoteTableModel().itemData(modelIndex);
    	    			String tableGuid =  (String)ix.values().toArray()[0];
    	    			if (tableGuid.equals(selectedNoteGUIDs.get(i))) {
    	    				listManager.getNoteTableModel().removeRow(j);
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
     	StringBuffer noteString = new StringBuffer(100);
     	noteString.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
     		"<!DOCTYPE en-note SYSTEM \"http://xml.evernote.com/pub/enml2.dtd\">\n" +
     		"<en-note>\n");
     	
     	if (Global.overrideDefaultFont()) {
     		noteString.append("<font face=\"" +Global.getDefaultFont() +"\" >");
     		noteString.append("<span style=\"font-size:" +Global.getDefaultFontSize() +"pt;\">");
     		noteString.append("<br clear=\"none\" />\n");
     		noteString.append("</span>\n</font>\n");
     	} else
     		noteString.append("<br clear=\"none\" />\n");
     	noteString.append("</en-note>");
	
    	Long l = new Long(currentTime.getTimeInMillis());
    	String randint = new String(Long.toString(l));    	
    	
    	// Find a notebook.  We first look for a selected notebook (the "All Notebooks" one doesn't count).  
    	// Then we look
    	// for the first non-archived notebook.  Finally, if nothing else we 
    	// pick the first notebook in the list.
    	String notebook = null;
    	listManager.getNotebookIndex().get(0).getGuid();
    	List<QTreeWidgetItem> selectedNotebook = notebookTree.selectedItems();
    	if (selectedNotebook.size() > 0 && !selectedNotebook.get(0).text(0).equalsIgnoreCase("All Notebooks") && !selectedNotebook.get(0).text(2).equalsIgnoreCase("STACK")) {
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
    				//goodNotebooks.add(listManager.getNotebookIndex().get(i).deepCopy());
        			goodNotebooks.add((Notebook)Global.deepCopy(listManager.getNotebookIndex().get(i)));
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
    	newNote.setTitle("Untitled Note");
    	newNote.setContent(noteString.toString());
    	newNote.setDeleted(0);
    	newNote.setCreated(System.currentTimeMillis());
    	newNote.setUpdated(System.currentTimeMillis());
    	newNote.setActive(true);
    	NoteAttributes na = new NoteAttributes();
    	na.setLatitude(0.0);
    	na.setLongitude(0.0);
    	na.setAltitude(0.0);
    	newNote.setAttributes(new NoteAttributes());
		newNote.setTagGuids(new ArrayList<String>());
		newNote.setTagNames(new ArrayList<String>());
    	
    	// If new notes are to be created based upon the selected tags, then we need to assign the tags
    	if (Global.newNoteWithSelectedTags()) {	
    		List<QTreeWidgetItem> selections = tagTree.selectedItems();
        	QTreeWidgetItem currentSelection;
        	for (int i=0; i<selections.size(); i++) {
        		currentSelection = selections.get(i);
        		newNote.getTagGuids().add(currentSelection.text(2));
        		newNote.getTagNames().add(currentSelection.text(0));
        	}
    	}
    	
    	conn.getNoteTable().addNote(newNote, true);
    	NoteMetadata metadata = new NoteMetadata();
    	metadata.setGuid(newNote.getGuid());
    	metadata.setDirty(true);
    	listManager.addNote(newNote, metadata);
//    	noteTableView.insertRow(newNote, true, -1);
    	
    	currentNote = newNote;
    	currentNoteGuid = currentNote.getGuid();
    	noteTableView.clearSelection();
    	refreshEvernoteNote(true);
    	listManager.countNotebookResults(listManager.getNoteIndex());
    	browserWindow.titleLabel.setFocus();
    	browserWindow.titleLabel.selectAll();
//    	notebookTree.updateCounts(listManager.getNotebookIndex(), listManager.getNotebookCounter());
    	
    	// If the window is hidden, then we want to popup this in an external window & 
    	if (!isVisible())
    		listDoubleClick();
    	waitCursor(false);
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
    @SuppressWarnings("unused")
	private void updateNoteTitle(String guid, String title) {
    	listManager.setNoteSynchronized(guid, false);
    	
    	// We do this manually because if we've edited the note in an 
    	// external window we run into the possibility of signal recursion
    	// looping.
    	if (guid.equals(currentNoteGuid)) {
    		browserWindow.titleLabel.blockSignals(true);
    		browserWindow.titleLabel.setText(title);
    		browserWindow.titleLabel.blockSignals(false);
    	}
    }
    // Signal received that note content has changed.  Normally we just need the guid to remove
    // it from the cache.
    @SuppressWarnings("unused")
	private void invalidateNoteCache(String guid, String content) {
    	noteCache.remove(guid);
		refreshEvernoteNote(true);
    }
    // Signal received that a note guid has changed
    @SuppressWarnings("unused")
	private void noteGuidChanged(String oldGuid, String newGuid) {
    	if (noteCache.containsKey(oldGuid)) {
    		if (!oldGuid.equals(currentNoteGuid)) {
    			String cache = noteCache.get(oldGuid);
    			noteCache.put(newGuid, cache);
    			noteCache.remove(oldGuid);
    		} else {
    			noteCache.remove(oldGuid);
    			noteCache.put(newGuid, browserWindow.getContent());
    		}
    	}
  
    	listManager.updateNoteGuid(oldGuid, newGuid, false);
    	if (currentNoteGuid.equals(oldGuid)) {
    		if (currentNote != null)
    			currentNote.setGuid(newGuid);
    		currentNoteGuid = newGuid;
    	}
   		
    	if (externalWindows.containsKey(oldGuid)) {
   			ExternalBrowse b = externalWindows.get(oldGuid);
   			externalWindows.remove(oldGuid);
   			b.getBrowserWindow().getNote().setGuid(newGuid);
   			externalWindows.put(newGuid, b);
   		}

    	for (int i=0; i<listManager.getNoteIndex().size(); i++) {
    		if (listManager.getNoteIndex().get(i).getGuid().equals(newGuid)) {
    			noteTableView.proxyModel.addGuid(newGuid, listManager.getNoteMetadata().get(newGuid));
    			i=listManager.getNoteIndex().size();
    		}
    	}
    	
    	if (listManager.getNoteTableModel().metaData.containsKey(oldGuid)) {
    		NoteMetadata meta = listManager.getNoteTableModel().metaData.get(oldGuid);
    		listManager.getNoteTableModel().metaData.put(newGuid, meta);
    		listManager.getNoteTableModel().metaData.remove(oldGuid);
    	}
    	
    }
    // Toggle the note editor button bar
    private void toggleEditorButtonBar() {
    	if (browserWindow.buttonsVisible) {
    		browserWindow.hideButtons();
    		menuBar.showEditorBar.setChecked(browserWindow.buttonsVisible);
//    		Global.saveWindowVisible("editorButtonBar", browserWindow.buttonsVisible);
    	} else {
    		browserWindow.buttonsVisible = true;
    		showEditorButtons(browserWindow);
    	}
    	Global.saveWindowVisible("editorButtonBar", browserWindow.buttonsVisible);
    }
    // Show editor buttons
    private void showEditorButtons(BrowserWindow browser) {
   		browser.buttonLayout.setVisible(true);
   		browser.undoAction.setVisible(false);
   		
   		browser.undoButton.setVisible(false);

   		browser.undoAction.setVisible(Global.isEditorButtonVisible("undo"));
   		browser.redoAction.setVisible(Global.isEditorButtonVisible("redo"));
   		browser.cutAction.setVisible(Global.isEditorButtonVisible("cut"));
   		browser.copyAction.setVisible(Global.isEditorButtonVisible("copy"));
   		browser.pasteAction.setVisible(Global.isEditorButtonVisible("paste"));
   		browser.strikethroughAction.setVisible(Global.isEditorButtonVisible("strikethrough"));
   		browser.underlineAction.setVisible(Global.isEditorButtonVisible("underline"));
   		browser.boldAction.setVisible(Global.isEditorButtonVisible("bold"));
   		browser.italicAction.setVisible(Global.isEditorButtonVisible("italic"));
   		browser.hlineAction.setVisible(Global.isEditorButtonVisible("hline"));
   		browser.indentAction.setVisible(Global.isEditorButtonVisible("indent"));
   		browser.outdentAction.setVisible(Global.isEditorButtonVisible("outdent"));
   		browser.bulletListAction.setVisible(Global.isEditorButtonVisible("bulletList"));
   		browser.numberListAction.setVisible(Global.isEditorButtonVisible("numberList"));
   		browser.fontListAction.setVisible(Global.isEditorButtonVisible("font"));
   		browser.fontSizeAction.setVisible(Global.isEditorButtonVisible("fontSize"));
   		browser.fontColorAction.setVisible(Global.isEditorButtonVisible("fontColor"));
   		browser.fontHilightAction.setVisible(Global.isEditorButtonVisible("fontHilight"));
   		browser.leftAlignAction.setVisible(Global.isEditorButtonVisible("alignLeft"));
   		browser.centerAlignAction.setVisible(Global.isEditorButtonVisible("alignCenter"));
   		browser.rightAlignAction.setVisible(Global.isEditorButtonVisible("alignRight"));
   		browser.spellCheckAction.setVisible(Global.isEditorButtonVisible("spellCheck"));
   		browser.todoAction.setVisible(Global.isEditorButtonVisible("todo"));
    }
    private void duplicateNote(String guid) {
		
		Note oldNote = conn.getNoteTable().getNote(guid, true, false,false,false,true);
		List<Resource> resList = conn.getNoteTable().noteResourceTable.getNoteResources(guid, true);
		oldNote.setContent(conn.getNoteTable().getNoteContentNoUTFConversion(guid));
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
					
//		Note newNote = oldNote.deepCopy();
		Note newNote = (Note)Global.deepCopy(oldNote);
		newNote.setUpdateSequenceNum(0);
		newNote.setGuid(newGuid);
		newNote.setDeleted(0);
		newNote.setActive(true);
		
		/*
		List<String> tagNames = new ArrayList<String>();
		List<String> tagGuids = new ArrayList<String>();;
		for (int i=0; i<oldNote.getTagGuidsSize(); i++) {
			tagNames.add(oldNote.getTagNames().get(i));
			tagGuids.add(oldNote.getTagGuids().get(i));
		}

		// Sort note Tags to make them look nice
		for (int i=0; i<tagNames.size()-1; i++) {
			if (tagNames.get(i).compareTo(tagNames.get(i+1))<0) {
				String n1 = tagNames.get(i);
				String n2 = tagNames.get(i+1);
				tagNames.set(i, n2);
				tagNames.set(i+1, n1);
			}
		}
		newNote.setTagGuids(tagGuids);
		newNote.setTagNames(tagNames);
		
		// Add tag guids to note
		*/
		
		// Duplicate resources
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
			conn.getNoteTable().noteResourceTable.saveNoteResource(
					(Resource)Global.deepCopy(resList.get(i)), true);
		}
		newNote.setResources(resList);
		
		// Add note to the database
		conn.getNoteTable().addNote(newNote, true);
		NoteMetadata metaData = new NoteMetadata();
		NoteMetadata oldMeta = listManager.getNoteMetadata().get(oldNote.getGuid());
		metaData.copy(oldMeta);
		metaData.setGuid(newNote.getGuid());
		listManager.addNote(newNote, metaData);
		noteTableView.insertRow(newNote, metaData, true, -1);
		currentNoteGuid = newNote.getGuid();
		currentNote = newNote;
		refreshEvernoteNote(true);
		listManager.countNotebookResults(listManager.getNoteIndex());
		waitCursor(false);
	}
	// View all notes
	@SuppressWarnings("unused")
	private void allNotes() {
		clearAttributeFilter();
		clearNotebookFilter();
		clearSavedSearchFilter();
		clearTrashFilter();
		clearTagFilter();
		searchField.clear();
		if (Global.mimicEvernoteInterface) {
			notebookTree.selectGuid("");
		}
		notebookTreeSelection();
		refreshEvernoteNote(true);
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
		if (oldGuid != null && !oldGuid.equals(newGuid))
			Global.resourceMap.put(oldGuid, newGuid);
	}
	// View a thumbnail of the note
	public void thumbnailView() {
		
		String thumbnailName = Global.getFileManager().getResDirPath("thumbnail-" + currentNoteGuid + ".png");
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
	// An error happened while saving a note.  Inform the user
	@SuppressWarnings("unused")
	private void saveRunnerError(String guid, String msg) {
		if (msg == null) {
			String title = "*Unknown*";
			for (int i=0; i<listManager.getMasterNoteIndex().size(); i++) {
				if (listManager.getMasterNoteIndex().get(i).getGuid().equals(guid)) {
					title = listManager.getMasterNoteIndex().get(i).getTitle();
					i=listManager.getMasterNoteIndex().size();
				}
			}
			msg = tr("An error has happened while saving the note \"") +title+
			tr("\".\n\nThis is probably due to a document that is too complex for NixNote to process.  "+
			"As a result, changes to the note may not be saved properly in the database."+
			"\n\nA cached copy is being preserved so you can recover any data, but data may" +
			"\nbe lost.  Please review the note to recover any critical data before restarting.");
			
			QMessageBox.information(this, tr("Error Saving Note"), tr(msg));
		}
	}
	private void thumbnailHTMLReady(String guid, QByteArray html, Integer zoom) {
		logger.log(logger.HIGH, "Entering thumnailHTMLReady()");
		logger.log(logger.HIGH, "Thumbnail ready for " +guid);
		// Find an idle preview object
		for (int i=0; i<thumbGenerators.size(); i++) {
			if (thumbGenerators.get(i).mutex.tryLock()) {
				logger.log(logger.EXTREME, "Idle generator found - loading thumbnail for " +guid);
				thumbGenerators.get(i).loadContent(guid, html, zoom);
				return;
			}
		} 
		if (thumbGenerators.size() >= 1) {
			logger.log(logger.EXTREME, "No available thumbnail generators.  Aborting " +guid);
			return;
		}
		
		logger.log(logger.EXTREME, "Creating new thumbnail generator " +guid);
		Thumbnailer preview = new Thumbnailer(logger, conn, listManager, thumbnailRunner);
		thumbGenerators.add(preview);

		if (preview.mutex.tryLock()) {
			logger.log(logger.EXTREME, "Loading thumbnail for  " +guid);
			preview.loadContent(guid, html, zoom);
		}
		logger.log(logger.HIGH, "Exiting thumnailHTMLReady()");
	}
	
	
	
	//**********************************************************
    //**********************************************************
    //* Online user actions
    //**********************************************************
    //**********************************************************
    private void setupOnlineMenu() {
    	if (!Global.isConnected) {
    		menuBar.noteOnlineHistoryAction.setEnabled(false);
    		menuBar.selectiveSyncAction.setEnabled(false);
    		return;
    	} else {
    		menuBar.noteOnlineHistoryAction.setEnabled(true);
    		menuBar.selectiveSyncAction.setEnabled(true);
    	}
    }
    @SuppressWarnings("unused")
	private void viewNoteHistory() {
    	if (currentNoteGuid == null || currentNoteGuid.equals("")) 
    		return;
    	if (currentNote.getUpdateSequenceNum() == 0) {
    		setMessage(tr("Note has never been synchronized."));
			QMessageBox.information(this, tr("Error"), tr("This note has never been sent to Evernote, so there is no history."));
			return;
    	}
    	
    	setMessage(tr("Getting Note History"));
    	waitCursor(true);
    	Note currentOnlineNote = null;
    	versions = null;
    	try {
    		if (Global.isPremium())
    			versions = syncRunner.localNoteStore.listNoteVersions(syncRunner.authToken, currentNoteGuid);
    		else
    			versions = new ArrayList<NoteVersionId>();
    		currentOnlineNote = syncRunner.localNoteStore.getNote(syncRunner.authToken, currentNoteGuid, true, true, false, false);
		} catch (EDAMUserException e) {
			setMessage("EDAMUserException: " +e.getMessage());
			return;
		} catch (EDAMSystemException e) {
			setMessage("EDAMSystemException: " +e.getMessage());
			return;
		} catch (EDAMNotFoundException e) {
			setMessage(tr("Note not found on server."));
			QMessageBox.information(this, tr("Error"), tr("This note could not be found on Evernote's servers."));
			return;
		} catch (TException e) {
			setMessage("EDAMTransactionException: " +e.getMessage());
			return;
		}
		
		// If we've gotten this far, we have a good note.
		if (historyWindow == null) {
			historyWindow = new OnlineNoteHistory(logger, conn);
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
		setMessage(tr("History retrieved"));
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
			StringBuilder versionDate = new StringBuilder(simple.format(versions.get(i).getSaved()));
			if (versionDate.toString().equals(selection))
				index = i;
		}
		
		if (index > -1 || selection.indexOf("Current") > -1) {
			Note historyNote = null;
			try {
				if (index > -1) {
					usn = versions.get(index).getUpdateSequenceNum();
					historyNote = syncRunner.localNoteStore.getNoteVersion(syncRunner.authToken, currentNoteGuid, usn, true, true, true);
				} else
					historyNote = syncRunner.localNoteStore.getNote(syncRunner.authToken, currentNoteGuid, true,true,true,true);
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
    	setMessage(tr("Restoring as new note."));
    	duplicateNote(reloadHistoryWindow(historyWindow.historyCombo.currentText()));
    	setMessage(tr("Note has been restored as a new note."));
    }
    @SuppressWarnings("unused")
	private void restoreHistoryNote() {
    	setMessage(tr("Restoring note."));
    	Note n = reloadHistoryWindow(historyWindow.historyCombo.currentText());
    	conn.getNoteTable().expungeNote(n.getGuid(), true, false);
    	n.setActive(true);
    	n.setDeleted(0);
		for (int i=0; i<n.getResourcesSize(); i++) {
			n.getResources().get(i).setActive(true);
			conn.getNoteTable().noteResourceTable.saveNoteResource(n.getResources().get(i), true);
		}
		NoteMetadata metadata = new NoteMetadata();
		metadata.setGuid(n.getGuid());
    	listManager.addNote(n, metadata);
    	conn.getNoteTable().addNote(n, true);
    	refreshEvernoteNote(true);
    	setMessage(tr("Note has been restored."));
    }
    @SuppressWarnings("unused")
	private void setupSelectiveSync() {
    	
    	// Get a list of valid notebooks
    	List<Notebook> notebooks = null; 
    	List<Tag> tags = null;
    	List<LinkedNotebook> linkedNotebooks = null;
    	try {
   			notebooks = syncRunner.localNoteStore.listNotebooks(syncRunner.authToken);
   			tags = syncRunner.localNoteStore.listTags(syncRunner.authToken);
   			linkedNotebooks = syncRunner.localNoteStore.listLinkedNotebooks(syncRunner.authToken);
		} catch (EDAMUserException e) {
			setMessage("EDAMUserException: " +e.getMessage());
			return;
		} catch (EDAMSystemException e) {
			setMessage("EDAMSystemException: " +e.getMessage());
			return;
		} catch (TException e) {
			setMessage("EDAMTransactionException: " +e.getMessage());
			return;
		} catch (EDAMNotFoundException e) {
			setMessage("EDAMNotFoundException: " +e.getMessage());
			return;
		}
    	
		// Split up notebooks into synchronized & non-synchronized
    	List<Notebook> ignoredBooks = new ArrayList<Notebook>();
    	List<String> dbIgnoredNotebooks = conn.getSyncTable().getIgnoreRecords("NOTEBOOK");
    	
    	for (int i=notebooks.size()-1; i>=0; i--) {
    		for (int j=0; j<dbIgnoredNotebooks.size(); j++) {
    			if (notebooks.get(i).getGuid().equalsIgnoreCase(dbIgnoredNotebooks.get(j))) {
    				ignoredBooks.add(notebooks.get(i));
    				j=dbIgnoredNotebooks.size();
    			}
    		}
    	}
    	
    	// split up tags into synchronized & non-synchronized
    	List<Tag> ignoredTags = new ArrayList<Tag>();
    	List<String> dbIgnoredTags = conn.getSyncTable().getIgnoreRecords("TAG");
    	
    	for (int i=tags.size()-1; i>=0; i--) {
    		for (int j=0; j<dbIgnoredTags.size(); j++) {
    			if (tags.get(i).getGuid().equalsIgnoreCase(dbIgnoredTags.get(j))) {
    				ignoredTags.add(tags.get(i));
    				j=dbIgnoredTags.size();
    			}
    		}
    	}
    	
    	// split up linked notebooks into synchronized & non-synchronized
    	List<LinkedNotebook> ignoredLinkedNotebooks = new ArrayList<LinkedNotebook>();
    	List<String> dbIgnoredLinkedNotebooks = conn.getSyncTable().getIgnoreRecords("LINKEDNOTEBOOK");
    	for (int i=linkedNotebooks.size()-1; i>=0; i--) {
    		String notebookGuid = linkedNotebooks.get(i).getGuid();
    		for (int j=0; j<dbIgnoredLinkedNotebooks.size(); j++) {
    			if (notebookGuid.equalsIgnoreCase(dbIgnoredLinkedNotebooks.get(j))) {
    				ignoredLinkedNotebooks.add(linkedNotebooks.get(i));
    				j=dbIgnoredLinkedNotebooks.size();
    			}
    		}
    	}
    	
		IgnoreSync ignore = new IgnoreSync(notebooks, ignoredBooks, tags, ignoredTags, linkedNotebooks, ignoredLinkedNotebooks);
		ignore.exec();
		if (!ignore.okClicked())
			return;
		
		waitCursor(true);
		
		// Clear out old notebooks & add  the new ones
		List<String> oldIgnoreNotebooks = conn.getSyncTable().getIgnoreRecords("NOTEBOOK");
		for (int i=0; i<oldIgnoreNotebooks.size(); i++) {
			conn.getSyncTable().deleteRecord("IGNORENOTEBOOK-"+oldIgnoreNotebooks.get(i));
		}
		
		List<String> newNotebooks = new ArrayList<String>();
		for (int i=ignore.getIgnoredBookList().count()-1; i>=0; i--) {
			String text = ignore.getIgnoredBookList().takeItem(i).text();
			for (int j=0; j<notebooks.size(); j++) {
				if (notebooks.get(j).getName().equalsIgnoreCase(text)) {
					Notebook n = notebooks.get(j);
					conn.getSyncTable().addRecord("IGNORENOTEBOOK-"+n.getGuid(), n.getGuid());
					j=notebooks.size();
					newNotebooks.add(n.getGuid());
				}
			}
		}
		
		// Clear out old tags & add new ones
		List<String> oldIgnoreTags = conn.getSyncTable().getIgnoreRecords("TAG");
		for (int i=0; i<oldIgnoreTags.size(); i++) {
			conn.getSyncTable().deleteRecord("IGNORETAG-"+oldIgnoreTags.get(i));
		}
		
		List<String> newTags = new ArrayList<String>();
		for (int i=ignore.getIgnoredTagList().count()-1; i>=0; i--) {
			String text = ignore.getIgnoredTagList().takeItem(i).text();
			for (int j=0; j<tags.size(); j++) {
				if (tags.get(j).getName().equalsIgnoreCase(text)) {
					Tag t = tags.get(j);
					conn.getSyncTable().addRecord("IGNORETAG-"+t.getGuid(), t.getGuid());
					newTags.add(t.getGuid());
					j=tags.size();
				}
			}
		}
		
		// Clear out old tags & add new ones
		List<String> oldIgnoreLinkedNotebooks = conn.getSyncTable().getIgnoreRecords("LINKEDNOTEBOOK");
		for (int i=0; i<oldIgnoreLinkedNotebooks.size(); i++) {
			conn.getSyncTable().deleteRecord("IGNORELINKEDNOTEBOOK-"+oldIgnoreLinkedNotebooks.get(i));
		}
		
		List<String> newLinked = new ArrayList<String>();
		for (int i=ignore.getIgnoredLinkedNotebookList().count()-1; i>=0; i--) {
			String text = ignore.getIgnoredLinkedNotebookList().takeItem(i).text();
			for (int j=0; j<linkedNotebooks.size(); j++) {
				if (linkedNotebooks.get(j).getShareName().equalsIgnoreCase(text)) {
					LinkedNotebook t = linkedNotebooks.get(j);
					conn.getSyncTable().addRecord("IGNORELINKEDNOTEBOOK-"+t.getGuid(), t.getGuid());
					newLinked.add(t.getGuid());
					j=linkedNotebooks.size();
				}
			}
		}
		
		conn.getNoteTable().expungeIgnoreSynchronizedNotes(newNotebooks, newTags, newLinked);
		waitCursor(false);
		refreshLists();
    }
    
    
	//**********************************************************
	//**********************************************************
	//* XML Modifying methods
	//**********************************************************
	//**********************************************************
	// An error has happended fetching a resource.  let the user know
	private void resourceErrorMessage() {
		if (inkNote)
			return;
		waitCursor(false);
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
		browserWindow.setReadOnly(true);
		waitCursor(true);
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
				thumbnailRunner.interrupt = true;
				saveNoteIndexWidth();
				saveNoteColumnPositions();
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
		setMessage(tr("Finalizing Synchronization"));
		syncThreadsReady++;
		syncRunning = false;
		syncRunner.syncNeeded = false;
		synchronizeAnimationTimer.stop();
		synchronizeButton.setIcon(new QIcon(iconPath+"synchronize.png"));
		saveNote();
		if (currentNote == null) {
			currentNote = conn.getNoteTable().getNote(currentNoteGuid, false, false, false, false, true);
		}
		listManager.refreshNoteMetadata();
		noteIndexUpdated(true);
		noteTableView.selectionModel().blockSignals(true);
		scrollToGuid(currentNoteGuid);
		noteTableView.selectionModel().blockSignals(false);
		refreshEvernoteNote(false);
		scrollToGuid(currentNoteGuid);
		waitCursor(false);
		
		// Check to see if there were any shared notebook errors
		if (syncRunner.error && syncRunner.errorSharedNotebooks.size() > 0) {
			String guid = syncRunner.errorSharedNotebooks.get(0);
			String notebookGuid = conn.getLinkedNotebookTable().getLocalNotebookGuid(guid);
			String localName = listManager.getNotebookNameByGuid(notebookGuid);
			SharedNotebookSyncError syncDialog = new SharedNotebookSyncError(localName);
			syncDialog.exec();
			if (syncDialog.okPressed()) {
				if (syncDialog.doNothing.isChecked()) {
					syncRunner.errorSharedNotebooksIgnored.put(guid, guid);
					evernoteSync();
				}
				if (syncDialog.deleteNotebook.isChecked()) {
					conn.getNoteTable().expungeNotesByNotebook(notebookGuid, true, false);
					conn.getNotebookTable().expungeNotebook(notebookGuid, false);
					conn.getLinkedNotebookTable().expungeNotebook(guid, false);
					conn.getLinkedNotebookTable().expungeNotebook(guid, false);
					evernoteSync();
				}
				refreshLists();
				return;
			}
		}
		
		// Finalize the synchronization
		if (!syncRunner.error)
			setMessage(tr("Synchronization Complete"));
		else
			setMessage(tr("Synchronization completed with errors.  Please check the log for details."));
		logger.log(logger.MEDIUM, "Sync complete.");
	}   
	public void saveUploadAmount(long t) {
		Global.saveUploadAmount(t);
	}
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
		if (!indexDisabled && indexRunner.idle) { 
			thumbnailRunner.interrupt = true;
			indexRunner.addWork("SCAN");
		}
		logger.log(logger.EXTREME, "Leaving NixNote index timer");
	}

	@SuppressWarnings("unused")
	private void indexStarted() {
		setMessage(tr("Indexing notes"));
	}
	@SuppressWarnings("unused")
	private void indexComplete() {
		setMessage(tr("Index complete"));
	}
	@SuppressWarnings("unused")
	private synchronized void toggleNoteIndexing() {
		logger.log(logger.HIGH, "Entering NeverNote.toggleIndexing");
		indexDisabled = !indexDisabled;
		if (!indexDisabled)
			setMessage(tr("Indexing is now enabled."));
		else
			setMessage(tr("Indexing is now disabled."));
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
			if (tagDeadCount > MAX && !disableTagThreadCheck) {
				QMessageBox.information(this, tr("A thread has died."), tr("It appears as the tag counter thread has died.  I recommend "+
				"checking stopping NixNote, saving the logs for later viewing, and restarting.  Sorry."));
				disableTagThreadCheck = true;
			}
		} else
			tagDeadCount=0;
		
		alive = listManager.threadCheck(Global.notebookCounterThreadId);
		if (!alive) {
			notebookThreadDeadCount++;
			if (notebookThreadDeadCount > MAX && !disableNotebookThreadCheck) {
				QMessageBox.information(this, tr("A thread has died."), tr("It appears as the notebook counter thread has died.  I recommend "+
					"checking stopping NixNote, saving the logs for later viewing, and restarting.  Sorry."));
				disableNotebookThreadCheck=true;
			}
		} else
			notebookThreadDeadCount=0;
		
		alive = listManager.threadCheck(Global.trashCounterThreadId);
		if (!alive) {
			trashDeadCount++;
			if (trashDeadCount > MAX && !disableTrashThreadCheck) {
				QMessageBox.information(this, tr("A thread has died."), ("It appears as the trash counter thread has died.  I recommend "+
					"checking stopping NixNote, saving the logs for later viewing, and restarting.  Sorry."));
				disableTrashThreadCheck = true;
			}
		} else
			trashDeadCount = 0;

		alive = listManager.threadCheck(Global.saveThreadId);
		if (!alive) {
			saveThreadDeadCount++;
			if (saveThreadDeadCount > MAX && !disableSaveThreadCheck) {
				QMessageBox.information(this, tr("A thread has died."), tr("It appears as the note saver thread has died.  I recommend "+
					"checking stopping NixNote, saving the logs for later viewing, and restarting.  Sorry."));
				disableSaveThreadCheck = true;
			}
		} else
			saveThreadDeadCount=0;

		if (!syncThread.isAlive()) {
			syncThreadDeadCount++;
			if (syncThreadDeadCount > MAX && !disableSyncThreadCheck) {
				QMessageBox.information(this, tr("A thread has died."), tr("It appears as the synchronization thread has died.  I recommend "+
					"checking stopping NixNote, saving the logs for later viewing, and restarting.  Sorry."));
				disableSyncThreadCheck = true;
			}
		} else
			syncThreadDeadCount=0;

		if (!indexThread.isAlive()) {
			indexThreadDeadCount++;
			if (indexThreadDeadCount > MAX && !disableIndexThreadCheck) {
				QMessageBox.information(this, tr("A thread has died."), tr("It appears as the index thread has died.  I recommend "+
					"checking stopping NixNote, saving the logs for later viewing, and restarting.  Sorry."));
				disableIndexThreadCheck = true;
			}
		} else
			indexThreadDeadCount=0;

		
	}

	private void thumbnailTimer() {
		if (Global.enableThumbnails() && !syncRunning && indexRunner.idle) {
			thumbnailRunner.addWork("SCAN");
		}
	}
	
	//**************************************************
	//* Backup & Restore
	//**************************************************
	@SuppressWarnings("unused")
	private void databaseBackup() {
		QFileDialog fd = new QFileDialog(this);
		fd.setFileMode(FileMode.AnyFile);
		fd.setConfirmOverwrite(true);
		fd.setWindowTitle(tr("Backup Database"));
		fd.setFilter(tr("NixNote Export (*.nnex);;All Files (*.*)"));
		fd.setAcceptMode(AcceptMode.AcceptSave);
		if (saveLastPath == null || saveLastPath.equals(""))
			fd.setDirectory(System.getProperty("user.home"));
		else
			fd.setDirectory(saveLastPath);
		if (fd.exec() == 0 || fd.selectedFiles().size() == 0) {
			return;
		}
		
		
    	waitCursor(true);
    	saveLastPath = fd.selectedFiles().get(0);
    	saveLastPath = saveLastPath.substring(0,saveLastPath.lastIndexOf("/"));
    	setMessage(tr("Backing up database"));
    	saveNote();
//    	conn.backupDatabase(Global.getUpdateSequenceNumber(), Global.getSequenceDate());
    	
    	ExportData noteWriter = new ExportData(conn, true);
    	String fileName = fd.selectedFiles().get(0);

    	if (!fileName.endsWith(".nnex"))
    		fileName = fileName +".nnex";
    	noteWriter.exportData(fileName);
    	setMessage(tr("Database backup completed."));
 

    	waitCursor(false);
	}
	@SuppressWarnings("unused")
	private void databaseRestore() {
		if (QMessageBox.question(this, tr("Confirmation"),
				tr("This is used to restore a database from backups.\n" +
				"It is HIGHLY recommened that this only be used to populate\n" +
				"an empty database.  Restoring into a database that\n already has data" +
				" can cause problems.\n\nAre you sure you want to continue?"),
				QMessageBox.StandardButton.Yes, 
				QMessageBox.StandardButton.No)==StandardButton.No.value()) {
					return;
				}
		
		
		QFileDialog fd = new QFileDialog(this);
		fd.setFileMode(FileMode.ExistingFile);
		fd.setConfirmOverwrite(true);
		fd.setWindowTitle(tr("Restore Database"));
		fd.setFilter(tr("NixNote Export (*.nnex);;All Files (*.*)"));
		fd.setAcceptMode(AcceptMode.AcceptOpen);
		if (saveLastPath == null || saveLastPath.equals(""))
			fd.setDirectory(System.getProperty("user.home"));
		else
			fd.setDirectory(saveLastPath);
		if (fd.exec() == 0 || fd.selectedFiles().size() == 0) {
			return;
		}
		
		
		waitCursor(true);
    	saveLastPath = fd.selectedFiles().get(0);
    	saveLastPath = saveLastPath.substring(0,saveLastPath.lastIndexOf("/"));

		setMessage(tr("Restoring database"));
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
    	setMessage(tr("Database has been restored."));
    	waitCursor(false);
	}
	@SuppressWarnings("unused")
	private void exportNotes() {
		QFileDialog fd = new QFileDialog(this);
		fd.setFileMode(FileMode.AnyFile);
		fd.setConfirmOverwrite(true);
		fd.setWindowTitle(tr("Backup Database"));
		fd.setFilter(tr("NixNote Export (*.nnex);;All Files (*.*)"));
		fd.setAcceptMode(AcceptMode.AcceptSave);
		fd.setDirectory(System.getProperty("user.home"));
		if (fd.exec() == 0 || fd.selectedFiles().size() == 0) {
			return;
		}
		
		
    	waitCursor(true);
    	setMessage(tr("Exporting Notes"));
    	saveNote();
    	
		if (selectedNoteGUIDs.size() == 0 && !currentNoteGuid.equals("")) 
			selectedNoteGUIDs.add(currentNoteGuid);
		
    	ExportData noteWriter = new ExportData(conn, false, selectedNoteGUIDs);
    	String fileName = fd.selectedFiles().get(0);

    	if (!fileName.endsWith(".nnex"))
    		fileName = fileName +".nnex";
    	noteWriter.exportData(fileName);
    	setMessage(tr("Export completed."));
 

    	waitCursor(false);
		
	}
	@SuppressWarnings("unused")
	private void importNotes() {
		QFileDialog fd = new QFileDialog(this);
		fd.setFileMode(FileMode.ExistingFile);
		fd.setConfirmOverwrite(true);
		fd.setWindowTitle(tr("Import Notes"));
		fd.setFilter(tr("NixNote Export (*.nnex);;Evernote Export (*.enex);;All Files (*.*)"));
		fd.setAcceptMode(AcceptMode.AcceptOpen);
		if (saveLastPath == null || saveLastPath.equals(""))
			fd.setDirectory(System.getProperty("user.home"));
		else
			fd.setDirectory(saveLastPath);
		if (fd.exec() == 0 || fd.selectedFiles().size() == 0) {
			return;
		}
		
		
    	waitCursor(true);
    	setMessage(tr("Importing Notes"));
    	saveNote();
    	
		if (selectedNoteGUIDs.size() == 0 && !currentNoteGuid.equals("")) 
			selectedNoteGUIDs.add(currentNoteGuid);
		
    	String fileName = fd.selectedFiles().get(0);
//    	saveLastPath.substring(0,fileName.lastIndexOf("/"));

    	if (fileName.endsWith(".nnex")) {
        	ImportData noteReader = new ImportData(conn, false);
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
    	} else {
        	if (fileName.endsWith(".enex")) {
            	ImportEnex noteReader = new ImportEnex(conn, false);
        		if (selectedNotebookGUIDs != null && selectedNotebookGUIDs.size() > 0) 
        			noteReader.setNotebookGuid(selectedNotebookGUIDs.get(0));
        		else
        			noteReader.setNotebookGuid(listManager.getNotebookIndex().get(0).getGuid());
  
        		waitCursor(false);
        		if (QMessageBox.question(this, tr("Confirmation"), 
        				tr("Create new tags from import?"),
        				QMessageBox.StandardButton.Yes, 
        				QMessageBox.StandardButton.No) == StandardButton.Yes.value()) {
        							noteReader.createNewTags = true;
        		} else
        			noteReader.createNewTags = false;
        		waitCursor(true);
        		noteReader.importData(fileName);
    	
        		if (noteReader.lastError != 0) {
        			setMessage(noteReader.getErrorMessage());
        			logger.log(logger.LOW, "Import problem: " +noteReader.lastError);
        			waitCursor(false);
        			return;
        		}
        	}
    	}
    	
    	listManager.loadNoteTitleColors();
    	refreshLists();
    	refreshEvernoteNote(false);
    	setMessage(tr("Notes have been imported."));
    	waitCursor(false);
    	
    	setMessage(tr("Import completed."));
 

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
	//* Action from when a user clicks Copy As URL
	//**************************************************
	@SuppressWarnings("unused")
	private void copyAsUrlClicked() {
		QClipboard clipboard = QApplication.clipboard();
		QMimeData mime = new QMimeData();
		String url;
		mime.setText(currentNoteGuid);
		List<QUrl> urls = new ArrayList<QUrl>();
		
		// Start building the URL
		User user = Global.getUserInformation();

		// Check that we have everything we need
   		if ((user.getShardId().equals("") || user.getId() == 0) && !Global.bypassSynchronizationWarning()) {
   			SynchronizationRequiredWarning warning = new SynchronizationRequiredWarning(this);
   			warning.exec();
   			if (!warning.neverSynchronize())
   				return;
   			else {
   				Global.setBypassSynchronizationWarning(true);
   				user.setShardId("s0");
   				user.setId(0);
   			}	
   		}

		
		// Start building a list of URLs based upon the selected notes
    	noteTableView.showColumn(Global.noteTableGuidPosition);
    	
    	List<QModelIndex> selections = noteTableView.selectionModel().selectedRows();
    	if (!Global.isColumnVisible("guid"))
    		noteTableView.hideColumn(Global.noteTableGuidPosition);

   		// Check that the note is either synchronized, or in a local notebook
   		for (int i=0; i<selections.size(); i++) {
   			QModelIndex index;
   			int row = selections.get(i).row();
    		index = noteTableView.proxyModel.index(row, Global.noteTableGuidPosition);
    		SortedMap<Integer, Object> ix = noteTableView.proxyModel.itemData(index);
       		String selectedGuid = (String)ix.values().toArray()[0];
       		
       		Note n = conn.getNoteTable().getNote(selectedGuid, false, false, false, false, false);
       		if (n.getUpdateSequenceNum() == 0 && !conn.getNotebookTable().isNotebookLocal(n.getNotebookGuid())) {
       			QMessageBox.critical(this, tr("Please Synchronize") ,tr("Please either synchronize or move any " +
       					"new notes to a local notebook."));
       			return; 
       		}
   		}

   		// Start building the URLs
    	for (int i=0; i<selections.size(); i++) {
    		QModelIndex index;
   			int row = selections.get(i).row();
    		index = noteTableView.proxyModel.index(row, Global.noteTableGuidPosition);
    		SortedMap<Integer, Object> ix = noteTableView.proxyModel.itemData(index);
       		String selectedGuid = (String)ix.values().toArray()[0];
       		mime.setText(selectedGuid);
		
       		String lid;
       		String gid;
       		Note selectedNote = conn.getNoteTable().getNote(selectedGuid, false, false, false, false, false);
       		if (selectedNote.getUpdateSequenceNum() > 0) {
       			gid = selectedGuid;
       			lid = selectedGuid;
       		} else {
       			gid = "00000000-0000-0000-0000-000000000000";
       			lid = selectedGuid;
       		}
       		url = new String("evernote://///view/") + new String(user.getId() + "/" +user.getShardId() +"/"
       				+gid+"/"+lid +"/");
       		urls.add(new QUrl(url));
    	}
		mime.setUrls(urls);
		clipboard.setMimeData(mime);
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
			logger.log(logger.LOW, "Adding file monitor: " +records.get(i).folder);
			if (records.get(i).keep) 
				importKeepWatcher.addPath(records.get(i).folder);
			else
				importDeleteWatcher.addPath(records.get(i).folder);
		}
		
		logger.log(logger.EXTREME, "List of directories being watched (kept)...");
		List<String> monitorDelete = importKeepWatcher.directories();
		for (int i=0; i<monitorDelete.size(); i++) {
			logger.log(logger.EXTREME, monitorDelete.get(i));
		}
		logger.log(logger.EXTREME, "<end of list>");
		logger.log(logger.EXTREME, "List of directories being watched (delete)...");
		monitorDelete = importDeleteWatcher.directories();
		for (int i=0; i<monitorDelete.size(); i++) {
			logger.log(logger.EXTREME, monitorDelete.get(i));
		}
		logger.log(logger.EXTREME, "<end of list>");
		
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
	
	// Menu folderImport action triggered
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
		logger.log(logger.LOW, "Inside folderImportKeep");
		String whichOS = System.getProperty("os.name");
		if (whichOS.contains("Windows")) 
			dirName = dirName.replace('/','\\');
		
		FileImporter importer = new FileImporter(logger, conn);
		
		QDir dir = new QDir(dirName);
		List<QFileInfo> list = dir.entryInfoList();
		String notebook = conn.getWatchFolderTable().getNotebook(dirName);

		for (int i=0; i<list.size(); i++){
			logger.log(logger.LOW, "File found: " +list.get(i).fileName());
			boolean redundant = false;
			// Check if we've already imported this one or if it existed before
			for (int j=0; j<importedFiles.size(); j++) {
				logger.log(logger.LOW, "redundant file list: " +list.get(i).absoluteFilePath());
				if (importedFiles.get(j).equals(list.get(i).absoluteFilePath()))
					redundant = true;
			}
			
			logger.log(logger.LOW, "Checking if redundant: " +redundant);
			if (!redundant) {
				importer.setFileInfo(list.get(i));
				importer.setFileName(list.get(i).absoluteFilePath());
			
			
				logger.log(logger.LOW, "File importing is a file: " +list.get(i).isFile());
				logger.log(logger.LOW, "File importing is a valid: " +importer.isValidType());
				if (list.get(i).isFile() && importer.isValidType()) {
			
					if (!importer.importFile()) {
						// If we can't get to the file, it is probably locked.  We'll try again later.
						logger.log(logger.LOW, "Unable to save externally edited file.  Saving for later.");
						importFilesKeep.add(list.get(i).absoluteFilePath());
					} else {

						Note newNote = importer.getNote();
						newNote.setNotebookGuid(notebook);
						newNote.setTitle(dir.at(i));
						NoteMetadata metadata = new NoteMetadata();
						metadata.setDirty(true);
						metadata.setGuid(newNote.getGuid());
						listManager.addNote(newNote, metadata);
						conn.getNoteTable().addNote(newNote, true);
						noteTableView.insertRow(newNote, metadata, true, -1);
						listManager.updateNoteContent(newNote.getGuid(), importer.getNoteContent());
						listManager.countNotebookResults(listManager.getNoteIndex());
						importedFiles.add(list.get(i).absoluteFilePath());
					}
				}
			}
		}
        
        
	}
	
	public void folderImportDelete(String dirName) {
		logger.log(logger.LOW, "Inside folderImportDelete");
		String whichOS = System.getProperty("os.name");
		if (whichOS.contains("Windows")) 
			dirName = dirName.replace('/','\\');
		
		FileImporter importer = new FileImporter(logger, conn);
		QDir dir = new QDir(dirName);
		List<QFileInfo> list = dir.entryInfoList();
		String notebook = conn.getWatchFolderTable().getNotebook(dirName);
		
		for (int i=0; i<list.size(); i++){
			logger.log(logger.LOW, "File found: " +list.get(i).fileName());
			importer.setFileInfo(list.get(i));
			importer.setFileName(list.get(i).absoluteFilePath());
			
			logger.log(logger.LOW, "File importing is a file: " +list.get(i).isFile());
			logger.log(logger.LOW, "File importing is a valid: " +importer.isValidType());
			if (list.get(i).isFile() && importer.isValidType()) {
		
				if (!importer.importFile()) {
					// If we can't get to the file, it is probably locked.  We'll try again later.
					logger.log(logger.LOW, "Unable to save externally edited file.  Saving for later.");
					importFilesKeep.add(list.get(i).absoluteFilePath());
				} else {
		
					Note newNote = importer.getNote();
					newNote.setNotebookGuid(notebook);
					newNote.setTitle(dir.at(i));
					NoteMetadata metadata = new NoteMetadata();
					metadata.setDirty(true);
					metadata.setGuid(newNote.getGuid());
					listManager.addNote(newNote, metadata);
					conn.getNoteTable().addNote(newNote, true);
					noteTableView.insertRow(newNote, metadata, true, -1);
					listManager.updateNoteContent(newNote.getGuid(), importer.getNoteContent());
					listManager.countNotebookResults(listManager.getNoteIndex());
					dir.remove(dir.at(i));
				}
			}
		}
	}
	
	
	//**************************************************
	//* External events
	//**************************************************
	private void externalFileEdited(String fileName) throws NoSuchAlgorithmException {
		logger.log(logger.HIGH, "Entering exernalFileEdited");

		// Strip URL prefix and base dir path
		String dPath = FileUtils.toForwardSlashedPath(Global.getFileManager().getResDirPath());
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
        	updateResourceContentHash(browserWindow, r.getGuid(), oldHash, newHash);
        }
        if (externalWindows.containsKey(r.getNoteGuid())) {
        	updateResourceContentHash(externalWindows.get(r.getNoteGuid()).getBrowserWindow(), 
        			r.getGuid(), oldHash, newHash);
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
        
        if (externalWindows.containsKey(r.getNoteGuid())) {
        	QWebSettings.setMaximumPagesInCache(0);
			QWebSettings.setObjectCacheCapacities(0, 0, 0);
			externalWindows.get(r.getNoteGuid()).getBrowserWindow().getBrowser().triggerPageAction(WebAction.Reload);
			
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
	public void updateResourceContentHash(BrowserWindow browser, String guid, String oldHash, String newHash) {
		int position = browserWindow.getContent().indexOf("en-tag=\"en-media\" guid=\""+guid+"\" type=");
		int endPos;
		for (;position>-1;) {
			endPos = browser.getContent().indexOf(">", position+1);
			String oldSegment = browser.getContent().substring(position,endPos);
			int hashPos = oldSegment.indexOf("hash=\"");
			int hashEnd = oldSegment.indexOf("\"", hashPos+7);
			String hash = oldSegment.substring(hashPos+6, hashEnd);
			if (hash.equalsIgnoreCase(oldHash)) {
				String newSegment = oldSegment.replace(oldHash, newHash);
				String content = browser.getContent().substring(0,position) +
				                 newSegment +
				                 browser.getContent().substring(endPos);
				browser.setContent(new QByteArray(content));;
			}
			
			position = browser.getContent().indexOf("en-tag=\"en-media\" guid=\""+guid+"\" type=", position+1);
		}
	}


	//*************************************************
	//* Minimize to tray
	//*************************************************
	@Override
	public void changeEvent(QEvent e) {
		if (e.type() == QEvent.Type.WindowStateChange) {
			if (QSystemTrayIcon.isSystemTrayAvailable()) {
				if (isMinimized() && (Global.showTrayIcon() || Global.showTrayIcon())) {
					e.accept();
					QTimer.singleShot(10, this, "hide()");
					return;
				}
				if (isMaximized())
					windowMaximized = true;
				else 
					windowMaximized = false;
			}
 		}
	}
	
	//*************************************************
	//* Check database userid & passwords
	//*************************************************
	private static boolean databaseCheck(String url,String userid, String userPassword, String cypherPassword) {
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

	//*************************************************
	//* View / Hide source HTML for a note
	//*************************************************
	public void viewSource() {
		browserWindow.showSource(menuBar.viewSource.isChecked());
	}
	//*************************************************
	// Block the program.  This is used for things  
	// like async web calls.
	//*************************************************
	@SuppressWarnings("unused")
	private void blockApplication(BrowserWindow b) {
		// Block all signals
		waitCursor(true);
		blockSignals(true);
		
		blockTimer = new QTimer();
		blockTimer.setSingleShot(true);
		blockTimer.setInterval(15000);
		blockTimer.timeout.connect(this, "unblockApplication()");
		blockingWindow  = b;
		blockTimer.start();
	}
	
	@SuppressWarnings("unused")
	private void unblockApplication() {
		waitCursor(false);
		if (blockingWindow != null && new GregorianCalendar().getTimeInMillis() > blockingWindow.unblockTime && blockingWindow.unblockTime != -1) {
			QMessageBox.critical(null, tr("No Response from CodeCogs") ,tr("Unable to contact CodeCogs for LaTeX formula."));
			blockingWindow.unblockTime = -1;
			blockingWindow.awaitingHttpResponse = false;
		}
		blockingWindow = null;
		blockSignals(false);
	}
}
