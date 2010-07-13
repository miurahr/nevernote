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


public class DBRunnerRequest {
	public static int GENERIC 			= 1;
	public static int DATABASE			= 2;
	public static int DELETED_ITEM 		= 3;
	public static int NOTEBOOK 			= 4;
	public static int TAG 				= 5;
	public static int SAVED_SEARCH 		= 6;
	public static int NOTE				= 7;
	public static int RESOURCE			= 8;
	public static int NOTE_TAGS			= 9;
	public static int ENSEARCH			= 10;
	public static int WATCH_FOLDER		= 11;
	public static int WORD				= 12;
	public static int Invalid_XML		= 13;
	public static int Sync				= 14;
	
	public volatile int requestor_id;
	public volatile int category;
	public volatile int type;
	public volatile String request;
}
