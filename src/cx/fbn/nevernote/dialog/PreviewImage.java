package cx.fbn.nevernote.dialog;

import com.trolltech.qt.core.QEvent;
import com.trolltech.qt.gui.QLabel;
import com.trolltech.qt.gui.QMouseEvent;
import com.trolltech.qt.gui.QWidget;

//**********************************************
//**********************************************
//* Show a full screen preview of the note.  This
//* isn't really used much any more.
//**********************************************
//**********************************************

public class PreviewImage extends QLabel {

	public String ID;
	
	public PreviewImage(QWidget parent) {
		super(parent);
		setMouseTracking(true);
	}
	
	@Override
	public void enterEvent(QEvent arg__1) {
		System.out.println("Entering "+ID);
	}
	
	@Override
	public void leaveEvent(QEvent arg__1) {
		System.out.println("Leaving "+ID);
	}
	
	@Override
	public void mousePressEvent(QMouseEvent arg__1) {
		System.out.println("Selection of " +ID);
	}
	
}
