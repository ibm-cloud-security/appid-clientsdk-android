package com.ibm.mobilefirstplatform.appid_clientsdk_android;

import android.util.Base64;

import com.ibm.mobilefirstplatform.clientsdk.android.core.api.Request;
import com.ibm.mobilefirstplatform.clientsdk.android.core.api.Response;
import com.ibm.mobilefirstplatform.clientsdk.android.core.api.ResponseListener;
import com.ibm.mobilefirstplatform.clientsdk.android.core.internal.ResponseImpl;
import com.ibm.mobilefirstplatform.clientsdk.android.security.mca.internal.AuthorizationRequest;
import com.ibm.mobilefirstplatform.clientsdk.android.security.mca.internal.AuthorizationRequestManager;
import com.ibm.mobilefirstplatform.clientsdk.android.security.mca.internal.preferences.AuthorizationManagerPreferences;

import org.json.JSONObject;

import java.net.MalformedURLException;
import java.security.PrivateKey;
import java.security.Signature;
import java.util.HashMap;

/**
 * Created by rotembr on 11/12/2016.
 */

public class AppIdTokenManager {

    private static final String tokenPath = "/oauth/v3/token";
    private AuthorizationManagerPreferences preferences;

    AppIdTokenManager(AuthorizationManagerPreferences preferences) {
        this.preferences = preferences;
    }

    void sendTokenRequest(String code) {
        String header = createTokenRequestHeaders();
        HashMap<String, String> params = new HashMap<>();
        params.put("code", code);
        params.put("client_id", AppId.getInstance().getTenantId());
        params.put("grant_type", "authorization_code");
        params.put("redirect_uri", AppIdAuthorizationManager.redirect_uri);
        AuthorizationRequestManager.RequestOptions options = new AuthorizationRequestManager.RequestOptions();
        options.parameters = params;
        AuthorizationRequest request = null;
        try {
            request = new AuthorizationRequest(getTokenUrl(), Request.POST);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        request.addHeader("authorization", header);
        request.send(options.parameters, new ResponseListener() {
            @Override
            public void onSuccess(Response response) {
                saveTokenFromResponse(response);
                AppIdAuthorizationManager.getInstance().handleAuthorizationSuccess(response);
            }

            @Override
            public void onFailure(Response response, Throwable t, JSONObject extendedInfo) {
                AppIdAuthorizationManager.getInstance().handleAuthorizationFailure(response, t, extendedInfo);
            }
        });
    }

    private String getTokenUrl() {
        return AppIdAuthorizationManager.getInstance().getServerHost() + tokenPath;
    }

    private String createTokenRequestHeaders() {
        String tokenAuthHeader = null;
        try {
            String userName = AppId.getInstance().getTenantId() + "-" + preferences.clientId.get();
            AppIdRegistrationManager appIdRM = AppIdAuthorizationManager.getInstance().getAppIdRegistrationManager();
            PrivateKey privateKey = appIdRM.getCertificateStore().getStoredKeyPair().getPrivate();
            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initSign(privateKey);
            signature.update(userName.getBytes());
            String password = Base64.encodeToString(signature.sign(), Base64.NO_WRAP);
            tokenAuthHeader = "Basic " + Base64.encodeToString((userName + ":" + password).getBytes(), Base64.NO_WRAP);
            return tokenAuthHeader;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return tokenAuthHeader;
    }

    /**
     * Extract token from response and save it locally
     *
     * @param response response that contain the token
     */
    private void saveTokenFromResponse(Response response) {
        try {
            JSONObject responseJSON = ((ResponseImpl) response).getResponseJSON();
            String accessToken = responseJSON.getString("access_token");
            String idToken = responseJSON.getString("id_token");
            //save the tokens
            preferences.accessToken.set(accessToken);
            preferences.idToken.set(idToken);
            //save the user identity separately
            String[] idTokenData = idToken.split("\\.");
            byte[] decodedIdTokenData = Base64.decode(idTokenData[1], Base64.URL_SAFE);
            String decodedIdTokenString = new String(decodedIdTokenData);
            JSONObject idTokenJSON = new JSONObject(decodedIdTokenString);
            if (idTokenJSON.has("imf.user")) {
                preferences.userIdentity.set(idTokenJSON.getJSONObject("imf.user"));
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to save token from response", e);
        }
    }
}
