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

package com.ibm.cloud.appid.android.internal.tokens;

import com.ibm.cloud.appid.android.api.tokens.IdentityToken;
import com.ibm.mobilefirstplatform.clientsdk.android.logger.api.Logger;

import org.json.JSONArray;
import org.json.JSONException;

public class IdentityTokenImpl extends AbstractToken implements IdentityToken {

	private static final Logger logger = Logger.getLogger(Logger.INTERNAL_PREFIX + IdentityTokenImpl.class.getName());
	private final static String NAME = "name";
	private final static String EMAIL = "email";
	private final static String GENDER = "gender";
	private final static String LOCALE = "locale";
	private final static String PICTURE = "picture";
	private final static String IDENTITIES = "identities";

	public IdentityTokenImpl (String raw) throws RuntimeException {
		super(raw);
	}

	@Override
	public String getName () {
		return (String) getValue(NAME);
	}

	@Override
	public String getEmail () {
		return (String) getValue(EMAIL);
	}

	@Override
	public String getGender () {
		return (String) getValue(GENDER);
	}

	@Override
	public String getLocale () {
		return (String) getValue(LOCALE);
	}

	@Override
	public String getPicture () {
		return (String) getValue(PICTURE);
	}

	@Override
	public JSONArray getIdentities () {
		try {
			return getPayload().getJSONArray(IDENTITIES);
		} catch (JSONException e){
			logger.warn("Failed to retrieve " + IDENTITIES + ", possibly anonymous user");
			return new JSONArray();
		}
	}
}
