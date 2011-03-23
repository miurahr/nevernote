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

import com.trolltech.qt.gui.QAction;
import com.trolltech.qt.gui.QMenu;
import com.trolltech.qt.gui.QMenuBar;

import cx.fbn.nevernote.Global;
import cx.fbn.nevernote.NeverNote;

public class MainMenuBar extends QMenuBar {

	private final NeverNote 		parent;
	public QAction			printAction;				// Action when a user selects Print from the file menu
	public QAction			connectAction;				// Connect/Disconnect to Evernote
	public QAction			fullReindexAction;			// Action when a user wants to reindex the entire database
	public QAction 			synchronizeAction;			// Synchronize data with Evernote	
	public QAction			selectiveSyncAction;		// Specify which notebooks or tags to ignore
	public QAction			settingsAction;				// Show user config settings
	public QAction			emailAction;				// Action when a user selects "email"
	public QAction			backupAction;				// Backup the database
	public QAction			restoreAction;				// Restore from a backup
	public QAction 			emptyTrashAction;			// Action when a user wants to clear the trash file
	public QAction			exitAction;					// Action when user selects "exit"
	public QAction			aboutAction;				// Action when a user selects "About"
	public QAction			checkForUpdates;			// Check for newer versions
	public QAction			loggerAction;				// Action when a user selects "Log"
	public QAction			releaseAction;				// Release notes

	public QAction			noteAdd;					// Add a note
	public QAction			noteAttributes;				// Action when a user selects note attributes
	public QAction			noteTags;					// Assign a note tags
	public QAction			noteDelete;					// Delete the current note
	public QAction			noteRestoreAction;			// Restore a note
	public QAction			noteReindex;				// Action when a user wants to reindex a note
	public QAction			noteDuplicateAction;		// Duplicate an existing note
	public QAction			noteMergeAction;			// Merge notes
	public QAction			noteExportAction;			// Export notes
	public QAction			noteImportAction;			// Import notes
	
	public QAction			editFind;					// find text in the current note
	public QAction			editUndo;					// Undo last change
	public QAction			editRedo;					// Redo last change
	public QAction			editCut;					// Cut selected text
	public QAction			editPaste;					// Paste selected text
	public QAction			editPasteWithoutFormat;		// Paste selected text
	public QAction			editCopy;					// Copy selected text;
	
	public QAction			wideListView;				// View with list on the top
	public QAction			narrowListView;				// View with list on the side
	public QAction			thumbnailView;				// view thumbnails
	public QAction			hideSavedSearches;			// show/hide saved searches
	public QAction			hideZoom;					// show/hide the zoom spinner
	public QAction			hideSearch;					// Show/hide the search window
	public QAction			hideQuota;					// show/hide the quota window
	public QAction			hideNotebooks;				// show/hide notebooks
	public QAction			hideTags;					// show/hide tags
	public QAction			hideAttributes;				// show/hide note information
	public QAction			hideTrash;					// show/hide trash tree
	public QAction			hideNoteList;				// show/hide the list of notes
	public QAction			showEditorBar;				// show/hide the editor button bar
	public QAction			hideLeftSide;				// Hide the entire left side
	
	public QAction			formatBold;					// Bold selected text
	public QAction			formatItalic;				// Italics selected text
	public QAction			formatUnderline;			// Underline selected text
	public QAction			formatStrikethrough;		// Strikethrough selected text
	public QAction			formatSuperscript;			// Superscript selected text
	public QAction			formatSubscript;			// Subscript selected text
	public QAction			formatNumberList;			// insert a numbered list
	public QAction			formatBulletList;			// insert a bulleted list;
	public QAction			alignLeftAction;			// Left justify text
	public QAction			alignRightAction;			// Right justify text
	public QAction			alignCenterAction;			// Center text
	public QAction			horizontalLineAction;		// Insert a horizontal line
	public QAction 			indentAction;				// Indent
	public QAction			outdentAction;				// outdent menu action
	
	public QAction			noteOnlineHistoryAction;	// Pull note history from Evernote
	
	public QAction			accountAction;				// Account dialog box action
	public QAction			disableIndexing;			// put indexing on hold.
//	public QAction			compactAction;				// free unused space in the database
	public QAction			databaseStatusAction;		// Current database status
	public QAction			folderImportAction;			// Automatically import files 
	public QAction			spellCheckAction;			// Spell checker
	public QAction			encryptDatabaseAction;		// Encrypt the local database
	
	public QAction			notebookEditAction;			// Edit the selected notebook
	public QAction			notebookAddAction;			// Add a new notebook
	public QAction			notebookDeleteAction;		// Delete a notebook
	public QAction			notebookPublishAction;		// Publish a notebook
	public QAction			notebookShareAction;		// Share a notebook with others
	public QAction			notebookCloseAction;		// Close notebooks
	public QAction			notebookIconAction;			// Change the icon
	public QAction			notebookStackAction;		// Stack/Unstack the icon.
	
