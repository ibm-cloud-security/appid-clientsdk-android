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

package com.ibm.bluemix.appid.android;

import android.support.test.runner.AndroidJUnit4;

import com.ibm.bluemix.appid.android.api.tokens.AccessToken;
import com.ibm.bluemix.appid.android.api.tokens.IdentityToken;
import com.ibm.bluemix.appid.android.internal.tokens.AccessTokenImpl;
import com.ibm.bluemix.appid.android.internal.tokens.IdentityTokenImpl;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.*;

import java.util.Date;

@RunWith (AndroidJUnit4.class)
public class TokensTest {
	private static final String accessTokenRaw = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpPU0UiLCJqcGsiOnsiYWxnIjoiUlNBIiwibW9kIjoiQUtTZDA4R3ViajR3a2ZWTmN5MWcyYUREMlNQNHJTQXhxcVNwcTNCeVRRdzFBNE5SbE5fMm9ieWFVX05TQTBvMmtCV0xEWDNiTk80dHlCcWROSHpjRWhZdU1XYWFmdGV1clB4OV9MaTZOZzRIeE1na19NdWNDcVBlckRONnBmNklHeEp4V1hVVDNSOTQ5WEpHdFBOVndSQ2V5MWloZUZjVXA1TTRMR1p4SGZaZmtnX1lWSE91NUZzeDZmMGFMMlFfNlFiVUVsZTJaa3dIejlHaDhPTG9MY1ZxX3lCazliSFY0NkRZUXdOazNfcFFjZDh0Z214cFJZRUQ2WDJPN1BkakVtNk5VNlpFMTdtZXV4MEpfVEtVcHlaekNVZU1ZeW9RYnVDMktzY0hPNkticGtUSmFVZy1PeWdOSUFOX0Z3eTdobGpDWFZBczA1TGdJVmRqcEhpREJyTSIsImV4cCI6IkFRQUIifX0.eyJpc3MiOiJsb2NhbGhvc3Q6NjAxMiIsImV4cCI6MTQ4NTE4OTgzNCwiYXVkIjoiZTFkMDBjMTgtMTRiOS00MzQ1LTg0YzUtMDYzODMzNDMxYTEwIiwic3ViIjoiMWM5YWRmZmItMDY0ZC00YzY2LThlZDItYmJjMTJiZjkzZjU4IiwiaWF0IjoxNDg1MTg2MjM0LCJhdXRoQnkiOiJmYWNlYm9vayIsInNjb3BlIjoiZGVmYXVsdCJ9.RpLGYHOoEvomVoxIklAeDg7aMjVTsfGWJhGubhX8IIVGaoElMXu5ufT1E6G7AradOL3hm7yAvwguaBtE4CQkLIxA_3iCIJPKa-cHwSivQ4o96yTNOlqtAMK_f8-nh0zcVcCQNMe8HRBvFZuTtrL2Lx_KTQiYTeHyQ3QykIn9XGcEW6p8k2zx0IU574FZgLPH6-uOjFlZu4i5uDufCLX0lbEYJ5H6_EIh9uyyC436JfP0R5awHkUGTmkkj25ddhJXVCOgsUv-AUUfGKak3Wn5NhnEbUQdgUvU2yQqz41qDzGRqH81le-siFEDyPi4ls8SfXaP-c4V4qofugN0LrGmOg";
	private static final String identityTokenRaw = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpPU0UiLCJqcGsiOnsiYWxnIjoiUlNBIiwibW9kIjoiQUtTZDA4R3ViajR3a2ZWTmN5MWcyYUREMlNQNHJTQXhxcVNwcTNCeVRRdzFBNE5SbE5fMm9ieWFVX05TQTBvMmtCV0xEWDNiTk80dHlCcWROSHpjRWhZdU1XYWFmdGV1clB4OV9MaTZOZzRIeE1na19NdWNDcVBlckRONnBmNklHeEp4V1hVVDNSOTQ5WEpHdFBOVndSQ2V5MWloZUZjVXA1TTRMR1p4SGZaZmtnX1lWSE91NUZzeDZmMGFMMlFfNlFiVUVsZTJaa3dIejlHaDhPTG9MY1ZxX3lCazliSFY0NkRZUXdOazNfcFFjZDh0Z214cFJZRUQ2WDJPN1BkakVtNk5VNlpFMTdtZXV4MEpfVEtVcHlaekNVZU1ZeW9RYnVDMktzY0hPNkticGtUSmFVZy1PeWdOSUFOX0Z3eTdobGpDWFZBczA1TGdJVmRqcEhpREJyTSIsImV4cCI6IkFRQUIifX0.eyJpc3MiOiJsb2NhbGhvc3Q6NjAxMiIsInN1YiI6IjFjOWFkZmZiLTA2NGQtNGM2Ni04ZWQyLWJiYzEyYmY5M2Y1OCIsImF1ZCI6ImUxZDAwYzE4LTE0YjktNDM0NS04NGM1LTA2MzgzMzQzMWExMCIsImV4cCI6MTQ4NTE4OTgzNCwiaWF0IjoxNDg1MTg2MjM0LCJuYW1lIjoiQXNhZiBNYW5hc3NlbiIsImdlbmRlciI6Im1hbGUiLCJsb2NhbGUiOiJlbl9VUyIsInBpY3R1cmUiOiJodHRwczovL2ZiLXMtYi1hLmFrYW1haWhkLm5ldC9oLWFrLXhmYTEvdi90MS4wLTEvYzIwLjAuNTAuNTAvcDUweDUwLzE5NTkzNjNfMTAxNTIwMzY4NjkxOTI0MDFfMTAyNjQxODk0NzAzMzQzMDI1N19uLmpwZz9vaD0wOWY5ZjQ3N2IyMzcwYTMwYmJiZGY1M2U2ZTA2ODc1ZiZvZT01OTBBMEI2QSZfX2dkYV9fPTE0OTUwNjYwNzFfYjQzMzU0OTFmYTMxZDliMzdhNGE3NTQ3YTU1NDAzYzEiLCJpZGVudGl0aWVzIjpbeyJwcm92aWRlciI6ImZhY2Vib29rIiwiaWQiOiIxMDE1MzUwMjA0NDE4MjQwMSJ9XSwiYXV0aEJ5IjoiZmFjZWJvb2siLCJvYXV0aGNsaWVudCI6eyJ0eXBlIjoic2VydmVyYXBwIiwic29mdHdhcmVfaWQiOiI3OTllMmZiYi1jZTZkLTQzMjEtOGJkYy1mMjZkMjY3M2E0YTMuN2ZkMzE0ZTUtODIzZS00MWQ2LWEzODAtNzg0NDI2ZDA5MWY1LmUxZDAwYzE4LTE0YjktNDM0NS04NGM1LTA2MzgzMzQzMWExMCIsInNvZnR3YXJlX3ZlcnNpb24iOiIxLjAuMCJ9fQ.UaN6NG0rvZrRbqnNzwNpooKeHSJfoM5afXcb33BIbvn428imTKuamm1YLpf1YJ9rMJmJqfqdFidCF6rYRjyMmRiLVVXotPBlootmeyUh2WvrdUybxDx7keOJ9p-I0kDFEXEaEHSpBly1gSlXB6KrdltdW8HibfGQWw8xi7nsxN0fUf327IUoCzTDesB5EA29-ZvOOvbCBjsCmSHWcYaAq0oFb2r9IJbGaaLC2lDw1Koh3VuCvyvlP-FhfwhpxzoWHUx7AOCidDBv7SwYXXTY9iFzMCct3DvfMoikClDm3hF_uIGW8lbw1LIeiBRKN_489nxD7P-yCir4d9hg85QwnQ";
	private AccessToken accessToken;
	private IdentityToken identityToken;

