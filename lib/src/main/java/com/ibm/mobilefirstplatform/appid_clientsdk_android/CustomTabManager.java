package com.ibm.mobilefirstplatform.appid_clientsdk_android;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.customtabs.CustomTabsCallback;
import android.support.customtabs.CustomTabsClient;
import android.support.customtabs.CustomTabsIntent;
import android.support.customtabs.CustomTabsServiceConnection;
import android.support.customtabs.CustomTabsSession;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by rotembr on 08/01/2017.
 */

class CustomTabManager {

    private final static String HANDLE_AUTHORIZATION_RESPONSE = "com.ibm.mobilefirstplatform.appid_clientsdk_android.HANDLE_AUTHORIZATION_RESPONSE";
    private static final String STABLE_PACKAGE = "com.android.chrome";
    private static final String BETA_PACKAGE = "com.chrome.beta";
    private static final String DEV_PACKAGE = "com.chrome.dev";
    private static final String LOCAL_PACKAGE = "com.google.android.apps.chrome";
    private static final String ACTION_CUSTOM_TABS_CONNECTION = "android.support.customtabs.action.CustomTabsService";
    private static final int URI_ANDROID_APP_SCHEME = 2;
    private static final int AUTH_CANCEL_CODE = 100;
    private static final String TAG = "CustomTabManager";

    private CustomTabsClient mClient;
    private CustomTabsSession mCustomTabsSession;
    private static String sPackageNameToUse;
    PendingIntent authorizationCompletePendingIntent;

