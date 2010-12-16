package cx.fbn.nevernote.gui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import com.evernote.edam.type.Note;
import com.trolltech.qt.core.QDateTime;
import com.trolltech.qt.core.QModelIndex;
import com.trolltech.qt.core.Qt;
import com.trolltech.qt.core.Qt.AspectRatioMode;
import com.trolltech.qt.core.Qt.TransformationMode;
import com.trolltech.qt.gui.QAbstractTableModel;
import com.trolltech.qt.gui.QColor;
import com.trolltech.qt.gui.QIcon;
import com.trolltech.qt.gui.QImage;
import com.trolltech.qt.gui.QPixmap;

import cx.fbn.nevernote.Global;
import cx.fbn.nevernote.filters.NoteSortFilterProxyModel;
import cx.fbn.nevernote.utilities.ListManager;

public class NoteTableModel extends QAbstractTableModel {
	private final ListManager 		listManager;
	private final Object[] 			headers;
	private List<Note>				noteIndex;
	private List<Note>				masterNoteIndex;
	private List<String>			unsynchronizedNotes;
	public HashMap<String,Integer>	titleColors;
	public NoteSortFilterProxyModel proxyModel;		// note sort model
	
	public NoteTableModel(ListManager m) {
		headers = new Object[Global.noteTableColumnCount];
		listManager = m;
		masterNoteIndex = null;
		unsynchronizedNotes = new ArrayList<String>();
	}
	
	public List<Note> getNoteIndex() {
		return noteIndex;
	}
	public void setNoteIndex(List<Note> list) {
		noteIndex = list;
	}
	public List<Note> getMasterNoteIndex() {
		return masterNoteIndex;
	}
	public void setMasterNoteIndex(List<Note> list) {
		masterNoteIndex = list;
	}
	public void setSortProxyModel(NoteSortFilterProxyModel m) {
		proxyModel = m;
	}
	
	public List<String> getUnsynchronizedNotes() {
		return unsynchronizedNotes;
	}
	public void setUnsynchronizedNotes(List<String> list) {
		unsynchronizedNotes = list;
	}
	
	public HashMap<String, Integer> getTitleColors() {
		return titleColors;
	}
	public void setTitleColors(HashMap<String, Integer> map) {
		titleColors = map;
	}
	
	@Override
	public int columnCount(QModelIndex arg0) {
		return Global.noteTableColumnCount;
	}

	@Override
	public Object data(QModelIndex index, int role) {
		if (index == null)
			return null;
        switch (role) {
        case Qt.ItemDataRole.DisplayRole: {
            return valueAt(index.row(), index.column());
        }
        case Qt.ItemDataRole.DecorationRole: {
        	if (index.column() == Global.noteTableThumbnailPosition ||
        		index.column() == Global.noteTableSynchronizedPosition)
        		return valueAt(index.row(), index.column());
        	else
        		return null;
        }
        case Qt.ItemDataRole.BackgroundRole: {
        	String guid = (String)valueAt(index.row(), Global.noteTableGuidPosition);
    		QColor backgroundColor = new QColor(QColor.white);
    		if (titleColors != null && titleColors.containsKey(guid)) {
    			int color = titleColors.get(guid);
    			backgroundColor.setRgb(color);
    		}
        	return backgroundColor;
        }
        case Qt.ItemDataRole.ForegroundRole: {
        	String guid = (String)valueAt(index.row(), Global.noteTableGuidPosition);
    		QColor backgroundColor = new QColor(QColor.white);
    		QColor foregroundColor = new QColor(QColor.black);
    		if (titleColors != null && titleColors.containsKey(guid)) {
    			int color = titleColors.get(guid);
    			backgroundColor.setRgb(color);
    			if (backgroundColor.rgb() == QColor.black.rgb() || backgroundColor.rgb() == QColor.blue.rgb()) 
    				foregroundColor.setRgb(QColor.white.rgb());
    		} 
        	return foregroundColor;
        }
        default:
            break;
        }
		return null;
	}

