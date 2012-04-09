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

import java.util.ArrayList;
import java.util.List;

import com.evernote.edam.type.Note;
import com.evernote.edam.type.Notebook;

import cx.fbn.nevernote.sql.DatabaseConnection;
import cx.fbn.nevernote.utilities.ApplicationLogger;

public class FilterEditorNotebooks {
	DatabaseConnection conn;
	ApplicationLogger logger;
	
	public FilterEditorNotebooks(DatabaseConnection conn, ApplicationLogger logger) {
		this.logger = logger;
		this.conn = conn;
	}
	
	
	public List<Notebook> getValidNotebooks(Note note, List<Notebook> notebooks) {
		
		List<Notebook> books = new ArrayList<Notebook>();
		for (int i=0; i<notebooks.size(); i++) 
			books.add(notebooks.get(i));
		
		// If this is a new note, it isn't tied to any particular notebook.h
		if (note == null || note.getUpdateSequenceNum() == 0) {
			for (int i=books.size()-1; i>=0; i--) {
				if (conn.getNotebookTable().isReadOnly(books.get(i).getGuid()))
					books.remove(i);
			}
			return books;
		}
		
		boolean linked = conn.getNotebookTable().isLinked(note.getNotebookGuid());
		
		// If the notebook is linked, then we really only need to return the one notebookd
		if (linked) {
			List<Notebook> newList = new ArrayList<Notebook>();
			for (int i=0; i<books.size(); i++) {
				if (books.get(i).getGuid().equals(note.getNotebookGuid())) {
					newList.add(books.get(i));
					return newList;
				}
			}
		} 
		// If the note's notebook isn't linked, then we need to return any non-linked notebook
		List<Notebook> newList = new ArrayList<Notebook>();
		for (int i=0; i<books.size(); i++) {
			if (!conn.getNotebookTable().isLinked(books.get(i).getGuid())) {
				newList.add(books.get(i));
			}
		}
		return newList;
	}

}
