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

package com.ibm.cloud.appid.android.internal.config;

import com.ibm.cloud.appid.android.api.AppID;

public class Config {
	private final static String OAUTH_ENDPOINT = "/oauth/v3/";
	private final static String ATTRIBUTES_ENDPOINT = "/api/v1/";
	private static final String serverUrlPrefix = "https://appid-oauth";
	private static final String userProfilesPrefix = "https://appid-profiles";
	private static final String PUBLIC_KEYS_ENDPOINT = "/publickeys";

	private Config(){}

	public static String getOAuthServerUrl (AppID appId) {
		String serverUrl = appId.getBluemixRegionSuffix() + OAUTH_ENDPOINT;
		if (null != appId.overrideOAuthServerHost) {
			serverUrl = appId.overrideOAuthServerHost;
		}
		serverUrl += appId.getTenantId();
		return serverUrl;
	}

	public static String getUserProfilesServerUrl (AppID appId) {
		String serverUrl = appId.getBluemixRegionSuffix();
		if (null != appId.overrideUserProfilesHost) {
			serverUrl = appId.overrideUserProfilesHost;
		}
		serverUrl += ATTRIBUTES_ENDPOINT;
		return serverUrl;
	}

	public static String getPublicKeysEndpoint (AppID appId) {
		return Config.getOAuthServerUrl(appId) + PUBLIC_KEYS_ENDPOINT;
	}

	public static String getIssuer(AppID appId) {
		return Config.getOAuthServerUrl(appId).split("/")[2];
	}

}
