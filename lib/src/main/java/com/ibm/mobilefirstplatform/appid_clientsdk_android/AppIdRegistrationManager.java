package com.ibm.mobilefirstplatform.appid_clientsdk_android;

import android.content.Context;
import android.provider.Settings;
import android.util.Base64;

import com.ibm.mobilefirstplatform.clientsdk.android.core.api.Request;
import com.ibm.mobilefirstplatform.clientsdk.android.core.api.Response;
import com.ibm.mobilefirstplatform.clientsdk.android.core.api.ResponseListener;
import com.ibm.mobilefirstplatform.clientsdk.android.security.api.AppIdentity;
import com.ibm.mobilefirstplatform.clientsdk.android.security.api.DeviceIdentity;
import com.ibm.mobilefirstplatform.clientsdk.android.security.identity.BaseAppIdentity;
import com.ibm.mobilefirstplatform.clientsdk.android.security.identity.BaseDeviceIdentity;
import com.ibm.mobilefirstplatform.clientsdk.android.security.mca.internal.preferences.AuthorizationManagerPreferences;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.security.KeyPair;
import java.security.interfaces.RSAPublicKey;

/**
 * Created by rotembr on 08/12/2016.
 */

public class AppIdRegistrationManager {

    private static final String registrationPath = "/oauth/v3/";
    private AuthorizationManagerPreferences preferences;
    private KeyPair registrationKeyPair;
    private AppIdKeyStore appIdKeyStore;

    AppIdRegistrationManager(Context context, AuthorizationManagerPreferences preferences){
        this.preferences = preferences;
        this.appIdKeyStore = new AppIdKeyStore();
    }

    /**
     *
     * @return the certificate store
     */
    public AppIdKeyStore getAppIdKeyStore(){
        return appIdKeyStore;
    }

    /**
     * Invoke request for registration, the result of the request should contain ClientId.
     *
     */
    void invokeInstanceRegistrationRequest(Context context, final ResponseListener responseListener) {
        try {
            JSONObject reqJson =  createRegistrationParams(context);
            AppIDRequest request = new AppIDRequest(getRegistrationUrl(), Request.POST);
            request.send(reqJson, new ResponseListener() {
                @Override
                public void onSuccess(Response response) {
                    try {
                        saveClientId(response);
                        responseListener.onSuccess(response);
                    } catch (Exception e) {
                        responseListener.onFailure(null, e, null);
                        throw new RuntimeException("Failed to save certificate from response", e);
                    }
                }

                @Override
                public void onFailure(Response response, Throwable t, JSONObject extendedInfo) {
                    responseListener.onFailure(response ,t, extendedInfo);
                }

            });
        }catch (MalformedURLException e) {
            responseListener.onFailure(null, e, null);
            throw new RuntimeException("Failed to create registration request", e);
        }
        catch (Exception e) {
            responseListener.onFailure(null, e, null);
            throw new RuntimeException("Failed to create registration params", e);
        }
    }

    private String getRegistrationUrl() {
        return AppIdAuthorizationManager.getInstance().getServerHost() + registrationPath + AppId.getInstance().getTenantId() + "/clients";
    }

    /**
     * Generate the params that will be used during the registration phase
     *
     * @return JSONObject with all the parameters
     */
    private JSONObject createRegistrationParams(Context context) throws Exception {
        registrationKeyPair = this.appIdKeyStore.generateKeypair(context);
        JSONObject params = new JSONObject();
        DeviceIdentity deviceData = new BaseDeviceIdentity(preferences.deviceIdentity.getAsMap());
        AppIdentity applicationData = new BaseAppIdentity(preferences.appIdentity.getAsMap());
        JSONArray redirectUris = new JSONArray();
        JSONArray responseTypes = new JSONArray();
        JSONArray grantTypes = new JSONArray();
        JSONArray keys = new JSONArray();
        JSONObject key = new JSONObject();
        key.put("e", encodeUrlSafe(((RSAPublicKey)registrationKeyPair.getPublic()).getPublicExponent().toByteArray()));
        key.put("n", encodeUrlSafe(((RSAPublicKey)registrationKeyPair.getPublic()).getModulus().toByteArray()));
        key.put("kty", registrationKeyPair.getPublic().getAlgorithm());
        keys.put(0, key);
        JSONObject jwks = new JSONObject();
        jwks.put("keys", keys);
        redirectUris.put(0, AppId.redirectUri);
        responseTypes.put(0, "code");
        grantTypes.put(0, "authorization_code");
        grantTypes.put(1, "password");
        params.put("redirect_uris", redirectUris);
        params.put("token_endpoint_auth_method", "client_secret_basic");
        params.put("response_types", responseTypes);
        params.put("grant_types", grantTypes);
        params.put("client_name", context.getApplicationInfo().loadLabel(context.getPackageManager()).toString());
        params.put("software_id", applicationData.getId());
        params.put("software_version", applicationData.getVersion());
        params.put("device_id", deviceData.getId());
        params.put("device_model", deviceData.getModel());
        params.put("device_os", deviceData.getOS());
        params.put("client_type", "mobileapp");
        params.put("jwks", jwks);
        return params;
    }

    private String encodeUrlSafe(byte[] data) throws UnsupportedEncodingException {
        return new String(Base64.encode(data, Base64.URL_SAFE | Base64.NO_WRAP),"UTF-8");
    }

    /**
     * Extract the certificate data from response and save it on local storage
     *
     * @param response contains the certificate data
     */
    private void saveClientId(Response response) throws JSONException {
        String responseBody = response.getResponseText();
        JSONObject jsonResponse = new JSONObject(responseBody);
        //save the clientId
        preferences.clientId.set(jsonResponse.getString("client_id"));

    }

}
