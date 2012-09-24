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

package cx.fbn.nevernote.dialog;

import java.util.ArrayList;
import java.util.List;

import com.trolltech.qt.core.Qt;
import com.trolltech.qt.gui.QApplication;
import com.trolltech.qt.gui.QCheckBox;
import com.trolltech.qt.gui.QComboBox;
import com.trolltech.qt.gui.QFormLayout;
import com.trolltech.qt.gui.QGroupBox;
import com.trolltech.qt.gui.QHBoxLayout;
import com.trolltech.qt.gui.QLabel;
import com.trolltech.qt.gui.QScrollArea;
import com.trolltech.qt.gui.QSpinBox;
import com.trolltech.qt.gui.QStyleFactory;
import com.trolltech.qt.gui.QSystemTrayIcon;
import com.trolltech.qt.gui.QVBoxLayout;
import com.trolltech.qt.gui.QWidget;

import cx.fbn.nevernote.Global;

public class ConfigAppearancePage extends QWidget {
	private final QComboBox dateFormat;
	private final QComboBox timeFormat;
	private final QComboBox styleFormat;
	private final QComboBox	tagBehavior;
	private final QCheckBox standardPalette;
	private final QCheckBox	showSplashScreen;
	private final QCheckBox showTrayIcon;
	private final QCheckBox verifyDelete;
	private final QCheckBox	pdfPreview;
	private final QCheckBox anyTagSelection;
	private final QCheckBox	checkForUpdates;
	private final QCheckBox newNoteWithTags;
	private final QCheckBox	mimicEvernote;
	private final QCheckBox	startMinimized;
	private final QCheckBox minimizeOnClose;
	private final QCheckBox includeTagChildren;
	private final QCheckBox displayRightToLeft;
	private final QComboBox startupNotebook;
	private final QSpinBox autoSaveInterval;
	
	private final List<String> tformats;
	private final List<String> dformats;
	
