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
//* Share a notebook with another user.
//**********************************************
//**********************************************

import java.util.ArrayList;
import java.util.List;

import org.apache.thrift.TException;

import com.evernote.edam.error.EDAMNotFoundException;
import com.evernote.edam.error.EDAMSystemException;
import com.evernote.edam.error.EDAMUserException;
import com.evernote.edam.type.Notebook;
import com.evernote.edam.type.SharedNotebook;
import com.trolltech.qt.core.QModelIndex;
import com.trolltech.qt.core.Qt.ItemFlag;
import com.trolltech.qt.core.Qt.ItemFlags;
import com.trolltech.qt.gui.QAbstractItemView.SelectionBehavior;
import com.trolltech.qt.gui.QAbstractItemView.SelectionMode;
import com.trolltech.qt.gui.QApplication;
import com.trolltech.qt.gui.QDialog;
import com.trolltech.qt.gui.QFontMetrics;
import com.trolltech.qt.gui.QHBoxLayout;
import com.trolltech.qt.gui.QIcon;
import com.trolltech.qt.gui.QLabel;
import com.trolltech.qt.gui.QPushButton;
import com.trolltech.qt.gui.QTableWidget;
import com.trolltech.qt.gui.QTableWidgetItem;
import com.trolltech.qt.gui.QVBoxLayout;

import cx.fbn.nevernote.sql.DatabaseConnection;
import cx.fbn.nevernote.threads.SyncRunner;

public class ShareNotebook extends QDialog {
	private final QPushButton				okButton;
	private final QPushButton				addButton;
	private final QPushButton				deleteButton;
	private boolean							okClicked;
	public final QTableWidget				table;
	private final List<SharedNotebook>		notebooks;
	private final DatabaseConnection		conn;
	private final Notebook					notebook;
	private final SyncRunner				syncRunner;
	private final String iconPath = new String("classpath:cx/fbn/nevernote/icons/");
	
	public ShareNotebook(String guid, DatabaseConnection c, Notebook n,
						SyncRunner s) {
		setWindowIcon(new QIcon(iconPath+"globe.png"));
		okClicked = false;
		conn = c;
		syncRunner = s;;
		notebook = n;
		
		notebooks = conn.getSharedNotebookTable().getForNotebook(n.getGuid());
		okButton = new QPushButton();
		okButton.setText(tr("OK"));
		okButton.pressed.connect(this, "onClicked()");
		
		QHBoxLayout horizontalLayout = new QHBoxLayout();
		QHBoxLayout buttonLayout = new QHBoxLayout();
		buttonLayout.addStretch(1);
		buttonLayout.addWidget(okButton);
		setWindowTitle(tr("Share Notebook \"") +notebook.getName() + tr("\" With Others"));	
		
		table = new QTableWidget(notebooks.size(),3);
		List<String> headers = new ArrayList<String>();
		headers.add(tr("Email"));
		headers.add(tr("Access"));
		headers.add(tr("Login Required"));
		table.setHorizontalHeaderLabels(headers);
		table.verticalHeader().setVisible(false);
		table.setAlternatingRowColors(true);
		table.setSelectionBehavior(SelectionBehavior.SelectRows);
		table.setSelectionMode(SelectionMode.SingleSelection);
		table.itemSelectionChanged.connect(this, "tableSelection()");
		horizontalLayout.addWidget(table);
		
		
		addButton = new QPushButton();
		addButton.setText(tr("Add"));
		addButton.clicked.connect(this, "addPressed()");
		
		deleteButton = new QPushButton();
		deleteButton.setText(tr("Delete"));
		deleteButton.setEnabled(false);
		deleteButton.clicked.connect(this, "deletePressed()");
		
		QVBoxLayout editLayout = new QVBoxLayout();
		editLayout.addWidget(addButton);
		editLayout.addWidget(deleteButton);
		
		QHBoxLayout listLayout = new QHBoxLayout();
		listLayout.addLayout(horizontalLayout);
		listLayout.addLayout(editLayout);
		
		QVBoxLayout mainLayout = new QVBoxLayout();
		if (syncRunner.authToken == null) {
			QLabel msg = new QLabel(tr("You must be connected to make changes."));
			mainLayout.addWidget(msg);
		}
		mainLayout.addLayout(listLayout);
		mainLayout.addSpacing(1);
		mainLayout.addLayout(buttonLayout);
		setLayout(mainLayout);
		
		table.setColumnWidth(0, 160);
		resize(500, 200);
		load();
		
		if (syncRunner.authToken == null) {
			addButton.setEnabled(false);
			deleteButton.setEnabled(false);
		}
		
	}
	
