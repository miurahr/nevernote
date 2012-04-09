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
//* Edit or create a saved search
//**********************************************
//**********************************************

import java.util.List;

import com.evernote.edam.type.SavedSearch;
import com.trolltech.qt.gui.QDialog;
import com.trolltech.qt.gui.QGridLayout;
import com.trolltech.qt.gui.QIcon;
import com.trolltech.qt.gui.QLabel;
import com.trolltech.qt.gui.QLineEdit;
import com.trolltech.qt.gui.QPushButton;

public class SavedSearchEdit extends QDialog {
	private boolean 		okPressed;
	private final QLineEdit	searchName;
	private final QLineEdit	query;
	QPushButton 			ok;
	List<SavedSearch>		currentSearches;
	private final String iconPath = new String("classpath:cx/fbn/nevernote/icons/");
	
	// Constructor
	public SavedSearchEdit() {
		okPressed = false;
		setWindowTitle(tr("Add a search"));
		setWindowIcon(new QIcon(iconPath+"search.png"));
		QGridLayout grid = new QGridLayout();
		setLayout(grid);
		
		QGridLayout textLayout = new QGridLayout();
		searchName = new QLineEdit();
		textLayout.addWidget(new QLabel(tr("Name")), 1,1);
		textLayout.addWidget(searchName, 1, 2);
		query = new QLineEdit();
		textLayout.addWidget(new QLabel(tr("String")), 2,1);
		textLayout.addWidget(query, 2, 2);
		textLayout.setContentsMargins(10, 10,-10, -10);
		grid.addLayout(textLayout, 1, 1);
		
		QGridLayout buttonLayout = new QGridLayout();
		ok = new QPushButton(tr("OK"));
		ok.clicked.connect(this, "okButtonPressed()");
		ok.setEnabled(false);
		QPushButton cancel = new QPushButton(tr("Cancel"));
		cancel.clicked.connect(this, "cancelButtonPressed()");
		searchName.textChanged.connect(this, "textChanged()");
		query.textChanged.connect(this, "textChanged()");
		buttonLayout.addWidget(ok, 1, 1);
		buttonLayout.addWidget(cancel, 1,2);
		grid.addLayout(buttonLayout,2,1);
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
	
	// Get the name from the field
	public String getName() {
		return searchName.text().trim();
	}
	
	// Set the tag name
	public void setName(String name) {
		searchName.setText(name);
	}
	// get the query
	public String getQuery() {
		return query.text().trim();
	}
	// Set the query
	public void setQuery(String q) {
		query.setText(q);
	}
	// Check if the OK button was pressed
	public boolean okPressed() {
		return okPressed;
	}
	
	// Set the window title
	public void setTitle(String s) {
		setWindowTitle(s);
	}
	// List of existing tags
	public void setSearchList(List<SavedSearch> t) {
		currentSearches = t;
	}
	// Watch what text is being entered
	@SuppressWarnings("unused")
	private void textChanged() {
		if (searchName.text().trim().equals("")) {
			ok.setEnabled(false);
			return;
		}
		if (currentSearches == null) {
			ok.setEnabled(false);
			return;
		}
		if (query.text().trim().equals("")) {
			ok.setEnabled(false);
			return;
		}
/*		for (int i=0; i<currentSearches.size(); i++) {
			String s = currentSearches.get(i).getName();
			if (s.equalsIgnoreCase(searchName.text())) {
				ok.setEnabled(false);
				return;
			}
		} */
		ok.setEnabled(true);
	}
	// Watch what text is being entered
}
