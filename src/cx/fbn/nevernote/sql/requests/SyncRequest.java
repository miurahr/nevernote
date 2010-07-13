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



public class SyncRequest extends DBRunnerRequest {
	public static int Create_Table     						= 1;
	public static int Drop_Table 							= 2;
	public static int Set_Record							= 3;
	public static int Get_Record							= 4;


	public volatile int			int1;
	public volatile long		long1;
	public volatile String		key;
	public volatile String		value;
	
	public String responseValue;
	
	public SyncRequest() {
		category = Sync;
	}
	
	public SyncRequest copy() {
		SyncRequest req = new SyncRequest();
		req.type = type;
		req.category = category;
		req.requestor_id = requestor_id;
		
		req.int1 = int1;
		req.long1 = long1;
		if (key != null) 
			req.key = new String(key);
		if (value != null) 
			req.value = new String(value);
		if (responseValue != null) 
			req.responseValue = new String(responseValue);

		return req;
	}
	
}