package com.ibm.mobilefirstplatform.appid_clientsdk_android;

import android.content.Context;
import android.provider.Settings;
import android.util.Base64;

import com.ibm.mobilefirstplatform.clientsdk.android.core.api.Request;
import com.ibm.mobilefirstplatform.clientsdk.android.core.api.Response;
import com.ibm.mobilefirstplatform.clientsdk.android.core.api.ResponseListener;
import com.ibm.mobilefirstplatform.clientsdk.android.security.api.AppIdentity;
import com.ibm.mobilefirstplatform.clientsdk.android.security.api.DeviceIdentity;
import com.ibm.mobilefirstplatform.clientsdk.android.security.identity.BaseAppIdentity;
import com.ibm.mobilefirstplatform.clientsdk.android.security.identity.BaseDeviceIdentity;
import com.ibm.mobilefirstplatform.clientsdk.android.security.mca.internal.certificate.CertificateStore;
import com.ibm.mobilefirstplatform.clientsdk.android.security.mca.internal.certificate.DefaultJSONSigner;
import com.ibm.mobilefirstplatform.clientsdk.android.security.mca.internal.certificate.KeyPairUtility;
import com.ibm.mobilefirstplatform.clientsdk.android.security.mca.internal.preferences.AuthorizationManagerPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;
import java.util.Date;
import java.util.Set;
import java.util.UUID;

/**
 * Created by rotembr on 08/12/2016.
 */

public class AppIdRegistrationManager {

