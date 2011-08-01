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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.FileNameMap;
import java.net.URI;
import java.net.URLConnection;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;

import com.evernote.edam.limits.Constants;
import com.evernote.edam.type.Data;
import com.evernote.edam.type.Note;
import com.evernote.edam.type.Notebook;
import com.evernote.edam.type.Resource;
import com.evernote.edam.type.ResourceAttributes;
import com.evernote.edam.type.Tag;
import com.swabunga.spell.engine.Configuration;
import com.swabunga.spell.engine.SpellDictionary;
import com.swabunga.spell.engine.SpellDictionaryHashMap;
import com.swabunga.spell.engine.Word;
import com.swabunga.spell.event.SpellCheckEvent;
import com.swabunga.spell.event.SpellCheckListener;
import com.swabunga.spell.event.SpellChecker;
import com.swabunga.spell.event.StringWordTokenizer;
import com.trolltech.qt.core.QByteArray;
import com.trolltech.qt.core.QCoreApplication;
import com.trolltech.qt.core.QDataStream;
import com.trolltech.qt.core.QDateTime;
import com.trolltech.qt.core.QEvent;
import com.trolltech.qt.core.QEvent.Type;
import com.trolltech.qt.core.QFile;
import com.trolltech.qt.core.QFileSystemWatcher;
import com.trolltech.qt.core.QIODevice;
import com.trolltech.qt.core.QMimeData;
import com.trolltech.qt.core.QTextCodec;
import com.trolltech.qt.core.QUrl;
import com.trolltech.qt.core.Qt;
import com.trolltech.qt.core.Qt.Key;
import com.trolltech.qt.core.Qt.KeyboardModifier;
import com.trolltech.qt.core.Qt.KeyboardModifiers;
import com.trolltech.qt.gui.QAction;
import com.trolltech.qt.gui.QApplication;
import com.trolltech.qt.gui.QCalendarWidget;
import com.trolltech.qt.gui.QClipboard;
import com.trolltech.qt.gui.QClipboard.Mode;
import com.trolltech.qt.gui.QColor;
import com.trolltech.qt.gui.QComboBox;
import com.trolltech.qt.gui.QDateEdit;
import com.trolltech.qt.gui.QDesktopServices;
import com.trolltech.qt.gui.QFileDialog;
import com.trolltech.qt.gui.QFileDialog.AcceptMode;
import com.trolltech.qt.gui.QFileDialog.FileMode;
import com.trolltech.qt.gui.QFont;
import com.trolltech.qt.gui.QFontDatabase;
import com.trolltech.qt.gui.QFormLayout;
import com.trolltech.qt.gui.QGridLayout;
import com.trolltech.qt.gui.QHBoxLayout;
import com.trolltech.qt.gui.QIcon;
import com.trolltech.qt.gui.QImage;
import com.trolltech.qt.gui.QKeyEvent;
import com.trolltech.qt.gui.QKeySequence;
import com.trolltech.qt.gui.QLabel;
import com.trolltech.qt.gui.QLineEdit;
import com.trolltech.qt.gui.QListWidgetItem;
import com.trolltech.qt.gui.QMatrix;
import com.trolltech.qt.gui.QMessageBox;
import com.trolltech.qt.gui.QPalette;
import com.trolltech.qt.gui.QPalette.ColorRole;
import com.trolltech.qt.gui.QPushButton;
import com.trolltech.qt.gui.QShortcut;
import com.trolltech.qt.gui.QSplitter;
import com.trolltech.qt.gui.QTextEdit;
import com.trolltech.qt.gui.QTextEdit.LineWrapMode;
import com.trolltech.qt.gui.QTimeEdit;
import com.trolltech.qt.gui.QToolButton;
import com.trolltech.qt.gui.QToolButton.ToolButtonPopupMode;
import com.trolltech.qt.gui.QVBoxLayout;
import com.trolltech.qt.gui.QWidget;
import com.trolltech.qt.network.QNetworkAccessManager;
import com.trolltech.qt.network.QNetworkReply;
import com.trolltech.qt.network.QNetworkReply.NetworkError;
import com.trolltech.qt.network.QNetworkRequest;
import com.trolltech.qt.webkit.QWebPage;
import com.trolltech.qt.webkit.QWebPage.WebAction;
import com.trolltech.qt.webkit.QWebSettings;
import com.trolltech.qt.webkit.QWebView;

import cx.fbn.nevernote.Global;
import cx.fbn.nevernote.dialog.EnCryptDialog;
import cx.fbn.nevernote.dialog.EnDecryptDialog;
import cx.fbn.nevernote.dialog.GeoDialog;
import cx.fbn.nevernote.dialog.InsertLatexImage;
import cx.fbn.nevernote.dialog.InsertLinkDialog;
import cx.fbn.nevernote.dialog.SpellCheck;
import cx.fbn.nevernote.dialog.TableDialog;
import cx.fbn.nevernote.dialog.TagAssign;
import cx.fbn.nevernote.evernote.EnCrypt;
import cx.fbn.nevernote.filters.FilterEditorTags;
import cx.fbn.nevernote.signals.NoteResourceSignal;
import cx.fbn.nevernote.signals.NoteSignal;
import cx.fbn.nevernote.sql.DatabaseConnection;
import cx.fbn.nevernote.utilities.ApplicationLogger;
import cx.fbn.nevernote.utilities.FileUtils;
import cx.fbn.nevernote.utilities.Pair;
import cx.fbn.nevernote.xml.HtmlTagModifier;

public class BrowserWindow extends QWidget {

	public final QLineEdit titleLabel;
	private final QLineEdit urlText;
	private final QLabel authorLabel;
	private final QLineEdit authorText;
	private final QComboBox geoBox;
	public final TagLineEdit tagEdit;
	public final QLabel tagLabel;
	private final QPushButton urlLabel;
	private final QLabel alteredLabel;
	private final QDateEdit alteredDate;
	private final QTimeEdit alteredTime;
	private final QDateEdit createdDate;
	private final QTimeEdit createdTime;
	private final QLabel subjectLabel;
	private final QDateEdit subjectDate;
	private final QTimeEdit subjectTime;
	public final QComboBox notebookBox;
	private final QLabel notebookLabel;
	private final QLabel createdLabel;
	public final QComboBox fontSize;
	public final QAction	fontSizeAction;
	private boolean extendedOn;
	public boolean buttonsVisible;
	private final String iconPath;
	private final ContentView browser;
	private final QTextEdit sourceEdit;
	private String sourceEditHeader;
	Highlighter syntaxHighlighter;
	private List<Tag> allTags;
	private List<String> currentTags;
	public NoteSignal noteSignal;
	public Signal2<String,String> evernoteLinkClicked;
	private List<Notebook> notebookList;
	private Note currentNote;
	private String saveNoteTitle;
	private String saveTagList;
	private boolean insideList;
	private final DatabaseConnection conn;
	private final QCalendarWidget createdCalendarWidget;
	private final QCalendarWidget alteredCalendarWidget;
	private final QCalendarWidget subjectCalendarWidget;

	public final QPushButton undoButton;
	public final QAction	undoAction;
	public final QPushButton redoButton;
	public final QAction	redoAction;
	public final QPushButton cutButton;
	public final QAction	cutAction;
	public final QPushButton copyButton;
	public final QAction	copyAction;
	public final QPushButton pasteButton;
	public final QAction	pasteAction;
	public final QPushButton boldButton;
	public final QAction	boldAction;
	public final QPushButton underlineButton;
	public final QAction	underlineAction;
	public final QPushButton italicButton;
	public final QAction	italicAction;
	public final Signal0 focusLost;
	public final NoteResourceSignal resourceSignal;

	public QPushButton rightAlignButton;
	public final QAction	rightAlignAction;
	public QPushButton leftAlignButton;
	public final QAction	leftAlignAction;
	public QPushButton centerAlignButton;
	public final QAction	centerAlignAction;

	public final QPushButton strikethroughButton;
	public final QAction	strikethroughAction;
	public final QPushButton hlineButton;
	public final QAction	hlineAction;
	public final QPushButton indentButton;
	public final QAction	indentAction;
	public final QPushButton outdentButton;
	public final QAction	outdentAction;
	public final QPushButton bulletListButton;
	public final QAction	bulletListAction;
	public final QPushButton numberListButton;
	public final QAction	numberListAction;
	public final QPushButton spellCheckButton;
	public final QAction	spellCheckAction;
	public final QPushButton todoButton;
	public final QAction	todoAction;

	public final QShortcut focusTitleShortcut;
	public final QShortcut focusTagShortcut;
	public final QShortcut focusNoteShortcut;
	public final QShortcut focusUrlShortcut;
	public final QShortcut focusAuthorShortcut;
	
	public EditorButtonBar buttonLayout;
	public final QComboBox fontList;
	public final QAction	fontListAction;
	public final QToolButton fontColor;
	public final QAction	fontColorAction;
	private final ColorMenu fontColorMenu;
	public final QToolButton fontHilight;
	public final QAction	fontHilightAction;
	private final ColorMenu fontHilightColorMenu;
	public final QFileSystemWatcher fileWatcher;
	public int cursorPosition;
	private boolean forceTextPaste;
	private String selectedFile;
	private String currentHyperlink;
	public boolean keepPDFNavigationHidden;
	private final ApplicationLogger logger;
	SpellDictionary dictionary;
    SpellDictionary userDictionary;
    SpellChecker spellChecker;
    SuggestionListener spellListener;
	private final HashMap<String,Integer> previewPageList;  
	boolean insertHyperlink;
	boolean insideTable;
	boolean insideEncryption;
	public Signal1<BrowserWindow> blockApplication;
	public Signal0 unblockApplication;
	public boolean awaitingHttpResponse;
	public long	unblockTime;
	String latexGuid;  // This is set if we are editing an existing LaTeX formula.  Useful to track guid.

	
	public static class SuggestionListener implements SpellCheckListener {
		public boolean abortSpellCheck = false;
		public boolean errorsFound = false;
		private final SpellCheck		spellCheckDialog;
		
		
		private final BrowserWindow parent;
		public SuggestionListener(BrowserWindow parent, SpellChecker checker) {
			this.parent = parent;
			spellCheckDialog = new SpellCheck(checker);
		}
		public void spellingError(SpellCheckEvent event) {
			errorsFound = true;
			spellCheckDialog.setWord(event.getInvalidWord());

		    @SuppressWarnings("unchecked")
			List<Word> suggestions = event.getSuggestions();
		    spellCheckDialog.clearSuggestions();
		    if (!suggestions.isEmpty()) {
//		       spellCheckDialog.setCurrentSuggestion(suggestions.get(0).getWord());
		       for (int i=0; i<suggestions.size(); i++) {
		          spellCheckDialog.addSuggestion(suggestions.get(i).getWord());
		       }
		       spellCheckDialog.setSelectedSuggestion(0);
		    }
		    spellCheckDialog.exec();
		    if (spellCheckDialog.cancelPressed()) {
		    	abortSpellCheck = true;
		    	event.cancel();
		    	return;
		    }
		    if (spellCheckDialog.replacePressed()) {
		    	QClipboard clipboard = QApplication.clipboard();
		    	clipboard.setText(spellCheckDialog.getReplacementWord()); 
		    	parent.pasteClicked();
		    }
		    event.cancel();
		 }
	}

	
	
