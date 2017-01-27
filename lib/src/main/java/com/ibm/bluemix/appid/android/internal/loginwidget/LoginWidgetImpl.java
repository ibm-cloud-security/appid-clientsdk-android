/*
	Copyright 2017 IBM Corp.
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

package com.ibm.bluemix.appid.android.internal.loginwidget;

import android.app.Activity;
import android.support.annotation.NonNull;

import com.ibm.bluemix.appid.android.api.AuthorizationListener;
import com.ibm.bluemix.appid.android.api.LoginWidget;
import com.ibm.bluemix.appid.android.internal.OAuthManager;
import com.ibm.bluemix.appid.android.internal.authorizationmanager.AuthorizationManager;

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

