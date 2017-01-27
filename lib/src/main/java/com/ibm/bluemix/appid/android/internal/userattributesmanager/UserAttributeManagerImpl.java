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