    void launchBrowserTab(final Activity activity, final Uri uri) {
        final Context context = activity.getApplicationContext();
        final CustomTabsServiceConnection connection = new CustomTabsServiceConnection() {
            @Override
            public void onCustomTabsServiceConnected(ComponentName componentName, CustomTabsClient client) {
                client.warmup(0);
                mClient = client;
                getSession().mayLaunchUrl(uri, null, null);
                CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder(getSession());
                CustomTabsIntent customTabsIntent = builder.build();
                customTabsIntent.intent.putExtra(Intent.EXTRA_REFERRER, Uri.parse(URI_ANDROID_APP_SCHEME + "//" + context.getPackageName()));
                customTabsIntent.intent.setPackage(getPackageNameToUse(context));
                customTabsIntent.intent.addFlags(PendingIntent.FLAG_ONE_SHOT);
                customTabsIntent.launchUrl(activity, uri);
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                mClient = null;
            }
        };

        Intent postAuthorizationIntent = new Intent(HANDLE_AUTHORIZATION_RESPONSE);
        authorizationCompletePendingIntent = PendingIntent.getActivity(context, 0, postAuthorizationIntent, 0);

        boolean bindSuccess = CustomTabsClient.bindCustomTabsService(context, getPackageNameToUse(context), connection);
        if (!bindSuccess) { //TODO: should we allow to open not in chrome tab- if yes this "if" code need to be removed
            JSONObject errorInfo = new JSONObject();
            try{
                String errMsg = "failed to bindCustomTabsService";
                errorInfo.put("errorCode", 0);
                errorInfo.put("msg", errMsg);
                AppIdAuthorizationManager.getInstance().handleAuthorizationFailure(null, null, errorInfo);
                Log.e(TAG, errMsg);
                return;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Goes through all apps that handle VIEW intents and have a warmup service. Picks
     * the one chosen by the user if there is one, otherwise makes a best effort to return a
     * valid package name.
     *
     * This is <strong>not</strong> threadsafe.
     *
     * @param context {@link Context} to use for accessing {@link PackageManager}.
     * @return The package name recommended to use for connecting to custom tabs related components.
     */
    private static String getPackageNameToUse(Context context) {
        if (sPackageNameToUse != null) {
            return sPackageNameToUse;
        }
        PackageManager pm = context.getPackageManager();
        // Get default VIEW intent handler.
        Intent activityIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.example.com"));
        ResolveInfo defaultViewHandlerInfo = pm.resolveActivity(activityIntent, 0);
        String defaultViewHandlerPackageName = null;
        if (defaultViewHandlerInfo != null) {
            defaultViewHandlerPackageName = defaultViewHandlerInfo.activityInfo.packageName;
        }
        // Get all apps that can handle VIEW intents.
        List<ResolveInfo> resolvedActivityList = pm.queryIntentActivities(activityIntent, 0);
        List<String> packagesSupportingCustomTabs = new ArrayList<>();
        for (ResolveInfo info : resolvedActivityList) {
            Intent serviceIntent = new Intent();
            serviceIntent.setAction(ACTION_CUSTOM_TABS_CONNECTION);
            serviceIntent.setPackage(info.activityInfo.packageName);
            if (pm.resolveService(serviceIntent, 0) != null) {
                packagesSupportingCustomTabs.add(info.activityInfo.packageName);
            }
        }
        // Now packagesSupportingCustomTabs contains all apps that can handle both VIEW intents
        // and service calls.
        if (packagesSupportingCustomTabs.isEmpty()) {
            sPackageNameToUse = null;
        } else if (packagesSupportingCustomTabs.size() == 1) {
            sPackageNameToUse = packagesSupportingCustomTabs.get(0);
        } else if (!TextUtils.isEmpty(defaultViewHandlerPackageName)
                && !hasSpecializedHandlerIntents(context, activityIntent)
                && packagesSupportingCustomTabs.contains(defaultViewHandlerPackageName)) {
            sPackageNameToUse = defaultViewHandlerPackageName;
        } else if (packagesSupportingCustomTabs.contains(STABLE_PACKAGE)) {
            sPackageNameToUse = STABLE_PACKAGE;
        } else if (packagesSupportingCustomTabs.contains(BETA_PACKAGE)) {
            sPackageNameToUse = BETA_PACKAGE;
        } else if (packagesSupportingCustomTabs.contains(DEV_PACKAGE)) {
            sPackageNameToUse = DEV_PACKAGE;
        } else if (packagesSupportingCustomTabs.contains(LOCAL_PACKAGE)) {
            sPackageNameToUse = LOCAL_PACKAGE;
        }
        return sPackageNameToUse;
    }

    /**
     * Used to check whether there is a specialized handler for a given intent.
     * @param intent The intent to check with.
     * @return Whether there is a specialized handler for the given intent.
     */
    private static boolean hasSpecializedHandlerIntents(Context context, Intent intent) {
        try {
            PackageManager pm = context.getPackageManager();
            List<ResolveInfo> handlers = pm.queryIntentActivities(
                    intent,
                    PackageManager.GET_RESOLVED_FILTER);
            if (handlers == null || handlers.size() == 0) {
                return false;
            }
            for (ResolveInfo resolveInfo : handlers) {
                IntentFilter filter = resolveInfo.filter;
                if (filter == null) continue;
                if (filter.countDataAuthorities() == 0 || filter.countDataPaths() == 0) continue;
                if (resolveInfo.activityInfo == null) continue;
                return true;
            }
        } catch (RuntimeException e) {
            Log.e(TAG, "Runtime exception while getting specialized handlers");
        }
        return false;
    }

    private CustomTabsSession getSession() {
        if (mClient == null) {
            mCustomTabsSession = null;
        } else if (mCustomTabsSession == null) {
            mCustomTabsSession = mClient.newSession(new CustomTabsCallback() {
                @Override
                public void onNavigationEvent(int navigationEvent, Bundle extras) {
                    Log.w(TAG, "onNavigationEvent: Code = " + navigationEvent);
                    if(!AppIdAuthorizationManager.getInstance().isAuthorizationCompleted && navigationEvent == CustomTabsCallback.TAB_HIDDEN) {
                        JSONObject cancelInfo = new JSONObject();
                        try {
                            cancelInfo.put("errorCode", AUTH_CANCEL_CODE);
                            cancelInfo.put("msg", "Authentication canceled by user");
                            AppIdAuthorizationManager.getInstance().handleAuthorizationFailure(null, null, cancelInfo);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        }
        return mCustomTabsSession;
    }
}

