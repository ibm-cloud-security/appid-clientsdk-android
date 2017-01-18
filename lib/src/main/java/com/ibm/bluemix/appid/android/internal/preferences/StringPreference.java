package com.ibm.bluemix.appid.android.internal.preferences;

import android.content.SharedPreferences;

import com.ibm.bluemix.appid.android.internal.preferences.encryptors.PreferenceEncryptor;

public class StringPreference {

	private final String name;
	private final SharedPreferences sharedPreferences;
	private final SharedPreferences.Editor editor;
	private final PreferenceEncryptor encryptor;

	StringPreference(String name, SharedPreferences sharedPreferences, PreferenceEncryptor encryptor) {
		this.name = name;
		this.sharedPreferences = sharedPreferences;
		this.editor = sharedPreferences.edit();
		this.encryptor = encryptor;
	}

	public synchronized String get() {
		String value = sharedPreferences.getString(name, null);
		return value == null ? null : encryptor.decrypt(value);
	}

	public synchronized void set(String value) {
		value = (value == null) ? null : encryptor.encrypt(value);
		editor.putString(name, value);
		editor.commit();
	}

	public void clear() {
		set(null);
	}
}

