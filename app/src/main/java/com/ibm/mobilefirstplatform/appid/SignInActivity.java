package com.ibm.mobilefirstplatform.appid;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;

import static com.ibm.mobilefirstplatform.appid.MainActivity.LOGIN_CANCEL;
import static com.ibm.mobilefirstplatform.appid.MainActivity.LOGIN_SUBMITTED;

/**
 * A login screen that offers login via userName/password.
 */

public class SignInActivity extends Activity {
    // UI references.
    private AutoCompleteTextView muserNameView;
    private EditText mPasswordView;
    private View mLoginFormView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.sign_in);
        // Set up the login form.
        muserNameView = (AutoCompleteTextView) findViewById(R.id.userName);
        mPasswordView = (EditText) findViewById(R.id.password);
        mLoginFormView = findViewById(R.id.login_form);
    }

    public void attemptLogin(View v) {
        String userName = muserNameView.getText().toString();
        String password = mPasswordView.getText().toString();
        Intent inent = new Intent();
        inent.putExtra("username",userName);
        inent.putExtra("password", password);
        setResult(LOGIN_SUBMITTED, inent);
        finish();
    }

    public void cancelLogin(View v){
        Intent inent = new Intent();
        setResult(LOGIN_CANCEL, inent);
        finish();
    }
}
