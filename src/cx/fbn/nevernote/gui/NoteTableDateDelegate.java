package cx.fbn.nevernote.gui;

import java.text.SimpleDateFormat;

import com.trolltech.qt.core.QDateTime;
import com.trolltech.qt.core.QLocale;
import com.trolltech.qt.gui.QStyledItemDelegate;

import cx.fbn.nevernote.Global;

public class NoteTableDateDelegate extends QStyledItemDelegate {

	
	@Override
	public String displayText(Object value, QLocale locale ) {
		
		String fmt = Global.getDateFormat() + " " + Global.getTimeFormat();
		String dateTimeFormat = new String(fmt);
		SimpleDateFormat simple = new SimpleDateFormat(dateTimeFormat);
		
		StringBuilder date = new StringBuilder(simple.format(value));
		QDateTime created = QDateTime.fromString(date.toString(), fmt);
		return created.toString(fmt);
	 }

}
