package cx.fbn.nevernote.evernote;


public class NoteMetadata {
	private String guid;
	private int color;
	private boolean pinned;
	private boolean dirty;
	
	public NoteMetadata() {
		color = -1;
		pinned = false;
		dirty = false;
	}
	
	public String getGuid() {
		return guid;
	}
	public void setGuid(String g) {
		guid = g;
	}
	
	public int getColor() {
		return color;
	}
	public void setColor(int c) {
		color = c;
	}
	public void setPinned(boolean value) {
		pinned = value;
	}
	public boolean isPinned() {
		return pinned;
	}
	public void setDirty(boolean v) {
		dirty = v;
	}
	public boolean isDirty() {
		return dirty;
	}
	public void copy(NoteMetadata m) {
		color = m.getColor();
		guid = m.getGuid();
		pinned = m.isPinned();
		dirty = m.isDirty();
	}
}
