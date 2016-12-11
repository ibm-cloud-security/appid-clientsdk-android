package com.ibm.mobilefirstplatform.appid_clientsdk_android;

import android.support.test.runner.AndroidJUnit4;
import android.test.InstrumentationTestCase;

import com.ibm.mobilefirstplatform.clientsdk.android.core.api.Response;
import com.ibm.mobilefirstplatform.clientsdk.android.core.api.ResponseListener;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;


import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

/**
 * Created by rotembr on 11/12/2016.
 */
@RunWith(MockitoJUnitRunner.class)
public class AppIdTests {

    @Mock
    AppIdRegistrationManager appIdRM;

    @Before
    public void setUp() {
        AppId.createInstance(getInstrumentation().getTargetContext(),"testTenant", "TestRegion");
    }


//    @Test
//    public void loginTest() {
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
//                testListener.onFailure(null,null,info);
//                return null;
//            }
//        }).when(appIdRM).invokeInstanceRegistrationRequest(testListener);
//
//        AppId.getInstance().login(getInstrumentation().getTargetContext(), testListener);
//    }

}
