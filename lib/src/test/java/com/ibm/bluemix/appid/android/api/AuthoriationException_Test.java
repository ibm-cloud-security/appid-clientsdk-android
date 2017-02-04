package com.ibm.bluemix.appid.android.api;

import com.ibm.bluemix.appid.android.testhelpers.ClassHelper;
import com.ibm.mobilefirstplatform.appid_clientsdk_android.BuildConfig;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import static org.assertj.core.api.Java6Assertions.*;

@RunWith (RobolectricTestRunner.class)
@FixMethodOrder (MethodSorters.NAME_ASCENDING)
@Config (constants = BuildConfig.class)
public class AuthoriationException_Test {

	@Test (expected = AuthorizationException.class)
	public void test () throws AuthorizationException {
		AuthorizationException ae = new AuthorizationException("hello");
		ClassHelper.assertSame(ae, AuthorizationException.class);
		assertThat(ae.getMessage()).isEqualTo("hello");
		throw ae;
	}
}
