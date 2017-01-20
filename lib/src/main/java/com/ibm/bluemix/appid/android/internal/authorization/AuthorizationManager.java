/*
	Copyright 2014-17 IBM Corp.
	Licensed under the Apache License, Version 2.0 (the "License");
	you may not use this file except in compliance with the License.
	You may obtain a copy of the License at
	http://www.apache.org/licenses/LICENSE-2.0
	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and
	limitations under the License.
*/

package com.ibm.bluemix.appid.android.internal.authorization;

import android.app.Activity;
import android.net.Uri;

import com.ibm.bluemix.appid.android.api.AppID;
import com.ibm.bluemix.appid.android.api.AuthorizationListener;
import com.ibm.bluemix.appid.android.internal.OAuthManager;
import com.ibm.bluemix.appid.android.internal.config.Config;
import com.ibm.bluemix.appid.android.internal.registration.RegistrationManager;

public class AuthorizationManager {
	private static final String OAUTH_AUTHORIZATION_PATH = "/authorization";

	private final AppID appId;
	private final OAuthManager oAuthManager;
	private final RegistrationManager registrationManager;

	private final static String CLIENT_ID = "client_id";
	private final static String RESPONSE_TYPE = "response_type";
	private final static String RESPONSE_TYPE_CODE = "code";

	private final static String SCOPE = "scope";
	private final static String SCOPE_OPENID = "openid";

	private final static String REDIRECT_URI = "redirect_uri";
	private final static String USE_LOGIN_WIDGET = "use_login_widget";

	// TODO: document
	public AuthorizationManager (final OAuthManager oAuthManager) {
		this.oAuthManager = oAuthManager;
		this.appId = oAuthManager.getAppId();
		this.registrationManager = oAuthManager.getRegistrationManager();
	}

	// TODO: document
	public String getAuthorizationUrl(boolean useLoginWidget) {
		String clientId = registrationManager.getRegistrationDataString(RegistrationManager.CLIENT_ID);
		String redirectUri = registrationManager.getRegistrationDataString(RegistrationManager.REDIRECT_URIS, 0);

		String serverUrl = Config.getServerUrl(this.appId) + OAUTH_AUTHORIZATION_PATH;
		serverUrl = Uri.parse(serverUrl).buildUpon()
				.appendQueryParameter(RESPONSE_TYPE, RESPONSE_TYPE_CODE)
				.appendQueryParameter(CLIENT_ID, clientId)
				.appendQueryParameter(REDIRECT_URI, redirectUri)
				.appendQueryParameter(SCOPE, SCOPE_OPENID)
				.appendQueryParameter(USE_LOGIN_WIDGET, String.valueOf(useLoginWidget))
				.build().toString();
		return serverUrl;
	}

	// TODO: document
	public void launchAuthorizationUI (Activity activity, AuthorizationListener authorizationListener){
		String authorizationUrl = getAuthorizationUrl(true);
		String redirectUri = registrationManager.getRegistrationDataString(RegistrationManager.REDIRECT_URIS, 0);
		AuthorizationUIManager auim = new AuthorizationUIManager(oAuthManager, authorizationListener, authorizationUrl, redirectUri);
		auim.launch(activity);
	}

}
