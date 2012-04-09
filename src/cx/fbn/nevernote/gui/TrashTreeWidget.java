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

import java.util.ArrayList;
import java.util.List;

import com.trolltech.qt.core.QByteArray;
import com.trolltech.qt.core.Qt;
import com.trolltech.qt.gui.QAbstractItemView;
import com.trolltech.qt.gui.QAction;
import com.trolltech.qt.gui.QBrush;
import com.trolltech.qt.gui.QColor;
import com.trolltech.qt.gui.QContextMenuEvent;
import com.trolltech.qt.gui.QDragEnterEvent;
import com.trolltech.qt.gui.QHeaderView;
import com.trolltech.qt.gui.QIcon;
import com.trolltech.qt.gui.QMenu;
import com.trolltech.qt.gui.QTreeWidget;
import com.trolltech.qt.gui.QTreeWidgetItem;

public class TrashTreeWidget extends QTreeWidget {
	private QAction emptyAction;
	private QTreeWidgetItem trashItem;
	private Integer trashCount;	
	private final String iconPath;
	private final QIcon trashIcon;
	private final QIcon trashFullIcon;
	
	public void setEmptyAction(QAction a) {
		emptyAction = a;
	}
	
	
	public TrashTreeWidget() {
    	trashCount =  0;
    	setProperty("hideTree", true);
    	iconPath = new String("classpath:cx/fbn/nevernote/icons/");
        trashIcon = new QIcon(iconPath+"trash.png");
        trashFullIcon = new QIcon(iconPath+"trash-full.png");
        header().hide();
        //setMaximumHeight(30);
        setMinimumHeight(30);
	}
	
	public void updateCounts(Integer cnt) {
		QBrush gray = new QBrush();
		gray.setColor(QColor.gray);
		QBrush black = new QBrush();
		black.setColor(QColor.black);
		
		trashCount = cnt;
		trashItem.setText(1, trashCount.toString());
		header().resizeSection(1, 0);
		if (trashCount > 0) {
			trashItem.setForeground(0, black);			
			trashItem.setForeground(1, black);
			trashItem.setIcon(0, trashFullIcon);
		} else {
			trashItem.setIcon(0,trashIcon);
			trashItem.setForeground(0, gray);			
			trashItem.setForeground(1, gray);						
		}
	}
	
	public void load() {
        trashItem = new QTreeWidgetItem();
        trashItem.setIcon(0, trashIcon);
        trashItem.setText(0, tr("Trash"));
        Qt.Alignment ra = new Qt.Alignment(Qt.AlignmentFlag.AlignRight);
        trashItem.setTextAlignment(1, ra.value());
        List<String> headers = new ArrayList<String>();
        headers.add("");
        headers.add("");
        setHeaderLabels(headers);
		setColumnCount(2);
		header().setResizeMode(0, QHeaderView.ResizeMode.ResizeToContents);
		header().setResizeMode(1, QHeaderView.ResizeMode.Stretch);
		header().setMovable(false);
        setSelectionMode(QAbstractItemView.SelectionMode.MultiSelection);
        addTopLevelItem(trashItem);

	}

	@Override
	public void contextMenuEvent(QContextMenuEvent event) {
		QMenu menu = new QMenu(this);
		menu.addAction(emptyAction);
		menu.exec(event.globalPos());
	}
	
	
	@Override
	public void dragEnterEvent(QDragEnterEvent event) {
		event.mimeData().setData("application/x-nevernote-trash", new QByteArray(currentItem().text(1)));
		event.accept();
	}
}
