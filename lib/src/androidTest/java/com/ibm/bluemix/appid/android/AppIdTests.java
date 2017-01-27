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

import com.ibm.bluemix.appid.android.api.AppID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

import static android.support.test.InstrumentationRegistry.getInstrumentation;

@RunWith(AndroidJUnit4.class)
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
	public void getTenantId () {
		assertEquals(appId.getTenantId(), testTenantId);
	}

	@Test
	public void getRegion () {
		assertEquals(appId.getBluemixRegionSuffix(), testRegion);
	}
}

