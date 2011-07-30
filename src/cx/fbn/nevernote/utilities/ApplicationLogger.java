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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import cx.fbn.nevernote.Global;


public class ApplicationLogger {
	
	public final int LOW = 1;
	public final int MEDIUM = 2;
	public final int HIGH = 3;
	public final int EXTREME = 4;
	
	FileOutputStream fileStream;
	PrintStream		 stdoutPrintStream;
	
//	private final List<String> logText;
	
    public ApplicationLogger(String name){
//        logText = new ArrayList<String>();
        try {
			fileStream = new FileOutputStream(Global.getFileManager().getLogsDirFile(name));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
//        stdoutStream = new OutStream(new ByteArrayOutputStream(), name);
    	stdoutPrintStream  = new PrintStream(fileStream);
 //   	systemStdoutPrintStream = System.out;
    }

//    public List<String> getText() {
//    	return stdoutStream.getText();
//    }
    

	/**
	 * @return the logText
	 */
//	public List<String> getLogText() {
//		return logText;
//	}
	

    public synchronized void log(int messageLevel, String s) {
    	if (messageLevel <= Global.messageLevel) {
    		String DATE_FORMAT_NOW = "yyyy-MM-dd HH:mm:ss.SS ";
            Calendar cal = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
                        
			stdoutPrintStream.println(sdf.format(cal.getTime()) +s);
//   		System.setOut(stdoutPrintStream);
//    		System.out.println(sdf.format(cal.getTime()) +s);
//    		System.setOut(systemStdoutPrintStream);	
    	}
    }
    
    public void log(int level, StackTraceElement e[]) {
		if (level >= Global.messageLevel) {
			String DATE_FORMAT_NOW = "yyyy-MM-dd HH:mm:ss.SS ";
            Calendar cal = Calendar.getInstance();
            SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
            
//			System.setOut(systemStdoutPrintStream);
			System.out.println(e);
    		System.out.println("*** Stack Trace Requested ***");
    		System.out.println(sdf.format(cal.getTime()));
    		for (StackTraceElement element : e) {
    			System.out.println("Line Number: " +new Integer(element.getLineNumber()));
    			System.out.println("Class Name: " +element.getClassName());
    			System.out.println("Method Name:" +element.getMethodName());
    			System.out.println("File Name:" +element.getFileName());
    			System.out.println("-Next Element-");
    		}
    		System.out.println("**************************"); 
    		System.setOut(stdoutPrintStream);
			System.out.print(e);
    		System.out.print("*** Stack Trace Requested ***");
    		System.out.print(sdf.format(cal.getTime()));
    		for (StackTraceElement element : e) {
    			System.out.print("Line Number: " +new Integer(element.getLineNumber()));
    			System.out.print("Class Name: " +element.getClassName());
    			System.out.print("Method Name:" +element.getMethodName());
    			System.out.print("File Name:" +element.getFileName());
    			System.out.print("-Next Element-");
    		}
    		System.out.print("**************************"); 
		}
    }
}
