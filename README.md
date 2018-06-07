# IBM Cloud App ID Android SDK

[![Bluemix powered][img-bluemix-powered]][url-bluemix]
[![Travis][img-travis-master]][url-travis-master]
[![Coverage Status][img-coveralls-master]][url-coveralls-master]
[![Codacy Badge][img-codacy]][url-codacy]
[![Release](https://jitpack.io/v/ibm-cloud-security/appid-clientsdk-android.svg)](https://jitpack.io/#ibm-cloud-security/appid-clientsdk-android)
[![License][img-license]][url-bintray]

[![GithubWatch][img-github-watchers]][url-github-watchers]
[![GithubStars][img-github-stars]][url-github-stars]
[![GithubForks][img-github-forks]][url-github-forks]

## Requirements
* API 27 or above
* Java 8.x
* Android SDK Tools 26.1.1+
* Android SDK Platform Tools 27.0.1+
* Android Build Tools version 27.0.0+

## Installing the SDK:
1.  Add the JitPack repository to the your root `build.gradle` file at the end of the repository.

```gradle
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}
```

2. Add the dependency for the App ID client SDK:
```gradle
dependencies {
        compile 'com.github.ibm-cloud-security:appid-clientsdk-android:3.+'
}
```

3. Within your Android project in Android Studio, open the build.gradle file of your app module (not the project build.gradle), and add the following line to the defaultConfig:
```
defaultConfig {
    ...
    manifestPlaceholders = ['appIdRedirectScheme': android.defaultConfig.applicationId]
}
```

## Initializing the App ID Client SDK

Initialize the client SDK by passing the context, tenantId and region parameters to the initialize method. A common, though not mandatory, place to put the initialization code is in the `onCreate` method of the main activity in your Android application.
```java
AppID.getInstance().initialize(getApplicationContext(), <tenantId>, AppID.REGION_UK);
```
* Replace "tenantId" with the App ID service tenantId.
* Replace the AppID.REGION_UK with your App ID region (AppID.REGION_US_SOUTH, AppID.REGION_SYDNEY).

## Using the Login Widget
Use the `LoginWidget` class to start the authorization flow.   

```java
LoginWidget loginWidget = AppID.getInstance().getLoginWidget();
loginWidget.launch(this, new AuthorizationListener() {
    @Override
    public void onAuthorizationFailure (AuthorizationException exception) {
        //Exception occurred
    }

    @Override
    public void onAuthorizationCanceled () {
        //Authentication canceled by the user
    }

    @Override
    public void onAuthorizationSuccess (AccessToken accessToken, IdentityToken identityToken, RefreshToken refreshToken) {
        //User authenticated
    }
});
```
**Note**:

* The default configuration use Facebook and Google as authentication options. If you configure only one of them the login widget will *not* launch and the user will be redirected to the configured identity provider authentication screen.
* When using Cloud Directory, and "Email verification" is configured to *not* allow users to sign-in without email verification, then the "onAuthorizationSuccess" of the "AuthorizationListener" will be invoked without tokens.

## Managing Cloud Directory with the Android SDK

 Make sure to set Cloud Directory identity provider to ON in AppID dashboard, when using the following APIs.

### Sign in using Resource Owner Password
 You can obtain access token, id token and refresh token by supplying the end user's username and password.
```java
AppID.getInstance().signinWithResourceOwnerPassword(getApplicationContext(), username, password, new TokenResponseListener() {
    @Override
    public void onAuthorizationFailure (AuthorizationException exception) {
        //Exception occurred
    }

    @Override
    public void onAuthorizationSuccess (AccessToken accessToken, IdentityToken identityToken, RefreshToken refreshToken) {
        //User authenticated
    }
});
```

### Sign Up
Make sure to set **Allow users to sign up and reset their password** to **ON**, in the settings for Cloud Directory.
Use the LoginWidget class to start the sign up flow.

```java
LoginWidget loginWidget = AppID.getInstance().getLoginWidget();
loginWidget.launchSignUp(this, new AuthorizationListener() {
    @Override
    public void onAuthorizationFailure (AuthorizationException exception) {
        //Exception occurred
    }

    @Override
    public void onAuthorizationCanceled () {
        //Sign up canceled by the user
    }

    @Override
    public void onAuthorizationSuccess (AccessToken accessToken, IdentityToken identityToken, RefreshToken refreshToken) {
        if (accessToken != null && identityToken != null) {
            //User authenticated
        } else {
            //email verification is required
        }
    }
});
```
### Forgot Password
  Make sure to set **Allow users to sign up and reset their password** and **Forgot password email** to **ON**, in the settings for Cloud Directory

 Use LoginWidget class to start the forgot password flow.
```java
LoginWidget loginWidget = AppID.getInstance().getLoginWidget();
loginWidget.launchForgotPassword(this, new AuthorizationListener() {
    @Override
 	public void onAuthorizationFailure (AuthorizationException exception) {
        //Exception occurred
    }

    @Override
    public void onAuthorizationCanceled () {
        // Forogt password canceled by the user
    }

    @Override
    public void onAuthorizationSuccess (AccessToken accessToken, IdentityToken identityToken, RefreshToken refreshToken) {
        // Forgot password finished, in this case accessToken and identityToken will be null.
    }
});
```
### Change Details
  Make sure to set **Allow users to sign up and reset their password** to **ON**, in Cloud Directory settings that are in AppID dashboard. Use LoginWidget class to start the change details flow. This API can be used only when the user is logged in using Cloud Directory identity provider.

```java
LoginWidget loginWidget = AppID.getInstance().getLoginWidget();
loginWidget.launchChangeDetails(this, new AuthorizationListener() {
    @Override
    public void onAuthorizationFailure (AuthorizationException exception) {
        // Exception occurred
    }

    @Override
    public void onAuthorizationCanceled () {
        // Changed details canceled by the user
    }

    @Override
    public void onAuthorizationSuccess (AccessToken accessToken, IdentityToken identityToken, RefreshToken refreshToken) {
        // User authenticated, and fresh tokens received
    }
});
```
### Change Password
   Make sure to set **Allow users to sign up and reset their password** to **ON**, in the settings for Cloud Directory.

   Use LoginWidget class to start the change password flow. This API can be used only when the user logged in by using Cloud Directory as their identity provider.

```java
LoginWidget loginWidget = AppID.getInstance().getLoginWidget();
loginWidget.launchChangePassword(this, new AuthorizationListener() {
    @Override
    public void onAuthorizationFailure (AuthorizationException exception) {
        // Exception occurred
    }

    @Override
    public void onAuthorizationCanceled () {
        // Change password canceled by the user
    }

    @Override
    public void onAuthorizationSuccess (AccessToken accessToken, IdentityToken identityToken, RefreshToken refreshToken) {
        // User authenticated, and fresh tokens received
    }
});
```

## Anonymous Sign in
```java
AppID.getInstance().signinAnonymously(getApplicationContext(), new AuthorizationListener() {
    @Override
    public void onAuthorizationFailure(AuthorizationException exception) {
        //Exception occurred
    }

    @Override
    public void onAuthorizationCanceled() {
        //Authentication canceled by the user
    }

    @Override
    public void onAuthorizationSuccess(AccessToken accessToken, IdentityToken identityToken, RefreshToken refreshToken) {
        //User authenticated
    }
});
```

## Sign in with refresh token
It is recommended to store the refresh token locally such that it will be possible to sign in with the refresh token without requiring the user to type his credentials again.

```java
AppID.getInstance().signinWithRefreshToken(getApplicationContext(), refreshTokenString, new AuthorizationListener() {
    @Override
    public void onAuthorizationFailure(AuthorizationException exception) {
        //Exception occurred
    }

    @Override
    public void onAuthorizationCanceled() {
        //Authentication canceled by the user
    }

    @Override
    public void onAuthorizationSuccess(AccessToken accessToken, IdentityToken identityToken, RefreshToken refreshToken) {
        //User authenticated
    }
});
```

## Manage User Profiles

Using the App ID UserProfileManager, you are able to create, delete, and retrieve user profile attributes as well as get additional info about a user.

```java
AppID appId = AppID.getInstance();
String name = "name";
String value = "value";

appId.getUserProfileManager().setAttribute(name, value, new UserProfileResponseListener() {
    @Override
    public void onSuccess(JSONObject attributes) {
        // Set attribute "name" to "value" successfully
    }

    @Override
    public void onFailure(UserAttributesException e) {
        // Exception occurred
    }
});

appId.getUserProfileManager().getAttribute(name, new UserProfileResponseListener() {
    @Override
    public void onSuccess(JSONObject attributes) {
        // Got attribute "name" successfully
    }

    @Override
    public void onFailure(UserAttributesException e) {
        // Exception occurred
    }
});

appId.getUserProfileManager().getAllAttributes(new UserProfileResponseListener() {
    @Override
    public void onSuccess(JSONObject attributes) {
        // Got all attributes successfully
    }

    @Override
    public void onFailure(UserAttributesException e) {
        // Exception occurred
    }
});

appId.getUserProfileManager().deleteAttribute(name, new UserProfileResponseListener() {
    @Override
    public void onSuccess(JSONObject attributes) {
        // Attribute "name" deleted successfully
    }

    @Override
    public void onFailure(UserAttributesException e) {
        // Exception occurred
    }
});

// Retrieve user info using the latest stores access and identity tokens
appId.getUserProfileManager().getUserInfo(new UserProfileResponseListener() {
    @Override
    public void onSuccess(JSONObject userInfo) {
        // retrieved user info successfully
    }

    @Override
    public void onFailure(UserInfoException e) {
        // Exception occurred
    }
});

// Retrieve user info using your own accessToken.
// Optionally, pass an identityToken for response verification. (recommended)
appId.getUserProfileManager().getUserInfo(accessToken, identityToken, new UserProfileResponseListener() {
    @Override
    public void onSuccess(JSONObject userInfo) {
        // retrieved user info successfully
    }

    @Override
    public void onFailure(UserInfoException e) {
        // Exception occurred
    }
});
```

## Invoking protected resources
```java
BMSClient bmsClient = BMSClient.getInstance();
bmsClient.initialize(getApplicationContext(), AppID.REGION_UK);

AppIDAuthorizationManager appIdAuthMgr = new AppIDAuthorizationManager(AppID.getInstance())
bmsClient.setAuthorizationManager(appIdAuthMgr);

Request request = new Request("http://my-mobile-backend.mybluemix.net/protected", Request.GET);
request.send(this, new ResponseListener() {

@Override
public void onSuccess (Response response) {
    Log.d("Myapp", "onRegistrationSuccess :: " + response.getResponseText());
}

@Override
public void onFailure (Response response, Throwable t, JSONObject extendedInfo) {
    if (null != t) {
        Log.d("Myapp", "onRegistrationFailure :: " + t.getMessage());
    } else if (null != extendedInfo) {
        Log.d("Myapp", "onRegistrationFailure :: " + extendedInfo.toString());
    } else {
        Log.d("Myapp", "onRegistrationFailure :: " + response.getResponseText());
        }
    }
});
```

## License
This package contains code licensed under the Apache License, Version 2.0 (the "License"). You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0 and may also view the License in the LICENSE file within this package.

[img-bluemix-powered]: https://img.shields.io/badge/bluemix-powered-blue.svg
[url-bluemix]: http://bluemix.net
[url-bintray]: https://bintray.com/ibmcloudsecurity/appid-clientsdk-android
[img-license]: https://img.shields.io/github/license/ibm-cloud-security/appid-clientsdk-android.svg
[img-version]: https://img.shields.io/bintray/v/ibmcloudsecurity/maven/appid-clientsdk-android.svg

[img-github-watchers]: https://img.shields.io/github/watchers/ibm-cloud-security/appid-clientsdk-android.svg?style=social&label=Watch
[url-github-watchers]: https://github.com/ibm-cloud-security/appid-clientsdk-android/watchers
[img-github-stars]: https://img.shields.io/github/stars/ibm-cloud-security/appid-clientsdk-android.svg?style=social&label=Star
[url-github-stars]: https://github.com/ibm-cloud-security/appid-clientsdk-android/stargazers
[img-github-forks]: https://img.shields.io/github/forks/ibm-cloud-security/appid-clientsdk-android.svg?style=social&label=Fork
[url-github-forks]: https://github.com/ibm-cloud-security/appid-clientsdk-android/network

[img-travis-master]: https://travis-ci.org/ibm-cloud-security/appid-clientsdk-android.svg
[url-travis-master]: https://travis-ci.org/ibm-cloud-security/appid-clientsdk-android

[img-coveralls-master]: https://coveralls.io/repos/github/ibm-cloud-security/appid-clientsdk-android/badge.svg?branch=master
[url-coveralls-master]: https://coveralls.io/github/ibm-cloud-security/appid-clientsdk-android?branch=master

[img-codacy]: https://api.codacy.com/project/badge/Grade/be6f5f4cdae446909279d014bc650b1b
[url-codacy]: https://www.codacy.com/app/rotembr/appid-clientsdk-android?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=ibm-cloud-security/appid-clientsdk-android&amp;utm_campaign=Badge_Grad
