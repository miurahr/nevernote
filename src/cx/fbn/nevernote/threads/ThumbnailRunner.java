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

package cx.fbn.nevernote.threads;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import com.evernote.edam.type.Note;
import com.trolltech.qt.core.QBuffer;
import com.trolltech.qt.core.QByteArray;
import com.trolltech.qt.core.QIODevice;
import com.trolltech.qt.core.QMutex;
import com.trolltech.qt.core.QObject;
import com.trolltech.qt.core.QTemporaryFile;
import com.trolltech.qt.gui.QPixmap;

import cx.fbn.nevernote.Global;
import cx.fbn.nevernote.signals.NoteSignal;
import cx.fbn.nevernote.sql.DatabaseConnection;
import cx.fbn.nevernote.utilities.ApplicationLogger;
import cx.fbn.nevernote.xml.NoteFormatter;


/*
 * 
 * @author Randy Baumgarte
 * 
 * Thumbnail Overview:
 * 
 * How thumbnails are generated is a bit odd.  The problem is that 
 * process of creating the thumbnail involves actually creating an HTML
 * version of the note & all of its resources.  That is very CPU intensive
 * so we try to do it in a separate thread.  Unfortunately, the QWebPage class 
 * which actually creates the thumbnail must be in the main GUI thread.
 * This is the odd way I've tried to get around the problem.
 * 
 * First, the thumbail thread finds a note which needs a thumbnail.  This
 * can be done by either scanning the database or specifically being told
 * a note needs a new thumbnail.  
 * 
 * When a note is found, this thread will read the database and write out
 * the resources and create an HTML version of the note.  It then signals
 * the main GUI thread that a note is ready.  
 * 
 * Next, the main GUI thread will process the signal received from the 
 * thumbnail thread.  The GUI thread will create a QWebPage (via the
 * Thumbnailer class) and will render the image.  The image is written to 
 * the database to be used in the thumbnail view.
 * 
 */
public class ThumbnailRunner extends QObject implements Runnable {
	
	private final ApplicationLogger 			logger;
	private String 								guid;
	public  NoteSignal 							noteSignal;
	private boolean								keepRunning;
	private final DatabaseConnection			conn;
	private volatile LinkedBlockingQueue<String> workQueue;
	private static int 							MAX_QUEUED_WAITING = 1000;
	public QMutex								mutex;



