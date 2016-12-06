package com.ibm.mobilefirstplatform.appid_clientsdk_android;

/**
 * Created by rotembr on 06/12/2016.
 */

import android.content.Context;
import android.provider.Settings;
import android.util.Base64;

import com.ibm.mobilefirstplatform.clientsdk.android.core.api.Request;
import com.ibm.mobilefirstplatform.clientsdk.android.core.api.Response;
import com.ibm.mobilefirstplatform.clientsdk.android.core.api.ResponseListener;
import com.ibm.mobilefirstplatform.clientsdk.android.core.internal.ResponseImpl;
import com.ibm.mobilefirstplatform.clientsdk.android.logger.api.Logger;
import com.ibm.mobilefirstplatform.clientsdk.android.security.api.AppIdentity;
import com.ibm.mobilefirstplatform.clientsdk.android.security.api.DeviceIdentity;
import com.ibm.mobilefirstplatform.clientsdk.android.security.identity.BaseAppIdentity;
import com.ibm.mobilefirstplatform.clientsdk.android.security.identity.BaseDeviceIdentity;
import com.ibm.mobilefirstplatform.clientsdk.android.security.mca.internal.AuthorizationRequest;
import com.ibm.mobilefirstplatform.clientsdk.android.security.mca.internal.AuthorizationRequestManager;
import com.ibm.mobilefirstplatform.clientsdk.android.security.mca.internal.Utils;
import com.ibm.mobilefirstplatform.clientsdk.android.security.mca.internal.certificate.CertificateStore;
import com.ibm.mobilefirstplatform.clientsdk.android.security.mca.internal.certificate.CertificatesUtility;
import com.ibm.mobilefirstplatform.clientsdk.android.security.mca.internal.certificate.DefaultJSONSigner;
import com.ibm.mobilefirstplatform.clientsdk.android.security.mca.internal.certificate.KeyPairUtility;
import com.ibm.mobilefirstplatform.clientsdk.android.security.mca.internal.preferences.AuthorizationManagerPreferences;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyPair;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

import static com.ibm.mobilefirstplatform.appid_clientsdk_android.AppId.overrideServerHost;

public class AppIdAuthorizationProcessManager {

    private AuthorizationManagerPreferences preferences;
    private KeyPair registrationKeyPair;
    private DefaultJSONSigner jsonSigner;

    private CertificateStore certificateStore;
    private Logger logger;
    private String sessionId;
    private ResponseListener listener;

    static final String redirect_uri = "http://localhost/code";
    private static final String serverName = "https://imf-authserver";
    private static final String authorizationPath = "/oauth/v2/authorization";
    private static final String tokenPath = "/oauth/v2/token";
    private static final String tenantId = AppId.getInstance().getTenantId();
    private static final String registrationPath = "/imf-authserver/authorization/v1/apps/" + tenantId + "/clients/instance";

