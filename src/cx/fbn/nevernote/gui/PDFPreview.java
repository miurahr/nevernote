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

package cx.fbn.nevernote.gui;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;

public class PDFPreview {

	public int getPageCount(String filePath) {
		try {
	    	String whichOS = System.getProperty("os.name");
			if (whichOS.contains("Windows")) {
				filePath = filePath.replace("\\","/");
			}    		
	    	PDDocument document = null;
			document = PDDocument.load( filePath );
			return document.getNumberOfPages();
		} catch (Exception e) {
			return 0;
		}
		
	}
	
    // Setup the preview for PDFs
    public boolean setupPreview(String filePath, String appl, int pageNumber) {
		// Fix stupid Windows file separation characters
    	String whichOS = System.getProperty("os.name");
		if (whichOS.contains("Windows")) {
			filePath = filePath.replace("\\","/");
		}
    	if (appl.equals("pdf")) {
    		
    		PDDocument document = null;
    		try {
				document = PDDocument.load( filePath );
				if (document.getNumberOfPages() <= pageNumber)
					return false;
				if (document.getDocumentCatalog().getAllPages().size() <= pageNumber)
					return false;
				PDPage page = (PDPage)document.getDocumentCatalog().getAllPages().get( pageNumber );
				BufferedImage bi = page.convertToImage();
				
				File outputfile;
				outputfile = new File(filePath +".png");
				ImageIO.write(bi, "png", outputfile);
				return true;
    		
			} catch (IOException e1) {
				return false;
			}
    	}
    	return false;

    }
}
