package com.ibm.bluemix.appid.android.api;

import com.ibm.bluemix.appid.android.api.tokens.AccessToken;
import com.ibm.bluemix.appid.android.api.tokens.IdentityToken;

public interface AuthorizationListener {
	void onAuthorizationFailure(AuthorizationException exception);
	void onAuthorizationCanceled();
	void onAuthorizationSuccess(AccessToken accessToken, IdentityToken identityToken);
}
