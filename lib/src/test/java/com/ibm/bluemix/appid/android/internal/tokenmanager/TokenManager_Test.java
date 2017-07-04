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
import com.ibm.bluemix.appid.android.api.TokenResponseListener;
import com.ibm.bluemix.appid.android.api.tokens.AccessToken;
import com.ibm.bluemix.appid.android.api.tokens.IdentityToken;
import com.ibm.bluemix.appid.android.internal.OAuthManager;
import com.ibm.bluemix.appid.android.internal.network.AppIDRequest;
import com.ibm.bluemix.appid.android.internal.preferences.JSONPreference;
import com.ibm.bluemix.appid.android.internal.preferences.PreferenceManager;
import com.ibm.bluemix.appid.android.internal.registrationmanager.RegistrationManager;
import com.ibm.bluemix.appid.android.internal.tokens.AccessTokenImpl;
import com.ibm.bluemix.appid.android.internal.tokens.IdentityTokenImpl;
import com.ibm.bluemix.appid.android.testing.helpers.Consts;
import com.ibm.mobilefirstplatform.clientsdk.android.core.api.Response;
import com.ibm.mobilefirstplatform.clientsdk.android.core.api.ResponseListener;

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.robolectric.RobolectricTestRunner;

import java.math.BigInteger;
import java.security.Signature;
import java.security.interfaces.RSAPrivateKey;
import java.util.List;
import java.util.Map;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
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

        testReponse = new Response() {
            @Override
            public int getStatus() {
                return 200;
            }

            @Override
            public String getResponseText() {
                return "{\"access_token\": " + expectedAccessToken.getRaw() +", " +
                        "\"id_token\":" + expectedIdToken.getRaw() + "}";
            }

            @Override
            public byte[] getResponseBytes() {
                return new byte[0];
            }

            @Override
            public Map<String, List<String>> getHeaders() {
                return null;
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
        }).when(stubRequest).send(any(Map.class), any(ResponseListener.class));

        spyTokenManager.obtainTokens(username, password, Consts.ACCESS_TOKEN, new TokenResponseListener() {
            @Override
            public void onAuthorizationFailure(AuthorizationException exception) {
                fail("should get to onAuthorizationSuccess");
            }

            @Override
            public void onAuthorizationSuccess(AccessToken accessToken, IdentityToken identityToken) {
                assertEquals(accessToken.getRaw(), expectedAccessToken.getRaw());
                assertEquals(identityToken.getRaw(), expectedIdToken.getRaw());
            }
        });
    }

    @Test
    public void obtainTokensRop_failure() {

        final String testDescription = "test description error123";
        testReponse = new Response() {
            @Override
            public int getStatus() {
                return 400;
            }

            @Override
            public String getResponseText() {
                return "{\"error\": \"invalid_grant\" , \"error_description\": \"" + testDescription + "\" }";
            }

            @Override
            public byte[] getResponseBytes() {
                return new byte[0];
            }

            @Override
            public Map<String, List<String>> getHeaders() {
                return null;
            }
        };

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                ResponseListener responseListener = (ResponseListener) args[1];
                responseListener.onFailure(testReponse ,null, null);
                return null;
            }
        }).when(stubRequest).send(any(Map.class), any(ResponseListener.class));

        spyTokenManager.obtainTokens(username, password, null, new TokenResponseListener() {
            @Override
            public void onAuthorizationFailure(AuthorizationException exception) {
                assertEquals(exception.getMessage(), "Failed to retrieve tokens: " + testDescription);
            }

            @Override
            public void onAuthorizationSuccess(AccessToken accessToken, IdentityToken identityToken) {
                fail("should get to onAuthorizationFailure");

            }
        });
        //test the exception parsing
        testReponse = null;
        spyTokenManager.obtainTokens(username, password, null, new TokenResponseListener() {
            @Override
            public void onAuthorizationFailure(AuthorizationException exception) {
                assertEquals(exception.getMessage(), "Failed to retrieve tokens" );
            }

            @Override
            public void onAuthorizationSuccess(AccessToken accessToken, IdentityToken identityToken) {
                fail("should get to onAuthorizationFailure");

            }
        });
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

        spyTokenManager.obtainTokens(username, password, null, new TokenResponseListener() {
            @Override
            public void onAuthorizationFailure(AuthorizationException exception) {
                assertEquals(exception.getMessage(), "Failed to parse server response");
            }

            @Override
            public void onAuthorizationSuccess(AccessToken accessToken, IdentityToken identityToken) {
                fail("should get to onAuthorizationFailure");

            }
        });
        //bad access token
        testReponse = new Response() {
            @Override
            public int getStatus() {
                return 200;
            }

            @Override
            public String getResponseText() {
                return "{\"access_token\": " + "\"bad access token\"" + ", " +
                        "\"id_token\":" + expectedIdToken.getRaw() + "}";
            }

            @Override
            public byte[] getResponseBytes() {
                return new byte[0];
            }

            @Override
            public Map<String, List<String>> getHeaders() {
                return null;
            }
        };
        spyTokenManager.obtainTokens(username, password, null, new TokenResponseListener() {
            @Override
            public void onAuthorizationFailure(AuthorizationException exception) {
                assertEquals(exception.getMessage(), "Failed to parse access_token");
            }

            @Override
            public void onAuthorizationSuccess(AccessToken accessToken, IdentityToken identityToken) {
                fail("should get to onAuthorizationFailure");

            }
        });
        //bad id token
        testReponse = new Response() {
            @Override
            public int getStatus() {
                return 200;
            }

            @Override
            public String getResponseText() {
                return "{\"access_token\": " + expectedAccessToken.getRaw() + ", " +
                        "\"id_token\":" + "\"bad Id token\"" + "}";
            }

            @Override
            public byte[] getResponseBytes() {
                return new byte[0];
            }

            @Override
            public Map<String, List<String>> getHeaders() {
                return null;
            }
        };

        spyTokenManager.obtainTokens(username, password, null, new TokenResponseListener() {
            @Override
            public void onAuthorizationFailure(AuthorizationException exception) {
                assertEquals(exception.getMessage(), "Failed to parse id_token");
            }

            @Override
            public void onAuthorizationSuccess(AccessToken accessToken, IdentityToken identityToken) {
                fail("should get to onAuthorizationFailure");

            }
        });
    }



    @Test
    public void obtainTokens_Authorization_Code_success() {

        testReponse = new Response() {
            @Override
            public int getStatus() {
                return 200;
            }

            @Override
            public String getResponseText() {
                return "{\"access_token\": " + expectedAccessToken.getRaw() +", " +
                        "\"id_token\":" + expectedIdToken.getRaw() + "}";
            }

            @Override
            public byte[] getResponseBytes() {
                return new byte[0];
            }

            @Override
            public Map<String, List<String>> getHeaders() {
                return null;
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
        }).when(stubRequest).send(any(Map.class), any(ResponseListener.class));

        spyTokenManager.obtainTokens("Some Code", new AuthorizationListener() {
            @Override
            public void onAuthorizationCanceled() {
                fail("should get to onAuthorizationSuccess");
            }

            @Override
            public void onAuthorizationFailure(AuthorizationException exception) {
                fail("should get to onAuthorizationSuccess");
            }

            @Override
            public void onAuthorizationSuccess(AccessToken accessToken, IdentityToken identityToken) {
                assertEquals(accessToken.getRaw(), expectedAccessToken.getRaw());
                assertEquals(identityToken.getRaw(), expectedIdToken.getRaw());
            }
        });
    }

    @Test
    public void obtainTokens_Authorization_Code_failure() {

        testReponse = new Response() {
            @Override
            public int getStatus() {
                return 200;
            }

            @Override
            public String getResponseText() {
                return "{\"access_token\": " + expectedAccessToken.getRaw() +", " +
                        "\"id_token\":" + expectedIdToken.getRaw() + "}";
            }

            @Override
            public byte[] getResponseBytes() {
                return new byte[0];
            }

            @Override
            public Map<String, List<String>> getHeaders() {
                return null;
            }
        };

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                ResponseListener responseListener = (ResponseListener) args[1];
                responseListener.onFailure(null, null, null);
                return null;
            }
        }).when(stubRequest).send(any(Map.class), any(ResponseListener.class));

        spyTokenManager.obtainTokens("Some Code", new AuthorizationListener() {
            @Override
            public void onAuthorizationCanceled() {
                fail("should get to onAuthorizationFailure");
            }

            @Override
            public void onAuthorizationFailure(AuthorizationException exception) {
                assertEquals(exception.getMessage(), "Failed to retrieve tokens");
            }

            @Override
            public void onAuthorizationSuccess(AccessToken accessToken, IdentityToken identityToken) {
                fail("should get to onAuthorizationFailure");

            }
        });
    }
}
