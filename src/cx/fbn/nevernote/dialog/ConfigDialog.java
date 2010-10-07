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

package cx.fbn.nevernote.dialog;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import com.trolltech.qt.core.QSize;
import com.trolltech.qt.core.Qt.AlignmentFlag;
import com.trolltech.qt.core.Qt.ItemFlag;
import com.trolltech.qt.gui.QApplication;
import com.trolltech.qt.gui.QDialog;
import com.trolltech.qt.gui.QHBoxLayout;
import com.trolltech.qt.gui.QIcon;
import com.trolltech.qt.gui.QListView;
import com.trolltech.qt.gui.QListWidget;
import com.trolltech.qt.gui.QListWidgetItem;
import com.trolltech.qt.gui.QPushButton;
import com.trolltech.qt.gui.QStackedWidget;
import com.trolltech.qt.gui.QVBoxLayout;
import com.trolltech.qt.gui.QWidget;

import cx.fbn.nevernote.Global;
import cx.fbn.nevernote.utilities.AESEncrypter;
public class ConfigDialog extends QDialog {
	private final QListWidget 				contentsWidget;
	private final QStackedWidget 			pagesWidget;
	private final ConfigConnectionPage		connectionPage;
	private final ConfigDebugPage			debugPage;
	private final ConfigAppearancePage 		appearancePage;
	private final ConfigSpellPage			spellPage;
	private final ConfigIndexPage			indexPage;
	