	@Override
	public int rowCount(QModelIndex arg0) {
		if (listManager != null && listManager.getMasterNoteIndex() != null) 
			return listManager.getMasterNoteIndex().size();
		else
			return 0;
	}

	
	private Object valueAt(int row, int col) {
		Note note = listManager.getMasterNoteIndex().get(row);
		
		if (col == Global.noteTableGuidPosition)
			return note.getGuid();
		if (col == Global.noteTableCreationPosition) 
			return note.getCreated();
		if (col == Global.noteTableChangedPosition) 
			return note.getUpdated();
		if (col == Global.noteTableSubjectDatePosition) {
			if (note.getAttributes().getSubjectDate() > 0)
				return note.getAttributes().getSubjectDate();
			else
				return note.getCreated();				
		}
		if (col == Global.noteTableTitlePosition)
			return note.getTitle();
		if (col == Global.noteTableAuthorPosition)
			return note.getAttributes().getAuthor();
		if (col == Global.noteTableSourceUrlPosition)
			return note.getAttributes().getSourceURL();
		if (col == Global.noteTableSynchronizedPosition) {
			String iconPath = new String("classpath:cx/fbn/nevernote/icons/");
			QIcon dotIcon = new QIcon(iconPath+"dot.png");
			String guid = note.getGuid();
			for (int i=0; i<unsynchronizedNotes.size(); i++) {
				if (unsynchronizedNotes.get(i).equalsIgnoreCase(guid)) 
					return dotIcon;
			}
			return null;
		}
		if (col == Global.noteTableTagPosition) {
			String tags = new String();
			for (int i=0; i<note.getTagNamesSize(); i++) {
				tags = tags + note.getTagNames().get(i);
				if (i!=note.getTagNamesSize()-1)
					tags = tags + ", ";
			}
			return tags;
		}
		if (col == Global.noteTableNotebookPosition) {
			for (int i=0; i<listManager.getNotebookIndex().size(); i++) {
				if (listManager.getNotebookIndex().get(i).getGuid().equals(note.getNotebookGuid()))
					return listManager.getNotebookIndex().get(i).getName();
			}
		}
		if (col == Global.noteTableGuidPosition) {
			return note.getGuid();
		}
		if (col == Global.noteTableThumbnailPosition) {
			if (!Global.enableThumbnails())
				return null;
			if (Global.getListView() == Global.View_List_Wide) {
//				QImage img = listManager.getThumbnail(note.getGuid());
				QPixmap img = listManager.getThumbnailPixmap(note.getGuid());
				if (img != null)
					return img.scaled(Global.smallThumbnailSize, 
						AspectRatioMode.KeepAspectRatio, TransformationMode.SmoothTransformation);
				else
					return null;
			}
			else {
				QImage img = listManager.getThumbnail(note.getGuid());
				if (img != null)
					return img.scaled(Global.largeThumbnailSize,
						AspectRatioMode.KeepAspectRatio, TransformationMode.SmoothTransformation);
				else
					return null;
			}
				
		}
		return "";
	}
	
	
	@Override
	public java.lang.Object headerData(int section, Qt.Orientation orientation, int role) {
		if (role != Qt.ItemDataRole.DisplayRole)
			return null;
		
		if (orientation == Qt.Orientation.Horizontal && section < headers.length) { 
			return headers[section];
		}
		return null;
		
	}
	
	
	@Override
	public boolean setHeaderData(int section, Qt.Orientation orientation, Object value, int role) {
		if (orientation == Qt.Orientation.Horizontal && section < headers.length && role == Qt.ItemDataRole.DisplayRole) {
			headers[section] = value;
			headerDataChanged.emit(orientation, section, section);
			return true;
		}
		return false;
	}

	
	// Update a note title
	public void updateNoteTitle(String guid, String title) {
		for (int i=0; i<getMasterNoteIndex().size(); i++) {
			if (getMasterNoteIndex().get(i).getGuid().equals(guid)) {
				getMasterNoteIndex().get(i).setTitle(title);
				QModelIndex idx = createIndex(i, Global.noteTableTitlePosition, nativePointer());
				setData(idx, title, Qt.ItemDataRole.EditRole); 
				i = getMasterNoteIndex().size();
			}	
		}
		// Update the list tables 
		for (int i=0; i<getNoteIndex().size(); i++) {
			if (getNoteIndex().get(i).getGuid().equals(guid)) {
				getNoteIndex().get(i).setTitle(title);
				i = getNoteIndex().size();
			}
		}
	}
	
