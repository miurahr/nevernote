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
//* This dialog is called when the Help/Log
//* menu option is clicked.
//**********************************************
//**********************************************


import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import com.trolltech.qt.gui.QComboBox;
import com.trolltech.qt.gui.QDialog;
import com.trolltech.qt.gui.QHBoxLayout;
import com.trolltech.qt.gui.QIcon;
import com.trolltech.qt.gui.QLabel;
import com.trolltech.qt.gui.QTextEdit;
import com.trolltech.qt.gui.QVBoxLayout;

import cx.fbn.nevernote.Global;

public class LogFileDialog extends QDialog {
	public final QComboBox		fileCombo;	 
	public final QTextEdit		textField;
	private final List<String>		logs;
	private final String iconPath = new String("classpath:cx/fbn/nevernote/icons/");
	
	// Constructor
	public LogFileDialog(List<String> l) {
		setWindowTitle(tr("Application Logs"));
		logs = l;
		setWindowIcon(new QIcon(iconPath+"notebook-green.png"));
		QVBoxLayout main = new QVBoxLayout();
		setLayout(main);
		fileCombo = new QComboBox(this);
		
		QHBoxLayout comboLayout = new QHBoxLayout();
		comboLayout.addWidget(new QLabel(tr("Log File:")));
		comboLayout.addWidget(fileCombo);
		comboLayout.addStretch(100);
		fileCombo.currentIndexChanged.connect(this, "indexChanged(Integer)");
		
		main.addLayout(comboLayout);
				
		textField = new QTextEdit(this);
		main.addWidget(textField);
		
		fileCombo.addItem(tr("Message Log"), "MessageLog");
		fileCombo.addItem(tr("Application Log"), "NeverNoteLog");
		fileCombo.addItem(tr("Synchronization Log"), "SyncLog");
		fileCombo.addItem(tr("Save Log"), "SaveLog");
		fileCombo.addItem(tr("Tag Counter Log"), "TagCounterLog");
		fileCombo.addItem(tr("Notebook Counter Log"), "NotebookCounterLog");
		fileCombo.addItem(tr("Trash Counter Log"), "TrashCounterLog");
		fileCombo.addItem(tr("Note Browser Log"), "NoteBrowserLog");
		fileCombo.addItem(tr("Export Log"), "ExportLog");
		fileCombo.addItem(tr("Import Log"), "ImportLog");
//		fileCombo.addItem(tr("Global Log"), "GlobalLog");
		fileCombo.addItem(tr("Index Log"), "IndexLog");
		fileCombo.addItem(tr("Database Connection Log"), "DatabaseLog");
		fileCombo.addItem(tr("Thumbnail Generator Log"), "ThumbnailLog");
		fileCombo.addItem(tr("NixNote Database SQL Trace File"), "NeverNoteDBLog");
		fileCombo.addItem(tr("Index Database SQL Trace File"), "IndexDBLog");
		fileCombo.addItem(tr("Resource Database SQL Trace File"), "ResourceDBLog");
		
		
	}
	
	@SuppressWarnings("unused")
	private void restoreAsNewPushed() {
		this.close();
	}
	@SuppressWarnings("unused")
	private void restorePushed() {
		this.close();
	}
	@SuppressWarnings("unused")
	private void cancelPressed() {
		this.close();
	}
	
	@SuppressWarnings("unused")
	private void indexChanged(Integer index) {
		String value = (String) fileCombo.itemData(index);

		if (value.equals("MessageLog")) {
			textField.clear();
			for (int i=0; i<logs.size(); i++) {
				textField.append(logs.get(i));
			}
			return;
		}		
		if (value.equals("NeverNoteLog")) {
			loadFile("nevernote.log");
			return;
		}
		if (value.equals("SyncLog")) {
			loadFile("syncRunner.log");
			return;
		}
		if (value.equals("SaveLog")) {
			loadFile("saveRunner.log");
			return;
		}
		if (value.equals("SaveLog")) {
			loadFile("saveRunner.log");
			return;
		}
		if (value.equals("TagCounterLog")) {
			loadFile("tag_counter.log");
			return;
		}
		if (value.equals("NotebookCounterLog")) {
			loadFile("notebook_counter.log");
			return;
		}
		if (value.equals("TrashCounterLog")) {
			loadFile("trash_counter.log");
			return;
		}
		if (value.equals("NoteBrowserLog")) {
			loadFile("browser.log");
			return;
		}
		if (value.equals("ExportLog")) {
			loadFile("export.log");
			return;
		}
		if (value.equals("ImportLog")) {
			loadFile("import.log");
			return;
		}
		if (value.equals("GlobalLog")) {
			loadFile("global.log");
			return;
		}
		if (value.equals("IndexLog")) {
			loadFile("indexRunner.log");
			return;
		}
		if (value.equals("DatabaseLog")) {
			loadFile("nevernote-database.log");
			return;
		}
		if (value.equals("ThumbnailLog")) {
			loadFile("thumbnailRunner.log");
			return;
		}
		if (value.equals("NeverNoteDBLog")) {
			loadTraceFile("NeverNote.trace.db");
			return;
		}	
		if (value.equals("IndexDBLog")) {
			loadTraceFile("Index.trace.db");
			return;
		}	
		if (value.equals("ResourceDBLog")) {
			loadTraceFile("Resources.trace.db");
			return;
		}	
	}
	
	private void loadFile(String file) {
		textField.clear();
		File f = Global.getFileManager().getLogsDirFile(file);
		try {
			BufferedReader in = new BufferedReader(new FileReader(f.getAbsolutePath()));
			String data;
			while ((data=in.readLine()) != null) {
				textField.append(data);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
		
	private void loadTraceFile(String file) {
		textField.clear();
		File f = Global.getFileManager().getDbDirFile(file);
		try {
			BufferedReader in = new BufferedReader(new FileReader(f.getAbsolutePath()));
			String data;
			while ((data=in.readLine()) != null) {
				textField.append(data);
			}
		} catch (FileNotFoundException e) {
			return;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
 


	
	
	
	