	public BrowserWindow(DatabaseConnection c) {
		logger = new ApplicationLogger("browser.log");
		logger.log(logger.HIGH, "Setting up browser");
		iconPath = new String("classpath:cx/fbn/nevernote/icons/");
		forceTextPaste = false;
		insertHyperlink = true;
		insideTable = false;
		insideEncryption = false;
		
		fileWatcher = new QFileSystemWatcher();
//		fileWatcher.fileChanged.connect(this, "fileChanged(String)");
		noteSignal = new NoteSignal();
		titleLabel = new QLineEdit();
		evernoteLinkClicked = new Signal2<String,String>();
		titleLabel.setMaxLength(Constants.EDAM_NOTE_TITLE_LEN_MAX);
		urlText = new QLineEdit();
		authorText = new QLineEdit();
		geoBox = new QComboBox();
		urlLabel = new QPushButton();
		urlLabel.clicked.connect(this, "sourceUrlClicked()");
		authorLabel = new QLabel();
		conn = c;
		
		focusLost = new Signal0();

		tagEdit = new TagLineEdit(allTags);
		tagLabel = new QLabel(tr("Tags:"));
		tagEdit.focusLost.connect(this, "modifyTagsTyping()");

		createdCalendarWidget = new QCalendarWidget();
		createdDate = new QDateEdit();
		createdDate.setDisplayFormat(Global.getDateFormat());
		createdDate.setCalendarPopup(true);
		createdDate.setCalendarWidget(createdCalendarWidget);
		createdTime = new QTimeEdit();
		createdDate.dateChanged.connect(this, "createdChanged()");
		createdTime.timeChanged.connect(this, "createdChanged()");

		alteredCalendarWidget = new QCalendarWidget();
		alteredDate = new QDateEdit();
		alteredDate.setDisplayFormat(Global.getDateFormat());
		alteredDate.setCalendarPopup(true);
		alteredDate.setCalendarWidget(alteredCalendarWidget);
		alteredTime = new QTimeEdit();
		alteredLabel = new QLabel(tr("Altered:"));
		alteredDate.dateChanged.connect(this, "alteredChanged()");
		alteredTime.timeChanged.connect(this, "alteredChanged()");

		subjectCalendarWidget = new QCalendarWidget();
		subjectDate = new QDateEdit();
		subjectDate.setDisplayFormat(Global.getDateFormat());
		subjectDate.setCalendarPopup(true);
		subjectDate.setCalendarWidget(subjectCalendarWidget);
		subjectTime = new QTimeEdit();
		subjectLabel = new QLabel(tr("Subject Date:"));
		subjectDate.dateChanged.connect(this, "subjectDateTimeChanged()");
		subjectTime.timeChanged.connect(this, "subjectDateTimeChanged()");
		authorText.textChanged.connect(this, "authorChanged()");
		urlText.textChanged.connect(this, "sourceUrlChanged()");

		notebookBox = new QComboBox();
		notebookLabel = new QLabel(tr("Notebook"));
		createdLabel = new QLabel(tr("Created:"));
		// selectedText = new String();

		urlLabel.setVisible(false);
		urlText.setVisible(false);
		authorLabel.setVisible(false);
		
		geoBox.setVisible(false);
		geoBox.addItem(new QIcon(iconPath+"globe.png"), "");
		geoBox.addItem(new String(tr("Set")));
		geoBox.addItem(new String(tr("Clear")));
		geoBox.addItem(new String(tr("View On Map")));
		geoBox.activated.connect(this, "geoBoxChanged()");
		
		authorText.setVisible(false);
		createdDate.setVisible(false);
		alteredLabel.setVisible(false);
		//notebookBox.setVisible(false);
		notebookLabel.setVisible(false);
		createdLabel.setVisible(false);
		createdTime.setVisible(false);
		alteredDate.setVisible(false);
		alteredTime.setVisible(false);
		subjectLabel.setVisible(false);
		subjectDate.setVisible(false);
		subjectTime.setVisible(false);
		extendedOn = false;
		buttonsVisible = true;
		setAcceptDrops(true);

		browser = new ContentView(this);
				
		browser.page().setLinkDelegationPolicy(
				QWebPage.LinkDelegationPolicy.DelegateAllLinks);
		browser.linkClicked.connect(this, "linkClicked(QUrl)");
		currentHyperlink = "";
		
		//Setup the source editor
		sourceEdit = new QTextEdit(this);
		sourceEdit.setVisible(false);
		sourceEdit.setTabChangesFocus(true);
		sourceEdit.setLineWrapMode(LineWrapMode.NoWrap);
		QFont font = new QFont();
		font.setFamily("Courier");
		font.setFixedPitch(true);
		font.setPointSize(10);
		sourceEdit.setFont(font);
		syntaxHighlighter = new Highlighter(sourceEdit.document());
		sourceEdit.textChanged.connect(this, "sourceEdited()");

		QVBoxLayout v = new QVBoxLayout();
		QFormLayout notebookLayout = new QFormLayout();
		QGridLayout dateLayout = new QGridLayout();
		titleLabel.setReadOnly(false);
		titleLabel.editingFinished.connect(this, "titleEdited()");
		browser.page().contentsChanged.connect(this, "contentChanged()");
		browser.page().selectionChanged.connect(this, "selectionChanged()");
		browser.page().mainFrame().javaScriptWindowObjectCleared.connect(this,
				"exposeToJavascript()");

		notebookBox.activated.connect(this, "notebookChanged()");
		resourceSignal = new NoteResourceSignal();
		
		QHBoxLayout tagLayout = new QHBoxLayout();
		v.addWidget(titleLabel, 0);
		notebookLayout.addRow(notebookLabel, notebookBox);
		tagLayout.addLayout(notebookLayout, 0);
		tagLayout.stretch(4);
		tagLayout.addWidget(tagLabel, 0);
		tagLayout.addWidget(tagEdit, 1);
		v.addLayout(tagLayout);

		QHBoxLayout urlLayout = new QHBoxLayout();
		urlLayout.addWidget(urlLabel, 0);
		urlLayout.addWidget(urlText, 0);
		v.addLayout(urlLayout);

		QHBoxLayout authorLayout = new QHBoxLayout();
		authorLayout.addWidget(authorLabel, 0);
		authorLayout.addWidget(authorText, 0);
		authorLayout.addWidget(geoBox);
		v.addLayout(authorLayout);

		dateLayout.addWidget(createdLabel, 0, 0);
		dateLayout.addWidget(createdDate, 0, 1);
		dateLayout.addWidget(createdTime, 0, 2);
		dateLayout.setColumnStretch(9, 100);
		dateLayout.addWidget(alteredLabel, 0, 3);
		dateLayout.addWidget(alteredDate, 0, 4);
		dateLayout.addWidget(alteredTime, 0, 5);
		dateLayout.addWidget(subjectLabel, 0, 6);
		dateLayout.addWidget(subjectDate, 0, 7);
		dateLayout.addWidget(subjectTime, 0, 8);
		v.addLayout(dateLayout, 0);

		undoButton = newEditorButton("undo", tr("Undo Change"));
		redoButton = newEditorButton("redo", tr("Redo Change"));
		cutButton = newEditorButton("cut", tr("Cut"));
		copyButton = newEditorButton("copy", tr("Copy"));
		pasteButton = newEditorButton("paste", tr("Paste"));
		boldButton = newEditorButton("bold", tr("Bold"));
		underlineButton = newEditorButton("underline", tr("Underline"));
		italicButton = newEditorButton("italic", tr("Italic"));

		rightAlignButton = newEditorButton("justifyRight", tr("Right Align"));
		leftAlignButton = newEditorButton("justifyLeft", tr("Left Align"));
		centerAlignButton = newEditorButton("justifyCenter", tr("Center Align"));

		strikethroughButton = newEditorButton("strikethrough", tr("Strikethrough"));
		hlineButton = newEditorButton("hline", tr("Insert Horizontal Line"));
		indentButton = newEditorButton("indent", tr("Shift Right"));
		outdentButton = newEditorButton("outdent", tr("Shift Left"));
		bulletListButton = newEditorButton("bulletList", tr("Bullet List"));
		numberListButton = newEditorButton("numberList", tr("Number List"));
		spellCheckButton = newEditorButton("spellCheck", tr("Spell Check"));
		todoButton = newEditorButton("todo", tr("To-do"));

		
		buttonLayout = new EditorButtonBar();
		v.addWidget(buttonLayout);
		
		undoAction = buttonLayout.addWidget(undoButton);
		buttonLayout.toggleUndoVisible.triggered.connect(this, "toggleUndoVisible(Boolean)");
		redoAction = buttonLayout.addWidget(redoButton);
		buttonLayout.toggleRedoVisible.triggered.connect(this, "toggleRedoVisible(Boolean)");
		
		buttonLayout.addWidget(newSeparator());
		cutAction = buttonLayout.addWidget(cutButton);
		buttonLayout.toggleCutVisible.triggered.connect(this, "toggleCutVisible(Boolean)");
		copyAction = buttonLayout.addWidget(copyButton);
		buttonLayout.toggleCopyVisible.triggered.connect(this, "toggleCopyVisible(Boolean)");
		pasteAction = buttonLayout.addWidget(pasteButton);
		buttonLayout.togglePasteVisible.triggered.connect(this, "togglePasteVisible(Boolean)");

		buttonLayout.addWidget(newSeparator());
		boldAction = buttonLayout.addWidget(boldButton);
		buttonLayout.toggleBoldVisible.triggered.connect(this, "toggleBoldVisible(Boolean)");
		italicAction = buttonLayout.addWidget(italicButton);
		buttonLayout.toggleItalicVisible.triggered.connect(this, "toggleItalicVisible(Boolean)");
		underlineAction = buttonLayout.addWidget(underlineButton);
		buttonLayout.toggleUnderlineVisible.triggered.connect(this, "toggleUnderlineVisible(Boolean)");
		strikethroughAction = buttonLayout.addWidget(strikethroughButton);
		buttonLayout.toggleStrikethroughVisible.triggered.connect(this, "toggleStrikethroughVisible(Boolean)");

		
		buttonLayout.addWidget(newSeparator());
		leftAlignAction = buttonLayout.addWidget(leftAlignButton);
		buttonLayout.toggleLeftAlignVisible.triggered.connect(this, "toggleLeftAlignVisible(Boolean)");
		centerAlignAction = buttonLayout.addWidget(centerAlignButton);
		buttonLayout.toggleCenterAlignVisible.triggered.connect(this, "toggleCenterAlignVisible(Boolean)");
		rightAlignAction = buttonLayout.addWidget(rightAlignButton);
		buttonLayout.toggleRightAlignVisible.triggered.connect(this, "toggleRightAlignVisible(Boolean)");

		buttonLayout.addWidget(newSeparator());
		hlineAction = buttonLayout.addWidget(hlineButton);
		buttonLayout.toggleHLineVisible.triggered.connect(this, "toggleHLineVisible(Boolean)");

		indentAction = buttonLayout.addWidget(indentButton);
		buttonLayout.toggleIndentVisible.triggered.connect(this, "toggleIndentVisible(Boolean)");
		outdentAction = buttonLayout.addWidget(outdentButton);
		buttonLayout.toggleOutdentVisible.triggered.connect(this, "toggleOutdentVisible(Boolean)");
		bulletListAction = buttonLayout.addWidget(bulletListButton);
		buttonLayout.toggleBulletListVisible.triggered.connect(this, "toggleBulletListVisible(Boolean)");
		numberListAction = buttonLayout.addWidget(numberListButton);
		buttonLayout.toggleNumberListVisible.triggered.connect(this, "toggleNumberListVisible(Boolean)");

		// Setup the font & font size combo boxes
		buttonLayout.addWidget(newSeparator());
		fontList = new QComboBox();
		fontSize = new QComboBox();
		fontList.setToolTip("Font");
		fontSize.setToolTip("Font Size");
		fontList.activated.connect(this, "fontChanged(String)");
		fontSize.activated.connect(this, "fontSizeChanged(String)");
		fontListAction = buttonLayout.addWidget(fontList);
		buttonLayout.toggleFontVisible.triggered.connect(this, "toggleFontListVisible(Boolean)");
		fontSizeAction = buttonLayout.addWidget(fontSize);
		buttonLayout.toggleFontSizeVisible.triggered.connect(this, "toggleFontSizeVisible(Boolean)");
		QFontDatabase fonts = new QFontDatabase();
		List<String> fontFamilies = fonts.families();
		for (int i = 0; i < fontFamilies.size(); i++) {
			fontList.addItem(fontFamilies.get(i));
			if (i == 0) {
				loadFontSize(fontFamilies.get(i));
			}
		}

//		buttonLayout.addWidget(newSeparator(), 0);
		fontColor = newToolButton("fontColor", tr("Font Color"));
		fontColorMenu = new ColorMenu(this);
		fontColor.setMenu(fontColorMenu.getMenu());
		fontColor.setPopupMode(ToolButtonPopupMode.MenuButtonPopup);
		fontColor.setAutoRaise(false);
		fontColorMenu.getMenu().triggered.connect(this, "fontColorClicked()");
		fontColorAction = buttonLayout.addWidget(fontColor);
		buttonLayout.toggleFontColorVisible.triggered.connect(this, "toggleFontColorVisible(Boolean)");
		fontHilight = newToolButton("fontHilight", tr("Font Hilight Color"));
		fontHilight.setPopupMode(ToolButtonPopupMode.MenuButtonPopup);
		fontHilight.setAutoRaise(false);
		fontHilightColorMenu = new ColorMenu(this);
		fontHilightColorMenu.setDefault(QColor.yellow);
		fontHilight.setMenu(fontHilightColorMenu.getMenu());
		fontHilightColorMenu.getMenu().triggered.connect(this, "fontHilightClicked()");
		fontHilightAction = buttonLayout.addWidget(fontHilight);
		fontHilightColorMenu.setDefault(QColor.yellow);
		buttonLayout.toggleFontHilight.triggered.connect(this, "toggleFontHilightVisible(Boolean)");
		
		spellCheckAction = buttonLayout.addWidget(spellCheckButton);
		buttonLayout.toggleNumberListVisible.triggered.connect(this, "spellCheckClicked()");
		buttonLayout.toggleSpellCheck.triggered.connect(this, "toggleSpellCheckVisible(Boolean)");
		
		todoAction = buttonLayout.addWidget(todoButton);
		buttonLayout.toggleNumberListVisible.triggered.connect(this, "todoClicked()");
		buttonLayout.toggleTodo.triggered.connect(this, "toggleTodoVisible(Boolean)");

		// Setup the source browser);

//		buttonLayout.addWidget(new QLabel(), 1);
		QSplitter editSplitter = new QSplitter(this);
		editSplitter.addWidget(browser);
		editSplitter.setOrientation(Qt.Orientation.Vertical);
		editSplitter.addWidget(sourceEdit);

		

//		v.addWidget(browser, 1);
//		v.addWidget(sourceEdit);
		v.addWidget(editSplitter);
		setLayout(v);

		browser.downloadAttachmentRequested.connect(this,
				"downloadAttachment(QNetworkRequest)");
		browser.downloadImageRequested.connect(this,
				"downloadImage(QNetworkRequest)");
		setTabOrder(notebookBox, tagEdit);
		setTabOrder(tagEdit, browser);
		
		focusNoteShortcut = new QShortcut(this);
		setupShortcut(focusNoteShortcut, "Focus_Note");
		focusNoteShortcut.activated.connect(this, "focusNote()");
		focusTitleShortcut = new QShortcut(this);
		setupShortcut(focusTitleShortcut, "Focus_Title");
		focusTitleShortcut.activated.connect(this, "focusTitle()");
		focusTagShortcut = new QShortcut(this);
		setupShortcut(focusTagShortcut, "Focus_Tag");
		focusTagShortcut.activated.connect(this, "focusTag()");
		focusAuthorShortcut = new QShortcut(this);
		setupShortcut(focusAuthorShortcut, "Focus_Author");
		focusAuthorShortcut.activated.connect(this, "focusAuthor()");
		focusUrlShortcut = new QShortcut(this);
		setupShortcut(focusUrlShortcut, "Focus_Url");
		focusUrlShortcut.activated.connect(this, "focusUrl()");
		
		browser.page().mainFrame().setTextSizeMultiplier(Global.getTextSizeMultiplier());
		browser.page().mainFrame().setZoomFactor(Global.getZoomFactor());
		
		previewPageList = new HashMap<String,Integer>();
		
		browser.page().microFocusChanged.connect(this, "microFocusChanged()");
		
		//Setup colors
		
		QPalette pal = new QPalette();
		pal.setColor(ColorRole.Text, QColor.black);
		titleLabel.setPalette(pal);
		authorText.setPalette(pal);
		authorLabel.setPalette(pal);
		urlLabel.setPalette(pal);
		urlText.setPalette(pal);
		createdDate.setPalette(pal);
		createdTime.setPalette(pal);
		alteredDate.setPalette(pal);
		alteredTime.setPalette(pal);
		subjectDate.setPalette(pal);
		subjectTime.setPalette(pal);
		tagEdit.setPalette(pal);
		notebookBox.setPalette(pal);
		
		blockApplication = new Signal1<BrowserWindow>();
		unblockApplication = new Signal0();
		
		logger.log(logger.HIGH, "Browser setup complete");
	}

	
	
	private void setupShortcut(QShortcut action, String text) {
		if (!Global.shortcutKeys.containsAction(text))
			return;
		action.setKey(new QKeySequence(Global.shortcutKeys.getShortcut(text)));
	}
	
	

	
	// Getter for the QWebView
	public QWebView getBrowser() {
		return browser;
	}

	// Block signals while loading data or things are flagged as dirty by
	// mistake
	public void loadingData(boolean val) {
		logger.log(logger.EXTREME, "Entering BrowserWindow.loadingData() " +val);
		notebookBox.blockSignals(val);
		browser.page().blockSignals(val);
		browser.page().mainFrame().blockSignals(val);
		titleLabel.blockSignals(val);
		alteredDate.blockSignals(val);
		alteredTime.blockSignals(val);
		createdTime.blockSignals(val);
		createdDate.blockSignals(val);
		subjectDate.blockSignals(val);
		subjectTime.blockSignals(val);
		urlText.blockSignals(val);
		authorText.blockSignals(val);
		if (!val)
			exposeToJavascript();
		logger.log(logger.EXTREME, "Exiting BrowserWindow.loadingData() " +val);
	}

	// Enable/disable
	public void setReadOnly(boolean v) {
		setEnabled(true);
		titleLabel.setEnabled(!v);
		notebookBox.setEnabled(!v);
		tagEdit.setEnabled(!v);
		authorLabel.setEnabled(!v);
		geoBox.setEnabled(!v);
		urlText.setEnabled(!v);
		createdDate.setEnabled(!v);
		subjectDate.setEnabled(!v);
		alteredDate.setEnabled(!v);
		authorText.setEnabled(!v);
		createdTime.setEnabled(!v);
		alteredTime.setEnabled(!v);
		subjectTime.setEnabled(!v);
		getBrowser().setEnabled(true);
//		getBrowser().setEnabled(!v);
	}
	
	// expose this class to Javascript on the web page
	private void exposeToJavascript() {
		browser.page().mainFrame().addToJavaScriptWindowObject("jambi", this);
	}

	// Custom event queue
	@Override
	public boolean event(QEvent e) {
		if (e.type().equals(QEvent.Type.FocusOut)) {
			logger.log(logger.EXTREME, "Focus lost");
			focusLost.emit();
		}
		return super.event(e);
	}

	// clear out browser
	public void clear() {
		logger.log(logger.EXTREME, "Entering BrowserWindow.clear()");
		setNote(null);
		setContent(new QByteArray());
		tagEdit.setText("");
		tagEdit.tagCompleter.reset();
		urlLabel.setText(tr("Source URL:"));
		titleLabel.setText("");
		logger.log(logger.EXTREME, "Exiting BrowserWindow.clear()");
	}

	public void setContent(QByteArray data) {
		sourceEdit.blockSignals(true);
		browser.setContent(data);
		setSource(getBrowser().page().mainFrame().toHtml());
	}
	// get/set current note
	public void setNote(Note n) {
		currentNote = n;
		if (n == null)
			n = new Note();
		saveNoteTitle = n.getTitle();

	}

	public Note getNote() {
		return currentNote;
	}

	// New Editor Button
	private QPushButton newEditorButton(String name, String toolTip) {
		QPushButton button = new QPushButton();
//		QIcon icon = new QIcon(iconPath + name + ".gif");
		QIcon icon = new QIcon(iconPath + name + ".png");
		button.setIcon(icon);
		button.setToolTip(toolTip);
		button.clicked.connect(this, name + "Clicked()");
		return button;
	}
	// New Editor Button
	private QToolButton newToolButton(String name, String toolTip) {
		QToolButton button = new QToolButton();
//		QIcon icon = new QIcon(iconPath + name + ".gif");
		QIcon icon = new QIcon(iconPath + name + ".png");
		button.setIcon(icon);
		button.setToolTip(toolTip);
		button.clicked.connect(this, name + "Clicked()");
		return button;
	}

	// New Separator
	private QLabel newSeparator() {
		return new QLabel("   ");
	}

	// Set the title in the window
	public void setTitle(String t) {
		titleLabel.setText(t);
		saveNoteTitle = t;
		checkNoteTitle();
	}

	// Return the current text title
	public String getTitle() {
		return titleLabel.text();
	}

	// Set the tag name string
	public void setTag(String t) {
		saveTagList = t;
		tagEdit.setText(t);
		tagEdit.tagCompleter.reset();
	}

	// Set the source URL
	public void setUrl(String t) {
		urlLabel.setText(tr("Source URL:\t"));
		urlText.setText(t);
	}