	// Update a note title
	public void updateNoteTags(String guid, List<String> tags, List<String> names) {
		for (int i=0; i<getMasterNoteIndex().size(); i++) {
			if (getMasterNoteIndex().get(i).getGuid().equals(guid)) {
				getMasterNoteIndex().get(i).setTagGuids(tags);
				getMasterNoteIndex().get(i).setTagNames(names);
				String display = new String("");
				Collections.sort(names);
				for (int j=0; j<names.size(); j++) {
					display = display+names.get(j);
					if (j+2<names.size()) {
						display = display+Global.tagDelimeter+" ";
					}
				}
				QModelIndex idx = createIndex(i, Global.noteTableTagPosition, nativePointer());
				setData(idx, display, Qt.ItemDataRole.EditRole); 
				i = getMasterNoteIndex().size();
			}	
		}
	}

	
	public void updateNoteCreatedDate(String guid, QDateTime date) {
		
		for (int i=0; i<getMasterNoteIndex().size(); i++) {
			if (getMasterNoteIndex().get(i).getGuid().equals(guid)) {
				getMasterNoteIndex().get(i).setCreated((long)date.toTime_t()*1000);
				QModelIndex idx = createIndex(i, Global.noteTableCreationPosition, nativePointer());
				setData(idx, new Long(getMasterNoteIndex().get(i).getCreated()), Qt.ItemDataRole.EditRole); 
				i = getMasterNoteIndex().size();
			}	
		}
		
		// Update the list tables 
		for (int i=0; i<getNoteIndex().size(); i++) {
			if (getNoteIndex().get(i).getGuid().equals(guid)) {
				getNoteIndex().get(i).setCreated((long)date.toTime_t()*1000);
				i = getNoteIndex().size();
			}
		}
	}
	
	public void updateNoteSubjectDate(String guid, QDateTime date) {
		
		for (int i=0; i<getMasterNoteIndex().size(); i++) {
			if (getMasterNoteIndex().get(i).getGuid().equals(guid)) {
				getMasterNoteIndex().get(i).getAttributes().setSubjectDate((long)date.toTime_t()*1000);
				QModelIndex idx = createIndex(i, Global.noteTableSubjectDatePosition, nativePointer());
				setData(idx, new Long(getMasterNoteIndex().get(i).getAttributes().getSubjectDate()), Qt.ItemDataRole.EditRole); 
				i = getMasterNoteIndex().size();
			}	
		}
		
		// Update the list tables 
		for (int i=0; i<getNoteIndex().size(); i++) {
			if (getNoteIndex().get(i).getGuid().equals(guid)) {
				getNoteIndex().get(i).getAttributes().setSubjectDate((long)date.toTime_t()*1000);
				i = getNoteIndex().size();
			}
		}
	}
	
	public void updateNoteChangedDate(String guid, QDateTime date) {
		for (int i=0; i<getMasterNoteIndex().size(); i++) {
			if (getMasterNoteIndex().get(i).getGuid().equals(guid)) {
				getMasterNoteIndex().get(i).setUpdated((long)date.toTime_t()*1000);
				QModelIndex idx = createIndex(i, Global.noteTableChangedPosition, nativePointer());
				setData(idx, new Long(getMasterNoteIndex().get(i).getAttributes().getSubjectDate()), Qt.ItemDataRole.EditRole); 
				i = getMasterNoteIndex().size();
			}	
		}
		// Update the list tables 
		for (int i=0; i<getNoteIndex().size(); i++) {
			if (getNoteIndex().get(i).getGuid().equals(guid)) {
				getNoteIndex().get(i).setUpdated((long)date.toTime_t()*1000);
				i = getNoteIndex().size();
			}
		}
	}

	public void updateNoteGuid(String oldGuid, String newGuid) {
		
		for (int i=0; i<getNoteIndex().size(); i++) {
			if (getNoteIndex().get(i).getGuid() != null && getNoteIndex().get(i).getGuid().equals(oldGuid)) {
				getNoteIndex().get(i).setGuid(newGuid);; 
				i=getNoteIndex().size()+1;
			}
		}
		
		boolean k = true;
		if (k) return;
		
		for (int i=0; i<getMasterNoteIndex().size(); i++) {
			if (getMasterNoteIndex().get(i).getGuid() != null && getMasterNoteIndex().get(i).getGuid().equals(oldGuid)) {
				getMasterNoteIndex().get(i).setGuid(newGuid);
//				QModelIndex idx = createIndex(i, Global.noteTableGuidPosition, nativePointer());
//				setData(idx, new String(getMasterNoteIndex().get(i).getGuid()), Qt.ItemDataRole.EditRole); 
				i=getMasterNoteIndex().size()+1;
			}
		}		
	}
	
	public void updateNoteNotebook(String guid, String notebookGuid) {
		for (int i=0; i<getMasterNoteIndex().size(); i++) {
			if (getMasterNoteIndex().get(i).getGuid().equals(guid)) {
				getMasterNoteIndex().get(i).setNotebookGuid(notebookGuid);
				for (int j=0; j<listManager.getNotebookIndex().size(); j++) {
					if (listManager.getNotebookIndex().get(j).getGuid().equals(notebookGuid)) {
						String name = listManager.getNotebookIndex().get(j).getName();
						QModelIndex idx = createIndex(i, Global.noteTableNotebookPosition, nativePointer());
						setData(idx, name, Qt.ItemDataRole.EditRole); 
						j=listManager.getNotebookIndex().size();
					}
				}
				i=getMasterNoteIndex().size();
			}
		}
		for (int i=0; i<getNoteIndex().size(); i++) {
			if (getNoteIndex().get(i).getGuid().equals(guid)) {
				getNoteIndex().get(i).setNotebookGuid(notebookGuid);
				i=getNoteIndex().size();
			}
		}
	}

