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


import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang3.StringEscapeUtils;

import com.evernote.edam.type.Accounting;
import com.evernote.edam.type.PrivilegeLevel;
import com.evernote.edam.type.User;
import com.evernote.edam.type.UserAttributes;
import com.swabunga.spell.engine.Configuration;
import com.trolltech.qt.core.QByteArray;
import com.trolltech.qt.core.QSettings;
import com.trolltech.qt.core.QSize;
import com.trolltech.qt.gui.QPalette;

import cx.fbn.nevernote.config.FileManager;
import cx.fbn.nevernote.config.InitializationException;
import cx.fbn.nevernote.config.StartupConfig;
import cx.fbn.nevernote.gui.ContainsAttributeFilterTable;
import cx.fbn.nevernote.gui.DateAttributeFilterTable;
import cx.fbn.nevernote.gui.ShortcutKeys;
import cx.fbn.nevernote.utilities.ApplicationLogger;
import cx.fbn.nevernote.utilities.Pair;


//*****************************************************
//*****************************************************
//* Global constants & static functions used by 
//* multiple threads.
//*****************************************************
//*****************************************************

public class Global {
	// Set current version and the known versions.
	public static String version = "1.2";
	public static String[] validVersions = {"1.2", "1.1", "1.0", "0.99", "0.98", "0.97", "0.96"};
    public static String username = ""; 
    public static String password = "";     
    

    // Each thread has an ID.  This is used primarily to check the status
    // of running threads.
    public static final int mainThreadId=0;
    public static final int syncThreadId=1;
    public static final int tagCounterThreadId=2;
    public static final int trashCounterThreadId=3;   // This should always be the highest thread ID
    public static final int indexThreadId=4;   	// Thread for indexing words
    public static final int saveThreadId=5;   	// Thread used for processing data to saving content
    public static final int notebookCounterThreadId=6;   // Notebook Thread
    public static final int indexThread03Id=7;   // unused
    public static final int indexThread04Id=8;   // unused
    public static final int dbThreadId=9;   // This should always be the highest thread ID
    public static final int threadCount = 10;
    
    
    // These variables deal with where the list of notes appears
    // They will either be vertical (View_List_Narrow) or will be
    // on top of the note (View_List_Wide).  It also has the size of
    // thumbnails displayed in each view
    public static int View_List_Wide = 1;
    public static int View_List_Narrow = 2;
    public static QSize smallThumbnailSize = new QSize(100,75);
    public static QSize largeThumbnailSize = new QSize(300,225);

    // This is used to keep a running list of passwords that the user
    // wants us to remember.
    public static HashMap<String,Pair<String,String>> passwordSafe = new HashMap<String, Pair<String,String>>();
    public static List<Pair<String,String>> passwordRemember = new ArrayList<Pair<String,String>>();
    
    
    //public static String currentNotebookGuid;
    
    // These deal with Evernote user settings
    public static User user; 
    public static long authTimeRemaining;
    public static long authRefreshTime;
    public static long failedRefreshes = 0; 
    public static String userStoreUrl;
    public static String noteStoreUrl;
    public static String noteStoreUrlBase;

    // When we want to shut down we set this to true to short
    // circut other threads
    public static boolean keepRunning;
        
    // In the note list, these are the column numbers
    // so I don't need to hard code numbers.
    public static int noteTableCreationPosition = 0;
    public static int noteTableTitlePosition = 1;
    public static int noteTableTagPosition = 2;
    public static int noteTableNotebookPosition = 3;
    public static int noteTableChangedPosition = 4;
    public static int noteTableGuidPosition = 5;
    public static int noteTableAuthorPosition = 6;
    public static int noteTableSourceUrlPosition = 7;
    public static int noteTableSubjectDatePosition = 8;
    public static int noteTableSynchronizedPosition = 9;
    public static int noteTableThumbnailPosition = 10;
    public static int noteTablePinnedPosition = 11;
    public static int noteTableColumnCount = 12;
    public static Integer cryptCounter = 0;
    
    //public static int minimumWordCount = 2;
    
    // Regular expression to parse text with when indexing
    private static String wordRegex;
    
    // Experimental fixes.  Set via Edit/Preferences/Debugging
    public static boolean enableCarriageReturnFix = false;
    public static boolean enableHTMLEntitiesFix = false;
    
    // Used to set & retrieve ini & Windows registry settings
    public static QSettings	settings;     // Set & get ini settings
    public static boolean isConnected;    // Are we connected to Evernote
    public static boolean showDeleted = false;   // Show deleted notes?
    public static boolean disableUploads = false;  // Should we disable uploads (used in testing features)
	public static int messageLevel;   // The level of messages to write to the log files
	public static String tagDelimeter = ",";   // This is used to separate out tag names when entering above note
	public static String attachmentNameDelimeter = "------";  // Used to separate out attachment names in the res directory
	
	
	//* Database fields
	public static String	databaseName = new String("NeverNote");  // database name.  used for multiple databases to separate settings.
	public static String	indexDatabaseName = new String("Index"); // searchable words database
	public static String	resourceDatabaseName = new String("Resources");  // attachments database
	public static DateAttributeFilterTable createdSinceFilter;
	public static DateAttributeFilterTable createdBeforeFilter;
	public static DateAttributeFilterTable changedSinceFilter;
	public static DateAttributeFilterTable changedBeforeFilter;
	public static ContainsAttributeFilterTable containsFilter;
	
	// Log file used for debugging
	public static ApplicationLogger    logger;
	//PrintStream stdoutStream;
	
	// Application key shortcuts & appearance
	public static QPalette 				originalPalette;
	public static ShortcutKeys			shortcutKeys;
	
	public static boolean				disableViewing;  // used to disable the editor
	
	// When saving a note, this is a list of things we strip out because Evernote hates them
	public static List<String>				invalidElements = new ArrayList<String>();
	public static HashMap<String, ArrayList<String>> 	invalidAttributes = new HashMap<String, ArrayList<String>>();
	
	public static boolean mimicEvernoteInterface; // Try to mimic Evernote or allow multiple notebook selection
	public static HashMap<String,String> resourceMap;   // List of attachments for a note.
	public static String cipherPassword = "";    // If the database is encrypted, this stores the password
	public static String databaseCache = "16384";  // Default DB cache size
	
	// These are used for performance testing
	static Calendar startTraceTime;   
	static Calendar intervalTraceTime;
	
	private static FileManager fileManager;  // Used to access files & directories
	
    // Do initial setup 
    public static void setup(StartupConfig startupConfig) throws InitializationException  {
        settings = new QSettings("fbn.cx", startupConfig.getName());
        disableViewing = startupConfig.getDisableViewing();

        fileManager = new FileManager(startupConfig.getHomeDirPath(), startupConfig.getProgramDirPath());


		getServer();  // Setup URL to connect to
		
		// Get regular expressions used to parse out words
		settings.beginGroup("General");
		String regex = (String) settings.value("regex", "[,\\s]+");
		setWordRegex(regex);
		settings.endGroup();
		
		//Setup debugging information
		settings.beginGroup("Debug");
		String msglevel = (String) settings.value("messageLevel", "Low");
		settings.endGroup();
		
		
		//messageLevel = 1;
		setMessageLevel(msglevel);
		keepRunning = true;  // Make sure child threads stay running
		disableUploads = disableUploads();  // Should we upload anything?  Normally true.
		enableCarriageReturnFix = enableCarriageReturnFix();  // Enable test fix?
		enableHTMLEntitiesFix = enableHtmlEntitiesFix();  // Enable test fix?
		
		logger = new ApplicationLogger("global.log");  // Setup log for this class 
		shortcutKeys = new ShortcutKeys();  // Setup keyboard shortcuts.
		mimicEvernoteInterface = getMimicEvernoteInterface();  // Should we mimic Evernote's notebook behavior
		resourceMap = new HashMap<String,String>();  // Setup resource map used to store attachments when editing
			
		databaseCache = getDatabaseCacheSize();	 // Set database cache size	
    }

