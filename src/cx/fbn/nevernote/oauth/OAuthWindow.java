
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


/* This method is used to present the user with the web view of Evernote
 * that they need to grant permission to Nixnote.
 */


package cx.fbn.nevernote.oauth;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import com.trolltech.qt.core.QUrl;
import com.trolltech.qt.gui.QDialog;
import com.trolltech.qt.gui.QGridLayout;
import com.trolltech.qt.gui.QIcon;
import com.trolltech.qt.network.QSslSocket;
import com.trolltech.qt.webkit.QWebView;

import cx.fbn.nevernote.Global;
import cx.fbn.nevernote.utilities.ApplicationLogger;

public class OAuthWindow extends QDialog {
	private final static String consumerKey = "baumgarr"; 
	private final static String consumerSecret = "60d4cdedb074b0ac";
	public String response;

	private final String temporaryCredUrl;	  
	private final String permanentCredUrl;


	static final String urlBase = "https://"+Global.getServer();

	public boolean error;
	public String errorMessage;

	static final String requestTokenUrl = urlBase + "/oauth";
	static final String accessTokenUrl = urlBase + "/oauth";
	static final String authorizationUrlBase = urlBase + "/OAuth.action";
	private final String iconPath = new String("classpath:cx/fbn/nevernote/");
	private final QWebView tempPage;
	private final QWebView authPage;
	private final QGridLayout grid;
	private NNOAuthNetworkAccessManager manager;

	static final String callbackUrl = "index.jsp?action=callbackReturn";
	private final ApplicationLogger logger;


	// Constructor.
	public OAuthWindow(ApplicationLogger l) {
		logger = l;
		int millis = (int) System.currentTimeMillis();
		int time = millis / 1000;


		// Create the URLs needed for authentication with Evernote
		temporaryCredUrl = "https://"+Global.getServer() + "/oauth?oauth_consumer_key=" +consumerKey + "&oauth_signature=" +
				consumerSecret + "%26&oauth_signature_method=PLAINTEXT&oauth_timestamp="+String.valueOf(time)+
				"&oauth_nonce="+String.valueOf(millis) +"&oauth_callback=nnoauth";

		permanentCredUrl = "https://"+Global.getServer() + "/oauth?oauth_consumer_key=" +consumerKey + "&oauth_signature=" +
				consumerSecret + "%26&oauth_signature_method=PLAINTEXT&oauth_timestamp="+String.valueOf(time)+
				"&oauth_nonce="+String.valueOf(millis) +"&oauth_token=";


		// Build the window
		setWindowTitle(tr("Please Grant Nixnote Access"));
		setWindowIcon(new QIcon(iconPath+"icons/password.png"));
		grid = new QGridLayout();
		setLayout(grid);
		tempPage = new QWebView();
		authPage = new QWebView();
		grid.addWidget(authPage);
		tempPage.loadFinished.connect(this, "temporaryCredentialsReceived()");

		error = false;
		errorMessage = "";
		
		// Check that SSL sockets are supported
		logger.log(logger.MEDIUM, "SSL Sockets Supported: " +QSslSocket.supportsSsl());
		if (!QSslSocket.supportsSsl()) {
			errorMessage = new String(tr("SSL Support not found.  Aborting connection"));
			error = true;
		}
		
		// Load the temporary URL to start the authentication procesess.  When 
		// finished, this QWebView will contain the URL to start the
		// authentication process.
		QUrl tu = new QUrl(temporaryCredUrl);
		tempPage.load(tu);
	}

	
	// This method is triggered when the temporary credentials are received from Evernote
	public void temporaryCredentialsReceived() {
		logger.log(logger.MEDIUM, "Temporary Credentials Received");
		String contents = tempPage.page().mainFrame().toPlainText();
		logger.log(logger.MEDIUM, "Temporary Credentials:" +contents);
		int index = contents.indexOf("&oauth_token_secret");
		if (index > 0) {
			contents = contents.substring(0,index);
			QUrl accessUrl = new QUrl(urlBase+"/OAuth.action?" +contents);
			manager = new NNOAuthNetworkAccessManager(logger);
			authPage.page().setNetworkAccessManager(manager);
			manager.tokenFound.connect(this, "tokenFound(String)");

			authPage.load(accessUrl);  
			grid.addWidget(authPage);
		} else {
			error = true;
			errorMessage = new String(tr("OAuth error retrieving temporary token"));
			this.close();
		}
	}

	// This method is signaled when NNOAuthNetworkAccessManager finds an OAuth token
	// in the network request.
	public void tokenFound(String token) {
		logger.log(logger.MEDIUM, "*** TOKEN *** " +token);
		if (token.indexOf("auth_verifier") <= 0) {
			errorMessage = new String(tr("Error receiving authorization"));
			error = true;
			this.close();
		}
		tempPage.disconnect();
		tempPage.loadFinished.connect(this, "permanentCredentialsReceived()");
		logger.log(logger.HIGH,"Permanent URL: " +permanentCredUrl+token);
		tempPage.load(new QUrl(permanentCredUrl+token));
	}

	
	// This method is used when the permanent credentials are finally
	// received to grant access to Evernote.
	public void permanentCredentialsReceived() {
		String contents = tempPage.page().mainFrame().toPlainText();
		if (contents.startsWith("oauth_token=S%3D")) {
			logger.log(logger.HIGH, "Permanent Credentials:" +contents);
			String decoded;
			try {
				response = "";
				decoded = URLDecoder.decode(contents,"UTF-8");
				logger.log(logger.HIGH, "Decoded URL:"+decoded);
				response = decoded;
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}

			this.close();
		}
	}

}
