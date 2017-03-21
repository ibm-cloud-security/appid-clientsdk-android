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

package com.ibm.bluemix.appid.android.internal.tokens;

import com.ibm.bluemix.appid.android.api.tokens.AccessToken;
import com.ibm.bluemix.appid.android.testing.helpers.Consts;
import com.ibm.mobilefirstplatform.appid_clientsdk_android.BuildConfig;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.Date;

import static org.assertj.core.api.Java6Assertions.*;

@RunWith (RobolectricTestRunner.class)
@FixMethodOrder (MethodSorters.NAME_ASCENDING)
@Config (constants = BuildConfig.class)
public class AbstractToken_Test {

	@Test(expected = RuntimeException.class)
	public void withNull(){
		new AccessTokenImpl(null);
	}

	@Test(expected = RuntimeException.class)
	public void withInvalidToken(){
		new AccessTokenImpl("hello");
	}

	@Test(expected = RuntimeException.class)
	public void withInvalidTokenComponents(){
		new AccessTokenImpl("YQ.YQ.YQ");
	}

	@Test()
	public void withValidToken(){
		AccessToken token = new AccessTokenImpl(Consts.ACCESS_TOKEN);
		assertThat(token).isNotNull();
		assertThat(token.getRaw()).isEqualTo(Consts.ACCESS_TOKEN);
		assertThat(token.getHeader()).isNotNull();
		assertThat(token.getPayload()).isNotNull();
		assertThat(token.getSignature()).isNotNull();
		assertThat(token.getIssuer()).isEqualTo("imf-authserver.stage1.mybluemix.net");
		assertThat(token.getSubject()).isEqualTo("09b7fea5-2e4e-40b8-9d81-df50071a3053");
		assertThat(token.getAudience()).isEqualTo("408eb36a2a069ad89cd19c789a96b7cf36b550ec");
		assertThat(token.getExpiration()).isEqualTo(new Date(1489957459000L));
		assertThat(token.getIssuedAt()).isEqualTo(new Date(1487365459000L));
		assertThat(token.getTenant()).isEqualTo("50d0beed-add7-48dd-8b0a-c818cb456bb4");
		assertThat(token.getAuthenticationMethods().get(0)).isEqualTo("facebook");

		Object nonExistingValue = ((AbstractToken)token).getValue("do-not-exist");
		assertThat(nonExistingValue).isNull();
	}
}