    public AppIdAuthorizationProcessManager(Context context, AuthorizationManagerPreferences preferences) {
        this.logger = Logger.getLogger(Logger.INTERNAL_PREFIX + com.ibm.mobilefirstplatform.clientsdk.android.security.mca.internal.AuthorizationProcessManager.class.getSimpleName());
        this.preferences = preferences;
        this.jsonSigner = new DefaultJSONSigner();
        File keyStoreFile = new File(context.getFilesDir().getAbsolutePath(), "mfp.keystore");
        String uuid = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        certificateStore = new CertificateStore(keyStoreFile, uuid);
        //case where the shared preferences were deleted but the certificate is saved in the keystore
        if (preferences.clientId.get() == null && certificateStore.isCertificateStored()) {
            try {
                X509Certificate certificate = certificateStore.getCertificate();
                preferences.clientId.set(CertificatesUtility.getClientIdFromCertificate(certificate));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        //generate new random session id
        sessionId = UUID.randomUUID().toString();
    }

    String getAuthorizationUrl() {
        //Rotem: ask if that ok for performance?
        String queryParams = "?";
        queryParams += "response_type=code";
        queryParams += "&client_id=" + tenantId;
        queryParams += "&redirect_uri="+ redirect_uri;
        queryParams += "&scope=openid";
        queryParams += "&use_login_widget=true";
        return getServerHost() + authorizationPath + queryParams;
    }

    void setResponseListener(ResponseListener listener){
        this.listener = listener;
    }
    /**
     * @return the authentication server host name
     */
    private String getServerHost() {
        String serverHost = serverName + AppId.getInstance().getBluemixRegionSuffix();
        if (null != overrideServerHost) {
            serverHost = overrideServerHost;
        }
        return serverHost;
    }

    private String getRegistrationUrl() {
        return getServerHost() + registrationPath;
    }

    private String getTokenUrl() {
        return getServerHost() + tokenPath;
    }

    /**
     * Invoke request for registration, the result of the request should contain ClientId.
     *
     */
    void invokeInstanceRegistrationRequest(final ResponseListener responseListener) {
        AuthorizationRequestManager.RequestOptions options = new AuthorizationRequestManager.RequestOptions();
        options.parameters = createRegistrationParams();
        AuthorizationRequest request = null;
        try {
            request = new AuthorizationRequest(getRegistrationUrl(), Request.POST);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        request.addHeader("X-WL-Session", sessionId);
        request.send(options.parameters, responseListener);
    }

    /**
     * Generate the params that will be used during the registration phase
     *
     * @return Map with all the parameters
     */
    private HashMap<String, String> createRegistrationParams() {
        registrationKeyPair = KeyPairUtility.generateRandomKeyPair();

        JSONObject csrJSON = new JSONObject();
        HashMap<String, String> params;

        try {
            DeviceIdentity deviceData = new BaseDeviceIdentity(preferences.deviceIdentity.getAsMap());
            AppIdentity applicationData = new BaseAppIdentity(preferences.appIdentity.getAsMap());

            csrJSON.put("deviceId", deviceData.getId());
            csrJSON.put("deviceOs", "" + deviceData.getOS());
            csrJSON.put("deviceModel", deviceData.getModel());
            csrJSON.put("applicationId", applicationData.getId());
            csrJSON.put("applicationVersion", applicationData.getVersion());
            csrJSON.put("environment", "android");

            String csrValue = jsonSigner.sign(registrationKeyPair, csrJSON);

            params = new HashMap<>(1);
            params.put("CSR", csrValue);

            return params;
        } catch (Exception e) {
            throw new RuntimeException("Failed to create registration params", e);
        }
    }


    /**
     * Extract the certificate data from response and save it on local storage
     *
     * @param response contains the certificate data
     */
    void saveCertificateFromResponse(Response response) {
        try {
            String responseBody = response.getResponseText();
            JSONObject jsonResponse = new JSONObject(responseBody);
            //handle certificate
            String certificateString = jsonResponse.getString("certificate");
            X509Certificate certificate = CertificatesUtility.base64StringToCertificate(certificateString);
            CertificatesUtility.checkValidityWithPublicKey(certificate, registrationKeyPair.getPublic());
            certificateStore.saveCertificate(registrationKeyPair, certificate);
            //save the clientId separately
            preferences.clientId.set(jsonResponse.getString("clientId"));
        } catch (Exception e) {
            throw new RuntimeException("Failed to save certificate from response", e);
        }
        logger.debug("certificate successfully saved");
    }

    void sendTokenRequest(String code) {
        String header = createTokenRequestHeaders();
        HashMap<String, String> params = new HashMap<>();
        params.put("code", code);
        params.put("client_id", tenantId);
        params.put("grant_type", "authorization_code");
        params.put("redirect_uri", redirect_uri);
        AuthorizationRequestManager.RequestOptions options = new AuthorizationRequestManager.RequestOptions();
        options.parameters = params;
        AuthorizationRequest request = null;
        try {
            request = new AuthorizationRequest(getTokenUrl(), Request.POST);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        request.addHeader("authorization", header);
        request.send(options.parameters, new ResponseListener() {
            @Override
            public void onSuccess(Response response) {
                saveTokenFromResponse(response);
                listener.onSuccess(response);
            }

            @Override
            public void onFailure(Response response, Throwable t, JSONObject extendedInfo) {
                listener.onFailure(response, t, extendedInfo);
            }
        });
    }

    private String createTokenRequestHeaders() {
//        JSONObject payload = new JSONObject();

//
//        String d = tenantId + ":YjQ0M2YzNjMtZTRlYy00N2ZlLWFjNjUtMDA4YjNkNjBhMTFj";
//
//        byte[] data = new byte[0];
//        try {
//            data = d.getBytes("UTF-8");
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//        }
//        String base64 = Base64.encodeToString(data, Base64.NO_WRAP);
//        try{
//            headers = new HashMap<>(1);
//            headers.put("authorization", "basic " + base64);
//        }
//        catch (Exception e) {
//            throw new RuntimeException("Failed to create token request headers", e);
//        }

//        try {
//            payload.put("code", grantCode);
//
//            KeyPair keyPair = certificateStore.getStoredKeyPair();
//            String jws = jsonSigner.sign(keyPair, payload);
//
//            headers = new HashMap<>(1);
//            headers.put("X-WL-Authenticate", jws);
//
//        } catch (Exception e) {
//            throw new RuntimeException("Failed to create token request headers", e);
//        }

        PrivateKey privateKey = registrationKeyPair.getPrivate();
        String userName = tenantId + "-" + preferences.clientId.get();
        String tokenAuthHeader = null;
        try {
            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initSign(privateKey);
            signature.update(userName.getBytes());
            String password = Base64.encodeToString(signature.sign(), Base64.NO_WRAP);
            tokenAuthHeader = "basic " + Base64.encodeToString((userName + ":" + password).getBytes(), Base64.NO_WRAP);
            return tokenAuthHeader;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return tokenAuthHeader;
    }

    /**
     * Extract token from response and save it locally
     *
     * @param response response that contain the token
     */
    private void saveTokenFromResponse(Response response) {
        try {
            JSONObject responseJSON = ((ResponseImpl) response).getResponseJSON();
            String accessToken = responseJSON.getString("access_token");
            String idToken = responseJSON.getString("id_token");
            //save the tokens
            preferences.accessToken.set(accessToken);
            preferences.idToken.set(idToken);
            //save the user identity separately
            String[] idTokenData = idToken.split("\\.");
            byte[] decodedIdTokenData = Base64.decode(idTokenData[1], Base64.DEFAULT);
            String decodedIdTokenString = new String(decodedIdTokenData);
            JSONObject idTokenJSON = new JSONObject(decodedIdTokenString);
            if (idTokenJSON.has("imf.user")) {
                preferences.userIdentity.set(idTokenJSON.getJSONObject("imf.user"));
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to save token from response", e);
        }
    }
}