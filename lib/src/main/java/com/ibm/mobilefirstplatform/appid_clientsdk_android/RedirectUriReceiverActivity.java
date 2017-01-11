package com.ibm.mobilefirstplatform.appid_clientsdk_android;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by rotembr on 08/01/2017.
 */

public class RedirectUriReceiverActivity extends Activity {

    private static final String TAG = "RedirectUriActivity";
    private static final String ERROR = "error";
    private static final String ERROR_CODE = "error_code";
    private static final String ERROR_DESCRIPTION = "error_description";
    private static final String KEY_CODE = "code";

    private PendingIntent mCompleteIntent;
    private Uri redirectUri;
    private Intent responseIntent;

    @Override
    public void onCreate(Bundle savedInstanceBundle) {
        super.onCreate(savedInstanceBundle);
        Intent intent = this.getIntent();
        redirectUri = intent.getData();
        mCompleteIntent = AppIdAuthorizationManager.getInstance().getCustomTabManager().authorizationCompletePendingIntent;
        responseIntent = new Intent();
        responseIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        if(mCompleteIntent == null) {
            Log.e(TAG,"Authorization Complete pending Intent can not be null");
        }else if(redirectUri == null) {
            Log.e(TAG,"got null redirect Uri");
        } else {
            if(redirectUri.getQueryParameterNames().contains("error")) {
                handleAuthorizationError();
            } else {
                handleAuthorizationComplete();
            }
        }
        this.finish();
    }

    private void handleAuthorizationComplete() {
        try {
            Log.d(TAG, "Forwarding redirect");
            String code = redirectUri.getQueryParameter(KEY_CODE);
            AppIdAuthorizationManager.getInstance().getAppIdTokenManager().sendTokenRequest(code);
            AppIdAuthorizationManager.getInstance().isAuthorizationCompleted = true;
            mCompleteIntent.send(this, 0, responseIntent);
        } catch (PendingIntent.CanceledException ex) {
            Log.e(TAG, "Unable to send pending intent", ex);
        }
    }

    private void handleAuthorizationError() {
        JSONObject errorInfo = new JSONObject();
        try{
            Log.d(TAG, "Authorization flow error");
            String error = redirectUri.getQueryParameter(ERROR);
            errorInfo.put("errorCode", redirectUri.getQueryParameter(ERROR_CODE));
            errorInfo.put("msg", redirectUri.getQueryParameter(ERROR_DESCRIPTION));
            AppIdAuthorizationManager.getInstance().handleAuthorizationFailure(null, null, errorInfo);
            AppIdAuthorizationManager.getInstance().isAuthorizationCompleted = true;
            Log.e(TAG, error);
            mCompleteIntent.send(this, 0, responseIntent);
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (PendingIntent.CanceledException ex) {
            Log.e(TAG, "Unable to send pending intent", ex);
        }
    }
}