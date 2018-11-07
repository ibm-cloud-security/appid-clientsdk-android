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
package com.ibm.cloud.appid.android.internal.preferences;

import android.content.Context;
import android.content.SharedPreferences;

import com.ibm.mobilefirstplatform.appid_clientsdk_android.BuildConfig;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import static org.assertj.core.api.Java6Assertions.assertThat;

@RunWith (RobolectricTestRunner.class)
@FixMethodOrder (MethodSorters.NAME_ASCENDING)
@Config (constants = BuildConfig.class)
public class JSONPreference_Test {

	@Test
	public void testJSONPreference() throws JSONException{
		SharedPreferences sharedPreferences = RuntimeEnvironment.application.getSharedPreferences("test", Context.MODE_PRIVATE);
		JSONPreference jsonPreference = new JSONPreference("test-name", sharedPreferences);
		assertThat(jsonPreference.get()).isNull();

		JSONObject jsonObject = new JSONObject();
		jsonObject.put("json-name", "json-value");

		jsonPreference.set(jsonObject);
		assertThat(jsonPreference.getAsJSON().getString("json-name")).isEqualTo("json-value");

		jsonPreference.clear();
		assertThat(jsonPreference.getAsJSON()).isNull();
	}
}
