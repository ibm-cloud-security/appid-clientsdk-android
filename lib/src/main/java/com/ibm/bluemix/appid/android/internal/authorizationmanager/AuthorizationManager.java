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
import android.content.Context;
import android.net.Uri;
import android.support.annotation.VisibleForTesting;

import com.ibm.bluemix.appid.android.api.AppID;
import com.ibm.bluemix.appid.android.api.AuthorizationException;
import com.ibm.bluemix.appid.android.api.AuthorizationListener;
import com.ibm.bluemix.appid.android.api.TokenResponseListener;
import com.ibm.bluemix.appid.android.api.tokens.AccessToken;
import com.ibm.bluemix.appid.android.api.tokens.IdentityToken;
import com.ibm.bluemix.appid.android.internal.OAuthManager;
import com.ibm.bluemix.appid.android.internal.config.Config;
import com.ibm.bluemix.appid.android.internal.network.AppIDRequest;
import com.ibm.bluemix.appid.android.internal.network.AppIDRequestFactory;
import com.ibm.bluemix.appid.android.internal.registrationmanager.RegistrationListener;
import com.ibm.bluemix.appid.android.internal.registrationmanager.RegistrationManager;
import com.ibm.bluemix.appid.android.internal.registrationmanager.RegistrationStatus;
import com.ibm.bluemix.appid.android.internal.tokens.AccessTokenImpl;
import com.ibm.mobilefirstplatform.clientsdk.android.core.api.Response;
import com.ibm.mobilefirstplatform.clientsdk.android.core.api.ResponseListener;
import com.ibm.mobilefirstplatform.clientsdk.android.logger.api.Logger;

import org.json.JSONException;
import org.json.JSONObject;

public class AuthorizationManager {
    private final static String OAUTH_AUTHORIZATION_PATH = "/authorization";
    private final static String CHANGE_PASSWORD_PATH = "/cloud_directory/change_password";

    private final AppID appId;
    private final OAuthManager oAuthManager;
    private final RegistrationManager registrationManager;

    private AppIDRequestFactory appIDRequestFactory = new AppIDRequestFactory();

    private final static String CLIENT_ID = "client_id";
    private final static String RESPONSE_TYPE = "response_type";
    private final static String RESPONSE_TYPE_CODE = "code";
    private final static String RESPONSE_TYPE_SIGN_UP = "sign_up";
    private final static String USER_ID = "user_id";

    private final static String SCOPE = "scope";
    private final static String SCOPE_OPENID = "openid";

    private final static String REDIRECT_URI = "redirect_uri";
    private final static String IDP = "idp";
    private final static String APPID_ACCESS_TOKEN = "appid_access_token";
    private final static String ID = "id";
    private final static String PROVIDER = "provider";
    private final static String CLOUD_DIRECTORY_IDP = "cloud_directory";

    private String serverUrl;

    private final static Logger logger = Logger.getLogger(Logger.INTERNAL_PREFIX + AuthorizationManager.class.getName());

    /**
     * @param oAuthManager
     * @param ctx          the Context that will be bind to the custom chrome tab.
     *                     The AuthorizationManager constructor.
     */
    public AuthorizationManager(final OAuthManager oAuthManager, final Context ctx) {
        this.oAuthManager = oAuthManager;
        this.appId = oAuthManager.getAppId();
        this.registrationManager = oAuthManager.getRegistrationManager();
        this.serverUrl = Config.getOAuthServerUrl(this.appId) + OAUTH_AUTHORIZATION_PATH;
        AuthorizationUIManager.bindCustomTabsService(ctx, serverUrl);
    }

    /**
     * @return The Authorization endpoint url.
     */
    private String getAuthorizationUrl(String idpName, AccessToken accessToken, String responseType) {
        String clientId = registrationManager.getRegistrationDataString(RegistrationManager.CLIENT_ID);
        String redirectUri = registrationManager.getRegistrationDataString(RegistrationManager.REDIRECT_URIS, 0);

        Uri.Builder builder = Uri.parse(serverUrl).buildUpon()
                .appendQueryParameter(RESPONSE_TYPE, responseType)
                .appendQueryParameter(CLIENT_ID, clientId)
                .appendQueryParameter(REDIRECT_URI, redirectUri)
                .appendQueryParameter(SCOPE, SCOPE_OPENID);

        if (idpName != null) {
            builder.appendQueryParameter(IDP, idpName);
        }
        if (accessToken != null) {
            builder.appendQueryParameter(APPID_ACCESS_TOKEN, accessToken.getRaw());
        }
        return builder.build().toString();
    }

