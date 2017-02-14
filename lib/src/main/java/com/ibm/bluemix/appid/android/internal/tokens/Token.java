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

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Date;
import java.util.List;

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
	List<String> getAuthenticationMethods();
	boolean isExpired();
	boolean isAnonymous();
}
