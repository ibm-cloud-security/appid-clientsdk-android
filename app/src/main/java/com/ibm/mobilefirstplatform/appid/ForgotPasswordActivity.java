package com.ibm.mobilefirstplatform.appid;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AutoCompleteTextView;

import com.ibm.bluemix.appid.android.internal.network.AppIDRequest;
import com.ibm.bluemix.appid.android.internal.network.AppIDRequestFactory;
import com.ibm.mobilefirstplatform.clientsdk.android.core.api.Response;
import com.ibm.mobilefirstplatform.clientsdk.android.core.api.ResponseListener;
import com.ibm.mobilefirstplatform.clientsdk.android.logger.api.Logger;

import org.json.JSONException;
import org.json.JSONObject;

import static com.ibm.mobilefirstplatform.appid.MainActivity.MY_BACKEND_URL;
import static com.ibm.mobilefirstplatform.appid.SignInActivity.FORGOR_PASSWORD_CANCEL;
import static com.ibm.mobilefirstplatform.appid.SignInActivity.FORGOT_PASSWORD_SUCCESS;

public class ForgotPasswordActivity extends Activity {

    private AutoCompleteTextView mEmailView;
    private AppIDRequestFactory appIDRequestFactory = new AppIDRequestFactory();
    private final static Logger logger = Logger.getLogger(SignUpActivity.class.getName());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);
        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);
    }

    public void attemptForgotPassword(View v) {
        showProgress();
        JSONObject body = new JSONObject();
        try {
            body.put("email", mEmailView.getText().toString());
        } catch (JSONException e) {
            logger.error("Error while getting user sign up input");
            e.printStackTrace();
            hideProgress();
            showMessage("Failure", "Bad user input");
        }

        AppIDRequest request = appIDRequestFactory.createRequest(MY_BACKEND_URL + "/forgot_password/submit/mobile", AppIDRequest.POST);
        request.send(body, new ResponseListener() {
            @Override
            public void onSuccess(Response response) {
                try {
                    logger.debug("forgot password request onSuccess");
                    hideProgress();
                    String formattedName = response.getResponseJSON().getJSONObject("name").getString("formatted");
                    String email =  response.getResponseJSON().getJSONArray("emails").getJSONObject(0).getString("value");
                    String uuid = response.getResponseJSON().getString("id");
                    Intent intent = new Intent();
                    intent.putExtra("formattedName", formattedName);
                    intent.putExtra("email", email);
                    intent.putExtra("uuid", uuid);
                    setResult(FORGOT_PASSWORD_SUCCESS, intent);
                    finish();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Response response, Throwable t, JSONObject extendedInfo) {
                logger.error("forgot password request onFailure");
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

    public void cancelForgotPassword(View v) {
        Intent intent = new Intent();
        setResult(FORGOR_PASSWORD_CANCEL, intent);
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
                findViewById(R.id.email).setEnabled(false);
                findViewById(R.id.forgotPasswordButtonSubmit).setEnabled(false);
                findViewById(R.id.cancelForgotPasswordButton).setEnabled(false);
            }
        });
    }

    private void hideProgress() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                findViewById(R.id.sign_up_progress).setVisibility(View.GONE);
                findViewById(R.id.email).setEnabled(true);
                findViewById(R.id.forgotPasswordButtonSubmit).setEnabled(true);
                findViewById(R.id.cancelForgotPasswordButton).setEnabled(true);
            }
        });
    }
}