	public ConfigAppearancePage(QWidget parent) {
//		super(parent);
		
		dformats = new ArrayList<String>();
		tformats = new ArrayList<String>();
		
		dformats.add("MM/dd/yy - 02/03/09");
		dformats.add("MM/dd/yyyy - 02/03/2009");
		dformats.add("M/dd/yyyy - 2/03/2009");
		dformats.add("M/d/yyyy - 2/3/2009");
		dformats.add("dd/MM/yy - 03/02/09");
		dformats.add("d/M/yy - 3/2/09");
		dformats.add("dd/MM/yyyy - 03/02/2009");
		dformats.add("d/M/yyyy - 3/2/2009");
		dformats.add("yyyy/MM/dd - 2009/02/03");
		dformats.add("yy/MM/dd - 09/02/03");
		
		tformats.add("HH:mm:ss - 18:13:01");
		tformats.add("HH:mm:ss a - 18:13:01 pm");
		tformats.add("HH:mm - 18:13");
		tformats.add("HH:mm a - 18:13 pm");
		tformats.add("hh:mm:ss - 06:13:01");
		tformats.add("hh:mm:ss a - 06:13:01 pm");
		tformats.add("h:mm:ss a - 6:13:01 pm");
		tformats.add("hh:mm - 06:13");
		tformats.add("hh:mm a - 06:13 pm");
		tformats.add("h:mm a - 6:13 pm");

		
		// Style sheet formats
		List<String> styles = QStyleFactory.keys();
		QGroupBox styleGroup = new QGroupBox(tr("GUI Style"));
		styleFormat = new QComboBox();				
		styleFormat.addItems(styles);
		styleFormat.activated.connect(this, "styleSelected(String)");
		
		standardPalette = new QCheckBox();
		standardPalette.setText(tr("Use standard palette"));
		standardPalette.clicked.connect(this, "standardPaletteChanged()");

		QFormLayout styleLayout = new QFormLayout();
		styleLayout.addWidget(styleFormat);
		styleLayout.addWidget(standardPalette);
		
		styleGroup.setLayout(styleLayout);

		QGroupBox tagBehaviorGroup = new QGroupBox(tr("Tag Behavior"));
		tagBehavior = new QComboBox();
		tagBehavior.addItem(tr("Do nothing"),"DoNothing");
		tagBehavior.addItem(tr("Count tags & do not hide inactive"),"NoHideInactiveCount");
		tagBehavior.addItem(tr("Count tags & hide inactive"),"HideInactiveCount");
		tagBehavior.addItem(tr("Color active tags"),"ColorActive");
		
		QFormLayout tagLayout = new QFormLayout();
		tagLayout.addWidget(tagBehavior);
		tagBehaviorGroup.setLayout(tagLayout);
		
		
		
		// Date/Time settings
		QGroupBox datetimeGroup = new QGroupBox(tr("Date/Time Format"));
		dateFormat = new QComboBox();				
		for (int i=0; i<dformats.size(); i++) {
			dateFormat.addItem(tr(dformats.get(i)));
		}
		
		timeFormat = new QComboBox();		
		for (int i=0; i<tformats.size(); i++) {
			timeFormat.addItem(tr(tformats.get(i)));
		}

		QFormLayout formatLayout = new QFormLayout();
		formatLayout.addWidget(dateFormat);
		formatLayout.addWidget(timeFormat);
		datetimeGroup.setLayout(formatLayout);
		
		mimicEvernote = new QCheckBox(tr("Mimic Evernote Selection Behavior (Requires Restart)"));
		showSplashScreen = new QCheckBox(tr("Show Splash Screen on Startup"));
		showTrayIcon = new QCheckBox(tr("Minimize To Tray"));
		minimizeOnClose = new QCheckBox(tr("Close To Tray"));
		if (!QSystemTrayIcon.isSystemTrayAvailable()) { 
			showTrayIcon.setEnabled(false);
			minimizeOnClose.setEnabled(false);
		}
		verifyDelete = new QCheckBox(tr("Verify Deletes"));
		startMinimized = new QCheckBox(tr("Start Minimized"));
		pdfPreview = new QCheckBox(tr("Display PDF Documents Inline"));
		checkForUpdates = new QCheckBox(tr("Check For Updates At Startup"));
		newNoteWithTags = new QCheckBox(tr("Create New Notes With Selected Tags"));
		anyTagSelection = new QCheckBox(tr("Display Notes Matching Any Selected Tags"));
		includeTagChildren = new QCheckBox(tr("Include Children In Tag Selection"));
		displayRightToLeft = new QCheckBox(tr("Display Notes Right-To-Left"));
		
		QHBoxLayout startupNotebookLayout = new QHBoxLayout();
		startupNotebook = new QComboBox();
		startupNotebook.addItem(tr("All Notebooks"), "AllNotebooks");
		startupNotebook.addItem(tr("Default Notebook"), "DefaultNotebook");
		startupNotebookLayout.addWidget(new QLabel(tr("Startup Notebook")));
		startupNotebookLayout.addWidget(startupNotebook);
		startupNotebookLayout.addStretch();
	
		
		QHBoxLayout autoSaveLayout = new QHBoxLayout();
		autoSaveLayout.addWidget(new QLabel(tr("Automatic Save Interval (in Minutes)")));
		autoSaveInterval = new QSpinBox();
		autoSaveLayout.addWidget(autoSaveInterval);
		autoSaveInterval.setMaximum(1440);
		autoSaveInterval.setMinimum(0);
		
		QVBoxLayout mainLayout = new QVBoxLayout(this);
		mainLayout.addWidget(styleGroup);
		mainLayout.addWidget(datetimeGroup);
		mainLayout.addLayout(autoSaveLayout);
		mainLayout.addWidget(tagBehaviorGroup);
		
		
		QVBoxLayout checkboxLayout = new QVBoxLayout();
		checkboxLayout.addWidget(mimicEvernote); 
		checkboxLayout.addLayout(startupNotebookLayout);
		checkboxLayout.addWidget(showTrayIcon);
		checkboxLayout.addWidget(minimizeOnClose);
		checkboxLayout.addWidget(startMinimized);
		checkboxLayout.addWidget(showSplashScreen);
		checkboxLayout.addWidget(verifyDelete);
		checkboxLayout.addWidget(pdfPreview);
		checkboxLayout.addWidget(newNoteWithTags);
		checkboxLayout.addWidget(anyTagSelection);
		checkboxLayout.addWidget(includeTagChildren);
		checkboxLayout.addWidget(displayRightToLeft);
		checkboxLayout.addWidget(checkForUpdates);
		checkboxLayout.addStretch(1);

		
		QWidget checkBoxGroup = new QWidget();
		checkBoxGroup.setLayout(checkboxLayout);

		QScrollArea scrollArea = new QScrollArea();
		scrollArea.setHorizontalScrollBarPolicy(Qt.ScrollBarPolicy.ScrollBarAsNeeded);
		scrollArea.setVerticalScrollBarPolicy(Qt.ScrollBarPolicy.ScrollBarAsNeeded);
		scrollArea.setWidgetResizable(true);
		scrollArea.setWidget(checkBoxGroup);

		mainLayout.addWidget(scrollArea);
		setLayout(mainLayout);
		
		showTrayIcon.clicked.connect(this, "showTrayIconClicked(Boolean)");
		showTrayIconClicked(showTrayIcon.isChecked());

	}
	
