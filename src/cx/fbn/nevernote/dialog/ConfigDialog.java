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

//**********************************************
//**********************************************
//* This dialog is the debugging information 
//* page used in the Edit/Preferences dialog
//**********************************************
//**********************************************

package cx.fbn.nevernote.dialog;

import com.swabunga.spell.engine.Configuration;
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
public class ConfigDialog extends QDialog {
	private final QListWidget 				contentsWidget;
	private final ConfigFontPage			fontPage;
	private final QStackedWidget 			pagesWidget;
	private final ConfigConnectionPage		connectionPage;
	private final ConfigDebugPage			debugPage;
	private final ConfigAppearancePage 		appearancePage;
	private final ConfigSpellPage			spellPage;
	private final ConfigIndexPage			indexPage;
    private final String iconPath = new String("classpath:cx/fbn/nevernote/icons/");
	
	public ConfigDialog(QWidget parent) {
		
		contentsWidget = new QListWidget(this);
		setWindowIcon(new QIcon(iconPath+"config.png"));
		contentsWidget.setViewMode(QListView.ViewMode.IconMode);
		contentsWidget.setIconSize(new QSize(96, 84));
		contentsWidget.setMovement(QListView.Movement.Static);
		contentsWidget.setMaximumWidth(128);
		contentsWidget.setSpacing(12);
		
		pagesWidget = new QStackedWidget(this);
		fontPage = new ConfigFontPage(this);
		connectionPage = new ConfigConnectionPage(this);
		appearancePage = new ConfigAppearancePage(this);
		indexPage = new ConfigIndexPage(this);
		debugPage = new ConfigDebugPage(this);
		spellPage = new ConfigSpellPage(this);
		pagesWidget.addWidget(appearancePage);
		pagesWidget.addWidget(fontPage);
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

	//******************************************
	//* Ok button is pushed.  Save values
	//******************************************
	public void okPushed() {
		Global.setServer(debugPage.getServer());
		Global.setEnableThumbnails(debugPage.getEnableThumbnails());

		
		if (debugPage.getDisableUploads())
			Global.disableUploads = true;
		else
			Global.disableUploads = false;
		Global.setDisableUploads(Global.disableUploads);
		Global.setMimicEvernoteInterface(appearancePage.getMimicEvernote());
		Global.setMinimizeOnClose(appearancePage.getMinimizeOnClose());
		
		if (appearancePage.getShowSplashScreen())
			Global.saveWindowVisible("SplashScreen", true);
		else
			Global.saveWindowVisible("SplashScreen", false);
			
		
		if (appearancePage.getPdfPreview())
			Global.setPdfPreview(true);
		else
			Global.setPdfPreview(false);

		if (appearancePage.getCheckForUpdates())
			Global.setCheckVersionUpgrade(true);
		else
			Global.setCheckVersionUpgrade(false);

		
		if (appearancePage.getNewNoteWithTags())
			Global.setNewNoteWithSelectedTags(true);
		else
			Global.setNewNoteWithSelectedTags(false);
		
		if (appearancePage.getAnyTagSelection())
			Global.setAnyTagSelectionMatch(true);
		else
			Global.setAnyTagSelectionMatch(false);
		
		Global.setAutoSaveInterval(appearancePage.getAutoSaveInterval());
						
		Global.setAutomaticLogin(connectionPage.getAutomaticLogin());
		Global.setProxyValue("url", connectionPage.getProxyUrl());
		Global.setProxyValue("port", connectionPage.getProxyPort());
		Global.setProxyValue("userid", connectionPage.getProxyUserid());
		Global.setProxyValue("password", connectionPage.getProxyPassword());
		
		Global.setShowTrayIcon(appearancePage.getShowTrayIcon());
		Global.setVerifyDelete(appearancePage.getVerifyDelete());
		Global.setStartMinimized(appearancePage.getStartMinimized());
		Global.setSynchronizeOnClose(connectionPage.getSynchronizeOnClose());
		Global.setSynchronizeDeletedContent(connectionPage.getSynchronizeDeletedContent());
		Global.setTagBehavior(appearancePage.getTagBehavior());
		Global.setIndexAttachmentsLocally(indexPage.getIndexAttachmentsLocally());
		Global.setIndexNoteBody(indexPage.getIndexNoteBody());
		Global.setIndexNoteTitle(indexPage.getIndexNoteTitle());
		Global.setIndexImageRecognition(indexPage.getIndexImageRecognition());
		Global.setAutomaticWildcardSearches(indexPage.getAutomaticWildcardSearches());
		Global.setSpecialIndexCharacters(indexPage.getSpecialCharacters());
		Global.setIncludeTagChildren(appearancePage.getIncludeTagChildren());
		Global.setDisplayRightToLeft(appearancePage.getDisplayRightToLeft());
		
		Global.userStoreUrl = "https://"+debugPage.getServer()+"/edam/user";
		Global.setWordRegex(indexPage.getRegex());
		Global.setRecognitionWeight(indexPage.getRecognitionWeight());
		Global.setIndexThreadSleepInterval(indexPage.getSleepInterval());
		Global.setMessageLevel( debugPage.getDebugLevel());
		Global.saveCarriageReturnFix(debugPage.getCarriageReturnFix());
		Global.enableCarriageReturnFix = debugPage.getCarriageReturnFix();
		Global.saveHtmlEntitiesFix(debugPage.getHtmlEntitiesFix());
		Global.enableHTMLEntitiesFix = debugPage.getHtmlEntitiesFix();
		
		Global.setSpellSetting(Configuration.SPELL_IGNOREDIGITWORDS, spellPage.getIgnoreDigitWords());
		Global.setSpellSetting(Configuration.SPELL_IGNOREINTERNETADDRESSES, spellPage.getIgnoreInternetAddresses());
		Global.setSpellSetting(Configuration.SPELL_IGNOREMIXEDCASE, spellPage.getIgnoreMixedCase());
		Global.setSpellSetting(Configuration.SPELL_IGNOREUPPERCASE, spellPage.getIgnoreUpperCase());
		Global.setSpellSetting(Configuration.SPELL_IGNORESENTENCECAPITALIZATION, spellPage.getIgnoreSentenceCapitalization());
		
		String guiFormat = appearancePage.getStyle();
		QApplication.setStyle(guiFormat);
		QApplication.style().standardPalette();
		Global.setStyle(guiFormat);
		Global.setStandardPalette(appearancePage.getStandardPalette());
		if (Global.useStandardPalette())
			QApplication.setPalette(QApplication.style().standardPalette());
		else
			QApplication.setPalette(Global.originalPalette);
		Global.setStartupNotebook(appearancePage.getStartupNotebook());
		
		String dateFmt = appearancePage.getDateFormat();
		String timeFmt = appearancePage.getTimeFormat();
		int dash = dateFmt.indexOf("-");
		dateFmt = dateFmt.substring(0,dash-1);
		dash = timeFmt.indexOf("-");
		timeFmt = timeFmt.substring(0,dash-1);
		
		Global.setDateFormat(dateFmt);
		Global.setTimeFormat(timeFmt);
		
		Global.setSyncInterval(connectionPage.getSyncInterval());
		
		Global.setOverrideDefaultFont(fontPage.overrideFont());
		Global.setDefaultFont(fontPage.getFont());
		Global.setDefaultFontSize(fontPage.getFontSize());
		Global.setDatabaseCache(debugPage.getDatabaseCacheSize());
				
		close();
	}
	
	
	// Reject the current style
	@Override
	public void reject() {
		QApplication.setStyle(Global.getStyle());
		super.reject();
	}
	
	//* return the debugging information page
	public ConfigDebugPage getDebugPage() {
		return debugPage;
	}
	
	// Get the Evernote connection page
	public ConfigConnectionPage getConfigPage() {
		return connectionPage;
	}
	
	// Create icons used for navigating the page
	public void createIcons() {
		String iconPath = new String("classpath:cx/fbn/nevernote/icons/");

		
		QListWidgetItem formatsButton = new QListWidgetItem(contentsWidget);
		formatsButton.setText(tr("Appearance"));
		formatsButton.setTextAlignment(AlignmentFlag.AlignHCenter.value());
		formatsButton.setFlags(ItemFlag.ItemIsSelectable, ItemFlag.ItemIsEnabled);
		formatsButton.setIcon(new QIcon(iconPath+"appearance.jpg"));
		
		QListWidgetItem fontButton = new QListWidgetItem(contentsWidget);
		fontButton.setText(tr("Fonts"));
		fontButton.setTextAlignment(AlignmentFlag.AlignHCenter.value());
		fontButton.setFlags(ItemFlag.ItemIsSelectable, ItemFlag.ItemIsEnabled);
		fontButton.setIcon(new QIcon(iconPath+"fontConfig.png"));
		
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
	
	// this is called when the user switches config pages
	protected void changePage(QListWidgetItem current, QListWidgetItem previous) {
		pagesWidget.setCurrentIndex(contentsWidget.row(current));
	}
	
	// Load initial settings
	private void loadSettings() {
		Global.originalPalette = QApplication.palette();
		
		debugPage.setServer(Global.getServer());
		debugPage.setDisableUploads(Global.disableUploads);
		debugPage.setEnableThumbnails(Global.enableThumbnails());
//		if (Global.getUpdateSequenceNumber() > 0)
			debugPage.serverCombo.setEnabled(false);

		appearancePage.setAutoSaveInterval(Global.getAutoSaveInterval());
		connectionPage.setAutomaticLogin(Global.automaticLogin());
		appearancePage.setMimicEvernote(Global.getMimicEvernoteInterface());
		appearancePage.setShowTrayIcon(Global.showTrayIcon());
		connectionPage.setSynchronizeOnClose(Global.synchronizeOnClose());
		connectionPage.setSyncronizeDeletedContent(Global.synchronizeDeletedContent());
		appearancePage.setVerifyDelete(Global.verifyDelete());
		appearancePage.setStartMinimized(Global.startMinimized());
		appearancePage.setPdfPreview(Global.pdfPreview());
		appearancePage.setCheckForUpdates(Global.checkVersionUpgrade());
		appearancePage.setNewNoteWithTags(Global.newNoteWithSelectedTags());
		appearancePage.setAnyTagSelection(Global.anyTagSelectionMatch());
		appearancePage.setShowSplashScreen(Global.isWindowVisible("SplashScreen"));
		appearancePage.setTagBehavior(Global.tagBehavior());
		appearancePage.setMinimizeOnClose(Global.minimizeOnClose());
		appearancePage.setIncludeTagChildren(Global.includeTagChildren());
		appearancePage.setDisplayRightToLeft(Global.displayRightToLeft());
		appearancePage.setStartupNotebook(Global.getStartupNotebook());
		
		indexPage.setRegex(Global.getWordRegex());
		indexPage.setSleepInterval(Global.getIndexThreadSleepInterval());
		connectionPage.setSyncInterval(Global.getSyncInterval());
		
		appearancePage.setDateFormat(Global.getDateFormat());
		appearancePage.setTimeFormat(Global.getTimeFormat());
		appearancePage.setStyle(Global.getStyle());
		appearancePage.setStandardPalette(Global.useStandardPalette());
						
		debugPage.setDebugLevel(Global.getMessageLevel());
		debugPage.setCarriageReturnFix(Global.enableCarriageReturnFix());
		debugPage.setHtmlEntitiesFix(Global.enableHTMLEntitiesFix);
		
	}
	
}
