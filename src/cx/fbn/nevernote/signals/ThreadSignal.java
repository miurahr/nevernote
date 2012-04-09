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

import com.trolltech.qt.QSignalEmitter;

public class ThreadSignal extends QSignalEmitter {
	public Signal1<String> 	filterChanged = new Signal1<String>();
	public Signal0			exit = new Signal0();
	public Signal2<String, String>  deleteNoteFromWordIndex = new Signal2<String, String>();
	public Signal4<String, String, String, Integer> addNoteToWordIndex = new Signal4<String, String, String, Integer>();
	public Signal3<String, String, Boolean>  indexNeeded = new Signal3<String, String, Boolean>();
}
