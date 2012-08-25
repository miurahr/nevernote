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


import java.text.SimpleDateFormat;

import com.trolltech.qt.gui.QDialog;
import com.trolltech.qt.gui.QGridLayout;
import com.trolltech.qt.gui.QGroupBox;
import com.trolltech.qt.gui.QHBoxLayout;
import com.trolltech.qt.gui.QIcon;
import com.trolltech.qt.gui.QLabel;
import com.trolltech.qt.gui.QPushButton;

import cx.fbn.nevernote.Global;

public class AccountDialog extends QDialog {
    String iconPath = new String("classpath:cx/fbn/nevernote/icons/");
	private final QPushButton ok;
	
	// Constructor
	public AccountDialog() {
		
		// Setup window layout, title, & icon
		setWindowTitle(tr("Account Information"));
		setWindowIcon(new QIcon(new QIcon(iconPath+"account.png")));
		QGridLayout grid = new QGridLayout();
		setLayout(grid);
		QLabel premium;
		if (Global.isPremium())
			premium = new QLabel(tr("Premium"));
		else
			premium = new QLabel(tr("Free"));
		
		String userName = Global.username;
		
		// calculate the upload amount
		Long uploadAmt = Global.getUploadAmount();
		Long uploadLimit = Global.getUploadLimit();
		Long uploadLimitEnd = Global.getUploadLimitEnd();
		Long pct;
		if (uploadLimit > 0)
			pct = uploadAmt*100 / uploadLimit;
		else
			pct = new Long(0);
		String unit = tr(" Bytes");
		
		if (uploadAmt > 0) {
			uploadAmt = uploadAmt/1024;
			unit = tr(" KB");
		}
		if (uploadAmt >= 1024) {
			uploadAmt = uploadAmt / 1024;
			unit = tr(" MB");
		}
		if (uploadLimit > 0)
			uploadLimit = uploadLimit/1024/1024;
	
		//showing only date need
		String fmt = Global.getDateFormat() /* + " " + Global.getTimeFormat()*/;
		String dateTimeFormat = new String(fmt);
		SimpleDateFormat simple = new SimpleDateFormat(dateTimeFormat);
		StringBuilder endDate = new StringBuilder(simple.format(uploadLimitEnd));
		
		// Show limits
		QGroupBox limitGroup = new QGroupBox(tr("Account:"));

		QGridLayout textGrid = new QGridLayout();
		textGrid.addWidget(new QLabel(tr("User Name:")),1,1);
		textGrid.addWidget(new QLabel(userName), 1,2);
		textGrid.addWidget(new QLabel(tr("Account Type:")), 2,1);
		textGrid.addWidget(premium, 2, 2);
		textGrid.addWidget(new QLabel(tr("Limit:")), 3,1);
		textGrid.addWidget(new QLabel(uploadLimit.toString() +" MB"),3,2);
		textGrid.addWidget(new QLabel(tr("Uploaded In This Period:")), 4,1);
		if (uploadAmt > 0)
			textGrid.addWidget(new QLabel(uploadAmt.toString()+unit +" ("+pct+"%)"),4,2);
		else
			textGrid.addWidget(new QLabel(tr("Less than 1MB")),4,2);
		textGrid.addWidget(new QLabel(tr("Current Cycle Ends:")), 5,1);
		textGrid.addWidget(new QLabel(endDate.toString()),5,2);
		limitGroup.setLayout(textGrid);

		grid.addWidget(limitGroup, 1, 1);

		//UserAttributes attrib = Global.getUserAttributes();
		//QGridLayout attribGrid = new QGridLayout();
		//QGroupBox attribGroup = new QGroupBox(tr("User Attributes"));
		//attribGrid.addWidget(new QLabel(tr("Incoming Email:")), 1,1);
		//String server = Global.getServer();
		//if (server.startsWith("www."))
			//server = server.substring(4);
		
		//usually evernote mail is user@m.evernote.com
		//server = "m."+server;
		
		//attribGrid.addWidget(createIncomingEmailField(attrib.getIncomingEmailAddress()+"@"+server), 1,2);
		//attribGroup.setLayout(attribGrid);
		//grid.addWidget(attribGroup, 2,1);

		
		QHBoxLayout buttonLayout = new QHBoxLayout();
		ok = new QPushButton("OK");
		ok.clicked.connect(this, "okPushed()");
		buttonLayout.addStretch();
		buttonLayout.addWidget(ok);
		buttonLayout.addStretch();
		grid.addLayout(buttonLayout,3,1);
	}
	
	// build a field used for the incomming email
	/*
	private QWidget createIncomingEmailField(String email){
		QTextEdit emailTextEdit = new QTextEdit();
		
		String emailLinkFormat="<a href=\"mailto:%1$s\">%1$s</a>";
		String emailHtml = String.format(emailLinkFormat, email);
		
		emailTextEdit.setLineWrapMode(LineWrapMode.NoWrap);

		QTextDocument doc = new QTextDocument();
		doc.setHtml(emailHtml);
		doc.setMaximumBlockCount(1);
		
		emailTextEdit.setDocument(doc);
		emailTextEdit.setReadOnly(true);

		//set background color as for disabled control 
		QPalette palette = new QPalette();
		QColor backgroundColor = QApplication.palette().color(ColorRole.Window);
		palette.setColor(ColorRole.Base, backgroundColor);
		
		emailTextEdit.setAutoFillBackground(true);
		emailTextEdit.setPalette(palette);
		
		//remove frame borders
		emailTextEdit.setFrameShape(QFrame.Shape.NoFrame);
		
		//set height of emailTextEdit actually single line
		QFontMetrics fontMetrics = emailTextEdit.fontMetrics();
		//we also add some pixels to avoid showing scrollbars
		int height = fontMetrics.height() + emailTextEdit.frameWidth()*2 + 8;
		emailTextEdit.setFixedHeight(height);
		
		return emailTextEdit;
	}
	*/
	
	// OK button pushed, close the window
	@SuppressWarnings("unused")
	private void okPushed() {
		this.close();
	}
	
	// return the actual OK button
	public QPushButton getOkButton() {
		return ok;
	}
}
