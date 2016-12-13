package com.ibm.mobilefirstplatform.appid_clientsdk_android;

import org.junit.Before;
import org.junit.Test;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static org.junit.Assert.*;

/**
 * Created by rotembr on 11/12/2016.
 */

public class AppIdTests {

    @Before
    public void setup() {
        AppId.createInstance(getInstrumentation().getTargetContext(),"testTenant", "TestRegion");
    }

    @Test
    public void loginTest() {
        fail();
//        final ResponseListener testListener = new ResponseListener() {
//            @Override
//            public void onSuccess(Response response) {
//                fail("The login should be rejected");
//            }
//
//            @Override
//            public void onFailure(Response response, Throwable t, JSONObject extendedInfo) {
//                try {
//                    assertEquals(extendedInfo.getString("msg"), "test msg");
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//            }
//        };
//
//        final JSONObject info = new JSONObject();
//        try {
//            info.put("msg", "test msg");
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//
//
//        doAnswer(new Answer() {
//            @Override
//            public Object answer(InvocationOnMock invocation) throws Throwable {
//                ResponseListener r = (ResponseListener) invocation.getArguments()[0];
//                r.onFailure(null,null,info);
//                return null;
//            }
//        }).when(appIdRM).invokeInstanceRegistrationRequest(testListener);
//
//        AppId.getInstance().login(getInstrumentation().getTargetContext(), testListener);
    }

}
