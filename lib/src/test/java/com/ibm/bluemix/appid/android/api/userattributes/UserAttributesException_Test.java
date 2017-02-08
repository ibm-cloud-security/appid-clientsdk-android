package com.ibm.bluemix.appid.android.api.userattributes;

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
public class UserAttributesException_Test {

	@Test (expected = UserAttributesException.class)
	public void test () throws UserAttributesException {

		UserAttributesException uae1 = new UserAttributesException(UserAttributesException.Error.FAILED_TO_CONNECT, null);
		assertThat(uae1.getError()).isEqualTo(UserAttributesException.Error.FAILED_TO_CONNECT);

		UserAttributesException uae2 = new UserAttributesException(UserAttributesException.Error.NOT_FOUND, null);
		assertThat(uae2.getError()).isEqualTo(UserAttributesException.Error.NOT_FOUND);

		throw uae2;
	}
}
