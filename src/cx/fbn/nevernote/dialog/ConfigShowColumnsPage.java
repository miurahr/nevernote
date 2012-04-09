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

import com.trolltech.qt.gui.QComboBox;
import com.trolltech.qt.gui.QGroupBox;
import com.trolltech.qt.gui.QHBoxLayout;
import com.trolltech.qt.gui.QRadioButton;
import com.trolltech.qt.gui.QVBoxLayout;
import com.trolltech.qt.gui.QWidget;

public class ConfigShowColumnsPage extends QWidget {
	private final QRadioButton showDateCreated;
	private final QRadioButton hideDateCreated;
	private final QRadioButton showDateSubject;
	private final QRadioButton hideDateSubject;
	private final QRadioButton showDateChanged;
	private final QRadioButton hideDateChanged;
	private final QRadioButton showAuthor;
	private final QRadioButton hideAuthor;
	private final QRadioButton sourceUrlShow;
	private final QRadioButton sourceUrlHide;
	private final QRadioButton showTags;
	private final QRadioButton hideTags;
	private final QRadioButton showNotebook;
	private final QRadioButton hideNotebook;
	private final QRadioButton showSynchronized;
	private final QRadioButton hideSynchronized;

	
	QComboBox messageCombo;
	public ConfigShowColumnsPage(QWidget parent) {
		super(parent);

		// Date Created Column
		QGroupBox dateCreatedGroup =  new QGroupBox(tr("Date Created"));
		QHBoxLayout dateCreatedLayout = new QHBoxLayout();
		showDateCreated = new QRadioButton(tr("Show"));
		hideDateCreated = new QRadioButton(tr("Hide"));
		dateCreatedLayout.addWidget(showDateCreated);
		dateCreatedLayout.addWidget(hideDateCreated);
		dateCreatedLayout.setStretch(1, 100);
		dateCreatedGroup.setLayout(dateCreatedLayout);


		// Subject Date Column
		QGroupBox dateSubjectGroup =  new QGroupBox(tr("Subject Date"));
		QHBoxLayout dateSubjectLayout = new QHBoxLayout();
		showDateSubject = new QRadioButton(tr("Show"));
		hideDateSubject = new QRadioButton(tr("Hide"));
		dateSubjectLayout.addWidget(showDateSubject);
		dateSubjectLayout.addWidget(hideDateSubject);
		dateSubjectLayout.setStretch(1, 100);
		dateSubjectGroup.setLayout(dateSubjectLayout);


		// Title Column
		QGroupBox sourceUrlGroup =  new QGroupBox(tr("Source URL"));
		QHBoxLayout sourceUrlLayout = new QHBoxLayout();
		sourceUrlShow = new QRadioButton(tr("Show"));
		sourceUrlHide = new QRadioButton(tr("Hide"));
		sourceUrlLayout.addWidget(sourceUrlShow);
		sourceUrlLayout.addWidget(sourceUrlHide);
		sourceUrlLayout.setStretch(1, 100);
		sourceUrlGroup.setLayout(sourceUrlLayout);

		// Author Column
		QGroupBox authorGroup =  new QGroupBox(tr("Author"));
		QHBoxLayout authorLayout = new QHBoxLayout();
		showAuthor = new QRadioButton(tr("Show"));
		hideAuthor = new QRadioButton(tr("Hide"));
		authorLayout.addWidget(showAuthor);
		authorLayout.addWidget(hideAuthor);
		authorLayout.setStretch(1, 100);
		authorGroup.setLayout(authorLayout);

		
		// Date Changed Column
		QGroupBox dateChangedGroup =  new QGroupBox(tr("Date Changed"));
		QHBoxLayout dateChangedLayout = new QHBoxLayout();
		showDateChanged = new QRadioButton(tr("Show"));
		hideDateChanged = new QRadioButton(tr("Hide"));
		dateChangedLayout.addWidget(showDateChanged);
		dateChangedLayout.addWidget(hideDateChanged);
		dateChangedLayout.setStretch(1, 100);
		dateChangedGroup.setLayout(dateChangedLayout);

		// Notebook Column
		QGroupBox notebookGroup =  new QGroupBox(tr("Notebook"));
		QHBoxLayout notebookLayout = new QHBoxLayout();
		showNotebook = new QRadioButton(tr("Show"));
		hideNotebook = new QRadioButton(tr("Hide"));
		notebookLayout.addWidget(showNotebook);
		notebookLayout.addWidget(hideNotebook);
		notebookLayout.setStretch(1, 100);
		notebookGroup.setLayout(notebookLayout);

		// Tags Column
		QGroupBox tagsGroup =  new QGroupBox(tr("Tags"));
		QHBoxLayout tagsLayout = new QHBoxLayout();
		showTags = new QRadioButton(tr("Show"));
		hideTags = new QRadioButton(tr("Hide"));
		tagsLayout.addWidget(showTags);
		tagsLayout.addWidget(hideTags);
		tagsLayout.setStretch(1, 100);
		tagsGroup.setLayout(tagsLayout);

		// Synchronized Column
		QGroupBox synchronizedGroup =  new QGroupBox(tr("Synchronized Indicator"));
		QHBoxLayout syncLayout = new QHBoxLayout();
		showSynchronized = new QRadioButton(tr("Show"));
		hideSynchronized = new QRadioButton(tr("Hide"));
		syncLayout.addWidget(showSynchronized);
		syncLayout.addWidget(hideSynchronized);
		syncLayout.setStretch(1, 100);
		synchronizedGroup.setLayout(syncLayout);
		
		QVBoxLayout mainLayout = new QVBoxLayout();
		mainLayout.addWidget(dateCreatedGroup);
		mainLayout.addWidget(dateChangedGroup);
		mainLayout.addWidget(dateSubjectGroup);
		mainLayout.addWidget(notebookGroup);
		mainLayout.addWidget(tagsGroup);
		mainLayout.addWidget(sourceUrlGroup);
		mainLayout.addWidget(authorGroup);
		mainLayout.addWidget(synchronizedGroup);
		mainLayout.addStretch(1);
		setLayout(mainLayout);
	}
	