	@SuppressWarnings("unused")
	private void onClicked() {
		okClicked = true;
		close();
	}
	

	public boolean okClicked() {
		return okClicked;
	}
	
	@SuppressWarnings("unused")
	private void itemSelected() {
		okButton.setEnabled(true);
	}
	
	private void load() {
		for (int i=0; i<notebooks.size(); i++) {
			addRow(i, notebooks.get(i).getEmail(), 
					 notebooks.get(i).isNotebookModifiable(),
					 notebooks.get(i).isRequireLogin());
		}
	}

	private void addRow(int row, String email, boolean mod, boolean login) {
		QFontMetrics f = QApplication.fontMetrics();
		int fontHeight = f.height();

		QTableWidgetItem emailWidget = new QTableWidgetItem();
		emailWidget.setText(email);
		table.setItem(row, 0, emailWidget);
		table.setRowHeight(row, fontHeight);
		emailWidget.setToolTip(email);
		ItemFlags flags = emailWidget.flags();
		flags.clear(ItemFlag.ItemIsEditable);
		emailWidget.setFlags(flags);

		QTableWidgetItem accessWidget = new QTableWidgetItem();
		if (mod)
			accessWidget.setText(tr("Modify"));
		else
			accessWidget.setText(tr("Read Only"));
		table.setItem(row, 1,accessWidget);
		accessWidget.setFlags(flags);

		QTableWidgetItem loginWidget = new QTableWidgetItem();
		if (login)
			loginWidget.setText(tr("True"));
		else
			loginWidget.setText(tr("False"));
		table.setItem(row, 2, loginWidget);
		loginWidget.setFlags(flags);
		

	}
	
	@SuppressWarnings("unused")
	private void tableSelection() {
		if (syncRunner.authToken != null)
			deleteButton.setEnabled(true);
	}
	
	
	@SuppressWarnings("unused")
	private void addPressed() {

		
	}
	
	
	@SuppressWarnings("unused")
	private void deletePressed() {
		QModelIndex index = table.currentIndex();
		int row = index.row();
		
		QTableWidgetItem dirWidget = table.item(row, 0);
		String value = dirWidget.text();
		List<SharedNotebook> notebooks = conn.getSharedNotebookTable().getForNotebook(notebook.getGuid());
		
		List<Long> ids = new ArrayList<Long>();
		for (int i=0; i<notebooks.size(); i++) {
			if (notebooks.get(i).getEmail().equalsIgnoreCase(value)) {
				ids.add(notebooks.get(i).getId());
			}
		}
		
		if (ids.size() > 0) {
			try {
				syncRunner.localNoteStore.expungeSharedNotebooks(syncRunner.authToken, ids);
			} catch (EDAMUserException e) {
				e.printStackTrace();
			} catch (EDAMNotFoundException e) {
				e.printStackTrace();
			} catch (EDAMSystemException e) {
				e.printStackTrace();
			} catch (TException e) {
				e.printStackTrace();
			}
			for (int i=0; i<ids.size(); i++)
				conn.getSharedNotebookTable().expungeNotebook(ids.get(i), false);
		}
		
		table.clear();
		load();
		if (table.rowCount() == 0) {
			deleteButton.setEnabled(false);
		}		
	}
}