	public QAction			savedSearchAddAction;		// Add a saved search
	public QAction			savedSearchEditAction;		// Edit a saved search
	public QAction			savedSearchDeleteAction;	// Delete a saved search
	public QAction			savedSearchIconAction;		// Change a saved search icon
	
	public QAction			tagEditAction;				// Edit a tag
	public QAction			tagAddAction;				// Add a tag
	public QAction			tagDeleteAction;			// Delete a tag
	public QAction			tagIconAction;				// Change the icon
	public QAction			tagMergeAction;				// Merge tags
	
	//**************************************************************************
	//* Menu Bar Titles
	//**************************************************************************
	
	private QMenu			fileMenu;					// File menu
	private QMenu			noteMenu;					// Note menu 
	private QMenu			notebookMenu;				// Notebook menu
	private QMenu			tagMenu;					// Tag menu
	private QMenu			savedSearchMenu;			// Saved Searches		

	private QMenu			editMenu;					// Edit menu

	private QMenu			formatMenu;					// Text format menu
	private QMenu			viewMenu;					// show/hide stuff
	private QMenu			listMenu;					// bullet or numbered list
	private QMenu			indentMenu;					// indent or outdent menu
	private QMenu			alignMenu;					// Left/Right/Center justify
	
	private QMenu			onlineMenu;					// View online stuff (if connected)
	
	private QMenu			toolsMenu;					// Tools menu
	
	private QMenu			helpMenu;	
	
