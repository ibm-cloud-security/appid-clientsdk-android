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
package com.ibm.bluemix.appid.android.internal.authorizationmanager;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;

import com.ibm.bluemix.appid.android.api.AppID;
import com.ibm.bluemix.appid.android.api.AuthorizationException;
import com.ibm.bluemix.appid.android.api.AuthorizationListener;
import com.ibm.bluemix.appid.android.api.TokenResponseListener;
import com.ibm.bluemix.appid.android.api.tokens.AccessToken;
import com.ibm.bluemix.appid.android.api.tokens.IdentityToken;
import com.ibm.bluemix.appid.android.internal.OAuthManager;
import com.ibm.bluemix.appid.android.internal.network.AppIDRequest;
import com.ibm.bluemix.appid.android.internal.network.AppIDRequestFactory;
import com.ibm.bluemix.appid.android.internal.registrationmanager.RegistrationListener;
import com.ibm.bluemix.appid.android.internal.registrationmanager.RegistrationManager;
import com.ibm.bluemix.appid.android.internal.registrationmanager.RegistrationStatus;
import com.ibm.bluemix.appid.android.internal.tokenmanager.TokenManager;
import com.ibm.bluemix.appid.android.internal.tokens.AccessTokenImpl;
import com.ibm.bluemix.appid.android.internal.tokens.IdentityTokenImpl;
import com.ibm.bluemix.appid.android.testing.helpers.Consts;
import com.ibm.mobilefirstplatform.appid_clientsdk_android.BuildConfig;
import com.ibm.mobilefirstplatform.clientsdk.android.core.api.Response;
import com.ibm.mobilefirstplatform.clientsdk.android.core.api.ResponseListener;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.doAnswer;

