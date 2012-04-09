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

//**********************************************
//**********************************************
//* Show the spell check dialog
//**********************************************
//**********************************************

import java.util.List;

import com.swabunga.spell.engine.Word;
import com.swabunga.spell.event.SpellChecker;
import com.trolltech.qt.core.Qt.AlignmentFlag;
import com.trolltech.qt.gui.QDialog;
import com.trolltech.qt.gui.QGridLayout;
import com.trolltech.qt.gui.QIcon;
import com.trolltech.qt.gui.QLabel;
import com.trolltech.qt.gui.QLineEdit;
import com.trolltech.qt.gui.QListWidget;
import com.trolltech.qt.gui.QPushButton;

public class SpellCheck extends QDialog {

	private boolean 	replacePressed;
	private boolean		cancelPressed;
	private final QLabel	currentWord;
	private final QLineEdit	replacementWord;
	private String misspelledWord;
	private final QPushButton replace;
	private final QPushButton ignore;
	private final QPushButton ignoreAll;
	private final QPushButton addToDictionary;
	private final QListWidget suggestions;
	private final SpellChecker checker;
	private final String iconPath = new String("classpath:cx/fbn/nevernote/icons/");
	
	
	// Constructor
	public SpellCheck(SpellChecker checker) {
		setWindowIcon(new QIcon(iconPath+"spellCheck.png"));
		replacePressed = false;
		cancelPressed = false;
		this.checker = checker;
		setWindowTitle(tr("Spell Check"));
		QGridLayout grid = new QGridLayout();
		setLayout(grid);
		QGridLayout suggestionGrid = new QGridLayout();
		QGridLayout buttonGrid = new QGridLayout();
		
		currentWord = new QLabel(misspelledWord);
		replacementWord = new QLineEdit();
		suggestions = new QListWidget();
		
		replacementWord.textChanged.connect(this, "validateInput()");
		suggestions.itemSelectionChanged.connect(this, "replacementChosen()");
		
		suggestionGrid.addWidget(currentWord, 1, 1);
		suggestionGrid.addWidget(new QLabel(tr("Suggestion")), 2,1);
		suggestionGrid.addWidget(replacementWord, 3, 1);
		suggestionGrid.addWidget(suggestions,4,1);
		suggestionGrid.setContentsMargins(10, 10,  -10, -10);
		grid.addLayout(suggestionGrid,1,1);
		
		replace = new QPushButton(tr("Replace"));
		ignore = new QPushButton(tr("Ignore"));
		ignoreAll = new QPushButton(tr("Ignore All"));
		addToDictionary = new QPushButton(tr("Add To Dictionary"));
		replace.clicked.connect(this, "replaceButtonPressed()");
		ignore.clicked.connect(this, "ignoreButtonPressed()");
		ignoreAll.clicked.connect(this, "ignoreAllButtonPressed()");
		addToDictionary.clicked.connect(this, "addToDictionaryButtonPressed()");
		QPushButton cancel = new QPushButton(tr("Cancel"));
		cancel.clicked.connect(this, "cancelButtonPressed()");
		suggestionGrid.addWidget(replace, 1, 2);
		suggestionGrid.addWidget(ignore, 2, 2);
		suggestionGrid.addWidget(ignoreAll,3,2);
		suggestionGrid.addWidget(addToDictionary,4,2);
		suggestionGrid.setAlignment(addToDictionary, AlignmentFlag.AlignTop);
		buttonGrid.addWidget(new QLabel(), 1,1);
		buttonGrid.addWidget(cancel, 1,2);
		buttonGrid.addWidget(new QLabel(), 1,3);
		buttonGrid.setColumnStretch(1, 10);
		buttonGrid.setColumnStretch(3, 10);
		grid.addLayout(buttonGrid,2,1);
	}
	
	// The OK button was pressed
	@SuppressWarnings("unused")
	private void replaceButtonPressed() {
		replacePressed = true;
		cancelPressed = false;
		close();
	}
	
	// The CANCEL button was pressed
	@SuppressWarnings("unused")
	private void cancelButtonPressed() {
		replacePressed = false;
		cancelPressed = true;
		close();
	}
	
	// The ignore button was pressed
	@SuppressWarnings("unused")
	private void ignoreButtonPressed() {
		replacePressed = false;
		cancelPressed = false;
		close();
	}
	
	// The ignore button was pressed
	@SuppressWarnings("unused")
	private void ignoreAllButtonPressed() {
		checker.ignoreAll(misspelledWord);
		close();
	}
	
	// Get the userid from the field
	public String getReplacementWord() {
		return replacementWord.text();
	}
	
	// Set the current misspelled word
	public void setWord(String w) {
		misspelledWord = w;
		currentWord.setText(tr("Word: ") +misspelledWord);
	}
	
	// Check if the OK button was pressed
	public boolean replacePressed() {
		return replacePressed;
	}
	
	// Check if the OK button was pressed
	public boolean cancelPressed() {
		return cancelPressed;
	}
	
	// Validate user input
	public void validateInput() {
		replace.setEnabled(true);
		suggestions.clear();
		if (replacementWord.text().trim().equals("")) {
			replace.setEnabled(false);
			return;
		}
		
		@SuppressWarnings("unchecked")
		List<Word> values = checker.getSuggestions(replacementWord.text(), 10);
		for (int i=0; i<values.size(); i++) {
			suggestions.addItem(values.get(i).toString());
		}
	}
	
	@SuppressWarnings("unused")
	private void replacementChosen() {
		String sel = suggestions.currentItem().text();
		replacementWord.setText(sel);
	}
	
	//Add an item to the dictionary
	@SuppressWarnings("unused")
	private void addToDictionaryButtonPressed() {
		checker.addToDictionary(misspelledWord);
		this.close();
	}
	
	// Add a suggestion
	public void addSuggestion(String word) {
		suggestions.addItem(word);
	}
	
	// Set the current suggestion
	public void setCurrentSuggestion(String word) {
		replacementWord.setText(word);
	}
	
	// Empty out the list
	public void clearSuggestions() {
		suggestions.clear();
	}
	
	public void setSelectedSuggestion(int index) {
		if (index < suggestions.count())
			suggestions.setCurrentRow(index);
	}
}