    // Get/Set word parsing regular expression
    public static String getWordRegex() {
    	return wordRegex;
    }
    public static void setWordRegex(String r) {
    	wordRegex = r;
    }

   // Set the debug message level
   public static void setMessageLevel(String msglevel) {
    	if (msglevel.equalsIgnoreCase("low")) 
			messageLevel = 1;
		if (msglevel.equalsIgnoreCase("medium")) 
			messageLevel = 2;
		if (msglevel.equalsIgnoreCase("high")) 
				messageLevel = 3;
		if (msglevel.equalsIgnoreCase("extreme")) 
					messageLevel = 4;
		settings.beginGroup("Debug");
		settings.setValue("messageLevel", msglevel);
		settings.endGroup();    	
    }

   //****************************************************
   //****************************************************
   //** Save user account information from Evernote
   //****************************************************
   //****************************************************
    public static void saveUserInformation(User user) {
    	settings.beginGroup("User");
		settings.setValue("id", user.getId());
		settings.setValue("username", user.getUsername());
		settings.setValue("email", user.getEmail());
		settings.setValue("name", user.getName());
		settings.setValue("timezone", user.getTimezone());
		settings.setValue("privilege", user.getPrivilege().getValue());
		settings.setValue("created", user.getCreated());
		settings.setValue("updated", user.getUpdated());
		settings.setValue("deleted", user.getDeleted());
		settings.setValue("shard", user.getShardId());
		settings.endGroup();
		isPremium();
		if (user.getAttributes()!=null)
			saveUserAttributes(user.getAttributes());
		if (user.getAccounting()!=null)
			saveUserAccounting(user.getAccounting());

    }
    public static User getUserInformation() {
    	User user = new User();
    	settings.beginGroup("User");
    	try {	
    		user.setId((Integer)settings.value("id", 0));    		
    	} catch  (java.lang.ClassCastException e) {
    		user.setId(new Integer((String)settings.value("id", "0")));
    	}
		String username = (String)settings.value("username", "");
		String email = (String)settings.value("email", "");
		String name = (String)settings.value("name", "");
		String timezone = (String)settings.value("timezone", "");
		Integer privilege = 0;
		try {	
			privilege = new Integer((String)settings.value("privilege", "0"));			
		} catch (java.lang.ClassCastException e) {
			privilege = (Integer)settings.value("privilege", 0);
		}

		try {	
			String date = (String)settings.value("created", "0");
			user.setCreated(new Long(date));
			date = (String)settings.value("updated", "0");
			user.setUpdated(new Long(date));
			date = (String)settings.value("deleted", "0");
			user.setDeleted(new Long(date));
		} catch (java.lang.ClassCastException e) {
			Long date = (Long)settings.value("created", 0);
			user.setCreated(date);
			date = (Long)settings.value("updated", 0);
			user.setUpdated(date);
			date = (Long)settings.value("deleted", 0);
			user.setDeleted(date);
		}

		String shard = (String)settings.value("shard", "");
    	settings.endGroup();
    	
    	user.setUsername(username);
    	user.setEmail(email);
    	user.setName(name);
    	user.setTimezone(timezone);
    	PrivilegeLevel userLevel = PrivilegeLevel.findByValue(privilege);
    	user.setPrivilege(userLevel);
    	user.setShardId(shard);
    	return user;
    }
    
    public static void saveUserAttributes(UserAttributes attrib) {
    	settings.beginGroup("UserAttributes");
		settings.setValue("defaultLocationName", attrib.getDefaultLocationName());
		settings.setValue("defaultLatitude", attrib.getDefaultLocationName());
		settings.setValue("defaultLongitude", attrib.getDefaultLocationName());
		settings.setValue("incomingEmailAddress", attrib.getIncomingEmailAddress());
		settings.endGroup();
    }
    public static UserAttributes getUserAttributes() {
    	settings.beginGroup("UserAttributes");
    	UserAttributes attrib = new UserAttributes();
		attrib.setDefaultLocationName((String)settings.value("defaultLocationName",""));
		attrib.setDefaultLatitudeIsSet(false);
		attrib.setDefaultLongitudeIsSet(false);
		attrib.setIncomingEmailAddress((String)settings.value("incomingEmailAddress", ""));
		settings.endGroup();
		return attrib;
    }
    public static void saveUserAccounting(Accounting acc) {
    	settings.beginGroup("UserAccounting");
		settings.setValue("uploadLimit", acc.getUploadLimit());
		settings.setValue("uploadLimitEnd", acc.getUploadLimitEnd());
		settings.setValue("uploadLimitNextMonth", acc.getUploadLimitNextMonth());
		settings.setValue("premiumServiceStart", acc.getPremiumServiceStart());
		settings.setValue("nextPaymentDue", acc.getNextPaymentDue());
		settings.setValue("uploadAmount", acc.getUpdated());
		settings.endGroup();
    }
    public static long getUploadLimitEnd() {
    	Long limit;
    	settings.beginGroup("UserAccounting");
    	
    	// Upload limit
		try {
			String val  = (String)settings.value("uploadLimitEnd", "0");
			limit = new Long(val.trim());
		} catch (Exception e) {
			try {
				limit = (Long)settings.value("uploadLimitEnd", 0);
			} catch (Exception e1) {
				limit = new Long(0);
			}
		}
	
		// return value
    	settings.endGroup();
    	return limit;
    }
    public static void saveUploadAmount(long amount) {
    	settings.beginGroup("UserAccounting");
		settings.setValue("uploadAmount", amount);
		settings.endGroup();
   }
    public static long getUploadAmount() {
		long amt=0;
		settings.beginGroup("UserAccounting");
		try {
			String num = (String)settings.value("uploadAmount", "0");
			amt = new Long(num.trim());
		} catch (Exception e) {
			try {
				amt = (Integer)settings.value("uploadAmount", 0);
			} catch (Exception e1) {
				amt = 0;
			}
		}
		settings.endGroup();
		return amt;
    }
    public static void saveEvernoteUpdateCount(long amount) {
    	settings.beginGroup("UserAccounting");
		settings.setValue("updateCount", amount);
		settings.endGroup();
    }
    public static long getEvernoteUpdateCount() {
		long amt;
		settings.beginGroup("UserAccounting");
		try {
			String num = (String)settings.value("updateCount", new Long(0).toString());
			amt = new Long(num.trim());
		} catch (java.lang.ClassCastException e) {
			amt = 0;
		}
		settings.endGroup();
		return amt;
    }
    public static boolean isPremium() {
		int level;
		settings.beginGroup("User");
		try {
			String num = (String)settings.value("privilege", "1");
			level = new Integer(num.trim());
		} catch (java.lang.ClassCastException e) {
			try {
				level = (Integer)settings.value("privilege", 1);
			} catch (Exception e1) {
				level = 1;
			}
		}
		settings.endGroup();
		PrivilegeLevel userLevel = PrivilegeLevel.findByValue(level);
		if (userLevel == PrivilegeLevel.NORMAL)
			return false;
		return true;
		
   }
    public static long getUploadLimit() {
		settings.beginGroup("UserAccounting");
		long limit;
		try {
			String num = (String)settings.value("uploadLimit", new Long(0).toString());
			limit = new Long(num.trim());
		} catch (java.lang.ClassCastException e) {
			limit = 0;
		}
		settings.endGroup();
		return limit;
    }

    
    
    //****************************************************
    //****************************************************
    //** View settings.  Used to restore settings 
    //** when starting and to control how the program
    //** behaves.
    //****************************************************
    //****************************************************
    
