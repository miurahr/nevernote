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



//**********************************************
//**********************************************
//* This dialog is the debugging information 
//* page used in the Edit/Preferences dialog.  
//* It is the spelling dialog.
//**********************************************
//**********************************************
package cx.fbn.nevernote.dialog;

import com.swabunga.spell.engine.Configuration;
import com.trolltech.qt.gui.QCheckBox;
import com.trolltech.qt.gui.QVBoxLayout;
import com.trolltech.qt.gui.QWidget;

import cx.fbn.nevernote.Global;

public class ConfigSpellPage extends QWidget {
	

	private final QCheckBox ignoreDigitWords;
	private final QCheckBox	ignoreInternetAddresses;
	private final QCheckBox ignoreMixedCase;
	private final QCheckBox ignoreUpperCase;
	private final QCheckBox	ignoreSentenceCapitalization;

	
	public ConfigSpellPage(QWidget parent) {	
		ignoreDigitWords = new QCheckBox(tr("Ignore words with numbers in them"));
		ignoreInternetAddresses = new QCheckBox(tr("Ignore internet addresses (i.e. http:// ftp://)"));
		ignoreMixedCase = new QCheckBox(tr("Ignore words with upper and lower cased letters"));
		ignoreUpperCase = new QCheckBox(tr("Ignore upper cased words"));
		ignoreSentenceCapitalization = new QCheckBox(tr("Ignore that sentences should begin with a capital letter"));
		
		setIgnoreDigitWords(Global.getSpellSetting(Configuration.SPELL_IGNOREDIGITWORDS));
		setIgnoreInternetAddresses(Global.getSpellSetting(Configuration.SPELL_IGNOREINTERNETADDRESSES));
		setIgnoreMixedCase(Global.getSpellSetting(Configuration.SPELL_IGNOREMIXEDCASE));
		setIgnoreUpperCase(Global.getSpellSetting(Configuration.SPELL_IGNOREUPPERCASE));
		setIgnoreSentenceCapitalization(Global.getSpellSetting(Configuration.SPELL_IGNORESENTENCECAPITALIZATION));
		
		QVBoxLayout mainLayout = new QVBoxLayout();
		mainLayout.addWidget(ignoreDigitWords);
		mainLayout.addWidget(ignoreInternetAddresses);
		mainLayout.addWidget(ignoreMixedCase);
		mainLayout.addWidget(ignoreUpperCase);
		mainLayout.addWidget(ignoreSentenceCapitalization);
		mainLayout.addStretch(1);
		setLayout(mainLayout);
	}
	
	//****************************************
	//* Getter/Setter for ignore digit words *
	//****************************************
	public boolean getIgnoreDigitWords() {
		return ignoreDigitWords.isChecked();
	}
	public void setIgnoreDigitWords(boolean value) {
		ignoreDigitWords.setChecked(value);
	}
	
	//***********************************************
	//* Getter/Setter for ignore internet addresses *
	//***********************************************
	public boolean getIgnoreInternetAddresses() {
		return ignoreInternetAddresses.isChecked();
	}
	public void setIgnoreInternetAddresses(boolean value) {
		ignoreInternetAddresses.setChecked(value);
	}
	
	//*********************************************
	//* Getter/Setter for ignore Mixed Case words *
	//*********************************************
	public boolean getIgnoreMixedCase() {
		return ignoreMixedCase.isChecked();
	}
	public void setIgnoreMixedCase(boolean value) {
		ignoreMixedCase.setChecked(value);
	}
	
	//*********************************************
	//* Getter/Setter for ignore Mixed Case words *
	//*********************************************
	public boolean getIgnoreUpperCase() {
		return ignoreUpperCase.isChecked();
	}
	public void setIgnoreUpperCase(boolean value) {
		ignoreUpperCase.setChecked(value);
	}
	
	//****************************************************************
	//* Getter/Setter for ignore that sentences begin with a capital *
	//****************************************************************
	public boolean getIgnoreSentenceCapitalization() {
		return ignoreSentenceCapitalization.isChecked();
	}
	public void setIgnoreSentenceCapitalization(boolean value) {
		ignoreSentenceCapitalization.setChecked(value);
	}

}
