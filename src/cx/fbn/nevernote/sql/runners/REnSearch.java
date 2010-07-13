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


package cx.fbn.nevernote.sql.runners;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.regex.Pattern;

import com.evernote.edam.type.Note;
import com.evernote.edam.type.Notebook;
import com.evernote.edam.type.Tag;

import cx.fbn.nevernote.sql.driver.NSqlQuery;
import cx.fbn.nevernote.utilities.ApplicationLogger;

public class REnSearch {
	
	private final List<String>	searchWords;
	private final List<String> 	notebooks;
	private final List<String> 	tags;
	private final List<String> 	intitle;
	private final List<String>	created;
	private final List<String>	updated;
	private final List<String> 	resource;
	private final List<String>	subjectDate;
	private final List<String>	longitude;
	private final List<String>	latitude;
	private final List<String>	altitude;
	private final List<String>	author;
	private final List<String>	source;
	private final List<String>	sourceApplication;
	private final List<String>	recoType;
	private final List<String>	todo;
	private final List<Tag>		tagIndex;
	private final ApplicationLogger logger;
	private final RDatabaseConnection db;
	private boolean any;
	private int	minimumWordLength = 3;
	private int minimumRecognitionWeight = 80;
	private final RDatabaseConnection conn;
	
	public REnSearch(RDatabaseConnection c, ApplicationLogger l, RDatabaseConnection d, String s, List<Tag> t, int m, int r) {
		db = d;
		logger = l;
		conn = c;
		tagIndex = t;
		minimumWordLength = m;
		minimumRecognitionWeight = r;
		searchWords = new ArrayList<String>();
		notebooks = new ArrayList<String>();
		tags = new ArrayList<String>();
		intitle = new ArrayList<String>();
		created = new  ArrayList<String>();
		updated = new ArrayList<String>();
		resource = new ArrayList<String>();
		subjectDate = new ArrayList<String>();
		longitude = new ArrayList<String>();
		latitude = new ArrayList<String>();
		altitude = new ArrayList<String>();
		author = new ArrayList<String>();
		source = new ArrayList<String>();
		sourceApplication = new ArrayList<String>();
		recoType = new ArrayList<String>();
		todo = new ArrayList<String>();
		any = false;
		
		if (s == null) 
			return;
		if (s.trim().equals(""))
			return;
		
		resolveSearch(s);
	}
		
	public List<String> getWords() { return searchWords; }
	public List<String> getNotebooks() { return notebooks; }
	public List<String> getIntitle() {	return intitle; }
	public List<String> getTags() {	return tags; }
	public List<String> getResource() {	return resource; }
	public List<String> getAuthor() { return author; }	
	public List<String> getSource() { return source; }	
	public List<String> getSourceApplication() { return sourceApplication; }	
	public List<String> getRecoType() {	return recoType; }	
	public List<String> getToDo() { return todo; }
	public List<String> getLongitude() { return longitude; }
	public List<String> getLatitude() { return latitude; }
	public List<String> getAltitude() { return altitude; }
	public List<String> getCreated() { return created; }
	public List<String> getUpdated() { return updated; }
	public List<String> getSubjectDate() { return subjectDate; }
	

	// match tag names
	private boolean matchTagsAll(List<String> tagNames) {
		List<String> list = getTags();
				
		for (int j=0; j<list.size(); j++) {
			boolean negative = false;
			negative = false;
			if (list.get(j).startsWith("-"))
				negative = true;
			int pos = list.get(j).indexOf(":");
			String filterName = cleanupWord(list.get(j).substring(pos+1));
			filterName = filterName.replace("*", ".*");   // setup for regular expression pattern match
			
			if (tagNames.size() == 0 && !negative)
				return false;
			if (tagNames.size() == 0 && negative)
				return true;
			
			for (int i=0; i<tagNames.size(); i++) {		
				boolean matches = Pattern.matches(filterName.toLowerCase(),tagNames.get(i).toLowerCase());
				if (matches && negative)
					return false;
				if (!matches && !negative)
					return false;
			}
		}
		return true;
	}
	
