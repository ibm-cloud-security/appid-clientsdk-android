package com.ibm.bluemix.appid.android.api.tokens;

import android.util.Base64;

import com.ibm.mobilefirstplatform.clientsdk.android.logger.api.Logger;

import org.json.JSONException;
import org.json.JSONObject;

// TODO: document
abstract class AbstractToken {
	private final String raw;
	private JSONObject header;
	private JSONObject payload;
	private final String signature;

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
}