	public void setDateCreated(boolean value) {
		if (value)
			showDateCreated.click();
		else
			hideDateCreated.click();
	}
	
	public void setDateSubject(boolean value) {
		if (value)
			showDateSubject.click();
		else
			hideDateSubject.click();
	}

	public void setDateChanged(boolean value) {
		if (value)
			showDateChanged.click();
		else
			hideDateChanged.click();
	}

	public void setAuthor(boolean value) {
		if (value)
			showAuthor.click();
		else
			hideAuthor.click();
	}

	public void setSourceUrl(boolean value) {
		if (value)
			sourceUrlShow.click();
		else
			sourceUrlHide.click();
	}
	public void setNotebook(boolean value) {
		if (value)
			showNotebook.click();
		else
			hideNotebook.click();
	}
	public void setTags(boolean value) {
		if (value)
			showTags.click();
		else
			hideTags.click();
	}
	public void setSynchronized(boolean value) {
		if (value)
			showSynchronized.click();
		else
			hideSynchronized.click();
	}
	
	public boolean showDateCreated() {
		return showDateCreated.isChecked();
	}
	public boolean showDateChanged() {
		return showDateChanged.isChecked();
	}
	public boolean showDateSubject() {
		return showDateSubject.isChecked();
	}
	public boolean showAuthor() {
		return showAuthor.isChecked();
	}
	public boolean showSourceUrl() {
		return sourceUrlShow.isChecked();
	}
	public boolean showTags() {
		return showTags.isChecked();
	}
	public boolean showSynchronized() {
		return showSynchronized.isChecked();
	}
	public boolean showNotebook() {
		return showNotebook.isChecked();
	}






}