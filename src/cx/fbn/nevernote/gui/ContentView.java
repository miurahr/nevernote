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

import com.trolltech.qt.core.QEvent;
import com.trolltech.qt.core.QMimeData;
import com.trolltech.qt.core.Qt;
import com.trolltech.qt.gui.QAction;
import com.trolltech.qt.gui.QContextMenuEvent;
import com.trolltech.qt.gui.QDropEvent;
import com.trolltech.qt.gui.QKeyEvent;
import com.trolltech.qt.gui.QKeySequence;
import com.trolltech.qt.gui.QKeySequence.StandardKey;
import com.trolltech.qt.gui.QMenu;
import com.trolltech.qt.gui.QShortcut;
import com.trolltech.qt.network.QNetworkRequest;
import com.trolltech.qt.webkit.QWebPage;
import com.trolltech.qt.webkit.QWebView;

import cx.fbn.nevernote.Global;

public class ContentView extends QWebView {

	BrowserWindow parent;
	QMenu contextMenu = new QMenu(this);
	QMenu tableMenu = new QMenu(this);
	QAction cutAction;
	QShortcut cutShortcut;
	QAction copyAction;
	QShortcut copyShortcut;
	QAction pasteAction;
	QShortcut pasteShortcut;
	QAction pasteWithoutFormatAction;
	QShortcut pasteWithoutFormatShortcut;
	QAction todoAction;
	QShortcut todoShortcut;
	QAction encryptAction;
	QShortcut encryptShortcut;
	QAction downloadAttachment;
	QShortcut downloadAttachmentShortcut;
	QAction downloadImage;
	QShortcut downloadImageShortcut;
	QAction rotateImageRight;
	QShortcut rotateImageRightShortcut;
	QAction rotateImageLeft;
	QShortcut rotateImageLeftShortcut;	
	QAction	insertLinkAction;
	QShortcut insertLinkShortcut;
	QAction insertLatexAction;
	QShortcut insertLatexShortcut;
	QAction	insertTableAction;
	QShortcut insertTableShortcut;
	QAction	insertTableRowAction;
	QShortcut insertTableRowShortcut;
	QAction	insertTableColumnAction;
	QShortcut insertTableColumnShortcut;
	QAction	deleteTableRowAction;
	QShortcut deleteTableRowShortcut;
	QAction	deleteTableColumnAction;
	QShortcut deleteTableColumnShortcut;
	QAction openAction;
	QAction insertQuickLinkAction;
	QShortcut insertQuickLinkShortcut;
	QMenu imageMenu;
	
	QAction redBackgroundColor;
	
	QShortcut insertDateTimeShortcut;
	
	Signal1<QNetworkRequest> downloadAttachmentRequested;
	Signal1<QNetworkRequest> downloadImageRequested;
	
