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

package com.ibm.bluemix.appid.android.internal.tokenmanager;

import android.util.Base64;

import com.ibm.bluemix.appid.android.api.AppID;
import com.ibm.bluemix.appid.android.api.AuthorizationException;
import com.ibm.bluemix.appid.android.api.AuthorizationListener;
import com.ibm.bluemix.appid.android.api.TokenResponseListener;
import com.ibm.bluemix.appid.android.api.tokens.AccessToken;
import com.ibm.bluemix.appid.android.api.tokens.IdentityToken;
import com.ibm.bluemix.appid.android.api.tokens.RefreshToken;
import com.ibm.bluemix.appid.android.internal.OAuthManager;
import com.ibm.bluemix.appid.android.internal.config.Config;
import com.ibm.bluemix.appid.android.internal.network.AppIDRequest;
import com.ibm.bluemix.appid.android.internal.registrationmanager.RegistrationManager;
import com.ibm.bluemix.appid.android.internal.tokens.AccessTokenImpl;
import com.ibm.bluemix.appid.android.internal.tokens.IdentityTokenImpl;
import com.ibm.bluemix.appid.android.internal.tokens.RefreshTokenImpl;
import com.ibm.mobilefirstplatform.clientsdk.android.core.api.Response;
import com.ibm.mobilefirstplatform.clientsdk.android.core.api.ResponseListener;
import com.ibm.mobilefirstplatform.clientsdk.android.logger.api.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.util.HashMap;
import java.util.Map;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.IncorrectClaimException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureException;

public class TokenManager {

	private final AppID appId;
	private final RegistrationManager registrationManager;
	private AccessToken latestAccessToken;
	private IdentityToken latestIdentityToken;
	private RefreshToken latestRefreshToken;
	private Map<String,RSAPublicKey> publicKeys;
	private static final Logger logger = Logger.getLogger(Logger.INTERNAL_PREFIX + TokenManager.class.getName());
	private static final String OAUTH_TOKEN_PATH = "/token";
	private final static String CLIENT_ID = "client_id";
	private final static String GRANT_TYPE = "grant_type";
	private final static String GRANT_TYPE_AUTH_CODE = "authorization_code";
	private final static String CODE = "code";
	private final static String REDIRECT_URI = "redirect_uri";
	private final static String AUTHORIZATION_HEADER = "Authorization";
	private final static String USERNAME = "username";
	private final static String PASSWORD = "password";
	private final static String GRANT_TYPE_PASSWORD = "password";
	private static final String ACCESS_TOKEN = "access_token";
	private static final String ID_TOKEN = "id_token";
	private static final String REFRESH_TOKEN = "refresh_token";
	private static final String GRANT_TYPE_REFRESH = "refresh_token";
	private final static String APPID_ACCESS_TOKEN = "appid_access_token";
	private final static String ERROR_DESCRIPTION= "error_description";
	private final static String ERROR= "error";
	private final static String INVALID_GRANT= "invalid_grant";

	public TokenManager (OAuthManager oAuthManager) {
		this.appId = oAuthManager.getAppId();
		this.registrationManager = oAuthManager.getRegistrationManager();
		this.publicKeys = new HashMap<>();
	}

	public void obtainTokensAuthCode(String code, final AuthorizationListener listener) {
		logger.debug("obtainTokensAuthCode");
		String clientId = registrationManager.getRegistrationDataString(RegistrationManager.CLIENT_ID);
		String redirectUri = registrationManager.getRegistrationDataString(RegistrationManager.REDIRECT_URIS, 0);

		HashMap<String, String> formParams = new HashMap<>();
		formParams.put(CODE, code);
		formParams.put(CLIENT_ID, clientId);
		formParams.put(GRANT_TYPE, GRANT_TYPE_AUTH_CODE);
		formParams.put(REDIRECT_URI, redirectUri);

		retrieveTokens(formParams, listener);
	}