	// match tag names
	private boolean matchTagsAny(List<String> tagNames) {
		List<String> list = getTags();
		if (list.size() == 0)
			return true;
		
		boolean negative = false;		
		boolean found = false;
		
		for (int j=0; j<list.size(); j++) {
			negative = false;
			if (list.get(j).startsWith("-"))
				negative = true;
			int pos = list.get(j).indexOf(":");
			String filterName = cleanupWord(list.get(j).substring(pos+1));
			filterName = filterName.replace("*", ".*");   // setup for regular expression pattern match
			
			if (tagNames.size() == 0)
				found = false;

			for (int i=0; i<tagNames.size(); i++) {		
				boolean matches = Pattern.matches(filterName.toLowerCase(),tagNames.get(i).toLowerCase());
				if (matches)
					found = true;
			}
		}
		if (negative)
			return !found;
		else
			return found;
	}
	
	// Match notebooks in search terms against notes
	private boolean matchNotebook(String guid) {
		if (getNotebooks().size() == 0)
			return true;
		RNotebookTable bookTable = new RNotebookTable(logger, conn);
		List<Notebook> books = bookTable.getAll();

		String name = new String("");
		for (int i=0; i<books.size(); i++) {
			if (guid.equalsIgnoreCase(books.get(i).getGuid())) {
				name = books.get(i).getName();
				i=books.size();
			}
		}
		if (any)
			return matchListAny(getNotebooks(), name);
		else
			return matchListAll(getNotebooks(), name);
	}
	// Match notebooks in search terms against notes
	private boolean matchListAny(List<String> list, String title) {
		if (list.size() == 0)
			return true;
		boolean negative = false;
		boolean found = false;
		for (int i=0; i<list.size(); i++) {
			int pos = list.get(i).indexOf(":");
			negative = false;
			if (list.get(i).startsWith("-"))
				negative = true;
			String filterName = cleanupWord(list.get(i).substring(pos+1));
			filterName = filterName.replace("*", ".*");   // setup for regular expression pattern match
			boolean matches = Pattern.matches(filterName.toLowerCase(),title.toLowerCase());
			if (matches)
				found = true;
		}
		if (negative)
			return !found;
		else
			return found;
	}
	// Match notebooks in search terms against notes
	private boolean matchContentAny(Note n) {
		if (todo.size() == 0 && resource.size() == 0)
			return true;
		
		
		n = conn.getNoteTable().getNote(n.getGuid(), true, true, false, false, false);
		for (int i=0; i<todo.size(); i++) {
			String value = todo.get(i);
			value = value.replace("\"", "");
			boolean desiredState;
			if (!value.endsWith(":false") && !value.endsWith(":true") && !value.endsWith(":*") && !value.endsWith("*"))
				return false;
			if (value.endsWith(":false"))
				desiredState = false;
			else
				desiredState = true;
			if (value.startsWith("-"))
				desiredState = !desiredState;
			int pos = n.getContent().indexOf("<en-todo");
			if (pos == -1 && value.startsWith("-") && (value.endsWith("*") || value.endsWith(":")))
				return true;
			if (value.endsWith("*"))
				return true;
			while (pos > -1) {
				int endPos = n.getContent().indexOf("/>", pos);
				String segment = n.getContent().substring(pos, endPos);
				boolean currentState;
				if (segment.toLowerCase().indexOf("checked=\"true\"") == -1)
					currentState = false;
				else
					currentState = true;
				if (desiredState == currentState)
					return true;
				
				pos = n.getContent().indexOf("<en-todo", pos+1);
			}
		}
		
		// Check resources
		for (int i=0; i<resource.size(); i++) {
			String resourceString = resource.get(i);
			resourceString = resourceString.replace("\"", "");
			boolean negative = false;
			if (resourceString.startsWith("-"))
				negative = true;
			resourceString = resourceString.substring(resourceString.indexOf(":")+1);
			for (int j=0; j<n.getResourcesSize(); j++) {
				boolean match = stringMatch(n.getResources().get(j).getMime(), resourceString, negative);
				if (match)
					return true;
			}
		}
		return false;
	}
	
	
	// Take the initial search & split it apart
	private void resolveSearch(String search) {
		List<String> words = new ArrayList<String>();
		StringBuffer b = new StringBuffer(search);
		
		int len = search.length();
		char nextChar = ' ';
		boolean quote = false;
		for (int i=0; i<len; i++) {
			if (search.charAt(i)==nextChar && !quote) {
				b.setCharAt(i,'\0');
				nextChar = ' ';
			} else {
				if (search.charAt(i)=='\"') {
					if (!quote) {
						quote=true;
					} else {
						quote=false;
					}
				}
			}
			if (((i+2)<len) && search.charAt(i) == '\\') {
				i=i+2;
			}
		}
		
		search = b.toString();
		int pos = 0;
		for (int i=0; i<search.length(); i++) {
			if (search.charAt(i) == '\0') {
				search = search.substring(1);
				i=0;
			} else {
				pos = search.indexOf('\0');
				if (pos > 0) {
					words.add(search.substring(0,pos).toLowerCase());
					search = search.substring(pos);
					i=0;
				}
			}
		}
		if (search.charAt(0)=='\0')	
			words.add(search.substring(1).toLowerCase());
		else
			words.add(search.toLowerCase());
		parseTerms(words);
	}

	
	// Parse out individual words into separate lists
	// Supported options
	// Tags
	// Notebooks
	// Intitle
	// author
	// source
	// source application
	// created
	// updated
	// subject date

