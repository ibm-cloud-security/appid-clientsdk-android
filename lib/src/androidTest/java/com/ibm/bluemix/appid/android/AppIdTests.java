/*
	Copyright 2014-17 IBM Corp.
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

import com.ibm.bluemix.appid.android.api.AppID;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

import static android.support.test.InstrumentationRegistry.getInstrumentation;

public class AppIdTests {

	private static final String testTenantId = "testTenant";
	private static final String testRegion = "TestRegion";
	private AppID appId;

	@Before
	public void setup () {
		this.appId = AppID.getInstance();
		this.appId.initialize(getInstrumentation().getTargetContext(), testTenantId, testRegion);
	}

	@Test
	public void getTenantIdTest () {
		assertEquals(appId.getTenantId(), testTenantId);
	}

	@Test
	public void getRegionTest () {
		assertEquals(appId.getBluemixRegionSuffix(), testRegion);
	}
}

