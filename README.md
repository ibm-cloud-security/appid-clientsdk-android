# appid-clientsdk-android

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
AppId.createInstance(this.getApplicationContext(), appIdTenantId, region);
```
* Replace 'region' with the region where your Bluemix service is hosted.
you can use: AppId.REGION_US_SOUTH , AppId.REGION_UK, AppId.REGION_SYDNEY.
* Replace 'appIdTenantId' with the tenantId.


### Login
A call to Login will pop-up the login widgit and triggers the autentication process.  

```java
AppId.getInstance().login(this, new ResponseListener() {
@Override
public void onSuccess (Response response) {
Log.d("Myapp", "onSuccess :: " + response.getResponseText());
}
@Override
public void onFailure (Response response, Throwable t, JSONObject extendedInfo) {
if (null != t) {
Log.d("Myapp", "onFailure :: " + t.getMessage());
} else if (null != extendedInfo) {
Log.d("Myapp", "onFailure :: " + extendedInfo.toString());
} else {
Log.d("Myapp", "onFailure :: " + response.getResponseText());
}
}
});
```

### getUserIdentity
After the user is autenticate, this call return the user id
```java
UserIdentity userIdentity = AppId.getInstance().getUserIdentity();
String userDisplayName = userIdentity.getDisplayName();
String userID = userIdentity.getId();
String userAuthBy = userIdentity.getAuthBy();
```

### getUserProfilePicture
Return a string represent the authenticated user profile picture URL.
```Java
String picUrl = AppId.getInstance().getUserProfilePicture();
```

### getCachedAuthorizationHeader
Return the cached authoriation header if there is one.
```Java
String picUrl = AppId.getInstance().getCachedAuthorizationHeader();
```

#### Protected resources also supported: 
upon request to protected resource the authentication process will trigger, and if there is no authenticate user the login widgit will pop-up.
```java
Request request = new Request("http://my-mobile-backend.mybluemix.net/protected", Request.GET);
request.send(this, new ResponseListener() {
@Override
public void onSuccess (Response response) {
Log.d("Myapp", "onSuccess :: " + response.getResponseText());
}
@Override
public void onFailure (Response response, Throwable t, JSONObject extendedInfo) {
if (null != t) {
Log.d("Myapp", "onFailure :: " + t.getMessage());
} else if (null != extendedInfo) {
Log.d("Myapp", "onFailure :: " + extendedInfo.toString());
} else {
Log.d("Myapp", "onFailure :: " + response.getResponseText());
}
}
});
```
