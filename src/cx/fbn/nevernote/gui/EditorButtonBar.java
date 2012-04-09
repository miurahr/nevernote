package cx.fbn.nevernote.gui;

import com.trolltech.qt.gui.QAction;
import com.trolltech.qt.gui.QContextMenuEvent;
import com.trolltech.qt.gui.QMenu;
import com.trolltech.qt.gui.QToolBar;

import cx.fbn.nevernote.Global;

public class EditorButtonBar extends QToolBar {
	QMenu contextMenu;
	QAction toggleUndoVisible;
	QAction toggleRedoVisible;
	QAction toggleCutVisible;
	QAction toggleCopyVisible;
	QAction toggleCopyBoldVisible;
	QAction togglePasteVisible;
	QAction toggleBoldVisible;
	QAction toggleUnderlineVisible;
	QAction toggleItalicVisible;
	QAction toggleRightAlignVisible;
	QAction toggleLeftAlignVisible;
	QAction toggleCenterAlignVisible;

	QAction toggleStrikethroughVisible;
	QAction toggleHLineVisible;
	QAction toggleIndentVisible;
	QAction toggleOutdentVisible;
	QAction toggleBulletListVisible;
	QAction toggleNumberListVisible;
	
	QAction toggleFontVisible;
	QAction toggleFontSizeVisible;
	QAction toggleFontColorVisible;
	QAction toggleFontHilight;
	QAction toggleSpellCheck;
	QAction toggleTodo;
	


	public EditorButtonBar() {
		contextMenu = new QMenu();
		
		toggleUndoVisible = addAction("undo" ,tr("Undo"));
		toggleRedoVisible = addAction("redo", tr("Redo Change"));
		toggleCutVisible = addAction("cut", tr("Cut"));
		toggleCopyVisible = addAction("copy", tr("Copy"));
		togglePasteVisible = addAction("paste", tr("Paste"));
		toggleBoldVisible = addAction("bold", tr("Bold"));
		toggleItalicVisible = addAction("italic", tr("Italic"));
		toggleUnderlineVisible = addAction("underline", tr("Underline"));
		toggleStrikethroughVisible = addAction("strikethrough", tr("Strikethrough"));

		toggleLeftAlignVisible = addAction("alignLeft", tr("Left Align"));
		toggleCenterAlignVisible = addAction("alignCenter", tr("Center Align"));
		toggleRightAlignVisible = addAction("alignRight", tr("Right Align"));

		toggleHLineVisible = addAction("hline", tr("Insert Horizontal Line"));
		toggleIndentVisible = addAction("indent", tr("Shift Right"));
		toggleOutdentVisible = addAction("outdent", tr("Shift Left"));
		toggleBulletListVisible = addAction("bulletList", tr("Bullet List"));
		toggleNumberListVisible = addAction("numberList", tr("Number List"));

		toggleFontVisible = addAction("font", tr("Font"));
		toggleFontSizeVisible = addAction("fontSize", tr("Font Size"));
		toggleFontColorVisible = addAction("fontColor", tr("Font Color"));
		toggleFontHilight = addAction("fontHilight", tr("Font Hilight"));
		toggleSpellCheck = addAction("spellCheck", tr("Spell Check"));
		toggleTodo = addAction("todo", tr("To-Do"));
	}
	
	
	private QAction addAction(String config, String name) {
		QAction newAction = new QAction(this);
		newAction.setText(name);
		newAction.setCheckable(true);
		newAction.setChecked(Global.isEditorButtonVisible(config));
		contextMenu.addAction(newAction);
		return newAction;
	}
	
	@Override
	public void contextMenuEvent(QContextMenuEvent event) {
		contextMenu.exec(event.globalPos());
	}
}
