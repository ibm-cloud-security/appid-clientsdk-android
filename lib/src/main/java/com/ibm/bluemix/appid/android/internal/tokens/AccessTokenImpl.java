package com.ibm.bluemix.appid.android.internal.tokens;

import com.ibm.bluemix.appid.android.api.tokens.AccessToken;
import com.ibm.mobilefirstplatform.clientsdk.android.logger.api.Logger;

public class AccessTokenImpl extends AbstractToken implements AccessToken {

	private static final Logger logger = Logger.getLogger(Logger.INTERNAL_PREFIX + AccessTokenImpl.class.getName());
	private final static String SCOPE = "scope";

	public AccessTokenImpl (String raw) throws RuntimeException {
		super(raw);
	}

	@Override
	public String getScope () {
		return (String) getValue(SCOPE);
	}

}
