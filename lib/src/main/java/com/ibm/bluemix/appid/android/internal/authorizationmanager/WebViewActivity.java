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

import android.net.Uri;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.ViewGroup;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.ibm.bluemix.appid.android.api.AuthorizationException;
import com.ibm.bluemix.appid.android.api.AuthorizationListener;
import com.ibm.bluemix.appid.android.internal.OAuthManager;
import com.ibm.mobilefirstplatform.appid_clientsdk_android.R;
import com.ibm.mobilefirstplatform.clientsdk.android.logger.api.Logger;

public class WebViewActivity extends AppCompatActivity {

	private AuthorizationListener authorizationListener;
	private OAuthManager oAuthManager;
	private String redirectUrl;

	private static final Logger logger = Logger.getLogger(Logger.INTERNAL_PREFIX + WebViewActivity.class.getName());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
		logger.debug("onCreate");
        setContentView(R.layout.activity_web_view);
		WebView webView = (WebView) findViewById(R.id.webView1);
        webView.getSettings().setJavaScriptEnabled(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            webView.setWebViewClient(new WebViewClientNewAPI());
        }else{
            webView.setWebViewClient(new WebViewClientOldAPI());
        }
        webView.clearCache(true);

		String serverUrl = getIntent().getStringExtra(AuthorizationUIManager.EXTRA_URL);
		this.redirectUrl = getIntent().getStringExtra(AuthorizationUIManager.EXTRA_REDIRECT_URL);

		String authFlowContextGuid = getIntent().getStringExtra(AuthorizationUIManager.EXTRA_AUTH_FLOW_CONTEXT_GUID);
		AuthorizationFlowContext ctx = AuthorizationFlowContextStore.remove(authFlowContextGuid);

		this.oAuthManager = ctx.getOAuthManager();
		this.authorizationListener = ctx.getAuthorizationListener();

		logger.debug("serverUrl: " + serverUrl);
		logger.debug("redirectUrl: " + redirectUrl);

		if (authorizationListener == null || serverUrl == null || this.redirectUrl == null){
			logger.error("Failed to retrieve one of the following: authorizationListener, serverUrl, redirectUrl");
			finish();
		} else {
			webView.loadUrl(serverUrl);
		}
    }

    //override here in order to avoid window leaks
    @Override
    public void finish() {
        ViewGroup view = (ViewGroup) getWindow().getDecorView();
        view.removeAllViews();
        super.finish();
    }

    @Override
    public void onBackPressed() {
		finish();
		authorizationListener.onAuthorizationCanceled();
    }

    private class WebViewClientOldAPI extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(final WebView view, String url) {
            Uri uri = Uri.parse(url);
            loadUri(view, uri);
            return true;
        }
    }

    private class WebViewClientNewAPI extends WebViewClient {
        @RequiresApi (api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            Uri uri = request.getUrl();
            loadUri(view, uri);
            return true;
        }
    }

    private void loadUri(WebView view, Uri uri) {
		String url = uri.toString();
		String code = uri.getQueryParameter("code");
        String error = uri.getQueryParameter("error");
		if (url.startsWith(redirectUrl) && code != null) {
			logger.debug("Grant code received from authorization server.");
            finish();
			oAuthManager.getTokenManager().obtainTokens(code, authorizationListener);

        } else if (url.startsWith(redirectUrl) && error != null){
            String errorCode = uri.getQueryParameter("error_code");
            String errorDescription = uri.getQueryParameter("error_description");
            logger.error("error: " + error);
            logger.error("errorCode: " + errorCode);
            logger.error("errorDescription: " + errorDescription);
            authorizationListener.onAuthorizationFailure(new AuthorizationException("Failed to obtain access and identity tokens"));
            finish();
        } else {
            //when working locally uncomment this 'if' (replacing localhost with 10.0.2.2)
          //  if(AppID.overrideOAuthServerHost != null && uri.getHost().equals("localhost")) {
               //when working locally replacing localhost with 10.0.2.2
         //       url = AppID.overrideOAuthServerHost.replace("/oauth/v3/","") + url.substring(21, url.length());
        //    }
            view.loadUrl(url);
        }
    }
}
