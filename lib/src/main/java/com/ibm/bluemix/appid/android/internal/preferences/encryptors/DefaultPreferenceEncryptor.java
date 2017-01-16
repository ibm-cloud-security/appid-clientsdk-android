package com.ibm.bluemix.appid.android.internal.preferences.encryptors;

/**
 * Created on 1/16/17.
 */

public class DefaultPreferenceEncryptor implements PreferenceEncryptor {

	@Override
	public String encrypt (String str) {
		return str;
	}

	@Override
	public String decrypt (String str) {
		return str;
	}
}