	private void showTrayIconClicked(Boolean checked) {
//		if (!checked) {
//			minimizeOnClose.setEnabled(false);
//			minimizeOnClose.setChecked(false);
//		} else
//			if (QSystemTrayIcon.isSystemTrayAvailable()) 
//				minimizeOnClose.setEnabled(true);
//			else
//				minimizeOnClose.setEnabled(false);
	}

	
	//*****************************************
	//* date format get/set methods 
	//*****************************************
	public void setDateFormat(String id) {
		for (int i=0; i<dformats.size(); i++) {
			String d = dformats.get(i);
			if (d.substring(0,id.length()).equals(id))
				dateFormat.setCurrentIndex(i);
		}
	}
	public String getDateFormat() {
		int i = dateFormat.currentIndex();
		return dateFormat.itemText(i);	
	}
	

	
	//*****************************************
	//* time format get/set methods 
	//*****************************************
	public void setTimeFormat(String id) {
		for (int i=0; i<tformats.size(); i++) {
			String d = tformats.get(i);
			int dash = d.indexOf("-");
			d = d.substring(0,dash-1);
			if (d.equals(id)) {
				timeFormat.setCurrentIndex(i);
				return;
			}
		}
	}
	public String getTimeFormat() {
		int i = timeFormat.currentIndex();
		return timeFormat.itemText(i);	
	}

	
	//*****************************************
	//* gui style format get/set methods 
	//*****************************************
	public void setStyle(String id) {
		for (int i=0; i<styleFormat.count(); i++) {
			String d = styleFormat.itemText(i);
			if (d.equals(id))
				styleFormat.setCurrentIndex(i);
		}
	}
	public String getStyle() {
		int i = styleFormat.currentIndex();
		return styleFormat.itemText(i);	
	}
	
	//*****************************************
	//* palette style get/set methods 
	//*****************************************
	public void setStandardPalette(boolean value) {
		standardPalette.setChecked(value);
	}
	public boolean getStandardPalette() {
		return standardPalette.isChecked();	
	}
	
	//*******************************************
	//* Show/Hide tray icon get/set
	//*******************************************
	public void setShowTrayIcon(boolean val) {
		showTrayIcon.setChecked(val);	
		showTrayIconClicked(showTrayIcon.isChecked());
	}
	public boolean getShowTrayIcon() {
		return showTrayIcon.isChecked();
	}
	
	
	//*******************************************
	//* minimize on close get/set
	//*******************************************
	public void setMinimizeOnClose(boolean val) {
		minimizeOnClose.setChecked(val);	
	}
	public boolean getMinimizeOnClose() {
		return minimizeOnClose.isChecked();
	}
	
	
	//*****************************************
	//* Show the splash screen on startup
	//*****************************************
	public void setShowSplashScreen(boolean val) {
		showSplashScreen.setChecked(val);
	}
	public boolean getShowSplashScreen() {
		return showSplashScreen.isChecked();
	}
	
