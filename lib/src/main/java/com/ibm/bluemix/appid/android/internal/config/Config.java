package com.ibm.bluemix.appid.android.internal.config;

import com.ibm.bluemix.appid.android.api.AppId;

import static com.ibm.bluemix.appid.android.api.AppId.overrideServerHost;

/**
 * Created on 1/16/17.
 */

public class Config {
	private static final String serverUrlPrefix = "https://imf-authserver";

	public static String getServerUrl(AppId appId) {
		String serverUrl = serverUrlPrefix + appId.getBluemixRegionSuffix() + "/oauth/v3/";
		if (null != appId.overrideServerHost) {
			serverUrl = appId.overrideServerHost;
		}
		serverUrl += appId.getTenantId();
		return serverUrl;
	}
}
