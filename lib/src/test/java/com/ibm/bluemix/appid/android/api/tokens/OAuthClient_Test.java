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

package com.ibm.bluemix.appid.android.api.tokens;
import com.ibm.bluemix.appid.android.internal.tokens.IdentityTokenImpl;
import com.ibm.bluemix.appid.android.internal.tokens.OAuthClientImpl;
import com.ibm.bluemix.appid.android.testing.helpers.Consts;
import com.ibm.mobilefirstplatform.appid_clientsdk_android.BuildConfig;

import org.json.JSONException;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import static org.assertj.core.api.Java6Assertions.*;

@RunWith (RobolectricTestRunner.class)
@FixMethodOrder (MethodSorters.NAME_ASCENDING)
@Config (constants = BuildConfig.class)
public class OAuthClient_Test {

	@Test ()
	public void testWithValidIdToken ()  {
		IdentityToken idToken = new IdentityTokenImpl(Consts.ID_TOKEN);
		OAuthClient oAuthClient = new OAuthClientImpl(idToken);
		assertThat(oAuthClient).isNotNull();
		assertThat(oAuthClient.getType()).isEqualTo("mobileapp");
		//assertThat(oAuthClient.getName()).isEqualTo("name");
		assertThat(oAuthClient.getSoftwareId()).isEqualTo("com.ibm.mobilefirstplatform.appid");
		assertThat(oAuthClient.getSoftwareVersion()).isEqualTo("1.0");
		assertThat(oAuthClient.getDeviceId()).isEqualTo("eee2c78d-0f12-3808-91eb-c63475dbbf95");
		assertThat(oAuthClient.getDeviceModel()).isEqualTo("GT-I9500");
		assertThat(oAuthClient.getDeviceOS()).isEqualTo("android");

	}

	@Test (expected = RuntimeException.class)
	public void testWithNoOAuthClientInIdToken(){
		IdentityToken idToken = new IdentityTokenImpl(Consts.ID_TOKEN);
		idToken.getPayload().remove("oauth_client");
		new OAuthClientImpl(idToken);
	}

	@Test ()
	public void testWithMissingOAuthClientProperties() throws JSONException{
		IdentityToken idToken = new IdentityTokenImpl(Consts.ID_TOKEN);
		idToken.getPayload().getJSONObject("oauth_client").remove("type");
		OAuthClient oAuthClient = new OAuthClientImpl(idToken);
		assertThat(oAuthClient.getType()).isNull();
	}
}