package cx.fbn.nevernote.gui.controls;

import com.trolltech.qt.gui.QAction;
import com.trolltech.qt.gui.QMouseEvent;
import com.trolltech.qt.gui.QProgressBar;

/**
 * Progress bar for showing remaining space for this month in evernote account
 *  
 * @author danil
 *
 */
public class QuotaProgressBar extends QProgressBar {

	private QAction mouseClickAction;

	public QuotaProgressBar() {
		setMouseTracking(true);
	}

	@Override
	protected void mouseReleaseEvent(QMouseEvent arg__1) {
		super.mouseReleaseEvent(arg__1);
		
		mouseClickAction.trigger();
	}

	public void setMouseClickAction(QAction mouseClickAction) {
		this.mouseClickAction = mouseClickAction;
	}

	public QAction getMouseClickAction() {
		return mouseClickAction;
	}
}
