# Bluemix AppID
Android SDK for the Bluemix AppID service

[![Bluemix powered][img-bluemix-powered]][url-bluemix]
[![Travis][img-travis-master]][url-travis-master]
[![Coveralls][img-coveralls-master]][url-coveralls-master]
[![Codacy][img-codacy]][url-codacy]
[![Version][img-version]][url-bintray]
[![License][img-license]][url-bintray]

[![GithubWatch][img-github-watchers]][url-github-watchers]
[![GithubStars][img-github-stars]][url-github-stars]
[![GithubForks][img-github-forks]][url-github-forks]

## Pre Requirement:

In your Android project in Android Studio, open the build.gradle file of your app module (not the project build.gradle), and add the following line to the defaultConfig:

```
defaultConfig {
...
manifestPlaceholders = ['appIdRedirectScheme': android.defaultConfig.applicationId]
}
```

## Using the SDK:

### Initializing the AppId client SDK

Initialize the client SDK by passing the context, appId tenantId and region parameters to the createInstance method. A common, though not mandatory, place to put the createInstance code is in the onCreate method of the main activity in your Android application.

```java
String tenantId = "your-tenant-id";
String region = AppID.REGION_US_SOUTH;

AppID appId = AppID.getInstance();
appId.initialize(getApplicationContext(), tenantId, region);
```
* `tenantId` value should be retrieved from the Service Credentials tab of the AppID Dashboard
* Replace `region` with the region where your Bluemix service is hosted. you can use: `.REGION_US_SOUTH` , `REGION_UK`, `REGION_SYDNEY`.

### Using Login Widget
Use LoginWidget class to start the authorization flow.   

```java
// need code sample
```

### Access and Identity Tokens
```java
// need code sample
```

### Anonymous Login
```java
// need code sample
```

### User profile attributes
```java
// need code sample
```

### Invoking protected resources
```java
BMSClient bmsClient = BMSClient.getInstance();
bmsClient.initialize(.....);

AppIDAuthorizationManager appIdAuthMgr = new .....
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

### License
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

[img-coveralls-master]: https://coveralls.io/repos/github/ibm-cloud-security/appid-clientsdk-android/badge.svg
[url-coveralls-master]: https://coveralls.io/github/ibm-cloud-security/appid-clientsdk-android

[img-codacy]: https://api.codacy.com/project/badge/Grade/d41f8f069dd343769fcbdb55089561fc?branch=master
[url-codacy]: https://www.codacy.com/app/ibm-cloud-security/appid-clientsdk-android
