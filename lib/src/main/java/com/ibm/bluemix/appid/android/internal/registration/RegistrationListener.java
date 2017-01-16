package com.ibm.bluemix.appid.android.internal.registration;

/**
 * Created on 1/16/17.
 */

public interface RegistrationListener {
	void onRegistrationFailure (String message);
	void onRegistrationSuccess ();
}
