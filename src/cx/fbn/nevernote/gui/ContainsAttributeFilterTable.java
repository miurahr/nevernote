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
import cx.fbn.nevernote.sql.NoteTable;

public class ContainsAttributeFilterTable {
	ArrayList<AttributeFilter> table;
	
	public ContainsAttributeFilterTable() {
		table = new ArrayList<AttributeFilter>();
		table.add(new AttributeFilter("Images"));
		table.add(new AttributeFilter("Audio"));
		table.add(new AttributeFilter("Ink"));
		table.add(new AttributeFilter("Encrypted Text"));
		table.add(new AttributeFilter("To-Do Items"));
		table.add(new AttributeFilter("Unfinished to-do items"));
		table.add(new AttributeFilter("Finished to-do items"));
		table.add(new AttributeFilter("Attachment"));
		table.add(new AttributeFilter("PDF"));




		
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
	
	public int size() {
		return table.size();
	}
	
	public boolean check(NoteTable sqlTable, Note n) {
		boolean result = true;
		
		for (int i=0; i<table.size(); i++) {
			if (table.get(i).isSet()) {
				n = sqlTable.getNote(n.getGuid(), true, true, false, false, false);
				if (table.get(i).getName().equalsIgnoreCase("images"))
					result = checkMime(n, "image/");
				if (table.get(i).getName().equalsIgnoreCase("audio"))
					result = checkMime(n, "audio/");
				if (table.get(i).getName().equalsIgnoreCase("ink"))
					result = checkMime(n, "application/vnd.evernote.ink");
				if (table.get(i).getName().equalsIgnoreCase("Attachment"))
					result = checkAttachment(n);
				if (table.get(i).getName().equalsIgnoreCase("pdf"))
					result = checkMime(n, "application/pdf");
				if (table.get(i).getName().equalsIgnoreCase("Encrypted Text"))
					result = checkText(n.getContent(), "<en-crypt");
				if (table.get(i).getName().equalsIgnoreCase("To-Do Items"))
					result = checkText(n.getContent(), "<en-todo");
				if (table.get(i).getName().equalsIgnoreCase("Unfinished to-do items"))
					result = checkTodo(n.getContent(), false);
				if (table.get(i).getName().equalsIgnoreCase("Finished to-do items"))
					result = checkTodo(n.getContent(), true);


			}
		}
		return result;
	}
	
	private boolean checkMime(Note n, String mime) {
		for (int i=0; i<n.getResourcesSize(); i++) {
			if (n.getResources().get(i).getMime().startsWith(mime))
				return true;
		}
		return false;
	}

	private boolean checkAttachment(Note n) {
		for (int i=0; i<n.getResourcesSize(); i++) {
			if (n.getResources().get(i).getAttributes() != null && n.getResources().get(i).getAttributes().isAttachment())
				return true;
		}
		return false;
	}
	
	private boolean checkTodo(String content, boolean checked) {
		int pos = content.indexOf("<en-todo");
		
		for (; pos >=0 ; pos=content.indexOf("<en-todo", pos+1)) {
			int endPos = content.indexOf("/>", pos);
			String segment = content.substring(pos, endPos);
			boolean currentState = false;
			if (segment.indexOf("checked=\"true\"") > -1)
					currentState = true;
			if (currentState == checked)
				return true;
		}
		
			
		return false;
		
	}
	
	private boolean checkText(String content, String text) {
		if (content.indexOf(text) > -1)
			return true;
		else
			return false;
	}
	// Get the name of a particular attribute check
	public String getName(int i) {
		return table.get(i).getName();
	}
}
