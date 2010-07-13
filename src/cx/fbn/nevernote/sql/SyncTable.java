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

import cx.fbn.nevernote.Global;
import cx.fbn.nevernote.sql.requests.SyncRequest;

public class SyncTable {
	int id;
	
	// Constructor
	public SyncTable(int i) {
		id = i;
	}
	// Create the table
	public void createTable() {
		SyncRequest request = new SyncRequest();
		request.requestor_id = id;
		request.type = SyncRequest.Create_Table;
		Global.dbRunner.addWork(request);
	}
	// Drop the table
	public void dropTable() {
		SyncRequest request = new SyncRequest();
		request.requestor_id = id;
		request.type = SyncRequest.Drop_Table;
		Global.dbRunner.addWork(request);
	}
	// Set the last sequence date
	public void setLastSequenceDate(long date) {
		SyncRequest request = new SyncRequest();
		request.requestor_id = id;
		request.type = SyncRequest.Set_Record;
		request.key = "LastSequenceDate";
		request.value = new Long(date).toString();
		Global.dbRunner.addWork(request);
	}
	// Set the last sequence date
	public void setUpdateSequenceNumber(int number) {
		SyncRequest request = new SyncRequest();
		request.requestor_id = id;
		request.type = SyncRequest.Set_Record;
		request.key = "UpdateSequenceNumber";
		request.value = new Integer(number).toString();
		Global.dbRunner.addWork(request);
	}
	// get last sequence date
	public long getLastSequenceDate() {
		SyncRequest request = new SyncRequest();
		request.requestor_id = id;
		request.type = SyncRequest.Get_Record;
		request.key = "LastSequenceDate";
		Global.dbRunner.addWork(request);
		Global.dbClientWait(id);
		SyncRequest req = Global.dbRunner.syncResponse.get(id).copy();
		Long date = new Long(req.responseValue);
		return date;
	}
	// Get invalid attributes for a given element
	public int getUpdateSequenceNumber() {
		SyncRequest request = new SyncRequest();
		request.requestor_id = id;
		request.type = SyncRequest.Get_Record;
		request.key = "UpdateSequenceNumber";
		Global.dbRunner.addWork(request);
		Global.dbClientWait(id);
		SyncRequest req = Global.dbRunner.syncResponse.get(id).copy();
		Integer number = new Integer(req.responseValue);
		return number;
	}

	
	

}
