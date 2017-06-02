package com.ibm.bluemix.appid.android.internal.authorizationmanager;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
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
    private AuthorizationManager authManager;
    private String username = "testUser";
    private String password = "testPassword";
    private String testError = "Some Error";
    private static final AccessToken expectedAccessToken = new AccessTokenImpl(Consts.ACCESS_TOKEN);
    private static final IdentityToken expectedIdToken = new IdentityTokenImpl(Consts.ID_TOKEN);

    private Response testResponse = new Response() {
        @Override
        public int getStatus() {
            return 0;
        }

        @Override
        public String getResponseText() {
            return testError;
        }

        @Override
        public byte[] getResponseBytes() {
            return new byte[0];
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
        when(pmMock.resolveActivity(eq(any(Intent.class)), 0)).thenReturn(null);
        when(pmMock.queryIntentActivities(eq(any(Intent.class)), 0)).thenReturn(null);

        when(oAuthManagerMock.getTokenManager()).thenReturn(tokenManagerMock);
        doAnswer(new Answer<Void>() {
                     @Override
                     public Void answer(InvocationOnMock invocation) {
                         Object[] args = invocation.getArguments();
                         TokenResponseListener tokenListener = (TokenResponseListener) args[2];
                         tokenListener.onAuthorizationSuccess(expectedAccessToken, expectedIdToken);
                         return null;
                     }
                 }
        ).when(tokenManagerMock).obtainTokens(eq(username), eq(password), any(TokenResponseListener.class));

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


        authManager.obtainTokensWithROP(mockContext, username, password, new TokenResponseListener() {
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

        authManager.obtainTokensWithROP(mockContext, username, password, new TokenResponseListener() {
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
        authManager.launchAuthorizationUI(mockActivity, new AuthorizationListener() {
            @Override
            public void onAuthorizationFailure(AuthorizationException exception) {
                assertEquals(exception.getMessage(), "Could NOT find installed browser that support Chrome tabs on the device.");
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
}