    /**
     * @return the change password endpoint url.
     */
    private String getChangePasswordUrl(String userId, String redirectUri) {
        String changePasswordEndpoint = Config.getOAuthServerUrl(this.appId) + CHANGE_PASSWORD_PATH;
        String clientId = registrationManager.getRegistrationDataString(RegistrationManager.CLIENT_ID);
        Uri.Builder builder = Uri.parse(changePasswordEndpoint).buildUpon()
                .appendQueryParameter(USER_ID, userId)
                .appendQueryParameter(CLIENT_ID, clientId)
                .appendQueryParameter(REDIRECT_URI, redirectUri);

        return builder.build().toString();
    }

    public void launchAuthorizationUI(final Activity activity, final AuthorizationListener authorizationListener) {
        launchAuthorizationUI(activity, null, authorizationListener);
    }

    /**
     * @param activity              the activity to launch the chrome tab on to.
     * @param accessToken           if not null, this access token will be added to the request.
     * @param authorizationListener the authorization listener of the client.
     *                              launch the authorization url in the chrome tab after successful registration.
     */
    public void launchAuthorizationUI(final Activity activity, final AccessToken accessToken, final AuthorizationListener authorizationListener) {
        registrationManager.ensureRegistered(activity, new RegistrationListener() {
            @Override
            public void onRegistrationFailure(RegistrationStatus error) {
                logger.error(error.getDescription());
                authorizationListener.onAuthorizationFailure(new AuthorizationException(error.getDescription()));
            }

            @Override
            public void onRegistrationSuccess() {
                String authorizationUrl = getAuthorizationUrl(null, accessToken, RESPONSE_TYPE_CODE);
                String redirectUri = registrationManager.getRegistrationDataString(RegistrationManager.REDIRECT_URIS, 0);
                AuthorizationUIManager auim = createAuthorizationUIManager(oAuthManager, authorizationListener, authorizationUrl, redirectUri);
                auim.launch(activity);
            }
        });
    }

    /**
     * @param activity              the activity to launch the chrome tab on to.
     * @param authorizationListener the authorization listener of the client.
     *                              launch the authorization url with response_type=sign_up in the chrome tab after successful registration.
     */
    public void launchSignUpAuthorizationUI(final Activity activity, final AuthorizationListener authorizationListener) {
        registrationManager.ensureRegistered(activity, new RegistrationListener() {
            @Override
            public void onRegistrationFailure(RegistrationStatus error) {
                logger.error(error.getDescription());
                authorizationListener.onAuthorizationFailure(new AuthorizationException(error.getDescription()));
            }

            @Override
            public void onRegistrationSuccess() {
                String signUpAuthorizationUrl = getAuthorizationUrl(null, null, RESPONSE_TYPE_SIGN_UP);
                String redirectUri = registrationManager.getRegistrationDataString(RegistrationManager.REDIRECT_URIS, 0);
                AuthorizationUIManager auim = createAuthorizationUIManager(oAuthManager, authorizationListener, signUpAuthorizationUrl, redirectUri);
                auim.launch(activity);
            }
        });
    }

    /**
     * @param activity              the activity to launch the chrome tab on to.
     * @param authorizationListener the authorization listener of the client.
     *                              launch the change password UI in chrome tab only if the client logged-in otherwise return an error.
     */
    public void launchChangePasswordUI(final Activity activity, final AuthorizationListener authorizationListener) {
        try {
            IdentityToken currentIdToken = this.oAuthManager.getTokenManager().getLatestIdentityToken();
            if (currentIdToken == null) {
                authorizationListener.onAuthorizationFailure(new AuthorizationException("No identity token found."));
            } else if (!CLOUD_DIRECTORY_IDP.equals(currentIdToken.getIdentities().getJSONObject(0).getString(PROVIDER))) {
                authorizationListener.onAuthorizationFailure(new AuthorizationException("The identity token was not retrieved using cloud directory idp."));
            } else {
                String userId = currentIdToken.getIdentities().getJSONObject(0).getString(ID);
                String redirectUri = registrationManager.getRegistrationDataString(RegistrationManager.REDIRECT_URIS, 0);
                String changePasswordUrl = getChangePasswordUrl(userId, redirectUri);
                AuthorizationUIManager auim = createAuthorizationUIManager(oAuthManager, authorizationListener, changePasswordUrl, redirectUri);
                auim.launch(activity);
            }
        } catch (JSONException e) {
            e.printStackTrace();
            authorizationListener.onAuthorizationFailure(new AuthorizationException(e.getMessage()));
        }
    }

