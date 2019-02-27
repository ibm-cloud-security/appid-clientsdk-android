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

package com.ibm.cloud.appid.android.internal.tokens;

import com.ibm.cloud.appid.android.api.tokens.AccessToken;
import com.ibm.cloud.appid.android.testing.helpers.Consts;
import com.ibm.mobilefirstplatform.appid_clientsdk_android.BuildConfig;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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

		List<String> expectedAud = new ArrayList<>();
		expectedAud.add(Consts.CLIENT_ID);

		AccessToken token = new AccessTokenImpl(Consts.ACCESS_TOKEN);
		assertThat(token).isNotNull();
		assertThat(token.getRaw()).isEqualTo(Consts.ACCESS_TOKEN);
		assertThat(token.getHeader()).isNotNull();
		assertThat(token.getPayload()).isNotNull();
		assertThat(token.getSignature()).isNotNull();
		assertThat(token.getIssuer()).isEqualTo(Consts.ISSUER);
		assertThat(token.getSubject()).isEqualTo("311a9f74-439d-4741-9347-ae565a0eb137");
		assertThat(token.getAudience()).isEqualTo(expectedAud);
		assertThat(token.getExpiration()).isEqualTo(new Date(1550511572L*1000));
		assertThat(token.getIssuedAt()).isEqualTo(new Date(1550507962L*1000));
		assertThat(token.getTenant()).isEqualTo(Consts.TENANT);
		assertThat(token.getAuthenticationMethods().get(0)).isEqualTo("facebook");
		assertThat(token.getVersion()).isEqualTo(Consts.VERSION);
		assertThat(token.getAzp()).isEqualTo(Consts.CLIENT_ID);



		Object nonExistingValue = ((AbstractToken)token).getValue("do-not-exist");
		assertThat(nonExistingValue).isNull();
	}
}
