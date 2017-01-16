package com.ibm.bluemix.appid.android.internal.preferences;

import android.content.SharedPreferences;

import com.ibm.bluemix.appid.android.internal.preferences.encryptors.PreferenceEncryptor;

import org.json.JSONException;
import org.json.JSONObject;

public class JSONPreference extends StringPreference {

	JSONPreference(String name, SharedPreferences sharedPreferences, PreferenceEncryptor encryptor) {
		super(name, sharedPreferences, encryptor);
	}

	public void set(JSONObject json) {
		super.set(json.toString());
	}

	public JSONObject getAsJSON() throws JSONException {
		String stringValue = super.get();
		return new JSONObject(stringValue);
	}
//
//	public Map getAsMap() throws JSONException {
//		String stringValue = super.get();
//		JSONObject json = new JSONObject(stringValue);
//
//		Map<String, Object> map = new HashMap<>();
//		Iterator<String> keys = json.keys();
//
//		while (keys.hasNext()) {
//			String element = keys.next();
//			map.put(element, json.get(element));
//		}
//		return map;
//	}
}
