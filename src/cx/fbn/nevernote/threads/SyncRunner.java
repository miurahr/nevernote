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

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.transport.THttpClient;
import org.apache.thrift.transport.TTransportException;

import com.evernote.edam.error.EDAMNotFoundException;
import com.evernote.edam.error.EDAMSystemException;
import com.evernote.edam.error.EDAMUserException;
import com.evernote.edam.notestore.NoteStore;
import com.evernote.edam.notestore.SyncChunk;
import com.evernote.edam.notestore.SyncState;
import com.evernote.edam.type.Data;
import com.evernote.edam.type.Note;
import com.evernote.edam.type.Notebook;
import com.evernote.edam.type.Resource;
import com.evernote.edam.type.SavedSearch;
import com.evernote.edam.type.Tag;
import com.evernote.edam.type.User;
import com.evernote.edam.userstore.AuthenticationResult;
import com.evernote.edam.userstore.UserStore;
import com.trolltech.qt.core.QObject;
import com.trolltech.qt.gui.QMessageBox;

import cx.fbn.nevernote.signals.NoteIndexSignal;
import cx.fbn.nevernote.signals.NoteResourceSignal;
import cx.fbn.nevernote.signals.NoteSignal;
import cx.fbn.nevernote.signals.NotebookSignal;
import cx.fbn.nevernote.signals.SavedSearchSignal;
import cx.fbn.nevernote.signals.StatusSignal;
import cx.fbn.nevernote.signals.SyncSignal;
import cx.fbn.nevernote.signals.TagSignal;
import cx.fbn.nevernote.sql.DatabaseConnection;
import cx.fbn.nevernote.sql.DeletedItemRecord;
import cx.fbn.nevernote.utilities.ApplicationLogger;

public class SyncRunner extends QObject implements Runnable {
	
	private final ApplicationLogger logger;
		private DatabaseConnection 		conn;
		private boolean					idle;
		private boolean 				error;
		public volatile boolean			isConnected;
		public volatile boolean	 		keepRunning;
		public volatile String			authToken;
		private long					evernoteUpdateCount;
		
		public volatile NoteStore.Client 		noteStore;
		private UserStore.Client				userStore;
		
		public volatile StatusSignal			status;
		public volatile TagSignal				tagSignal;
		public volatile NotebookSignal			notebookSignal;
		public volatile NoteIndexSignal			noteIndexSignal;
		public volatile NoteSignal				noteSignal;
		public volatile SavedSearchSignal		searchSignal;
		public volatile NoteResourceSignal		resourceSignal;
		public volatile SyncSignal				syncSignal;
		public volatile boolean					authRefreshNeeded;
		public volatile boolean					syncNeeded;
		public volatile boolean					disableUploads;
		public volatile boolean 				syncDeletedContent;
		private volatile Vector<String>			dirtyNoteGuids;
		
	    public volatile String username = ""; 
	    public volatile String password = ""; 
		public volatile String userStoreUrl;
	    private final static String consumerKey = "baumgarte"; 
	    private final static String consumerSecret = "eb8b5740e17cb55f";
	    public String noteStoreUrlBase;
	    private THttpClient userStoreTrans;
	    private TBinaryProtocol userStoreProt;
	    private AuthenticationResult authResult;
	    private User user; 
	    private long authTimeRemaining;
	    public long authRefreshTime;
	    public long failedRefreshes = 0;
	    public  THttpClient noteStoreTrans;
	    public TBinaryProtocol noteStoreProt;
	    public String noteStoreUrl;
	    public long sequenceDate;
	    public int updateSequenceNumber;
	    private boolean refreshNeeded;
	    private volatile LinkedBlockingQueue<String> workQueue;
//		private static int MAX_EMPTY_QUEUE_COUNT = 1;
		private static int MAX_QUEUED_WAITING = 1000;
		String dbuid;
		String dburl;
		String dbpswd;
		String dbcpswd;
	
		
		
	public SyncRunner(String logname, String u, String uid, String pswd, String cpswd) {
		logger = new ApplicationLogger(logname);
		
		noteSignal = new NoteSignal();
		status = new StatusSignal();
		tagSignal = new TagSignal();
		notebookSignal = new NotebookSignal();
		noteIndexSignal = new NoteIndexSignal();
		noteSignal = new NoteSignal();
		searchSignal = new SavedSearchSignal();
		syncSignal = new SyncSignal();
		resourceSignal = new NoteResourceSignal();
		dbuid = uid;
		dburl = u;
		dbpswd = pswd;
		dbcpswd = cpswd;
//		this.setAutoDelete(false);
		
		isConnected = false;
		syncNeeded = false;
		authRefreshNeeded = false;
		keepRunning = true;
		idle = true;
		noteStore = null;
		userStore = null;
		authToken = null;
		disableUploads = false;
//		setAutoDelete(false);
		workQueue=new LinkedBlockingQueue<String>(MAX_QUEUED_WAITING);
	}
	@Override
	public void run() {
		try {
			logger.log(logger.EXTREME, "Starting thread");
			conn = new DatabaseConnection(logger, dburl, dbuid, dbpswd, dbcpswd);
			while(keepRunning) {
				String work = workQueue.take();
				logger.log(logger.EXTREME, "Work found: " +work);
				if (work.equalsIgnoreCase("stop"))
					return;
				idle=false;
				error=false;
				if (authRefreshNeeded == true) {
					logger.log(logger.EXTREME, "Refreshing connection");
					refreshConnection();
				}
				if (syncNeeded) {
					logger.log(logger.EXTREME, "SyncNeeded is true");
					refreshNeeded=false;
					sequenceDate = conn.getSyncTable().getLastSequenceDate();
					updateSequenceNumber = conn.getSyncTable().getUpdateSequenceNumber();
					try {
						logger.log(logger.EXTREME, "Beginning sync");
						evernoteSync();
						logger.log(logger.EXTREME, "Sync finished");
					} catch (UnknownHostException e) {
						status.message.emit(e.getMessage());
					}
				}
				dirtyNoteGuids = null;
				idle=true;
				logger.log(logger.EXTREME, "Signaling refresh finished.  refreshNeeded=" +refreshNeeded);
				syncSignal.finished.emit(refreshNeeded);
			}
		}	
		catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		conn.dbShutdown();
	}

	
	public DatabaseConnection getConnection() {
		return conn;
	}

	public boolean isIdle() {
		return idle;
	}


	public void setConnected(boolean c) {
		isConnected = c;
	}
	public void setKeepRunning(boolean r) {
		logger.log(logger.EXTREME, "Setting keepRunning=" +r);
		keepRunning = r;
	}
	public void setNoteStore(NoteStore.Client c) {
		logger.log(logger.EXTREME, "Setting NoteStore in sync thread");
		noteStore = c;
	}
	public void setUserStore(UserStore.Client c) {
		logger.log(logger.EXTREME, "Setting UserStore in sync thread");
		userStore = c;
	}

	public void setEvernoteUpdateCount(long s) {
		logger.log(logger.EXTREME, "Setting Update Count in sync thread");
		evernoteUpdateCount = s;
	}
	
