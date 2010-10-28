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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import com.evernote.edam.notestore.NoteVersionId;
import com.evernote.edam.type.Note;
import com.trolltech.qt.core.QTemporaryFile;
import com.trolltech.qt.core.Qt.ContextMenuPolicy;
import com.trolltech.qt.gui.QComboBox;
import com.trolltech.qt.gui.QDialog;
import com.trolltech.qt.gui.QHBoxLayout;
import com.trolltech.qt.gui.QIcon;
import com.trolltech.qt.gui.QLabel;
import com.trolltech.qt.gui.QPushButton;
import com.trolltech.qt.gui.QVBoxLayout;

import cx.fbn.nevernote.Global;
import cx.fbn.nevernote.gui.BrowserWindow;
import cx.fbn.nevernote.sql.DatabaseConnection;
import cx.fbn.nevernote.utilities.ApplicationLogger;
import cx.fbn.nevernote.xml.NoteFormatter;

public class OnlineNoteHistory extends QDialog {
	public final QPushButton 	restoreAsNew;
	public final QPushButton 	restore;
	private final DatabaseConnection  conn;
	public final QComboBox		historyCombo;	 
	private final BrowserWindow	browser;
	private final ApplicationLogger logger;
	List<QTemporaryFile>	tempFiles;
	private final String iconPath = new String("classpath:cx/fbn/nevernote/icons/");
	
	// Constructor
	public OnlineNoteHistory(ApplicationLogger l, DatabaseConnection c) {
		setWindowTitle(tr("Online Note History"));
		setWindowIcon(new QIcon(iconPath+"notebook-green.png"));
		QVBoxLayout main = new QVBoxLayout();
		setLayout(main);
		historyCombo = new QComboBox(this);
		
		QHBoxLayout comboLayout = new QHBoxLayout();
		comboLayout.addWidget(new QLabel(tr("History Date:")));
		comboLayout.addWidget(historyCombo);
		comboLayout.addStretch(100);
		
		main.addLayout(comboLayout);
				
		conn = c;
		browser = new BrowserWindow(conn);
		main.addWidget(browser);
		browser.titleLabel.setVisible(false);
		browser.notebookBox.setVisible(false);
		browser.hideButtons();
		browser.tagEdit.setVisible(false);
		browser.tagLabel.setVisible(false);
		
		QHBoxLayout buttonLayout = new QHBoxLayout();
		buttonLayout.addStretch(100);
		restore = new QPushButton(tr("Restore Note"));
		restore.clicked.connect(this, "restorePushed()");
		
		restoreAsNew = new QPushButton(tr("Restore As New Note"));
		restoreAsNew.clicked.connect(this, "restoreAsNewPushed()");
		QPushButton cancel = new QPushButton(tr("Cancel"));
		cancel.clicked.connect(this, "cancelPressed()");
		
		buttonLayout.addWidget(restore);
		buttonLayout.addWidget(restoreAsNew);
		buttonLayout.addWidget(cancel);
		main.addLayout(buttonLayout);
		
		browser.getBrowser().setContextMenuPolicy(ContextMenuPolicy.NoContextMenu);
		tempFiles = new ArrayList<QTemporaryFile>();
		logger = l;
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
	
	public void setCurrent(boolean isDirty) {
		if (isDirty) 
			historyCombo.addItem(new String(tr("Current (Non Synchronized)")));
		else
			historyCombo.addItem(new String(tr("Current (Synchronized)")));
				
	}
	
	public void load(List<NoteVersionId> versions) {
		String fmt = Global.getDateFormat() + " " + Global.getTimeFormat();
		String dateTimeFormat = new String(fmt);
		SimpleDateFormat simple = new SimpleDateFormat(dateTimeFormat);
		
		for (int i=0; i<versions.size(); i++) {
			StringBuilder versionDate = new StringBuilder(simple.format(versions.get(i).getSaved()));
			historyCombo.addItem(versionDate.toString());
		}
	}
	
	public void setContent(Note currentNote) {
		
		
		NoteFormatter formatter = new NoteFormatter(logger, conn, tempFiles);
		formatter.setNote(currentNote, false);
		formatter.setHighlight(null);
		formatter.setNoteHistory(true);
		
		StringBuffer js = new StringBuffer();
		
		// We need to prepend the note with <HEAD></HEAD> or encoded characters are ugly 
		js.append("<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">");	
		js.append("<style type=\"text/css\">en-crypt-temp { border-style:solid; border-color:blue; padding:1mm 1mm 1mm 1mm; }</style>");
		js.append("</head>");
		js.append(formatter.rebuildNoteHTML());
		js.append("</HTML>");
		
		browser.setNote(currentNote);
		browser.getBrowser().page().mainFrame().setHtml(js.toString());
	}
}
 


	
	
	
	