	// The user want's to launch a web browser on the source of the URL
	public void sourceUrlClicked() {
		// Make sure we have a valid URL
		if (urlText.text().trim().equals(""))
			return;
		
		String url = urlText.text();
		if (!url.toLowerCase().startsWith(tr("http://")))
			url = tr("http://") +url;
		
        if (!QDesktopServices.openUrl(new QUrl(url))) {
        	logger.log(logger.LOW, "Error opening file :" +url);
        }
	}
	
	public void setAuthor(String t) {
		authorLabel.setText(tr("Author:\t"));
		authorText.setText(t);
	}

	// Set the creation date
	public void setCreation(long date) {
		QDateTime dt = new QDateTime();
		dt.setTime_t((int) (date / 1000));
		createdDate.setDateTime(dt);
		createdTime.setDateTime(dt);
		createdDate.setDisplayFormat(Global.getDateFormat());
		createdTime.setDisplayFormat(Global.getTimeFormat());
	}

	// Set the creation date
	public void setAltered(long date) {
		QDateTime dt = new QDateTime();
		dt.setTime_t((int) (date / 1000));
		alteredDate.setDateTime(dt);
		alteredTime.setDateTime(dt);
		alteredDate.setDisplayFormat(Global.getDateFormat());
		alteredTime.setDisplayFormat(Global.getTimeFormat());
	}

	// Set the subject date
	public void setSubjectDate(long date) {
		QDateTime dt = new QDateTime();
		dt.setTime_t((int) (date / 1000));
		subjectDate.setDateTime(dt);
		subjectTime.setDateTime(dt);
		subjectDate.setDisplayFormat(Global.getDateFormat());
		subjectTime.setDisplayFormat(Global.getTimeFormat());
	}

	// Toggle the extended attribute information
	public void toggleInformation() {
		if (extendedOn) {
			extendedOn = false;
		} else {
			extendedOn = true;
		}
		urlLabel.setVisible(extendedOn);
		urlText.setVisible(extendedOn);
		authorText.setVisible(extendedOn);
		geoBox.setVisible(extendedOn);
		authorLabel.setVisible(extendedOn);
		createdDate.setVisible(extendedOn);
		createdTime.setVisible(extendedOn);
		createdLabel.setVisible(extendedOn);
		alteredLabel.setVisible(extendedOn);
		alteredDate.setVisible(extendedOn);
		alteredTime.setVisible(extendedOn);
		//notebookBox.setVisible(extendedOn);
		notebookLabel.setVisible(extendedOn);
		subjectLabel.setVisible(extendedOn);
		subjectDate.setVisible(extendedOn);
		subjectTime.setVisible(extendedOn);
	}

	public void hideButtons() {

		undoButton.parentWidget().setVisible(false);
		buttonsVisible = false;
	}


	// Is the extended view on?
	public boolean isExtended() {
		return extendedOn;
	}

	// Listener for when a link is clicked
	@SuppressWarnings("unused")
	private void openFile() {
		logger.log(logger.EXTREME, "Starting openFile()");
		File fileHandle = new File(selectedFile);
		URI fileURL = fileHandle.toURI();
		String localURL = fileURL.toString();
		QUrl url = new QUrl(localURL);
		QFile file = new QFile(selectedFile);
		
		logger.log(logger.EXTREME, "Adding to fileWatcher:"+file.fileName());
		fileWatcher.addPath(file.fileName());
        
        if (!QDesktopServices.openUrl(url)) {
        	logger.log(logger.LOW, "Error opening file :" +url);
        }
	}
	
	
	// Listener for when a link is clicked
	@SuppressWarnings("unused")
	private void linkClicked(QUrl url) {
		logger.log(logger.EXTREME, "URL Clicked: " +url.toString());
		if (url.toString().startsWith("latex:")) {
			int position = url.toString().lastIndexOf(".");
			String guid = url.toString().substring(0,position);
			position = guid.lastIndexOf("/");
			guid = guid.substring(position+1);
			editLatex(guid);
			return;
		}
		if (url.toString().startsWith("evernote:/view/")) {
			StringTokenizer tokens = new StringTokenizer(url.toString().replace("evernote:/view/", ""), "/");
			tokens.nextToken();
			tokens.nextToken();
			String sid = tokens.nextToken();
			String lid = tokens.nextToken();
			
			// Emit that we want to switch to a new note
			evernoteLinkClicked.emit(sid, lid);

			return;
		}
		if (url.toString().startsWith("nnres://")) {
			logger.log(logger.EXTREME, "URL is NN resource");
			if (url.toString().endsWith("/vnd.evernote.ink")) {
				logger.log(logger.EXTREME, "Unable to open ink note");
				QMessageBox.information(this, tr("Unable Open"), tr("This is an ink note.\n"+
					"Ink notes are not supported since Evernote has not\n published any specifications on them\n" +
					"and I'm too lazy to figure them out by myself."));
				return;
			}
			String fullName = url.toString().substring(8);
			int index = fullName.indexOf(".");
			String guid = "";
			String type = "";
			if (index >-1) {
				type = fullName.substring(index+1);
				guid = fullName.substring(0,index);
			}
			index = guid.indexOf(Global.attachmentNameDelimeter);
			if (index > -1) {
				guid = guid.substring(0,index);
			}
			List<Resource> resList = currentNote.getResources();
			Resource res = null;
			for (int i=0; i<resList.size(); i++) {
				if (resList.get(i).getGuid().equals(guid)) {
					res = resList.get(i);
					i=resList.size();
				}
			}
			if (res == null) {
				String resGuid = Global.resourceMap.get(guid);
				if (resGuid != null) 
					res = conn.getNoteTable().noteResourceTable.getNoteResource(resGuid, true);
			}
			if (res != null) {
				String fileName;
				if (res.getAttributes() != null && 
						res.getAttributes().getFileName() != null && 
						!res.getAttributes().getFileName().trim().equals(""))
					fileName = res.getGuid()+Global.attachmentNameDelimeter+res.getAttributes().getFileName();
				else
					fileName = res.getGuid()+"."+type;
				QFile file = new QFile(Global.getFileManager().getResDirPath(fileName));
		        QFile.OpenMode mode = new QFile.OpenMode();
		        mode.set(QFile.OpenModeFlag.WriteOnly);
		        boolean openResult = file.open(mode);
				logger.log(logger.EXTREME, "File opened:" +openResult);
		        QDataStream out = new QDataStream(file);
		        Resource resBinary = conn.getNoteTable().noteResourceTable.getNoteResource(res.getGuid(), true);
				QByteArray binData = new QByteArray(resBinary.getData().getBody());
				resBinary = null;
				logger.log(logger.EXTREME, "Writing resource");
		        out.writeBytes(binData.toByteArray());
		        file.close();
			        
		        String whichOS = System.getProperty("os.name");
				if (whichOS.contains("Windows")) 
		        	url.setUrl("file:///"+file.fileName());
		        else
		        	url.setUrl("file://"+file.fileName());
		 //       fileWatcher.removePath(file.fileName());
				logger.log(logger.EXTREME, "Adding file watcher " +file.fileName());
				fileWatcher.addPath(file.fileName());
		        
		        // If we can't open it, then prompt the user to save it.
		        if (!QDesktopServices.openUrl(url)) {
					logger.log(logger.EXTREME, "We can't handle this.  Where do we put it?");
		        	QFileDialog dialog = new QFileDialog();
		        	dialog.show();
		        	if (dialog.exec()!=0) {
		        		List<String> fileNames = dialog.selectedFiles(); //gets all selected filenames
		        		if (fileNames.size() == 0) 
		        			return;
		        		String sf = fileNames.get(0);
		        		QFile saveFile = new QFile(sf);
				        mode.set(QFile.OpenModeFlag.WriteOnly);
				        saveFile.open(mode);
				        QDataStream saveOut = new QDataStream(saveFile);
				        saveOut.writeBytes(binData.toByteArray());
				        saveFile.close();
				        return;
		        	}
				}
			}
			return;
		}
		logger.log(logger.EXTREME, "Launching URL");
		QDesktopServices.openUrl(url);
	}

	// Listener for when BOLD is clicked
	@SuppressWarnings("unused")
	private void undoClicked() {
		browser.page().triggerAction(WebAction.Undo);
		browser.setFocus();
	}

	// Listener for when BOLD is clicked
	@SuppressWarnings("unused")
	private void redoClicked() {
		browser.page().triggerAction(WebAction.Redo);
		browser.setFocus();
	}

	// Listener for when BOLD is clicked
	@SuppressWarnings("unused")
	private void boldClicked() {
		browser.page().triggerAction(WebAction.ToggleBold);
		microFocusChanged();
		browser.setFocus();
	}

	// Listener for when Italics is clicked
	@SuppressWarnings("unused")
	private void italicClicked() {
		browser.page().triggerAction(WebAction.ToggleItalic);
		microFocusChanged();
		browser.setFocus();
	}

	// Listener for when UNDERLINE is clicked
	@SuppressWarnings("unused")
	private void underlineClicked() {
		browser.page().triggerAction(WebAction.ToggleUnderline);
		microFocusChanged();
		browser.setFocus();
	}

	// Listener for when Strikethrough is clicked
	@SuppressWarnings("unused")
	private void strikethroughClicked() {
		browser.page().mainFrame().evaluateJavaScript(
				"document.execCommand('strikeThrough', false, '');");
		browser.setFocus();
	}

	// Listener for when cut is clicked
	@SuppressWarnings("unused")
	private void cutClicked() {
		browser.page().triggerAction(WebAction.Cut);
		browser.setFocus();
	}

	// Listener when COPY is clicked
	@SuppressWarnings("unused")
	private void copyClicked() {
		browser.page().triggerAction(WebAction.Copy);
		browser.setFocus();
	}

	// Listener when PASTE is clicked
	public void pasteClicked() {
		logger.log(logger.EXTREME, "Paste Clicked");
		if (forceTextPaste) {
			pasteWithoutFormattingClicked();
			return;
		}
		QClipboard clipboard = QApplication.clipboard();
		QMimeData mime = clipboard.mimeData();
		
//		 String x = mime.html();

		if (mime.hasImage()) {
			logger.log(logger.EXTREME, "Image paste found");
			browser.setFocus();
			insertImage(mime);
			browser.setFocus();
			return;
		}

		if (mime.hasUrls()) {
			logger.log(logger.EXTREME, "URL paste found");
			if (!mime.text().startsWith("evernote:")) {
				handleNoteLink(mime);
			} else {
				handleUrls(mime);
				browser.setFocus();
			}
			return;
		}
		
		String text = mime.html();
		if (text.contains("en-tag") && mime.hasHtml()) {
			logger.log(logger.EXTREME, "Intra-note paste found");
			text = fixInternotePaste(text);
			mime.setHtml(text);
			clipboard.setMimeData(mime);
		}

		logger.log(logger.EXTREME, "Final paste choice encountered");
		browser.page().triggerAction(WebAction.Paste);
		browser.setFocus();

	}

	// Paste text without formatting
	private void pasteWithoutFormattingClicked() {
		logger.log(logger.EXTREME, "Paste without format clipped");
		QClipboard clipboard = QApplication.clipboard();
		QMimeData mime = clipboard.mimeData();
		if (!mime.hasText())
			return;
		String text = mime.text();
		clipboard.clear();
		clipboard.setText(text, Mode.Clipboard);
		browser.page().triggerAction(WebAction.Paste);

		// This is done because pasting into an encryption block
		// can cause multiple cells (which can't happen).  It 
		// just goes through the table, extracts the data, & 
		// puts it back as one table cell.
		if (insideEncryption) {
			String js = new String( "function fixEncryption() { "
					+"   var selObj = window.getSelection();"
					+"   var selRange = selObj.getRangeAt(0);"
					+"   var workingNode = window.getSelection().anchorNode;"
					+"   while(workingNode != null && workingNode.nodeName.toLowerCase() != 'table') { " 
					+"           workingNode = workingNode.parentNode;"
					+"   } "
					+"   workingNode.innerHTML = window.jambi.fixEncryptionPaste(workingNode.innerHTML);"
					+"} fixEncryption();");
			browser.page().mainFrame().evaluateJavaScript(js);
		}
	}
	
	// This basically removes all the table tags and returns just the contents.
	// This is called by JavaScript to fix encryption pastes.
	public String fixEncryptionPaste(String data) {
		data = data.replace("<tbody>", "");
		data = data.replace("</tbody>", "");
		data = data.replace("<tr>", "");
		data = data.replace("</tr>", "");
		data = data.replace("<td>", "");
		data = data.replace("</td>", "<br>");
		data = data.replace("<br><br>", "<br>");

		return "<tbody><tr><td>"+data+"</td></tr></tbody>";
	}
	
	// insert date/time
	@SuppressWarnings("unused")
	private void insertDateTime() {
		String fmt = Global.getDateFormat() + " " + Global.getTimeFormat();
		String dateTimeFormat = new String(fmt);
		SimpleDateFormat simple = new SimpleDateFormat(dateTimeFormat);
		Calendar cal = Calendar.getInstance();
		
		browser.page().mainFrame().evaluateJavaScript(
			"document.execCommand('insertHtml', false, '"+simple.format(cal.getTime())+"');");
		
		browser.setFocus();

	}

	// Listener when Left is clicked
	@SuppressWarnings("unused")
	private void justifyLeftClicked() {
		browser.page().mainFrame().evaluateJavaScript(
				"document.execCommand('JustifyLeft', false, '');");
		browser.setFocus();
	}

	// Listener when Center is clicked
	@SuppressWarnings("unused")
	private void justifyCenterClicked() {
		browser.page().mainFrame().evaluateJavaScript(
				"document.execCommand('JustifyCenter', false, '');");
		browser.setFocus();
	}

	// Listener when Left is clicked
	@SuppressWarnings("unused")
	private void justifyRightClicked() {
		browser.page().mainFrame().evaluateJavaScript(
				"document.execCommand('JustifyRight', false, '');");
		browser.setFocus();
	}

	// Listener when HLINE is clicked
	@SuppressWarnings("unused")
	private void hlineClicked() {
		browser.page().mainFrame().evaluateJavaScript(
				"document.execCommand('insertHorizontalRule', false, '');");
		browser.setFocus();
	}

	// Listener when outdent is clicked
	private void outdentClicked() {
		browser.page().mainFrame().evaluateJavaScript(
				"document.execCommand('outdent', false, '');");
		browser.setFocus();
	}

	// Listener when a bullet list is clicked
	@SuppressWarnings("unused")
	private void bulletListClicked() {
		browser.page().mainFrame().evaluateJavaScript(
				"document.execCommand('InsertUnorderedList', false, '');");
		browser.setFocus();
	}

	// Listener when a bullet list is clicked
	@SuppressWarnings("unused")
	private void numberListClicked() {
		browser.page().mainFrame().evaluateJavaScript(
				"document.execCommand('InsertOrderedList', false, '');");
		browser.setFocus();
	}

	// Listener when indent is clicked
	private void indentClicked() {
		browser.page().mainFrame().evaluateJavaScript(
				"document.execCommand('indent', false, '');");
		browser.setFocus();
	}

	// Listener when the font name is changed
	@SuppressWarnings("unused")
	private void fontChanged(String font) {
		browser.page().mainFrame().evaluateJavaScript(
				"document.execCommand('fontName',false,'" + font + "');");
		browser.setFocus();
	}

	// Listener when a font size is changed
	@SuppressWarnings("unused")
	private void fontSizeChanged(String font) {
		String text = browser.selectedText();
		if (text.trim().equalsIgnoreCase(""))
			return;

		String selectedText = browser.selectedText();
		String url = "<span style=\"font-size:" +font +"pt; \">"+selectedText +"</a>";
		String script = "document.execCommand('insertHtml', false, '"+url+"');";
		browser.page().mainFrame().evaluateJavaScript(script);
/*		browser.page().mainFrame().evaluateJavaScript(
				"document.execCommand('fontSize',false,'"
						+ font + "');");
*/
		browser.setFocus();
	}

	// Load the font combo box based upon the font selected
	private void loadFontSize(String name) {	
		QFontDatabase db = new QFontDatabase(); 
		fontSize.clear();
		List<Integer> points = db.pointSizes(name); 
		for (int i=0; i<points.size(); i++) { 
			fontSize.addItem(points.get(i).toString()); 
		}
		/*
		fontSize.addItem("x-small");
		fontSize.addItem("small");
		fontSize.addItem("medium");
		fontSize.addItem("large");
		fontSize.addItem("x-large");
		fontSize.addItem("xx-large");
		fontSize.addItem("xxx-large");
		*/
	}

	// Listener when a font size is changed
	@SuppressWarnings("unused")
	private void fontColorClicked() {
//		QColorDialog dialog = new QColorDialog();
//		QColor color = QColorDialog.getColor();
		QColor color = fontColorMenu.getColor();
		if (color.isValid())
			browser.page().mainFrame().evaluateJavaScript(
					"document.execCommand('foreColor',false,'" + color.name()
							+ "');");
		browser.setFocus();
	}

