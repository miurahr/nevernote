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
//* Show the current database status
//**********************************************
//**********************************************

import java.text.NumberFormat;

import com.trolltech.qt.gui.QDialog;
import com.trolltech.qt.gui.QGridLayout;
import com.trolltech.qt.gui.QIcon;
import com.trolltech.qt.gui.QLabel;
import com.trolltech.qt.gui.QPushButton;

public class DatabaseStatus extends QDialog {
	QLabel indexNeeded;
	QLabel syncNeeded;
	QLabel noteCount;
	QLabel notebookCount;
	QLabel tagCount;
	QLabel savedSearchCount;
	QLabel resourceCount;
	QLabel indexCount;
	QLabel resourceIndexNeeded;
	private final QPushButton ok;
    private final String iconPath = new String("classpath:cx/fbn/nevernote/icons/");
	
	// Constructor
	public DatabaseStatus() {
		setWindowTitle(tr("Current Database Status"));
		setWindowIcon(new QIcon(iconPath+"database.png"));
		QGridLayout grid = new QGridLayout();
		setLayout(grid);
		noteCount = new QLabel();
		indexNeeded = new QLabel();
		syncNeeded = new QLabel();
		notebookCount = new QLabel();
		tagCount = new QLabel();
		savedSearchCount = new QLabel();		
		resourceCount = new QLabel();
		resourceIndexNeeded = new QLabel();
		indexCount = new QLabel();
		
		grid.addWidget(new QLabel(tr("Notebooks:")), 0,0);
		grid.addWidget(notebookCount, 0,1);
		
		grid.addWidget(new QLabel(tr("Tags:")), 1,0);
		grid.addWidget(tagCount, 1,1);
		
		grid.addWidget(new QLabel(tr("Total Notes:")), 2,0);
		grid.addWidget(noteCount, 2,1);
		
		grid.addWidget(new QLabel(tr("Unsynchronized Notes:")), 3,0);
		grid.addWidget(syncNeeded, 3, 1);
		
		grid.addWidget(new QLabel(tr("Unindexed Notes:")), 4,0);
		grid.addWidget(indexNeeded, 4, 1);
				
		grid.addWidget(new QLabel(tr("Attachments/Images:")), 5,0);
		grid.addWidget(resourceCount, 5,1);

		grid.addWidget(new QLabel(tr("Unindexed Attachments/Images:")), 6,0);
		grid.addWidget(resourceIndexNeeded, 6, 1);

		grid.addWidget(new QLabel(tr("Saved Searches:")),7,0);
		grid.addWidget(savedSearchCount, 7,1);
		
		grid.addWidget(new QLabel(tr("Words In Index")), 8,0);
		grid.addWidget(indexCount, 8,1);
			
		QGridLayout buttonLayout = new QGridLayout();
		ok = new QPushButton(tr("OK"));
		ok.clicked.connect(this, "okPushed()");
		buttonLayout.addWidget(ok, 1, 1);
		grid.addLayout(buttonLayout,9,1);
	}
	
	@SuppressWarnings("unused")
	private void okPushed() {
		this.close();
	}
	public void setUnindexed(int d) {
		indexNeeded.setText(NumberFormat.getInstance().format(d));
	}
	public void setUnsynchronized(int d) {
		syncNeeded.setText(NumberFormat.getInstance().format(d));
	}
	public void setNoteCount(int d) {
		noteCount.setText(NumberFormat.getInstance().format(d));
	}
	public void setNotebookCount(int d) {
		notebookCount.setText(NumberFormat.getInstance().format(d));
	}
	public void setTagCount(int d) {
		tagCount.setText(NumberFormat.getInstance().format(d));
	}
	public void setSavedSearchCount(int d) {
		savedSearchCount.setText(NumberFormat.getInstance().format(d));
	}
	public void setResourceCount(int d) {
		resourceCount.setText(NumberFormat.getInstance().format(d));
	}
	public void setUnindexedResourceCount(int r)  {
		resourceIndexNeeded.setText(NumberFormat.getInstance().format(r));
	}
	public void setWordCount(int d) {
		indexCount.setText(NumberFormat.getInstance().format(d));
	}
 	public QPushButton getOkButton() {
		return ok;
	}
}
