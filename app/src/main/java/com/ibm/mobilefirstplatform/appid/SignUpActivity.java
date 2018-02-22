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

import com.ibm.bluemix.appid.android.internal.network.AppIDRequest;
import com.ibm.bluemix.appid.android.internal.network.AppIDRequestFactory;
import com.ibm.mobilefirstplatform.clientsdk.android.core.api.Response;
import com.ibm.mobilefirstplatform.clientsdk.android.core.api.ResponseListener;
import com.ibm.mobilefirstplatform.clientsdk.android.logger.api.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import static com.ibm.mobilefirstplatform.appid.SignInActivity.SIGN_UP_CANCEL;
import static com.ibm.mobilefirstplatform.appid.SignInActivity.SIGN_UP_SUCCESS;

public class SignUpActivity extends Activity {

    private AutoCompleteTextView mFirstNameView;
    private AutoCompleteTextView mLastNameView;
    private AutoCompleteTextView mPhoneView;
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private EditText mRePasswordView;

    private AppIDRequestFactory appIDRequestFactory = new AppIDRequestFactory();
    private final static Logger logger = Logger.getLogger(SignUpActivity.class.getName());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        // Set up the sign up form.
        mFirstNameView = (AutoCompleteTextView) findViewById(R.id.firstName);
        mLastNameView = (AutoCompleteTextView) findViewById(R.id.lastName);
        mPhoneView = (AutoCompleteTextView) findViewById(R.id.phoneNumber);
        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);
        mPasswordView = (EditText) findViewById(R.id.password);
        mRePasswordView = (EditText) findViewById(R.id.rePassword);
    }

    public void attemptSignUp(View v) {
        showProgress();
        JSONObject profileObject = new JSONObject();
        try {
            profileObject.put("firstName", mFirstNameView.getText().toString());
            profileObject.put("lastName", mLastNameView.getText().toString());
            profileObject.put("phoneNumber", mPhoneView.getText().toString());
            profileObject.put("email", mEmailView.getText().toString());
            profileObject.put("password", mPasswordView.getText().toString());
            profileObject.put("confirmed_password", mRePasswordView.getText().toString());
        } catch (JSONException e) {
            logger.error("Error while getting user sign up input");
            e.printStackTrace();
            hideProgress();
            showMessage("Failure", "Bad user input");
        }

        AppIDRequest request = appIDRequestFactory.createRequest("http://10.0.2.2:1234/sign_up/mobile/submit", AppIDRequest.POST);
        request.send(profileObject, new ResponseListener() {
            @Override
            public void onSuccess(Response response) {
                try {
                    logger.debug("sign up request onSuccess");
                    hideProgress();
                    String formattedName = response.getResponseJSON().getJSONObject("name").getString("formatted");
                    String email =  response.getResponseJSON().getJSONArray("emails").getJSONObject(0).getString("value");
                    Intent intent = new Intent();
                    intent.putExtra("formattedName", formattedName);
                    intent.putExtra("email", email);
                    setResult(SIGN_UP_SUCCESS, intent);
                    finish();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Response response, Throwable t, JSONObject extendedInfo) {
                logger.error("sign up request onFailure");
                if (response != null) {
                    logger.error("response: " + response.toString());
                } else if (extendedInfo != null) {
                    logger.error("extendedInfo: " + extendedInfo.toString());
                } else if (t != null) {
                    logger.error("exception: " + t.getMessage());
                } else {
                    logger.error("Request Failure");
                }
                hideProgress();
                if (response.getStatus() >= 400 && response.getStatus() < 500) {
                    showMessage("Failure", response.getResponseText());
                } else {
                    showMessage("Failure", "Something went wrong, try again later");
                }
            }
        });
    }

    public void cancelSignUp(View v) {
        Intent intent = new Intent();
        setResult(SIGN_UP_CANCEL, intent);
        finish();
    }

    private void showMessage(String status, String msg) {
        Utils.showMessage(status, msg, this);
    }

    private void showProgress() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                findViewById(R.id.sign_up_progress).setVisibility(View.VISIBLE);
                findViewById(R.id.sign_up_progress).bringToFront();
                findViewById(R.id.firstName).setEnabled(false);
                findViewById(R.id.lastName).setEnabled(false);
                findViewById(R.id.email).setEnabled(false);
                findViewById(R.id.phoneNumber).setEnabled(false);
                findViewById(R.id.password).setEnabled(false);
                findViewById(R.id.rePassword).setEnabled(false);
                findViewById(R.id.signUpButtonSubmit).setEnabled(false);
                findViewById(R.id.cancelSignUpButton).setEnabled(false);
            }
        });
    }

    private void hideProgress() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                findViewById(R.id.sign_up_progress).setVisibility(View.GONE);
                findViewById(R.id.firstName).setEnabled(true);
                findViewById(R.id.lastName).setEnabled(true);
                findViewById(R.id.email).setEnabled(true);
                findViewById(R.id.phoneNumber).setEnabled(true);
                findViewById(R.id.password).setEnabled(true);
                findViewById(R.id.rePassword).setEnabled(true);
                findViewById(R.id.signUpButtonSubmit).setEnabled(true);
                findViewById(R.id.cancelSignUpButton).setEnabled(true);
            }
        });
    }
}
