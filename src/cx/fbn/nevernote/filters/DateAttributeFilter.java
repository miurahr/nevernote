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

package cx.fbn.nevernote.filters;

import com.trolltech.qt.core.QCoreApplication;
import com.trolltech.qt.core.QDateTime;

import cx.fbn.nevernote.filters.AttributeFilter;

public abstract class DateAttributeFilter extends AttributeFilter {
	protected boolean checkSince;
	public abstract boolean attributeCheck(QDateTime a, QDateTime b);

	public DateAttributeFilter() {
		super();
	}
	public DateAttributeFilter(String n) {
		super(n);
	}
	public abstract String getLabel();

	public static class checkToday extends DateAttributeFilter {
		public checkToday(boolean since) {
	  	    super();
			checkSince=since; 
		}
		// Check if it was within the last day
		public boolean attributeCheck(QDateTime noteDate, QDateTime current) {
			if (checkSince)
				return noteDate.daysTo(current) == 0;
			else 
				return noteDate.daysTo(current) > 0;
		}
		public String getLabel(){
			return QCoreApplication.translate("DateAttributeFilter", "Today");
		}
	}
	public static class checkYesterday extends DateAttributeFilter {
		public checkYesterday(boolean since) {
			super();
			checkSince=since; 
		}
		// Check if it was within the last two days
		public boolean attributeCheck(QDateTime noteDate, QDateTime current) {
		if (checkSince) 
			return noteDate.daysTo(current) <= 1;
		else
			return noteDate.daysTo(current) > 1;
		}
		public String getLabel(){
			return QCoreApplication.translate("DateAttributeFilter", "Yesterday");
		}
	}
	public static class checkThisWeek extends DateAttributeFilter {
		public checkThisWeek(boolean since) {
	  	     super();
			checkSince=since; 
		}
		// Check if it was within the last two days
		public boolean attributeCheck(QDateTime noteDate, QDateTime current) {
			if (checkSince) 
				return noteDate.daysTo(current) <= 7;
			else
				return noteDate.daysTo(current) > 7;
		}
		public String getLabel(){
			return QCoreApplication.translate("DateAttributeFilter", "This Week");
		}
	}
	public static class checkLastWeek extends DateAttributeFilter {
		public checkLastWeek(boolean since) {
				super();
				checkSince=since; 
		}
		// Check if it was within the last two weeks
		public boolean attributeCheck(QDateTime noteDate, QDateTime current) {
			if (checkSince) 
				return noteDate.daysTo(current) <= 14;
			else
				return noteDate.daysTo(current) > 14;
		}
		public String getLabel(){
			return QCoreApplication.translate("DateAttributeFilter", "Last Week");
		}
	}
	public static class checkMonth extends DateAttributeFilter {
		public checkMonth(boolean since) {
			super();
			checkSince=since; 
		}
		// Check if it was within the last month
		public boolean attributeCheck(QDateTime noteDate, QDateTime current) {
			if (checkSince) {
				if (noteDate.date().year() == current.date().year())
					return noteDate.date().month() - current.date().month() == 0;
				else
					return false;
			} else {
				if (noteDate.date().year() < current.date().year())
					return true;
				else
					return noteDate.date().month() - current.date().month() != 0;
			}
		}
		public String getLabel(){
			return QCoreApplication.translate("DateAttributeFilter", "This Month");
		}
	}
	public static class checkLastMonth extends DateAttributeFilter {
		public checkLastMonth(boolean since) {
			super();
			checkSince=since; 
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
		public String getLabel(){
			return QCoreApplication.translate("DateAttributeFilter", "Last Month");
		}
	}
	public static class checkYear extends DateAttributeFilter {
		public checkYear(boolean since) {
			super();
			checkSince=since; 
		}
		// Check if it was within this year
		public boolean attributeCheck(QDateTime noteDate, QDateTime current) {
			int ny = noteDate.date().year();
			int cy = current.date().year();
			if (checkSince)
				return cy-ny == 0;
			else
				return cy-ny > 0;
		}	
		public String getLabel(){
			return QCoreApplication.translate("DateAttributeFilter", "This Year");
		}
	}
	public static class checkLastYear extends DateAttributeFilter {
		public checkLastYear(boolean since) {
			super();
			checkSince=since; 
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
		public String getLabel(){
			return QCoreApplication.translate("DateAttributeFilter", "Last Year");
		}
	}
}
