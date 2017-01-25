package com.ibm.bluemix.appid.android.internal.loginwidget;

import android.app.Activity;
import android.support.annotation.NonNull;

import com.ibm.bluemix.appid.android.api.AuthorizationListener;
import com.ibm.bluemix.appid.android.api.LoginWidget;
import com.ibm.bluemix.appid.android.internal.OAuthManager;
import com.ibm.bluemix.appid.android.internal.authorizationmanager.AuthorizationManager;

/**
 * Created on 1/25/17.
 */

public class LoginWidgetImpl implements LoginWidget {

	private final OAuthManager oAuthManager;

	// TODO: document
	public LoginWidgetImpl(@NonNull OAuthManager oAuthManager){
		this.oAuthManager = oAuthManager;
	}

	// TODO: document
	public void launch (@NonNull final Activity activity, @NonNull final AuthorizationListener authorizationListener){
		AuthorizationManager am = oAuthManager.getAuthorizationManager();
		am.launchAuthorizationUI(activity, authorizationListener);
	}
}

