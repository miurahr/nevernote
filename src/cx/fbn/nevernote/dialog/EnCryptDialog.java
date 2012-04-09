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
//* This is the dialog when a user tries to 
//* encrypt text in a note
//**********************************************
//**********************************************


import com.trolltech.qt.gui.QDialog;
import com.trolltech.qt.gui.QGridLayout;
import com.trolltech.qt.gui.QIcon;
import com.trolltech.qt.gui.QLabel;
import com.trolltech.qt.gui.QLineEdit;
import com.trolltech.qt.gui.QPushButton;

public class EnCryptDialog extends QDialog {

	private boolean 	okPressed;
	private final QLineEdit	password;
	private final QLineEdit password2;
	private final QLineEdit hint;
	private final QPushButton ok;
	private final QLabel error;
	private final String iconPath = new String("classpath:cx/fbn/nevernote/icons/");
	
	
	// Constructor
	public EnCryptDialog() {
		okPressed = false;
		setWindowTitle(tr("Encrypt Text"));
		setWindowIcon(new QIcon(iconPath+"password.png"));
		QGridLayout grid = new QGridLayout();
		QGridLayout input = new QGridLayout();
		QGridLayout msgGrid = new QGridLayout();
		QGridLayout button = new QGridLayout();
		setLayout(grid);
		
		
		hint = new QLineEdit("");
		password = new QLineEdit("");
		password.setEchoMode(QLineEdit.EchoMode.Password);
		password2 = new QLineEdit("");
		password2.setEchoMode(QLineEdit.EchoMode.Password);
		
		
		input.addWidget(new QLabel(tr("Password")), 1,1);
		input.addWidget(password, 1, 2);
		input.addWidget(new QLabel(tr("Verify")), 2,1);
		input.addWidget(password2, 2, 2);
		input.addWidget(new QLabel(tr("Hint")), 3,1);
		input.addWidget(hint, 3, 2);
		input.setContentsMargins(10, 10,  -10, -10);
		grid.addLayout(input, 1,1);
		
		error = new QLabel();
		msgGrid.addWidget(error, 1, 1);
		grid.addLayout(msgGrid, 2, 1);
		
		ok = new QPushButton(tr("OK"));
		ok.clicked.connect(this, "okButtonPressed()");
		ok.setEnabled(false);
		
		QPushButton cancel = new QPushButton(tr("Cancel"));
		cancel.clicked.connect(this, "cancelButtonPressed()");
		button.addWidget(ok, 1, 1);
		button.addWidget(cancel, 1,2);
		grid.addLayout(button, 3, 1);
		
		password.textChanged.connect(this, "validateInput()");
		password2.textChanged.connect(this, "validateInput()");
		hint.textChanged.connect(this, "validateInput()");
		
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
	// Get the the validated password from the field
	public String getPasswordVerify() {
		return password2.text();
	}
	// Get the password 
	public String getPassword() {
		return password.text();
	}
	// Get the password hint
	public String getHint() {
			return hint.text();
	}
	// Check if the OK button was pressed
	public boolean okPressed() {
		return okPressed;
	}
	// Check if proper input was input
	@SuppressWarnings("unused")
	private void validateInput() {
		ok.setEnabled(false);
		error.setText("");
		if (password.text().length()<4) {
			error.setText(tr("Password must be at least 4 characters"));
			return;
		}
		if (!password.text().equals(password2.text())) {
			error.setText(tr("Passwords do not match"));
			return;
		}
		if (hint.text().trim().equals("")) {
			error.setText(tr("Hint must be entered"));
			return;
		}
		
		ok.setEnabled(true);
	}
}
