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

package cx.fbn.nevernote.filters;

import java.util.TreeSet;

import com.trolltech.qt.core.QAbstractItemModel;
import com.trolltech.qt.core.QModelIndex;
import com.trolltech.qt.core.QObject;
import com.trolltech.qt.core.Qt;
import com.trolltech.qt.gui.QIcon;
import com.trolltech.qt.gui.QImage;
import com.trolltech.qt.gui.QSortFilterProxyModel;

import cx.fbn.nevernote.Global;

public class NoteSortFilterProxyModel extends QSortFilterProxyModel {
	private final TreeSet<String> guids;
	public Signal2<Integer,Integer> sortChanged;
	public boolean blocked;
	
	public NoteSortFilterProxyModel(QObject parent) {
		super(parent);
		boolean blocked = false;
		guids = new TreeSet<String>();
		setDynamicSortFilter(true);
		sortChanged = new Signal2<Integer,Integer>();
//		logger = new ApplicationLogger("filter.log");
	}
	public void clear() {
		guids.clear();
	}
	public void addGuid(String guid) {
//		if (!guids.containsKey(guid))
			guids.add(guid);
	}
	public void filter() {
		invalidateFilter();
	}
	@Override
	protected boolean filterAcceptsRow(int sourceRow, QModelIndex sourceParent) {
		if (guids.size() == 0)
			return false;
		QAbstractItemModel model = sourceModel();
		QModelIndex guidIndex = sourceModel().index(sourceRow, Global.noteTableGuidPosition);
		String guid = (String)model.data(guidIndex);
		
		if (guids.contains(guid))
			return true;
		else
			return false;
	}
	
	
	@Override
	public void sort(int col, Qt.SortOrder order) {
		if (col != Global.noteTableThumbnailPosition) {
			if (!blocked)	{
				sortChanged.emit(col, order.value());    // Signal that the sort order has been modified
			}
			super.sort(col,order);
		}
	}
	
	@Override
	protected boolean lessThan(QModelIndex left, QModelIndex right) {
		Object leftData = sourceModel().data(left);
		Object rightData = sourceModel().data(right);
		
		if (rightData == null)
			return true;
		if (leftData instanceof QIcon)
			return true;
		if (leftData instanceof QImage && rightData instanceof QImage)
			return true;
		if (leftData instanceof Long && rightData instanceof Long) {
			  Long leftLong = (Long)leftData;
			  Long rightLong = (Long)rightData;
			  return leftLong.compareTo(rightLong) < 0;            
		}
		if (leftData instanceof String && rightData instanceof String) {
			String leftString = (String)leftData;
			String rightString = (String)rightData;
			return leftString.compareTo(rightString) < 0;
		}
		
		return super.lessThan(left, right);
	}
}