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


public class DatabaseRequest extends DBRunnerRequest {
        // NFC TODO: change to use an Enum and clarify distinction with constants on DBRunnerRequest 
	public static int Create_Tables    			= 1;
	public static int Drop_Tables				= 2;
	public static int Shutdown					= 4;
	public static int Compact					= 5;
	public static int Execute_Sql               = 6;
	public static int Execute_Sql_Index			= 7;
	public static int Backup_Database			= 8;

	public volatile String			string1;
	public volatile String			string2;
	public volatile int 			int1;
	public volatile long			long1;
	
	public DatabaseRequest() {
		category = DATABASE;
	}
	
	public DatabaseRequest copy() {
		DatabaseRequest request = new DatabaseRequest();
		
		request.requestor_id = requestor_id;
		request.type = type;
		request.category = category;
		request.long1 = long1;
		request.int1 = int1;
		if (string1 != null)
			request.string1 = new String(string1);
		if (string2 != null)
			request.string2 = new String(string2);
		
		return request;
	}
	
}