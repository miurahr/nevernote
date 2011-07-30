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
//* Dialog box used to show watched folders
//**********************************************
//**********************************************

import java.util.ArrayList;
import java.util.List;

import com.evernote.edam.type.Notebook;
import com.trolltech.qt.core.QModelIndex;
import com.trolltech.qt.core.Qt.ItemDataRole;
import com.trolltech.qt.gui.QAbstractItemView.SelectionBehavior;
import com.trolltech.qt.gui.QAbstractItemView.SelectionMode;
import com.trolltech.qt.gui.QApplication;
import com.trolltech.qt.gui.QDialog;
import com.trolltech.qt.gui.QFontMetrics;
import com.trolltech.qt.gui.QHBoxLayout;
import com.trolltech.qt.gui.QIcon;
import com.trolltech.qt.gui.QPushButton;
import com.trolltech.qt.gui.QTableWidget;
import com.trolltech.qt.gui.QTableWidgetItem;
import com.trolltech.qt.gui.QVBoxLayout;

import cx.fbn.nevernote.sql.WatchFolderRecord;

public class WatchFolder extends QDialog {
	private final QPushButton		okButton;
	private final QPushButton		cancelButton;
	private final QPushButton		addButton;
	private final QPushButton		editButton;
	private final QPushButton		deleteButton;
	private boolean					okClicked;
	public final QTableWidget		table;
	private final List<Notebook>	notebooks;
	private final List<WatchFolderRecord> records;
	private final String iconPath = new String("classpath:cx/fbn/nevernote/icons/");
	
	public WatchFolder(List<WatchFolderRecord> w, List<Notebook> n) {
		setWindowIcon(new QIcon(iconPath+"folder.png"));
		okClicked = false;
		notebooks = n;
		records = w;
		
		okButton = new QPushButton();
		okButton.setText(tr("OK"));
		okButton.pressed.connect(this, "onClicked()");
		
		cancelButton = new QPushButton();
		cancelButton.setText(tr("Cancel"));
		cancelButton.pressed.connect(this, "onCancel()");
		
		QHBoxLayout horizontalLayout = new QHBoxLayout();
		QHBoxLayout buttonLayout = new QHBoxLayout();
		buttonLayout.addStretch(1);
		buttonLayout.addWidget(okButton);
		buttonLayout.addWidget(cancelButton);
		setWindowTitle(tr("Auto Import Folders"));	
		
		table = new QTableWidget(records.size(),3);
		List<String> headers = new ArrayList<String>();
		headers.add(tr("Directory"));
		headers.add(tr("Target Notebook"));
		headers.add(tr("Keep"));
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
		
		editButton = new QPushButton();
		editButton.setText(tr("Edit"));
		editButton.setEnabled(false);
		editButton.clicked.connect(this, "editPressed()");
		
		deleteButton = new QPushButton();
		deleteButton.setText(tr("Delete"));
		deleteButton.setEnabled(false);
		deleteButton.clicked.connect(this, "deletePressed()");
		
		QVBoxLayout editLayout = new QVBoxLayout();
		editLayout.addWidget(addButton);
		editLayout.addWidget(editButton);
		editLayout.addWidget(deleteButton);
		
		QHBoxLayout listLayout = new QHBoxLayout();
		listLayout.addLayout(horizontalLayout);
		listLayout.addLayout(editLayout);
		
		QVBoxLayout mainLayout = new QVBoxLayout();
		mainLayout.addLayout(listLayout);
		mainLayout.addSpacing(1);
		mainLayout.addLayout(buttonLayout);
		setLayout(mainLayout);

//		QTableWidgetItem dir = new QTableWidgetItem();
//		QTableWidgetItem book = new QTableWidgetItem();
		
		table.setColumnWidth(0, 160);
		resize(500, 200);
		load();
		
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
	
	private void load() {
		for (int i=0; i<records.size(); i++) {
			addRow(i, records.get(i).folder, records.get(i).notebook, records.get(i).keep);
		}
	}

	private void addRow(int row, String folder, String notebook, boolean keepAfter) {
		QFontMetrics f = QApplication.fontMetrics();
		int fontHeight = f.height();

		QTableWidgetItem dir = new QTableWidgetItem();
		dir.setText(folder);
		table.setItem(row, 0, dir);
		table.setRowHeight(row, fontHeight);
		dir.setToolTip(folder);
	
		QTableWidgetItem book = new QTableWidgetItem();
		book.setText(notebook);
		table.setItem(row, 1, book);
		
		QTableWidgetItem keep = new QTableWidgetItem();
		if (keepAfter) {
			keep.setText(tr("Keep"));
			keep.setData(ItemDataRole.UserRole, "Keep");
		} else {
			keep.setText(tr("Delete"));
			keep.setData(ItemDataRole.UserRole, "Delete");
		}
		table.setItem(row, 2, keep);

	}
	
	@SuppressWarnings("unused")
	private void tableSelection() {
		editButton.setEnabled(true);
		deleteButton.setEnabled(true);
	}
	
	
	@SuppressWarnings("unused")
	private void addPressed() {
		WatchFolderAdd dialog = new WatchFolderAdd(null, notebooks);
		dialog.exec();
		if (dialog.okClicked()) {
			String dir = dialog.directory.text();
			String notebook = dialog.books.currentText();
			
			boolean keep;
			int index = dialog.keep.currentIndex();
			String value  = (String) dialog.keep.itemData(index);
			if (value.equalsIgnoreCase("keep"))
				keep = true;
			else
				keep = false;
			table.insertRow(table.rowCount());
			addRow(table.rowCount()-1, dir, notebook, keep);		
		}
		
	}

	
	@SuppressWarnings("unused")
	private void editPressed() {
		WatchFolderRecord record = new WatchFolderRecord();
		QModelIndex index = table.currentIndex();
		int row = index.row();
		QTableWidgetItem item = table.item(row, 0);
		record.folder = item.text();
		item = table.item(row, 1);
		record.notebook = item.text();
		item = table.item(row,2);
		if (item.data(ItemDataRole.UserRole).toString().equalsIgnoreCase("keep"))
			record.keep = true;
		else
			record.keep = false;
		
		WatchFolderAdd dialog = new WatchFolderAdd(record, notebooks);
		dialog.exec();
		if (dialog.okClicked()) {
			String dir = dialog.directory.text();
			String notebook = dialog.books.currentText();
			
			boolean keep;
			int idx = dialog.keep.currentIndex();
			if (dialog.keep.itemData(idx, ItemDataRole.UserRole).toString().equalsIgnoreCase("keep"))
				keep = true;
			else
				keep = false;
			table.removeRow(row);
			table.insertRow(table.rowCount());
			addRow(table.rowCount()-1, dir, notebook, keep);
			WatchFolderRecord newRecord = new WatchFolderRecord();
			newRecord.folder = dir;
			newRecord.notebook = notebook;
			newRecord.keep = keep;
			records.add(newRecord);
		}
		table.setCurrentIndex(index);
		
	}
	
	
	@SuppressWarnings("unused")
	private void deletePressed() {
		QModelIndex index = table.currentIndex();
		int row = index.row();
		
		QTableWidgetItem dirWidget = table.item(row, 0);
		String value = dirWidget.text();
		table.removeRow(row);
		
		for (int i=0; i<records.size(); i++) {
			if (value.equals(records.get(i).folder)) {
				records.remove(i);
				i=records.size();
			}
		}
		
		if (table.rowCount() == 0) {
			editButton.setEnabled(false);
			deleteButton.setEnabled(false);
		}		
	}
}
