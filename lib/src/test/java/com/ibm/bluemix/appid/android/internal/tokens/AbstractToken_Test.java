package com.ibm.bluemix.appid.android.internal.tokens;

import com.ibm.bluemix.appid.android.api.tokens.AccessToken;
import com.ibm.bluemix.appid.android.testing.helpers.Consts;
import com.ibm.mobilefirstplatform.appid_clientsdk_android.BuildConfig;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.Date;

import static org.assertj.core.api.Java6Assertions.*;

@RunWith (RobolectricTestRunner.class)
@FixMethodOrder (MethodSorters.NAME_ASCENDING)
@Config (constants = BuildConfig.class)
public class AbstractToken_Test {

	@Test(expected = RuntimeException.class)
	public void withNull(){
		new AccessTokenImpl(null);
	}

	@Test(expected = RuntimeException.class)
	public void withInvalidToken(){
		new AccessTokenImpl("hello");
	}

	@Test(expected = RuntimeException.class)
	public void withInvalidTokenComponents(){
		new AccessTokenImpl("YQ.YQ.YQ");
	}

	@Test()
	public void withValidToken(){
		AccessToken token = new AccessTokenImpl(Consts.ACCESS_TOKEN);
		assertThat(token).isNotNull();
		assertThat(token.getRaw()).isEqualTo(Consts.ACCESS_TOKEN);
		assertThat(token.getHeader()).isNotNull();
		assertThat(token.getPayload()).isNotNull();
		assertThat(token.getSignature()).isNotNull();
		assertThat(token.getIssuer()).isEqualTo("imf-authserver.stage1-dev.ng.bluemix.net");
// TODO: restore
//		assertThat(token.getSubject()).isEqualTo("sub");
		assertThat(token.getAudience()).isEqualTo("741efc868b9a3f37b1cea5b1a50d50f74182dfb4");
		assertThat(token.getExpiration()).isEqualTo(new Date(1485550431000L));
		assertThat(token.getIssuedAt()).isEqualTo(new Date(1485546831000L));
		assertThat(token.getTenant()).isEqualTo("66f79ab9-a54e-4fa2-ad3c-406df494d018");
// TODO: restore
//		assertThat(token.getAuthenticationMethods().get(0)).isEqualTo("facebook");
		assertThat(token.isExpired()).isEqualTo(true);

		Object nonExistingValue = ((AbstractToken)token).getValue("do-not-exist");
		assertThat(nonExistingValue).isNull();
	}
}
