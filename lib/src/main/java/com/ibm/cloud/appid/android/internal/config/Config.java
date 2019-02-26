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
	private static final String PUBLIC_KEYS_ENDPOINT = "/publickeys";

	private Config(){}

	public static String getOAuthServerUrl (AppID appId) {
		String serverUrl = convertEndpoints(appId.getBluemixRegion());

		serverUrl += OAUTH_ENDPOINT;

		if (null != appId.overrideOAuthServerHost) {
			serverUrl = appId.overrideOAuthServerHost;
		}
		serverUrl += appId.getTenantId();
		return serverUrl;
	}

	public static String getUserProfilesServerUrl (AppID appId) {
		String serverUrl = convertEndpoints(appId.getBluemixRegion());

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
			return overrideServerUrlSplit[0] + "//" + overrideServerUrlSplit[2] + OAUTH_ENDPOINT + appId.getTenantId();
		}

		return Config.getOAuthServerUrl(appId);
	}


	/**
	 * converts old bluemix.net endpoints to new cloud.ibm.com endpoints
	 * @param region
	 * @return
	 */
	private static String convertEndpoints(String region) {

		if(region != null && region.contains("bluemix.net")) {
			switch (region) {
				case ".stage1" + REGION_UK_OLD:
					return AppID.REGION_UK_STAGE1;
				case ".stage1" + REGION_US_SOUTH_OLD:
					return AppID.REGION_US_SOUTH_STAGE1;
				case REGION_US_SOUTH_OLD:
					return AppID.REGION_US_SOUTH;
				case REGION_UK_OLD:
					return AppID.REGION_UK;
				case REGION_SYDNEY_OLD:
					return AppID.REGION_SYDNEY;
				case REGION_GERMANY_OLD:
					return AppID.REGION_GERMANY;
				case REGION_US_EAST_OLD:
					return AppID.REGION_US_EAST;
				case REGION_TOKYO_OLD:
					return AppID.REGION_TOKYO;
			}
		}

		return region;
	}
}
