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
package com.ibm.bluemix.appid.android.internal.tokenmanager;

import com.ibm.bluemix.appid.android.api.AppID;
import com.ibm.bluemix.appid.android.api.AuthorizationException;
import com.ibm.bluemix.appid.android.api.AuthorizationListener;
import com.ibm.bluemix.appid.android.api.tokens.AccessToken;
import com.ibm.bluemix.appid.android.api.tokens.IdentityToken;
import com.ibm.bluemix.appid.android.api.tokens.RefreshToken;
import com.ibm.bluemix.appid.android.internal.OAuthManager;
import com.ibm.bluemix.appid.android.internal.network.AppIDRequest;
import com.ibm.bluemix.appid.android.internal.preferences.JSONPreference;
import com.ibm.bluemix.appid.android.internal.preferences.PreferenceManager;
import com.ibm.bluemix.appid.android.internal.registrationmanager.RegistrationManager;
import com.ibm.bluemix.appid.android.internal.tokens.AccessTokenImpl;
import com.ibm.bluemix.appid.android.internal.tokens.IdentityTokenImpl;
import com.ibm.bluemix.appid.android.internal.tokens.RefreshTokenImpl;
import com.ibm.bluemix.appid.android.testing.helpers.Consts;
import com.ibm.bluemix.appid.android.testing.mocks.Response_Mock;
import com.ibm.mobilefirstplatform.clientsdk.android.core.api.Response;
import com.ibm.mobilefirstplatform.clientsdk.android.core.api.ResponseListener;

import org.codehaus.plexus.util.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.robolectric.RobolectricTestRunner;

