/*
	Copyright 2017 IBM Corp.
	Licensed under the Apache License, Version 2.0 (the "License");
	you may not use this file except in compliance with the License.
	You may obtain a copy of the License at
	http://www.apache.org/licenses/LICENSE-2.0
	Unless required by applicable law or agreed to in writing, software
	distributed under the License is distributed on an "AS IS" BASIS,
	WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	See the License for the specific language governing permissions and
	limitations under the License.
*/
package com.ibm.mobilefirstplatform.appid;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;

import com.ibm.bluemix.appid.android.api.AppID;
import com.ibm.bluemix.appid.android.api.AuthorizationException;
import com.ibm.bluemix.appid.android.api.TokenResponseListener;
import com.ibm.bluemix.appid.android.api.tokens.AccessToken;
import com.ibm.bluemix.appid.android.api.tokens.IdentityToken;
import com.ibm.mobilefirstplatform.clientsdk.android.logger.api.Logger;


import static com.ibm.mobilefirstplatform.appid.MainActivity.SIGN_IN_CANCEL;
import static com.ibm.mobilefirstplatform.appid.MainActivity.SIGN_IN_SUCCESS;

/**
 * A login screen that offers login via userName/password.
 */

public class SignInActivity extends Activity {
    // UI references.
    private AutoCompleteTextView mUserNameView;
    private EditText mPasswordView;

    public final static int SIGN_UP_SUCCESS = 2;
    public final static int SIGN_UP_CANCEL = 3;

    private final static Logger logger = Logger.getLogger(SignInActivity.class.getName());
    private String anonymousAccessTokenRaw = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        // Set up the login form.
        mUserNameView = (AutoCompleteTextView) findViewById(R.id.userName);
        mPasswordView = (EditText) findViewById(R.id.password);
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            anonymousAccessTokenRaw = bundle.getString("anonymousAccessTokenRaw");
        }
    }

    public void attemptLogin(View v) {
        showProgress();
        String username = mUserNameView.getText().toString();
        String password = mPasswordView.getText().toString();
        AppID.getInstance().obtainTokensWithROP(getApplicationContext(), username, password, new TokenResponseListener() {
            @Override
            public void onAuthorizationFailure(AuthorizationException exception) {
                logger.info("onAuthorizationFailure: " + exception.getMessage());
                hideProgress();
                showMessage("Failure",exception.getMessage());
            }

            @Override
            public void onAuthorizationSuccess(AccessToken accessToken, IdentityToken identityToken) {
                logger.info("onAuthorizationSuccess");
                Intent intent = new Intent();
                setResult(SIGN_IN_SUCCESS, intent);
                hideProgress();
                finish();
            }
        }, anonymousAccessTokenRaw);

    }

    public void selfSignUp(View v) {
        logger.debug("selfSignUp clicked");
        Intent intent = new Intent(getApplicationContext(), SignUpActivity.class);
        startActivityForResult(intent, SIGN_UP_SUCCESS);
        setResult(SIGN_UP_SUCCESS, intent);
    }

    public void selfForgotPassword(View v) {
        logger.debug("selfForgotPassword clicked");
        Intent intent = new Intent(getApplicationContext(), ForgotPasswordActivity.class);
        startActivityForResult(intent, FORGOT_PASSWORD_SUCCESS);
        setResult(FORGOT_PASSWORD_SUCCESS, intent);
    }

    public void cancelLogin(View v) {
        logger.debug("sign in cancel");
        Intent intent = new Intent();
        setResult(SIGN_IN_CANCEL, intent);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == SIGN_UP_CANCEL) {
            logger.debug("sign up canceled");
            return;
        }
        if (resultCode == SIGN_UP_SUCCESS) {
            //if user sign up confirmation is NOT required we can show this msg instead
            //showMessage("Success", "Your profile has been successfully created");
            logger.debug("Show sign up success activity");
            Intent intent = new Intent(getApplicationContext(), SignUpSuccessActivity.class);
            intent.putExtra("formattedName", data.getExtras().getString("formattedName"));
            intent.putExtra("email", data.getExtras().getString("email"));
            startActivity(intent);
        }
    }

    private void showMessage(String status, String msg) {
        Utils.showMessage(status, msg, this);
    }

    private void showProgress() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                findViewById(R.id.sign_in_progress).setVisibility(View.VISIBLE);
                findViewById(R.id.sign_in_progress).bringToFront();
                findViewById(R.id.userName).setEnabled(false);
                findViewById(R.id.password).setEnabled(false);
                findViewById(R.id.cancelSignInButton).setEnabled(false);
                findViewById(R.id.open_sign_up_button).setEnabled(false);
                findViewById(R.id.sign_in_button).setEnabled(false);
            }
        });
    }

    private void hideProgress() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                findViewById(R.id.sign_in_progress).setVisibility(View.GONE);
                findViewById(R.id.userName).setEnabled(true);
                findViewById(R.id.password).setEnabled(true);
                findViewById(R.id.cancelSignInButton).setEnabled(true);
                findViewById(R.id.open_sign_up_button).setEnabled(true);
                findViewById(R.id.sign_in_button).setEnabled(true);
            }
        });
    }

}