	private void parseTerms(List<String> words) {
		int minLen = minimumWordLength;
		
		for (int i=0; i<words.size(); i++) {
			String word = words.get(i);
			int pos = word.indexOf(":");
			if (word.startsWith("any:")) {
				any = true;
				word = word.substring(4).trim();
				pos = word.indexOf(":");
			}
			if (pos < 0 && (word.length() >= minLen || word.indexOf('*')>=0)) 
				getWords().add(word);
			if (word.startsWith("intitle:")) 
				intitle.add("*"+word+"*");
			if (word.startsWith("-intitle:")) 
				intitle.add("*"+word+"*");
			if (word.startsWith("notebook:")) 
				notebooks.add(word);
			if (word.startsWith("-notebook:")) 
				notebooks.add(word);
			if (word.startsWith("tag:")) 
				tags.add(word);
			if (word.startsWith("-tag:")) 
				tags.add(word);
			if (word.startsWith("resource:")) 
				resource.add(word);
			if (word.startsWith("-resource:")) 
				resource.add(word);
			if (word.startsWith("author:")) 
				author.add(word);
			if (word.startsWith("-author:")) 
				author.add(word);
			if (word.startsWith("source:")) 
				source.add(word);
			if (word.startsWith("-source:")) 
				source.add(word);
			if (word.startsWith("sourceapplication:")) 
				sourceApplication.add(word);
			if (word.startsWith("-sourceapplication:")) 
				sourceApplication.add(word);
			if (word.startsWith("recotype:")) 
				recoType.add(word);
			if (word.startsWith("-recotype:")) 
				recoType.add(word);
			if (word.startsWith("todo:")) 
				todo.add(word);
			if (word.startsWith("-todo:")) 
				todo.add(word);

			if (word.startsWith("latitude:")) 
				latitude.add(word);
			if (word.startsWith("-latitude:")) 
				latitude.add(word);
			if (word.startsWith("longitude:")) 
				longitude.add(word);
			if (word.startsWith("-longitude:")) 
				longitude.add(word);
			if (word.startsWith("altitude:")) 
				altitude.add(word);
			if (word.startsWith("-altitude:")) 
				altitude.add(word);

			if (word.startsWith("created:")) 
				created.add(word);
			if (word.startsWith("-created:")) 
				created.add(word);
			if (word.startsWith("updated:")) 
				updated.add(word);
			if (word.startsWith("-updated:")) 
				updated.add(word);
			if (word.startsWith("subjectdate:")) 
				created.add(word);
			if (word.startsWith("-subjectdate:")) 
				created.add(word);
		
		}
	}
	// Match notebooks in search terms against notes
	private boolean matchListAll(List<String> list, String title) {
		if (list.size() == 0)
			return true;
		boolean negative = false;
		for (int i=0; i<list.size(); i++) {
			int pos = list.get(i).indexOf(":");
			negative = false;
			if (list.get(i).startsWith("-"))
				negative = true;
			String filterName = cleanupWord(list.get(i).substring(pos+1));
			filterName = filterName.replace("*", ".*");   // setup for regular expression pattern match
			boolean matches = Pattern.matches(filterName.toLowerCase(),title.toLowerCase());
			if (matches && negative)
				return false;
			if (matches && !negative)
				return true;
		}
		if (negative)
			return true;
		else
			return false;
	}
	// Match notebooks in search terms against notes
	private boolean matchContentAll(Note n) {
		if (todo.size() == 0 && resource.size() == 0)
			return true;
		
		boolean returnTodo = false;
		boolean returnResource = false;
		
		if (todo.size() == 0)
			returnTodo = true;
		if (resource.size() == 0)
			returnResource = true;
		
		
		n = conn.getNoteTable().getNote(n.getGuid(), true, true, false, false, false);
		for (int i=0; i<todo.size(); i++) {
			String value = todo.get(i);
			value = value.replace("\"", "");
			boolean desiredState;
			if (!value.endsWith(":false") && !value.endsWith(":true") && !value.endsWith(":*") && !value.endsWith("*"))
				return false;
			if (value.endsWith(":false"))
				desiredState = false;
			else
				desiredState = true;
			if (value.startsWith("-"))
				desiredState = !desiredState;
			int pos = n.getContent().indexOf("<en-todo");
			if (pos == -1 && value.startsWith("-") && (value.endsWith("*") || value.endsWith(":")))
				return true;
			if (pos > -1 && value.startsWith("-") && (value.endsWith("*") || value.endsWith(":")))
				return false;
			if (pos == -1) 
 				return false;
			if (value.endsWith("*"))
				returnTodo = true;
			while (pos > -1) {
				int endPos = n.getContent().indexOf("/>", pos);
				String segment = n.getContent().substring(pos, endPos);
				boolean currentState;
				if (segment.toLowerCase().indexOf("checked=\"true\"") == -1)
					currentState = false;
				else
					currentState = true;
				if (desiredState == currentState)
					returnTodo = true;
				
				pos = n.getContent().indexOf("<en-todo", pos+1);
			}
		}
		
		// Check resources
		for (int i=0; i<resource.size(); i++) {
			String resourceString = resource.get(i);
			resourceString = resourceString.replace("\"", "");
			boolean negative = false;
			if (resourceString.startsWith("-"))
				negative = true;
			resourceString = resourceString.substring(resourceString.indexOf(":")+1);
			if (resourceString.equals(""))
				return false;
			for (int j=0; j<n.getResourcesSize(); j++) {
				boolean match = stringMatch(n.getResources().get(j).getMime(), resourceString, negative);
				if (!match)
					return false;
				returnResource = true;
			}
		}
		
		
		return returnResource && returnTodo;
	}
	