	//for testing purpose
	AppIDRequest createAppIDRequest(String url, String method) {
		return new AppIDRequest(url, method);
	}

	private void retrieveTokens(HashMap<String, String> formParams, final TokenResponseListener listener) {
		String tokenUrl = Config.getOAuthServerUrl(appId) + OAUTH_TOKEN_PATH;
		String clientId = registrationManager.getRegistrationDataString(RegistrationManager.CLIENT_ID);

		AppIDRequest request = createAppIDRequest(tokenUrl, "POST");

		try {
			request.addHeader(AUTHORIZATION_HEADER, createAuthenticationHeader(clientId));
		} catch (Exception e) {
			logger.error("Failed to create authentication header", e);
			return;
		}

		request.send(formParams, new ResponseListener() {
			@Override
			public void onFailure (Response response, Throwable t, JSONObject extendedInfo) {
				logger.error("Failed to retrieve tokens from authorization server", t);

				try {
					if (response.getStatus() == 400) {
						JSONObject responseJSON = new JSONObject(response.getResponseText());
						if (INVALID_GRANT.equals(responseJSON.getString(ERROR))) {
							listener.onAuthorizationFailure(new AuthorizationException(responseJSON.getString(ERROR_DESCRIPTION)));
							return;
						}
					} else if (response.getStatus() == 403) {
						JSONObject responseJSON = new JSONObject(response.getResponseText());
						listener.onAuthorizationFailure(new AuthorizationException(responseJSON.getString(ERROR_DESCRIPTION)));
						return;
					}

					listener.onAuthorizationFailure(new AuthorizationException("Failed to retrieve tokens"));
				} catch (Exception e) {
					logger.error("Failed to retrieve tokens from authorization server", t);
					listener.onAuthorizationFailure(new AuthorizationException("Failed to retrieve tokens"));
				}
			}

			@Override
			public void onSuccess (Response response) {
				extractTokens(response, listener);
			}
		});
	}

	public void obtainTokensRoP(String username, String password, String accessTokenString, final TokenResponseListener listener) {
		logger.debug("obtainTokensRoP");

		HashMap<String, String> formParams = new HashMap<>();
		formParams.put(USERNAME, username);
		formParams.put(PASSWORD, password);
		formParams.put(GRANT_TYPE, GRANT_TYPE_PASSWORD);
		if (accessTokenString != null) {
			formParams.put(APPID_ACCESS_TOKEN, accessTokenString);
		}
		retrieveTokens(formParams, listener);
	}

	public void obtainTokensRefreshToken(String refreshTokenString, final TokenResponseListener listener) {
		logger.debug("obtainTokensRefreshToken");
		if (refreshTokenString == null) {
			listener.onAuthorizationFailure(new AuthorizationException("Missing refresh-token"));
		}
		HashMap<String, String> formParams = new HashMap<>();
		formParams.put(REFRESH_TOKEN, refreshTokenString);
		formParams.put(GRANT_TYPE, GRANT_TYPE_REFRESH);
		retrieveTokens(formParams, listener);
	}

	private String createAuthenticationHeader (String clientId) throws Exception {
		PrivateKey privateKey = registrationManager.getPrivateKey();
		Signature signature = Signature.getInstance("SHA256withRSA");
		signature.initSign(privateKey);
		signature.update(clientId.getBytes());
		String encodedClientId = Base64.encodeToString(signature.sign(), Base64.NO_WRAP);
		return "Basic " + Base64.encodeToString((clientId + ":" + encodedClientId).getBytes(), Base64.NO_WRAP);
	}

	/**
	 * Extract token from response and save it locally
	 *
	 * @param response response that contain the token
	 */
	protected void extractTokens (Response response, TokenResponseListener tokenResponseListener) {
		logger.debug("Extracting tokens from server response");

		JSONObject responseJSON;
		try {
			responseJSON = new JSONObject(response.getResponseText());
		} catch (Exception e){
			logger.error("Failed to parse server response", e);
			tokenResponseListener.onAuthorizationFailure(new AuthorizationException("Failed to parse server response"));
			return;
		}
		extractTokens(responseJSON,tokenResponseListener,0);
	}

