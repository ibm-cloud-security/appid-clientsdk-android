package com.ibm.mobilefirstplatform.appid_clientsdk_android;

import android.content.Context;
import android.os.Build;
import android.security.KeyPairGeneratorSpec;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableEntryException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;

/**
 * Created by rotembr on 04/01/2017.
 */

public class AppIdKeyStore {

    private static final String alias = "registration";
    private static final String ANDROID_KEY_STORE = "AndroidKeyStore";
    private KeyStore keyStore;

    public KeyPair generateKeypair(Context context) throws CertificateException, KeyStoreException, IOException, UnrecoverableEntryException, NoSuchAlgorithmException, NoSuchProviderException, InvalidAlgorithmParameterException {
        loadKeyStore();
        KeyPair keyPair = null;
        if (!keyStore.containsAlias(alias)) {
            KeyPairGenerator generator = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_RSA, ANDROID_KEY_STORE);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                KeyGenParameterSpec spec  = new KeyGenParameterSpec.Builder(alias, KeyProperties.PURPOSE_SIGN)
                        .setDigests(KeyProperties.DIGEST_SHA256)
                        .setSignaturePaddings(KeyProperties.SIGNATURE_PADDING_RSA_PKCS1)
                        .build();
                generator.initialize(spec);

            }else {
                KeyPairGeneratorSpec spec = new KeyPairGeneratorSpec.Builder(context)
                        .setAlias(alias)
                        .setKeyType(KeyProperties.KEY_ALGORITHM_RSA)
                        .build();
                generator.initialize(spec);

            }
            keyPair = generator.generateKeyPair();
        }else {
            keyPair = getStoredKeyPair();
        }
        return keyPair;
    }

    private void loadKeyStore() throws IOException, KeyStoreException, CertificateException, NoSuchAlgorithmException {
        if (keyStore == null) {
            keyStore = KeyStore.getInstance(ANDROID_KEY_STORE);
            keyStore.load(null);
        }
    }

    public KeyPair getStoredKeyPair() throws CertificateException, KeyStoreException, IOException, UnrecoverableEntryException, NoSuchAlgorithmException {
        loadKeyStore();
        KeyPair keyPair = null;
        if (keyStore.containsAlias(alias)) {
            KeyStore.PrivateKeyEntry pke = (KeyStore.PrivateKeyEntry) keyStore.getEntry(alias, null);
            Certificate cert = pke.getCertificate();
            keyPair = new KeyPair(cert.getPublicKey(), pke.getPrivateKey());
        }else{
            throw new IOException("No keyPair found");
        }
        return keyPair;
    }
}
