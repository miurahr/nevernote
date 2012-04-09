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

package cx.fbn.nevernote.utilities;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import cx.fbn.nevernote.Global;

public class OutStream extends FilterOutputStream {

	List<String> buffer;
	File file;
	FileOutputStream fos;
	DataOutputStream dos;
	
	public OutStream(OutputStream out, String name) {
		super(out);
		buffer = new ArrayList<String>();
		
		file = Global.getFileManager().getLogsDirFile(name);
		try {
			fos = new FileOutputStream(file);
			dos = new DataOutputStream(fos);
		} catch (FileNotFoundException e) {}
	}
	
	
	@Override
	public synchronized void write(byte b[]) throws IOException {
	        String aString = new String(b);
	        buffer.add(aString);
	        dos.writeBytes(aString +"\n");
	    }

	@Override
	public synchronized void write(byte b[], int off, int len) throws IOException {
	    String aString = new String(b , off , len);
	    buffer.add(aString);
	    dos.writeBytes(aString +"\n");
	}
	
	public List<String> getText() {
		return buffer;
	}

}
