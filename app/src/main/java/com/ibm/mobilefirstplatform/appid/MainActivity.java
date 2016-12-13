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
import com.ibm.mobilefirstplatform.clientsdk.android.core.api.Request;
import com.ibm.mobilefirstplatform.clientsdk.android.core.api.Response;
import com.ibm.mobilefirstplatform.clientsdk.android.core.api.ResponseListener;

import org.json.JSONObject;

import java.net.URL;

public class MainActivity extends AppCompatActivity implements ResponseListener {

    private final static String mcaTenantId = "76ac844c-075c-41b3-b95e-86629713b6a2";
    // server suffix url
    private final static String region = ".stage1-dev.ng.bluemix.net";//AppId.REGION_UK;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        AppId.createInstance(this.getApplicationContext(), mcaTenantId, region);
//        AppId.overrideServerHost = "http://10.0.2.2:6001"; //only when working locally
    }

    public void onLoginClick(View v){
        showProgress();
        AppId.getInstance().login(this.getApplicationContext(), this);
    }

    public void onProtectedRequestClick(View v){
        showProgress();
        Request r = new Request( "http://appid-rotem.stage1.mybluemix.net" + "/protectedResource", Request.GET);
        r.send(this, this);
    }

    @Override
    public void onSuccess(Response response) {
        // here we handle authentication success
        Log.i("Login", "success");
        Log.i("Login", response.toString());
        showDetailsAndPicture(response.getResponseText());
    }

    @Override
    public void onFailure(Response response, Throwable t, JSONObject extendedInfo) {
        // handle auth failure
        Log.i("Login", "fail");
        if(response != null){
            Log.i("Login",response.toString());
            showDetails("Failure in Login/protected resource", response.getResponseText());
        }
        if(extendedInfo != null){
            Log.i("Login",extendedInfo.toString());
            showDetails("Login canceled" ,extendedInfo.toString());
        }
        if(null != t){
            Log.i("Login",t.getMessage());
            showDetails("Login error" ,t.getMessage());
        }
    }

    private void showDetails(final String result, final String response) {
        //run on main thread
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TextView nameTextView = (TextView) findViewById(R.id.name);
                nameTextView.setText(result);
                TextView responseText = (TextView) findViewById(R.id.textViewProtectedResourceResponse);
                responseText.setText(response);
                hideProgress();
            }
        });
    }

    private void showDetailsAndPicture(final String response) {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                URL picUrl = AppId.getInstance().getUserProfilePicture();
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
                            showDetails("Hello " + AppId.getInstance().getUserIdentity().getDisplayName(), response);
                        }
                    });
                } catch (Exception e) {
                    showDetails("Login error" + e.getMessage(), response);
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
                findViewById(R.id.protectedRequestButton).setEnabled(false);
            }
        });
    }

    private void hideProgress(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                findViewById(R.id.loadingPanel).setVisibility(View.GONE);
                findViewById(R.id.loginButton).setEnabled(true);
                findViewById(R.id.protectedRequestButton).setEnabled(true);

            }
        });
    }

}
