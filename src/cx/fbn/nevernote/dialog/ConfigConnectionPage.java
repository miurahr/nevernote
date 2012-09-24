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
//* This dialog is the Edit/Preferences dialog 
//* when "Connection" is selected.  Used to store
//* Evernote information.
//**********************************************
//**********************************************

package cx.fbn.nevernote.dialog;

import java.util.List;

import com.trolltech.qt.gui.QCheckBox;
import com.trolltech.qt.gui.QComboBox;
import com.trolltech.qt.gui.QFormLayout;
import com.trolltech.qt.gui.QGridLayout;
import com.trolltech.qt.gui.QGroupBox;
import com.trolltech.qt.gui.QLabel;
import com.trolltech.qt.gui.QLineEdit;
import com.trolltech.qt.gui.QSpinBox;
import com.trolltech.qt.gui.QVBoxLayout;
import com.trolltech.qt.gui.QWidget;

import cx.fbn.nevernote.Global;
import cx.fbn.nevernote.utilities.SyncTimes;

public class ConfigConnectionPage extends QWidget {

	private final QCheckBox autoLogin;
	private final QComboBox syncInterval;
	private final SyncTimes syncTimes;
	private final QCheckBox	synchronizeOnClose;
	private final QCheckBox	synchronizeDeletedContents;
	
	private final QLineEdit proxyHost;
	private final QSpinBox proxyPort;
	private final QLineEdit proxyUserid;
	private final QLineEdit proxyPassword;
	
	public ConfigConnectionPage(QWidget parent) {
		
		// Userid settings
		QGroupBox useridGroup = new QGroupBox(tr("Connection"));
		
		syncInterval = new QComboBox(this);
		syncTimes = new SyncTimes();
		syncInterval.addItems(syncTimes.stringValues());
		
		autoLogin = new QCheckBox("Automatic Connect");
		synchronizeDeletedContents = new QCheckBox("Synchronze Deleted Note Content");
		synchronizeOnClose = new QCheckBox("Synchronize On Shutdown (only if connected)");
		
		QGroupBox proxyGroup = new QGroupBox("Proxy Settings");
		QLabel proxyHostLabel = new QLabel(tr("Host"));
		QLabel proxyPortLabel = new QLabel(tr("Port"));
		QLabel proxyUseridLabel = new QLabel(tr("Userid"));
		QLabel proxyPasswordLabel = new QLabel(tr("Password"));
		proxyHost = new QLineEdit();
		proxyPort = new QSpinBox();
		proxyUserid = new QLineEdit();
		proxyPassword = new QLineEdit();
		proxyPassword.setEchoMode(QLineEdit.EchoMode.Password);
		
		proxyHost.setText(Global.getProxyValue("url"));
		String portString = Global.getProxyValue("port");
		Integer port = new Integer(80);
		try {
			port = new Integer(portString.trim());
		} catch (Exception e) {
		}

		proxyPort.setMinimum(1);
		proxyPort.setMaximum(65565);
		proxyPort.setValue(port);
		proxyUserid.setText(Global.getProxyValue("userid"));
		proxyPassword.setText(Global.getProxyValue("password"));
		
		if (!proxyHost.text().trim().equals("") && proxyPort.text().trim().equals(""))
			proxyPort.setValue(80);
		
		
		QFormLayout useridLayout = new QFormLayout();
		useridLayout.addWidget(new QLabel(tr("Syncronization Interval")));
		useridLayout.addWidget(syncInterval);
		useridLayout.addWidget(autoLogin);
		useridLayout.addWidget(synchronizeOnClose);
		useridLayout.addWidget(synchronizeDeletedContents);
		
		QGridLayout proxyLayout = new QGridLayout();
		proxyLayout.addWidget(proxyHostLabel,1,1);
		proxyLayout.addWidget(proxyHost,1,2);
		proxyLayout.addWidget(proxyPortLabel,2,1);
		proxyLayout.addWidget(proxyPort,2,2);
		proxyLayout.addWidget(proxyUseridLabel,3,1);
		proxyLayout.addWidget(proxyUserid,3,2);
		proxyLayout.addWidget(proxyPasswordLabel,4,1);
		proxyLayout.addWidget(proxyPassword,4,2);
				
		useridGroup.setLayout(useridLayout);
		proxyGroup.setLayout(proxyLayout);
		
		// Add everything together
		QVBoxLayout mainLayout = new QVBoxLayout();
		mainLayout.addWidget(useridGroup);
		mainLayout.addWidget(proxyGroup);
		mainLayout.addStretch(1);
		setLayout(mainLayout);
		
	}

	
	
	
	//*******************************************
	//* Automatic login get/set
	//*******************************************
	public void setAutomaticLogin(boolean val) {
		autoLogin.setChecked(val);
	}
	public boolean getAutomaticLogin() {
		return autoLogin.isChecked();
	}

	

	//*****************************************
	//* Synchronize Deleted Note Content
	//*****************************************
	public void setSyncronizeDeletedContent(boolean val) {
		synchronizeDeletedContents.setChecked(val);
	}
	public boolean getSynchronizeDeletedContent() {
		return synchronizeDeletedContents.isChecked();
	}
	

	
	//******************************************
	//* Get Proxy settings
	//******************************************
	public String getProxyUrl() {
		return proxyHost.text().trim();
	}
	public String getProxyPort() {
		if (!proxyHost.text().trim().equalsIgnoreCase("") && proxyPort.text().trim().equals(""))
			return "80";
		if (proxyHost.text().trim().equals(""))
			return "";
		return proxyPort.text().trim();
	}
	public String getProxyUserid() {
		if (proxyHost.text().trim().equals(""))
			return "";
		return proxyUserid.text().trim();
	}
	public String getProxyPassword() {
		if (proxyHost.text().trim().equals(""))
			return "";
		return proxyPassword.text().trim();
	}

	
	
	
	//*****************************************
	//* Get/set synchronize on close
	//*****************************************
	public boolean getSynchronizeOnClose() {
		return synchronizeOnClose.isChecked();
	}
	public void setSynchronizeOnClose(boolean val) {
		synchronizeOnClose.setChecked(val);
	}
	
	
	//*****************************************
	//* Get/set sync intervals
	//*****************************************
	public String getSyncInterval() {
		int i = syncInterval.currentIndex();
		return syncInterval.itemText(i);	
	}
	public void setSyncInterval(String s) {
		List<String> vals = syncTimes.stringValues();
		for (int i=0; i<vals.size(); i++) {
			if (vals.get(i).equalsIgnoreCase(s))
				syncInterval.setCurrentIndex(i);
		}
	}
}
