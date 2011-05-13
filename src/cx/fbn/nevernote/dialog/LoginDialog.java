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

//**********************************************
//**********************************************
//* Login to Evernote
//**********************************************
//**********************************************

import com.trolltech.qt.gui.QDialog;
import com.trolltech.qt.gui.QGridLayout;
import com.trolltech.qt.gui.QIcon;
import com.trolltech.qt.gui.QLabel;
import com.trolltech.qt.gui.QLineEdit;
import com.trolltech.qt.gui.QPushButton;

import cx.fbn.nevernote.Global;

public class LoginDialog extends QDialog {

	private boolean 	okPressed;
	private final QLineEdit	userid;
	private final QLineEdit	password;
	private final QPushButton ok;
	private final String iconPath = new String("classpath:cx/fbn/nevernote/icons/");
	
	// Constructor
	public LoginDialog() {
		okPressed = false;
		setWindowTitle(tr("NeverNote Login"));
		setWindowIcon(new QIcon(iconPath+"password.png"));
		QGridLayout grid = new QGridLayout();
		setLayout(grid);
		QGridLayout passwordGrid = new QGridLayout();
		QGridLayout buttonGrid = new QGridLayout();
		
		String useridValue = Global.username;
		String passwordValue = Global.password;
		
		userid = new QLineEdit(useridValue);
		password = new QLineEdit(passwordValue);
		password.setEchoMode(QLineEdit.EchoMode.Password);
		
		userid.textChanged.connect(this, "validateInput()");
		password.textChanged.connect(this, "validateInput()");
		
		passwordGrid.addWidget(new QLabel(tr("Userid")), 1,1);
		passwordGrid.addWidget(userid, 1, 2);
		passwordGrid.addWidget(new QLabel(tr("Password")), 2,1);
		passwordGrid.addWidget(password, 2, 2);
		passwordGrid.setContentsMargins(10, 10,  -10, -10);
		grid.addLayout(passwordGrid,1,1);
		
		ok = new QPushButton(tr("OK"));
		ok.clicked.connect(this, "okButtonPressed()");
		QPushButton cancel = new QPushButton(tr("Cancel"));
		cancel.clicked.connect(this, "cancelButtonPressed()");
		buttonGrid.addWidget(ok, 1, 1);
		buttonGrid.addWidget(cancel, 1,2);
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
	
	// Get the userid from the field
	public String getUserid() {
		return userid.text();
	}
	
	// Get the password 
	public String getPassword() {
		return password.text();
	}
	
	// Check if the OK button was pressed
	public boolean okPressed() {
		return okPressed;
	}
	
	// Validate user input
	public void validateInput() {
		ok.setEnabled(true);
		if (userid.text().trim().equals("")) {
			ok.setEnabled(false);
			return;
		}		if (password.text().trim().equals("")) {
			ok.setEnabled(false);
			return;
		}
	}
}