    //* Get/Set if we should show a tray icon
    public static boolean showTrayIcon() {
		settings.beginGroup("General");
		try {
			String max = (String) settings.value("showTrayIcon", "false");
			settings.endGroup();
			if (!max.equalsIgnoreCase("true"))
				return false;
			else
				return true;   	
		} catch (java.lang.ClassCastException e) {
			Boolean value = (Boolean) settings.value("showTrayIcon", false);
			settings.endGroup();
			return value;
		}
    }
    public static void setShowTrayIcon(boolean val) {
		settings.beginGroup("General");
		if (val)
			settings.setValue("showTrayIcon", "true");
		else
			settings.setValue("showTrayIcon", "false");
		settings.endGroup();
    }
    
    // Get/Set window maximized when closed last
    public static boolean wasWindowMaximized() {
    	try {
			settings.beginGroup("General");
			String max = (String) settings.value("isMaximized", "true");
			settings.endGroup();
			if (!max.equalsIgnoreCase("true"))
				return false;
			return true;   	
		} catch (java.lang.ClassCastException e) {
			Boolean value = (Boolean) settings.value("isMaximized", true);
			settings.endGroup();
			return value;
		}
    }
    public static void saveWindowMaximized(boolean isMax) {
		settings.beginGroup("General");
		if (isMax)
			settings.setValue("isMaximized", "true");
		else
			settings.setValue("isMaximized", "false");
		settings.endGroup();
    }
    
    // Get/set currently viewed note Guid
    public static String getLastViewedNoteGuid() {
		settings.beginGroup("General");
		String guid = (String) settings.value("lastViewedNote", "");
		settings.endGroup();
		return guid;   	
    }
    public static void saveCurrentNoteGuid(String guid) {
		settings.beginGroup("General");
		if (guid != null)
			settings.setValue("lastViewedNote", guid);
		else
			settings.setValue("lastViewedNote", "");
		settings.endGroup();
    }
    
    // Get/Set the note column we are sorted on and the order
    public static void setSortColumn(int i) {
    	int view = Global.getListView();
		settings.beginGroup("General");
    	if (view == Global.View_List_Wide)
    		settings.setValue("sortColumn", i);
    	else
    		settings.setValue("sortColumn-Narrow", i);
		settings.endGroup();
    }
    public static int getSortColumn() {;
    String key;
	if (Global.getListView() == Global.View_List_Wide)
		key = "sortColumn";
	else
		key = "sortColumn-Narrow";

	settings.beginGroup("General");
	int order;	
	try {
		String val  = settings.value(key, new Integer(0)).toString();
		order = new Integer(val.trim());
	} catch (Exception e) {
		try {
			order = (Integer)settings.value(key, 0);
		} catch (Exception e1) {
		    order = 0;
		}
	}
	
	settings.endGroup();
	return order;
    }
    public static void setSortOrder(int i) {
    	int view = Global.getListView();
		settings.beginGroup("General");
    	if (view == Global.View_List_Wide)
    		settings.setValue("sortOrder", i);
    	else
    		settings.setValue("sortOrder-Narrow", i);
		settings.endGroup();
    }
    public static int getSortOrder() {
    	int view = Global.getListView();
		settings.beginGroup("General");
		String key;
    	if (view == Global.View_List_Wide)
    		key = "sortOrder";
   		else
   			key = "sortOrder-Narrow";

		int order;	
		try {
			String val  = settings.value(key, new Integer(0)).toString();
			order = new Integer(val.trim());
		} catch (Exception e) {
			try {
				order = (Integer)settings.value(key, 0);
			} catch (Exception e1) {
			    order = 0;
			}
		}
		
		settings.endGroup();
		return order;
    }
    
    // Should we automatically log in to Evernote when starting?
    public static boolean automaticLogin() {
    	try {
    		settings.beginGroup("General");
    		String text = (String)settings.value("automaticLogin", "false");
    		settings.endGroup();
    		if (text.equalsIgnoreCase("true"))
    			return true;
    		else
    			return false;		
    	} catch (java.lang.ClassCastException e) {
    		Boolean value = (Boolean) settings.value("automaticLogin", false);
    		settings.endGroup();
    		return value;
    	}
    }
    public static void setAutomaticLogin(boolean val) {
		settings.beginGroup("General");
		if (val)
			settings.setValue("automaticLogin", "true");
		else
			settings.setValue("automaticLogin", "false");
		settings.endGroup();
    }
    
    // Should it save the Evernote password?
    public static boolean rememberPassword() {
    	try {
			settings.beginGroup("General");
			String text = (String)settings.value("rememberPassword", "false");
			settings.endGroup();
			if (text.equalsIgnoreCase("true"))
				return true;
			else
				return false;	
		} catch (java.lang.ClassCastException e) {
			Boolean value = (Boolean) settings.value("rememberPassword", false);
			settings.endGroup();
			return value;
		}
    }
    public static void setRememberPassword(boolean val) {
		settings.beginGroup("General");
		if (val)
			settings.setValue("rememberPassword", "true");
		else
			settings.setValue("rememberPassword", "false");
		settings.endGroup();
    }

    // Get/set the Evernote server Url.  
    public static void setServer(String server) {
		settings.beginGroup("General");
		settings.setValue("server", server);
		settings.endGroup();    	
    }
    public static String getServer() {
		settings.beginGroup("General");
		String text = (String)settings.value("server", "www.evernote.com");
		if (text.equals("www.evernote.com")) {
			userStoreUrl = "https://www.evernote.com/edam/user";
		    noteStoreUrlBase = "www.evernote.com/edam/note/";	
		} else {
			userStoreUrl = "https://sandbox.evernote.com/edam/user";
			noteStoreUrlBase = "sandbox.evernote.com/edam/note/";
		}
		settings.endGroup();
//		if (isPremium())
			noteStoreUrlBase = "https://" + noteStoreUrlBase;
//		else
//			noteStoreUrlBase = "http://" + noteStoreUrlBase;
		return text;
    }

    // Get/Set if we should disable uploads to Evernote
    public static boolean disableUploads() {
    	settings.beginGroup("General");
    	try {
    		String text = (String)settings.value("disableUploads", "false");
    		settings.endGroup();
    		if (text.equalsIgnoreCase("true"))
    			return true;
    		else
    			return false;
		} catch (java.lang.ClassCastException e) {
			Boolean value = (Boolean) settings.value("disableUploads", false);
			settings.endGroup();
			return value;
		}
    }
    public static void setDisableUploads(boolean val) {
		settings.beginGroup("General");
		if (val)
			settings.setValue("disableUploads", "true");
		else
			settings.setValue("disableUploads", "false");
		settings.endGroup();
		disableUploads = val;
    }
 
    // Should we view PDF documents inline?
    public static boolean pdfPreview() {
		settings.beginGroup("General");
		try {
			String text = (String)settings.value("pdfPreview", "true");
			settings.endGroup();
			if (text.equalsIgnoreCase("true"))
				return true;
			else
				return false;
		} catch (java.lang.ClassCastException e) {
			Boolean value = (Boolean) settings.value("pdfPreview", true);
			settings.endGroup();
			return value;
		}
    }
    public static void setPdfPreview(boolean val) {
		settings.beginGroup("General");
		if (val)
			settings.setValue("pdfPreview", "true");
		else
			settings.setValue("pdfPreview", "false");
		settings.endGroup();
    }
    
    // When creating a new note, should it inherit tags that are currently selected?
    public static boolean newNoteWithSelectedTags() {
		settings.beginGroup("General");
		try {
			String text = (String)settings.value("newNoteWithSelectedTags", "false");
			settings.endGroup();
			if (text.equalsIgnoreCase("true"))
				return true;
			else
				return false;
		} catch (java.lang.ClassCastException e) {
			Boolean value = (Boolean) settings.value("newNoteWithSelectedTags", false);
			settings.endGroup();
			return value;
		}
    }
    public static void setNewNoteWithSelectedTags(boolean val) {
		settings.beginGroup("General");
		if (val)
			settings.setValue("newNoteWithSelectedTags", "true");
		else
			settings.setValue("newNoteWithSelectedTags", "false");
		settings.endGroup();
    }
    
