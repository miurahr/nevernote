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
//* page used in the Edit/Preferences dialog
//**********************************************
//**********************************************

package cx.fbn.nevernote.dialog;

import com.trolltech.qt.gui.QCheckBox;
import com.trolltech.qt.gui.QComboBox;
import com.trolltech.qt.gui.QGroupBox;
import com.trolltech.qt.gui.QHBoxLayout;
import com.trolltech.qt.gui.QLabel;
import com.trolltech.qt.gui.QSpinBox;
import com.trolltech.qt.gui.QTextBrowser;
import com.trolltech.qt.gui.QVBoxLayout;
import com.trolltech.qt.gui.QWidget;

import cx.fbn.nevernote.Global;

public class ConfigDebugPage extends QWidget {
	
	QComboBox messageCombo;
	QComboBox serverCombo;
	QCheckBox disableUploads;
	QCheckBox carriageReturnFix;
	QCheckBox htmlEntitiesFix;
	QCheckBox enableThumbnails;
	QSpinBox  databaseCache;
	QCheckBox reloadSharedNotebooks;
	
	public ConfigDebugPage(QWidget parent) {
		super(parent);
		// Server settings
		QGroupBox serverGroup =  new QGroupBox(tr("Server Configuration"));
		QLabel serverLabel = new QLabel(tr("Server"));
		serverCombo = new QComboBox();
		serverCombo.addItem("www.evernote.com");
		serverCombo.addItem("sandbox.evernote.com");
		disableUploads = new QCheckBox();
		disableUploads.setText(tr("Disable uploads to server"));

		QHBoxLayout serverLayout = new QHBoxLayout();
		serverLayout.addWidget(serverLabel);
		serverLayout.addWidget(serverCombo);
		serverLayout.addWidget(disableUploads);
		serverGroup.setLayout(serverLayout);

		QLabel messageLevelLabel = new QLabel(tr("Message Level"));
		messageCombo = new QComboBox();
		messageCombo.addItem(tr("Low"),"Low");
		messageCombo.addItem(tr("Medium"),"Medium");
		messageCombo.addItem(tr("High"),"High");
		messageCombo.addItem(tr("Extreme"),"Extreme");
		
		QHBoxLayout messageLayout = new QHBoxLayout();
		messageLayout.addWidget(messageLevelLabel);
		messageLayout.addWidget(messageCombo);
		messageLayout.setStretch(1, 100);
		
		
		QHBoxLayout databaseCacheLayout = new QHBoxLayout();
		databaseCache = new QSpinBox();
		databaseCacheLayout.addWidget(new QLabel(tr("Database Cache (MB) - Requires restart")));
		databaseCache.setMinimum(4);
		databaseCache.setMaximum(128);
		databaseCache.setValue(new Integer(Global.databaseCache)/1024);
		databaseCacheLayout.addWidget(databaseCache);
		databaseCacheLayout.setStretch(1, 100);
		
		QVBoxLayout mainLayout = new QVBoxLayout();
		mainLayout.addLayout(messageLayout);
		mainLayout.addLayout(databaseCacheLayout);
		
		QHBoxLayout thumbnailLayout = new QHBoxLayout();
		QLabel thumbnailLabel = new QLabel(tr("Enable Thumbnails (experimental)"));
		thumbnailLayout.addWidget(thumbnailLabel);
		enableThumbnails = new QCheckBox(this);
		thumbnailLayout.addWidget(enableThumbnails);
		mainLayout.addLayout(thumbnailLayout);
		
		mainLayout.addWidget(serverGroup);
		
		QGroupBox crlfGroup = new QGroupBox(tr("Carriage Return Fix"));
		String crlfMessage = new String(tr("Note: The carriage return is a test fix.  If you " +
		"enable it, it will do some modifications to the notes you view to try and" +
		" get the carriage returns to look correct.  This is due to the way that " +
		"the way Evernote 3.1 Windows client is dealing with carriage returns.  This fix"+
		"will try and correct this problem.  This fix is not permanent unless you edit a note.  If" +
		"you edit a note, this fix is PERMANENT and will be sent to Evernote on the next sync.  I haven't" +
		"had any issues with this, but please be aware of this condition."));
		carriageReturnFix = new QCheckBox(this);
		QVBoxLayout crlfLayout = new QVBoxLayout();
		carriageReturnFix.setText(tr("Enable Carriage Return Fix"));
		crlfLayout.addWidget(carriageReturnFix);
		crlfGroup.setLayout(crlfLayout);

		QGroupBox htmlGroup = new QGroupBox(tr("Android Note Fix"));
		String entitiesMessage = new String(tr("Note: This is an experimental fix to correct Unicode" +
				" notes created on Android Evernote clients."));
		htmlEntitiesFix = new QCheckBox(this);
		QVBoxLayout htmlLayout = new QVBoxLayout();
		htmlEntitiesFix.setText(tr("Enable Android Fix"));
		htmlLayout.addWidget(htmlEntitiesFix);
		htmlGroup.setLayout(htmlLayout);
		
		reloadSharedNotebooks = new QCheckBox(tr("Shared Notebooks"));
		QGroupBox refresh = new QGroupBox(tr("Special Refresh (WARNING - This can cause unsynchronized data loss)."));
		QVBoxLayout refreshLayout = new QVBoxLayout();
		refreshLayout.addWidget(reloadSharedNotebooks);
		refresh.setLayout(refreshLayout);
		
		
		QTextBrowser msg = new QTextBrowser(this);
		QTextBrowser htmlMsg = new QTextBrowser(this);
		msg.setText(crlfMessage);
		htmlMsg.setText(entitiesMessage);
		crlfLayout.addWidget(msg);
		mainLayout.addWidget(crlfGroup);
		htmlLayout.addWidget(htmlMsg);
		mainLayout.addWidget(htmlGroup);
		mainLayout.addWidget(refresh);

		mainLayout.addStretch(1);
		setLayout(mainLayout);
		
		serverCombo.activated.connect(this, "serverOptionChanged()");
	}
	
