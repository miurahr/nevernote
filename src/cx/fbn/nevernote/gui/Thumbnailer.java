package cx.fbn.nevernote.gui;

import com.trolltech.qt.core.QByteArray;
import com.trolltech.qt.core.QFile;
import com.trolltech.qt.core.QIODevice;
import com.trolltech.qt.core.QObject;
import com.trolltech.qt.core.QSize;
import com.trolltech.qt.core.QUrl;
import com.trolltech.qt.core.Qt.Orientation;
import com.trolltech.qt.core.Qt.ScrollBarPolicy;
import com.trolltech.qt.gui.QImage;
import com.trolltech.qt.gui.QPainter;
import com.trolltech.qt.webkit.QWebPage;

import cx.fbn.nevernote.Global;

public class Thumbnailer extends QObject {
    public QWebPage page;
    public QImage image;
    public QPainter painter;
    public Signal1<String> finished;
    public String guid;
    
    public Thumbnailer(String g, QSize s)
    {
    	guid = g;
    	finished = new Signal1<String>();
    	page = new QWebPage();
        painter = new QPainter();

    	page.mainFrame().setScrollBarPolicy(Orientation.Horizontal, ScrollBarPolicy.ScrollBarAlwaysOff);
    	page.mainFrame().setScrollBarPolicy(Orientation.Vertical, ScrollBarPolicy.ScrollBarAlwaysOff);
    	page.loadFinished.connect(this, "loadFinished(Boolean)");
    }
    
    public void setContent(String content) {
    	QFile file = new QFile(Global.currentDir+"res/thumbnail-"+guid+".html");
    	file.open(new QIODevice.OpenMode(QIODevice.OpenModeFlag.WriteOnly));
    	file.write(new QByteArray(content));
    	file.close(); 
    	page.mainFrame().load(new QUrl(QUrl.fromLocalFile(file.fileName()).toString()));
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
        
        image.save(Global.currentDir+"res/thumbnail-"+guid+".png");
        finished.emit(guid);
    }
}
