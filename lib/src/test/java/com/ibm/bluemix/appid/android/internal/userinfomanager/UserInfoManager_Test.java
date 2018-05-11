/*
	Copyright 2018 IBM Corp.
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
package com.ibm.bluemix.appid.android.internal.userinfomanager;

import android.content.Context;
import android.content.pm.PackageManager;

import com.ibm.bluemix.appid.android.api.AppID;
import com.ibm.bluemix.appid.android.api.tokens.AccessToken;
import com.ibm.bluemix.appid.android.api.tokens.IdentityToken;
import com.ibm.bluemix.appid.android.api.userinfo.UserInfoResponseListener;
import com.ibm.bluemix.appid.android.api.userinfo.UserInfoException;
import com.ibm.bluemix.appid.android.internal.network.AppIDRequest;
import com.ibm.bluemix.appid.android.internal.tokenmanager.TokenManager;
import com.ibm.bluemix.appid.android.internal.tokens.AccessTokenImpl;
import com.ibm.bluemix.appid.android.internal.tokens.IdentityTokenImpl;
import com.ibm.bluemix.appid.android.testing.helpers.Consts;
import com.ibm.mobilefirstplatform.clientsdk.android.core.api.Response;
import com.ibm.mobilefirstplatform.clientsdk.android.core.api.ResponseListener;
import okhttp3.RequestBody;

import org.json.JSONException;
import org.json.JSONObject;
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
import org.robolectric.RobolectricTestRunner;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class UserInfoManager_Test {

    @Mock
    private RequestBody stubRequestBody;
    @Mock
    private AppIDRequest stubRequest;
    @Mock
    private AppIDRequest specificStubRequest;
    @Mock
    private TokenManager tokenManagerMock;
    @Mock
    private Context mockContext;
    @Mock
    private PackageManager pmMock;

    private UserInfoManager userInfoManagerSpy;
    private static final AccessToken expectedAccessToken = new AccessTokenImpl(Consts.ACCESS_TOKEN);
    private static final IdentityToken expectedIdentityToken = new IdentityTokenImpl(Consts.ID_TOKEN);

    @Before
    public void before(){
        MockitoAnnotations.initMocks(this);
        UserInfoManager userInfoManager = new UserInfoManager(tokenManagerMock);
        when(tokenManagerMock.getLatestAccessToken()).thenReturn(expectedAccessToken);
        userInfoManagerSpy = Mockito.spy(userInfoManager);
        Mockito.doReturn(stubRequest).when(userInfoManagerSpy).createAppIDRequest(anyString(), anyString());
        when(mockContext.getApplicationContext()).thenReturn(mockContext);
        when(mockContext.getPackageManager()).thenReturn(pmMock);
        AppID.getInstance().initialize(mockContext,"00001111-1111-1111-1111-123456789012",".test");
    }

    @Test
    public void getUserInfo_happy_flow() {

        final Response testResponse = new Response() {
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
                return "{\"sub\": \"30\", \"email\":\"test@ibm.com\"}";
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
                return null;
            }
        };

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                ResponseListener responseListener = (ResponseListener) args[0];
                responseListener.onSuccess(testResponse);
                return null;
            }
        }).when(stubRequest).send(any(ResponseListener.class), any(RequestBody.class), eq(expectedAccessToken));

        userInfoManagerSpy.getUserInfo(expectedAccessToken, null, new UserInfoResponseListener() {
            @Override
            public void onSuccess(JSONObject userInfo) {
                try {
                    assertEquals(userInfo.getString("sub"), "30");
                    assertEquals(userInfo.getString("email"), "test@ibm.com");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onFailure(UserInfoException e) {
                fail("should get to onSuccess");
            }
        });

    }

    @Test
    public void getUserInfo_happy_flow_null_response_text() {

        final Response testResponse = new Response() {
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
                return null;
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
                return null;
            }
        };

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                ResponseListener responseListener = (ResponseListener) args[0];
                responseListener.onSuccess(testResponse);
                return null;
            }
        }).when(stubRequest).send(any(ResponseListener.class), any(RequestBody.class), eq(expectedAccessToken));

        userInfoManagerSpy.getUserInfo(expectedAccessToken, null, new UserInfoResponseListener() {
            @Override
            public void onSuccess(JSONObject userInfo) {
                assertEquals(userInfo.toString(), "{}");
            }

            @Override
            public void onFailure(UserInfoException e) {
                fail("should get to onSuccess");
            }
        });

    }

    @Test
    public void getUserInfo_happy_flow_empty_response_text() {

        final Response testResponse = new Response() {
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
                return "";
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
                return null;
            }
        };

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                ResponseListener responseListener = (ResponseListener) args[0];
                responseListener.onSuccess(testResponse);
                return null;
            }
        }).when(stubRequest).send(any(ResponseListener.class), any(RequestBody.class), eq(expectedAccessToken));

        userInfoManagerSpy.getUserInfo(expectedAccessToken, null, new UserInfoResponseListener() {
            @Override
            public void onSuccess(JSONObject userInfo) {
                assertEquals(userInfo.toString(), "{}");
            }

            @Override
            public void onFailure(UserInfoException e) {
                fail("should get to onSuccess");
            }
        });

    }

    @Test
    public void getUserInfo_request_failure_json_format() {

        final Response testResponse = new Response() {
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
                return "56";
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
                return null;
            }
        };

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                ResponseListener responseListener = (ResponseListener) args[0];
                responseListener.onSuccess(testResponse);
                return null;
            }
        }).when(stubRequest).send(any(ResponseListener.class), any(RequestBody.class), eq(expectedAccessToken));

        userInfoManagerSpy.getUserInfo(expectedAccessToken, null, new UserInfoResponseListener() {
            @Override
            public void onSuccess(JSONObject userInfo) {
                fail("should get to onFailure");

            }

            @Override
            public void onFailure(UserInfoException e) {
                assertEquals(e.getError(), UserInfoException.Error.JSON_PARSE_ERROR);
            }
        });
    }

    @Test
    public void getUserInfo_request_failure() {

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                ResponseListener responseListener = (ResponseListener) args[0];
                responseListener.onFailure(null, null, null);
                return null;
            }
        }).when(stubRequest).send(any(ResponseListener.class), any(RequestBody.class), eq(expectedAccessToken));

        userInfoManagerSpy.getUserInfo(expectedAccessToken, null, new UserInfoResponseListener() {
            @Override
            public void onSuccess(JSONObject userInfo) {
                fail("should get to onFailure");

            }

            @Override
            public void onFailure(UserInfoException e) {
                assertEquals(e.getError(), UserInfoException.Error.FAILED_TO_CONNECT);
            }
        });
    }

    @Test
    public void getUserInfo_request_failure_401() {
        final Response testResponse = new Response() {
            @Override
            public String getRequestURL() {
                return null;
            }

            @Override
            public int getStatus() {
                return 401;
            }

            @Override
            public String getResponseText() {
                return null;
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
                return null;
            }
        };
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                ResponseListener responseListener = (ResponseListener) args[0];
                responseListener.onFailure(testResponse, null, null);
                return null;
            }
        }).when(stubRequest).send(any(ResponseListener.class), any(RequestBody.class), eq(expectedAccessToken));

        userInfoManagerSpy.getUserInfo(expectedAccessToken, null, new UserInfoResponseListener() {
            @Override
            public void onSuccess(JSONObject userInfo) {
                fail("should get to onFailure");

            }

            @Override
            public void onFailure(UserInfoException e) {
                assertEquals(e.getError(), UserInfoException.Error.UNAUTHORIZED);
            }
        });
    }

    @Test
    public void getUserInfo_request_failure_404() {
        final Response testResponse = new Response() {
            @Override
            public String getRequestURL() {
                return null;
            }

            @Override
            public int getStatus() {
                return 404;
            }

            @Override
            public String getResponseText() {
                return null;
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
                return null;
            }
        };
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                ResponseListener responseListener = (ResponseListener) args[0];
                responseListener.onFailure(testResponse, null, null);
                return null;
            }
        }).when(stubRequest).send(any(ResponseListener.class), any(RequestBody.class), eq(expectedAccessToken));

        userInfoManagerSpy.getUserInfo(expectedAccessToken, null, new UserInfoResponseListener() {
            @Override
            public void onSuccess(JSONObject userInfo) {
                fail("should get to onFailure");

            }

            @Override
            public void onFailure(UserInfoException e) {
                assertEquals(e.getError(), UserInfoException.Error.NOT_FOUND);
            }
        });
    }

    @Test
    public void getUserInfo_happy_flow_no_token_supplied_validate_idToken() {

        when(tokenManagerMock.getLatestIdentityToken()).thenReturn(expectedIdentityToken);

        final Response testResponse = new Response() {
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
                return "{\"sub\": \"09b7fea5-2e4e-40b8-9d81-df50071a3053\", \"email\":\"test@ibm.com\"}";
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
                return null;
            }
        };

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                ResponseListener responseListener = (ResponseListener) args[0];
                responseListener.onSuccess(testResponse);
                return null;
            }
        }).when(stubRequest).send(any(ResponseListener.class), any(RequestBody.class), eq(expectedAccessToken));

        userInfoManagerSpy.getUserInfo(new UserInfoResponseListener() {
            @Override
            public void onSuccess(JSONObject userInfo) {
                try {
                    assertEquals(userInfo.getInt("sub"), "09b7fea5-2e4e-40b8-9d81-df50071a3053");
                    assertEquals(userInfo.getString("email"), "test@ibm.com");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onFailure(UserInfoException e) {
                fail("should get to onSuccess");
            }
        });
    }

    @Test
    public void getUserInfo_no_token_supplied_mismathed_subjects() {

        when(tokenManagerMock.getLatestIdentityToken()).thenReturn(expectedIdentityToken);

        final Response testResponse = new Response() {
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
                return "{\"sub\": \"wrong sub\", \"email\":\"test@ibm.com\"}";
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
                return null;
            }
        };

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                ResponseListener responseListener = (ResponseListener) args[0];
                responseListener.onSuccess(testResponse);
                return null;
            }
        }).when(stubRequest).send(any(ResponseListener.class), any(RequestBody.class), eq(expectedAccessToken));

        userInfoManagerSpy.getUserInfo(new UserInfoResponseListener() {
            @Override
            public void onSuccess(JSONObject userInfo) {
                fail("should get to onFailure");

            }

            @Override
            public void onFailure(UserInfoException e) {
                assertEquals(e.getError(), UserInfoException.Error.CONFLICTING_SUBJECTS);
            }
        });
    }
}
