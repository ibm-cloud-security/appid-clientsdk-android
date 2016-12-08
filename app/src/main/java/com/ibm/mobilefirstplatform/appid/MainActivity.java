package com.ibm.mobilefirstplatform.appid;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.ibm.mobilefirstplatform.appid_clientsdk_android.AppId;
import com.ibm.mobilefirstplatform.clientsdk.android.core.api.Response;
import com.ibm.mobilefirstplatform.clientsdk.android.core.api.ResponseListener;

import org.json.JSONObject;

import java.net.URL;

public class MainActivity extends AppCompatActivity implements ResponseListener {

    private final static String mcaTenantId = "76ac844c-075c-41b3-b95e-86629713b6a2";
    private final static String region = AppId.REGION_UK; // replace with server suffix url
    private AppId appId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        appId = AppId.createInstance(this.getApplicationContext(), mcaTenantId, region);
        appId.overrideServerHost = "http://10.0.2.2:6001"; //only when working locally
    }

    public void onLoginClick(View v){
        showProgress();
        appId.login(this.getApplicationContext(), this);
    }

    @Override
    public void onSuccess(Response response) {
        // here we handle authentication success
        Log.i("Rotem", "success");
        Log.i("Rotem", response.toString());
        showDetailsAndPicture();
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
        showDetails("Failure in Login");
    }

    private void showDetails(final String result) {
        //run on main thread
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TextView nameTextView = (TextView) findViewById(R.id.name);
                nameTextView.setText(result);
                hideProgress();
            }
        });
    }

    private void showDetailsAndPicture() {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                URL picUrl = appId.getUserProfilePicture();
                try {
                    final Bitmap bmp = BitmapFactory.decodeStream(picUrl.openConnection().getInputStream());
                    //run on main thread
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ImageView profilePicture = (ImageView) findViewById(R.id.profilePic);
                            profilePicture.setImageBitmap(bmp);
                            profilePicture.requestLayout();
                            profilePicture.getLayoutParams().height = 350;
                            profilePicture.getLayoutParams().width = 350;
                            profilePicture.setScaleType(ImageView.ScaleType.FIT_XY);
                            profilePicture.setVisibility(View.VISIBLE);
                            showDetails("Hello " + appId.getUserDisplayName());
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void showProgress(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                findViewById(R.id.loadingPanel).setVisibility(View.VISIBLE);
                findViewById(R.id.loadingPanel).bringToFront();
                findViewById(R.id.loginButton).setEnabled(false);
            }
        });
    }

    private void hideProgress(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                findViewById(R.id.loadingPanel).setVisibility(View.GONE);
                findViewById(R.id.loginButton).setEnabled(true);
            }
        });
    }

}
