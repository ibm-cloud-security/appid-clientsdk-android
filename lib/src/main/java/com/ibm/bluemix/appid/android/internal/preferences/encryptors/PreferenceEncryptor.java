package com.ibm.bluemix.appid.android.internal.preferences.encryptors;

public interface PreferenceEncryptor {
	String encrypt(String str);
	String decrypt(String str);
}
