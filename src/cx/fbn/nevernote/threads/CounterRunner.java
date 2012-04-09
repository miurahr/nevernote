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

package cx.fbn.nevernote.threads;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.LinkedBlockingQueue;

import com.evernote.edam.type.Note;
import com.trolltech.qt.core.QMutex;
import com.trolltech.qt.core.QObject;

import cx.fbn.nevernote.filters.NotebookCounter;
import cx.fbn.nevernote.filters.TagCounter;
import cx.fbn.nevernote.signals.NotebookSignal;
import cx.fbn.nevernote.signals.TagSignal;
import cx.fbn.nevernote.signals.TrashSignal;
import cx.fbn.nevernote.sql.DatabaseConnection;
import cx.fbn.nevernote.utilities.ApplicationLogger;

public class CounterRunner extends QObject implements Runnable {
	 
	private class NoteRecord {
		public String notebookGuid;
		public Vector<String> tags;
		public boolean active;
		
		public NoteRecord()  {
			tags = new Vector<String>();
		}
	}
	private final ApplicationLogger 	logger;
	private volatile boolean			keepRunning;
	public int							ID;
	public volatile NotebookSignal		notebookSignal;
	public volatile TrashSignal			trashSignal;
	public volatile TagSignal			tagSignal;
	private volatile Vector<NoteRecord> records;
	public int							type;
	public QMutex						threadLock;
	
	public static int					EXIT=0;
	public static int 					NOTEBOOK=1;
	public static int					TAG=2;
	public static int 					TRASH=3;
	
	public boolean 						ready = false;
	public boolean						abortCount = false;
	private final DatabaseConnection 					conn;

	private volatile LinkedBlockingQueue<Integer> readyQueue = new LinkedBlockingQueue<Integer>();
	
	
	//*********************************************
	//* Constructor                               *
	//*********************************************
	public CounterRunner(String logname, int t, String u, String i, String r, String uid, String pswd, String cpswd) {
		type = t;

		threadLock = new QMutex();
		logger = new ApplicationLogger(logname);
//		setAutoDelete(false);	
		conn = new DatabaseConnection(logger, u, i, r, uid, pswd, cpswd, 300);
		keepRunning = true;
		notebookSignal = new NotebookSignal();
		tagSignal = new TagSignal();
		trashSignal = new TrashSignal();
		
		records = new Vector<NoteRecord>();
	}
	
	
	
	//*********************************************
	//* Run unit                                  *
	//*********************************************
	@Override
	public void run() {
		boolean keepRunning = true;
		
		thread().setPriority(Thread.MIN_PRIORITY);
		while(keepRunning) {
			ready = true;
			try {
				
				type = readyQueue.take();
				threadLock.lock();
				if (type == EXIT)
					keepRunning = false;
				if (type == NOTEBOOK)
					countNotebookResults();
				if (type == TAG)
					countTagResults();
				if (type == TRASH)
					countTrashResults();
				threadLock.unlock();
			} catch (InterruptedException e) {}
		}
		conn.dbShutdown();
	}
	
	
	
