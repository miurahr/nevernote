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

import java.util.HashMap;

public class StringUtils {

	  private StringUtils() {}
	  
	  private static HashMap<String,String> htmlEntities;
	  static {
	    htmlEntities = new HashMap<String,String>();
	    htmlEntities.put("&lt;","<")    ; htmlEntities.put("&gt;",">");
	    htmlEntities.put("&amp;","&")   ; htmlEntities.put("&quot;","\"");
	    htmlEntities.put("&agrave;","à"); htmlEntities.put("&agrave;","À");
	    htmlEntities.put("&acirc;","â") ; htmlEntities.put("&auml;","ä");
	    htmlEntities.put("&auml;","Ä")  ; htmlEntities.put("&acirc;","Â");
	    htmlEntities.put("&aring;","å") ; htmlEntities.put("&aring;","Å");
	    htmlEntities.put("&aelig;","æ") ; htmlEntities.put("&aElig;","Æ" );
	    htmlEntities.put("&ccedil;","ç"); htmlEntities.put("&ccedil;","Ç");
	    htmlEntities.put("&eacute;","é"); htmlEntities.put("&eacute;","É" );
	    htmlEntities.put("&egrave;","è"); htmlEntities.put("&egrave;","È");
	    htmlEntities.put("&ecirc;","ê") ; htmlEntities.put("&ecirc;","Ê");
	    htmlEntities.put("&euml;","ë")  ; htmlEntities.put("&euml;","Ë");
	    htmlEntities.put("&iuml;","ï")  ; htmlEntities.put("&iuml;","Ï");
	    htmlEntities.put("&ocirc;","ô") ; htmlEntities.put("&ocirc;","Ô");
	    htmlEntities.put("&ouml;","ö")  ; htmlEntities.put("&ouml;","Ö");
	    htmlEntities.put("&oslash;","ø") ; htmlEntities.put("&oslash;","Ø");
	    htmlEntities.put("&szlig;","ß") ; htmlEntities.put("&ugrave;","ù");
	    htmlEntities.put("&ugrave;","Ù"); htmlEntities.put("&ucirc;","û");
	    htmlEntities.put("&ucirc;","Û") ; htmlEntities.put("&uuml;","ü");
	    htmlEntities.put("&uuml;","Ü")  ; htmlEntities.put("&nbsp;"," ");
	    htmlEntities.put("&copy;","\u00a9"); htmlEntities.put("&apos;", "'");
	    htmlEntities.put("&reg;","\u00ae"); htmlEntities.put("&iexcl;", "\u00a1");
	    htmlEntities.put("&euro;","\u20a0"); htmlEntities.put("&cent;", "\u00a2");
	    htmlEntities.put("&pound;", "\u00a3"); htmlEntities.put("&curen;", "\u00a4");
	    htmlEntities.put("&yen;", "\u00a5"); htmlEntities.put("&brvbar;", "\u00a6");
	    htmlEntities.put("&sect;", "\u00a7"); htmlEntities.put("&uml;", "\u00a8");
	    htmlEntities.put("&copy;", "\u00a9"); htmlEntities.put("&ordf;", "\u00aa");
	    htmlEntities.put("&laqo;", "\u00ab"); htmlEntities.put("&not;", "\u00ac");
	    htmlEntities.put("&reg;", "\u00ae"); htmlEntities.put("&macr;", "\u00af");
	  }


	  
	  public static final String unescapeHTML(String source, int start){
		     int i,j;

		     i = source.indexOf("&", start);
		     while (i>-1) {
		        j = source.indexOf(";" ,i);
		        if (j > i) {
		           String entityToLookFor = source.substring(i , j + 1);
		           String value = htmlEntities.get(entityToLookFor);
		           if (value != null) {
		        	   value = " ";
		        	   source = new StringBuffer().append(source.substring(0 , i).toLowerCase())
	                                   .append(value)
	                                   .append(source.substring(j + 1))
	                                   .toString();
		        	   i = source.indexOf("&", i+1);
		           }
		        }
		     }
		     return source;
		  }

	  
	  public static final String unescapeHTML2(String source, int start){
	     int i,j;

	     i = source.indexOf("&", start);
	     if (i > -1) {
	        j = source.indexOf(";" ,i);
	        if (j > i) {
	           String entityToLookFor = source.substring(i , j + 1);
	           String value = htmlEntities.get(entityToLookFor);
	           if (value != null) {
	        	   value = " ";
	        	   source = new StringBuffer().append(source.substring(0 , i).toLowerCase())
                                   .append(value)
                                   .append(source.substring(j + 1))
                                   .toString();
                return unescapeHTML(source, i + 1); // recursive call
	           }
	        }
	     }
	     return source;
	  }
}
