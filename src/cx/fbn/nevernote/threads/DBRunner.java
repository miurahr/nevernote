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

import java.util.Vector;
import java.util.concurrent.LinkedBlockingQueue;

import com.trolltech.qt.core.QObject;

import cx.fbn.nevernote.Global;
import cx.fbn.nevernote.config.InitializationException;
import cx.fbn.nevernote.signals.DBRunnerSignal;
import cx.fbn.nevernote.sql.requests.DBRunnerRequest;
import cx.fbn.nevernote.sql.requests.DatabaseRequest;
import cx.fbn.nevernote.sql.requests.DeletedItemRequest;
import cx.fbn.nevernote.sql.requests.EnSearchRequest;
import cx.fbn.nevernote.sql.requests.InvalidXMLRequest;
import cx.fbn.nevernote.sql.requests.NoteRequest;
import cx.fbn.nevernote.sql.requests.NoteTagsRequest;
import cx.fbn.nevernote.sql.requests.NotebookRequest;
import cx.fbn.nevernote.sql.requests.ResourceRequest;
import cx.fbn.nevernote.sql.requests.SavedSearchRequest;
import cx.fbn.nevernote.sql.requests.SyncRequest;
import cx.fbn.nevernote.sql.requests.TagRequest;
import cx.fbn.nevernote.sql.requests.WatchFolderRequest;
import cx.fbn.nevernote.sql.requests.WordRequest;
import cx.fbn.nevernote.sql.runners.RDatabaseConnection;
import cx.fbn.nevernote.sql.runners.REnSearch;
import cx.fbn.nevernote.utilities.ApplicationLogger;

public class DBRunner extends QObject implements Runnable {
	private final ApplicationLogger 			logger;
	
	private final RDatabaseConnection 		conn;

	// NFC TODO: why do these need to be volatile?
	private volatile LinkedBlockingQueue<DBRunnerRequest> workQueue;

	public volatile Vector<DBRunnerRequest> 	genericResponse;
	public volatile Vector<DeletedItemRequest> 	deletedItemResponse;
	public volatile Vector<NotebookRequest> 	notebookResponse;
	public volatile Vector<TagRequest> 			tagResponse;
	public volatile Vector<SavedSearchRequest> 	savedSearchResponse;
	public volatile Vector<NoteRequest>			noteResponse;
	public volatile Vector<ResourceRequest>		resourceResponse;
	public volatile Vector<NoteTagsRequest>		noteTagsResponse;
	public volatile Vector<EnSearchRequest>		enSearchResponse;
	public volatile Vector<WatchFolderRequest>	watchFolderResponse;
	public volatile Vector<WordRequest>			wordResponse;
	public volatile Vector<InvalidXMLRequest>	invalidXMLResponse;
	public volatile Vector<SyncRequest>			syncResponse;
	
	// priority queues
	public volatile Vector<DBRunnerRequest>     user;
	public volatile Vector<DBRunnerRequest>		background;
	public volatile Vector<DBRunnerRequest>		discretionary;
	private static int MAX_EMPTY_QUEUE_COUNT = 100;
	private static int MAX_QUEUED_WAITING = 100;

	
	public DBRunnerSignal dbSignal;
	public boolean keepRunning;
	private final String url;
	private final String cypherPassword;
	private final String userid;
	private final String userPassword;
	