	private boolean stringMatch(String content, String text, boolean negative) {
		String regex;
		if (content == null && !negative)
			return false;
		if (content == null && negative)
			return true;
		
		if (text.endsWith("*")) {
			text = text.substring(0,text.length()-1);
			regex = text;
		} else {
			regex = text;
		}
		content = content.toLowerCase();
		regex = regex.toLowerCase();
		boolean matches = content.startsWith(regex);
		if (negative)
			return !matches;
		return matches;
	}
	
	// Remove odd strings from search terms
	private String cleanupWord(String word) {
		if (word.startsWith("\""))
			word = word.substring(1);
		if (word.endsWith("\""))
            word = word.substring(0,word.length()-1);
		word = word.replace("\\\"","\"");
		word = word.replace("\\\\","\\");
		
		return word;
	}

	
	// Match dates
	private boolean matchDatesAll(List<String> dates, long noteDate) {
		if (dates.size()== 0) 
			return true;
		
		boolean negative = false;
		for (int i=0; i<dates.size(); i++) {
			String requiredDate = dates.get(i);
			if (requiredDate.startsWith("-"))
				negative = true;
			
			int response = 0;
			requiredDate = requiredDate.substring(requiredDate.indexOf(":")+1);
			try {
				response = dateCheck(requiredDate, noteDate);
			} catch (java.lang.NumberFormatException e) {return false;}  {
				if (negative && response < 0)
					return false;
				if (!negative && response > 0)
					return false;
			}
		}
		return true;
	}
	private boolean matchDatesAny(List<String> dates, long noteDate) {
		if (dates.size()== 0) 
			return true;
		
		boolean negative = false;
		for (int i=0; i<dates.size(); i++) {
			String requiredDate = dates.get(i);
			if (requiredDate.startsWith("-"))
				negative = true;
			
			int response = 0;
			requiredDate = requiredDate.substring(requiredDate.indexOf(":")+1);
			try {
				response = dateCheck(requiredDate, noteDate);
			} catch (java.lang.NumberFormatException e) {return false;}  {
				if (negative && response > 0)
					return true;
				if (!negative && response < 0)
					return true;
			}
		}
		return false;
	}
	