	private static final String EXPECTED_ISSUER = "localhost:6012";
	private static final String EXPECTED_AUDIENCE = "e1d00c18-14b9-4345-84c5-063833431a10";
	private static final String EXPECTED_SUBJECT = "1c9adffb-064d-4c66-8ed2-bbc12bf93f58";
	private static final Date EXPECTED_EXPIRATION = new Date(1485189834000L);
	private static final Date EXPECTED_ISSUED_AT = new Date(1485186234000L);
	private static final String EXPECTED_AUTH_BY = "facebook";
	private static final String EXPECTED_SCOPE = "default";
	private static final String EXPECTED_TENANT = "???";
	private static final String EXPECTED_NAME = "Asaf Manassen";
	private static final String EXPECTED_EMAIL = "???";
	private static final String EXPECTED_GENDER = "male";
	private static final String EXPECTED_LOCALE = "en_US";
	private static final String EXPECTED_PICTURE = "https://fb-s-b-a.akamaihd.net/h-ak-xfa1/v/t1.0-1/c20.0.50.50/p50x50/1959363_10152036869192401_1026418947033430257_n.jpg?oh=09f9f477b2370a30bbbdf53e6e06875f&oe=590A0B6A&__gda__=1495066071_b4335491fa31d9b37a4a7547a55403c1";
	private static final String EXPECTED_OAUTH_TYPE = "serverapp";
	private static final String EXPECTED_OAUTH_NAME = "????";
	private static final String EXPECTED_OAUTH_SOFTWARE_ID = "799e2fbb-ce6d-4321-8bdc-f26d2673a4a3.7fd314e5-823e-41d6-a380-784426d091f5.e1d00c18-14b9-4345-84c5-063833431a10";
	private static final String EXPECTED_OAUTH_SOFTWARE_VERSION = "1.0.0";
	private static final String EXPECTED_OAUTH_DEVICE_ID = "???";
	private static final String EXPECTED_OAUTH_DEVICE_MODEL = "???";
	private static final String EXPECTED_OAUTH_DEVICE_OS = "???";

