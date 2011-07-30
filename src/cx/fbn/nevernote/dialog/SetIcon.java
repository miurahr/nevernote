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
//* Show the "set icon" dialog box for notebooks, tags, etc...
//**********************************************
//**********************************************


import com.trolltech.qt.gui.QCheckBox;
import com.trolltech.qt.gui.QDialog;
import com.trolltech.qt.gui.QFileDialog;
import com.trolltech.qt.gui.QFileDialog.AcceptMode;
import com.trolltech.qt.gui.QFileDialog.FileMode;
import com.trolltech.qt.gui.QGridLayout;
import com.trolltech.qt.gui.QIcon;
import com.trolltech.qt.gui.QPushButton;
import com.trolltech.qt.gui.QSizePolicy.Policy;

import cx.fbn.nevernote.Global;

public class SetIcon extends QDialog {
	private boolean okPressed;
	QPushButton 	ok;
	QPushButton		iconButton;
	QCheckBox		useDefault;
	QIcon			defaultIcon;
	boolean			startUseDefault;
	String			path;
	
	private final String iconPath = new String("classpath:cx/fbn/nevernote/icons/");
	
	// Constructor
	public SetIcon(QIcon i, String path) {
		okPressed = false;
		this.path = path;
		setWindowTitle(tr("Set Icon"));
		QGridLayout grid = new QGridLayout();
		setWindowIcon(new QIcon(iconPath+"nevernote.png"));
		setLayout(grid);
		
		QGridLayout textGrid = new QGridLayout();
		textGrid.setContentsMargins(10, 10,-10, -10);
		useDefault = new QCheckBox();
		iconButton = new QPushButton();
		iconButton.setSizePolicy(Policy.Fixed, Policy.Fixed);
		iconButton.clicked.connect(this, "iconButtonPressed()");
		useDefault.setText(tr("Use Default"));
		iconButton.setIcon(i);
		
		textGrid.addWidget(iconButton,1,1);
		textGrid.addWidget(useDefault,2,1);
		useDefault.clicked.connect(this, "useDefaultIconChecked(Boolean)");
		grid.addLayout(textGrid,1,1);
		
		QGridLayout buttonGrid = new QGridLayout();
		ok = new QPushButton(tr("OK"));
		ok.clicked.connect(this, "okButtonPressed()");
		ok.setEnabled(false);
		QPushButton cancel = new QPushButton(tr("Cancel"));
		cancel.clicked.connect(this, "cancelButtonPressed()");
		buttonGrid.addWidget(ok, 3, 1);
		buttonGrid.addWidget(cancel, 3,2);
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
	
	// Check if the OK button was pressed
	public boolean okPressed() {
		return okPressed;
	}
	
	// Icon Button pressed
	@SuppressWarnings("unused")
	private void iconButtonPressed() {
		QFileDialog fd = new QFileDialog(this);
		fd.setFileMode(FileMode.ExistingFile);
		fd.setConfirmOverwrite(true);
		fd.setWindowTitle(tr("Icon"));
		fd.setFilter(tr("PNG (*.png);;All Files (*.*)"));
		fd.setAcceptMode(AcceptMode.AcceptOpen);
		if (path == null || path.equals(""))
			fd.setDirectory(Global.getFileManager().getImageDirPath(""));
		else
			fd.setDirectory(path);
		if (fd.exec() == 0 || fd.selectedFiles().size() == 0) {
			return;
		}
		
		this.path = fd.selectedFiles().get(0);
		this.path = path.substring(0,path.lastIndexOf("/"));
		ok.setEnabled(true);
		String path = fd.selectedFiles().get(0);
		iconButton.setIcon(new QIcon(path));
		iconButton.setSizePolicy(Policy.Fixed, Policy.Fixed);
	}
	
	public void setUseDefaultIcon(boolean val) {
		useDefault.setChecked(val);
		iconButton.setEnabled(!val);
		startUseDefault = val;
	}
	
	public QIcon getIcon() {
		if (useDefault.isChecked())
			return null;
		return iconButton.icon();
	}
	
	public void useDefaultIconChecked(Boolean value) {
		iconButton.setEnabled(!value);
		
		if (value != startUseDefault) 
			ok.setEnabled(true);
		else
			ok.setEnabled(false);
	}
	
	public String getFileType() {
		return "PNG";
	}
	
	public String getPath() {
		return path;
	}
}