	@SuppressWarnings("unused")
	private void printCalendar(Calendar calendar) {
		// define output format and print
		SimpleDateFormat sdf = new SimpleDateFormat("d MMM yyyy hh:mm:ss aaa");
		String date = sdf.format(calendar.getTime());
		System.err.print(date);
		calendar = new GregorianCalendar();
	}
	
	
	//****************************************
	//****************************************
	// Match search terms against notes
	//****************************************
	//****************************************
	public List<Note> matchWords() {
		logger.log(logger.EXTREME, "Inside EnSearch.matchWords()");
		
		StringBuffer buffer = new StringBuffer(100);
		Integer counter = 0;
		boolean subSelect = false;
		
		buffer.append("Select guid from Note ");
		if (searchWords.size() > 0) 
			subSelect = true;
		if (subSelect) {
			buffer.append(" where guid in ");
		
			// Build the query words
			String connector;
			if (any)
				connector = new String("or");
			else
				connector = new String("and");
			for (int i=0; i<getWords().size(); i++) {
				buffer.append("(Select distinct guid from words where ");
				buffer.append("weight >= :weight"+counter.toString() +" and ");
				if (getWords().get(i).indexOf("*")==-1)
					buffer.append("word=:word" +counter.toString());
				else
					buffer.append("word like :word" +counter.toString());
				counter++;
				buffer.append(") ");
				if (i < getWords().size() -1)
					buffer.append(" " +connector +" guid in ");
			}
		}
		
		NSqlQuery query = new NSqlQuery(db.getConnection());
		
		if (!query.prepare(buffer.toString()))
			logger.log(logger.HIGH, "EnSearch Sql Prepare Failed:" +query.lastError());
		
		if (subSelect) {
			// Do the binding
			Integer binder = 0;
			for (int i=0; i<getWords().size(); i++) {
				String val = getWords().get(i);
				val = val.replace('*', '%');
				query.bindValue(":weight"+binder.toString(), minimumRecognitionWeight);
				query.bindValue(":word"+binder.toString(), cleanupWord(val));
				binder++;
			}	
		}

		List<Note> guids = new ArrayList<Note>();
		RNoteTable noteTable = new RNoteTable(logger, conn);  
		if (!query.exec()) 
			logger.log(logger.EXTREME, "EnSearch.matchWords query failed: " +query.lastError());
		List<String> validGuids = new ArrayList<String>();
		while (query.next()) {
			String guid = query.valueString(0);
			validGuids.add(guid);
		}

		List<Note> noteIndex = noteTable.getAllNotes();
		for (int i=0; i<noteIndex.size(); i++) {
			Note n = noteIndex.get(i);
			boolean good = true;
			
			if (!validGuids.contains(n.getGuid()))
				good = false;
						
			// Start matching special stuff, like tags & notebooks
			if (any) {
				if (good && !matchTagsAny(n.getTagNames()))
					good = false;
				if (good && !matchNotebook(n.getNotebookGuid()))
					good = false;
				if (good && !matchListAny(getIntitle(), n.getTitle()))
					good = false;
				if (good && !matchListAny(getAuthor(), n.getAttributes().getAuthor()))
					good = false;
				if (good && !matchListAny(getSource(), n.getAttributes().getSource()))
					good = false;
				if (good && !matchListAny(getSourceApplication(), n.getAttributes().getSourceApplication()))
					good = false;
				if (good && !matchContentAny(n))
					good = false;
				if (good && !matchDatesAny(getCreated(), n.getCreated()))
					good = false;
				if (good && !matchDatesAny(getUpdated(), n.getUpdated()))
					good = false;
				if (good && n.getAttributes() != null && !matchDatesAny(getSubjectDate(), n.getAttributes().getSubjectDate()))
					good = false;
			} else {
				if (good && !matchTagsAll(n.getTagNames()))
					good = false;
				if (good && !matchNotebook(n.getNotebookGuid()))
					good = false;
				if (good && !matchListAll(getIntitle(), n.getTitle()))
					good = false;
				if (good && !matchListAll(getAuthor(), n.getAttributes().getAuthor()))
					good = false;
				if (good && !matchListAll(getSource(), n.getAttributes().getSource()))
					good = false;
				if (good && !matchListAll(getSourceApplication(), n.getAttributes().getSourceApplication()))
					good = false;
				if (good && !matchContentAll(n))
					good = false;
				if (good && !matchDatesAll(getCreated(), n.getCreated()))
					good = false;
				if (good && !matchDatesAll(getUpdated(), n.getUpdated()))
					good = false;
				if (good && n.getAttributes() != null && !matchDatesAll(getSubjectDate(), n.getAttributes().getSubjectDate()))
					good = false;
			}
			if (good) {
				guids.add(n);
			}
		}
 		
		// For performance reasons, we didn't get the tags for every note individually.  We now need to 
		// get them
		List<NoteTagsRecord> noteTags = noteTable.noteTagsTable.getAllNoteTags();
 		for (int i=0; i<guids.size(); i++) {
			List<String> tags = new ArrayList<String>();
			List<String> names = new ArrayList<String>();
			for (int j=0; j<noteTags.size(); j++) {
				if (guids.get(i).getGuid().equals(noteTags.get(j).noteGuid)) {
					tags.add(noteTags.get(j).tagGuid);
					names.add(getTagNameByGuid(noteTags.get(j).tagGuid));
				}
			}
			
			guids.get(i).setTagGuids(tags);
			guids.get(i).setTagNames(names);
		};
		logger.log(logger.EXTREME, "Leaving EnSearch.matchWords()");
		return guids;
	}
	
	
	
