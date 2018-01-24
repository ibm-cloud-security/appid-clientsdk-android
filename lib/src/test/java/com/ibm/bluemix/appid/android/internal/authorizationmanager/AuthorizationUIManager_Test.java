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
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;

import com.ibm.bluemix.appid.android.api.AuthorizationException;
import com.ibm.bluemix.appid.android.api.AuthorizationListener;
import com.ibm.bluemix.appid.android.api.tokens.AccessToken;
import com.ibm.bluemix.appid.android.api.tokens.IdentityToken;
import com.ibm.bluemix.appid.android.api.tokens.RefreshToken;
import com.ibm.bluemix.appid.android.internal.OAuthManager;

import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

import java.util.ArrayList;
import java.util.List;

import static com.ibm.bluemix.appid.android.internal.authorizationmanager.AuthorizationUIManager.EXTRA_REDIRECT_URL;
import static com.ibm.bluemix.appid.android.internal.authorizationmanager.AuthorizationUIManager.EXTRA_URL;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class AuthorizationUIManager_Test {

    @Mock
    private Context mockContext;
    @Mock
    private OAuthManager mockOAuthManager;
    @Mock
    private PackageManager pmMock;
    @Mock
    private ResolveInfo resolveInfoMock;
    @Mock
    private ResolveInfo resolveInfoMock2;
    @Mock
    private ActivityInfo activityInfoMock;
    @Mock
    private ActivityInfo activityInfoMock2;
    @Mock
    private Activity activityMock;

    private AuthorizationListener authorizationListener;
    private static final String STABLE_PACKAGE = "com.android.chrome";
    private static final String BETA_PACKAGE = "com.chrome.beta";
    private static final String DEV_PACKAGE = "com.chrome.dev";
    private static final String LOCAL_PACKAGE = "com.google.android.apps.chrome";
    private Intent spyIntent = Mockito.spy(new Intent());
    private AuthorizationUIManager authorizationUIManager;
    private AuthorizationUIManager authorizationUIManagerSpy;
    private List<ResolveInfo> resolveInfos;

    @Before
    public void before(){
        MockitoAnnotations.initMocks(this);
        when(mockContext.getPackageManager()).thenReturn(pmMock);
        resolveInfos = new ArrayList<>();
        resolveInfoMock.activityInfo = activityInfoMock;
        activityInfoMock.packageName = "com.android.chrome_test";
        resolveInfos.add(resolveInfoMock);
        when(pmMock.queryIntentActivities(any(Intent.class), eq(0))).thenReturn(resolveInfos);
        when(pmMock.resolveService(any(Intent.class), eq(0))).thenReturn(resolveInfoMock);
        Mockito.doReturn(mockContext).when(activityMock).getApplicationContext();
        authorizationListener = new AuthorizationListener() {
            @Override
            public void onAuthorizationCanceled() {
                fail("should get to onAuthorizationFailure");
            }

            @Override
            public void onAuthorizationFailure(AuthorizationException exception) {
                assertEquals(exception.getMessage(), "Could NOT find installed browser that support Chrome tabs on the device.");
            }

            @Override
            public void onAuthorizationSuccess(AccessToken accessToken, IdentityToken identityToken, RefreshToken refreshToken) {
                fail("should get to onAuthorizationFailure");
            }
        };
        authorizationUIManager = new AuthorizationUIManager(mockOAuthManager, authorizationListener, "https://serverurlTest", "https://redirectTest");
        authorizationUIManagerSpy = Mockito.spy(authorizationUIManager);
        Mockito.doReturn(spyIntent).when(authorizationUIManagerSpy).createChromeTabIntent(activityMock);
        resolveInfoMock2.activityInfo = activityInfoMock2;

    }

    @Test
    public void launch_test() {
        Mockito.reset(spyIntent);
        authorizationUIManagerSpy.launch(activityMock);
        Mockito.verify(spyIntent).putExtra(EXTRA_REDIRECT_URL, "https://redirectTest");
        Mockito.verify(spyIntent).putExtra(EXTRA_URL, "https://serverurlTest");

    }

    @Test
    public void launch_test_LOCAL_PACKAGE() {
        Mockito.reset(spyIntent);
        activityInfoMock2.packageName = LOCAL_PACKAGE;
        resolveInfos.add(resolveInfoMock2);
        authorizationUIManagerSpy.reset_sPackageNameToUse();
        authorizationUIManagerSpy.launch(activityMock);
        Mockito.verify(spyIntent).putExtra(EXTRA_REDIRECT_URL, "https://redirectTest");
        Mockito.verify(spyIntent).putExtra(EXTRA_URL, "https://serverurlTest");

    }

    @Test
    public void launch_test_DEV_PACKAGE() {
        Mockito.reset(spyIntent);
        activityInfoMock2.packageName = DEV_PACKAGE;
        resolveInfos.add(resolveInfoMock2);
        authorizationUIManagerSpy.reset_sPackageNameToUse();
        authorizationUIManagerSpy.launch(activityMock);
        Mockito.verify(spyIntent).putExtra(EXTRA_REDIRECT_URL, "https://redirectTest");
        Mockito.verify(spyIntent).putExtra(EXTRA_URL, "https://serverurlTest");

    }
    @Test
    public void launch_test_BETA_PACKAGE() {
        Mockito.reset(spyIntent);
        activityInfoMock2.packageName = BETA_PACKAGE;
        resolveInfos.add(resolveInfoMock2);
        authorizationUIManagerSpy.reset_sPackageNameToUse();
        authorizationUIManagerSpy.launch(activityMock);
        Mockito.verify(spyIntent).putExtra(EXTRA_REDIRECT_URL, "https://redirectTest");
        Mockito.verify(spyIntent).putExtra(EXTRA_URL, "https://serverurlTest");

    }
    @Test
    public void launch_test_STABLE_PACKAGE() {
        Mockito.reset(spyIntent);
        activityInfoMock2.packageName = STABLE_PACKAGE;
        resolveInfos.add(resolveInfoMock2);
        authorizationUIManagerSpy.reset_sPackageNameToUse();
        authorizationUIManagerSpy.launch(activityMock);
        Mockito.verify(spyIntent).putExtra(EXTRA_REDIRECT_URL, "https://redirectTest");
        Mockito.verify(spyIntent).putExtra(EXTRA_URL, "https://serverurlTest");

    }

    @Test
    public void launch_test_hasSpecializedHandlerIntents_test() {
        Mockito.reset(spyIntent);
        authorizationUIManagerSpy.reset_sPackageNameToUse();
        activityInfoMock2.packageName = STABLE_PACKAGE;
        resolveInfos.add(resolveInfoMock2);
        when(pmMock.resolveActivity(any(Intent.class), eq(0))).thenReturn(resolveInfoMock);

        authorizationUIManagerSpy.launch(activityMock);
        Mockito.verify(spyIntent).putExtra(EXTRA_REDIRECT_URL, "https://redirectTest");
        Mockito.verify(spyIntent).putExtra(EXTRA_URL, "https://serverurlTest");

    }

    @Test
    public void launch_test_hasSpecializedHandlerIntents_return_true() {
        authorizationUIManagerSpy.reset_sPackageNameToUse();
        activityInfoMock2.packageName = STABLE_PACKAGE;
        resolveInfos.add(resolveInfoMock2);
        IntentFilter mockFilter = Mockito.mock(IntentFilter.class);
        resolveInfoMock2.filter = mockFilter;
        resolveInfoMock2.activityInfo = Mockito.mock(ActivityInfo.class);
        when(mockFilter.countDataAuthorities()).thenReturn(3);
        when(mockFilter.countDataPaths()).thenReturn(3);

        when(pmMock.resolveActivity(any(Intent.class), eq(0))).thenReturn(resolveInfoMock);

        when(pmMock.queryIntentActivities(any(Intent.class), eq(PackageManager.GET_RESOLVED_FILTER))).thenReturn(resolveInfos);

        authorizationUIManagerSpy.launch(activityMock);


    }
}
