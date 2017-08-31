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

package com.ibm.bluemix.appid.android.internal.network;

import com.ibm.bluemix.appid.android.api.tokens.AccessToken;
import com.ibm.bluemix.appid.android.api.tokens.IdentityToken;
import com.ibm.mobilefirstplatform.clientsdk.android.core.api.ResponseListener;
import com.ibm.mobilefirstplatform.clientsdk.android.core.internal.BaseRequest;
import com.ibm.mobilefirstplatform.clientsdk.android.core.internal.TLSEnabledSSLSocketFactory;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.RequestBody;

import org.json.JSONObject;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;

import javax.net.ssl.SSLSocketFactory;

public class AppIDRequest extends BaseRequest {
	private static OkHttpClient httpClient = new OkHttpClient();

	static {
		SSLSocketFactory tlsEnabledSSLSocketFactory;
		try {
			tlsEnabledSSLSocketFactory = new TLSEnabledSSLSocketFactory();
			httpClient.setSslSocketFactory(tlsEnabledSSLSocketFactory);
		} catch (KeyManagementException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Constructs the authorization request
	 *
	 * @param url    request url
	 * @param method request method
	 */
	public AppIDRequest (String url, String method) {
		super(url, method);

		// we want to handle redirects in-place
		httpClient.setFollowRedirects(false);
	}

	public void send(final ResponseListener listener, final AccessToken accessToken, final IdentityToken identityToken) {
        if (accessToken != null && identityToken != null) {
            removeHeaders("Authorization");
            addHeader("Authorization", "Bearer " + accessToken.getRaw() + " " + identityToken.getRaw());
        }
        send(listener);
	}

	public void send(final ResponseListener listener, final RequestBody requestBody, final AccessToken accessToken) {

		if (accessToken != null) {
			removeHeaders("Authorization");
			addHeader("Authorization", "Bearer " + accessToken.getRaw());
		}

		if (requestBody != null) {
			super.sendRequest(listener, requestBody);
		} else {
			send(listener);
		}
	}

	@Override
	public void send (JSONObject json, ResponseListener listener) {
		super.send(json, listener);
	}

	@Override
	public void send (Map<String, String> formParameters, ResponseListener listener) {
		super.send(formParameters, listener);
	}

    @Override
    public void send(final ResponseListener listener) {
        super.send(listener);
    }


}
