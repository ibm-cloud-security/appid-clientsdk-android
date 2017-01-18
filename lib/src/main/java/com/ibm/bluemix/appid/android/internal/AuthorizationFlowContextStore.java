package com.ibm.bluemix.appid.android.internal;

import com.ibm.bluemix.appid.android.internal.authorization.AuthorizationFlowContext;

import java.util.HashMap;
import java.util.Map;

public class AuthorizationFlowContextStore {

	private static Map<String, AuthorizationFlowContext> store = new HashMap();

	public synchronized static void push(String guid, AuthorizationFlowContext ctx){
		store.put(guid, ctx);
	}

	public synchronized static AuthorizationFlowContext remove(String guid){
		return store.remove(guid);
	}

}
