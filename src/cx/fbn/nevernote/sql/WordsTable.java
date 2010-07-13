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
import cx.fbn.nevernote.sql.requests.WordRequest;


public class WordsTable {
	private final int id;
	
	// Constructor
	public WordsTable(int i) {
		id = i;
	}
	// Create the table
	public void createTable() {
		WordRequest request = new WordRequest();
		request.requestor_id = id;
		request.type = WordRequest.Create_Table;
		Global.dbRunner.addWork(request);
	}
	// Drop the table
	public void dropTable() {
		WordRequest request = new WordRequest();
		request.requestor_id = id;
		request.type = WordRequest.Drop_Table;
		Global.dbRunner.addWork(request);
	}
	// Count unindexed notes
	public int getWordCount() {
		WordRequest request = new WordRequest();
		request.requestor_id = id;
		request.type = WordRequest.Get_Word_Count;
		Global.dbRunner.addWork(request);
		Global.dbClientWait(id);
		WordRequest req = Global.dbRunner.wordResponse.get(id).copy();
		return req.responseInt;
	}

	// Clear out the word index table
	public void clearWordIndex() {
		WordRequest request = new WordRequest();
		request.requestor_id = id;
		request.type = WordRequest.Clear_Word_Index;
		Global.dbRunner.addWork(request);
	}	

	//********************************************************************************
	//********************************************************************************
	//* Support adding & deleting index words
	//********************************************************************************
	//********************************************************************************
	public void expungeFromWordIndex(String guid, String type) {
		WordRequest request = new WordRequest();
		request.requestor_id = id;
		request.type = WordRequest.Expunge_From_Word_Index;
		request.string1 = guid;
		request.string2 = type;
		Global.dbRunner.addWork(request);
	}
	// Reindex a note
	public synchronized void addWordToNoteIndex(String guid, String word, String type, Integer weight) {
		WordRequest request = new WordRequest();
		request.requestor_id = id;
		request.type = WordRequest.Add_Word_To_Note_Index;
		request.string1 = guid;
		request.string2 = word;
		request.string3 = type;
		request.int1 = weight;
		Global.dbRunner.addWork(request);
		Global.dbClientWait(id);
	}


}
