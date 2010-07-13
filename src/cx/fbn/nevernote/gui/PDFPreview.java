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

package cx.fbn.nevernote.gui;

import java.awt.Image;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import com.sun.pdfview.PDFFile;
import com.sun.pdfview.PDFPage;

public class PDFPreview {

	public int getPageCount(String filePath) {
		File file = new File(filePath);
		RandomAccessFile raf;
		try {
			raf = new RandomAccessFile(file, "r");
			FileChannel channel = raf.getChannel();
			ByteBuffer buf = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size());
			PDFFile pdffile = new PDFFile(buf);
			return pdffile.getNumPages();
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
    		
    		try {
    			File file = new File(filePath);
    			RandomAccessFile raf = new RandomAccessFile(file, "r");
    			FileChannel channel = raf.getChannel();
    			ByteBuffer buf = channel.map(FileChannel.MapMode.READ_ONLY, 0, channel.size());
    			PDFFile pdffile = new PDFFile(buf);
    		
    			// draw the first page to an image
    			PDFPage page = pdffile.getPage(pageNumber);
    			//get the width and height for the doc at the default zoom 
    			Rectangle rect = new Rectangle(0,0,
                    (int)page.getBBox().getWidth(),
                    (int)page.getBBox().getHeight()); 
            
    			//generate the image
    			Image img = page.getImage(
    					rect.width, rect.height, //width & height
    					rect, // clip rect
    					null, // null for the ImageObserver
    					true, // fill background with white
    					true  // block until drawing is done
                    	);
    			ImageIcon icon = new ImageIcon(img);
    			BufferedImage bi = new BufferedImage(icon.getIconWidth(), icon.getIconHeight(), BufferedImage.TYPE_INT_RGB);
    			bi.getGraphics().drawImage(icon.getImage(), 0, 0, null);
    			File outputfile;
//    			if (pageNumber == 0)
    				outputfile = new File(filePath +".png");
//    			else
//    				outputfile = new File(filePath+"-page-"+pageNumber+".png");
    			ImageIO.write(bi, "png", outputfile);
    			return true;
    		} catch (Exception e) {}
    	}
    	return false;

    }
}