	public ConfigDialog(QWidget parent) {
		
		contentsWidget = new QListWidget(this);
		contentsWidget.setViewMode(QListView.ViewMode.IconMode);
		contentsWidget.setIconSize(new QSize(96, 84));
		contentsWidget.setMovement(QListView.Movement.Static);
		contentsWidget.setMaximumWidth(128);
		contentsWidget.setSpacing(12);
		
		pagesWidget = new QStackedWidget(this);
		connectionPage = new ConfigConnectionPage(this);
		appearancePage = new ConfigAppearancePage(this);
		indexPage = new ConfigIndexPage(this);
		debugPage = new ConfigDebugPage(this);
		spellPage = new ConfigSpellPage(this);
		pagesWidget.addWidget(appearancePage);
		pagesWidget.addWidget(indexPage);
		pagesWidget.addWidget(spellPage);
		pagesWidget.addWidget(connectionPage);
		pagesWidget.addWidget(debugPage);
		
		QPushButton cancelButton = new QPushButton(tr("Cancel"));
		QPushButton okButton = new QPushButton(tr("OK"));
		okButton.clicked.connect(this, "okPushed()");
		cancelButton.clicked.connect(this, "close()");
		
		createIcons();
		contentsWidget.setCurrentRow(0);
		
		QHBoxLayout horizontalLayout = new QHBoxLayout();
		horizontalLayout.addWidget(contentsWidget);
		horizontalLayout.addWidget(pagesWidget,1);
		
		QHBoxLayout buttonLayout = new QHBoxLayout();
		buttonLayout.addStretch(1);
		buttonLayout.addWidget(okButton);
		buttonLayout.addWidget(cancelButton);
		setWindowTitle(tr("Settings"));	
		
		QVBoxLayout mainLayout = new QVBoxLayout();
		mainLayout.addLayout(horizontalLayout);
		mainLayout.addSpacing(1);
		mainLayout.addLayout(buttonLayout);
		setLayout(mainLayout);
		
		loadSettings();
	}
	public void okPushed() {
		Global.setServer(debugPage.getServer());
		AESEncrypter aes = new AESEncrypter();
		aes.setUserid(connectionPage.getUserid().trim());
		
		if (debugPage.getDisableUploads())
			Global.disableUploads = true;
		else
			Global.disableUploads = false;
		Global.setDisableUploads(Global.disableUploads);
		Global.setMimicEvernoteInterface(appearancePage.getMimicEvernote());
		
		if (appearancePage.getShowSplashScreen())
			Global.saveWindowVisible("SplashScreen", true);
		else
			Global.saveWindowVisible("SplashScreen", false);
			
		
		if (appearancePage.getPdfPreview())
			Global.setPdfPreview(true);
		else
			Global.setPdfPreview(false);
		
		if (appearancePage.getNewNoteWithTags())
			Global.setNewNoteWithSelectedTags(true);
		else
			Global.setNewNoteWithSelectedTags(false);
		
		Global.setAutoSaveInterval(appearancePage.getAutoSaveInterval());
						
		Global.setAutomaticLogin(connectionPage.getAutomaticLogin());
		Global.setRememberPassword(connectionPage.getRememberPassword());
		if (connectionPage.getRememberPassword()) {	
			aes.setPassword(connectionPage.getPassword());
		}
		Global.setShowTrayIcon(appearancePage.getShowTrayIcon());
		Global.setVerifyDelete(appearancePage.getVerifyDelete());
		Global.setStartMinimized(appearancePage.getStartMinimized());
		Global.setSynchronizeOnClose(connectionPage.getSynchronizeOnClose());
		Global.setSynchronizeDeletedContent(connectionPage.getSynchronizeDeletedContent());
		Global.setTagBehavior(appearancePage.getTagBehavior());
    	FileOutputStream out = null;
		try {
			out = new FileOutputStream(Global.getFileManager().getHomeDirFile("secure.txt"));
		} catch (FileNotFoundException e) {
			// if it isn't found we'll write it.
		}
		if (out != null)
			aes.encrypt(out);
		Global.userStoreUrl = "https://"+debugPage.getServer()+"/edam/user";
		Global.setWordRegex(indexPage.getRegex());
		Global.setRecognitionWeight(indexPage.getRecognitionWeight());
		Global.setMinimumWordLength(indexPage.getWordLength());
		Global.minimumWordCount = indexPage.getWordLength();	
		Global.setIndexThreads(indexPage.getIndexThreads());
		Global.setIndexThreadSleepInterval(indexPage.getSleepInterval());
		Global.setMessageLevel( debugPage.getDebugLevel());
		Global.saveCarriageReturnFix(debugPage.getCarriageReturnFix());
		Global.enableCarriageReturnFix = debugPage.getCarriageReturnFix();
		
		String guiFormat = appearancePage.getStyle();
		QApplication.setStyle(guiFormat);
		QApplication.style().standardPalette();
		Global.setStyle(guiFormat);
		Global.setStandardPalette(appearancePage.getStandardPalette());
		if (Global.useStandardPalette())
			QApplication.setPalette(QApplication.style().standardPalette());
		else
			QApplication.setPalette(Global.originalPalette);
		
		String dateFmt = appearancePage.getDateFormat();
		String timeFmt = appearancePage.getTimeFormat();
		int dash = dateFmt.indexOf("-");
		dateFmt = dateFmt.substring(0,dash-1);
		dash = timeFmt.indexOf("-");
		timeFmt = timeFmt.substring(0,dash-1);
		
		Global.setDateFormat(dateFmt);
		Global.setTimeFormat(timeFmt);
		
		Global.setSyncInterval(connectionPage.getSyncInterval());
				
		close();
	}
	@Override
	public void reject() {
		QApplication.setStyle(Global.getStyle());
		super.reject();
	}
	
	public ConfigDebugPage getDebugPage() {
		return debugPage;
	}
	
	
	public ConfigConnectionPage getConfigPage() {
		return connectionPage;
	}
	
