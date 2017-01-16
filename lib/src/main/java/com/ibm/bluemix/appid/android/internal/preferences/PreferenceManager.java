package com.ibm.bluemix.appid.android.internal.preferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.provider.Settings;

import com.ibm.bluemix.appid.android.internal.preferences.encryptors.AESEncryptor;
import com.ibm.bluemix.appid.android.internal.preferences.encryptors.DefaultPreferenceEncryptor;
import com.ibm.bluemix.appid.android.internal.preferences.encryptors.PreferenceEncryptor;
import com.ibm.mobilefirstplatform.clientsdk.android.logger.api.Logger;

public class PreferenceManager {

	private final SharedPreferences sharedPreferences;
	private final PreferenceEncryptor encryptor;
	private static final Logger logger = Logger.getLogger(Logger.INTERNAL_PREFIX + PreferenceManager.class.getName());
	private static final String DEFAULT_PREF_MANAGER_NAME = "com.ibm.bluemix.appid.android.preferences";

	public PreferenceManager (Context context, String name, int mode, PreferenceEncryptor encryptor) {
		this.sharedPreferences = context.getSharedPreferences(name, mode);
		this.encryptor = (encryptor == null) ? new DefaultPreferenceEncryptor() : encryptor;
	}

	public static PreferenceManager getDefaultPreferenceManager(Context context){
		String uuid = Settings.Secure.getString(context.getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
		PreferenceEncryptor encryptor = new AESEncryptor(uuid);
		return new PreferenceManager(context, DEFAULT_PREF_MANAGER_NAME, Context.MODE_PRIVATE, encryptor);
	}

	public StringPreference getStringPreference(String name){
		return new StringPreference(name, sharedPreferences, encryptor);
	}

	public JSONPreference getJSONPreference(String name){
		return new JSONPreference(name, sharedPreferences, encryptor);
	}


}