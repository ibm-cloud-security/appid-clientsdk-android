package com.ibm.bluemix.appid.android.testing.mocks;

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
