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
//* decrypt text in a note
//**********************************************
//**********************************************

import com.trolltech.qt.gui.QCheckBox;
import com.trolltech.qt.gui.QDialog;
import com.trolltech.qt.gui.QGridLayout;
import com.trolltech.qt.gui.QIcon;
import com.trolltech.qt.gui.QLabel;
import com.trolltech.qt.gui.QLineEdit;
import com.trolltech.qt.gui.QPushButton;

public class EnDecryptDialog extends QDialog {

	private boolean 	okPressed;
	private final QLineEdit	password;
	private final QLineEdit password2;
	private final QLabel hint;
	private final QPushButton ok;
	private final QLabel error;
	private final QCheckBox permanent;
	private final QCheckBox remember;
	private final String iconPath = new String("classpath:cx/fbn/nevernote/icons/");
	
	// Constructor
	public EnDecryptDialog() {
		okPressed = false;
		setWindowTitle(tr("Decrypt Text"));
		setWindowIcon(new QIcon(iconPath+"password.png"));
		QGridLayout grid = new QGridLayout();
		QGridLayout input = new QGridLayout();
		QGridLayout msgGrid = new QGridLayout();
		QGridLayout button = new QGridLayout();
		setLayout(grid);
		
		
		hint = new QLabel("");
		password = new QLineEdit("");
		password.setEchoMode(QLineEdit.EchoMode.Password);
		password2 = new QLineEdit("");
		password2.setEchoMode(QLineEdit.EchoMode.Password);
		
		
		input.addWidget(new QLabel(tr("Password")), 1,1);
		input.addWidget(password, 1, 2);
		input.addWidget(new QLabel(tr("Verify")), 2,1);
		input.addWidget(password2, 2, 2);
		
		permanent = new QCheckBox();
		permanent.setText(tr("Permanently Decrypt"));
		input.addWidget(permanent,3,2);

		remember = new QCheckBox();
		remember.setText(tr("Remember For This Session"));
		input.addWidget(remember,4,2);
		
		input.setContentsMargins(10, 10,  -10, -10);
		grid.addLayout(input, 1,1);
		
		msgGrid.addWidget(new QLabel(tr("Hint: ")), 1,1);
		msgGrid.addWidget(hint, 1, 2);
		msgGrid.addWidget(new QLabel(""), 1,3);
		msgGrid.setColumnStretch(3, 100);
		error = new QLabel();
		msgGrid.addWidget(error, 2, 2);
		grid.addLayout(msgGrid, 2, 1);		
		
		ok = new QPushButton("OK");
		ok.clicked.connect(this, "okButtonPressed()");
		ok.setEnabled(false);
		
		QPushButton cancel = new QPushButton(tr("Cancel"));
		cancel.clicked.connect(this, "cancelButtonPressed()");
		button.addWidget(ok, 1, 1);
		button.addWidget(cancel, 1,2);
		grid.addLayout(button, 3, 1);
		
		password.textChanged.connect(this, "validateInput()");
		password2.textChanged.connect(this, "validateInput()");
		
	}
	public boolean permanentlyDecrypt() {
		return permanent.isChecked();
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
	public void setHint(String h) {
		hint.setText(h.replace("&apos;", "'"));
	}
	public String getHint() {
		return hint.text();
	}
	// Set the error message
	public void setError(String e) {
		error.setText(e);
	}
	// Check if the OK button was pressed
	public boolean okPressed() {
		return okPressed;
	}
	// Check if we should remember the password
	public boolean rememberPassword() {
		return remember.isChecked();
	}
	// Check if proper input was input
	@SuppressWarnings("unused")
	private void validateInput() {
		ok.setEnabled(false);
		error.setText("");
		if (!password.text().equals(password2.text())) {
			error.setText("Passwords do not match");
			return;
		}
		
		ok.setEnabled(true);
	}
}
