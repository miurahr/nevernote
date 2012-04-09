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

package cx.fbn.nevernote.filters;

import com.evernote.edam.type.Note;
import com.trolltech.qt.core.QCoreApplication;

public class ContainsAttributeFilterFactory {
	private ContainsAttributeFilterFactory() {
	}
	public static enum Contains {Images,Audio,Ink,EncryptedText,Todo,UnfinishedTodo,FinishedTodo,Attachment,PDF};

	public static ContainsAttributeFilter create(Contains fType){
        switch (fType) {
        case Images:
            return new ContainsAttributeFilterMime(QCoreApplication.translate("cx.fbn.nevernote.filters.ContainsAttributeFilter",
                                                   "Images"),"image/");
        case Audio:
            return new ContainsAttributeFilterMime(QCoreApplication.translate("cx.fbn.nevernote.filters.ContainsAttributeFilter",
                                                   "Audio"),"audio/");
    	case Ink:
            return new ContainsAttributeFilterMime(QCoreApplication.translate("cx.fbn.nevernote.filters.ContainsAttributeFilter",
                                                   "Ink"),"application/vnd.evernote.ink");
        case EncryptedText:
            return new ContainsAttributeFilterContent(QCoreApplication.translate("cx.fbn.nevernote.filters.ContainsAttributeFilter",
                                                   "Encrypted Text"), "<en-crypt");
        case Todo:
            return new ContainsAttributeFilterContent(QCoreApplication.translate("cx.fbn.nevernote.filters.ContainsAttributeFilter",
                                                   "ToDo Items"),"<en-todo");
        case UnfinishedTodo:
            return new ContainsAttributeFilterTodo(QCoreApplication.translate("cx.fbn.nevernote.filters.ContainsAttributeFilter",
                                                   "Unfinished to-do items"),false);
        case FinishedTodo:
            return new ContainsAttributeFilterTodo(QCoreApplication.translate("cx.fbn.nevernote.filters.ContainsAttributeFilter",
                                                   "Finished to-do items"), true);
        case Attachment:
            return new ContainsAttributeFilterAttachment(QCoreApplication.translate("cx.fbn.nevernote.filters.ContainsAttributeFilter",
                                                   "Attachment"));
        case PDF:
            return new ContainsAttributeFilterMime(QCoreApplication.translate("cx.fbn.nevernote.filters.ContainsAttributeFilter",
                                                   "PDF"),"application/pdf");
		}
        throw new IllegalArgumentException("The filter type " + fType + " is not recognized.");

	}
}

// Contains filter strategies
class ContainsAttributeFilterMime extends ContainsAttributeFilter {
	private final String _mime;
	public ContainsAttributeFilterMime(String n, String m) {
		super(n);
		_mime = m; 
	}
	@Override
	public boolean attributeCheck(Note n) {
        for (int i=0; i<n.getResourcesSize(); i++) {
       	    if (n.getResources().get(i).getMime().startsWith(_mime))
           	    return true;
        }
       	return false;
	}
}

class ContainsAttributeFilterContent extends ContainsAttributeFilter {
	private final String _text;
	public ContainsAttributeFilterContent(String n, String text) {
		super(n);
		_text = text;
	}
	@Override
	public boolean attributeCheck(Note n) {
        if (n.getContent().indexOf(_text) > -1)
            return true;
        else
            return false;
    }
}

class ContainsAttributeFilterTodo extends ContainsAttributeFilter {
	private final boolean _checked;
	public ContainsAttributeFilterTodo(String n, boolean checked) {
		super(n);
		_checked = checked;
	}
		@Override
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
		@Override
		public boolean attributeCheck(Note n) {
                	for (int i=0; i<n.getResourcesSize(); i++) {
                     		if (n.getResources().get(i).getAttributes() != null 
					&& n.getResources().get(i).getAttributes().isAttachment())
                                	return true;
                	}
                	return false;
		}
	}

