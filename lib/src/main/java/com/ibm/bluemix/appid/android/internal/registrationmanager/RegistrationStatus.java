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

package com.ibm.bluemix.appid.android.internal.registrationmanager;

public enum RegistrationStatus
{
    NOT_REGISTRED {
        public String getDescription() {
            return "OAuth client not registered";
        }
    },
    REGISTERED_SUCCESSFULLY {
        public String getDescription(){
            return "OAuth client successfully registered";
        }

    },
    FAILED_TO_REGISTER {
        public String getDescription(){
            return "Failed to register OAuth client";
        }
    },
    FAILED_TO_SAVE_REGISTRATION_DATA {
        public String getDescription(){
            return "Failed to save OAuth client registration data";
        }
    },
    FAILED_TO_CREATE_REGISTRATION_PARAMETERS {
        public String getDescription(){
            return "Failed to create registration parameters";
        }
    };

    public abstract String getDescription();
}