	private String getTagNameByGuid(String guid) {
		for (int i=0; i<tagIndex.size(); i++) {
			if (tagIndex.get(i).getGuid().equals(guid)) 
					return tagIndex.get(i).getName();
		}		
		return "";
	}

	// Compare dates
	public int dateCheck(String date, long noteDate)  throws java.lang.NumberFormatException  {
		int offset = 0;
		boolean found = false;
		GregorianCalendar calendar = new GregorianCalendar();
		
		if (date.contains("-")) {
			String modifier = date.substring(date.indexOf("-")+1);
			offset = new Integer(modifier);
			offset = 0-offset;
			date = date.substring(0,date.indexOf("-"));
		}
		
		if (date.contains("+")) {
			String modifier = date.substring(date.indexOf("+")+1);
			offset = new Integer(modifier);
			date = date.substring(0,date.indexOf("+"));
		}
		
		if (date.equalsIgnoreCase("today")) {
			calendar.add(Calendar.DATE, offset);
			calendar.set(Calendar.HOUR, 0);
			calendar.set(Calendar.MINUTE, 0);
			calendar.set(Calendar.SECOND, 1);
			found = true;
		}
		
		if (date.equalsIgnoreCase("month")) {
			calendar.add(Calendar.MONTH, offset);
			calendar.set(Calendar.DAY_OF_MONTH, 1);
			calendar.set(Calendar.HOUR, 0);
			calendar.set(Calendar.MINUTE, 0);
			calendar.set(Calendar.SECOND, 1);
			found = true;
		}

		if (date.equalsIgnoreCase("year")) {
			calendar.add(Calendar.YEAR, offset);
			calendar.set(Calendar.MONTH, Calendar.JANUARY);
			calendar.set(Calendar.DAY_OF_MONTH, 1);
			calendar.set(Calendar.HOUR, 0);
			calendar.set(Calendar.MINUTE, 0);
			calendar.set(Calendar.SECOND, 1);
			found = true;
		}

		if (date.equalsIgnoreCase("week")) {
			calendar.add(Calendar.DATE, 0-calendar.get(Calendar.DAY_OF_WEEK)+1);
			calendar.add(Calendar.DATE,(offset*7));
			calendar.set(Calendar.HOUR, 0);
			calendar.set(Calendar.MINUTE, 0);
			calendar.set(Calendar.SECOND, 1);

			found = true;
		}
		
		// If nothing was found, then we have a date number
		if (!found) {
			calendar = stringToGregorianCalendar(date);
		}
		
		
		String dateTimeFormat = new String("yyyyMMdd-HHmmss");
		SimpleDateFormat simple = new SimpleDateFormat(dateTimeFormat);
		StringBuilder creationDate = new StringBuilder(simple.format(noteDate));
		GregorianCalendar nCalendar = stringToGregorianCalendar(creationDate.toString().replace("-", "T"));
		if (calendar == null || nCalendar == null)  // If we have something invalid, it automatically fails
			return 1;
		return calendar.compareTo(nCalendar);
	}
	private GregorianCalendar stringToGregorianCalendar(String date) {
		String datePart = date;
		GregorianCalendar calendar = new GregorianCalendar();
		boolean GMT = false;
		String timePart = "";
		if (date.contains("T")) {
			datePart = date.substring(0,date.indexOf("T"));
			timePart = date.substring(date.indexOf("T")+1);
		} else {
			timePart = "000001";
		}
		if (datePart.length() != 8)
			return null;
		calendar.set(Calendar.YEAR, new Integer(datePart.substring(0,4)));
		calendar.set(Calendar.MONTH, new Integer(datePart.substring(4,6))-1);
		calendar.set(Calendar.DAY_OF_MONTH, new Integer(datePart.substring(6)));
		if (timePart.endsWith("Z")) {
			GMT = true;
			timePart = timePart.substring(0,timePart.length()-1);
		}
		timePart = timePart.concat("000000");
		timePart = timePart.substring(0,6);
		calendar.set(Calendar.HOUR, new Integer(timePart.substring(0,2)));
		calendar.set(Calendar.MINUTE, new Integer(timePart.substring(2,4)));
		calendar.set(Calendar.SECOND, new Integer(timePart.substring(4)));
		if (GMT)
			calendar.set(Calendar.ZONE_OFFSET, -1*(calendar.get(Calendar.ZONE_OFFSET)/(1000*60*60)));
		return calendar;

	}
		
}
