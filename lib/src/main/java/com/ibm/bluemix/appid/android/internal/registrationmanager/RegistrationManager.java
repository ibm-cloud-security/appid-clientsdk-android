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

package com.ibm.bluemix.appid.android.internal.registrationmanager;

import android.content.Context;
import android.util.Base64;

import com.ibm.bluemix.appid.android.api.AppID;
import com.ibm.bluemix.appid.android.internal.OAuthManager;
import com.ibm.bluemix.appid.android.internal.network.AppIDRequest;
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
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.interfaces.RSAPublicKey;

public class RegistrationManager {

    private static final String OAUTH_REGISTRATION_PATH = "/clients";
	private static final String OAUTH_CLIENT_REGISTRATION_DATA_PREF
			= "com.ibm.bluemix.appid.android.REGISTRATION_DATA";
	private static final String TENANT_ID_PREF = "com.ibm.bluemix.appid.android.tenantid";
	private static final String OAUTH_CLIENT_REDIRECT_URI_PATH = ":/mobile/callback";

	public static final String CLIENT_ID = "client_id";
	public static final String CLIENT_NAME = "client_name";
	public static final String SOFTWARE_ID = "software_id";
	public static final String SOFTWARE_VERSION = "software_version";
	public static final String DEVICE_ID = "device_id";
	public static final String DEVICE_MODEL = "device_model";
	public static final String DEVICE_OS = "device_os";
	public static final String DEVICE_OS_VERSION = "device_os_version";
	public static final String CLIENT_TYPE = "client_type";
	public static final String REDIRECT_URIS = "redirect_uris";

	private AppID appId;
	private PreferenceManager preferenceManager;
    private RegistrationKeyStore registrationKeyStore;
	private RegistrationStatus status = RegistrationStatus.NOT_REGISTRED;

	private static final Logger logger = Logger.getLogger(Logger.INTERNAL_PREFIX + RegistrationManager.class.getName());

    public RegistrationManager (OAuthManager oAuthManager){
		this.appId = oAuthManager.getAppId();
		this.preferenceManager = oAuthManager.getPreferenceManager();
		this.registrationKeyStore = new RegistrationKeyStore();
    }

	public void ensureRegistered(Context context, final RegistrationListener registrationListener){
		String storedClientId = getRegistrationDataString(CLIENT_ID);
		String storedTenantId = preferenceManager.getStringPreference(TENANT_ID_PREF).get();

		if (storedClientId != null && appId.getTenantId().equals(storedTenantId)){
			// OAuth client is already registered
			logger.debug("OAuth client is already registered.");
			registrationListener.onRegistrationSuccess();
		} else {
			// Oauth client is not registered yet or registered for a different tenantId.
			// Proceed with OAuth client registration flow
			logger.info("Registering a new OAuth client");
			registerOAuthClient(context, new ResponseListener() {
				@Override
				public void onFailure (Response response, Throwable t, JSONObject extendedInfo) {
                    status = RegistrationStatus.FAILED_TO_REGISTER;
					logger.error(status.getDescription(), t);
					registrationListener.onRegistrationFailure(status);
				}

				@Override
				public void onSuccess (Response response) {
                    status = RegistrationStatus.REGISTERED_SUCCESSFULLY;
					logger.info(status.getDescription());
					registrationListener.onRegistrationSuccess();
				}
			});
		}
	}

