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

import java.util.GregorianCalendar;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import com.evernote.edam.type.Resource;
import com.trolltech.qt.core.QMutex;
import com.trolltech.qt.core.QObject;

import cx.fbn.nevernote.Global;
import cx.fbn.nevernote.evernote.EnmlConverter;
import cx.fbn.nevernote.signals.NoteSignal;
import cx.fbn.nevernote.sql.DatabaseConnection;
import cx.fbn.nevernote.utilities.ApplicationLogger;
import cx.fbn.nevernote.utilities.Pair;

public class SaveRunner extends QObject implements Runnable {
	 
	private final ApplicationLogger 	logger;
	private volatile boolean			keepRunning;
	public QMutex						threadLock;
	private final DatabaseConnection 	conn;
	private boolean						idle;
	public NoteSignal					noteSignals;

	private volatile LinkedBlockingQueue<Pair<String, String>> workQueue = new LinkedBlockingQueue<Pair<String, String>>();
	
	
	//*********************************************
	//* Constructor                               *
	//*********************************************
	public SaveRunner(String logname, String u, String uid, String pswd, String cpswd) {
		logger = new ApplicationLogger(logname);
		conn = new DatabaseConnection(logger, u, uid, pswd, cpswd);
		threadLock = new QMutex();
		keepRunning = true;
		noteSignals = new NoteSignal();
	}
	
	
	
	//*********************************************
	//* Run unit                                  *
	//*********************************************
	@Override
	public void run() {
		thread().setPriority(Thread.MIN_PRIORITY);
		boolean keepRunning = true;
		
		while(keepRunning) {
			try {
				Pair<String, String> content;
				idle = true;
				content = workQueue.take();
				if (!content.getFirst().equalsIgnoreCase("stop")) {						
					idle = false;
					
					// This is a bit of a hack.  It causes this thread to pause for 0.2 seconds.
					// This helps make sure that the main thread gets to the
					// database first when switching notes, othrewise it really 
					// slows things down when fetching new notes.
					GregorianCalendar now = new GregorianCalendar();
					long prev = now.getTimeInMillis();
					prev = prev+200;
					while (prev>now.getTimeInMillis()) {
						now = new GregorianCalendar();						
					}
					
					updateNoteContent(content.getFirst(), content.getSecond());
				} else {
					return;
				}
				threadLock.unlock();
			} catch (InterruptedException e) { }
		}
		conn.dbShutdown();
	}
	
	
	public synchronized void addWork(String guid, String content) {
		while(workQueue.size() > 0) {}
		Pair<String, String> pair = new Pair<String, String>(guid, content);
		workQueue.offer(pair);
	}
	
	public synchronized void release(String guid, String content) {
		Pair<String, String> pair = new Pair<String, String>(guid, content);
		workQueue.add(pair);
	}
	
	public synchronized int getWorkQueueSize() {
		return workQueue.size();
	}

	
	//*********************************************
	//* Getter & Setter method to tell the thread *
	//* to keep running.                          *
	//*********************************************
	public void setKeepRunning(boolean b) {
		keepRunning = b;
	}
	public boolean keepRunning() {
		return keepRunning;
	}
	
	public boolean isIdle() {
		return idle;
	}
	
	
	//*********************************************
	//* Do the actual work			              *
	//*********************************************
	public void updateNoteContent(String guid, String content) {
		logger.log(logger.HIGH, "Entering ListManager.updateNoteContent");
		
		// Actually save the content
		EnmlConverter enml = new EnmlConverter(logger);
		String newContent = enml.convert(guid, content);
		String fixedContent = enml.fixEnXMLCrap(newContent);
		if (fixedContent != null) {
			conn.getNoteTable().updateNoteContent(guid, fixedContent);
			logger.log(logger.EXTREME, "Saving new note resources");
			List<Resource> oldResources = conn.getNoteTable().noteResourceTable.getNoteResources(guid, false);
			List<String> newResources = enml.getResources();
			removeObsoleteResources(oldResources, newResources);
		} else {
			noteSignals.noteSaveRunnerError.emit(guid, null);
		}
		logger.log(logger.HIGH, "Leaving ListManager.updateNoteContent");
	}
	
	// Remove resources that are no longer needed
	private void removeObsoleteResources(List<Resource> oldResources, List<String> newResources) {
		if (oldResources == null || oldResources.size() == 0)
			return;
		if (newResources == null || newResources.size() == 0) {
			for (int i=0; i<oldResources.size(); i++) {
				conn.getNoteTable().noteResourceTable.expungeNoteResource(oldResources.get(i).getGuid());
			}
		}
		for (int i=0; i<oldResources.size(); i++) {
			boolean matchFound = false;
			for (int j=0; j<newResources.size(); j++) {
				if (newResources.get(j).equalsIgnoreCase(oldResources.get(i).getGuid())) 
					matchFound = true;
				if (Global.resourceMap.get(newResources.get(j))!= null) {
					if (Global.resourceMap.get(newResources.get(j)).equalsIgnoreCase(oldResources.get(i).getGuid())) 
						matchFound = true;
				}
				if (matchFound)
					j = newResources.size();
			}
			if (!matchFound)
				conn.getNoteTable().noteResourceTable.expungeNoteResource(oldResources.get(i).getGuid());
		}
	}
}
