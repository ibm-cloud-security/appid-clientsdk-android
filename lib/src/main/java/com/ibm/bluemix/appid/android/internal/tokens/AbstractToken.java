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

package com.ibm.bluemix.appid.android.internal.tokens;

import android.util.Base64;

import com.ibm.mobilefirstplatform.clientsdk.android.logger.api.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

public abstract class AbstractToken implements Token {
	private final String raw;
	private JSONObject header;
	private JSONObject payload;
	private final String signature;

	private final static String ISSUER = "iss";
	private final static String SUBJECT = "sub";
	private final static String AUDIENCE = "aud";
	private final static String EXPIRATION = "exp";
	private final static String ISSUED_AT = "iat";
	private final static String TENANT = "tenant";
	private final static String AUTH_BY = "auth_by";


	Logger logger = Logger.getLogger(Logger.INTERNAL_PREFIX + AbstractToken.class.getName());

	public AbstractToken (String raw) throws RuntimeException {
		this.raw = raw;
		String[] tokenComponents = raw.split("\\.");
		String headerComponent = tokenComponents[0];
		String payloadComponent = tokenComponents[1];
		this.signature = tokenComponents[2];

		String decodedHeader = new String(Base64.decode(headerComponent, Base64.URL_SAFE));
		String decodedPayload = new String(Base64.decode(payloadComponent, Base64.URL_SAFE));

		try {
			this.header = new JSONObject(decodedHeader);
			this.payload = new JSONObject(decodedPayload);
		} catch (JSONException e){
			this.header = null;
			this.payload = null;
			logger.error("Failed to parse JWT", e);
			throw new RuntimeException("Failed to parse JWT");
		}
	}

	public String getRaw () {
		return raw;
	}

	public JSONObject getHeader () {
		return header;
	}

	public JSONObject getPayload () {
		return payload;
	}

	public String getSignature () {
		return signature;
	}

	@Override
	public String getIssuer () {
		return (String) getValue(ISSUER);
	}

	@Override
	public String getSubject () {
		return (String) getValue(SUBJECT);
	}

	@Override
	public String getAudience () {
		return (String) getValue(AUDIENCE);
	}

	@Override
	public Date getExpiration () {
		long epochTimestamp = (int)getValue(EXPIRATION);
		return new Date(epochTimestamp * 1000);
	}

	@Override
	public Date getIssuedAt () {
		long epochTimestamp = (int)getValue(ISSUED_AT);
		return new Date(epochTimestamp * 1000);
	}

	@Override
	public String getTenant () {
		return (String) getValue(TENANT);
	}

	@Override
	public String getAuthBy () {
		return (String) getValue(AUTH_BY);
	}

	protected Object getValue (String name){
		try {
			return getPayload().get(name);
		} catch (JSONException e){
			logger.error("Failed to retrieve " + name, e);
			return null;
		}
	}

	public boolean isExpired(){
		return getExpiration().before(new Date());
	}
}
