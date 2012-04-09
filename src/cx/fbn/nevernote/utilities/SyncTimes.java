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
package cx.fbn.nevernote.utilities;

import java.util.ArrayList;
import java.util.List;

public class SyncTimes {
	private final List<String> stringTimes;
	private final List<Integer> minuteTimes;
	
	public SyncTimes() {
		stringTimes = new ArrayList<String>();
		minuteTimes = new ArrayList<Integer>();
		
		stringTimes.add("manually");
		minuteTimes.add(0);
		
		stringTimes.add("15 minutes");
		minuteTimes.add(15);
		
		stringTimes.add("30 minutes");
		minuteTimes.add(30);
		
		stringTimes.add("45 minutes");
		minuteTimes.add(45);
		
		stringTimes.add("60 minutes");
		minuteTimes.add(60);
		
		stringTimes.add("120 minutes");
		minuteTimes.add(120);
		
		stringTimes.add("240 minutes");
		minuteTimes.add(240);
		
		stringTimes.add("600 minutes");
		minuteTimes.add(600);
		
		stringTimes.add("Shortly after the end of the world");
		minuteTimes.add(-1);
	}
	
	public List<String> stringValues() {
		return stringTimes;
	}
	
	public int timeValue(String t) {
		for (int i=0; i<stringTimes.size(); i++) 
			if (stringTimes.get(i).equalsIgnoreCase(t))
				return minuteTimes.get(i);
		
		return 15;
	}
}
