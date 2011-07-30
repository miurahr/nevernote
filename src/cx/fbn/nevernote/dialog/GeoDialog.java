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
//* This is the dialog when the user clicks
//* the geo tag for a note.
//**********************************************
//**********************************************

import com.trolltech.qt.gui.QDialog;
import com.trolltech.qt.gui.QDoubleValidator;
import com.trolltech.qt.gui.QDoubleValidator.Notation;
import com.trolltech.qt.gui.QGridLayout;
import com.trolltech.qt.gui.QIcon;
import com.trolltech.qt.gui.QLabel;
import com.trolltech.qt.gui.QLineEdit;
import com.trolltech.qt.gui.QPushButton;

public class GeoDialog extends QDialog {

	private boolean 	okPressed;
	private final QLineEdit	altitude;
	private final QLineEdit	latitude;
	private final QLineEdit	longitude;
	private final QPushButton ok;
	private final String iconPath = new String("classpath:cx/fbn/nevernote/icons/");
	
	
	// Constructor
	public GeoDialog() {
		okPressed = false;
		setWindowTitle(tr("Geo Location"));
		setWindowIcon(new QIcon(iconPath+"globe.png"));
		QGridLayout grid = new QGridLayout();
		setLayout(grid);
		QGridLayout passwordGrid = new QGridLayout();
		QGridLayout buttonGrid = new QGridLayout();
		
		
		longitude = new QLineEdit();
		QDoubleValidator longVal = new QDoubleValidator(-180.0,180.0,4,longitude);
		longVal.setNotation(Notation.StandardNotation);
		longitude.setValidator(longVal);
		
		latitude = new QLineEdit();
		QDoubleValidator latVal = new QDoubleValidator(-90.0,90.0,4,latitude);
		latVal.setNotation(Notation.StandardNotation);
		latitude.setValidator(latVal);
		
		altitude = new QLineEdit();
		QDoubleValidator altVal = new QDoubleValidator(-9999.0,9999.0,4,altitude);
		altVal.setNotation(Notation.StandardNotation);
		altitude.setValidator(altVal);

		
		passwordGrid.addWidget(new QLabel(tr("Longitude")), 1,1);
		passwordGrid.addWidget(longitude, 1, 2);
		passwordGrid.addWidget(new QLabel(tr("Latitude")), 2,1);
		passwordGrid.addWidget(latitude, 2, 2);
		passwordGrid.addWidget(new QLabel(tr("Altitude")), 3,1);
		passwordGrid.addWidget(altitude, 3, 2);
		passwordGrid.setContentsMargins(10, 10,  -10, -10);
		grid.addLayout(passwordGrid,1,1);
		
		ok = new QPushButton(tr("OK"));
		ok.clicked.connect(this, "okButtonPressed()");
		QPushButton cancel = new QPushButton(tr("Cancel"));
		cancel.clicked.connect(this, "cancelButtonPressed()");
		buttonGrid.addWidget(ok, 1, 1);
		buttonGrid.addWidget(cancel, 1,2);
		grid.addLayout(buttonGrid,2,1);
	}
	
	// The OK button was pressed
	@SuppressWarnings("unused")
	private void okButtonPressed() {
		okPressed = true;
		close();
	}
	
	// The CANCEL button was pressed
	@SuppressWarnings("unused")
	private void cancelButtonPressed() {
		okPressed = false;
		close();
	}
	
	// Get the longitude
	public double getLongitude() {
		try {
			return new Double(longitude.text());
		} catch (java.lang.NumberFormatException e) {
			return 0.0;
		}
	}
	
	// Get the latitude
	public double getLatitude() {
		try {
			return new Double(latitude.text());
		} catch (java.lang.NumberFormatException e) {
			return 0.0;
		}
	}
	
	// Get the altitude
	public double getAltitude() {
		try {
			return new Double(altitude.text()); 
		} catch (java.lang.NumberFormatException e) {
			return 0.0;
		}
	}
	
	
	public void setLongitude(double value) {
		longitude.setText(new Float(value).toString());
	}

	public void setLatitude(double value) {
		latitude.setText(new Float(value).toString());
	}

	public void setAltitude(double value) {
		altitude.setText(new Float(value).toString());
	}

	
	// Check if the OK button was pressed
	public boolean okPressed() {
		return okPressed;
	}

	
}
