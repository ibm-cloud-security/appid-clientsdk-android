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
package com.ibm.bluemix.appid.android.internal.userattributesmanager;

import android.content.Context;
import android.content.pm.PackageManager;

import com.ibm.bluemix.appid.android.api.AppID;
import com.ibm.bluemix.appid.android.api.tokens.AccessToken;
import com.ibm.bluemix.appid.android.api.userattributes.UserAttributeResponseListener;
import com.ibm.bluemix.appid.android.api.userattributes.UserAttributesException;
import com.ibm.bluemix.appid.android.internal.network.AppIDRequest;
import com.ibm.bluemix.appid.android.internal.tokenmanager.TokenManager;
import com.ibm.bluemix.appid.android.internal.tokens.AccessTokenImpl;
import com.ibm.bluemix.appid.android.testing.helpers.Consts;
import com.ibm.mobilefirstplatform.clientsdk.android.core.api.Response;
import com.ibm.mobilefirstplatform.clientsdk.android.core.api.ResponseListener;
import com.squareup.okhttp.RequestBody;

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
public class UserAttributeManagerImpl_Test {

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

    private UserAttributeManagerImpl userAttributeManagerSpy;
    private static final AccessToken expectedAccessToken = new AccessTokenImpl(Consts.ACCESS_TOKEN);

    @Before
    public void before(){
        MockitoAnnotations.initMocks(this);
        UserAttributeManagerImpl userAttributeManager = new UserAttributeManagerImpl(tokenManagerMock);
        when(tokenManagerMock.getLatestAccessToken()).thenReturn(expectedAccessToken);
        userAttributeManagerSpy = Mockito.spy(userAttributeManager);
        Mockito.doReturn(stubRequest).when(userAttributeManagerSpy).createAppIDRequest(anyString(), anyString());
        when(mockContext.getApplicationContext()).thenReturn(mockContext);
        when(mockContext.getPackageManager()).thenReturn(pmMock);
        AppID.getInstance().initialize(mockContext,"00001111-1111-1111-1111-123456789012",".test");
    }

    @Test
    public void getAllAttributes_happy_flow() {

        final Response testReponse = new Response() {
            @Override
            public int getStatus() {
                return 200;
            }

            @Override
            public String getResponseText() {
                return "{\"age\": 30, \"email\":\"test@ibm.com\"}";
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
                ResponseListener responseListener = (ResponseListener) args[0];
                responseListener.onSuccess(testReponse);
                return null;
            }
        }).when(stubRequest).send(any(ResponseListener.class), any(RequestBody.class), eq(expectedAccessToken));

        userAttributeManagerSpy.getAllAttributes(expectedAccessToken, new UserAttributeResponseListener() {
            @Override
            public void onSuccess(JSONObject attributes) {
                try {
                    assertEquals(attributes.getInt("age"), 30);
                    assertEquals(attributes.getString("email"), "test@ibm.com");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onFailure(UserAttributesException e) {
                fail("should get to onSuccess");
            }
        });

    }

    @Test
    public void getAllAttributes_happy_flow_null_response_text() {

        final Response testReponse = new Response() {
            @Override
            public int getStatus() {
                return 200;
            }

            @Override
            public String getResponseText() {
                return null;
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
                ResponseListener responseListener = (ResponseListener) args[0];
                responseListener.onSuccess(testReponse);
                return null;
            }
        }).when(stubRequest).send(any(ResponseListener.class), any(RequestBody.class), eq(expectedAccessToken));

        userAttributeManagerSpy.getAllAttributes(expectedAccessToken, new UserAttributeResponseListener() {
            @Override
            public void onSuccess(JSONObject attributes) {
                assertEquals(attributes.toString(), "{}");
            }

            @Override
            public void onFailure(UserAttributesException e) {
                fail("should get to onSuccess");
            }
        });

    }

    @Test
    public void getAllAttributes_happy_flow_empty_response_text() {

        final Response testReponse = new Response() {
            @Override
            public int getStatus() {
                return 200;
            }

            @Override
            public String getResponseText() {
                return "";
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
                ResponseListener responseListener = (ResponseListener) args[0];
                responseListener.onSuccess(testReponse);
                return null;
            }
        }).when(stubRequest).send(any(ResponseListener.class), any(RequestBody.class), eq(expectedAccessToken));