	// Listener for when a background color change is requested
	@SuppressWarnings("unused")
	private void fontHilightClicked() {
//		QColorDialog dialog = new QColorDialog();
//		QColor color = QColorDialog.getColor();
		QColor color = fontHilightColorMenu.getColor();
		if (color.isValid())
			browser.page().mainFrame().evaluateJavaScript(
					"document.execCommand('backColor',false,'" + color.name()
							+ "');");
		browser.setFocus();
	}
	
	// Listener for when a background color change is requested
	@SuppressWarnings("unused")
	private void superscriptClicked() {
		browser.page().mainFrame().evaluateJavaScript(
					"document.execCommand('superscript');");
		browser.setFocus();
	}
	
	// Listener for when a background color change is requested
	@SuppressWarnings("unused")
	private void subscriptClicked() {
		browser.page().mainFrame().evaluateJavaScript(
					"document.execCommand('subscript');");
		browser.setFocus();
	}
	// Insert a to-do checkbox
	@SuppressWarnings("unused")
	private void todoClicked() {
		FileNameMap fileNameMap = URLConnection.getFileNameMap();
		String script_start = new String(
				"document.execCommand('insertHtml', false, '");
		String script_end = new String("');");
		String todo = new String(
				"<input TYPE=\"CHECKBOX\" value=\"false\" " +
				"onMouseOver=\"style.cursor=\\'hand\\'\" " +
				"onClick=\"value=checked; window.jambi.contentChanged(); \" />");
		browser.page().mainFrame().evaluateJavaScript(
				script_start + todo + script_end);
		browser.setFocus();
	}

	// Encrypt the selected text
	@SuppressWarnings("unused")
	private void encryptText() {
		String text = browser.selectedText();
		if (text.trim().equalsIgnoreCase(""))
			return;
		text = new String(text.replaceAll("\n", "<br/>"));

		EnCryptDialog dialog = new EnCryptDialog();
		dialog.exec();
		if (!dialog.okPressed()) {
			return;
		}

		EnCrypt crypt = new EnCrypt();
		String encrypted = crypt.encrypt(text, dialog.getPassword().trim(), 64);
		String decrypted = crypt.decrypt(encrypted, dialog.getPassword().trim(), 64);

		if (encrypted.trim().equals("")) {
			QMessageBox.information(this, tr("Error"), tr("Error Encrypting String"));
			return;
		}
		StringBuffer buffer = new StringBuffer(encrypted.length() + 100);
		buffer.append("<img en-tag=\"en-crypt\" cipher=\"RC2\" hint=\""
				+ dialog.getHint().replace("'","\\'") + "\" length=\"64\" ");
		buffer.append("contentEditable=\"false\" alt=\"");
		buffer.append(encrypted);
		buffer.append("\" src=\"").append(FileUtils.toForwardSlashedPath(Global.getFileManager().getImageDirPath("encrypt.png") +"\""));
		Global.cryptCounter++;
		buffer.append(" id=\"crypt"+Global.cryptCounter.toString() +"\"");
		buffer.append(" onMouseOver=\"style.cursor=\\'hand\\'\"");
		buffer.append(" onClick=\"window.jambi.decryptText(\\'crypt"+Global.cryptCounter.toString() 
				+"\\', \\'"+encrypted+"\\', \\'"+dialog.getHint().replace("'", "\\&amp;apos;")+"\\');\"");
		buffer.append("style=\"display:block\" />");

		String script_start = new String(
				"document.execCommand('insertHtml', false, '");
		String script_end = new String("');");
		browser.page().mainFrame().evaluateJavaScript(
				script_start + buffer.toString() + script_end);
	}

	
	// Insert a hyperlink
	public void insertLink() {
		logger.log(logger.EXTREME, "Inserting link");
		String text = browser.selectedText();
		if (text.trim().equalsIgnoreCase(""))
			return;

		InsertLinkDialog dialog = new InsertLinkDialog(insertHyperlink);
		if (currentHyperlink != null && currentHyperlink != "") {
			dialog.setUrl(currentHyperlink);
		}
		dialog.exec();
		if (!dialog.okPressed()) {
			logger.log(logger.EXTREME, "Insert link canceled");
			return;
		}
		
		// Take care of inserting new links
		if (insertHyperlink) {
			String selectedText = browser.selectedText();
			if (dialog.getUrl().trim().equals(""))
				return;
			logger.log(logger.EXTREME, "Inserting link on text "+selectedText);
			logger.log(logger.EXTREME, "URL Link " +dialog.getUrl().trim());
			String dUrl = StringUtils.replace(dialog.getUrl().trim(), "'", "\\'");
			String url = "<a href=\"" +dUrl
					+"\" title=" +dUrl 
					+" >"+selectedText +"</a>";
			String script = "document.execCommand('insertHtml', false, '"+url+"');";
			browser.page().mainFrame().evaluateJavaScript(script);
			return;
		}
		
		// Edit existing links
		String js = new String( "function getCursorPos() {"
				+"var cursorPos;"
				+"if (window.getSelection) {"
				+"   var selObj = window.getSelection();"
				+"   var selRange = selObj.getRangeAt(0);"
				+"   var workingNode = window.getSelection().anchorNode.parentNode;"
				+"   while(workingNode != null) { " 
				+"      if (workingNode.nodeName.toLowerCase()=='a') workingNode.setAttribute('href','" +dialog.getUrl() +"');"
				+"      workingNode = workingNode.parentNode;"
				+"   }"
				+"}"
				+"} getCursorPos();");
			browser.page().mainFrame().evaluateJavaScript(js);
		
		if (!dialog.getUrl().trim().equals("")) {
			contentChanged();
			return;
		}
		
		// Remove URL
		js = new String( "function getCursorPos() {"
				+"var cursorPos;"
				+"if (window.getSelection) {"
				+"   var selObj = window.getSelection();"
				+"   var selRange = selObj.getRangeAt(0);"
				+"   var workingNode = window.getSelection().anchorNode.parentNode;"
				+"   while(workingNode != null) { " 
				+"      if (workingNode.nodeName.toLowerCase()=='a') { "
				+"         workingNode.removeAttribute('href');"
				+"         workingNode.removeAttribute('title');"
				+"         var text = document.createTextNode(workingNode.innerText);"
				+"         workingNode.parentNode.insertBefore(text, workingNode);"
				+"         workingNode.parentNode.removeChild(workingNode);"
				+"      }"
				+"      workingNode = workingNode.parentNode;"
				+"   }"
				+"}"
				+"} getCursorPos();");
			browser.page().mainFrame().evaluateJavaScript(js);
			
			contentChanged();

		
	}
	
	
	// Insert a hyperlink
	public void insertLatex() {
		editLatex(null);
	}
	public void editLatex(String guid) {
		logger.log(logger.EXTREME, "Inserting latex");
		String text = browser.selectedText();
		if (text.trim().equalsIgnoreCase(" ") || text.trim().equalsIgnoreCase("")) {
			InsertLatexImage dialog = new InsertLatexImage();
			if (guid != null) {
				String formula = conn.getNoteTable().noteResourceTable.getNoteSourceUrl(guid).replace("http://latex.codecogs.com/gif.latex?", "");
				dialog.setFormula(formula);
			}
			dialog.exec();
			if (!dialog.okPressed()) {
				logger.log(logger.EXTREME, "Edit LaTex canceled");
				return;
			}
			text = dialog.getFormula().trim();
		}
		blockApplication.emit(this);
		logger.log(logger.EXTREME, "Inserting LaTeX formula:" +text);
		latexGuid = guid;
		text = StringUtils.replace(text, "'", "\\'");
		String url = "http://latex.codecogs.com/gif.latex?" +text;
		logger.log(logger.EXTREME, "Sending request to codecogs --> " + url);
		QNetworkAccessManager manager = new QNetworkAccessManager(this);
		manager.finished.connect(this, "insertLatexImageReady(QNetworkReply)");
		unblockTime = new GregorianCalendar().getTimeInMillis()+5000;
		awaitingHttpResponse = true;
		manager.get(new QNetworkRequest(new QUrl(url)));
	}
	
	public void insertLatexImageReady(QNetworkReply reply) {
		logger.log(logger.EXTREME, "Response received from CodeCogs");
		if (reply.error() != NetworkError.NoError) 
			return;

		unblockTime = -1;
		if (!awaitingHttpResponse)
			return;
		
		awaitingHttpResponse = false;
		QUrl replyUrl = reply.url();		
		QByteArray image = reply.readAll();
		reply.close();
		logger.log(logger.EXTREME, "New image size: " +image.size());

		Resource newRes = null;
		QFile tfile;
		String path;
		if (latexGuid == null) {
			logger.log(logger.EXTREME, "Creating temporary gif");			
			path = Global.getFileManager().getResDirPath("latex-temp.gif");
			tfile = new QFile(path);
			tfile.open(new QIODevice.OpenMode(QIODevice.OpenModeFlag.WriteOnly));
			logger.log(logger.EXTREME, "File Open: " +tfile.errorString());
			tfile.write(image);
			logger.log(logger.EXTREME, "Bytes writtes: "+tfile.size());
			tfile.close();
			logger.log(logger.EXTREME, "Creating resource");
			int sequence = 0;
			if (currentNote.getResources() != null || currentNote.getResources().size() > 0)
				sequence = currentNote.getResources().size();
			newRes = createResource(path,sequence ,"image/gif", false);
			QImage pix = new QImage();
			pix.loadFromData(image);
			newRes.setHeight(new Integer(pix.height()).shortValue());
			newRes.setWidth(new Integer(pix.width()).shortValue());
			logger.log(logger.EXTREME, "Renaming temporary file to " +newRes.getGuid()+".gif");
			path = Global.getFileManager().getResDirPath(newRes.getGuid()+".gif");
			tfile.rename(path);
		} else {
			newRes = conn.getNoteTable().noteResourceTable.getNoteResource(latexGuid, false);
			path = Global.getFileManager().getResDirPath(newRes.getGuid()+".gif");
			tfile = new QFile(path);
			tfile.open(new QIODevice.OpenMode(QIODevice.OpenModeFlag.WriteOnly));
			tfile.write(image);
			tfile.close();
			newRes.getData().setBody(image.toByteArray());
			// Calculate the new hash value
	    	MessageDigest md;

	    	logger.log(logger.EXTREME, "Generating MD5");
	    	try {
				md = MessageDigest.getInstance("MD5");
		    	md.update(image.toByteArray());
		    	byte[] hash = md.digest();
		    	newRes.getData().setBodyHash(hash);
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
			}
			QImage pix = new QImage();
			pix.loadFromData(image);
			newRes.setHeight(new Integer(pix.height()).shortValue());
			newRes.setWidth(new Integer(pix.width()).shortValue());
			conn.getNoteTable().noteResourceTable.updateNoteResource(newRes, true);
		}

		logger.log(logger.EXTREME, "Setting source: " +replyUrl.toString());
		newRes.getAttributes().setSourceURL(replyUrl.toString());
		conn.getNoteTable().noteResourceTable.updateNoteSourceUrl(newRes.getGuid(), replyUrl.toString(), true);
		
		for(int i=0; i<currentNote.getResourcesSize(); i++) {
			if (currentNote.getResources().get(i).getGuid().equals(newRes.getGuid())) {
				currentNote.getResources().remove(i);
				i=currentNote.getResourcesSize();
			}
		}
		currentNote.getResources().add(newRes);
		

		// do the actual insert into the note.  We only do this on new formulas.  
		if (latexGuid == null) {
			StringBuffer buffer = new StringBuffer(100);
			String formula = replyUrl.toString().toLowerCase().replace("http://latex.codecogs.com/gif.latex?", "");
			buffer.append("<a href=\"latex://"+path.replace("\\", "/")+"\" title=\""+formula+"\""
					+"><img src=\"");
			buffer.append(path.replace("\\", "/"));
			buffer.append("\" en-tag=\"en-latex\" type=\"image/gif\""
				+" hash=\""+Global.byteArrayToHexString(newRes.getData().getBodyHash()) +"\""
				+" guid=\"" +newRes.getGuid() +"\""
				+ " /></a>");
		
			String script_start = new String("document.execCommand('insertHTML', false, '");
			String script_end = new String("');");
			browser.page().mainFrame().evaluateJavaScript(
					script_start + buffer + script_end);
		} else {
			HtmlTagModifier modifier = new HtmlTagModifier(getContent());
			modifier.modifyLatexTagHash(newRes);
			String newContent = modifier.getHtml();
			setContent(new QByteArray(newContent));
		}

		logger.log(logger.EXTREME, "New HTML set\n" +browser.page().currentFrame().toHtml());
		QWebSettings.setMaximumPagesInCache(0);
		QWebSettings.setObjectCacheCapacities(0, 0, 0);
		
		browser.page().mainFrame().setHtml(browser.page().mainFrame().toHtml());
		browser.reload();
		contentChanged();
//		resourceSignal.contentChanged.emit(path);
		unblockTime = -1;
    	unblockApplication.emit();
		return;
		
	}

	
	
	// Insert a table
	public void insertTable() {
		TableDialog dialog = new TableDialog();
		dialog.exec();
		if (!dialog.okPressed()) {
			return;
		}
		
		int cols = dialog.getCols();
		int rows = dialog.getRows();
		int width = dialog.getWidth();
		boolean percent = dialog.isPercent();
		
		String newHTML = "<table border=\"1\" width=\"" +new Integer(width).toString();
		if (percent)
			newHTML = newHTML +"%";
		newHTML = newHTML + "\"><tbody>";

		for (int i=0; i<rows; i++) {
			newHTML = newHTML +"<tr>";
			for (int j=0; j<cols; j++) {
				newHTML = newHTML +"<td>&nbsp;</td>";
			}
			newHTML = newHTML +"</tr>";
		}
		newHTML = newHTML+"</tbody></table>";	
	
		String script = "document.execCommand('insertHtml', false, '"+newHTML+"');";
		browser.page().mainFrame().evaluateJavaScript(script);
	}
	
	
	// Text content changed
	@SuppressWarnings("unused")
	private void selectionChanged() {
		browser.encryptAction.setEnabled(true);
		browser.insertLinkAction.setEnabled(true);
		String scriptStart = "var selection_text = (window.getSelection()).toString();"
				+ "var range = (window.getSelection()).getRangeAt(0);"
				+ "var parent_html = range.commonAncestorContainer.innerHTML;"
				+ "if (parent_html == undefined) {window.jambi.saveSelectedText(selection_text); return;}"
				+ "var first_text = range.startContainer.nodeValue.substr(range.startOffset);"
				+ "var last_text = (range.endContainer.nodeValue).substring(0,range.endOffset);"
				+ "var start = parent_html.indexOf(first_text);"
				+ "var end = parent_html.indexOf(last_text,start+1)+last_text.length;"
				+ "var value = parent_html.substring(start,end);"
				+ "window.jambi.saveSelectedText(value);" ;
		browser.page().mainFrame().evaluateJavaScript(scriptStart);

	}

	public void saveSelectedText(String text) {
		boolean enabled = true;
		if (text.trim().length() == 0)
			enabled=false;
		if (text.indexOf("en-tag=\"en-crypt\"") >= 0)
			enabled=false;
		if (text.indexOf("<img en-tag=\"en-media\"") >= 0)
			enabled=false;
		if (text.indexOf("<a en-tag=\"en-media\"") >= 0)
			enabled=false;
		if (text.indexOf("<input ") >= 0)
			enabled=false;
		
		browser.encryptAction.setEnabled(enabled);
		browser.insertLinkAction.setEnabled(enabled);
//		selectedText = text;
	}

