package cx.fbn.nevernote.xml;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.w3c.tidy.Tidy;
import org.w3c.tidy.TidyMessage;

import com.evernote.edam.type.Note;
import com.evernote.edam.type.Resource;
import com.trolltech.qt.core.QByteArray;
import com.trolltech.qt.core.QDataStream;
import com.trolltech.qt.core.QFile;
import com.trolltech.qt.core.QIODevice;
import com.trolltech.qt.core.QIODevice.OpenModeFlag;
import com.trolltech.qt.core.QTemporaryFile;
import com.trolltech.qt.core.QTextCodec;
import com.trolltech.qt.core.QUrl;
import com.trolltech.qt.core.Qt.BGMode;
import com.trolltech.qt.gui.QColor;
import com.trolltech.qt.gui.QPainter;
import com.trolltech.qt.gui.QPainter.RenderHint;
import com.trolltech.qt.gui.QPixmap;
import com.trolltech.qt.xml.QDomAttr;
import com.trolltech.qt.xml.QDomDocument;
import com.trolltech.qt.xml.QDomElement;
import com.trolltech.qt.xml.QDomNodeList;

import cx.fbn.nevernote.Global;
import cx.fbn.nevernote.config.FileManager;
import cx.fbn.nevernote.filters.EnSearch;
import cx.fbn.nevernote.gui.PDFPreview;
import cx.fbn.nevernote.sql.DatabaseConnection;
import cx.fbn.nevernote.utilities.ApplicationLogger;

public class NoteFormatter {

	private final ApplicationLogger logger;
	private final DatabaseConnection conn;
	public boolean resourceError = false;
	public boolean readOnly = false; 
	public boolean inkNote = false;
	public boolean addHighlight = true;
	private Note currentNote;
	private String currentNoteGuid;
	private boolean pdfPreview;
	ArrayList<QTemporaryFile> tempFiles;
	private EnSearch enSearch;
	private boolean noteHistory;
	public boolean formatError;
	
	public NoteFormatter(ApplicationLogger logger, DatabaseConnection conn, List<QTemporaryFile> tempFiles2) {
		this.logger = logger;
		this.conn = conn;
		noteHistory = false;
	}
	
	
	private class TidyListener implements org.w3c.tidy.TidyMessageListener {
		
		ApplicationLogger logger;
		public boolean errorFound; 
		
		public TidyListener(ApplicationLogger logger) {
			this.logger = logger;
			errorFound = false;
		}
		@Override
		public void messageReceived(TidyMessage msg) {
			if (msg.getLevel() == TidyMessage.Level.ERROR) {
				logger.log(logger.LOW, "******* JTIDY ERORR *******");
				logger.log(logger.LOW, "Error Code: " +msg.getErrorCode());
				logger.log(logger.LOW, "Column: " +msg.getColumn());
				logger.log(logger.LOW, "Column: " +msg.getColumn());
				logger.log(logger.LOW, "Line: " +msg.getLine());
				logger.log(logger.LOW, "Message: " +msg.getMessage());
				logger.log(logger.LOW, "***************************");
				errorFound = true;
			} else 
				logger.log(logger.EXTREME, "JTidy Results: "+msg.getMessage());
		}
		
	}

	
	
	public void setNote(Note note, boolean pdfPreview) {
		currentNote = note;
		this.pdfPreview = pdfPreview;
		readOnly = false;
		currentNoteGuid = null;
		if (note != null) {
			currentNoteGuid = note.getGuid();
			readOnly = conn.getNotebookTable().isReadOnly(note.getNotebookGuid());
		} 
		resourceError = false;
	}
	
	public void setHighlight(EnSearch search) {
		if (search==null || search.hilightWords == null ||search.hilightWords.size() == 0) {
			enSearch = null;
			addHighlight = false;
		} else {
			enSearch = search;
			addHighlight = true;
		}
	}
	
	// Set if we are coming here through note histary.  It triggers longer file names to avoid conflicts
	public void setNoteHistory(boolean value) {
		noteHistory = value;
	}
	
