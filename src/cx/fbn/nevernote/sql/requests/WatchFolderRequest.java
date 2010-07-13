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

import cx.fbn.nevernote.sql.runners.WatchFolderRecord;


public class WatchFolderRequest extends DBRunnerRequest {
	public static int Create_Tables    			= 1;
	public static int Drop_Tables				= 2;
	public static int Expunge_Folder			= 3;
	public static int Get_All					= 4;
	public static int Add_Watch_Folder			= 5;
	public static int Expunge_All				= 6;
	public static int Get_Notebook				= 7;

	public volatile String			string1;
	public volatile String			string2;
	public volatile Boolean			bool1;
	public volatile Integer			int1;
	
	public List<WatchFolderRecord> responseWatchFolders;
	public String					responseString;
	
	public WatchFolderRequest() {
		category = WATCH_FOLDER;
	}
	
	public WatchFolderRequest copy() {
		WatchFolderRequest request = new WatchFolderRequest();
		
		request.requestor_id = requestor_id;
		request.type = type;
		request.category = category;
		if (string1 != null)
			request.string1 = new String(string1);
		if (string2 != null)
			request.string2 = new String(string2);
		if (int1 != null)
			request.int1 = new Integer(int1);
		if (bool1 != null)
			request.bool1 = new Boolean(bool1);
		
		if (responseWatchFolders != null) {
			request.responseWatchFolders = new ArrayList<WatchFolderRecord>();
			for (int i=0; i<responseWatchFolders.size(); i++) {
				WatchFolderRecord newRec = new WatchFolderRecord();
				newRec.depth = responseWatchFolders.get(i).depth;
				newRec.keep = responseWatchFolders.get(i).keep;
				newRec.folder = responseWatchFolders.get(i).folder;
				newRec.notebook = responseWatchFolders.get(i).notebook;
				request.responseWatchFolders.add(newRec);
			}
		}
		
		if (responseString != null) {
			request.responseString = new String(responseString);
		}
		
		return request;
	}
	
}