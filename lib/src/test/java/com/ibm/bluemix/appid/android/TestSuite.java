package com.ibm.bluemix.appid.android;

import com.ibm.bluemix.appid.android.api.AppIDAuthorizationManager_Test;
import com.ibm.bluemix.appid.android.api.AppID_Test;
import com.ibm.bluemix.appid.android.api.AuthorizationException_Test;
import com.ibm.bluemix.appid.android.api.tokens.AccessToken_Test;
import com.ibm.bluemix.appid.android.api.tokens.IdentityToken_Test;
import com.ibm.bluemix.appid.android.api.tokens.OAuthClient_Test;
import com.ibm.bluemix.appid.android.api.userattributes.UserAttributesException_Test;
import com.ibm.bluemix.appid.android.internal.helpers.AuthorizationHeaderHelper_Test;
import com.ibm.bluemix.appid.android.internal.tokens.AbstractToken_Test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)

@Suite.SuiteClasses({
		// API
		AppID_Test.class,
		AuthorizationException_Test.class,
////		AccessToken_Test.class,
////		IdentityToken_Test.class,
		OAuthClient_Test.class,
		UserAttributesException_Test.class,
		AppIDAuthorizationManager_Test.class,

		// Internal
		AbstractToken_Test.class,
		AuthorizationHeaderHelper_Test.class
})
public class TestSuite {}
