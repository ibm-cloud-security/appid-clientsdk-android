package com.ibm.bluemix.appid.android.api.userattributes;

public class UserAttributesException extends Exception {

	public enum Error {
		FAILED_TO_CONNECT {
			public String getDescription(){
				return "Failed to connect to the server";
			}
		},
		NOT_FOUND {
			public String getDescription(){
				return "Attribute not found";
			}
		};

		public abstract String getDescription();
	}

	private Error error;

	public UserAttributesException(Error error, String message){
		super(message);
		this.error = error;
	}

	public Error getError(){
		return error;
	}
}
