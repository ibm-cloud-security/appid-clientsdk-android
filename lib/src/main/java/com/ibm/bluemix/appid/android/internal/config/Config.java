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

package com.ibm.bluemix.appid.android.internal.config;

import com.ibm.bluemix.appid.android.api.AppID;

public class Config {
	private static final String serverUrlPrefix = "https://imf-authserver";

	public static String getServerUrl(AppID appId) {
		String serverUrl = serverUrlPrefix + appId.getBluemixRegionSuffix() + "/oauth/v3/";
		if (null != appId.overrideServerHost) {
			serverUrl = appId.overrideServerHost;
		}
		serverUrl += appId.getTenantId();
		return serverUrl;
	}
}
