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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import com.evernote.edam.type.Data;
import com.evernote.edam.type.Resource;
import com.evernote.edam.type.ResourceAttributes;
import com.trolltech.qt.core.QByteArray;

import cx.fbn.nevernote.sql.driver.NSqlQuery;
import cx.fbn.nevernote.utilities.ApplicationLogger;



public class NoteResourceTable  {
	/**
	 * 
	 */
//	private static final long serialVersionUID = 1L;
	private final ApplicationLogger 		logger;
	private final DatabaseConnection 		db;	
	
	// Constructor
	public NoteResourceTable(ApplicationLogger l, DatabaseConnection d) {
		logger = l;
		db = d;
	}
	// Create the table
	public void createTable() {
		NSqlQuery query = new NSqlQuery(db.getResourceConnection());
        // Create the NoteResource table
        logger.log(logger.HIGH, "Creating table NoteResource...");
        if (!query.exec("Create table NoteResources (guid varchar primary key, " +
        		"noteGuid varchar, updateSequenceNumber integer, dataHash varchar, "+
        		"dataSize integer, dataBinary blob, "+
        		"mime varchar, width integer, height integer, duration integer, "+
        		"active integer, recognitionHash varchar, recognitionSize integer, " +
        		"recognitionBinary varchar, attributeSourceUrl varchar, attributeTimestamp timestamp, " +
        		"attributeLatitude double, attributeLongitude double, "+
        		"attributeAltitude double, attributeCameraMake varchar, attributeCameraModel varchar, "
        		+"attributeClientWillIndex varchar, attributeRecoType varchar, attributeFileName varchar,"+
        		"attributeAttachment boolean, isDirty boolean, indexNeeded boolean)"))
        	logger.log(logger.HIGH, "Table NoteResource creation FAILED!!!"); 
        if (!query.exec("CREATE INDEX unindexed_resources on noteresources (indexneeded desc, guid);"))
        	logger.log(logger.HIGH, "Noteresources unindexed_resources index creation FAILED!!!");
        if (!query.exec("CREATE INDEX resources_dataheshhex on noteresources (datahash, guid);"))
        	logger.log(logger.HIGH, "Noteresources resources_datahash index creation FAILED!!!");  
        if (!query.exec("create index RESOURCES_GUID_INDEX on noteresources (noteGuid, guid);"))
        	logger.log(logger.HIGH, "Noteresources resources_datahash index creation FAILED!!!");  
	}
	// Drop the table
	public void dropTable() {		
		NSqlQuery query = new NSqlQuery(db.getResourceConnection());
		query.exec("Drop table NoteResources");
	}
	// Reset the dirty flag
	public void  resetDirtyFlag(String guid) {
		NSqlQuery query = new NSqlQuery(db.getResourceConnection());
		
		query.prepare("Update noteresources set isdirty=false where guid=:guid");
		query.bindValue(":guid", guid);
		if (!query.exec())
			logger.log(logger.EXTREME, "Error resetting noteresource dirty field. " +query.lastError());
		else
			query.exec("commit");
	}
	// Set if the resource should be indexed
	public void  setIndexNeeded(String guid, Boolean indexNeeded) {
		NSqlQuery query = new NSqlQuery(db.getResourceConnection());		
		query.prepare("Update noteresources set indexNeeded=:needed where guid=:guid");
		query.bindValue(":needed", indexNeeded);
		query.bindValue(":guid", guid);
		if (!query.exec())
			logger.log(logger.EXTREME, "Error setting noteresource indexneeded field: " +query.lastError());
		else
			query.exec("commit");
	}
	// get any unindexed resource
	public List<String> getNextUnindexed(int limit) {
		List<String> guids = new ArrayList<String>();
        NSqlQuery query = new NSqlQuery(db.getResourceConnection());
        				
		if (!query.exec("Select guid from NoteResources where indexNeeded = true limit " +limit))
			logger.log(logger.EXTREME, "NoteResources SQL retrieve has failed on getNextUnindexed(): " +query.lastError());

		// Get a list of the notes
		String guid;
		while (query.next()) {
			guid = new String();
			guid = query.valueString(0);
			guids.add(guid);
		}	
		return guids;	
	}
	// get any unindexed resource
	public List<String> getUnindexed() {
		List<String> guids = new ArrayList<String>();
        NSqlQuery query = new NSqlQuery(db.getResourceConnection());
        				
		if (!query.exec("Select guid from NoteResources where indexNeeded = true"))
			logger.log(logger.EXTREME, "NoteResources SQL retrieve has failed on getUnindexed(): " +query.lastError());

		// Get a list of the notes
		String guid;
		while (query.next()) {
			guid = new String();
			guid = query.valueString(0);
			guids.add(guid);
		}	
		return guids;	
	}

