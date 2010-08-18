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

package cx.fbn.nevernote.gui;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

import com.evernote.edam.type.Note;
import com.trolltech.qt.core.QDateTime;

import cx.fbn.nevernote.filters.AttributeFilter;
import cx.fbn.nevernote.filters.DateAttributeFilter;

public class DateAttributeFilterTable {
	ArrayList<DateAttributeFilter> table;
	private boolean checkCreated;

	public DateAttributeFilterTable(boolean since, boolean created) {
		checkCreated = created;
		table = new ArrayList<DateAttributeFilter>();
		table.add(new DateAttributeFilter.checkToday(since));
		table.add(new DateAttributeFilter.checkYesterday(since));
		table.add(new DateAttributeFilter.checkThisWeek(since));
		table.add(new DateAttributeFilter.checkLastWeek(since));
		table.add(new DateAttributeFilter.checkMonth(since));
		table.add(new DateAttributeFilter.checkLastMonth( since));
		table.add(new DateAttributeFilter.checkYear(since));
		table.add(new DateAttributeFilter.checkLastYear(since));
	}
	
	public void reset() {
		for (int i=0; i<table.size(); i++) 
			table.get(i).set(false);
	}
	
	public void select(String name) {
		for (int i=0; i<table.size(); i++) 
			if (table.get(i).getName().equals(name))
				table.get(i).set(true);
	}
	public void select(int i) {
		table.get(i).set(true);
	}
	
	public int size() { 
		return table.size();
	}
	
	public boolean check(Note n) {
		QDateTime noteDate;
		String dateTimeFormat = new String("MM/dd/yyyy HH:mm:ss");
		SimpleDateFormat simple = new SimpleDateFormat(dateTimeFormat);
		if (checkCreated) {
			StringBuilder creationDate = new StringBuilder(simple.format(n.getCreated()));
			noteDate = QDateTime.fromString(creationDate.toString(), "MM/dd/yyyy HH:mm:ss");
		} else {
			StringBuilder updatedDate = new StringBuilder(simple.format(n.getUpdated()));
			noteDate = QDateTime.fromString(updatedDate.toString(), "MM/dd/yyyy HH:mm:ss");
		}
		
		QDateTime current = new QDateTime();
		current = QDateTime.currentDateTime();
		
		for (int i=0; i<table.size(); i++) {
			if (table.get(i).isSet()
			   && !table.get(i).attributeCheck(noteDate, current))
				return false;
		}
		return true;
	}
	
	// Get the name of a particular attribute check
	public String getLabel(int i) {
		return table.get(i).getLabel();
	}
	public String getName(int i) {
		return table.get(i).getName();
	}
}
