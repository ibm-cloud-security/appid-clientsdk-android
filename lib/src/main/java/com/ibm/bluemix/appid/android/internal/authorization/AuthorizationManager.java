package com.ibm.bluemix.appid.android.internal.authorization;

import com.ibm.bluemix.appid.android.api.AppId;
import com.ibm.bluemix.appid.android.internal.preferences.PreferenceManager;
import com.ibm.bluemix.appid.android.internal.registration.RegistrationManager;

import java.util.List;
import java.util.Map;

public class AuthorizationManager {
	private static final String OAUTH_CLIENT_AUTHORIZATION_PATH = "/oauth/v3/";
	private static final String WWW_AUTHENTICATE_HEADER_NAME = "Www-Authenticate";

	private final AppId appId;
	private final PreferenceManager preferenceManager;
	private final RegistrationManager registrationManager;

//	private ResponseListener listener;
//	private AppIdRegistrationManager appIdRegistrationManager;
//	private AppIdTokenManager appIdTokenManager;
//	private CustomTabManager customTabManager;

	public AuthorizationManager (final AppId appId, final PreferenceManager preferenceManager) {
		this.appId = appId;
		this.preferenceManager = preferenceManager;
		this.registrationManager = new RegistrationManager(appId, preferenceManager);
	}


//	public void AppIdAuthorizationManager (Context context) {
//		this.preferences = new AppIdPreferences(context);
//		//init generic data, like device data and application data
//		if (preferences.deviceIdentity.get() == null) {
//			preferences.deviceIdentity.set(new BaseDeviceIdentity(context));
//		}
//		if (preferences.appIdentity.get() == null) {
//			preferences.appIdentity.set(new BaseAppIdentity(context));
//		}
//		this.appIdRegistrationManager = new AppIdRegistrationManager(context, preferences);
//		this.appIdTokenManager = new AppIdTokenManager(preferences);
//		this.customTabManager = new CustomTabManager();
//	}

	String getAuthorizationUrl() {
		return Uri.parse(getServerHost() + OAUTH_CLIENT_AUTHORIZATION_PATH + AppId.getInstance().getTenantId() + "/authorization").buildUpon()
				.appendQueryParameter("response_type", "code")
				.appendQueryParameter("client_id", preferences.clientId.get())
				.appendQueryParameter("redirect_uri", AppId.redirectUri)
				.appendQueryParameter("scope", "openid")
				.appendQueryParameter("use_login_widget", "true")
				.build().toString();
	}

	/**
	 * Handle success in the authentication process. The response listeners will be updated with
	 * success
	 * @param response final success response from the server
	 */
	public void handleAuthorizationSuccess(Response response) {
		listener.onSuccess(response);
	}

	/**
	 * Handle failure in the authentication process. The response listener will be updated with
	 * failure
	 * @param response response that caused to failure
	 * @param t additional info about the failure
	 */
	public void handleAuthorizationFailure(Response response, Throwable t, JSONObject extendedInfo) {
		listener.onFailure(response, t, extendedInfo);
	}


	/**
	 *  this will pop the login widget in case user access protected resource.
	 */
	@Override
	public void obtainAuthorization(Context context, ResponseListener listener, Object... params) {
		Activity activity = (Activity) context;
		AppId.getInstance().login(activity,listener);
	}

	/**
	 * @return the locally stored authorization header or null if the value is not exist.
	 */
	public synchronized String getCachedAuthorizationHeader() {
		String accessToken = preferences.accessToken.get();
		String idToken = preferences.idToken.get();
		if (accessToken != null && idToken != null) {
			return AuthorizationHeaderHelper.BEARER + " " + accessToken + " " + idToken;
		}
		return null;
	}
}
