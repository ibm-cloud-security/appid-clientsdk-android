package com.ibm.mobilefirstplatform.appid_clientsdk_android;

import com.ibm.mobilefirstplatform.clientsdk.android.core.api.BMSClient;
import com.ibm.mobilefirstplatform.clientsdk.android.core.api.Response;
import com.ibm.mobilefirstplatform.clientsdk.android.core.api.ResponseListener;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static org.junit.Assert.*;

/**
 * Created by rotembr on 11/12/2016.
 */

public class AppIdTests {

    private static final  String testTenantId = "testTenant";
    private static  final String testRegion = "TestRegion";

    @Before
    public void setup() {
        AppId.createInstance(getInstrumentation().getTargetContext(), testTenantId, testRegion);
    }

    @Test
    public void authManagerTest() {
        assertEquals(BMSClient.getInstance().getAuthorizationManager(), AppIdAuthorizationManager.getInstance());
    }

    @Test
    public void getTenantIdTest() {
        assertEquals(AppId.getInstance().getTenantId(), testTenantId);
    }

    @Test
    public void getRegionTest() {
        assertEquals(AppId.getInstance().getBluemixRegionSuffix(), testRegion);
    }

}