	//***************************************************************
    //***************************************************************
    //** These functions deal with Evernote communications
    //***************************************************************
    //***************************************************************
	// Synchronize changes with Evernote
	@SuppressWarnings("unused")
	private void evernoteSync() throws java.net.UnknownHostException {
		logger.log(logger.HIGH, "Entering SyncRunner.evernoteSync");

		if (isConnected && keepRunning) {
			error = false;
			logger.log(logger.EXTREME, "Synchronizing with Evernote");
			status.message.emit(tr("Synchronizing with Evernote"));
			
			// Get user information
			try {
				logger.log(logger.EXTREME, "getting user from userstore");
				User user = userStore.getUser(authToken);
				logger.log(logger.EXTREME, "Saving user information");
				syncSignal.saveUserInformation.emit(user);
			} catch (EDAMUserException e1) {
				e1.printStackTrace();
				status.message.emit(tr("User exception getting user account information.  Aborting sync and disconnecting"));
				syncSignal.errorDisconnect.emit();
				enDisconnect();
				return;
			} catch (EDAMSystemException e1) {
				e1.printStackTrace();
				status.message.emit(tr("System error user account information.  Aborting sync and disconnecting!"));
				syncSignal.errorDisconnect.emit();
				enDisconnect();
				return;
			} catch (TException e1) {
				e1.printStackTrace();
				syncSignal.errorDisconnect.emit();
				status.message.emit(tr("Transaction error getting user account information.  Aborting sync and disconnecting!"));
				enDisconnect();
				return;
			}
			
			// Get sync state
			SyncState syncState = null;
			try {	
				logger.log(logger.EXTREME, "Getting sync state");
				syncState = noteStore.getSyncState(authToken);  
				syncSignal.saveUploadAmount.emit(syncState.getUploaded());
				syncSignal.saveEvernoteUpdateCount.emit(syncState.getUpdateCount());
				evernoteUpdateCount = syncState.getUpdateCount();
			} catch (EDAMUserException e) {
				e.printStackTrace();
				status.message.emit(tr("Error getting sync state! Aborting sync and disconnecting!"));
				syncSignal.errorDisconnect.emit();
				enDisconnect();
				return;
			} catch (EDAMSystemException e) {
				e.printStackTrace();
				status.message.emit(tr("Error getting sync state! Aborting sync and disconnecting!"));
				syncSignal.errorDisconnect.emit();
				enDisconnect();
				return;
			} catch (TException e) {
				e.printStackTrace();
				status.message.emit(tr("Error getting sync state! Aborting sync and disconnecting!"));
				syncSignal.errorDisconnect.emit();
				enDisconnect();
				return;
			}
			
			if (syncState == null) {
				logger.log(logger.EXTREME, "Sync State is null");
				status.message.emit(tr("Syncronization Error!"));
				return;
			}

			// Determine what to do. 
			// If we need to do a full sync.
			logger.log(logger.LOW, "Full Sequence Before: " +syncState.getFullSyncBefore());
			logger.log(logger.LOW, "Last Sequence Date: " +sequenceDate);
			if (syncState.getFullSyncBefore() > sequenceDate) {
				logger.log(logger.EXTREME, "Full sequence date has expired");
				sequenceDate = 0;
				conn.getSyncTable().setLastSequenceDate(0);
				updateSequenceNumber = 0;
				conn.getSyncTable().setUpdateSequenceNumber(0);
			}

			// If there are remote changes
			logger.log(logger.LOW, "Update Count: " +syncState.getUpdateCount());
			logger.log(logger.LOW, "Last Update Count: " +updateSequenceNumber);
			
			if (syncState.getUpdateCount() > updateSequenceNumber) {
				logger.log(logger.EXTREME, "Refresh needed is true");
				refreshNeeded = true;
				logger.log(logger.EXTREME, "Downloading changes");
				syncRemoteToLocal();
			}
			
			if (!disableUploads) {
				logger.log(logger.EXTREME, "Uploading changes");
				// Synchronize remote changes
				if (!error)
					syncExpunged();
				if (!error)
					syncLocalTags();
				if (!error)
					syncLocalNotebooks();
				if (!error) 
					syncDeletedNotes();
				if (!error)
					syncLocalNotes();
				if (!error)
					syncLocalSavedSearches();
			}
			if (refreshNeeded)
				syncSignal.refreshLists.emit();
			if (!error) {
				logger.log(logger.EXTREME, "Sync completed.  Errors=" +error);
				if (!disableUploads)
					status.message.emit(tr("Synchronizing complete"));
				else
					status.message.emit(tr("Download syncronization complete.  Uploads have been disabled."));
				
				logger.log(logger.EXTREME, "Saving sync time");
				if (syncState.getCurrentTime() > sequenceDate)
					sequenceDate = syncState.getCurrentTime();
				if (syncState.getUpdateCount() > updateSequenceNumber)
					updateSequenceNumber = syncState.getUpdateCount();
				conn.getSyncTable().setLastSequenceDate(sequenceDate);
				conn.getSyncTable().setUpdateSequenceNumber(updateSequenceNumber);
			}
		}
		logger.log(logger.HIGH, "Leaving SyncRunner.evernoteSync");
	}
	// Sync deleted items with Evernote
	private void syncExpunged() {
		logger.log(logger.HIGH, "Entering SyncRunner.syncExpunged");
		
		List<DeletedItemRecord> expunged = conn.getDeletedTable().getAllDeleted();
 		boolean error = false;
		for (int i=0; i<expunged.size(); i++) {
			
			if (authRefreshNeeded)
				refreshConnection();

			try {
				if (expunged.get(i).type.equalsIgnoreCase("TAG")) {
					logger.log(logger.EXTREME, "Tag expunged");
					updateSequenceNumber = noteStore.expungeTag(authToken, expunged.get(i).guid);
					conn.getSyncTable().setUpdateSequenceNumber(updateSequenceNumber);
					
				}
				if 	(expunged.get(i).type.equalsIgnoreCase("NOTEBOOK")) {
					logger.log(logger.EXTREME, "Notebook expunged");
					updateSequenceNumber = noteStore.expungeNotebook(authToken, expunged.get(i).guid);
					conn.getSyncTable().setLastSequenceDate(sequenceDate);
				}
				if (expunged.get(i).type.equalsIgnoreCase("NOTE")) {
					logger.log(logger.EXTREME, "Note expunged");
					updateSequenceNumber = noteStore.deleteNote(authToken, expunged.get(i).guid);
					conn.getSyncTable().setUpdateSequenceNumber(updateSequenceNumber);
				}
				if (expunged.get(i).type.equalsIgnoreCase("SAVEDSEARCH")) {
					logger.log(logger.EXTREME, "saved search expunged");
					updateSequenceNumber = noteStore.expungeSearch(authToken, expunged.get(i).guid);
					conn.getSyncTable().setLastSequenceDate(sequenceDate);
				}
			} catch (EDAMUserException e) {
				logger.log(logger.LOW, "EDAM User Excepton in syncExpunged: " +expunged.get(i).guid);
				logger.log(logger.LOW, e.getStackTrace());
				error = true;
			} catch (EDAMSystemException e) {
				logger.log(logger.LOW, "EDAM System Excepton in syncExpunged: "+expunged.get(i).guid);
				logger.log(logger.LOW, e.getStackTrace());
				error=true;
			} catch (EDAMNotFoundException e) {
				logger.log(logger.LOW, "EDAM Not Found Excepton in syncExpunged: "+expunged.get(i).guid);
//				logger.log(logger.LOW, e.getStackTrace());
				//error=true;
			} catch (TException e) {
				logger.log(logger.LOW, "EDAM TExcepton in syncExpunged: "+expunged.get(i).guid);
				logger.log(logger.LOW, e.getStackTrace());
				error=true;
			}
		}
		if (!error)
			conn.getDeletedTable().expungeAllDeletedRecords();
		
		logger.log(logger.HIGH, "Leaving SyncRunner.syncExpunged");

	}
	private void syncDeletedNotes() {
		if (syncDeletedContent)
			return;
		logger.log(logger.HIGH, "Entering SyncRunner.syncDeletedNotes");
		status.message.emit(tr("Synchronizing deleted notes."));

		List<Note> notes = conn.getNoteTable().getDirty();
		// Sync the local notebooks with Evernote's
		for (int i=0; i<notes.size() && keepRunning; i++) {
			
			if (authRefreshNeeded)
				refreshConnection();
			
			Note enNote = notes.get(i);
			try {
				if (enNote.getUpdateSequenceNum() > 0 && (enNote.isActive() == false || enNote.getDeleted() > 0)) {
					if (syncDeletedContent) {
						logger.log(logger.EXTREME, "Deleted note found & synch content selected");
						Note delNote = conn.getNoteTable().getNote(enNote.getGuid(), true, true, true, true, true);
						delNote = getNoteContent(delNote);
						delNote = noteStore.updateNote(authToken, delNote);
						enNote.setUpdateSequenceNum(delNote.getUpdateSequenceNum());
						conn.getNoteTable().updateNoteSequence(enNote.getGuid(), enNote.getUpdateSequenceNum());
					} else {
						logger.log(logger.EXTREME, "Deleted note found & sync content not selected");
						int usn = noteStore.deleteNote(authToken, enNote.getGuid());
						enNote.setUpdateSequenceNum(usn);
						conn.getNoteTable().updateNoteSequence(enNote.getGuid(), enNote.getUpdateSequenceNum());						
					}
					logger.log(logger.EXTREME, "Resetting deleted dirty flag");
					conn.getNoteTable().resetDirtyFlag(enNote.getGuid());
					updateSequenceNumber = enNote.getUpdateSequenceNum();
					logger.log(logger.EXTREME, "Saving sequence number");
					conn.getSyncTable().setUpdateSequenceNumber(updateSequenceNumber);
				}				
			} catch (EDAMUserException e) {
				//logger.log(logger.LOW, "*** EDAM User Excepton syncLocalNotes "+e);
				//status.message.emit("Error sending local note: " +e.getParameter());
				//logger.log(logger.LOW, e.toString());	
				//error = true;
			} catch (EDAMSystemException e) {
				logger.log(logger.LOW, "*** EDAM System Excepton syncLocalNotes "+e);
				status.message.emit(tr("Error: ") +e);
				logger.log(logger.LOW, e.toString());		
				error = true;
			} catch (EDAMNotFoundException e) {
				//logger.log(logger.LOW, "*** EDAM Not Found Excepton syncLocalNotes " +e);
				//status.message.emit("Error deleting local note: " +e +" - Continuing");
				//logger.log(logger.LOW, e.toString());		
				//error = true;
			} catch (TException e) {
				logger.log(logger.LOW, "*** EDAM TExcepton syncLocalNotes "+e);
				status.message.emit(tr("Error sending local note: ") +e);
				logger.log(logger.LOW, e.toString());	
				error = true;
			}		
		}
	}
	// Sync notes with Evernote
	private void syncLocalNotes() {
		logger.log(logger.HIGH, "Entering SyncRunner.syncNotes");
		status.message.emit(tr("Sending local notes."));

		List<Note> notes = conn.getNoteTable().getDirty();
		// Sync the local notebooks with Evernote's
		for (int i=0; i<notes.size() && keepRunning; i++) {
			
			if (authRefreshNeeded)
				refreshConnection();
			
			Note enNote = notes.get(i);
			if (enNote.isActive()) {
				try {
					logger.log(logger.EXTREME, "Active dirty note found - non new");
					if (enNote.getUpdateSequenceNum() > 0) {
						enNote = getNoteContent(enNote);
						System.out.println("--------");
						System.out.println("Note:" +enNote);
						System.out.println("--------");
						logger.log(logger.MEDIUM, "Updating note : "+ enNote.getGuid() +" <title>" +enNote.getTitle()+"</title>");
						enNote = noteStore.updateNote(authToken, enNote);
					} else { 
						logger.log(logger.EXTREME, "Active dirty found - new note");
						logger.log(logger.MEDIUM, "Creating note : "+ enNote.getGuid() +" <title>" +enNote.getTitle()+"</title>");
						String oldGuid = enNote.getGuid();
						enNote = getNoteContent(enNote);
						enNote = noteStore.createNote(authToken, enNote);
						noteSignal.guidChanged.emit(oldGuid, enNote.getGuid());
						conn.getNoteTable().updateNoteGuid(oldGuid, enNote.getGuid());
					}
					updateSequenceNumber = enNote.getUpdateSequenceNum();
					logger.log(logger.EXTREME, "Saving note");
					conn.getNoteTable().updateNoteSequence(enNote.getGuid(), enNote.getUpdateSequenceNum());
					List<Resource> rl = enNote.getResources();
					logger.log(logger.EXTREME, "Getting note resources");
					for (int j=0; j<enNote.getResourcesSize() && keepRunning; j++) {
						Resource newRes = rl.get(j);
						Data d = newRes.getData();
						if (d!=null) {	
							logger.log(logger.EXTREME, "Calculating resource hash");
							String hash = byteArrayToHexString(d.getBodyHash());
							logger.log(logger.EXTREME, "updating resources by hash");
							String oldGuid = conn.getNoteTable().noteResourceTable.getNoteResourceGuidByHashHex(enNote.getGuid(), hash);
							conn.getNoteTable().updateNoteResourceGuidbyHash(enNote.getGuid(), newRes.getGuid(), hash);
							resourceSignal.resourceGuidChanged.emit(enNote.getGuid(), oldGuid, newRes.getGuid());
						}
					}
					logger.log(logger.EXTREME, "Resetting note dirty flag");
					conn.getNoteTable().resetDirtyFlag(enNote.getGuid());
					updateSequenceNumber = enNote.getUpdateSequenceNum();
					logger.log(logger.EXTREME, "Emitting note sequence number change");
					conn.getSyncTable().setUpdateSequenceNumber(updateSequenceNumber);

				} catch (EDAMUserException e) {
					logger.log(logger.LOW, "*** EDAM User Excepton syncLocalNotes "+e);
					status.message.emit(tr("Error sending local note: ") +e.getParameter());
					logger.log(logger.LOW, e.toString());	
					error = true;
				} catch (EDAMSystemException e) {
					logger.log(logger.LOW, "*** EDAM System Excepton syncLocalNotes "+e);
					status.message.emit(tr("Error: ") +e);
					logger.log(logger.LOW, e.toString());		
					error = true;
				} catch (EDAMNotFoundException e) {
					logger.log(logger.LOW, "*** EDAM Not Found Excepton syncLocalNotes " +e);
					status.message.emit(tr("Error sending local note: ") +e);
					logger.log(logger.LOW, e.toString());	
					error = true;
				} catch (TException e) {
					logger.log(logger.LOW, "*** EDAM TExcepton syncLocalNotes "+e);
					status.message.emit(tr("Error sending local note: ") +e);
					logger.log(logger.LOW, e.toString());	
					error = true;
				}
			}
		}
		logger.log(logger.HIGH, "Entering SyncRunner.syncNotes");

	}
	// Sync Notebooks with Evernote
	private void syncLocalNotebooks() {
		logger.log(logger.HIGH, "Entering SyncRunner.syncLocalNotebooks");
		
		status.message.emit(tr("Sending local notebooks."));
		List<Notebook> remoteList = new ArrayList<Notebook>();
		try {
			logger.log(logger.EXTREME, "Getting remote notebooks to compare with local");
			remoteList = noteStore.listNotebooks(authToken);
		} catch (EDAMUserException e1) {
			logger.log(logger.LOW, "*** EDAM User Excepton syncLocalNotebooks getting remote Notebook List");
			status.message.emit(tr("Error: ") +e1);
			logger.log(logger.LOW, e1.toString());		
			error = true;
		} catch (EDAMSystemException e1) {
			logger.log(logger.LOW, "*** EDAM System Excepton syncLocalNotebooks getting remote Notebook List");
			status.message.emit(tr("Error: ") +e1);
			logger.log(logger.LOW, e1.toString());	
			error = true;
		} catch (TException e1) {
			logger.log(logger.LOW, "*** EDAM Transaction Excepton syncLocalNotebooks getting remote Notebook List");
			status.message.emit(tr("Error: ") +e1);
			logger.log(logger.LOW, e1.toString());	
			error = true;
		}
		logger.log(logger.EXTREME, "Getting local dirty notebooks");
		List<Notebook> notebooks = conn.getNotebookTable().getDirty();
		int sequence;
		// Sync the local notebooks with Evernote's
		for (int i=0; i<notebooks.size() && keepRunning; i++) {
			
			if (authRefreshNeeded)
				refreshConnection();
			
			Notebook enNotebook = notebooks.get(i);
			try {
				if (enNotebook.getUpdateSequenceNum() > 0) {
					logger.log(logger.EXTREME, "Existing notebook is dirty");
					sequence = noteStore.updateNotebook(authToken, enNotebook);
				} else {
					logger.log(logger.EXTREME, "New dirty notebook found");
					String oldGuid = enNotebook.getGuid();
					boolean found = false;
					
					// Look for a notebook with the same name.  If one is found, we don't need 
					// to create another one
					logger.log(logger.EXTREME, "Looking for matching notebook name");
					for (int k=0; k<remoteList.size() && !found && keepRunning; k++) {
						if (remoteList.get(k).getName().equalsIgnoreCase(enNotebook.getName())) {
							enNotebook = remoteList.get(k);
							logger.log(logger.EXTREME, "Matching notebook found");
							found = true;
						}
					}
					if (!found)
						enNotebook = noteStore.createNotebook(authToken, enNotebook);
					
					logger.log(logger.EXTREME, "Updating notebook in database");
					conn.getNotebookTable().updateNotebookGuid(oldGuid, enNotebook.getGuid());
					sequence = enNotebook.getUpdateSequenceNum();
				}
				logger.log(logger.EXTREME, "Updating notebook sequence in database");
				conn.getNotebookTable().updateNotebookSequence(enNotebook.getGuid(), sequence);
				logger.log(logger.EXTREME, "Resetting dirty flag in notebook");
				conn.getNotebookTable().resetDirtyFlag(enNotebook.getGuid());
				updateSequenceNumber = sequence;
				logger.log(logger.EXTREME, "Emitting sequence number to main thread");
				conn.getSyncTable().setUpdateSequenceNumber(updateSequenceNumber);
			} catch (EDAMUserException e) {
				logger.log(logger.LOW, "*** EDAM User Excepton syncLocalNotebooks");
				logger.log(logger.LOW, e.toString());	
				error = true;
			} catch (EDAMSystemException e) {
				logger.log(logger.LOW, "*** EDAM System Excepton syncLocalNotebooks");
				logger.log(logger.LOW, e.toString());		
				error = true;
			} catch (EDAMNotFoundException e) {
				logger.log(logger.LOW, "*** EDAM Not Found Excepton syncLocalNotebooks");
				logger.log(logger.LOW, e.toString());		
				error = true;
			} catch (TException e) {
				logger.log(logger.LOW, "*** EDAM TExcepton syncLocalNotebooks");
				logger.log(logger.LOW, e.toString());	
				error = true;
			}		
		}
		logger.log(logger.HIGH, "Leaving SyncRunner.syncLocalNotebooks");

	}
	// Sync Tags with Evernote
	private void syncLocalTags() {
		logger.log(logger.HIGH, "Entering SyncRunner.syncLocalTags");
		List<Tag> remoteList = new ArrayList<Tag>();
		status.message.emit(tr("Sending local tags."));
		
		try {
			logger.log(logger.EXTREME, "Getting remote tags to compare names with the local tags");
			remoteList = noteStore.listTags(authToken);
		} catch (EDAMUserException e1) {
			logger.log(logger.LOW, "*** EDAM User Excepton syncLocalTags getting remote Tag List");
			status.message.emit(tr("Error: ") +e1);
			logger.log(logger.LOW, e1.toString());	
			error = true;
		} catch (EDAMSystemException e1) {
			logger.log(logger.LOW, "*** EDAM System Excepton syncLocalTags getting remote Tag List");
			status.message.emit(tr("Error: ") +e1);
			logger.log(logger.LOW, e1.toString());		
			error = true;
		} catch (TException e1) {
			logger.log(logger.LOW, "*** EDAM Transaction Excepton syncLocalTags getting remote Tag List");
			status.message.emit(tr("Error: ") +e1);
			logger.log(logger.LOW, e1.toString());	
			error = true;
		}		
		
		int sequence;
		
		Tag enTag = findNextTag();
		while(enTag!=null) {
			if (authRefreshNeeded)
				refreshConnection();

			try {
				if (enTag.getUpdateSequenceNum() > 0) {
					logger.log(logger.EXTREME, "Updating tag");
					sequence = noteStore.updateTag(authToken, enTag);
				} else {
					
					// Look for a tag with the same name.  If one is found, we don't need 
					// to create another one
					logger.log(logger.EXTREME, "New tag.  Comparing with remote names");
					boolean found = false;
					String oldGuid = enTag.getGuid();
					for (int k=0; k<remoteList.size() && !found && keepRunning; k++) {
						if (remoteList.get(k).getName().equalsIgnoreCase(enTag.getName())) {
							conn.getTagTable().updateTagGuid(enTag.getGuid(), remoteList.get(k).getGuid());
							enTag = remoteList.get(k);
							logger.log(logger.EXTREME, "Matching tag name found");
							found = true;
						}
					}
					if (!found)
						enTag = noteStore.createTag(authToken, enTag);
					else
						enTag.setUpdateSequenceNum(noteStore.updateTag(authToken,enTag));
					sequence = enTag.getUpdateSequenceNum();
					if (!oldGuid.equals(enTag.getGuid())) {
						logger.log(logger.EXTREME, "Updating tag guid");
						conn.getTagTable().updateTagGuid(oldGuid, enTag.getGuid());
					}
				}
				logger.log(logger.EXTREME, "Updating tag sequence number");
				conn.getTagTable().updateTagSequence(enTag.getGuid(), sequence);
				logger.log(logger.EXTREME, "Resetting tag dirty flag");
				conn.getTagTable().resetDirtyFlag(enTag.getGuid());
				logger.log(logger.EXTREME, "Emitting sequence number to the main thread.");
				updateSequenceNumber = sequence;
				conn.getSyncTable().setUpdateSequenceNumber(updateSequenceNumber);
			} catch (EDAMUserException e) {
				logger.log(logger.LOW, "*** EDAM User Excepton syncLocalTags");
				logger.log(logger.LOW, e.toString());		
				error = true;
			} catch (EDAMSystemException e) {
				logger.log(logger.LOW, "** EDAM System Excepton syncLocalTags");
				logger.log(logger.LOW, e.toString());	
				error = true;
			} catch (EDAMNotFoundException e) {
				logger.log(logger.LOW, "*** EDAM Not Found Excepton syncLocalTags");
				logger.log(logger.LOW, e.toString());	
				error = true;
			} catch (TException e) {
				logger.log(logger.LOW, "*** EDAM TExcepton syncLocalTags");
				logger.log(logger.LOW, e.toString());		
				error = true;
			}	
			
			// Find the next tag
			logger.log(logger.EXTREME, "Finding next tag");
			enTag = findNextTag();
		}
		logger.log(logger.HIGH, "Leaving SyncRunner.syncLocalTags");
	}
	// Sync Tags with Evernote
	private void syncLocalSavedSearches() {
		logger.log(logger.HIGH, "Entering SyncRunner.syncLocalSavedSearches");
		List<SavedSearch> remoteList = new ArrayList<SavedSearch>();
		status.message.emit(tr("Sending saved searches."));
	
		logger.log(logger.EXTREME, "Getting saved searches to compare with local");
		try {
			remoteList = noteStore.listSearches(authToken);
		} catch (EDAMUserException e1) {
			logger.log(logger.LOW, "*** EDAM User Excepton syncLocalTags getting remote saved search List");
			status.message.emit(tr("Error: ") +e1);
			logger.log(logger.LOW, e1.toString());	
			error = true;
		} catch (EDAMSystemException e1) {
			logger.log(logger.LOW, "*** EDAM System Excepton syncLocalTags getting remote saved search List");
			status.message.emit(tr("Error: ") +e1);
			logger.log(logger.LOW, e1.toString());		
			error = true;
		} catch (TException e1) {
			logger.log(logger.LOW, "*** EDAM Transaction Excepton syncLocalTags getting remote saved search List");
			status.message.emit(tr("Error: ") +e1);
			logger.log(logger.LOW, e1.toString());	
			error = true;
		}		
		
		List<SavedSearch> searches = conn.getSavedSearchTable().getDirty();
		int sequence;
		// Sync the local notebooks with Evernote's
		logger.log(logger.EXTREME, "Beginning to send saved searches");
		for (int i=0; i<searches.size() &&  keepRunning; i++) {
			
			if (authRefreshNeeded)
				refreshConnection();
			
			SavedSearch enSearch = searches.get(i);
			try {
				if (enSearch.getUpdateSequenceNum() > 0) 
					sequence = noteStore.updateSearch(authToken, enSearch);
				else {
					logger.log(logger.EXTREME, "New saved search found.");
					// Look for a tag with the same name.  If one is found, we don't need 
					// to create another one
					boolean found = false;
					logger.log(logger.EXTREME, "Matching remote saved search names with local");
					for (int k=0; k<remoteList.size() && !found && keepRunning; k++) {
						if (remoteList.get(k).getName().equalsIgnoreCase(enSearch.getName())) {
							enSearch = remoteList.get(k);
							found = true;
							logger.log(logger.EXTREME, "Matching saved search found");
							sequence = enSearch.getUpdateSequenceNum();
						}
					}

					String oldGuid = enSearch.getGuid();
					if (!found)
						enSearch = noteStore.createSearch(authToken, enSearch);
					sequence = enSearch.getUpdateSequenceNum();
					logger.log(logger.EXTREME, "Updating tag guid in local database");
					conn.getTagTable().updateTagGuid(oldGuid, enSearch.getGuid());
				}
				logger.log(logger.EXTREME, "Updating tag sequence in local database");
				conn.getSavedSearchTable().updateSavedSearchSequence(enSearch.getGuid(), sequence);
				logger.log(logger.EXTREME, "Resetting tag dirty flag");
				conn.getSavedSearchTable().resetDirtyFlag(enSearch.getGuid());
				logger.log(logger.EXTREME, "Emitting sequence number to the main thread.");
				updateSequenceNumber = sequence;
				conn.getSyncTable().setUpdateSequenceNumber(updateSequenceNumber);
			} catch (EDAMUserException e) {
				logger.log(logger.LOW, "*** EDAM User Excepton syncLocalTags");
				logger.log(logger.LOW, e.toString());	
				error = true;
			} catch (EDAMSystemException e) {
				logger.log(logger.LOW, "** EDAM System Excepton syncLocalTags");
				logger.log(logger.LOW, e.toString());	
				error = true;
			} catch (EDAMNotFoundException e) {
				logger.log(logger.LOW, "*** EDAM Not Found Excepton syncLocalTags");
				logger.log(logger.LOW, e.toString());	
				error = true;
			} catch (TException e) {
				logger.log(logger.LOW, "*** EDAM TExcepton syncLocalTags");
				logger.log(logger.LOW, e.toString());	
				error = true;
			}		
		}
		logger.log(logger.HIGH, "Entering SyncRunner.syncLocalSavedSearches");
	}	

