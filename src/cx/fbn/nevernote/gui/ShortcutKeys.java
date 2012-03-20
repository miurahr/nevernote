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
import java.util.HashMap;
import java.util.Scanner;
import java.util.Vector;

import cx.fbn.nevernote.Global;

public class ShortcutKeys {
	public String File_Note_Add;  	  			// Add a new note
	public String File_Note_Reindex;			// Reindex the current note
	public String File_Note_Modify_Tags;		// Change current note tags
	public String File_Note_Delete;				// Delete a tag
	public String File_Note_Restore;			// Undelete a note
	public String File_Note_Duplicate;			// duplicate a note
	public String File_Notebook_Add;			// Add a notebook
	public String File_Notebook_Edit;			// Edit an existing notebook
	public String File_Notebook_Delete;			// Delete the existing notebook
	public String File_Notebook_Open; 			// Open a closed (i.e. archived) notebook
	public String File_Notebook_Close;			// Close (i.e. archive) a notebook
	public String File_Tag_Add;					// Add a notebook
	public String File_Tag_Edit;				// Edit an existing notebook
	public String File_Tag_Delete;				// Delete the existing notebook
	public String File_SavedSearch_Add;			// Add a notebook
	public String File_SavedSearch_Edit;		// Edit an existing notebook
	public String File_SavedSearch_Delete;		// Delete the existing notebook
	public String File_Email;					// Email note
	public String File_Print;					// Print
	public String File_Backup;					// Backup the database
	public String File_Restore;					// Restore the database
	public String File_Empty_Trash;				// Purge all delete notes
	public String File_Exit;					// I'm outahere

	public String Edit_Find_In_Note;			// Search only within the current note
	public String Edit_Undo;					// Undo last change
	public String Edit_Redo;					// Redo the last undone change
	public String Edit_Cut;						// Cut current selection to the clipboard
	public String Edit_Copy;					// Copy the current selection to the clipboard
	public String Edit_Paste;					// Paste
	public String Edit_Paste_Without_Formatting; // Paste as plain text
	public String Edit_Preferences;				// Settings dialog box
	public String Edit_Insert_Hyperlink;		// Encrypt selected text
	public String Edit_Insert_Table;			// Insert table into note
	public String Edit_Insert_Table_Row;		// Insert row into table
	public String Edit_Delete_Table_Row;		// Delete a table row
	public String Edit_Insert_Todo;				// Insert todo
	public String Edit_Encrypt_Text;			// Encrypt selected text
	public String Edit_Rotate_Image_Right;		// Rotate an image right
	public String Edit_Rotate_Image_Left;		// Rotate an image left

	public String View_Extended_Information;	// View details on the current note
	public String View_Thumbnail;				// View Image Thumbnail
	public String View_Show_Note_List;			// Show current notes
	public String View_Show_Notebooks;			// Show notebooks
	public String View_Show_Tags;				// Show the tags window
	public String View_Show_Attribute_Searches;	// Show the attribute selection tree
	public String View_Show_SavedSearches;		// Show the saved search tree
	public String View_Show_Trash;				// Show the trash window
	public String View_Show_Editor_Button_Bar;	// Hide the editor button bar
	public String View_Show_Left_Side;			// Hide all left hand windows

	public String Format_Bold;					// Bold (duh)
	public String Format_Underline;				// Underline
	public String Format_Italic;				// Italic
	public String Format_Strikethrough;			// Strikethrough
	public String Format_Horizontal_Line;		// Href line
	public String Format_Superscript;			// Set superscript
	public String Format_Subscript;				// Subscript
	public String Format_Alignment_Left;		// Left align text
	public String Format_Alignment_Center;		// Center text
	public String Format_Alignment_Right;		// Right align text
	public String Format_List_Bullet;			// Bullet list
	public String Format_List_Numbered;			// Numbered list 
	public String Format_Indent_Increase;		// Increase the indentation
	public String Format_Indent_Decrease;		// Decrease the indent

	public String Online_Note_History;			// Synchronize with Evernote
	
