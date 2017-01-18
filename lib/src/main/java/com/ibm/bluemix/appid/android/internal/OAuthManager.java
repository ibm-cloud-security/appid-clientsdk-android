package com.ibm.bluemix.appid.android.internal;

import android.content.Context;

import com.ibm.bluemix.appid.android.api.AppId;
import com.ibm.bluemix.appid.android.internal.authorization.AuthorizationManager;
import com.ibm.bluemix.appid.android.internal.preferences.PreferenceManager;
import com.ibm.bluemix.appid.android.internal.registration.RegistrationManager;
import com.ibm.bluemix.appid.android.internal.token.TokenManager;

public class OAuthManager {

	private AppId appId;
	private PreferenceManager preferenceManager;
	private RegistrationManager registrationManager;
	private AuthorizationManager authorizationManager;
	private TokenManager tokenManager;

	public OAuthManager(Context ctx, AppId appId){
		this.appId = appId;
		this.preferenceManager = PreferenceManager.getDefaultPreferenceManager(ctx);
		this.registrationManager = new RegistrationManager(this);
		this.authorizationManager = new AuthorizationManager(this);
		this.tokenManager = new TokenManager(this);
	}

	public AppId getAppId () {
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
