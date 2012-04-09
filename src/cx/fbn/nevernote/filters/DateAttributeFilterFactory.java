/*
 * This file is part of NixNote 
 * Copyright 2009,2010 Randy Baumgarte
 * Copyright 2010 Hiroshi Miura
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

import com.evernote.edam.type.Note;
import com.trolltech.qt.core.QCoreApplication;
import com.trolltech.qt.core.QDateTime;

public class DateAttributeFilterFactory {
    private DateAttributeFilterFactory () {};
    public enum FilterType {Today,Yesterday,ThisWeek,LastWeek,Month,LastMonth,Year,LastYear};

    public static DateAttributeFilter getFilter (FilterType fType, boolean since, boolean created) {
        switch(fType) {
            case Today:
                return new checkToday(since,created);
            case Yesterday:
                return new checkYesterday(since,created);
            case ThisWeek:
                return new checkThisWeek(since,created);
            case LastWeek:
                return new checkLastWeek(since,created);
            case Month:
                return new checkMonth(since,created);
            case LastMonth:
                return new checkLastMonth(since,created);
            case Year:
                return new checkYear(since,created);
            case LastYear:
                return new checkLastYear(since,created);
		}
 		throw new IllegalArgumentException("The filter type " + fType + " is not recognized.");
	}
}

class checkToday extends DateAttributeFilter {
    public checkToday(boolean since,boolean created) {
        super(since, created);
    }
	// Check if it was within the last day
	@Override
	public boolean attributeCheck(Note n) {
		QDateTime noteDate, current;
		noteDate = noteTime(n);
		current = currentTime();
		if (checkSince)
			return noteDate.daysTo(current) == 0;
		else 
			return noteDate.daysTo(current) > 0;
	}
	@Override
	public String getLabel(){
		return QCoreApplication.translate("cx.fbn.nevernote.filters.DateAttributeFilter", "Today");
	}
}

class checkYesterday extends DateAttributeFilter {
	public checkYesterday(boolean since,boolean created) {
        super(since,created);
    }

	// Check if it was within the last two days
    @Override
	public boolean attributeCheck(Note n) {
        QDateTime noteDate, current;
        noteDate = noteTime(n);
        current = currentTime();
		if (checkSince) 
			return noteDate.daysTo(current) <= 1;
		else
			return noteDate.daysTo(current) > 1;
	}
	@Override
	public String getLabel(){
		return QCoreApplication.translate("cx.fbn.nevernote.filters.DateAttributeFilter", "Yesterday");
	}
}

class checkThisWeek extends DateAttributeFilter {
    public checkThisWeek(boolean since,boolean created) {
        super(since,created);
    }

	// Check if it was within the last two days
    @Override
	public boolean attributeCheck(Note n) {
        QDateTime noteDate, current;
        noteDate = noteTime(n);
        current = currentTime();

		if (checkSince) 
			return noteDate.daysTo(current) <= 7;
		else
			return noteDate.daysTo(current) > 7;
	}
	@Override
	public String getLabel(){
		return QCoreApplication.translate("cx.fbn.nevernote.filters.DateAttributeFilter", "This Week");
	}
}

class checkLastWeek extends DateAttributeFilter {
    public checkLastWeek(boolean since,boolean created) {
        super(since,created);
    }

	// Check if it was within the last two weeks
    @Override
	public boolean attributeCheck(Note n) {
        QDateTime noteDate, current;
        noteDate = noteTime(n);
        current = currentTime();

		if (checkSince) 
			return noteDate.daysTo(current) <= 14;
		else
			return noteDate.daysTo(current) > 14;
	}
	@Override
	public String getLabel(){
		return QCoreApplication.translate("cx.fbn.nevernote.filters.DateAttributeFilter", "Last Week");
	}
}

class checkMonth extends DateAttributeFilter {
    public checkMonth(boolean since,boolean created) {
        super(since,created);
    }

	// Check if it was within the last month
    @Override
	public boolean attributeCheck(Note n) {
        QDateTime noteDate, current;
        noteDate = noteTime(n);
        current = currentTime();

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
	@Override
	public String getLabel(){
		return QCoreApplication.translate("cx.fbn.nevernote.filters.DateAttributeFilter", "This Month");
	}
}

class checkLastMonth extends DateAttributeFilter {
    public checkLastMonth(boolean since,boolean created) {
        super(since,created);
    }

	// Check if it was within the last two months
    @Override
	public boolean attributeCheck(Note n) {
        QDateTime noteDate, current;
        noteDate = noteTime(n);
        current = currentTime();

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
	@Override
	public String getLabel(){
		return QCoreApplication.translate("cx.fbn.nevernote.filters.DateAttributeFilter", "Last Month");
	}
}

class checkYear extends DateAttributeFilter {
	public checkYear(boolean since,boolean created) {
		super(since,created);
	}
	// Check if it was within this year
    @Override
	public boolean attributeCheck(Note n) {
        QDateTime noteDate, current;
        noteDate = noteTime(n);
        current = currentTime();

		int ny = noteDate.date().year();
		int cy = current.date().year();
		if (checkSince)
			return cy-ny == 0;
		else
			return cy-ny > 0;
	}	

	@Override
	public String getLabel(){
		return QCoreApplication.translate("cx.fbn.nevernote.filters.DateAttributeFilter", "This Year");
	}
}

class checkLastYear extends DateAttributeFilter {
    public checkLastYear(boolean since,boolean created) {
        super(since,created);
    }

	// Check if it was within the last year
    @Override
	public boolean attributeCheck(Note n) {
        QDateTime noteDate, current;
        noteDate = noteTime(n);
        current = currentTime();

		int ny = noteDate.date().year();
		int cy = current.date().year();
		if (checkSince) 
			return cy-ny <=1;
		else
			return cy-ny > 1;
	}

	@Override
	public String getLabel(){
		return QCoreApplication.translate("cx.fbn.nevernote.filters.DateAttributeFilter", "Last Year");
	}
}
