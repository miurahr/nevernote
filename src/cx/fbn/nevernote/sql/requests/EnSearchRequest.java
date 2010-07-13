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

import com.evernote.edam.type.Note;
import com.evernote.edam.type.Tag;


public class EnSearchRequest extends DBRunnerRequest {
	public volatile String			string1;
	public volatile List<Tag>		tags;
	public volatile int 				int1;
	public volatile int				int2;
	public volatile List<Note>		responseNotes;
	public volatile List<String>	responseStrings;

	
	public EnSearchRequest() {
		category = ENSEARCH;
	}
	
	public EnSearchRequest copy() {
		EnSearchRequest request = new EnSearchRequest();
		
		request.requestor_id = requestor_id;
		request.type = type;
		request.int1 = int1;
		request.int2 = int2;
		request.category = category;
		if (string1 != null)
			request.string1 = new String(string1);
		if (tags!=null) { 
			request.tags = new ArrayList<Tag>();
			for (int i=0; i<tags.size(); i++)
				request.tags.add(tags.get(i).deepCopy());
		}
		
		if (responseNotes != null) {
			request.responseNotes = new ArrayList<Note>();
			for (int i=0; i<responseNotes.size(); i++) {
				request.responseNotes.add(responseNotes.get(i).deepCopy());
			}
		}
		
		if (responseStrings != null) {
			request.responseStrings = new ArrayList<String>();
			for (int i=0; i<responseStrings.size(); i++) {
				request.responseStrings.add(new String(responseStrings.get(i)));
			}
		}

		return request;
	}
	
}