	// Decrypt clicked text
	public void decryptText(String id, String text, String hint) {
		EnCrypt crypt = new EnCrypt();
		String plainText = null;
		Calendar currentTime = new GregorianCalendar();
		Long l = new Long(currentTime.getTimeInMillis());
		String slot = new String(Long.toString(l));
		
		// First, try to decrypt with any keys we already have
		for (int i=0; i<Global.passwordRemember.size(); i++) {
			plainText = crypt.decrypt(text, Global.passwordRemember.get(i).getFirst(), 64);
			if (plainText != null) {
				slot = new String(Long.toString(l));
				Global.passwordSafe.put(slot, Global.passwordRemember.get(i));
				removeEncryption(id, plainText, false, slot);	
				return;
			}
		}
		
		
		EnDecryptDialog dialog = new EnDecryptDialog();
		dialog.setHint(hint);
		while (plainText == null || !dialog.okPressed()) {
			dialog.exec();
			if (!dialog.okPressed()) {
				return;
			}
			plainText = crypt.decrypt(text, dialog.getPassword().trim(), 64);
			if (plainText == null) {
				QMessageBox.warning(this, tr("Incorrect Password"), tr("The password entered is not correct"));
			}
		}
		Pair<String,String> passwordPair = new Pair<String,String>();
		passwordPair.setFirst(dialog.getPassword());
		passwordPair.setSecond(dialog.getHint());
		Global.passwordSafe.put(slot, passwordPair);
//		removeEncryption(id, plainText.replaceAll("\n", "<br/>"), dialog.permanentlyDecrypt(), slot);
		removeEncryption(id, plainText, dialog.permanentlyDecrypt(), slot);
		if (dialog.rememberPassword()) {
			Pair<String, String> pair = new Pair<String,String>();
			pair.setFirst(dialog.getPassword());
			pair.setSecond(dialog.getHint());
			Global.passwordRemember.add(pair);
		}

	}

	// Get the editor tag line
	public TagLineEdit getTagLine() {
		return tagEdit;
	}

	// Modify a note's tags
	@SuppressWarnings("unused")
	private void modifyTags() {
		TagAssign tagWindow = new TagAssign(allTags, currentTags, !conn.getNotebookTable().isLinked(currentNote.getNotebookGuid()));
		tagWindow.exec();
		if (tagWindow.okClicked()) {
			currentTags.clear();
			StringBuffer tagDisplay = new StringBuffer();

			List<QListWidgetItem> newTags = tagWindow.getTagList()
					.selectedItems();
			for (int i = 0; i < newTags.size(); i++) {
				currentTags.add(newTags.get(i).text());
				tagDisplay.append(newTags.get(i).text());
				if (i < newTags.size() - 1) {
					tagDisplay.append(Global.tagDelimeter + " ");
				}
			}
			tagEdit.setText(tagDisplay.toString());
			noteSignal.tagsChanged.emit(currentNote.getGuid(), currentTags);
		}
	}

	// Tag line has been modified by typing text
	@SuppressWarnings("unused")
	private void modifyTagsTyping() {
		String completionText = "";
		if (tagEdit.currentCompleterSelection != null && !tagEdit.currentCompleterSelection.equals("")) {
			completionText = tagEdit.currentCompleterSelection;
			tagEdit.currentCompleterSelection = "";
		}
		
		if (tagEdit.text().equalsIgnoreCase(saveTagList))
			return;

		// We know something has changed...
		String oldTagArray[] = saveTagList.split(Global.tagDelimeter);
		String newTagArray[];
		if (!completionText.equals("")) {
			String before = tagEdit.text().substring(0,tagEdit.cursorPosition());
			int lastDelimiter = before.lastIndexOf(Global.tagDelimeter);
			if (lastDelimiter > 0)
				before = before.substring(0,before.lastIndexOf(Global.tagDelimeter));
			else 
				before = "";
			String after = tagEdit.text().substring(tagEdit.cursorPosition());
			newTagArray = (before+Global.tagDelimeter+completionText+Global.tagDelimeter+after).split(Global.tagDelimeter);
		}
		else {
			newTagArray = tagEdit.text().split(Global.tagDelimeter);
		}
		
		// Remove any traling or leading blanks
		for (int i=0; i<newTagArray.length; i++)
			newTagArray[i] = newTagArray[i].trim().replaceAll("^\\s+", "");;
		
		// Remove any potential duplicates from the new list
		for (int i=0; i<newTagArray.length; i++) {
			boolean foundOnce = false;
			for (int j=0; j<newTagArray.length; j++) {
				if (newTagArray[j].equalsIgnoreCase(newTagArray[i])) {
					if (!foundOnce) {
						foundOnce = true;
					} else
						newTagArray[j] = "";
				}
			}
		}

		List<String> newTagList = new ArrayList<String>();
		List<String> oldTagList = new ArrayList<String>();

		for (int i = 0; i < oldTagArray.length; i++)
			if (!oldTagArray[i].trim().equals(""))
				oldTagList.add(oldTagArray[i]);
		for (int i = 0; i < newTagArray.length; i++)
			if (!newTagArray[i].trim().equals(""))
				newTagList.add(newTagArray[i]);

		if (conn.getNotebookTable().isLinked(currentNote.getNotebookGuid())) {
			for (int i=newTagList.size()-1; i>=0; i--) {
				boolean found = false;
				for (int j=0; j<allTags.size(); j++) {
					if (allTags.get(j).getName().equalsIgnoreCase(newTagList.get(i))) {
						found = true;
						j=allTags.size();
					}
				}
				if (!found)
					newTagList.remove(i);
			}
		}

		// Let's cleanup the appearance of the tag list
		Collections.sort(newTagList);
		String newDisplay = "";
		for (int i=0; i<newTagList.size(); i++) {
			newDisplay = newDisplay+newTagList.get(i);
			if (i<newTagList.size()-1)
				newDisplay = newDisplay+Global.tagDelimeter +" ";
		}
		tagEdit.blockSignals(true);
		tagEdit.setText(newDisplay);
		tagEdit.blockSignals(false);
		
		// We now have lists of the new & old. Remove duplicates. If all
		// are removed from both then nothing has really changed
		for (int i = newTagList.size() - 1; i >= 0; i--) {
			String nTag = newTagList.get(i);
			for (int j = oldTagList.size() - 1; j >= 0; j--) {
				String oTag = oldTagList.get(j);
				if (oTag.equalsIgnoreCase(nTag)) {
					oldTagList.remove(j);
					newTagList.remove(i);
					j = -1;
				}
			}
		}

		if (oldTagList.size() != 0 || newTagList.size() != 0) {
			currentTags.clear();
			newTagArray = tagEdit.text().split(Global.tagDelimeter);
			for (int i = 0; i < newTagArray.length; i++)
				if (!newTagArray[i].trim().equals(""))
					currentTags.add(newTagArray[i].trim());

			noteSignal.tagsChanged.emit(currentNote.getGuid(), currentTags);
		}
		
	}

	// Tab button was pressed
	public void tabPressed() {
		if (insideEncryption)
			return;
		if (!insideList && !insideTable) {
			String script_start = new String(
			"document.execCommand('insertHtml', false, '&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;');");
			browser.page().mainFrame().evaluateJavaScript(script_start);
			return;
		}
		if (insideList) {
			indentClicked();
		}
		if (insideTable) {
			String js = new String( "function getCursorPosition() { "
					+"   var selObj = window.getSelection();"
					+"   var selRange = selObj.getRangeAt(0);"
					+"   var workingNode = window.getSelection().anchorNode;"
					+"   var rowCount = 0;"
					+"   var colCount = 0;"
					+"   while(workingNode != null && workingNode.nodeName.toLowerCase() != 'table') { " 
					+"      if (workingNode.nodeName.toLowerCase()=='tr') {"
					+"         rowCount = rowCount+1;"
					+"      }"
					+"      if (workingNode.nodeName.toLowerCase() == 'td') {"
					+"         colCount = colCount+1;"
					+"      }"
					+"      if (workingNode.previousSibling != null)"
					+"          workingNode = workingNode.previousSibling;"
					+"      else "
					+"           workingNode = workingNode.parentNode;"
					+"   }"
					+"   var nodes = workingNode.getElementsByTagName('tr');"
					+"   var tableRows = nodes.length;"
					+"   nodes = nodes[0].getElementsByTagName('td');"
					+"   var tableColumns = nodes.length;"
					+"   window.jambi.setTableCursorPositionTab(rowCount, colCount, tableRows, tableColumns);"
					+"} getCursorPosition();");
			browser.page().mainFrame().evaluateJavaScript(js);
		}
	}
	
	// If a user presses tab from within a table
	public void setTableCursorPositionTab(int currentRow, int currentCol, int tableRows, int tableColumns) {
		if (tableRows == currentRow && currentCol == tableColumns) {
			insertTableRow();
		}
		KeyboardModifiers modifiers = new KeyboardModifiers(KeyboardModifier.NoModifier);
		QKeyEvent right = new QKeyEvent(Type.KeyPress, Qt.Key.Key_Right.value(), modifiers);
		QKeyEvent end = new QKeyEvent(Type.KeyPress, Qt.Key.Key_End.value(), modifiers);
		QKeyEvent end2 = new QKeyEvent(Type.KeyPress, Qt.Key.Key_End.value(), modifiers);
		getBrowser().focusWidget();
		QCoreApplication.postEvent(getBrowser(), end);
		QCoreApplication.postEvent(getBrowser(), right);
		QCoreApplication.postEvent(getBrowser(), end2);
	}
		
	public void backtabPressed() {
		if (insideEncryption) 
			return;
		if (insideList)
			outdentClicked();
		if (insideTable) {
			String js = new String( "function getCursorPosition() { "
					+"   var selObj = window.getSelection();"
					+"   var selRange = selObj.getRangeAt(0);"
					+"   var workingNode = window.getSelection().anchorNode;"
					+"   var rowCount = 0;"
					+"   var colCount = 0;"
					+"   while(workingNode != null && workingNode.nodeName.toLowerCase() != 'table') { " 
					+"      if (workingNode.nodeName.toLowerCase()=='tr') {"
					+"         rowCount = rowCount+1;"
					+"      }"
					+"      if (workingNode.nodeName.toLowerCase() == 'td') {"
					+"         colCount = colCount+1;"
					+"      }"
					+"      if (workingNode.previousSibling != null)"
					+"          workingNode = workingNode.previousSibling;"
					+"      else "
					+"           workingNode = workingNode.parentNode;"
					+"   }"
					+"   var nodes = workingNode.getElementsByTagName('tr');"
					+"   var tableRows = nodes.length;"
					+"   nodes = nodes[0].getElementsByTagName('td');"
					+"   var tableColumns = nodes.length;"
					+"   window.jambi.setTableCursorPositionBackTab(rowCount, colCount, tableRows, tableColumns);"
					+"} getCursorPosition();");
			browser.page().mainFrame().evaluateJavaScript(js);
			
		}
	}
	
	// If a user presses backtab from within a table
	public void setTableCursorPositionBackTab(int currentRow, int currentCol, int tableRows, int tableColumns) {
		if (currentRow  == 1 && currentCol == 1) {
			return;
		}
		KeyboardModifiers modifiers = new KeyboardModifiers(KeyboardModifier.NoModifier);
		QKeyEvent left = new QKeyEvent(Type.KeyPress, Qt.Key.Key_Left.value(), modifiers);
		QKeyEvent home = new QKeyEvent(Type.KeyPress, Qt.Key.Key_Home.value(), modifiers);
		getBrowser().focusWidget();
		QCoreApplication.postEvent(getBrowser(), home);
		QCoreApplication.postEvent(getBrowser(), left);
	}
	
	
	public void setInsideList() {
		insideList = true;
	}
	
	// The title has been edited
	@SuppressWarnings("unused")
	private void titleEdited() {
		// If we don't have a good note, or if the current title
		// matches the old title then we don't need to do anything
		if (currentNote == null)
			return;
		if (currentNote.getTitle().trim().equals(titleLabel.text().trim()))
			return;
		
		// If we have a real change, we need to save it.
		noteSignal.titleChanged.emit(currentNote.getGuid(), titleLabel.text());
		currentNote.setTitle(titleLabel.text());
		saveNoteTitle = titleLabel.text();
		checkNoteTitle();
	}

	// Set the list of note tags
	public void setAllTags(List<Tag> l) {
		allTags = l;
		tagEdit.setTagList(l);
	}

	// Setter for the current tags
	public void setCurrentTags(List<String> s) {
		currentTags = s;
	}

	// Save the list of notebooks
	public void setNotebookList(List<Notebook> n) {
		notebookList = n;
		loadNotebookList();
	}

	// Load the notebook list and select the current notebook
	private void loadNotebookList() {
		if (notebookBox.count() != 0)
			notebookBox.clear();
		if (notebookList == null)
			return;

		for (int i = 0; i < notebookList.size(); i++) {
			notebookBox.addItem(notebookList.get(i).getName());
			if (currentNote != null) {
				if (currentNote.getNotebookGuid().equals(
						notebookList.get(i).getGuid())) {
					notebookBox.setCurrentIndex(i);
				}
			}
		}
	}
	
	
	// Set the notebook for a note
	public void setNotebook(String notebook) {
		currentNote.setNotebookGuid(notebook);
		loadNotebookList();
	}

	// Get the contents of the editor
	public String getContent() {
		return browser.page().currentFrame().toHtml();
	}

	// The note contents have changed
	public void contentChanged() {
		String content = getContent();
		setSource(content);
		
		checkNoteTitle();
		noteSignal.noteChanged.emit(currentNote.getGuid(), content); 
	}

	// The notebook selection has changed
	@SuppressWarnings("unused")
	private void notebookChanged() {
		boolean changed = false;
		String n = notebookBox.currentText();
		for (int i = 0; i < notebookList.size(); i++) {
			if (n.equals(notebookList.get(i).getName())) {
				if (!notebookList.get(i).getGuid().equals(currentNote.getNotebookGuid())) {
					String guid = conn.getNotebookTable().findNotebookByName(n);
					if (conn.getNotebookTable().isLinked(guid)) {
						tagEdit.setText("");
						noteSignal.tagsChanged.emit(currentNote.getGuid(), new ArrayList<String>());
						FilterEditorTags t = new FilterEditorTags(conn, logger);
						setAllTags(t.getValidTags(currentNote));
					}
					currentNote.setNotebookGuid(notebookList.get(i).getGuid());
					changed = true;
				}
				i = notebookList.size();
			}
		}
		
		// If the notebook changed, signal the update
		if (changed)
			noteSignal.notebookChanged.emit(currentNote.getGuid(), currentNote
					.getNotebookGuid());
	}

	// Check the note title
	private void checkNoteTitle() {
		String text = browser.page().currentFrame().toPlainText();
		if (saveNoteTitle.trim().equals("") || saveNoteTitle.trim().equals("Untitled Note")) {
			int newLine = text.indexOf("\n");
			if (newLine > 0) {
				text = text.substring(0, newLine);
				if (text.trim().equals(""))
					text = tr("Untitled Note");
				titleLabel.setText(text);
			} else {
				if (text.length() > Constants.EDAM_NOTE_TITLE_LEN_MAX)
					titleLabel.setText(text.substring(0, Constants.EDAM_NOTE_TITLE_LEN_MAX));
				else {
					titleLabel.blockSignals(true);
					if (text.trim().equals(""))
						titleLabel.setText(tr("Untitled Note"));
					else
						titleLabel.setText(text);
					titleLabel.blockSignals(false);
				}
			}
			noteSignal.titleChanged.emit(currentNote.getGuid(), titleLabel
					.text());
		}
	}

	// Return the note contents so we can email them
	public String getContentsToEmail() {
		return browser.page().currentFrame().toPlainText().trim();
		/*
		 * int body = browser.page().currentFrame().toHtml().indexOf("<body>");
		 * String temp = browser.page().currentFrame().toHtml(); if (body == -1)
		 * temp = "<html><body><b>Test</b></body></html>"; else temp =
		 * "<html>"+temp.substring(body); return temp; // return
		 * urlEncode(browser.page().currentFrame().toHtml());
		 */
	}

	// Insert an image into the editor
	private void insertImage(QMimeData mime) {
		logger.log(logger.EXTREME, "Entering insertImage");
		QImage img = (QImage) mime.imageData();
		String script_start = new String(
				"document.execCommand('insertHTML', false, '");
		String script_end = new String("');");

		long now = new Date().getTime();
		String path = Global.getFileManager().getResDirPath(
				(new Long(now).toString()) + ".jpg");

		// This block is just a hack to make sure we wait at least 1ms so we
		// don't
		// have collisions on image names
		long i = new Date().getTime();
		while (now == i)
			i = new Date().getTime();

		// Open the file & write the data
		QFile tfile = new QFile(path);
		tfile.open(new QIODevice.OpenMode(QIODevice.OpenModeFlag.WriteOnly));
		if (!img.save(tfile)) {
			tfile.close();
			return;
		}
		tfile.close();
		
		Resource newRes = createResource(QUrl.fromLocalFile(path).toString(), 0, "image/jpeg", false);
		if (newRes == null)
			return;
		currentNote.getResources().add(newRes);

		// do the actual insert into the note
		StringBuffer buffer = new StringBuffer(100);
		buffer.append("<img src=\"");
		buffer.append(tfile.fileName());
		buffer.append("\" en-tag=en-media type=\"image/jpeg\""
				+" hash=\""+Global.byteArrayToHexString(newRes.getData().getBodyHash()) +"\""
				+" guid=\"" +newRes.getGuid() +"\""
				+" onContextMenu=\"window.jambi.imageContextMenu(&amp." +tfile.fileName() +"&amp.);\""
				+ " />");
		
		browser.page().mainFrame().evaluateJavaScript(
				script_start + buffer + script_end);

		return;
	}

