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

package cx.fbn.nevernote.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import com.evernote.edam.type.Tag;
import com.trolltech.qt.core.QEvent;
import com.trolltech.qt.core.QModelIndex;
import com.trolltech.qt.gui.QLineEdit;

import cx.fbn.nevernote.Global;

public class TagLineEdit extends QLineEdit {
	private String text;
	private final boolean changed;
	public Signal2<List<String>, String> text_changed = new Signal2<List<String>, String>();
	public TagLineCompleter tagCompleter;
	public Signal0 focusLost = new Signal0();
	public String currentCompleterSelection;
	
	public TagLineEdit(List<Tag> allTags) {
		textChanged.connect(this, "textChanged(String)");
		tagCompleter = new TagLineCompleter(this);
		text_changed.connect(tagCompleter, "update(List, String)");
		tagCompleter.activated.connect(this, "completeText(String)");
		tagCompleter.highlightedIndex.connect(this, "completerSelected(QModelIndex)");
		changed = false;
		currentCompleterSelection = null;
	}
	

	public boolean hasChanged() {
		return changed;
	}
	
	@SuppressWarnings("unused")
	private void textChanged(String t) {
		text = t;
		int cursor_pos = cursorPosition();
		String temp_text = text.substring(0,cursor_pos);
		int delimeter = temp_text.trim().lastIndexOf(Global.tagDelimeter);
		String prefix = "";
		if (delimeter > 0)
		   prefix = temp_text.substring(delimeter+1).trim();
		else
			prefix = temp_text.trim();
		
		List<String> newTags = new ArrayList<String>();
		List<String> currentTags = new ArrayList<String>();
		String list[] = text.split(Global.tagDelimeter);
		for (String element : list) {
			currentTags.add(element.trim());
		}
	
		text_changed.emit(currentTags, prefix.trim());

		
	}
	
	// This method is used to store the current selection from the completer.  It is
	// saved until later in case focus is lost while one is selected, but Enter is never pressed.
	@SuppressWarnings("unused")
	private void completerSelected(QModelIndex model) {
		currentCompleterSelection = null;
		if (model == null)
			return;
		tagCompleter.setCurrentRow(model.row());
		currentCompleterSelection = tagCompleter.currentCompletion();
		tagCompleter.setCurrentRow(model.row());
	}
	
	public void  completeText(String text){
		int cursor_pos = cursorPosition();
		String before_text = text().substring(0,cursor_pos);
		String after_text = text().substring(cursor_pos) +" " +Global.tagDelimeter;
		int prefix_len = before_text.lastIndexOf(Global.tagDelimeter);
		if (prefix_len == -1) {
			prefix_len = cursor_pos;
			before_text = "";
		} else {
			before_text = before_text.substring(0, prefix_len);
		}

		setText(rebuildTags(before_text+Global.tagDelimeter+text+Global.tagDelimeter+after_text));
//		setText(before_text +text +Global.tagDelimeter +" " +after_text);
//		setCursorPosition(cursor_pos - prefix_len + text().length() +2);
		setCursorPosition(text().length());
	}

	public void setTagList(List<Tag> t) {
		tagCompleter.resetList();
		tagCompleter.setTagList(t);
	}
	
	@Override
	public boolean event(QEvent e) {
		if (e.type().equals(QEvent.Type.FocusOut)) {
			
			focusLost.emit();
		}
		return super.event(e);
	}
	
	private String rebuildTags(String tags) {
		StringBuffer tagBuffer = new StringBuffer(tags.length() *2);
		StringTokenizer tokens = new StringTokenizer (tags, Global.tagDelimeter);
		boolean first = true;
		while (tokens.hasMoreElements()) {
			String token = tokens.nextToken().trim();
			if (!token.equals("")) {
				if (!first)
					tagBuffer.append(Global.tagDelimeter+ " ");
				first = false;
				tagBuffer.append(token);
			}
		}
		return tagBuffer.toString();
	}
}
