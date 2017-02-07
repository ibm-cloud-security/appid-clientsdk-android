package com.ibm.bluemix.appid.android.testing.mocks;

import java.io.IOException;
import java.net.HttpURLConnection;

/**
 * Created on 2/4/17.
 */

public class HttpURLConnection_Mock extends HttpURLConnection {

	public HttpURLConnection_Mock() {
		super(null);
	}

	@Override
	public int getResponseCode(){
		return 401;
	}

	@Override
	public String getHeaderField (String name) {
		return "Bearer scope=\"appid_default\"";
	}

	@Override
	public void disconnect () {
		throw new RuntimeException("Not implemented");
	}

	@Override
	public boolean usingProxy () {
		throw new RuntimeException("Not implemented");
	}

	@Override
	public void connect () throws IOException {
		throw new RuntimeException("Not implemented");
	}
}