	public MainMenuBar(NeverNote p) {
		parent = p;
		
		
		fullReindexAction = new QAction(tr("Reindex Database"), this);
		fullReindexAction.setToolTip("Reindex all notes");
		fullReindexAction.triggered.connect(parent, "fullReindex()");
		setupShortcut(fullReindexAction, "Tools_Reindex_Database");
				
		printAction = new QAction(tr("Print"), this);
		printAction.setToolTip("Print the current note");
		printAction.triggered.connect(parent, "printNote()");
		setupShortcut(printAction, "File_Print");
		
		emailAction = new QAction(tr("Email"), this);
		emailAction.setToolTip("Email the current note");
		emailAction.triggered.connect(parent, "emailNote()");
		setupShortcut(emailAction, "File_Email");
		
		backupAction = new QAction(tr("Backup Database"), this);
		backupAction.setToolTip("Backup the current database");
		backupAction.triggered.connect(parent, "databaseBackup()");
		setupShortcut(backupAction, "File_Backup");

		restoreAction = new QAction(tr("Restore Database"), this);
		restoreAction.setToolTip("Restore the database from a backup");
		restoreAction.triggered.connect(parent, "databaseRestore()");
		setupShortcut(restoreAction, "File_Restore");
			
		emptyTrashAction = new QAction(tr("Empty Trash"), this);
		emptyTrashAction.setToolTip("Empty the trash folder");
		emptyTrashAction.triggered.connect(parent, "emptyTrash()");
		setupShortcut(emptyTrashAction, "File_Empty_Trash");
		
		noteRestoreAction = new QAction(tr("Restore"), this);
		noteRestoreAction.setToolTip("Restore a deleted file from the trash");
		noteRestoreAction.triggered.connect(parent, "restoreNote()");
		noteRestoreAction.setVisible(false);
		setupShortcut(noteRestoreAction, "File_Note_Restore");
				
		settingsAction = new QAction(tr("Preferences"), this);
		settingsAction.setToolTip("Program settings");
		settingsAction.triggered.connect(parent, "settings()");
		setupShortcut(settingsAction, "Edit_Preferences");
		
		exitAction = new QAction(tr("Exit"), this);
		exitAction.setToolTip("Close the program");
		exitAction.triggered.connect(parent, "closeNeverNote()");
		exitAction.setShortcut("Ctrl+Q");
		setupShortcut(exitAction, "File_Exit");
		
		noteAttributes = new QAction(tr("Extended Information"), this);
		noteAttributes.setToolTip("Show/Hide extended note attributes");
		noteAttributes.triggered.connect(parent, "toggleNoteInformation()");
		noteAttributes.setShortcut("F8");
		setupShortcut(noteAttributes, "View_Extended_Information");
		
		noteReindex = new QAction(tr("Reindex"), this);
		noteReindex.setToolTip(tr("Reindex this note"));
		noteReindex.triggered.connect(parent, "reindexNote()");
		setupShortcut(noteReindex, "File_Note_Reindex");
		
		noteDuplicateAction = new QAction(tr("Duplicate"), this);
		noteDuplicateAction.setToolTip(tr("Duplicate this note"));
		noteDuplicateAction.triggered.connect(parent, "duplicateNote()");
		setupShortcut(noteReindex, "File_Note_Duplicate");
		
		noteMergeAction = new QAction(tr("Merge Notes"), this);
		noteMergeAction.setToolTip(tr("Merge Multiple notes"));
		noteMergeAction.triggered.connect(parent, "mergeNotes()");
		setupShortcut(noteMergeAction, "File_Note_Merge");
		
		noteExportAction = new QAction(tr("Export Selected Notes"), this);
		noteExportAction.setToolTip(tr("Export selected notes"));
		noteExportAction.triggered.connect(parent, "exportNotes()");
		setupShortcut(noteExportAction, "File_Note_Export");
		
		noteImportAction = new QAction(tr("Import Notes"), this);
		noteImportAction.setToolTip(tr("Import notes"));
		noteImportAction.triggered.connect(parent, "importNotes()");
		setupShortcut(noteImportAction, "File_Note_Import");
		
		noteAdd = new QAction(tr("Add"), this);
		noteAdd.setToolTip(tr("Add a new note"));
		noteAdd.triggered.connect(parent, "addNote()");
		setupShortcut(noteAdd, "File_Note_Add");
		//noteAdd.setShortcut("Ctrl+N");
		
		noteTags = new QAction(tr("Modify Tags"), this);
		noteTags.setToolTip(tr("Change the tags assigned to this note"));
		noteTags.triggered.connect(parent.browserWindow, "modifyTags()");
		setupShortcut(noteTags, "File_Note_Modify_Tags");
		
		noteDelete = new QAction(tr("Delete"), this);
		noteDelete.setToolTip(tr("Delete this note"));
		noteDelete.triggered.connect(parent, "deleteNote()");
		setupShortcut(noteDelete, "File_Note_Delete");
	
		editFind = new QAction(tr("Find In Note"), this);
		editFind.setToolTip(tr("Find a string in the current note"));
		editFind.triggered.connect(parent, "findText()");
		setupShortcut(editFind, "Edit_Find_In_Note");
		//editFind.setShortcut("Ctrl+F");
		
		editUndo = new QAction(tr("Undo"), this);
		editUndo.setToolTip(tr("Undo"));
		editUndo.triggered.connect(parent.browserWindow, "undoClicked()");	
		setupShortcut(editUndo, "Edit_Undo");
		//editUndo.setShortcut("Ctrl+Z");
		
		editRedo = new QAction(tr("Redo"), this);
		editRedo.setToolTip(tr("Redo"));
		editRedo.triggered.connect(parent.browserWindow, "redoClicked()");
		setupShortcut(editRedo, "Edit_Redo");
		//editRedo.setShortcut("Ctrl+Y");
	
		editCut = new QAction(tr("Cut"), this);
		editCut.setToolTip(tr("Cut"));
		editCut.triggered.connect(parent.browserWindow, "cutClicked()");
		setupShortcut(editCut, "Edit_Cut");
		//editCut.setShortcut("Ctrl+X");
		
		editCopy = new QAction(tr("Copy"), this);
		editCopy.setToolTip(tr("Copy"));
		editCopy.triggered.connect(parent.browserWindow, "copyClicked()");
		setupShortcut(editCopy, "Edit_Copy");
		//editCopy.setShortcut("Ctrl+C");
		
		editPaste = new QAction(tr("Paste"), this);
		editPaste.setToolTip(tr("Paste"));
		editPaste.triggered.connect(parent.browserWindow, "pasteClicked()");
		setupShortcut(editPaste, "Edit_Paste");

		editPasteWithoutFormat = new QAction(tr("Paste Without Formatting"), this);
		editPasteWithoutFormat.setToolTip(tr("Paste Without Formatting"));
		editPasteWithoutFormat.triggered.connect(parent.browserWindow, "pasteWithoutFormattingClicked()");
		setupShortcut(editPasteWithoutFormat, "Edit_Paste_Without_Formatting");
		
		hideNoteList = new QAction(tr("Show Note List"), this);
		hideNoteList.setToolTip("Show/Hide Note List");
		hideNoteList.triggered.connect(parent, "toggleNoteListWindow()");
		hideNoteList.setCheckable(true);
		hideNoteList.setChecked(true);
		setupShortcut(hideNoteList, "View_Show_Note_List");
		
		hideTags = new QAction(tr("Show Tags"), this);
		hideTags.setToolTip("Show/Hide Tags");
		hideTags.triggered.connect(parent, "toggleTagWindow()");
		hideTags.setCheckable(true);
		hideTags.setChecked(true);
		setupShortcut(hideTags, "View_Show_Tags");
			
		hideNotebooks = new QAction(tr("Show Notebooks"), this);
		hideNotebooks.setToolTip("Show/Hide Notebooks");
		hideNotebooks.triggered.connect(parent, "toggleNotebookWindow()");
		hideNotebooks.setCheckable(true);
		hideNotebooks.setChecked(true);
		setupShortcut(hideNotebooks, "View_Show_Notebooks");
		
		hideZoom = new QAction(tr("Show Zoom"), this);
		hideZoom.setToolTip("Show/Hide Zoom");
		hideZoom.triggered.connect(parent, "toggleZoomWindow()");
		hideZoom.setCheckable(true);
		hideZoom.setChecked(true);
		setupShortcut(hideZoom, "View_Show_Zoom");
		
		hideQuota = new QAction(tr("Show Quota Bar"), this);
		hideQuota.setToolTip("Show/Hide Quota");
		hideQuota.triggered.connect(parent, "toggleQuotaWindow()");
		hideQuota.setCheckable(true);
		hideQuota.setChecked(true);
		setupShortcut(hideQuota, "View_Show_Quota");
		
		hideSearch = new QAction(tr("Show Search Box"), this);
		hideSearch.setToolTip("Show/Hide Search Box");
		hideSearch.triggered.connect(parent, "toggleSearchWindow()");
		hideSearch.setCheckable(true);
		hideSearch.setChecked(true);
		setupShortcut(hideSearch, "View_Show_Search");

		wideListView = new QAction(tr("Wide List View"), this);
		wideListView.setToolTip("Wide List Viwe");
		wideListView.setCheckable(true);
		wideListView.changed.connect(parent, "wideListView()");
		setupShortcut(wideListView, "View_Wide_List");
		
		narrowListView = new QAction(tr("Narrow List View"), this);
		narrowListView.setToolTip("Narrow List View");
		narrowListView.setCheckable(true);
		narrowListView.changed.connect(parent, "narrowListView()");
		setupShortcut(narrowListView, "View_Narrow_List");
		
		thumbnailView = new QAction(tr("Preview"), this);
		thumbnailView.setToolTip("Preview Notes");
		thumbnailView.triggered.connect(parent, "thumbnailView()");
		setupShortcut(thumbnailView, "View_Thumbnail");
		
		hideSavedSearches = new QAction(tr("Show Saved Searches"), this);
		hideSavedSearches.setToolTip("Show/Hide Saved Searches");
		hideSavedSearches.triggered.connect(parent, "toggleSavedSearchWindow()");
		hideSavedSearches.setCheckable(true);
		hideSavedSearches.setChecked(true);
		setupShortcut(hideSavedSearches, "View_Show_SavedSearches");
		
		hideAttributes = new QAction(tr("Show Attribute Searches"), this);
		hideAttributes.setToolTip("Show/Hide Attribute Searches");
		hideAttributes.triggered.connect(parent, "toggleAttributesWindow()");
		hideAttributes.setCheckable(true);
		hideAttributes.setChecked(true);
		setupShortcut(hideAttributes, "View_Show_Attribute_Searches");

		hideTrash = new QAction(tr("Show Trash"), this);
		hideTrash.setToolTip("Show/Hide Trash Tree");
		hideTrash.triggered.connect(parent, "toggleTrashWindow()");
		hideTrash.setCheckable(true);
		hideTrash.setChecked(true);
		setupShortcut(hideTrash, "View_Show_Trash");
		

		showEditorBar = new QAction(tr("Show Editor Button Bar"), this);
		showEditorBar.setToolTip("Show/Hide Editor Button Bar");
		showEditorBar.triggered.connect(parent, "toggleEditorButtonBar()");
		showEditorBar.setCheckable(true);
		showEditorBar.setChecked(true);
		setupShortcut(showEditorBar, "View_Show_Editor_Button_Bar");
		

		hideLeftSide = new QAction(tr("Hide Left Side Panels"), this);
		hideLeftSide.setToolTip("Hide The Entire Left Side");
		hideLeftSide.triggered.connect(parent, "toggleLeftSide()");
		hideLeftSide.setCheckable(true);
		hideLeftSide.setChecked(false);
		setupShortcut(hideLeftSide, "View_Show_Left_Side");
		//hideLeftSide.setShortcut("F11");

		alignLeftAction = new QAction(tr("Left"), this);
		alignLeftAction.setToolTip(tr("Left Align"));
		alignLeftAction.triggered.connect(parent.browserWindow, "justifyLeftClicked()");
		setupShortcut(alignLeftAction, "Format_Alignment_Left");
		//alignLeftAction.setShortcut("Ctrl+L");
		
		alignRightAction = new QAction(tr("Right"), this);
		alignRightAction.setToolTip(tr("Right Align"));
		alignRightAction.triggered.connect(parent.browserWindow, "justifyRightClicked()");
		setupShortcut(alignRightAction, "Format_Alignment_Right");
		//alignRightAction.setShortcut("Ctrl+R");
		
		alignCenterAction = new QAction(tr("Center"), this);
		alignCenterAction.setToolTip(tr("Center Align"));
		alignCenterAction.triggered.connect(parent.browserWindow, "justifyCenterClicked()");
		setupShortcut(alignCenterAction, "Format_Alignment_Center");
		//alignCenterAction.setShortcut("Ctrl+C");
		
		formatBold = new QAction(tr("Bold"), this);
		formatBold.setToolTip(tr("Bold"));
		formatBold.triggered.connect(parent.browserWindow, "boldClicked()");
		setupShortcut(formatBold, "Format_Bold");
		//formatBold.setShortcut("Ctrl+B");
		
		formatItalic = new QAction(tr("Italic"), this);
		formatItalic.setToolTip(tr("Italic"));
		formatItalic.triggered.connect(parent.browserWindow, "italicClicked()");
		setupShortcut(formatItalic, "Format_Italic");
		//formatItalic.setShortcut("Ctrl+I");
		
		formatUnderline = new QAction(tr("Underline"), this);
		formatUnderline.setToolTip(tr("Underline"));
		formatUnderline.triggered.connect(parent.browserWindow, "underlineClicked()");
		setupShortcut(formatUnderline, "Format_Underline");
//		formatUnderline.setShortcut("Ctrl+U");

		
		formatSuperscript = new QAction(tr("Superscript"), this);
		formatSuperscript.setToolTip(tr("Superscript"));
		formatSuperscript.triggered.connect(parent.browserWindow, "superscriptClicked()");
		setupShortcut(formatSuperscript, "Format_Superscript");


		formatSubscript = new QAction(tr("Subscript"), this);
		formatSubscript.setToolTip(tr("Subscript"));
		formatSubscript.triggered.connect(parent.browserWindow, "subscriptClicked()");
		setupShortcut(formatSubscript, "Format_Subscript");

		
		formatStrikethrough = new QAction(tr("Strikethrough"), this);
		formatStrikethrough.setToolTip(tr("Strikethrough"));
		formatStrikethrough.triggered.connect(parent.browserWindow, "strikethroughClicked()");
		setupShortcut(formatStrikethrough, "Format_Strikethrough");

		horizontalLineAction = new QAction(tr("Horizontal Line"), this);
		horizontalLineAction.setToolTip(tr("Horizontal Line"));
		horizontalLineAction.triggered.connect(parent.browserWindow, "hlineClicked()");
		setupShortcut(horizontalLineAction, "Format_Horizontal_Line");
		
		formatBulletList = new QAction(tr("Bulleted List"), this);
//		formatBulletList.setText(tr("Numbered List"));
		formatBulletList.triggered.connect(parent.browserWindow, "bulletListClicked()");
		setupShortcut(formatBulletList, "Format_List_Bullet");
//		formatBulletList.setShortcut("Ctrl+Shift+B");
		
		formatNumberList = new QAction(tr("Numbered List"), this);
		formatNumberList.setText(tr("Numbered list"));
		formatNumberList.triggered.connect(parent.browserWindow, "numberListClicked()");
		setupShortcut(formatNumberList, "Format_List_Numbered");
//		formatNumberList.setShortcut("Ctrl+Shift+O");

		indentAction = new QAction(tr(">> Increase"), this);
		indentAction.setText(tr(">> Increase"));
		indentAction.triggered.connect(parent.browserWindow, "indentClicked()");
		setupShortcut(indentAction, "Format_Indent_Increase");
		//indentAction.setShortcut("Ctrl+M");

		outdentAction = new QAction(tr("<< Decrease"), this);
		outdentAction.setText(tr("<< Decrease"));
		outdentAction.triggered.connect(parent.browserWindow, "outdentClicked()");
		setupShortcut(outdentAction, "Format_Indent_Decrease");
		//outdentAction.setShortcut("Ctrl+Shift+M");
		
		notebookAddAction = new QAction(tr("Add"), this);
		notebookAddAction.triggered.connect(parent, "addNotebook()");
		setupShortcut(notebookAddAction, "File_Notebook_Add");
		
		notebookEditAction = new QAction(tr("Edit"), this);
		notebookEditAction.setEnabled(false);
		notebookEditAction.triggered.connect(parent, "editNotebook()");
		setupShortcut(notebookEditAction, "File_Notebook_Edit");
		
		notebookDeleteAction = new QAction(tr("Delete"), this);
		notebookDeleteAction.setEnabled(false);
		notebookDeleteAction.triggered.connect(parent, "deleteNotebook()");
		setupShortcut(notebookDeleteAction, "File_Notebook_Delete");
		
		notebookPublishAction = new QAction(tr("Share With The World"), this);
		notebookPublishAction.setEnabled(false);
		notebookPublishAction.setVisible(false);
		notebookPublishAction.triggered.connect(parent, "publishNotebook()");
		setupShortcut(notebookPublishAction, "File_Notebook_Publish");

		notebookShareAction = new QAction(tr("Share With Individuals"), this);
		notebookShareAction.setEnabled(false);
		notebookShareAction.setVisible(false);
		notebookShareAction.triggered.connect(parent, "shareNotebook()");
		setupShortcut(notebookShareAction, "File_Notebook_Share");
		
		
		notebookCloseAction = new QAction(tr("Open/Close Notebooks"), this);
		notebookCloseAction.setEnabled(true);
		notebookCloseAction.triggered.connect(parent, "closeNotebooks()");
		setupShortcut(notebookCloseAction, "File_Notebook_Close");

		notebookIconAction = new QAction(tr("Change Icon"), this);
		notebookIconAction.setEnabled(false);
		notebookIconAction.triggered.connect(parent, "setNotebookIcon()");
		setupShortcut(notebookIconAction, "File_Notebook_Icon");
		
		notebookStackAction = new QAction(tr("Set Stack"), this);
		notebookStackAction.setEnabled(false);
		notebookStackAction.triggered.connect(parent, "stackNotebook()");
		setupShortcut(notebookStackAction, "File_Notebook_Stack");
		
		tagAddAction = new QAction(tr("Add"),this);
		tagAddAction.triggered.connect(parent, "addTag()");
		//tagAddAction.setShortcut("Ctrl+Shift+T");
		setupShortcut(tagAddAction, "File_Tag_Add");
		
		tagEditAction = new QAction(tr("Edit"), this);
		tagEditAction.triggered.connect(parent, "editTag()");
		tagEditAction.setEnabled(false);
		setupShortcut(tagEditAction, "File_Tag_Edit");
		
		tagDeleteAction = new QAction(tr("Delete"), this);
		tagDeleteAction.triggered.connect(parent, "deleteTag()");
		tagDeleteAction.setEnabled(false);		
		setupShortcut(tagDeleteAction, "File_Tag_Delete");
				
		tagIconAction = new QAction(tr("Change Icon"), this);
		tagIconAction.triggered.connect(parent, "setTagIcon()");
		tagIconAction.setEnabled(false);		
		setupShortcut(tagIconAction, "File_Tag_Icon");
		
		tagMergeAction = new QAction(tr("Merge Tags"), this);
		tagMergeAction.triggered.connect(parent, "mergeTags()");
		tagMergeAction.setEnabled(false);		
		setupShortcut(tagMergeAction, "File_Tag_Merge");
				
		savedSearchAddAction = new QAction(tr("Add"),this);
		savedSearchAddAction.triggered.connect(parent, "addSavedSearch()");
		setupShortcut(savedSearchAddAction, "File_SavedSearch_Add");
		
		savedSearchEditAction = new QAction(tr("Edit"), this);
		savedSearchEditAction.triggered.connect(parent, "editSavedSearch()");
		savedSearchEditAction.setEnabled(false);
		setupShortcut(savedSearchEditAction, "File_SavedSearch_Edit");
		
		savedSearchDeleteAction = new QAction(tr("Delete"), this);
		savedSearchDeleteAction.triggered.connect(parent, "deleteSavedSearch()");
		savedSearchDeleteAction.setEnabled(false);		
		setupShortcut(savedSearchDeleteAction, "File_SavedSearch_Delete");

		savedSearchIconAction = new QAction(tr("Change Icon"), this);
		savedSearchIconAction.triggered.connect(parent, "setSavedSearchIcon()");
		savedSearchIconAction.setEnabled(false);		
		setupShortcut(savedSearchIconAction, "File_SavedSearch_Icon");		
				
		connectAction = new QAction(tr("Connect"), this);
		connectAction.setToolTip("Connect to Evernote");
		connectAction.triggered.connect(parent, "remoteConnect()");
		setupShortcut(connectAction, "Online_Connect");
		
		synchronizeAction = new QAction(tr("Synchronize with Evernote"), this);
		synchronizeAction.setToolTip("Delete all local data & get a fresh copy");
		synchronizeAction.triggered.connect(parent, "evernoteSync()");
		synchronizeAction.setEnabled(false);
		setupShortcut(synchronizeAction, "Online_Synchronize");
		//synchronizeAction.setShortcut("F9");
		
		noteOnlineHistoryAction = new QAction(tr("Note History"), this);
		noteOnlineHistoryAction.triggered.connect(parent, "viewNoteHistory()");
		noteOnlineHistoryAction.setEnabled(false);
		setupShortcut(noteOnlineHistoryAction, "Online_Note_History");
		
		selectiveSyncAction = new QAction(tr("Selective Synchronize"), this);
		selectiveSyncAction.setToolTip("Selectively ignore some notes");
		selectiveSyncAction.triggered.connect(parent, "setupSelectiveSync()");
		selectiveSyncAction.setEnabled(false);
		setupShortcut(synchronizeAction, "Online_Selective_Sync");
		
		
		
		accountAction = new QAction(tr("Account Information"), this);
		accountAction.setToolTip(tr("Account Information"));
		accountAction.triggered.connect(parent, "accountInformation()");
		setupShortcut(accountAction, "Tools_Account_Information");
		
//		compactAction = new QAction(tr("Compact Database"), this);
//		compactAction.setToolTip(tr("Free unused database space"));
//		compactAction.triggered.connect(parent, "compactDatabase()");
//		setupShortcut(compactAction, "Tools_Compact_Database");

		databaseStatusAction = new QAction(tr("Database Status"), this);
		databaseStatusAction.setToolTip(tr("Show current database information"));
		databaseStatusAction.triggered.connect(parent, "databaseStatus()");
		setupShortcut(databaseStatusAction, "Tools_Database_Status");
		
		
		disableIndexing = new QAction(tr("Disable Note Indexing"), this);
		disableIndexing.setToolTip("Manually Stop Note Indexing");
		disableIndexing.triggered.connect(parent, "toggleNoteIndexing()");
		disableIndexing.setCheckable(true);
		disableIndexing.setChecked(false);
		setupShortcut(disableIndexing, "Tools_Disable_Note_Indexing");
		
		
		folderImportAction = new QAction(tr("Automatic Folder Importing"), this);
		folderImportAction.setToolTip("Import Files Automatically");
		folderImportAction.triggered.connect(parent, "folderImport()");
		setupShortcut(folderImportAction, "Tools_Folder_Import");
		
		spellCheckAction = new QAction(tr("Spell Check"), this);
		spellCheckAction.setToolTip("Check for spelling errors");
		spellCheckAction.triggered.connect(parent.browserWindow, "spellCheckClicked()");
		setupShortcut(spellCheckAction, "Tools_Spell_Check");

		encryptDatabaseAction = new QAction(tr("Encrypt Database"), this);
		encryptDatabaseAction.setToolTip("Encrypt the database upon shutdown");
		encryptDatabaseAction.triggered.connect(parent, "doDatabaseEncrypt()");
		setupShortcut(encryptDatabaseAction, "Tools_Database_Encrypt");
		if (Global.cipherPassword != null && Global.cipherPassword != "") {
			encryptDatabaseAction.setText("Decrypt Database");
			encryptDatabaseAction.setToolTip("Decrypt the database upon shutdown");
		}
		
		loggerAction = new QAction(tr("Logs"), this);
		loggerAction.setToolTip("Show the detailed application log");
		loggerAction.triggered.connect(parent, "logger()");
		setupShortcut(loggerAction, "About_Log");
				
		releaseAction = new QAction(tr("Release Notes"), this);
		releaseAction.setToolTip("Release notes");
		releaseAction.triggered.connect(parent, "releaseNotes()");	
		setupShortcut(releaseAction, "About_Release_Notes");
		
		checkForUpdates = new QAction(tr("Check For Updates"), this);
		checkForUpdates.setToolTip("Check for newer versions");
		checkForUpdates.triggered.connect(parent, "checkForUpdates()"); 
		setupShortcut(checkForUpdates, "Help_Check_For_Updates");
		
		aboutAction = new QAction(tr("About"), this);
		aboutAction.setToolTip("About NeverNote");
		aboutAction.triggered.connect(parent, "about()"); 
		setupShortcut(aboutAction, "About_About");
		
		setupMenuBar();
	}
	
