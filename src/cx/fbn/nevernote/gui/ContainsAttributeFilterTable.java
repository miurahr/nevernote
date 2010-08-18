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

package cx.fbn.nevernote.gui;

import java.util.ArrayList;

import com.evernote.edam.type.Note;

import cx.fbn.nevernote.filters.AttributeFilter;
import cx.fbn.nevernote.filters.ContainsAttributeFilter;
import cx.fbn.nevernote.sql.NoteTable;

public class ContainsAttributeFilterTable {
	ArrayList<ContainsAttributeFilter> table;
	
	public ContainsAttributeFilterTable() {
		table = new ArrayList<ContainsAttributeFilter>();
		table.add(new ContainsAttributeFilter.Mime("Image", "image/"));
		table.add(new ContainsAttributeFilter.Mime("Audio", "audio/"));
		table.add(new ContainsAttributeFilter.Mime("Ink", "application/vnd.evernote.ink"));
		table.add(new ContainsAttributeFilter.Content("Encrypted Text", "<en-crypt"));
		table.add(new ContainsAttributeFilter.Content("To-Do Items", "<en-todo"));
		table.add(new ContainsAttributeFilter.Todo("Unfinished to-do items", false));
		table.add(new ContainsAttributeFilter.Todo("Finished to-do items", true));
		table.add(new ContainsAttributeFilter.Attachment("Attachment"));
		table.add(new ContainsAttributeFilter.Mime("PDF","application/pdf"));
		
	}
	
	public void reset() {
		for (int i=0; i<table.size(); i++) 
			table.get(i).set(false);
	}
	
	public void select(String name) {
		for (int i=0; i<table.size(); i++) 
			if (table.get(i).getName().equalsIgnoreCase(name))
				table.get(i).set(true);
	}
	public void select(int i) {
		table.get(i).set(true);
	}
	
	public int size() {
		return table.size();
	}
	
	public boolean check(NoteTable sqlTable, Note n) {
		for (int i=0; i<table.size(); i++) {
			if (table.get(i).isSet()) {
				n = sqlTable.getNote(n.getGuid(), true, true, false, false, false);
				if (!table.get(i).checkContent(n)) 
					return false;
			}
		}
		return true;
	}
	
	// Get the name of a particular attribute check
	public String getName(int i) {
		return table.get(i).getName();
	}
}
