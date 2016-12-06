package com.ibm.mobilefirstplatform.appid_clientsdk_android;

import android.content.Context;
import android.content.Intent;

import com.ibm.mobilefirstplatform.clientsdk.android.core.api.Response;
import com.ibm.mobilefirstplatform.clientsdk.android.core.api.ResponseListener;
import com.ibm.mobilefirstplatform.clientsdk.android.security.api.AppIdentity;
import com.ibm.mobilefirstplatform.clientsdk.android.security.api.DeviceIdentity;
import com.ibm.mobilefirstplatform.clientsdk.android.security.api.UserIdentity;
import com.ibm.mobilefirstplatform.clientsdk.android.security.identity.BaseAppIdentity;
import com.ibm.mobilefirstplatform.clientsdk.android.security.identity.BaseDeviceIdentity;
import com.ibm.mobilefirstplatform.clientsdk.android.security.identity.BaseUserIdentity;
import com.ibm.mobilefirstplatform.clientsdk.android.security.mca.internal.AuthorizationHeaderHelper;
import com.ibm.mobilefirstplatform.clientsdk.android.security.mca.internal.AuthorizationRequest;
import com.ibm.mobilefirstplatform.clientsdk.android.security.mca.internal.preferences.AuthorizationManagerPreferences;

import org.json.JSONObject;

import java.util.InputMismatchException;
import java.util.Map;

/**
 * Created by rotembr on 04/12/2016.
 */

public class AppId {

    private static AppId instance;
    AuthorizationManagerPreferences preferences;
    AppIdAuthorizationProcessManager appIdauthorizationProcessManager;
    private String tenantId = null;
    private String bluemixRegionSuffix = null;
    private ResponseListener responseListener;

    public static String overrideServerHost = null;

    public final static String REGION_US_SOUTH = ".ng.bluemix.net";
    public final static String REGION_UK = ".eu-gb.bluemix.net";
    public final static String REGION_SYDNEY = ".au-syd.bluemix.net";


    private AppId(Context context) {
        this.preferences = new AuthorizationManagerPreferences(context);
        this.appIdauthorizationProcessManager = new AppIdAuthorizationProcessManager(context, preferences);
        //init generic data, like device data and application data
        if (preferences.deviceIdentity.get() == null) {
            preferences.deviceIdentity.set(new BaseDeviceIdentity(context));
        }
        if (preferences.appIdentity.get() == null) {
            preferences.appIdentity.set(new BaseAppIdentity(context));
        }
    }

    public static synchronized AppId createInstance(Context context, String tenantId, String bluemixRegion) {
        instance = new AppId(context.getApplicationContext());
        instance.tenantId = tenantId;
        instance.bluemixRegionSuffix = bluemixRegion;
        if (null == tenantId || null == bluemixRegion) {
            throw new InputMismatchException("tenantId can't be null");
        }
        if (null == tenantId || null == bluemixRegion) {
            throw new InputMismatchException("bluemixRegion can't be null");
        }
        AuthorizationRequest.setup();
        return instance;
    }
    /**
     * @return The singleton instance
     */
    public static AppId getInstance() {
        if (instance == null) {
            throw new IllegalStateException("getInstance can't be called before createInstance");
        }
        return instance;
    }

    public void login(Context context, ResponseListener listener) {
        if (preferences.clientId.get() == null) {
            appIdauthorizationProcessManager.invokeInstanceRegistrationRequest(context);
        } else {
            this.responseListener = listener;
            Intent intent = new Intent(context, WebViewActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }
    }


    /**
     * @return The MCA instance tenantId
     */
    public String getTenantId(){
        return tenantId;
    }

    /**
     * @return Bluemix region suffix ,use to build URLs
     */
    public String getBluemixRegionSuffix(){
        return bluemixRegionSuffix;
    }

    /**
     * Handle success in the authorization process. The response listeners will be updated with
     * success
     * @param response final success response from the server
     */
    void handleAuthorizationSuccess(Response response) {
        responseListener.onSuccess(response);
    }

    /**
     * Handle failure in the authorization process. The response listeners will be updated with
     * failure
     * @param response response that caused to failure
     * @param t additional info about the failure
     */
    void handleAuthorizationFailure(Response response, Throwable t, JSONObject extendedInfo) {
        responseListener.onFailure(response, t, extendedInfo);
    }





//    @Override
//    public boolean isAuthorizationRequired(int statusCode, Map<String, List<String>> headers) {
//        if (headers.containsKey(WWW_AUTHENTICATE_HEADER_NAME)){
//            String authHeader = headers.get(WWW_AUTHENTICATE_HEADER_NAME).get(0);
//            return AuthorizationHeaderHelper.isAuthorizationRequired(statusCode, authHeader);
//        } else {
//            return false;
//        }
//    }
//
//    @Override
//    public boolean isAuthorizationRequired(HttpURLConnection urlConnection) throws IOException {
//        return AuthorizationHeaderHelper.isAuthorizationRequired(urlConnection);
//    }



    public String getCachedAuthorizationHeader() {
        String accessToken = preferences.accessToken.get();
        String idToken = preferences.idToken.get();

        if (accessToken != null && idToken != null) {
            return AuthorizationHeaderHelper.BEARER + " " + accessToken + " " + idToken;
        }
        return null;
    }



    public UserIdentity getUserIdentity() {
        Map map = preferences.userIdentity.getAsMap();
        return (map == null) ? null : new BaseUserIdentity(map);
    }

    public DeviceIdentity getDeviceIdentity() {
        return new BaseDeviceIdentity(preferences.deviceIdentity.getAsMap());
    }

    public AppIdentity getAppIdentity() {
        return new BaseAppIdentity(preferences.appIdentity.getAsMap());
    }


}
