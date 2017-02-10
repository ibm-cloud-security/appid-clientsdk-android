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
