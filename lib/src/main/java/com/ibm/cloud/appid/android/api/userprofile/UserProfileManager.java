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

package com.ibm.cloud.appid.android.api.userprofile;

import android.support.annotation.NonNull;

import com.ibm.cloud.appid.android.api.tokens.AccessToken;
import com.ibm.cloud.appid.android.api.tokens.IdentityToken;

public interface UserProfileManager {
	void setAttribute(@NonNull String name, @NonNull String value, UserProfileResponseListener listener);
	void setAttribute(@NonNull String name, @NonNull String value, AccessToken accessToken, UserProfileResponseListener listener);

	void getAttribute(@NonNull String name, UserProfileResponseListener listener);
	void getAttribute(@NonNull String name, AccessToken accessToken, UserProfileResponseListener listener);

	void deleteAttribute(@NonNull String name, UserProfileResponseListener listener);
	void deleteAttribute(@NonNull String name, AccessToken accessToken, UserProfileResponseListener listener);

	void getAllAttributes(@NonNull UserProfileResponseListener listener);
	void getAllAttributes(AccessToken accessToken, @NonNull UserProfileResponseListener listener);

	void getUserInfo(@NonNull UserProfileResponseListener listener);
	void getUserInfo(@NonNull AccessToken accessToken, IdentityToken identityToken, @NonNull UserProfileResponseListener listener);
}
