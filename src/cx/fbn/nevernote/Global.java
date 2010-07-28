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


//import java.io.ByteArrayOutputStream;

import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import com.evernote.edam.type.Accounting;
import com.evernote.edam.type.PrivilegeLevel;
import com.evernote.edam.type.User;
import com.evernote.edam.type.UserAttributes;
import com.trolltech.qt.core.QByteArray;
import com.trolltech.qt.core.QSettings;
import com.trolltech.qt.gui.QPalette;

import cx.fbn.nevernote.config.FileManager;
import cx.fbn.nevernote.config.InitializationException;
import cx.fbn.nevernote.config.StartupConfig;
import cx.fbn.nevernote.gui.ContainsAttributeFilterTable;
import cx.fbn.nevernote.gui.DateAttributeFilterTable;
import cx.fbn.nevernote.gui.ShortcutKeys;
import cx.fbn.nevernote.utilities.ApplicationLogger;

public class Global {
	public static String version = "0.88";
    public static String username = ""; 
    public static String password = "";     
    

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
    
    
    public static HashMap<String,String> passwordSafe = new HashMap<String, String>();
    public static List<String> passwordRemember = new ArrayList<String>();
    public static String currentNotebookGuid;
    public static User user; 
    public static long authTimeRemaining;
    public static long authRefreshTime;
    public static long failedRefreshes = 0;
    public static boolean keepRunning;
    
    public static String userStoreUrl;
    public static String noteStoreUrl;
    public static String noteStoreUrlBase;
    
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
    public static int noteTableColumnCount = 10;
    public static Integer cryptCounter = 0;
    
    public static int minimumWordCount = 2;
    private static String wordRegex;
    public static boolean enableCarriageReturnFix = false;
    
    public static String name = null;
    public static QSettings	settings;
    public static boolean isConnected;
    public static boolean showDeleted = false;
    public static boolean disableUploads = false;
	public static int messageLevel;
	public static String tagDelimeter = ",";
	public static String attachmentNameDelimeter = "------";
	
	public static String	databaseName = new String("NeverNote");
	public static String	indexDatabaseName = new String("IndexDatabase.db");
	public static DateAttributeFilterTable createdSinceFilter;
	public static DateAttributeFilterTable createdBeforeFilter;
	public static DateAttributeFilterTable changedSinceFilter;
	public static DateAttributeFilterTable changedBeforeFilter;
	public static ContainsAttributeFilterTable containsFilter;
//	public static DBLock				dbLock;
//	public static DBLock				indexLock;
	public static ApplicationLogger    logger;
	PrintStream stdoutStream;
	public static QPalette 				originalPalette;
	public static ShortcutKeys			shortcutKeys;
	public static boolean				disableViewing;
	
	public static List<String>				invalidElements = new ArrayList<String>();
	public static HashMap<String, ArrayList<String>> 	invalidAttributes = new HashMap<String, ArrayList<String>>();
	public static boolean mimicEvernoteInterface;
	public static HashMap<String,String> resourceMap;
	public static String cipherPassword = "";
	
	static Calendar startTraceTime;
	static Calendar intervalTraceTime;

	private static FileManager fileManager;
	
    // Do initial setup 
    public static void setup(StartupConfig startupConfig) throws InitializationException  {
        settings = new QSettings("fbn.cx", startupConfig.getName());
        disableViewing = startupConfig.getDisableViewing();

        fileManager = new FileManager(startupConfig.getHomeDirPath());


			getServer();
			settings.beginGroup("General");
			String regex = (String) settings.value("regex", "[,\\s]+");
			setWordRegex(regex);
			String wordString = settings.value("wordLength", "4").toString();
			Integer wordLen = new Integer(wordString);
			Global.minimumWordCount = wordLen;
			settings.endGroup();
			settings.beginGroup("Debug");
			String msglevel = (String) settings.value("messageLevel", "Low");
			settings.endGroup();
			messageLevel = 1;
			setMessageLevel(msglevel);
			keepRunning = true;
			disableUploads = disableUploads();
			enableCarriageReturnFix = enableCarriageReturnFix();
//			dbLock = new DBLock();
//			indexLock = new DBLock();
			logger = new ApplicationLogger("global.log");
			shortcutKeys = new ShortcutKeys();
			mimicEvernoteInterface = getMimicEvernoteInterface();
			resourceMap = new HashMap<String,String>();
				
    }