	public List<String> getAll() {
		List<String> guids = new ArrayList<String>();
		NSqlQuery query = new NSqlQuery(db.getResourceConnection());
		
		query.prepare("Select guid from noteresources;");
		if (!query.exec())
			logger.log(logger.EXTREME, "Error getting all note resource guids. " +query.lastError());
		
		while (query.next()) {
			guids.add(query.valueString(0));
		}
		return guids;
	}
	
	public List<String> findInkNotes() {
		List<String> guids = new ArrayList<String>();
		NSqlQuery query = new NSqlQuery(db.getResourceConnection());
		
		query.prepare("Select guid from noteresources where mime='application/vnd.evernote.ink'");
		if (!query.exec())
			logger.log(logger.EXTREME, "Error searching for ink notes. " +query.lastError());
		
		while (query.next()) {
			guids.add(query.valueString(0));
		}
		return guids;
	}
	
	public void saveNoteResource(Resource r, boolean isDirty) {
		logger.log(logger.HIGH, "Entering saveNoteResources: isDirty " +isDirty);
		boolean check;
		logger.log(logger.HIGH, "Note: " +r.getNoteGuid());
		logger.log(logger.HIGH, "Resource: " +r.getGuid());
		NSqlQuery query = new NSqlQuery(db.getResourceConnection());
		SimpleDateFormat simple = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		
		query.prepare("Insert Into NoteResources ("
				+"guid, noteGuid, dataHash, dataSize, dataBinary, updateSequenceNumber, "
				+"mime, width, height, duration, active, recognitionHash, "				
				+"recognitionSize, recognitionBinary, attributeSourceUrl, attributeTimestamp, "
				+"attributeLatitude, attributeLongitude, attributeAltitude, attributeCameraMake, "
				+"attributeCameraModel, "
				+"attributeClientWillIndex, attributeRecoType, attributeFileName, attributeAttachment, isDirty, "
				+"indexNeeded) Values("
				+":guid, :noteGuid, :dataHash,:dataSize, :dataBody, :updateSequenceNumber, "
				+":mime, :width, :height, :duration, :active, :recognitionHash, "				
				+":recognitionSize, :recognitionBody, :attributeSourceUrl, :attributeTimestamp, "
				+":attributeLatitude, :attributeLongitude, :attributeAltitude, :attributeCameraMake, "
				+":attributeCameraModel, "
				+":attributeClientWillIndex, :attributeRecoType, :attributeFileName, :attributeAttachment, "
				+":isDirty, true)");
	
			query.bindValue(":guid", r.getGuid());
			query.bindValue(":noteGuid", r.getNoteGuid());
			if (r.getData() != null) {
				query.bindValue(":dataHash", byteArrayToHexString(r.getData().getBodyHash()));
//				query.bindValue(":dataHash", "c0369123fe9871d675ae456fd056ba33");
				query.bindValue(":dataSize", r.getData().getSize());
				query.bindBlob(":dataBody", r.getData().getBody());
			}
			query.bindValue(":updateSequenceNumber", r.getUpdateSequenceNum());
			query.bindValue(":mime", r.getMime());
			query.bindValue(":width", new Integer(r.getWidth()));
			query.bindValue(":height", new Integer(r.getHeight()));
			query.bindValue(":duration", new Integer(r.getDuration()));
			query.bindValue(":active", r.isActive());
			if (r.getRecognition() != null) {
				query.bindValue(":recognitionHash", r.getRecognition().getBodyHash());
				query.bindValue(":recognitionSize", r.getRecognition().getSize());
				if (r.getRecognition().getBody() != null)
					query.bindValue(":recognitionBody", new String(r.getRecognition().getBody()));
				else
					query.bindValue(":recognitionBody", "");
			} else {
				query.bindValue(":recognitionHash", "");
				query.bindValue(":recognitionSize", 0);
				query.bindValue(":recognitionBody", "");
			}
			if (r.getAttributes() != null) {
				query.bindValue(":attributeSourceUrl", r.getAttributes().getSourceURL());
				StringBuilder ts = new StringBuilder(simple.format(r.getAttributes().getTimestamp()));
				query.bindValue(":attributeTimestamp", ts.toString());
				query.bindValue(":attributeLatitude", r.getAttributes().getLatitude());
				query.bindValue(":attributeLongitude", r.getAttributes().getLongitude());
				query.bindValue(":attributeAltitude", r.getAttributes().getAltitude());
				query.bindValue(":attributeCameraMake", r.getAttributes().getCameraMake());
				query.bindValue(":attributeCameraModel", r.getAttributes().getCameraModel());
				query.bindValue(":attributeClientWillIndex", r.getAttributes().isClientWillIndex());
				query.bindValue(":attributeRecoType", r.getAttributes().getRecoType());
				query.bindValue(":attributeFileName", r.getAttributes().getFileName());
				query.bindValue(":attributeAttachment", r.getAttributes().isAttachment());			
			} 
			query.bindValue(":isDirty", isDirty);
						
			check = query.exec();
			if (!check) {
				logger.log(logger.MEDIUM, "*** NoteResource Table insert failed.");		
				logger.log(logger.MEDIUM, query.lastError());
			} else
				query.exec("commit");
			
						
			logger.log(logger.HIGH, "Leaving DBRunner.saveNoteResources");
	}
	// delete an old resource
	public void expungeNoteResource(String guid) {
		NSqlQuery query = new NSqlQuery(db.getResourceConnection());
		query.prepare("delete from NoteResources where guid=:guid");
		query.bindValue(":guid", guid);
		query.exec();
		query.exec("commit");
		
		NSqlQuery query2 = new NSqlQuery(db.getConnection());
		query2.prepare("Delete from InkImages where guid=:guid");
		query2.bindValue(":guid", guid);
		query2.exec();
		query2.exec("commit");

	}

	
	// Get a note resource from the database by it's hash value
	public String getNoteResourceGuidByHashHex(String noteGuid, String hash) {
		logger.log(logger.HIGH, "Entering DBRunner.getNoteResourceGuidByHashHex");

		boolean check;
		NSqlQuery query = new NSqlQuery(db.getResourceConnection());
		
		check = query.prepare("Select guid from NoteResources " +
					"where noteGuid=:noteGuid and dataHash=:hash");
		if (check)
			logger.log(logger.EXTREME, "NoteResource SQL select prepare was successful.");
		else {
 			logger.log(logger.EXTREME, "NoteResource SQL select prepare has failed.");
			logger.log(logger.MEDIUM, query.lastError());
		}
		query.bindValue(":noteGuid", noteGuid);
		query.bindValue(":hash", hash);
	
		check = query.exec();
		if (!check)	 {
			logger.log(logger.MEDIUM, "dbRunner.getNoteResourceGuidByHashHex Select failed." +
					"Note Guid:" +noteGuid+
					"Data Body Hash:" +hash);		
			logger.log(logger.MEDIUM, query.lastError());
		}
		if (!query.next()) {
			logger.log(logger.MEDIUM, "Note Resource not found.");
			return null;
		}
		return query.valueString(0);
	}