	//******************************************
	//* Message set/get
	//******************************************
	public void setDebugLevel(String level) {
		int i = messageCombo.findData(level);
		if (i>0)
			messageCombo.setCurrentIndex(i);
	}
	public String getDebugLevel() {
		int i = messageCombo.currentIndex();
		return messageCombo.itemData(i).toString();
	}


	//******************************************
	//* Experimental fixes
	//******************************************
	public void setHtmlEntitiesFix(boolean val) {
		htmlEntitiesFix.setChecked(val);
	}
	public boolean getHtmlEntitiesFix() {
		return htmlEntitiesFix.isChecked();
	}
	public void setCarriageReturnFix(boolean val) {
		carriageReturnFix.setChecked(val);
	}
	public boolean getCarriageReturnFix() {
		return carriageReturnFix.isChecked();
	}

	
	//******************************************
	//* Server set/get
	//******************************************
	public void setServer(String server) {
		int i = serverCombo.findText(server);
		if (i>0)
			serverCombo.setCurrentIndex(i);
	}
	public String getServer() {
		int i = serverCombo.currentIndex();
		return serverCombo.itemText(i);
	}
	@SuppressWarnings("unused")
	private void serverOptionChanged() {
		String text = serverCombo.currentText();
		if (text.equalsIgnoreCase("www.evernote.com")) 
			disableUploads.setChecked(true);
	}
	//*****************************************
	//* Disable uploads 
	//*****************************************
	public void setDisableUploads(boolean val) {
		disableUploads.setChecked(val);
	}
	public boolean getDisableUploads() {
		return disableUploads.isChecked();
	}
	
	//****************************************
	//* Thumbnails
	//****************************************
	public void setEnableThumbnails(boolean val) {
		enableThumbnails.setChecked(val);
	}
	
	public boolean getEnableThumbnails() {
		return enableThumbnails.isChecked();
	}

	
	public String getDatabaseCacheSize() {
		return new Integer(databaseCache.value()*1024).toString();
	}

	
	//***************************************
	//* Special refreshes
	//***************************************
	public boolean reloadSharedNotebooksClicked() {
		return reloadSharedNotebooks.isChecked();
	}

}