	// Rebuild the note HTML to something usable
	public String rebuildNoteHTML() {
		formatError = false;
		if (currentNote == null)
			return null;
	 	logger.log(logger.HIGH, "Entering NeverNote.rebuildNoteHTML");
		logger.log(logger.EXTREME, "Note guid: " +currentNoteGuid);
		logger.log(logger.EXTREME, "Note Text:" +currentNote);
		QDomDocument doc = new QDomDocument();
		QDomDocument.Result result = doc.setContent(currentNote.getContent());

		// Handle any errors
		if (!result.success) {
			logger.log(logger.LOW, "Error parsing document.  Attempting to restructure");
			Tidy tidy = new Tidy();
			TidyListener tidyListener = new TidyListener(logger);
			tidy.setMessageListener(tidyListener);
			tidy.getStderr().close();  // the listener will capture messages
			tidy.setXmlTags(true);
			
			QTextCodec codec;
			codec = QTextCodec.codecForName("UTF-8");
	        QByteArray unicode =  codec.fromUnicode(currentNote.getContent());
	        
	        logger.log(logger.MEDIUM, "Starting JTidy check");
	        logger.log(logger.MEDIUM, "Start of JTidy Input");
	        logger.log(logger.MEDIUM, currentNote.getContent());
	        logger.log(logger.MEDIUM, "End Of JTidy Input");
			ByteArrayInputStream is = new ByteArrayInputStream(unicode.toByteArray());
	        ByteArrayOutputStream os = new ByteArrayOutputStream();
	        tidy.setInputEncoding("UTF-8");
			tidy.parse(is, os);
			String tidyContent = os.toString();
			if (tidyListener.errorFound) {
				logger.log(logger.LOW, "Restructure failed!!!");
			} else {
				doc = null;
				doc = new QDomDocument();
				result = doc.setContent(tidyContent);
			}
		}
		if (!result.success) {
			logger.log(logger.MEDIUM, "Parse error when rebuilding XML to HTML");
			logger.log(logger.MEDIUM, "Note guid: " +currentNoteGuid);
			logger.log(logger.MEDIUM, "Error: "+result.errorMessage);
			logger.log(logger.MEDIUM, "Line: " +result.errorLine + " Column: " +result.errorColumn);
			System.out.println("Error: "+result.errorMessage);
			System.out.println("Line: " +result.errorLine + " Column: " +result.errorColumn);
			logger.log(logger.EXTREME, "**** Start of unmodified note HTML");
			logger.log(logger.EXTREME, currentNote.getContent());
			logger.log(logger.EXTREME, "**** End of unmodified note HTML");
			formatError = true;
			readOnly = true;
			return currentNote.getContent();
		}

		if (tempFiles == null)
			tempFiles = new ArrayList<QTemporaryFile>();
		tempFiles.clear();
		
		doc = modifyTags(doc);
		if (addHighlight)
			doc = addHilight(doc);
		QDomElement docElem = doc.documentElement();
		docElem.setTagName("Body");
//		docElem.setAttribute("bgcolor", "green");
		logger.log(logger.EXTREME, "Rebuilt HTML:");
		logger.log(logger.EXTREME, doc.toString());	
		logger.log(logger.HIGH, "Leaving NeverNote.rebuildNoteHTML");
		// Fix the stupid problem where inserting an <img> tag after an <a> tag (which is done
		// to get the <en-media> application tag to work properly) causes spaces to be inserted
		// between the <a> & <img>.  This messes things up later.  This is an ugly hack.
		StringBuffer html = new StringBuffer(doc.toString());
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

		return html.toString(); //.replace("<Body", "<Body dir=\"rtl\"");
	}	