	/**
	 * @throws InitializationException when opening the database fails
	 */
	public DBRunner(String u, String id, String pass, String cypher) throws InitializationException {
		workQueue=new LinkedBlockingQueue<DBRunnerRequest>(MAX_QUEUED_WAITING);

		url=u;
		userid = id;
		userPassword = pass;
		cypherPassword=cypher;

		//***********************************************
		//* These are the priority queues.    
		//***********************************************
		user = new Vector<DBRunnerRequest>();
		background = new Vector<DBRunnerRequest>();
		discretionary = new Vector<DBRunnerRequest>();

		//***********************************************
		//* These are database response queues.  Each
		//* thread has its own individual queue and     
		//* will access it based upod it's individual
		//* thread ID
		//***********************************************
		
		genericResponse = new Vector<DBRunnerRequest>();
		for (int i=0; i<Global.dbThreadId; i++)
			genericResponse.add(new DBRunnerRequest());
	
		deletedItemResponse = new Vector<DeletedItemRequest>();
		for (int i=0; i<Global.dbThreadId; i++)
			deletedItemResponse.add(new DeletedItemRequest());
		
		notebookResponse = new Vector<NotebookRequest>();
		for (int i=0; i<Global.dbThreadId; i++)
			notebookResponse.add(new NotebookRequest());

		tagResponse = new Vector<TagRequest>();
		for (int i=0; i<Global.dbThreadId; i++)
			tagResponse.add(new TagRequest());

		savedSearchResponse = new Vector<SavedSearchRequest>();
		for (int i=0; i<Global.dbThreadId; i++)
			savedSearchResponse.add(new SavedSearchRequest());

		noteResponse = new Vector<NoteRequest>();
		for (int i=0; i<Global.dbThreadId; i++)
			noteResponse.add(new NoteRequest());
		
		resourceResponse = new Vector<ResourceRequest>();
		for (int i=0; i<Global.dbThreadId; i++)
			resourceResponse.add(new ResourceRequest());
		
		noteTagsResponse = new Vector<NoteTagsRequest>();
		for (int i=0; i<Global.dbThreadId; i++)
			noteTagsResponse.add(new NoteTagsRequest());
		
		enSearchResponse = new Vector<EnSearchRequest>();
		for (int i=0; i<Global.dbThreadId; i++)
			enSearchResponse.add(new EnSearchRequest());
		
		watchFolderResponse = new Vector<WatchFolderRequest>();
		for (int i=0; i<Global.dbThreadId; i++)
			watchFolderResponse.add(new WatchFolderRequest());
		
		wordResponse = new Vector<WordRequest>();
		for (int i=0; i<Global.dbThreadId; i++)
			wordResponse.add(new WordRequest());

		invalidXMLResponse = new Vector<InvalidXMLRequest>();
		for (int i=0; i<Global.dbThreadId; i++)
			invalidXMLResponse.add(new InvalidXMLRequest());
		
		syncResponse = new Vector<SyncRequest>();
		for (int i=0; i<Global.dbThreadId; i++)
			syncResponse.add(new SyncRequest());

		dbSignal = new DBRunnerSignal();

                logger = new ApplicationLogger("dbrunner.log");

                conn = new RDatabaseConnection(logger);
                conn.dbSetup(url, userid, userPassword, cypherPassword);
	}

	
	public void run() {
		thread().setPriority(Thread.NORM_PRIORITY);
		
		
		dbSignal.start.connect(this, "releaseThread()");
	
		keepRunning=true;
		while(keepRunning) {
			try {
				DBRunnerRequest request;
				//System.gc();
				request = workQueue.take();
				logger.log(logger.EXTREME, "Work pulled.  Current size is now " +workQueue.size());
				prioritizeWork(request); 
				
				// Now the queue should be empty.  
				// Try to do work
				while (user.size() > 0 || background.size() > 0 || discretionary.size() > 0) {
					if (workQueue.size() > 0)
						emptyQueue();
					if (user.size() > 0)
						releaseWork(user);
					else
						if (background.size() > 0)
							releaseWork(background);
						else
							releaseWork(discretionary);
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		return;
	}
	
	@SuppressWarnings("unused")
	private void releaseThread() {
		Global.dbContinue();
	}
	
	private void emptyQueue() {
		logger.log(logger.EXTREME, "Draining queue.  Current size " +workQueue.size());
		int count = 0;
		while (workQueue.size() > 0 && count > MAX_EMPTY_QUEUE_COUNT) {
			DBRunnerRequest request;
			try {
				request = workQueue.take();
				prioritizeWork(request);
			} catch (InterruptedException e) {}
			count++;
			logger.log(logger.EXTREME, "Draining queue - counter is now " +count);
		}
	}
	
	private void releaseWork(Vector<DBRunnerRequest> queue) {
		logger.log(logger.EXTREME, "Releasing work.  Current size is " +workQueue.size());
		while(queue.size() > 0) {
			DBRunnerRequest request = queue.get(0);
			queue.remove(0);
			doWork(request);
		}
		logger.log(logger.EXTREME, "Leaving release queue.");
	}

	private void prioritizeWork(DBRunnerRequest request) {
		logger.log(logger.EXTREME, "Entering prioritizeWork()");
		if (request.requestor_id == Global.mainThreadId || 
				(request.category == DBRunnerRequest.NOTE) && (request.type == NoteRequest.Set_Index_Needed)) {
			user.add(request);
			return;
		} 
		if (request.requestor_id == Global.tagCounterThreadId) {
			background.add(request);
			return;
		}
		if (request.requestor_id == Global.trashCounterThreadId) {
			discretionary.add(request);
			return;
		}
		
		// Anything remaining should be index threads, put
		// them in a low priority
		discretionary.add(request);
		logger.log(logger.EXTREME, "Leaving prioritizeWork()");
   	}
	
	
	public synchronized void addWork(DBRunnerRequest s) {
		while(!workQueue.offer(s));
	}
	
	private void doWork(DBRunnerRequest s) {
		if (s.category == DBRunnerRequest.GENERIC){
			String work = new String(s.request);
			if (work.equalsIgnoreCase("shutdown"))
				keepRunning = false;
		} else 	if (s.category == DBRunnerRequest.DATABASE){
			DatabaseRequest work = copyDatabaseRequest(s);
			doDatabaseRequest(work);
		} else if (s.category == DBRunnerRequest.DELETED_ITEM) {
			DeletedItemRequest work = copyDeletedItemRequest(s);
			Global.dbrunnerWorkLock.unlock();	
			doDeletedItemRequest(work);
		} else if (s.category == DBRunnerRequest.NOTEBOOK) {
			NotebookRequest work = copyNotebookRequest(s);
			doNotebookRequest(work);
		} else if (s.category == DBRunnerRequest.TAG	) {
			TagRequest work = copyTagRequest(s);
			doTagRequest(work);
		} else if (s.category == DBRunnerRequest.SAVED_SEARCH	) {
			SavedSearchRequest work = copySavedSearchRequest(s);
			doSavedSearchRequest(work);
		} else if (s.category == DBRunnerRequest.NOTE) {
			NoteRequest work = copyNoteRequest(s);
			doNoteRequest(work);
		} else if (s.category == DBRunnerRequest.RESOURCE) {
			ResourceRequest work = copyResourceRequest(s);
			doResourceRequest(work);
		} else if (s.category == DBRunnerRequest.NOTE_TAGS) {
			NoteTagsRequest work = copyNoteTagsRequest(s);
			doNoteTagsRequest(work);
		} else if (s.category == DBRunnerRequest.ENSEARCH) {
			EnSearchRequest work = copyEnSearchRequest(s);
			doEnSearchRequest(work);
		} else if (s.category == DBRunnerRequest.WATCH_FOLDER) {
			WatchFolderRequest work = copyWatchFolderRequest(s);
			doWatchFolderRequest(work);
		} else if (s.category == DBRunnerRequest.WORD) {
			WordRequest work = copyWordRequest(s);
			doWordRequest(work);
		} else if (s.category == DBRunnerRequest.Invalid_XML) {
			InvalidXMLRequest work = copyInvalidXMLRequest(s);
			doInvalidXMLRequest(work);
		}	else if (s.category == DBRunnerRequest.Sync) {
				SyncRequest work = copySyncRequest(s);
				doSyncRequest(work);
		}	
		return;
	}
	
	
	//*********************************************
	//* If the requestor is expecting a response, *
	//* release them so they can pick it up.      *
	//*********************************************
	private void release(int i) {
		logger.log(logger.EXTREME, "Releasing "+i);
		Global.dbClientContinue(i);
//		if (i == Global.mainThreadId)
//			Global.mainThreadWait.wakeOne();
	}
	
	//**************************************
	//* Copy items off the work queue so   *
	//* the resources can be freed up.     *
	//**************************************
	private DatabaseRequest copyDatabaseRequest(DBRunnerRequest n) {
		DatabaseRequest request = new DatabaseRequest();
		DatabaseRequest old = (DatabaseRequest) n;
		request = old.copy();
		return request;
	}
	private SyncRequest copySyncRequest(DBRunnerRequest n) {
		SyncRequest request = new SyncRequest();
		SyncRequest old = (SyncRequest) n;
		request = old.copy();
		return request;
	}
	private DeletedItemRequest copyDeletedItemRequest(DBRunnerRequest r) {
		DeletedItemRequest request = new DeletedItemRequest();
		DeletedItemRequest old = (DeletedItemRequest) r;
		request = old.copy();
		return request;
	}	
	private NotebookRequest copyNotebookRequest(DBRunnerRequest n) {
		NotebookRequest request = new NotebookRequest();
		NotebookRequest old = (NotebookRequest) n;
		request = old.copy();
		return request;
	}
	private TagRequest copyTagRequest(DBRunnerRequest n) {
		TagRequest request = new TagRequest();
		TagRequest old = (TagRequest) n;
		request = old.copy();
		return request;
	}
	private SavedSearchRequest copySavedSearchRequest(DBRunnerRequest n) {
		SavedSearchRequest request = new SavedSearchRequest();
		SavedSearchRequest old = (SavedSearchRequest) n;
		request = old.copy();
		return request;
	}
	private NoteRequest copyNoteRequest(DBRunnerRequest n) {
		NoteRequest request = new NoteRequest();
		NoteRequest old = (NoteRequest) n;
		request = old.copy();
		return request;
	}
	private ResourceRequest copyResourceRequest(DBRunnerRequest n) {
		ResourceRequest request = new ResourceRequest();
		ResourceRequest old = (ResourceRequest) n;
		request = old.copy();
		return request;
	}
	private NoteTagsRequest copyNoteTagsRequest(DBRunnerRequest n) {
		NoteTagsRequest request = new NoteTagsRequest();
		NoteTagsRequest old = (NoteTagsRequest) n;
		request = old.copy();
		return request;
	}
	private EnSearchRequest copyEnSearchRequest(DBRunnerRequest n) {
		EnSearchRequest request = new EnSearchRequest();
		EnSearchRequest old = (EnSearchRequest) n;
		request = old.copy();
		return request;
	}
	private WatchFolderRequest copyWatchFolderRequest(DBRunnerRequest n) {
		WatchFolderRequest request = new WatchFolderRequest();
		WatchFolderRequest old = (WatchFolderRequest) n;
		request = old.copy();
		return request;
	}
	private WordRequest copyWordRequest(DBRunnerRequest n) {
		WordRequest request = new WordRequest();
		WordRequest old = (WordRequest) n;
		request = old.copy();
		return request;
	}
	private InvalidXMLRequest copyInvalidXMLRequest(DBRunnerRequest n) {
		InvalidXMLRequest request = new InvalidXMLRequest();
		InvalidXMLRequest old = (InvalidXMLRequest) n;
		request = old.copy();
		return request;
	}	
	//**************************************
	//* Database requests                  *
	//**************************************
	private void doDatabaseRequest(DatabaseRequest r) {
		if (r.type == DatabaseRequest.Create_Tables) {
			conn.createTables();
			release(r.requestor_id);
			return;
		} else if (r.type == DatabaseRequest.Drop_Tables) {
			conn.dropTables();
			return;
		} else if (r.type == DatabaseRequest.Compact) {
			conn.compactDatabase();
			release(r.requestor_id);
			return;
		} else if (r.type == DatabaseRequest.Shutdown) {
			conn.dbShutdown();
			keepRunning = false;
			return;
		} else if (r.type == DatabaseRequest.Execute_Sql) {
			conn.executeSql(r.string1);
			release(r.requestor_id);
			return;
		}  else if (r.type == DatabaseRequest.Execute_Sql_Index) {
			release(r.requestor_id);
			return;
		}  else if (r.type == DatabaseRequest.Backup_Database) {
			conn.backupDatabase();
			release(r.requestor_id);
			return;
		}
		return;
	}
	
	//**************************************
	//* Notebook database requests         *
	//**************************************
	private void doNotebookRequest(NotebookRequest n) {
		logger.log(logger.EXTREME, "Notebook request: " +n.category + " " + n.type + " from " +n.requestor_id);
		if (n.type == NotebookRequest.Create_Table) {
			conn.getNotebookTable().createTable();
		}
		if (n.type == NotebookRequest.Drop_Table) {
			conn.getNotebookTable().dropTable();
		}
		if (n.type == NotebookRequest.Expunge_Notebook) {
			conn.getNotebookTable().expungeNotebook(n.string1, n.bool1);
		}
		if (n.type == NotebookRequest.Add_Notebook) {
			conn.getNotebookTable().addNotebook(n.notebook, n.bool1, n.bool2);
		}
		if (n.type == NotebookRequest.Update_Notebook) {
			conn.getNotebookTable().updateNotebook(n.notebook, n.bool1);
		}
		if (n.type == NotebookRequest.Sync_Notebook) {
			conn.getNotebookTable().syncNotebook(n.notebook, n.bool1);
		}
		if (n.type == NotebookRequest.Get_All) {
			notebookResponse.get(n.requestor_id).responseNotebooks = conn.getNotebookTable().getAll();
			release(n.requestor_id);
		}
		if (n.type == NotebookRequest.Get_All_Local) {
			notebookResponse.get(n.requestor_id).responseNotebooks = conn.getNotebookTable().getAllLocal();
			release(n.requestor_id);
		}
		if (n.type == NotebookRequest.Get_All_Archived) {
			notebookResponse.get(n.requestor_id).responseNotebooks = conn.getNotebookTable().getAllArchived();
			release(n.requestor_id);
		}
		if (n.type == NotebookRequest.Get_Dirty) {
			notebookResponse.get(n.requestor_id).responseNotebooks = conn.getNotebookTable().getDirty();
			release(n.requestor_id);
		}
		if (n.type == NotebookRequest.Set_Archived) {
			conn.getNotebookTable().setArchived(n.string1, n.bool1);
		}
		if (n.type == NotebookRequest.Is_Notebook_Local) {
			notebookResponse.get(n.requestor_id).responseBoolean = conn.getNotebookTable().isNotebookLocal(n.string1);
			release(n.requestor_id);
		}
		if (n.type == NotebookRequest.Reset_Dirty) {
			conn.getNotebookTable().resetDirtyFlag(n.string1);
		}
		if (n.type == NotebookRequest.Find_Note_By_Name) {
			notebookResponse.get(n.requestor_id).responseString = conn.getNotebookTable().findNotebookByName(n.string1);
			release(n.requestor_id);
		}
		if (n.type == NotebookRequest.Update_Notebook_Guid) {
			conn.getNotebookTable().updateNotebookGuid(n.string1, n.string2);
		}
		if (n.type == NotebookRequest.Update_Notebook_Sequence) {
			conn.getNotebookTable().updateNotebookSequence(n.string1, n.int1);
		}
		if (n.type == NotebookRequest.Notebook_Counts) {
			notebookResponse.get(n.requestor_id).responseCounts = conn.getNotebookTable().getNotebookCounts();
			release(n.requestor_id);
		}
		logger.log(logger.EXTREME, "End of Notebook request");
		return;
	}

	//******************************************
	//* Deleted (expunged) database requests   *
	//******************************************
	private void doDeletedItemRequest(DeletedItemRequest r) {
		logger.log(logger.EXTREME, "DeletedItem request: " +r.category + " " + r.type + " from " +r.requestor_id);
		if (r.type == DeletedItemRequest.Create_Table) {
			conn.getDeletedTable().createTable();
		} else 	if (r.type == DeletedItemRequest.Drop_Table) {
			conn.getDeletedTable().dropTable();
		} else 	if (r.type == DeletedItemRequest.Expunge_All) {
			conn.getDeletedTable().expungeAllDeletedRecords();
		} else 	if (r.type == DeletedItemRequest.Add_Deleted_Item) {
			conn.getDeletedTable().addDeletedItem(r.string1, r.string2);
		} else 	if (r.type == DeletedItemRequest.Get_All) {
			deletedItemResponse.get(r.requestor_id).responseDeletedRecords = conn.getDeletedTable().getAllDeleted();
			release(r.requestor_id);
		} else 	if (r.type == DeletedItemRequest.Expunge_Record) {
			conn.getDeletedTable().expungeDeletedItem(r.string1, r.string2);
			release(r.requestor_id);
		}
		logger.log(logger.EXTREME, "End Of DeletedItem request");
		return;
	}

	//******************************************
	//* Tag database requests                  *
	//******************************************
	private void doTagRequest(TagRequest r) {
		logger.log(logger.EXTREME, "Tag request: " +r.category + " " + r.type + " from " +r.requestor_id);
		if (r.type == TagRequest.Create_Table) {
			conn.getTagTable().createTable();
		} else if (r.type == TagRequest.Drop_Table) {
			conn.getTagTable().dropTable();
		} else if (r.type == TagRequest.Add_Tag) {
			conn.getTagTable().addTag(r.tag, r.bool1);
		} else if (r.type == TagRequest.Expunge_Tag) {
			conn.getTagTable().expungeTag(r.string1, r.bool1);
		} else if (r.type == TagRequest.Exists) {
			tagResponse.get(r.requestor_id).responseBool = conn.getTagTable().exists(r.string1);
			release(r.requestor_id);
		} else if (r.type == TagRequest.Find_Tag_By_Name) {
			tagResponse.get(r.requestor_id).responseString = conn.getTagTable().findTagByName(r.string1);
			release(r.requestor_id);
		} else if (r.type == TagRequest.Get_All) {
			tagResponse.get(r.requestor_id).responseTags = conn.getTagTable().getAll();
			release(r.requestor_id);
		} else if (r.type == TagRequest.Get_Dirty) {
			tagResponse.get(r.requestor_id).responseTags = conn.getTagTable().getDirty();
			release(r.requestor_id);
		} else if (r.type == TagRequest.Get_Tag) {
			tagResponse.get(r.requestor_id).responseTag = conn.getTagTable().getTag(r.string1);
			release(r.requestor_id);
		} else if (r.type == TagRequest.Reset_Dirty_Flag) {
			conn.getTagTable().resetDirtyFlag(r.string1);
		} else if (r.type == TagRequest.Save_Tags) {
			conn.getTagTable().saveTags(r.tags);
		} else if (r.type == TagRequest.Sync_Tag) {
			conn.getTagTable().syncTag(r.tag, r.bool1);
		} else if (r.type == TagRequest.Update_Parent) {
			conn.getTagTable().updateTagParent(r.string1, r.string2);
		} else if (r.type == TagRequest.Update_Tag) {
			conn.getTagTable().updateTag(r.tag, r.bool1);
		} else if (r.type == TagRequest.Update_Tag_Guid) {
			conn.getTagTable().updateTagGuid(r.string1, r.string2);
		} else if (r.type == TagRequest.Update_Tag_Sequence) {
			conn.getTagTable().updateTagSequence(r.string1, r.int1);
		} 
		logger.log(logger.EXTREME, "End of tag request");
		return;
	}

	//******************************************
	//* Saved Search database requests         *
	//******************************************
	private void doSavedSearchRequest(SavedSearchRequest r) {
		logger.log(logger.EXTREME, "Saved Search request: " +r.category + " " + r.type + " from " +r.requestor_id);
		if (r.type == SavedSearchRequest.Create_Table) {
			conn.getSavedSearchTable().createTable();
		} else 	if (r.type == SavedSearchRequest.Drop_Table) {
			conn.getSavedSearchTable().dropTable();
		} else 	if (r.type == SavedSearchRequest.Add_Saved_Search) {
			conn.getSavedSearchTable().addSavedSearch(r.savedSearch, r.bool1);
		} else 	if (r.type == SavedSearchRequest.Exists) {
			savedSearchResponse.get(r.requestor_id).responseBoolean = conn.getSavedSearchTable().exists(r.string1);
			release(r.requestor_id);
		} else 	if (r.type == SavedSearchRequest.Expunge_Saved_Search) {
			conn.getSavedSearchTable().expungeSavedSearch(r.string1, r.bool1);
		} else 	if (r.type == SavedSearchRequest.Find_Saved_Search_By_Name) {
			savedSearchResponse.get(r.requestor_id).responseString = conn.getSavedSearchTable().findSavedSearchByName(r.string1);
			release(r.requestor_id);
		} else 	if (r.type == SavedSearchRequest.Get_All) {
			savedSearchResponse.get(r.requestor_id).responseSavedSearches = conn.getSavedSearchTable().getAll();
			release(r.requestor_id);
		} else 	if (r.type == SavedSearchRequest.Get_Dirty) {
			savedSearchResponse.get(r.requestor_id).responseSavedSearches = conn.getSavedSearchTable().getDirty();
			release(r.requestor_id);
		} else 	if (r.type == SavedSearchRequest.Get_Saved_Search) {
			savedSearchResponse.get(r.requestor_id).responseSavedSearch = conn.getSavedSearchTable().getSavedSearch(r.string1);
			release(r.requestor_id);
		} else 	if (r.type == SavedSearchRequest.Sync_Saved_Search) {
			conn.getSavedSearchTable().syncSavedSearch(r.savedSearch, r.bool1);
		} else 	if (r.type == SavedSearchRequest.Update_Saved_Search) {
			conn.getSavedSearchTable().updateSavedSearch(r.savedSearch, r.bool1);
		} else if (r.type == SavedSearchRequest.Reset_Dirty_Flag) {
			conn.getSavedSearchTable().resetDirtyFlag(r.string1);
		} 
		logger.log(logger.EXTREME, "End of saved search request");
		return;
	}

	//******************************************
	//* Saved Search database requests         *
	//******************************************
	private void doNoteRequest(NoteRequest r) {
		logger.log(logger.EXTREME, "Note request: " +r.category + " " + r.type + " from " +r.requestor_id);
		if (r.type == NoteRequest.Create_Table) {
			conn.getNoteTable().createTable();
		} else 	if (r.type == NoteRequest.Drop_Table) {
			conn.getNoteTable().dropTable();
		} else 	if (r.type == NoteRequest.Get_Note) {
			noteResponse.get(r.requestor_id).responseNote = conn.getNoteTable().getNote(r.string1, r.bool1, r.bool2, r.bool3, r.bool4, r.bool5);
			logger.log(logger.EXTREME, "Releasing " +r.requestor_id);
			release(r.requestor_id);
		} else 	if (r.type == NoteRequest.Add_Note) {
			conn.getNoteTable().addNote(r.note, r.bool1);
		} else 	if (r.type == NoteRequest.Update_Note_Title) {
			conn.getNoteTable().updateNoteTitle(r.string1, r.string2);
		} else 	if (r.type == NoteRequest.Update_Note_Creation_Date) {
			conn.getNoteTable().updateNoteCreatedDate(r.string1, r.date);
		} else 	if (r.type == NoteRequest.Update_Note_Altered_Date) {
			conn.getNoteTable().updateNoteAlteredDate(r.string1, r.date);
		} else 	if (r.type == NoteRequest.Update_Note_Subject_Date) {
			conn.getNoteTable().updateNoteSubjectDate(r.string1, r.date);
		} else 	if (r.type == NoteRequest.Update_Note_Source_Url) {
			conn.getNoteTable().updateNoteSourceUrl(r.string1, r.string2);
		} else 	if (r.type == NoteRequest.Update_Note_Author) {
			conn.getNoteTable().updateNoteAuthor(r.string1, r.string2);
		} else 	if (r.type == NoteRequest.Update_Note_Notebook) {
			conn.getNoteTable().updateNoteNotebook(r.string1, r.string2, r.bool1);
		} else 	if (r.type == NoteRequest.Update_Note_Content) {
			conn.getNoteTable().updateNoteContent(r.string1, r.string2);
		} else 	if (r.type == NoteRequest.Delete_Note) {
			conn.getNoteTable().deleteNote(r.string1);
		} else 	if (r.type == NoteRequest.Restore_Note) {
			conn.getNoteTable().restoreNote(r.string1);
		} else 	if (r.type == NoteRequest.Expunge_Note) {
			conn.getNoteTable().expungeNote(r.string1, r.bool1, r.bool2);
		} else 	if (r.type == NoteRequest.Expunge_All_Deleted_Notes) {
			conn.getNoteTable().expungeAllDeletedNotes();
		} else 	if (r.type == NoteRequest.Update_Note_Sequence) {
			conn.getNoteTable().updateNoteSequence(r.string1, r.int1);
		} else 	if (r.type == NoteRequest.Update_Note_Guid) {
			conn.getNoteTable().updateNoteGuid(r.string1, r.string2);
		} else 	if (r.type == NoteRequest.Update_Note) {
			conn.getNoteTable().updateNote(r.note, r.bool1);
		} else 	if (r.type == NoteRequest.Exists) {
			noteResponse.get(r.requestor_id).responseBoolean =conn.getNoteTable().exists(r.string1);
			release(r.requestor_id);
		} else 	if (r.type == NoteRequest.Sync_Note) {
			conn.getNoteTable().syncNote(r.note, r.bool1);
		} else 	if (r.type == NoteRequest.Get_Dirty) {
			noteResponse.get(r.requestor_id).responseNotes = conn.getNoteTable().getDirty();
			release(r.requestor_id);
		} else 	if (r.type == NoteRequest.Get_Unsynchronized_Guids) {
			noteResponse.get(r.requestor_id).responseStrings = conn.getNoteTable().getUnsynchronizedGUIDs();
			release(r.requestor_id);
		} else 	if (r.type == NoteRequest.Reset_Dirty_Flag) {
			conn.getNoteTable().resetDirtyFlag(r.string1);
		} else 	if (r.type == NoteRequest.Get_All_Guids) {
			noteResponse.get(r.requestor_id).responseStrings = conn.getNoteTable().getAllGuids();
			release(r.requestor_id);
		} else 	if (r.type == NoteRequest.Get_All_Notes) {
			noteResponse.get(r.requestor_id).responseNotes = conn.getNoteTable().getAllNotes();
			release(r.requestor_id);
		} else 	if (r.type == NoteRequest.Get_Unindexed_Count) {
			noteResponse.get(r.requestor_id).responseInt = conn.getNoteTable().getUnindexedCount();
			release(r.requestor_id);
		} else 	if (r.type == NoteRequest.Get_Dirty_Count) {
			noteResponse.get(r.requestor_id).responseInt = conn.getNoteTable().getDirtyCount();
			logger.log(logger.EXTREME, "Note request: " +r.category + " " + r.type + " from " +r.requestor_id);
			release(r.requestor_id);
		} else 	if (r.type == NoteRequest.Update_Resource_Guid_By_Hash) {
			conn.getNoteTable().updateNoteResourceGuidbyHash(r.string1, r.string2, r.string3);
		} else 	if (r.type == NoteRequest.Set_Index_Needed) {
			conn.getNoteTable().setIndexNeeded(r.string1, r.bool1);
		} else 	if (r.type == NoteRequest.Reindex_All_Notes) {
			conn.getNoteTable().reindexAllNotes();
		} else 	if (r.type == NoteRequest.Get_Unindexed) {
			noteResponse.get(r.requestor_id).responseStrings = conn.getNoteTable().getUnindexed();
			release(r.requestor_id);
		} else 	if (r.type == NoteRequest.Get_Next_Unindexed) {
			noteResponse.get(r.requestor_id).responseStrings = conn.getNoteTable().getNextUnindexed(r.int1);
			release(r.requestor_id);
		} else 	if (r.type == NoteRequest.Update_Resource_Content_Hash) {
			conn.getNoteTable().updateResourceContentHash(r.string1, r.string2, r.string3);
		} else  if (r.type == NoteRequest.Get_Note_Count) {
			noteResponse.get(r.requestor_id).responseInt = conn.getNoteTable().getNoteCount();
			release(r.requestor_id);
		} else if (r.type == NoteRequest.Reset_Note_Sequence) {
			conn.getNoteTable().resetNoteSequence(r.string1);
		} else if (r.type == NoteRequest.Get_Deleted_Count) {
			noteResponse.get(r.requestor_id).responseInt = conn.getNoteTable().getDeletedCount();
			release(r.requestor_id);
		} else if (r.type == NoteRequest.Is_Note_Dirty) {
			noteResponse.get(r.requestor_id).responseBoolean = conn.getNoteTable().isNoteDirty(r.string1);
			release(r.requestor_id);
		} else if (r.type == NoteRequest.Get_Note_Content_Binary) {
			noteResponse.get(r.requestor_id).responseString = conn.getNoteTable().getNoteContentBinary(r.string1);
			release(r.requestor_id);
		} else if (r.type == NoteRequest.Get_Title_Colors) {
			noteResponse.get(r.requestor_id).responsePair = conn.getNoteTable().getNoteTitleColors();
			release(r.requestor_id);
		} else 	if (r.type == NoteRequest.Set_Title_Colors) {
			conn.getNoteTable().setNoteTitleColor(r.string1, r.int1);
		} else if (r.type == NoteRequest.Get_Thumbnail) {
			noteResponse.get(r.requestor_id).responseBytes = conn.getNoteTable().getThumbnail(r.string1);
			release(r.requestor_id);
		} else if (r.type == NoteRequest.Is_Thumbail_Needed) {
			noteResponse.get(r.requestor_id).responseBoolean = conn.getNoteTable().isThumbnailNeeded(r.string1);
			release(r.requestor_id);
		} else if (r.type == NoteRequest.Set_Thumbnail_Needed) {
			conn.getNoteTable().setThumbnailNeeded(r.string1, r.bool1);
		}  else if (r.type == NoteRequest.Set_Thumbnail) {
			conn.getNoteTable().setThumbnail(r.string1, r.bytes);
		} 
		logger.log(logger.EXTREME, "End of Note request");
		return;
	}

	//******************************************
	//* Note Resource database requests        *
	//******************************************
	private void doResourceRequest(ResourceRequest r) {
		logger.log(logger.EXTREME, "Resource request: " +r.category + " " + r.type);
		if (r.type == ResourceRequest.Create_Table) {
			conn.getNoteTable().noteResourceTable.createTable();
		} else if (r.type == ResourceRequest.Drop_Table) {
			conn.getNoteTable().noteResourceTable.dropTable();
		} if (r.type == ResourceRequest.Expunge_Note_Resource) {
			conn.getNoteTable().noteResourceTable.expungeNoteResource(r.string1);
		} else if (r.type == ResourceRequest.Get_Next_Unindexed) {
			resourceResponse.get(r.requestor_id).responseStrings = conn.getNoteTable().noteResourceTable.getNextUnindexed(r.int1);
			release(r.requestor_id);
		} else if (r.type == ResourceRequest.Get_Note_Resource) {
			resourceResponse.get(r.requestor_id).responseResource = conn.getNoteTable().noteResourceTable.getNoteResource(r.string1, r.bool1);
			release(r.requestor_id);
		} else if (r.type == ResourceRequest.Get_Note_Resource_Data_Body_By_Hash_Hex) {
			resourceResponse.get(r.requestor_id).responseResource = conn.getNoteTable().noteResourceTable.getNoteResourceDataBodyByHashHex(r.string1, r.string2);
			release(r.requestor_id);
		} else if (r.type == ResourceRequest.Get_Note_Resource_Guid_By_Hash_Hex) {
			resourceResponse.get(r.requestor_id).responseString = conn.getNoteTable().noteResourceTable.getNoteResourceGuidByHashHex(r.string1, r.string2);
			release(r.requestor_id);
		} else if (r.type == ResourceRequest.Get_Note_Resource_Recognition) {
			resourceResponse.get(r.requestor_id).responseResource = conn.getNoteTable().noteResourceTable.getNoteResourceRecognition(r.string1);
			release(r.requestor_id);
		} else if (r.type == ResourceRequest.Get_Note_Resources) {
			resourceResponse.get(r.requestor_id).responseResources = conn.getNoteTable().noteResourceTable.getNoteResources(r.string1, r.bool1);
			release(r.requestor_id);
		} else if (r.type == ResourceRequest.Get_Note_Resources_Recognition) {
			resourceResponse.get(r.requestor_id).responseResources = conn.getNoteTable().noteResourceTable.getNoteResourcesRecognition(r.string1);
			release(r.requestor_id);
		} else if (r.type == ResourceRequest.Get_Resource_Count) {
			resourceResponse.get(r.requestor_id).responseInteger = conn.getNoteTable().noteResourceTable.getResourceCount();
			release(r.requestor_id);
		} else if (r.type == ResourceRequest.Reindex_All) {
			conn.getNoteTable().noteResourceTable.reindexAll();
		} else if (r.type == ResourceRequest.Reset_Dirty_Flag) {
			conn.getNoteTable().noteResourceTable.resetDirtyFlag(r.string1);
		} else if (r.type == ResourceRequest.Save_Note_Resource) {
			conn.getNoteTable().noteResourceTable.saveNoteResource(r.resource, r.bool1);
		} else if (r.type == ResourceRequest.Set_Index_Needed) {
			conn.getNoteTable().noteResourceTable.setIndexNeeded(r.string1, r.bool1);
		} else if (r.type == ResourceRequest.Update_Note_Resource) {
			conn.getNoteTable().noteResourceTable.updateNoteResource(r.resource, r.bool1);
		} else if (r.type == ResourceRequest.Update_Note_Resource_Guid) {
			conn.getNoteTable().noteResourceTable.updateNoteResourceGuid(r.string1, r.string2, r.bool1);
		} else if (r.type == ResourceRequest.Reset_Update_Sequence_Number) {
			conn.getNoteTable().noteResourceTable.resetUpdateSequenceNumber(r.string1, r.bool1);
		}
		logger.log(logger.EXTREME, "End of note resource request");
	}


	//******************************************
	//* Note Resource database requests        *
	//******************************************
	private void doNoteTagsRequest(NoteTagsRequest r) {
		logger.log(logger.EXTREME, "Note Tags request: " +r.category + " " + r.type + " from " +r.requestor_id);
		if (r.type == NoteTagsRequest.Create_Table) {
			conn.getNoteTable().noteTagsTable.createTable();
		} else if (r.type == NoteTagsRequest.Drop_Table) {
			conn.getNoteTable().noteTagsTable.dropTable();
		}  else if (r.type == NoteTagsRequest.Check_Note_Note_Tags) {
			noteTagsResponse.get(r.requestor_id).responseBoolean = conn.getNoteTable().noteTagsTable.checkNoteNoteTags(r.string1, r.string2);
			release(r.requestor_id);
		} if (r.type == NoteTagsRequest.Delete_Note_Tag) {
			conn.getNoteTable().noteTagsTable.deleteNoteTag(r.string1);
		} if (r.type == NoteTagsRequest.Get_All_Note_Tags) {
			noteTagsResponse.get(r.requestor_id).responseNoteTagsRecord = conn.getNoteTable().noteTagsTable.getAllNoteTags();
			release(r.requestor_id);
		} if (r.type == NoteTagsRequest.Get_Note_Tags) {
			noteTagsResponse.get(r.requestor_id).responseStrings = conn.getNoteTable().noteTagsTable.getNoteTags(r.string1);
			release(r.requestor_id);
		} if (r.type == NoteTagsRequest.Save_Note_Tag) {
			conn.getNoteTable().noteTagsTable.saveNoteTag(r.string1, r.string2);
		} 
		if (r.type == NoteTagsRequest.Tag_Counts) {
			noteTagsResponse.get(r.requestor_id).responseCounts = conn.getNoteTable().noteTagsTable.getTagCounts();
			release(r.requestor_id);
		} 
		logger.log(logger.EXTREME, "End of note tags request");
	}


	//**************************************
	//* Search requests                    *
	//**************************************
	private void doEnSearchRequest(EnSearchRequest r) {
		logger.log(logger.EXTREME, "EnSearch request: " +r.category + " " + r.type + " from " +r.requestor_id);
		REnSearch s = new REnSearch(conn, logger, conn, r.string1, r.tags, r.int1, r.int2);
		enSearchResponse.get(r.requestor_id).responseNotes = s.matchWords();
		enSearchResponse.get(r.requestor_id).responseStrings = s.getWords();
		release(r.requestor_id);
		logger.log(logger.EXTREME, "End of EnSearch request");
		return;
	}
	
	
	
	//**************************************
	//* Watch Folders                      *
	//**************************************
	private void doWatchFolderRequest(WatchFolderRequest r) {
		logger.log(logger.EXTREME, "Watch folder request: " +r.category + " " + r.type + " from " +r.requestor_id);
		if (r.type == WatchFolderRequest.Create_Tables) {
			conn.getWatchFolderTable().createTable();
		} else if (r.type == WatchFolderRequest.Add_Watch_Folder) {
			conn.getWatchFolderTable().addWatchFolder(r.string1, r.string2, r.bool1, r.int1);
		} else if (r.type == WatchFolderRequest.Get_All) {
			watchFolderResponse.get(r.requestor_id).responseWatchFolders = conn.getWatchFolderTable().getAll();
			release(r.requestor_id);
		}
		else if (r.type == WatchFolderRequest.Get_Notebook) {
			watchFolderResponse.get(r.requestor_id).responseString = conn.getWatchFolderTable().getNotebook(r.string1);
			release(r.requestor_id);
		} else if (r.type == WatchFolderRequest.Drop_Tables) {
			conn.getWatchFolderTable().dropTable();
		} else if (r.type == WatchFolderRequest.Expunge_Folder) {
			conn.getWatchFolderTable().expungeWatchFolder(r.string1);
		} else if (r.type == WatchFolderRequest.Expunge_All) {
			conn.getWatchFolderTable().expungeAll();
		}
		logger.log(logger.EXTREME, "End of watch folder request");
	}
	
	//**************************************
	//* Word Index                         *
	//**************************************
	private void doWordRequest(WordRequest r) {
		logger.log(logger.EXTREME, "Word request: " +r.category + " " + r.type + " from " +r.requestor_id);
		if (r.type == WordRequest.Create_Table) {
			conn.getWordsTable().createTable();
		} else if (r.type == WordRequest.Drop_Table) {
			conn.getWordsTable().dropTable();
		} else if (r.type == WordRequest.Expunge_From_Word_Index) {
			conn.getWordsTable().expungeFromWordIndex(r.string1, r.string2);
		} else if (r.type == WordRequest.Clear_Word_Index) {
			conn.getWordsTable().clearWordIndex();
		} else if (r.type == WordRequest.Get_Word_Count) {
			wordResponse.get(r.requestor_id).responseInt = conn.getWordsTable().getWordCount();
			release(r.requestor_id);
		} else if (r.type == WordRequest.Add_Word_To_Note_Index) {
			conn.getWordsTable().addWordToNoteIndex(r.string1, r.string2, r.string3, r.int1);
			release(r.requestor_id);
		} 
		logger.log(logger.EXTREME, "End of word request");
	}
	
	
	//**************************************
	//* Invalid XML                        *
	//**************************************
	private void doInvalidXMLRequest(InvalidXMLRequest r) {
		if (r.type == InvalidXMLRequest.Create_Table) {
			conn.getInvalidXMLTable().createTable();
			return;
		} else if (r.type == InvalidXMLRequest.Drop_Table) {
			conn.getInvalidXMLTable().dropTable();
			return;
		} else if (r.type == InvalidXMLRequest.Get_Invalid_Elements) {
			invalidXMLResponse.get(r.requestor_id).responseList = conn.getInvalidXMLTable().getInvalidElements();
			release(r.requestor_id);
			return;
		} else if (r.type == InvalidXMLRequest.Get_Invalid_Attributes) {
			invalidXMLResponse.get(r.requestor_id).responseArrayList = conn.getInvalidXMLTable().getInvalidAttributes(r.string1);
			release(r.requestor_id);
			return;
		} else if (r.type == InvalidXMLRequest.Get_Invalid_Attribute_Elements) {
			invalidXMLResponse.get(r.requestor_id).responseList = conn.getInvalidXMLTable().getInvalidAttributeElements();
			release(r.requestor_id);
			return;
		} else if (r.type == InvalidXMLRequest.Add_Invalid_Element) {
			conn.getInvalidXMLTable().addElement(r.string1);
			return;
		} else if (r.type == InvalidXMLRequest.Add_Invalid_Attribute) {
			conn.getInvalidXMLTable().addAttribute(r.string1, r.string2);
			return;
		} 
	}
	
	
	//**************************************
	//* Sync database                      *
	//**************************************
	private void doSyncRequest(SyncRequest r) {
		if (r.type == SyncRequest.Create_Table) {
			conn.getInvalidXMLTable().createTable();
			return;
		} else if (r.type == SyncRequest.Drop_Table) {
			conn.getInvalidXMLTable().dropTable();
			return;
		} else if (r.type == SyncRequest.Get_Record) {
			syncResponse.get(r.requestor_id).responseValue = conn.getSyncTable().getRecord(r.key);
			release(r.requestor_id);
			return;
		}  else if (r.type == SyncRequest.Set_Record) {
			conn.getSyncTable().setRecord(r.key,r.value);
			return;
		} 
	}
}