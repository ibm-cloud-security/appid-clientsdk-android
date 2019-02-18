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
	public final static String REGION_US_SOUTH_OLD = ".ng.bluemix.net";
	public final static String REGION_US_EAST_OLD = ".us-east.bluemix.net";
	public final static String REGION_UK_OLD = ".eu-gb.bluemix.net";
	public final static String REGION_SYDNEY_OLD = ".au-syd.bluemix.net";
	public final static String REGION_GERMANY_OLD = ".eu-de.bluemix.net";
	public final static String REGION_TOKYO_OLD = ".jp-tok.bluemix.net";

	private final static String OAUTH_ENDPOINT = "/oauth/v4/";
	private final static String ATTRIBUTES_ENDPOINT = "/api/v1/";
	private static final String serverUrlPrefix = "https://appid-oauth";
	private static final String userProfilesPrefix = "https://appid-profiles";
	private static final String PUBLIC_KEYS_ENDPOINT = "/publickeys";
	private static final String PROTOCOL = "http";

	private Config(){}

	public static String getOAuthServerUrl (AppID appId) {
		String region = appId.getBluemixRegion();

		String serverUrl = (region != null && region.startsWith(PROTOCOL)) ? region : serverUrlPrefix + region;
		serverUrl += OAUTH_ENDPOINT;

		if (null != appId.overrideOAuthServerHost) {
			serverUrl = appId.overrideOAuthServerHost;
		}
		serverUrl += appId.getTenantId();
		return serverUrl;
	}

	public static String getUserProfilesServerUrl (AppID appId) {
		String region = appId.getBluemixRegion();
		String serverUrl = (region != null && region.startsWith(PROTOCOL)) ? region : userProfilesPrefix + region;

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

		if (null != appId.overrideOAuthServerHost) {
			String[] overrideServerUrlSplit =  appId.overrideOAuthServerHost.split("/");
			return overrideServerUrlSplit[0] + "//" + overrideServerUrlSplit[2];
		}

		String region = appId.getBluemixRegion();
		if (region == null) {
			return serverUrlPrefix;
		}

		String issuer = region.contains("cloud.ibm.com") ? serverUrlPrefix + suffixFromRegion(region) :
				Config.getOAuthServerUrl(appId);


		String[] issuerSplit = issuer.split("/");
		return issuerSplit[0] + "//" + issuerSplit[2];
	}

	private static String suffixFromRegion(String region) {
		switch (region) {
			case AppID.REGION_UK_STAGE1:
				return ".stage1" + REGION_UK_OLD;
			case AppID.REGION_US_SOUTH_STAGE1:
				return ".stage1" + REGION_US_SOUTH_OLD;
			case AppID.REGION_US_SOUTH:
				return REGION_US_SOUTH_OLD;
			case AppID.REGION_UK:
				return REGION_UK_OLD;
			case AppID.REGION_SYDNEY:
				return REGION_SYDNEY_OLD;
			case AppID.REGION_GERMANY:
				return REGION_GERMANY_OLD;
			case AppID.REGION_US_EAST:
				return REGION_US_EAST_OLD;
			case AppID.REGION_TOKYO:
				return REGION_TOKYO_OLD;
		}

		return region;
	}

}
