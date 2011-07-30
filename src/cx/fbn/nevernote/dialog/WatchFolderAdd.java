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
//* dialog box used to add a watched folder
//**********************************************
//**********************************************

import java.util.List;

import com.evernote.edam.type.Notebook;
import com.trolltech.qt.gui.QComboBox;
import com.trolltech.qt.gui.QDialog;
import com.trolltech.qt.gui.QFileDialog;
import com.trolltech.qt.gui.QFileDialog.FileMode;
import com.trolltech.qt.gui.QGridLayout;
import com.trolltech.qt.gui.QHBoxLayout;
import com.trolltech.qt.gui.QIcon;
import com.trolltech.qt.gui.QLabel;
import com.trolltech.qt.gui.QPushButton;
import com.trolltech.qt.gui.QVBoxLayout;

import cx.fbn.nevernote.sql.WatchFolderRecord;

public class WatchFolderAdd extends QDialog {
	private final QPushButton		okButton;
	private final QPushButton		cancelButton;
	private boolean					okClicked;
	private final List<Notebook>	notebooks;
	private final WatchFolderRecord record;
	public final QLabel			directory;
	public final QComboBox		keep;
	public final QComboBox		books;
	private final String iconPath = new String("classpath:cx/fbn/nevernote/icons/");
	
	public WatchFolderAdd(WatchFolderRecord w, List<Notebook> n) {
		setWindowIcon(new QIcon(iconPath+"folder.png"));
		okClicked = false;
		notebooks = n;
		record = w;
		
		okButton = new QPushButton();
		okButton.setText(tr("OK"));
		okButton.pressed.connect(this, "onClicked()");
		
		cancelButton = new QPushButton();
		cancelButton.setText(tr("Cancel"));
		cancelButton.pressed.connect(this, "onCancel()");
		
		QPushButton folderButton = new QPushButton();
		folderButton.setText(tr("Directory"));
		folderButton.clicked.connect(this, "folderButtonClicked()");
		
		directory = new QLabel();
		if (record != null)
			directory.setText(record.folder);
		else 
			directory.setText(System.getProperty("user.home"));
		
		keep = new QComboBox();
		keep.addItem(tr("Keep"),"Keep");
		keep.addItem(tr("Delete"),"Delete");
		if (record != null) {
			if (record.keep)
				keep.setCurrentIndex(0);
			else
				keep.setCurrentIndex(1);
		}
		
		books = new QComboBox();
		for (int i=0; i<notebooks.size(); i++) {
			books.addItem(notebooks.get(i).getName());
			if (record != null) {
				if (record.notebook.equals(notebooks.get(i).getName()))
					books.setCurrentIndex(i);
			}
		}
		
		QGridLayout grid = new QGridLayout();
		grid.addWidget(directory,0,1);
		grid.addWidget(folderButton,0,0);
		grid.addWidget(new QLabel(tr("Notebook")),1,0);
		grid.addWidget(books,1,1);
		grid.addWidget(new QLabel(tr("After Import")), 2,0);
		grid.addWidget(keep,2,1);
				
		QHBoxLayout buttonLayout = new QHBoxLayout();
		buttonLayout.addStretch(1);
		buttonLayout.addWidget(okButton);
		buttonLayout.addWidget(cancelButton);
		setWindowTitle(tr("Add Import Folder"));	
				
			
		QVBoxLayout mainLayout = new QVBoxLayout();
		mainLayout.addLayout(grid);
		mainLayout.addSpacing(1);
		mainLayout.addLayout(buttonLayout);
		setLayout(mainLayout);
				
	}
	
	@SuppressWarnings("unused")
	private void onClicked() {
		okClicked = true;
		close();
	}
	
	@SuppressWarnings("unused")
	private void onCancel() {
		okClicked = false;
		close();
	}
	
	public boolean okClicked() {
		return okClicked;
	}
	
	@SuppressWarnings("unused")
	private void itemSelected() {
		okButton.setEnabled(true);
	}
	
	@SuppressWarnings("unused")
	private void folderButtonClicked() {
		QFileDialog fileDialog = new QFileDialog();
		fileDialog.setFileMode(FileMode.DirectoryOnly);
		fileDialog.fileSelected.connect(this, "folderSelected(String)");
		fileDialog.exec();
	}
	
	@SuppressWarnings("unused")
	private void folderSelected(String f) {
		 String whichOS = System.getProperty("os.name");
			if (whichOS.contains("Windows")) 
				f = f.replace('/','\\');
		directory.setText(f);
	}
	
}
