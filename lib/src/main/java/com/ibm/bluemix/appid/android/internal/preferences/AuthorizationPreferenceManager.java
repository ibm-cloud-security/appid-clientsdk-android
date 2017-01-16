/*
	Copyright 2014-17 IBM Corp.
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


public class AuthorizationPreferenceManager extends PreferenceManager {

//	public StringPreference clientId = new StringPreference("clientId");
//	public TokenPreference accessToken = new TokenPreference("accessToken");
//	public TokenPreference idToken = new TokenPreference("idToken");

//	public JSONPreference userIdentity = new JSONPreference("userIdentity");
//	public JSONPreference deviceIdentity = new JSONPreference("deviceIdentity");
//	public JSONPreference appIdentity = new JSONPreference("appIdentity");

	public AuthorizationPreferenceManager (Context context) {
		super(context, "AuthorizationManagerPreferences", Context.MODE_PRIVATE, null);

		// TODO: add encryption to PrefManager
//		String uuid = Settings.Secure.getString(context.getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
//		PreferenceEncryptor encryptor = new AESEncryptor(uuid);
	}

//	/**
//	 * Holds authorization manager Token preference
//	 */
//	public class TokenPreference {
//
//		String runtimeValue;
//		StringPreference savedValue;
//
//		public TokenPreference(String prefName) {
//			savedValue = new StringPreference(prefName);
//		}
//
//		public void set(String value) {
//			runtimeValue = value;
//			if (persistencePolicy.get() == MCAAuthorizationManager.PersistencePolicy.ALWAYS) {
//				savedValue.set(value);
//			} else {
//				savedValue.clear();
//			}
//		}
//
//		public String get() {
//			if (runtimeValue == null && persistencePolicy.get() == MCAAuthorizationManager.PersistencePolicy.ALWAYS) {
//				return savedValue.get();
//			}
//			return runtimeValue;
//		}
//
//		public void updateStateByPolicy() {
//			if (persistencePolicy.get() == MCAAuthorizationManager.PersistencePolicy.ALWAYS) {
//				savedValue.set(runtimeValue);
//			} else {
//				savedValue.clear();
//			}
//		}
//
//		public void clear() {
//			savedValue.clear();
//			runtimeValue = null;
//		}
//	}
}