    @VisibleForTesting
    AuthorizationUIManager createAuthorizationUIManager(OAuthManager oAuthManager, AuthorizationListener authorizationListener, String authUrl, String redirectUri) {
        return new AuthorizationUIManager(oAuthManager, authorizationListener, authUrl, redirectUri);
    }

    private void continueAnonymousLogin(String accessTokenString, boolean allowCreateNewAnonymousUser, final AuthorizationListener listener) {
        AccessToken accessToken;
        if (accessTokenString == null) {
            accessToken = oAuthManager.getTokenManager().getLatestAccessToken();
        } else {
            accessToken = new AccessTokenImpl(accessTokenString);
        }

        if (accessToken == null && !allowCreateNewAnonymousUser) {
            listener.onAuthorizationFailure(new AuthorizationException("Not allowed to create new anonymous users"));
            return;
        }

        String authorizationUrl = getAuthorizationUrl(AccessTokenImpl.IDP_ANONYMOUS, accessToken, RESPONSE_TYPE_CODE);

        AppIDRequest request = appIDRequestFactory.createRequest(authorizationUrl, AppIDRequest.GET);
        request.send(new ResponseListener() {
                         @Override
                         public void onSuccess(Response response) {
                             logger.debug("loginAnonymously.Response in onSuccess:" + response.getResponseText());
                             String location = response.getHeaders().get("Location").toString();
                             String locationUrl = location.substring(1, location.length() - 1); // removing []
                             String code = Uri.parse(locationUrl).getQueryParameter("code");
                             oAuthManager.getTokenManager().obtainTokens(code, listener);
                         }

                         @Override
                         public void onFailure(Response response, Throwable t, JSONObject extendedInfo) {
                             String message = (response == null) ? "" : response.getResponseText();
                             logger.debug("loginAnonymously.Response in onFailure:" + message, t);
                             message = (t != null) ? t.getLocalizedMessage() : "Authorization request failed.";
                             message = (extendedInfo != null) ? message + extendedInfo.toString() : message;
                             listener.onAuthorizationFailure(new AuthorizationException(message));
                         }
                     }
        );
    }

    public void loginAnonymously(final Context context, final String accessTokenString, final boolean allowCreateNewAnonymousUser, final AuthorizationListener authorizationListener) {
        registrationManager.ensureRegistered(context, new RegistrationListener() {
            @Override
            public void onRegistrationFailure(RegistrationStatus error) {
                logger.error(error.getDescription());
                authorizationListener.onAuthorizationFailure(new AuthorizationException(error.getDescription()));
            }

            @Override
            public void onRegistrationSuccess() {
                continueAnonymousLogin(accessTokenString, allowCreateNewAnonymousUser, authorizationListener);
            }
        });
    }

    public void setAppIDRequestFactory(AppIDRequestFactory appIDRequestFactory) {
        this.appIDRequestFactory = appIDRequestFactory;
    }

    public void obtainTokensWithROP(final Context context, final String username, final String password, final String accessTokenString, final TokenResponseListener tokenResponseListener) {
        registrationManager.ensureRegistered(context, new RegistrationListener() {
            @Override
            public void onRegistrationFailure(RegistrationStatus error) {
                logger.error(error.getDescription());
                tokenResponseListener.onAuthorizationFailure(new AuthorizationException(error.getDescription()));
            }

            @Override
            public void onRegistrationSuccess() {
                oAuthManager.getTokenManager().obtainTokens(username, password, accessTokenString, tokenResponseListener);
            }
        });
    }


}