    // Minimum weight for text OCRed from Evernote. Anything below this
    // Won't be shown to the user when they search
    public static void setRecognitionWeight(int len) {
		settings.beginGroup("General");
		settings.setValue("recognitionWeight", len);
		settings.endGroup();    	
    }
    public static int getRecognitionWeight() {
		settings.beginGroup("General");
		Integer len;
		try {
			len = (Integer)settings.value("recognitionWeight", 30);
		} catch (Exception e) {
			len = 80;
		}
		settings.endGroup();
		return len;
    }
    
    // get/set current debug message level
    public static String getMessageLevel() {
		settings.beginGroup("Debug");
		String text = (String)settings.value("messageLevel", "Low");
		settings.endGroup();
		setMessageLevel(text);
		return text;
    }
    public static void setDateFormat(String format) {
		settings.beginGroup("General");
		settings.setValue("dateFormat", format);
		settings.endGroup();    	
    }
    
    // Get/Set user date/time formats
    public static String getDateFormat() {
		settings.beginGroup("General");
		String text = (String)settings.value("dateFormat", "MM/dd/yyyy");
		settings.endGroup();
		return text;
    }
    public static void setTimeFormat(String format) {
		settings.beginGroup("General");
		settings.setValue("timeFormat", format);
		settings.endGroup();    	
    }
    public static String getTimeFormat() {
		settings.beginGroup("General");
		String text = (String)settings.value("timeFormat", "HH:mm:ss");
		settings.endGroup();
		return text;
    }
    
    // How often should we sync with Evernote?
    public static String getSyncInterval() {
		settings.beginGroup("General");
		String text = (String)settings.value("syncInterval", "15 minutes");
		settings.endGroup();
		return text;    	
    }
    public static void setSyncInterval(String format) {
		settings.beginGroup("General");
		settings.setValue("syncInterval", format);
		settings.endGroup();    	
    }
    
    // Get/Set the width of columns and their position for the 
    // next start.
    public static void setColumnWidth(String col, int width) {
    	if (Global.getListView() == Global.View_List_Wide)
    		settings.beginGroup("ColumnWidths");
    	else 
    		settings.beginGroup("ColumnWidths-Narrow");
   		settings.setValue(col, width);
   		settings.endGroup();
   	}
    public static int getColumnWidth(String col) {
    	int view = Global.getListView();
    	if (view == Global.View_List_Wide)
    		settings.beginGroup("ColumnWidths");
    	else
    		settings.beginGroup("ColumnWidths-Narrow");
		Integer width;
		try {
			String val  = (String)settings.value(col, "0");
			width = new Integer(val.trim());
		} catch (Exception e) {
			try {
				width = (Integer)settings.value(col, 0);
			} catch (Exception e1) {
				width = 0;
			}
		}
		settings.endGroup();
		return width;
    }
    public static void setColumnPosition(String col, int width) {
    	if (Global.getListView() == Global.View_List_Wide)
    		settings.beginGroup("ColumnPosition");
    	else
    		settings.beginGroup("ColumnPosition-Narrow");
		settings.setValue(col, width);
		settings.endGroup();
    }
    public static int getColumnPosition(String col) {
    	if (Global.getListView() == Global.View_List_Wide)
    		settings.beginGroup("ColumnPosition");
    	else
    		settings.beginGroup("ColumnPosition-Narrow");
		Integer width;
		try {
			String val  = (String)settings.value(col, "-1");
			width = new Integer(val.trim());
		} catch (Exception e) {
			try {
				width = (Integer)settings.value(col, 0);
			} catch (Exception e1) {
				width = 0;
			}
		}
		settings.endGroup();
		return width;
    }
    
    // Ping the user when they try to delete or just do it.
    public static boolean verifyDelete() {
		settings.beginGroup("General");
		try {
			String text = (String)settings.value("verifyDelete", "true");
			settings.endGroup();
			if (text.equalsIgnoreCase("true"))
				return true;
			else
				return false;
		} catch (java.lang.ClassCastException e) {
			Boolean value = (Boolean) settings.value("verifyDelete", true);
			settings.endGroup();
			return value;
		}
    }
    public static void setVerifyDelete(boolean val) {
		settings.beginGroup("General");
		if (val)
			settings.setValue("verifyDelete", "true");
		else
			settings.setValue("verifyDelete", "false");
		settings.endGroup();
    }
    
    // Should it start minimized?
    public static boolean startMinimized() {
		settings.beginGroup("General");
		try {
			String text = (String)settings.value("startMinimized", "false");
			settings.endGroup();
			if (text.equalsIgnoreCase("true"))
				return true;
			else
				return false;
		} catch (java.lang.ClassCastException e) {
			Boolean value = (Boolean) settings.value("startMinimized", false);
			settings.endGroup();
			return value;
		}
    }
    public static void setStartMinimized(boolean val) {
		settings.beginGroup("General");
		if (val)
			settings.setValue("startMinimized", "true");
		else
			settings.setValue("startMinimized", "false");
		settings.endGroup();
    }
    
    // Should we upload the content of any deleted notes
    public static boolean synchronizeDeletedContent() {
		settings.beginGroup("General");
		try {
			String text = (String)settings.value("syncDeletedContent", "false");
			settings.endGroup();
			if (text.equalsIgnoreCase("true"))
				return true;
			else
				return false;
		} catch (java.lang.ClassCastException e) {
			Boolean value = (Boolean) settings.value("syncDeletedContent", false);
			settings.endGroup();
			return value;
		}
    }	
    public static void setSynchronizeDeletedContent(boolean val) {
		settings.beginGroup("General");
		if (val)
			settings.setValue("syncDeletedContent", "true");
		else
			settings.setValue("syncDeletedContent", "false");
		settings.endGroup();
    }
    
    // Is a section of the window visible?  Used to hide things people don't
    // want to see.
    public static boolean isWindowVisible(String window) {
		settings.beginGroup("WindowsVisible");
		try {
			String defaultValue = "true";
			if (window.equalsIgnoreCase("noteInformation"))
				defaultValue = "false";
			String text = (String)settings.value(window, defaultValue);
			settings.endGroup();
			if (text.equalsIgnoreCase("true"))
				return true;
		else
			return false;		
		} catch (java.lang.ClassCastException e) {
			boolean defaultValue = true;
			if (window.equalsIgnoreCase("noteInformation"))
				defaultValue = false;
			Boolean value = (Boolean) settings.value("showTrayIcon", defaultValue);
			settings.endGroup();
			return value;
		}
    }
    public static void saveWindowVisible(String window, boolean val) {
		settings.beginGroup("WindowsVisible");
		if (val)
			settings.setValue(window, "true");
		else
			settings.setValue(window, "false");
		settings.endGroup();
    }
    
    // Is a list in the column in the note list visible?  
    public static boolean isColumnVisible(String window) {
    	String defaultValue = "true";
    	int view = Global.getListView();
    	if (Global.getListView() == Global.View_List_Wide)
    		settings.beginGroup("ColumnsVisible");
    	else
    		settings.beginGroup("ColumnsVisible-Narrow"); 
		if (window.equalsIgnoreCase("thumbnail") && view == Global.View_List_Wide)
			defaultValue = "false";
		if (window.equalsIgnoreCase("thumbnail"))
			defaultValue = "false";
		if (window.equalsIgnoreCase("Guid"))
			defaultValue = "false";
		try {
			String text = (String)settings.value(window, defaultValue);
			settings.endGroup();
			if (text.equalsIgnoreCase("true"))
				return true;
			else
				return false;
		} catch (java.lang.ClassCastException e) {
			boolean defBool = false;
			if (window.equalsIgnoreCase("true"))
				defBool = true;
			else
				defBool = false;
			Boolean value = (Boolean) settings.value(window, defBool);
			settings.endGroup();
			return value;
		}
    }
    public static void saveColumnVisible(String column, boolean val) {
    	if (Global.getListView() == Global.View_List_Wide)
    		settings.beginGroup("ColumnsVisible");
    	else
    		settings.beginGroup("ColumnsVisible-Narrow");    		
		if (val)
			settings.setValue(column, "true");
		else
			settings.setValue(column, "false");
		settings.endGroup();
    }
    
