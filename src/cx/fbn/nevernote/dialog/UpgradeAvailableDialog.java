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
//* Dialog box to notify that a new release
//* is available.
//**********************************************
//**********************************************

import com.trolltech.qt.core.QUrl;
import com.trolltech.qt.gui.QCheckBox;
import com.trolltech.qt.gui.QDialog;
import com.trolltech.qt.gui.QGridLayout;
import com.trolltech.qt.gui.QHBoxLayout;
import com.trolltech.qt.gui.QIcon;
import com.trolltech.qt.gui.QPushButton;
import com.trolltech.qt.gui.QVBoxLayout;
import com.trolltech.qt.webkit.QWebView;

import cx.fbn.nevernote.Global;

public class UpgradeAvailableDialog extends QDialog {

	private boolean 	okPressed;
	private final QPushButton ok;
	private final QCheckBox doNotRemindeMe;
	private final String iconPath = new String("classpath:cx/fbn/nevernote/icons/");
	
	// Constructor
	public UpgradeAvailableDialog() {
		okPressed = false;
		setWindowTitle(tr("Upgrade Available"));
		setWindowIcon(new QIcon(iconPath+"nevernote.png"));
		QVBoxLayout grid = new QVBoxLayout();
		QGridLayout input = new QGridLayout();
		QHBoxLayout button = new QHBoxLayout();
		setLayout(grid);		
			
		QWebView page = new QWebView(this);
		page.setUrl(new QUrl(Global.getUpdateAnnounceUrl()));
		
		doNotRemindeMe = new QCheckBox();
		doNotRemindeMe.setText(tr("Automatically check for updates at startup"));
		input.addWidget(page,1,1);
		doNotRemindeMe.setChecked(true);
		input.addWidget(doNotRemindeMe,2,1);
		
		grid.addLayout(input);
		
		ok = new QPushButton("OK");
		ok.clicked.connect(this, "okButtonPressed()");

		button.addStretch();
		button.addWidget(ok);
		button.addStretch();
		grid.addLayout(button);		
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
	
	public boolean remindMe() {
		return doNotRemindeMe.isChecked();
	}
}