	public String Online_Synchronize;			// Synchronize with Evernote
	public String Online_Connect;				// Connect to Evernote
	public String Tools_Account_Information;	// Show account information
	public String Tools_Reindex_Database;		// Reindex the entire database
	public String Tools_Disable_Note_Indexing;	// Disable note indexing
	public String Tools_Compact_Database;		// Free unused database space
	public String Tools_Database_Status;		// Current database information

	public String About_Release_Notes;			// Current version's release notes
	public String About_Log;					// Message log
	public String About_About;					// About dialog box
	
	public String Focus_Title;					// Switch focus to the title bar
	public String Focus_Tag;					// Switch focus to the tag edit
	public String Focus_Note;					// Switch focus to the note
	public String Focus_Author;					// Switch focus to the author
	public String Focus_Url;					// Switch focus to the URL
	
	public String Insert_DateTime;				// Insert the current date/time
	
	HashMap<String, String> actionMap;
	HashMap<String, String> shortcutMap;
	
	public ShortcutKeys() {
		File_Note_Add = new String("Ctrl+N");		// Add a new note
		File_Note_Reindex = new String();			// Reindex the current note
		File_Note_Modify_Tags = new String();		// Change current note tags
		File_Note_Delete = new String();			// Delete a tag
		File_Note_Restore = new String();			// Undelete a note
		File_Note_Duplicate = new String();			// Duplicate a note
		File_Notebook_Add = new String();			// Add a notebook
		File_Notebook_Edit = new String();			// Edit an existing notebook
		File_Notebook_Delete = new String();		// Delete the existing notebook
		File_Notebook_Open = new String(); 			// Open a closed (i.e. archived) notebook
		File_Notebook_Close = new String();			// Close (i.e. archive) a notebook
		File_Tag_Add = new String("Ctrl+Shift+T");	// Add a notebook
		File_Tag_Edit = new String();				// Edit an existing notebook
		File_Tag_Delete = new String();				// Delete the existing notebook
		File_SavedSearch_Add = new String();		// Add a notebook
		File_SavedSearch_Edit = new String();		// Edit an existing notebook
		File_SavedSearch_Delete = new String();		// Delete the existing notebook
		File_Email = new String("Ctrl+Shift+E");	// Email note
		File_Print = new String("Ctrl+P");			// Print
		File_Backup = new String("");				// Backup
		File_Restore = new String("");				// Restore
		File_Empty_Trash = new String();			// Purge all delete notes
		File_Exit = new String("Ctrl+Q");			// I'm outahere

		Edit_Find_In_Note = new String("Ctrl+F");	// Search only within the current note
		Edit_Undo = new String("Ctrl+Z");			// Undo last change
		Edit_Redo = new String("Ctrl+Y");			// Redo the last undone change
		Edit_Cut = new String("Ctrl+X");			// Cut current selection to the clipboard
		Edit_Copy = new String("Ctrl+C");			// Copy the current selection to the clipboard
		Edit_Paste = new String("Ctrl+V");			// Paste
		Edit_Paste_Without_Formatting = new String("Ctrl+Shift+P"); // Paste as plain text
		Edit_Preferences = new String();			// Settings dialog box
		
		Edit_Insert_Hyperlink = new String("Ctrl+K");  	// Insert a hyperlink
		Edit_Insert_Table = new String();  				// Insert a table
		Edit_Insert_Table_Row = new String();  			// Insert a table row
		Edit_Delete_Table_Row = new String();  			// Delete a table row
		Edit_Insert_Todo = new String();
		Edit_Encrypt_Text = new String();
		Edit_Rotate_Image_Right = new String();
		Edit_Rotate_Image_Left = new String();

		View_Extended_Information = new String("F8");	// View details on the current note
		View_Thumbnail = new String();				// View the thumbnail
		View_Show_Note_List = new String("F10");	// Show current notes
		View_Show_Notebooks = new String();			// Show notebooks
		View_Show_Tags = new String();				// Show the tags window
		View_Show_Attribute_Searches = new String();	// Show the attribute selection tree
		View_Show_SavedSearches = new String();		// Show the saved search tree
		View_Show_Trash = new String();				// Show the trash window
		View_Show_Editor_Button_Bar = new String();	// Hide the editor button bar
		View_Show_Left_Side = new String("F11");	// Hide all left hand windows

		Format_Bold = new String("Ctrl+B");			// Bold (duh)
		Format_Underline = new String("Ctrl+U");		// Underline
		Format_Italic = new String("Ctrl+I");			// Italic
		Format_Strikethrough = new String("Ctrl+-");	// Strikethrough
		Format_Horizontal_Line = new String();		// Href line
		Format_Superscript = new String("Ctrl+=");	// Set superscript
		Format_Subscript = new String("Ctrl+Shift+=");	// Subscript
		Format_Alignment_Left = new String("Ctrl+L");	// Left align text
		Format_Alignment_Center = new String("Ctrl+E");	// Center text
		Format_Alignment_Right = new String("Ctrl+R");	// Right align text
		Format_List_Bullet = new String("Ctrl+Shift+B");	// Bullet list
		Format_List_Numbered = new String("Ctrl+Shift+N");	// Numbered list 
		Format_Indent_Increase = new String("Ctrl+M");		// Increase the indentation
		Format_Indent_Decrease = new String("Ctrl+Shift+M");	// Decrease the indent

		Online_Note_History = new String();
		
		Online_Synchronize = new String("F9");		// Synchronize with Evernote
		Online_Connect = new String();				// Connect to Evernote
		Tools_Account_Information = new String();	// Show account information
		Tools_Reindex_Database = new String();		// Reindex the entire database
		Tools_Disable_Note_Indexing = new String();	// Disable note indexing
		Tools_Compact_Database = new String();		// Free unused database space
		Tools_Database_Status = new String();		// Current database information

		About_Release_Notes = new String();			// Current version's release notes
		About_Log = new String();					// Message log
		About_About = new String();					// About dialog box
		
		Insert_DateTime = new String("Ctrl+;");
		
		Focus_Title = new String();
		Focus_Tag = new String("Ctrl+Shift+T");
		Focus_Note = new String();
		Focus_Author = new String();
		Focus_Url = new String();
		
		// Setup value Array
		shortcutMap = new HashMap<String, String>();
		actionMap = new HashMap<String, String>();
		
		// Load the defaults
		loadKey("File_Note_Add", File_Note_Add);
		loadKey("File_Tag_Add", File_Tag_Add);
		loadKey("File_Email", File_Email);
		loadKey("File_Print", File_Print);
		loadKey("File_Backup", File_Backup);
		loadKey("File_Restore", File_Restore);
		loadKey("File_Exit", File_Exit);
		
		loadKey("Edit_Find_In_Note", Edit_Find_In_Note);
		loadKey("Edit_Undo", Edit_Undo);
		loadKey("Edit_Redo", Edit_Redo);
		loadKey("Edit_Cut", Edit_Cut);
		loadKey("Edit_Copy", Edit_Copy);
		loadKey("Edit_Paste", Edit_Paste);
		loadKey("Edit_Paste_Without_Formatting", Edit_Paste_Without_Formatting);
		loadKey("Edit_Insert_Hyperlink", Edit_Insert_Hyperlink);
		loadKey("Edit_Insert_Table_Row", Edit_Insert_Table_Row);
		loadKey("Edit_Delete_Table_Row", Edit_Delete_Table_Row);
		loadKey("Edit_Insert_Todo", Edit_Insert_Todo);
		loadKey("Edit_Rotate_Image_Right", Edit_Rotate_Image_Right);
		loadKey("Edit_Rotate_Image_Left", Edit_Rotate_Image_Left);
		
		loadKey("View_Extended_Information", View_Extended_Information);
		loadKey("View_Thumbnail", View_Thumbnail);
		loadKey("View_Show_Note_List", View_Show_Note_List);
		loadKey("View_Show_Left_Side",View_Show_Left_Side);
		
		loadKey("Format_Bold", Format_Bold);
		loadKey("Format_Underline", Format_Underline);
		loadKey("Format_Italic", Format_Italic);
		loadKey("Format_Strikethrough", Format_Strikethrough);
		loadKey("Format_Superscript", Format_Superscript);
		loadKey("Format_Subscript", Format_Subscript);
		loadKey("Format_Alignment_Left", Format_Alignment_Left);
		loadKey("Format_Alignment_Center", Format_Alignment_Center);
		loadKey("Format_Alignment_Right", Format_Alignment_Right);
		loadKey("Format_List_Bullet", Format_List_Bullet);
		loadKey("Format_List_Numbered", Format_List_Numbered);
		loadKey("Format_Indent_Increase", Format_Indent_Increase);
		loadKey("Format_Indent_Decrease", Format_Indent_Decrease);
		loadKey("Tools_Synchronize", Online_Synchronize);


		loadKey("Focus_Title", Focus_Title);
		loadKey("Focus_Tag", Focus_Tag);
		loadKey("Focus_Note", Focus_Note);
		loadKey("Focus_Author", Focus_Author);
		loadKey("Focus_Url", Focus_Url);
		
		loadKey("Insert_DateTime", Insert_DateTime);
		
		loadCustomKeys();
		
	}
	
