package com.ibm.mobilefirstplatform.appid_clientsdk_android;

import android.net.Uri;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.ViewGroup;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class WebViewActivity extends AppCompatActivity {

    private WebView webView;
    private AppIdAuthorizationProcessManager appIdAPM = AppId.getInstance().appIdAuthorizationProcessManager;
    private String redirect_uri = appIdAPM.redirect_uri;

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
        webView.loadUrl(appIdAPM.getAuthorizationUrl());
    }

    //we need to override here in order to avoid window leaks
    @Override
    public void finish() {
        ViewGroup view = (ViewGroup) getWindow().getDecorView();
        view.removeAllViews();
        super.finish();
    }

    private class WebViewClientOldAPI extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(final WebView view, String url) {
            Uri uri=Uri.parse(url);
            loadUrl(view, uri);
            return true;
        }
    }

    private class WebViewClientNewAPI extends WebViewClient {
        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            Uri uri = request.getUrl();
            loadUrl(view, uri);
            return true;
        }
    }

    private void loadUrl(WebView view, Uri uri) {
        String code = uri.getQueryParameter("code");
        String url = uri.toString();
        if (url.startsWith(redirect_uri) && code != null) {
            appIdAPM.sendTokenRequest(code);
            finish();
        } else {
            if(uri.getHost().equals("localhost")) { //only needed when working locally replace localhost with 10.0.2.2
                url = "http://10.0.2.2" + url.substring(16, url.length());
            }
            view.loadUrl(url);
        }
    }

}