@RunWith (RobolectricTestRunner.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@Config (constants = BuildConfig.class)
public class AuthorizationManager_Test {

    @Mock
    private OAuthManager oAuthManagerMock;
    @Mock
    private RegistrationManager registrationManager;
    @Mock
    private TokenManager tokenManagerMock;
    @Mock
    private AppID appidMock;
    @Mock
    private PackageManager pmMock;
    @Mock
    private Context mockContext;
    @Mock
    private AppIDRequestFactory appIDRequestFactoryMock;
    @Mock
    private AppIDRequest mockRequest;
    @Mock
    private Activity mockActivity;
    @Mock
    private IdentityToken mockIdToken;
    private AuthorizationManager authManager;
    private String username = "testUser";
    private String password = "testPassword";
    private String testError = "Some Error";
    private String code = "1234";
    private String passedAccessToken = Consts.ACCESS_TOKEN;
    private static final AccessToken expectedAccessToken = new AccessTokenImpl(Consts.ACCESS_TOKEN);
    private static final IdentityToken expectedIdToken = new IdentityTokenImpl(Consts.ID_TOKEN);
    private Response testResponse = new Response() {
        @Override
        public String getRequestURL() {
            return null;
        }

        @Override
        public int getStatus() {
            return 0;
        }

        @Override
        public String getResponseText() {
            return testError;
        }

        @Override
        public JSONObject getResponseJSON() {
            return null;
        }

        @Override
        public byte[] getResponseBytes() {
            return new byte[0];
        }

        @Override
        public InputStream getResponseByteStream() {
            return null;
        }

        @Override
        public long getContentLength() {
            return 0;
        }

        @Override
        public Map<String, List<String>> getHeaders() {
            Map<String, List<String>> map =  new HashMap<String, List<String>>();
            map.put("Location", new LinkedList<String>());
            return map;
        }
    };

    private Response testGoodResponse = new Response() {
        @Override
        public String getRequestURL() {
            return null;
        }

        @Override
        public int getStatus() {
            return 200;
        }

        @Override
        public String getResponseText() {
            return code;
        }

        @Override
        public JSONObject getResponseJSON() {
            return null;
        }

        @Override
        public byte[] getResponseBytes() {
            return new byte[0];
        }

        @Override
        public InputStream getResponseByteStream() {
            return null;
        }

        @Override
        public long getContentLength() {
            return 0;
        }

        @Override
        public Map<String, List<String>> getHeaders() {
            Map<String, List<String>> map =  new HashMap<String, List<String>>();
            map.put("Location", new LinkedList<String>());
            return map;
        }
    };

    @Before
    public void before() {
        MockitoAnnotations.initMocks(this);
        when(oAuthManagerMock.getAppId()).thenReturn(appidMock);
        when(oAuthManagerMock.getRegistrationManager()).thenReturn(registrationManager);
        when(appidMock.getBluemixRegionSuffix()).thenReturn(".stubPrefix");
        when(mockContext.getPackageManager()).thenReturn(pmMock);

        when(oAuthManagerMock.getTokenManager()).thenReturn(tokenManagerMock);
        doAnswer(new Answer<Void>() {
                     @Override
                     public Void answer(InvocationOnMock invocation) {
                         Object[] args = invocation.getArguments();
                         TokenResponseListener tokenListener = (TokenResponseListener) args[3];
                         tokenListener.onAuthorizationSuccess(expectedAccessToken, expectedIdToken);
                         return null;
                     }
                 }
        ).when(tokenManagerMock).obtainTokens(eq(username), eq(password), eq(passedAccessToken), any(TokenResponseListener.class));

        doAnswer(new Answer<Void>() {
                     @Override
                     public Void answer(InvocationOnMock invocation) {
                         Object[] args = invocation.getArguments();
                         AuthorizationListener authListener = (AuthorizationListener) args[1];
                         authListener.onAuthorizationSuccess(expectedAccessToken, expectedIdToken);
                         return null;
                     }
                 }
        ).when(tokenManagerMock).obtainTokens(anyString(), any(AuthorizationListener.class));

        authManager = new AuthorizationManager(oAuthManagerMock, mockContext);
        appidMock.overrideOAuthServerHost = null;
    }

    @Test
    public void obtainTokensWithROP_registrationFailure() {

        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) {
                Object[] args = invocation.getArguments();
                RegistrationListener regListener = (RegistrationListener) args[1];
                regListener.onRegistrationFailure(RegistrationStatus.NOT_REGISTRED);
                return null;
            }
        }).when(registrationManager).ensureRegistered(eq(mockContext), any(RegistrationListener.class));


        authManager.obtainTokensWithROP(mockContext, username, password, passedAccessToken, new TokenResponseListener() {
            @Override
            public void onAuthorizationFailure(AuthorizationException exception) {
                assertEquals(exception.getMessage(), RegistrationStatus.NOT_REGISTRED.getDescription());
            }

            @Override
            public void onAuthorizationSuccess(AccessToken accessToken, IdentityToken identityToken) {
                fail("should get to onAuthorizationFailure");
            }
        });
    }

    @Test
    public void obtainTokensWithROP_registrationSuccess() {
        doAnswer(new Answer<Void>() {
                     public Void answer(InvocationOnMock invocation) {
                         Object[] args = invocation.getArguments();
                         RegistrationListener regListener = (RegistrationListener) args[1];
                         regListener.onRegistrationSuccess();
                         return null;
                     }
                 }
        ).when(registrationManager).ensureRegistered(eq(mockContext), any(RegistrationListener.class));

        authManager.obtainTokensWithROP(mockContext, username, password, passedAccessToken, new TokenResponseListener() {
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
    public void loginAnonymously_success() {
        doAnswer(new Answer<Void>() {
                     public Void answer(InvocationOnMock invocation) {
                         Object[] args = invocation.getArguments();
                         RegistrationListener regListener = (RegistrationListener) args[1];
                         regListener.onRegistrationSuccess();
                         return null;
                     }
                 }
        ).when(registrationManager).ensureRegistered(eq(mockContext), any(RegistrationListener.class));

        authManager.setAppIDRequestFactory(appIDRequestFactoryMock);
        when(appIDRequestFactoryMock.createRequest(anyString(), anyString())).thenReturn(mockRequest);
        doAnswer(new Answer<Void>() {
                     public Void answer(InvocationOnMock invocation) {
                         Object[] args = invocation.getArguments();
                         ResponseListener responseListener = (ResponseListener) args[0];
                         responseListener.onSuccess(testResponse);
                         return null;
                     }
                 }
        ).when(mockRequest).send(any(ResponseListener.class));

        authManager.loginAnonymously(mockContext, expectedAccessToken.getRaw(), true, new AuthorizationListener() {
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
    public void loginAnonymously_failure() {
        doAnswer(new Answer<Void>() {
                     public Void answer(InvocationOnMock invocation) {
                         Object[] args = invocation.getArguments();
                         RegistrationListener regListener = (RegistrationListener) args[1];
                         regListener.onRegistrationFailure(RegistrationStatus.NOT_REGISTRED);
                         return null;
                     }
                 }
        ).when(registrationManager).ensureRegistered(eq(mockContext), any(RegistrationListener.class));


        authManager.loginAnonymously(mockContext, expectedAccessToken.getRaw(), true, new AuthorizationListener() {
            @Override
            public void onAuthorizationCanceled() {
                fail("should get to onAuthorizationFailure");
            }

            @Override
            public void onAuthorizationFailure(AuthorizationException exception) {
                assertEquals(exception.getMessage(), RegistrationStatus.NOT_REGISTRED.getDescription());
            }

            @Override
            public void onAuthorizationSuccess(AccessToken accessToken, IdentityToken identityToken) {
                fail("should get to onAuthorizationFailure");
            }
        });
    }

    @Test
    public void loginAnonymously_requestFailure() {
        doAnswer(new Answer<Void>() {
                     public Void answer(InvocationOnMock invocation) {
                         Object[] args = invocation.getArguments();
                         RegistrationListener regListener = (RegistrationListener) args[1];
                         regListener.onRegistrationSuccess();
                         return null;
                     }
                 }
        ).when(registrationManager).ensureRegistered(eq(mockContext), any(RegistrationListener.class));

        authManager.setAppIDRequestFactory(appIDRequestFactoryMock);
        when(appIDRequestFactoryMock.createRequest(anyString(), anyString())).thenReturn(mockRequest);
        doAnswer(new Answer<Void>() {
                     public Void answer(InvocationOnMock invocation) {
                         Object[] args = invocation.getArguments();
                         ResponseListener responseListener = (ResponseListener) args[0];
                         responseListener.onFailure(null, new Throwable(testError), null);
                         return null;
                     }
                 }
        ).when(mockRequest).send(any(ResponseListener.class));

        authManager.loginAnonymously(mockContext, expectedAccessToken.getRaw(), true, new AuthorizationListener() {
            @Override
            public void onAuthorizationCanceled() {
                fail("should get to onAuthorizationFailure");
            }

            @Override
            public void onAuthorizationFailure(AuthorizationException exception) {
                assertEquals(exception.getMessage(), testError);
            }

            @Override
            public void onAuthorizationSuccess(AccessToken accessToken, IdentityToken identityToken) {
                fail("should get to onAuthorizationFailure");
            }
        });
    }

    @Test
    public void launchAuthorizationUI_failure(){
        Activity activity = new Activity();
        doAnswer(new Answer<Void>() {
                     public Void answer(InvocationOnMock invocation) {
                         Object[] args = invocation.getArguments();
                         RegistrationListener regListener = (RegistrationListener) args[1];
                         regListener.onRegistrationFailure(RegistrationStatus.NOT_REGISTRED);
                         return null;
                     }
                 }
        ).when(registrationManager).ensureRegistered(eq(activity), any(RegistrationListener.class));

        authManager.launchAuthorizationUI(activity, new AuthorizationListener() {
            @Override
            public void onAuthorizationFailure(AuthorizationException exception) {
                assertEquals(exception.getMessage(), RegistrationStatus.NOT_REGISTRED.getDescription());
            }

            @Override
            public void onAuthorizationSuccess(AccessToken accessToken, IdentityToken identityToken) {
                fail("should get to onAuthorizationFailure");
            }

            @Override
            public void onAuthorizationCanceled() {
                fail("should get to onAuthorizationFailure");
            }
        });
    }

    @Test
    public void launchAuthorizationUI_success(){

        final AuthorizationManager spyAuthManager = spy(authManager);
        doAnswer(new Answer<Void>() {
                     public Void answer(InvocationOnMock invocation) {
                         Object[] args = invocation.getArguments();
                         RegistrationListener regListener = (RegistrationListener) args[1];
                         regListener.onRegistrationSuccess();
                         return null;
                     }
                 }
        ).when(registrationManager).ensureRegistered(eq(mockActivity), any(RegistrationListener.class));

        when(mockActivity.getApplicationContext()).thenReturn(mockContext);
        spyAuthManager.launchAuthorizationUI(mockActivity, new AuthorizationListener() {
            @Override
            public void onAuthorizationFailure(AuthorizationException exception) {
                String expectedAuthUrl = "https://appid-oauth.stubPrefix/oauth/v3/null/authorization?response_type=code&client_id=null&redirect_uri=null&scope=openid";
                assertEquals(exception.getMessage(), "Could NOT find installed browser that support Chrome tabs on the device.");
                verify(spyAuthManager).createAuthorizationUIManager(any(OAuthManager.class), any(AuthorizationListener.class), eq(expectedAuthUrl), anyString());

            }

            @Override
            public void onAuthorizationSuccess(AccessToken accessToken, IdentityToken identityToken) {
                fail("should get to onAuthorizationFailure");
            }

            @Override
            public void onAuthorizationCanceled() {
                fail("should get to onAuthorizationFailure");
            }
        });
    }

    @Test
    public void launchSignUpAuthorizationUI_failure(){
        Activity activity = new Activity();
        doAnswer(new Answer<Void>() {
                     public Void answer(InvocationOnMock invocation) {
                         Object[] args = invocation.getArguments();
                         RegistrationListener regListener = (RegistrationListener) args[1];
                         regListener.onRegistrationFailure(RegistrationStatus.NOT_REGISTRED);
                         return null;
                     }
                 }
        ).when(registrationManager).ensureRegistered(eq(activity), any(RegistrationListener.class));

        authManager.launchSignUpAuthorizationUI(activity, new AuthorizationListener() {
            @Override
            public void onAuthorizationFailure(AuthorizationException exception) {
                assertEquals(exception.getMessage(), RegistrationStatus.NOT_REGISTRED.getDescription());
            }

            @Override
            public void onAuthorizationSuccess(AccessToken accessToken, IdentityToken identityToken) {
                fail("should get to onAuthorizationFailure");
            }

            @Override
            public void onAuthorizationCanceled() {
                fail("should get to onAuthorizationFailure");
            }
        });
    }

    @Test
    public void launchSigUpAuthorizationUI_success() throws Exception{

        final AuthorizationManager spyAuthManager =  spy(authManager);
        doAnswer(new Answer<Void>() {
                     public Void answer(InvocationOnMock invocation) {
                         Object[] args = invocation.getArguments();
                         RegistrationListener regListener = (RegistrationListener) args[1];
                         regListener.onRegistrationSuccess();
                         return null;
                     }
                 }
        ).when(registrationManager).ensureRegistered(eq(mockActivity), any(RegistrationListener.class));

        when(mockActivity.getApplicationContext()).thenReturn(mockContext);
        spyAuthManager.launchSignUpAuthorizationUI(mockActivity, new AuthorizationListener() {
            @Override
            public void onAuthorizationFailure(AuthorizationException exception) {
                String expectedAuthUrl = "https://appid-oauth.stubPrefix/oauth/v3/null/authorization?response_type=sign_up&client_id=null&redirect_uri=null&scope=openid";
                assertEquals(exception.getMessage(), "Could NOT find installed browser that support Chrome tabs on the device.");
                verify(spyAuthManager).createAuthorizationUIManager(any(OAuthManager.class), any(AuthorizationListener.class), eq(expectedAuthUrl), anyString());
            }

            @Override
            public void onAuthorizationSuccess(AccessToken accessToken, IdentityToken identityToken) {
                fail("should get to onAuthorizationFailure");
            }

            @Override
            public void onAuthorizationCanceled() {
                fail("should get to onAuthorizationFailure");
            }
        });
    }

    @Test
    public void launchChangePasswordUI_noIdToken() throws Exception {

        final AuthorizationManager spyAuthManager =  spy(authManager);
        when(mockActivity.getApplicationContext()).thenReturn(mockContext);
        spyAuthManager.launchChangePasswordUI(mockActivity, new AuthorizationListener() {
            @Override
            public void onAuthorizationFailure(AuthorizationException exception) {
                assertEquals(exception.getMessage(), "No identity token found.");
            }

            @Override
            public void onAuthorizationSuccess(AccessToken accessToken, IdentityToken identityToken) {
                fail("should get to onAuthorizationFailure");
            }

            @Override
            public void onAuthorizationCanceled() {
                fail("should get to onAuthorizationFailure");
            }
        });
    }

    @Test
    public void launchChangePasswordUI_IdTokenNotRetrievedWithCD() throws Exception {
        when(oAuthManagerMock.getTokenManager()).thenReturn(tokenManagerMock);
        when(tokenManagerMock.getLatestIdentityToken()).thenReturn(expectedIdToken);
        final AuthorizationManager spyAuthManager =  spy(authManager);
        when(mockActivity.getApplicationContext()).thenReturn(mockContext);
        spyAuthManager.launchChangePasswordUI(mockActivity, new AuthorizationListener() {
            @Override
            public void onAuthorizationFailure(AuthorizationException exception) {
                assertEquals(exception.getMessage(), "The identity token was not retrieved using cloud directory idp.");
            }

            @Override
            public void onAuthorizationSuccess(AccessToken accessToken, IdentityToken identityToken) {
                fail("should get to onAuthorizationFailure");
            }

            @Override
            public void onAuthorizationCanceled() {
                fail("should get to onAuthorizationFailure");
            }
        });
    }

    @Test
    public void launchChangePasswordUI_success() throws Exception {
        when(oAuthManagerMock.getTokenManager()).thenReturn(tokenManagerMock);
        when(tokenManagerMock.getLatestIdentityToken()).thenReturn(mockIdToken);
        JSONArray identitiesArray = new JSONArray();
        JSONObject providerObject = new JSONObject();
        providerObject.put("provider", "cloud_directory");
        providerObject.put("id", "1234");
        identitiesArray.put(providerObject);
        when(mockIdToken.getIdentities()).thenReturn(identitiesArray);
        final AuthorizationManager spyAuthManager =  spy(authManager);
        when(mockActivity.getApplicationContext()).thenReturn(mockContext);
        spyAuthManager.launchChangePasswordUI(mockActivity, new AuthorizationListener() {
            @Override
            public void onAuthorizationFailure(AuthorizationException exception) {
                String expectedAuthUrl = "https://appid-oauth.stubPrefix/oauth/v3/null/cloud_directory/change_password?user_id=1234";
                assertEquals(exception.getMessage(), "Could NOT find installed browser that support Chrome tabs on the device.");
                verify(spyAuthManager).createAuthorizationUIManager(any(OAuthManager.class), any(AuthorizationListener.class), eq(expectedAuthUrl), anyString());
            }

            @Override
            public void onAuthorizationSuccess(AccessToken accessToken, IdentityToken identityToken) {
                fail("should get to onAuthorizationFailure");
            }

            @Override
            public void onAuthorizationCanceled() {
                fail("should get to onAuthorizationFailure");
            }
        });
    }

    @Test
    public void launchChangePasswordUI_JSONException() throws Exception {
        when(oAuthManagerMock.getTokenManager()).thenReturn(tokenManagerMock);
        when(tokenManagerMock.getLatestIdentityToken()).thenReturn(mockIdToken);
        JSONArray identitiesArray = new JSONArray();
        JSONObject providerObject = new JSONObject();
        providerObject.put("provider", "cloud_directory");
        identitiesArray.put(providerObject);
        when(mockIdToken.getIdentities()).thenReturn(identitiesArray);
        final AuthorizationManager spyAuthManager =  spy(authManager);
        when(mockActivity.getApplicationContext()).thenReturn(mockContext);
        spyAuthManager.launchChangePasswordUI(mockActivity, new AuthorizationListener() {
            @Override
            public void onAuthorizationFailure(AuthorizationException exception) {
                assertEquals(exception.getMessage(), "No value for id");
            }

            @Override
            public void onAuthorizationSuccess(AccessToken accessToken, IdentityToken identityToken) {
                fail("should get to onAuthorizationFailure");
            }

            @Override
            public void onAuthorizationCanceled() {
                fail("should get to onAuthorizationFailure");
            }
        });
    }

    @Test
    public void launchChangeDetailsUI_noIdToken() throws Exception {

        final AuthorizationManager spyAuthManager =  spy(authManager);
        when(mockActivity.getApplicationContext()).thenReturn(mockContext);
        spyAuthManager.launchChangeDetailsUI(mockActivity, new AuthorizationListener() {
            @Override
            public void onAuthorizationFailure(AuthorizationException exception) {
                assertEquals(exception.getMessage(), "No identity token found.");
            }

            @Override
            public void onAuthorizationSuccess(AccessToken accessToken, IdentityToken identityToken) {
                fail("should get to onAuthorizationFailure");
            }

            @Override
            public void onAuthorizationCanceled() {
                fail("should get to onAuthorizationFailure");
            }
        });
    }

    @Test
    public void launchChangeDetailsUI_IdTokenNotRetrievedWithCD() throws Exception {
        when(oAuthManagerMock.getTokenManager()).thenReturn(tokenManagerMock);
        when(tokenManagerMock.getLatestIdentityToken()).thenReturn(expectedIdToken);
        final AuthorizationManager spyAuthManager =  spy(authManager);
        when(mockActivity.getApplicationContext()).thenReturn(mockContext);
        spyAuthManager.launchChangeDetailsUI(mockActivity, new AuthorizationListener() {
            @Override
            public void onAuthorizationFailure(AuthorizationException exception) {
                assertEquals(exception.getMessage(), "The identity token was not retrieved using cloud directory idp.");
            }

            @Override
            public void onAuthorizationSuccess(AccessToken accessToken, IdentityToken identityToken) {
                fail("should get to onAuthorizationFailure");
            }

            @Override
            public void onAuthorizationCanceled() {
                fail("should get to onAuthorizationFailure");
            }
        });
    }

    @Test
    public void launchChangeDetailsUI_success() throws Exception {
        when(oAuthManagerMock.getTokenManager()).thenReturn(tokenManagerMock);
        when(tokenManagerMock.getLatestIdentityToken()).thenReturn(mockIdToken);
        authManager.setAppIDRequestFactory(appIDRequestFactoryMock);
        when(appIDRequestFactoryMock.createRequest(anyString(), anyString())).thenReturn(mockRequest);
        doAnswer(new Answer<Void>() {
                     public Void answer(InvocationOnMock invocation) {
                         Object[] args = invocation.getArguments();
                         ResponseListener responseListener = (ResponseListener) args[0];
                         responseListener.onSuccess(testGoodResponse);
                         return null;
                     }
                 }
        ).when(mockRequest).send(any(ResponseListener.class), any(AccessToken.class), any(IdentityToken.class));
        JSONArray identitiesArray = new JSONArray();
        JSONObject providerObject = new JSONObject();
        providerObject.put("provider", "cloud_directory");
        providerObject.put("id", "1234");
        identitiesArray.put(providerObject);
        when(mockIdToken.getIdentities()).thenReturn(identitiesArray);
        final AuthorizationManager spyAuthManager =  spy(authManager);
        when(mockActivity.getApplicationContext()).thenReturn(mockContext);
        spyAuthManager.launchChangeDetailsUI(mockActivity, new AuthorizationListener() {
            @Override
            public void onAuthorizationFailure(AuthorizationException exception) {
                String expectedAuthUrl = "https://appid-oauth.stubPrefix/oauth/v3/null/cloud_directory/change_details?code=1234";
                assertEquals(exception.getMessage(), "Could NOT find installed browser that support Chrome tabs on the device.");
                verify(spyAuthManager).createAuthorizationUIManager(any(OAuthManager.class), any(AuthorizationListener.class), eq(expectedAuthUrl), anyString());
            }

            @Override
            public void onAuthorizationSuccess(AccessToken accessToken, IdentityToken identityToken) {
                fail("should get to onAuthorizationFailure");
            }

            @Override
            public void onAuthorizationCanceled() {
                fail("should get to onAuthorizationFailure");
            }
        });
    }

    @Test
    public void launchChangeDetailsUI_JSONException() throws Exception {
        when(oAuthManagerMock.getTokenManager()).thenReturn(tokenManagerMock);
        when(tokenManagerMock.getLatestIdentityToken()).thenReturn(mockIdToken);
        authManager.setAppIDRequestFactory(appIDRequestFactoryMock);
        when(appIDRequestFactoryMock.createRequest(anyString(), anyString())).thenReturn(mockRequest);
        doAnswer(new Answer<Void>() {
                     public Void answer(InvocationOnMock invocation) {
                         Object[] args = invocation.getArguments();
                         ResponseListener responseListener = (ResponseListener) args[0];
                         responseListener.onSuccess(testGoodResponse);
                         return null;
                     }
                 }
        ).when(mockRequest).send(any(ResponseListener.class), any(AccessToken.class), any(IdentityToken.class));
        JSONArray identitiesArray = new JSONArray();
        JSONObject providerObject = new JSONObject();
        identitiesArray.put(providerObject);
        when(mockIdToken.getIdentities()).thenReturn(identitiesArray);
        final AuthorizationManager spyAuthManager =  spy(authManager);
        when(mockActivity.getApplicationContext()).thenReturn(mockContext);
        spyAuthManager.launchChangeDetailsUI(mockActivity, new AuthorizationListener() {
            @Override
            public void onAuthorizationFailure(AuthorizationException exception) {
                assertEquals(exception.getMessage(), "No value for provider");
            }

            @Override
            public void onAuthorizationSuccess(AccessToken accessToken, IdentityToken identityToken) {
                fail("should get to onAuthorizationFailure");
            }

            @Override
            public void onAuthorizationCanceled() {
                fail("should get to onAuthorizationFailure");
            }
        });
    }

    @Test
    public void launchChangeDetailsUI_failure_in_request_with_response() throws Exception {
        when(oAuthManagerMock.getTokenManager()).thenReturn(tokenManagerMock);
        when(tokenManagerMock.getLatestIdentityToken()).thenReturn(mockIdToken);
        authManager.setAppIDRequestFactory(appIDRequestFactoryMock);
        when(appIDRequestFactoryMock.createRequest(anyString(), anyString())).thenReturn(mockRequest);
        doAnswer(new Answer<Void>() {
                     public Void answer(InvocationOnMock invocation) {
                         Object[] args = invocation.getArguments();
                         ResponseListener responseListener = (ResponseListener) args[0];
                         responseListener.onFailure(testResponse, null, null);
                         return null;
                     }
                 }
        ).when(mockRequest).send(any(ResponseListener.class), any(AccessToken.class), any(IdentityToken.class));
        JSONArray identitiesArray = new JSONArray();
        JSONObject providerObject = new JSONObject();
        providerObject.put("provider", "cloud_directory");
        providerObject.put("id", "1234");
        identitiesArray.put(providerObject);
        when(mockIdToken.getIdentities()).thenReturn(identitiesArray);
        final AuthorizationManager spyAuthManager =  spy(authManager);
        when(mockActivity.getApplicationContext()).thenReturn(mockContext);
        spyAuthManager.launchChangeDetailsUI(mockActivity, new AuthorizationListener() {
            @Override
            public void onAuthorizationFailure(AuthorizationException exception) {
                String expectedError = "Code request failure ,response: " + testResponse.toString();
                assertEquals(exception.getMessage(), expectedError);
            }

            @Override
            public void onAuthorizationSuccess(AccessToken accessToken, IdentityToken identityToken) {
                fail("should get to onAuthorizationFailure");
            }

            @Override
            public void onAuthorizationCanceled() {
                fail("should get to onAuthorizationFailure");
            }
        });
    }

    @Test
    public void launchChangeDetailsUI_failure_in_request_with_json() throws Exception {
        final JSONObject expectedInfo = new JSONObject();
        expectedInfo.put("error", "some error");
        when(oAuthManagerMock.getTokenManager()).thenReturn(tokenManagerMock);
        when(tokenManagerMock.getLatestIdentityToken()).thenReturn(mockIdToken);
        authManager.setAppIDRequestFactory(appIDRequestFactoryMock);
        when(appIDRequestFactoryMock.createRequest(anyString(), anyString())).thenReturn(mockRequest);
        doAnswer(new Answer<Void>() {
                     public Void answer(InvocationOnMock invocation) {
                         Object[] args = invocation.getArguments();
                         ResponseListener responseListener = (ResponseListener) args[0];
                         responseListener.onFailure(null, null, expectedInfo);
                         return null;
                     }
                 }
        ).when(mockRequest).send(any(ResponseListener.class), any(AccessToken.class), any(IdentityToken.class));
        JSONArray identitiesArray = new JSONArray();
        JSONObject providerObject = new JSONObject();
        providerObject.put("provider", "cloud_directory");
        providerObject.put("id", "1234");
        identitiesArray.put(providerObject);
        when(mockIdToken.getIdentities()).thenReturn(identitiesArray);
        final AuthorizationManager spyAuthManager =  spy(authManager);
        when(mockActivity.getApplicationContext()).thenReturn(mockContext);
        spyAuthManager.launchChangeDetailsUI(mockActivity, new AuthorizationListener() {
            @Override
            public void onAuthorizationFailure(AuthorizationException exception) {
                String expectedError = "Code request failure  ,extendedInfo: " + expectedInfo.toString();
                assertEquals(exception.getMessage(), expectedError);
            }

            @Override
            public void onAuthorizationSuccess(AccessToken accessToken, IdentityToken identityToken) {
                fail("should get to onAuthorizationFailure");
            }

            @Override
            public void onAuthorizationCanceled() {
                fail("should get to onAuthorizationFailure");
            }
        });
    }

    @Test
    public void launchChangeDetailsUI_failure_in_request_with_exception() throws Exception {
        final Throwable expectedException = new Throwable("some exception");
        when(oAuthManagerMock.getTokenManager()).thenReturn(tokenManagerMock);
        when(tokenManagerMock.getLatestIdentityToken()).thenReturn(mockIdToken);
        authManager.setAppIDRequestFactory(appIDRequestFactoryMock);
        when(appIDRequestFactoryMock.createRequest(anyString(), anyString())).thenReturn(mockRequest);
        doAnswer(new Answer<Void>() {
                     public Void answer(InvocationOnMock invocation) {
                         Object[] args = invocation.getArguments();
                         ResponseListener responseListener = (ResponseListener) args[0];
                         responseListener.onFailure(null, expectedException, null);
                         return null;
                     }
                 }
        ).when(mockRequest).send(any(ResponseListener.class), any(AccessToken.class), any(IdentityToken.class));
        JSONArray identitiesArray = new JSONArray();
        JSONObject providerObject = new JSONObject();
        providerObject.put("provider", "cloud_directory");
        providerObject.put("id", "1234");
        identitiesArray.put(providerObject);
        when(mockIdToken.getIdentities()).thenReturn(identitiesArray);
        final AuthorizationManager spyAuthManager =  spy(authManager);
        when(mockActivity.getApplicationContext()).thenReturn(mockContext);
        spyAuthManager.launchChangeDetailsUI(mockActivity, new AuthorizationListener() {
            @Override
            public void onAuthorizationFailure(AuthorizationException exception) {
                String expectedError = "Code request failure  ,exception: " + expectedException.getMessage();
                assertEquals(exception.getMessage(), expectedError);
            }

            @Override
            public void onAuthorizationSuccess(AccessToken accessToken, IdentityToken identityToken) {
                fail("should get to onAuthorizationFailure");
            }

            @Override
            public void onAuthorizationCanceled() {
                fail("should get to onAuthorizationFailure");
            }
        });
    }

    @Test
    public void launchForgotPasswordUI_failure() throws Exception {

        final AuthorizationManager spyAuthManager =  spy(authManager);
        doAnswer(new Answer<Void>() {
                     public Void answer(InvocationOnMock invocation) {
                         Object[] args = invocation.getArguments();
                         RegistrationListener regListener = (RegistrationListener) args[1];
                         regListener.onRegistrationSuccess();
                         return null;
                     }
                 }
        ).when(registrationManager).ensureRegistered(eq(mockActivity), any(RegistrationListener.class));

        when(mockActivity.getApplicationContext()).thenReturn(mockContext);
        spyAuthManager.launchForgotPasswordUI(mockActivity, new AuthorizationListener() {
            @Override
            public void onAuthorizationSuccess(AccessToken accessToken, IdentityToken identityToken) {
                fail("should get to onAuthorizationFailure");
            }

            @Override
            public void onAuthorizationFailure(AuthorizationException exception) {
                String expectedAuthUrl = "https://appid-oauth.stubPrefix/oauth/v3/null/cloud_directory/forgot_password";
                assertEquals(exception.getMessage(), "Could NOT find installed browser that support Chrome tabs on the device.");
                verify(spyAuthManager).createAuthorizationUIManager(any(OAuthManager.class), any(AuthorizationListener.class), eq(expectedAuthUrl), anyString());
            }

            @Override
            public void onAuthorizationCanceled() {
                fail("should get to onAuthorizationFailure");
            }
        });
    }
}
