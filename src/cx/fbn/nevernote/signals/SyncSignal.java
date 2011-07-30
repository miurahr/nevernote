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

import com.evernote.edam.notestore.NoteStore;
import com.evernote.edam.type.User;
import com.evernote.edam.userstore.UserStore;
import com.trolltech.qt.QSignalEmitter;


public class SyncSignal extends QSignalEmitter {
	public Signal1<Boolean> finished = new Signal1<Boolean>();
	public Signal0	errorDisconnect = new Signal0();
//	public Signal1<Long> setSequenceDate = new Signal1<Long>();
	public Signal1<Long> saveUploadAmount = new Signal1<Long>();
//	public Signal1<Integer> setUpdateSequenceNumber = new Signal1<Integer>();
	public Signal1<Integer> saveEvernoteUpdateCount = new Signal1<Integer>();
	public Signal1<User> saveUserInformation = new Signal1<User>();
	public Signal1<String> saveAuthToken = new Signal1<String>();
	public Signal1<UserStore.Client>  saveUserStore = new Signal1<UserStore.Client>();
	public Signal1<NoteStore.Client>  saveNoteStore = new Signal1<NoteStore.Client>();
	public Signal1<Boolean> authRefreshComplete = new Signal1<Boolean>();
	
	public Signal0 refreshNotebooks = new Signal0();
	public Signal0 refreshTags = new Signal0();
	public Signal0 refreshSavedSearches = new Signal0();
	public Signal0 refreshNotes = new Signal0();
	public Signal0 refreshLists = new Signal0();
}
