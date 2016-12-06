package com.ibm.mobilefirstplatform.appid;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.ibm.mobilefirstplatform.appid_clientsdk_android.AppId;
import com.ibm.mobilefirstplatform.clientsdk.android.core.api.Response;
import com.ibm.mobilefirstplatform.clientsdk.android.core.api.ResponseListener;

import org.json.JSONObject;

public class MainActivity extends AppCompatActivity implements ResponseListener {

    private final static String mcaTenantId = "76ac844c-075c-41b3-b95e-86629713b6a2";
    private final static String region = AppId.REGION_UK; // replace with server suffix url

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        AppId appid = AppId.createInstance(this.getApplicationContext(), mcaTenantId, region);

        appid.overrideServerHost = "http://10.0.2.2:6001"; //only when working locally

        appid.login(this.getApplicationContext(), this);

    }

    @Override
    public void onSuccess(Response response) {
        // here we handle authentication success
        Log.i("Rotem", "success");
        Log.i("Rotem", response.toString());
    }

    @Override
    public void onFailure(Response response, Throwable t, JSONObject extendedInfo) {
        // handle auth failure
        Log.i("Rotem", "fail");
        if(response != null){
            Log.i("Rotem",response.toString());
        }
        if(extendedInfo != null){
            Log.i("Rotem",extendedInfo.toString());
        }
    }

}