	@Before
	public void setup () {
		accessToken = new AccessTokenImpl(accessTokenRaw);
		identityToken = new IdentityTokenImpl(identityTokenRaw);
	}

	@Test
	public void testAccessToken () {

		assertEquals(accessToken.getRaw(), accessTokenRaw);
		assertEquals(accessToken.getSignature(), accessTokenRaw.split("\\.")[2]);
		assertEquals(accessToken.getIssuer(), EXPECTED_ISSUER);
		assertEquals(accessToken.getSubject(), EXPECTED_SUBJECT);
		assertEquals(accessToken.getAudience(), EXPECTED_AUDIENCE);
		assertEquals(accessToken.getExpiration(), EXPECTED_EXPIRATION);
		assertEquals(accessToken.getIssuedAt(), EXPECTED_ISSUED_AT);
//		assertEquals(accessToken.getTenant(), EXPECTED_TENANT);
//		assertEquals(accessToken.getAuthBy(), EXPECTED_AUTH_BY);
		assertEquals(accessToken.getScope(), EXPECTED_SCOPE);
	}

	@Test
	public void testIdentityToken () {
		assertEquals(identityToken.getRaw(), identityTokenRaw);
		assertEquals(identityToken.getSignature(), identityTokenRaw.split("\\.")[2]);
		assertEquals(identityToken.getIssuer(), EXPECTED_ISSUER);
		assertEquals(identityToken.getSubject(), EXPECTED_SUBJECT);
		assertEquals(identityToken.getAudience(), EXPECTED_AUDIENCE);
		assertEquals(identityToken.getExpiration(), EXPECTED_EXPIRATION);
		assertEquals(identityToken.getIssuedAt(), EXPECTED_ISSUED_AT);
//		assertEquals(identityToken.getTenant(), EXPECTED_TENANT);
//		assertEquals(identityToken.getAuthBy(), EXPECTED_AUTH_BY);
		assertEquals(identityToken.getName(), EXPECTED_NAME);
//		assertEquals(identityToken.getEmail(), EXPECTED_EMAIL);
		assertEquals(identityToken.getGender(), EXPECTED_GENDER);
		assertEquals(identityToken.getLocale(), EXPECTED_LOCALE);
		assertEquals(identityToken.getPicture(), EXPECTED_PICTURE);

//		OAuthClient oAuthClient = identityToken.getOAuthClient();
//		assertEquals(oAuthClient.getType(), EXPECTED_OAUTH_TYPE);
////		assertEquals(oAuthClient.getName(), EXPECTED_OAUTH_NAME);
//		assertEquals(oAuthClient.getSoftwareId(), EXPECTED_OAUTH_SOFTWARE_ID);
//		assertEquals(oAuthClient.getSoftwareVersion(), EXPECTED_OAUTH_SOFTWARE_VERSION);
////		assertEquals(oAuthClient.getDeviceId(), EXPECTED_OAUTH_DEVICE_ID);
////		assertEquals(oAuthClient.getDeviceModel(), EXPECTED_OAUTH_DEVICE_MODEL);
////		assertEquals(oAuthClient.getDeviceOS(), EXPECTED_OAUTH_DEVICE_OS);
	}
}
