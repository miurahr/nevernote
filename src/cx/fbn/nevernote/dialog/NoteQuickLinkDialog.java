/*
 * This file is part of NixNote 
 * Copyright 2011 Randy Baumgarte
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
//* This is the dialog that shows a user
//* a quick popup of a note based upon its title.
//* It is used in the Quick Link function.
//**********************************************
//**********************************************

import java.util.List;

import com.evernote.edam.type.Note;
import com.trolltech.qt.core.QByteArray;
import com.trolltech.qt.core.QTemporaryFile;
import com.trolltech.qt.core.Qt.ContextMenuPolicy;
import com.trolltech.qt.gui.QComboBox;
import com.trolltech.qt.gui.QDialog;
import com.trolltech.qt.gui.QHBoxLayout;
import com.trolltech.qt.gui.QIcon;
import com.trolltech.qt.gui.QLabel;
import com.trolltech.qt.gui.QPushButton;
import com.trolltech.qt.gui.QVBoxLayout;

import cx.fbn.nevernote.gui.BrowserWindow;
import cx.fbn.nevernote.sql.DatabaseConnection;
import cx.fbn.nevernote.utilities.ApplicationLogger;
import cx.fbn.nevernote.utilities.Pair;
import cx.fbn.nevernote.xml.NoteFormatter;

public class NoteQuickLinkDialog extends QDialog {
	public final QPushButton 	ok;
	public final QPushButton 	cancel;
	private final DatabaseConnection  conn;
	public final QComboBox		titleCombo;	 
	private final BrowserWindow	browser;
	private final ApplicationLogger logger;
	List<Pair<String,String>> results;
	public boolean okPressed;
	private List<QTemporaryFile> tempFiles;
	private final String iconPath = new String("classpath:cx/fbn/nevernote/icons/");
	
	// Constructor
	public NoteQuickLinkDialog(ApplicationLogger l, DatabaseConnection c, String text) {
		okPressed = false;
		setWindowTitle(tr("Quick Link Notes"));
		setWindowIcon(new QIcon(iconPath+"notebook-green.png"));
		QVBoxLayout main = new QVBoxLayout();
		setLayout(main);
		titleCombo = new QComboBox(this);
		
		QHBoxLayout comboLayout = new QHBoxLayout();
		comboLayout.addWidget(new QLabel(tr("Matching Notes:")));
		comboLayout.addWidget(titleCombo);
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
		ok = new QPushButton(tr("OK"));
		ok.clicked.connect(this, "okPressed()");
		
		cancel = new QPushButton(tr("Cancel"));
		cancel.clicked.connect(this, "cancelPressed()");
		
		buttonLayout.addWidget(ok);
		buttonLayout.addWidget(cancel);
		main.addLayout(buttonLayout);
		
		browser.getBrowser().setContextMenuPolicy(ContextMenuPolicy.NoContextMenu);
		logger = l;
		
		// Search for matching notes
		results = conn.getNoteTable().findNotesByTitle(text);
		
		// Add the results to the combo box
		for (int i=0; i<results.size(); i++) {
			titleCombo.addItem(results.get(i).getSecond(), results.get(i).getFirst());
		}
		titleCombo.activated.connect(this, "selectionChanged(String)");
		
		// Load the results into the combo box
		if (results.size() > 0)	{
			Note currentNote = conn.getNoteTable().getNote(results.get(0).getFirst(), true, true, false, true, true);
			setContent(currentNote);
		}
	}

	// Cancel button pressed
	@SuppressWarnings("unused")
	private void cancelPressed() {
		this.close();
	}
	
	// OK button pressed
	@SuppressWarnings("unused")
	private void okPressed() {
		okPressed = true;
		close();
	}

	// When the selection changes, we refresh the browser window with the new content
	@SuppressWarnings("unused")
	private void selectionChanged(String text) {
		int pos = titleCombo.currentIndex();
		String guid = results.get(pos).getFirst();
		Note note = conn.getNoteTable().getNote(guid, true, true, false, true, true);
		setContent(note);
	}
	
	// Return the note the user is currently viewing
	public String getSelectedNote() {
		int pos = titleCombo.currentIndex();
		return results.get(pos).getFirst();
	}
	
	
	// Load the content of the note into the viewing window.
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
		browser.setContent(new QByteArray(js.toString()));
	}
	
	// give the results from the DB search back to the caller.
	public List<Pair<String,String>> getResults() {
		return results;
	}
	
	
}
 


	
	
	
	

