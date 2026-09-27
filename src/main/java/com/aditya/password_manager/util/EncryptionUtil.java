package com.aditya.password_manager.util;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Base64;

public class EncryptionUtil {

	// The developer's secret key
	private static final String DEV_SECRET_KEY = "AdityaDevKey2026!";
	private static final String ALGORITHM = "AES";

	// Helper method to combine inputs into a perfect 16-byte AES key
	private static SecretKeySpec generateCustomKey(String userSecret, String dob, String gender) throws Exception {
		String combinedString = userSecret + DEV_SECRET_KEY + dob + gender;

		// Hash the combined string to get a standard length byte array
		MessageDigest sha = MessageDigest.getInstance("SHA-256");
		byte[] keyBytes = sha.digest(combinedString.getBytes("UTF-8"));

		// Trim to 16 bytes for 128-bit AES encryption
		keyBytes = Arrays.copyOf(keyBytes, 16);
		return new SecretKeySpec(keyBytes, ALGORITHM);
	}

	public static String encrypt(String plainText, String userSecret, String dob, String gender) throws Exception {
		SecretKeySpec secretKey = generateCustomKey(userSecret, dob, gender);
		Cipher cipher = Cipher.getInstance(ALGORITHM);
		cipher.init(Cipher.ENCRYPT_MODE, secretKey);

		byte[] encryptedBytes = cipher.doFinal(plainText.getBytes());
		return Base64.getEncoder().encodeToString(encryptedBytes);
	}

	public static String decrypt(String encryptedText, String userSecret, String dob, String gender) throws Exception {
		SecretKeySpec secretKey = generateCustomKey(userSecret, dob, gender);
		Cipher cipher = Cipher.getInstance(ALGORITHM);
		cipher.init(Cipher.DECRYPT_MODE, secretKey);

		byte[] decodedBytes = Base64.getDecoder().decode(encryptedText);
		byte[] decryptedBytes = cipher.doFinal(decodedBytes);

		return new String(decryptedBytes);
	}
}