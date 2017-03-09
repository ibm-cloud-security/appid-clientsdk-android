package com.ibm.bluemix.appid.android.api;

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
		when(appId.getBluemixRegionSuffix()).thenReturn(".region.com");
		when(appId.getTenantId()).thenReturn("tenant-id");
	}

	@Test
	public void testConfig(){
		String url = com.ibm.bluemix.appid.android.internal.config.Config.getOAuthServerUrl(appId);
		assertThat(url).isEqualTo("https://appid-oauth.region.com/oauth/v3/tenant-id");

		url = com.ibm.bluemix.appid.android.internal.config.Config.getUserProfilesServerUrl(appId);
		assertThat(url).isEqualTo("https://appid-profiles.region.com/api/v1/");

		appId.overrideOAuthServerHost = "oauth-server-host-";
		appId.overrideUserProfilesHost = "user-profiles-host";

		url = com.ibm.bluemix.appid.android.internal.config.Config.getOAuthServerUrl(appId);
		assertThat(url).isEqualTo("oauth-server-host-tenant-id");

		url = com.ibm.bluemix.appid.android.internal.config.Config.getUserProfilesServerUrl(appId);
		assertThat(url).isEqualTo("user-profiles-host/api/v1/");
	}
}
