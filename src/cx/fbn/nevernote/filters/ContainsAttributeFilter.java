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

import com.evernote.edam.type.Note;

import cx.fbn.nevernote.filters.AttributeFilter;


public abstract class ContainsAttributeFilter extends AttributeFilter {
	public ContainsAttributeFilter(String n) {
		super(n);
	}
	public abstract boolean checkContent(Note n);

	public static class Mime extends ContainsAttributeFilter {
		private String _mime;
		public Mime(String n, String m) {
			super(n);
			_mime = m; 
		}
		public boolean checkContent(Note n) {
                	for (int i=0; i<n.getResourcesSize(); i++) {
                        	if (n.getResources().get(i).getMime().startsWith(_mime))
                                	return true;
                	}
                	return false;
		}
	}
	public static class Attachment extends ContainsAttributeFilter {
		public Attachment(String n) {
			super(n);
		}
		public boolean checkContent(Note n) {
                	for (int i=0; i<n.getResourcesSize(); i++) {
                     		if (n.getResources().get(i).getAttributes() != null 
					&& n.getResources().get(i).getAttributes().isAttachment())
                                	return true;
                	}
                	return false;
		}
	}
	public static class Todo extends ContainsAttributeFilter {
		private boolean _checked;
		public Todo(String n, boolean checked) {
			super(n);
			_checked = checked;
		}
		public boolean checkContent(Note n) {
			String content = n.getContent();
                	int pos = content.indexOf("<en-todo");
                	for (; pos >=0 ; pos=content.indexOf("<en-todo", pos+1)) {
                       		int endPos = content.indexOf("/>", pos);
                       		String segment = content.substring(pos, endPos);
                       		boolean currentState = false;
                       		if (segment.indexOf("checked=\"true\"") > -1)
                                        currentState = true;
                       		if (currentState == _checked)
                                	return true;
                	}
                	return false;
		}
	}
	public static class Content extends ContainsAttributeFilter {
		private String _text;
		public Content(String n, String text) {
			super(n);
			_text = text;
		}
		public boolean checkContent(Note n) {
                	if (n.getContent().indexOf(_text) > -1)
                       	 return true;
                	else
                       	 return false;
		}
	}
}
