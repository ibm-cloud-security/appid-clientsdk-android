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
		},
		UNAUTHORIZED {
			public String getDescription(){
				return "Access to attribute is unauthorized";
			}
		};

		public abstract String getDescription();
	}

	private Error error;

	public UserAttributesException(Error error){
		super(error.getDescription());
		this.error = error;
	}

	public Error getError(){
		return error;
	}
}