    // Is a particular editor button visible?
    public static boolean isEditorButtonVisible(String window) {
		settings.beginGroup("EditorButtonsVisible");
		try {
			String text = (String)settings.value(window, "true");
			settings.endGroup();
			if (text.equalsIgnoreCase("true"))
				return true;
			else
				return false;
		} catch (java.lang.ClassCastException e) {
			Boolean value = (Boolean) settings.value(window, true);
			settings.endGroup();
			return value;
		}
    }
    public static void saveEditorButtonsVisible(String column, boolean val) {
		settings.beginGroup("EditorButtonsVisible");
		if (val)
			settings.setValue(column, "true");
		else
			settings.setValue(column, "false");
		settings.endGroup();
    }
    
    // Should the test fixes be enabled
    public static boolean enableCarriageReturnFix() {
    	try {
    		settings.beginGroup("Debug");
    		String text = (String)settings.value("enableCarriageReturnFix", "false");
    		settings.endGroup();
    		if (text.equalsIgnoreCase("true"))
    			return true;
    		else
    			return false;
		} catch (java.lang.ClassCastException e) {
			Boolean value = (Boolean) settings.value("enableCarriageReturnFix", false);
			settings.endGroup();
			return value;
		}
    }
    public static void saveCarriageReturnFix(boolean val) {
		settings.beginGroup("Debug");
		if (val)
			settings.setValue("enableCarriageReturnFix", "true");
		else
			settings.setValue("enableCarriageReturnFix", "false");
		settings.endGroup();
    }
    public static boolean enableHtmlEntitiesFix() {
    	try {
    		settings.beginGroup("Debug");
    		String text = (String)settings.value("enableHtmlEntitiesFix", "false");
    		settings.endGroup();
    		if (text.equalsIgnoreCase("true"))
    			return true;
    		else
    			return false;
		} catch (java.lang.ClassCastException e) {
			Boolean value = (Boolean) settings.value("enableHtmlEntitiesFix", false);
			settings.endGroup();
			return value;
		}
    }
    public static void saveHtmlEntitiesFix(boolean val) {
		settings.beginGroup("Debug");
		if (val)
			settings.setValue("enableHtmlEntitiesFix", "true");
		else
			settings.setValue("enableHtmlEntitiesFix", "false");
		settings.endGroup();
    }

//    public static void setIndexThreads(int val) {
//		settings.beginGroup("General");
//		settings.setValue("indexThreads", val);
//		settings.endGroup();
//   }
//    public static int getIndexThreads() {
//		settings.beginGroup("General");
//		Integer threads;
//		try {
//			String val  = (String)settings.value("indexThreads", "1");
//			threads = new Integer(val.trim());
//		} catch (Exception e) {
//			try {
//				threads = (Integer)settings.value("indexThreads", 1);
//			} catch (Exception e1) {
//				threads = 1;
//			}
//		}
//		settings.endGroup();
//		threads = 1;
//		return threads;
    
    // Get/Set text zoom factor
//   }
    public static void setZoomFactor(double val) {
		settings.beginGroup("General");
		settings.setValue("zoomFactor", val);
		settings.endGroup();
    }
    public static double getZoomFactor() {
		settings.beginGroup("General");
		Double threads;
		try {
			String val  = (String)settings.value("zoomFactor", "1.0");
			threads = new Double(val.trim());
		} catch (Exception e) {
			try {
				threads = (Double)settings.value("zoomFactor", 1.0);
			} catch (Exception e1) {
				threads = new Double(1);
			}
		}
		settings.endGroup();
		return threads;
    }
    public static void setTextSizeMultiplier(double val) {
		settings.beginGroup("General");
		settings.setValue("textMultiplier", val);
		settings.endGroup();
    }
    public static double getTextSizeMultiplier() {
		settings.beginGroup("General");
		Double threads;
		try {
			String val  = (String)settings.value("textMultiplier", "1");
			threads = new Double(val.trim());
		} catch (Exception e) {
			try {
				threads = (Double)settings.value("textMultiplier", 1);
			} catch (Exception e1) {
				threads = new Double(1);
			}
		}
		settings.endGroup();
		return threads;
    }
    
    
    // Should we mimic Evernote and restrict the notebooks selected?
    public static boolean getMimicEvernoteInterface() {
		settings.beginGroup("General");
		try {
			String text = (String)settings.value("mimicEvernoteInterface", "true");
			settings.endGroup();
			if (text.equalsIgnoreCase("true"))
				return true;
			else
				return false;
		} catch (java.lang.ClassCastException e) {
			Boolean value = (Boolean) settings.value("mimicEvernoteInterface", true);
			settings.endGroup();
			return value;
		}
    }
    public static void setMimicEvernoteInterface(boolean value) {
    	settings.beginGroup("General");
    	if (value)
    		settings.setValue("mimicEvernoteInterface", "true");
    	else
    		settings.setValue("mimicEvernoteInterface", "false"); 
    	settings.endGroup();
    }
    
    
    // Synchronize with Evernote when closing?
    public static boolean synchronizeOnClose() {
		settings.beginGroup("General");
		try {
			String text = (String)settings.value("synchronizeOnClose", "false");
			settings.endGroup();
			if (text.equalsIgnoreCase("true"))
				return true;
			else
				return false;
		} catch (java.lang.ClassCastException e) {
			Boolean value = (Boolean) settings.value("synchronizeOnClose", false);
			settings.endGroup();
			return value;
		}
    }
    public static void setSynchronizeOnClose(boolean val) {
		settings.beginGroup("General");
		if (val)
			settings.setValue("synchronizeOnClose", "true");
		else
			settings.setValue("synchronizeOnClose", "false");
		settings.endGroup();
    }

    // Get/set the database version.  Not really used any more, but kept
    // for compatibility.
    public static void setDatabaseVersion(String version) {
		settings.beginGroup("General");
		settings.setValue("databaseVersion", version);
		settings.endGroup();
    }
    public static String getDatabaseVersion() {
		settings.beginGroup("General");
		String val  = (String)settings.value("databaseVersion", "0.70");
		settings.endGroup();
		return val;
    }

    // Get the URL (full path) of the main database
    public static String getDatabaseUrl() {
		settings.beginGroup("General");
		String val  = (String)settings.value("DatabaseURL", "");
		settings.endGroup();
		if (val.equals(""))
			val = "jdbc:h2:"+Global.getFileManager().getDbDirPath(Global.databaseName);
		return val;
    }
    
    // get the url (full path) of the searchable word database
    public static String getIndexDatabaseUrl() {
		settings.beginGroup("General");
		String val  = (String)settings.value("IndexDatabaseURL", "");
		settings.endGroup();
		if (val.equals(""))
			val = "jdbc:h2:"+Global.getFileManager().getDbDirPath(Global.indexDatabaseName);
		return val;
    }
    
