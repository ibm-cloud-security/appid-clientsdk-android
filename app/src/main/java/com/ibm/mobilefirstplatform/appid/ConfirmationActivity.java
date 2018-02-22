package com.ibm.mobilefirstplatform.appid;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.ibm.mobilefirstplatform.clientsdk.android.logger.api.Logger;

public class ConfirmationActivity extends AppCompatActivity {
    private final static Logger logger = Logger.getLogger(ConfirmationActivity.class.getName());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirmation);

        Uri encodeUri = this.getIntent().getData();
        String decodeUrl = Uri.decode(encodeUri.toString());

        Uri uri = Uri.parse(decodeUrl);

        logger.debug("uri:" + uri);

        String uuid = uri.getQueryParameter("uuid");
        String language = uri.getQueryParameter("language");
        String errorStatusCode = uri.getQueryParameter("errorStatusCode");
        String errorDescription = uri.getQueryParameter("errorDescription");
        if (null != errorDescription) {
            TextView textViewSignUpResult = (TextView)findViewById(R.id.textViewSignUpResult);
            textViewSignUpResult.setText(errorDescription);
        }
    }
}
