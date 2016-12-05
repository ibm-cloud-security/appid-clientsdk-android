package com.ibm.mobilefirstplatform.appid_clientsdk_android;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    private final static String mcaTenantId = "76ac844c-075c-41b3-b95e-86629713b6a2";
    private final static String region = "localhost";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        Appid appid = Appid.createInstance(this.getApplicationContext(), mcaTenantId, region);
//        appid.login(this.getApplicationContext(), this);

    }

}