	public ContentView(BrowserWindow p) {
		parent=p;
		contextMenu = new QMenu(this);
		
		openAction = new QAction(tr("Open"), this);
		openAction.setText(tr("Open"));
		contextMenu.addAction(openAction);
		openAction.triggered.connect(parent, "openFile()");
		
		cutAction = new QAction(tr("Cut"), this);
		cutAction.triggered.connect(parent, "cutClicked()");
		contextMenu.addAction(cutAction); 
		contextMenu.insertSeparator(cutAction);
		setupShortcut(cutAction, "Edit_Cut");
		cutShortcut = new QShortcut(this);
		cutShortcut.activated.connect(parent, "cutClicked()");
		setupShortcut(cutShortcut, "Edit_Cut");
		
		copyAction = new QAction(tr("Copy"), this);
		copyAction.triggered.connect(parent, "copyClicked()");
		contextMenu.addAction(copyAction); 
		setupShortcut(copyAction, "Edit_Copy");
		copyShortcut = new QShortcut(this);
		copyShortcut.activated.connect(parent, "copyClicked()");
		setupShortcut(copyShortcut, "Edit_Copy");
		
		pasteAction = pageAction(QWebPage.WebAction.Paste);
		pasteAction.disconnect();
		pasteAction.triggered.connect(parent, "pasteClicked()");
		
		contextMenu.addAction(pasteAction); 
		setupShortcut(pasteAction, "Edit_Paste");
		pasteShortcut = new QShortcut(this);
		pasteShortcut.activated.connect(parent, "pasteClicked()");
		setupShortcut(pasteShortcut, "Edit_Paste");
		
		pasteWithoutFormatAction = new QAction(tr("Paste Without Formatting"), this);
		pasteWithoutFormatAction.triggered.connect(parent, "pasteWithoutFormattingClicked()");
		contextMenu.addAction(pasteWithoutFormatAction); 
		setupShortcut(pasteWithoutFormatAction, "Edit_Paste_Without_Formatting");
//		pasteWithoutFormatShortcut = new QShortcut(this);
//		pasteWithoutFormatShortcut.activated.connect(parent, "pasteWithoutFormattingClicked()");
//		setupShortcut(pasteWithoutFormatShortcut, "Edit_Paste_Without_Formatting");
		
		contextMenu.addSeparator();
		QMenu colorMenu = new QMenu(tr("Background Color"));
		contextMenu.addMenu(colorMenu);
		
		colorMenu.addAction(setupColorMenuOption("White"));
		colorMenu.addAction(setupColorMenuOption("Red"));
		colorMenu.addAction(setupColorMenuOption("Blue"));
		colorMenu.addAction(setupColorMenuOption("Green"));
		colorMenu.addAction(setupColorMenuOption("Yellow"));
		colorMenu.addAction(setupColorMenuOption("Black"));
		colorMenu.addAction(setupColorMenuOption("Grey"));
		colorMenu.addAction(setupColorMenuOption("Purple"));
		colorMenu.addAction(setupColorMenuOption("Brown"));
		colorMenu.addAction(setupColorMenuOption("Orange"));
		colorMenu.addAction(setupColorMenuOption("Powder Blue"));
		
		
		contextMenu.addSeparator();
		todoAction = new QAction(tr("To-do"), this);
		todoAction.triggered.connect(parent, "todoClicked()");
		contextMenu.addAction(todoAction);
		setupShortcut(todoAction, "Edit_Insert_Todo");
		contextMenu.insertSeparator(todoAction);
		todoShortcut = new QShortcut(this);
		todoShortcut.activated.connect(parent, "todoClicked()");
		setupShortcut(todoShortcut, "Edit_Insert_Todo");
		
		encryptAction = new QAction(tr("Encrypt Selected Text"), this);
		encryptAction.triggered.connect(parent, "encryptText()");
		contextMenu.addAction(encryptAction);
		contextMenu.insertSeparator(encryptAction);
		setupShortcut(encryptAction, "Edit_Encrypt_Text");
		encryptAction.setEnabled(false);
		encryptShortcut = new QShortcut(this);
		encryptShortcut.activated.connect(parent, "encryptText()");
		setupShortcut(encryptShortcut, "Edit_Encrypt_Text");
		
		insertLinkAction = new QAction(tr("Insert Hyperlink"), this);
		insertLinkAction.triggered.connect(parent, "insertLink()");
		setupShortcut(insertLinkAction, "Edit_Insert_Hyperlink");
		contextMenu.addAction(insertLinkAction);
		insertLinkAction.setEnabled(false);
		insertLinkShortcut = new QShortcut(this);
		setupShortcut(insertLinkShortcut, "Edit_Insert_Hyperlink");
		insertLinkShortcut.activated.connect(parent, "insertLink()");
		
		insertQuickLinkAction = new QAction(tr("Quick Link"), this);
		insertQuickLinkAction.triggered.connect(parent, "insertQuickLink()");
		setupShortcut(insertQuickLinkAction, "Edit_Insert_QuickLink");
		contextMenu.addAction(insertQuickLinkAction);
		insertQuickLinkAction.setEnabled(false);
		insertQuickLinkShortcut = new QShortcut(this);
		setupShortcut(insertQuickLinkShortcut, "Edit_Insert_Quicklink");
		insertQuickLinkShortcut.activated.connect(parent, "insertQuickLink()");

		insertLatexAction = new QAction(tr("Insert LaTeX Formula"), this);
		insertLatexAction.triggered.connect(parent, "insertLatex()");
		setupShortcut(insertLatexAction, "Edit_Insert_Latex");
		contextMenu.addAction(insertLatexAction);
		insertLatexShortcut = new QShortcut(this);
		setupShortcut(insertLatexShortcut, "Edit_Insert_Latex");
		insertLatexShortcut.activated.connect(parent, "insertLatex()");
		
		contextMenu.addMenu(tableMenu);
		tableMenu.setTitle("Table");
		insertTableAction = new QAction(tr("Insert Table"), this);
		insertTableAction.triggered.connect(parent, "insertTable()");
		setupShortcut(insertTableAction, "Edit_Insert_Table");
		tableMenu.addAction(insertTableAction);
		insertTableShortcut = new QShortcut(this);
		setupShortcut(insertTableShortcut, "Edit_Insert_Table");
		insertTableShortcut.activated.connect(parent, "insertTable()");

		insertTableRowAction = new QAction(tr("Insert Row"), this);
		insertTableRowAction.triggered.connect(parent, "insertTableRow()");
		setupShortcut(insertTableRowAction, "Edit_Insert_Table_Row");
		tableMenu.addAction(insertTableRowAction);
		insertTableRowShortcut = new QShortcut(this);
		setupShortcut(insertTableRowShortcut, "Edit_Insert_Table_Row");
		insertTableRowShortcut.activated.connect(parent, "insertTableRow()");
		
		insertTableColumnAction = new QAction(tr("Insert Column"), this);
		insertTableColumnAction.triggered.connect(parent, "insertTableColumn()");
		setupShortcut(insertTableColumnAction, "Edit_Insert_Table_Column");
		tableMenu.addAction(insertTableColumnAction);
		insertTableColumnShortcut = new QShortcut(this);
		setupShortcut(insertTableColumnShortcut, "Edit_Insert_Table_Column");
		insertTableColumnShortcut.activated.connect(parent, "insertTableColumn()");
		
		deleteTableRowAction = new QAction(tr("Delete Row"), this);
		deleteTableRowAction.triggered.connect(parent, "deleteTableRow()");
		setupShortcut(deleteTableRowAction, "Edit_Delete_Table_Row");
		tableMenu.addAction(deleteTableRowAction);
		deleteTableRowShortcut = new QShortcut(this);
		setupShortcut(deleteTableRowShortcut, "Edit_Delete_Table_Row");
		deleteTableRowShortcut.activated.connect(parent, "deleteTableRow()");
		
		deleteTableColumnAction = new QAction(tr("Delete Column"), this);
		deleteTableColumnAction.triggered.connect(parent, "deleteTableColumn()");
		setupShortcut(deleteTableColumnAction, "Edit_Delete_Table_Column");
		tableMenu.addAction(deleteTableColumnAction);
		deleteTableColumnShortcut = new QShortcut(this);
		setupShortcut(deleteTableColumnShortcut, "Edit_Delete_Table_Column");
		deleteTableColumnShortcut.activated.connect(parent, "deleteTableColumn()");
		
		
		insertDateTimeShortcut = new QShortcut(this);
		insertDateTimeShortcut.activated.connect(parent, "insertDateTime()");
		setupShortcut(insertDateTimeShortcut, "Insert_DateTime");
			
		imageMenu = new QMenu();
		imageMenu.setTitle(tr("Image"));
		contextMenu.addMenu(imageMenu);
		downloadImage = pageAction(QWebPage.WebAction.DownloadImageToDisk);
		downloadImage.setText(tr("Save Image"));
		imageMenu.addAction(downloadImage);
//		contextMenu.insertSeparator(downloadImage);
//		page().downloadRequested.connect(this, "downloadImage(QNetworkRequest)");
		downloadImageRequested = new Signal1<QNetworkRequest>();
		
		rotateImageRight = new QAction(tr("Rotate Right"), this);
		imageMenu.addAction(rotateImageRight);
		rotateImageRight.triggered.connect(parent, "rotateImageRight()");
		rotateImageRightShortcut = new QShortcut(this);
		setupShortcut(rotateImageRightShortcut, "Edit_Image_Rotate_Right");
		rotateImageRightShortcut.activated.connect(parent, "rotateImageRight()");
		
		rotateImageLeft = new QAction(tr("Rotate Left"), this);
		imageMenu.addAction(rotateImageLeft);
		rotateImageLeft.triggered.connect(parent, "rotateImageLeft()");
		rotateImageLeftShortcut = new QShortcut(this);
		setupShortcut(rotateImageLeftShortcut, "Edit_Image_Rotate_Left");
		rotateImageLeftShortcut.activated.connect(parent, "rotateImageLeft()");
		
		downloadAttachment = pageAction(QWebPage.WebAction.DownloadLinkToDisk);
		downloadAttachment.setText(tr("Save Attachment"));
		contextMenu.addAction(downloadAttachment);
		page().downloadRequested.connect(this, "downloadAttachment(QNetworkRequest)");
		downloadAttachmentRequested = new Signal1<QNetworkRequest>();
		
	}
	