    private static final String registrationPath = "/oauth/v3/";
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
        this.sessionId = UUID.randomUUID().toString();
    }

    /**
     *
     * @return the certificate store
     */
    public CertificateStore getCertificateStore(){
        return certificateStore;
    }

    /**
     * Invoke request for registration, the result of the request should contain ClientId.
     *
     */
    void invokeInstanceRegistrationRequest(Context context, final ResponseListener responseListener) {
        try {
            JSONObject reqJson =  createRegistrationParams(context);
            AppIDRequest request = new AppIDRequest(getRegistrationUrl(), Request.POST);
            request.addHeader("X-WL-Session", sessionId);
            request.send(reqJson, new ResponseListener() {
                @Override
                public void onSuccess(Response response) {
                    try {
                        saveClientId(response);
                        responseListener.onSuccess(response);
                    } catch (Exception e) {
                        responseListener.onFailure(null, e, null);
                        throw new RuntimeException("Failed to save certificate from response", e);
                    }
                }

                @Override
                public void onFailure(Response response, Throwable t, JSONObject extendedInfo) {
                    responseListener.onFailure(response ,t, extendedInfo);
                }
            });
        }catch (MalformedURLException e) {
            responseListener.onFailure(null, e, null);
            throw new RuntimeException("Failed to create registration request", e);
        }
        catch (Exception e) {
            responseListener.onFailure(null, e, null);
            throw new RuntimeException("Failed to create registration params", e);
        }
    }

    private String getRegistrationUrl() {
        return AppIdAuthorizationManager.getInstance().getServerHost() + registrationPath + AppId.getInstance().getTenantId() + "/clients";
    }

    /**
     * Generate the params that will be used during the registration phase
     *
     * @return JSONObject with all the parameters
     */
    private JSONObject createRegistrationParams(Context context) throws Exception {
        registrationKeyPair = KeyPairUtility.generateRandomKeyPair();
        this.certificateStore.saveCertificate(registrationKeyPair, createX509Certificate(registrationKeyPair));
        JSONObject params = new JSONObject();
        DeviceIdentity deviceData = new BaseDeviceIdentity(preferences.deviceIdentity.getAsMap());
        AppIdentity applicationData = new BaseAppIdentity(preferences.appIdentity.getAsMap());
        JSONArray redirectUris = new JSONArray();
        JSONArray responseTypes = new JSONArray();
        JSONArray grantTypes = new JSONArray();
        JSONArray keys = new JSONArray();
        JSONObject key = new JSONObject();
        key.put("e", encodeUrlSafe(((RSAPublicKey)registrationKeyPair.getPublic()).getPublicExponent().toByteArray()));
        key.put("n", encodeUrlSafe(((RSAPublicKey)registrationKeyPair.getPublic()).getModulus().toByteArray()));
        key.put("kty", registrationKeyPair.getPublic().getAlgorithm());
        keys.put(0, key);
        JSONObject jwks = new JSONObject();
        jwks.put("keys", keys);
        redirectUris.put(0, "https://" + deviceData.getId() + "/mobile/callback");
        responseTypes.put(0, "code");
        grantTypes.put(0, "authorization_code");
        grantTypes.put(1, "password");
        params.put("redirect_uris", redirectUris);
        params.put("token_endpoint_auth_method", "client_secret_basic");
        params.put("response_types", responseTypes);
        params.put("grant_types", grantTypes);
        params.put("client_name", context.getApplicationInfo().loadLabel(context.getPackageManager()).toString());
        params.put("software_id", applicationData.getId());
        params.put("software_version", applicationData.getVersion());
        params.put("device_id", deviceData.getId());
        params.put("device_model", deviceData.getModel());
        params.put("device_os", deviceData.getOS());
        params.put("client_type", "mobileapp");
        params.put("jwks", jwks);
        String base64StringParams = Base64.encodeToString(params.toString().getBytes(Charset.forName("UTF-8")),Base64.URL_SAFE | Base64.NO_WRAP);
        params.put("software_statement", base64StringParams + "." + encodeUrlSafe(signData(base64StringParams, registrationKeyPair.getPrivate())));
        return params;
    }

    private String encodeUrlSafe(byte[] data) throws UnsupportedEncodingException {
        return new String(Base64.encode(data, Base64.URL_SAFE | Base64.NO_WRAP),"UTF-8");
    }

    private byte[] signData(String csrJSONData, PrivateKey privateKey) throws NoSuchAlgorithmException, InvalidKeyException, SignatureException {
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initSign(privateKey);
        signature.update(csrJSONData.getBytes());
        return signature.sign();
    }

    /**
     * Extract the certificate data from response and save it on local storage
     *
     * @param response contains the certificate data
     */
    private void saveClientId(Response response) throws JSONException {
        String responseBody = response.getResponseText();
        JSONObject jsonResponse = new JSONObject(responseBody);
        //save the clientId
        preferences.clientId.set(jsonResponse.getString("client_id"));

    }

    private X509Certificate createX509Certificate(final KeyPair keyPair) {
        return new X509Certificate() {
            @Override
            public void checkValidity() throws CertificateExpiredException, CertificateNotYetValidException {

            }

            @Override
            public void checkValidity(Date date) throws CertificateExpiredException, CertificateNotYetValidException {

            }

            @Override
            public int getVersion() {
                return 0;
            }

            @Override
            public BigInteger getSerialNumber() {
                return null;
            }

            @Override
            public Principal getIssuerDN() {
                return null;
            }

            @Override
            public Principal getSubjectDN() {
                return null;
            }

            @Override
            public Date getNotBefore() {
                return null;
            }

            @Override
            public Date getNotAfter() {
                return null;
            }

            @Override
            public byte[] getTBSCertificate() throws CertificateEncodingException {
                return new byte[0];
            }

            @Override
            public byte[] getSignature() {
                return new byte[0];
            }

            @Override
            public String getSigAlgName() {
                return null;
            }

            @Override
            public String getSigAlgOID() {
                return null;
            }

            @Override
            public byte[] getSigAlgParams() {
                return new byte[0];
            }

            @Override
            public boolean[] getIssuerUniqueID() {
                return new boolean[0];
            }

            @Override
            public boolean[] getSubjectUniqueID() {
                return new boolean[0];
            }

            @Override
            public boolean[] getKeyUsage() {
                return new boolean[0];
            }

            @Override
            public int getBasicConstraints() {
                return 0;
            }

            @Override
            public byte[] getEncoded() throws CertificateEncodingException {
                return new byte[0];
            }

            @Override
            public void verify(PublicKey publicKey) throws CertificateException, NoSuchAlgorithmException, InvalidKeyException, NoSuchProviderException, SignatureException {

            }

            @Override
            public void verify(PublicKey publicKey, String s) throws CertificateException, NoSuchAlgorithmException, InvalidKeyException, NoSuchProviderException, SignatureException {

            }

            @Override
            public String toString() {
                return null;
            }

            @Override
            public PublicKey getPublicKey() {
                return keyPair.getPublic();
            }

            @Override
            public boolean hasUnsupportedCriticalExtension() {
                return false;
            }

            @Override
            public Set<String> getCriticalExtensionOIDs() {
                return null;
            }

            @Override
            public Set<String> getNonCriticalExtensionOIDs() {
                return null;
            }

            @Override
            public byte[] getExtensionValue(String s) {
                return new byte[0];
            }
        };
    }
}
