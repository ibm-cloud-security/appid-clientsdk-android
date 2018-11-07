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
package com.ibm.cloud.appid.android.internal.network;

import com.ibm.cloud.appid.android.api.tokens.AccessToken;
import com.ibm.cloud.appid.android.internal.tokens.AccessTokenImpl;
import com.ibm.cloud.appid.android.testing.helpers.Consts;
import com.ibm.mobilefirstplatform.clientsdk.android.core.api.Response;
import com.ibm.mobilefirstplatform.clientsdk.android.core.api.ResponseListener;

import org.json.JSONObject;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.mockito.Mockito;
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

@RunWith(RobolectricTestRunner.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class AppIDRequest_Test {

    AppIDRequestFactory appIDRequestFactory = new AppIDRequestFactory();
    AppIDRequest appIDRequest = appIDRequestFactory.createRequest("testUrl", "testMethod");
    AppIDRequest spyAppIDRequest = Mockito.spy(appIDRequest);
    private static final AccessToken expectedAccessToken = new AccessTokenImpl(Consts.ACCESS_TOKEN);

    @Test
    public void send_test(){

        Mockito.doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                String header = (String) args[0];
                if (!header.equals("Authorization")) {
                    fail("header is not Authorization");
                }
                return null;
            }
        }).when(spyAppIDRequest).removeHeaders(anyString());

        Mockito.doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                String header = (String) args[0];
                String bearerPlusToken = (String) args[1];
                if (!header.equals("Authorization")) {
                    fail("header is not Authorization");
                }
                if (!bearerPlusToken.equals("Bearer " + expectedAccessToken.getRaw())) {
                    fail("addHeader params mismatch");
                }
                return null;
            }
        }).when(spyAppIDRequest).addHeader(anyString(),anyString());


        final Response testReponse = new Response() {
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

        Mockito.doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                Object[] args = invocation.getArguments();
                ResponseListener responseListener = (ResponseListener) args[0];
                responseListener.onSuccess(testReponse);
                return null;
            }
        }).when(spyAppIDRequest).send(any(ResponseListener.class));


        spyAppIDRequest.send(new ResponseListener() {
            @Override
            public void onSuccess(Response response) {
                assertEquals(response.getStatus(), 200);
            }

            @Override
            public void onFailure(Response response, Throwable t, JSONObject extendedInfo) {
                fail("should get to onSuccess");
            }
        }, null, expectedAccessToken);
    }
}
