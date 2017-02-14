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

import com.ibm.bluemix.appid.android.api.AppIDAuthorizationManager_Test;
import com.ibm.bluemix.appid.android.api.AppID_Test;
import com.ibm.bluemix.appid.android.api.AuthorizationException_Test;
import com.ibm.bluemix.appid.android.api.tokens.OAuthClient_Test;
import com.ibm.bluemix.appid.android.api.userattributes.UserAttributesException_Test;
import com.ibm.bluemix.appid.android.internal.helpers.AuthorizationHeaderHelper_Test;
import com.ibm.bluemix.appid.android.internal.tokens.AbstractToken_Test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)

@Suite.SuiteClasses({
		// API
		AppID_Test.class,
		AuthorizationException_Test.class,
////		AccessToken_Test.class,
////		IdentityToken_Test.class,
		OAuthClient_Test.class,
		UserAttributesException_Test.class,
		AppIDAuthorizationManager_Test.class,

		// Internal
		AbstractToken_Test.class,
		AuthorizationHeaderHelper_Test.class
})
public class TestSuite {}
