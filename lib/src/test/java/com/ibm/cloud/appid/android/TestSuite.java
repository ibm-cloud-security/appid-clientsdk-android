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

package com.ibm.cloud.appid.android;

import com.ibm.cloud.appid.android.api.AppIDAuthorizationManager_Test;
import com.ibm.cloud.appid.android.api.AppID_Test;
import com.ibm.cloud.appid.android.api.AuthorizationException_Test;
import com.ibm.cloud.appid.android.api.Config_Test;
import com.ibm.cloud.appid.android.api.ConfigOld_Test;
import com.ibm.cloud.appid.android.api.tokens.AccessToken_Test;
import com.ibm.cloud.appid.android.api.tokens.IdentityToken_Test;
import com.ibm.cloud.appid.android.api.userprofile.UserProfileException_Test;
import com.ibm.cloud.appid.android.internal.authorizationmanager.AuthorizationManager_Test;
import com.ibm.cloud.appid.android.internal.authorizationmanager.AuthorizationUIManager_Test;
import com.ibm.cloud.appid.android.internal.helpers.AuthorizationHeaderHelper_Test;
import com.ibm.cloud.appid.android.internal.loginwidget.LoginWidgetImpl_Test;
import com.ibm.cloud.appid.android.internal.network.AppIDRequestFactory_Test;
import com.ibm.cloud.appid.android.internal.network.AppIDRequest_Test;
import com.ibm.cloud.appid.android.internal.preferences.JSONPreference_Test;
import com.ibm.cloud.appid.android.internal.preferences.StringPreference_Test;
import com.ibm.cloud.appid.android.internal.tokenmanager.TokenManager_Test;
import com.ibm.cloud.appid.android.internal.tokens.AbstractToken_Test;
import com.ibm.cloud.appid.android.internal.userprofilemanager.UserProfileManagerImpl_Test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)

@Suite.SuiteClasses({
		// API
		AppID_Test.class,
		AuthorizationException_Test.class,
		AccessToken_Test.class,
		IdentityToken_Test.class,
		UserProfileException_Test.class,
		AppIDAuthorizationManager_Test.class,

		// Internal
		AbstractToken_Test.class,
		AuthorizationHeaderHelper_Test.class,
		StringPreference_Test.class,
		JSONPreference_Test.class,
		Config_Test.class,
		ConfigOld_Test.class,
		AuthorizationManager_Test.class,
        TokenManager_Test.class,
		AuthorizationUIManager_Test.class,
		UserProfileManagerImpl_Test.class,
		AppIDRequest_Test.class,
		AppIDRequestFactory_Test.class,
		LoginWidgetImpl_Test.class
})
public class TestSuite {}
