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
//* Publish (make public) a notebook.
//**********************************************
//**********************************************

import com.evernote.edam.type.NoteSortOrder;
import com.evernote.edam.type.Notebook;
import com.evernote.edam.type.Publishing;
import com.trolltech.qt.core.Qt;
import com.trolltech.qt.gui.QComboBox;
import com.trolltech.qt.gui.QDialog;
import com.trolltech.qt.gui.QHBoxLayout;
import com.trolltech.qt.gui.QIcon;
import com.trolltech.qt.gui.QLabel;
import com.trolltech.qt.gui.QLineEdit;
import com.trolltech.qt.gui.QPushButton;
import com.trolltech.qt.gui.QTextEdit;
import com.trolltech.qt.gui.QVBoxLayout;

public class PublishNotebook extends QDialog {
	private final QPushButton		okButton;
	private final QPushButton		cancelButton;
	private boolean					okClicked;
	private final QLabel			urlLabel;
	private final QLineEdit			url;		
	private final String iconPath = new String("classpath:cx/fbn/nevernote/icons/");
	private final QTextEdit			description;
	private final QComboBox			sortedBy;
	private final QComboBox			sortOrder;
	private boolean stopButtonPressed = false;
	private final QPushButton stopButton;
	
	public PublishNotebook(String userid, String url, Notebook n) {
		setWindowIcon(new QIcon(iconPath+"globe.png"));
		okClicked = false;

		
		okButton = new QPushButton();
		okButton.setText(tr("OK"));
		okButton.pressed.connect(this, "onClicked()");
		
		userid = "sysrabt";
		
		urlLabel = new QLabel("http://"+url +tr("/pub/") +userid + tr("/"));
		QHBoxLayout urlLayout = new QHBoxLayout();
		urlLayout.addWidget(urlLabel);
		this.url = new QLineEdit();
		this.url.textChanged.connect(this, "urlEdited()");
		urlLayout.addWidget(this.url);
		
		QVBoxLayout textEditLayout = new QVBoxLayout();
		textEditLayout.addWidget(new QLabel(tr("Notebook: ") +n.getName()));
		textEditLayout.addWidget(new QLabel(tr("Public URL")));
		textEditLayout.addLayout(urlLayout);
		textEditLayout.addWidget(new QLabel(tr("Description")));
		description = new QTextEdit();
		textEditLayout.addWidget(description);
		
		sortedBy = new QComboBox(this);
		sortOrder = new QComboBox(this);
		QHBoxLayout orderLayout = new QHBoxLayout();
		orderLayout.addWidget(new QLabel(tr("Sort By")));
		Qt.Alignment right = new Qt.Alignment();
		right.set(Qt.AlignmentFlag.AlignRight);
		orderLayout.setAlignment(right);
		orderLayout.addWidget(sortedBy);
		orderLayout.addSpacing(50);
		orderLayout.addWidget(new QLabel(tr("Sort Order")));
		orderLayout.addWidget(sortOrder);
		
		sortedBy.addItem(tr("Date Created"), NoteSortOrder.CREATED);
		sortedBy.addItem(tr("Date Updated"), NoteSortOrder.UPDATED);
		
		sortOrder.addItem(tr("Newest to oldest"), false);
		sortOrder.addItem(tr("Oldest to newest"), true);
		
		textEditLayout.addLayout(orderLayout);
		
		cancelButton = new QPushButton();
		cancelButton.setText(tr("Cancel"));
		cancelButton.pressed.connect(this, "onCancel()");
		
		stopButton = new QPushButton(tr("Stop Sharing"));
		stopButton.setVisible(false);
		stopButton.clicked.connect(this, "stopPublishing()");
		
		QHBoxLayout buttonLayout = new QHBoxLayout();
		buttonLayout.addStretch(1);
		buttonLayout.addWidget(stopButton);
		buttonLayout.addWidget(okButton);
		buttonLayout.addWidget(cancelButton);
		setWindowTitle(tr("Share A Notebook With The World"));	
		
		QVBoxLayout mainLayout = new QVBoxLayout();
		mainLayout.addLayout(textEditLayout);
		mainLayout.addSpacing(1);
		mainLayout.addLayout(buttonLayout);
		setLayout(mainLayout);

		if (n.isPublished()) {
			Publishing p = n.getPublishing();
			this.url.setText(p.getUri());
			description.setText(p.getPublicDescription());
			int position = sortOrder.findData(p.isAscending());
			sortOrder.setCurrentIndex(position);
			position = sortedBy.findData(p.getOrder());
			sortedBy.setCurrentIndex(position);
			okButton.setText(tr("Save Changes"));
			stopButton.setVisible(true);
		} else {
			okButton.setEnabled(false);
		}
		
		resize(500, 200);
		
	}
	
	@SuppressWarnings("unused")
	private void onClicked() {
		okClicked = true;
		stopButtonPressed = false;
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
	
	@SuppressWarnings("unused")
	private void urlEdited() {
		if (url.text().trim().equals(""))
			okButton.setEnabled(false);
		else
			okButton.setEnabled(true);
	}
	
	@SuppressWarnings("unused")
	private void stopPublishing() {
		stopButtonPressed = true;
		okClicked = true;
		close();
	}
	
	public boolean isStopPressed() {
		return stopButtonPressed;
	}
	
	public Publishing getPublishing() {
		Publishing p = new Publishing();
		p.setPublicDescription(description.toPlainText());
		int i = sortedBy.currentIndex();
		p.setOrder((NoteSortOrder) sortedBy.itemData(i));
		p.setAscending((Boolean)sortOrder.itemData(i));
		p.setUri(url.text());
		return p;
	}
}