	// Get a note resource from the database by it's hash value
	public Resource getNoteResourceDataBodyByHashHex(String noteGuid, String hash) {
		logger.log(logger.HIGH, "Entering DBRunner.getNoteResourceDataBodyByHash");

		boolean check;
		NSqlQuery query = new NSqlQuery(db.getResourceConnection());
		
		check = query.prepare("Select guid, mime, from NoteResources " +
					"where noteGuid=:noteGuid and dataHash=:hash");
		if (!check) {
 			logger.log(logger.EXTREME, "NoteResource SQL select prepare has failed.");
			logger.log(logger.MEDIUM, query.lastError());
		}
		query.bindValue(":noteGuid", noteGuid);
		query.bindValue(":hash", hash);
	
		if (!query.exec()) {
			logger.log(logger.MEDIUM, "NoteResource Select failed." +
					"Note Guid:" +noteGuid+
					"Data Body Hash:" +hash);		
			logger.log(logger.MEDIUM, query.lastError());
		}
		if (!query.next()) {
			logger.log(logger.MEDIUM, "Note Resource not found.");
			return null;
		}
		
		Resource r = new Resource();
		r.setGuid(query.valueString(0));
		r.setMime(query.valueString(1));
		
		NSqlQuery binary = new NSqlQuery(db.getResourceConnection());
		if (!binary.prepare("Select databinary from NoteResources " +
					"where guid=:guid")) {
			logger.log(logger.MEDIUM, "Prepare for NoteResources Binary failed");
			logger.log(logger.MEDIUM, binary.lastError());
		}
		
		if (!binary.exec()) {
			logger.log(logger.MEDIUM, "NoteResources Binary Select failed." +
					"Note Guid:" +noteGuid+
					"Data Body Hash:" +hash);		
			logger.log(logger.MEDIUM, binary.lastError());
		}
		if (!binary.next()) {
			logger.log(logger.MEDIUM, "Note Resource Binary not found.");
			return null;
		}
		
		Data d = new Data();
		r.setData(d);
		d.setBody(binary.valueString(0).getBytes());
		logger.log(logger.HIGH, "Leaving DBRunner.getNoteResourceDataBodyByHash");
		return r;
	}

	
	// Get a note's resourcesby Guid
	public Resource getNoteResource(String guid, boolean withBinary) {
		if (guid == null)
			return null;
		
		NSqlQuery query = new NSqlQuery(db.getResourceConnection());
		String queryString;
		queryString = new String("Select guid, noteGuid, mime, width, height, duration, "
				+"active, updateSequenceNumber, dataHash, dataSize, "
				+"recognitionHash, recognitionSize, "
				+"attributeLatitude, attributeLongitude, attributeAltitude, "
				+"attributeCameraMake, attributeCameraModel, attributeClientWillIndex, "
				+"attributeRecoType, attributeFileName, attributeAttachment, attributeSourceUrl "
				+" from NoteResources where guid=:guid");

		
		query.prepare(queryString);
		
		query.bindValue(":guid", guid);
		if (!query.exec()) {
			logger.log(logger.EXTREME, "NoteResources SQL select has failed.");
			logger.log(logger.MEDIUM, query.lastError());
			return null;
		}
		Resource r = null;
		if (query.next()) {
									
			r = new Resource();
			r.setGuid(query.valueString(0));     	// Resource Guid
			r.setNoteGuid(query.valueString(1));   // note Guid
			r.setMime(query.valueString(2));       // Mime Type
			r.setWidth(new Short(query.valueString(3)));  // Width
			r.setHeight(new Short(query.valueString(4)));  // Height
			r.setDuration(new Short(query.valueString(5)));  // Duration
			r.setActive(new Boolean(query.valueString(6)));  // active
			r.setUpdateSequenceNum(new Integer(query.valueString(7)));  // update sequence number
			
			Data d = new Data();
			byte[] h = query.valueString(8).getBytes();    // data hash
			QByteArray hData = new QByteArray(h);
			QByteArray bData = new QByteArray(QByteArray.fromHex(hData));
			d.setBodyHash(bData.toByteArray());
			d.setSize(new Integer(query.valueString(9)));
			r.setData(d);
			
			Data rec = new Data();
			if (query.valueObject(10) != null)
				rec.setBodyHash(query.valueString(10).getBytes());   // Recognition Hash
			if (query.valueObject(11) != null)
				rec.setSize(new Integer(query.valueString(11)));
			else
				rec.setSize(0);
			r.setRecognition(rec);

			ResourceAttributes a = new ResourceAttributes();
			if (!query.valueString(12).equals(""))              // Latitude
				a.setLatitude(new Float(query.valueString(12)));
			if (!query.valueString(13).equals(""))              // Longitude
				a.setLongitude(new Float(query.valueString(13)));
			if (!query.valueString(14).equals(""))              // Altitude
				a.setAltitude(new Float(query.valueString(14)));
			a.setCameraMake(stringValue(query.valueString(15)));              // Camera Make
			a.setCameraModel(stringValue(query.valueString(16)));
			a.setClientWillIndex(booleanValue(query.valueString(17).toString(),false));  // Camera Model
			a.setRecoType(stringValue(query.valueString(18)));                 // Recognition Type
			a.setFileName(stringValue(query.valueString(19)));                  // File Name
			a.setAttachment(booleanValue(query.valueString(20).toString(),false));
			a.setSourceURL(query.valueString(21));
			r.setAttributes(a);
		
			if (withBinary) {
			    
				query.prepare("Select dataBinary from NoteResources where guid=:guid");
				query.bindValue(":guid", r.getGuid());
				query.exec();
				if (query.next()) {
					byte[] b = query.getBlob(0);
					r.getData().setBody(b);
				}
			} 
		}
		return r;
	}
	
	
	// Get a note's resourcesby Guid
	public List<Resource> getNoteResources(String noteGuid, boolean withBinary) {
		if (noteGuid == null)
			return null;
		List<Resource> res = new ArrayList<Resource>();
		
		NSqlQuery query = new NSqlQuery(db.getResourceConnection());
		query.prepare("Select guid"
				+" from NoteResources where noteGuid = :noteGuid");
		query.bindValue(":noteGuid", noteGuid);
		if (!query.exec()) {
			logger.log(logger.EXTREME, "NoteResources SQL select has failed.");
			logger.log(logger.MEDIUM, query.lastError());
			return null;
		}
		while (query.next()) {
			String guid = (query.valueString(0));
			res.add(getNoteResource(guid, withBinary));
		}	
		return res;
	}
	// Get all of a note's recognition data by the note guid
	public List<Resource> getNoteResourcesRecognition(String noteGuid) {
		if (noteGuid == null)
			return null;
		boolean check;
		List<Resource> res = new ArrayList<Resource>();
		NSqlQuery query = new NSqlQuery(db.getResourceConnection());
		check = query.prepare("Select "
				+"recognitionHash, recognitionSize, recognitionBinary "
				+" from NoteResources where noteGuid=:guid");
		if (!check) {
			logger.log(logger.EXTREME, "NoteTable.getNoteRecognition SQL prepare has failed.");
			logger.log(logger.MEDIUM, query.lastError());
			return null;
		}
		query.bindValue(":guid", noteGuid);
		if (!check) {
			logger.log(logger.EXTREME, "NoteTable.getNoteRecognition exec has failed.");
			logger.log(logger.MEDIUM, query.lastError());
			return null;
		}
		while (query.next()) {	
			Resource r = new Resource();		
			res.add(r);
			Data rec = new Data();
			rec.setBodyHash(query.valueString(0).getBytes());
			String x = new String(query.valueString(1));
			if (!x.equals("")) {
				rec.setSize(new Integer(x));
				rec.setBody(query.valueString(2).getBytes());
			} else
				rec.setSize(0);
			r.setRecognition(rec);
		}	
		return res;
	}
	
	
	// Get a note's recognition data by it's guid.
	public Resource getNoteResourceRecognition(String guid) {
		if (guid == null)
			return null;
		boolean check;
		NSqlQuery query = new NSqlQuery(db.getResourceConnection());
		check = query.prepare("Select "
				+"recognitionHash, recognitionSize, recognitionBinary, noteGuid "
				+" from NoteResources where guid=:guid");
		if (!check) {
			logger.log(logger.EXTREME, "NoteTable.getNoteRecognition SQL prepare has failed.");
			logger.log(logger.MEDIUM, query.lastError());
			return null;
		}
		query.bindValue(":guid", guid);
		query.exec();
		if (!check) {
			logger.log(logger.EXTREME, "NoteTable.getNoteRecognition exec has failed.");
			logger.log(logger.MEDIUM, query.lastError());
			return null;
		}
		Resource r = null;
		while (query.next()) {
									
			r = new Resource();		
			Data rec = new Data();
			rec.setBodyHash(query.valueString(0).getBytes());
			String x = new String(query.valueString(1));
			if (!x.equals("")) {
				rec.setSize(new Integer(x));
				rec.setBody(query.valueString(2).getBytes());
			} else
				rec.setSize(0);
			r.setRecognition(rec);
			r.setNoteGuid(query.valueString(3));
		}	
		return r;
	}

