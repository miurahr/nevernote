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

import com.trolltech.qt.gui.QComboBox;
import com.trolltech.qt.gui.QGridLayout;
import com.trolltech.qt.gui.QGroupBox;
import com.trolltech.qt.gui.QHBoxLayout;
import com.trolltech.qt.gui.QRadioButton;
import com.trolltech.qt.gui.QWidget;

public class ConfigShowEditorButtonsPage extends QWidget {
	private final QRadioButton showUndo;
	private final QRadioButton hideUndo;
	private final QRadioButton showRedo;
	private final QRadioButton hideRedo;
	private final QRadioButton showCut;
	private final QRadioButton hideCut;
	private final QRadioButton showCopy;
	private final QRadioButton hideCopy;
	private final QRadioButton showPaste;
	private final QRadioButton hidePaste;
	private final QRadioButton showBold;
	private final QRadioButton hideBold;
	private final QRadioButton showItalic;
	private final QRadioButton hideItalic;
	private final QRadioButton showUnderline;
	private final QRadioButton hideUnderline;
	private final QRadioButton showStrikethrough;
	private final QRadioButton hideStrikethrough;
	private final QRadioButton showHline;
	private final QRadioButton hideHline;
	private final QRadioButton showOutdent;
	private final QRadioButton hideOutdent;
	private final QRadioButton showIndent;
	private final QRadioButton hideIndent;
	private final QRadioButton showBulletList;
	private final QRadioButton hideBulletList;
	private final QRadioButton showNumberList;
	private final QRadioButton hideNumberList;
	private final QRadioButton showFont;
	private final QRadioButton hideFont;
	private final QRadioButton showFontSize;
	private final QRadioButton hideFontSize;
	private final QRadioButton showFontColor;
	private final QRadioButton hideFontColor;
	private final QRadioButton showFontHighlight;
	private final QRadioButton hideFontHighlight;
	private final QRadioButton showAlignLeft;
	private final QRadioButton hideAlignLeft;
	private final QRadioButton showAlignCenter;
	private final QRadioButton hideAlignCenter;
	private final QRadioButton showAlignRight;
	private final QRadioButton hideAlignRight;
	
