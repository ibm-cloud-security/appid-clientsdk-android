/*
	Copyright 2014-17 IBM Corp.
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

package com.ibm.bluemix.appid.android.internal.registration;

import android.content.Context;
import android.util.Base64;

import com.ibm.bluemix.appid.android.api.AppId;
import com.ibm.bluemix.appid.android.internal.AppIDRequest;
import com.ibm.bluemix.appid.android.internal.config.Config;
import com.ibm.bluemix.appid.android.internal.preferences.PreferenceManager;
import com.ibm.mobilefirstplatform.clientsdk.android.core.api.Request;
import com.ibm.mobilefirstplatform.clientsdk.android.core.api.Response;
import com.ibm.mobilefirstplatform.clientsdk.android.core.api.ResponseListener;
import com.ibm.mobilefirstplatform.clientsdk.android.logger.api.Logger;
import com.ibm.mobilefirstplatform.clientsdk.android.security.api.AppIdentity;
import com.ibm.mobilefirstplatform.clientsdk.android.security.api.DeviceIdentity;
import com.ibm.mobilefirstplatform.clientsdk.android.security.identity.BaseAppIdentity;
import com.ibm.mobilefirstplatform.clientsdk.android.security.identity.BaseDeviceIdentity;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.security.KeyPair;
import java.security.interfaces.RSAPublicKey;

public class RegistrationManager {

    private static final String OAUTH_CLIENT_REGISTRATION_PATH = "/clients";
	private static final String OAUTH_CLIENT_REGISTRATION_DATA_PREF_NAME
			= "com.ibm.bluemix.appid.android.registrationdata";
	private static final String OAUTH_CLIENT_ID_PREF_NAME = "com.ibm.bluemix.appid.android.clientid";
	private static final String TENANT_ID_PREF_NAME = "com.ibm.bluemix.appid.android.tenantid";
	private static final String OAUTH_CLIENT_REDIRECT_URI_PATH = "://mobile/callback";
	private static final String OAUTH_CLIENT_ID = "client_id";

	private AppId appId;
	private PreferenceManager preferenceManager;
    private KeyPair registrationKeyPair;
    private RegistrationKeyStore registrationKeyStore;

	private static final Logger logger = Logger.getLogger(Logger.INTERNAL_PREFIX + RegistrationManager.class.getName());

    public RegistrationManager (AppId appId, PreferenceManager preferenceManager){
		this.appId = appId;
		this.preferenceManager = preferenceManager;
        this.registrationKeyStore = new RegistrationKeyStore();
    }

	public void ensureRegistered(Context context, final RegistrationListener registrationListener){
		logger.debug("-> ensureRegistered");
		String storedClientId = preferenceManager.getStringPreference(OAUTH_CLIENT_ID_PREF_NAME).get();
		String storedTenantId = preferenceManager.getStringPreference(TENANT_ID_PREF_NAME).get();

		if (storedClientId != null && appId.getTenantId().equals(storedTenantId)){
			// OAuth client is already registered
			logger.debug("OAuth client is already registered. Returning success.");
			registrationListener.onRegistrationSuccess();
		} else {
			// Oauth client is not registered yet or registered for a different tenantId.
			// Proceed with OAuth client registration flow
			logger.info("Registering a new OAuth client");
			registerOAuthClient(context, new ResponseListener() {
				@Override
				public void onFailure (Response response, Throwable t, JSONObject extendedInfo) {
					logger.error("Failed to register OAuth client", t);
					registrationListener.onRegistrationFailure("Failed to register OAuth client");
				}

				@Override
				public void onSuccess (Response response) {
					preferenceManager.getStringPreference(TENANT_ID_PREF_NAME).set(appId.getTenantId());
					logger.info("OAuth client successfully registered. Returning success.");
					registrationListener.onRegistrationSuccess();
				}
			});
		}
	}

    /**
     * Invoke request for registration, the result of the request should contain ClientId.
     *
     */
    void registerOAuthClient(Context context, final ResponseListener responseListener) {
        try {
			String registrationUrl = Config.getServerUrl(appId) + OAUTH_CLIENT_REGISTRATION_PATH;

			JSONObject reqJson =  createRegistrationParams(context);
            AppIDRequest request = new AppIDRequest(registrationUrl, Request.POST);
            request.send(reqJson, new ResponseListener() {
                @Override
                public void onSuccess(Response response) {
                    try {
						// Registration success, persist client_id to shared prefs
						String responseBody = response.getResponseText();
						JSONObject jsonResponse = new JSONObject(responseBody);
						String clientId = jsonResponse.getString(OAUTH_CLIENT_ID);
						preferenceManager.getJSONPreference(OAUTH_CLIENT_REGISTRATION_DATA_PREF_NAME).set(jsonResponse);
						preferenceManager.getStringPreference(OAUTH_CLIENT_ID_PREF_NAME).set(clientId);
                        responseListener.onSuccess(response);
                    } catch (Exception e) {
						logger.error("Failed to save OAuth client registration data", e);
                        responseListener.onFailure(null, e, null);
                    }
                }

                @Override
                public void onFailure(Response response, Throwable t, JSONObject extendedInfo) {
                    responseListener.onFailure(response ,t, extendedInfo);
                }

            });
        } catch (MalformedURLException e) {
			logger.error("Failed to create registration request", e);
            responseListener.onFailure(null, e, null);
        }
        catch (Exception e) {
			logger.error("Failed to create registration parameters", e);
            responseListener.onFailure(null, e, null);
        }
    }

    /**
     * Generate the params that will be used during the registration phase
     *
     * @return JSONObject with all the parameters
     */
    private JSONObject createRegistrationParams(Context context) throws Exception {
		logger.info("Creating OAuth client registration parameters");
        registrationKeyPair = this.registrationKeyStore.generateKeypair(context);
        JSONObject params = new JSONObject();
        DeviceIdentity deviceData = new BaseDeviceIdentity(context);
        AppIdentity applicationData = new BaseAppIdentity(context);
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
        redirectUris.put(0, applicationData.getId() + OAUTH_CLIENT_REDIRECT_URI_PATH;
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
		logger.debug("OAuth client registration parameters");
		logger.debug(params.toString(4));
        return params;
    }

    private String encodeUrlSafe(byte[] data) throws UnsupportedEncodingException {
        return new String(Base64.encode(data, Base64.URL_SAFE | Base64.NO_WRAP),"UTF-8");
    }

	public JSONObject getRegistrationData(){
		try {
			return preferenceManager.getJSONPreference(OAUTH_CLIENT_REGISTRATION_DATA_PREF_NAME).getAsJSON();
		} catch (JSONException e){
			logger.error("Failed to retrieve registration data from preferences", e);
			return null;
		}
	}

	public String getRegisteredClientId(){
		try {
			JSONObject registrationData = getRegistrationData();
			return registrationData.getString(OAUTH_CLIENT_ID);
		} catch (JSONException e){
			logger.error("Failed to retrieve " + OAUTH_CLIENT_ID, e);
			return null;
		}
	}

	public String getRegisteredRedirectUri(){
		try {
			JSONObject registrationData = getRegistrationData();
			return registrationData.getJSONArray("redirect_uris").getString(0);
		} catch (JSONException e){
			logger.error("Failed to retrieve redirect_uris", e);
			return null;
		}
	}
}