	private void addImageHilight(String resGuid, QFile f) {
		if (enSearch == null || enSearch.hilightWords == null || enSearch.hilightWords.size() == 0)
			return;
		
		// Get the recognition XML that tells where to hilight on the image
		Resource recoResource = conn.getNoteTable().noteResourceTable.getNoteResourceRecognition(resGuid);
		if (recoResource.getRecognition().getBody() == null || recoResource.getRecognition().getBody().length == 0)
			return;
		QByteArray recoData = new QByteArray(recoResource.getRecognition().getBody());
		String xml = recoData.toString();
		
		// Get a painter for the image.  This is the background (the initial image).
    	QPixmap pix = new QPixmap(f.fileName());
    	QPixmap hilightedPix = new QPixmap(pix.size());
    	QPainter p = new QPainter(hilightedPix);
    	p.drawPixmap(0,0, pix);

    	// Create a transparent pixmap.  The only non-transparent
    	// piece is the hilight that will be overlayed to hilight text no the background
    	QPixmap overlayPix = new QPixmap(pix.size());
    	overlayPix.fill(QColor.transparent);
    	QPainter p2 = new QPainter(overlayPix);
    	p2.setBackgroundMode(BGMode.TransparentMode);
    	p2.setRenderHint(RenderHint.Antialiasing, true);
		QColor yellow = QColor.yellow;
//		yellow.setAlphaF(0.4);
    	p2.setBrush(yellow);
	
		// Get the recognition data from the note
    	QDomDocument doc = new QDomDocument();
    	doc.setContent(xml);
    	
    	// Go through all "item" nodes
		QDomNodeList anchors = doc.elementsByTagName("item");
		for (int i=0; i<anchors.length(); i++) {
			QDomElement element = anchors.at(i).toElement();
			int x = new Integer(element.attribute("x"));   // x coordinate
			int y = new Integer(element.attribute("y"));   // y coordinate
			int w = new Integer(element.attribute("w"));   // width
			int h = new Integer(element.attribute("h"));   // height
			QDomNodeList children = element.childNodes();  // all children ("t" nodes).
			
			// Go through the children ("t" nodes)
			for (int j=0; j<children.length(); j++) {
		    	QDomElement child = children.at(j).toElement();
		    	if (child.nodeName().equalsIgnoreCase("t")) {
		    		String text = child.text();   // recognition text
		    		int weight = new Integer(child.attribute("w"));  // recognition weight
		    		if (weight >= Global.getRecognitionWeight()) {   // Are we above the maximum?
		    			
		    			// Check to see if this word matches something we were searching for.
		    			for (int k=0; k<enSearch.hilightWords.size(); k++) {
		    				String searchWord = enSearch.hilightWords.get(k).toLowerCase();
		    				if (searchWord.startsWith("*"))
		    					searchWord = searchWord.substring(1);
		    				if (searchWord.endsWith("*"))
		    					searchWord = searchWord.substring(0,searchWord.length()-1);
		    				if (text.toLowerCase().contains(searchWord)) {
		    					p2.drawRect(x,y,w,h);			
		    				}
		    			}
		    		}
		    	}
			}
		}
    	p2.end();
    	
    	// Paint the hilight onto the background.
    	p.setOpacity(0.4);
    	p.drawPixmap(0,0, overlayPix);
    	p.end();
    	
    	// Save over the initial pixmap.
    	hilightedPix.save(f.fileName());
	}
	
