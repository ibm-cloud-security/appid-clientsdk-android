package com.ibm.mobilefirstplatform.appid;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.ibm.bluemix.appid.android.api.AppID;
import com.ibm.bluemix.appid.android.api.AppIDAuthorizationManager;
import com.ibm.bluemix.appid.android.api.AuthorizationException;
import com.ibm.bluemix.appid.android.api.AuthorizationListener;
import com.ibm.bluemix.appid.android.api.LoginWidget;
import com.ibm.bluemix.appid.android.api.TokenResponseListener;
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

    private final static String tenantId = "AppID_tenantId";
    private final static String region = AppID.REGION_US_SOUTH; //AppID.REGION_UK ,AppID.REGION_SYDNEY
    private final static String protectedUrl = "protected_URL";

    private final static Logger logger = Logger.getLogger(MainActivity.class.getName());
    private AppID appId;
    private AppIDAuthorizationManager appIDAuthorizationManager;
    private AccessToken anonymousAccessToken;
    private AccessToken identifiedAccessToken;
    private AccessToken useThisToken;

    public final static int LOGIN_SUBMITTED = 2;
    public final static int LOGIN_CANCEL = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        logger.setLogLevel(Logger.LEVEL.DEBUG);
        Logger.setSDKDebugLoggingEnabled(true);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BMSClient bmsClient = BMSClient.getInstance();
        bmsClient.initialize(this, region);
        // Initialize AppID SDK
        appId = AppID.getInstance();
        appId.initialize(this, tenantId, region);
        // Add integration with BMSClient. Optional.
        this.appIDAuthorizationManager = new AppIDAuthorizationManager(this.appId);
        bmsClient.setAuthorizationManager(appIDAuthorizationManager);
    }

    public void onAnonLoginClick(View v) {
        logger.debug("onAnonLoginClick");
        showProgress();
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
                anonymousAccessToken = accessToken;
                extractAndDisplayDataFromIdentityToken(identityToken);
            }
        });
    }

    public void onTokensClick(View v) {
        startTokensActivity(appIDAuthorizationManager.getIdentityToken(), appIDAuthorizationManager.getAccessToken());
    }

    public void onLoginClick(View v) {
        logger.debug("onLoginClick");
        showProgress();
        LoginWidget loginWidget = appId.getLoginWidget();
        loginWidget.launch(this, new AuthorizationListener() {
            @Override
            public void onAuthorizationFailure(AuthorizationException exception) {
                logger.info("onAuthorizationFailure: " + exception.getMessage());
                showResponse(exception.getMessage());
                hideProgress();
            }

            @Override
            public void onAuthorizationCanceled() {
                logger.info("onAuthorizationCanceled");
                hideProgress();
            }

            @Override
            public void onAuthorizationSuccess(AccessToken accessToken, IdentityToken identityToken) {
                logger.info("onAuthorizationSuccess");
                logger.info("access_token: " + accessToken.getRaw());
                logger.info("id_token: " + identityToken.getRaw());
                logger.info("access_token isExpired: " + accessToken.isExpired());
                logger.info("id_token isExpired: " + identityToken.isExpired());
                identifiedAccessToken = accessToken;
                extractAndDisplayDataFromIdentityToken(identityToken);
            }
        }, anonymousAccessToken != null ? anonymousAccessToken.getRaw() : null);
    }

    public void onGetTokenUsingRoP(View v) {
        logger.debug("onGetTokenUsingRoP");
        showResponse("");
        showProgress();
        Intent inent;
        inent = new Intent(getApplicationContext(), SignInActivity.class);
        startActivityForResult(inent, LOGIN_SUBMITTED);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == LOGIN_CANCEL) {
            hideProgress();
            logger.info("onGetTokenUsingRoP canceled");
            return;
        }
        if (resultCode == LOGIN_SUBMITTED && data != null) {
            String username = data.getStringExtra("username");
            String password = data.getStringExtra("password");
            appId.obtainTokensWithROP(getApplicationContext(), username, password, new TokenResponseListener() {
                @Override
                public void onAuthorizationFailure(AuthorizationException exception) {
                    logger.info("onAuthorizationFailure: " + exception.getMessage());
                    showResponse(exception.getMessage());
                    hideProgress();
                }

                @Override
                public void onAuthorizationSuccess(AccessToken accessToken, IdentityToken identityToken) {
                    logger.info("onAuthorizationSuccess");
                    logger.info("access_token: " + accessToken.getRaw());
                    logger.info("id_token: " + identityToken.getRaw());
                    logger.info("access_token isExpired: " + accessToken.isExpired());
                    logger.info("id_token isExpired: " + identityToken.isExpired());
                    identifiedAccessToken = accessToken;
                    extractAndDisplayDataFromIdentityToken(identityToken);
                }
            }, anonymousAccessToken != null ? anonymousAccessToken.getRaw() : null);
        }
    }

    public void onProtectedRequestClick(View v) {
        showResponse("");
        showProgress();
        Request r = new Request(protectedUrl, Request.GET);
        r.send(this, new ResponseListener() {
            @Override
            public void onSuccess(Response response) {
                logger.info("Request onSuccess");
                hideProgress();
                IdentityToken identityToken = appIDAuthorizationManager.getIdentityToken();
                extractAndDisplayDataFromIdentityToken(identityToken);
                showResponse(response.getResponseText());
            }

            @Override
            public void onFailure(Response response, Throwable t, JSONObject extendedInfo) {
                logger.error("Request onFailure");
                if (response != null) {
                    showResponse("response: " + response.toString());
                } else if (extendedInfo != null) {
                    showResponse("extendedInfo: " + extendedInfo.toString());
                } else if (t != null) {
                    showResponse("exception: " + t.getMessage());
                } else {
                    showResponse("Request Failure");
                }
                hideProgress();
            }
        });
    }

    public void onPutAttributeClick(View v) {
        String name = ((EditText) findViewById(R.id.editAttrName)).getText().toString();
        String value = ((EditText) findViewById(R.id.editAttrValue)).getText().toString();
        appId.getUserAttributeManager().setAttribute(name, value, useThisToken, new UserAttributeResponseListener() {
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
        String name = ((EditText) findViewById(R.id.editAttrName)).getText().toString();
        appId.getUserAttributeManager().getAttribute(name, useThisToken, new UserAttributeResponseListener() {
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
        String name = ((EditText) findViewById(R.id.editAttrName)).getText().toString();
        appId.getUserAttributeManager().getAllAttributes(useThisToken, new UserAttributeResponseListener() {
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

    public void onDeleteAttributeClick(View v) {
        String name = ((EditText) findViewById(R.id.editAttrName)).getText().toString();
        appId.getUserAttributeManager().deleteAttribute(name, useThisToken, new UserAttributeResponseListener() {
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

    private void extractAndDisplayDataFromIdentityToken(IdentityToken identityToken) {
        String picUrl = null;
        String displayName = null;
        try {
            String userId = identityToken.getSubject();
            if (identityToken.isAnonymous()) {
                picUrl = null;
                displayName = "Anonymous User ( " + userId + " )";
            } else {
                picUrl = identityToken.getPicture();
                displayName = identityToken.getName() + " ( " + userId + " )";
            }
            logger.info("User is: " + userId);
            showPictureAndName(picUrl, displayName);
            logger.info("extractAndDisplayDataFromIdentityToken done");
        } catch (Exception e) {
            logger.error("ERROR", e);
        }
    }

    private void startTokensActivity(IdentityToken identityToken, AccessToken accessToken) {
        Intent intent = new Intent(this, TokensActivity.class);
        String idToken = identityToken != null && identityToken.getPayload() != null ? identityToken.getPayload().toString() : "";
        String accToken = accessToken != null && accessToken.getPayload() != null ? accessToken.getPayload().toString() : "";
        intent.putExtra("IDENTITY_TOKEN", idToken);
        intent.putExtra("ACCESS_TOKEN", accToken);
        startActivity(intent);
    }


    private void showResponse(final String result) {
        //run on main thread
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                TextView nameTextView = (TextView) findViewById(R.id.textViewProtectedResourceResponse);
                nameTextView.setText(result);
                hideProgress();
            }
        });
    }


    private void showPictureAndName(final String picUrl, final String displayName) {
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
                            if (bmp == null) {
                                profilePicture.setImageResource(R.drawable.ic_anon_user);
                            } else {
                                profilePicture.setImageBitmap(bmp);
                            }
                            profilePicture.requestLayout();
                            profilePicture.getLayoutParams().height = 300;
                            profilePicture.getLayoutParams().width = 300;
                            profilePicture.setScaleType(ImageView.ScaleType.FIT_XY);
                            profilePicture.setVisibility(View.VISIBLE);
                            TextView nameTextView = (TextView) findViewById(R.id.name);
                            nameTextView.setText(displayName);
                            hideProgress();
                        }
                    });
                } catch (Exception e) {
                    showResponse("Login error" + e.getMessage());
                    e.printStackTrace();
                    hideProgress();
                }
            }
        });
    }

    private void showProgress() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                findViewById(R.id.loadingPanel).setVisibility(View.VISIBLE);
                findViewById(R.id.loadingPanel).bringToFront();
                findViewById(R.id.loginButton).setEnabled(false);
                findViewById(R.id.ropButton).setEnabled(false);
                findViewById(R.id.protectedRequestButton).setEnabled(false);
                findViewById(R.id.deleteAttribute).setEnabled(false);
                findViewById(R.id.anonloginButton).setEnabled(false);
                findViewById(R.id.getAllAttributes).setEnabled(false);
                findViewById(R.id.getAttribute).setEnabled(false);
                findViewById(R.id.showTokensButton).setEnabled(false);
                findViewById(R.id.putAttribute).setEnabled(false);
                findViewById(R.id.editAttrName).setEnabled(false);
                findViewById(R.id.editAttrValue).setEnabled(false);
            }
        });
    }

    private void hideProgress() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                findViewById(R.id.loadingPanel).setVisibility(View.GONE);
                findViewById(R.id.loginButton).setEnabled(true);
                findViewById(R.id.ropButton).setEnabled(true);
                findViewById(R.id.protectedRequestButton).setEnabled(true);
                findViewById(R.id.deleteAttribute).setEnabled(true);
                findViewById(R.id.anonloginButton).setEnabled(true);
                findViewById(R.id.getAllAttributes).setEnabled(true);
                findViewById(R.id.getAttribute).setEnabled(true);
                findViewById(R.id.showTokensButton).setEnabled(true);
                findViewById(R.id.putAttribute).setEnabled(true);
                findViewById(R.id.editAttrName).setEnabled(true);
                findViewById(R.id.editAttrValue).setEnabled(true);
            }
        });
    }


    public void onRadioButtonClicked(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();

        // Check which radio button was clicked
        String token = "No token";
        switch (view.getId()) {
            case R.id.radio_last:
                if (checked)
                    useThisToken = null;
                token = appIDAuthorizationManager.getAccessToken() != null ?
                        appIDAuthorizationManager.getAccessToken().getRaw() : "No token";
                ((TextView) findViewById(R.id.textViewProtectedResourceResponse)).setText(token);
                break;
            case R.id.radio_anon:
                if (checked)
                    useThisToken = anonymousAccessToken;
                token = useThisToken != null ?
                        useThisToken.getRaw() : "No token";
                ((TextView) findViewById(R.id.textViewProtectedResourceResponse)).setText(token);
                break;
            case R.id.radio_id:
                if (checked)
                    useThisToken = identifiedAccessToken;
                token = useThisToken != null ?
                        useThisToken.getRaw() : "No token";
                ((TextView) findViewById(R.id.textViewProtectedResourceResponse)).setText(token);
                break;
        }
    }
}
