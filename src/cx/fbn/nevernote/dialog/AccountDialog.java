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

import com.evernote.edam.type.UserAttributes;
import com.trolltech.qt.gui.QDialog;
import com.trolltech.qt.gui.QGridLayout;
import com.trolltech.qt.gui.QGroupBox;
import com.trolltech.qt.gui.QHBoxLayout;
import com.trolltech.qt.gui.QLabel;
import com.trolltech.qt.gui.QPushButton;

import cx.fbn.nevernote.Global;

public class AccountDialog extends QDialog {

	private final QPushButton ok;
	
	// Constructor
	public AccountDialog() {
		setWindowTitle("Account Information");
		QGridLayout grid = new QGridLayout();
		setLayout(grid);
		QLabel premium;
		if (Global.isPremium())
			premium = new QLabel("Premium");
		else
			premium = new QLabel("Free");
		
		Long uploadAmt = Global.getUploadAmount();
		Long uploadLimit = Global.getUploadLimit();
		Long uploadLimitEnd = Global.getUploadLimitEnd();
		Long pct;
		if (uploadLimit > 0)
			pct = uploadAmt*100 / uploadLimit;
		else
			pct = new Long(0);
		String unit = " Bytes";
		
		if (uploadAmt > 0) {
			uploadAmt = uploadAmt/1024;
			unit = " KB";
		}
		if (uploadAmt >= 1024) {
			uploadAmt = uploadAmt / 1024;
			unit = " MB";
		}
		if (uploadLimit > 0)
			uploadLimit = uploadLimit/1024/1024;
	
		
		String fmt = Global.getDateFormat() + " " + Global.getTimeFormat();
		String dateTimeFormat = new String(fmt);
		SimpleDateFormat simple = new SimpleDateFormat(dateTimeFormat);
		StringBuilder endDate = new StringBuilder(simple.format(uploadLimitEnd));
		
		QGridLayout textGrid = new QGridLayout();
		QGroupBox limitGroup = new QGroupBox(tr("Account:"));
		textGrid.addWidget(new QLabel("Account Type:"), 1,1);
		textGrid.addWidget(premium, 1, 2);
		textGrid.addWidget(new QLabel("Limit:"), 2,1);
		textGrid.addWidget(new QLabel(uploadLimit.toString() +" MB"),2,2);
		textGrid.addWidget(new QLabel("Uploaded In This Period:"), 3,1);
		textGrid.addWidget(new QLabel(uploadAmt.toString()+unit +" ("+pct+"%)"),3,2);
		textGrid.addWidget(new QLabel("Current Cycle Ends:"), 4,1);
		textGrid.addWidget(new QLabel(endDate.toString()),4,2);
		limitGroup.setLayout(textGrid);

		grid.addWidget(limitGroup, 1, 1);

		UserAttributes attrib = Global.getUserAttributes();
		QGridLayout attribGrid = new QGridLayout();
		QGroupBox attribGroup = new QGroupBox(tr("User Attributes"));
		attribGrid.addWidget(new QLabel(tr("Incoming Email:")), 1,1);
		String server = Global.getServer();
		if (server.startsWith("www."))
			server = server.substring(4);
		attribGrid.addWidget(new QLabel(attrib.getIncomingEmailAddress()+"@"+Global.getServer()), 1,2);
		attribGroup.setLayout(attribGrid);
		grid.addWidget(attribGroup, 2,1);

		
		QHBoxLayout buttonLayout = new QHBoxLayout();
		ok = new QPushButton("OK");
		ok.clicked.connect(this, "okPushed()");
		buttonLayout.addStretch();
		buttonLayout.addWidget(ok);
		buttonLayout.addStretch();
		grid.addLayout(buttonLayout,3,1);
	}
	
	@SuppressWarnings("unused")
	private void okPushed() {
		this.close();
	}
	

	public QPushButton getOkButton() {
		return ok;
	}
}