	private void setupShortcut(QAction action, String text) {
		if (!Global.shortcutKeys.containsAction(text))
			return;
		action.setShortcut(Global.shortcutKeys.getShortcut(text));
	}
	
	private void setupShortcut(QShortcut action, String text) {
		if (!Global.shortcutKeys.containsAction(text))
			return;
		action.setKey(new QKeySequence(Global.shortcutKeys.getShortcut(text)));
	}
	
	private QAction setupColorMenuOption(String color) {
		QAction backgroundColor = new QAction(tr(color), this);
		color = color.replace(" ", "");
		backgroundColor.triggered.connect(this, "setBackground"+color+"()");
		return backgroundColor;
	}
	
	@Override
	public boolean event(QEvent event)
    {
        if (event.type().equals(QEvent.Type.KeyPress)) {
            QKeyEvent ke = (QKeyEvent) event;
            if (ke.key() == Qt.Key.Key_Tab.value()) {
    			parent.tabPressed();
    			ke.accept();
                return true;
            }
            if (ke.key() == Qt.Key.Key_Backtab.value()) {
            	parent.backtabPressed();
            	return true;
            }
        }
        return super.event(event);
    }
	
	
	@Override
	public void keyPressEvent(QKeyEvent e) {
			
		// This is done because if we set the content editable, the scroll keys are
		// ignored by webkit.
		if (e.key() == Qt.Key.Key_PageUp.value() || e.key() == Qt.Key.Key_PageDown.value()) {
			int bottom = page().mainFrame().geometry().bottom();
			int top = page().mainFrame().geometry().top();
			int scrollValue = top-bottom;
			if (e.key() == Qt.Key.Key_PageDown.value())
				scrollValue = -1*scrollValue;
			page().mainFrame().scroll(0, scrollValue);
		}
				
		// This is done to allow proper pasting of resources, otherwise it messes up multiple pastes
		if (e.matches(StandardKey.Paste)) {
			parent.pasteClicked();
			e.accept();
			return;
		}
		super.keyPressEvent(e);
	}

//	public void downloadImage(QNetworkRequest req) {
//		System.out.println(req.url().toString());
//		downloadImageRequested.emit(req);
//	}
	public void downloadAttachment(QNetworkRequest req) {
		downloadAttachmentRequested.emit(req);
	}

	
	@Override
	public void dropEvent(QDropEvent e) {
		setFocus();
		QMimeData mime = e.mimeData();
		parent.handleUrls(mime);
		parent.contentChanged();
//		triggerPageAction(WebAction.Reload);
	}
	
