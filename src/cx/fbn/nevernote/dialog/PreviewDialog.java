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
//* Show a full screen preview of the note.  This
//* isn't really used much any more.
//**********************************************
//**********************************************



import com.trolltech.qt.gui.QDialog;
import com.trolltech.qt.gui.QHBoxLayout;
import com.trolltech.qt.gui.QImage;
import com.trolltech.qt.gui.QPixmap;
import com.trolltech.qt.gui.QPushButton;
import com.trolltech.qt.gui.QScrollArea;
import com.trolltech.qt.gui.QVBoxLayout;
import com.trolltech.qt.gui.QWidget;

import cx.fbn.nevernote.Global;
public class PreviewDialog extends QDialog {

	
	public PreviewDialog(QWidget parent) {
		
		QWidget masterLabel = new QWidget();
		
		QVBoxLayout subLayout = new QVBoxLayout();
		setMouseTracking(true);
		
		PreviewImage imageLabel = new PreviewImage(masterLabel);
		imageLabel.ID = "Number 1";
        QImage image = new QImage(Global.getFileManager().getResDirPath("thumbnail.png"));
        imageLabel.setPixmap(QPixmap.fromImage(image).scaled(400, 400));
        subLayout.addWidget(imageLabel);

		PreviewImage imageLabel2 = new PreviewImage(masterLabel);
		imageLabel2.ID = "Number 2";
		QImage image2 = new QImage(Global.getFileManager().getResDirPath("thumbnail.png"));
        imageLabel2.setPixmap(QPixmap.fromImage(image2).scaled(400,400));
        subLayout.addWidget(imageLabel2);
        masterLabel.setLayout(subLayout);

        QScrollArea scrollArea = new QScrollArea();
        scrollArea.setWidget(masterLabel);
		
		QPushButton okButton = new QPushButton(tr("OK"));
		okButton.clicked.connect(this, "okPushed()");
		;
		
		QVBoxLayout verticalLayout = new QVBoxLayout();
		verticalLayout.addWidget(scrollArea);
		
		QHBoxLayout buttonLayout = new QHBoxLayout();
		buttonLayout.addStretch(1);
		buttonLayout.addWidget(okButton);
		setWindowTitle(tr("Note Preview"));	
		
		QVBoxLayout mainLayout = new QVBoxLayout();
		mainLayout.addLayout(verticalLayout);
		mainLayout.addSpacing(1);
		mainLayout.addLayout(buttonLayout);
		setLayout(mainLayout);

	}
	public void okPushed() {

		close();
	}
		
}
