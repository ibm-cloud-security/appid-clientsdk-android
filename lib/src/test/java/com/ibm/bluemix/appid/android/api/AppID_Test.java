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

import com.ibm.bluemix.appid.android.internal.OAuthManager;
import com.ibm.bluemix.appid.android.internal.loginwidget.LoginWidgetImpl;
import com.ibm.bluemix.appid.android.internal.registrationmanager.RegistrationStatus;
import com.ibm.bluemix.appid.android.internal.userattributesmanager.UserAttributeManagerImpl;
import com.ibm.bluemix.appid.android.testing.helpers.ClassHelper;
import com.ibm.bluemix.appid.android.testing.helpers.Consts;
import com.ibm.bluemix.appid.android.testing.helpers.ExceptionMessageMatcher;
import com.ibm.mobilefirstplatform.appid_clientsdk_android.BuildConfig;

import org.assertj.core.api.ThrowableAssert;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.mockito.Mockito;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.assertj.core.api.Java6Assertions.catchThrowable;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;

import java.util.Locale;

@RunWith (RobolectricTestRunner.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Config (constants = BuildConfig.class)
public class AppID_Test {

	private static final String testTenantId = "testTenant";
	private static final String testRegion = "TestRegion";
	private AppID appId;

	@Before
	public void setup () {
		this.appId = AppID.getInstance();
		assertThat(this.appId).isNotNull();
	}

	@Test()
	public void test01_uninitialized(){

		Throwable thrown1 = catchThrowable(new ThrowableAssert.ThrowingCallable() {
			@Override
			public void call () throws Throwable {
				appId.getBluemixRegionSuffix();
			}
		});

		Throwable thrown2 = catchThrowable(new ThrowableAssert.ThrowingCallable() {
			@Override
			public void call () throws Throwable {
				appId.getTenantId();
			}
		});

		Throwable thrown3 = catchThrowable(new ThrowableAssert.ThrowingCallable() {
			@Override
			public void call () throws Throwable {
				appId.getLoginWidget();
			}
		});

		Throwable thrown4 = catchThrowable(new ThrowableAssert.ThrowingCallable() {
			@Override
			public void call () throws Throwable {
				appId.getUserAttributeManager();
			}
		});

		Throwable thrown5 = catchThrowable(new ThrowableAssert.ThrowingCallable() {
			@Override
			public void call () throws Throwable {
				appId.getOAuthManager();
			}
		});

		Throwable thrown6 = catchThrowable(new ThrowableAssert.ThrowingCallable() {
			@Override
			public void call () throws Throwable {
				appId.setPreferredLocale(Locale.GERMAN);
			}
		});

		assertThat(thrown1).hasMessageContaining("AppID is not initialized");
		assertThat(thrown2).hasMessageContaining("AppID is not initialized");
		assertThat(thrown3).hasMessageContaining("AppID is not initialized");
		assertThat(thrown4).hasMessageContaining("AppID is not initialized");
		assertThat(thrown5).hasMessageContaining("AppID is not initialized");
		assertThat(thrown6).hasMessageContaining("AppID is not initialized");
	}

	@Test()
	public void test02_initialized(){
		this.appId.initialize(RuntimeEnvironment.application, testTenantId, testRegion);
		assertThat(appId.getTenantId()).isEqualTo(testTenantId);
		assertThat(appId.getBluemixRegionSuffix()).isEqualTo(testRegion);
		assertThat(appId.getLoginWidget()).isNotNull();
		ClassHelper.assertSame(appId.getLoginWidget(), LoginWidgetImpl.class);
		assertThat(appId.getOAuthManager()).isNotNull();
		ClassHelper.assertSame(appId.getOAuthManager(), OAuthManager.class);
		assertThat(appId.getUserAttributeManager()).isNotNull();
		ClassHelper.assertSame(appId.getUserAttributeManager(), UserAttributeManagerImpl.class);

		AuthorizationListener listener = mock(AuthorizationListener.class);

		appId.loginAnonymously(RuntimeEnvironment.application, listener);
		appId.loginAnonymously(RuntimeEnvironment.application, "access_token", listener);
		appId.loginAnonymously(RuntimeEnvironment.application, "access_token", false, listener);

        appId.setPreferredLocale(Locale.GERMAN);

		verifyListenerFailed(3, listener);
	}

	@Test
	public void test03_loginUsingRoP(){
		this.appId.initialize(RuntimeEnvironment.application, testTenantId, testRegion);

		TokenResponseListener listener = mock(TokenResponseListener.class);

		appId.obtainTokensWithROP(RuntimeEnvironment.application, "testUsername", "testPassword", listener);

		verifyListenerFailed(1, listener);
	}

	@Test
	public void test04_loginUsingRoPWithNullAccessToken(){
		this.appId.initialize(RuntimeEnvironment.application, testTenantId, testRegion);

		TokenResponseListener listener = mock(TokenResponseListener.class);

		appId.obtainTokensWithROP(RuntimeEnvironment.application, "testUsername", "testPassword", listener, null);

		verifyListenerFailed(1, listener);
	}

	@Test
	public void test05_loginUsingRoPWithAccessToken(){
		this.appId.initialize(RuntimeEnvironment.application, testTenantId, testRegion);

		TokenResponseListener listener = mock(TokenResponseListener.class);

		appId.obtainTokensWithROP(RuntimeEnvironment.application, "testUsername", "testPassword", listener, Consts.ACCESS_TOKEN);

		verifyListenerFailed(1, listener);
	}

	@Test
	public void test06_refreshTokens(){
		this.appId.initialize(RuntimeEnvironment.application, testTenantId, testRegion);

		TokenResponseListener listener = mock(TokenResponseListener.class);

		appId.refreshTokens(RuntimeEnvironment.application, "refreshToken", listener);

		verifyListenerFailed(1, listener);
	}

	private void verifyListenerFailed(int wantedNumberOfInvocations, TokenResponseListener listener) {
		ExceptionMessageMatcher<AuthorizationException> matcher = new ExceptionMessageMatcher<>(RegistrationStatus.FAILED_TO_REGISTER.getDescription());
		Mockito.verify(listener, times(wantedNumberOfInvocations)).onAuthorizationFailure(argThat(matcher));
	}
}

