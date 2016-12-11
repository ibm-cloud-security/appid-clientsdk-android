package com.ibm.mobilefirstplatform.appid_clientsdk_android;

import android.content.Context;
import android.provider.Settings;

import com.ibm.mobilefirstplatform.clientsdk.android.core.api.Request;
import com.ibm.mobilefirstplatform.clientsdk.android.core.api.Response;
import com.ibm.mobilefirstplatform.clientsdk.android.core.api.ResponseListener;
import com.ibm.mobilefirstplatform.clientsdk.android.security.api.AppIdentity;
import com.ibm.mobilefirstplatform.clientsdk.android.security.api.DeviceIdentity;
import com.ibm.mobilefirstplatform.clientsdk.android.security.identity.BaseAppIdentity;
import com.ibm.mobilefirstplatform.clientsdk.android.security.identity.BaseDeviceIdentity;
import com.ibm.mobilefirstplatform.clientsdk.android.security.mca.internal.AuthorizationRequest;
import com.ibm.mobilefirstplatform.clientsdk.android.security.mca.internal.AuthorizationRequestManager;
import com.ibm.mobilefirstplatform.clientsdk.android.security.mca.internal.certificate.CertificateStore;
import com.ibm.mobilefirstplatform.clientsdk.android.security.mca.internal.certificate.CertificatesUtility;
import com.ibm.mobilefirstplatform.clientsdk.android.security.mca.internal.certificate.DefaultJSONSigner;
import com.ibm.mobilefirstplatform.clientsdk.android.security.mca.internal.certificate.KeyPairUtility;
import com.ibm.mobilefirstplatform.clientsdk.android.security.mca.internal.preferences.AuthorizationManagerPreferences;

import org.json.JSONObject;

import java.io.File;
import java.net.MalformedURLException;
import java.security.KeyPair;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.UUID;

/**
 * Created by rotembr on 08/12/2016.
 */

public class AppIdRegistrationManager {

    private static final String registrationPath = "/imf-authserver/authorization/v1/apps/";
    private AuthorizationManagerPreferences preferences;
    private KeyPair registrationKeyPair;
    private String sessionId;
    private DefaultJSONSigner jsonSigner;
    private CertificateStore certificateStore;

    AppIdRegistrationManager(Context context, AuthorizationManagerPreferences preferences){
        this.preferences = preferences;
        this.jsonSigner = new DefaultJSONSigner();
        File keyStoreFile = new File(context.getFilesDir().getAbsolutePath(), "mfp.keystore");
        String uuid = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        this.certificateStore = new CertificateStore(keyStoreFile, uuid);
        //case where the shared preferences were deleted but the certificate is saved in the keystore
        if (preferences.clientId.get() == null && certificateStore.isCertificateStored()) {
            try {
                X509Certificate certificate = certificateStore.getCertificate();
                preferences.clientId.set(CertificatesUtility.getClientIdFromCertificate(certificate));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        this.sessionId = UUID.randomUUID().toString();
    }

    CertificateStore getCertificateStore() {
        return certificateStore;
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

    private String getRegistrationUrl() {
        return AppIdAuthorizationManager.getInstance().getServerHost() + registrationPath + AppId.getInstance().getTenantId() + "/clients/instance";
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
    }

}
