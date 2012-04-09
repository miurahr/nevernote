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
//* Dialog used when a user tries to stack
//* or unstack a notebook.
//**********************************************
//**********************************************

import java.util.List;

import com.trolltech.qt.gui.QComboBox;
import com.trolltech.qt.gui.QDialog;
import com.trolltech.qt.gui.QGridLayout;
import com.trolltech.qt.gui.QLabel;
import com.trolltech.qt.gui.QPushButton;

public class StackNotebook extends QDialog {
	private boolean 		okPressed;
	private final QComboBox	stack;
	QPushButton 			ok;
	List<String>			currentStacks;
	
	// Constructor
	public StackNotebook() {
		okPressed = false;
		setWindowTitle(tr("Stack Notebook"));
		QGridLayout grid = new QGridLayout();
		setLayout(grid);
		
		QGridLayout textGrid = new QGridLayout();
		stack = new QComboBox();
		stack.setEditable(true);
		textGrid.addWidget(new QLabel(tr("Stack Name")), 1,1);
		textGrid.addWidget(stack, 1, 2);
		textGrid.setContentsMargins(10, 10,-10, -10);
		grid.addLayout(textGrid,1,1);
		
		QGridLayout buttonGrid = new QGridLayout();
		ok = new QPushButton(tr("OK"));
		ok.clicked.connect(this, "okButtonPressed()");
		ok.setEnabled(true);
		QPushButton cancel = new QPushButton(tr("Cancel"));
		cancel.clicked.connect(this, "cancelButtonPressed()");
		stack.currentStringChanged.connect(this, "textChanged(String)");
		buttonGrid.addWidget(ok, 3, 1);
		buttonGrid.addWidget(cancel, 3,2);
		grid.addLayout(buttonGrid,2,1);
	}
	
	// The OK button was pressed
	@SuppressWarnings("unused")
	private void okButtonPressed() {
		okPressed = true;
		close();
	}
	
	// The CANCEL button was pressed
	@SuppressWarnings("unused")
	private void cancelButtonPressed() {
		okPressed = false;
		close();
	}
	
	// Get the name from the field
	public String getStackName() {
		return stack.currentText();
	}
	
	public void setStackNames(List<String> names) {
		currentStacks = names;
		stack.clear();
		for (int i=0; i<names.size(); i++) {
			stack.addItem(names.get(i));
		}
	}
	
	// Set the tag name
	public void setStackName(String name) {
		stack.setEditText(name);
	}
	
	// Check if the OK button was pressed
	public boolean okPressed() {
		return okPressed;
	}
	
	// Set the window title
	public void setTitle(String s) {
		setWindowTitle(s);
	}
	// List of existing tags
	public void setTagList(List<String> t) {
		currentStacks = t;
	}
	// Watch what text is being entered
	@SuppressWarnings("unused")
	private void textChanged(String text) {
		ok.setEnabled(true);
	}
}
