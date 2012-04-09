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

import java.util.List;

import com.trolltech.qt.gui.QCheckBox;
import com.trolltech.qt.gui.QComboBox;
import com.trolltech.qt.gui.QFontDatabase;
import com.trolltech.qt.gui.QGridLayout;
import com.trolltech.qt.gui.QGroupBox;
import com.trolltech.qt.gui.QLabel;
import com.trolltech.qt.gui.QVBoxLayout;
import com.trolltech.qt.gui.QWidget;

import cx.fbn.nevernote.Global;

public class ConfigFontPage extends QWidget {
	private final QCheckBox overrideFonts;
	private final QComboBox fontList;
	private final QComboBox fontSizeList;
	private String font;
	private String fontSize;
		
	public ConfigFontPage(QWidget parent) {
		
		font = Global.getDefaultFont();
		fontSize = Global.getDefaultFontSize();
		
		// Group Box
		QGroupBox fontGroup = new QGroupBox(tr("New Note Defaults"));
		QGridLayout fontLayout = new QGridLayout();
		overrideFonts = new QCheckBox(tr("Override Defaults")); 
		overrideFonts.setCheckable(true);
		overrideFonts.setChecked(Global.overrideDefaultFont());
		overrideFonts.clicked.connect(this, "toggleFontOverride(Boolean)");
		fontLayout.addWidget(overrideFonts,1,2);
		
		
		// Fonts
		fontList = new QComboBox();				
		fontList.activated.connect(this, "fontSelected(String)");
		fontLayout.addWidget(new QLabel(tr("Font")),2,1);
		fontLayout.addWidget(fontList,2,2);
		
		// Font Sizes
		fontSizeList = new QComboBox();				
		fontLayout.addWidget(fontSizeList,3,2);
		fontSizeList.activated.connect(this, "fontSizeSelected(String)");
		fontLayout.addWidget(new QLabel(tr("Size")),3,1);
		fontGroup.setLayout(fontLayout);
		fontLayout.setColumnStretch(2, 100);
		toggleFontOverride(Global.overrideDefaultFont());
		loadFonts();
		loadSettings();
		
		
		QVBoxLayout mainLayout = new QVBoxLayout();
		mainLayout.addWidget(fontGroup);
		mainLayout.addStretch(1);
		setLayout(mainLayout);
	}
	
	private void toggleFontOverride(Boolean value) {
		fontList.setEnabled(value);
		fontSizeList.setEnabled(value);
	}
	
	private void loadFonts() {
		QFontDatabase fonts = new QFontDatabase();
		List<String> fontFamilies = fonts.families();
		for (int i = 0; i < fontFamilies.size(); i++) {
			if (font.equals(""))
				font = fontFamilies.get(i);
			fontList.addItem(fontFamilies.get(i));
			if (i == 0) {
				loadFontSize(fontFamilies.get(i));
			}
		}

	}
	
	// Load the font combo box based upon the font selected
	private void loadFontSize(String name) {	
		QFontDatabase db = new QFontDatabase(); 
		fontSizeList.clear();
		List<Integer> points = db.pointSizes(name); 
		for (int i=0; i<points.size(); i++) { 
			if (fontSize.equals(""))
				fontSize = points.get(i).toString();
			fontSizeList.addItem(points.get(i).toString()); 
		}

	}
	
	@SuppressWarnings("unused")
	private void fontSelected(String font) {
		this.font = font;
		loadFontSize(font);
	}
	
	@SuppressWarnings("unused")
	private void fontSizeSelected(String size) {
		this.fontSize = size;
	}
	
	private void loadSettings() {
		if (!Global.getDefaultFont().equals("")) {
			int index = fontList.findText(Global.getDefaultFont());
			fontList.setCurrentIndex(index);
		}
		if (!Global.getDefaultFontSize().equals("")) {
			int index = fontSizeList.findText(Global.getDefaultFontSize());
			fontSizeList.setCurrentIndex(index);
		}
	}
	
	public String getFont() {
		return font;
	}
	public String getFontSize() {
		return fontSize;
	}
	public boolean overrideFont() {
		return overrideFonts.isChecked();
	}
	
}
