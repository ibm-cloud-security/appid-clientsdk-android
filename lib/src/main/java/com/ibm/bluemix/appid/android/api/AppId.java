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

import com.ibm.bluemix.appid.android.internal.OAuthManager;

public class AppId {

    private final String tenantId;
    private final String bluemixRegionSuffix;
	private final OAuthManager oAuthManager;

    public static String overrideServerHost = null;

    public final static String REGION_US_SOUTH = ".ng.bluemix.net";
    public final static String REGION_UK = ".eu-gb.bluemix.net";
    public final static String REGION_SYDNEY = ".au-syd.bluemix.net";


	// TODO: document
	public AppId(Context context, String tenantId, String bluemixRegionSuffix) {
		this.tenantId = tenantId;
		this.bluemixRegionSuffix = bluemixRegionSuffix;

		this.oAuthManager = new OAuthManager(context, this);
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

	protected OAuthManager getOAuthManager(){
		return oAuthManager;
	}
}