	// Handle pasting of a note-to-note link
	private void handleNoteLink(QMimeData mime) {
		for (int i=0; i<mime.urls().size(); i++) {
			StringTokenizer tokens = new StringTokenizer(mime.urls().get(i).toString().replace("evernote:///view/", ""), "/");
			tokens.nextToken();
			tokens.nextToken();
			String sid = tokens.nextToken();
			String lid = tokens.nextToken();
			
			if (!sid.equals(currentNote.getGuid()) && !lid.equals(currentNote.getGuid())) {
				
				Note note = conn.getNoteTable().getNote(sid, false, false, false, false, false);
				if (note == null)
					note = conn.getNoteTable().getNote(lid, false, false, false, false, false);
		
				if (note == null)
					return;

				// If we've gotten this far, we have a bunch of values.  We need to build the link.
				StringBuffer url = new StringBuffer(100);
				String script_start = new String(
					"document.execCommand('insertHtml', false, '");
				String script_end = new String("');");
	
				url.append("<a href=\""+mime.urls().get(i).toString() +"\" style=\"color:#69aa35\">");
				url.append(note.getTitle());
				url.append("</a>");
				if (mime.urls().size() > 1)
					url.append("&nbsp;");
				browser.page().mainFrame().evaluateJavaScript(
						script_start + url + script_end);
			}
		}
	}
	
	// Handle URLs that are trying to be pasted
	public void handleUrls(QMimeData mime) {
		logger.log(logger.EXTREME, "Starting handleUrls");
		FileNameMap fileNameMap = URLConnection.getFileNameMap();

		List<QUrl> urlList = mime.urls();
		String url = new String();
		String script_start = new String(
				"document.execCommand('createLink', false, '");
		String script_end = new String("');");

		for (int i = 0; i < urlList.size(); i++) {
			url = urlList.get(i).toString();
			// Find out what type of file we have
			String mimeType = fileNameMap.getContentTypeFor(url);

			// If null returned, we need to guess at the file type
			if (mimeType == null)
				mimeType = "application/"
						+ url.substring(url.lastIndexOf(".") + 1);

			// Check if we have an image or some other type of file
			if (url.substring(0, 5).equalsIgnoreCase("file:")
					&& mimeType.substring(0, 5).equalsIgnoreCase("image")) {
				handleLocalImageURLPaste(mime, mimeType);
				return;
			}
			String[] type = mimeType.split("/");
			boolean valid = validAttachment(type[1]);
			boolean smallEnough = checkFileAttachmentSize(url);
			if (smallEnough && valid
					&& url.substring(0, 5).equalsIgnoreCase("file:")
					&& !mimeType.substring(0, 5).equalsIgnoreCase("image")) {
				handleLocalAttachment(mime, mimeType);
				return;
			}
			browser.page().mainFrame().evaluateJavaScript(
					script_start + url + script_end);
		}
		return;
	}

	// If a URL being pasted is an image URL, then attach the image
	private void handleLocalImageURLPaste(QMimeData mime, String mimeType) {
		List<QUrl> urlList = mime.urls();
		String url = new String();
		String script_start_image = new String(
				"document.execCommand('insertHtml', false, '");
		String script_end = new String("');");
		StringBuffer buffer;

		// Copy the image over into the resource directory and create a new resource 
		// record for each url pasted
		for (int i = 0; i < urlList.size(); i++) {
			url = urlList.get(i).toString();

			Resource newRes = createResource(url, i, mimeType, false);
			if (newRes == null)
				return;
			currentNote.getResources().add(newRes);
			buffer = new StringBuffer(100);
			
			// Open the file & write the data
			String fileName = Global.getFileManager().getResDirPath(newRes.getGuid());
			QFile tfile = new QFile(fileName);
			tfile.open(new QIODevice.OpenMode(QIODevice.OpenModeFlag.WriteOnly));
			tfile.write(newRes.getData().getBody());
			tfile.close();
			buffer.append(script_start_image);
			buffer.append("<img src=\"" + FileUtils.toForwardSlashedPath(fileName));
//			if (mimeType.equalsIgnoreCase("image/jpg"))
//				mimeType = "image/jpeg";
			buffer.append("\" en-tag=\"en-media\" type=\"" + mimeType +"\""
					+" hash=\""+Global.byteArrayToHexString(newRes.getData().getBodyHash()) +"\""
					+" guid=\"" +newRes.getGuid() +"\""
					+" onContextMenu=\"window.jambi.imageContextMenu(&apos;" +tfile.fileName() +"&apos;);\""
					+ " />");
			buffer.append(script_end);
			browser.page().mainFrame().evaluateJavaScript(buffer.toString());
		}
		return;
	}
	

	// If a URL being pasted is a local file URL, then attach the file
	private void handleLocalAttachment(QMimeData mime, String mimeType) {
		logger.log(logger.EXTREME, "Attaching local file");
		List<QUrl> urlList = mime.urls();
		String script_start = new String(
				"document.execCommand('insertHtml', false, '");
		String script_end = new String("');");
		StringBuffer buffer;

			String[] type = mimeType.split("/");
			String icon = findIcon(type[1]);
			if (icon.equals("attachment.png"))
				icon = findIcon(type[0]);
			buffer = new StringBuffer(100);

		for (int i = 0; i < urlList.size(); i++) {
			String url = urlList.get(i).toString();

			// Start building the HTML
			if (icon.equals("attachment.png"))
				icon = findIcon(url.substring(url.lastIndexOf(".")+1));
			String imageURL = FileUtils.toFileURLString(Global.getFileManager().getImageDirFile(icon));

			logger.log(logger.EXTREME, "Creating resource ");
			Resource newRes = createResource(url, i, mimeType, true);
			if (newRes == null)
				return;
			logger.log(logger.EXTREME, "New resource size: " +newRes.getData().getSize());
			currentNote.getResources().add(newRes);
			
			String fileName = newRes.getGuid() + Global.attachmentNameDelimeter+newRes.getAttributes().getFileName();
			// If we have a PDF, we need to setup the preview.
			if (icon.equalsIgnoreCase("pdf.png") && Global.pdfPreview()) {
				logger.log(logger.EXTREME, "Setting up PDF preview");
				if (newRes.getAttributes() != null && 
						newRes.getAttributes().getFileName() != null && 
						!newRes.getAttributes().getFileName().trim().equals(""))
					fileName = newRes.getGuid()+Global.attachmentNameDelimeter+
						newRes.getAttributes().getFileName();
				else
					fileName = newRes.getGuid()+".pdf";
				QFile file = new QFile(Global.getFileManager().getResDirPath(fileName));
		        QFile.OpenMode mode = new QFile.OpenMode();
		        mode.set(QFile.OpenModeFlag.WriteOnly);
		        file.open(mode);
		        QDataStream out = new QDataStream(file);
//		        Resource resBinary = conn.getNoteTable().noteResourceTable.getNoteResource(newRes.getGuid(), true);
				QByteArray binData = new QByteArray(newRes.getData().getBody());
//				resBinary = null;
		        out.writeBytes(binData.toByteArray());
		        file.close();

				PDFPreview pdfPreview = new PDFPreview();
				if (pdfPreview.setupPreview(Global.getFileManager().getResDirPath(fileName), "pdf",0)) {
			        imageURL = file.fileName() + ".png";
				}
			}
						
			logger.log(logger.EXTREME, "Generating link tags");
			buffer.delete(0, buffer.length());
			buffer.append("<a en-tag=\"en-media\" guid=\"" +newRes.getGuid()+"\" ");
			buffer.append(" onContextMenu=\"window.jambi.imageContextMenu(&apos;")
		      .append(Global.getFileManager().getResDirPath(fileName))
		      .append("&apos;);\" ");			buffer.append("type=\"" + mimeType + "\" href=\"nnres://" + fileName +"\" hash=\""+Global.byteArrayToHexString(newRes.getData().getBodyHash()) +"\" >");
			buffer.append("<img src=\"" + imageURL + "\" title=\"" +newRes.getAttributes().getFileName());
			buffer.append("\"></img>");
			buffer.append("</a>");
			browser.page().mainFrame().evaluateJavaScript(
					script_start + buffer.toString() + script_end);
		}
		return;
	}

	private Resource createResource(String url, int sequence, String mime, boolean attachment) {
		logger.log(logger.EXTREME, "Inside create resource");
		QFile resourceFile;
		String urlTest = new QUrl(url).toLocalFile();
		if (!urlTest.equals(""))
			url = urlTest;
		url = url.replace("/", File.separator);
		logger.log(logger.EXTREME, "Reading from file to create resource");
    	resourceFile = new QFile(url); 
    	resourceFile.open(new QIODevice.OpenMode(QIODevice.OpenModeFlag.ReadOnly));
//    	logger.log(logger.EXTREME, "Error opening file "+url.toString()  +": "+resourceFile.errorString());
    	byte[] fileData = resourceFile.readAll().toByteArray();
    	resourceFile.close();
    	if (fileData.length == 0)
    		return null;
    	MessageDigest md;
    	try {
    		logger.log(logger.EXTREME, "Generating MD5");
    		md = MessageDigest.getInstance("MD5");
    		md.update(fileData);
    		byte[] hash = md.digest();
  
    		Resource r = new Resource();
    		Calendar time = new GregorianCalendar();
    		long prevTime = time.getTimeInMillis();
    		while (prevTime == time.getTimeInMillis()) {
    			time = new GregorianCalendar();
    		}
    		r.setGuid(time.getTimeInMillis()+new Integer(sequence).toString());
    		r.setNoteGuid(currentNote.getGuid());
    		r.setMime(mime);
    		r.setActive(true);
    		r.setUpdateSequenceNum(0);
    		r.setWidth((short) 0);
    		r.setHeight((short) 0);
    		r.setDuration((short) 0);
    		  		
    		Data d = new Data();
    		d.setBody(fileData);
    		d.setBodyIsSet(true);
    		d.setBodyHash(hash);
    		d.setBodyHashIsSet(true);
    		r.setData(d);
    		d.setSize(fileData.length);
    		
    		int fileNamePos = url.lastIndexOf(File.separator);
    		if (fileNamePos == -1)
    			fileNamePos = url.lastIndexOf("/");
   			String fileName = url.substring(fileNamePos+1);
    		ResourceAttributes a = new ResourceAttributes();
    		a.setAltitude(0);
    		a.setAltitudeIsSet(false);
    		a.setLongitude(0);
    		a.setLongitudeIsSet(false);
    		a.setLatitude(0);
    		a.setLatitudeIsSet(false);
    		a.setCameraMake("");
    		a.setCameraMakeIsSet(false);
    		a.setCameraModel("");
    		a.setCameraModelIsSet(false);
    		a.setAttachment(attachment);
    		a.setAttachmentIsSet(true);
    		a.setClientWillIndex(false);
    		a.setClientWillIndexIsSet(true);
    		a.setRecoType("");
    		a.setRecoTypeIsSet(false);
    		a.setSourceURL(url);
    		a.setSourceURLIsSet(true);
    		a.setTimestamp(0);
    		a.setTimestampIsSet(false);
    		a.setFileName(fileName);
    		a.setFileNameIsSet(true);
    		r.setAttributes(a);
    		
    		conn.getNoteTable().noteResourceTable.saveNoteResource(r, true);
    		logger.log(logger.EXTREME, "Resource created");
    		return r;
    	} catch (NoSuchAlgorithmException e1) {
    		e1.printStackTrace();
		}
    	return null;
	}
	

    // find the appropriate icon for an attachment
    private String findIcon(String appl) {
    	appl = appl.toLowerCase();
        File f = Global.getFileManager().getImageDirFile(appl + ".png");
    	if (f.exists())
    		return appl+".png";
    	return "attachment.png";
    }

	// Check if the account supports this type of attachment
	private boolean validAttachment(String type) {
		if (Global.isPremium())
			return true;
		if (type.equalsIgnoreCase("JPG"))
			return true;
		if (type.equalsIgnoreCase("PNG"))
			return true;
		if (type.equalsIgnoreCase("GIF"))
			return true;
		if (type.equalsIgnoreCase("MP3"))
			return true;
		if (type.equalsIgnoreCase("WAV"))
			return true;
		if (type.equalsIgnoreCase("AMR"))
			return true;
		if (type.equalsIgnoreCase("PDF"))
			return true;
		String error = tr("Non-premium accounts can only attach JPG, PNG, GIF, MP3, WAV, AMR, or PDF files.");
		QMessageBox.information(this, tr("Non-Premium Account"), error);

		return false;
	}

	// Check the file attachment to be sure it isn't over 25 mb
	private boolean checkFileAttachmentSize(String url) {
		String fileName = url.substring(8);
		QFile resourceFile = new QFile(fileName);
		resourceFile.open(new QIODevice.OpenMode(
				QIODevice.OpenModeFlag.ReadOnly));
		long size = resourceFile.size();
		resourceFile.close();
		size = size / 1024 / 1024;
		if (size < 50 && Global.isPremium())
			return true;
		if (size < 25)
			return true;

		String error = tr("A file attachment may not exceed 25MB.");
		QMessageBox.information(this, tr("Attachment Size"), error);
		return false;
	}


	@SuppressWarnings("unused")
	private void createdChanged() {
		QDateTime dt = new QDateTime();
		dt.setDate(createdDate.date());
		dt.setTime(createdTime.time());
		noteSignal.createdDateChanged.emit(currentNote.getGuid(), dt);

	}

	@SuppressWarnings("unused")
	private void alteredChanged() {
		QDateTime dt = new QDateTime();
		dt.setDate(alteredDate.date());
		dt.setTime(alteredTime.time());
		noteSignal.alteredDateChanged.emit(currentNote.getGuid(), dt);
	}

	@SuppressWarnings("unused")
	private void subjectDateTimeChanged() {
		QDateTime dt = new QDateTime();
		dt.setDate(subjectDate.date());
		dt.setTime(subjectTime.time());
		noteSignal.subjectDateChanged.emit(currentNote.getGuid(), dt);

	}

	@SuppressWarnings("unused")
	private void sourceUrlChanged() {
		noteSignal.sourceUrlChanged.emit(currentNote.getGuid(), urlText.text());
	}

	@SuppressWarnings("unused")
	private void authorChanged() {
		noteSignal.authorChanged.emit(currentNote.getGuid(), authorText.text());
	}
	
	@SuppressWarnings("unused")
	private void geoBoxChanged() {
		int index = geoBox.currentIndex();
		geoBox.setCurrentIndex(0);
		if (index == 1) {
			GeoDialog box = new GeoDialog();
			box.setLongitude(currentNote.getAttributes().getLongitude());
			box.setLatitude(currentNote.getAttributes().getLatitude());
			box.setAltitude(currentNote.getAttributes().getAltitude());
			box.exec();
			if (!box.okPressed())
				return;
			double alt = box.getAltitude();
			double lat = box.getLatitude();
			double lon = box.getLongitude();
			if (alt != currentNote.getAttributes().getAltitude() ||
				lon != currentNote.getAttributes().getLongitude() ||
				lat != currentNote.getAttributes().getLatitude()) {
					noteSignal.geoChanged.emit(currentNote.getGuid(), lon, lat, alt);
					currentNote.getAttributes().setAltitude(alt);
					currentNote.getAttributes().setLongitude(lon);
					currentNote.getAttributes().setLatitude(lat);
			}
		}
		
		if (index == 2) {
			noteSignal.geoChanged.emit(currentNote.getGuid(), 0.0, 0.0, 0.0);
			currentNote.getAttributes().setAltitude(0.0);
			currentNote.getAttributes().setLongitude(0.0);
			currentNote.getAttributes().setLatitude(0.0);
		}
		
		if (index == 3 || index == 0) {
			QDesktopServices.openUrl(new QUrl("http://maps.google.com/maps?z=6&q="+currentNote.getAttributes().getLatitude() +"," +currentNote.getAttributes().getLongitude()));
		}
	}

