package com.ibm.bluemix.appid.android.internal.tokens;

import com.ibm.bluemix.appid.android.api.tokens.AccessToken;
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

	private static final String ACCESS_TOKEN = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpPU0UifQ.eyJpc3MiOiJpbWYtYXV0aHNlcnZlci5zdGFnZTEtZGV2Lm5nLmJsdWVtaXgubmV0IiwiZXhwIjoxNDg1NTUwNDMxLCJhdWQiOiI3NDFlZmM4NjhiOWEzZjM3YjFjZWE1YjFhNTBkNTBmNzQxODJkZmI0IiwiaWF0IjoxNDg1NTQ2ODMxLCJhdXRoX2J5IjoiZmFjZWJvb2siLCJ0ZW5hbnQiOiI2NmY3OWFiOS1hNTRlLTRmYTItYWQzYy00MDZkZjQ5NGQwMTgiLCJzY29wZSI6ImRlZmF1bHQifQ.JmRXVyY77zXLbkHGmyfdD6yI_ZmfF7J0WYqsvuQYheZikna-IHdffvLd-0NXlb9BRHThrKdLqVoNRRqlrBGhkdLNvSZbp3vR2Uddp5xbK0g5n3pjfkn5r-DKyStI-sxyi6xlV5YhdKhkiacrQ_14qa8aP1hJItW4XhoTyZBJZjk7I5dfYkeErXbjHA4k51J0cqHrzPtZl200phq91R4Uh2g3WkJrz9jOkvFzJJ1vzVDHy8VeOwce3ttxsmUDnuHcqBdlGEIe5H6Fw5zbPb5WWnTDDpIivFe6MhHswho0Qy59LAVKKSvIKLcyVe9MkWyqnQDpqig3xbcFuUSwzqnc9w";
	private static final String ID_TOKEN = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpPU0UifQ.eyJpc3MiOiJpbWYtYXV0aHNlcnZlci5zdGFnZTEtZGV2Lm5nLmJsdWVtaXgubmV0IiwiYXVkIjoiNzQxZWZjODY4YjlhM2YzN2IxY2VhNWIxYTUwZDUwZjc0MTgyZGZiNCIsImV4cCI6MTQ4NTU1MDQzMSwiYXV0aF9ieSI6ImZhY2Vib29rIiwidGVuYW50IjoiNjZmNzlhYjktYTU0ZS00ZmEyLWFkM2MtNDA2ZGY0OTRkMDE4IiwiaWF0IjoxNDg1NTQ2ODMxLCJuYW1lIjoiRG9uIExvbiIsImdlbmRlciI6Im1hbGUiLCJsb2NhbGUiOiJrb19LUiIsInBpY3R1cmUiOiJodHRwczovL3Njb250ZW50Lnh4LmZiY2RuLm5ldC92L3QxLjAtMS9wNTB4NTAvMTM1MDE1NTFfMjg2NDA3ODM4Mzc4ODkyXzE3ODU3NjYyMTE3NjY3MzA2OTdfbi5qcGc_b2g9MjQyYmMyZmI1MDU2MDliNDQyODc0ZmRlM2U5ODY1YTkmb2U9NTkwN0IxQkMiLCJpZGVudGl0aWVzIjpbeyJwcm92aWRlciI6ImZhY2Vib29rIiwiaWQiOiIzNzc0NDAxNTkyNzU2NTkifV0sIm9hdXRoX2NsaWVudCI6eyJ0eXBlIjoibW9iaWxlYXBwIiwic29mdHdhcmVfaWQiOiJjb20uaWJtLm1vYmlsZWZpcnN0cGxhdGZvcm0uYXBwaWQiLCJzb2Z0d2FyZV92ZXJzaW9uIjoiMS4wIiwiZGV2aWNlX2lkIjoiZWVlMmM3OGQtMGYxMi0zODA4LTkxZWItYzYzNDc1ZGJiZjk1IiwiZGV2aWNlX21vZGVsIjoiR1QtSTk1MDAiLCJkZXZpY2Vfb3MiOiJhbmRyb2lkIn19.h_ksq84XW3Jf-sChbPUl_BBX318ZyBbPEj-fsJ8t0ABFtyMSJcst35FguCyzwPO-Qodd8leRCgfzdALkmJWMrWtIw_HKhP61v2dw1_0OmaVeB5TUPGFGLRDvPB2mRNBaX8KJoYBiGofd3vjb85o-TwjN0gGnTwkQrO-xSTXAZLw4ou5_QMdycXawCUrxpUCxOuIfAOsXP7F4pJaZBqBo9BVEMeSyMKztpLoGYEZaD6vnVpPEHmajkcpCDzCNyb-YXkvGSRmd03yep-2jV6vvrUX2ss7eaZGJfzS_XkCnxXZ6H9knAPwcyq0QnYoHrkPJfVMhi1G5VJwRiKMT2P2twg";

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
		AccessToken token = new AccessTokenImpl(ACCESS_TOKEN);
		assertThat(token).isNotNull();
		assertThat(token.getRaw()).isEqualTo(ACCESS_TOKEN);
		assertThat(token.getHeader()).isNotNull();
		assertThat(token.getPayload()).isNotNull();
		assertThat(token.getSignature()).isNotNull();
		assertThat(token.getIssuer()).isEqualTo("imf-authserver.stage1-dev.ng.bluemix.net");
//		assertThat(token.getSubject()).isEqualTo("sub");
		assertThat(token.getAudience()).isEqualTo("741efc868b9a3f37b1cea5b1a50d50f74182dfb4");
		assertThat(token.getExpiration()).isEqualTo(new Date(1485550431000L));
		assertThat(token.getIssuedAt()).isEqualTo(new Date(1485546831000L));
		assertThat(token.getTenant()).isEqualTo("66f79ab9-a54e-4fa2-ad3c-406df494d018");
		assertThat(token.getAuthBy()).isEqualTo("facebook");
		assertThat(token.isExpired()).isEqualTo(true);

		Object nonExistingValue = ((AbstractToken)token).getValue("do-not-exist");
		assertThat(nonExistingValue).isNull();

	}


}
