/*
 * This file is part of NeverNote 
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

package cx.fbn.nevernote.filters;

import com.trolltech.qt.core.QCoreApplication;
import com.evernote.edam.type.Note;
import cx.fbn.nevernote.filters.ContainsAttributeFilter;

public class ContainsAttributeFilterFactory {
	private ContainsAttributeFilterFactory() {
	}
    private static final String textDomain="AttributeFilter";
	public static enum Contains {Images,Audio,Ink,EncryptedText,Todo,UnfinishedTodo,FinishedTodo,Attachment,PDF};

	public static ContainsAttributeFilter create(Contains fType){
        switch (fType) {
        case Images:
            return new ContainsAttributeFilterMime(QCoreApplication.translate(textDomain,"Images"),"image/");
        case Audio:
            return new ContainsAttributeFilterMime(QCoreApplication.translate(textDomain,"Audio"),"audio/");
    	case Ink:
            return new ContainsAttributeFilterMime(QCoreApplication.translate(textDomain,"Ink"),"application/vnd.evernote.ink");
        case EncryptedText:
            return new ContainsAttributeFilterContent(QCoreApplication.translate(textDomain,"Encrypted Text"), "<en-crypt");
        case Todo:
            return new ContainsAttributeFilterContent(QCoreApplication.translate(textDomain,"ToDo Items"),"<en-todo");
        case UnfinishedTodo:
            return new ContainsAttributeFilterTodo("Unfinished to-do items",false);
        case FinishedTodo:
            return new ContainsAttributeFilterTodo("Finished to-do items", true);
        case Attachment:
            return new ContainsAttributeFilterAttachment("Attachment");
        case PDF:
            return new ContainsAttributeFilterMime("PDF","application/pdf");
		}
		return null;
	}
}

class ContainsAttributeFilterMime extends ContainsAttributeFilter {
	private String _mime;
	public ContainsAttributeFilterMime(String n, String m) {
		super(n);
		_mime = m; 
	}
	public boolean attributeCheck(Note n) {
        for (int i=0; i<n.getResourcesSize(); i++) {
       	    if (n.getResources().get(i).getMime().startsWith(_mime))
           	    return true;
        }
       	return false;
	}
}

class ContainsAttributeFilterContent extends ContainsAttributeFilter {
	private String _text;
	public ContainsAttributeFilterContent(String n, String text) {
		super(n);
		_text = text;
	}
	public boolean attributeCheck(Note n) {
        if (n.getContent().indexOf(_text) > -1)
            return true;
        else
            return false;
    }
}

class ContainsAttributeFilterTodo extends ContainsAttributeFilter {
	private boolean _checked;
	public ContainsAttributeFilterTodo(String n, boolean checked) {
		super(n);
		_checked = checked;
	}
		public boolean attributeCheck(Note n) {
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


class ContainsAttributeFilterAttachment extends ContainsAttributeFilter {
		public ContainsAttributeFilterAttachment(String n) {
			super(n);
		}
		public boolean attributeCheck(Note n) {
                	for (int i=0; i<n.getResourcesSize(); i++) {
                     		if (n.getResources().get(i).getAttributes() != null 
					&& n.getResources().get(i).getAttributes().isAttachment())
                                	return true;
                	}
                	return false;
		}
	}

