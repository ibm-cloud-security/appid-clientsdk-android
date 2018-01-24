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

package com.ibm.bluemix.appid.android.api;


import android.app.Activity;
import android.content.Context;
import android.os.Build;

import com.ibm.bluemix.appid.android.api.tokens.AccessToken;
import com.ibm.bluemix.appid.android.api.tokens.IdentityToken;
import com.ibm.bluemix.appid.android.api.tokens.RefreshToken;
import com.ibm.bluemix.appid.android.internal.helpers.AuthorizationHeaderHelper;
import com.ibm.bluemix.appid.android.internal.OAuthManager;
import com.ibm.bluemix.appid.android.internal.tokenmanager.TokenManager;
import com.ibm.mobilefirstplatform.clientsdk.android.core.api.ResponseListener;
import com.ibm.mobilefirstplatform.clientsdk.android.logger.api.Logger;
import com.ibm.mobilefirstplatform.clientsdk.android.security.api.AppIdentity;
import com.ibm.mobilefirstplatform.clientsdk.android.security.api.AuthorizationManager;
import com.ibm.mobilefirstplatform.clientsdk.android.security.api.DeviceIdentity;
import com.ibm.mobilefirstplatform.clientsdk.android.security.api.UserIdentity;
import com.ibm.mobilefirstplatform.clientsdk.android.security.identity.BaseAppIdentity;
import com.ibm.mobilefirstplatform.clientsdk.android.security.identity.BaseDeviceIdentity;
import com.ibm.mobilefirstplatform.clientsdk.android.security.identity.BaseUserIdentity;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AppIDAuthorizationManager implements AuthorizationManager {

	private final OAuthManager oAuthManager;
	private static final String BEARER = "Bearer";

	private final static Logger logger = Logger.getLogger(Logger.INTERNAL_PREFIX + AppIDAuthorizationManager.class.getName());

	public AppIDAuthorizationManager(AppID appID){
		this.oAuthManager = appID.getOAuthManager();
	}

    /**
     * Check if the params came from response that requires authorization
     * @param statusCode of the response
     * @param headers response headers
     * @return true if status is 401 or 403 and The value of the header contains 'Bearer'
     */
 	@Override
	public boolean isAuthorizationRequired (int statusCode, Map<String, List<String>> headers) {
		logger.debug("isAuthorizationRequired");
        if ((statusCode == 401 || statusCode == 403) &&
				headers != null &&
				headers.containsKey(WWW_AUTHENTICATE_HEADER_NAME)){
            String authHeader = headers.get(WWW_AUTHENTICATE_HEADER_NAME).get(0);
            return AuthorizationHeaderHelper.isAuthorizationRequired(statusCode, authHeader);
        } else {
            return false;
        }
	}

	/**
     * A response is an OAuth error response only if,
     * 1. Its status is either 401 or 403
     * 2. The value of the "WWW-Authenticate" header contains 'Bearer'
     *
     * @param urlConnection connection to check the authorization conditions for.
     * @return true if the response satisfies both conditions
     * @throws IOException in case connection doesn't contains response code.
     */
	@Override
	public boolean isAuthorizationRequired (HttpURLConnection urlConnection) throws IOException {
		logger.debug("isAuthorizationRequired");
        return AuthorizationHeaderHelper.isAuthorizationRequired(urlConnection);
	}

	@Override
	public void obtainAuthorization (final Context context, final ResponseListener listener, Object... params) {
		logger.debug("obtainAuthorization");

		TokenManager tokenManager = oAuthManager.getTokenManager();
		RefreshToken latestRefreshToken = tokenManager.getLatestRefreshToken();
		if (latestRefreshToken != null) {
			tokenManager.obtainTokensRefreshToken(latestRefreshToken.getRaw(), new TokenResponseListener() {
				@Override
				public void onAuthorizationFailure(AuthorizationException exception) {
					launchAuthorization(context, listener);
				}

				@Override
				public void onAuthorizationSuccess(AccessToken accessToken, IdentityToken identityToken, RefreshToken refreshToken) {
					listener.onSuccess(null);
				}
			});
		}
	}

	private void launchAuthorization (final Context context, final ResponseListener listener) {
		oAuthManager.getAuthorizationManager().launchAuthorizationUI((Activity)context, new AuthorizationListener() {
			@Override
			public void onAuthorizationFailure (AuthorizationException exception) {
				listener.onFailure(null, exception, null);
			}

			@Override
			public void onAuthorizationCanceled () {
				listener.onFailure(null, new RuntimeException("Authorization canceled"), null);
			}

			@Override
			public void onAuthorizationSuccess (AccessToken accessToken, IdentityToken identityToken, RefreshToken refreshToken) {
				listener.onSuccess(null);
			}
		});
	}

	@Override
	public String getCachedAuthorizationHeader () {
		AccessToken accessToken = getAccessToken();
		IdentityToken identityToken = getIdentityToken();
		logger.debug("getCachedAuthorizationHeader");
		if (accessToken == null || identityToken == null){
			return null;
		}
		return BEARER + " " + accessToken.getRaw() + " " + identityToken.getRaw();
	}

	@Override
	public void clearAuthorizationData () {
		logger.debug("clearAuthorizationData");
		oAuthManager.getTokenManager().clearStoredTokens();
	}

	@Override
	public UserIdentity getUserIdentity () {
		logger.debug("getUserIdentity");
		IdentityToken identityToken = getIdentityToken();
		if (identityToken == null) {
			return null;
		}
		Map map = new HashMap();
		map.put(UserIdentity.ID, identityToken.getSubject());
		map.put(UserIdentity.AUTH_BY, identityToken.getAuthenticationMethods().get(0));
		map.put(UserIdentity.DISPLAY_NAME, identityToken.getName());
		return new BaseUserIdentity(map);
	}

	@Override
	public DeviceIdentity getDeviceIdentity () {
		logger.debug("getDeviceIdentity");
		IdentityToken identityToken = getIdentityToken();
		if (identityToken == null) {
			return null;
		}
		Map map = new HashMap();
		map.put(DeviceIdentity.ID, identityToken.getOAuthClient().getDeviceId());
		map.put(DeviceIdentity.OS, identityToken.getOAuthClient().getDeviceOS());
		map.put(DeviceIdentity.MODEL, identityToken.getOAuthClient().getDeviceModel());
		map.put(DeviceIdentity.BRAND, Build.BRAND);
		map.put(DeviceIdentity.OS_VERSION, Build.VERSION.RELEASE);
		return new BaseDeviceIdentity(map);
	}

	@Override
	public AppIdentity getAppIdentity () {
		logger.debug("getAppIdentity");
		IdentityToken identityToken = getIdentityToken();
		if (identityToken == null) {
			return null;
		}
		Map map = new HashMap();
		map.put(AppIdentity.ID, identityToken.getOAuthClient().getSoftwareId());
		map.put(AppIdentity.VERSION, identityToken.getOAuthClient().getSoftwareVersion());
		return new BaseAppIdentity(map);
	}

	/**
	 * log out
	 * @param context
	 * @param listener
	 * currently just call to clearAuthorizationData()
	 */
	@Override
	public void logout (Context context, ResponseListener listener) {
		logger.debug("logout");
		clearAuthorizationData();
		// TODO: implement logout
	}

	public AccessToken getAccessToken () {
		return oAuthManager.getTokenManager().getLatestAccessToken();
	}

	public IdentityToken getIdentityToken () {
		return oAuthManager.getTokenManager().getLatestIdentityToken();
	}

	public RefreshToken getRefreshToken() {
		return oAuthManager.getTokenManager().getLatestRefreshToken();
	}
}
