package com.ibm.bluemix.appid.android.api;

import com.ibm.bluemix.appid.android.testing.mocks.HttpURLConnection_Mock;
import com.ibm.mobilefirstplatform.appid_clientsdk_android.BuildConfig;
import com.ibm.mobilefirstplatform.clientsdk.android.security.api.AuthorizationManager;

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import static org.assertj.core.api.Java6Assertions.*;

@RunWith (RobolectricTestRunner.class)
@FixMethodOrder (MethodSorters.NAME_ASCENDING)
@Config (constants = BuildConfig.class)
public class AppIDAuthorizationManager_Test {

	private AppID appId;
	private AppIDAuthorizationManager appIdAuthManager;

	@Before
	public void before(){
		appId = AppID.getInstance();
		appId.initialize(RuntimeEnvironment.application, "a", "b");

		appIdAuthManager = new AppIDAuthorizationManager(appId);
	}

	@Test
	public void testIsAuthorizationRequired () {
		Map<String, List<String>> headers;

		headers = new HashMap<>();
		headers.put("Dummy", Arrays.asList("Dummy"));

		// Non-401 status
		assertThat(appIdAuthManager.isAuthorizationRequired(200, null)).isEqualTo(false);

		// 401 status, but headers are null
		assertThat(appIdAuthManager.isAuthorizationRequired(401, null)).isEqualTo(false);

		// 401 status, no Authorization header
		assertThat(appIdAuthManager.isAuthorizationRequired(401, headers)).isEqualTo(false);

		// 401 status, Authorization header exist, but invalid value
		headers.put(AuthorizationManager.WWW_AUTHENTICATE_HEADER_NAME, Arrays.asList("Dummy"));
		assertThat(appIdAuthManager.isAuthorizationRequired(401, headers)).isEqualTo(false);

		// 401 status, Authorization header exists, Bearer exists, but not appid_default scope
		headers.remove(AuthorizationManager.WWW_AUTHENTICATE_HEADER_NAME);
		headers.put(AuthorizationManager.WWW_AUTHENTICATE_HEADER_NAME, Arrays.asList("Bearer Dummy"));
		assertThat(appIdAuthManager.isAuthorizationRequired(401, headers)).isEqualTo(false);

		// Authorization required
		headers.remove(AuthorizationManager.WWW_AUTHENTICATE_HEADER_NAME);
		headers.put(AuthorizationManager.WWW_AUTHENTICATE_HEADER_NAME, Arrays.asList("Bearer scope=\"appid_default\""));
		assertThat(appIdAuthManager.isAuthorizationRequired(401, headers)).isEqualTo(true);

		// Check with httpUrlConnection
		HttpURLConnection_Mock httpURLConnection = new HttpURLConnection_Mock();
		try {
			assertThat(appIdAuthManager.isAuthorizationRequired(httpURLConnection)).isEqualTo(true);
		} catch (IOException e){
			assertThat(e).isNull();
		}
	}

}