	QComboBox messageCombo;
	public ConfigShowEditorButtonsPage(QWidget parent) {
		super(parent);

		// Undo Button
		QGroupBox undoGroup =  new QGroupBox(tr("Undo"));
		QHBoxLayout undoLayout = new QHBoxLayout();
		showUndo = new QRadioButton(tr("Show"));
		hideUndo = new QRadioButton(tr("Hide"));
		undoLayout.addWidget(showUndo);
		undoLayout.addWidget(hideUndo);
		undoLayout.setStretch(1, 100);
		undoGroup.setLayout(undoLayout);


		// Redo Button
		QGroupBox redoGroup =  new QGroupBox(tr("Redo"));
		QHBoxLayout redoLayout = new QHBoxLayout();
		showRedo = new QRadioButton(tr("Show"));
		hideRedo = new QRadioButton(tr("Hide"));
		redoLayout.addWidget(showRedo);
		redoLayout.addWidget(hideRedo);
		redoLayout.setStretch(1, 100);
		redoGroup.setLayout(redoLayout);


		// Paste button
		QGroupBox pasteGroup =  new QGroupBox(tr("Paste"));
		QHBoxLayout pasteLayout = new QHBoxLayout();
		showPaste = new QRadioButton(tr("Show"));
		hidePaste = new QRadioButton(tr("Hide"));
		pasteLayout.addWidget(showPaste);
		pasteLayout.addWidget(hidePaste);
		pasteLayout.setStretch(1, 100);
		pasteGroup.setLayout(pasteLayout);

		// Copy button
		QGroupBox copyGroup =  new QGroupBox(tr("Copy"));
		QHBoxLayout copyLayout = new QHBoxLayout();
		showCopy = new QRadioButton(tr("Show"));
		hideCopy = new QRadioButton(tr("Hide"));
		copyLayout.addWidget(showCopy);
		copyLayout.addWidget(hideCopy);
		copyLayout.setStretch(1, 100);
		copyGroup.setLayout(copyLayout);

		
		// Cut Button
		QGroupBox cutGroup =  new QGroupBox(tr("Cut"));
		QHBoxLayout cutLayout = new QHBoxLayout();
		showCut = new QRadioButton(tr("Show"));
		hideCut = new QRadioButton(tr("Hide"));
		cutLayout.addWidget(showCut);
		cutLayout.addWidget(hideCut);
		cutLayout.setStretch(1, 100);
		cutGroup.setLayout(cutLayout);

		// Notebook Column
		QGroupBox underlineGroup =  new QGroupBox(tr("Underline"));
		QHBoxLayout underlineLayout = new QHBoxLayout();
		showUnderline = new QRadioButton(tr("Show"));
		hideUnderline = new QRadioButton(tr("Hide"));
		underlineLayout.addWidget(showUnderline);
		underlineLayout.addWidget(hideUnderline);
		underlineLayout.setStretch(1, 100);
		underlineGroup.setLayout(underlineLayout);

		// Bold Button
		QGroupBox boldGroup =  new QGroupBox(tr("Bold"));
		QHBoxLayout boldLayout = new QHBoxLayout();
		showBold = new QRadioButton(tr("Show"));
		hideBold = new QRadioButton(tr("Hide"));
		boldLayout.addWidget(showBold);
		boldLayout.addWidget(hideBold);
		boldLayout.setStretch(1, 100);
		boldGroup.setLayout(boldLayout);


		// Italic Button
		QGroupBox italicGroup =  new QGroupBox(tr("Italic"));
		QHBoxLayout italicLayout = new QHBoxLayout();
		showItalic = new QRadioButton(tr("Show"));
		hideItalic = new QRadioButton(tr("Hide"));
		italicLayout.addWidget(showItalic);
		italicLayout.addWidget(hideItalic);
		italicLayout.setStretch(1, 100);
		italicGroup.setLayout(italicLayout);

		// Strikethrough Button
		QGroupBox strikethroughGroup =  new QGroupBox(tr("Strikethrough"));
		QHBoxLayout strikethroughLayout = new QHBoxLayout();
		showStrikethrough = new QRadioButton(tr("Show"));
		hideStrikethrough = new QRadioButton(tr("Hide"));
		strikethroughLayout.addWidget(showStrikethrough);
		strikethroughLayout.addWidget(hideStrikethrough);
		strikethroughLayout.setStretch(1, 100);
		strikethroughGroup.setLayout(strikethroughLayout);


		// Hline Button
		QGroupBox hlineGroup =  new QGroupBox(tr("Horizontal Line"));
		QHBoxLayout hlineLayout = new QHBoxLayout();
		showHline = new QRadioButton(tr("Show"));
		hideHline = new QRadioButton(tr("Hide"));
		hlineLayout.addWidget(showHline);
		hlineLayout.addWidget(hideHline);
		hlineLayout.setStretch(1, 100);
		hlineGroup.setLayout(hlineLayout);

		// Outdent Button
		QGroupBox outdentGroup =  new QGroupBox(tr("Outdent"));
		QHBoxLayout outdentLayout = new QHBoxLayout();
		showOutdent = new QRadioButton(tr("Show"));
		hideOutdent = new QRadioButton(tr("Hide"));
		outdentLayout.addWidget(showOutdent);
		outdentLayout.addWidget(hideOutdent);
		outdentLayout.setStretch(1, 100);
		outdentGroup.setLayout(outdentLayout);

		// Indent Button
		QGroupBox indentGroup =  new QGroupBox(tr("Indent"));
		QHBoxLayout indentLayout = new QHBoxLayout();
		showIndent = new QRadioButton(tr("Show"));
		hideIndent = new QRadioButton(tr("Hide"));
		indentLayout.addWidget(showIndent);
		indentLayout.addWidget(hideIndent);
		indentLayout.setStretch(1, 100);
		indentGroup.setLayout(indentLayout);

		// Bullet List Button
		QGroupBox bulletListGroup =  new QGroupBox(tr("Bullet List"));
		QHBoxLayout bulletListLayout = new QHBoxLayout();
		showBulletList = new QRadioButton(tr("Show"));
		hideBulletList = new QRadioButton(tr("Hide"));
		bulletListLayout.addWidget(showBulletList);
		bulletListLayout.addWidget(hideBulletList);
		bulletListLayout.setStretch(1, 100);
		bulletListGroup.setLayout(bulletListLayout);

		// Number List Button
		QGroupBox numberListGroup =  new QGroupBox(tr("Numbered List"));
		QHBoxLayout numberListLayout = new QHBoxLayout();
		showNumberList = new QRadioButton(tr("Show"));
		hideNumberList = new QRadioButton(tr("Hide"));
		numberListLayout.addWidget(showNumberList);
		numberListLayout.addWidget(hideNumberList);
		numberListLayout.setStretch(1, 100);
		numberListGroup.setLayout(numberListLayout);

		// Font drop down list
		QGroupBox fontGroup =  new QGroupBox(tr("Fonts"));
		QHBoxLayout fontLayout = new QHBoxLayout();
		showFont = new QRadioButton(tr("Show"));
		hideFont = new QRadioButton(tr("Hide"));
		fontLayout.addWidget(showFont);
		fontLayout.addWidget(hideFont);
		fontLayout.setStretch(1, 100);
		fontGroup.setLayout(fontLayout);

		// Font sizes drop down list
		QGroupBox fontSizeGroup =  new QGroupBox(tr("Font Size"));
		QHBoxLayout fontSizeLayout = new QHBoxLayout();
		showFontSize = new QRadioButton(tr("Show"));
		hideFontSize = new QRadioButton(tr("Hide"));
		fontSizeLayout.addWidget(showFontSize);
		fontSizeLayout.addWidget(hideFontSize);
		fontSizeLayout.setStretch(1, 100);
		fontSizeGroup.setLayout(fontSizeLayout);
		
		// Font color button
		QGroupBox fontColorGroup =  new QGroupBox(tr("Font Color"));
		QHBoxLayout fontColorLayout = new QHBoxLayout();
		showFontColor = new QRadioButton(tr("Show"));
		hideFontColor = new QRadioButton(tr("Hide"));
		fontColorLayout.addWidget(showFontColor);
		fontColorLayout.addWidget(hideFontColor);
		fontColorLayout.setStretch(1, 100);
		fontColorGroup.setLayout(fontColorLayout);
		
		// highlight button
		QGroupBox fontHighlightGroup =  new QGroupBox(tr("Text Highlight"));
		QHBoxLayout fontHighlightLayout = new QHBoxLayout();
		showFontHighlight = new QRadioButton(tr("Show"));
		hideFontHighlight = new QRadioButton(tr("Hide"));
		fontHighlightLayout.addWidget(showFontHighlight);
		fontHighlightLayout.addWidget(hideFontHighlight);
		fontHighlightLayout.setStretch(1, 100);
		fontHighlightGroup.setLayout(fontHighlightLayout);
				
		// Align Left
		QGroupBox alignLeftGroup =  new QGroupBox(tr("Align Left"));
		QHBoxLayout alignLeftLayout = new QHBoxLayout();
		showAlignLeft = new QRadioButton(tr("Show"));
		hideAlignLeft = new QRadioButton(tr("Hide"));
		alignLeftLayout.addWidget(showAlignLeft);
		alignLeftLayout.addWidget(hideAlignLeft);
		alignLeftLayout.setStretch(1, 100);
		alignLeftGroup.setLayout(alignLeftLayout);
		
		// Align Center
		QGroupBox alignCenterGroup =  new QGroupBox(tr("Align Center"));
		QHBoxLayout alignCenterLayout = new QHBoxLayout();
		showAlignCenter = new QRadioButton(tr("Show"));
		hideAlignCenter = new QRadioButton(tr("Hide"));
		alignCenterLayout.addWidget(showAlignCenter);
		alignCenterLayout.addWidget(hideAlignCenter);
		alignCenterLayout.setStretch(1, 100);
		alignCenterGroup.setLayout(alignCenterLayout);
		
		// Align Right
		QGroupBox alignRightGroup =  new QGroupBox(tr("Align Right"));
		QHBoxLayout alignRightLayout = new QHBoxLayout();
		showAlignRight = new QRadioButton(tr("Show"));
		hideAlignRight = new QRadioButton(tr("Hide"));
		alignRightLayout.addWidget(showAlignRight);
		alignRightLayout.addWidget(hideAlignRight);
		alignRightLayout.setStretch(1, 100);
		alignRightGroup.setLayout(alignRightLayout);
		
		QGridLayout mainLayout = new QGridLayout();
		mainLayout.addWidget(undoGroup, 0,0);
		mainLayout.addWidget(redoGroup, 0,1);
		mainLayout.addWidget(cutGroup, 0,2);
		mainLayout.addWidget(copyGroup, 1,0);
		mainLayout.addWidget(pasteGroup, 1,1);
		mainLayout.addWidget(boldGroup, 1,2);
		mainLayout.addWidget(italicGroup, 2,0);
		mainLayout.addWidget(underlineGroup, 2,1);
		mainLayout.addWidget(strikethroughGroup, 2,2);
		mainLayout.addWidget(hlineGroup, 3,0);
		mainLayout.addWidget(indentGroup, 3,1);
		mainLayout.addWidget(outdentGroup, 3,2);
		mainLayout.addWidget(bulletListGroup, 4,0);
		mainLayout.addWidget(numberListGroup, 4,1);
		mainLayout.addWidget(fontGroup, 5,0);
		mainLayout.addWidget(fontSizeGroup, 5,1);
		mainLayout.addWidget(fontColorGroup, 5,2);
		mainLayout.addWidget(fontHighlightGroup, 4,2);
		mainLayout.addWidget(alignLeftGroup, 6,0);
		mainLayout.addWidget(alignCenterGroup, 6,1);
		mainLayout.addWidget(alignRightGroup, 6,2);

		mainLayout.setColumnStretch(3, 100);
		mainLayout.setRowStretch(7, 100);
		setLayout(mainLayout);
	}
	