	// Save Note Resource
	public void updateNoteResource(Resource r, boolean isDirty) {
		logger.log(logger.HIGH, "Entering ListManager.updateNoteResource");
		expungeNoteResource(r.getGuid());
		saveNoteResource(r, isDirty);
		logger.log(logger.HIGH, "Leaving RNoteResourceTable.updateNoteResource");
	}
	// Update note resource GUID
	public void updateNoteResourceGuid(String oldGuid, String newGuid, boolean isDirty) {
		logger.log(logger.HIGH, "Entering RNoteResourceTable.updateNoteResourceGuid");
		NSqlQuery query = new NSqlQuery(db.getResourceConnection());
		query.prepare("update NoteResources set guid=:newGuid, isDirty=:isDirty where guid=:oldGuid");
		query.bindValue(":newGuid", newGuid);
		query.bindValue(":isDirty", isDirty);
		query.bindValue(":oldGuid", oldGuid);
		query.exec();
		query.exec("commit");
		logger.log(logger.HIGH, "Leaving RNoteResourceTable.updateNoteResourceGuid");
	}
	// Update note resource GUID
	public void resetUpdateSequenceNumber(String guid, boolean isDirty) {
		logger.log(logger.HIGH, "Entering RNoteResourceTable.updateNoteResourceGuid");
		NSqlQuery query = new NSqlQuery(db.getResourceConnection());
		query.prepare("update NoteResources set updateSequenceNumber=0, isDirty=:isDirty where guid=:guid");
		query.bindValue(":isDirty", isDirty);
		query.bindValue(":guid", guid);
		query.exec();
		query.exec("commit");
		logger.log(logger.HIGH, "Leaving RNoteResourceTable.updateNoteResourceGuid");
	}
	