	protected void extractTokens(JSONObject responseJSON, TokenResponseListener listener,int flowStep) {
		if (flowStep==0){
			logger.debug("Extracting and verifying access token from server response");
			String accessTokenString;
			AccessToken accessToken;
			try {
				accessTokenString = responseJSON.getString(ACCESS_TOKEN);
				accessToken = new AccessTokenImpl(accessTokenString);
				String kid=accessToken.getHeader().getString("kid");
				RSAPublicKey accessTokenPublicKey = getPublicKeyByKid(kid);
				flowStep++;
				if(accessTokenPublicKey == null){
					lookUpPublicKey(kid,responseJSON,listener,ACCESS_TOKEN,flowStep);
				}else {
					saveToken(kid,responseJSON,accessTokenPublicKey,listener,ACCESS_TOKEN,flowStep);
				}
			} catch (RuntimeException|AuthorizationException|JSONException e){
				logger.error("Failed to parse access_token", e);
				listener.onAuthorizationFailure(new AuthorizationException("Failed to parse access_token, error :"+e.getMessage()));
			}
		}else if (flowStep==1){
			logger.debug("Extracting and verifying id token from server response");
			String identityTokenString;
			IdentityToken identityToken;
			try {
				identityTokenString = responseJSON.getString(ID_TOKEN);
				identityToken = new IdentityTokenImpl(identityTokenString);
				String kid=identityToken.getHeader().getString("kid");
				RSAPublicKey identityTokenPublicKey = getPublicKeyByKid(kid);
				flowStep++;
				if(identityTokenPublicKey == null){
					lookUpPublicKey(kid,responseJSON,listener,ID_TOKEN,flowStep);
				}else {
					saveToken(kid,responseJSON,identityTokenPublicKey,listener,ID_TOKEN,flowStep);
				}
			} catch (RuntimeException|AuthorizationException|JSONException e){
				logger.error("Failed to parse id_token", e);
				listener.onAuthorizationFailure(new AuthorizationException("Failed to parse id_token, error :"+e.getMessage()));
			}
		}else {
			RefreshToken refreshToken = null;
			try {
				logger.debug("responseJSON.has");
				logger.debug(""+responseJSON.has(REFRESH_TOKEN));
				if(responseJSON.has(REFRESH_TOKEN)){
					String refreshTokenString = responseJSON.getString(REFRESH_TOKEN);
					refreshToken = new RefreshTokenImpl(refreshTokenString);
				}
			} catch (RuntimeException|JSONException e){
				logger.error("Failed to parse refresh_token", e);
			}
			latestRefreshToken = refreshToken;
			listener.onAuthorizationSuccess(latestAccessToken,latestIdentityToken,latestRefreshToken);
		}
	}

	protected void lookUpPublicKey(final String tokenKid, final JSONObject responseJSON, final TokenResponseListener listener, final String tokenType, final int flowStep){
		AppIDRequest request = new AppIDRequest(Config.getPublicKeysEndpoint(appId),AppIDRequest.GET);
		request.send(new ResponseListener() {
			@Override
			public void onSuccess(Response response) {
				try {
					RSAPublicKey publicKey = getPublickey(response,tokenKid);
					if(publicKey==null){
						listener.onAuthorizationFailure(new AuthorizationException("Failed to retrieve public keys for Kid from server"));
					}else{
						saveToken(tokenKid,responseJSON,publicKey,listener,tokenType,flowStep);
					}
				} catch (AuthorizationException exception) {
					logger.error("Failed to parse public keys from server");
					listener.onAuthorizationFailure(exception);
				}
			}

			@Override
			public void onFailure(Response response, Throwable t, JSONObject extendedInfo) {
				logger.error("Failed to retrieve public keys from server", t);
				listener.onAuthorizationFailure(new AuthorizationException("Failed to retrieve public keys from server"));
			}
		});
	}

