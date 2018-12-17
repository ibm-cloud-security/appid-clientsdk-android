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
package com.ibm.cloud.appid.android.api;

import com.ibm.mobilefirstplatform.appid_clientsdk_android.BuildConfig;

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.mockito.Mockito.*;
import static org.assertj.core.api.Java6Assertions.*;

@RunWith (RobolectricTestRunner.class)
@FixMethodOrder (MethodSorters.NAME_ASCENDING)
@Config (constants = BuildConfig.class)
public class Config_Test {

	@Mock private AppID appId;

	@Before
	public void before() {
		MockitoAnnotations.initMocks(this);
		when(appId.getBluemixRegion()).thenReturn("https://us-south.appid.cloud.ibm.com");
		when(appId.getTenantId()).thenReturn("tenant-id");
	}

	@Test
	public void testConfig(){
		String url = com.ibm.cloud.appid.android.internal.config.Config.getOAuthServerUrl(appId);
		assertThat(url).isEqualTo("https://us-south.appid.cloud.ibm.com/oauth/v3/tenant-id");

		url = com.ibm.cloud.appid.android.internal.config.Config.getUserProfilesServerUrl(appId);
		assertThat(url).isEqualTo("https://us-south.appid.cloud.ibm.com/api/v1/");

		url = com.ibm.cloud.appid.android.internal.config.Config.getIssuer(appId);
		assertThat(url).isEqualTo("appid-oauth.ng.bluemix.net");

		appId.overrideOAuthServerHost = "oauth-server-host-";
		appId.overrideUserProfilesHost = "user-profiles-host";

		url = com.ibm.cloud.appid.android.internal.config.Config.getOAuthServerUrl(appId);
		assertThat(url).isEqualTo("oauth-server-host-tenant-id");

		url = com.ibm.cloud.appid.android.internal.config.Config.getUserProfilesServerUrl(appId);
		assertThat(url).isEqualTo("user-profiles-host/api/v1/");

		// need to reset server hosts, because they are global variables that can impact other tests
        appId.overrideOAuthServerHost = null;
        appId.overrideUserProfilesHost = null;
	}
}
