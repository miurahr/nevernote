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
//* Chaneg or create a notebook
//**********************************************
//**********************************************

import java.util.List;

import com.evernote.edam.type.Notebook;
import com.trolltech.qt.gui.QCheckBox;
import com.trolltech.qt.gui.QDialog;
import com.trolltech.qt.gui.QGridLayout;
import com.trolltech.qt.gui.QIcon;
import com.trolltech.qt.gui.QLabel;
import com.trolltech.qt.gui.QLineEdit;
import com.trolltech.qt.gui.QPushButton;

public class NotebookEdit extends QDialog {
	private boolean 		okPressed;
	private final QLineEdit		notebook;
	private final QCheckBox 	localRemote;
	private final QPushButton		ok;
	private List<Notebook>  currentNotebooks;
	private final QCheckBox		isDefault;
	private boolean startDefault;
	private String startText;
	private List<String> stacks;
	private boolean stackEdit;
	private final QLabel notebookLabel;
	private final String iconPath = new String("classpath:cx/fbn/nevernote/icons/");	
	
	// Constructor
	public NotebookEdit() {
		okPressed = false;
		stackEdit = false;
		setWindowTitle(tr("Add Notebook"));
		setWindowIcon(new QIcon(iconPath+"notebook-green.png"));
		QGridLayout grid = new QGridLayout();
		setLayout(grid);
		
		QGridLayout textLayout = new QGridLayout();
		notebook = new QLineEdit();
		notebookLabel = new QLabel(tr("Notebook Name"));
		textLayout.addWidget(notebookLabel, 1,1);
		textLayout.addWidget(notebook, 1, 2);
		textLayout.setContentsMargins(10, 10,-10, -10);
		grid.addLayout(textLayout,1,1);
		
		localRemote = new QCheckBox();
		localRemote.setText(tr("Local Notebook"));
		localRemote.setChecked(false);
		grid.addWidget(localRemote, 2,1);

		isDefault = new QCheckBox();
		isDefault.setText(tr("Default Notebook"));
		isDefault.setChecked(false);
		isDefault.toggled.connect(this, "defaultNotebookChecked(Boolean)");
		grid.addWidget(isDefault, 3,1);

		QGridLayout buttonLayout = new QGridLayout();
		ok = new QPushButton(tr("OK"));
		ok.clicked.connect(this, "okButtonPressed()");
		ok.setEnabled(false);
		QPushButton cancel = new QPushButton(tr("Cancel"));
		cancel.clicked.connect(this, "cancelButtonPressed()");
		notebook.textChanged.connect(this, "textChanged()");
		buttonLayout.addWidget(ok, 1, 1);
		buttonLayout.addWidget(cancel, 1,2);
		grid.addLayout(buttonLayout,4,1);
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
	
	// Get the userid from the field
	public String getNotebook() {
		return notebook.text();
	}
	
	// Set the stack names
	public void setStacks(List<String> s) {
		stacks = s;
		stackEdit = true;
		notebookLabel.setText(new String(tr("Stack Name")));
	}
	
	// Set the notebook name
	public void setNotebook(String name) {
		if (name.equalsIgnoreCase("All Notebooks")) {
			notebook.setEnabled(false);
			localRemote.setEnabled(false);
			isDefault.setEnabled(false);
		}
		notebook.setText(name);
		startText = name;
	}
	
	// Is this a local notebook?
	public boolean isLocal() {
		return localRemote.isChecked();
	}
	
	// Hide the local/remote checkbox
	public void setLocalCheckboxEnabled(boolean visible) {
		localRemote.setEnabled(visible);
	}
	
	
	// Check if the OK button was pressed
	public boolean okPressed() {
		return okPressed;
	}
	
	// Set the window title
	public void setTitle(String s) {
		setWindowTitle(s);
	}
	
	// set notebooks 
	public void setNotebooks(List<Notebook> n) {
		currentNotebooks = n;
	}
	
	// Get default notebook
	public void setDefaultNotebook(boolean val) {
		startDefault = val;
		isDefault.setChecked(val);
		if (val) 
			isDefault.setEnabled(true);
	}
	public boolean isDefaultNotebook() {
		return isDefault.isChecked();
	}
	
	// Action when the default notebook icon is checked
	@SuppressWarnings("unused")
	private void defaultNotebookChecked(Boolean val) {
		if (val != startDefault || !startText.equals(notebook.text())) 
			ok.setEnabled(true);
		else
			ok.setEnabled(false);
	}
	
	// Hide checkboxes
	public void hideDefaultCheckbox() {
		isDefault.setVisible(false);
	}
	public void hideLocalCheckbox() {
		localRemote.setVisible(false);
	}
	
	// Watch what text is being entered
	@SuppressWarnings("unused")
	private void textChanged() {
		if (notebook.text().equals("")) {
			ok.setEnabled(false);
			return;
		}
		if (notebook.text().equalsIgnoreCase("All Notebooks")) {
			ok.setEnabled(false);
			return;
		}
		if (stackEdit) {
			for (int i=0; i<stacks.size(); i++) {
				if (stacks.get(i).equalsIgnoreCase(notebook.text())) {
					ok.setEnabled(false);
					return;
				}
			}
		}
		if (!stackEdit) {
			if (currentNotebooks == null) {
				ok.setEnabled(false);
				return;
			}
			for (int i=0; i<currentNotebooks.size(); i++) {
				String s = currentNotebooks.get(i).getName();
				if (s.equalsIgnoreCase(notebook.text())) {
					ok.setEnabled(false);
					return;
				}
			}
		}
		ok.setEnabled(true);
	}
}
