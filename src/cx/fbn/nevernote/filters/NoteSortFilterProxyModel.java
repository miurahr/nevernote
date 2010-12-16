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

import java.util.HashMap;
import java.util.Map;

import com.trolltech.qt.core.QAbstractItemModel;
import com.trolltech.qt.core.QModelIndex;
import com.trolltech.qt.core.QObject;
import com.trolltech.qt.core.Qt;
import com.trolltech.qt.gui.QIcon;
import com.trolltech.qt.gui.QImage;
import com.trolltech.qt.gui.QSortFilterProxyModel;

import cx.fbn.nevernote.Global;

public class NoteSortFilterProxyModel extends QSortFilterProxyModel {
	private final Map<String,String> guids;
	
	public NoteSortFilterProxyModel(QObject parent) {
		super(parent);
		guids = new HashMap<String,String>();
		setDynamicSortFilter(true);
//		logger = new ApplicationLogger("filter.log");
	}
	public void clear() {
		guids.clear();
	}
	public void addGuid(String guid) {
//		if (!guids.containsKey(guid))
			guids.put(guid, null);
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
		
		if (guids.containsKey(guid))
			return true;
		else
			return false;
	}
	
	
	@Override
	public void sort(int col, Qt.SortOrder order) {
		if (col != Global.noteTableThumbnailPosition)
			super.sort(col,order);
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