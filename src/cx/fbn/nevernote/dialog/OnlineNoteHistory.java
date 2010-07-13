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

package cx.fbn.nevernote.dialog;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.List;

import com.evernote.edam.notestore.NoteVersionId;
import com.evernote.edam.type.Note;
import com.evernote.edam.type.Resource;
import com.trolltech.qt.core.QByteArray;
import com.trolltech.qt.core.QFile;
import com.trolltech.qt.core.QIODevice;
import com.trolltech.qt.core.Qt.ContextMenuPolicy;
import com.trolltech.qt.gui.QComboBox;
import com.trolltech.qt.gui.QDialog;
import com.trolltech.qt.gui.QHBoxLayout;
import com.trolltech.qt.gui.QLabel;
import com.trolltech.qt.gui.QPushButton;
import com.trolltech.qt.gui.QVBoxLayout;
import com.trolltech.qt.xml.QDomAttr;
import com.trolltech.qt.xml.QDomDocument;
import com.trolltech.qt.xml.QDomElement;
import com.trolltech.qt.xml.QDomNodeList;

import cx.fbn.nevernote.Global;
import cx.fbn.nevernote.gui.BrowserWindow;
import cx.fbn.nevernote.sql.DatabaseConnection;

public class OnlineNoteHistory extends QDialog {
	public final QPushButton 	restoreAsNew;
	public final QPushButton 	restore;
	private DatabaseConnection  conn;
	public final QComboBox		historyCombo;	 
	private final BrowserWindow	browser;
	
	// Constructor
	public OnlineNoteHistory(DatabaseConnection c) {
		setWindowTitle("Online Note History");
		QVBoxLayout main = new QVBoxLayout();
		setLayout(main);
		historyCombo = new QComboBox(this);
		
		QHBoxLayout comboLayout = new QHBoxLayout();
		comboLayout.addWidget(new QLabel("History Date:"));
		comboLayout.addWidget(historyCombo);
		comboLayout.addStretch(100);
		
		main.addLayout(comboLayout);
				
		browser = new BrowserWindow(conn);
		main.addWidget(browser);
		browser.titleLabel.setVisible(false);
		browser.notebookBox.setVisible(false);
		browser.hideButtons();
		browser.tagEdit.setVisible(false);
		browser.tagLabel.setVisible(false);
		
		QHBoxLayout buttonLayout = new QHBoxLayout();
		buttonLayout.addStretch(100);
		restore = new QPushButton("Restore Note");
		restore.clicked.connect(this, "restorePushed()");
		
		restoreAsNew = new QPushButton("Restore As New Note");
		restoreAsNew.clicked.connect(this, "restoreAsNewPushed()");
		QPushButton cancel = new QPushButton("Cancel");
		cancel.clicked.connect(this, "cancelPressed()");
		
		buttonLayout.addWidget(restore);
		buttonLayout.addWidget(restoreAsNew);
		buttonLayout.addWidget(cancel);
		main.addLayout(buttonLayout);
		
		browser.getBrowser().setContextMenuPolicy(ContextMenuPolicy.NoContextMenu);

	}
	
	@SuppressWarnings("unused")
	private void restoreAsNewPushed() {
		this.close();
	}
	@SuppressWarnings("unused")
	private void restorePushed() {
		this.close();
	}
	@SuppressWarnings("unused")
	private void cancelPressed() {
		this.close();
	}
	
	public void setCurrent(boolean isDirty) {
		if (isDirty) 
			historyCombo.addItem(new String("Current (Non Synchronized)"));
		else
			historyCombo.addItem(new String("Current (Synchronized)"));
				
	}
	
	public void load(List<NoteVersionId> versions) {
		String fmt = Global.getDateFormat() + " " + Global.getTimeFormat();
		String dateTimeFormat = new String(fmt);
		SimpleDateFormat simple = new SimpleDateFormat(dateTimeFormat);
		
		for (int i=0; i<versions.size(); i++) {
			StringBuilder versionDate = new StringBuilder(simple.format(versions.get(i).getServiceUpdated()));
			historyCombo.addItem(versionDate.toString());
		}
	}
	
	public void setContent(Note currentNote) {
		StringBuffer b = rebuildNoteHTML(currentNote);
		StringBuffer js = new StringBuffer();

		// We need to prepend the note with <HEAD></HEAD> or encoded characters are ugly 
		js.append("<html><head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\">");	
		js.append("<style type=\"text/css\">en-crypt-temp { border-style:solid; border-color:blue; padding:1mm 1mm 1mm 1mm; }</style>");
		js.append("</head>");
		js.append(b.toString());
		js.append("</HTML>");
//		js.replace("<!DOCTYPE en-note SYSTEM 'http://xml.evernote.com/pub/enml2.dtd'>", "");
//		js.replace("<?xml version='1.0' encoding='UTF-8'?>", "");
		
		browser.setNote(currentNote);
		browser.getBrowser().page().mainFrame().setHtml(js.toString());
	}
 
