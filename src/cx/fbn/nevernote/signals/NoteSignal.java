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

import com.evernote.edam.type.Note;
import com.trolltech.qt.QSignalEmitter;
import com.trolltech.qt.core.QByteArray;
import com.trolltech.qt.core.QDateTime;

import cx.fbn.nevernote.gui.BrowserWindow;


public class NoteSignal extends QSignalEmitter {
	public Signal2<String, List<String>> 		tagsChanged = new Signal2<String, List<String>>(); 
	public Signal2<String, String>		tagsAdded = new Signal2<String, String>();
	public Signal2<String, String> 		titleChanged = new Signal2<String, String>();
	public Signal2<String, String> 		noteChanged = new Signal2<String, String>();
	public Signal2<String, String> 		notebookChanged = new Signal2<String,String>();
	public Signal2<String, QDateTime>	createdDateChanged = new Signal2<String, QDateTime>();
	public Signal2<String, QDateTime>	alteredDateChanged = new Signal2<String, QDateTime>();
	public Signal2<String, QDateTime>	subjectDateChanged = new Signal2<String, QDateTime>();
	public Signal2<String, String>		authorChanged = new Signal2<String, String>();
	public Signal2<String, String>		sourceUrlChanged = new Signal2<String, String>();
	public Signal4<String, Double, Double,Double> geoChanged = new Signal4<String,Double,Double,Double>();
	public Signal0						quotaChanged = new Signal0();
	public Signal1<String>				noteIndexed = new Signal1<String>();
	public Signal1<Note>				noteAdded = new Signal1<Note>();
	public Signal2<String, String>		guidChanged = new Signal2<String, String>();
	public Signal1<Integer>				titleColorChanged = new Signal1<Integer>();
	public Signal2<Note, Boolean>		noteDownloaded = new Signal2<Note, Boolean>();
	public Signal2<String, String>		noteSaveRunnerError = new Signal2<String, String>();
	public Signal3<String,QByteArray,Integer>		thumbnailPageReady = new Signal3<String,QByteArray,Integer>();
	public Signal3<BrowserWindow,String,String>	browserLinkClicked = new Signal3<BrowserWindow,String,String>();
	public Signal0				notePinned = new Signal0();
}


