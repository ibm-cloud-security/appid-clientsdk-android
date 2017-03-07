package com.ibm.mobilefirstplatform.appid;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.ibm.bluemix.appid.android.api.AppID;
import com.ibm.bluemix.appid.android.api.AppIDAuthorizationManager;
import com.ibm.bluemix.appid.android.api.AuthorizationException;
import com.ibm.bluemix.appid.android.api.AuthorizationListener;
import com.ibm.bluemix.appid.android.api.LoginWidget;
import com.ibm.bluemix.appid.android.api.tokens.AccessToken;
import com.ibm.bluemix.appid.android.api.tokens.IdentityToken;
import com.ibm.bluemix.appid.android.api.userattributes.UserAttributeResponseListener;
import com.ibm.bluemix.appid.android.api.userattributes.UserAttributesException;
import com.ibm.mobilefirstplatform.clientsdk.android.core.api.BMSClient;
import com.ibm.mobilefirstplatform.clientsdk.android.core.api.Request;
import com.ibm.mobilefirstplatform.clientsdk.android.core.api.Response;
import com.ibm.mobilefirstplatform.clientsdk.android.core.api.ResponseListener;
import com.ibm.mobilefirstplatform.clientsdk.android.logger.api.Logger;

import org.json.JSONObject;

import java.net.URL;

public class MainActivity extends AppCompatActivity {

	private final static String mcaTenantId = "50d0beed-add7-48dd-8b0a-c818cb456bb4";
	private final static String region = ".stage1.mybluemix.net";

	private final static Logger logger = Logger.getLogger(MainActivity.class.getName());
	private AppID appId;
	private AppIDAuthorizationManager appIDAuthorizationManager;

	@Override
	protected void onCreate (Bundle savedInstanceState) {
		logger.setLogLevel(Logger.LEVEL.DEBUG);
		Logger.setSDKDebugLoggingEnabled(true);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		BMSClient bmsClient= BMSClient.getInstance();
		bmsClient.initialize(this, region);

		// Initialize AppID SDK
		appId = AppID.getInstance();
		appId.initialize(this, mcaTenantId, region);
		//uncomment to run locally
//		appId.overrideOAuthServerHost = "http://10.0.2.2:6003/oauth/v3/";
//		appId.userProfilesHost = "http://10.0.2.2:9080/user/v1/";

		// Add integration with BMSClient. Optional.
		this.appIDAuthorizationManager = new AppIDAuthorizationManager(this.appId);
		bmsClient.setAuthorizationManager(appIDAuthorizationManager);
	}

	public void onAnonLoginClick (View v) {
		logger.debug("onAnonLoginClick");
		appId.loginAnonymously(getApplicationContext(), new AuthorizationListener() {
			@Override
			public void onAuthorizationFailure(AuthorizationException exception) {
				logger.error("Anonymous authorization failure");
				if (exception != null) {
					logger.debug(exception.getLocalizedMessage(), exception);
				}
				hideProgress();
			}

			@Override
			public void onAuthorizationCanceled() {
				logger.warn("Anonymous authorization cancellation");
				hideProgress();
			}

			@Override
			public void onAuthorizationSuccess(AccessToken accessToken, IdentityToken identityToken) {
				logger.info("Anonymous authorization success");
				hideProgress();
				extractAndDisplayDataFromIdentityToken(identityToken);
			}
		});
	}

	public void onLoginClick (View v) {
		logger.debug("onLoginClick");
		showProgress();

		LoginWidget loginWidget = appId.getLoginWidget();

		loginWidget.launch(this, new AuthorizationListener() {
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
				logger.info("access_token: " + accessToken.getRaw());
				logger.info("id_token: " + identityToken.getRaw());
				logger.info("access_token isExpired: " + accessToken.isExpired());
				logger.info("id_token isExpired: " + identityToken.isExpired());
				hideProgress();
				extractAndDisplayDataFromIdentityToken(identityToken);
			}
		});
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

	public void onPutAttributeClick(View v) {
		String name = ((EditText)findViewById(R.id.editAttrName)).getText().toString();
		String value = ((EditText)findViewById(R.id.editAttrValue)).getText().toString();
		appId.getUserAttributeManager().setAttribute(name, value, new UserAttributeResponseListener() {
			@Override
			public void onSuccess(JSONObject attributes) {
				showResponse(attributes.toString());
			}

			@Override
			public void onFailure(UserAttributesException e) {
				showResponse(e.getError() + " : " + e.getMessage());
			}
		});
	}

	public void onGetAttributeClick(View v) {
		String name = ((EditText)findViewById(R.id.editAttrName)).getText().toString();
		appId.getUserAttributeManager().getAttribute(name, new UserAttributeResponseListener() {
			@Override
			public void onSuccess(JSONObject attributes) {
				showResponse(attributes.toString());
			}

			@Override
			public void onFailure(UserAttributesException e) {
				showResponse(e.getError() + " : " + e.getMessage());
			}
		});
	}

	public void onGetAllAttributesClick(View v) {
		appId.getUserAttributeManager().getAllAttributes( new UserAttributeResponseListener() {
			@Override
			public void onSuccess(JSONObject attributes) {
				showResponse(attributes.toString());
			}

			@Override
			public void onFailure(UserAttributesException e) {
				showResponse(e.getError() + " : " + e.getMessage());
			}
		});
	}

	public void onDeleteAttributeClick (View v) {
		String name = ((EditText)findViewById(R.id.editAttrName)).getText().toString();
		appId.getUserAttributeManager().deleteAttribute(name, new UserAttributeResponseListener() {
			@Override
			public void onSuccess(JSONObject attributes) {
				showResponse(attributes.toString());
			}

			@Override
			public void onFailure(UserAttributesException e) {
				showResponse(e.getError() + " : " + e.getMessage());
			}
		});
	}

	private void extractAndDisplayDataFromIdentityToken (IdentityToken identityToken) {
		String picUrl = null;
		String displayName = null;
		try {
			if (identityToken.isAnonymous()){
				picUrl = null;
				displayName = "Anonymous User ( " + identityToken.getSubject() + " )";
			} else {
				picUrl = identityToken.getPicture();
				displayName = identityToken.getName();
			}

			showPictureAndName(picUrl, displayName);
			logger.info("extractAndDisplayDataFromIdentityToken done");
		} catch (Exception e){
			logger.error("ERROR", e);
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
                    final Bitmap bmp = picUrl == null ?
							null :
							BitmapFactory.decodeStream(new URL(picUrl).openConnection().getInputStream());
                    //run on main thread
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ImageView profilePicture = (ImageView) findViewById(R.id.profilePic);
							if(bmp == null) {
								profilePicture.setImageResource(R.mipmap.ic_launcher);
							}else {
								profilePicture.setImageBitmap(bmp);
							}
                            profilePicture.requestLayout();
                            profilePicture.getLayoutParams().height = 350;
                            profilePicture.getLayoutParams().width = 350;
                            profilePicture.setScaleType(ImageView.ScaleType.FIT_XY);
                            profilePicture.setVisibility(View.VISIBLE);
							findViewById(R.id.anonloginButton).setVisibility(View.GONE);
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