    // Get the url (full path) of the attachment database
    public static String getResourceDatabaseUrl() {
		settings.beginGroup("General");
		String val  = (String)settings.value("ResourceDatabaseURL", "");
		settings.endGroup();
		if (val.equals(""))
			val = "jdbc:h2:"+Global.getFileManager().getDbDirPath(Global.resourceDatabaseName);
		return val;
    }
    public static void setDatabaseUrl(String value) {
		settings.beginGroup("General");
		settings.setValue("DatabaseURL", value);
		settings.endGroup();
    }
    public static void setIndexDatabaseUrl(String value) {
		settings.beginGroup("General");
		settings.setValue("IndexDatabaseURL", value);
		settings.endGroup();
    }
    public static void setResourceDatabaseUrl(String value) {
		settings.beginGroup("General");
		settings.setValue("ResourceDatabaseURL", value);
		settings.endGroup();
    }
    public static String getDatabaseUserid() {
		settings.beginGroup("General");
		String val  = (String)settings.value("databaseUserid", "");
		settings.endGroup();
		return val;
    }
    public static String getDatabaseUserPassword() {
		settings.beginGroup("General");
		String val  = (String)settings.value("databaseUserPassword", "");
		settings.endGroup();
		return val;
    }
    
    // get/Set the style sheet and the palette to control the look & feel
    public static void setStyle(String style) {
		settings.beginGroup("General");
		settings.setValue("style", style);
		settings.endGroup();
    }
    public static String getStyle() {
		settings.beginGroup("General");
		String val  = (String)settings.value("style", "Cleanlooks");
		settings.endGroup();
		return val;
    }
    public static boolean useStandardPalette() {
		settings.beginGroup("General");
		try {
			String text = (String)settings.value("standardPalette", "true");
			settings.endGroup();
			if (text.equalsIgnoreCase("true"))
				return true;
			else
				return false;
		} catch (java.lang.ClassCastException e) {
			Boolean value = (Boolean) settings.value("standardPalette", true);
			settings.endGroup();
			return value;
		}
    }
    public static void setStandardPalette(boolean val) {
		settings.beginGroup("General");
		if (val)
			settings.setValue("standardPalette", "true");
		else
			settings.setValue("standardPalette", "false");
		settings.endGroup();
    }
    
    // Set the amount of time to wait between indexing
    // Get/Set interval when the index thread wakes up.
    public static void setIndexThreadSleepInterval(int sleep) {
		settings.beginGroup("General");
		settings.setValue("IndexThreadSleepInterval", sleep);
		settings.endGroup();
    }
    public static int getIndexThreadSleepInterval() {
		settings.beginGroup("General");
		Integer sleep;
		try {
			String val  = (String)settings.value("IndexThreadSleepInterval", "300");
			sleep = new Integer(val.trim());
		} catch (Exception e) {
			try {
				sleep = (Integer)settings.value("IndexThreadSleepInterval", 0);
			} catch (Exception e1) {
				sleep = 300;
			}
		}
		settings.endGroup();
		return sleep;
    }
    
    
    // Get/Set a window state for later restoring
    public static void saveState(String name, QByteArray state) {
    	int view = Global.getListView();
    	if (view == Global.View_List_Narrow)
    		name = name +"Narrow";
		settings.beginGroup("SaveState");
		settings.setValue(name, state);
		settings.endGroup();
    }
    
    public static QByteArray restoreState(String name) {
    	int view = Global.getListView();
    	if (view == Global.View_List_Narrow)
    		name = name +"Narrow";
		settings.beginGroup("SaveState");
		QByteArray state = (QByteArray)settings.value(name);
		settings.endGroup();
		return state;
    }
    public static void saveGeometry(String name, QByteArray state) {
    	int view = Global.getListView();
    	if (view == Global.View_List_Narrow)
    		settings.beginGroup("SaveGeometryNarrow");
    	else
    		settings.beginGroup("SaveGeometry");
		settings.setValue(name, state);
		settings.endGroup();
    }
    
    public static QByteArray restoreGeometry(String name) {
    	int view = Global.getListView();
    	if (view == Global.View_List_Narrow)
    		settings.beginGroup("SaveGeometryNarrow");
    	else
    		settings.beginGroup("SaveGeometry");
		QByteArray state = (QByteArray)settings.value(name);
		settings.endGroup();
		return state;
    }
    
    
    // Set how often to do an automatic save
    public static void setAutoSaveInterval(int interval) {
		settings.beginGroup("General");
		settings.setValue("autoSaveInterval", interval);
		settings.endGroup();
    }
    public static int getAutoSaveInterval() {
		settings.beginGroup("General");
		Integer value;
		try {
			String val  = (String)settings.value("autoSaveInterval", "5");
			value = new Integer(val.trim());
		} catch (Exception e) {
			try {
				value = (Integer)settings.value("autoSaveInterval", 5);
			} catch (Exception e1) {
				value = 5;
			}
		}
		settings.endGroup();
		return value;
    }
     
    // Add an invalid attribute & element to the database so we don't bother parsing it in the future
    // These values we automatically remove from any note.
    // Add invalid attributes
    public static void addInvalidAttribute(String element, String attribute) {
    	
		List<String> attributes = invalidAttributes.get(element);
		if (attributes != null) {
			for (int i=0; i<attributes.size(); i++)
				if (attribute.equalsIgnoreCase(attributes.get(i))) {
					return;
			}
    	}
    	
    	ArrayList<String> attributeList;
    	if (!invalidAttributes.containsKey(element)) {
    		attributeList = new ArrayList<String>();
    		attributeList.add(attribute);
    		invalidAttributes.put(element, attributeList);
    	}
    	else {
    		attributeList = invalidAttributes.get(element);
    		attributeList.add(attribute);
    		invalidAttributes.put(element,attributeList);
    	}
    }
   
    // Add invalid attributes
    public static void addInvalidElement(String element) {
		for (int i=0; i<invalidElements.size(); i++) {
			if (element.equalsIgnoreCase(invalidElements.get(i)))
				return;
		}
    	invalidElements.add(element);
    }
    
    // Get/Set proxy information
    // Proxy settings
    public static String getProxyValue(String key) {
		settings.beginGroup("Proxy");
		String val  = (String)settings.value(key, "");
		settings.endGroup();
		return val;
    }
    public static void setProxyValue(String key, String value) {
		settings.beginGroup("Proxy");
		settings.setValue(key, value);
		settings.endGroup();
    }
    
    // Change a byte array to a hex string
    // Convert a byte array to a hex string
	public static String byteArrayToHexString(byte data[]) {
		StringBuffer buf = new StringBuffer();
	    for (byte element : data) {
	    	int halfbyte = (element >>> 4) & 0x0F;
	        int two_halfs = 0;
	        do {
		       	if ((0 <= halfbyte) && (halfbyte <= 9))
		               buf.append((char) ('0' + halfbyte));
		           else
		           	buf.append((char) ('a' + (halfbyte - 10)));
		       	halfbyte = element & 0x0F;
	        } while(two_halfs++ < 1);
	    }
	    return buf.toString();		
	}

    
	// Get/Set spelling settings
	public static boolean getSpellSetting(String value) {
		settings.beginGroup("Spell");
		String text = (String)settings.value(value, "");
		settings.endGroup();
		if (text.equalsIgnoreCase("true"))
			return true;
		if (text.equalsIgnoreCase("false"))
			return false;
		if (value.equalsIgnoreCase(Configuration.SPELL_IGNOREDIGITWORDS))
			return true;
		if (value.equalsIgnoreCase(Configuration.SPELL_IGNOREINTERNETADDRESSES))
			return true;
		if (value.equalsIgnoreCase(Configuration.SPELL_IGNOREUPPERCASE))
			return true;
		if (value.equalsIgnoreCase(Configuration.SPELL_IGNORESENTENCECAPITALIZATION))
			return true;
		return false;
    }
    public static void setSpellSetting(String setting, boolean val) {
		settings.beginGroup("Spell");
		if (val)
			settings.setValue(setting, "true");
		else
			settings.setValue(setting, "false");
		settings.endGroup();
    }
	
