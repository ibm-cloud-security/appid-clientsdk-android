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

package com.ibm.bluemix.appid.android;


import com.ibm.bluemix.appid.android.internal.OAuthManager;
import com.ibm.bluemix.appid.android.internal.loginwidget.LoginWidgetImpl;

public class AppID_androidTest {

//	private static final String testTenantId = "testTenant";
//	private static final String testRegion = "TestRegion";
//	private AppID appId;
//
//	public void setup () {
//		this.appId = AppID.getInstance();
//		assertThat(this.appId).isNotNull();
//	}
//
//	public void test01_uninitialized(){
//
//		Throwable thrown1 = catchThrowable(new ThrowableAssert.ThrowingCallable() {
//			@Override
//			public void call () throws Throwable {
//				appId.getBluemixRegionSuffix();
//			}
//		});
//
//		Throwable thrown2 = catchThrowable(new ThrowableAssert.ThrowingCallable() {
//			@Override
//			public void call () throws Throwable {
//				appId.getTenantId();
//			}
//		});
//
//		Throwable thrown3 = catchThrowable(new ThrowableAssert.ThrowingCallable() {
//			@Override
//			public void call () throws Throwable {
//				appId.getLoginWidget();
//			}
//		});
//
//		Throwable thrown4 = catchThrowable(new ThrowableAssert.ThrowingCallable() {
//			@Override
//			public void call () throws Throwable {
//				appId.getUserAttributeManager();
//			}
//		});
//
//		Throwable thrown5 = catchThrowable(new ThrowableAssert.ThrowingCallable() {
//			@Override
//			public void call () throws Throwable {
//				appId.getOAuthManager();
//			}
//		});
//
//		assertThat(thrown1).hasMessageContaining("AppID is not initialized");
//		assertThat(thrown2).hasMessageContaining("AppID is not initialized");
//		assertThat(thrown3).hasMessageContaining("AppID is not initialized");
//		assertThat(thrown4).hasMessageContaining("AppID is not initialized");
//		assertThat(thrown5).hasMessageContaining("AppID is not initialized");
//	}
//
//	public void test02_initialized(){
//		this.appId.initialize(RuntimeEnvironment.application, testTenantId, testRegion);
//		assertThat(appId.getTenantId()).isEqualTo(testTenantId);
//		assertThat(appId.getBluemixRegionSuffix()).isEqualTo(testRegion);
//		assertThat(appId.getLoginWidget()).isNotNull();
//		assertThat(appId.getLoginWidget().getClass().getName()).isEqualTo(LoginWidgetImpl.class.getName());
//		assertThat(appId.getOAuthManager()).isNotNull();
//		assertThat(appId.getOAuthManager().getClass().getName()).isEqualTo(OAuthManager.class.getName());
//	}
}

