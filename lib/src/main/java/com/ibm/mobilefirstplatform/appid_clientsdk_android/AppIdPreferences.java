package com.ibm.mobilefirstplatform.appid_clientsdk_android;

import android.content.Context;

import com.ibm.mobilefirstplatform.clientsdk.android.security.mca.internal.preferences.AuthorizationManagerPreferences;

/**
 * Created by odedb on 19/12/2016.
 */

public class AppIdPreferences extends AuthorizationManagerPreferences {

    public StringPreference tenantId = new StringPreference("tenantId");
    public AppIdPreferences(Context context) {
        super(context);
    }

    /**
     * Holds single string preference value
     */
    public class StringPreference {

        String prefName;
        String value;

        StringPreference(String prefName) {
            this(prefName, null);
        }

        StringPreference(String prefName, String defaultValue) {
            this.prefName = prefName;
            this.value = sharedPreferences.getString(prefName, defaultValue);
        }

        public String get() {
            return value == null ? null : stringEncryption.decrypt(value);
        }

        public void set(String value) {
            this.value = value == null ? null : stringEncryption.encrypt(value);
            commit();
        }

        public void clear() {
            this.value = null;
            commit();
        }

        private void commit() {
            editor.putString(prefName, value);
            editor.commit();
        }
    }



}