	// Get/Set how we should display tags (color them, hide unused, or do nothing)
	// What to do with inactive tags?
	public static String tagBehavior() {
		settings.beginGroup("General");
		String text = (String)settings.value("tagBehavior", "DoNothing");
		settings.endGroup();
		return text;
	}
	// What to do with inactive tags?
	public static void setTagBehavior(String value) {
		settings.beginGroup("General");
		settings.setValue("tagBehavior", value);
		settings.endGroup();
	}

    
	// Should the toolbar be visible?
	public static boolean isToolbarButtonVisible(String window) {
		settings.beginGroup("ToolbarButtonsVisible");
		try {
			String text = (String)settings.value(window, "true");
			settings.endGroup();
			if (text.equalsIgnoreCase("true"))
				return true;
			else
				return false;	
		} catch (java.lang.ClassCastException e) {
			Boolean value = (Boolean) settings.value(window, true);
			settings.endGroup();
			return value;
		}
    }
    public static void saveToolbarButtonsVisible(String column, boolean val) {
		settings.beginGroup("ToolbarButtonsVisible");
		if (val)
			settings.setValue(column, "true");
		else
			settings.setValue(column, "false");
		settings.endGroup();
    }
	
    // Are thumbnails enabled?
    
    public static boolean enableThumbnails() {
		settings.beginGroup("Debug");
		try {
			String text = (String)settings.value("thumbnails", "true");
			settings.endGroup();
			if (text.equalsIgnoreCase("true"))
				return true;
			else
				return false;
		} catch (java.lang.ClassCastException e) {
			Boolean value = (Boolean) settings.value("thumbnails", true);
			settings.endGroup();
			return value;
		}
    }
    public static void setEnableThumbnails(boolean val) {
		settings.beginGroup("Debug");
		if (val)
			settings.setValue("thumbnails", "true");
		else
			settings.setValue("thumbnails", "false");
		settings.endGroup();
    }
	
    // Trace used for performance tuning.  Not normally used in production.
	// Print date/time.  Used mainly for performance tracing
	public static void trace(boolean resetInterval) {
		String fmt = "MM/dd/yy HH:mm:ss.SSSSSS";
		String dateTimeFormat = new String(fmt);
		SimpleDateFormat simple = new SimpleDateFormat(dateTimeFormat);
		Calendar cal = Calendar.getInstance();
		if (intervalTraceTime == null) 
			intervalTraceTime = Calendar.getInstance();
		if (startTraceTime == null)
			startTraceTime = Calendar.getInstance();
		
		float interval = (cal.getTimeInMillis() - intervalTraceTime.getTimeInMillis());
		float total = (cal.getTimeInMillis() - startTraceTime.getTimeInMillis());
		
//		if (interval > 00.0) {
			StackTraceElement[] exceptions = Thread.currentThread().getStackTrace();
			System.out.println("------------------------------------------");

			System.out.println("Date/Time " +simple.format(cal.getTime()));
			System.out.format("Interval Time: %-10.6f%n", interval);
			System.out.format("Total Time: %-10.6f%n", total);
			for (int i=2; i<5 && i<exceptions.length; i++) {
				System.out.println(exceptions[i]);
			}
//		}
		if (resetInterval)
			intervalTraceTime = cal;
	}
	public static void traceReset() {
		intervalTraceTime = null;
		startTraceTime = null;
	}

    
	// Get the FileManager class to manage local files & directories
	public static FileManager getFileManager() {
        return fileManager;
    }
	
	// Should the note editor be disabled?
    public static boolean getDisableViewing() {
        return disableViewing;
    }

    //**********************
    //* Thumbnail zoom level
    //**********************
    public static int calculateThumbnailZoom(String content) {
    	int zoom = 1;
		if (content.indexOf("application/pdf") == -1) {
			if (content.indexOf("image/") == -1) {
				String text =  StringEscapeUtils.unescapeHtml4(content.replaceAll("\\<.*?\\>", ""));
				zoom = 2;
				if (text.length() < 500) 
					zoom = 2;
				if (text.length() < 250)
					zoom = 3;
				if (text.length() < 100)
					zoom = 4;
				if (text.length() < 50)
					zoom = 5;
				if (text.length() < 10)
					zoom = 6;
			}
		}
		return zoom;
    }
    
    //**********************
    //* List View settings 
    //**********************
    public static void setListView(int view) {
		settings.beginGroup("General");
		settings.setValue("listView", view);
		settings.endGroup();
    }
    public static int getListView() {
		settings.beginGroup("General");
		Integer value;
		try {
			String val  = (String)settings.value("listView", View_List_Wide);
			value = new Integer(val.trim());
		} catch (Exception e) {
			try {
				value = (Integer)settings.value("listView", View_List_Wide);
			} catch (Exception e1) {
				value = View_List_Wide;
			}
		}
		settings.endGroup();
		return value;
    }

    
    
    //*******************
    // Font Settings
    //*******************
    public static boolean overrideDefaultFont() {
		settings.beginGroup("Font");
		try {
			String text = (String)settings.value("overrideFont", "false");
			settings.endGroup();
			if (text.equalsIgnoreCase("true"))
				return true;
			else
				return false;	
		} catch (java.lang.ClassCastException e) {
			Boolean value = (Boolean) settings.value("overrideFont", false);
			settings.endGroup();
			return value;
		}

    }
    
    //****************************************************
    // Get/Set the default font settings for a new note
    //****************************************************
    public static void setOverrideDefaultFont(boolean value) {
		settings.beginGroup("Font");
		settings.setValue("overrideFont", value);
		settings.endGroup();	
    }
    public static String getDefaultFont() {
		settings.beginGroup("Font");
		String val  = (String)settings.value("font", "");
		settings.endGroup();
		return val;
    }
    public static void setDefaultFont(String value) {
		settings.beginGroup("Font");
		settings.setValue("font", value);
		settings.endGroup();
    }
    public static String getDefaultFontSize() {
		settings.beginGroup("Font");
		String val  = (String)settings.value("fontSize", "");
		settings.endGroup();
		return val;
    }
    public static void setDefaultFontSize(String value) {
		settings.beginGroup("Font");
		settings.setValue("fontSize", value);
		settings.endGroup();
    }
    
    
    //*******************************************
    // Override the close & minimize instead.
    //*******************************************
    public static boolean minimizeOnClose() {
		settings.beginGroup("General");
		try {
			String text = (String)settings.value("minimizeOnClose", "false");
			settings.endGroup();
			if (text.equalsIgnoreCase("true"))
				return true;
			else
				return false;
		} catch (java.lang.ClassCastException e) {
			Boolean value = (Boolean) settings.value("minimizeOnClose", false);
			settings.endGroup();
			return value;
		}
    }
    public static void setMinimizeOnClose(boolean value) {
		settings.beginGroup("General");
		settings.setValue("minimizeOnClose", value);
		settings.endGroup();	
    }

    //*********************************
    // Check version information
    //*********************************
    public static boolean checkVersionUpgrade() {
		settings.beginGroup("Upgrade");
		try {
			String text = (String)settings.value("checkForUpdates", "true");
			settings.endGroup();
			if (text.equalsIgnoreCase("true"))
				return true;
			else
				return false;
		} catch (java.lang.ClassCastException e) {
			Boolean value = (Boolean) settings.value("checkForUpdates", true);
			settings.endGroup();
			return value;
		}
    }
    public static void setCheckVersionUpgrade(boolean value) {
		settings.beginGroup("Upgrade");
		settings.setValue("checkForUpdates", value);
		settings.endGroup();	
    }
    public static String getUpdatesAvailableUrl() {
		settings.beginGroup("Upgrade");
		String text = (String)settings.value("avialableUrl", "http://nevernote.sourceforge.net/versions.txt");
		settings.endGroup();	
		return text;
    }
    public static String getUpdateAnnounceUrl() {
		settings.beginGroup("Upgrade");
		String text = (String)settings.value("announceUrl", "http://nevernote.sourceforge.net/upgrade.html");
		settings.endGroup();	
		return text;
    }
    