	public ThumbnailRunner(String logname, String u, String uid, String pswd, String cpswd) {
		logger = new ApplicationLogger(logname);
		conn = new DatabaseConnection(logger, u, uid, pswd, cpswd);
		noteSignal = new NoteSignal();
		guid = null;
		keepRunning = true;
		mutex = new QMutex();
		workQueue=new LinkedBlockingQueue<String>(MAX_QUEUED_WAITING);	
	}
	
	
	@Override
	public void run() {
		thread().setPriority(Thread.MIN_PRIORITY);
		
		logger.log(logger.MEDIUM, "Starting thumbnail thread ");
		while (keepRunning) {
			try {
				String work = workQueue.take();
				if (work.startsWith("GENERATE")) {
					work = work.replace("GENERATE ", "");
					guid = work;
					generateThumbnail();
				}
				if (work.startsWith("SCAN")) {
					scanDatabase();
				}
				if (work.startsWith("IMAGE")) {
					work = work.replace("IMAGE ", "");
					guid = work;
					processImage();
				}
				if (work.startsWith("STOP")) {
					logger.log(logger.MEDIUM, "Stopping thumbail thread");
					keepRunning = false;
				}
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		conn.dbShutdown();
	}
	
	
	private void processImage() {
		boolean abort = true;
		if (abort)
			return;
		mutex.lock();
		logger.log(logger.EXTREME, "Image found "+guid);
			
		logger.log(logger.EXTREME, "Getting image");
		QPixmap image = new QPixmap();
		if (!image.load(Global.getFileManager().getResDirPath()+"thumbnail-"+guid+".png")) {
			logger.log(logger.EXTREME, "Failure to reload image. Aborting.");
			mutex.unlock();
			return;
		}
		
		
		logger.log(logger.EXTREME, "Opening buffer");
        QBuffer buffer = new QBuffer();
        if (!buffer.open(QIODevice.OpenModeFlag.WriteOnly)) {
        	logger.log(logger.EXTREME, "Failure to open buffer.  Aborting.");
        	mutex.unlock();
        	return;
        }
	        
		logger.log(logger.EXTREME, "Filling buffer");
        if (!image.save(buffer, "PNG")) {
        	logger.log(logger.EXTREME, "Failure to write to buffer.  Aborting.");	  
        	mutex.unlock();
        	return;
        }
        buffer.close();
	        
		logger.log(logger.EXTREME, "Updating database");
		QByteArray b = new QBuffer(buffer).buffer();
		conn.getNoteTable().setThumbnail(guid, b);
		conn.getNoteTable().setThumbnailNeeded(guid, false);
		mutex.unlock();
	}
	
	
	
	private void scanDatabase() {
		// If there is already work in the queue, that takes priority
		logger.log(logger.HIGH, "Scanning database for notes needing thumbnail");
		if (workQueue.size() > 0)
			return;
		
		// Find a few records that need thumbnails
		List<String> guids = conn.getNoteTable().findThumbnailsNeeded();
		logger.log(logger.HIGH, guids.size() +" records returned");
		for (int i=0; i<guids.size() && keepRunning; i++) {
			guid = guids.get(i);
			logger.log(logger.HIGH, "Working on:" +guids.get(i));
			generateThumbnail();
		}
		logger.log(logger.HIGH, "Scan completed");
	}

		
	public synchronized boolean addWork(String request) {

		if (workQueue.size() == 0) {
			workQueue.offer(request);
			return true;
		}
		return false;
	}
	
	public synchronized int getWorkQueueSize() {
		return workQueue.size();
	}
	
	private void generateThumbnail() {
		QByteArray js = new QByteArray();
		logger.log(logger.HIGH, "Starting thumbnail for " +guid);
		ArrayList<QTemporaryFile> tempFiles = new ArrayList<QTemporaryFile>();
		Note currentNote = conn.getNoteTable().getNote(guid,true,true,false,true,false);
		NoteFormatter formatter = new NoteFormatter(logger, conn, tempFiles);
		currentNote = conn.getNoteTable().getNote(guid, true, true, false, true, false);
		formatter.setNote(currentNote, true);
		formatter.setHighlight(null);
		js.append("<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">");		
		js.append("<style type=\"text/css\">.en-crypt-temp { border-collapse:collapse; border-style:solid; border-color:blue; padding:0.0mm 0.0mm 0.0mm 0.0mm; }</style>");
		js.append("<style type=\"text/css\">en-hilight { background-color: rgb(255,255,0) }</style>");
		js.append("<style> img { max-width:100%; }</style>");
		js.append("<style type=\"text/css\">en-spell { text-decoration: none; border-bottom: dotted 1px #cc0000; }</style>");
		js.append("</head>");
		js.append(formatter.rebuildNoteHTML());
		js.append("</HTML>");
		js.replace("<!DOCTYPE en-note SYSTEM 'http://xml.evernote.com/pub/enml.dtd'>", "");
		js.replace("<!DOCTYPE en-note SYSTEM 'http://xml.evernote.com/pub/enml2.dtd'>", "");
		js.replace("<?xml version='1.0' encoding='UTF-8'?>", "");
		int zoom = 1;
		String content = currentNote.getContent();
		zoom = Global.calculateThumbnailZoom(content);
		logger.log(logger.HIGH, "Thumbnail file ready");
		noteSignal.thumbnailPageReady.emit(guid, js, zoom);
	}
		
	


}
