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
	boolean checkSince;
	boolean checkCreated;


	public void setSince() {
		checkSince = true;
	}
	
	public void setBefore() {
		checkSince = false;
	}
	
	public void setCreated() {
		checkCreated = true;
	}
	
	public void setUpdated() {
		checkCreated = false;
	}

	public DateAttributeFilterTable() {
		checkSince = true;
		checkCreated = true;
		table = new ArrayList<DateAttributeFilter>();
		table.add(new checkToday());
		table.add(new checkYesterday());
		table.add(new checkThisWeek());
		table.add(new checkLastWeek());
		table.add(new checkMonth());
		table.add(new checkLastMonth());
		table.add(new checkYear());
		table.add(new checkLastYear());
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
	
	public int size() { 
		return table.size();
	}
	
	public boolean check(Note n) {
		QDateTime noteDate;
		String dateTimeFormat = new String("MM/dd/yyyy HH:mm:ss");
		SimpleDateFormat simple = new SimpleDateFormat(dateTimeFormat);
		StringBuilder creationDate = new StringBuilder(simple.format(n.getCreated()));
		StringBuilder updatedDate = new StringBuilder(simple.format(n.getUpdated()));
		if (checkCreated)
			noteDate = QDateTime.fromString(creationDate.toString(), "MM/dd/yyyy HH:mm:ss");
		else 
			noteDate = QDateTime.fromString(updatedDate.toString(), "MM/dd/yyyy HH:mm:ss");
		
		QDateTime current = new QDateTime();
		current = QDateTime.currentDateTime();
		
		boolean result = true;
		
		for (int i=0; i<table.size(); i++) {
			if (table.get(i).isSet()
			   && !table.get(i).attributeCheck(noteDate, current))
				result = false;
		}
		return result;
	}

	@SuppressWarnings("unused")
	private String tr(String s) { // this is fake func for I18N utility
		return s;
	}
	private class checkToday extends DateAttributeFilter {
		public checkToday() {
	  	     super(tr("Today")); // widget label
		}
		// Check if it was within the last day
		public boolean attributeCheck(QDateTime noteDate, QDateTime current) {
			if (checkSince)
				return noteDate.daysTo(current) == 0;
			else 
				return noteDate.daysTo(current) > 0;
		}
	}
	private class checkYesterday extends DateAttributeFilter {
		public checkYesterday() {
	  	     super(tr("Yesterday"));
		}
		// Check if it was within the last two days
		public boolean attributeCheck(QDateTime noteDate, QDateTime current) {
		if (checkSince) 
			return noteDate.daysTo(current) <= 1;
		else
			return noteDate.daysTo(current) > 1;
		}
	}
	private class checkThisWeek extends DateAttributeFilter {
		public checkThisWeek() {
	  	     super(tr("This Week"));
		}
		// Check if it was within the last two days
		public boolean attributeCheck(QDateTime noteDate, QDateTime current) {
			if (checkSince) 
				return noteDate.daysTo(current) <= 7;
			else
				return noteDate.daysTo(current) > 7;
		}
	}
	private class checkLastWeek extends DateAttributeFilter {
		public checkLastWeek() {
                	super(tr("Last Week"));
                }
		// Check if it was within the last two weeks
		public boolean attributeCheck(QDateTime noteDate, QDateTime current) {
			if (checkSince) 
				return noteDate.daysTo(current) <= 14;
			else
				return noteDate.daysTo(current) > 14;
		}
	}
	private class checkMonth extends DateAttributeFilter {
		public checkMonth() {
                	super(tr("This Month"));
                }
		// Check if it was within the last month
		public boolean attributeCheck(QDateTime noteDate, QDateTime current) {
			if (checkSince)
				return noteDate.date().month() - current.date().month() == 0;
			else
				return noteDate.date().month() - current.date().month() != 0;
		}
	}
	private class checkLastMonth extends DateAttributeFilter {
		public checkLastMonth() {
			super(tr("Last Month"));
		}
		// Check if it was within the last two months
		public boolean attributeCheck(QDateTime noteDate, QDateTime current) {
			int ny = noteDate.date().year();
			int cy = current.date().year();
			int nm = noteDate.date().month();
			int cm = current.date().month();

			if (cy-ny >= 0)  {
				cm = cm+12*(cy-ny);
			} else {
				return false;
			}
			if (checkSince) {
				return cm-nm <=1;
			} else {
				return cm-nm > 1;
			}
		}
	}
	private class checkYear extends DateAttributeFilter {
		public checkYear() {
			super(tr("This Year"));
		}
		// Check if it was within this year
		public boolean attributeCheck(QDateTime noteDate, QDateTime current) {
			int ny = noteDate.date().year();
			int cy = current.date().year();
			if (checkSince)
				return ny-cy == 0;
			else
				return ny-cy < 0;
		}	
	}
	private class checkLastYear extends DateAttributeFilter {
		public checkLastYear() {
			super(tr("Last Year"));
		}
		// Check if it was within the last year
		public boolean attributeCheck(QDateTime noteDate, QDateTime current) {
			int ny = noteDate.date().year();
			int cy = current.date().year();
			if (checkSince) 
				return cy-ny <=1;
			else
				return cy-ny > 1;
		}
	}
	
	// Get the name of a particular attribute check
	public String getName(int i) {
		return table.get(i).getName();
	}
}