	// Sync evernote changes with local database
	private void syncRemoteToLocal() {
		logger.log(logger.HIGH, "Entering SyncRunner.syncRemoteToLocal");

		List<Note> dirtyNotes = conn.getNoteTable().getDirty();
		dirtyNoteGuids = new Vector<String>();
		for (int i=0; i<dirtyNotes.size() && keepRunning; i++) {
			dirtyNoteGuids.add(dirtyNotes.get(i).getGuid());
		}
		
		int chunkSize = 10;
		SyncChunk chunk = null;
		boolean fullSync = false;
		boolean more = true;
		
		if (updateSequenceNumber == 0)
			fullSync = true;
		
		status.message.emit(tr("Downloading 0% complete."));
		
		while(more &&  keepRunning) {
			
			if (authRefreshNeeded)
				refreshConnection();
			
			chunk = null;
			int sequence = updateSequenceNumber;
			try {
				logger.log(logger.EXTREME, "Getting chunk from Evernote");
				chunk = noteStore.getSyncChunk(authToken, sequence, chunkSize, fullSync);
			} catch (EDAMUserException e) {
				error = true;
				e.printStackTrace();
				status.message.emit(e.getMessage());
			} catch (EDAMSystemException e) {
				error = true;
				e.printStackTrace();
				status.message.emit(e.getMessage());
			} catch (TException e) {
				error = true;
				e.printStackTrace();
				status.message.emit(e.getMessage());
			} 
			if (error || chunk == null) 
				return;
				
		
			
			syncRemoteTags(chunk.getTags());
			syncRemoteSavedSearches(chunk.getSearches());
			syncRemoteNotebooks(chunk.getNotebooks());
			syncRemoteNotes(chunk.getNotes(), fullSync);
			syncRemoteResources(chunk.getResources());
			
			// Do the local deletes
			logger.log(logger.EXTREME, "Doing local deletes");
			List<String> guid = chunk.getExpungedNotes();
			if (guid != null) 
				for (int i=0; i<guid.size() && keepRunning; i++) {
					logger.log(logger.EXTREME, "Expunging local note from database");
					conn.getNoteTable().expungeNote(guid.get(i), true, false);
				}
			guid = chunk.getExpungedNotebooks();
			if (guid != null)
				for (int i=0; i<guid.size() && keepRunning; i++) {
					logger.log(logger.EXTREME, "Expunging local notebook from database");
					conn.getNotebookTable().expungeNotebook(guid.get(i), false);
				}
			guid = chunk.getExpungedTags();
			if (guid != null)
				for (int i=0; i<guid.size() && keepRunning; i++) {
					logger.log(logger.EXTREME, "Expunging tags from local database");
					conn.getTagTable().expungeTag(guid.get(i), false);
				}
			guid = chunk.getExpungedSearches();
			if (guid != null) 
				for (int i=0; i<guid.size() && keepRunning; i++) {
					logger.log(logger.EXTREME, "Expunging saved search from local database");
					conn.getSavedSearchTable().expungeSavedSearch(guid.get(i), false);
				}

			
			// Check for more notes
			if (chunk.getChunkHighUSN() <= updateSequenceNumber) 
				more = false;
			if (error)
				more = false;
			logger.log(logger.EXTREME, "More notes? " +more);

			
			// Save the chunk sequence number
			if (!error && chunk.getChunkHighUSN() > 0) {
				logger.log(logger.EXTREME, "emitting sequence number to main thread");
				updateSequenceNumber = chunk.getChunkHighUSN();
				conn.getSyncTable().setLastSequenceDate(chunk.getCurrentTime());
				conn.getSyncTable().setUpdateSequenceNumber(updateSequenceNumber);
			}
			
			
			if (more) {
				long pct = chunk.getChunkHighUSN() * 100;
				conn.getSyncTable().setLastSequenceDate(chunk.getCurrentTime());
				pct = pct/evernoteUpdateCount;
				status.message.emit(tr("Downloading ") +new Long(pct).toString()+tr("% complete."));
			}
		}

		logger.log(logger.HIGH, "Leaving SyncRunner.syncRemoteToLocal");
	}
	// Sync remote tags
	private void syncRemoteTags(List<Tag> tags) {
		logger.log(logger.EXTREME, "Entering SyncRunner.syncRemoteTags");
		if (tags != null) {
			for (int i=0; i<tags.size() && keepRunning; i++) {
				String oldGuid;
				oldGuid = conn.getTagTable().findTagByName(tags.get(i).getName());
				if (oldGuid != null && !tags.get(i).getGuid().equalsIgnoreCase(oldGuid))
					conn.getTagTable().updateTagGuid(oldGuid, tags.get(i).getGuid());
				conn.getTagTable().syncTag(tags.get(i), false);
			}
		}
		logger.log(logger.EXTREME, "Leaving SyncRunner.syncRemoteTags");
	}
	// Sync remote tags
	private void syncRemoteSavedSearches(List<SavedSearch> searches) {
		logger.log(logger.EXTREME, "Entering SyncRunner.syncSavedSearches");
		if (searches != null) {
			for (int i=0; i<searches.size() && keepRunning; i++) {
				String oldGuid;
				oldGuid = conn.getSavedSearchTable().findSavedSearchByName(searches.get(i).getName());
				if (oldGuid != null && !searches.get(i).getGuid().equalsIgnoreCase(oldGuid))
					conn.getSavedSearchTable().updateSavedSearchGuid(oldGuid, searches.get(i).getGuid());
				conn.getSavedSearchTable().syncSavedSearch(searches.get(i), false);
			}
		}
		logger.log(logger.EXTREME, "Leaving SyncRunner.syncSavedSearches");
	}
	// Sync remote Notebooks 2
	private void syncRemoteNotebooks(List<Notebook> notebooks) {
		logger.log(logger.EXTREME, "Entering SyncRunner.syncRemoteNotebooks");
		if (notebooks != null) {
			for (int i=0; i<notebooks.size() && keepRunning; i++) {
				String oldGuid;
				oldGuid = conn.getNotebookTable().findNotebookByName(notebooks.get(i).getName());
				if (oldGuid != null && !conn.getNotebookTable().isNotebookLocal(oldGuid) && !notebooks.get(i).getGuid().equalsIgnoreCase(oldGuid))
					conn.getNotebookTable().updateNotebookGuid(oldGuid, notebooks.get(i).getGuid());
				conn.getNotebookTable().syncNotebook(notebooks.get(i), false); 
			}
		}			
		logger.log(logger.EXTREME, "Leaving SyncRunner.syncRemoteNotebooks");
	}
	// Sync remote Resources
	private void syncRemoteResources(List<Resource> resource) {
		// This is how the logic for this works.
		// 1.) If the resource is not in the local database, we add it.
		// 2.) If a copy of the resource is in the local database and the note isn't dirty, we update the local copy
		// 3.) If a copy of the resource is in the local databbase and it is dirty and the hash doesn't match, we ignore it because there
		//     is a conflict.  The note conflict should get a copy of the resource at that time.
		
		logger.log(logger.EXTREME, "Entering SyncRunner.syncRemoteResources");
		if (resource != null) {
			for (int i=0; i<resource.size() && keepRunning; i++) {
				boolean saveNeeded = false;
/* #1 */		Resource r = getEvernoteResource(resource.get(i).getGuid(), true,true,true);
				Resource l = conn.getNoteTable().noteResourceTable.getNoteResource(r.getGuid(), false);
				if (l == null) {
					saveNeeded = true;
				} else {
/* #2 */			boolean isNoteDirty = conn.getNoteTable().isNoteDirty(r.getNoteGuid());
					if (!isNoteDirty)
						saveNeeded = true;
					else {
/* #3 */				String remoteHash = "";
						if (r != null && r.getData() != null && r.getData().getBodyHash() != null)
							remoteHash = byteArrayToHexString(r.getData().getBodyHash());
						String localHash = "";
						if (l != null && l.getData() != null && l.getData().getBodyHash() != null)
							remoteHash = byteArrayToHexString(l.getData().getBodyHash());
				
						if (localHash.equalsIgnoreCase(remoteHash))
							saveNeeded = true;
					}
				}
				
				if (saveNeeded) 
					conn.getNoteTable().noteResourceTable.updateNoteResource(r, false);

			}
		}
		logger.log(logger.EXTREME, "Leaving SyncRunner.syncRemoteResources");
	}
	// Sync remote notebooks
	private void syncRemoteNotes(List<Note> note, boolean fullSync) {
		logger.log(logger.EXTREME, "Entering SyncRunner.syncRemoteNotes");
		if (note != null) {
			for (int i=0; i<note.size() && keepRunning; i++) {
				Note n = getEvernoteNote(note.get(i).getGuid(), true, fullSync, true,true);
				if (n!=null) {
					
					// Basically, this is how the sync logic for a note works.
					// If the remote note has changed and the local has not, we
					// accept the change.
					// If both the local & remote have changed but the sequence
					// numbers are the same, we don't accept the change.  This
					// seems to happen when attachments are indexed by the server.
					// If both the local & remote have changed and the sequence numbers
					// are different we move the local copy to a local notebook (making sure
					// to copy all resources) and we accept the new one.			
					boolean conflictingNote = true;
					logger.log(logger.EXTREME, "Checking for duplicate note " +n.getGuid());
					if (dirtyNoteGuids.contains(n.getGuid())) { 
						logger.log(logger.EXTREME, "Conflict check beginning");
						conflictingNote = checkForConflict(n);
						logger.log(logger.EXTREME, "Conflict check results " +conflictingNote);
						if (conflictingNote)
							moveConflictingNote(n.getGuid());
					}
					if (conflictingNote || fullSync) {
						logger.log(logger.EXTREME, "Saving Note");
						conn.getNoteTable().syncNote(n, false);
						noteSignal.noteChanged.emit(n.getGuid(), null);   // Signal to ivalidate note cache
 						logger.log(logger.EXTREME, "Note Saved");
						if (fullSync && n.getResources() != null) {
							for (int q=0; q<n.getResources().size() && keepRunning; q++) {
								logger.log(logger.EXTREME, "Getting note resources.");
								conn.getNoteTable().noteResourceTable.updateNoteResource(n.getResources().get(q), false);
							}
						}
					}
				}
			}
		}
		logger.log(logger.EXTREME, "Leaving SyncRunner.syncRemoteNotes");
	}
	private Note getEvernoteNote(String guid, boolean withContent, boolean withResourceData, boolean withResourceRecognition, boolean withResourceAlternateData) { 
		Note n = null;
		try {
			logger.log(logger.EXTREME, "Retrieving note " +guid);
			n = noteStore.getNote(authToken, guid, withContent, withResourceData, withResourceRecognition, withResourceAlternateData);
			logger.log(logger.EXTREME, "Note " +guid +" has been retrieved.");
		} catch (EDAMUserException e) {
			logger.log(logger.LOW, "*** EDAM User Excepton getEvernoteNote");
			logger.log(logger.LOW, e.toString());	
			error = true;
			e.printStackTrace();
		} catch (EDAMSystemException e) {
			logger.log(logger.LOW, "*** EDAM System Excepton getEvernoteNote");
			logger.log(logger.LOW, e.toString());	
			error = true;
			e.printStackTrace();
		} catch (EDAMNotFoundException e) {
			logger.log(logger.LOW, "*** EDAM Not Found Excepton getEvernoteNote");
			logger.log(logger.LOW, e.toString());	
			error = true;
			e.printStackTrace();
		} catch (TException e) {
			logger.log(logger.LOW, "*** EDAM TExcepton getEvernoteNote");
			logger.log(logger.LOW, e.toString());	
			error = true;
			e.printStackTrace();
		}
		return n;
	}
	private Resource getEvernoteResource(String guid, boolean withData, boolean withRecognition, boolean withAttributes) { 
		Resource n = null;
		try {
			logger.log(logger.EXTREME, "Retrieving resource " +guid);
			n = noteStore.getResource(authToken, guid, withData, withRecognition, withAttributes, withAttributes);
			logger.log(logger.EXTREME, "Resource " +guid +" has been retrieved.");
		} catch (EDAMUserException e) {
			logger.log(logger.LOW, "*** EDAM User Excepton getEvernoteNote");
			logger.log(logger.LOW, e.toString());	
			error = true;
			e.printStackTrace();
		} catch (EDAMSystemException e) {
			logger.log(logger.LOW, "*** EDAM System Excepton getEvernoteNote");
			logger.log(logger.LOW, e.toString());	
			error = true;
			e.printStackTrace();
		} catch (EDAMNotFoundException e) {
			logger.log(logger.LOW, "*** EDAM Not Found Excepton getEvernoteNote");
			logger.log(logger.LOW, e.toString());	
			error = true;
			e.printStackTrace();
		} catch (TException e) {
			logger.log(logger.LOW, "*** EDAM TExcepton getEvernoteNote");
			logger.log(logger.LOW, e.toString());	
			error = true;
			e.printStackTrace();
		}
		return n;
	}

	
	private boolean checkForConflict(Note n) {
		logger.log(logger.EXTREME, "Checking note sequence number  " +n.getGuid());
		Note oldNote = conn.getNoteTable().getNote(n.getGuid(), false, false, false, false, false);
		logger.log(logger.EXTREME, "Local/Remote sequence numbers: " +oldNote.getUpdateSequenceNum()+"/"+n.getUpdateSequenceNum());
		if (oldNote.getUpdateSequenceNum() == n.getUpdateSequenceNum())
			return false;
		return true;
	}
	
