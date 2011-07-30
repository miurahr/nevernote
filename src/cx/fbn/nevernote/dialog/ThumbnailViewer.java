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

package cx.fbn.nevernote.dialog;

//**********************************************
//**********************************************
//* This isn't really used but at one time it
//* was used to create a full page preview.
//**********************************************
//**********************************************

import java.util.List;

import com.trolltech.qt.core.QPoint;
import com.trolltech.qt.core.QRect;
import com.trolltech.qt.core.Qt;
import com.trolltech.qt.core.Qt.WindowModality;
import com.trolltech.qt.gui.QApplication;
import com.trolltech.qt.gui.QColor;
import com.trolltech.qt.gui.QDesktopWidget;
import com.trolltech.qt.gui.QDialog;
import com.trolltech.qt.gui.QGridLayout;
import com.trolltech.qt.gui.QImage;
import com.trolltech.qt.gui.QKeyEvent;
import com.trolltech.qt.gui.QLabel;
import com.trolltech.qt.gui.QMouseEvent;
import com.trolltech.qt.gui.QPaintEvent;
import com.trolltech.qt.gui.QPainter;
import com.trolltech.qt.gui.QPalette;
import com.trolltech.qt.gui.QPalette.ColorRole;
import com.trolltech.qt.gui.QPixmap;
import com.trolltech.qt.gui.QWheelEvent;
public class ThumbnailViewer extends QDialog {
	private String thumbnail;
	private final QLabel picture;
	QGridLayout grid = new QGridLayout();
	public Signal0 upArrow;
	public Signal0 downArrow;
	public Signal0 leftArrow;
	public Signal0 rightArrow;
	private QImage	image;
	private List<String> guids;
	

	public ThumbnailViewer() {
		this.setVisible(false);
		
		leftArrow = new Signal0();
		rightArrow = new Signal0();
		upArrow = new Signal0();
		downArrow = new Signal0();


		setAutoFillBackground(true);
		QPalette palette = new QPalette(palette());
	    // Set background colour to black
	    palette.setColor(ColorRole.Base, QColor.black);
	    setPalette(palette);
	    
		grid = new QGridLayout();
		setLayout(grid);
		
		
		picture = new QLabel();
/*		
		QLabel left = new QLabel();
		QLabel right = new QLabel();
		
		grid.addWidget(left, 0,0);
		grid.addWidget(picture,0,1);
		grid.addWidget(right, 0,2);
		grid.setWidgetSpacing(1);
		grid.setContentsMargins(10, 10,  -10, -10);
*/		
		
		setWindowModality(WindowModality.ApplicationModal);
		setWindowFlags(Qt.WindowType.FramelessWindowHint);
		setAttribute(Qt.WidgetAttribute.WA_TranslucentBackground);
//		setBackgroundRole(ColorRole.Shadow);
//		setAttribute(Qt.WidgetAttribute.WA_DeleteOnClose);
//		showFullScreen();
//		this.hide();

	}
	public void setThumbnail(String thumb) {
		thumbnail = thumb;
		image = new QImage(thumbnail);
		picture.setPixmap(QPixmap.fromImage(image));
	}
	public void setThumbnail(QImage i) {
		image = i;
		picture.setPixmap(QPixmap.fromImage(image));
	}
	

	@Override
	public void keyPressEvent(QKeyEvent e) {
		if (e.key() == Qt.Key.Key_Up.value() || e.key() == Qt.Key.Key_Right.value()) {
			upArrow.emit();
		}
		if (e.key() == Qt.Key.Key_Down.value() || e.key() == Qt.Key.Key_Left.value()) {
			downArrow.emit();
		}

		super.keyPressEvent(e);
	}
	
	
	@Override
	public void mousePressEvent(QMouseEvent e) {
		if (e.button() == Qt.MouseButton.LeftButton)
			close();
	}
	
	@Override
	public void wheelEvent(QWheelEvent e) {
		int numDegrees = e.delta() / 8;
        int numSteps = numDegrees / 15;
        
        if (e.orientation().equals(Qt.Orientation.Vertical)) { 
        	if (numSteps > 0) {
        		for (int i=0; i<numSteps; i++) {
        			upArrow.emit();
        			repaint();
        		}
        	}
        	if (numSteps < 0) {
        		for (int i=numSteps; i<0; i++) {
        			downArrow.emit();
        			repaint();
        		}
        	}
        }
	}
	
	@Override
	public void paintEvent(QPaintEvent e) {
		QDesktopWidget desktop = QApplication.desktop();
		int screen = desktop.screenNumber();
		this.setMaximumSize(desktop.size());
		this.setMinimumSize(desktop.size());
		resize(desktop.size());
		
		QPainter painter = new QPainter(this);
		
		painter.fillRect(desktop.screenGeometry(screen), QColor.black);

		QRect availGeo = desktop.availableGeometry(screen);
		
		int x1 = (availGeo.width()/2)-(image.size().width()/2);
		int y1 = (availGeo.height()/2)-(image.size().height()/2);
		
		painter.drawImage(new QPoint(x1,y1), image);
	}


	public List<String> getGuids() {
		return guids;
	}
	public void setGuids(List<String> g) {
		guids = g;
	}


}
