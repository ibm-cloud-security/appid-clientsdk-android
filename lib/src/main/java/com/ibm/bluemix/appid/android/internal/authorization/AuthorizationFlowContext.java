package com.ibm.bluemix.appid.android.internal.authorization;


import com.ibm.bluemix.appid.android.api.AuthorizationListener;
import com.ibm.bluemix.appid.android.internal.OAuthManager;

public class AuthorizationFlowContext {
	private final OAuthManager oAuthManager;
	private final AuthorizationListener authorizationListener;

	public AuthorizationFlowContext(OAuthManager oAuthManager, AuthorizationListener authorizationListener){
		this.oAuthManager = oAuthManager;
		this.authorizationListener = authorizationListener;
	}

	public OAuthManager getOAuthManager () {
		return oAuthManager;
	}

	public AuthorizationListener getAuthorizationListener () {
		return authorizationListener;
	}
}
