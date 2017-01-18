/*
	Copyright 2014-17 IBM Corp.
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


package com.ibm.bluemix.appid.android.internal.authorization;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.customtabs.CustomTabsIntent;


import com.ibm.mobilefirstplatform.clientsdk.android.logger.api.Logger;


public class ChromeTabActivity extends Activity {

    private static final int URI_ANDROID_APP_SCHEME = 2;
    private Uri uri;
    private static final String USED_INTENT = "USED_INTENT";
    private static final String AUTH_CANCEL_CODE = "100";
    private static final String TAG = "ChromeTabActivity";

	private static final Logger logger = Logger.getLogger(Logger.INTERNAL_PREFIX + ChromeTabActivity.class.getName());

    @Override
    public void onCreate(Bundle savedInstanceBundle) {
        super.onCreate(savedInstanceBundle);
        logger.debug("onCreate");
        Intent intent = getIntent();
        if (!AuthorizationUIManager.POST_AUTHORIZATION_INTENT.equals(intent.getAction())) {
            Bundle extras = intent.getExtras();
            if (extras != null) {
                uri = extras.getParcelable(AuthorizationUIManager.EXTRA_URL);
            }
            if (uri != null) {

//                customTabManager = AppIdAuthorizationManager.getInstance().getCustomTabManager();
//                CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder(customTabManager.getSession());
				CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder();
				builder.enableUrlBarHiding();
                CustomTabsIntent customTabsIntent = builder.build();

                customTabsIntent.intent.setPackage(AuthorizationUIManager.getPackageNameToUse(this.getApplicationContext()));
                customTabsIntent.intent.addFlags(PendingIntent.FLAG_ONE_SHOT);

				//This will launch the chrome tab
				logger.debug("launching custom tab with url: " + uri.toString());
                customTabsIntent.launchUrl(this, uri);
            } else {
				logger.error("launch url cannot be null");
                finish();
            }
        } else {
            //if we launch after authorization completed
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent intent = getIntent();
        if (!intent.hasExtra(USED_INTENT)) {
            // First time onResume called we add USED_INTENT=true
			// to indicate that this intent was already active for the next time,
            // after the chrome tab closes by user.
            intent.putExtra(USED_INTENT, true);
        } else {
            //if here the user pressed the back button.
			logger.error("Authentication cancelled!");
			finish();
        }
    }
}