	public void updateNoteAuthor(String guid, String author) {
		for (int i=0; i<getMasterNoteIndex().size(); i++) {
			if (getMasterNoteIndex().get(i).getGuid().equals(guid)) {
				getMasterNoteIndex().get(i).getAttributes().setAuthor(author);
				getMasterNoteIndex().get(i).getAttributes().setAltitudeIsSet(true);
				QModelIndex idx = createIndex(i, Global.noteTableAuthorPosition, nativePointer());
				setData(idx, author, Qt.ItemDataRole.EditRole); 
				i = getMasterNoteIndex().size();
			}	
		}
		// Update the list tables 
		for (int i=0; i<getNoteIndex().size(); i++) {
			if (getNoteIndex().get(i).getGuid().equals(guid)) {
				getNoteIndex().get(i).getAttributes().setAuthor(author);
				getNoteIndex().get(i).getAttributes().setAuthorIsSet(true);
				i = getNoteIndex().size();
			}
		}
	}
	
	public void updateNoteSourceUrl(String guid, String url) {
		for (int i=0; i<getMasterNoteIndex().size(); i++) {
			if (getMasterNoteIndex().get(i).getGuid().equals(guid)) {
				getMasterNoteIndex().get(i).getAttributes().setSourceURL(url);
				getMasterNoteIndex().get(i).getAttributes().setSourceURLIsSet(true);
				QModelIndex idx = createIndex(i, Global.noteTableSourceUrlPosition, nativePointer());
				setData(idx, url, Qt.ItemDataRole.EditRole); 
				i = getMasterNoteIndex().size();
			}	
		}
		// Update the list tables 
		for (int i=0; i<getNoteIndex().size(); i++) {
			if (getNoteIndex().get(i).getGuid().equals(guid)) {
				getNoteIndex().get(i).getAttributes().setSourceURL(url);
				getNoteIndex().get(i).getAttributes().setSourceURLIsSet(true);
				i = getNoteIndex().size();
			}
		}
	}

	public void updateNoteSyncStatus(String guid, boolean sync) {
		
		boolean found = false;
		for (int i=0; i<unsynchronizedNotes.size(); i++) {			
			// If the note is now synchronized, but it is in the unsynchronized list, remove it
			if (unsynchronizedNotes.get(i).equalsIgnoreCase(guid) && sync) {
				unsynchronizedNotes.remove(i);
				found = true;
				i=unsynchronizedNotes.size();
			}
			
			// If the note is not synchronized, but it is already in the unsynchronized list, do nothing
			if (unsynchronizedNotes.get(i).equalsIgnoreCase(guid) && sync) {
				found = true;
				i=unsynchronizedNotes.size();
			}
		}
		
		// If we've gotten through the entire list, then we consider it synchronized.  If this is 
		// wrong, add it to the list.
		if (!sync && !found) 
			unsynchronizedNotes.add(guid);
		
		// Now we need to go through the table & update it
		for (int i=0; i<getMasterNoteIndex().size(); i++) {
			if (getMasterNoteIndex().get(i).getGuid().equals(guid)) {
				QModelIndex idx = createIndex(i, Global.noteTableSynchronizedPosition, nativePointer());
				String value;
				if (!sync)
					value = tr("false");
				else
					value = tr("true");
				setData(idx, value, Qt.ItemDataRole.EditRole); 
				return;
			}	
		}
	}
	
	public void addNote(Note n) {
		getNoteIndex().add(n);
		getMasterNoteIndex().add(n);
		proxyModel.addGuid(n.getGuid());
		proxyModel.invalidate();
//		proxyModel.filter();
	}
	
	
	@Override
	public boolean setData(QModelIndex index, Object value,int role) {
		if (role == Qt.ItemDataRole.EditRole) {
			dataChanged.emit(index, index);
			return true;
		} else {
			return super.setData(index, value, role);
		}
	}
	
	public void updateNoteTitleColor(String guid, Integer color) {
		getTitleColors().remove(guid);
		getTitleColors().put(guid, color);
		layoutChanged.emit();
	}

	@Override
	public Qt.ItemFlags flags(QModelIndex index) {
		Qt.ItemFlag flags[] = { Qt.ItemFlag.ItemIsEnabled, 
								Qt.ItemFlag.ItemIsDragEnabled,
								Qt.ItemFlag.ItemIsSelectable };
		
		return new Qt.ItemFlags(flags);
	}
	
}
