package com.ibm.bluemix.appid.android.internal.userattributesmanager;

import android.support.annotation.NonNull;

import com.ibm.bluemix.appid.android.api.tokens.AccessToken;
import com.ibm.bluemix.appid.android.api.userattributes.UserAttributeManager;
import com.ibm.bluemix.appid.android.api.userattributes.UserAttributeResponseListener;
import com.ibm.bluemix.appid.android.internal.OAuthManager;

// TODO: Implement user attribute manager
public class UserAttributeManagerImpl implements UserAttributeManager {
	private final OAuthManager oAuthManager;

	public UserAttributeManagerImpl(OAuthManager oAuthManager){
		this.oAuthManager = oAuthManager;
	}

	@Override
	public void setAttribute (@NonNull String name, @NonNull String value, UserAttributeResponseListener listener) {
		AccessToken accessToken = oAuthManager.getTokenManager().getLatestAccessToken();
		this.setAttribute(name, value, accessToken, listener);
	}

	@Override
	public void setAttribute (@NonNull String name, @NonNull String value, @NonNull AccessToken accessToken, UserAttributeResponseListener listener) {
		throw new RuntimeException("Not Implemented");
	}

	@Override
	public void getAttribute (@NonNull String name, UserAttributeResponseListener listener) {
		AccessToken accessToken = oAuthManager.getTokenManager().getLatestAccessToken();
		this.getAttribute(name, accessToken, listener);
	}

	@Override
	public void getAttribute (@NonNull String name, @NonNull AccessToken accessToken, UserAttributeResponseListener listener) {
		throw new RuntimeException("Not Implemented");
	}

	@Override
	public void deleteAttribute (@NonNull String name, UserAttributeResponseListener listener) {
		AccessToken accessToken = oAuthManager.getTokenManager().getLatestAccessToken();
		this.deleteAttribute(name, accessToken, listener);
	}

	@Override
	public void deleteAttribute (@NonNull String name, @NonNull AccessToken accessToken, UserAttributeResponseListener listener) {
		throw new RuntimeException("Not Implemented");
	}
}
