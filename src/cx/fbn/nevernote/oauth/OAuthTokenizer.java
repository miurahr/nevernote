
/*
  * This file is part of NixNote 
 * Copyright 2012 Randy Baumgarte
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



/* This class is used to parse out the OAuth reply from Evernote */

package cx.fbn.nevernote.oauth;

import java.util.ArrayList;

public class OAuthTokenizer {

	  public String oauth_token;
	  public String edam_shard;
	  public String edam_userId;
	  public String edam_expires;
	  public String edam_noteStoreUrl;
	  public String edam_webApiUrlPrefix;
	  
	  public OAuthTokenizer() {
		  oauth_token = new String();
		  edam_shard = new String();
		  edam_userId = new String();
		  edam_expires = new String();
		  edam_noteStoreUrl = new String();
		  edam_webApiUrlPrefix = new String();
	  }
	
	  public void tokenize(String decoded) {
		ArrayList<String> tokens = new ArrayList<String>();
		for (;decoded!=null && decoded.length()>0;) {
			int i=decoded.indexOf("&");
			if (i>0) {
				tokens.add(decoded.substring(0,i));
				decoded=decoded.substring(i+1);
			} else {
				tokens.add(decoded);
				decoded="";
			}
		}
		String oauth_tokenString = "oauth_token=";
		String edam_shardString = "edam_shard=";
		String edam_userIdString = "edam_userid=";
		String edam_expiresString = "edam_expires=";
		String edam_noteStoreUrlString ="edam_notestoreurl=";
		String edam_webApiUrlPrefixString = "edam_webapiurlprefix=";
		oauth_token = "";
		edam_shard = "";
		edam_userId = "";
		edam_expires = "";
		edam_noteStoreUrl = "";
		edam_webApiUrlPrefix = "";
		
		for (int i=0; i<tokens.size(); i++) {
			String token = tokens.get(i);
			if (token.toLowerCase().startsWith(oauth_tokenString)) {
				oauth_token = token.substring(oauth_tokenString.length());
			}
			if (token.toLowerCase().startsWith(edam_shardString)) {
				edam_shard = token.substring(edam_shardString.length());
			}
			if (token.toLowerCase().startsWith(edam_userIdString)) {
				edam_userId = token.substring(edam_userIdString.length());
			}
			if (token.toLowerCase().startsWith(edam_expiresString)) {
				edam_expires = token.substring(edam_expiresString.length());
			}
			if (token.toLowerCase().startsWith(edam_noteStoreUrlString)) {
				edam_noteStoreUrl = token.substring(edam_noteStoreUrlString.length());
			}
			if (token.toLowerCase().startsWith(edam_webApiUrlPrefixString)) {
				edam_webApiUrlPrefix = token.substring(edam_webApiUrlPrefixString.length());
			}
		}

	}
}
