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

public class DateAttributeFilterTable {
	ArrayList<AttributeFilter> table;
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
		table = new ArrayList<AttributeFilter>();
		table.add(new AttributeFilter("Today"));
		table.add(new AttributeFilter("Yesterday"));
		table.add(new AttributeFilter("This Week"));
		table.add(new AttributeFilter("Last Week"));
		table.add(new AttributeFilter("This Month"));
		table.add(new AttributeFilter("Last Month"));
		table.add(new AttributeFilter("This Year"));
		table.add(new AttributeFilter("Last Year"));
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
	
	public int size() { return table.size();
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
			if (table.get(i).isSet()) {
				if (table.get(i).getName().equalsIgnoreCase("today")) {
					if (!checkToday(noteDate, current))
						result = false;
				}
				if (table.get(i).getName().equalsIgnoreCase("yesterday")) {
					if (!checkYesterday(noteDate, current))
						result = false;
				}
				if (table.get(i).getName().equalsIgnoreCase("this week")) {
					if (!checkWeek(noteDate, current))
						result = false;
				}
				if (table.get(i).getName().equalsIgnoreCase("last week")) {
					if (!checkLastWeek(noteDate, current))
						result = false;
				}
				if (table.get(i).getName().equalsIgnoreCase("this month")) {
					if (!checkMonth(noteDate, current))
						result = false;
				}
				if (table.get(i).getName().equalsIgnoreCase("last month")) {
					if (!checkLastMonth(noteDate, current))
						result = false;
				}
				if (table.get(i).getName().equalsIgnoreCase("this year")) {
					if (!checkYear(noteDate, current))
						result = false;
				}
				if (table.get(i).getName().equalsIgnoreCase("last year")) {
					if (!checkLastYear(noteDate, current))
						result = false;
				}
			}
		}
		return result;
	}
	
	
	// Check if it was within the last day
	private boolean checkToday(QDateTime noteDate, QDateTime current) {
		if (checkSince)
			return noteDate.daysTo(current) == 0;
		else 
			return noteDate.daysTo(current) > 0;
	}
	
	// Check if it was within the last two days
	private boolean checkYesterday(QDateTime noteDate, QDateTime current) {
		if (checkSince) 
			return noteDate.daysTo(current) <= 1;
		else
			return noteDate.daysTo(current) > 1;
	}
	
	
	// Check if it was within the last two days
	private boolean checkWeek(QDateTime noteDate, QDateTime current) {
		if (checkSince) 
			return noteDate.daysTo(current) <= 7;
		else
			return noteDate.daysTo(current) > 7;
	}
	
	
	
	
	// Check if it was within the last two weeks
	private boolean checkLastWeek(QDateTime noteDate, QDateTime current) {
		if (checkSince) 
			return noteDate.daysTo(current) <= 14;
		else
			return noteDate.daysTo(current) > 14;
	}
	
	
	
	// Check if it was within the last month
	private boolean checkMonth(QDateTime noteDate, QDateTime current) {
		if (checkSince)
			return noteDate.date().month() - current.date().month() == 0;
		else
			return noteDate.date().month() - current.date().month() != 0;
	}
	
	
	
	
	// Check if it was within the last two months
	private boolean checkLastMonth(QDateTime noteDate, QDateTime current) {
		int ny = noteDate.date().year();
		int cy = current.date().year();
		int nm = noteDate.date().month();
		int cm = current.date().month();

		while (cy-ny >= 1) {
			cm = cm+12;
			cy--;
		}
		if (checkSince) 
			return cm-nm <=1;
		else
			return cm-nm > 1;
	}
	
	
	
	// Check if it was within the last two days
	private boolean checkYear(QDateTime noteDate, QDateTime current) {
		int ny = noteDate.date().year();
		int cy = current.date().year();
		if (checkSince)
			return ny-cy == 0;
		else
			return ny-cy < 0;
	}	
	
	
	
	// Check if it was within the last two days
	private boolean checkLastYear(QDateTime noteDate, QDateTime current) {
		int ny = noteDate.date().year();
		int cy = current.date().year();
		if (checkSince) 
			return cy-ny <=1;
		else
			return cy-ny > 1;
	}
	
	
	// Get the name of a particular attribute check
	public String getName(int i) {
		return table.get(i).getName();
	}
}
