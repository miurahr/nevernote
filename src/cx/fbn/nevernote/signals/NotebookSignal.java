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

package cx.fbn.nevernote.signals;

import java.util.List;

import com.evernote.edam.type.Notebook;
import com.trolltech.qt.QSignalEmitter;

import cx.fbn.nevernote.filters.NotebookCounter;


public class NotebookSignal extends QSignalEmitter {
	public Signal0 listChanged = new Signal0();
	public Signal2<List<Notebook>, List<NotebookCounter>> refreshNotebookTreeCounts = new Signal2<List<Notebook>, List<NotebookCounter>>();
	public Signal1<List<NotebookCounter>> countsChanged = new Signal1<List<NotebookCounter>>();
}
