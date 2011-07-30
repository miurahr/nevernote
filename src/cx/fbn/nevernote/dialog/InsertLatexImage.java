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
//* This is the dialog box that pops up when
//* a user tries to insert a LaTeX image.
//**********************************************
//**********************************************

import com.trolltech.qt.core.Qt;
import com.trolltech.qt.gui.QDialog;
import com.trolltech.qt.gui.QGridLayout;
import com.trolltech.qt.gui.QIcon;
import com.trolltech.qt.gui.QLabel;
import com.trolltech.qt.gui.QPushButton;
import com.trolltech.qt.gui.QTextEdit;

public class InsertLatexImage extends QDialog {

	private boolean 	okPressed;
	private final QTextEdit	url;
	private final QPushButton ok;
	private String		latexText;
	private final String iconPath = new String("classpath:cx/fbn/nevernote/icons/");
		
	// Constructor
	public InsertLatexImage() {
		okPressed = false;
		setWindowTitle(tr("Insert LaTeX Formula"));
		setWindowIcon(new QIcon(iconPath+"link.png"));
		QGridLayout grid = new QGridLayout();
		QGridLayout input = new QGridLayout();
		QGridLayout button = new QGridLayout();
		setLayout(grid);
		
		
		url = new QTextEdit("");
		
		input.addWidget(new QLabel(tr("Formula")), 1,1);
		input.addWidget(url, 2, 1);
		input.setContentsMargins(10, 10,  -10, -10);
		grid.addLayout(input, 1,1);
			
		ok = new QPushButton(tr("OK"));
		ok.clicked.connect(this, "accept()");
		ok.setEnabled(false);
		
		QPushButton cancel = new QPushButton(tr("Cancel"));
		cancel.clicked.connect(this, "reject()");
		button.addWidget(ok, 1, 1);
		button.addWidget(cancel, 1,2);
		grid.addLayout(button, 3, 1);
		url.textChanged.connect(this, "validateInput()");
		
		setAttribute(Qt.WidgetAttribute.WA_DeleteOnClose);
	}
	// Set the formula
	public void setFormula(String x) {
		url.setText(x);
	}
	// Get the formula 
	public String getFormula() {
		return latexText;
	}
	// Set the url
	public void setUrl(String u) {
		url.setText(u);
	}
	// Check if the OK button was pressed
	public boolean okPressed() {
		return okPressed;
	}
	// Check that we have a valid URL
	@SuppressWarnings("unused")
	private void validateInput() {
		ok.setEnabled(true);
		if (url.toPlainText().trim().equals("")) 
			ok.setEnabled(false);
	}
	
	@Override
	public void accept() {
		if (ok.isEnabled()) {
			okPressed = true;
			latexText = url.toPlainText();
			super.accept();
		}
	}
	
	@Override
	public void reject() {
		okPressed=false;
		super.reject();
	}
}