    public static String getWordRegex() {
    	return wordRegex;
    }
    public static void setWordRegex(String r) {
    	wordRegex = r;
    }
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
		settings.endGroup();
		isPremium();
		if (user.getAttributes()!=null)
			saveUserAttributes(user.getAttributes());
		if (user.getAccounting()!=null)
			saveUserAccounting(user.getAccounting());

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
    public static boolean showTrayIcon() {
		settings.beginGroup("General");
		String max = (String) settings.value("showTrayIcon", "true");
		settings.endGroup();
		if (!max.equalsIgnoreCase("true"))
			return false;
		return true;   	
    }
    public static void setShowTrayIcon(boolean val) {
		settings.beginGroup("General");
		if (val)
			settings.setValue("showTrayIcon", "true");
		else
			settings.setValue("showTrayIcon", "false");
		settings.endGroup();
    }
    public static boolean wasWindowMaximized() {
		settings.beginGroup("General");
		String max = (String) settings.value("isMaximized", "true");
		settings.endGroup();
		if (!max.equalsIgnoreCase("true"))
			return false;
		return true;   	
    }
    public static void saveWindowMaximized(boolean isMax) {
		settings.beginGroup("General");
		if (isMax)
			settings.setValue("isMaximized", "true");
		else
			settings.setValue("isMaximized", "false");
		settings.endGroup();
    }
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
    public static void setSortColumn(int i) {
		settings.beginGroup("General");
		settings.setValue("sortColumn", i);
		settings.endGroup();
    }
    public static int getSortColumn() {;
	settings.beginGroup("General");
	int order;	
	try {
		String val  = settings.value("sortColumn", new Integer(0)).toString();
		order = new Integer(val.trim());
	} catch (Exception e) {
		try {
			order = (Integer)settings.value("sortColumn", 0);
		} catch (Exception e1) {
		    order = 0;
		}
	}
	
	settings.endGroup();
	return order;
    }
    public static void setSortOrder(int i) {
		settings.beginGroup("General");
		settings.setValue("sortOrder", i);
		settings.endGroup();
    }
    public static int getSortOrder() {
		settings.beginGroup("General");
		int order;	
		try {
			String val  = settings.value("sortOrder", new Integer(0)).toString();
			order = new Integer(val.trim());
		} catch (Exception e) {
			try {
				order = (Integer)settings.value("sortOrder", 0);
			} catch (Exception e1) {
			    order = 0;
			}
		}
		
		settings.endGroup();
		return order;
    }
    public static boolean automaticLogin() {
		settings.beginGroup("General");
		String text = (String)settings.value("automaticLogin", "false");
		settings.endGroup();
		if (text.equalsIgnoreCase("true"))
			return true;
		else
			return false;		
    }
    public static void setAutomaticLogin(boolean val) {
		settings.beginGroup("General");
		if (val)
			settings.setValue("automaticLogin", "true");
		else
			settings.setValue("automaticLogin", "false");
		settings.endGroup();
    }
    public static boolean rememberPassword() {
		settings.beginGroup("General");
		String text = (String)settings.value("rememberPassword", "false");
		settings.endGroup();
		if (text.equalsIgnoreCase("true"))
			return true;
		else
			return false;		
    }
    public static void setRememberPassword(boolean val) {
		settings.beginGroup("General");
		if (val)
			settings.setValue("rememberPassword", "true");
		else
			settings.setValue("rememberPassword", "false");
		settings.endGroup();
    }
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
		if (isPremium())
			noteStoreUrlBase = "https://" + noteStoreUrlBase;
		else
			noteStoreUrlBase = "http://" + noteStoreUrlBase;
		return text;
    }
    public static boolean disableUploads() {
		settings.beginGroup("General");
		String text = (String)settings.value("disableUploads", "false");
		settings.endGroup();
		if (text.equalsIgnoreCase("true"))
			return true;
		else
			return false;
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
    public static boolean pdfPreview() {
		settings.beginGroup("General");
		String text = (String)settings.value("pdfPreview", "true");
		settings.endGroup();
		if (text.equalsIgnoreCase("true"))
			return true;
		else
			return false;
    }
    public static void setPdfPreview(boolean val) {
		settings.beginGroup("General");
		if (val)
			settings.setValue("pdfPreview", "true");
		else
			settings.setValue("pdfPreview", "false");
		settings.endGroup();
    }
    public static void setMinimumWordLength(int len) {
		settings.beginGroup("General");
		settings.setValue("minimumWordLength", len);
		settings.endGroup();    	
    }
    public static int getMinimumWordLength() {
		settings.beginGroup("General");
		Integer len = 4;
		try {
			String val  = (String)settings.value("minimumWordLength", "4");
			len = new Integer(val);
		} catch (Exception e) {
			try {
				len = (Integer)settings.value("minimumWordLength", 4);
			} catch (Exception e1) {
				len = 4;
			}
		}
		settings.endGroup();
		return len;
		
    }
    public static void setRecognitionWeight(int len) {
		settings.beginGroup("General");
		settings.setValue("recognitionWeight", len);
		settings.endGroup();    	
    }
    public static int getRecognitionWeight() {
		settings.beginGroup("General");
		Integer len;
		try {
			len = (Integer)settings.value("recognitionWeight", 80);
		} catch (Exception e) {
			len = 80;
		}
		settings.endGroup();
		return len;
    }
    public static String getMessageLevel() {
		settings.beginGroup("Debug");
		String text = (String)settings.value("messageLevel", "Low");
		settings.endGroup();
		return text;
    }
    public static void setDateFormat(String format) {
		settings.beginGroup("General");
		settings.setValue("dateFormat", format);
		settings.endGroup();    	
    }
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
    public static void setColumnWidth(String col, int width) {
		settings.beginGroup("ColumnWidths");
		settings.setValue(col, width);
		settings.endGroup();
    }
    public static int getColumnWidth(String col) {
		settings.beginGroup("ColumnWidths");
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
		settings.beginGroup("ColumnPosition");
		settings.setValue(col, width);
		settings.endGroup();
    }
    public static int getColumnPosition(String col) {
		settings.beginGroup("ColumnPosition");
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
    public static boolean verifyDelete() {
		settings.beginGroup("General");
		String text = (String)settings.value("verifyDelete", "true");
		settings.endGroup();
		if (text.equalsIgnoreCase("true"))
			return true;
		else
			return false;		
    }
    public static void setVerifyDelete(boolean val) {
		settings.beginGroup("General");
		if (val)
			settings.setValue("verifyDelete", "true");
		else
			settings.setValue("verifyDelete", "false");
		settings.endGroup();
    }
    public static boolean synchronizeDeletedContent() {
		settings.beginGroup("General");
		String text = (String)settings.value("syncDeletedContent", "false");
		settings.endGroup();
		if (text.equalsIgnoreCase("true"))
			return true;
		else
			return false;		
    }
    public static void setSynchronizeDeletedContent(boolean val) {
		settings.beginGroup("General");
		if (val)
			settings.setValue("syncDeletedContent", "true");
		else
			settings.setValue("syncDeletedContent", "false");
		settings.endGroup();
    }
    public static boolean isWindowVisible(String window) {
		settings.beginGroup("WindowsVisible");
		String text = (String)settings.value(window, "true");
		settings.endGroup();
		if (text.equalsIgnoreCase("true"))
			return true;
		else
			return false;		
    }
    public static void saveWindowVisible(String window, boolean val) {
		settings.beginGroup("WindowsVisible");
		if (val)
			settings.setValue(window, "true");
		else
			settings.setValue(window, "false");
		settings.endGroup();
    }
    public static boolean isColumnVisible(String window) {
		settings.beginGroup("ColumnsVisible");
		String text = (String)settings.value(window, "true");
		settings.endGroup();
		if (text.equalsIgnoreCase("true"))
			return true;
		else
			return false;	
    }
    public static void saveColumnVisible(String column, boolean val) {
		settings.beginGroup("ColumnsVisible");
		if (val)
			settings.setValue(column, "true");
		else
			settings.setValue(column, "false");
		settings.endGroup();
    }
    public static boolean isEditorButtonVisible(String window) {
		settings.beginGroup("EditorButtonsVisible");
		String text = (String)settings.value(window, "true");
		settings.endGroup();
		if (text.equalsIgnoreCase("true"))
			return true;
		else
			return false;	
    }
    public static void saveEditorButtonsVisible(String column, boolean val) {
		settings.beginGroup("EditorButtonsVisible");
		if (val)
			settings.setValue(column, "true");
		else
			settings.setValue(column, "false");
		settings.endGroup();
    }
    public static boolean enableCarriageReturnFix() {
		settings.beginGroup("Debug");
		String text = (String)settings.value("enableCarriageReturnFix", "false");
		settings.endGroup();
		if (text.equalsIgnoreCase("true"))
			return true;
		else
			return false;	
    }
    public static void saveCarriageReturnFix(boolean val) {
		settings.beginGroup("Debug");
		if (val)
			settings.setValue("enableCarriageReturnFix", "true");
		else
			settings.setValue("enableCarriageReturnFix", "false");
		settings.endGroup();
    }
    public static void setIndexThreads(int val) {
		settings.beginGroup("General");
		settings.setValue("indexThreads", val);
		settings.endGroup();
    }
    public static int getIndexThreads() {
		settings.beginGroup("General");
		Integer threads;
		try {
			String val  = (String)settings.value("indexThreads", "1");
			threads = new Integer(val.trim());
		} catch (Exception e) {
			try {
				threads = (Integer)settings.value("indexThreads", 1);
			} catch (Exception e1) {
				threads = 1;
			}
		}
		settings.endGroup();
		threads = 1;
		return threads;
    }
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

    public static boolean getMimicEvernoteInterface() {
		settings.beginGroup("General");
		String text = (String)settings.value("mimicEvernoteInterface", "true");
		settings.endGroup();
		if (text.equalsIgnoreCase("true"))
			return true;
		else
			return false;
    }
    public static void setMimicEvernoteInterface(boolean value) {
    	settings.beginGroup("General");
    	if (value)
    		settings.setValue("mimicEvernoteInterface", "true");
    	else
    		settings.setValue("mimicEvernoteInterface", "false"); 
    	settings.endGroup();
    }
    
    public static boolean synchronizeOnClose() {
		settings.beginGroup("General");
		String text = (String)settings.value("synchronizeOnClose", "false");
		settings.endGroup();
		if (text.equalsIgnoreCase("true"))
			return true;
		else
			return false;
    }
    public static void setSynchronizeOnClose(boolean val) {
		settings.beginGroup("General");
		if (val)
			settings.setValue("synchronizeOnClose", "true");
		else
			settings.setValue("synchronizeOnClose", "false");
		settings.endGroup();
    }
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
    public static String getDatabaseUrl() {
		settings.beginGroup("General");
		String val  = (String)settings.value("DatabaseURL", "");
		settings.endGroup();
		if (val.equals(""))
			val = "jdbc:h2:"+Global.getFileManager().getDbDirPath(Global.databaseName);
		return val;
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
    public static void setStyle(String style) {
		settings.beginGroup("General");
		settings.setValue("style", style);
		settings.endGroup();
    }
    public static String getStyle() {
		settings.beginGroup("General");
		String val  = (String)settings.value("style", "");
		settings.endGroup();
		return val;
    }
    public static boolean useStandardPalette() {
		settings.beginGroup("General");
		String text = (String)settings.value("standardPalette", "true");
		settings.endGroup();
		if (text.equalsIgnoreCase("true"))
			return true;
		else
			return false;
    }
    public static void setStandardPalette(boolean val) {
		settings.beginGroup("General");
		if (val)
			settings.setValue("standardPalette", "true");
		else
			settings.setValue("standardPalette", "false");
		settings.endGroup();
    }
    
    public static void saveState(String name, QByteArray state) {
		settings.beginGroup("SaveState");
		settings.setValue(name, state);
		settings.endGroup();
    }
    
    public static QByteArray restoreState(String name) {
		settings.beginGroup("SaveState");
		QByteArray state = (QByteArray)settings.value(name);
		settings.endGroup();
		return state;
    }
    public static void saveGeometry(String name, QByteArray state) {
		settings.beginGroup("SaveGeometry");
		settings.setValue(name, state);
		settings.endGroup();
    }
    
    public static QByteArray restoreGeometry(String name) {
		settings.beginGroup("SaveGeometry");
		QByteArray state = (QByteArray)settings.value(name);
		settings.endGroup();
		return state;
    }
    
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
				value = 0;
			}
		}
		settings.endGroup();
		return value;
    }
     
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


    public static FileManager getFileManager() {
        return fileManager;
    }
    public static boolean getDisableViewing() {
        return disableViewing;
    }
    

}

