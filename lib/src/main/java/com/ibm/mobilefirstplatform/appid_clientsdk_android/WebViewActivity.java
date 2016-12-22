package com.ibm.mobilefirstplatform.appid_clientsdk_android;

import android.net.Uri;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.ViewGroup;
import android.webkit.CookieManager;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.json.JSONException;
import org.json.JSONObject;

public class WebViewActivity extends AppCompatActivity {

    //Default return code when cancel is pressed during authentication.
    private static final String AUTH_CANCEL_CODE = "100";
    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);
        webView = (WebView) findViewById(R.id.webView1);
        webView.getSettings().setJavaScriptEnabled(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            webView.setWebViewClient(new WebViewClientNewAPI());
        }else{
            webView.setWebViewClient(new WebViewClientOldAPI());
        }
        webView.clearCache(true);
        webView.loadUrl(AppIdAuthorizationManager.getInstance().getAuthorizationUrl());
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
        JSONObject cancelInfo = new JSONObject();
        try{
            cancelInfo.put("errorCode", AUTH_CANCEL_CODE);
            cancelInfo.put("msg", "Authentication canceled by user");
            AppIdAuthorizationManager.getInstance().handleAuthorizationFailure(null, null, cancelInfo);
            finish();
        } catch (JSONException e) {
            e.printStackTrace();
        }
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
        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            Uri uri = request.getUrl();
            loadUri(view, uri);
            return true;
        }
    }

    private void loadUri(WebView view, Uri uri) {
        String code = uri.getQueryParameter("code");
        String url = uri.toString();
        if (url.startsWith(AppIdRegistrationManager.redirectUri) && code != null) {
            AppIdTokenManager appIdTM = AppIdAuthorizationManager.getInstance().getAppIdTokenManager();
            appIdTM.sendTokenRequest(code);
            finish();
        } else {
            //when working locally uncomment this 'if' (replacing localhost with 10.0.2.2)
//            if(AppId.overrideServerHost != null && uri.getHost().equals("localhost")) {
//                //when working locally replacing localhost with 10.0.2.2
//                url = AppId.overrideServerHost + url.substring(21, url.length());
//            }
            view.loadUrl(url);
        }
    }
}
