/*
 * This file is part of NixNote 
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

import java.util.List;

import com.evernote.edam.type.Note;
import com.evernote.edam.type.Tag;

import cx.fbn.nevernote.sql.DatabaseConnection;
import cx.fbn.nevernote.sql.REnSearch;
import cx.fbn.nevernote.utilities.ApplicationLogger;

public class EnSearch {
	

	private List<Note>			matches;
	public List<String>			hilightWords;
	
	public EnSearch(DatabaseConnection conn, ApplicationLogger logger, String s, List<Tag> t, int weight) {
		if (s == null) 
			return;
		if (s.trim().equals(""))
			return;
		
		matches = null;
		REnSearch request = new REnSearch(conn, logger, s, t, weight);
		matches = request.matchWords();
		hilightWords = request.getWords();
	}
		
	public List<Note> matchWords() {
		return matches;
	}
	


	
}
