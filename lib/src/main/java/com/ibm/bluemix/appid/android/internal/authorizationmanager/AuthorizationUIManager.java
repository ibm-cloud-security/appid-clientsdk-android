/*
	Copyright 2017 IBM Corp.
	Licensed under the Apache License, Version 2.0 (the "License");
	you may not use this file except in compliance with the License.
	You may obtain a copy of the License at
	http://www.apache.org/licenses/LICENSE-2.0
	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and
	limitations under the License.
*/

package com.ibm.bluemix.appid.android.internal.authorizationmanager;

import android.app.Activity;
import android.content.ActivityNotFoundException;
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
import android.support.customtabs.CustomTabsServiceConnection;
import android.support.customtabs.CustomTabsSession;
import android.text.TextUtils;

import com.ibm.bluemix.appid.android.api.AuthorizationException;
import com.ibm.bluemix.appid.android.api.AuthorizationListener;
import com.ibm.bluemix.appid.android.internal.OAuthManager;
import com.ibm.mobilefirstplatform.clientsdk.android.logger.api.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


class AuthorizationUIManager {

    private static final String STABLE_PACKAGE = "com.android.chrome";
    private static final String BETA_PACKAGE = "com.chrome.beta";
    private static final String DEV_PACKAGE = "com.chrome.dev";
    private static final String LOCAL_PACKAGE = "com.google.android.apps.chrome";
    private static final String ACTION_CUSTOM_TABS_CONNECTION = "android.support.customtabs.action.CustomTabsService";
    public static final String EXTRA_URL = "com.ibm.bluemix.appid.android.URL";
    public static final String EXTRA_AUTH_FLOW_CONTEXT_GUID = "com.ibm.bluemix.appid.android.AUTH_FLOW_CONTEXT_GUID";
    public static final String EXTRA_REDIRECT_URL = "com.ibm.bluemix.appid.android.REDIRECT_URL";

    private static CustomTabsClient mClient;
    private static  CustomTabsSession mCustomTabsSession;
    private static String sPackageNameToUse;

    private final OAuthManager oAuthManager;
    private final AuthorizationListener authorizationListener;
    private final String serverUrl;
    private final String redirectUrl;

    private final static Logger logger = Logger.getLogger(Logger.INTERNAL_PREFIX + AuthorizationUIManager.class.getName());

    private static boolean isChromeTabSupported = true;

    // TODO: document
    public AuthorizationUIManager(OAuthManager oAuthManager, AuthorizationListener authorizationListener, String serverUrl, String redirectUrl) {
        this.oAuthManager = oAuthManager;
        this.authorizationListener = authorizationListener;
        this.serverUrl = serverUrl;
        this.redirectUrl = redirectUrl;
    }

    public void launch(final Activity activity) {
        final Context context = activity.getApplicationContext();

        String authFlowContextGuid = UUID.randomUUID().toString();
        AuthorizationFlowContext ctx = new AuthorizationFlowContext(oAuthManager, authorizationListener);
        AuthorizationFlowContextStore.push(authFlowContextGuid, ctx);

        // If we cant find a package name, it means there's no browser that supports
        // Chrome Custom Tabs installed. So, we fallback to the WebView
        // (There might be a browser other than Chrome that support Chrome tabs)

        if (getPackageNameToUse(context) == null || !isChromeTabSupported) {
            // Use WebView

            logger.debug("Launching WebViewActivity");
            try {
                Intent intent = new Intent(context, WebViewActivity.class);
                intent.putExtra(EXTRA_AUTH_FLOW_CONTEXT_GUID, authFlowContextGuid);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra(EXTRA_URL, serverUrl);
                intent.putExtra(EXTRA_REDIRECT_URL, redirectUrl);
                context.startActivity(intent);
            } catch (ActivityNotFoundException e) {
                logger.error("Activity not found", e);
                authorizationListener.onAuthorizationFailure(new AuthorizationException(e.getMessage()));
            }
        } else {
            // Use Chrome tabs
            logger.debug("Launching ChromeTabActivity");
            Intent intent = new Intent(activity, ChromeTabActivity.class);
            intent.putExtra(EXTRA_AUTH_FLOW_CONTEXT_GUID, authFlowContextGuid);
            intent.putExtra(EXTRA_REDIRECT_URL, redirectUrl);
            intent.putExtra(EXTRA_URL, serverUrl);
            // Open ChromeTabActivity that will open the ChromeTab on top of it
            intent.putExtra(EXTRA_URL, serverUrl);
            activity.startActivity(intent);
        }
    }

    static void bindCustomTabsService(Context context, final String serverUrl) {
        if (getPackageNameToUse(context) != null) {
            CustomTabsServiceConnection connection = new CustomTabsServiceConnection() {
                @Override
                public void onCustomTabsServiceConnected(ComponentName componentName, CustomTabsClient client) {
                    logger.debug("onCustomTabsServiceConnected");
                    if (client != null) {
                        client.warmup(0); //for better performances
                    }
                    mClient = client;
                    CustomTabsSession session = getSession();
                    if (session != null) {
                        session.mayLaunchUrl(Uri.parse(serverUrl), null, null); //for better performances
                    }
                }

                @Override
                public void onServiceDisconnected(ComponentName name) {
                    logger.debug("onServiceDisconnected");
                    mClient = null;
                }
            };

            // This will trigger 'onCustomTabsServiceConnected' when success.
            isChromeTabSupported = CustomTabsClient.bindCustomTabsService(context, getPackageNameToUse(context), connection);
            if (!isChromeTabSupported) {
                logger.error("Failed to bind to CustomTabsService, fallback to webview");
            }
        }

    }


    /**
     * Goes through all apps that handle VIEW intents and have a warmup service. Picks
     * the one chosen by the user if there is one, otherwise makes a best effort to return a
     * valid package name.
     * <p>
     * This is <strong>not</strong> threadsafe.
     *
     * @param context {@link Context} to use for accessing {@link PackageManager}.
     * @return The package name recommended to use for connecting to custom tabs related components.
     */
    protected static String getPackageNameToUse(Context context) {
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
     *
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
                if (filter != null &&
                        filter.countDataAuthorities() != 0 &&
                        filter.countDataPaths() != 00 &&
                        resolveInfo.activityInfo != null) {
                    return true;
                }
            }
        } catch (RuntimeException e) {
            logger.error("Runtime exception while getting specialized handlers");
        }
        return false;
    }

    private static CustomTabsSession getSession() {
        if (mClient == null) {
            mCustomTabsSession = null;
        } else if (mCustomTabsSession == null) {
            mCustomTabsSession = mClient.newSession(new CustomTabsCallback() {
                @Override
                public void onNavigationEvent(int navigationEvent, Bundle extras) {
                    logger.debug("onNavigationEvent: Code = " + navigationEvent);
                }
            });
        }
        return mCustomTabsSession;
    }

}