	protected void saveToken(String tokenKid, JSONObject responseJSON, RSAPublicKey publicKey, TokenResponseListener listener, String tokenType, int flowStep) {
		String clientId = registrationManager.getRegistrationDataString(RegistrationManager.CLIENT_ID);
		try {
			String tokenString = responseJSON.getString(tokenType);
			Boolean verify = verifyToken(publicKey,tokenString,Config.getIssuer(appId),clientId,appId.getTenantId());
			if(verify){
				if(tokenType.equals(ACCESS_TOKEN)){
					AccessToken accessToken = new AccessTokenImpl(tokenString);
					latestAccessToken = accessToken;
				}else if(tokenType.equals(ID_TOKEN)){
					IdentityToken identityToken = new IdentityTokenImpl(tokenString);
					latestIdentityToken= identityToken;
				}
				extractTokens(responseJSON,listener,flowStep);
			}else{
				listener.onAuthorizationFailure(new AuthorizationException("Failed to verify "+tokenType));
			}
		} catch (Exception exception) {
			clearStoredTokens();
			listener.onAuthorizationFailure(new AuthorizationException("Failed to parse "+ tokenType + ",error : "+ exception.getMessage()));
		}
	}

	protected RSAPublicKey getPublicKeyByKid(String tokenKid) throws AuthorizationException {
		if(tokenKid == null || tokenKid.equals("")){
			throw new AuthorizationException("Invalid Kid");
		}
		if(publicKeys.containsKey(tokenKid)){
			return publicKeys.get(tokenKid);
		}
		return null;
	}

	protected RSAPublicKey getPublickey(Response response,String tokenKid) throws AuthorizationException {
		JSONObject responseJSON;
		try {
			responseJSON = new JSONObject(response.getResponseText());
			JSONArray jwkArray = responseJSON.getJSONArray("keys");
			for(int i = 0; i < jwkArray.length(); i++)
			{
				JSONObject jwkJSONObject = jwkArray.getJSONObject(i);
				BigInteger modulus = new BigInteger(1, Base64.decode(jwkJSONObject.getString("n"),Base64.URL_SAFE));
				BigInteger exponent = new BigInteger(1, Base64.decode(jwkJSONObject.getString("e"),Base64.URL_SAFE));
				RSAPublicKey publicKey = (RSAPublicKey) KeyFactory.getInstance("RSA").generatePublic(new RSAPublicKeySpec(modulus, exponent));
				publicKeys.put(jwkJSONObject.getString("kid"),publicKey);
			}
		} catch (Exception e){
			logger.error("Failed to parse server response", e);
			throw new AuthorizationException("Failed to parse public keys from server");
		}
		return publicKeys.get(tokenKid);
	}

	protected boolean verifyToken(RSAPublicKey rsaPublicKey,String token,String issuer,String audience,String tenant) throws SignatureException,IncorrectClaimException {
		if(rsaPublicKey==null){
			return false;
		}
		try {
			Jwts.parser().requireIssuer(issuer).requireAudience(audience)
					.require("tenant", tenant).setSigningKey(rsaPublicKey)
					.parseClaimsJws(token).getBody();
			return true;
		} catch (SignatureException|IncorrectClaimException exception) { // Invalid signature/claims
			throw exception;
		}
	}

	public AccessToken getLatestAccessToken () {
		return latestAccessToken;
	}

	public IdentityToken getLatestIdentityToken () {
		return latestIdentityToken;
	}

	public RefreshToken getLatestRefreshToken() {
		return latestRefreshToken;
	}

	public void clearStoredTokens(){
		latestAccessToken = null;
		latestIdentityToken = null;
		latestRefreshToken = null;
	}
}
