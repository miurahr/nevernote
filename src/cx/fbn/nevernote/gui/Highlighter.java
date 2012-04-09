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

import com.trolltech.qt.core.QRegExp;
import com.trolltech.qt.core.Qt;
import com.trolltech.qt.gui.QBrush;
import com.trolltech.qt.gui.QColor;
import com.trolltech.qt.gui.QFont;
import com.trolltech.qt.gui.QSyntaxHighlighter;
import com.trolltech.qt.gui.QTextCharFormat;
import com.trolltech.qt.gui.QTextDocument;

public class Highlighter extends QSyntaxHighlighter {
	
	public class HighlightingRule {
		public QRegExp pattern;
		public QTextCharFormat format;
		
		public HighlightingRule(QRegExp pattern, QTextCharFormat format) {
			this.pattern = pattern;
			this.format = format;
		}
	}

	public Highlighter(QTextDocument parent)  {
		super(parent);
	}

	@Override
	protected void highlightBlock(String text) {
		QTextCharFormat format = new QTextCharFormat();
		QBrush brush = new QBrush(QColor.blue, Qt.BrushStyle.SolidPattern);
		format.setForeground(brush);
		format.setFontWeight(QFont.Weight.Bold.value());
		
		int index = text.indexOf("<");
		while (index >= 0) {
			int length = text.indexOf(">", index)-index+1;
			setFormat(index, length, format);
			index = text.indexOf("<", index+1);
		}
		setCurrentBlockState(0);
	}


}
