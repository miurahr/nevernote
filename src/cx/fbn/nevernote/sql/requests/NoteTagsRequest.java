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

import cx.fbn.nevernote.sql.runners.NoteTagsRecord;
import cx.fbn.nevernote.utilities.Pair;

public class NoteTagsRequest extends DBRunnerRequest {
	public static int Create_Table     			= 1;
	public static int Drop_Table 				= 2;
	public static int Get_Note_Tags				= 3;
	public static int Get_All_Note_Tags			= 4;
	public static int Check_Note_Note_Tags		= 5;
	public static int Save_Note_Tag				= 6;
	public static int Delete_Note_Tag			= 7;
	public static int Tag_Counts				= 8;


	public volatile String					string1;
	public volatile String					string2;
	
	public volatile List<String>			responseStrings;
	public volatile List<NoteTagsRecord> 	responseNoteTagsRecord;
	public volatile boolean					responseBoolean;
	public volatile List<Pair<String,Integer>>		responseCounts;
	
	public NoteTagsRequest() {
		category = NOTE_TAGS;
	}
	
	public NoteTagsRequest copy() {
		NoteTagsRequest request = new NoteTagsRequest();
		
		request.requestor_id = requestor_id;
		request.type = type;
		request.category = category;
		if (string1 != null)
			request.string1 = new String(string1);
		if (string2 != null)
			request.string2 = new String(string2);
		
		if (responseStrings != null) {
			request.responseStrings = new ArrayList<String>();
			for (int i=0; i<responseStrings.size(); i++) {
				request.responseStrings.add(new String(responseStrings.get(i)));
			}
		}
		
		if (responseNoteTagsRecord != null) {
			request.responseNoteTagsRecord = new ArrayList<NoteTagsRecord>();
			for (int i=0; i<responseNoteTagsRecord.size(); i++) {
				NoteTagsRecord record = new NoteTagsRecord();
				record.noteGuid = new String(responseNoteTagsRecord.get(i).noteGuid);
				record.tagGuid = new String(responseNoteTagsRecord.get(i).tagGuid);
				request.responseNoteTagsRecord.add(record);
			}
		}
		
		if (responseCounts != null) {
			request.responseCounts = new ArrayList<Pair<String,Integer>>();
			for (int i=0; i<responseCounts.size(); i++) {
				Pair<String,Integer> newPair = new Pair<String,Integer>();
				newPair.setFirst(responseCounts.get(i).getFirst());
				newPair.setSecond(responseCounts.get(i).getSecond());
				request.responseCounts.add(newPair);
			}
		}
		
		return request;
	}
	
}