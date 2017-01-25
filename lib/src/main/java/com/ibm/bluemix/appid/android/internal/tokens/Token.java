package com.ibm.bluemix.appid.android.internal.tokens;

import org.json.JSONObject;

import java.util.Date;

public interface Token {
	String getRaw ();
	JSONObject getHeader ();
	JSONObject getPayload ();
	String getSignature ();

	String getIssuer();
	String getSubject();
	String getAudience();
	Date getExpiration();
	Date getIssuedAt();
	String getTenant();
	String getAuthBy();
}
