package cx.fbn.nevernote.gui;

import com.trolltech.qt.gui.QComboBox;
import com.trolltech.qt.gui.QHBoxLayout;
import com.trolltech.qt.gui.QLabel;
import com.trolltech.qt.gui.QSpinBox;
import com.trolltech.qt.gui.QVBoxLayout;
import com.trolltech.qt.gui.QWidget;

import cx.fbn.nevernote.gui.controls.QuotaProgressBar;

public class SearchPanel extends QWidget {
	private final QLabel zoomLabel;
	private final QSpinBox zoomSpinner;
	private final NotebookTreeWidget notebook;
	private final QComboBox search;
	private final QuotaProgressBar bar;
	
	public SearchPanel(QComboBox search, QuotaProgressBar bar, NotebookTreeWidget notebook, QSpinBox zoom) {
		QVBoxLayout mainLayout = new QVBoxLayout();
		QVBoxLayout subLayout = new QVBoxLayout();
		zoomSpinner = zoom;
		this.bar = bar;
		this.search = search;
		this.notebook = notebook;
		
		setLayout(mainLayout);
		subLayout.addWidget(search);
		subLayout.addWidget(bar);
		QHBoxLayout zoomLayout = new QHBoxLayout();
		zoomLabel = new QLabel(tr("Zoom:"));
		zoomLayout.addWidget(zoomLabel);
		zoomLayout.addWidget(zoom);
		zoomLayout.setStretch(1, 100);
		subLayout.addLayout(zoomLayout);
		mainLayout.addLayout(subLayout);
		mainLayout.addWidget(notebook);
		//setStyleSheet("QTreeView {border: 0.0em;}");
		mainLayout.setContentsMargins(0, 0, 0, 0);
	}
	
	public void hideZoom() {
		zoomLabel.hide();
		zoomSpinner.hide();
	}
	public void showZoom() {
		zoomLabel.show();
		zoomSpinner.show();
	}
	
	public void toggleNotebook() {
		show();
		if (notebook.isVisible())
			notebook.hide();
		else
			notebook.show();
		
		checkVisibility();
	}
	
	public void toggleSearchField() {
		show();
		if (search.isVisible())
			search.hide();
		else
			search.show();
		checkVisibility();
	}
	
	private void checkVisibility() {
		if (notebook.isHidden() && bar.isHidden() && search.isHidden() && zoomSpinner.isHidden()) {
			hide();
		}
	}
	
	public void toggleQuotaBar() {
		show();
		if (bar.isVisible())
			bar.hide();
		else
			bar.show();
		checkVisibility();
	}
	
	public void toggleZoom() {
		show();
		if (zoomSpinner.isVisible()) {
			zoomSpinner.hide();
			zoomLabel.hide();
		} else {
			zoomSpinner.show();
			zoomLabel.show();
		}
		checkVisibility();
	}
}