	private void moveConflictingNote(String guid) {
		logger.log(logger.EXTREME, "Conflicting change found for note " +guid);
		List<Notebook> books = conn.getNotebookTable().getAllLocal();
		String notebookGuid = null;
		for (int i=0; i<books.size() && keepRunning; i++) {
			if (books.get(i).getName().equalsIgnoreCase("Conflicting Changes (local)") ||
					books.get(i).getName().equalsIgnoreCase("Conflicting Changes")) {
				notebookGuid = books.get(i).getGuid();
				i=books.size();
			}
		}
		
		if (notebookGuid == null) {
			logger.log(logger.EXTREME, "Building conflicting change notebook " +guid);
			Calendar currentTime = new GregorianCalendar();
			Long l = new Long(currentTime.getTimeInMillis());
			long prevTime = l;
			while (prevTime==l) {
				currentTime = new GregorianCalendar();
				l=currentTime.getTimeInMillis();
			}
			String randint = new String(Long.toString(l));
		
			Notebook newBook = new Notebook();
			newBook.setUpdateSequenceNum(0);
			newBook.setGuid(randint);
			newBook.setName("Conflicting Changes");
			newBook.setServiceCreated(new Date().getTime());
			newBook.setServiceUpdated(new Date().getTime());
			newBook.setDefaultNotebook(false);
			newBook.setPublished(false);
			
			conn.getNotebookTable().addNotebook(newBook, false, true);
			notebookGuid = newBook.getGuid();
		}
		
		// Now that we have a good notebook guid, we need to move the conflicting note
		// to the local notebook
		logger.log(logger.EXTREME, "Moving conflicting note " +guid);
		Calendar currentTime = new GregorianCalendar();
		Long l = new Long(currentTime.getTimeInMillis());
		long prevTime = l;
		while (prevTime==l) {
			currentTime = new GregorianCalendar();
			l = currentTime.getTimeInMillis();
		}
		String newGuid = new String(Long.toString(l));
					
		Note oldNote = conn.getNoteTable().getNote(guid, true, true, false, false, false);
		for (int i=0; i<oldNote.getResources().size() && keepRunning; i++) {
			l = new Long(currentTime.getTimeInMillis());
			String newResG = new String(Long.toString(l));
			String oldResG = oldNote.getResources().get(i).getGuid();
			conn.getNoteTable().noteResourceTable.resetUpdateSequenceNumber(oldResG, true);
			conn.getNoteTable().noteResourceTable.updateNoteResourceGuid(oldResG, newResG, true);
		}
		
		conn.getNoteTable().resetNoteSequence(guid);
		conn.getNoteTable().updateNoteGuid(guid, newGuid);
		conn.getNoteTable().updateNoteNotebook(newGuid, notebookGuid, true);
		
		
		noteSignal.guidChanged.emit(guid,newGuid);
	}
	
	

	
	//******************************************************
	//******************************************************
	//** Utility Functions
	//******************************************************
	//******************************************************
	// Convert a byte array to a hex string
	private static String byteArrayToHexString(byte data[]) {
		StringBuffer buf = new StringBuffer();
	    for (byte element : data) {
	    	int halfbyte = (element >>> 4) & 0x0F;
	        int two_halfs = 0;
	        do {
		       	if ((0 <= halfbyte) && (halfbyte <= 9))
		               buf.append((char) ('0' + halfbyte));
		           else
		           	buf.append((char) ('a' + (halfbyte - 10)));
		       	halfbyte = element & 0x0F;
	        } while(two_halfs++ < 1);
	    }
	    return buf.toString();		
	}

	
	