	public void createIcons() {
		String iconPath = new String("classpath:cx/fbn/nevernote/icons/");

		
		QListWidgetItem formatsButton = new QListWidgetItem(contentsWidget);
		formatsButton.setText(tr("Appearance"));
		formatsButton.setTextAlignment(AlignmentFlag.AlignHCenter.value());
		formatsButton.setFlags(ItemFlag.ItemIsSelectable, ItemFlag.ItemIsEnabled);
		formatsButton.setIcon(new QIcon(iconPath+"appearance.jpg"));
		
		QListWidgetItem indexButton = new QListWidgetItem(contentsWidget);
		indexButton.setText(tr("Indexing"));
		indexButton.setTextAlignment(AlignmentFlag.AlignHCenter.value());
		indexButton.setFlags(ItemFlag.ItemIsSelectable, ItemFlag.ItemIsEnabled);
		indexButton.setIcon(new QIcon(iconPath+"search_config.jpg"));

		QListWidgetItem spellButton = new QListWidgetItem(contentsWidget);
		spellButton.setText(tr("Spell Check"));
		spellButton.setTextAlignment(AlignmentFlag.AlignHCenter.value());
		spellButton.setFlags(ItemFlag.ItemIsSelectable, ItemFlag.ItemIsEnabled);
		spellButton.setIcon(new QIcon(iconPath+"dictionary.png"));

		QListWidgetItem configButton = new QListWidgetItem(contentsWidget);
		configButton.setText(tr("Connection"));
		configButton.setTextAlignment(AlignmentFlag.AlignHCenter.value());
		configButton.setFlags(ItemFlag.ItemIsSelectable, ItemFlag.ItemIsEnabled);
		configButton.setIcon(new QIcon(iconPath+"synchronize.png"));

		QListWidgetItem debugButton = new QListWidgetItem(contentsWidget);
		debugButton.setText(tr("Debugging"));
		debugButton.setTextAlignment(AlignmentFlag.AlignHCenter.value());
		debugButton.setFlags(ItemFlag.ItemIsSelectable, ItemFlag.ItemIsEnabled);
		debugButton.setIcon(new QIcon(iconPath+"debug.jpg"));
		
		contentsWidget.currentItemChanged.connect(this, "changePage(QListWidgetItem, QListWidgetItem)");
	}
	
	protected void changePage(QListWidgetItem current, QListWidgetItem previous) {
		pagesWidget.setCurrentIndex(contentsWidget.row(current));
	}
	
	private void loadSettings() {
		Global.originalPalette = QApplication.palette();
		
		debugPage.setServer(Global.getServer());
		debugPage.setDisableUploads(Global.disableUploads);
//		if (Global.getUpdateSequenceNumber() > 0)
			debugPage.serverCombo.setEnabled(false);
		
		if (Global.username.equalsIgnoreCase("") || Global.password.equalsIgnoreCase("")) {
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
		}
		appearancePage.setAutoSaveInterval(Global.getAutoSaveInterval());
		connectionPage.setUserid(Global.username);
		connectionPage.setPassword(Global.password);
		connectionPage.setAutomaticLogin(Global.automaticLogin());
		connectionPage.setRememberPassword(Global.rememberPassword());
		appearancePage.setMimicEvernote(Global.getMimicEvernoteInterface());
		appearancePage.setShowTrayIcon(Global.showTrayIcon());
		connectionPage.setSynchronizeOnClose(Global.synchronizeOnClose());
		connectionPage.setSyncronizeDeletedContent(Global.synchronizeDeletedContent());
		appearancePage.setVerifyDelete(Global.verifyDelete());
		appearancePage.setStartMinimized(Global.startMinimized());
		appearancePage.setPdfPreview(Global.pdfPreview());
		appearancePage.setNewNoteWithTags(Global.newNoteWithSelectedTags());
		appearancePage.setShowSplashScreen(Global.isWindowVisible("SplashScreen"));
		appearancePage.setTagBehavior(Global.tagBehavior());
		
		indexPage.setRegex(Global.getWordRegex());
		indexPage.setWordLength(Global.getMinimumWordLength());
		indexPage.setIndexThreads(Global.getIndexThreads());
		indexPage.setSleepInterval(Global.getIndexThreadSleepInterval());
		connectionPage.setSyncInterval(Global.getSyncInterval());
		
		appearancePage.setDateFormat(Global.getDateFormat());
		appearancePage.setTimeFormat(Global.getTimeFormat());
		appearancePage.setStyle(Global.getStyle());
		appearancePage.setStandardPalette(Global.useStandardPalette());
						
		debugPage.setDebugLevel(Global.getMessageLevel());
		debugPage.setCarriageReturnFix(Global.enableCarriageReturnFix());
		
	}
	
}
