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

import java.util.ArrayList;
import java.util.List;

import com.trolltech.qt.gui.QApplication;
import com.trolltech.qt.gui.QCheckBox;
import com.trolltech.qt.gui.QComboBox;
import com.trolltech.qt.gui.QFormLayout;
import com.trolltech.qt.gui.QGroupBox;
import com.trolltech.qt.gui.QHBoxLayout;
import com.trolltech.qt.gui.QLabel;
import com.trolltech.qt.gui.QSpinBox;
import com.trolltech.qt.gui.QStyleFactory;
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
	private final QCheckBox newNoteWithTags;
	private final QCheckBox	mimicEvernote;
	private final QSpinBox autoSaveInterval;
	
	private final List<String> tformats;
	private final List<String> dformats;
	
	public ConfigAppearancePage(QWidget parent) {
//		super(parent);
		
		dformats = new ArrayList<String>();
		tformats = new ArrayList<String>();
		
		dformats.add("MM/dd/yy - 12/31/09");
		dformats.add("MM/dd/yyyy - 12/31/2009");
		dformats.add("dd/MM/yy - 31/12/09");
		dformats.add("dd/MM/yyyy - 31/12/2009");
		dformats.add("yyyy/MM/dd - 2009/12/31");
		dformats.add("yy/MM/dd - 9/12/31");
		
		tformats.add("HH:mm:ss - 2:13:01");
		tformats.add("HH:mm:ss a - 2:13:01 am");
		tformats.add("HH:mm - 2:13");
		tformats.add("HH:mm a - 2:13 am");

		
		// Style sheet formats
		List<String> styles = QStyleFactory.keys();
		QGroupBox styleGroup = new QGroupBox(tr("GUI Style"));
		styleFormat = new QComboBox();				
		styleFormat.addItems(styles);
		styleFormat.activated.connect(this, "styleSelected(String)");
		
		standardPalette = new QCheckBox();
		standardPalette.setText("Use standard palette");
		standardPalette.clicked.connect(this, "standardPaletteChanged()");

		QFormLayout styleLayout = new QFormLayout();
		styleLayout.addWidget(styleFormat);
		styleLayout.addWidget(standardPalette);
		
		styleGroup.setLayout(styleLayout);

		QGroupBox tagBehaviorGroup = new QGroupBox(tr("Tag Behavior"));
		tagBehavior = new QComboBox();
		tagBehavior.addItem("Do nothing");
		tagBehavior.addItem("Count tags & do not hide inactive");
		tagBehavior.addItem("Count tags & hide inactive");
		tagBehavior.addItem("Color active tags");
		
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
		
		mimicEvernote = new QCheckBox("Mimic Evernote Selection Behavior (Requires Restart)");
		showSplashScreen = new QCheckBox("Show Splash Screen on Startup");
		showTrayIcon = new QCheckBox("Show Tray Icon");
		verifyDelete = new QCheckBox("Verify Deletes");
		pdfPreview = new QCheckBox("Display PDF Documents Inline");
		newNoteWithTags = new QCheckBox("Create New Notes With Selected Tags");
		
		QHBoxLayout autoSaveLayout = new QHBoxLayout();
		autoSaveLayout.addWidget(new QLabel("Automatic Save Interval (in Minutes)"));
		autoSaveInterval = new QSpinBox();
		autoSaveLayout.addWidget(autoSaveInterval);
		autoSaveInterval.setMaximum(1440);
		autoSaveInterval.setMinimum(0);
		
		QVBoxLayout mainLayout = new QVBoxLayout();
		mainLayout.addWidget(styleGroup);
		mainLayout.addWidget(datetimeGroup);
		mainLayout.addLayout(autoSaveLayout);
		mainLayout.addWidget(tagBehaviorGroup);
		mainLayout.addWidget(mimicEvernote); 
		mainLayout.addWidget(showTrayIcon);
		mainLayout.addWidget(showSplashScreen);
		mainLayout.addWidget(verifyDelete);
		mainLayout.addWidget(pdfPreview);
		mainLayout.addWidget(newNoteWithTags);
		mainLayout.addStretch(1);
		setLayout(mainLayout);


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
	}
	public boolean getShowTrayIcon() {
		return showTrayIcon.isChecked();
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
			String d = tagBehavior.itemText(i);
			if (d.equalsIgnoreCase("Do Nothing") && value.equalsIgnoreCase("DoNothing")) {
				tagBehavior.setCurrentIndex(i);
				return;
			}
			if (d.equalsIgnoreCase("Count tags & hide inactive") && value.equalsIgnoreCase("HideInactiveCount")) {
				tagBehavior.setCurrentIndex(i);
				return;
			}
			if (d.equalsIgnoreCase("Count tags & do not hide inactive") && value.equalsIgnoreCase("NoHideInactiveCount")) {
				tagBehavior.setCurrentIndex(i);
				return;
			}
			if (d.equalsIgnoreCase("Color Active Tags") && value.equalsIgnoreCase("ColorActive")) {
				tagBehavior.setCurrentIndex(i);
				return;
			}
		}
	}
	public String getTagBehavior() {
		int i = tagBehavior.currentIndex();
		String behavior =  tagBehavior.itemText(i);	
		if (behavior.equalsIgnoreCase("Count tags & hide inactive")) {
			tagBehavior.setCurrentIndex(i);
			return "HideInactiveCount";
		}
		if (behavior.equalsIgnoreCase("Count tags & do not hide inactive")) {
			tagBehavior.setCurrentIndex(i);
			return "NoHideInactiveCount";
		}
		if (behavior.equalsIgnoreCase("Color Active Tags")) {
			tagBehavior.setCurrentIndex(i);
			return "ColorActive";
		}
		return "DoNothing";
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
	//* Create Note With Selected Tags
	//*****************************************
	public boolean getNewNoteWithTags() {
		return newNoteWithTags.isChecked();
	}
	public void setNewNoteWithTags(boolean val) {
		newNoteWithTags.setChecked(val);
	}

}
