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

package com.ibm.cloud.appid.android.internal.tokenmanager;

import android.util.Base64;

import com.ibm.cloud.appid.android.api.AppID;
import com.ibm.cloud.appid.android.api.AuthorizationException;
import com.ibm.cloud.appid.android.api.AuthorizationListener;
import com.ibm.cloud.appid.android.api.TokenResponseListener;
import com.ibm.cloud.appid.android.api.tokens.AccessToken;
import com.ibm.cloud.appid.android.api.tokens.IdentityToken;
import com.ibm.cloud.appid.android.api.tokens.RefreshToken;
import com.ibm.cloud.appid.android.internal.OAuthManager;
import com.ibm.cloud.appid.android.internal.config.Config;
import com.ibm.cloud.appid.android.internal.network.AppIDRequest;
import com.ibm.cloud.appid.android.internal.registrationmanager.RegistrationManager;
import com.ibm.cloud.appid.android.internal.tokens.AccessTokenImpl;
import com.ibm.cloud.appid.android.internal.tokens.IdentityTokenImpl;
import com.ibm.cloud.appid.android.internal.tokens.RefreshTokenImpl;
import com.ibm.cloud.appid.android.internal.tokens.Token;
import com.ibm.mobilefirstplatform.clientsdk.android.core.api.Response;
import com.ibm.mobilefirstplatform.clientsdk.android.core.api.ResponseListener;
import com.ibm.mobilefirstplatform.clientsdk.android.logger.api.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigInteger;
import java.security.Key;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.util.HashMap;
import java.util.Map;
import io.jsonwebtoken.IncorrectClaimException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureException;

public class TokenManager {

	private final AppID appId;
	private final RegistrationManager registrationManager;
	private AccessToken latestAccessToken;
	private IdentityToken latestIdentityToken;
	private RefreshToken latestRefreshToken;
	private Map<String,Key> publicKeys;
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
	private static final String GRANT_TYPE_REFRESH = "refresh_token";
	private final static String APPID_ACCESS_TOKEN = "appid_access_token";
	private final static String ERROR_DESCRIPTION = "error_description";
	private final static String ERROR = "error";
	private final static String INVALID_GRANT = "invalid_grant";
	protected enum TOKENS {
		ACCESS_TOKEN("access_token"),
		ID_TOKEN("id_token"),
		REFRESH_TOKEN("refresh_token");
		private final String token;
		TOKENS(final String text) {
			this.token = text;
		}
		@Override
		public String toString() {
			return token;
		}
	}

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

	protected void retrieveTokens(Map<String, String> formParams, final TokenResponseListener listener) {
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
		formParams.put(TOKENS.REFRESH_TOKEN.toString(), refreshTokenString);
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

		Map<String, Token> tokens;
		String refreshToken = null;
		try {
			JSONObject responseJSON = new JSONObject(response.getResponseText());
			if (responseJSON.has(TOKENS.ACCESS_TOKEN.toString()) && responseJSON.has(TOKENS.ID_TOKEN.toString())) {
				Token accessToken = new AccessTokenImpl(responseJSON.getString(TOKENS.ACCESS_TOKEN.toString()));
				Token identityToken = new IdentityTokenImpl(responseJSON.getString(TOKENS.ID_TOKEN.toString()));
				if (responseJSON.has(TOKENS.REFRESH_TOKEN.toString())) {
					refreshToken = responseJSON.getString(TOKENS.REFRESH_TOKEN.toString());
				}
				tokens = new HashMap<>();
				tokens.put(TOKENS.ACCESS_TOKEN.toString(), accessToken);
				tokens.put(TOKENS.ID_TOKEN.toString(), identityToken);
			} else {
				throw new Exception("Invalid response : Missing access_token/id_token");
			}
		} catch (Exception e){
			logger.error("Failed to parse server response", e);
			tokenResponseListener.onAuthorizationFailure(new AuthorizationException("Failed to parse server response, error : " + e.getMessage()));
			return;
		}
		RetrievedTokens retrievedTokens = new RetrievedTokens(tokens, refreshToken, TOKENS.ACCESS_TOKEN);
		extractTokens(retrievedTokens, tokenResponseListener);
	}

	protected TOKENS getNextToken(TOKENS token) {
		switch (token){
			case ACCESS_TOKEN: return TOKENS.ID_TOKEN;
			case ID_TOKEN: return TOKENS.REFRESH_TOKEN;
			default: return token;
		}
	}