    /**
     * Invoke request for registration, the result of the request should contain ClientId.
     *
     */
    private void registerOAuthClient(Context context, final ResponseListener responseListener) {
        try {
			String registrationUrl = Config.getOAuthServerUrl(appId) + OAUTH_REGISTRATION_PATH;

			JSONObject reqJson = createRegistrationParams(context);
			AppIDRequest request = new AppIDRequest(registrationUrl, Request.POST);
			logger.debug("Sending registration request to " + registrationUrl);
			request.send(reqJson, new ResponseListener() {
				@Override
				public void onSuccess (Response response) {
					try {
						// Registration success, persist registration data and tenantId to shared prefs
						String responseBody = response.getResponseText();
						JSONObject jsonResponse = new JSONObject(responseBody);
						preferenceManager.getJSONPreference(OAUTH_CLIENT_REGISTRATION_DATA_PREF).set(jsonResponse);
						preferenceManager.getStringPreference(TENANT_ID_PREF).set(appId.getTenantId());
						responseListener.onSuccess(response);
					} catch (Exception e) {
                        status = RegistrationStatus.FAILED_TO_SAVE_REGISTRATION_DATA;
						logger.error(status.getDescription(), e);
						responseListener.onFailure(null, e, null);
					}
				}

				@Override
				public void onFailure (Response response, Throwable t, JSONObject extendedInfo) {
					responseListener.onFailure(response, t, extendedInfo);
				}

			});
		}
        catch (Exception e) {
            status = RegistrationStatus.FAILED_TO_CREATE_REGISTRATION_PARAMETERS;
			logger.error(status.getDescription(), e);
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
		KeyPair registrationKeyPair = this.registrationKeyStore.generateKeyPair(context);
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
        redirectUris.put(0, applicationData.getId() + OAUTH_CLIENT_REDIRECT_URI_PATH);
        responseTypes.put(0, "code");
        grantTypes.put(0, "authorization_code");
        grantTypes.put(1, "password");
        params.put(REDIRECT_URIS, redirectUris);
        params.put("token_endpoint_auth_method", "client_secret_basic");
        params.put("response_types", responseTypes);
        params.put("grant_types", grantTypes);
        params.put(CLIENT_NAME, context.getApplicationInfo().loadLabel(context.getPackageManager()).toString());
        params.put(SOFTWARE_ID, applicationData.getId());
        params.put(SOFTWARE_VERSION, applicationData.getVersion());
        params.put(DEVICE_ID, deviceData.getId());
        params.put(DEVICE_MODEL, deviceData.getModel());
        params.put(DEVICE_OS, "android");
		params.put(DEVICE_OS_VERSION, deviceData.getOSVersion());
        params.put(CLIENT_TYPE, "mobileapp");
        params.put("jwks", jwks);
		logger.debug("OAuth client registration parameters");
		logger.debug(params.toString(4));
        return params;
    }

    private String encodeUrlSafe(byte[] data) throws UnsupportedEncodingException {
        return new String(Base64.encode(data, Base64.URL_SAFE | Base64.NO_WRAP),"UTF-8");
    }

	public PrivateKey getPrivateKey(){
		return registrationKeyStore.getKeyPair().getPrivate();
	}

	public JSONObject getRegistrationData(){
		try {
			return preferenceManager.getJSONPreference(OAUTH_CLIENT_REGISTRATION_DATA_PREF).getAsJSON();
		} catch (JSONException e){
			logger.error("Failed to retrieve registration data from preferences", e);
			return null;
		}
	}

	public String getRegistrationDataString(String name){
		JSONObject registrationData = getRegistrationData();
		try {
			return (registrationData == null) ? null : registrationData.getString(name);
		} catch (JSONException e){
			logger.error("Failed to retrieve " + name + " from registration data", e);
			return null;
		}
	}

	public String getRegistrationDataString(String arrayName, int arrayIndex){
		JSONObject registrationData = getRegistrationData();
		try {
			return (registrationData == null) ? null : registrationData.getJSONArray(arrayName).getString(arrayIndex);
		} catch (JSONException e){
			logger.error("Failed to retrieve " + arrayName + " from registration data", e);
			return null;
		}
	}

	public JSONObject getRegistrationDataObject(String name){
		JSONObject registrationData = getRegistrationData();
		try {
			return (registrationData == null) ? null : registrationData.getJSONObject(name);
		} catch (JSONException e){
			logger.error("Failed to retrieve " + name + " from registration data", e);
			return null;
		}
	}

	public JSONArray getRegistrationDataArray(String name){
		JSONObject registrationData = getRegistrationData();
		try {
			return (registrationData == null) ? null : registrationData.getJSONArray(name);
		} catch (JSONException e){
			logger.error("Failed to retrieve " + name + " from registration data", e);
			return null;
		}
	}

	public void clearRegistrationData(){
		preferenceManager.getStringPreference(TENANT_ID_PREF).clear();
		preferenceManager.getJSONPreference(OAUTH_CLIENT_REGISTRATION_DATA_PREF).clear();
	}

    // TODO: Remove?
	public RegistrationStatus getStatus() {
        return status;
    }
}
