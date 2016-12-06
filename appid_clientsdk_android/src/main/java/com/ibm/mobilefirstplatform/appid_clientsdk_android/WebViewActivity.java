package com.ibm.mobilefirstplatform.appid_clientsdk_android;

import android.net.Uri;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.view.ViewGroup;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.ibm.mobilefirstplatform.clientsdk.android.core.api.Request;
import com.ibm.mobilefirstplatform.clientsdk.android.core.api.Response;
import com.ibm.mobilefirstplatform.clientsdk.android.core.api.ResponseListener;
import com.ibm.mobilefirstplatform.clientsdk.android.core.internal.ResponseImpl;
import com.ibm.mobilefirstplatform.clientsdk.android.security.mca.internal.AuthorizationRequest;
import com.ibm.mobilefirstplatform.clientsdk.android.security.mca.internal.AuthorizationRequestManager;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Signature;
import java.util.HashMap;
import java.util.Map;

import static com.ibm.mobilefirstplatform.appid_clientsdk_android.AppId.overrideServerHost;

public class WebViewActivity extends AppCompatActivity {

    private WebView webView;
    private static final String tenantId = AppId.getInstance().getTenantId();
    private String serverHost = getServerHost();
    private String redirect_uri = "http://localhost/code";

    private String serverName = "https://imf-authserver";
    private String authorizationPath = "/oauth/v2/authorization";
    private String tokenPath = "/oauth/v2/token";


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
        //Rotem: ask if that ok for performance?
        String queryParams = "?";
        queryParams += "response_type=code";
        queryParams += "&client_id=" + tenantId;
        queryParams += "&redirect_uri="+ redirect_uri;
        queryParams += "&scope=openid";
        queryParams += "&use_login_widget=true";
        webView.loadUrl(serverHost + authorizationPath + queryParams);
    }

    //we need to override here in order to avoid window leaks
    @Override
    public void finish() {
        ViewGroup view = (ViewGroup) getWindow().getDecorView();
        view.removeAllViews();
        super.finish();
    }

    /**
     * @return the authentication server host name
     */
    private String getServerHost(){
        String serverHost = serverName + AppId.getInstance().getBluemixRegionSuffix();
        if(null != overrideServerHost){
            serverHost = overrideServerHost;
        }
        return serverHost;
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
            sendTokenRequest(code);
            finish();
        } else {
            if(uri.getHost().equals("localhost")) { //only needed when working localy replace localhost with 10.0.2.2
                url = serverHost + url.substring(21, url.length());
            }
            view.loadUrl(url);
        }
    }

    private void sendTokenRequest(String code) {
        String header = createTokenRequestHeaders();
        HashMap<String, String> params = new HashMap<>();
        params.put("code", code);
        params.put("client_id", tenantId);
        params.put("grant_type", "authorization_code");
        params.put("redirect_uri", redirect_uri);
        AuthorizationRequestManager.RequestOptions options = new AuthorizationRequestManager.RequestOptions();
        options.parameters = params;
        AuthorizationRequest request = null;
        try {
            request = new AuthorizationRequest(serverHost + tokenPath, Request.POST);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        request.addHeader("authorization", header);

        request.send(options.parameters, new ResponseListener() {
            @Override
            public void onSuccess(Response response) {
                saveTokenFromResponse(response);
                AppId.getInstance().handleAuthorizationSuccess(response);
            }
            @Override
            public void onFailure(Response response, Throwable t, JSONObject extendedInfo) {
                AppId.getInstance().handleAuthorizationFailure(response, t, extendedInfo);
            }
        });
    }



    private String createTokenRequestHeaders() {
//        JSONObject payload = new JSONObject();

//
//        String d = tenantId + ":YjQ0M2YzNjMtZTRlYy00N2ZlLWFjNjUtMDA4YjNkNjBhMTFj";
//
//        byte[] data = new byte[0];
//        try {
//            data = d.getBytes("UTF-8");
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//        }
//        String base64 = Base64.encodeToString(data, Base64.NO_WRAP);
//        try{
//            headers = new HashMap<>(1);
//            headers.put("authorization", "basic " + base64);
//        }
//        catch (Exception e) {
//            throw new RuntimeException("Failed to create token request headers", e);
//        }

//        try {
//            payload.put("code", grantCode);
//
//            KeyPair keyPair = certificateStore.getStoredKeyPair();
//            String jws = jsonSigner.sign(keyPair, payload);
//
//            headers = new HashMap<>(1);
//            headers.put("X-WL-Authenticate", jws);
//
//        } catch (Exception e) {
//            throw new RuntimeException("Failed to create token request headers", e);
//        }

        PrivateKey privateKey = AppId.getInstance().appIdauthorizationProcessManager.getClientPrivateKey();
        String userName = tenantId + "-" + AppId.getInstance().preferences.clientId.get();
        String tokenAuthHeader = null;
        try {
            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initSign(privateKey);
            signature.update(userName.getBytes());
            String password = Base64.encodeToString(signature.sign(),Base64.NO_WRAP);
            tokenAuthHeader = "Basic " + Base64.encodeToString((userName + ":" + password).getBytes(),Base64.NO_WRAP);
            return tokenAuthHeader;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return tokenAuthHeader;
    }



    /**
     * Extract token from response and save it locally
     * @param response response that contain the token
     */
    private void saveTokenFromResponse(Response response) {
        try {
            JSONObject responseJSON = ((ResponseImpl)response).getResponseJSON();
            String accessToken = responseJSON.getString("access_token");
            String idToken = responseJSON.getString("id_token");
            //save the tokens
            AppId.getInstance().preferences.accessToken.set(accessToken);
            AppId.getInstance().preferences.idToken.set(idToken);
            //save the user identity separately
            String[] idTokenData = idToken.split("\\.");
            byte[] decodedIdTokenData = Base64.decode(idTokenData[1], Base64.DEFAULT);
            String decodedIdTokenString = new String(decodedIdTokenData);
            JSONObject idTokenJSON = new JSONObject(decodedIdTokenString);
            if (idTokenJSON.has("imf.user")) {
                AppId.getInstance().preferences.userIdentity.set(idTokenJSON.getJSONObject("imf.user"));
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to save token from response", e);
        }
    }
}