	protected void extractTokens(RetrievedTokens retrievedTokens, TokenResponseListener listener) {
		TOKENS flowStep = retrievedTokens.getFlowStep();
		if (flowStep == TOKENS.ACCESS_TOKEN || flowStep == TOKENS.ID_TOKEN){
			logger.debug("Extracting and verifying " + flowStep.toString() + " token from server response");
			try {
				Token token = retrievedTokens.getTokens().get(flowStep.toString());
				String tokenType = flowStep.toString();
				String kid = token.getHeader().getString("kid");
				Key tokenPublicKey = getPublicKeyByKid(kid);
				retrievedTokens.setFlowStep(getNextToken(flowStep));
				if (tokenPublicKey == null) {
					lookUpPublicKey(kid, retrievedTokens, listener, tokenType);
				} else {
					saveToken(retrievedTokens, tokenPublicKey, listener, tokenType);
				}
			} catch (RuntimeException|AuthorizationException|JSONException e){
				logger.error("Failed to parse " + flowStep.toString() + ", error : ", e);
				listener.onAuthorizationFailure(new AuthorizationException("Failed to parse " + flowStep.toString() + ", error : " + e.getMessage()));
			}
		} else {
			if (retrievedTokens.getRefreshToken() != null){
				latestRefreshToken = new RefreshTokenImpl(retrievedTokens.getRefreshToken());
			}
			Map<String, Token> tokens = retrievedTokens.getTokens();
			latestIdentityToken = (IdentityToken) tokens.get(TOKENS.ID_TOKEN.toString());
			latestAccessToken = (AccessToken) tokens.get(TOKENS.ACCESS_TOKEN.toString());
			listener.onAuthorizationSuccess(latestAccessToken, latestIdentityToken, latestRefreshToken);
		}
	}

	protected void lookUpPublicKey(final String tokenKid, final RetrievedTokens retrievedTokens, final TokenResponseListener listener, final String tokenType){
		AppIDRequest request = createAppIDRequest(Config.getPublicKeysEndpoint(appId), AppIDRequest.GET);
		request.send(new ResponseListener() {
			@Override
			public void onSuccess(Response response){
				try {
					Key publicKey = getPublickey(response, tokenKid);
					saveToken(retrievedTokens, publicKey, listener, tokenType);
				} catch (AuthorizationException exception){
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

	protected void saveToken(final RetrievedTokens retrievedTokens, Key publicKey, TokenResponseListener listener, String tokenType) {
		String clientId = registrationManager.getRegistrationDataString(RegistrationManager.CLIENT_ID);
		try {
			Token token = retrievedTokens.getTokens().get(tokenType);
			Boolean verify = verifyToken(publicKey, token.getRaw(), Config.getIssuer(appId), clientId, appId.getTenantId());
			if (verify){
				extractTokens(retrievedTokens, listener);
			} else {
				clearStoredTokens();
				listener.onAuthorizationFailure(new AuthorizationException("Failed to verify "+tokenType));
			}
		} catch (Exception exception){
			clearStoredTokens();
			listener.onAuthorizationFailure(new AuthorizationException("Failed to parse "+ tokenType + ",error : "+ exception.getMessage()));
		}
	}

	protected Key getPublicKeyByKid(String tokenKid) throws AuthorizationException {
		if (tokenKid == null || tokenKid.equals("")){
			throw new AuthorizationException("Invalid Kid");
		}
		if (publicKeys.containsKey(tokenKid)){
			return publicKeys.get(tokenKid);
		}
		return null;
	}

	protected Key getPublickey(Response response, String tokenKid) throws AuthorizationException {
		JSONObject responseJSON;
		try {
			responseJSON = new JSONObject(response.getResponseText());
			JSONArray jwkArray = responseJSON.getJSONArray("keys");
			for(int i = 0; i < jwkArray.length(); i++)
			{
				JSONObject jwkJSONObject = jwkArray.getJSONObject(i);
				BigInteger modulus = new BigInteger(1, Base64.decode(jwkJSONObject.getString("n"), Base64.URL_SAFE));
				BigInteger exponent = new BigInteger(1, Base64.decode(jwkJSONObject.getString("e"), Base64.URL_SAFE));
				RSAPublicKey publicKey = (RSAPublicKey) KeyFactory.getInstance("RSA").generatePublic(new RSAPublicKeySpec(modulus, exponent));
				publicKeys.put(jwkJSONObject.getString("kid"), publicKey);
			}

			if (publicKeys.containsKey(tokenKid)){
				return publicKeys.get(tokenKid);
			} else {
				throw new AuthorizationException("Failed to retrieve public keys for Kid from server");
			}
		} catch (Exception e){
			logger.error("Failed to parse server response", e);
			throw new AuthorizationException("Failed to parse public keys from server");
		}
	}

	protected boolean verifyToken(Key rsaPublicKey, String token, String issuer, String audience, String tenant) throws SignatureException,IncorrectClaimException {
		if (rsaPublicKey == null){
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

	protected static class RetrievedTokens {
		private Map<String, Token> tokens;
		private String refreshToken;
		private TOKENS flowStep;
		RetrievedTokens(Map<String, Token> tokens, String refreshToken, TOKENS flowStep){
			this.tokens = tokens;
			this.refreshToken = refreshToken;
			this.flowStep = flowStep;
		}

		public Map<String, Token> getTokens() {
			return tokens;
		}

		public String getRefreshToken() {
			return refreshToken;
		}

		public TOKENS getFlowStep() {
			return flowStep;
		}

		public void setFlowStep(TOKENS nextToken) {
			this.flowStep = nextToken;
		}

	}
}
