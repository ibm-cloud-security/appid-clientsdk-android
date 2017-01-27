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

package com.ibm.bluemix.appid.android.internal.authorizationmanager;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.support.customtabs.CustomTabsIntent;


import com.ibm.bluemix.appid.android.api.AuthorizationException;
import com.ibm.bluemix.appid.android.api.AuthorizationListener;
import com.ibm.bluemix.appid.android.internal.OAuthManager;
import com.ibm.mobilefirstplatform.clientsdk.android.logger.api.Logger;


public class ChromeTabActivity extends Activity {

	public static final String EXTRA_REDIRECT_URI = "com.ibm.bluemix.appid.android.EXTRA_REDIRECT_URI";
	public static final String INTENT_GOT_HTTP_REDIRECT = "com.ibm.bluemix.appid.android.GOT_HTTP_REDIRECT";
	private static final String INTENT_ALREADY_USED = "com.ibm.bluemix.appid.android.INTENT_ALREADY_USED";
	private static final String POST_AUTHORIZATION_INTENT = "com.ibm.bluemix.appid.android.POST_AUTHORIZATION_INTENT";

	private BroadcastReceiver broadcastReceiver;
	private AuthorizationListener authorizationListener;
	private OAuthManager oAuthManager;
	private String redirectUrl;

	private static final Logger logger = Logger.getLogger(Logger.INTERNAL_PREFIX + ChromeTabActivity.class.getName());

	@Override
	public void onCreate (Bundle savedInstanceBundle) {
		logger.debug("onCreate");
		super.onCreate(savedInstanceBundle);
		Intent intent = getIntent();
		if (!POST_AUTHORIZATION_INTENT.equals(intent.getAction())) {

			String serverUrl = getIntent().getStringExtra(AuthorizationUIManager.EXTRA_URL);
			this.redirectUrl = getIntent().getStringExtra(AuthorizationUIManager.EXTRA_REDIRECT_URL);

			String authFlowContextGuid = getIntent().getStringExtra(AuthorizationUIManager.EXTRA_AUTH_FLOW_CONTEXT_GUID);
			AuthorizationFlowContext ctx = AuthorizationFlowContextStore.remove(authFlowContextGuid);
			this.oAuthManager = ctx.getOAuthManager();
			this.authorizationListener = ctx.getAuthorizationListener();

			logger.debug("serverUrl: " + serverUrl);
			logger.debug("redirectUrl: " + redirectUrl);


			CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
			builder.enableUrlBarHiding();
			CustomTabsIntent customTabsIntent = builder.build();

			customTabsIntent.intent.setPackage(AuthorizationUIManager.getPackageNameToUse(this.getApplicationContext()));
			customTabsIntent.intent.addFlags(PendingIntent.FLAG_ONE_SHOT);

			//This will launch the chrome tab
			Uri uri = Uri.parse(serverUrl);
			logger.debug("launching custom tab with url: " + uri.toString());
			customTabsIntent.launchUrl(this, uri);

			broadcastReceiver = new BroadcastReceiver() {
				@Override
				public void onReceive (Context context, Intent intent) {
					onBroadcastReceived(intent);
				}
			};

			IntentFilter intentFilter = new IntentFilter(INTENT_GOT_HTTP_REDIRECT);
			this.registerReceiver(broadcastReceiver, intentFilter);

		} else {
			//if we launch after authorization completed
			finish();
		}
	}

	private void onBroadcastReceived (Intent intent){
		Uri uri = intent.getParcelableExtra(ChromeTabActivity.EXTRA_REDIRECT_URI);
		String url = uri.toString();
		String code = uri.getQueryParameter("code");
		String error = uri.getQueryParameter("error");

		logger.info("onBroadcastReceived: " + url);

		Intent clearTopActivityIntent = new Intent(POST_AUTHORIZATION_INTENT);
		clearTopActivityIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

		if (url.startsWith(redirectUrl) && code != null) {
			logger.debug("Grant code received from authorization server.");
			oAuthManager.getTokenManager().obtainTokens(code, authorizationListener);

			startActivity(clearTopActivityIntent);
		} else if (url.startsWith(redirectUrl) && error != null){
			String errorCode = uri.getQueryParameter("error_code");
			String errorDescription = uri.getQueryParameter("error_description");
			logger.error("error: " + error);
			logger.error("errorCode: " + errorCode);
			logger.error("errorDescription: " + errorDescription);
			authorizationListener.onAuthorizationFailure(new AuthorizationException("Failed to obtain access and identity tokens"));
			startActivity(clearTopActivityIntent);
		}
	}

	@Override
	protected void onResume () {
		super.onResume();
		Intent intent = getIntent();
		if (!intent.hasExtra(INTENT_ALREADY_USED)) {
			// First time onResume called we add INTENT_ALREADY_USED=true
			// to indicate that this intent was already active for the next time,
			// after the chrome tab closes by user.
			intent.putExtra(INTENT_ALREADY_USED, true);
		} else {
			// User cancelled authentication
			finish();
			authorizationListener.onAuthorizationCanceled();
		}
	}

	@Override
	protected void onDestroy () {
		if (broadcastReceiver != null) {
			unregisterReceiver(broadcastReceiver);
		}
		super.onDestroy();
	}
}
