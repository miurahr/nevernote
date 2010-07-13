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


package cx.fbn.nevernote.sql;

import java.util.List;

import cx.fbn.nevernote.Global;
import cx.fbn.nevernote.sql.requests.WatchFolderRequest;
import cx.fbn.nevernote.sql.runners.WatchFolderRecord;
import cx.fbn.nevernote.utilities.ListManager;

public class WatchFolderTable {
	ListManager parent;
	int id;
	
	// Constructor
	public WatchFolderTable(int i) {
		id = i;
	}
	// Create the table
	public void createTable() {
		WatchFolderRequest request = new WatchFolderRequest();
		request.requestor_id = id;
		request.type = WatchFolderRequest.Create_Tables;
		Global.dbRunner.addWork(request);
	}
	// Drop the table
	public void dropTable() {
		WatchFolderRequest request = new WatchFolderRequest();
		request.requestor_id = id;
		request.type = WatchFolderRequest.Drop_Tables;
		Global.dbRunner.addWork(request);
	}
	// Add an item to the deleted table
	public void addWatchFolder(String folder, String notebook, boolean keep, int depth) {
		WatchFolderRequest request = new WatchFolderRequest();
		request.requestor_id = id;
		request.string1 = folder;
		request.string2 = notebook;
		request.bool1 = keep;
		request.int1 = depth;
		request.type = WatchFolderRequest.Add_Watch_Folder;
		Global.dbRunner.addWork(request);
	}
	public List<WatchFolderRecord> getAll() {
		WatchFolderRequest request = new WatchFolderRequest();
		request.requestor_id = id;
		request.type = WatchFolderRequest.Get_All;
		Global.dbRunner.addWork(request);
		Global.dbClientWait(id);
		WatchFolderRequest req = Global.dbRunner.watchFolderResponse.get(id).copy();
		return req.responseWatchFolders;
	}
	public void expungeFolder(String folder) {
		WatchFolderRequest request = new WatchFolderRequest();
		request.requestor_id = id;
		request.string1 = folder;
		request.type = WatchFolderRequest.Expunge_Folder;
		Global.dbRunner.addWork(request);
	}
	public void expungeAll() {
		WatchFolderRequest request = new WatchFolderRequest();
		request.requestor_id = id;
		request.type = WatchFolderRequest.Expunge_All;
		Global.dbRunner.addWork(request);
	}
	public String getNotebook(String dir) {
		WatchFolderRequest request = new WatchFolderRequest();
		request.requestor_id = id;
		request.type = WatchFolderRequest.Get_Notebook;
		request.string1 = dir;
		Global.dbRunner.addWork(request);
		Global.dbClientWait(id);
		WatchFolderRequest req = Global.dbRunner.watchFolderResponse.get(id).copy();
		return req.responseString;
	}

}