	//*******************************************
	//* verify deletes get/set
	//*******************************************
	public void setVerifyDelete(boolean val) {
		verifyDelete.setChecked(val);	
	}
	public boolean getVerifyDelete() {
		return verifyDelete.isChecked();
	}
	
	
	//*******************************************
	//* Show/Hide tray icon get/set
	//*******************************************
	public void setPdfPreview(boolean val) {
		pdfPreview.setChecked(val);	
	}
	public boolean getPdfPreview() {
		return pdfPreview.isChecked();
	}
	
	//*******************************************
	//* check for updates get/set
	//*******************************************
	public void setCheckForUpdates(boolean val) {
		checkForUpdates.setChecked(val);	
	}
	public boolean getCheckForUpdates() {
		return checkForUpdates.isChecked();
	}

	
	//********************************************
	//* Listeners for palette & style changes
	//********************************************
	public void styleSelected(String style) {
		QApplication.setStyle(style);
		QApplication.setPalette(QApplication.style().standardPalette());
	}	
	public void standardPaletteChanged() {
		if (standardPalette.isChecked())
			QApplication.setPalette(QApplication.style().standardPalette());
		else
			QApplication.setPalette(Global.originalPalette);
			
	}

	
	//*****************************************
	//* automatic save timer
	//*****************************************
	public void setAutoSaveInterval(int len) {
		autoSaveInterval.setValue(len);
	}
	public int getAutoSaveInterval() {
		return autoSaveInterval.value();	
	}

	
	//*****************************************
	//* Get/Set tag behavior combo box
	//*****************************************
	public void setTagBehavior(String value) {
		for (int i=0; i<tagBehavior.count(); i++) {
			String d = tagBehavior.itemData(i).toString();
			if (value.equalsIgnoreCase(d)) {
				tagBehavior.setCurrentIndex(i);
				return;
			}
		}
	}
	public String getTagBehavior() {
		int i = tagBehavior.currentIndex();
		return tagBehavior.itemData(i).toString();
	}

	//*****************************************
	//* Mimic Evernote Selection
	//*****************************************
	public boolean getMimicEvernote() {
		return mimicEvernote.isChecked();
	}
	public void setMimicEvernote(boolean val) {
		mimicEvernote.setChecked(val);
	}

	
	//*****************************************
	//* Mimic Evernote Selection
	//*****************************************
	public boolean getStartMinimized() {
		return startMinimized.isChecked();
	}
	public void setStartMinimized(boolean val) {
		startMinimized.setChecked(val);
	}


	//*****************************************
	//* Create Note With Selected Tags
	//*****************************************
	public boolean getNewNoteWithTags() {
		return newNoteWithTags.isChecked();
	}
	public void setNewNoteWithTags(boolean val) {
		newNoteWithTags.setChecked(val);
	}
	
	//*****************************************
	//* Set tag selection behavior
	//*****************************************
	public boolean getAnyTagSelection() {
		return anyTagSelection.isChecked();
	}
	public void setAnyTagSelection(boolean val) {
		anyTagSelection.setChecked(val);
	}


	//*****************************************
	//* Include a tag's child when selecting a parent
	//*****************************************
	public boolean getIncludeTagChildren() {
		return includeTagChildren.isChecked();
	}
	public void setIncludeTagChildren(boolean val) {
		includeTagChildren.setChecked(val);
	}
	//*****************************************
	//* Include a tag's child when selecting a parent
	//*****************************************
	public boolean getDisplayRightToLeft() {
		return displayRightToLeft.isChecked();
	}
	public void setDisplayRightToLeft(boolean val) {
		displayRightToLeft.setChecked(val);
	}

	
	//**************************************************
	//* Get/Set startup notebook
	//**************************************************
	public void setStartupNotebook(String value) {
		for (int i=0; i<startupNotebook.count(); i++) {
			String d = startupNotebook.itemData(i).toString();
			if (d.equals(value))
				startupNotebook.setCurrentIndex(i);
		}
	}
	public String getStartupNotebook() {
		int index = startupNotebook.currentIndex();
		return startupNotebook.itemData(index).toString();	
	}

}
