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



//**********************************************
//**********************************************
//* This dialog will prompt a user to synchronize
//* With Evernote if some information is missing.  
//* This should not need to be called under normal
//* circumstances.
//**********************************************
//**********************************************
package cx.fbn.nevernote.dialog;

import com.trolltech.qt.gui.QCheckBox;
import com.trolltech.qt.gui.QDialog;
import com.trolltech.qt.gui.QHBoxLayout;
import com.trolltech.qt.gui.QIcon;
import com.trolltech.qt.gui.QLabel;
import com.trolltech.qt.gui.QPushButton;
import com.trolltech.qt.gui.QTextBrowser;
import com.trolltech.qt.gui.QVBoxLayout;
import com.trolltech.qt.gui.QWidget;

public class SynchronizationRequiredWarning extends QDialog {
	
	private final QCheckBox neverSynchronize;
	private final QPushButton		okButton;
	
	public SynchronizationRequiredWarning(QWidget parent) {	
		setWindowTitle(tr("Synchronization Required"));
	    String iconPath = new String("classpath:cx/fbn/nevernote/icons/");
	    setWindowIcon(new QIcon(new QIcon(iconPath+"synchronize.png")));
		neverSynchronize = new QCheckBox(tr("I never want to synchronize with Evernote so quit bothering me."));
		okButton = new QPushButton(tr("Ok"));
		QTextBrowser warning = new QTextBrowser();
		warning.setText(tr("Please synchronize with Evernote before proceeding.\n\n" +
				"In order to use this feature you need to synchronize with Evernote.  After synchronizing" +
				" the necessary information will be saved so you won't need to do it again.  \n\n"
				+"If you don't wish to use Evernote's then please check the option at the bottom."));
		
		QVBoxLayout mainLayout = new QVBoxLayout();
		mainLayout.addWidget(warning);
		mainLayout.addWidget(neverSynchronize);
		mainLayout.addWidget(neverSynchronize);
		QHBoxLayout okLayout = new QHBoxLayout();
		okButton.clicked.connect(this, "okClicked()");
		okLayout.addWidget(new QLabel(""));
		okLayout.addWidget(okButton);
		okLayout.addWidget(new QLabel(""));
		okLayout.setStretch(0, 10);
		okLayout.setStretch(2, 10);
		
		mainLayout.addLayout(okLayout);
		setLayout(mainLayout);
	}
	
	public boolean neverSynchronize() {
		return neverSynchronize.isChecked();
	}

	@SuppressWarnings("unused")
	private void okClicked() {
		close();
	}
}