	public void setUndo(boolean value) {
		if (value)
			showUndo.click();
		else
			hideUndo.click();
	}
	
	public void setRedo(boolean value) {
		if (value)
			showRedo.click();
		else
			hideRedo.click();
	}

	public void setCut(boolean value) {
		if (value)
			showCut.click();
		else
			hideCut.click();
	}

	public void setCopy(boolean value) {
		if (value)
			showCopy.click();
		else
			hideCopy.click();
	}

	public void setPaste(boolean value) {
		if (value)
			showPaste.click();
		else
			hidePaste.click();
	}
	public void setUnderline(boolean value) {
		if (value)
			showUnderline.click();
		else
			hideUnderline.click();
	}
	public void setBold(boolean value) {
		if (value)
			showBold.click();
		else
			hideBold.click();
	}
	public void setItalic(boolean value) {
		if (value)
			showItalic.click();
		else
			hideItalic.click();
	}
	public void setStrikethrough(boolean value) {
		if (value)
			showStrikethrough.click();
		else
			hideStrikethrough.click();
	}
	public void setHline(boolean value) {
		if (value)
			showHline.click();
		else
			hideHline.click();
	}
	public void setIndent(boolean value) {
		if (value)
			showIndent.click();
		else
			hideIndent.click();
	}
	public void setOutdent(boolean value) {
		if (value)
			showOutdent.click();
		else
			hideOutdent.click();
	}
	public void setBulletList(boolean value) {
		if (value)
			showBulletList.click();
		else
			hideBulletList.click();
	}
	public void setNumberList(boolean value) {
		if (value)
			showNumberList.click();
		else
			hideNumberList.click();
	}
	public void setFont(boolean value) {
		if (value)
			showFont.click();
		else
			hideFont.click();
	}
	public void setFontSize(boolean value) {
		if (value)
			showFontSize.click();
		else
			hideFontSize.click();
	}
	public void setFontColor(boolean value) {
		if (value)
			showFontColor.click();
		else
			hideFontColor.click();
	}
	public void setFontHighlight(boolean value) {
		if (value)
			showFontHighlight.click();
		else
			hideFontHighlight.click();
	}
	public void setAlignLeft(boolean value) {
		if (value)
			showAlignLeft.click();
		else
			hideAlignLeft.click();
	}
	public void setAlignCenter(boolean value) {
		if (value)
			showAlignCenter.click();
		else
			hideAlignCenter.click();
	}
	public void setAlignRight(boolean value) {
		if (value)
			showAlignRight.click();
		else
			hideAlignRight.click();
	}


	
	public boolean showUndo() {
		return showUndo.isChecked();
	}
	public boolean showCut() {
		return showCut.isChecked();
	}
	public boolean showRedo() {
		return showRedo.isChecked();
	}
	public boolean showCopy() {
		return showCopy.isChecked();
	}
	public boolean showPaste() {
		return showPaste.isChecked();
	}
	public boolean showBold() {
		return showBold.isChecked();
	}
	public boolean showUnderline() {
		return showUnderline.isChecked();
	}
	public boolean showItalic() {
		return showItalic.isChecked();
	}
	public boolean showStrikethrough() {
		return showStrikethrough.isChecked();
	}	
	public boolean showHline() {
		return showHline.isChecked();
	}
	public boolean showIndent() {
		return showIndent.isChecked();
	}
	public boolean showOutdent() {
		return showOutdent.isChecked();
	}
	public boolean showNumberList() {
		return showNumberList.isChecked();
	}
	public boolean showBulletList() {
		return showBulletList.isChecked();
	}
	public boolean showFont() {
		return showFont.isChecked();
	}	
	public boolean showFontSize() {
		return showFontSize.isChecked();
	}	
	public boolean showFontColor() {
		return showFontColor.isChecked();
	}	
	public boolean showFontHilight() {
		return showFontHighlight.isChecked();
	}	
	public boolean showAlignCenter() {
		return showAlignCenter.isChecked();
	}	public 
	boolean showAlignLeft() {
		return showAlignLeft.isChecked();
	}
	public boolean showAlignRight() {
		return showAlignRight.isChecked();
	}





}