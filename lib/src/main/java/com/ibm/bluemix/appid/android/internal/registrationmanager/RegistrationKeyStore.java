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

import android.content.Context;
import android.os.Build;
import android.security.KeyPairGeneratorSpec;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;

import com.ibm.mobilefirstplatform.clientsdk.android.logger.api.Logger;

import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.util.Calendar;

import javax.security.auth.x500.X500Principal;

class RegistrationKeyStore {

    private static final String ALIAS = "com.ibm.bluemix.appid.android.REGISTRATION";
    private static final String ANDROID_KEY_STORE = "AndroidKeyStore";
    private static final Logger logger = Logger.getLogger(Logger.INTERNAL_PREFIX + RegistrationKeyStore.class.getName());

	KeyPair getKeyPair(){
		KeyStore keyStore = loadKeyStore();

		// If keypair already exist
		try {
			if (keyStore.containsAlias(ALIAS)) {
				KeyStore.PrivateKeyEntry pke = (KeyStore.PrivateKeyEntry) keyStore.getEntry(ALIAS, null);
				Certificate cert = pke.getCertificate();
				KeyPair keyPair = new KeyPair(cert.getPublicKey(), pke.getPrivateKey());
				return keyPair;
			}
		} catch (Exception e){
			logger.error("Failed to read from keystore", e);
			throw new RuntimeException("Failed to read from keystore");
		}
		return null;
	}

    KeyPair generateKeyPair (Context context) {
		// Generate and store new keypair
		KeyStore keyStore = loadKeyStore();
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_RSA, ANDROID_KEY_STORE);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                KeyGenParameterSpec spec = new KeyGenParameterSpec.Builder(ALIAS,
                        KeyProperties.PURPOSE_SIGN | KeyProperties.PURPOSE_VERIFY)
                        .setDigests(KeyProperties.DIGEST_SHA256)
                        .setSignaturePaddings(KeyProperties.SIGNATURE_PADDING_RSA_PKCS1)
                        .build();
                generator.initialize(spec);
            } else {
                Calendar start = Calendar.getInstance();
                Calendar end = Calendar.getInstance();
                end.add(Calendar.YEAR, 20);
                KeyPairGeneratorSpec spec = new KeyPairGeneratorSpec.Builder(context)
                        .setAlias(ALIAS)
                        .setSubject(new X500Principal("CN=AppID"))
                        .setSerialNumber(BigInteger.ONE)
                        .setStartDate(start.getTime())
                        .setEndDate(end.getTime())
                        .setKeyType(KeyProperties.KEY_ALGORITHM_RSA)
                        .build();
                generator.initialize(spec);
            }
			keyStore.deleteEntry(ALIAS);
            return generator.generateKeyPair();
        } catch (Exception e){
			logger.error("Failed to generate key pair", e);
			throw new RuntimeException("Failed to generate key pair");
        }
    }


	private KeyStore loadKeyStore() {
		try {
			KeyStore keyStore = KeyStore.getInstance(ANDROID_KEY_STORE);
			keyStore.load(null);
			return keyStore;
		} catch (Exception e){
			logger.error("Failed to load key store", e);
			throw new RuntimeException("Failed to load key store");
		}
	}
}
