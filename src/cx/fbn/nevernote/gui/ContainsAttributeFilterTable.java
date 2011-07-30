/*
 * This file is part of NixNote 
 * Copyright 2009,2010 Randy Baumgarte
 * Copyright 2010 Hiroshi Miura 
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

import cx.fbn.nevernote.filters.ContainsAttributeFilter;
import cx.fbn.nevernote.filters.ContainsAttributeFilterFactory;
import cx.fbn.nevernote.sql.NoteTable;

public class ContainsAttributeFilterTable {
	ArrayList<ContainsAttributeFilter> table;
	
    public ContainsAttributeFilterTable() {
        table = new ArrayList<ContainsAttributeFilter>();
        for (ContainsAttributeFilterFactory.Contains type: ContainsAttributeFilterFactory.Contains.values()) 
		table.add(ContainsAttributeFilterFactory.create(type));
	}
	
	public void reset() {
		for (int i=0; i<table.size(); i++) 
			table.get(i).set(false);
	}
	
	public void select(int i) {
		table.get(i).set(true);
	}
	
	public int size() {
		return table.size();
	}
	
	public boolean hasSelection() {
		for (int i=0; i<table.size(); i++) {
			if (table.get(i).isSet())
				return true;
		}
		return false;
	}
	
	public boolean check(NoteTable sqlTable, Note n) {
		for (int i=0; i<table.size(); i++) {
			if (table.get(i).isSet()) {
				n = sqlTable.getNote(n.getGuid(), true, true, false, false, false);
				if (!table.get(i).attributeCheck(n)) 
					return false;
			}
		}
		return true;
	}
	
	// Get the label of a particular attribute check
	public String getLabel(int i) {
		return table.get(i).getLabel();
	}
}
