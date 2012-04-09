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
//* Open or close a notebook to hide a notebook
//* from a user.
//**********************************************
//**********************************************

import java.util.List;

import com.evernote.edam.type.Notebook;
import com.trolltech.qt.gui.QAbstractItemView;
import com.trolltech.qt.gui.QDialog;
import com.trolltech.qt.gui.QHBoxLayout;
import com.trolltech.qt.gui.QIcon;
import com.trolltech.qt.gui.QLabel;
import com.trolltech.qt.gui.QListWidget;
import com.trolltech.qt.gui.QListWidgetItem;
import com.trolltech.qt.gui.QPushButton;
import com.trolltech.qt.gui.QSpacerItem;
import com.trolltech.qt.gui.QVBoxLayout;

public class NotebookArchive extends QDialog {
	private final QListWidget 		openBookList;
	private final QListWidget 		closedBookList;
	private final QPushButton		okButton;
	private final QPushButton		cancelButton;
	private boolean					okClicked;
	private final QPushButton		leftButton;
	private final QPushButton		rightButton;
	private final String iconPath = new String("classpath:cx/fbn/nevernote/icons/");
	
	public NotebookArchive(List<Notebook> allBooks, List<Notebook> archive) {
		setWindowIcon(new QIcon(iconPath+"notebook-green.png"));
		okClicked = false;
		openBookList = new QListWidget();
		openBookList.setSortingEnabled(true);
		openBookList.setSelectionMode(QAbstractItemView.SelectionMode.MultiSelection);
		
		okButton = new QPushButton();
		okButton.setText(tr("OK"));
		okButton.pressed.connect(this, "onClicked()");
		
		cancelButton = new QPushButton();
		cancelButton.setText(tr("Cancel"));
		cancelButton.pressed.connect(this, "onCancel()");
		
		QVBoxLayout openLayout = new QVBoxLayout();
		openLayout.addWidget(new QLabel(tr("Open Notebooks")));
		openLayout.addWidget(openBookList);
		
		rightButton = new QPushButton(this);
		rightButton.setIcon(new QIcon(iconPath+"forward.png"));
		leftButton = new QPushButton(this);
		leftButton.setIcon(new QIcon(iconPath+"back.png"));
		leftButton.setEnabled(false);
		rightButton.setEnabled(false);
		
		QVBoxLayout middleLayout = new QVBoxLayout();
		middleLayout.addSpacerItem(new QSpacerItem(1,1));
		middleLayout.addWidget(rightButton);
		middleLayout.addWidget(leftButton);
		middleLayout.addSpacerItem(new QSpacerItem(1,1));

		QVBoxLayout closeLayout = new QVBoxLayout();
		closeLayout.addWidget(new QLabel(tr("Closed Notebooks")));
		closedBookList = new QListWidget();
		closedBookList.setSortingEnabled(true);
		closedBookList.setSelectionMode(QAbstractItemView.SelectionMode.MultiSelection);
		closeLayout.addWidget(closedBookList);

		openBookList.itemSelectionChanged.connect(this, "openBookSelected()");
		closedBookList.itemSelectionChanged.connect(this, "closedBookSelected()");
		leftButton.clicked.connect(this, "toOpenList()");
		rightButton.clicked.connect(this, "toClosedList()");
		
		QHBoxLayout buttonLayout = new QHBoxLayout();
		buttonLayout.addStretch(1);
		buttonLayout.addWidget(okButton);
		buttonLayout.addWidget(cancelButton);
		setWindowTitle(tr("Open/Close Notebooks"));
		
		QHBoxLayout upperLayout = new QHBoxLayout();
		upperLayout.addLayout(openLayout);
		upperLayout.addLayout(middleLayout);
		upperLayout.addLayout(closeLayout);
		
		QVBoxLayout mainLayout = new QVBoxLayout();
		mainLayout.addLayout(upperLayout);
		//mainLayout.addStretch(1);
		mainLayout.addSpacing(1);
		mainLayout.addLayout(buttonLayout);
		setLayout(mainLayout);

		for (int i=0; i<allBooks.size(); i++) {
			boolean found = false;
			for (int j=0; j<archive.size(); j++) {
				if (archive.get(j).getName().equalsIgnoreCase(allBooks.get(i).getName())) {
					found = true;
					j=archive.size();
				}
			}
			if (!found) {
				QListWidgetItem item = new QListWidgetItem(allBooks.get(i).getName());
				item.setSelected(false);
				openBookList.addItem(item);
			}
		}
		
		setWindowTitle(tr("Open Notebooks"));
		for (int i=0; i<archive.size(); i++) {
			QListWidgetItem item = new QListWidgetItem(archive.get(i).getName());
			item.setSelected(false);
			closedBookList.addItem(item);
		}
		openBookList.itemSelectionChanged.connect(this, "itemSelected()");
	}
	
	@SuppressWarnings("unused")
	private void toClosedList() {
		List<QListWidgetItem> items = openBookList.selectedItems();
		for (int i=items.size()-1; i>=0; i--) {
			int row = openBookList.row(items.get(i));
			openBookList.takeItem(row);
			closedBookList.addItem(items.get(i).text());
		}
		if (openBookList.count() == 0)
			okButton.setEnabled(false);
		rightButton.setEnabled(false);
	}
	
	
	@SuppressWarnings("unused")
	private void toOpenList() {
		List<QListWidgetItem> items = closedBookList.selectedItems();
		for (int i=items.size()-1; i>=0; i--) {
			int row = closedBookList.row(items.get(i));
			closedBookList.takeItem(row);
			openBookList.addItem(items.get(i).text());
		}
		okButton.setEnabled(true);
		leftButton.setEnabled(false);
	}
	
	@SuppressWarnings("unused")
	private void closedBookSelected() {
		if (closedBookList.selectedItems().size() > 0)
			leftButton.setEnabled(true);
		else
			leftButton.setEnabled(false);
	}
	
	@SuppressWarnings("unused")
	private void openBookSelected() {
		if (openBookList.selectedItems().size() > 0)
			rightButton.setEnabled(true);
		else
			rightButton.setEnabled(false);
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
	
	public QListWidget getOpenBookList() {
		return openBookList;
	}
	
	public QListWidget getClosedBookList() {
		return closedBookList;
	}
	
	@SuppressWarnings("unused")
	private void itemSelected() {
		if (openBookList.selectedItems().size() == openBookList.count()) {
			okButton.setEnabled(false);
			rightButton.setEnabled(false);
			return;
		}
		rightButton.setEnabled(true);
		okButton.setEnabled(true);
	}
}
