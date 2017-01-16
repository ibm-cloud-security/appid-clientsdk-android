package com.ibm.bluemix.appid.android.api;

import android.app.Activity;

import com.ibm.bluemix.appid.android.internal.registration.RegistrationListener;
import com.ibm.bluemix.appid.android.internal.registration.RegistrationManager;

public class LoginWidget {

	private final AppId appId;
	private final AuthorizationListener authorizationListener;

	public LoginWidget(AppId appId, AuthorizationListener authorizationListener){
		this.appId = appId;
		this.authorizationListener = authorizationListener;
	}

	public void login(Activity activity){
		RegistrationManager rm = new RegistrationManager(appId, appId.preferenceManager);

		rm.ensureRegistered(activity.getApplicationContext(), new RegistrationListener() {
			@Override
			public void onRegistrationFailure (String message) {
				authorizationListener.onAuthorizationFailure(new AuthorizationException(message));
			}

			@Override
			public void onRegistrationSuccess () {
				// continue with regular flow
			}
		});



//        this.appIdAuthorizationManager.setResponseListener(listener);
//
//        if (preferences.clientId.get() == null || !tenantId.equals(preferences.tenantId.get())) {
//            final AppIdRegistrationManager appIdRM = appIdAuthorizationManager.getAppIdRegistrationManager();
//            appIdRM.invokeInstanceRegistrationRequest(activity.getApplicationContext(), new ResponseListener() {
//                @Override
//                public void onRegistrationSuccess(Response response) {
//                    preferences.tenantId.set(tenantId);
//                    Uri authorizationUri = Uri.parse(appIdAuthorizationManager.getAuthorizationUrl());
//                    appIdAuthorizationManager.getCustomTabManager().launchBrowserTab(activity, authorizationUri);
//                }
//
//                @Override
//                public void onRegistrationFailure(Response response, Throwable t, JSONObject extendedInfo) {
//                    listener.onRegistrationFailure(response, t, extendedInfo);
//                }
//            });
//        } else {
//            Uri authorizationUri = Uri.parse(appIdAuthorizationManager.getAuthorizationUrl());
//            appIdAuthorizationManager.getCustomTabManager().launchBrowserTab(activity, authorizationUri);
//        }
	}
}
