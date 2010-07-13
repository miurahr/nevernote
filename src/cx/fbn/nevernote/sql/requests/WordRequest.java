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


public class WordRequest extends DBRunnerRequest {
	public static int Create_Table     			= 1;
	public static int Drop_Table 				= 2;
	public static int Get_Word_Count			= 3;
	public static int Clear_Word_Index			= 4;
	public static int Expunge_From_Word_Index	= 5;
	public static int Add_Word_To_Note_Index	= 6;


	public volatile String			string1;
	public volatile String			string2;
	public volatile String			string3;
	public volatile String			string4;
	public volatile int				int1;
	
	public int responseInt;
	
	public WordRequest() {
		category = WORD;
	}
	
	public WordRequest copy() {
		WordRequest req = new WordRequest();
		req.type = type;
		req.category = category;
		req.requestor_id = requestor_id;
		
		req.responseInt = responseInt;
		
		if (string1  != null)
			req.string1 = new String(string1);
		if (string2  != null)
			req.string2 = new String(string2);
		if (string3  != null)
			req.string3 = new String(string3);
		if (string4  != null)
			req.string4 = new String(string4);
		req.int1 = int1;

		return req;
	}
	
}