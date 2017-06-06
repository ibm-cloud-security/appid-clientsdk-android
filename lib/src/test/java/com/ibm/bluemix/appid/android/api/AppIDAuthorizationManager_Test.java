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

package com.ibm.bluemix.appid.android.api;

import android.app.Activity;
import android.os.Build;

import com.ibm.bluemix.appid.android.api.tokens.AccessToken;
import com.ibm.bluemix.appid.android.api.tokens.IdentityToken;
import com.ibm.bluemix.appid.android.internal.OAuthManager;
import com.ibm.bluemix.appid.android.internal.authorizationmanager.AuthorizationManager;
import com.ibm.bluemix.appid.android.internal.tokenmanager.TokenManager;
import com.ibm.bluemix.appid.android.internal.tokens.AccessTokenImpl;
import com.ibm.bluemix.appid.android.internal.tokens.IdentityTokenImpl;
import com.ibm.bluemix.appid.android.testing.helpers.Consts;
import com.ibm.bluemix.appid.android.testing.mocks.HttpURLConnection_Mock;
import com.ibm.mobilefirstplatform.appid_clientsdk_android.BuildConfig;
import com.ibm.mobilefirstplatform.clientsdk.android.core.api.Response;
import com.ibm.mobilefirstplatform.clientsdk.android.core.api.ResponseListener;
import com.ibm.mobilefirstplatform.clientsdk.android.security.api.AppIdentity;
import com.ibm.mobilefirstplatform.clientsdk.android.security.api.DeviceIdentity;
import com.ibm.mobilefirstplatform.clientsdk.android.security.api.UserIdentity;

import org.json.JSONObject;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import static junit.framework.Assert.assertEquals;
import static org.assertj.core.api.Java6Assertions.*;
import static org.mockito.Mockito.*;

@RunWith (RobolectricTestRunner.class)
@FixMethodOrder (MethodSorters.NAME_ASCENDING)
@Config (constants = BuildConfig.class)
public class AppIDAuthorizationManager_Test {

	private AppIDAuthorizationManager appIdAuthManager;
	private static final AccessToken accessToken = new AccessTokenImpl(Consts.ACCESS_TOKEN);
	private static final IdentityToken idToken = new IdentityTokenImpl(Consts.ID_TOKEN);

	@Mock private OAuthManager oAuthManagerMock;
	@Mock private TokenManager tokenManagerMock;
	@Mock private AppID appIdMock;
	@Mock private AuthorizationManager authorizationManagerMock;

	@Before
	public void before(){
		MockitoAnnotations.initMocks(this);
		when(appIdMock.getOAuthManager()).thenReturn(oAuthManagerMock);
		when(oAuthManagerMock.getAuthorizationManager()).thenReturn(authorizationManagerMock);
		when(oAuthManagerMock.getTokenManager()).thenReturn(tokenManagerMock);
		appIdMock.initialize(RuntimeEnvironment.application, "a", "b");
		appIdAuthManager = new AppIDAuthorizationManager(appIdMock);
	}

	@Test
	public void testGetCachedAuthorizationHeader () {
		when(tokenManagerMock.getLatestIdentityToken()).thenReturn(idToken);
		when(tokenManagerMock.getLatestAccessToken()).thenReturn(accessToken);
		String cachedAuthHeader = appIdAuthManager.getCachedAuthorizationHeader();
		assertThat(cachedAuthHeader).isEqualTo("Bearer " + accessToken.getRaw() + " " + idToken.getRaw());

		when(tokenManagerMock.getLatestIdentityToken()).thenReturn(null);
		assertThat(appIdAuthManager.getCachedAuthorizationHeader()).isNull();

		when(tokenManagerMock.getLatestAccessToken()).thenReturn(null);
		assertThat(appIdAuthManager.getCachedAuthorizationHeader()).isNull();
	}

	@Test
	public void testIsAuthorizationRequired () {
		String authHeaderName = com.ibm.mobilefirstplatform.clientsdk.android.security.api.AuthorizationManager.WWW_AUTHENTICATE_HEADER_NAME;
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
		headers.put(authHeaderName, Arrays.asList("Dummy"));
		assertThat(appIdAuthManager.isAuthorizationRequired(401, headers)).isEqualTo(false);

		// 401 status, Authorization header exists, Bearer exists, but not appid_default scope
		headers.remove(authHeaderName);
		headers.put(authHeaderName, Arrays.asList("Bearer Dummy"));
		assertThat(appIdAuthManager.isAuthorizationRequired(401, headers)).isEqualTo(false);

		// Authorization required
		headers.remove(authHeaderName);
		headers.put(authHeaderName, Arrays.asList("Bearer scope=\"appid_default\""));
		assertThat(appIdAuthManager.isAuthorizationRequired(401, headers)).isEqualTo(true);

		// Check with httpUrlConnection
		HttpURLConnection_Mock httpURLConnection = new HttpURLConnection_Mock();
		try {
			assertThat(appIdAuthManager.isAuthorizationRequired(httpURLConnection)).isEqualTo(true);
		} catch (IOException e){
			assertThat(e).isNull();
		}
	}

