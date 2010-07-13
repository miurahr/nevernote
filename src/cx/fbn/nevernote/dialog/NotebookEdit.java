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

import java.util.List;

import com.evernote.edam.type.Notebook;
import com.trolltech.qt.gui.QCheckBox;
import com.trolltech.qt.gui.QDialog;
import com.trolltech.qt.gui.QGridLayout;
import com.trolltech.qt.gui.QLabel;
import com.trolltech.qt.gui.QLineEdit;
import com.trolltech.qt.gui.QPushButton;

public class NotebookEdit extends QDialog {
	private boolean 		okPressed;
	private final QLineEdit		notebook;
	private final QCheckBox 	localRemote;
	private final QPushButton		ok;
	private List<Notebook>  currentNotebooks;
		
	// Constructor
	public NotebookEdit() {
		okPressed = false;
		setWindowTitle("Add Notebook");
		QGridLayout grid = new QGridLayout();
		setLayout(grid);
		
		QGridLayout textLayout = new QGridLayout();
		notebook = new QLineEdit();
		textLayout.addWidget(new QLabel("Notebook Name"), 1,1);
		textLayout.addWidget(notebook, 1, 2);
		textLayout.setContentsMargins(10, 10,-10, -10);
		grid.addLayout(textLayout,1,1);
		
		localRemote = new QCheckBox();
		localRemote.setText("Local Notebook");
		localRemote.setChecked(false);
		grid.addWidget(localRemote, 2,1);
		
		QGridLayout buttonLayout = new QGridLayout();
		ok = new QPushButton("OK");
		ok.clicked.connect(this, "okButtonPressed()");
		ok.setEnabled(false);
		QPushButton cancel = new QPushButton("Cancel");
		cancel.clicked.connect(this, "cancelButtonPressed()");
		notebook.textChanged.connect(this, "textChanged()");
		buttonLayout.addWidget(ok, 1, 1);
		buttonLayout.addWidget(cancel, 1,2);
		grid.addLayout(buttonLayout,3,1);
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
	
	// Set the notebook name
	public void setNotebook(String name) {
		notebook.setText(name);
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
		ok.setEnabled(true);
	}
}