	// ************************************************************
	// * User chose to save an attachment. Pares out the request *
	// * into a guid & file. Save the result. *
	// ************************************************************
	public void downloadAttachment(QNetworkRequest request) {
		String guid;
		QFileDialog fd = new QFileDialog(this);
		fd.setFileMode(FileMode.AnyFile);
		fd.setConfirmOverwrite(true);
		fd.setWindowTitle(tr("Save File"));
		fd.setAcceptMode(AcceptMode.AcceptSave);
		fd.setDirectory(System.getProperty("user.home"));
		String name = request.url().toString();

		int pos = name.lastIndexOf(Global.attachmentNameDelimeter);
		if (pos > -1) {
			guid = name.substring(0, pos).replace("nnres://", "");
			name = name.substring(pos +Global.attachmentNameDelimeter.length());
			fd.selectFile(name);
			pos = name.lastIndexOf('.');
			if (pos > -1) {
				String mimeType = "(*." + name.substring(pos + 1)
						+ ");; All Files (*)";
				fd.setFilter(tr(mimeType));
			}
		} else {
			guid = name;
		}

		// Strip URL prefix and base dir
		guid = guid.replace("nnres://", "")
		        .replace(FileUtils.toForwardSlashedPath(Global.getFileManager().getResDirPath()), "");
		guid = guid.replace("file://", "").replace("/", "")
        	.replace(FileUtils.toForwardSlashedPath(Global.getFileManager().getResDirPath()), "");

		pos = guid.lastIndexOf('.');
		if (pos > 0)
			guid = guid.substring(0,pos);
		if (fd.exec() != 0 && fd.selectedFiles().size() > 0) {
			name = name.replace('\\', '/');
			Resource resBinary = conn.getNoteTable().noteResourceTable.getNoteResource(guid, true);
			QFile saveFile = new QFile(fd.selectedFiles().get(0));
			QFile.OpenMode mode = new QFile.OpenMode();
			mode.set(QFile.OpenModeFlag.WriteOnly);
			saveFile.open(mode);
			QDataStream saveOut = new QDataStream(saveFile);
			QByteArray binData = new QByteArray(resBinary.getData().getBody());
			saveOut.writeBytes(binData.toByteArray());
			saveFile.close();

		}
	}

	
	// ************************************************************
	// * User chose to save an attachment. Pares out the request *
	// * into a guid & file. Save the result. --- DONE FROM downloadAttachment now!!!!!   
	// ************************************************************
	public void downloadImage(QNetworkRequest request) {
		QFileDialog fd = new QFileDialog(this);
		fd.setFileMode(FileMode.AnyFile);
		fd.setConfirmOverwrite(true);
		fd.setWindowTitle(tr("Save File"));
		fd.setAcceptMode(AcceptMode.AcceptSave);
		fd.setDirectory(System.getProperty("user.home"));
		String name = request.url().toString();
		name = name.replace("nnres://", "");
		String dPath = FileUtils.toForwardSlashedPath(Global.getFileManager().getResDirPath());
		name = name.replace(dPath, "");
		int pos = name.lastIndexOf('.');
		String guid = name;
		if (pos > -1) {
			String mimeType = "(*." + name.substring(pos + 1)
			+ ");; All Files (*)";
				fd.setFilter(tr(mimeType));
			guid = guid.substring(0,pos);
		}
		pos = name.lastIndexOf(Global.attachmentNameDelimeter);
		if (pos > -1) {
			guid = name.substring(0, pos);
			fd.selectFile(name.substring(pos+Global.attachmentNameDelimeter.length()));		
		}
		if (fd.exec() != 0 && fd.selectedFiles().size() > 0) {
			Resource resBinary = conn.getNoteTable().noteResourceTable.getNoteResource(guid, true);
			String fileName = fd.selectedFiles().get(0);
			QFile saveFile = new QFile(fileName);
			QFile.OpenMode mode = new QFile.OpenMode();
			mode.set(QFile.OpenModeFlag.WriteOnly);
			saveFile.open(mode);
			QDataStream saveOut = new QDataStream(saveFile);
			QByteArray binData = new QByteArray(resBinary.getData().getBody());
			saveOut.writeBytes(binData.toByteArray());
			saveFile.close();
		}
	}

	
	// *************************************************************
	// * decrypt any hidden text.  We could do an XML parse, but 
	// * it is quicker here just to scan for an <img tag & do the fix
	// * the manual way
	// *************************************************************
	private void removeEncryption(String id, String plainText, boolean permanent, String slot) {
		if (!permanent) {
			plainText = " <table class=\"en-crypt-temp\" slot=\""
					+slot 
					+"\""
					+"border=1 width=100%><tbody><tr><td>"
					+plainText+"</td></tr></tbody></table>";
		}
		
		String html = browser.page().mainFrame().toHtml();
		String text = html;
		int imagePos = html.indexOf("<img");
		int endPos;
		for ( ;imagePos>0; ) {
			// Find the end tag
			endPos = text.indexOf(">", imagePos);
			String tag = text.substring(imagePos-1,endPos);
			if (tag.indexOf("id=\""+id+"\"") > -1) {
					text = text.substring(0,imagePos) +plainText+text.substring(endPos+1);	
					QTextCodec codec = QTextCodec.codecForName("UTF-8");
			        QByteArray unicode =  codec.fromUnicode(text);
					setContent(unicode);
					if (permanent)
						contentChanged();
			}
			imagePos = text.indexOf("<img", imagePos+1);
		}
	}
	
	
	//****************************************************************
	//* Focus shortcuts
	//****************************************************************
	@SuppressWarnings("unused")
	private void focusTitle() {
		titleLabel.setFocus();
	}
	@SuppressWarnings("unused")
	private void focusTag() {
		tagEdit.setFocus();
	}
	@SuppressWarnings("unused")
	private void focusNote() {
		browser.setFocus();
	}
	@SuppressWarnings("unused")
	private void focusAuthor() {
		authorLabel.setFocus();
	}
	@SuppressWarnings("unused")
	private void focusUrl() {
		urlLabel.setFocus();
	}
	

	//*****************************************************************
	//* Set the document background color
	//*****************************************************************
	public void setBackgroundColor(String color) {
		String js = "function changeBackground(color) {"
			+"document.body.style.background = color;"
			+"}" 
			+"changeBackground('" +color+"');";
		browser.page().mainFrame().evaluateJavaScript(js);
		contentChanged();
	}
	
	
	//****************************************************************
	//* MicroFocus changed
	//****************************************************************
	private void microFocusChanged() {
		boldButton.setDown(false);
		italicButton.setDown(false);
		underlineButton.setDown(false);
		browser.openAction.setEnabled(false);
		browser.downloadAttachment.setEnabled(false);
		browser.downloadImage.setEnabled(false);
		browser.rotateImageLeft.setEnabled(false);
		browser.rotateImageRight.setEnabled(false);
		browser.insertTableAction.setEnabled(true);
		browser.deleteTableColumnAction.setEnabled(false);
		browser.insertTableRowAction.setEnabled(false);
		browser.insertTableColumnAction.setEnabled(false);
		browser.deleteTableRowAction.setEnabled(false);
		browser.insertLinkAction.setText(tr("Insert Hyperlink"));
		insertHyperlink = true;
		currentHyperlink ="";
		insideList = false;
		insideTable = false;
		insideEncryption = false;
		forceTextPaste = false;
		
		String js = new String( "function getCursorPos() {"
			+"var cursorPos;"
			+"if (window.getSelection) {"
			+"   var selObj = window.getSelection();"
			+"   var selRange = selObj.getRangeAt(0);"
			+"   var workingNode = window.getSelection().anchorNode.parentNode;"
			+"   while(workingNode != null) { " 
//			+"      window.jambi.printNode(workingNode.nodeName);"
			+"      if (workingNode.nodeName=='TABLE') { if (workingNode.getAttribute('class').toLowerCase() == 'en-crypt-temp') window.jambi.insideEncryption(); }"
			+"      if (workingNode.nodeName=='B') window.jambi.boldActive();"
			+"      if (workingNode.nodeName=='I') window.jambi.italicActive();"
			+"      if (workingNode.nodeName=='U') window.jambi.underlineActive();"
			+"      if (workingNode.nodeName=='UL') window.jambi.setInsideList();"
			+"      if (workingNode.nodeName=='OL') window.jambi.setInsideList();"
			+"      if (workingNode.nodeName=='LI') window.jambi.setInsideList();"
			+"      if (workingNode.nodeName=='TBODY') window.jambi.setInsideTable();"
			+"      if (workingNode.nodeName=='A') {for(var x = 0; x < workingNode.attributes.length; x++ ) {if (workingNode.attributes[x].nodeName.toLowerCase() == 'href') window.jambi.setInsideLink(workingNode.attributes[x].nodeValue);}}"
			+"      if (workingNode.nodeName=='SPAN') {"
			+"         if (workingNode.getAttribute('style') == 'text-decoration: underline;') window.jambi.underlineActive();"
			+"      }"
			+"      workingNode = workingNode.parentNode;"
			+"   }"
			+"}"
			+"} getCursorPos();");
		browser.page().mainFrame().evaluateJavaScript(js);
	}
	
	public void printNode(String n) {
		System.out.println("Node Vaule: " +n);
	}
	
	public void insideEncryption() {
		insideEncryption = true;
		forceTextPaste();
	}
	
	//****************************************************************
	//* Insert a table row
	//****************************************************************
	public void insertTableRow() {
		
		String js = new String( "function insertTableRow() {"
			+"   var selObj = window.getSelection();"
			+"   var selRange = selObj.getRangeAt(0);"
			+"   var workingNode = window.getSelection().anchorNode.parentNode;"
			+"   var cellCount = 0;"
			+"   while(workingNode != null) { " 
			+"      if (workingNode.nodeName.toLowerCase()=='tr') {"
			+"           row = document.createElement('TR');"
			+"           var nodes = workingNode.getElementsByTagName('td');"
			+"           for (j=0; j<nodes.length; j=j+1) {"
			+"              cell = document.createElement('TD');"
			+"              cell.innerHTML='&nbsp;';"
			+"              row.appendChild(cell);"
			+"           }"			
			+"           workingNode.parentNode.insertBefore(row,workingNode.nextSibling);"
			+"           return;"
			+"      }"
			+"      workingNode = workingNode.parentNode;"
			+"   }"
			+"} insertTableRow();");
		browser.page().mainFrame().evaluateJavaScript(js);
		contentChanged();
	}
	
	public void insertTableColumn() {
		String js = new String( "function insertTableColumn() {"
				+"   var selObj = window.getSelection();"
				+"   var selRange = selObj.getRangeAt(0);"
				+"   var workingNode = window.getSelection().anchorNode.parentNode;"
				+"   var current = 0;"
				+"   while (workingNode.nodeName.toLowerCase() != 'table' && workingNode != null) {"
				+"       if (workingNode.nodeName.toLowerCase() == 'td') {"
				+"          var td = workingNode;"
				+"          while (td.previousSibling != null) { " 
				+"             current = current+1; td = td.previousSibling;"
				+"          }"
				+"       }"
				+"       workingNode = workingNode.parentNode; "
				+"   }"
				+"   if (workingNode == null) return;"
				+"   for (var i=0; i<workingNode.rows.length; i++) { " 
				+"      var cell = workingNode.rows[i].insertCell(current+1); "			
				+"      cell.innerHTML = '&nbsp'; "
				+"   }"
				+"} insertTableColumn();");
			browser.page().mainFrame().evaluateJavaScript(js);
			contentChanged();
	}
	
	//****************************************************************
	//* Delete a table row
	//****************************************************************
	public void deleteTableRow() {
		
		String js = new String( "function deleteTableRow() {"
			+"   var selObj = window.getSelection();"
			+"   var selRange = selObj.getRangeAt(0);"
			+"   var workingNode = window.getSelection().anchorNode.parentNode;"
			+"   var cellCount = 0;"
			+"   while(workingNode != null) { " 
			+"      if (workingNode.nodeName.toLowerCase()=='tr') {"
			+"           workingNode.parentNode.removeChild(workingNode);"
			+"           return;"
			+"      }"
			+"      workingNode = workingNode.parentNode;"
			+"   }"
			+"} deleteTableRow();");
		browser.page().mainFrame().evaluateJavaScript(js);
		contentChanged();
	}

	public void deleteTableColumn() {
		String js = new String( "function deleteTableColumn() {"
				+"   var selObj = window.getSelection();"
				+"   var selRange = selObj.getRangeAt(0);"
				+"   var workingNode = window.getSelection().anchorNode.parentNode;"
				+"   var current = 0;"
				+"   while (workingNode.nodeName.toLowerCase() != 'table' && workingNode != null) {"
				+"       if (workingNode.nodeName.toLowerCase() == 'td') {"
				+"          var td = workingNode;"
				+"          while (td.previousSibling != null) { " 
				+"             current = current+1; td = td.previousSibling;"
				+"          }"
				+"       }"
				+"       workingNode = workingNode.parentNode; "
				+"   }"
				+"   if (workingNode == null) return;"
				+"   for (var i=0; i<workingNode.rows.length; i++) { " 
				+"      workingNode.rows[i].deleteCell(current); "			
				+"   }"
				+"} deleteTableColumn();");
			browser.page().mainFrame().evaluateJavaScript(js);
			contentChanged();
	}
	
	
	public void setInsideTable() {
		browser.insertTableRowAction.setEnabled(true);
		browser.insertTableColumnAction.setEnabled(true);
		browser.deleteTableRowAction.setEnabled(true);
		browser.deleteTableColumnAction.setEnabled(true);
		browser.insertTableAction.setEnabled(false);
		browser.encryptAction.setEnabled(false);
		insideTable = true;
	}
	
	public void setInsideLink(String link) {
		browser.insertLinkAction.setText(tr("Edit Hyperlink"));
		currentHyperlink = link;
		insertHyperlink = false;
	}
	
	public void italicActive() {
		italicButton.setDown(true);
	}
	public void boldActive() {
		boldButton.setDown(true);
	}
	public void underlineActive() {
		underlineButton.setDown(true);
	}
	public void forceTextPaste() {
		forceTextPaste = true;
	}
	public void imageContextMenu(String f) {
		browser.downloadImage.setEnabled(true);
		browser.rotateImageRight.setEnabled(true);
		browser.rotateImageLeft.setEnabled(true);
		browser.openAction.setEnabled(true);
		selectedFile = f;
	}
	public void rotateImageRight() {
		QWebSettings.setMaximumPagesInCache(0);
		QWebSettings.setObjectCacheCapacities(0, 0, 0);
		QImage image = new QImage(selectedFile);
		QMatrix matrix = new QMatrix();
		matrix.rotate( 90.0 );
		image = image.transformed(matrix);
		image.save(selectedFile);
		QWebSettings.setMaximumPagesInCache(0);
		QWebSettings.setObjectCacheCapacities(0, 0, 0);
		browser.setHtml(browser.page().mainFrame().toHtml());
		browser.reload();
		contentChanged();
		resourceSignal.contentChanged.emit(selectedFile);

	}
	public void rotateImageLeft() {
		QImage image = new QImage(selectedFile);
		QMatrix matrix = new QMatrix();
		matrix.rotate( -90.0 );
		image = image.transformed(matrix);
		image.save(selectedFile);
		browser.setHtml(browser.page().mainFrame().toHtml());
		browser.reload();
		contentChanged();
		resourceSignal.contentChanged.emit(selectedFile);
	}
	public void resourceContextMenu(String f) {
		browser.downloadAttachment.setEnabled(true);
		browser.openAction.setEnabled(true);
		selectedFile = f;
	}
	public void latexContextMenu(String f) {
		browser.downloadImage.setEnabled(true);
		browser.rotateImageRight.setEnabled(true);
		browser.rotateImageLeft.setEnabled(true);
		browser.openAction.setEnabled(true);
		selectedFile = f;
	}

