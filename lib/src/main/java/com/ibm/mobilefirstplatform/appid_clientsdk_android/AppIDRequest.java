package com.ibm.mobilefirstplatform.appid_clientsdk_android;

import com.ibm.mobilefirstplatform.clientsdk.android.core.api.ResponseListener;
import com.ibm.mobilefirstplatform.clientsdk.android.security.mca.internal.AuthorizationRequest;

import org.json.JSONObject;

import java.net.MalformedURLException;

/**
 * Created by odedb on 19/12/2016.
 */

public class AppIDRequest extends AuthorizationRequest{

    /**
     * Constructs the authorization request
     *
     * @param url    request url
     * @param method request method
     * @throws MalformedURLException if url is not valid
     */
    public AppIDRequest(String url, String method) throws MalformedURLException {
        super(url, method);
    }

    public void send(JSONObject json, ResponseListener listener) {
        super.send(json, listener);
    }
}