	// Drop the table
	public void reindexAll() {		
		NSqlQuery query = new NSqlQuery(db.getResourceConnection());
		query.exec("Update NoteResources set indexneeded=true");
		query.exec("commit");
	}
	// Count attachments
	public int getResourceCount() {
        NSqlQuery query = new NSqlQuery(db.getResourceConnection());
		query.exec("select count(*) from noteresources");
		query.next(); 
		int returnValue = new Integer(query.valueString(0));
		return returnValue;
	}
	//
	// Count unindexed notes
	public int getUnindexedCount() {
        NSqlQuery query = new NSqlQuery(db.getResourceConnection());
		query.exec("select count(*) from noteresources where indexneeded=true");
		query.next(); 
		int returnValue = new Integer(query.valueString(0));
		return returnValue;
	}
	
	//********************************************
	//** Utility Functions
	//********************************************
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

	
	private String stringValue(Object value) {
		if (value != null && value.toString() != null) 
			return value.toString();
		else
			return null;
	}
	
	private  boolean booleanValue(Object value, boolean unknown) {
		if (value != null && value.toString() != null) {
			try {
				if ((Integer)value > 0)
					return true;
				else
					return false;
			} catch (java.lang.ClassCastException e) {
				try {
					String stringValue = (String)value;
					if (stringValue.equalsIgnoreCase("true"))
						return true;
					else 
						return false;
				} catch (java.lang.ClassCastException e1) { 
					return unknown;
				}
			}
		}
		else
			return unknown;
	}

