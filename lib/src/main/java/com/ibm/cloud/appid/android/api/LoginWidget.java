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

import android.app.Activity;
import android.support.annotation.NonNull;

public interface LoginWidget {
	/**
	 * Lunch the login widget user interface
	 * @param activity Parent activity
	 * @param authorizationListener
	 * @param accessTokenString
	 */
	void launch (@NonNull final Activity activity, @NonNull final AuthorizationListener authorizationListener, String accessTokenString);
	void launch (@NonNull final Activity activity, @NonNull final AuthorizationListener authorizationListener);
    /**
     * Launch only the sign up user interface
     * @param activity Parent activity
     * @param authorizationListener
     */
    void launchSignUp (@NonNull final Activity activity, @NonNull final AuthorizationListener authorizationListener);
	/**
	 * Launch the change password user interface
	 * @param activity Parent activity
	 * @param authorizationListener
	 */
	void launchChangePassword (@NonNull final Activity activity, @NonNull final AuthorizationListener authorizationListener);
	/**
	 * Launch the change details user interface
	 * @param activity Parent activity
	 * @param authorizationListener
	 */
	void launchChangeDetails (@NonNull final Activity activity, @NonNull final AuthorizationListener authorizationListener);
	/**
	 * Launch the forgot password user interface
	 * @param activity Parent activity
	 * @param authorizationListener
	 */
	void launchForgotPassword (@NonNull final Activity activity, @NonNull final AuthorizationListener authorizationListener);
}

