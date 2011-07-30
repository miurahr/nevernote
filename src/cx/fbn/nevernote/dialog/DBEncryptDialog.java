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
//* Set the database to be encrypted.
//**********************************************
//**********************************************

import com.trolltech.qt.gui.QComboBox;
import com.trolltech.qt.gui.QDialog;
import com.trolltech.qt.gui.QGridLayout;
import com.trolltech.qt.gui.QIcon;
import com.trolltech.qt.gui.QLabel;
import com.trolltech.qt.gui.QLineEdit;
import com.trolltech.qt.gui.QPushButton;

public class DBEncryptDialog extends QDialog {

	private boolean 	okPressed;
	private final QLineEdit	password1;
	private final QLineEdit	password2;
	private final QPushButton ok;
	private final QComboBox encryptionType;
	private final QLabel encryptionLabel;
	private final String iconPath = new String("classpath:cx/fbn/nevernote/icons/");
	
	
	// Constructor
	public DBEncryptDialog() {
		okPressed = false;
		setWindowTitle(tr("Database Encryption"));
		setWindowIcon(new QIcon(iconPath+"password.png"));
		QGridLayout grid = new QGridLayout();
		setLayout(grid);
		QGridLayout passwordGrid = new QGridLayout();
		QGridLayout buttonGrid = new QGridLayout();
				
		password1 = new QLineEdit();
		password1.setEchoMode(QLineEdit.EchoMode.Password);
		password2 = new QLineEdit();
		password2.setEchoMode(QLineEdit.EchoMode.Password);
		
		password1.textChanged.connect(this, "validateInput()");
		password2.textChanged.connect(this, "validateInput()");
		
		encryptionLabel = new QLabel(tr("Encryption Method"));
		encryptionType = new QComboBox();
		encryptionType.addItem(tr("AES"), "AES");
		encryptionType.addItem(tr("XTEA"), "XTEA");
		
		passwordGrid.addWidget(new QLabel(tr("Password")), 1,1);
		passwordGrid.addWidget(password1, 1, 2);
		passwordGrid.addWidget(new QLabel(tr("Verify Password")), 2,1);
		passwordGrid.addWidget(password2, 2, 2);
		passwordGrid.addWidget(encryptionLabel, 3,1);
		passwordGrid.addWidget(encryptionType, 3,2);
		passwordGrid.setContentsMargins(10, 10,  -10, -10);
		grid.addLayout(passwordGrid,1,1);
		
		
		
		ok = new QPushButton(tr("OK"));
		ok.setEnabled(false);
		ok.clicked.connect(this, "okButtonPressed()");
		QPushButton cancel = new QPushButton(tr("Cancel"));
		cancel.clicked.connect(this, "cancelButtonPressed()");
		buttonGrid.addWidget(ok, 1, 1);
		buttonGrid.addWidget(cancel, 1,2);
		grid.addLayout(buttonGrid,2,1);
	}
	
	public void hideEncryption() {
		encryptionType.setVisible(false);
		encryptionLabel.setVisible(false);
	}
	
	public String getEncryptionMethod() {
		int i = encryptionType.currentIndex();
		return encryptionType.itemData(i).toString();
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
	public String getPassword() {
		return password1.text();
	}
	
	// Check if the OK button was pressed
	public boolean okPressed() {
		return okPressed;
	}
	
	// Validate user input
	public void validateInput() {
		ok.setEnabled(true);
		if (password1.text().trim().equals("")) {
			ok.setEnabled(false);
			return;
		}	
		if (password1.text().length() < 4) {
			ok.setEnabled(false);
			return;
		}
		if (!password1.text().equals(password2.text())) {
			ok.setEnabled(false);
			return;
		}
		if (password1.text().indexOf(" ") > -1) {
			ok.setEnabled(false);
			return;
		}
	}
}
