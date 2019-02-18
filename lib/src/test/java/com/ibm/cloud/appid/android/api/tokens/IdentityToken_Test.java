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

package com.ibm.cloud.appid.android.api.tokens;
import com.ibm.cloud.appid.android.internal.tokens.IdentityTokenImpl;
import com.ibm.cloud.appid.android.testing.helpers.Consts;
import com.ibm.mobilefirstplatform.appid_clientsdk_android.BuildConfig;

import org.json.JSONArray;
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
public class IdentityToken_Test {

	@Test ()
	public void testWithValidIdentityToken() throws JSONException{
		IdentityToken idToken = new IdentityTokenImpl(Consts.ID_TOKEN);
		assertThat(idToken).isNotNull();
		assertThat(idToken.getName()).isEqualTo("Don Lon");
		assertThat(idToken.getEmail()).isEqualTo("donlonqwerty@gmail.com");
		assertThat(idToken.getGender()).isNotNull();
		assertThat(idToken.getLocale()).isNotNull();
		assertThat(idToken.getPicture()).isEqualTo("https://platform-lookaside.fbsbx.com/platform/profilepic/?asid=377440159275659&height=50&width=50&ext=1553099960&hash=AeSAHsu0pCqC9oXm");

		JSONArray identities = idToken.getIdentities();
		assertThat(identities).isNotNull();
		assertThat(identities.length()).isEqualTo(1);
		assertThat(identities.getJSONObject(0).getString("provider")).isEqualTo("facebook");
		assertThat(identities.getJSONObject(0).getString("id")).isEqualTo("377440159275659");

		idToken.getPayload().remove("identities");
		assertThat(idToken.getIdentities().length()).isEqualTo(0);
	}
}