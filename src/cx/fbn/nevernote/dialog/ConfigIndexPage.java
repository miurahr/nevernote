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

import com.trolltech.qt.gui.QGroupBox;
import com.trolltech.qt.gui.QHBoxLayout;
import com.trolltech.qt.gui.QLabel;
import com.trolltech.qt.gui.QLineEdit;
import com.trolltech.qt.gui.QSpinBox;
import com.trolltech.qt.gui.QVBoxLayout;
import com.trolltech.qt.gui.QWidget;

import cx.fbn.nevernote.Global;

public class ConfigIndexPage extends QWidget {

	private final QSpinBox  indexThreadSpinner;
	private final QSpinBox lengthSpinner;
	private final QSpinBox weightSpinner;
	private final QSpinBox sleepSpinner;
	private final QLineEdit regexEdit;
	
	public ConfigIndexPage(QWidget parent) {
//		super(parent);
		
		indexThreadSpinner = new QSpinBox(this);
		indexThreadSpinner.setMaximum(5);
		indexThreadSpinner.setMinimum(1);
			
		// Index threads layout
		QLabel threadLabel = new QLabel(tr("Maximum Threads"));
		QHBoxLayout threadsLayout = new QHBoxLayout();
		threadsLayout.addWidget(threadLabel);
		threadsLayout.addWidget(indexThreadSpinner);
		QGroupBox threadsGroup = new QGroupBox(tr("Indexing Threads (Requires Restart)"));
		threadsGroup.setLayout(threadsLayout);
		
		threadsGroup.setVisible(false);
		
		
		// Minimum word length
		QGroupBox wordLengthGroup = new QGroupBox(tr("Word Length"));
		QLabel wordLengthLabel = new QLabel(tr("Minimum Word Length"));
		lengthSpinner = new QSpinBox();
		lengthSpinner.setRange(1,10);
		lengthSpinner.setSingleStep(1);
		lengthSpinner.setValue(Global.minimumWordCount);
		
		QHBoxLayout wordLengthLayout = new QHBoxLayout();
		wordLengthLayout.addWidget(wordLengthLabel);
		wordLengthLayout.addWidget(lengthSpinner);
		wordLengthGroup.setLayout(wordLengthLayout);
		

		// Minimum word length
		QGroupBox weightGroup = new QGroupBox(tr("Recognition"));
		QLabel weightLabel = new QLabel(tr("Minimum Recognition Weight"));
		weightSpinner = new QSpinBox();
		weightSpinner.setRange(1,100);
		weightSpinner.setSingleStep(1);
		weightSpinner.setValue(Global.getRecognitionWeight());

		QHBoxLayout weightLayout = new QHBoxLayout();
		weightLayout.addWidget(weightLabel);
		weightLayout.addWidget(weightSpinner);
		weightGroup.setLayout(weightLayout);
		

		// Index sleep interval
		QGroupBox sleepGroup = new QGroupBox(tr("Index Interval"));
		QLabel sleepLabel = new QLabel(tr("Seconds between looking for unindexed notes"));
		sleepSpinner = new QSpinBox();
		sleepSpinner.setRange(30,600);
		sleepSpinner.setSingleStep(1);
		sleepSpinner.setValue(Global.getIndexThreadSleepInterval());

		QHBoxLayout sleepLayout = new QHBoxLayout();
		sleepLayout.addWidget(sleepLabel);
		sleepLayout.addWidget(sleepSpinner);
		sleepGroup.setLayout(sleepLayout);

		
		
		// Regular Expressions for word parsing
		QGroupBox regexGroup = new QGroupBox(tr("Word Parse"));
		QLabel regexLabel = new QLabel(tr("Regular Expression"));
		regexEdit = new QLineEdit();
		regexEdit.setText(Global.getWordRegex());

		QHBoxLayout regexLayout = new QHBoxLayout();
		regexLayout.addWidget(regexLabel);
		regexLayout.addWidget(regexEdit);		
		regexGroup.setLayout(regexLayout);
		
		
		QVBoxLayout mainLayout = new QVBoxLayout();
		mainLayout.addWidget(threadsGroup);
		mainLayout.addWidget(wordLengthGroup);
		mainLayout.addWidget(sleepGroup);
		mainLayout.addWidget(weightGroup);
		mainLayout.addWidget(regexGroup);
		mainLayout.addStretch(1);
		setLayout(mainLayout);


	}
	
	//*****************************************
	//* Word length get/set methods 
	//*****************************************
	public void setWordLength(int len) {
		lengthSpinner.setValue(len);
	}
	public int getWordLength() {
		return lengthSpinner.value();
	}
	
	
	//*****************************************
	//* Word length get/set methods 
	//*****************************************
	public void setSleepInterval(int len) {
		sleepSpinner.setValue(len);
	}
	public int getSleepInterval() {
		return sleepSpinner.value();
	}


	
	//*****************************************
	//* Recognition Weight 
	//*****************************************
	public void setRecognitionWeight(int len) {
		weightSpinner.setValue(len);
	}
	public int getRecognitionWeight() {
		return weightSpinner.value();
	}
	
	//*****************************************
	//* Index Threads get/set methods
	//*****************************************
	public void setIndexThreads(int value) {
		indexThreadSpinner.setValue(value);
	}
	public int getIndexThreads() {
		return indexThreadSpinner.value();
	}

	
	
	//*****************************************
	//* Regex get/set methods 
	//*****************************************
	public void setRegex(String s) {
		regexEdit.setText(s);
	}
	public String getRegex() {
		return regexEdit.text();
	}

}
