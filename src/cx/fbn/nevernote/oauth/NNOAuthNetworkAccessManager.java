
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

/* This class is used to listen to inbound network requests.  It 
 * examines all of them to look for the OAuth reply.  This reply
 * is what we need to get access to Evernote.
 */

package cx.fbn.nevernote.oauth;

import com.trolltech.qt.core.QIODevice;
import com.trolltech.qt.core.QObject;
import com.trolltech.qt.network.QNetworkAccessManager;
import com.trolltech.qt.network.QNetworkReply;
import com.trolltech.qt.network.QNetworkRequest;

import cx.fbn.nevernote.utilities.ApplicationLogger;

public class NNOAuthNetworkAccessManager extends QNetworkAccessManager {
	public Signal1<String> tokenFound;
	private ApplicationLogger logger;

	public NNOAuthNetworkAccessManager(ApplicationLogger l){
		super();
		tokenFound = new Signal1<String>();
		logger = l;
	}

	public NNOAuthNetworkAccessManager(QObject qObject){
		super(qObject);
		tokenFound = new Signal1<String>();
	}

	@Override
	protected QNetworkReply createRequest(Operation op,
			QNetworkRequest request, QIODevice outgoingData) {

		logger.log(logger.EXTREME,"NNOAuthNetworkAccessManager URL request scheme: " 
				+request.url().scheme() + " " + request.url().toString());
		String searchReq = "nnoauth?oauth_token=";
		int pos = request.url().toString().indexOf(searchReq);
		if (pos>0) {
			String token = request.url().toString().substring(pos+searchReq.length());
			tokenFound.emit(token);
		}
		return super.createRequest(op, request, outgoingData);
	}		
}