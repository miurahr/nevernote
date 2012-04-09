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
//* Used to create a table in a note
//**********************************************
//**********************************************


import com.trolltech.qt.gui.QComboBox;
import com.trolltech.qt.gui.QDialog;
import com.trolltech.qt.gui.QGridLayout;
import com.trolltech.qt.gui.QIcon;
import com.trolltech.qt.gui.QIntValidator;
import com.trolltech.qt.gui.QLabel;
import com.trolltech.qt.gui.QLineEdit;
import com.trolltech.qt.gui.QPushButton;
import com.trolltech.qt.gui.QSpinBox;

public class TableDialog extends QDialog {

	private boolean 			okPressed;
	private final QSpinBox		rows;
	private final QSpinBox 		cols;
	private final QLineEdit 	width;
	private final QPushButton 	ok;
	private final QLabel 		error;
	private final QIntValidator	widthValidator;
	private final QComboBox		 unit;
	private final String iconPath = new String("classpath:cx/fbn/nevernote/icons/");
	
	// Constructor
	public TableDialog() {
		okPressed = false;
		setWindowTitle(tr("Insert Table"));
		setWindowIcon(new QIcon(iconPath+"table.png"));
		QGridLayout grid = new QGridLayout();
		QGridLayout input = new QGridLayout();
		QGridLayout msgGrid = new QGridLayout();
		QGridLayout button = new QGridLayout();
		setLayout(grid);
		
		unit = new QComboBox(this);
		unit.addItem(tr("Percent"),new Boolean(true));
		unit.addItem(tr("Pixels"),new Boolean(false));
		
		
		width = new QLineEdit("80");
		widthValidator = new QIntValidator(0,100, this);
		width.setValidator(widthValidator);
		width.textChanged.connect(this, "validateWidth()");
		rows = new QSpinBox();
		cols = new QSpinBox();
		rows.setMaximum(30);
		rows.setMinimum(1);
		cols.setMaximum(30);
		cols.setMinimum(1);
		
		unit.activated.connect(this, "unitChanged()");
		
		input.addWidget(new QLabel(tr("Rows")), 1,1);
		input.addWidget(rows, 1, 2);
		input.addWidget(new QLabel(tr("Columns")), 2,1);
		input.addWidget(cols, 2, 2);
		input.addWidget(new QLabel(tr("Width")), 3,1);
		input.addWidget(width, 3, 2);
		input.addWidget(new QLabel(tr("Unit")),4,1);
		input.addWidget(unit,4,2);
 		input.setContentsMargins(10, 10,  -10, -10);
		grid.addLayout(input, 1,1);
		
		error = new QLabel();
		msgGrid.addWidget(error, 1, 1);
		grid.addLayout(msgGrid, 2, 1);
		
		ok = new QPushButton(tr("OK"));
		ok.clicked.connect(this, "okButtonPressed()");
		
		QPushButton cancel = new QPushButton(tr("Cancel"));
		cancel.clicked.connect(this, "cancelButtonPressed()");
		button.addWidget(ok, 1, 1);
		button.addWidget(cancel, 1,2);
		grid.addLayout(button, 3, 1);
		
//		width.textChanged.connect(this, "validateInput()");
		
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
	// Check if the OK button was pressed
	public boolean okPressed() {
		return okPressed;
	}
	// Check if proper input was input
	@SuppressWarnings("unused")
	private void validateInput() {
		ok.setEnabled(false);
		
		ok.setEnabled(true);
	}
	
	@SuppressWarnings("unused")
	private void validateWidth() {
		if (width.text().trim().length() == 0) {
			ok.setEnabled(false);
		} else
			ok.setEnabled(true);
	}
	
	@SuppressWarnings("unused")
	private void unitChanged() {
		int i = unit.currentIndex();
		if ((Boolean)unit.itemData(i)) { // if 'percent'
			Integer w = new Integer(width.text());
			if (w > 100)
				width.setText("80");
			widthValidator.setTop(100);
		} else {
			widthValidator.setTop(32767);
		}
	}
	
	public int getRows() {
		return new Integer(rows.text());
	}
	public int getCols() {
		return new Integer(cols.text());
	}
	public int getWidth() {
		return new Integer(width.text());
	}
	public boolean isPercent() {
		int i = unit.currentIndex();
		return ((Boolean)unit.itemData(i)).booleanValue();
	}

}