    //*******************
    // Index settings
    //*******************
    // Set/Get if we should index the text of a note
    public static boolean indexNoteBody() {
		settings.beginGroup("Index");
		try {
			String value = (String)settings.value("indexNoteBody", "true");
			settings.endGroup();
			if (value.equals("true"))
				return true;
			else
				return false;
		} catch (java.lang.ClassCastException e) {
			Boolean value = (Boolean) settings.value("indexNoteBody", true);
			settings.endGroup();
			return value;
		}
    }
    public static void setIndexNoteTitle(boolean value) {
		settings.beginGroup("Index");
		settings.setValue("indexNoteTitle", value);
		settings.endGroup();	
    }
    // Set/Get if we should index the title of a note
    public static boolean indexNoteTitle() {
		settings.beginGroup("Index");
		try {
			String value = (String)settings.value("indexNoteTitle", "true");
			settings.endGroup();
			if (value.equals("true"))
				return true;
			else
				return false;
		} catch (java.lang.ClassCastException e) {
			Boolean value = (Boolean) settings.value("indexNoteTitle", true);
			settings.endGroup();
			return value;
		}
    }
    public static void setIndexNoteBody(boolean value) {
		settings.beginGroup("Index");
		settings.setValue("indexNoteBody", value);
		settings.endGroup();	
    }
    // Set/Get if we should index any attachments
    public static boolean indexAttachmentsLocally() {
		settings.beginGroup("Index");
		try {
			String value = (String)settings.value("indexAttachmentsLocally", "true");
			settings.endGroup();
			if (value.equals("true"))
				return true;
			else
				return false;
		} catch (java.lang.ClassCastException e) {
			Boolean value = (Boolean) settings.value("indexAttachmentsLocally", true);
			settings.endGroup();
			return value;
		}
    }
    public static void setIndexImageRecognition(boolean value) {
		settings.beginGroup("Index");
		settings.setValue("indexImageRecognition", value);
		settings.endGroup();	
    }
    public static boolean indexImageRecognition() {
		settings.beginGroup("Index");
		try {
			String value = (String)settings.value("indexImageRecognition", "true");
			settings.endGroup();
			if (value.equals("true"))
				return true;
			else
				return false;
		} catch (java.lang.ClassCastException e) {
			Boolean value = (Boolean) settings.value("indexImageRecognition", true);
			settings.endGroup();
			return value;
		}
    }
    public static void setIndexAttachmentsLocally(boolean value) {
		settings.beginGroup("Index");
		settings.setValue("indexAttachmentsLocally", value);
		settings.endGroup();	
    }
    // Get/Set characters that shouldn't be removed from a word
    public static String getSpecialIndexCharacters() {
		settings.beginGroup("Index");
		String text = (String)settings.value("specialCharacters", "");
		settings.endGroup();	
		return text;
    }
    public static void setSpecialIndexCharacters(String value) {
		settings.beginGroup("Index");
		settings.setValue("specialCharacters", value);
		settings.endGroup();	
		databaseCache = value;
    }
    
    //*****************************************************************************
    // Control how tag selection behaves (should they be "and" or "or" selections
    //*****************************************************************************
    public static boolean anyTagSelectionMatch() {
		settings.beginGroup("General");
		try {
			String value = (String)settings.value("anyTagSelectionMatch", "false");
			settings.endGroup();
			if (value.equals("true"))
				return true;
			else
				return false;
		} catch (java.lang.ClassCastException e) {
			Boolean value = (Boolean) settings.value("anyTagSelectionMatch", false);
			settings.endGroup();
			return value;
		}
    }
    public static void setAnyTagSelectionMatch(boolean value) {
		settings.beginGroup("General");
		settings.setValue("anyTagSelectionMatch", value);
		settings.endGroup();	
    }

    //*****************************************************************************
    // Control if a user receives a warning when trying to create a note-to-note link
    // when the DB is not synchronized.
    //*****************************************************************************
    public static boolean bypassSynchronizationWarning() {
		settings.beginGroup("User");
		try {
			String value = (String)settings.value("bypassSynchronizationWarning", "false");
			settings.endGroup();
			if (value.equals("true"))
				return true;
			else
				return false;
		} catch (java.lang.ClassCastException e) {
			Boolean value = (Boolean) settings.value("bypassSynchronizationWarning", false);
			settings.endGroup();
			return value;
		}
    }
    public static void setBypassSynchronizationWarning(boolean value) {
		settings.beginGroup("User");
		settings.setValue("bypassSynchronizationWarning", value);
		settings.endGroup();	
    }

    
    //***********************
    //* Database cache size
    //***********************
    public static String getDatabaseCacheSize() {
		settings.beginGroup("Debug");
		String text = (String)settings.value("databaseCache", "16384");
		settings.endGroup();	
		return text;
    }
    public static void setDatabaseCache(String value) {
		settings.beginGroup("Debug");
		settings.setValue("databaseCache", value);
		settings.endGroup();	
		databaseCache = value;
    }

    
    // This is used to copy a class since Java's normal deep copy is wacked
    public static Object deepCopy(Object oldObj) 
    {
       ObjectOutputStream oos = null;
       ObjectInputStream ois = null;
       try
       {
          ByteArrayOutputStream bos = 
                new ByteArrayOutputStream(); // A
          oos = new ObjectOutputStream(bos); // B
          // serialize and pass the object
          oos.writeObject(oldObj);   // C
          oos.flush();               // D
          ByteArrayInputStream bin = 
                new ByteArrayInputStream(bos.toByteArray()); // E
          ois = new ObjectInputStream(bin);                  // F
          // return the new object
          return ois.readObject(); // G
       }
       catch(Exception e)
       {
          Global.logger.log(logger.LOW, "Exception in ObjectCloner = " + e);
       }
          try {
			oos.close();
	        ois.close();
		} catch (IOException e) {
			Global.logger.log(logger.LOW, "Exception in ObjectCloner = " + e);
			e.printStackTrace();
		}

		return null;
    }

    // If we should automatically select the children of any tag
    public static boolean includeTagChildren() {
		settings.beginGroup("General");
		try {
			String value = (String)settings.value("includeTagChildren", "false");
			settings.endGroup();
			if (value.equals("true"))
				return true;
			else
				return false;
		} catch (java.lang.ClassCastException e) {
			Boolean value = (Boolean) settings.value("includeTagChildren", false);
			settings.endGroup();
			return value;
		}

    }
    public static void setIncludeTagChildren(boolean value) {
		settings.beginGroup("General");
		settings.setValue("includeTagChildren", value);
		settings.endGroup();	
    }
    
    // If we should automatically wildcard searches
    public static boolean automaticWildcardSearches() {
		settings.beginGroup("General");
		try {
			String value = (String)settings.value("automaticWildcard", "false");
			settings.endGroup();
			if (value.equals("true"))
				return true;
			else
				return false;
		} catch (java.lang.ClassCastException e) {
			Boolean value = (Boolean) settings.value("automaticWildcard", false);
			settings.endGroup();
			return value;
		}

    }
    public static void setAutomaticWildcardSearches(boolean value) {
		settings.beginGroup("General");
		settings.setValue("automaticWildcard", value);
		settings.endGroup();	
    }

    // If we should automatically select the children of any tag
    public static boolean displayRightToLeft() {
		settings.beginGroup("General");
		try {
			String value = (String)settings.value("displayRightToLeft", "false");
			settings.endGroup();
			if (value.equals("true"))
				return true;
			else
				return false;
		} catch (java.lang.ClassCastException e) {
			Boolean value = (Boolean) settings.value("displayRightToLeft", false);
			settings.endGroup();
			return value;
		}

    }
    public static void setDisplayRightToLeft(boolean value) {
		settings.beginGroup("General");
		settings.setValue("displayRightToLeft", value);
		settings.endGroup();	
    }
}