	public void setupMenuBar() {
		fileMenu = addMenu(tr("&File"));
		
		noteMenu = fileMenu.addMenu(tr("&Note"));
		notebookMenu = fileMenu.addMenu(tr("Notebook"));
		tagMenu = fileMenu.addMenu(tr("Tag"));
		savedSearchMenu = fileMenu.addMenu(tr("Saved Searches"));
		fileMenu.addSeparator();
		fileMenu.addAction(emailAction);
		fileMenu.addAction(printAction);
		fileMenu.addSeparator();
		fileMenu.addAction(noteImportAction);
		fileMenu.addAction(noteExportAction);
		fileMenu.addAction(backupAction);
		fileMenu.addAction(restoreAction);
		fileMenu.addSeparator();
		fileMenu.addAction(emptyTrashAction);
		fileMenu.addAction(exitAction);

		editMenu = addMenu(tr("&Edit"));
		editMenu.addAction(editFind);
		editMenu.addSeparator();
		editMenu.addAction(editUndo);
		editMenu.addAction(editRedo);
		editMenu.addSeparator();
		editMenu.addAction(editCut);
		editMenu.addAction(editCopy);
		editMenu.addAction(editPaste);
		editMenu.addAction(editPasteWithoutFormat);
		editMenu.addSeparator();
		editMenu.addAction(settingsAction);
		
		viewMenu = addMenu(tr("&View"));
		viewMenu.addAction(noteAttributes);
		viewMenu.addSeparator();
		viewMenu.addAction(wideListView);
		viewMenu.addAction(narrowListView);
		viewMenu.addAction(thumbnailView);
		viewMenu.addSeparator();
		viewMenu.addAction(hideNoteList);
		viewMenu.addAction(hideSearch);
		viewMenu.addAction(hideQuota);
		viewMenu.addAction(hideZoom);
		viewMenu.addAction(hideNotebooks);
		viewMenu.addAction(hideTags);
		viewMenu.addAction(hideAttributes);
		viewMenu.addAction(hideSavedSearches);
		viewMenu.addAction(hideTrash);
		viewMenu.addAction(showEditorBar);
		viewMenu.addAction(hideLeftSide);
		
		formatMenu = addMenu(tr("&Format"));
		formatMenu.addAction(formatBold);
		formatMenu.addAction(formatUnderline);
		formatMenu.addAction(formatItalic);
		formatMenu.addSeparator();
		formatMenu.addAction(formatStrikethrough);
		formatMenu.addAction(horizontalLineAction);
		formatMenu.addSeparator();
		formatMenu.addAction(formatSuperscript);
		formatMenu.addAction(formatSubscript);
		formatMenu.addSeparator();

		alignMenu = formatMenu.addMenu(tr("Alignment"));
		alignMenu.addAction(alignLeftAction);
		alignMenu.addAction(alignCenterAction);
		alignMenu.addAction(alignRightAction);
		
		listMenu = formatMenu.addMenu(tr("Lists"));
		listMenu.addAction(formatBulletList);
		listMenu.addAction(formatNumberList);
		indentMenu = formatMenu.addMenu(tr("Indent"));
		indentMenu.addAction(indentAction);
		indentMenu.addAction(outdentAction);
		
		noteAttributes.setCheckable(true);
		noteMenu.addAction(noteAdd);
		noteMenu.addAction(noteDelete);
		noteMenu.addAction(noteReindex);
		noteMenu.addSeparator();
		noteMenu.addAction(noteTags);
		noteMenu.addAction(noteRestoreAction);
		noteMenu.addSeparator();
		noteMenu.addAction(noteDuplicateAction);
		noteMenu.addAction(noteMergeAction);

				
		notebookMenu.addAction(notebookAddAction);
		notebookMenu.addAction(notebookEditAction);
		notebookMenu.addAction(notebookDeleteAction);
		notebookMenu.addSeparator();
		notebookMenu.addAction(notebookPublishAction);
		notebookMenu.addAction(notebookShareAction);
		notebookMenu.addSeparator();
		notebookMenu.addAction(notebookStackAction);
		notebookMenu.addAction(notebookCloseAction);
		notebookMenu.addSeparator();
		notebookMenu.addAction(notebookIconAction);
		
		tagMenu.addAction(tagAddAction);
		tagMenu.addAction(tagEditAction);
		tagMenu.addAction(tagDeleteAction);
		tagMenu.addAction(tagMergeAction);
		tagMenu.addSeparator();
		tagMenu.addAction(tagIconAction);
		
		savedSearchMenu.addAction(savedSearchAddAction);
		savedSearchMenu.addAction(savedSearchEditAction);
		savedSearchMenu.addAction(savedSearchDeleteAction);
		savedSearchMenu.addSeparator();
		savedSearchMenu.addAction(savedSearchIconAction);
		
		onlineMenu = addMenu(tr("&Online"));
		onlineMenu.addAction(synchronizeAction);
		onlineMenu.addAction(connectAction);
		onlineMenu.addSeparator();
		onlineMenu.addAction(noteOnlineHistoryAction);
		onlineMenu.addAction(selectiveSyncAction);
		
		toolsMenu = addMenu(tr("&Tools"));
		toolsMenu.addAction(spellCheckAction);
		toolsMenu.addAction(accountAction);
		toolsMenu.addAction(fullReindexAction);
		toolsMenu.addAction(disableIndexing);
//		toolsMenu.addAction(compactAction);
		toolsMenu.addSeparator();
		toolsMenu.addAction(encryptDatabaseAction);
		toolsMenu.addAction(databaseStatusAction);
		toolsMenu.addSeparator();
		toolsMenu.addAction(folderImportAction);

		helpMenu = addMenu(tr("&Help"));
		helpMenu.addAction(releaseAction);
		helpMenu.addAction(checkForUpdates);
		helpMenu.addAction(loggerAction);
		helpMenu.addSeparator();
		helpMenu.addAction(aboutAction);
		
		addMenu(fileMenu);
		addMenu(editMenu);
		addMenu(viewMenu);
		addMenu(formatMenu);
		addMenu(onlineMenu);
		addMenu(toolsMenu);
		addMenu(helpMenu);

	}

	public void setupToolBarVisible() {
		viewMenu.addAction(parent.toolBar.toggleViewAction());
		setupShortcut(parent.toolBar.toggleViewAction(), "View_Toolbar");
	}
	
	private void setupShortcut(QAction action, String text) {
		if (!Global.shortcutKeys.containsAction(text))
			return;
		action.setShortcut(Global.shortcutKeys.getShortcut(text));
	}

}