	// Read in the custom keys (if they exist)
	private void loadCustomKeys() {
		File file = Global.getFileManager().getHomeDirFile("shortcuts.txt");
		try {
			Scanner scanner = new Scanner(file);
			while ( scanner.hasNextLine() ){
				String line = scanner.nextLine();
				line = line.replace("\t", " ");  // Replace tab characters
				line = line.replace("\n", " "); // replace newline
				line = line.replace("\r", " "); // replace carrage return
				line = line.trim();             // compress the line
				String split[] = line.split(" ");
				Vector<String> keyVector = new Vector<String>();
				for (int i=0; i<split.length; i++) {
					if (!split[i].trim().equals("") && !split[i].trim().startsWith("//"))
						keyVector.add(split[i]);
					if (split[i].trim().startsWith("//"))
						i=split.length;
				}
				if (keyVector.size() == 1)
					removeByAction(keyVector.get(0));
				if (keyVector.size() >=2) 
					loadKey(keyVector.get(0), keyVector.get(1));
				
			}
		} catch (FileNotFoundException e) {
			return;
		}
	}
	
	
	// Load a key value into the map for later use
	public void loadKey(String action, String shortcut) {
		shortcut = shortcut.trim().toLowerCase();
		action = action.trim().toLowerCase();
		
		// If we have an existing one, remove it.
		if (actionMap.containsKey(action))
			removeByAction(action);
		if (shortcutMap.containsKey(shortcut))
			removeByShortcut(shortcut);
		
		if (shortcut.equals("")) {
			removeByShortcut(shortcut);
			return;
		}
		
		// Add the new value
		actionMap.put(action.toLowerCase(), shortcut);
		shortcutMap.put(shortcut.toLowerCase(), action);
	}
	
	// Remove a shortcut by the Shortcut key
	public void removeByShortcut(String shortcut) {
		String action = shortcutMap.get(shortcut.toLowerCase());
		shortcutMap.remove(shortcut.toLowerCase());
		if (action != null)
			actionMap.remove(action.toLowerCase());
	}
	
	// Remove a shortcut by the action itself
	public void removeByAction(String action) {
		String shortcut = actionMap.get(action.toLowerCase());
		actionMap.remove(action.toLowerCase());
		if (shortcut != null)
			shortcutMap.remove(shortcut.toLowerCase());
	}
	
	// Check if a shortcut key exists
	public boolean containsShortcut(String shortcut) {
		return shortcutMap.containsKey(shortcut.toLowerCase());
	}
	
	// Check if an action exists
	public boolean containsAction(String action) {
		return actionMap.containsKey(action.toLowerCase());
	}
	
	// Get a key based upon the action
	public String getShortcut(String action) {
		return actionMap.get(action.toLowerCase());
	}
	
	// Get an action based upon the key
	public String getAction(String shortcut) {
		return shortcutMap.get(shortcut.toLowerCase());
	}
	
}
