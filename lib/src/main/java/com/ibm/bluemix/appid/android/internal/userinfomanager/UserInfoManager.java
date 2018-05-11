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

package com.ibm.bluemix.appid.android.internal.userinfomanager;

import com.ibm.bluemix.appid.android.api.AppID;
import com.ibm.bluemix.appid.android.api.tokens.AccessToken;
import com.ibm.bluemix.appid.android.api.tokens.IdentityToken;
import com.ibm.bluemix.appid.android.api.userinfo.UserInfoResponseListener;
import com.ibm.bluemix.appid.android.api.userinfo.UserInfoException;
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

public class UserInfoManager {
    private static final String USER_INFO_PATH = "/userinfo";

    private final TokenManager tokenManager;

    private final static Logger logger = Logger.getLogger(Logger.INTERNAL_PREFIX + UserInfoManager.class.getName());

    public UserInfoManager(TokenManager tokenManager){
        this.tokenManager = tokenManager;
    }

    public void getUserInfo (final UserInfoResponseListener listener) {
        getUserInfo(null, null, listener);
    }

    public void getUserInfo (AccessToken accessToken, IdentityToken identityToken, final UserInfoResponseListener listener) {
        if(accessToken == null) {
            accessToken = tokenManager.getLatestAccessToken();
        }

        if(identityToken == null) {
            identityToken = tokenManager.getLatestIdentityToken();
        }

        if(accessToken == null) {
            listener.onFailure(new UserInfoException(UserInfoException.Error.MISSING_ACCESS_TOKEN));
            return;
        }

        sendProtectedRequestAndValidateResponse(AppIDRequest.GET, accessToken, identityToken, listener);
    }

    //for testing purpose
    AppIDRequest createAppIDRequest(String url, String method) {
        return new AppIDRequest(url, method);
    }

    private void sendProtectedRequestAndValidateResponse(String method, AccessToken accessToken, final IdentityToken identityToken, final UserInfoResponseListener listener){
        String url = Config.getOAuthServerUrl(AppID.getInstance()) + USER_INFO_PATH;

        AppIDRequest req = createAppIDRequest(url, method);

        ResponseListener resListener = new ResponseListener() {

            @Override
            public void onSuccess(Response response) {
                String responseText = response.getResponseText() == null || response.getResponseText().equals("") ?
                        "{}" : response.getResponseText();
                try {
                    JSONObject userInfo = new JSONObject(responseText);

                    // Validate UserInfo Response if identity token is passed
                    if (identityToken != null && identityToken.getSubject() != null ) {
                        if (!identityToken.getSubject().equals((String) userInfo.get("sub"))) {
                            listener.onFailure(new UserInfoException(UserInfoException.Error.CONFLICTING_SUBJECTS));
                            return;
                        }
                    }

                    listener.onSuccess(userInfo);

                } catch (JSONException e) {
                    listener.onFailure(new UserInfoException(UserInfoException.Error.JSON_PARSE_ERROR));
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Response response, Throwable t, JSONObject extendedInfo) {
                String message = (t != null) ? t.getLocalizedMessage() : "";
                message = (extendedInfo != null) ? message + " : " + extendedInfo.toString() : message;
                logger.error(message);

                int errorCode = (response != null) ? response.getStatus() : 500;

                UserInfoException.Error error;
                switch(errorCode){
                    case 401: error = UserInfoException.Error.UNAUTHORIZED; break;
                    case 404: error = UserInfoException.Error.NOT_FOUND; break;
                    default: error = UserInfoException.Error.FAILED_TO_CONNECT; break;
                }
                listener.onFailure(new UserInfoException(error));
            }
        };

        req.send (resListener, null, accessToken);
    }
}