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
//* search for text witihn a note.
//**********************************************
//**********************************************


import com.trolltech.qt.gui.QCheckBox;
import com.trolltech.qt.gui.QDialog;
import com.trolltech.qt.gui.QGridLayout;
import com.trolltech.qt.gui.QIcon;
import com.trolltech.qt.gui.QLabel;
import com.trolltech.qt.gui.QLineEdit;
import com.trolltech.qt.gui.QPushButton;
import com.trolltech.qt.webkit.QWebPage;

public class FindDialog extends QDialog {

	private final QLineEdit	text;
	private final QCheckBox wrap;
	private final QCheckBox forward;
	private final QCheckBox backward;
	private final QCheckBox caseSensitive;
	private final QPushButton ok;
	private final String iconPath = new String("classpath:cx/fbn/nevernote/icons/");
	
	// Constructor
	public FindDialog() {
		setWindowTitle(tr("Find"));
		setWindowIcon(new QIcon(iconPath+"search.png"));
		QGridLayout grid = new QGridLayout();
		setLayout(grid);

		text = new QLineEdit();
		wrap = new QCheckBox();
		forward = new QCheckBox();
		backward = new QCheckBox();
		caseSensitive = new QCheckBox();

		QGridLayout textGrid = new QGridLayout();
		textGrid.addWidget(new QLabel(tr("Text")), 1,1);
		textGrid.addWidget(text, 1, 2);
		grid.addLayout(textGrid, 1, 1);
		
		QGridLayout opt = new QGridLayout();
		opt.addWidget(new QLabel(tr("Case Sensitive")), 1,1);
		opt.addWidget(caseSensitive, 1, 2);
		opt.addWidget(new QLabel(tr("Forward")), 2,1);
		opt.addWidget(forward, 2, 2);
		opt.addWidget(new QLabel(tr("Backward")), 3,1);
		opt.addWidget(backward, 3, 2);
		opt.addWidget(new QLabel(tr("Wrap")), 4,1);
		opt.addWidget(wrap, 4, 2);
		opt.setContentsMargins(10, 10,  -10, -10);
		grid.addLayout(opt,2,1);
		
		forward.clicked.connect(this, "forwardClicked()");
		backward.clicked.connect(this, "backwardClicked()");
		
		QGridLayout buttonLayout = new QGridLayout();
		ok = new QPushButton(tr("OK"));
		ok.clicked.connect(this, "okButtonPressed()");
		QPushButton cancel = new QPushButton(tr("Close"));
		cancel.clicked.connect(this, "closeButtonPressed()");
		buttonLayout.addWidget(ok, 1, 1);
		buttonLayout.addWidget(cancel, 1,2);
		grid.addLayout(buttonLayout,3,1);
	}
	
	
	//set focus on show
	public void setFocusOnTextField() {
		text.setFocus();
	}
	
	// The OK button was pressed
	@SuppressWarnings("unused")
	private void okButtonPressed() {
//		close();
	}
	// The CANCEL button was pressed
	@SuppressWarnings("unused")
	private void closeButtonPressed() {
		close();
	}
	// Check if the OK button was pressed
	public void okPressed() {
		return;
	}
	// The forward checkbox was clicked
	@SuppressWarnings("unused")
	private void forwardClicked() {
		backward.setChecked(false);
	}
	// The backward checkbox was clicked
	@SuppressWarnings("unused")
	private void backwardClicked() {
		forward.setChecked(false);
	}
	// Return search flags
	public QWebPage.FindFlags getFlags() {
		QWebPage.FindFlags flags = new QWebPage.FindFlags();
		
		if (wrap.isChecked()) 
			flags.set(QWebPage.FindFlag.FindWrapsAroundDocument);
		if (backward.isChecked()) 
			flags.set(QWebPage.FindFlag.FindBackward);
		if (caseSensitive.isChecked()) 
			flags.set(QWebPage.FindFlag.FindCaseSensitively);
					
		return flags;
	}
	// Return search text
	public String getText() {
		return text.text();
	}
	public QPushButton getOkButton() {
		return ok;
	}
}
