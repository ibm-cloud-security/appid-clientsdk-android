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

package com.ibm.bluemix.appid.android.internal.userprofilemanager;

import android.support.annotation.NonNull;

import com.ibm.bluemix.appid.android.api.AppID;
import com.ibm.bluemix.appid.android.api.tokens.AccessToken;
import com.ibm.bluemix.appid.android.api.tokens.IdentityToken;
import com.ibm.bluemix.appid.android.api.userprofile.UserProfileManager;
import com.ibm.bluemix.appid.android.api.userprofile.UserProfileResponseListener;
import com.ibm.bluemix.appid.android.api.userprofile.UserProfileException;
import com.ibm.bluemix.appid.android.internal.config.Config;
import com.ibm.bluemix.appid.android.internal.network.AppIDRequest;
import com.ibm.bluemix.appid.android.internal.tokenmanager.TokenManager;
import com.ibm.mobilefirstplatform.clientsdk.android.core.api.Response;
import com.ibm.mobilefirstplatform.clientsdk.android.core.api.ResponseListener;
import com.ibm.mobilefirstplatform.clientsdk.android.logger.api.Logger;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import java.util.function.*;

import org.json.JSONException;
import org.json.JSONObject;

public class UserProfileManagerImpl implements UserProfileManager {

	private static final String USER_INFO_PATH = "/userinfo";
	private static final String USER_PROFILE_ATTRIBUTES_PATH = "attributes";

	private final TokenManager tokenManager;

	private final static Logger logger = Logger.getLogger(Logger.INTERNAL_PREFIX + UserProfileManagerImpl.class.getName());

	public UserProfileManagerImpl(TokenManager tokenManager){
		this.tokenManager = tokenManager;
	}

	@Override
	public void setAttribute (@NonNull String name, @NonNull String value, UserProfileResponseListener listener) {
		this.setAttribute(name, value, null, listener);
	}

	@Override
	public void setAttribute (@NonNull String name, @NonNull String value, AccessToken accessToken, final UserProfileResponseListener listener) {
		AccessToken token = null;
		if(accessToken == null){
			token = tokenManager.getLatestAccessToken();
		}
		sendProtectedRequest(AppIDRequest.PUT, name, value, token, listener);
	}

	@Override
	public void getAttribute (@NonNull String name, UserProfileResponseListener listener) {
		this.getAttribute(name, null, listener);
	}

	@Override
	public void getAttribute (@NonNull String name, AccessToken accessToken, UserProfileResponseListener listener) {
		AccessToken token = null;
		if(accessToken == null){
			token = tokenManager.getLatestAccessToken();
		}
		sendProtectedRequest(AppIDRequest.GET, name, null, token, listener);
	}

	@Override
	public void deleteAttribute (@NonNull String name, UserProfileResponseListener listener) {
		this.deleteAttribute(name, null, listener);
	}

	@Override
	public void deleteAttribute (@NonNull String name, AccessToken accessToken, UserProfileResponseListener listener) {
		AccessToken token = null;
		if(accessToken == null){
			token = tokenManager.getLatestAccessToken();
		}
		sendProtectedRequest(AppIDRequest.DELETE, name, null, token, listener);
	}

	@Override
	public void getAllAttributes(@NonNull UserProfileResponseListener listener) {
		this.getAllAttributes(null, listener);
	}

	@Override
	public void getAllAttributes(AccessToken accessToken, @NonNull UserProfileResponseListener listener) {
		AccessToken token = null;
		if (accessToken == null) {
			token = tokenManager.getLatestAccessToken();
		}
		sendProtectedRequest(AppIDRequest.GET, null, null, token, listener);
	}

	@Override
	public void getUserInfo (@NonNull final UserProfileResponseListener listener) {
		AccessToken accessToken = tokenManager.getLatestAccessToken();

		if (accessToken == null) {
			listener.onFailure(new UserProfileException(UserProfileException.Error.MISSING_ACCESS_TOKEN));
		}

		getUserInfo(accessToken, tokenManager.getLatestIdentityToken(), listener);
	}

	@Override
	public void getUserInfo (@NonNull AccessToken accessToken, IdentityToken identityToken, @NonNull final UserProfileResponseListener listener) {
		sendAndValidateUserInfoRequest(AppIDRequest.GET, accessToken, identityToken, listener);
	}

