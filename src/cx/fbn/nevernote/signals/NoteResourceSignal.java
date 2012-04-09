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


public class NoteResourceSignal extends QSignalEmitter {
	public Signal1<String>				resourceIndexed = new Signal1<String>();
	public Signal2<String, Boolean>		resourceIndexNeeded = new Signal2<String, Boolean>();
	public Signal3<String,String,String>  resourceGuidChanged = new Signal3<String,String,String>();
	public Signal1<String>				contentChanged = new Signal1<String>();
}