import java.math.BigInteger;
import java.security.interfaces.RSAPrivateKey;
import java.util.Map;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TokenManager_Test {

    @Mock
    private PreferenceManager pmMock;
    @Mock
    private OAuthManager oAuthManagerMock;
    @Mock
    private AppID appidMock;
    @Mock
    private AppIDRequest stubRequest;
    @Mock
    private JSONPreference JSONPreferenceMock;

    private TokenManager spyTokenManager;
    private String username = "testUser";
    private String password = "testPassword";
    private String stubClientId = "00001111-1111-1111-1111-123456789012";
    private String stubRedirectUri = "http://stub";
    private static final AccessToken expectedAccessToken = new AccessTokenImpl(Consts.ACCESS_TOKEN);
    private static final IdentityToken expectedIdToken = new IdentityTokenImpl(Consts.ID_TOKEN);
    private static final RefreshToken expectedRefreshToken = new RefreshTokenImpl(Consts.REFRESH_TOKEN);
    private Response testReponse;

    @Before
    public void before() {
        MockitoAnnotations.initMocks(this);
        when(oAuthManagerMock.getAppId()).thenReturn(appidMock);
        when(oAuthManagerMock.getPreferenceManager()).thenReturn(pmMock);
        RegistrationManager registrationManager = new RegistrationManager(oAuthManagerMock);
        RegistrationManager spyRm = Mockito.spy(registrationManager);
        doReturn(stubClientId).when(spyRm).getRegistrationDataString(anyString());
        doReturn(stubRedirectUri).when(spyRm).getRegistrationDataString(anyString(), eq(0));
        doReturn(new RSAPrivateKey() {
            @Override
            public BigInteger getPrivateExponent() {
                return new BigInteger("57791d5430d593164082036ad8b29fb157791d5430d593164082036ad8b29fb157791d5430d593164082036ad8b29fb157791d5430d593164082036ad8b29fb1", 16);
            }

            @Override
            public String getAlgorithm() {
                return "RSA256";
            }

            @Override
            public String getFormat() {
                return "UTF8";
            }

            @Override
            public byte[] getEncoded() {
                return new byte[0];
            }

            @Override
            public BigInteger getModulus() {
                return new BigInteger("57791d5430d593164082036ad8b29fb157791d5430d593164082036ad8b29fb157791d5430d593164082036ad8b29fb157791d5430d593164082036ad8b29fb1", 16);
            }
        }).when(spyRm).getPrivateKey();
        when(oAuthManagerMock.getRegistrationManager()).thenReturn(spyRm);
        TokenManager tokenManager = new TokenManager(oAuthManagerMock);
        spyTokenManager = Mockito.spy(tokenManager);
        when(spyTokenManager.createAppIDRequest(anyString(), anyString())).thenReturn(stubRequest);
        doNothing().when(stubRequest).addHeader(anyString(), anyString());
    }

    @Test
    public void obtainTokensRop_success() {
        testReponse = createResponse();

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                ResponseListener responseListener = (ResponseListener) args[1];
                responseListener.onSuccess(testReponse);
                return null;
            }
        }).when(stubRequest).send(any(Map.class), any(ResponseListener.class));

        spyTokenManager.obtainTokensRoP(username, password, Consts.ACCESS_TOKEN, getExpectedSuccessListener());
    }

    @Test
    public void obtainTokensRefreshToken_success() {
        final String accessToken = expectedAccessToken.getRaw();
        final String idToken = expectedIdToken.getRaw();
        final String refreshToken = expectedRefreshToken.getRaw();

        testReponse = createResponse(createTokensResponseText(accessToken, idToken, refreshToken), 200);

        ArgumentMatcher<Map<String, String>> formParametersIncludeRefreshTokenMatcher = new ArgumentMatcher<Map<String, String>>() {
            @Override
            public boolean matches(Object argument) {
                return ((Map<String, String>) argument).containsKey("refresh_token");
            }
        };

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                ResponseListener responseListener = (ResponseListener) args[1];
                responseListener.onSuccess(testReponse);
                return null;
            }
        }).when(stubRequest).send(argThat(formParametersIncludeRefreshTokenMatcher), any(ResponseListener.class));

        // obtain tokens with refresh, should store the retrieved tokens (incl. refresh)
        spyTokenManager.obtainTokensRefreshToken(refreshToken, getExpectedSuccessListener(accessToken, idToken, refreshToken));

        verify(stubRequest, times(1)).send(argThat(formParametersIncludeRefreshTokenMatcher), any(ResponseListener.class));

        RefreshToken latestRefreshToken = spyTokenManager.getLatestRefreshToken();
        assertNotNull(latestRefreshToken);
        assertEquals(refreshToken, latestRefreshToken.getRaw());

        AccessToken latestAccessToken = spyTokenManager.getLatestAccessToken();
        assertNotNull(latestAccessToken);
        assertEquals(accessToken, latestAccessToken.getRaw());

        IdentityToken latestIdToken = spyTokenManager.getLatestIdentityToken();
        assertNotNull(latestIdToken);
        assertEquals(idToken, latestIdToken.getRaw());
    }

    @Test
    public void obtainTokensRefreshToken_failure() {
        final String testDescription = "invalid refresh token";
        testReponse = createResponse("{\"error\": \"invalid_grant\" , \"error_description\": \"" + testDescription + "\" }", 400);

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                ResponseListener responseListener = (ResponseListener) args[1];
                responseListener.onFailure(testReponse ,null, null);
                return null;
            }
        }).when(stubRequest).send(any(Map.class), any(ResponseListener.class));

        spyTokenManager.obtainTokensRefreshToken(expectedRefreshToken.getRaw(), getExpectedFailureListener(testDescription));
    }

    private Response createResponse() {
        return createResponse(createExpectedTokensResponse(), 200);
    }

    private Response createResponse(String responseText, int code) {
        return new Response_Mock(responseText, code);
    }

    @Test
    public void obtainTokensRop_failure() {

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                ResponseListener responseListener = (ResponseListener) args[1];
                responseListener.onFailure(testReponse ,null, null);
                return null;
            }
        }).when(stubRequest).send(any(Map.class), any(ResponseListener.class));

        // test 400
        final String testDescription400 = "test description error123";
        testReponse = createResponse("{\"error\": \"invalid_grant\" , \"error_description\": \"" + testDescription400 + "\" }", 400);
        spyTokenManager.obtainTokensRoP(username, password, null, getExpectedFailureListener(testDescription400));

        // test 403
        final String testDescription403 = "Pending User Verification";
        testReponse = createResponse("{\"error_code\": \"FORBIDDEN\" , \"error_description\": \"" + testDescription403 + "\" }", 403);
        spyTokenManager.obtainTokensRoP(username, password, null, getExpectedFailureListener(testDescription403));

        //test the exception parsing
        testReponse = null;
        spyTokenManager.obtainTokensRoP(username, password, null, getExpectedFailureListener("Failed to retrieve tokens"));
    }

    @Test
    public void obtainTokensRop_failures_in_parsing_response() {
        //bad response
        testReponse = null;
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                ResponseListener responseListener = (ResponseListener) args[1];
                responseListener.onSuccess(testReponse);
                return null;
            }
        }).when(stubRequest).send(any(Map.class), any(ResponseListener.class));

        spyTokenManager.obtainTokensRoP(username, password, null, getExpectedFailureListener("Failed to parse server response"));
        //bad access token
        testReponse = createResponse(createTokensResponseText("bad access token", expectedIdToken.getRaw()), 400);
        spyTokenManager.obtainTokensRoP(username, password, null, getExpectedFailureListener("Failed to parse access_token"));
        //bad id token
        testReponse = createResponse(createTokensResponseText(expectedAccessToken.getRaw(), "bad Id token"), 400);

        spyTokenManager.obtainTokensRoP(username, password, null, getExpectedFailureListener("Failed to parse id_token"));
    }

    @Test
    public void obtainTokens_Authorization_Code_success() {
        testReponse = createResponse();

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                ResponseListener responseListener = (ResponseListener) args[1];
                responseListener.onSuccess(testReponse);
                return null;
            }
        }).when(stubRequest).send(any(Map.class), any(ResponseListener.class));

        spyTokenManager.obtainTokensAuthCode("Some Code", getExpectedSuccessListener());
    }

    @Test
    public void obtainTokens_Authorization_Code_failure() {
        testReponse = createResponse();

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                ResponseListener responseListener = (ResponseListener) args[1];
                responseListener.onFailure(null, null, null);
                return null;
            }
        }).when(stubRequest).send(any(Map.class), any(ResponseListener.class));

        spyTokenManager.obtainTokensAuthCode("Some Code", getExpectedFailureListener("Failed to retrieve tokens"));
    }

    private AuthorizationListener getExpectedFailureListener(final String expectedErrorMessage) {
        return new AuthorizationListener() {
            @Override
            public void onAuthorizationCanceled() {
                fail("should get to onAuthorizationFailure");
            }

            @Override
            public void onAuthorizationFailure(AuthorizationException exception) {
                assertEquals(expectedErrorMessage, exception.getMessage());
            }

            @Override
            public void onAuthorizationSuccess(AccessToken accessToken, IdentityToken identityToken, RefreshToken refreshToken) {
                fail("should get to onAuthorizationFailure");

            }
        };
    }

    private AuthorizationListener getExpectedSuccessListener() {
        return getExpectedSuccessListener(expectedAccessToken.getRaw(), expectedIdToken.getRaw(), null);
    }

    private AuthorizationListener getExpectedSuccessListener(final String expAccessToken, final String expIdToken, final String expRefreshToken) {
        return new AuthorizationListener() {
            @Override
            public void onAuthorizationCanceled() {
                fail("should get to onAuthorizationSuccess");
            }

            @Override
            public void onAuthorizationFailure(AuthorizationException exception) {
                fail("should get to onAuthorizationSuccess");
            }

            @Override
            public void onAuthorizationSuccess(AccessToken accessToken, IdentityToken identityToken, RefreshToken refreshToken) {
                assertEquals(expAccessToken, accessToken.getRaw());
                assertEquals(expIdToken, identityToken.getRaw());
                if (expRefreshToken != null) {
                    assertEquals(expRefreshToken, refreshToken.getRaw());
                }
            }
        };
    }

    private String createExpectedTokensResponse() {
        return createTokensResponseText(expectedAccessToken.getRaw(), expectedIdToken.getRaw(), expectedRefreshToken.getRaw());
    }

    private String createTokensResponseText(String accessToken, String idToken) {
        return createTokensResponseText(accessToken, idToken, null);
    }

    private String createTokensResponseText(String accessToken, String idToken, String refreshToken) {
        JSONObject params = new JSONObject();
        try {
            params.put("access_token", accessToken);
            params.put("id_token", idToken);
            if (!StringUtils.isEmpty(refreshToken)) {
                params.put("refresh_token", refreshToken);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return params.toString();
    }

    private static class A extends ArgumentMatcher<Map> {
        @Override
        public boolean matches(Object argument) {
            return false;
        }
    }
}
