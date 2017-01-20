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

package com.ibm.bluemix.appid.android.internal;

import android.content.Context;

import com.ibm.bluemix.appid.android.api.AppID;
import com.ibm.bluemix.appid.android.internal.authorization.AuthorizationManager;
import com.ibm.bluemix.appid.android.internal.preferences.PreferenceManager;
import com.ibm.bluemix.appid.android.internal.registration.RegistrationManager;
import com.ibm.bluemix.appid.android.internal.token.TokenManager;

public class OAuthManager {

	private AppID appId;
	private PreferenceManager preferenceManager;
	private RegistrationManager registrationManager;
	private AuthorizationManager authorizationManager;
	private TokenManager tokenManager;

	public OAuthManager(Context ctx, AppID appId){
		this.appId = appId;
		this.preferenceManager = PreferenceManager.getDefaultPreferenceManager(ctx);
		this.registrationManager = new RegistrationManager(this);
		this.authorizationManager = new AuthorizationManager(this);
		this.tokenManager = new TokenManager(this);
	}

	public AppID getAppId () {
		return appId;
	}

	public PreferenceManager getPreferenceManager () {
		return preferenceManager;
	}

	public RegistrationManager getRegistrationManager () {
		return registrationManager;
	}

	public AuthorizationManager getAuthorizationManager () {
		return authorizationManager;
	}

	public TokenManager getTokenManager () {
		return tokenManager;
	}

}
