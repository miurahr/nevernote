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

import java.text.SimpleDateFormat;

import com.evernote.edam.type.Note;
import com.trolltech.qt.core.QDateTime;

public abstract class DateAttributeFilter extends AttributeFilter {
    protected boolean checkSince;
    private final boolean checkCreated;

    public DateAttributeFilter (boolean since, boolean created) {
	super();
	checkSince=since;
	checkCreated=created;
    }

    @Override
	public abstract boolean attributeCheck(Note n);

    protected QDateTime noteTime(Note n) {
        String dateTimeFormat = new String("MM/dd/yyyy HH:mm:ss");
        SimpleDateFormat simple = new SimpleDateFormat(dateTimeFormat);

        if (checkCreated) {
            StringBuilder creationDate = new StringBuilder(simple.format(n.getCreated()));
            return QDateTime.fromString(creationDate.toString(), "MM/dd/yyyy HH:mm:ss");
        } else {
            StringBuilder updatedDate = new StringBuilder(simple.format(n.getUpdated()));
            return QDateTime.fromString(updatedDate.toString(), "MM/dd/yyyy HH:mm:ss");
        }
    }

    protected QDateTime currentTime() {
        return QDateTime.currentDateTime();
    }
}
