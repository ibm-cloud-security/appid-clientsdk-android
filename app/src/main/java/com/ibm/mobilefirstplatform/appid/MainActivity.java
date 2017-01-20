package com.ibm.mobilefirstplatform.appid;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.ibm.bluemix.appid.android.api.AppID;
import com.ibm.bluemix.appid.android.api.AppIDAuthorizationManager;
import com.ibm.bluemix.appid.android.api.AuthorizationException;
import com.ibm.bluemix.appid.android.api.AuthorizationListener;
import com.ibm.bluemix.appid.android.api.LoginWidget;
import com.ibm.bluemix.appid.android.api.tokens.AccessToken;
import com.ibm.bluemix.appid.android.api.tokens.IdentityToken;
import com.ibm.mobilefirstplatform.clientsdk.android.core.api.BMSClient;
import com.ibm.mobilefirstplatform.clientsdk.android.core.api.Request;
import com.ibm.mobilefirstplatform.clientsdk.android.core.api.Response;
import com.ibm.mobilefirstplatform.clientsdk.android.core.api.ResponseListener;
import com.ibm.mobilefirstplatform.clientsdk.android.logger.api.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URL;

public class MainActivity extends AppCompatActivity {

	private final static String mcaTenantId = "76ac844c-075c-41b3-b95e-86629713b6a2"; //"11111111-1111-1111-1111-111111111bbb";
	private final static String region = ".stage1.mybluemix.net";//".stage1-dev.ng.bluemix.net";//AppId.REGION_UK;

	private final static Logger logger = Logger.getLogger(MainActivity.class.getName());
	private BMSClient bmsClient;
	private AppID appId;
	private AppIDAuthorizationManager appIDAuthorizationManager;

	@Override
	protected void onCreate (Bundle savedInstanceState) {
		logger.setLogLevel(Logger.LEVEL.DEBUG);
		Logger.setSDKDebugLoggingEnabled(true);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		bmsClient = BMSClient.getInstance();
		bmsClient.initialize(this, region);

		this.appId = new AppID(getApplicationContext(), mcaTenantId, region);
		this.appIDAuthorizationManager = new AppIDAuthorizationManager(this.appId);

		bmsClient.setAuthorizationManager(appIDAuthorizationManager);
	}

	public void onLoginClick (View v) {
		logger.debug("onLoginClick");
		showProgress();
		LoginWidget loginWidget = new LoginWidget(appId, new AuthorizationListener() {
			@Override
			public void onAuthorizationFailure (AuthorizationException exception) {
				logger.info("onAuthorizationFailure");
				hideProgress();
			}

			@Override
			public void onAuthorizationCanceled () {
				logger.info("onAuthorizationCanceled");
				hideProgress();
			}

			@Override
			public void onAuthorizationSuccess (AccessToken accessToken, IdentityToken identityToken) {
				logger.info("onAuthorizationSuccess");
				logger.info("access_token: " + accessToken.getPayload().toString());
				logger.info("id_token: " + identityToken.getPayload().toString());
				hideProgress();
				extractAndDisplayDataFromIdentityToken(identityToken);
			}
		});
		loginWidget.launch(this);
	}

	public void onProtectedRequestClick (View v) {
		showResponse("");
		showProgress();
		Request r = new Request("http://appid-rotem.stage1.mybluemix.net/protectedResource", Request.GET);
		r.send(this, new ResponseListener() {
			@Override
			public void onSuccess (Response response) {
				logger.info("Request onSuccess");
				hideProgress();
				IdentityToken identityToken = appIDAuthorizationManager.getIdentityToken();
				extractAndDisplayDataFromIdentityToken(identityToken);
				showResponse(response.getResponseText());
			}

			@Override
			public void onFailure (Response response, Throwable t, JSONObject extendedInfo) {
				logger.error("Request onFailure");
				hideProgress();
			}
		});
	}

	private void extractAndDisplayDataFromIdentityToken (IdentityToken identityToken) {

		try {
			String picUrl = identityToken
					.getPayload()
					.getJSONObject("imf.user")
					.getJSONObject("attributes")
					.getJSONObject("picture")
					.getJSONObject("data")
					.getString("url");

			final String displayName = identityToken
					.getPayload()
					.getJSONObject("imf.user")
					.getString("displayName");
			showPictureAndName(picUrl, displayName);
		} catch (JSONException e){

		}
	}



	private void showResponse (final String result) {
		//run on main thread
		runOnUiThread(new Runnable() {
			@Override
			public void run () {
				TextView nameTextView = (TextView) findViewById(R.id.textViewProtectedResourceResponse);
				nameTextView.setText(result);
				hideProgress();
			}
		});
	}


    private void showPictureAndName (final String picUrl, final String displayName) {
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try {

                    final Bitmap bmp = BitmapFactory.decodeStream(new URL(picUrl).openConnection().getInputStream());
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

							TextView nameTextView = (TextView) findViewById(R.id.name);
							nameTextView.setText(displayName);
                        }
                    });
                } catch (Exception e) {
                    showResponse("Login error" + e.getMessage());
                    e.printStackTrace();
                }
            }
        });
    }

	private void showProgress () {
		runOnUiThread(new Runnable() {
			@Override
			public void run () {
				findViewById(R.id.loadingPanel).setVisibility(View.VISIBLE);
				findViewById(R.id.loadingPanel).bringToFront();
				findViewById(R.id.loginButton).setEnabled(false);
				findViewById(R.id.protectedRequestButton).setEnabled(false);
			}
		});
	}

	private void hideProgress () {
		runOnUiThread(new Runnable() {
			@Override
			public void run () {
				findViewById(R.id.loadingPanel).setVisibility(View.GONE);
				findViewById(R.id.loginButton).setEnabled(true);
				findViewById(R.id.protectedRequestButton).setEnabled(true);
			}
		});
	}
}
