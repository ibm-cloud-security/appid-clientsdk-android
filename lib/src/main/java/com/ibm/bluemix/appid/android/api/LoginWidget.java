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
	public LoginWidget(AppId appId, AuthorizationListener authorizationListener){
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
				am.lauchAuthorizationUI(activity, authorizationListener);
			}
		});
	}
}
