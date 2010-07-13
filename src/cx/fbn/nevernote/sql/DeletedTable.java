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
import cx.fbn.nevernote.sql.requests.DeletedItemRequest;
import cx.fbn.nevernote.sql.runners.DeletedItemRecord;
import cx.fbn.nevernote.utilities.ListManager;

public class DeletedTable {
	ListManager parent;
	int id;
	
	// Constructor
	public DeletedTable(int i) {
		id = i;
	}
	// Create the table
	public void createTable() {
		DeletedItemRequest request = new DeletedItemRequest();
		request.requestor_id = id;
		request.type = DeletedItemRequest.Create_Table;
		Global.dbRunner.addWork(request);
	}
	// Drop the table
	public void dropTable() {
		DeletedItemRequest request = new DeletedItemRequest();
		request.requestor_id = id;
		request.type = DeletedItemRequest.Drop_Table;
		Global.dbRunner.addWork(request);
	}
	// Add an item to the deleted table
	public void addDeletedItem(String guid, String type) {
		DeletedItemRequest request = new DeletedItemRequest();
		request.requestor_id = id;
		request.string1 = guid;
		request.string2 = type;
		request.type = DeletedItemRequest.Add_Deleted_Item;
		Global.dbRunner.addWork(request);
	}
	public List<DeletedItemRecord> getAllDeleted() {
		DeletedItemRequest request = new DeletedItemRequest();
		request.requestor_id = id;
		request.type = DeletedItemRequest.Get_All;
		Global.dbRunner.addWork(request);
		Global.dbClientWait(id);
		DeletedItemRequest req = Global.dbRunner.deletedItemResponse.get(id).copy();
		return req.responseDeletedRecords;
	}
	public void expungeDeletedItem(String guid, String type) {
		DeletedItemRequest request = new DeletedItemRequest();
		request.requestor_id = id;
		request.string1 = guid;
		request.string2 = type;
		request.type = DeletedItemRequest.Expunge_Record;
		Global.dbRunner.addWork(request);
	}
	public void expungeAllDeletedRecords() {
		DeletedItemRequest request = new DeletedItemRequest();
		request.requestor_id = id;
		request.type = DeletedItemRequest.Expunge_All;
		Global.dbRunner.addWork(request);
	}

}