	//*******************************************************
	//* Find dirty tags, which do not have newly created parents
	//*******************************************************
	private Tag findNextTag() {
		logger.log(logger.HIGH, "Entering SyncRunner.findNextTag");
		Tag nextTag = null;
		List<Tag> tags = conn.getTagTable().getDirty();
		
		// Find the parent.  If the parent has a sequence > 0 then it is a good
		// parent.
		for (int i=0; i<tags.size() && keepRunning; i++) {
			if (tags.get(i).getParentGuid() == null) {
				logger.log(logger.HIGH, "Leaving SyncRunner.findNextTag - tag found without parent");
				return tags.get(i);
			}
			Tag parentTag = conn.getTagTable().getTag(tags.get(i).getParentGuid());
			if (parentTag.getUpdateSequenceNum() > 0) {
				logger.log(logger.HIGH, "Leaving SyncRunner.findNextTag - tag found");
				return tags.get(i);
			}
		}
		
		logger.log(logger.HIGH, "Leaving SyncRunner.findNextTag - no tags returned");
		return nextTag;
	}
	
	
	   // Connect to Evernote
    public boolean enConnect()  {
    	try {
			userStoreTrans = new THttpClient(userStoreUrl);
		} catch (TTransportException e) {
			QMessageBox mb = new QMessageBox(QMessageBox.Icon.Critical, "Transport Excepton", e.getLocalizedMessage());
			mb.exec();
			e.printStackTrace();
		}
		userStoreProt = new TBinaryProtocol(userStoreTrans);
	    userStore = new UserStore.Client(userStoreProt, userStoreProt);
	    syncSignal.saveUserStore.emit(userStore);
	    try {
			authResult = userStore.authenticate(username, password, consumerKey, consumerSecret);
		} catch (EDAMUserException e) {
			QMessageBox mb = new QMessageBox(QMessageBox.Icon.Critical, "Error", "Incorrect username/password");
			mb.exec();
			isConnected = false;
			return false;
		} catch (EDAMSystemException e) {
			QMessageBox mb = new QMessageBox(QMessageBox.Icon.Critical, "EDAM System Excepton", e.getLocalizedMessage());
			mb.exec();
			e.printStackTrace();
			isConnected = false;
		} catch (TException e) {
			QMessageBox mb = new QMessageBox(QMessageBox.Icon.Critical, "Transport Excepton", e.getLocalizedMessage());
			mb.exec();
			e.printStackTrace();
			isConnected = false;
		}
		
	    boolean versionOk = false;
		try {
//			versionOk = userStore.checkVersion("Dave's EDAMDemo (Java)", 
			versionOk = userStore.checkVersion("NeverNote", 
	            com.evernote.edam.userstore.Constants.EDAM_VERSION_MAJOR, 
	              com.evernote.edam.userstore.Constants.EDAM_VERSION_MINOR);
		} catch (TException e) {
			e.printStackTrace();
			isConnected = false;
		} 
	    if (!versionOk) { 
	        System.err.println("Incomatible EDAM client protocol version"); 
	        isConnected = false;
	    }
	    if (authResult != null) {
	    	user = authResult.getUser(); 
	    	authToken = authResult.getAuthenticationToken(); 
	    	noteStoreUrl = noteStoreUrlBase + user.getShardId();
	    	syncSignal.saveAuthToken.emit(authToken);
	    	syncSignal.saveNoteStore.emit(noteStore);
	    	
	 
	    	try {
	    		noteStoreTrans = new THttpClient(noteStoreUrl);
	    	} catch (TTransportException e) {
	    		QMessageBox mb = new QMessageBox(QMessageBox.Icon.Critical, "Transport Excepton", e.getLocalizedMessage());
	    		mb.exec();
	    		e.printStackTrace();
	    		isConnected = false;
	    	} 
	    	noteStoreProt = new TBinaryProtocol(noteStoreTrans);
	    	noteStore = 
	    		new NoteStore.Client(noteStoreProt, noteStoreProt); 
	    	isConnected = true;
	    	authTimeRemaining = authResult.getExpiration() - authResult.getCurrentTime();
	    	authRefreshTime = authTimeRemaining / 2;
	    }
	    
		// Get user information
		try {
			User user = userStore.getUser(authToken);
			syncSignal.saveUserInformation.emit(user);
		} catch (EDAMUserException e1) {
			e1.printStackTrace();
		} catch (EDAMSystemException e1) {
			e1.printStackTrace();
		} catch (TException e1) {
			e1.printStackTrace();
		}
	    
	    return isConnected;
    }
	// Disconnect from the database				
    public void enDisconnect() {
    	isConnected = false;
    }
    // Refresh the connection
    private synchronized void refreshConnection() {
		logger.log(logger.EXTREME, "Entering SyncRunner.refreshConnection()");
//        Calendar cal = Calendar.getInstance();
		
        // If we are not connected let's get out of here
        if (!isConnected)
        	return;
        
   		// If we fail too many times, then let's give up.
   		if (failedRefreshes >=5) {
   			logger.log(logger.EXTREME, "Refresh attempts have failed.  Disconnecting.");
   			isConnected = false;
   			return;
   		}
        
   		// If this is the first time through, then we need to set this
//   		if (authRefreshTime == 0 || cal.getTimeInMillis() > authRefreshTime) 
//   			authRefreshTime = cal.getTimeInMillis();
   		
 //  		// Default to checking again in 5 min.  This in case we fail.
 //  		authRefreshTime = authRefreshTime +(5*60*1000);     

   		// Try to get a new token
		AuthenticationResult newAuth = null; 
		logger.log(logger.EXTREME, "Beginning to try authentication refresh");
    	try {
    		if (userStore != null && authToken != null) 
    			newAuth = userStore.refreshAuthentication(authToken); 
    		else
    			return;
    		logger.log(logger.EXTREME, "UserStore.refreshAuthentication has succeeded.");
		} catch (EDAMUserException e) {
			e.printStackTrace();
			syncSignal.authRefreshComplete.emit(false);
			failedRefreshes++;
			return;
		} catch (EDAMSystemException e) {
			e.printStackTrace();
			syncSignal.authRefreshComplete.emit(false);
			failedRefreshes++;
			return;		
		} catch (TException e) { 
			e.printStackTrace();
			syncSignal.authRefreshComplete.emit(false);
			failedRefreshes++;
			return;
		}
		
		// If we didn't get a good auth, then we've failed
		if (newAuth == null) {
			failedRefreshes++;
			logger.log(logger.EXTREME, "Authentication failure #" +failedRefreshes);
			syncSignal.authRefreshComplete.emit(false);
			return;
		}
		
		// We got a good token.  Now we need to setup the time to renew it.
		logger.log(logger.EXTREME, "Saving authentication tokens");
		authResult = newAuth;
		authToken = new String(newAuth.getAuthenticationToken());
//		authTimeRemaining = authResult.getExpiration() - authResult.getCurrentTime();
//		authRefreshTime = cal.getTimeInMillis() + (authTimeRemaining/4);	
		failedRefreshes=0;
		syncSignal.authRefreshComplete.emit(true);
		authRefreshNeeded = false;
			
		// This should never happen, but if it does we consider this a faild attempt.
//		if (authTimeRemaining <= 0) {
//			failedRefreshes++;
//			syncSignal.authRefreshComplete.emit(false);
//		}
    }
	
	public synchronized boolean addWork(String request) {
		if (workQueue.offer(request))
			return true;
		return false;
	}
    
    private Note getNoteContent(Note n) {
    	n.setContent(conn.getNoteTable().getNoteContentBinary(n.getGuid()));
    	return n;
    }
}
