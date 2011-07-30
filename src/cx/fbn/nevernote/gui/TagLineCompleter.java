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

import com.evernote.edam.type.Tag;
import com.trolltech.qt.core.Qt.CaseSensitivity;
import com.trolltech.qt.gui.QCompleter;
import com.trolltech.qt.gui.QStringListModel;

public class TagLineCompleter extends QCompleter {
	private List<Tag> 			tags;
	private List<String> 		currentTags;
	private QStringListModel 	model;	
	private final TagLineEdit			editor;
	private String				currentText;
	
	public TagLineCompleter(TagLineEdit e) {
		editor = e;
		setWidget(editor);
//		editor.setCompleter(this);
		currentTags = new ArrayList<String>();
		setTagList(new ArrayList<Tag>());
		setCaseSensitivity(CaseSensitivity.CaseInsensitive);
	}
	
	public List<String> getTagList(){
		return currentTags;
	}
	
	public void update(List<String> t, String prefix) {
		currentTags = t;
 
		buildModelList();
				
		setCompletionPrefix(prefix);
		if (!prefix.trim().equals(""))
			complete();
	}
	
	public List<Tag> getTags() {
		return tags;
	}
	
	private List<String> buildModelList() {
		
		model = (QStringListModel) model();
		if (model == null) {
			model = new QStringListModel();
			setModel(model);
		}
		for (int i=0; i<model.rowCount(); i++)
			model.removeRow(i);
		
		List<String> newTagList = new ArrayList<String>();

		for (int i=0; i<tags.size(); i++) {
			boolean found=false;
			for (int j=0; j<currentTags.size() && !found; j++) {
				if (currentTags.get(j).trim().equalsIgnoreCase(tags.get(i).getName())) {
					found = true;
				}
			}
			if (!found) 
				newTagList.add(tags.get(i).getName());
		}
		
		model.setStringList(newTagList);
		return newTagList;
	}
	
	public void setTagList(List<Tag> t) {
		tags = t;
		resetList();
		buildModelList();
//		model = new QStringListModel(buildModelList(), this);
//		setModel(model);
	}
	
	public void resetList() {
		currentTags.clear();
	}
	

	public String currentText() {
		return currentText;
	}
	
	public void reset() {
		currentText = "";
	}

}