	@Override
	public void contextMenuEvent(QContextMenuEvent event) {
		if (event != null)
			contextMenu.exec(event.globalPos());
	}

	
	
	@SuppressWarnings("unused")
	private void setBackgroundWhite() {parent.setBackgroundColor("white");}
	@SuppressWarnings("unused")
	private void setBackgroundRed() {parent.setBackgroundColor("red");}
	@SuppressWarnings("unused")
	private void setBackgroundBlue() {parent.setBackgroundColor("blue");}
	@SuppressWarnings("unused")
	private void setBackgroundGreen() {parent.setBackgroundColor("green");}
	@SuppressWarnings("unused")
	private void setBackgroundYellow() {parent.setBackgroundColor("yellow");}
	@SuppressWarnings("unused")
	private void setBackgroundBlack() {parent.setBackgroundColor("black");}
	@SuppressWarnings("unused")
	private void setBackgroundPurple() {parent.setBackgroundColor("purple");}
	@SuppressWarnings("unused")
	private void setBackgroundBrown() {parent.setBackgroundColor("brown");}
	@SuppressWarnings("unused")
	private void setBackgroundGrey() {parent.setBackgroundColor("grey");}
	@SuppressWarnings("unused")
	private void setBackgroundOrange() {parent.setBackgroundColor("orange");}
	@SuppressWarnings("unused")
	private void setBackgroundPowderBlue() {parent.setBackgroundColor("powderblue");}


}
