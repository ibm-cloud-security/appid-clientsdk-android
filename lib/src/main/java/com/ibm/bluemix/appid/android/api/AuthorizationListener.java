package com.ibm.bluemix.appid.android.api;

import org.json.JSONObject;

public interface AuthorizationListener {
	void onAuthorizationFailure(AuthorizationException exception);
	void onAuthorizationSuccess(JSONObject accessToken, JSONObject identityToken);
}