	@Test
	public void testGetUserIdentity(){
		when(tokenManagerMock.getLatestIdentityToken()).thenReturn(null);
		assertThat(appIdAuthManager.getUserIdentity()).isNull();

		when(tokenManagerMock.getLatestIdentityToken()).thenReturn(idToken);
		UserIdentity userIdentity = appIdAuthManager.getUserIdentity();
		assertThat(userIdentity).isNotNull();
		assertThat(userIdentity.getId()).isEqualTo(idToken.getSubject());
		assertThat(userIdentity.getAuthBy()).isEqualTo(idToken.getAuthenticationMethods().get(0));
		assertThat(userIdentity.getDisplayName()).isEqualTo(idToken.getName());
	}

	@Test
	public void testGetDeviceIdentity(){
		when(tokenManagerMock.getLatestIdentityToken()).thenReturn(null);
		assertThat(appIdAuthManager.getDeviceIdentity()).isNull();

		when(tokenManagerMock.getLatestIdentityToken()).thenReturn(idToken);
		DeviceIdentity deviceIdentity = appIdAuthManager.getDeviceIdentity();
		assertThat(deviceIdentity).isNotNull();
		assertThat(deviceIdentity.getId()).isEqualTo(idToken.getOAuthClient().getDeviceId());
		assertThat(deviceIdentity.getModel()).isEqualTo(idToken.getOAuthClient().getDeviceModel());
		assertThat(deviceIdentity.getBrand()).isEqualTo(Build.BRAND);
		assertThat(deviceIdentity.getOS()).isEqualTo(idToken.getOAuthClient().getDeviceOS());
		assertThat(deviceIdentity.getOSVersion()).isEqualTo(Build.VERSION.RELEASE);
	}

	@Test
	public void testGetAppIdentity(){
		when(tokenManagerMock.getLatestIdentityToken()).thenReturn(null);
		assertThat(appIdAuthManager.getAppIdentity()).isNull();

		when(tokenManagerMock.getLatestIdentityToken()).thenReturn(idToken);
		AppIdentity appIdentity = appIdAuthManager.getAppIdentity();
		assertThat(appIdentity).isNotNull();
		assertThat(appIdentity.getId()).isEqualTo(idToken.getOAuthClient().getSoftwareId());
		assertThat(appIdentity.getVersion()).isEqualTo(idToken.getOAuthClient().getSoftwareVersion());
	}

	@Test
	public void testClearAuthorizationData(){
		appIdAuthManager.clearAuthorizationData();
		appIdAuthManager.clearAuthorizationData();
		verify(tokenManagerMock, times(2)).clearStoredTokens();

	}

	@Test
	public void testLogout(){
		appIdAuthManager.logout(RuntimeEnvironment.application, null);
		appIdAuthManager.logout(RuntimeEnvironment.application, null);
		verify(tokenManagerMock, times(2)).clearStoredTokens();
	}

	@Test
	public void obtainAuthorization_test_onAuthorizationSuccess(){
		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {
				Object[] args = invocation.getArguments();
				AuthorizationListener authorizationListener = (AuthorizationListener) args[1];
				authorizationListener.onAuthorizationSuccess(accessToken,idToken);
				return null;
			}
		}).when(authorizationManagerMock).launchAuthorizationUI(any(Activity.class), any(AuthorizationListener.class));


		appIdAuthManager.obtainAuthorization(Mockito.mock(Activity.class), new ResponseListener() {
			@Override
			public void onSuccess(Response response) {
                assertEquals(response, null);
			}

			@Override
			public void onFailure(Response response, Throwable t, JSONObject extendedInfo) {
				fail("should get to onSuccess");
			}
		}, null);
	}

	@Test
	public void obtainAuthorization_test_onAuthorizationFailure(){
		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {
				Object[] args = invocation.getArguments();
				AuthorizationListener authorizationListener = (AuthorizationListener) args[1];
				authorizationListener.onAuthorizationFailure(new AuthorizationException("test exception"));
				return null;
			}
		}).when(authorizationManagerMock).launchAuthorizationUI(any(Activity.class), any(AuthorizationListener.class));


		appIdAuthManager.obtainAuthorization(Mockito.mock(Activity.class), new ResponseListener() {
			@Override
			public void onSuccess(Response response) {
				fail("should get to onFailure");
			}

			@Override
			public void onFailure(Response response, Throwable t, JSONObject extendedInfo) {
				assertEquals(t.getMessage(), "test exception");
			}
		}, null);
	}

	@Test
	public void obtainAuthorization_test_onAuthorizationCanceled(){
		Mockito.doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable {
				Object[] args = invocation.getArguments();
				AuthorizationListener authorizationListener = (AuthorizationListener) args[1];
				authorizationListener.onAuthorizationCanceled();
				return null;
			}
		}).when(authorizationManagerMock).launchAuthorizationUI(any(Activity.class), any(AuthorizationListener.class));


		appIdAuthManager.obtainAuthorization(Mockito.mock(Activity.class), new ResponseListener() {
			@Override
			public void onSuccess(Response response) {
				fail("should get to onFailure");
			}

			@Override
			public void onFailure(Response response, Throwable t, JSONObject extendedInfo) {
				assertEquals(t.getMessage(), "Authorization canceled");
			}
		}, null);
	}

}
