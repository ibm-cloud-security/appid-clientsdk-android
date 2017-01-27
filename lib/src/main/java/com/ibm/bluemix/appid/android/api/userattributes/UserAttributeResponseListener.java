package com.ibm.bluemix.appid.android.api.userattributes;

import org.json.JSONObject;

public interface UserAttributeResponseListener {
	void onSuccess(JSONObject attributes);
	void onFailure(UserAttributesException e);
}
