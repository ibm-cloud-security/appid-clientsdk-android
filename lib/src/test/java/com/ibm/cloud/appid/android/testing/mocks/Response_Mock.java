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

package com.ibm.cloud.appid.android.testing.mocks;

import com.ibm.mobilefirstplatform.clientsdk.android.core.api.Response;

import org.json.JSONObject;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

public class Response_Mock implements Response {
    private int statusCode;
    private String responseText;

    public Response_Mock(String resposneText, int statusCode) {
        this.responseText = resposneText;
        this.statusCode = statusCode;
    }

    public String getRequestURL() {
        return null;
    }

    public int getStatus() {
        return statusCode;
    }

    public String getResponseText() {
        return responseText;
    }

    public JSONObject getResponseJSON() {
        return null;
    }

    public byte[] getResponseBytes() {
        return new byte[0];
    }

    public InputStream getResponseByteStream() {
        return null;
    }

    public long getContentLength() {
        return 0;
    }

    public Map<String, List<String>> getHeaders() {
        return null;
    }
}
