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
//* Create or edit a tag
//**********************************************
//**********************************************

import com.trolltech.qt.gui.QDialog;
import com.trolltech.qt.gui.QGridLayout;
import com.trolltech.qt.gui.QGroupBox;
import com.trolltech.qt.gui.QIcon;
import com.trolltech.qt.gui.QPushButton;
import com.trolltech.qt.gui.QRadioButton;
import com.trolltech.qt.gui.QTextBrowser;
import com.trolltech.qt.gui.QVBoxLayout;

public class SharedNotebookSyncError extends QDialog {
	private boolean 	okPressed;
	QPushButton ok;
	public final QRadioButton doNothing;
	public final QRadioButton deleteNotebook;
	public final QRadioButton convertToLocal;
	public final QRadioButton convertToShared;
	private final QGroupBox choiceGroup;
	private final String iconPath = new String("classpath:cx/fbn/nevernote/icons/");
	
	// Constructor
	public SharedNotebookSyncError(String notebook) {
		okPressed = false;
		setWindowTitle(tr("Shared Notebook Synchronization Error"));
		QGridLayout grid = new QGridLayout();
		setWindowIcon(new QIcon(iconPath+"synchronize.png"));
		QVBoxLayout vLayout = new QVBoxLayout();
		setLayout(vLayout);

		QTextBrowser msg1 = new QTextBrowser();
		msg1.setText(tr("There was an error with notebook ") +notebook 
				+tr("\nThe most probable reason is that the owner of the notebook has revoked your authority to view it.\n\n")
				+tr("Below are the choices available to resolve this issue."));
		vLayout.addWidget(msg1);
		
		choiceGroup = new QGroupBox(this);
		doNothing = new QRadioButton(this);
		doNothing.setChecked(true);
		doNothing.setText(tr("Do nothing and ask me later."));
		deleteNotebook = new QRadioButton(this);
		deleteNotebook.setText(tr("Permanently delete this notebook & all notes"));
		convertToLocal = new QRadioButton(this);
		convertToLocal.setText(tr("Convert this notebook to a local notebook and keep all notes"));
		convertToShared = new QRadioButton(this);
		convertToShared.setText(tr("Convert this notebook to a shared notebook and keep all notes"));
		
		QVBoxLayout optionLayout = new QVBoxLayout();
		optionLayout.addWidget(doNothing);
		optionLayout.addWidget(deleteNotebook);
//		optionLayout.addWidget(convertToLocal);
//		optionLayout.addWidget(convertToShared);
		choiceGroup.setLayout(optionLayout);
		vLayout.addWidget(choiceGroup);
		
		QGridLayout buttonGrid = new QGridLayout();
		ok = new QPushButton(tr("OK"));
		ok.clicked.connect(this, "okButtonPressed()");
		QPushButton cancel = new QPushButton(tr("Cancel"));
		cancel.clicked.connect(this, "cancelButtonPressed()");
		buttonGrid.addWidget(ok, 3, 1);
		buttonGrid.addWidget(cancel, 3,2);
		grid.addLayout(buttonGrid,3,1);
		vLayout.addLayout(grid);
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
	
	// Check if the OK button was pressed
	public boolean okPressed() {
		return okPressed;
	}
	
	// Set the window title
	public void setTitle(String s) {
		setWindowTitle(s);
	}
}