	// Update note source url. 
	public void updateNoteSourceUrl(String guid, String url, boolean isDirty) {
		logger.log(logger.HIGH, "Entering RNoteResourceTable.updateNoteSourceUrl()");
		NSqlQuery query = new NSqlQuery(db.getResourceConnection());
		query.prepare("update NoteResources set attributesourceurl=:url, isDirty=:isDirty where guid=:guid");
		query.bindValue(":guid", guid);
		query.bindValue(":isDirty", isDirty);
		query.bindValue(":url", url);
		query.exec();
		query.exec("commit");
		logger.log(logger.HIGH, "Leaving RNoteResourceTable.updateNoteSourceUrl()");
	}
	
	// Get note source
	public String getNoteSourceUrl(String guid) {
		logger.log(logger.HIGH, "Entering RNoteResourceTable.getNoteSourceUrl()");
		NSqlQuery query = new NSqlQuery(db.getResourceConnection());
		query.prepare("Select attributesourceurl from noteresources where guid=:guid");
		query.bindValue(":guid", guid);
		query.exec();
		if (query.next()) {
			logger.log(logger.HIGH, "Leaving RNoteResourceTable.getNoteSourceUrl()");
			return query.valueString(0);
		}
		logger.log(logger.HIGH, "Leaving RNoteResourceTable.getNoteSourceUrl() - no value found");
		return null;
	}
	
	// Get note source
	public List<String> getDistinctNoteGuids() {
		logger.log(logger.HIGH, "Entering NoteResourceTable.getDistinctNoteGuids()");
		List<String> guids = new ArrayList<String>();
		NSqlQuery query = new NSqlQuery(db.getResourceConnection());
		query.exec("select distinct noteguid from noteresources");
		if (query.next()) {
			guids.add(query.valueString(0));
		}
		logger.log(logger.HIGH, "Leaving NoteResourceTable.getDistinctNoteGuids()");
		return guids;
	}

	public void resetAllDirty() {
		NSqlQuery query = new NSqlQuery(db.getResourceConnection());
		query.exec("update noteresources set isdirty=false");
	}
}

