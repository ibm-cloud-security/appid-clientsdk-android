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

package com.ibm.bluemix.appid.android;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;


public class AppIdUnitTest {
//
//    private AppId mockAppId;
//    private AppIdAuthorizationManager mockAppIdAM;
//    private AppIdPreferences mockPreferences;
//    private AppIdRegistrationManager mockAppIdRM;
//    private CustomTabManager mockCustomTabManager;
//    private Activity mockActivity;
//    private ResponseListener testListener;
//    private Map facebookMap;
//    private Map googleMap;
//    private String facebookRealm = "wl_facebookRealm";
//    private String googleRealm = "wl_googleRealm";
//    private String googlePicUrl = "http://googlePicUrl.test";
//    private String facebookPicUrl = "http://facebookPicUrl.test";
//    private String testTenant = "TestTenantId";
//    @Before
//    public void setUp() throws Exception{
//        mockActivity = mock(Activity.class);
//        mockAppIdAM = mock(AppIdAuthorizationManager.class);
//        mockPreferences = mock(AppIdPreferences.class);
//
//        mockPreferences.clientId =  mock(SharedPreferencesManager.StringPreference.class);
//        SharedPreferencesManager.JSONPreference userIdentity = mock(SharedPreferencesManager.JSONPreference.class);
//        mockPreferences.userIdentity = userIdentity;
//
//        mockPreferences.tenantId = mock(AppIdPreferences.StringPreference.class);
//
//        mockAppIdRM = mock(AppIdRegistrationManager.class);
//        when(mockAppIdAM.getAppIdRegistrationManager()).thenReturn(mockAppIdRM);
//
//        mockCustomTabManager = mock(CustomTabManager.class);
//        when(mockAppIdAM.getCustomTabManager()).thenReturn(mockCustomTabManager);
//
//        mockAppId = Whitebox.invokeConstructor(AppId.class);
//        Whitebox.setInternalState(mockAppId, "appIdAuthorizationManager",mockAppIdAM);
//        Whitebox.setInternalState(mockAppId, "preferences", mockPreferences);
//        Whitebox.setInternalState(mockAppId, "tenantId", testTenant);
//        PowerMockito.mockStatic(AppId.class);
//
//        testListener = new ResponseListener() {
//            @Override
//            public void onSuccess(Response response) {
//
//            }
//
//            @Override
//            public void onFailure(Response response, Throwable t, JSONObject extendedInfo) {
//
//            }
//        };
//
//        facebookMap = new HashMap();
//        facebookMap.put(UserIdentity.AUTH_BY, facebookRealm);
//        JSONObject attrfb =  mock(JSONObject.class);
//        JSONObject picMock = mock(JSONObject.class);
//        JSONObject dataMock = mock(JSONObject.class);
//        when(dataMock.getString("url")).thenReturn(facebookPicUrl);
//        when(picMock.getJSONObject("data")).thenReturn(dataMock);
//        when(attrfb.getJSONObject("picture")).thenReturn(picMock);
//        facebookMap.put("attributes",attrfb);
//
//        googleMap = new HashMap();
//        googleMap.put(UserIdentity.AUTH_BY, googleRealm);
//        JSONObject attr = mock(JSONObject.class);
//        when(attr.getString("picture")).thenReturn(googlePicUrl);
//        googleMap.put("attributes",attr);
//    }
//
//    @Test
//    public void loginTestSuccess() throws Exception {
//        when(mockPreferences.clientId.get()).thenReturn(null);
//
//        doAnswer(new Answer<Void>() {
//            public Void answer(InvocationOnMock invocation) {
//                Object[] args = invocation.getArguments();
//                ResponseListener listener = (ResponseListener) args[1];
//                listener.onSuccess(mock(Response.class));
//                return null;
//            }
//        }).when(mockAppIdRM).invokeInstanceRegistrationRequest(any(Context.class), any(ResponseListener.class));
//
//        mockAppId.login(mockActivity, testListener);
//
//        verify(mockAppIdAM).setResponseListener(testListener);
//        verify(mockAppIdAM).getAppIdRegistrationManager();
//        verify(mockAppIdRM).invokeInstanceRegistrationRequest(any(Context.class), any(ResponseListener.class));
//        verify(mockCustomTabManager).launchBrowserTab(any(Activity.class),any(Uri.class));
//        verify(mockPreferences.tenantId).set(testTenant);
//
//    }
//
//    @Test
//    public void loginTestDifferentTenant() throws Exception {
//        when(mockPreferences.clientId.get()).thenReturn("clientId");
//        when(mockPreferences.tenantId.get()).thenReturn("diff_tenantId");
//        doAnswer(new Answer<Void>() {
//            public Void answer(InvocationOnMock invocation) {
//                Object[] args = invocation.getArguments();
//                ResponseListener listener = (ResponseListener) args[1];
//                listener.onSuccess(mock(Response.class));
//                return null;
//            }
//        }).when(mockAppIdRM).invokeInstanceRegistrationRequest(any(Context.class), any(ResponseListener.class));
//
//        mockAppId.login(mockActivity, testListener);
//
//        verify(mockAppIdAM).setResponseListener(testListener);
//        verify(mockAppIdAM).getAppIdRegistrationManager();
//        verify(mockAppIdRM).invokeInstanceRegistrationRequest(any(Context.class), any(ResponseListener.class));
//        verify(mockCustomTabManager).launchBrowserTab(any(Activity.class),any(Uri.class));
//        verify(mockPreferences.tenantId).set(testTenant);
//
//    }
//
//    @Test
//    public void loginTestFailure() throws Exception {
//        when(mockPreferences.clientId.get()).thenReturn(null);
//
//        doAnswer(new Answer<Void>() {
//            public Void answer(InvocationOnMock invocation) throws JSONException {
//                Object[] args = invocation.getArguments();
//                ResponseListener listener = (ResponseListener) args[1];
//                JSONObject extendedInfo = mock(JSONObject.class);
//                extendedInfo.put("test_error", "ErrorCode");
//                listener.onFailure(null,null,extendedInfo);
//                return null;
//            }
//        }).when(mockAppIdRM).invokeInstanceRegistrationRequest(any(Context.class), any(ResponseListener.class));
//
//        ResponseListener testListener = new ResponseListener() {
//            @Override
//            public void onSuccess(Response response) {
//                fail();
//            }
//
//            @Override
//            public void onFailure(Response response, Throwable t, JSONObject extendedInfo) {
//                try {
//                    verify(extendedInfo).put("test_error", "ErrorCode");
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//            }
//        };
//
//        mockAppId.login(mockActivity, testListener);
//        verify(mockAppIdAM).setResponseListener(testListener);
//        verify(mockAppIdAM).getAppIdRegistrationManager();
//        verify(mockAppIdRM).invokeInstanceRegistrationRequest(any(Context.class), any(ResponseListener.class));
//
//    }
//
//    @Test
//    public void loginTestNoRegistration() throws Exception {
//        when(mockPreferences.clientId.get()).thenReturn("clientId");
//        when(mockPreferences.tenantId.get()).thenReturn(testTenant);
//        mockAppId.login(mockActivity, testListener);
//        verify(mockCustomTabManager).launchBrowserTab(any(Activity.class),any(Uri.class));
//    }
//
//    @Test
//    public void getInstanceTest(){
//        try{
//            AppId.getInstance();
//            fail();
//        } catch (IllegalStateException e){
//
//        }
//    }
//
//    @Test
//    public void getCachedAuthorizationHeaderTest(){
//        mockAppId.getCachedAuthorizationHeader();
//        verify(mockAppIdAM).getCachedAuthorizationHeader();
//    }
//
//    @Test
//    public void getUserIdentityTest(){
//        mockAppId.getUserIdentity();
//        verify(mockAppIdAM).getUserIdentity();
//    }
//
//    @Test
//    public void getUserProfilePictureTestFacebookUser() throws JSONException, MalformedURLException {
//        when(mockPreferences.userIdentity.getAsMap()).thenReturn(facebookMap);
//        assertEquals(mockAppId.getUserProfilePicture(), facebookPicUrl);
//    }
//
//    @Test
//    public void getUserProfilePictureTestGoogleUser() throws MalformedURLException {
//        when(mockPreferences.userIdentity.getAsMap()).thenReturn(googleMap);
//        assertEquals(mockAppId.getUserProfilePicture(), googlePicUrl);
//
//    }
//
//    @Test
//    public void getUserProfilePictureNoUser() {
//        when(mockPreferences.userIdentity.getAsMap()).thenReturn(null);
//        assertEquals(mockAppId.getUserProfilePicture(), null);
//
//    }
//

}
