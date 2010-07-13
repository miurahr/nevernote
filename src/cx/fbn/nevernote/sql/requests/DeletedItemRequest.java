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


package cx.fbn.nevernote.sql.requests;

import java.util.ArrayList;
import java.util.List;

import cx.fbn.nevernote.sql.runners.DeletedItemRecord;

public class DeletedItemRequest extends DBRunnerRequest {
	public static int Create_Table     			= 1;
	public static int Drop_Table 				= 2;
	public static int Add_Deleted_Item			= 3;
	public static int Expunge_All				= 4;
	public static int Get_All					= 5;
	public static int Expunge_Record			= 6;

	public volatile String			string1;
	public volatile String			string2;
	
	public List<DeletedItemRecord>	responseDeletedRecords;
	
	public DeletedItemRequest() {
		category = DELETED_ITEM;
	}
	
	public DeletedItemRequest copy() {
		DeletedItemRequest request = new DeletedItemRequest();
		
		request.requestor_id = requestor_id;
		request.type = type;
		request.category = category;
		
		if (string1 != null)
			request.string1 = new String(string1);
		if (string2 != null)
			request.string2 = new String(string2);
		
		if (responseDeletedRecords != null) {
			request.responseDeletedRecords = new ArrayList<DeletedItemRecord>();
			for (int i=0; i<responseDeletedRecords.size(); i++) {
				DeletedItemRecord record = new DeletedItemRecord();
				record.guid = new String(responseDeletedRecords.get(i).guid);
				record.type = new String(responseDeletedRecords.get(i).type);
				request.responseDeletedRecords.add(record);
			}
		}
		
		return request;
	}
	
}