	public void setNoteIndex(List<Note> idx) {
		abortCount = true;
		threadLock.lock();
		abortCount = false;
		records.clear();
		if (idx != null) {
			for (int i=0; i<idx.size(); i++) {
				if (abortCount)
					return;
				NoteRecord record = new NoteRecord();
				record.notebookGuid = new String(idx.get(i).getNotebookGuid());
				record.active = idx.get(i).isActive();
				for (int j=0; j<idx.get(i).getTagGuidsSize(); j++) {
					if (abortCount)
						return;
					record.tags.add(new String(idx.get(i).getTagGuids().get(j)));
				}
				records.add(record);
			}
		}
		threadLock.unlock();
	}
	public void release(int type) {
		readyQueue.add(type);
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
	
	
	//*********************************************
	//* Do the actual counting                    *
	//*********************************************
	private void countNotebookResults() {
		logger.log(logger.EXTREME, "Entering ListManager.countNotebookResults");		
		if (abortCount)
			return;
		List<NotebookCounter> nCounter = new ArrayList<NotebookCounter>();
		for (int i=0; i<records.size(); i++) {
			if (abortCount)
				return;
			boolean found = false;
			for (int j=0; j<nCounter.size(); j++) {
				if (abortCount)
					return;
				if (records.get(i).active && nCounter.get(j).getGuid().equals(records.get(i).notebookGuid)) {
					nCounter.get(j).setCount(nCounter.get(j).getCount()+1);
					found = true;
					j=nCounter.size();
				}
			}
			if (!found && records.get(i).active) {
				NotebookCounter newCounter = new NotebookCounter();
				newCounter.setGuid(records.get(i).notebookGuid);
				newCounter.setCount(1);
				nCounter.add(newCounter);
			}
		}
		if (abortCount)
			return;
		notebookSignal.countsChanged.emit(nCounter);
		logger.log(logger.EXTREME, "Leaving ListManager.countNotebookResults()");
	}
	
	
	/*
	private void countTagResults() {
		logger.log(logger.EXTREME, "Entering ListManager.countTagResults");		
		List<TagCounter> counter = new ArrayList<TagCounter>();
		List<Tag> allTags = conn.getTagTable().getAll();
		
		if (abortCount) 
			return;
		if (allTags == null)
			return;
		for (int k=0; k<allTags.size() && keepRunning; k++) {
			TagCounter newCounter = new TagCounter();
			newCounter.setGuid(allTags.get(k).getGuid());
			newCounter.setCount(0);
			counter.add(newCounter);
		}
		
		if (type == TAG_ALL) {
			List<Pair<String, Integer>> tagCounts = conn.getNoteTable().noteTagsTable.getTagCounts();
			if (abortCount)
				return;
			for (int i=0; tagCounts != null &&  i<tagCounts.size(); i++) {
				if (abortCount)
					return;
				for (int j=0; j<counter.size(); j++) {
					if (abortCount)
						return;
					if (tagCounts.get(i).getFirst().equals(counter.get(j).getGuid())) {
						if (abortCount)
							return;
						counter.get(j).setCount(tagCounts.get(i).getSecond());
						j=counter.size();
					}
				}
			}
			if (abortCount)
				return;
			tagSignal.countsChanged.emit(counter);
			return;
		}
		
		
		if (abortCount)
			return;
		List<cx.fbn.nevernote.sql.NoteTagsRecord> tags = conn.getNoteTable().noteTagsTable.getAllNoteTags();
		for (int i=noteIndex.size()-1; i>=0; i--) {
			if (abortCount)
				return;
			String note = noteIndex.get(i);
			for (int x=0; tags!= null && x<tags.size() && keepRunning; x++) {
				if (abortCount)
					return;
				String tag = tags.get(x).tagGuid;
				for (int j=0; j<counter.size() && keepRunning; j++) {
					if (abortCount)
						return;
					if (counter.get(j).getGuid().equals(tag) && note.equals(tags.get(x).noteGuid)) {
						int c = counter.get(j).getCount()+1;
						counter.get(j).setCount(c);
					}
				}
			}
		}
		if (abortCount)
			return;
		tagSignal.countsChanged.emit(counter);
		logger.log(logger.EXTREME, "Leaving ListManager.countTagResults()");
	} */
	private void countTagResults() {
		logger.log(logger.EXTREME, "Entering ListManager.countTagResults");	
		if (abortCount)
			return;
		List<TagCounter> tCounter = new ArrayList<TagCounter>();
		for (int i=0; i<records.size(); i++) {
			if (abortCount)
				return;
			
			// Loop through the list of tags so we can count them
			Vector<String> tags = records.get(i).tags;
			for (int z=0; z<tags.size(); z++) {
				boolean found = false;
				for (int j=0; j<tCounter.size(); j++) {
					if (abortCount)
						return;
					if (tCounter.get(j).getGuid().equals(tags.get(z))) {
						tCounter.get(j).setCount(tCounter.get(j).getCount()+1);
						found = true;
						j=tCounter.size();
					}
				}
				if (!found) {
					TagCounter newCounter = new TagCounter();
					newCounter.setGuid(tags.get(z));
					newCounter.setCount(1);
					tCounter.add(newCounter);
				}
			}
		}
		if (abortCount)
			return;
		tagSignal.countsChanged.emit(tCounter);
		logger.log(logger.EXTREME, "Leaving ListManager.countTagResults");	
	}
	
	
	private void countTrashResults() {
		logger.log(logger.EXTREME, "Entering CounterRunner.countTrashResults()");		
		if (abortCount)
			return;

		int tCounter = 0;
		for (int i=0; i<records.size(); i++) {
			if (abortCount)
				return;
			if (!records.get(i).active)
				tCounter++;
		}
		
		if (abortCount)
			return;

		trashSignal.countChanged.emit(tCounter);
		logger.log(logger.EXTREME, "Leaving CounterRunner.countTrashResults()");
	}

}
