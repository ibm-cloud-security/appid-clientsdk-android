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

package com.ibm.bluemix.appid.android.internal.tokens;

import com.ibm.bluemix.appid.android.api.tokens.IdentityToken;
import com.ibm.bluemix.appid.android.api.tokens.OAuthClient;
import com.ibm.mobilefirstplatform.clientsdk.android.logger.api.Logger;

import org.json.JSONException;
import org.json.JSONObject;

public class OAuthClientImpl implements OAuthClient {

	private final JSONObject oauthClient;
	private final static Logger logger = Logger.getLogger(Logger.INTERNAL_PREFIX + OAuthClientImpl.class.getName());
	private final static String OAUTH_CLIENT = "oauth_client";
	private final static String TYPE = "type";
	private final static String NAME = "name";
	private final static String SOFTWARE_ID = "software_id";
	private final static String SOFTWARE_VERSION = "software_version";
	private final static String DEVICE_ID = "device_id";
	private final static String DEVICE_MODEL = "device_model";
	private final static String DEVICE_OS = "device_os";

	public OAuthClientImpl(IdentityToken identityToken){
		try {
			oauthClient = identityToken.getPayload().getJSONObject(OAUTH_CLIENT);
		} catch (JSONException e){
			logger.error("Failed to parse " + OAUTH_CLIENT, e);
			throw new RuntimeException("Failed to parse " + OAUTH_CLIENT);
		}
	}

	@Override
	public String getType () {
		return getStringValue(TYPE);
	}

	@Override
	public String getName () {
		return getStringValue(NAME);
	}

	@Override
	public String getSoftwareId () {
		return getStringValue(SOFTWARE_ID);
	}

	@Override
	public String getSoftwareVersion () {
		return getStringValue(SOFTWARE_VERSION);
	}

	@Override
	public String getDeviceId () {
		return getStringValue(DEVICE_ID);
	}

	@Override
	public String getDeviceModel () {
		return getStringValue(DEVICE_MODEL);
	}

	@Override
	public String getDeviceOS () {
		return getStringValue(DEVICE_OS);
	}

	private String getStringValue(String name){
		try {
			return oauthClient.getString(name);
		} catch (JSONException e){
			logger.error("Failed to retrieve " + name, e);
			return null;
		}
	}
}
