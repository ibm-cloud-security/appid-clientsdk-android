package com.ibm.mobilefirstplatform.appid_clientsdk_android;

import android.content.Context;
import android.content.Intent;

import com.ibm.mobilefirstplatform.clientsdk.android.core.api.Response;
import com.ibm.mobilefirstplatform.clientsdk.android.core.api.ResponseListener;
import com.ibm.mobilefirstplatform.clientsdk.android.security.api.UserIdentity;
import com.ibm.mobilefirstplatform.clientsdk.android.security.mca.internal.preferences.AuthorizationManagerPreferences;
import com.ibm.mobilefirstplatform.clientsdk.android.security.mca.internal.preferences.SharedPreferencesManager;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.reflect.Whitebox;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.doAnswer;

/**
 * Created by rotembr on 17/12/2016.
 */

public class AppIdUnitTest {

    private AppId mockAppId;
    private AppIdAuthorizationManager mockAppIdAM;
    private AuthorizationManagerPreferences mockPreferences;
    private AppIdRegistrationManager mockAppIdRM;
    private Context mockContext;
    private ResponseListener testListener;
    private Map facebookMap;
    private Map googleMap;
    private String facebookRealm = "wl_facebookRealm";
    private String googleRealm = "wl_googleRealm";
    private String googlePicUrl = "http://googlePicUrl.test";
    private String facebookPicUrl = "http://facebookPicUrl.test";

    @Before
    public void setUp() throws Exception{
        mockContext = mock(Context.class);
        mockAppIdAM = mock(AppIdAuthorizationManager.class);
        mockPreferences = mock(AuthorizationManagerPreferences.class);

        mockPreferences.clientId =  mock(SharedPreferencesManager.StringPreference.class);
        SharedPreferencesManager.JSONPreference userIdentity = mock(SharedPreferencesManager.JSONPreference.class);
        mockPreferences.userIdentity = userIdentity;

        mockAppIdRM = mock(AppIdRegistrationManager.class);
        when(mockAppIdAM.getAppIdRegistrationManager()).thenReturn(mockAppIdRM);

        mockAppId = Whitebox.invokeConstructor(AppId.class);
        Whitebox.setInternalState(mockAppId, "appIdAuthorizationManager",mockAppIdAM);
        Whitebox.setInternalState(mockAppId, "amPreferences", mockPreferences);
        PowerMockito.mockStatic(AppId.class);

        testListener = new ResponseListener() {
            @Override
            public void onSuccess(Response response) {

            }

            @Override
            public void onFailure(Response response, Throwable t, JSONObject extendedInfo) {

            }
        };

        facebookMap = new HashMap();
        facebookMap.put(UserIdentity.AUTH_BY, facebookRealm);
        JSONObject attrfb =  mock(JSONObject.class);
        JSONObject picMock = mock(JSONObject.class);
        JSONObject dataMock = mock(JSONObject.class);
        when(dataMock.getString("url")).thenReturn(facebookPicUrl);
        when(picMock.getJSONObject("data")).thenReturn(dataMock);
        when(attrfb.getJSONObject("picture")).thenReturn(picMock);
        facebookMap.put("attributes",attrfb);

        googleMap = new HashMap();
        googleMap.put(UserIdentity.AUTH_BY, googleRealm);
        JSONObject attr = mock(JSONObject.class);
        when(attr.getString("picture")).thenReturn(googlePicUrl);
        googleMap.put("attributes",attr);
    }

    @Test
    public void loginTestSuccess() throws Exception {
        when(mockPreferences.clientId.get()).thenReturn(null);

        doAnswer(new Answer<Void>() {
            public Void answer(InvocationOnMock invocation) {
                Object[] args = invocation.getArguments();
                ResponseListener listener = (ResponseListener) args[0];
                listener.onSuccess(mock(Response.class));
                return null;
            }
        }).when(mockAppIdRM).invokeInstanceRegistrationRequest(any(ResponseListener.class));

        mockAppId.login(mockContext, testListener);

        verify(mockAppIdAM).setResponseListener(testListener);
        verify(mockAppIdAM).getAppIdRegistrationManager();
        verify(mockAppIdRM).invokeInstanceRegistrationRequest(any(ResponseListener.class));
        verify(mockContext).startActivity(any(Intent.class));

    }

    @Test
    public void loginTestFailure() throws Exception {
        when(mockPreferences.clientId.get()).thenReturn(null);

        doAnswer(new Answer<Void>() {
            public Void answer(InvocationOnMock invocation) throws JSONException {
                Object[] args = invocation.getArguments();
                ResponseListener listener = (ResponseListener) args[0];
                JSONObject extendedInfo = mock(JSONObject.class);
                extendedInfo.put("test_error", "ErrorCode");
                listener.onFailure(null,null,extendedInfo);
                return null;
            }
        }).when(mockAppIdRM).invokeInstanceRegistrationRequest(any(ResponseListener.class));

        ResponseListener testListener = new ResponseListener() {
            @Override
            public void onSuccess(Response response) {
                fail();
            }

            @Override
            public void onFailure(Response response, Throwable t, JSONObject extendedInfo) {
                try {
                    verify(extendedInfo).put("test_error", "ErrorCode");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };

        mockAppId.login(null, testListener);
        verify(mockAppIdAM).setResponseListener(testListener);
        verify(mockAppIdAM).getAppIdRegistrationManager();
        verify(mockAppIdRM).invokeInstanceRegistrationRequest(any(ResponseListener.class));

    }

    @Test
    public void loginTessNoRegistration() throws Exception {
        when(mockPreferences.clientId.get()).thenReturn("clientId");
        mockAppId.login(mockContext, testListener);
        verify(mockContext).startActivity(any(Intent.class));
    }

    @Test
    public void getInstanceTest(){
        try{
            AppId.getInstance();
            fail();
        } catch (IllegalStateException e){

        }
    }

    @Test
    public void getCachedAuthorizationHeaderTest(){
        mockAppId.getCachedAuthorizationHeader();
        verify(mockAppIdAM).getCachedAuthorizationHeader();
    }

    @Test
    public void getUserIdentityTest(){
        mockAppId.getUserIdentity();
        verify(mockAppIdAM).getUserIdentity();
    }

    @Test
    public void getUserProfilePictureTestFacebookUser() throws JSONException, MalformedURLException {
        when(mockPreferences.userIdentity.getAsMap()).thenReturn(facebookMap);
        assertEquals(mockAppId.getUserProfilePicture(), new URL(facebookPicUrl));
    }

    @Test
    public void getUserProfilePictureTestGoogleUser() throws MalformedURLException {
        when(mockPreferences.userIdentity.getAsMap()).thenReturn(googleMap);
        assertEquals(mockAppId.getUserProfilePicture(), new URL(googlePicUrl));

    }

    @Test
    public void getUserProfilePictureNoUser() {
        when(mockPreferences.userIdentity.getAsMap()).thenReturn(null);
        assertEquals(mockAppId.getUserProfilePicture(), null);

    }


}
