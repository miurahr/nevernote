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
//* This is the dialog used to tell the program
//* to not synchronize some notebooks.
//**********************************************
//**********************************************

import java.util.List;

import com.evernote.edam.type.LinkedNotebook;
import com.evernote.edam.type.Notebook;
import com.evernote.edam.type.Tag;
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

public class IgnoreSync extends QDialog {
	private final QListWidget 		syncBookList;
	private final QListWidget 		ignoreBookList;
	private final QListWidget 		syncTagList;
	private final QListWidget 		ignoreTagList;
	private final QListWidget		syncLinkedNotebookList;
	private final QListWidget		ignoreLinkedNotebookList;
	private final QPushButton		okButton;
	private final QPushButton		cancelButton;
	private boolean					okClicked;
	private final QPushButton		leftButton;
	private final QPushButton		rightButton;
	private final QPushButton		leftTagButton;
	private final QPushButton		rightTagButton;
	private final QPushButton		leftLinkedNotebookButton;
	private final QPushButton		rightLinkedNotebookButton;
	private final QLabel			linkedLabelLeft;
	private final QLabel			linkedLabelRight;
	
	private final String iconPath = new String("classpath:cx/fbn/nevernote/icons/");
	
	public IgnoreSync(List<Notebook> allBooks, List<Notebook> archive, List<Tag> allTags, List<Tag> ignoreTags, 
			List<LinkedNotebook> allLinkedNotebooks, List<LinkedNotebook> ignoreLinkedNotebooks) {
		setWindowIcon(new QIcon(iconPath+"synchronize.png"));
		okClicked = false;
		syncBookList = new QListWidget();
		syncBookList.setSortingEnabled(true);
		syncBookList.setSelectionMode(QAbstractItemView.SelectionMode.MultiSelection);
		
		syncTagList = new QListWidget();
		syncTagList.setSortingEnabled(true);
		syncTagList.setSelectionMode(QAbstractItemView.SelectionMode.MultiSelection);
		
		syncLinkedNotebookList = new QListWidget();
		syncLinkedNotebookList.setSortingEnabled(true);
		syncLinkedNotebookList.setSelectionMode(QAbstractItemView.SelectionMode.MultiSelection);
		
		okButton = new QPushButton();
		okButton.setText(tr("OK"));
		okButton.pressed.connect(this, "onClicked()");
		
		cancelButton = new QPushButton();
		cancelButton.setText(tr("Cancel"));
		cancelButton.pressed.connect(this, "onCancel()");
		
		QVBoxLayout openLayout = new QVBoxLayout();
		openLayout.addWidget(new QLabel(tr("Synchronized Notebooks")));
		openLayout.addWidget(syncBookList);
		
		QVBoxLayout openTagLayout = new QVBoxLayout();
		openTagLayout.addWidget(new QLabel(tr("Synchronized Tags")));
		openTagLayout.addWidget(syncTagList);
		
		QVBoxLayout openLinkedNotebookLayout = new QVBoxLayout();
		linkedLabelLeft = new QLabel(tr("Synchronized Linked Notebooks"));
		openLinkedNotebookLayout.addWidget(linkedLabelLeft);
		openLinkedNotebookLayout.addWidget(syncLinkedNotebookList);
		
		rightButton = new QPushButton(this);
		rightButton.setIcon(new QIcon(iconPath+"forward.png"));
		leftButton = new QPushButton(this);
		leftButton.setIcon(new QIcon(iconPath+"back.png"));
		leftButton.setEnabled(false);
		rightButton.setEnabled(false);
		
		rightTagButton = new QPushButton(this);
		rightTagButton.setIcon(new QIcon(iconPath+"forward.png"));
		leftTagButton = new QPushButton(this);
		leftTagButton.setIcon(new QIcon(iconPath+"back.png"));
		leftTagButton.setEnabled(false);
		rightTagButton.setEnabled(false);
		
		rightLinkedNotebookButton = new QPushButton(this);
		rightLinkedNotebookButton.setIcon(new QIcon(iconPath+"forward.png"));
		leftLinkedNotebookButton = new QPushButton(this);
		leftLinkedNotebookButton.setIcon(new QIcon(iconPath+"back.png"));
		leftLinkedNotebookButton.setEnabled(false);
		rightLinkedNotebookButton.setEnabled(false);
		
		QVBoxLayout middleLayout = new QVBoxLayout();
		middleLayout.addSpacerItem(new QSpacerItem(1,1));
		middleLayout.addWidget(rightButton);
		middleLayout.addWidget(leftButton);
		middleLayout.addSpacerItem(new QSpacerItem(1,1));
		
		QVBoxLayout middleTagLayout = new QVBoxLayout();
		middleTagLayout.addSpacerItem(new QSpacerItem(1,1));
		middleTagLayout.addWidget(rightTagButton);
		middleTagLayout.addWidget(leftTagButton);
		middleTagLayout.addSpacerItem(new QSpacerItem(1,1));
		
		QVBoxLayout middleLinkedNotebookLayout = new QVBoxLayout();
		middleLinkedNotebookLayout.addSpacerItem(new QSpacerItem(1,1));
		middleLinkedNotebookLayout.addWidget(rightLinkedNotebookButton);
		middleLinkedNotebookLayout.addWidget(leftLinkedNotebookButton);
		middleLinkedNotebookLayout.addSpacerItem(new QSpacerItem(1,1));

		QVBoxLayout closeLayout = new QVBoxLayout();
		closeLayout.addWidget(new QLabel(tr("Non-Synchronized Notebooks")));
		ignoreBookList = new QListWidget();
		ignoreBookList.setSortingEnabled(true);
		ignoreBookList.setSelectionMode(QAbstractItemView.SelectionMode.MultiSelection);
		closeLayout.addWidget(ignoreBookList);

		QVBoxLayout closeTagLayout = new QVBoxLayout();
		closeTagLayout.addWidget(new QLabel(tr("Non-Synchronized Tags")));
		ignoreTagList = new QListWidget();
		ignoreTagList.setSortingEnabled(true);
		ignoreTagList.setSelectionMode(QAbstractItemView.SelectionMode.MultiSelection);
		closeTagLayout.addWidget(ignoreTagList);
		
		QVBoxLayout closeLinkedNotebookLayout = new QVBoxLayout();
		linkedLabelRight = new QLabel(tr("Non-Synchronized Linked Notebooks"));
		closeLinkedNotebookLayout.addWidget(linkedLabelRight);
		ignoreLinkedNotebookList = new QListWidget();
		ignoreLinkedNotebookList.setSortingEnabled(true);
		ignoreLinkedNotebookList.setSelectionMode(QAbstractItemView.SelectionMode.MultiSelection);
		closeLinkedNotebookLayout.addWidget(ignoreLinkedNotebookList);
		
		syncBookList.itemSelectionChanged.connect(this, "syncBookSelected()");
		ignoreBookList.itemSelectionChanged.connect(this, "ignoreBookSelected()");
		leftButton.clicked.connect(this, "toOpenList()");
		rightButton.clicked.connect(this, "toClosedList()");
		
		syncTagList.itemSelectionChanged.connect(this, "syncTagSelected()");
		ignoreTagList.itemSelectionChanged.connect(this, "ignoreTagSelected()");
		leftTagButton.clicked.connect(this, "toOpenTagList()");
		rightTagButton.clicked.connect(this, "toClosedTagList()");
		
		syncLinkedNotebookList.itemSelectionChanged.connect(this, "syncLinkedNotebookSelected()");
		ignoreLinkedNotebookList.itemSelectionChanged.connect(this, "ignoreLinkedNotebookSelected()");
		leftLinkedNotebookButton.clicked.connect(this, "toOpenLinkedNotebookList()");
		rightLinkedNotebookButton.clicked.connect(this, "toClosedLinkedNotebookList()");
		
		QHBoxLayout buttonLayout = new QHBoxLayout();
		buttonLayout.addStretch(1);
		buttonLayout.addWidget(okButton);
		buttonLayout.addWidget(cancelButton);
		setWindowTitle(tr("Open/Close Notebooks"));
		
		QHBoxLayout upperLayout = new QHBoxLayout();
		upperLayout.addLayout(openLayout);
		upperLayout.addLayout(middleLayout);
		upperLayout.addLayout(closeLayout);
		
		QHBoxLayout tagLayout = new QHBoxLayout();
		tagLayout.addLayout(openTagLayout);
		tagLayout.addLayout(middleTagLayout);
		tagLayout.addLayout(closeTagLayout);
		
		QHBoxLayout linkedNotebookLayout = new QHBoxLayout();
		linkedNotebookLayout.addLayout(openLinkedNotebookLayout);
		linkedNotebookLayout.addLayout(middleLinkedNotebookLayout);
		linkedNotebookLayout.addLayout(closeLinkedNotebookLayout);
		
		QVBoxLayout mainLayout = new QVBoxLayout();
		mainLayout.addLayout(upperLayout);
		mainLayout.addLayout(tagLayout);
		mainLayout.addLayout(linkedNotebookLayout);
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
				syncBookList.addItem(item);
			}
		}
		
		setWindowTitle(tr("Open Notebooks"));
		for (int i=0; i<archive.size(); i++) {
			QListWidgetItem item = new QListWidgetItem(archive.get(i).getName());
			item.setSelected(false);
			ignoreBookList.addItem(item);
		}
		
		for (int i=0; i<allTags.size(); i++) {
			boolean found = false;
			for (int j=0; j<ignoreTags.size(); j++) {
				if (ignoreTags.get(j).getName().equalsIgnoreCase(allTags.get(i).getName())) {
					found = true;
					j=ignoreTags.size();
				}
			}
			if (!found) {
				QListWidgetItem item = new QListWidgetItem(allTags.get(i).getName());
				item.setSelected(false);
				syncTagList.addItem(item);
			}
		}
		for (int i=0; i<ignoreTags.size(); i++) {
			QListWidgetItem item = new QListWidgetItem(ignoreTags.get(i).getName());
			item.setSelected(false);
			ignoreTagList.addItem(item);
		}
		
		for (int i=0; i<allLinkedNotebooks.size(); i++) {
			boolean found = false;
			for (int j=0; j<ignoreLinkedNotebooks.size(); j++) {
				if (ignoreLinkedNotebooks.get(j).getShareName().equalsIgnoreCase(allLinkedNotebooks.get(i).getShareName())) {
					found = true;
					j=ignoreLinkedNotebooks.size();
				}
			}
			if (!found) {
				QListWidgetItem item = new QListWidgetItem(allLinkedNotebooks.get(i).getShareName());
				item.setSelected(false);
				syncLinkedNotebookList.addItem(item);
			}
		}
		for (int i=0; i<ignoreLinkedNotebooks.size(); i++) {
			QListWidgetItem item = new QListWidgetItem(ignoreLinkedNotebooks.get(i).getShareName());
			item.setSelected(false);
			ignoreLinkedNotebookList.addItem(item);
		}


		syncBookList.itemSelectionChanged.connect(this, "itemSelected()");
		setWindowTitle(tr("Ignore Synchronized Notes"));

		if (allLinkedNotebooks.size() == 0) {
			linkedLabelLeft.setVisible(false);
			linkedLabelRight.setVisible(false);
			rightLinkedNotebookButton.setVisible(false);
			leftLinkedNotebookButton.setVisible(false);
			ignoreLinkedNotebookList.setVisible(false);
			syncLinkedNotebookList.setVisible(false);
		}
	}
	
	@SuppressWarnings("unused")
	private void toClosedList() {
		List<QListWidgetItem> items = syncBookList.selectedItems();
		for (int i=items.size()-1; i>=0; i--) {
			int row = syncBookList.row(items.get(i));
			syncBookList.takeItem(row);
			ignoreBookList.addItem(items.get(i).text());
		}
		if (syncBookList.count() == 0)
			okButton.setEnabled(false);
		rightButton.setEnabled(false);
	}
	
	
	@SuppressWarnings("unused")
	private void toOpenList() {
		List<QListWidgetItem> items = ignoreBookList.selectedItems();
		for (int i=items.size()-1; i>=0; i--) {
			int row = ignoreBookList.row(items.get(i));
			ignoreBookList.takeItem(row);
			syncBookList.addItem(items.get(i).text());
		}
		okButton.setEnabled(true);
		leftButton.setEnabled(false);
	}
	
	
	@SuppressWarnings("unused")
	private void toClosedTagList() {
		List<QListWidgetItem> items = syncTagList.selectedItems();
		for (int i=items.size()-1; i>=0; i--) {
			int row = syncTagList.row(items.get(i));
			syncTagList.takeItem(row);
			ignoreTagList.addItem(items.get(i).text());
		}
		rightTagButton.setEnabled(false);
	}
	
	
	@SuppressWarnings("unused")
	private void toOpenTagList() {
		List<QListWidgetItem> items = ignoreTagList.selectedItems();
		for (int i=items.size()-1; i>=0; i--) {
			int row = ignoreTagList.row(items.get(i));
			ignoreTagList.takeItem(row);
			syncTagList.addItem(items.get(i).text());
		}
		leftTagButton.setEnabled(false);
	}
	
	@SuppressWarnings("unused")
	private void toClosedLinkedNotebookList() {
		List<QListWidgetItem> items = syncLinkedNotebookList.selectedItems();
		for (int i=items.size()-1; i>=0; i--) {
			int row = syncLinkedNotebookList.row(items.get(i));
			syncLinkedNotebookList.takeItem(row);
			ignoreLinkedNotebookList.addItem(items.get(i).text());
		}
		rightLinkedNotebookButton.setEnabled(false);
	}
	
	
	@SuppressWarnings("unused")
	private void toOpenLinkedNotebookList() {
		List<QListWidgetItem> items = ignoreLinkedNotebookList.selectedItems();
		for (int i=items.size()-1; i>=0; i--) {
			int row = ignoreLinkedNotebookList.row(items.get(i));
			ignoreLinkedNotebookList.takeItem(row);
			syncLinkedNotebookList.addItem(items.get(i).text());
		}
		leftLinkedNotebookButton.setEnabled(false);
	}
	
	
	@SuppressWarnings("unused")
	private void ignoreBookSelected() {
		if (ignoreBookList.selectedItems().size() > 0)
			leftButton.setEnabled(true);
		else
			leftButton.setEnabled(false);
	}
	
	@SuppressWarnings("unused")
	private void syncBookSelected() {
		if (syncBookList.selectedItems().size() > 0)
			rightButton.setEnabled(true);
		else
			rightButton.setEnabled(false);
	}
	
	@SuppressWarnings("unused")
	private void ignoreTagSelected() {
		if (ignoreTagList.selectedItems().size() > 0)
			leftTagButton.setEnabled(true);
		else
			leftTagButton.setEnabled(false);
	}
	
	@SuppressWarnings("unused")
	private void syncTagSelected() {
		if (syncTagList.selectedItems().size() > 0)
			rightTagButton.setEnabled(true);
		else
			rightTagButton.setEnabled(false);
	}
	
	@SuppressWarnings("unused")
	private void ignoreLinkedNotebookSelected() {
		if (ignoreLinkedNotebookList.selectedItems().size() > 0)
			leftLinkedNotebookButton.setEnabled(true);
		else
			leftLinkedNotebookButton.setEnabled(false);
	}
	
	@SuppressWarnings("unused")
	private void syncLinkedNotebookSelected() {
		if (syncLinkedNotebookList.selectedItems().size() > 0)
			rightLinkedNotebookButton.setEnabled(true);
		else
			rightLinkedNotebookButton.setEnabled(false);
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
	
	public QListWidget getSyncBookList() {
		return syncBookList;
	}
	
	public QListWidget getIgnoredBookList() {
		return ignoreBookList;
	}
	
	public QListWidget getSyncTagList() {
		return syncTagList;
	}
	
	public QListWidget getIgnoredTagList() {
		return ignoreTagList;
	}
	
	public QListWidget getSyncLinkedNotebookList() {
		return syncLinkedNotebookList;
	}
	
	public QListWidget getIgnoredLinkedNotebookList() {
		return ignoreLinkedNotebookList;
	}
	
	@SuppressWarnings("unused")
	private void itemSelected() {
		if (syncBookList.selectedItems().size() == syncBookList.count()) {
			okButton.setEnabled(false);
			rightButton.setEnabled(false);
			return;
		}
		rightButton.setEnabled(true);
		okButton.setEnabled(true);
	}
}
