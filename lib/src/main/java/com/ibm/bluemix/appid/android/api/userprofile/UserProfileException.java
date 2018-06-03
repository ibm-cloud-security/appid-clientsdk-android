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

package com.ibm.bluemix.appid.android.api.userprofile;

public class UserProfileException extends Exception {

	public enum Error {
		INVALID_USERINFO_RESPONSE {
			public String getDescription(){
				return "User info response is invalid. Must be valid JSON and contain the required subject field";
			}
		},
		CONFLICTING_SUBJECTS {
			public String getDescription(){
				return "Conflicting subjects. UserInfoResponse.sub must match idToken.sub";
			}
		},
		MISSING_ACCESS_TOKEN {
			public String getDescription(){
				return "Access Token missing. User info request requires an access token";
			}
		},
		FAILED_TO_CONNECT {
			public String getDescription(){
				return "Failed to connect to the server";
			}
		},
		NOT_FOUND {
			public String getDescription(){
				return "Attribute not found";
			}
		},
		UNAUTHORIZED {
			public String getDescription(){
				return "Access to profile is unauthorized";
			}
		},
		JSON_PARSE_ERROR {
			public String getDescription(){
				return "Response text is not a valid JSON format";
			}
		};

		public abstract String getDescription();
	}

	private Error error;

	public UserProfileException(Error error){
		super(error.getDescription());
		this.error = error;
	}

	public Error getError(){
		return error;
	}
}
