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
//* Combine two tags
//**********************************************
//**********************************************

import java.util.List;

import com.evernote.edam.type.Tag;
import com.trolltech.qt.gui.QComboBox;
import com.trolltech.qt.gui.QDialog;
import com.trolltech.qt.gui.QHBoxLayout;
import com.trolltech.qt.gui.QIcon;
import com.trolltech.qt.gui.QLabel;
import com.trolltech.qt.gui.QPushButton;
import com.trolltech.qt.gui.QSpacerItem;
import com.trolltech.qt.gui.QVBoxLayout;

public class TagMerge extends QDialog {
	private final QComboBox 		newTag;
	private final QPushButton		okButton;
	private final QPushButton		cancelButton;
	private boolean					okClicked;
	private final String iconPath = new String("classpath:cx/fbn/nevernote/icons/");
	
	public TagMerge(List<Tag> tags) {
		setWindowIcon(new QIcon(iconPath+"tag.png"));
		okClicked = false;
		
		okButton = new QPushButton();
		okButton.setText(tr("OK"));
		okButton.pressed.connect(this, "onClicked()");
		
		cancelButton = new QPushButton();
		cancelButton.setText(tr("Cancel"));
		cancelButton.pressed.connect(this, "onCancel()");
	
		
		QVBoxLayout middleLayout = new QVBoxLayout();
		middleLayout.addSpacerItem(new QSpacerItem(1,1));
		middleLayout.addSpacerItem(new QSpacerItem(1,1));

		QVBoxLayout closeLayout = new QVBoxLayout();
		closeLayout.addWidget(new QLabel(tr("Merge Into")));
		newTag = new QComboBox();
		closeLayout.addWidget(newTag);
		
		QHBoxLayout buttonLayout = new QHBoxLayout();
		buttonLayout.addStretch(1);
		buttonLayout.addWidget(okButton);
		buttonLayout.addWidget(cancelButton);
		setWindowTitle(tr("Open/Close Notebooks"));
		
		QHBoxLayout upperLayout = new QHBoxLayout();
		upperLayout.addLayout(middleLayout);
		upperLayout.addLayout(closeLayout);
		
		QVBoxLayout mainLayout = new QVBoxLayout();
		mainLayout.addLayout(upperLayout);
		//mainLayout.addStretch(1);
		mainLayout.addSpacing(1);
		mainLayout.addLayout(buttonLayout);
		setLayout(mainLayout);
		
		for (int i=0; i<tags.size(); i++) {
			newTag.addItem(tags.get(i).getName(), tags.get(i).getGuid());
		}
		setWindowTitle(tr("Merge Tags"));
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
	
	public String getNewTagGuid() {
		int position = newTag.currentIndex();
		return newTag.itemData(position).toString();
	}
}
