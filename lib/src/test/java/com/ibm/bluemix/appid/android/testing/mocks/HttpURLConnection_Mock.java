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

package com.ibm.bluemix.appid.android.testing.mocks;

import java.io.IOException;
import java.net.HttpURLConnection;

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