        userAttributeManagerSpy.getAllAttributes(expectedAccessToken, new UserAttributeResponseListener() {
            @Override
            public void onSuccess(JSONObject attributes) {
                assertEquals(attributes.toString(), "{}");
            }

            @Override
            public void onFailure(UserAttributesException e) {
                fail("should get to onSuccess");
            }
        });

    }

    @Test
    public void getAllAttributes_request_failure_json_format() {

        final Response testReponse = new Response() {
            @Override
            public int getStatus() {
                return 200;
            }

            @Override
            public String getResponseText() {
                return "56";
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
                ResponseListener responseListener = (ResponseListener) args[0];
                responseListener.onSuccess(testReponse);
                return null;
            }
        }).when(stubRequest).send(any(ResponseListener.class), any(RequestBody.class), eq(expectedAccessToken));

        userAttributeManagerSpy.getAllAttributes(expectedAccessToken, new UserAttributeResponseListener() {
            @Override
            public void onSuccess(JSONObject attributes) {
                fail("should get to onFailure");

            }

            @Override
            public void onFailure(UserAttributesException e) {
                assertEquals(e.getError(), UserAttributesException.Error.JSON_PARSE_ERROR);
            }
        });
    }

    @Test
    public void getAllAttributes_request_failure() {

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                ResponseListener responseListener = (ResponseListener) args[0];
                responseListener.onFailure(null, null, null);
                return null;
            }
        }).when(stubRequest).send(any(ResponseListener.class), any(RequestBody.class), eq(expectedAccessToken));

        userAttributeManagerSpy.getAllAttributes(expectedAccessToken, new UserAttributeResponseListener() {
            @Override
            public void onSuccess(JSONObject attributes) {
                fail("should get to onFailure");

            }

            @Override
            public void onFailure(UserAttributesException e) {
                assertEquals(e.getError(), UserAttributesException.Error.FAILED_TO_CONNECT);
            }
        });
    }

    @Test
    public void getAllAttributes_request_failure_401() {
        final Response testReponse = new Response() {
            @Override
            public int getStatus() {
                return 401;
            }

            @Override
            public String getResponseText() {
                return null;
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
                ResponseListener responseListener = (ResponseListener) args[0];
                responseListener.onFailure(testReponse, null, null);
                return null;
            }
        }).when(stubRequest).send(any(ResponseListener.class), any(RequestBody.class), eq(expectedAccessToken));

        userAttributeManagerSpy.getAllAttributes(expectedAccessToken, new UserAttributeResponseListener() {
            @Override
            public void onSuccess(JSONObject attributes) {
                fail("should get to onFailure");

            }

            @Override
            public void onFailure(UserAttributesException e) {
                assertEquals(e.getError(), UserAttributesException.Error.UNAUTHORIZED);
            }
        });
    }

    @Test
    public void getAllAttributes_request_failure_404() {
        final Response testReponse = new Response() {
            @Override
            public int getStatus() {
                return 404;
            }

            @Override
            public String getResponseText() {
                return null;
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
                ResponseListener responseListener = (ResponseListener) args[0];
                responseListener.onFailure(testReponse, null, null);
                return null;
            }
        }).when(stubRequest).send(any(ResponseListener.class), any(RequestBody.class), eq(expectedAccessToken));

        userAttributeManagerSpy.getAllAttributes(expectedAccessToken, new UserAttributeResponseListener() {
            @Override
            public void onSuccess(JSONObject attributes) {
                fail("should get to onFailure");

            }

            @Override
            public void onFailure(UserAttributesException e) {
                assertEquals(e.getError(), UserAttributesException.Error.NOT_FOUND);
            }
        });
    }

    @Test
    public void getAllAttributes_happy_flow_no_token_supply() {

        final Response testReponse = new Response() {
            @Override
            public int getStatus() {
                return 200;
            }

            @Override
            public String getResponseText() {
                return "{\"age\": 30, \"email\":\"test@ibm.com\"}";
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
                ResponseListener responseListener = (ResponseListener) args[0];
                responseListener.onSuccess(testReponse);
                return null;
            }
        }).when(stubRequest).send(any(ResponseListener.class), any(RequestBody.class), eq(expectedAccessToken));

        userAttributeManagerSpy.getAllAttributes(new UserAttributeResponseListener() {
            @Override
            public void onSuccess(JSONObject attributes) {
                try {
                    assertEquals(attributes.getInt("age"), 30);
                    assertEquals(attributes.getString("email"), "test@ibm.com");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onFailure(UserAttributesException e) {
                fail("should get to onSuccess");
            }
        });

    }

    @Test
    public void setAttribute_no_token_supply() {

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                ResponseListener responseListener = (ResponseListener) args[0];
                responseListener.onFailure(null, null, null);
                return null;
            }
        }).when(stubRequest).send(any(ResponseListener.class), any(RequestBody.class), eq(expectedAccessToken));

        final Response testReponse = new Response() {
            @Override
            public int getStatus() {
                return 200;
            }

            @Override
            public String getResponseText() {
                return "{\"name\": \"testName\", \"value\":\"testValue\"}";
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

        Mockito.doReturn(specificStubRequest).when(userAttributeManagerSpy).createAppIDRequest(eq("https://appid-profiles.test/api/v1/attributes/testName"), eq(AppIDRequest.PUT));
        Mockito.doReturn(stubRequestBody).when(userAttributeManagerSpy).createRequestBody(eq("testValue"));

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                ResponseListener responseListener = (ResponseListener) args[0];
                if ((RequestBody)args[1] != stubRequestBody){
                    responseListener.onFailure(null,null,null);
                }
                responseListener.onSuccess(testReponse);
                return null;
            }
        }).when(specificStubRequest).send(any(ResponseListener.class), any(RequestBody.class), eq(expectedAccessToken));



        userAttributeManagerSpy.setAttribute("testName","testValue",new UserAttributeResponseListener() {
            @Override
            public void onSuccess(JSONObject attributes) {
                try {
                    assertEquals(attributes.getString("name"), "testName");
                    assertEquals(attributes.getString("value"), "testValue");
                } catch (JSONException e) {
                    fail(e.getMessage());
                    e.printStackTrace();
                }

            }

            @Override
            public void onFailure(UserAttributesException e) {
                fail("should get to onSuccess");
            }
        });

    }

    @Test
    public void getAttribute_no_token_supply() {

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                ResponseListener responseListener = (ResponseListener) args[0];
                responseListener.onFailure(null, null, null);
                return null;
            }
        }).when(stubRequest).send(any(ResponseListener.class), any(RequestBody.class), eq(expectedAccessToken));

        final Response testReponse = new Response() {
            @Override
            public int getStatus() {
                return 200;
            }

            @Override
            public String getResponseText() {
                return "{\"name\": \"testName\", \"value\":\"testValue\"}";
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

        Mockito.doReturn(specificStubRequest).when(userAttributeManagerSpy).createAppIDRequest(eq("https://appid-profiles.test/api/v1/attributes/testName"), eq(AppIDRequest.GET));
        Mockito.doReturn(stubRequestBody).when(userAttributeManagerSpy).createRequestBody((String) eq(null));

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                ResponseListener responseListener = (ResponseListener) args[0];
                if ((RequestBody)args[1] != null) {
                    responseListener.onFailure(null,null,null);
                }
                responseListener.onSuccess(testReponse);
                return null;
            }
        }).when(specificStubRequest).send(any(ResponseListener.class), any(RequestBody.class), eq(expectedAccessToken));


        userAttributeManagerSpy.getAttribute("testName",new UserAttributeResponseListener() {
            @Override
            public void onSuccess(JSONObject attributes) {
                try {
                    assertEquals(attributes.getString("name"), "testName");
                    assertEquals(attributes.getString("value"), "testValue");
                } catch (JSONException e) {
                    fail(e.getMessage());
                    e.printStackTrace();
                }

            }

            @Override
            public void onFailure(UserAttributesException e) {
                fail("should get to onSuccess");
            }
        });

    }

    @Test
    public void deleteAttribute_no_token_supply() {

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                ResponseListener responseListener = (ResponseListener) args[0];
                responseListener.onFailure(null, null, null);
                return null;
            }
        }).when(stubRequest).send(any(ResponseListener.class), any(RequestBody.class), eq(expectedAccessToken));

        final Response testReponse = new Response() {
            @Override
            public int getStatus() {
                return 200;
            }

            @Override
            public String getResponseText() {
                return "{\"name\": \"testName\", \"value\":\"testValue\"}";
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

        Mockito.doReturn(specificStubRequest).when(userAttributeManagerSpy).createAppIDRequest(eq("https://appid-profiles.test/api/v1/attributes/testName"), eq(AppIDRequest.DELETE));
        Mockito.doReturn(stubRequestBody).when(userAttributeManagerSpy).createRequestBody((String) eq(null));

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                ResponseListener responseListener = (ResponseListener) args[0];
                if ((RequestBody)args[1] != null) {
                    responseListener.onFailure(null,null,null);
                }
                responseListener.onSuccess(testReponse);
                return null;
            }
        }).when(specificStubRequest).send(any(ResponseListener.class), any(RequestBody.class), eq(expectedAccessToken));


        userAttributeManagerSpy.deleteAttribute("testName",new UserAttributeResponseListener() {
            @Override
            public void onSuccess(JSONObject attributes) {
                try {
                    assertEquals(attributes.getString("name"), "testName");
                    assertEquals(attributes.getString("value"), "testValue");
                } catch (JSONException e) {
                    fail(e.getMessage());
                    e.printStackTrace();
                }

            }

            @Override
            public void onFailure(UserAttributesException e) {
                fail("should get to onSuccess");
            }
        });

    }
}