	//*************************************************
	//* XML Modifying Methods
	//*************************************************
	private StringBuffer rebuildNoteHTML(Note note) {
		QDomDocument doc = new QDomDocument();
		QDomDocument.Result result = doc.setContent(note.getContent());
		if (!result.success) {
			return new StringBuffer(note.getContent());
		}
		
		doc = modifyTags(note, doc);
		QDomElement docElem = doc.documentElement();
		docElem.setTagName("Body");
		
		// Fix the stupid problem where inserting an <img> tag after an <a> tag (which is done
		// to get the <en-media> application tag to work properly) causes spaces to be inserted
		// between the <a> & <img>.  This messes things up later.  This is an ugly hack.
		String docString = doc.toString();
		StringBuffer html = new StringBuffer(docString.substring(docString.toLowerCase().indexOf("<body>")));
		
		for (int i=html.indexOf("<a en-tag=\"en-media\" ", 0); i>-1; i=html.indexOf("<a en-tag=\"en-media\" ", i)) {
			i=html.indexOf(">\n",i+1);
			int z = html.indexOf("<img",i);
			for (int j=z-1; j>i; j--) 
				html.deleteCharAt(j);
			i=html.indexOf("/>", z+1);
			z = html.indexOf("</a>",i);
			for (int j=z-1; j>i+1; j--) 
				html.deleteCharAt(j);
		} 
		return html;
	}	

	
	
	private QDomDocument modifyTags(Note note, QDomDocument doc) {
		QDomElement docElem = doc.documentElement();
		
		// Modify en-media tags
		QDomNodeList anchors = docElem.elementsByTagName("en-media");
		int enMediaCount = anchors.length();
		for (int i=enMediaCount-1; i>=0; i--) {
			QDomElement enmedia = anchors.at(i).toElement();
			if (enmedia.hasAttribute("type")) {
				QDomAttr attr = enmedia.attributeNode("type");
				QDomAttr hash = enmedia.attributeNode("hash");
				String[] type = attr.nodeValue().split("/");
				String appl = type[1];
				
				if (type[0] != null) {
					if (type[0].equals("image")) {
						modifyImageTags(note, docElem, enmedia, hash);
					}
					if (!type[0].equals("image")) {
						modifyApplicationTags(note, doc, docElem, enmedia, hash, appl);
					}
//					if (type[0].equals("audio")) {
//						modifyApplicationTags(doc, docElem, enmedia, hash, appl);
//					}
				}
			}
		}
		
		// Modify todo tags
		anchors = docElem.elementsByTagName("en-todo");
		int enTodoCount = anchors.length();
		for (int i=enTodoCount-1; i>=0; i--) {
			QDomElement enmedia = anchors.at(i).toElement();
			modifyTodoTags(enmedia);
		}
		
		// Modify en-crypt tags
		anchors = docElem.elementsByTagName("en-crypt");
		int enCryptLen = anchors.length();
		for (int i=enCryptLen-1; i>=0; i--) {
			QDomElement enmedia = anchors.at(i).toElement();
			//enmedia.setAttribute("style","display:none");
			enmedia.setAttribute("contentEditable","false");
			enmedia.setAttribute("src", Global.getDirectoryPath()+"images/encrypt.png");
			enmedia.setAttribute("en-tag","en-crypt");
			enmedia.setAttribute("alt", enmedia.text());
			Global.cryptCounter++;
			enmedia.setAttribute("id", "crypt"+Global.cryptCounter.toString());
			String encryptedText = enmedia.text();
			
			// If the encryption string contains crlf at the end, remove them because they mess up the javascript.
			if (encryptedText.endsWith("\n"))
				encryptedText = encryptedText.substring(0,encryptedText.length()-1);
			if (encryptedText.endsWith("\r"))
				encryptedText = encryptedText.substring(0,encryptedText.length()-1);
			
			// Add the commands
			enmedia.setAttribute("onClick", "window.jambi.decryptText('crypt"+Global.cryptCounter.toString()+"', '"+encryptedText+"', '"+enmedia.attribute("hint")+"');");
			enmedia.setAttribute("onMouseOver", "style.cursor='hand'");
			enmedia.setTagName("img");
			enmedia.removeChild(enmedia.firstChild());   // Remove the actual encrypted text
		}

		return doc;
	}
	
	
	
	
	   private void modifyApplicationTags(Note n, QDomDocument doc, QDomElement docElem, QDomElement enmedia, QDomAttr hash, String appl) {
		  
		   Resource r = null;
		   for (int i=0; i<n.getResourcesSize(); i++) {
			   String hashValue = hash.value();
			   byte res[] = n.getResources().get(i).getData().getBodyHash();
			   String resourceHashValue = new String(Global.byteArrayToHexString(res));
			   if (resourceHashValue.equalsIgnoreCase(hashValue)) {
				   r = n.getResources().get(i);
				   i=n.getResourcesSize();
			   }
		   }
		   
		   if (r!= null) {
				if (r.getData()!=null) {
					// Did we get a generic applicaiton?  Then look at the file name to 
					// try and find a good application type for the icon
					if (appl.equalsIgnoreCase("octet-stream")) {
						if (r.getAttributes() != null && r.getAttributes().getFileName() != null) {
							String fn = r.getAttributes().getFileName();
							int pos = fn.indexOf(".");
							if (pos > -1) {
								appl = fn.substring(pos+1);
							}
						}
					}
					
					String fileDetails = null;
					if (r.getAttributes() != null && r.getAttributes().getFileName() != null && !r.getAttributes().getFileName().equals(""))
						fileDetails = r.getAttributes().getFileName();
					if (fileDetails != null && !fileDetails.equals("")) 
						enmedia.setAttribute("href", "nnres://" +r.getGuid()+n.getUpdateSequenceNum() +Global.attachmentNameDelimeter +fileDetails);
					else
						enmedia.setAttribute("href", "nnres://" +r.getGuid()+n.getUpdateSequenceNum() +Global.attachmentNameDelimeter +appl);
					if (fileDetails == null || fileDetails.equals(""))
						fileDetails = "";
					enmedia.setAttribute("en-tag", "en-media");
					enmedia.setAttribute("guid", r.getGuid());
					enmedia.setTagName("a");
					QDomElement newText = doc.createElement("img");
					String icon = findIcon(appl);
					if (icon.equals("attachment.png"))
						icon = findIcon(fileDetails.substring(fileDetails.indexOf(".")+1));
					newText.setAttribute("src", Global.getDirectoryPath()+"images"+File.separator +icon);	
					newText.setAttribute("title", fileDetails);
					enmedia.removeChild(enmedia.firstChild());
					enmedia.appendChild(newText);
				}
			}
	    }
	    // find the appropriate icon for an attachment
	    private String findIcon(String appl) {
	    	appl = appl.toLowerCase();
	    	File f = new File(Global.getDirectoryPath()+"images"+File.separator +appl +".png");
	    	if (f.exists())
	    		return appl+".png";
	    	return "attachment.png";
	    }
	    
	
	
	
	
	
	