	//****************************************************************
	//* Apply CSS style to specified word
	//****************************************************************
/*	public void applyStyleToWords(String word, String style) {
		QFile script = new QFile("D:\\NeverNote\\js\\hilight1.js");
		script.open(OpenModeFlag.ReadOnly);
		String s = script.readAll().toString();
		String js = new String(s +" findit('"+word+"', '"+style+"');");
		browser.page().mainFrame().evaluateJavaScript(js);
		System.out.println(getContent());
	}
*/	
	//****************************************************************
	//* Someone tried to paste a resource between notes, so we need  *
	//* to do some special handling.                                 *
	//****************************************************************
	private String fixInternotePaste(String text) {
		logger.log(logger.EXTREME, "Fixing internote paste");
		String returnValue = fixInternotePasteSearch(text, "<img", "src=\"");
		return fixInternotePasteSearch(returnValue, "<a", "href=\"nnres://");
	}
	private String fixInternotePasteSearch(String text, String type, String locTag) {
		
		// First, let's fix the images.
		int startPos = text.indexOf(type);
		int endPos;
		for (; startPos>=0;) {
			endPos = text.indexOf(">", startPos+1);
			String segment = text.substring(startPos, endPos);
			if (segment.indexOf("en-tag") > -1) {
				String newSegment = segment;
				
				int guidStartPos = segment.indexOf("guid=\"");
				int guidEndPos = segment.indexOf("\"", guidStartPos+7);
				String guid = segment.substring(guidStartPos+6,guidEndPos);
				
				int mimeStartPos = segment.indexOf("type");
				int mimeEndPos = segment.indexOf("\"", mimeStartPos+7);
				String mime = segment.substring(mimeStartPos+6,mimeEndPos);

				int srcStartPos = segment.indexOf("src");
				int srcEndPos = segment.indexOf("\"", srcStartPos+6);
				String src = segment.substring(srcStartPos+5,srcEndPos);
				
				Calendar currentTime = new GregorianCalendar();
				Long l = new Long(currentTime.getTimeInMillis());
				long prevTime = l;
				while (l==prevTime) {
					currentTime = new GregorianCalendar();
					l= new Long(currentTime.getTimeInMillis());
				}
				
				Resource r = conn.getNoteTable().noteResourceTable.getNoteResource(guid, true);
				// if r==null, then the image doesn't exist (it was probably cut out of another note, so 
				// we need to recereate it
				if (r==null) {
					r = createResource(src, 1, mime, false);
					if (r==null)
						return "";
				}
		    	String randint = new String(Long.toString(l));
		    	String extension = null;
		    	if (r.getMime()!= null) {
		    		extension = r.getMime().toLowerCase();
		    		if (extension.indexOf("/")>-1)
		    			extension = extension.substring(extension.indexOf("/")+1);
		    	}
		    	String newFile = randint;
		    	if (r.getAttributes().getFileName() != null && r.getAttributes().getFileName() != "")
		    		if (!locTag.startsWith("src"))
		    			newFile = newFile+Global.attachmentNameDelimeter+r.getAttributes().getFileName();
		    	r.setNoteGuid(currentNote.getGuid());
		    	
		    	r.setGuid(randint);
		    	conn.getNoteTable().noteResourceTable.saveNoteResource(r, true);
				QFile f = new QFile(Global.getFileManager().getResDirPath(newFile));
				QByteArray bin = new QByteArray(r.getData().getBody());
				f.open(QFile.OpenModeFlag.WriteOnly);
				f.write(bin);
				f.close();
				newSegment = newSegment.replace("guid=\""+guid, "guid=\""+randint);
				currentNote.getResources().add(r);
				
				int startSrcPos = newSegment.indexOf(locTag);
				int endSrcPos = newSegment.indexOf("\"",startSrcPos+locTag.length()+1);
				String source; 
				if (locTag.startsWith("src")) {
					 source = newSegment.substring(startSrcPos+locTag.length(),endSrcPos);
						newSegment = newSegment.replace(source,
						        FileUtils.toForwardSlashedPath(Global.getFileManager().getResDirPath(newFile)));
				} else {
					source = newSegment.substring(startSrcPos+locTag.length(),endSrcPos);
					newSegment = newSegment.replace(source, newFile);
				}
				
				text = text.substring(0,startPos) + newSegment + text.substring(endPos);
			}
			startPos = text.indexOf(type, startPos+1);
		}
		return text;
	}


	public void nextPage(String file) {
		logger.log(logger.EXTREME, "Starting nextPage()");
		
		Integer pageNumber;
		if (previewPageList.containsKey(file))
			pageNumber = previewPageList.get(file)+1;
		else
			pageNumber = 2;
		previewPageList.remove(file);
		previewPageList.put(file, pageNumber);
		PDFPreview pdfPreview = new PDFPreview();
		boolean goodPreview = pdfPreview.setupPreview(file, "pdf", pageNumber);
		if (goodPreview) {

//			String html = getContent();
			QWebSettings.setMaximumPagesInCache(0);
			QWebSettings.setObjectCacheCapacities(0, 0, 0);
//			browser.setContent(new QByteArray());
			browser.setHtml(browser.page().mainFrame().toHtml());
			browser.reload();
//			browser.setContent(new QByteArray(html));
//			browser.triggerPageAction(WebAction.Reload);
//			pdfMouseOver(selectedFile);
		}
	}

	public void previousPage(String file) {
		logger.log(logger.EXTREME, "Starting previousPage()");
		
		Integer pageNumber;
		if (previewPageList.containsKey(file))
			pageNumber = previewPageList.get(file)-1;
		else
			pageNumber = 1;
		previewPageList.remove(file);
		previewPageList.put(file, pageNumber);
		PDFPreview pdfPreview = new PDFPreview();
		boolean goodPreview = pdfPreview.setupPreview(file, "pdf", pageNumber);
		if (goodPreview) {

//			String html = getContent();
			QWebSettings.setMaximumPagesInCache(0);
			QWebSettings.setObjectCacheCapacities(0, 0, 0);
			browser.setHtml(browser.page().mainFrame().toHtml());
			browser.reload();
//			browser.setContent(new QByteArray(html));
//			browser.triggerPageAction(WebAction.Reload);
		}
	}
	
/*	public void pdfMouseOver(String name) { 
		int pageNumber;
		if (previewPageList.containsKey(selectedFile))
			pageNumber = previewPageList.get(selectedFile)+1;
		else
			pageNumber = 1;
		
		if (pageNumber <= 1)
			browser.previousPageAction.setEnabled(false);
		else
			browser.previousPageAction.setEnabled(true);
		
		PDFPreview pdf = new PDFPreview();
		int totalPages = pdf.getPageCount(name);
		if (previewPageList.containsKey(selectedFile))
			pageNumber = previewPageList.get(selectedFile)+1;
		else
			pageNumber = 1;
		if (totalPages > pageNumber)
			browser.nextPageAction.setEnabled(true);
		else
			browser.nextPageAction.setEnabled(false);
	}
	

	public void pdfMouseOut() { 
//		browser.nextPageAction.setVisible(false);
//		browser.previousPageAction.setVisible(false);
	}
*/
	
	@SuppressWarnings("unused")
	private void toggleUndoVisible(Boolean toggle) {
		undoAction.setVisible(toggle);
		Global.saveEditorButtonsVisible("undo", toggle);
	}
	@SuppressWarnings("unused")
	private void toggleRedoVisible(Boolean toggle) {
		redoAction.setVisible(toggle);
		Global.saveEditorButtonsVisible("redo", toggle);
	}
	@SuppressWarnings("unused")
	private void toggleCutVisible(Boolean toggle) {
		cutAction.setVisible(toggle);
		Global.saveEditorButtonsVisible("cut", toggle);
	}
	@SuppressWarnings("unused")
	private void toggleCopyVisible(Boolean toggle) {
		copyAction.setVisible(toggle);
		Global.saveEditorButtonsVisible("copy", toggle);
	}
	@SuppressWarnings("unused")
	private void togglePasteVisible(Boolean toggle) {
		pasteAction.setVisible(toggle);
		Global.saveEditorButtonsVisible("paste", toggle);
	}
	@SuppressWarnings("unused")
	private void toggleBoldVisible(Boolean toggle) {
		boldAction.setVisible(toggle);
		Global.saveEditorButtonsVisible("bold", toggle);
	}
	@SuppressWarnings("unused")
	private void toggleItalicVisible(Boolean toggle) {
		italicAction.setVisible(toggle);
		Global.saveEditorButtonsVisible("italic", toggle);
	}
	@SuppressWarnings("unused")
	private void toggleUnderlineVisible(Boolean toggle) {
		underlineAction.setVisible(toggle);
		Global.saveEditorButtonsVisible("underline", toggle);
	}
	@SuppressWarnings("unused")
	private void toggleStrikethroughVisible(Boolean toggle) {
		strikethroughAction.setVisible(toggle);
		Global.saveEditorButtonsVisible("strikethrough", toggle);
	}
	@SuppressWarnings("unused")
	private void toggleLeftAlignVisible(Boolean toggle) {
		leftAlignAction.setVisible(toggle);
		Global.saveEditorButtonsVisible("alignLeft", toggle);
	}
	@SuppressWarnings("unused")
	private void toggleRightAlignVisible(Boolean toggle) {
		rightAlignAction.setVisible(toggle);
		Global.saveEditorButtonsVisible("alignRight", toggle);
	}	
	@SuppressWarnings("unused")
	private void toggleCenterAlignVisible(Boolean toggle) {
		centerAlignAction.setVisible(toggle);
		Global.saveEditorButtonsVisible("alignCenter", toggle);
	}
	@SuppressWarnings("unused")
	private void toggleHLineVisible(Boolean toggle) {
		hlineAction.setVisible(toggle);
		Global.saveEditorButtonsVisible("hline", toggle);
	}
	@SuppressWarnings("unused")
	private void toggleIndentVisible(Boolean toggle) {
		indentAction.setVisible(toggle);
		Global.saveEditorButtonsVisible("indent", toggle);
	}
	@SuppressWarnings("unused")
	private void toggleTodoVisible(Boolean toggle) {
		todoAction.setVisible(toggle);
		Global.saveEditorButtonsVisible("todo", toggle);
	}
	@SuppressWarnings("unused")
	private void toggleOutdentVisible(Boolean toggle) {
		outdentAction.setVisible(toggle);
		Global.saveEditorButtonsVisible("outdent", toggle);
	}
	@SuppressWarnings("unused")
	private void toggleBulletListVisible(Boolean toggle) {
		bulletListAction.setVisible(toggle);
		Global.saveEditorButtonsVisible("bulletList", toggle);
	}
	@SuppressWarnings("unused")
	private void toggleNumberListVisible(Boolean toggle) {
		numberListAction.setVisible(toggle);
		Global.saveEditorButtonsVisible("numberList", toggle);
	}
	@SuppressWarnings("unused")
	private void toggleFontListVisible(Boolean toggle) {
		fontListAction.setVisible(toggle);
		Global.saveEditorButtonsVisible("font", toggle);
	}
	@SuppressWarnings("unused")
	private void toggleFontColorVisible(Boolean toggle) {
		fontColorAction.setVisible(toggle);
		Global.saveEditorButtonsVisible("fontColor", toggle);
	}
	@SuppressWarnings("unused")
	private void toggleFontSizeVisible(Boolean toggle) {
		fontSizeAction.setVisible(toggle);
		Global.saveEditorButtonsVisible("fontSize", toggle);
	}
	@SuppressWarnings("unused")
	private void toggleFontHilightVisible(Boolean toggle) {
		fontHilightAction.setVisible(toggle);
		Global.saveEditorButtonsVisible("fontHilight", toggle);
	}
	@SuppressWarnings("unused")
	private void toggleSpellCheckVisible(Boolean toggle) {
		spellCheckAction.setVisible(toggle);
		Global.saveEditorButtonsVisible("spellCheck", toggle);
	}


	private void setupDictionary() {
		File wordList = new File(Global.getFileManager().getSpellDirPath()+Locale.getDefault()+".dic");
		try {
			dictionary = new SpellDictionaryHashMap(wordList);
			spellChecker = new SpellChecker(dictionary);
			
			File userWordList;
			userWordList = new File(Global.getFileManager().getSpellDirPathUser()+"user.dic");
			
			// Get the local user spell dictionary
			try {
				userDictionary = new SpellDictionaryHashMap(userWordList);
			} catch (FileNotFoundException e) {
				userWordList.createNewFile();
				userDictionary = new SpellDictionaryHashMap(userWordList);
			} catch (IOException e) {
				userWordList.createNewFile();
				userDictionary = new SpellDictionaryHashMap(userWordList);
			}
			
			spellListener = new SuggestionListener(this, spellChecker);
			
			// Add the user dictionary
			spellChecker.addSpellCheckListener(spellListener);
			spellChecker.setUserDictionary(userDictionary);

		} catch (FileNotFoundException e) {
			QMessageBox.critical(this, tr("Spell Check Error"), 
					tr("Dictionary ")+ Global.getFileManager().getSpellDirPath()+Locale.getDefault()+
						tr(".dic was not found."));
		} catch (IOException e) {
			QMessageBox.critical(this, tr("Spell Check Error"), 
					tr("Dictionary ")+ Global.getFileManager().getSpellDirPath()+Locale.getDefault()+
						tr(".dic is invalid."));
		}

	}
	
	// Invoke spell checker dialog
	@SuppressWarnings("unused")
	private void spellCheckClicked() {

		if (spellChecker == null) {
			setupDictionary();	
		}
		
		// Read user settings
		spellChecker.getConfiguration().setBoolean(Configuration.SPELL_IGNOREDIGITWORDS, 
				Global.getSpellSetting(Configuration.SPELL_IGNOREDIGITWORDS));
		spellChecker.getConfiguration().setBoolean(Configuration.SPELL_IGNOREINTERNETADDRESSES, 
				Global.getSpellSetting(Configuration.SPELL_IGNOREINTERNETADDRESSES));
		spellChecker.getConfiguration().setBoolean(Configuration.SPELL_IGNOREMIXEDCASE, 
				Global.getSpellSetting(Configuration.SPELL_IGNOREMIXEDCASE));
		spellChecker.getConfiguration().setBoolean(Configuration.SPELL_IGNOREUPPERCASE, 
				Global.getSpellSetting(Configuration.SPELL_IGNOREUPPERCASE));
		spellChecker.getConfiguration().setBoolean(Configuration.SPELL_IGNORESENTENCECAPITALIZATION, 
				Global.getSpellSetting(Configuration.SPELL_IGNORESENTENCECAPITALIZATION));

		spellListener.abortSpellCheck = false;
		spellListener.errorsFound = false;
		String content = getBrowser().page().mainFrame().toPlainText();
		StringWordTokenizer tokenizer = new StringWordTokenizer(content);
		if (!tokenizer.hasMoreWords())
			return;
		getBrowser().page().action(WebAction.MoveToStartOfDocument);

		getBrowser().setFocus();
		boolean found;
			
		// Move to the start of page
		KeyboardModifiers ctrl = new KeyboardModifiers(KeyboardModifier.ControlModifier.value());
		QKeyEvent home = new QKeyEvent(Type.KeyPress, Key.Key_Home.value(), ctrl);  
		browser.keyPressEvent(home);
		getBrowser().setFocus();
			
		tokenizer = new StringWordTokenizer(content);
		String word;
			
		while(tokenizer.hasMoreWords()) {
			word = tokenizer.nextWord();
			found = getBrowser().page().findText(word);
			if (found && !spellListener.abortSpellCheck) {
				spellChecker.checkSpelling(new StringWordTokenizer(word));
				getBrowser().setFocus();
			}
		}

		// Go to the end of the document & finish up.
		home = new QKeyEvent(Type.KeyPress, Key.Key_End.value(), ctrl);  
		browser.keyPressEvent(home);
		if (!spellListener.errorsFound)
			QMessageBox.information(this, tr("Spell Check Complete"), 
					tr("No Errors Found"));

    }
	
	// Source edited
	@SuppressWarnings("unused")
	private void sourceEdited() {
		QTextCodec codec = QTextCodec.codecForLocale();
		codec = QTextCodec.codecForName("UTF-8");
        String content =  codec.fromUnicode(sourceEdit.toHtml()).toString();
		content = StringEscapeUtils.unescapeHtml(removeTags(content));
		QByteArray data = new QByteArray(sourceEditHeader+content+"</body></html>");
		getBrowser().setContent(data);
		checkNoteTitle();
		noteSignal.noteChanged.emit(currentNote.getGuid(), sourceEdit.toPlainText()); 
	}
	
	private void setSource(String text) {
		sourceEdit.blockSignals(true);
		int body = text.indexOf("<body");
		if (body > 0) {
			body = text.indexOf(">",body);
			if (body > 0) {
				sourceEditHeader =text.substring(0, body+1);
				text = text.substring(body+1);
			}
		}
		text = text.replace("</body></html>", "");
		sourceEdit.setPlainText(text);
		sourceEdit.setReadOnly(!getBrowser().page().isContentEditable());
		syntaxHighlighter.rehighlight();
		sourceEdit.blockSignals(false);
	}

	// show/hide view source window
	public void showSource(boolean value) {
		sourceEdit.setVisible(value);
	}

	// Remove HTML tags
	private String removeTags(String text) {
		StringBuffer buffer = new StringBuffer(text);
		boolean inTag = false;
		int bodyPosition = text.indexOf("<body");
		for (int i=buffer.length()-1; i>=0; i--) {
			if (buffer.charAt(i) == '>')
				inTag = true;
			if (buffer.charAt(i) == '<')
				inTag = false;
			if (inTag || buffer.charAt(i) == '<' || i<bodyPosition)
				buffer.deleteCharAt(i);
		}
		
		return buffer.toString();
	}
}
