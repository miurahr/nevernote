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


package cx.fbn.nevernote.sql;

import java.util.ArrayList;
import java.util.List;

import cx.fbn.nevernote.sql.driver.NSqlQuery;
import cx.fbn.nevernote.utilities.ApplicationLogger;

public class WordsTable {
	private final ApplicationLogger 		logger;
	private final DatabaseConnection		db;

	
	// Constructor
	public WordsTable(ApplicationLogger l, DatabaseConnection d) {
		logger = l;
		db = d;
	}
	// Create the table
	public void createTable() {
		NSqlQuery query = new NSqlQuery(db.getIndexConnection());
        logger.log(logger.HIGH, "Creating table WORDS ...");
        if (!query.exec("create table words (word varchar, guid varchar, source varchar, weight int, primary key (word, guid, source));")) {
        	logger.log(logger.HIGH, "Table WORDS creation FAILED!!!");   
        	logger.log(logger.HIGH, query.lastError());
        }   
	}
	// Drop the table
	public void dropTable() {
		NSqlQuery query = new NSqlQuery(db.getIndexConnection());
		query.exec("drop table words");
	}
	// Count unindexed notes
	public int getWordCount() {
        NSqlQuery query = new NSqlQuery(db.getIndexConnection());
		query.exec("select count(*) from words");
		query.next(); 
		int returnValue = new Integer(query.valueString(0));
		return returnValue;
	}

	// Clear out the word index table
	public void clearWordIndex() {
		NSqlQuery query = new NSqlQuery(db.getIndexConnection());
        logger.log(logger.HIGH, "DELETE FROM WORDS");
        
        boolean check = query.exec("DELETE FROM WORDS");
        if (!check)
        	logger.log(logger.HIGH, "Table WORDS clear has FAILED!!!");  
	}	

	//********************************************************************************
	//********************************************************************************
	//* Support adding & deleting index words
	//********************************************************************************
	//********************************************************************************
	public void expungeFromWordIndex(String guid, String type) {
		NSqlQuery deleteWords = new NSqlQuery(db.getIndexConnection());
		if (!deleteWords.prepare("delete from words where guid=:guid and source=:source")) {
			logger.log(logger.EXTREME, "Note SQL select prepare deleteWords has failed.");
			logger.log(logger.MEDIUM, deleteWords.lastError());
		}
	
		deleteWords.bindValue(":guid", guid);
		deleteWords.bindValue(":source", type);
		deleteWords.exec();

	}
	// Expunge words
	public void expunge(String guid) {
		NSqlQuery deleteWords = new NSqlQuery(db.getIndexConnection());
		if (!deleteWords.prepare("delete from words where guid=:guid")) {
			logger.log(logger.EXTREME, "Word SQL select prepare expunge has failed.");
			logger.log(logger.MEDIUM, deleteWords.lastError());
		}
	
		deleteWords.bindValue(":guid", guid);
		deleteWords.exec();
	}
	// Reindex a note
	public synchronized void addWordToNoteIndex(String guid, String word, String type, Integer weight) {
		NSqlQuery findWords = new NSqlQuery(db.getIndexConnection());
		if (!findWords.prepare("Select weight from words where guid=:guid and source=:type and word=:word")) {
  			logger.log(logger.MEDIUM, "Prepare failed in addWordToNoteIndex()");
			logger.log(logger.MEDIUM, findWords.lastError());
		}
		
		findWords.bindValue(":guid", guid);
		findWords.bindValue(":type", type);
		findWords.bindValue(":word", word);
		
		boolean addNeeded = true;
		findWords.exec();
		// If we have a match, find out which has the heigher weight & update accordingly
		if (findWords.next()) {
			int recordWeight = new Integer(findWords.valueString(0));
			addNeeded = false;
			if (recordWeight < weight) {
				NSqlQuery updateWord = new NSqlQuery(db.getIndexConnection());
				if (!updateWord.prepare("Update words set weight=:weight where guid=:guid and source=:type and word=:word")) {
					logger.log(logger.MEDIUM, "Prepare failed for find words in addWordToNoteIndex()");
					logger.log(logger.MEDIUM, findWords.lastError());					
				}
				
				updateWord.bindValue(":weight", weight);
				updateWord.bindValue(":guid", guid);
				updateWord.bindValue(":type", type);
				updateWord.bindValue(":word",word);
				updateWord.exec();
			}
		}
		
		
		if (!addNeeded)
			return;
		
		NSqlQuery insertWords = new NSqlQuery(db.getIndexConnection());
		if (!insertWords.prepare("Insert Into Words (word, guid, weight, source)"
				+" Values(:word, :guid, :weight, :type )")) {
			logger.log(logger.EXTREME, "Note SQL select prepare checkWords has failed.");
			logger.log(logger.MEDIUM, insertWords.lastError());
		}
		insertWords.bindValue(":word", word);
		insertWords.bindValue(":guid", guid);
		insertWords.bindValue(":weight", weight);
		insertWords.bindValue(":type", type);
		if (!insertWords.exec()) {
			String err = insertWords.lastError();
			logger.log(logger.MEDIUM, "Error inserting words into index: " +err);
		}
	}

	// Get a list of GUIDs in the table
	public List<String> getGuidList() {
		NSqlQuery query = new NSqlQuery(db.getIndexConnection());
        logger.log(logger.HIGH, "gedGuidList()");
        
        boolean check = query.exec("Select distinct guid from words");
        if (!check)
        	logger.log(logger.HIGH, "Table WORDS select distinct guid has FAILED!!!");  
        
        List<String> guids = new ArrayList<String>();
        while (query.next()) {
        	guids.add(query.valueString(0));
        }
        return guids;
	}	

}
