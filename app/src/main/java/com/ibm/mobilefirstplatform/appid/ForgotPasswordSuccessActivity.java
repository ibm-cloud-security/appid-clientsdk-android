package com.ibm.mobilefirstplatform.appid;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.ibm.bluemix.appid.android.internal.network.AppIDRequest;
import com.ibm.bluemix.appid.android.internal.network.AppIDRequestFactory;
import com.ibm.mobilefirstplatform.clientsdk.android.core.api.Response;
import com.ibm.mobilefirstplatform.clientsdk.android.core.api.ResponseListener;

import org.json.JSONException;
import org.json.JSONObject;

import com.ibm.mobilefirstplatform.clientsdk.android.logger.api.Logger;

import static com.ibm.mobilefirstplatform.appid.MainActivity.MY_BACKEND_URL;

public class ForgotPasswordSuccessActivity extends AppCompatActivity {

    private String email, uuid;
    private String formattedName;
    private AppIDRequestFactory appIDRequestFactory = new AppIDRequestFactory();
    private final static Logger logger = Logger.getLogger(ForgotPasswordSuccessActivity.class.getName());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password_success);

        formattedName = getIntent().getExtras().getString("formattedName");
        email = getIntent().getExtras().getString("email");
        uuid = getIntent().getExtras().getString("uuid");

        TextView nameTextView = (TextView) findViewById(R.id.formattedName);
        nameTextView.setText(formattedName);
    }

    public void backToLogin(View v) {
        finish();
    }

    public void resendEmail(View v) {
        logger.debug("resending confirmation email: " + email);
        showProgress();
        JSONObject body = new JSONObject();
        try {
            body.put("uuid", uuid);
        } catch (JSONException e) {
            logger.error("Error while getting user resend input");
            e.printStackTrace();
            hideProgress();
            showMessage("Failure", "Bad email address");
        }

        AppIDRequest request = appIDRequestFactory.createRequest(MY_BACKEND_URL + "/resend/RESET_PASSWORD", AppIDRequest.POST);
        request.send(body, new ResponseListener() {
            @Override
            public void onSuccess(Response response) {
                logger.debug("resend email request onSuccess");
                hideProgress();
                showMessage("Resend", response.getResponseText());
            }

            @Override
            public void onFailure(Response response, Throwable t, JSONObject extendedInfo) {
                logger.error("resend email request onFailure");
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
                showMessage("Failure", "Something went wrong, try again later");
            }
        });
    }

    private void showMessage(String status, String msg) {
        Utils.showMessage(status, msg, this);
    }

    private void showProgress() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                findViewById(R.id.resend_email_progress).setVisibility(View.VISIBLE);
                findViewById(R.id.resend_email_progress).bringToFront();
                findViewById(R.id.backTologin).setEnabled(false);
                findViewById(R.id.resendEmail).setEnabled(false);
            }
        });
    }

    private void hideProgress() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                findViewById(R.id.resend_email_progress).setVisibility(View.GONE);
                findViewById(R.id.backTologin).setEnabled(true);
                findViewById(R.id.resendEmail).setEnabled(true);
            }
        });
    }
}