    private void modifyTodoTags(QDomElement todo) {
 		todo.setAttribute("type", "checkbox");
		String checked = todo.attribute("checked");
		todo.removeAttribute("checked");
		if (checked.equalsIgnoreCase("true"))
			todo.setAttribute("checked", "");
		else
			todo.setAttribute("unchecked","");
		todo.setAttribute("value", checked);
		todo.setAttribute("onClick", "value=checked;window.jambi.contentChanged(); ");
		todo.setTagName("input");
    }
    
    
    private void modifyImageTags(Note note, QDomElement docElem, QDomElement enmedia, QDomAttr hash) {
    	String type = enmedia.attribute("type");
    	if (type.startsWith("image/"))
    		type = "."+type.substring(6);
    	else
    		type="";
    	
    	Resource r = null;
		for (int i=0; i<note.getResourcesSize(); i++) {
			String hashValue = hash.value();
			byte res[] = note.getResources().get(i).getData().getBodyHash();
			String resourceHashValue = new String(Global.byteArrayToHexString(res));
		   if (hashValue.equalsIgnoreCase(resourceHashValue)) {
			   r = note.getResources().get(i);
			   i=note.getResourcesSize();
		   }
		}
    	
		if (r==null)
			return;
		
    	QFile tfile = new QFile(Global.getDirectoryPath()+"res"+File.separator +r.getGuid()+note.getUpdateSequenceNum()+type);
    	if (!tfile.exists()) {
   			if (r!= null && r.getData() != null && r.getData().getBody().length > 0) {
 				tfile.open(new QIODevice.OpenMode(QIODevice.OpenModeFlag.WriteOnly));
 				QByteArray binData = new QByteArray(r.getData().getBody());
				tfile.write(binData);
 				tfile.close();
  				enmedia.setAttribute("src", tfile.fileName());
  				enmedia.setAttribute("en-tag", "en-media");
  				enmedia.setNodeValue("");
    			enmedia.setAttribute("guid", r.getGuid());
    			enmedia.setTagName("img");
    		}
    	}
		enmedia.setAttribute("src", tfile.fileName());
		enmedia.setAttribute("en-tag", "en-media");
		enmedia.setNodeValue("");
		enmedia.setAttribute("guid", r.getGuid());
		enmedia.setTagName("img");
    }
}
