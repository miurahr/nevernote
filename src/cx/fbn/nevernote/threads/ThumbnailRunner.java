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
import com.trolltech.qt.core.QByteArray;
import com.trolltech.qt.core.QFile;
import com.trolltech.qt.core.QIODevice.OpenModeFlag;
import com.trolltech.qt.core.QObject;
import com.trolltech.qt.core.QTemporaryFile;

import cx.fbn.nevernote.Global;
import cx.fbn.nevernote.signals.NoteSignal;
import cx.fbn.nevernote.sql.DatabaseConnection;
import cx.fbn.nevernote.utilities.ApplicationLogger;
import cx.fbn.nevernote.xml.NoteFormatter;

public class ThumbnailRunner extends QObject implements Runnable {
	
	private final ApplicationLogger 			logger;
	private String 								guid;
	public  NoteSignal 							noteSignal;
	private boolean								keepRunning;
	private final DatabaseConnection			conn;
	private volatile LinkedBlockingQueue<String> workQueue;
	private static int 							MAX_QUEUED_WAITING = 1000;



	public ThumbnailRunner(String logname, String u, String uid, String pswd, String cpswd) {
		logger = new ApplicationLogger(logname);
		conn = new DatabaseConnection(logger, u, uid, pswd, cpswd);
		noteSignal = new NoteSignal();
		guid = null;
		keepRunning = true;
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
		String fileName = Global.getFileManager().getResDirPath("thumbnail-" + guid + ".html");
		QFile tFile = new QFile(fileName);
		tFile.open(OpenModeFlag.WriteOnly);
		tFile.write(js);
		tFile.close();
		logger.log(logger.HIGH, "Thumbnail file ready");
		noteSignal.thumbnailPageReady.emit(guid, fileName);
	}
		
	


}
