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
//* Create or edit a tag
//**********************************************
//**********************************************

import java.util.List;

import com.evernote.edam.type.Tag;
import com.trolltech.qt.gui.QCheckBox;
import com.trolltech.qt.gui.QDialog;
import com.trolltech.qt.gui.QGridLayout;
import com.trolltech.qt.gui.QIcon;
import com.trolltech.qt.gui.QLabel;
import com.trolltech.qt.gui.QLineEdit;
import com.trolltech.qt.gui.QPushButton;

public class TagEdit extends QDialog {
	private boolean 	okPressed;
	private final QLineEdit	tag;
	QPushButton ok;
	private final QCheckBox useParentTag;
	List<Tag>		currentTags;
	private final String iconPath = new String("classpath:cx/fbn/nevernote/icons/");
	
	// Constructor
	public TagEdit() {
		okPressed = false;
		setWindowTitle(tr("Add Tag"));
		QGridLayout grid = new QGridLayout();
		setWindowIcon(new QIcon(iconPath+"tag.png"));
		setLayout(grid);
		
		QGridLayout textGrid = new QGridLayout();
		tag = new QLineEdit();
		textGrid.addWidget(new QLabel(tr("Tag Name")), 1,1);
		textGrid.addWidget(tag, 1, 2);
		
		textGrid.setContentsMargins(10, 10,-10, -10);
		grid.addLayout(textGrid,1,1);

		useParentTag = new QCheckBox();
		useParentTag.setVisible(false);
		useParentTag.setChecked(false);
		grid.addWidget(useParentTag,2,1);
		
		QGridLayout buttonGrid = new QGridLayout();
		ok = new QPushButton(tr("OK"));
		ok.clicked.connect(this, "okButtonPressed()");
		ok.setEnabled(false);
		QPushButton cancel = new QPushButton(tr("Cancel"));
		cancel.clicked.connect(this, "cancelButtonPressed()");
		tag.textChanged.connect(this, "textChanged()");
		buttonGrid.addWidget(ok, 3, 1);
		buttonGrid.addWidget(cancel, 3,2);
		grid.addLayout(buttonGrid,3,1);
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
	public String getTag() {
		return tag.text();
	}
	
	// Set the tag name
	public void setTag(String name) {
		tag.setText(name);
	}

	// Set the tag name
	public void setParentTag(String name) {
		useParentTag.setText(tr("Create as child of \"") +name +"\"");
		useParentTag.setVisible(true);
		useParentTag.setChecked(true);
	}
	public QCheckBox getParentTag() {
		return useParentTag;
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
	public void setTagList(List<Tag> t) {
		currentTags = t;
	}
	// Watch what text is being entered
	@SuppressWarnings("unused")
	private void textChanged() {
		if (tag.text().equals("")) {
			ok.setEnabled(false);
			return;
		}
		if (currentTags == null) {
			ok.setEnabled(false);
			return;
		}
		for (int i=0; i<currentTags.size(); i++) {
			String s = currentTags.get(i).getName();
			if (s.equalsIgnoreCase(tag.text())) {
				ok.setEnabled(false);
				return;
			}
		}
		ok.setEnabled(true);
	}
}
