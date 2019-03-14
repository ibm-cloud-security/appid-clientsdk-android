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

package com.ibm.cloud.appid.android.api;

import android.content.Context;
import android.support.annotation.NonNull;

import com.ibm.cloud.appid.android.api.tokens.AccessToken;
import com.ibm.cloud.appid.android.api.tokens.RefreshToken;
import com.ibm.cloud.appid.android.api.userprofile.UserProfileManager;
import com.ibm.cloud.appid.android.internal.OAuthManager;
import com.ibm.cloud.appid.android.internal.loginwidget.LoginWidgetImpl;
import com.ibm.cloud.appid.android.internal.userprofilemanager.UserProfileManagerImpl;

import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public class AppID {

	private static AppID instance;

    private String tenantId;
    private String bluemixRegion;

	private LoginWidgetImpl loginWidget;
	private OAuthManager oAuthManager;
	private UserProfileManager userProfileManager;

    public static String overrideOAuthServerHost = null; //when use place the assignment before calling the AppID initialize function
	public static String overrideUserProfilesHost = null;

	public final static String REGION_US_SOUTH_STAGE1 = "https://us-south.appid.test.cloud.ibm.com";
	public final static String REGION_UK_STAGE1 = "https://eu-gb.appid.test.cloud.ibm.com";

    public final static String REGION_US_SOUTH = "https://us-south.appid.cloud.ibm.com";
    public final static String REGION_US_EAST = "https://us-east.appid.cloud.ibm.com";
    public final static String REGION_UK = "https://eu-gb.appid.cloud.ibm.com";
    public final static String REGION_SYDNEY = "https://au-syd.appid.cloud.ibm.com";
	public final static String REGION_GERMANY = "https://eu-de.appid.cloud.ibm.com";
	public final static String REGION_TOKYO = "https://jp-tok.appid.cloud.ibm.com";

	/**
	 * @return The AppID instance.
	 */
	@NonNull
	public static synchronized AppID getInstance(){
		if (null == instance) {
			synchronized (AppID.class) {
				if (null == instance) {
					instance = new AppID();
				}
			}
		}
		return instance;
	}

	private AppID(){}

	/**
	 * @param context
	 * @param tenantId
	 * @param bluemixRegion
	 * @return The AppID instance tenantId.
	 */
	@NonNull
	public AppID initialize (@NonNull Context context, @NonNull String tenantId, @NonNull String bluemixRegion) {
		this.tenantId = tenantId;
		this.bluemixRegion = bluemixRegion;
		this.oAuthManager = new OAuthManager(context.getApplicationContext(), this);
		this.loginWidget = new LoginWidgetImpl(this.oAuthManager);
		this.userProfileManager = new UserProfileManagerImpl(this.oAuthManager.getTokenManager());
		return instance;
	}

    /**
     * @return The AppID instance tenantId.
     */
	@NonNull
    public String getTenantId() {
		if (null == this.tenantId){
			throw new RuntimeException("AppID is not initialized. Use .initialize() first.");
		}
        return this.tenantId;
    }

    /**
	 * @deprecated
	 * @return Bluemix region suffix ,use to build URLs
	 */
	@NonNull
	public String getBluemixRegionSuffix() {
		if (null == this.bluemixRegion){
			throw new RuntimeException("AppID is not initialized. Use .initialize() first.");
		}
		return this.bluemixRegion;
	}

	/**
	 * @return Bluemix region ,use to build URLs
	 */
	@NonNull
	public String getBluemixRegion() {
		if (null == this.bluemixRegion){
			throw new RuntimeException("AppID is not initialized. Use .initialize() first.");
		}
		return this.bluemixRegion;
	}

	/**
	 * @return the login widget
	 */
	@NonNull
	public LoginWidget getLoginWidget() {
		if (null == this.loginWidget){
			throw new RuntimeException("AppID is not initialized. Use .initialize() first.");
		}
		return this.loginWidget;
	}

	/**
	 * Sets the preferred locale for UI pages
	 * @param locale
     */
	public void setPreferredLocale(Locale locale) {
		if (null == oAuthManager) {
			throw new RuntimeException("AppID is not initialized. Use .initialize() first.");
		}
		oAuthManager.setPreferredLocale(locale);
	}

	/**
	 * @return the OAuth Manager
	 */
	@NonNull
	protected OAuthManager getOAuthManager(){
		if (null == this.oAuthManager){
			throw new RuntimeException("AppID is not initialized. Use .initialize() first.");
		}
		return this.oAuthManager;
	}

	/**
	 * @return the User Attribute Manager
	 */
	@NonNull
	public UserProfileManager getUserProfileManager(){
		if (null == this.userProfileManager){
			throw new RuntimeException("AppID is not initialized. Use .initialize() first.");
		}
		return this.userProfileManager;
	}

	/**
	 * @deprecated use {@link #signinAnonymously(Context, AuthorizationListener)}
	 */
	@Deprecated
	public void loginAnonymously(@NotNull Context context, @NotNull AuthorizationListener authorizationListener){
		this.signinAnonymously(context, authorizationListener);
	}

	/**
	 * @deprecated use {@link #signinAnonymously(Context, String, AuthorizationListener)}
	 */
	@Deprecated
	public void loginAnonymously(@NotNull Context context, String accessToken, @NotNull AuthorizationListener authorizationListener){
		this.signinAnonymously(context, accessToken, authorizationListener);
	}

	/**
	 * @deprecated use {@link #signinAnonymously(Context, String, boolean, AuthorizationListener)}
	 */
	@Deprecated
	public void loginAnonymously(@NotNull Context context, String accessToken, boolean allowCreateNewAnonymousUser, @NotNull AuthorizationListener authorizationListener){
		oAuthManager.getAuthorizationManager().signinAnonymously(context, accessToken, allowCreateNewAnonymousUser, authorizationListener);
	}

	public void signinAnonymously(@NotNull Context context, @NotNull AuthorizationListener authorizationListener){
		this.signinAnonymously(context, null, true, authorizationListener);
	}

	public void signinAnonymously(@NotNull Context context, String accessToken, @NotNull AuthorizationListener authorizationListener){
		this.signinAnonymously(context, accessToken, true, authorizationListener);
	}

	public void signinAnonymously(@NotNull Context context, String accessToken, boolean allowCreateNewAnonymousUser, @NotNull AuthorizationListener authorizationListener){
		oAuthManager.getAuthorizationManager().signinAnonymously(context, accessToken, allowCreateNewAnonymousUser, authorizationListener);
	}

	/**
	 * Obtain token using Resource owner Password (RoP).
	 *
	 * @param username the resource owner username
	 * @param password the resource owner password
	 * @param tokenResponseListener the token response listener
	 */
	public void signinWithResourceOwnerPassword(@NotNull Context context, @NotNull String username, @NotNull String password, @NotNull TokenResponseListener tokenResponseListener) {
        AccessToken accessToken = oAuthManager.getTokenManager().getLatestAccessToken();
		if (accessToken != null && accessToken.isAnonymous()) {
			oAuthManager.getAuthorizationManager().signinWithResourceOwnerPassword(context, username, password, accessToken.getRaw(), tokenResponseListener);
		}
		oAuthManager.getAuthorizationManager().signinWithResourceOwnerPassword(context, username, password, null, tokenResponseListener);
	}

	/**
	 * @deprecated use {@link #obtainTokensWithROP(Context, String, String, TokenResponseListener, String)}
	 */
	public void obtainTokensWithROP(@NotNull Context context, @NotNull String username, @NotNull String password, @NotNull TokenResponseListener tokenResponseListener, String accessTokenString) {
		signinWithResourceOwnerPassword(context, username, password, tokenResponseListener, accessTokenString);
	}

	/**
	 * @deprecated use {@link #obtainTokensWithROP(Context, String, String, TokenResponseListener)}
	 */
	public void obtainTokensWithROP(@NotNull Context context, @NotNull String username, @NotNull String password, @NotNull TokenResponseListener tokenResponseListener) {
        signinWithResourceOwnerPassword(context, username, password, tokenResponseListener);
	}

	/**
	 * Obtain token using Resource owner Password (RoP).
	 *
	 * @param username the resource owner username
	 * @param password the resource owner password
	 * @param tokenResponseListener the token response listener
	 * @param accessTokenString previous access token of some anonymous user
	 */
	public void signinWithResourceOwnerPassword(@NotNull Context context, @NotNull String username, @NotNull String password, @NotNull TokenResponseListener tokenResponseListener, String accessTokenString) {
		if(accessTokenString == null) {
			signinWithResourceOwnerPassword(context, username, password, tokenResponseListener);
		} else {
			oAuthManager.getAuthorizationManager().signinWithResourceOwnerPassword(context, username, password, accessTokenString, tokenResponseListener);
		}
	}

	/**
	 * Obtain new access and identity tokens using a refresh token.
	 * 
	 * Note that the identity itself (user name/details) will not be refreshed by this operation, 
	 * it will remain the same identity but in a new token (new expiration time)
	 *
	 * @param refreshToken the refresh token
	 * @param tokenResponseListener the token response listener
	 */
	public void signinWithRefreshToken(@NotNull Context context, @NotNull String refreshToken, @NotNull TokenResponseListener tokenResponseListener) {
		if (refreshToken == null) {
			tokenResponseListener.onAuthorizationFailure(new AuthorizationException("Missing refresh-token"));
			return;
		}
		oAuthManager.getAuthorizationManager().signinWithRefreshToken(context, refreshToken, tokenResponseListener);
	}

	/**
	 * Obtain token using the latest refresh token stored in the SDK
	 *
	 * @param tokenResponseListener the token response listener
	 */
	public void signinWithRefreshToken(@NotNull Context context, @NotNull TokenResponseListener tokenResponseListener) {
		String refreshTokenString = null;
		RefreshToken refreshToken = oAuthManager.getTokenManager().getLatestRefreshToken();
		if (refreshToken != null) {
			refreshTokenString = refreshToken.getRaw();
		}
		signinWithRefreshToken(context, refreshTokenString, tokenResponseListener);
	}

}
