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


package cx.fbn.nevernote.filters;

import java.util.ArrayList;
import java.util.List;

import com.evernote.edam.type.Note;
import com.evernote.edam.type.Tag;

import cx.fbn.nevernote.Global;
import cx.fbn.nevernote.sql.requests.EnSearchRequest;

public class EnSearch {
	

	private List<Note>			matches;
	public List<String>			hilightWords;
	int id;
	
	public EnSearch(int i, String s, List<Tag> t, int len, int weight) {
		id = i;
		if (s == null) 
			return;
		if (s.trim().equals(""))
			return;
		
		matches = null;
		EnSearchRequest request = new EnSearchRequest();
		request.requestor_id = id;
//		request.type = DeletedItemRequest.Get_All;
		request.string1 = s;
		if (t!=null) {
			request.tags = new ArrayList<Tag>();
			for (int k=0; k<t.size(); k++) {
				request.tags.add(t.get(k).deepCopy());
			}
		}
		request.int1 = len;
		request.int2 = weight;
		Global.dbRunner.addWork(request);
		Global.dbClientWait(id);
		EnSearchRequest req = Global.dbRunner.enSearchResponse.get(id);
		hilightWords = req.responseStrings;
		matches = req.responseNotes;
	}
		
	public List<Note> matchWords() {
		return matches;
	}
	


	
}
