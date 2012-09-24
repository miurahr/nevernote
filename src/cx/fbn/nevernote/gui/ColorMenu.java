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

package cx.fbn.nevernote.gui;

import java.util.List;

import com.trolltech.qt.core.QSize;
import com.trolltech.qt.gui.QAction;
import com.trolltech.qt.gui.QColor;
import com.trolltech.qt.gui.QIcon;
import com.trolltech.qt.gui.QMenu;
import com.trolltech.qt.gui.QPixmap;
import com.trolltech.qt.gui.QWidget;

public class ColorMenu extends Object {
	
	private final QMenu menu;
	QWidget parent;
	QColor currentColor;
	
	public ColorMenu(QWidget b) {
		menu = new QMenu();
		parent = b;
		populateList();
		currentColor = new QColor("black");
	}
	
	public void setDefault(QColor color) {
		currentColor = color;
	}
		
	private void populateList() {
		List<String> colorNames = QColor.colorNames();
		for(int i=0; i<colorNames.size(); i++) {
			QColor color = new QColor(colorNames.get(i));
			QPixmap pix = new QPixmap(new QSize(22, 22));
			pix.fill(color);
			QAction newAction = new QAction(new QIcon(pix), "", parent);
			newAction.setToolTip(colorNames.get(i));
			newAction.setText(colorNames.get(i));
			newAction.hovered.connect(this, "itemHovered()");
			menu.addAction(newAction);
		}
	}
	
	@SuppressWarnings("unused")
	private void itemHovered() {
		if (menu.activeAction() != null && menu.activeAction().toolTip() != null)
			currentColor = new QColor(menu.activeAction().toolTip());
	}
	
	public QColor getColor() {
		return currentColor;
	}
	
	public QMenu getMenu() {
		return menu;
	}
}