    // Modify the en-media tag into an image tag so it can be displayed.
    private void modifyImageTags(QDomDocument doc, QDomElement docElem, QDomElement enmedia, QDomAttr hash) {
    	logger.log(logger.HIGH, "Entering NeverNote.modifyImageTags");
    	String type = enmedia.attribute("type");
    	if (type.startsWith("image/"))
    		type = "."+type.substring(6);
    	else
    		type="";
    	
    	String resGuid = conn.getNoteTable().noteResourceTable.getNoteResourceGuidByHashHex(currentNoteGuid, hash.value());
    	QFile tfile = new QFile(Global.getFileManager().getResDirPath(resGuid + type));
//    	if (!tfile.exists()) {
    		Resource r = null;
    		if (resGuid != null)
   				r = conn.getNoteTable().noteResourceTable.getNoteResource(resGuid,true);
   			if (r==null || r.getData() == null || r.getData().getBody().length == 0) {
   				resourceError = true;
   				readOnly = true;
   			}
   			if (r!= null && r.getData() != null && r.getData().getBody().length > 0) {
 				tfile.open(new QIODevice.OpenMode(QIODevice.OpenModeFlag.WriteOnly));
 				QByteArray binData = new QByteArray(r.getData().getBody());
				tfile.write(binData);
 				tfile.close();
 				
 				// If we have recognition text, outline it
 				addImageHilight(r.getGuid(), tfile);
 				
				enmedia.setAttribute("src", QUrl.fromLocalFile(tfile.fileName()).toString());
  				enmedia.setAttribute("en-tag", "en-media");
  				enmedia.setNodeValue("");
    			enmedia.setAttribute("guid", r.getGuid());
    			enmedia.setTagName("img");
    		}
//    	}
    	// Technically, we should do a file:// to have a proper url, but for some reason QWebPage hates
    	// them and won't generate a thumbnail image properly if we use them.
//		enmedia.setAttribute("src", QUrl.fromLocalFile(tfile.fileName()).toString());
		enmedia.setAttribute("src", tfile.fileName().toString());
		enmedia.setAttribute("en-tag", "en-media");
		enmedia.setTagName("img");
		if (r != null && r.getAttributes() != null && 
				(r.getAttributes().getSourceURL() == null || !r.getAttributes().getSourceURL().toLowerCase().startsWith("http://latex.codecogs.com/gif.latex?")))
			enmedia.setAttribute("onContextMenu", "window.jambi.imageContextMenu('" +tfile.fileName()  +"');");
		else {
			QDomElement newText = doc.createElement("a");
			enmedia.setAttribute("src", tfile.fileName().toString());
			enmedia.setAttribute("en-tag", "en-latex");
			newText.setAttribute("onMouseOver", "style.cursor='hand'");
			if (r!= null && r.getAttributes() != null && r.getAttributes().getSourceURL() != null)
				newText.setAttribute("title", r.getAttributes().getSourceURL());
			newText.setAttribute("href", "latex://"+tfile.fileName().toString());
			enmedia.parentNode().replaceChild(newText, enmedia);
			newText.appendChild(enmedia);

		}
		enmedia.setNodeValue("");
		enmedia.setAttribute("guid", resGuid);


		logger.log(logger.HIGH, "Leaving NeverNote.modifyImageTags");
    }
    
    
	// Modify tags from Evernote specific things to XHTML tags.
	private QDomDocument modifyTags(QDomDocument doc) {
		logger.log(logger.HIGH, "Entering NeverNote.modifyTags");
		if (tempFiles == null)
			tempFiles = new ArrayList<QTemporaryFile>();
		tempFiles.clear();
		QDomElement docElem = doc.documentElement();
		
		// Modify en-media tags
		QDomNodeList anchors = docElem.elementsByTagName("en-media");
		int enMediaCount = anchors.length();
		for (int i=enMediaCount-1; i>=0; --i) {
			QDomElement enmedia = anchors.at(i).toElement();
			if (enmedia.hasAttribute("type")) {
				QDomAttr attr = enmedia.attributeNode("type");
				QDomAttr hash = enmedia.attributeNode("hash");
				String[] type = attr.nodeValue().split("/");
				String appl = type[1];
				
				if (type[0] != null) {
					if (type[0].equals("image")) {
						modifyImageTags(doc, docElem, enmedia, hash);
					}
					if (!type[0].equals("image")) {
						modifyApplicationTags(doc, docElem, enmedia, hash, appl);
					}
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
			enmedia.setAttribute("contentEditable","false");
			enmedia.setAttribute("src", Global.getFileManager().getImageDirPath("encrypt.png"));
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
			String hint = enmedia.attribute("hint");
			hint = hint.replace("'","&apos;");
			enmedia.setAttribute("onClick", "window.jambi.decryptText('crypt"+Global.cryptCounter.toString()+"', '"+encryptedText+"', '"+hint+"');");
			enmedia.setAttribute("onMouseOver", "style.cursor='hand'");
			enmedia.setTagName("img");
			enmedia.removeChild(enmedia.firstChild());   // Remove the actual encrypted text
		}

		
		// Modify link tags
		anchors = docElem.elementsByTagName("a");
		enCryptLen = anchors.length();
		for (int i=0; i<anchors.length(); i++) {
			QDomElement element = anchors.at(i).toElement();
			if (!element.attribute("href").toLowerCase().startsWith("latex://"))
				element.setAttribute("title", element.attribute("href"));
			else {
				element.setAttribute("title", element.attribute("title").toLowerCase().replace("http://latex.codecogs.com/gif.latex?",""));
			}
		}

		logger.log(logger.HIGH, "Leaving NeverNote.modifyTags");
		return doc;
	}
	

	// Get an ink note image.  If an image doesn't exist then we fall back 
	// to the old ugly icon
    private boolean buildInkNote(QDomDocument doc, QDomElement docElem, QDomElement enmedia, QDomAttr hash, String appl) {
    	String resGuid = conn.getNoteTable().noteResourceTable.getNoteResourceGuidByHashHex(currentNote.getGuid(), hash.value());
    	Resource r = conn.getNoteTable().noteResourceTable.getNoteResource(resGuid, false);
 
    	// If we can't find the resource, then fall back to the old method.  We'll return & show
    	// an error later
    	if (r == null || r.getData() == null) 
    		return false;
    	
    	// If there isn't some type of error, continue on.
		if (!resourceError) {
			
			// Get a list of images in the database.  We'll use these to bulid the page.
			List<QByteArray> data = conn.getInkImagesTable().getImage(r.getGuid());
			
			// If no pictures are found, go back to & just show the icon
			if (data.size() == 0)
				return false;
			
			// We have pictures, so append them to the page.  This really isn't proper since
			// we leave the en-media tag in place, but since we can't edit the page it doesn't
			// hurt anything.
			for (int i=0; i<data.size(); i++) {
		    	QFile f = new QFile(Global.getFileManager().getResDirPath(resGuid + new Integer(i).toString()+".png"));
				f.open(OpenModeFlag.WriteOnly);
				f.write(data.get(i));
				f.close();
				QDomElement newImage = doc.createElement("img");
				newImage.setAttribute("src", QUrl.fromLocalFile(f.fileName()).toString());
				enmedia.appendChild(newImage);
			}
			return true;
		}
    	return false;
    }
	
	
    // Modify the en-media tag into an attachment
    private void modifyApplicationTags(QDomDocument doc, QDomElement docElem, QDomElement enmedia, QDomAttr hash, String appl) {
    	logger.log(logger.HIGH, "Entering NeverNote.modifyApplicationTags");
    	if (appl.equalsIgnoreCase("vnd.evernote.ink")) {
    		inkNote = true;
    	    if (buildInkNote(doc, docElem, enmedia, hash, appl))
    	    	return;
    	}
    	String resGuid = conn.getNoteTable().noteResourceTable.getNoteResourceGuidByHashHex(currentNote.getGuid(), hash.value());
    	Resource r = conn.getNoteTable().noteResourceTable.getNoteResource(resGuid, false);
    	if (r == null || r.getData() == null) 
    		resourceError = true;
		if (r!= null) {
			if (r.getData()!=null) {
				// Did we get a generic applicaiton?  Then look at the file name to 
				// try and find a good application type for the icon
				if (appl.equalsIgnoreCase("octet-stream")) {
					if (r.getAttributes() != null && r.getAttributes().getFileName() != null) {
						String fn = r.getAttributes().getFileName();
						int pos = fn.lastIndexOf(".");
						if (pos > -1) {
							appl = fn.substring(pos+1);
						}
					}
				}
				
				String fileDetails = null;
				if (r.getAttributes() != null && r.getAttributes().getFileName() != null && !r.getAttributes().getFileName().equals(""))
					fileDetails = r.getAttributes().getFileName();
				String contextFileName;
				FileManager fileManager = Global.getFileManager();
                if (fileDetails != null && !fileDetails.equals("")) {
                	if (!noteHistory) {
                		enmedia.setAttribute("href", "nnres://" +r.getGuid() 
                				+Global.attachmentNameDelimeter +fileDetails);
                		contextFileName = fileManager.getResDirPath(r.getGuid() 
                				+Global.attachmentNameDelimeter + fileDetails);
                	} else {
                		enmedia.setAttribute("href", "nnres://" +r.getGuid() + currentNote.getUpdateSequenceNum() 
                				+Global.attachmentNameDelimeter +fileDetails);
                		contextFileName = fileManager.getResDirPath(r.getGuid() + currentNote.getUpdateSequenceNum() 
                				+Global.attachmentNameDelimeter + fileDetails);
                	}
				} else { 
					if (!noteHistory) {
						enmedia.setAttribute("href", "nnres://" +r.getGuid() +currentNote.getUpdateSequenceNum()
								+Global.attachmentNameDelimeter +appl);
						contextFileName = fileManager.getResDirPath(r.getGuid() +currentNote.getUpdateSequenceNum() 
								+Global.attachmentNameDelimeter + appl);
					} else {
						enmedia.setAttribute("href", "nnres://" +r.getGuid() 
								+Global.attachmentNameDelimeter +appl);
						contextFileName = fileManager.getResDirPath(r.getGuid() 
								+Global.attachmentNameDelimeter + appl);
					}
				}
				contextFileName = contextFileName.replace("\\", "/");
				enmedia.setAttribute("onContextMenu", "window.jambi.resourceContextMenu('" +contextFileName +"');");
				if (fileDetails == null || fileDetails.equals(""))
					fileDetails = "";
				enmedia.setAttribute("en-tag", "en-media");
				enmedia.setAttribute("guid", r.getGuid());
				enmedia.setTagName("a");
				QDomElement newText = doc.createElement("img");
				boolean goodPreview = false;
				String filePath = "";
				if (appl.equalsIgnoreCase("pdf") && pdfPreview) {
					String fileName;
					Resource res = conn.getNoteTable().noteResourceTable.getNoteResource(r.getGuid(), true);
					if (res.getAttributes() != null && 
							res.getAttributes().getFileName() != null && 
							!res.getAttributes().getFileName().trim().equals(""))
						fileName = res.getGuid()+Global.attachmentNameDelimeter+res.getAttributes().getFileName();
					else
						fileName = res.getGuid()+".pdf";
					QFile file = new QFile(fileManager.getResDirPath(fileName));
			        QFile.OpenMode mode = new QFile.OpenMode();
			        mode.set(QFile.OpenModeFlag.WriteOnly);
			        file.open(mode);
			        QDataStream out = new QDataStream(file);
			        Resource resBinary = conn.getNoteTable().noteResourceTable.getNoteResource(res.getGuid(), true);
					QByteArray binData = new QByteArray(resBinary.getData().getBody());
					resBinary = null;
			        out.writeBytes(binData.toByteArray());
			        file.close();
			        PDFPreview pdfPreview = new PDFPreview();
					goodPreview = pdfPreview.setupPreview(file.fileName(), appl,0);
					if (goodPreview) {
						QDomElement span = doc.createElement("span");
						QDomElement table = doc.createElement("table");
						span.setAttribute("pdfNavigationTable", "true");
						QDomElement tr = doc.createElement("tr");
						QDomElement td = doc.createElement("td");
						QDomElement left = doc.createElement("img");
						left.setAttribute("onMouseDown", "window.jambi.nextPage('" +file.fileName() +"')");
						left.setAttribute("onMouseDown", "window.jambi.nextPage('" +file.fileName() +"')");
						left.setAttribute("onMouseOver", "style.cursor='hand'");
						QDomElement right = doc.createElement("img");
						right.setAttribute("onMouseDown", "window.jambi.nextPage('" +file.fileName() +"')");
						left.setAttribute("onMouseDown", "window.jambi.previousPage('" +file.fileName() +"')");
						// NFC TODO: should these be file:// URLs?
						left.setAttribute("src", Global.getFileManager().getImageDirPath("small_left.png"));
						right.setAttribute("src", Global.getFileManager().getImageDirPath("small_right.png"));
						right.setAttribute("onMouseOver", "style.cursor='hand'");
						
						table.appendChild(tr);
						tr.appendChild(td);
						td.appendChild(left);
						td.appendChild(right);
						span.appendChild(table);
						enmedia.parentNode().insertBefore(span, enmedia);
					} 
					filePath = fileName+".png";
				}
				String icon = findIcon(appl);
				if (icon.equals("attachment.png"))
					icon = findIcon(fileDetails.substring(fileDetails.indexOf(".")+1));
				// NFC TODO: should this be a 'file://' URL?
				newText.setAttribute("src", Global.getFileManager().getImageDirPath(icon));
				if (goodPreview) {
			        // NFC TODO: should this be a 'file://' URL?
					newText.setAttribute("src", fileManager.getResDirPathSpecialChar(filePath));
					newText.setAttribute("style", "border-style:solid; border-color:green; padding:0.5mm 0.5mm 0.5mm 0.5mm;");
				}
				newText.setAttribute("title", fileDetails);
				enmedia.removeChild(enmedia.firstChild());
				
				enmedia.appendChild(newText);
			}
		}
		logger.log(logger.HIGH, "Leaving NeverNote.modifyApplicationTags");
    }
    // Modify the en-to tag into an input field
    private void modifyTodoTags(QDomElement todo) {
    	logger.log(logger.HIGH, "Entering NeverNote.modifyTodoTags");
		todo.setAttribute("type", "checkbox");
		String checked = todo.attribute("checked");
		todo.removeAttribute("checked");
		if (checked.equalsIgnoreCase("true"))
			todo.setAttribute("checked", "");
		else
			todo.setAttribute("unchecked","");
		todo.setAttribute("value", checked);
		todo.setAttribute("onClick", "value=checked;window.jambi.contentChanged(); ");
		todo.setAttribute("onMouseOver", "style.cursor='hand'");
		todo.setTagName("input");
		logger.log(logger.HIGH, "Leaving NeverNote.modifyTodoTags");
    }
    
    
    
    // Modify any cached todo tags that may have changed
    public String modifyCachedTodoTags(String note) {
    	logger.log(logger.HIGH, "Entering NeverNote.modifyCachedTodoTags");
    	StringBuffer html = new StringBuffer(note);
		for (int i=html.indexOf("<input", 0); i>-1; i=html.indexOf("<input", i)) {
			int endPos =html.indexOf(">",i+1);
			String input = html.substring(i,endPos);
			if (input.indexOf("value=\"true\"") > 0) 
				input = input.replace(" unchecked=\"\"", " checked=\"\"");
			else
				input = input.replace(" checked=\"\"", " unchecked=\"\"");
			html.replace(i, endPos, input);
			i++;
		}
		logger.log(logger.HIGH, "Leaving NeverNote.modifyCachedTodoTags");
		return html.toString();
    }
    
    


	// Scan and do hilighting of words
	public QDomDocument addHilight(QDomDocument doc) {
//		EnSearch e = listManager.getEnSearch();
		if (enSearch.hilightWords == null || enSearch.hilightWords.size() == 0)
			return doc;
		XMLInsertHilight hilight = new XMLInsertHilight(doc, enSearch.hilightWords);
		return hilight.getDoc();
	}
	
	
    // find the appropriate icon for an attachment
    private String findIcon(String appl) {
    	logger.log(logger.HIGH, "Entering NeverNote.findIcon");
    	appl = appl.toLowerCase();
        String relativePath = appl + ".png";
        File f = Global.getFileManager().getImageDirFile(relativePath);
        if (f.exists()) {
            return relativePath;
        }
    	if (f.exists())
    		return appl+".png";
    	logger.log(logger.HIGH, "Leaving NeverNote.findIcon");
    	return "attachment.png";
    }

}
