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

import com.ibm.bluemix.appid.android.api.tokens.AccessToken;
import com.ibm.bluemix.appid.android.api.tokens.IdentityToken;
import com.ibm.bluemix.appid.android.internal.OAuthManager;
import com.ibm.bluemix.appid.android.internal.loginwidget.LoginWidgetImpl;
import com.ibm.bluemix.appid.android.internal.registrationmanager.RegistrationStatus;
import com.ibm.bluemix.appid.android.internal.userattributesmanager.UserAttributeManagerImpl;
import com.ibm.bluemix.appid.android.testing.helpers.ClassHelper;
import com.ibm.bluemix.appid.android.testing.helpers.Consts;
import com.ibm.mobilefirstplatform.appid_clientsdk_android.BuildConfig;

import org.assertj.core.api.ThrowableAssert;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import static org.assertj.core.api.Java6Assertions.*;

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

		assertThat(thrown1).hasMessageContaining("AppID is not initialized");
		assertThat(thrown2).hasMessageContaining("AppID is not initialized");
		assertThat(thrown3).hasMessageContaining("AppID is not initialized");
		assertThat(thrown4).hasMessageContaining("AppID is not initialized");
		assertThat(thrown5).hasMessageContaining("AppID is not initialized");
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

		AuthorizationListener listener = new AuthorizationListener() {
			@Override
			public void onAuthorizationFailure(AuthorizationException exception) {
                assertThat(exception.getMessage().equals(RegistrationStatus.FAILED_TO_REGISTER.getDescription()));
            }

			@Override
			public void onAuthorizationCanceled() {
                assert(false);
			}

			@Override
			public void onAuthorizationSuccess(AccessToken accessToken, IdentityToken identityToken) {
                assert(false);
			}
		};

		appId.loginAnonymously(RuntimeEnvironment.application, listener);
		appId.loginAnonymously(RuntimeEnvironment.application, "access_token", listener);
		appId.loginAnonymously(RuntimeEnvironment.application, "access_token", false, listener);
	}

	@Test
	public void test03_loginUsingRoP(){
		this.appId.initialize(RuntimeEnvironment.application, testTenantId, testRegion);

		TokenResponseListener listener = new TokenResponseListener() {
			@Override
			public void onAuthorizationFailure(AuthorizationException exception) {
				assertThat(exception.getMessage().equals(RegistrationStatus.FAILED_TO_REGISTER.getDescription()));
			}

			@Override
			public void onAuthorizationSuccess(AccessToken accessToken, IdentityToken identityToken) {
				assert(false);
			}
		};

		appId.obtainTokensWithROP(RuntimeEnvironment.application, "testUsername", "testPassword", listener);
	}

	@Test
	public void test04_loginUsingRoPWithNullAccessToken(){
		this.appId.initialize(RuntimeEnvironment.application, testTenantId, testRegion);

		TokenResponseListener listener = new TokenResponseListener() {
			@Override
			public void onAuthorizationFailure(AuthorizationException exception) {
				assertThat(exception.getMessage().equals(RegistrationStatus.FAILED_TO_REGISTER.getDescription()));
			}

			@Override
			public void onAuthorizationSuccess(AccessToken accessToken, IdentityToken identityToken) {
				assert(false);
			}
		};

		appId.obtainTokensWithROP(RuntimeEnvironment.application, "testUsername", "testPassword", listener, null);
	}

	@Test
	public void test05_loginUsingRoPWithAccessToken(){
		this.appId.initialize(RuntimeEnvironment.application, testTenantId, testRegion);

		TokenResponseListener listener = new TokenResponseListener() {
			@Override
			public void onAuthorizationFailure(AuthorizationException exception) {
				assertThat(exception.getMessage().equals(RegistrationStatus.FAILED_TO_REGISTER.getDescription()));
			}

			@Override
			public void onAuthorizationSuccess(AccessToken accessToken, IdentityToken identityToken) {
				assert(false);
			}
		};

		appId.obtainTokensWithROP(RuntimeEnvironment.application, "testUsername", "testPassword", listener, Consts.ACCESS_TOKEN);
	}
}