	private void sendAndValidateUserInfoRequest(String method, AccessToken accessToken, final IdentityToken identityToken, final UserProfileResponseListener listener){
		String url = Config.getOAuthServerUrl(AppID.getInstance()) + USER_INFO_PATH;

		AppIDRequest req = createAppIDRequest(url, method);

		ResponseListener resListener = new ResponseListener() {

			@Override
			public void onSuccess(Response response) {
				processUserInfoResponse(response, identityToken, listener);
			}

			@Override
			public void onFailure(Response response, Throwable t, JSONObject extendedInfo) {
				processRequestFailure(response, t, extendedInfo, listener);
			}
		};

		req.send (resListener, null, accessToken);
	}

	private void sendProtectedRequest(String method, String name, String value, AccessToken accessToken, final UserProfileResponseListener listener){
		String url = Config.getUserProfilesServerUrl(AppID.getInstance()) + USER_PROFILE_ATTRIBUTES_PATH;
		url = (name == null || name.length() == 0) ? url : url  + '/' + name;

		AppIDRequest req = createAppIDRequest(url, method);

		ResponseListener resListener = new ResponseListener() {
			@Override
			public void onSuccess(Response response) {
				processRequestResponse(response, listener);
			}

			@Override
			public void onFailure(Response response, Throwable t, JSONObject extendedInfo) {
				processRequestFailure(response, t, extendedInfo, listener);
			}
		};

		RequestBody requestBody = (value == null || value.length() == 0) ? null : createRequestBody(value);

		req.send (resListener, requestBody, accessToken);
	}

	private void processRequestResponse(Response response, UserProfileResponseListener listener) {
		String responseText = response.getResponseText() == null || response.getResponseText().equals("") ?
				"{}" : response.getResponseText();
		try {
			listener.onSuccess(new JSONObject(responseText));
		} catch (JSONException e) {
			listener.onFailure(new UserProfileException(UserProfileException.Error.JSON_PARSE_ERROR));
			e.printStackTrace();
		}
	}

	private void processUserInfoResponse(Response response, IdentityToken identityToken, UserProfileResponseListener listener) {
		String responseText = response.getResponseText() == null || response.getResponseText().equals("") ?
				"{}" : response.getResponseText();
		try {
			JSONObject userInfo = new JSONObject(responseText);

			// Validate UserInfo response has a subject
			if (userInfo.getString("sub") == null) {
				listener.onFailure(new UserProfileException(UserProfileException.Error.INVALID_USERINFO_RESPONSE));
				return;
			}

			// Validate UserInfo Response, if identity token is passed
			if (identityToken != null && identityToken.getSubject() != null && !identityToken.getSubject().equals(userInfo.getString("sub"))) {
				listener.onFailure(new UserProfileException(UserProfileException.Error.CONFLICTING_SUBJECTS));
				return;
			}

			listener.onSuccess(userInfo);

		} catch (JSONException e) {
			listener.onFailure(new UserProfileException(UserProfileException.Error.INVALID_USERINFO_RESPONSE));
			e.printStackTrace();
		}
	}

	private void processRequestFailure(Response response, Throwable t, JSONObject extendedInfo, UserProfileResponseListener listener) {
		String message = (t != null) ? t.getLocalizedMessage() : "";

		if (extendedInfo != null) {
			message = message + " : " + extendedInfo.toString();
		}

		logger.error(message);

		int errorCode = (response != null) ? response.getStatus() : 500;

		UserProfileException.Error error;
		switch(errorCode){
			case 401: error = UserProfileException.Error.UNAUTHORIZED; break;
			case 404: error = UserProfileException.Error.NOT_FOUND; break;
			default: error = UserProfileException.Error.FAILED_TO_CONNECT; break;
		}

		listener.onFailure(new UserProfileException(error));
	}

	//for testing purpose
	AppIDRequest createAppIDRequest(String url, String method) { // NOPMD
		return new AppIDRequest(url, method);
	}

	//for testing purpose
	RequestBody createRequestBody(String value) { // NOPMD
		return RequestBody.create(MediaType.parse("application/json"), value);
	}
}
