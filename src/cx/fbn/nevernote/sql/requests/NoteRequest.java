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

package cx.fbn.nevernote.sql.requests;

import java.util.ArrayList;
import java.util.List;

import com.evernote.edam.type.Note;
import com.trolltech.qt.core.QByteArray;
import com.trolltech.qt.core.QDateTime;

import cx.fbn.nevernote.utilities.Pair;

public class NoteRequest extends DBRunnerRequest {
	public static int Create_Table     					= 1;
	public static int Drop_Table 						= 2;
	public static int Add_Note							= 3;
	public static int Get_Note							= 4;
	public static int Update_Note_Title					= 5;
	public static int Update_Note_Creation_Date 		= 6;
	public static int Update_Note_Altered_Date			= 7;
	public static int Update_Note_Subject_Date			= 8;
	public static int Update_Note_Source_Url			= 9;
	public static int Update_Note_Author				= 10;
	public static int Update_Note_Notebook				= 11;
	public static int Update_Note_Content				= 12;
	public static int Delete_Note						= 13;
	public static int Restore_Note						= 14;
	public static int Expunge_Note						= 15;
	public static int Expunge_All_Deleted_Notes			= 16;
	public static int Update_Note_Sequence				= 17;
	public static int Update_Note_Guid					= 18;
	public static int Update_Note						= 19;
	public static int Exists							= 20;
	public static int Sync_Note							= 21;
	public static int Get_Dirty							= 22;
	public static int Get_Unsynchronized_Guids			= 23;
	public static int Reset_Dirty_Flag					= 24;
	public static int Get_All_Guids						= 25;
	public static int Get_All_Notes						= 26;
	public static int Get_Unindexed_Count				= 27;
	public static int Get_Dirty_Count					= 28;
	public static int Update_Resource_Guid_By_Hash 		= 29;
	public static int Set_Index_Needed					= 30;
	public static int Reindex_All_Notes					= 31;
	public static int Get_Unindexed						= 32;
	public static int Get_Next_Unindexed				= 33;
	public static int Update_Resource_Content_Hash		= 34;
	public static int Get_Note_Count					= 35;
	public static int Reset_Note_Sequence				= 36;
	public static int Get_Deleted_Count					= 37;
	public static int Is_Note_Dirty						= 38;
	public static int Get_Note_Content_Binary			= 39;
	public static int Get_Title_Colors					= 40;
	public static int Set_Title_Colors					= 41;
	public static int Set_Thumbnail						= 42;
	public static int Get_Thumbnail						= 43;
	public static int Is_Thumbail_Needed				= 44;
	public static int Set_Thumbnail_Needed				= 45;

	public volatile String					string1;
	public volatile String					string2;
	public volatile String					string3;
	public volatile QDateTime 				date;
	public volatile int						int1;
	public volatile boolean					bool1;
	public volatile boolean					bool2;
	public volatile boolean					bool3;
	public volatile boolean					bool4;
	public volatile boolean					bool5;
	public volatile Note					note;
	public volatile List<Note>		 		notes;
	public volatile QByteArray				bytes;
	
	public volatile List<Note>				responseNotes;
	public volatile Note					responseNote;
	public volatile String 					responseString;
	public volatile boolean					responseBoolean;
	public volatile List<String>			responseStrings;
	public volatile int						responseInt;
	public volatile List<Pair<String,Integer>> responsePair;
	public volatile QByteArray				responseBytes;
	
	public NoteRequest() {
		category = NOTE;
	}
	
	public NoteRequest copy() {
		NoteRequest request = new NoteRequest();
		
		request.requestor_id = requestor_id;
		request.type = type;
		request.category = category;
		request.bool1 = bool1;
		request.bool2 = bool2;
		request.bool3 = bool3;
		request.bool4 = bool4;
		request.bool5 = bool5;
		request.int1 = int1;
		
		if (date != null)
			request.date = new QDateTime(date);

		if (string1 != null)
			request.string1 = new String(string1);
		if (string2 != null)
			request.string2 = new String(string2);
		if (string3 != null)
			request.string3 = new String(string3);
		
		if (note != null)
			request.note = note.deepCopy();

		if (notes != null) {
			request.notes = new ArrayList<Note>();
			for (int i=0; i<notes.size(); i++)
				request.notes.add(notes.get(i).deepCopy());
		}
		
		request.responseInt = responseInt;
		request.responseBoolean = responseBoolean;
		if (bytes != null)
			request.bytes = new QByteArray(bytes);
		
		if (responseNotes != null) {
			request.responseNotes = new ArrayList<Note>();
			for (int i=0; i<responseNotes.size(); i++)
				if (responseNotes.get(i) != null)	
					request.responseNotes.add(responseNotes.get(i).deepCopy());
		}

		if (responseNote != null) {
			request.responseNote = responseNote.deepCopy();
		}
		
		if (responseString != null) {
			request.responseString = new String(responseString);
		}
		
		if (responseStrings != null) {
			request.responseStrings = new ArrayList<String>();
			for (int i=0; i<responseStrings.size(); i++) 
				request.responseStrings.add(new String(responseStrings.get(i)));
		}
		
		if (responsePair != null) {
			request.responsePair = new ArrayList<Pair<String,Integer>>();
			for (int i=0; i<responsePair.size(); i++) {
				Pair<String,Integer> p = new Pair<String,Integer>();
				p.setFirst(responsePair.get(i).getFirst());
				p.setSecond(responsePair.get(i).getSecond());
				request.responsePair.add(p);
			}
		}
		if (responseBytes != null)
			request.responseBytes = new QByteArray(responseBytes);

		return request;
	}
	
}