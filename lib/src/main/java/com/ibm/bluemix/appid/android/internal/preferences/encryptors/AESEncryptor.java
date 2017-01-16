package com.ibm.bluemix.appid.android.internal.preferences.encryptors;

import android.util.Base64;

import java.security.Key;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class AESEncryptor implements PreferenceEncryptor {

	final static String Algorithm = "AES";
	final static int minKeySize = 16;

	// TODO: Use unique deviceId instead
	final static String hashStr = "zDfb2E9yZartghdY";

	Key key;

	public AESEncryptor(String password) {
		key = new SecretKeySpec(hash(password.getBytes()), Algorithm);
	}

	@Override
	public String encrypt(String str) {
		byte[] bytes = doFinalWithMode(Cipher.ENCRYPT_MODE, str.getBytes());
		return Base64.encodeToString(bytes, Base64.NO_WRAP);
	}

	@Override
	public String decrypt(String str) {
		byte[] decode = Base64.decode(str.getBytes(), Base64.NO_WRAP);
		byte[] bytes = doFinalWithMode(Cipher.DECRYPT_MODE, decode);
		return new String(bytes);
	}

	private byte[] doFinalWithMode(int mode, byte[] data) {
		try {
			Cipher cipher = Cipher.getInstance(Algorithm);
			cipher.init(mode, key);
			return cipher.doFinal(data);
		} catch (Exception e) {
			e.printStackTrace();
		}

		return new byte[0];
	}

	private byte[] hash(byte[] array){
		byte[] result = Arrays.copyOf(array, minKeySize);
		byte[] hash = hashStr.getBytes();

		for (int i = 0 ; i < minKeySize ; ++i){
			result[i] ^= hash[i];
		}
		return result;
	}
}
