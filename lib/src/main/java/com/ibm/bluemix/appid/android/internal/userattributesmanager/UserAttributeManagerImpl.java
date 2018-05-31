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

package com.ibm.bluemix.appid.android.internal.userattributesmanager;

import android.support.annotation.NonNull;

import com.ibm.bluemix.appid.android.api.AppID;
import com.ibm.bluemix.appid.android.api.tokens.AccessToken;
import com.ibm.bluemix.appid.android.api.userattributes.UserAttributeManager;
import com.ibm.bluemix.appid.android.api.userattributes.UserAttributeResponseListener;
import com.ibm.bluemix.appid.android.api.userattributes.UserAttributesException;
import com.ibm.bluemix.appid.android.internal.config.Config;
import com.ibm.bluemix.appid.android.internal.network.AppIDRequest;
import com.ibm.bluemix.appid.android.internal.tokenmanager.TokenManager;
import com.ibm.mobilefirstplatform.clientsdk.android.core.api.Response;
import com.ibm.mobilefirstplatform.clientsdk.android.core.api.ResponseListener;
import com.ibm.mobilefirstplatform.clientsdk.android.logger.api.Logger;
import okhttp3.MediaType;
import okhttp3.RequestBody;

import org.json.JSONException;
import org.json.JSONObject;

public class UserAttributeManagerImpl implements UserAttributeManager {
	private static final String USER_PROFILE_ATTRIBUTES_PATH = "attributes";

	private final TokenManager tokenManager;

	private final static Logger logger = Logger.getLogger(Logger.INTERNAL_PREFIX + UserAttributeManagerImpl.class.getName());

	public UserAttributeManagerImpl(TokenManager tokenManager){
		this.tokenManager = tokenManager;
	}

	@Override
	public void setAttribute (@NonNull String name, @NonNull String value, UserAttributeResponseListener listener) {
		this.setAttribute(name, value, null, listener);
	}

	@Override
	public void setAttribute (@NonNull String name, @NonNull String value, AccessToken accessToken, final UserAttributeResponseListener listener) {
		if(accessToken == null) {
			accessToken = tokenManager.getLatestAccessToken();
		}
		sendProtectedRequest(AppIDRequest.PUT, name, value, accessToken, listener);
	}

	@Override
	public void getAttribute (@NonNull String name, UserAttributeResponseListener listener) {
		this.getAttribute(name, null, listener);
	}

	@Override
	public void getAttribute (@NonNull String name, AccessToken accessToken, UserAttributeResponseListener listener) {
		if(accessToken == null){
			accessToken = tokenManager.getLatestAccessToken();
		}
		sendProtectedRequest(AppIDRequest.GET, name, null, accessToken, listener);
	}

	@Override
	public void deleteAttribute (@NonNull String name, UserAttributeResponseListener listener) {
		this.deleteAttribute(name, null, listener);
	}

	@Override
	public void deleteAttribute (@NonNull String name, AccessToken accessToken, UserAttributeResponseListener listener) {
		if(accessToken == null){
			accessToken = tokenManager.getLatestAccessToken();
		}
		sendProtectedRequest(AppIDRequest.DELETE, name, null, accessToken, listener);
	}

	@Override
	public void getAllAttributes(@NonNull UserAttributeResponseListener listener) {
		this.getAllAttributes(null, listener);
	}

	@Override
	public void getAllAttributes(AccessToken accessToken, @NonNull UserAttributeResponseListener listener) {
		if (accessToken == null) {
			accessToken = tokenManager.getLatestAccessToken();
		}
		sendProtectedRequest(AppIDRequest.GET, null, null, accessToken, listener);
	}

	//for testing purpose
	AppIDRequest createAppIDRequest(String url, String method) {
		return new AppIDRequest(url, method);
	}
    //for testing purpose
    RequestBody createRequestBody(String value) {
        return RequestBody.create(MediaType.parse("application/json"), value);
    }

	private void sendProtectedRequest(String method, String name, String value, AccessToken accessToken, final UserAttributeResponseListener listener){
		String url = Config.getUserProfilesServerUrl(AppID.getInstance()) + USER_PROFILE_ATTRIBUTES_PATH;
		url = (name == null || name.length() == 0) ? url : url  + '/' + name;

		AppIDRequest req = createAppIDRequest(url, method);

		ResponseListener resListener = new ResponseListener() {
			@Override
			public void onSuccess(Response response) {
				String responseText = response.getResponseText() == null || response.getResponseText().equals("") ?
						"{}" : response.getResponseText();
				try {
					listener.onSuccess(new JSONObject(responseText));
				} catch (JSONException e) {
					listener.onFailure(new UserAttributesException(UserAttributesException.Error.JSON_PARSE_ERROR));
					e.printStackTrace();
				}
			}

			@Override
			public void onFailure(Response response, Throwable t, JSONObject extendedInfo) {
				String message = (t != null) ? t.getLocalizedMessage() : "";
				message = (extendedInfo != null) ? message + " : " + extendedInfo.toString() : message;
				logger.error(message);

				int errorCode = (response != null) ? response.getStatus() : 500;

				UserAttributesException.Error error;
				switch(errorCode){
					case 401: error = UserAttributesException.Error.UNAUTHORIZED; break;
					case 404: error = UserAttributesException.Error.NOT_FOUND; break;
					default: error = UserAttributesException.Error.FAILED_TO_CONNECT; break;
				}
				listener.onFailure(new UserAttributesException(error));
			}
		};

		RequestBody requestBody = (value == null || value.length() == 0) ? null : createRequestBody(value);

		req.send (resListener, requestBody, accessToken);
	}
}
