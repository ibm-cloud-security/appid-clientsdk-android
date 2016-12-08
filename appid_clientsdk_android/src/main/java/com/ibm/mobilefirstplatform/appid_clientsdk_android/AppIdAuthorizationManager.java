package com.ibm.mobilefirstplatform.appid_clientsdk_android;

import android.content.Context;

import com.ibm.mobilefirstplatform.clientsdk.android.core.api.ResponseListener;
import com.ibm.mobilefirstplatform.clientsdk.android.security.api.AppIdentity;
import com.ibm.mobilefirstplatform.clientsdk.android.security.api.AuthorizationManager;
import com.ibm.mobilefirstplatform.clientsdk.android.security.api.DeviceIdentity;
import com.ibm.mobilefirstplatform.clientsdk.android.security.api.UserIdentity;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.List;
import java.util.Map;

/**
 * Created by rotembr on 08/12/2016.
 */

public class AppIdAuthorizationManager implements AuthorizationManager {
    @Override
    public boolean isAuthorizationRequired(int statusCode, Map<String, List<String>> headers) {
        return false;
    }

    @Override
    public boolean isAuthorizationRequired(HttpURLConnection urlConnection) throws IOException {
        return false;
    }

    @Override
    public void obtainAuthorization(Context context, ResponseListener listener, Object... params) {

    }

    @Override
    public String getCachedAuthorizationHeader() {
        return null;
    }

    @Override
    public void clearAuthorizationData() {

    }

    @Override
    public UserIdentity getUserIdentity() {
        return null;
    }

    @Override
    public DeviceIdentity getDeviceIdentity() {
        return null;
    }

    @Override
    public AppIdentity getAppIdentity() {
        return null;
    }

    @Override
    public void logout(Context context, ResponseListener listener) {

    }
}
