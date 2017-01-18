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

package com.ibm.bluemix.appid.android.api;

import android.app.Activity;

import com.ibm.bluemix.appid.android.internal.OAuthManager;
import com.ibm.bluemix.appid.android.internal.authorization.AuthorizationManager;
import com.ibm.bluemix.appid.android.internal.registration.RegistrationListener;
import com.ibm.bluemix.appid.android.internal.registration.RegistrationManager;

public class LoginWidget {

	private final AuthorizationListener authorizationListener;
	private final OAuthManager oAuthManager;

	// TODO: document
	public LoginWidget(AppID appId, AuthorizationListener authorizationListener){
		this.authorizationListener = authorizationListener;
		this.oAuthManager = appId.getOAuthManager();
	}

	// TODO: document
	public void launch (final Activity activity){
		RegistrationManager rm = oAuthManager.getRegistrationManager();

		rm.ensureRegistered(activity.getApplicationContext(), new RegistrationListener() {
			@Override
			public void onRegistrationFailure (String message) {
				authorizationListener.onAuthorizationFailure(new AuthorizationException(message));
			}

			@Override
			public void onRegistrationSuccess () {
				AuthorizationManager am = oAuthManager.getAuthorizationManager();
				am.launchAuthorizationUI(activity, authorizationListener);
			}
		});
	}
}
