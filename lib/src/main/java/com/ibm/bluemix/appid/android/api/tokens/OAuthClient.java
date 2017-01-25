package com.ibm.bluemix.appid.android.api.tokens;


public interface OAuthClient {
	String getType();
	String getName();
	String getSoftwareId();
	String getSoftwareVersion();
	String getDeviceId();
	String getDeviceModel();
	String getDeviceOS();
}
