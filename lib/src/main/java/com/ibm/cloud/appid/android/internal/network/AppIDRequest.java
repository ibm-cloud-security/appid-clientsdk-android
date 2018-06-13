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
import com.ibm.cloud.appid.android.api.tokens.IdentityToken;
import com.ibm.mobilefirstplatform.clientsdk.android.core.api.ResponseListener;
import com.ibm.mobilefirstplatform.clientsdk.android.core.internal.BaseRequest;
import com.ibm.mobilefirstplatform.clientsdk.android.core.internal.TLSEnabledSSLSocketFactory;
import com.ibm.mobilefirstplatform.clientsdk.android.logger.api.Logger;

import okhttp3.RequestBody;
import okhttp3.OkHttpClient;

import org.json.JSONObject;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.Map;

import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;
import javax.net.ssl.TrustManager;

public class AppIDRequest extends BaseRequest {
	private static Logger logger = Logger.getLogger(Logger.INTERNAL_PREFIX + BaseRequest.class.getSimpleName());
	private static  OkHttpClient.Builder httpClient = new OkHttpClient.Builder();

	static {
		SSLSocketFactory tlsEnabledSSLSocketFactory;
		try {
			final TrustManager[] trustAllCerts = new TrustManager[]{
					new X509TrustManager() {
						@Override
						public void checkClientTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
							logger.info("AppIDRequest checkClientTrusted method : " + authType);
						}

						@Override
						public void checkServerTrusted(java.security.cert.X509Certificate[] chain, String authType) throws CertificateException {
							logger.info("AppIDRequest checkServerTrusted method : " + authType);
						}

						@Override
						public java.security.cert.X509Certificate[] getAcceptedIssuers() {
							return new java.security.cert.X509Certificate[]{};
						}
					}
			};
			tlsEnabledSSLSocketFactory = new TLSEnabledSSLSocketFactory();
			httpClient.sslSocketFactory(tlsEnabledSSLSocketFactory, (X509TrustManager) trustAllCerts[0]);
		} catch (KeyManagementException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (RuntimeException e) {
			logger.error("AppIDRequest RuntimeException : " + e.getLocalizedMessage());
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
		httpClient.followSslRedirects(false);
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
			super.sendRequest(null, listener, requestBody);
		} else {
			send(listener);
		}
	}

    public void send(final ResponseListener listener, final RequestBody requestBody, final AccessToken accessToken, final IdentityToken identityToken) {

        if (accessToken != null && identityToken != null) {
            removeHeaders("Authorization");
            addHeader("Authorization", "Bearer " + accessToken.getRaw() + " " + identityToken.getRaw());
        }

        if (requestBody != null) {
            super.sendRequest(null, listener, requestBody);
        } else {
            send(listener);
        }
    }

	@Override
	public void send (JSONObject json, ResponseListener listener) {
		super.send(json, listener);
	}

    public void send (JSONObject json, ResponseListener listener, final AccessToken accessToken, final IdentityToken identityToken) {
        if (accessToken != null && identityToken != null) {
            removeHeaders("Authorization");
            addHeader("Authorization", "Bearer " + accessToken.getRaw() + " " + identityToken.getRaw());
        }
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
