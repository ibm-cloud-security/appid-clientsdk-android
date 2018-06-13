package com.ibm.cloud.appid.android.testing.helpers;

import org.mockito.ArgumentMatcher;

public class ExceptionMessageMatcher<T extends Exception> extends ArgumentMatcher<T> {
    private String message;

    public ExceptionMessageMatcher(String message) {
        this.message = message;
    }

    @Override
    public boolean matches(Object argument) {
        return ((Exception)argument).getMessage().equals(message);
    }
}
