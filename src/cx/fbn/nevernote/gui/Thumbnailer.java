package cx.fbn.nevernote.gui;

import com.trolltech.qt.core.QBuffer;
import com.trolltech.qt.core.QIODevice;
import com.trolltech.qt.core.QObject;
import com.trolltech.qt.core.QSize;
import com.trolltech.qt.core.QUrl;
import com.trolltech.qt.core.Qt.Orientation;
import com.trolltech.qt.core.Qt.ScrollBarPolicy;
import com.trolltech.qt.gui.QImage;
import com.trolltech.qt.gui.QPainter;
import com.trolltech.qt.webkit.QWebPage;

import cx.fbn.nevernote.sql.DatabaseConnection;
import cx.fbn.nevernote.utilities.ListManager;

public class Thumbnailer extends QObject {
    public QWebPage page;
    public QImage image;
    public QPainter painter;
    public Signal1<String> finished;
    public String guid;
    private final DatabaseConnection conn;
    private final QSize size;
    public boolean idle = false;
    private final ListManager listManager;
    
    public Thumbnailer(DatabaseConnection conn, ListManager l)
    {
    	this.conn = conn;
    	finished = new Signal1<String>();
    	page = new QWebPage();
    	listManager = l;
        painter = new QPainter();
        size = new QSize(1024,768);
    	page.mainFrame().setScrollBarPolicy(Orientation.Horizontal, ScrollBarPolicy.ScrollBarAlwaysOff);
    	page.mainFrame().setScrollBarPolicy(Orientation.Vertical, ScrollBarPolicy.ScrollBarAlwaysOff);
    	page.loadFinished.connect(this, "loadFinished(Boolean)");
    }
    
    public void loadContent(String guid, String fileName) {
    	idle = false;
    	this.guid = guid;
    	page.setViewportSize(size);
    	page.mainFrame().load(new QUrl(QUrl.fromLocalFile(fileName)));
    }
    

    
	public void loadFinished(Boolean ok)
    {
		if (!ok)
			return;
		
    	QSize size = page.currentFrame().contentsSize();
    	if (size.height() > 2000)
    		size.setHeight(800);
    	if (size.width() < 600)
    		size.setWidth(600);
    	if (size.width() > 2000)
    		size.setWidth(600);
   
    	page.setViewportSize(size);
    	image = new QImage(size, QImage.Format.Format_RGB32);
        painter.begin(image);

        page.mainFrame().render(painter);             //<<<< THIS CAN LOCKUP if height too big!!!!
        painter.end();
        
       image = image.scaled(new QSize(100,100));
        
        QBuffer buffer = new QBuffer();
        buffer.open(QIODevice.OpenModeFlag.ReadWrite);
        image.save(buffer);
        conn.getNoteTable().setThumbnail(guid, buffer.data());
        conn.getNoteTable().setThumbnailNeeded(guid, false);
        
        listManager.getThumbnails().remove(guid);
		listManager.getThumbnails().put(guid, image);
		finished.emit(guid); 
        
        idle = true;
        return;
    }
}
