package com.ibm.bluemix.appid.android.internal.authorizationmanager;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.test.mock.MockContext;

import com.ibm.bluemix.appid.android.api.AppID;
import com.ibm.bluemix.appid.android.api.AuthorizationException;
import com.ibm.bluemix.appid.android.api.TokenResponseListener;
import com.ibm.bluemix.appid.android.api.tokens.AccessToken;
import com.ibm.bluemix.appid.android.api.tokens.IdentityToken;
import com.ibm.bluemix.appid.android.internal.OAuthManager;
import com.ibm.bluemix.appid.android.internal.registrationmanager.RegistrationListener;
import com.ibm.bluemix.appid.android.internal.registrationmanager.RegistrationManager;
import com.ibm.bluemix.appid.android.internal.registrationmanager.RegistrationStatus;
import com.ibm.bluemix.appid.android.internal.tokenmanager.TokenManager;
import com.ibm.bluemix.appid.android.internal.tokens.AccessTokenImpl;
import com.ibm.bluemix.appid.android.internal.tokens.IdentityTokenImpl;
import com.ibm.bluemix.appid.android.testing.helpers.Consts;
import com.ibm.mobilefirstplatform.appid_clientsdk_android.BuildConfig;

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
//import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.modules.junit4.PowerMockRunner;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.doAnswer;

//@RunWith(MockitoJUnitRunner.class)
@RunWith(PowerMockRunner.class)
//@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@PrepareForTest(AuthorizationUIManager.class)
//@Config(constants = BuildConfig.class)
public class AuthorizationManager_Test {

//    @Mock private OAuthManager oAuthManagerMock;
//    @Mock private RegistrationManager registrationManager;
//    @Mock private TokenManager tokenManagerMock;
//    @Mock private AppID appidMock;
//    @Mock private PackageManager pmMock;
//    @Mock private Context mockContext;
    private AuthorizationManager authManager;
//    private String username = "testUser";
//    private String password = "testPassword";
//    RegistrationManager rm;
//    @Mock Context mc;
//    private AccessToken expectedAccessToken = null;//new AccessTokenImpl(Consts.ACCESS_TOKEN);
//    private IdentityToken expectedIdToken = null;//new IdentityTokenImpl(Consts.ID_TOKEN);


    @Before
    public void before(){
//         rm = Mockito.mock(RegistrationManager.class);
//        MockitoAnnotations.initMocks(this);
//        when(oAuthManagerMock.getTokenManager()).thenReturn(tokenManagerMock);
//        doAnswer(new Answer<Void>() {
//                     @Override
//                     public Void answer(InvocationOnMock invocation) {
//                         Object[] args = invocation.getArguments();
//                         TokenResponseListener tokenListener = (TokenResponseListener) args[2];
//                         tokenListener.onAuthorizationSuccess(expectedAccessToken, expectedIdToken);
//                         return null;
//                     }
//                 }
//        ).when(tokenManagerMock).obtainTokens(eq(username), eq(password), any(TokenResponseListener.class));
//
//        when(oAuthManagerMock.getAppId()).thenReturn(appidMock);
//
//        when(oAuthManagerMock.getRegistrationManager()).thenReturn(registrationManager);
//        when(appidMock.getBluemixRegionSuffix()).thenReturn(".stubPrefix");
//        when(mockContext.getPackageManager()).thenReturn(pmMock);
//        when(pmMock.resolveActivity(eq(any(Intent.class)), 0)).thenReturn(null);
//        when(pmMock.queryIntentActivities(eq(any(Intent.class)), 0)).thenReturn(null);



//        oAuthManagerMock = Mockito.mock(OAuthManager.class);
//        appidMock = Mockito.mock(AppID.class);;
//        registrationManager = Mockito.mock(RegistrationManager.class);

//        MockitoAnnotations.initMocks(this);
//        when(oAuthManagerMock.getAppId()).thenReturn(appidMock);
//        when(oAuthManagerMock.getRegistrationManager()).thenReturn(registrationManager);
//        mc = Mockito.mock(Context.class);

        PowerMockito.mockStatic(AuthorizationUIManager.class);
        PowerMockito.doNothing().when(AuthorizationUIManager.class);
//        AuthorizationUIManager.bindCustomTabsService(mc, any(String.class));

//        authManager = new AuthorizationManager(oAuthManagerMock, mc);


    }

    @Test
    public void obtainTokensWithROP_regisrationFailure() {




//        doAnswer(new Answer<Void>() {
//                    @Override
//                     public Void answer(InvocationOnMock invocation) {
//                         Object[] args = invocation.getArguments();
//                         RegistrationListener regListener = (RegistrationListener) args[1];
//                         regListener.onRegistrationFailure(RegistrationStatus.NOT_REGISTRED);
//                         return null;
//                     }
//                 }).when(rm).ensureRegistered(null, eq(any(RegistrationListener.class)));
//
//
//
//        authManager.obtainTokensWithROP(null, username, password, new TokenResponseListener() {
//            @Override
//            public void onAuthorizationFailure(AuthorizationException exception) {
////                assertThat(exception.getMessage().equals(RegistrationStatus.NOT_REGISTRED.getDescription()));
//            }
//
//            @Override
//            public void onAuthorizationSuccess(AccessToken accessToken, IdentityToken identityToken) {
////                assert(false);
//            }
//        });
    }

    @Test
    public void obtainTokensWithROP_regisrationSuccess() {
//        doAnswer(new Answer<Void>() {
//                     public Void answer(InvocationOnMock invocation) {
//                         Object[] args = invocation.getArguments();
//                         RegistrationListener regListener = (RegistrationListener) args[1];
//                         regListener.onRegistrationSuccess();
//                         return null;
//                     }
//                 }
//        ).when(registrationManager).ensureRegistered(RuntimeEnvironment.application, eq(any(RegistrationListener.class)));
//
//        authManager.obtainTokensWithROP(RuntimeEnvironment.application, username, password, new TokenResponseListener() {
//            @Override
//            public void onAuthorizationFailure(AuthorizationException exception) {
//                assert(false);
//            }
//
//            @Override
//            public void onAuthorizationSuccess(AccessToken accessToken, IdentityToken identityToken) {
//                assertThat(accessToken.getRaw().equals(expectedAccessToken));
//                assertThat(identityToken.getRaw().equals(expectedIdToken));
//            }
//        });
    }
}
