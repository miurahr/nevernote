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

package cx.fbn.nevernote.gui;

import java.util.List;

import com.trolltech.qt.core.Qt.WidgetAttribute;
import com.trolltech.qt.gui.QCloseEvent;
import com.trolltech.qt.gui.QMdiSubWindow;

import cx.fbn.nevernote.Global;
import cx.fbn.nevernote.sql.DatabaseConnection;

public class ExternalBrowse extends QMdiSubWindow {
	private final DatabaseConnection  conn;
	private final BrowserWindow	browser;
	public Signal4<String, String, Boolean, BrowserWindow> contentsChanged;
	public Signal1<String>	windowClosing;
	boolean noteDirty;
	
	// Constructor
	public ExternalBrowse(DatabaseConnection c) {
		setAttribute(WidgetAttribute.WA_QuitOnClose, false);
		setWindowTitle(tr("NeverNote"));
		conn = c;
		contentsChanged = new Signal4<String, String, Boolean, BrowserWindow>();
		windowClosing = new Signal1<String>();
		browser = new BrowserWindow(conn);
		setWidget(browser);
		noteDirty = false;
		browser.titleLabel.textChanged.connect(this, "titleChanged(String)");
		browser.getBrowser().page().contentsChanged.connect(this, "contentChanged()");
	}
	
	private void contentChanged() {
		noteDirty = true;
		contentsChanged.emit(getBrowserWindow().getNote().getGuid(), getBrowserWindow().getContent(), false, getBrowserWindow());
	}

	
	@Override
	public void closeEvent(QCloseEvent event) {
		if (noteDirty) 
			contentsChanged.emit(getBrowserWindow().getNote().getGuid(), getBrowserWindow().getContent(), true, getBrowserWindow());
		windowClosing.emit(getBrowserWindow().getNote().getGuid());
	}
	
    public BrowserWindow getBrowserWindow() {
    	return browser;
    }
    
    private void titleChanged(String value) {
    	setWindowTitle(tr("NeverNote - ") +value);
    }
    
	private void updateTitle(String guid, String title) {
		if (guid.equals(getBrowserWindow().getNote().getGuid())) {
			getBrowserWindow().loadingData(true);
			getBrowserWindow().setTitle(title);
			getBrowserWindow().getNote().setTitle(title);
			getBrowserWindow().loadingData(false);
		}
	}
	private void updateNotebook(String guid, String notebook) {
		if (guid.equals(getBrowserWindow().getNote().getGuid())) {
			getBrowserWindow().loadingData(true);
			getBrowserWindow().setNotebook(notebook);
			getBrowserWindow().loadingData(false);
		}
	}
	
	private void updateTags(String guid, List<String> tags) {
		if (guid.equals(getBrowserWindow().getNote().getGuid())) {
			StringBuffer tagLine = new StringBuffer(100);
			for (int i=0; i<tags.size(); i++) {
				if (i>0)
					tagLine.append(Global.tagDelimeter+" ");
				tagLine.append(tags.get(i));
				
			}
			getBrowserWindow().loadingData(true);
			getBrowserWindow().getTagLine().setText(tagLine.toString());
			getBrowserWindow().loadingData(false);
		}
	}

}
