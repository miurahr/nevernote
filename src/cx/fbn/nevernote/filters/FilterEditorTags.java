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
import cx.fbn.nevernote.utilities.ApplicationLogger;

public class FilterEditorTags {
	DatabaseConnection conn;
	ApplicationLogger logger;
	public boolean permitNew;
	
	public FilterEditorTags(DatabaseConnection conn, ApplicationLogger logger) {
		this.logger = logger;
		this.conn = conn;
	}
	
	
	public List<Tag> getValidTags(Note note) {
		// Reset permit new flag
		permitNew = true;
		
		boolean linked = conn.getNotebookTable().isLinked(note.getNotebookGuid());
		
		// If the notebook is linked, then we really only need to return the one notebookd
		if (linked) {
			permitNew = false;
			return conn.getTagTable().getTagsForNotebook(note.getNotebookGuid());
		} else
			return conn.getTagTable().getTagsForNotebook("");
	}

}
