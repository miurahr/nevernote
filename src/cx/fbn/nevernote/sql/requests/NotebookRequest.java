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

import com.evernote.edam.type.Notebook;

import cx.fbn.nevernote.utilities.Pair;

public class NotebookRequest extends DBRunnerRequest {
	public static int Create_Table     			= 1;
	public static int Drop_Table 				= 2;
//	public static int Get_Notebook				= 3;
//	public static int Get_All_Notebooks			= 4;
	public static int Add_Notebook				= 5;
	public static int Expunge_Notebook 			= 6;
	public static int Find_Note_By_Name 		= 7;
	public static int Get_All					= 8;
	public static int Get_Dirty					= 9;
	public static int Is_Notebook_Local			= 10;
	public static int Reset_Dirty				= 11;
//	public static int Save_Notebooks			= 12;
	public static int Set_Archived				= 13;
	public static int Sync_Notebook				= 14;
	public static int Update_Notebook			= 15;
	public static int Update_Notebook_Guid		= 16;
	public static int Update_Notebook_Sequence 	= 17;
	public static int Get_All_Local				= 18;
	public static int Get_All_Archived			= 19;
	public static int Notebook_Counts			= 20;

	public volatile boolean			bool1;
	public volatile boolean			bool2;
	public volatile String			string1;
	public volatile String			string2;
	public volatile Notebook 		notebook;
	public volatile int				int1;
	
	public volatile List<Notebook>	responseNotebooks;
	public volatile boolean			responseBoolean;
	public volatile String			responseString;
	public volatile List<Pair<String,Integer>>		responseCounts;
	
	public NotebookRequest() {
		category = NOTEBOOK;
	}
	
	public NotebookRequest copy() {
		NotebookRequest request = new NotebookRequest();
		
		request.category = category;
		request.requestor_id = requestor_id;
		request.type = type;
		request.bool1 = bool1;
		request.bool2 = bool2;
		if (string1 != null)
			request.string1 = new String(string1);
		if (string2 != null)
			request.string2 = new String(string2);
		if (notebook != null)
			request.notebook = notebook.deepCopy();
		if (responseNotebooks != null) {
			request.responseNotebooks = new ArrayList<Notebook>();
			for (int i=0; i<responseNotebooks.size(); i++) {
				request.responseNotebooks.add(responseNotebooks.get(i).deepCopy());
			}
		}
		if (responseString != null) {
			request.responseString = new String(responseString);
		}
	
		if (responseCounts != null) {
			request.responseCounts = new ArrayList<Pair<String,Integer>>();
			for (int i=0; i<responseCounts.size(); i++) {
				Pair<String,Integer> newPair = new Pair<String,Integer>();
				newPair.setFirst(responseCounts.get(i).getFirst());
				newPair.setSecond(responseCounts.get(i).getSecond());
				request.responseCounts.add(newPair);
			}
		}
		
		
		return request;
	}
	
}