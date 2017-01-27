package com.ibm.bluemix.appid.android.api.userattributes;

import android.support.annotation.NonNull;

import com.ibm.bluemix.appid.android.api.tokens.AccessToken;

public interface UserAttributeManager {
	void setAttribute(@NonNull String name, @NonNull String value, UserAttributeResponseListener listener);
	void setAttribute(@NonNull String name, @NonNull String value, @NonNull AccessToken accessToken, UserAttributeResponseListener listener);
	void getAttribute(@NonNull String name, UserAttributeResponseListener listener);
	void getAttribute(@NonNull String name, @NonNull AccessToken accessToken, UserAttributeResponseListener listener);
	void deleteAttribute(@NonNull String name, UserAttributeResponseListener listener);
	void deleteAttribute(@NonNull String name, @NonNull AccessToken accessToken, UserAttributeResponseListener listener);
	// void getAllAttributes();
	// void getAllAttributes(@NonNull AccessToken accessToken)
}
