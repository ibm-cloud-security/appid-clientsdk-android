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

package com.ibm.bluemix.appid.android.api;

import android.content.Context;

import com.ibm.bluemix.appid.android.internal.preferences.PreferenceManager;

public class AppId {

    private final String tenantId;
    private final String bluemixRegionSuffix;
	protected final PreferenceManager preferenceManager;
//	private AppIdAuthorizationManager appIdAuthorizationManager;
//	private static final String facebookRealm = "wl_facebookRealm";
//	private static final String googleRealm = "wl_googleRealm";

//    protected static String redirectUri;
    public static String overrideServerHost = null;
    public final static String REGION_US_SOUTH = ".ng.bluemix.net";
    public final static String REGION_UK = ".eu-gb.bluemix.net";
    public final static String REGION_SYDNEY = ".au-syd.bluemix.net";

    public AppId(Context context, String tenantId, String bluemixRegionSuffix) {
		this.tenantId = tenantId;
		this.bluemixRegionSuffix = bluemixRegionSuffix;
		this.preferenceManager = PreferenceManager.getDefaultPreferenceManager(context);
		//instance.appIdAuthorizationManager = AppIdAuthorizationManager.createInstance(context);
		//instance.preferences = instance.appIdAuthorizationManager.getPreferences();
		//AppId.redirectUri = instance.appIdAuthorizationManager.getAppIdentity().getId() + "://mobile/callback";
    }

    /**
     * @return The AppId instance tenantId
     */
    public String getTenantId() {
        return tenantId;
    }

    /**
     * @return Bluemix region suffix ,use to build URLs
     */
    public String getBluemixRegionSuffix() {
        return bluemixRegionSuffix;
    }


}
