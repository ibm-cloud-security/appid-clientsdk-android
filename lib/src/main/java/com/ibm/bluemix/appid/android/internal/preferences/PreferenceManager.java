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

package com.ibm.bluemix.appid.android.internal.preferences;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferenceManager {

	private final SharedPreferences sharedPreferences;
	private static final String DEFAULT_PREF_MANAGER_NAME = "com.ibm.bluemix.appid.android.preferences";

	public PreferenceManager (Context context, String name, int mode) {
		this.sharedPreferences = context.getSharedPreferences(name, mode);
	}

	public static PreferenceManager getDefaultPreferenceManager(Context context){
		return new PreferenceManager(context, DEFAULT_PREF_MANAGER_NAME, Context.MODE_PRIVATE);
	}

	public StringPreference getStringPreference(String name){
		return new StringPreference(name, sharedPreferences);
	}

	public JSONPreference getJSONPreference(String name){
		return new JSONPreference(name, sharedPreferences);
	}


}