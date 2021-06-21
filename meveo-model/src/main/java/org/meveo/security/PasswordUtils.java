package org.meveo.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Arrays;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.lang.StringUtils;

/**
 * @author clement.bareth
 * @since 6.12.0
 * @version 6.12.0
 */
public class PasswordUtils {

	private static String factoryInstance = "PBKDF2WithHmacSHA256";
	private static String cipherInstance = "AES/CBC/PKCS5PADDING";
	private static String secretKeyType = "AES";
	private static byte[] ivCode = new byte[16];
	private static String SECRET_PREFIX = new String("🔒");
	
	/**
	 * Generate a salt from the given values
	 * 
	 * @param values the values
	 * @return the salt
	 */
	public static String getSalt(Object... values) {
		MessageDigest digest;
		try {
			digest = MessageDigest.getInstance("SHA-256");
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
		
		String stringValues = StringUtils.join(values);
		byte[] encodedhash = digest.digest(stringValues.getBytes(StandardCharsets.UTF_8));
		return new String(encodedhash, StandardCharsets.UTF_8);
	}

	/**
	 * Encrypt the given value with AES 
	 * 
	 * @param salt the salt to use during encryption
	 * @param value the value to encrypt
	 * @return the encrypted value
	 * @throws Exception if error occurs
	 */
	public static String encrypt(String salt, String value) {
		try {
			Cipher cipher = initCipher(salt, Cipher.ENCRYPT_MODE);
			byte[] encrypted = cipher.doFinal(value.getBytes(StandardCharsets.UTF_8));
			byte[] cipherWithIv = addIVToCipher(encrypted);
			byte[] encodedBytes = Base64.getEncoder().encode(cipherWithIv);
			return new String(encodedBytes, StandardCharsets.UTF_8);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Encrypt the given value with AES 
	 * 
	 * @param salt the salt used for the encryption
	 * @param encrypted the encrypted value to decrypt
	 * @return the decrypted value
	 * @throws Exception if error occurs
	 */
	public static String decrypt(String salt, String encrypted) {
		try {
			Cipher cipher = initCipher(salt, Cipher.DECRYPT_MODE);
			byte[] original = cipher.doFinal(Base64.getDecoder().decode(encrypted));
			byte[] originalWithoutIv = Arrays.copyOfRange(original, 16, original.length);
			return new String(originalWithoutIv);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	/**
	 * Encrypt the given value with AES 
	 * 
	 * @param salt the salt to use during encryption
	 * @param value the value to encrypt
	 * @return the encrypted value
	 * @throws Exception if error occurs
	 */
	public static String encryptNoSecret(String salt, String value) {
		try {
			Cipher cipher = initCipherNoSecret(salt, Cipher.ENCRYPT_MODE);
			byte[] encrypted = cipher.doFinal(value.getBytes());
			byte[] cipherWithIv = addIVToCipher(encrypted);
			return SECRET_PREFIX + Base64.getEncoder().encodeToString(cipherWithIv); // We use "␎" to retrieve whether a string is encoded
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Encrypt the given value with AES 
	 * 
	 * @param salt the salt used for the encryption
	 * @param encrypted the encrypted value to decrypt
	 * @return the decrypted value
	 * @throws Exception if error occurs
	 */
	public static String decryptNoSecret(String salt, String encrypted) {
		if(encrypted.startsWith(SECRET_PREFIX)) {	// Don't include this special char when decrypting
			encrypted = encrypted.substring(SECRET_PREFIX.length());
		} else {
			// Consider the string is not encrypted
			return encrypted;
		}
		
		try {
			Cipher cipher = initCipherNoSecret(salt, Cipher.DECRYPT_MODE);
			byte[] decodedBytes = Base64.getDecoder().decode(encrypted);
			byte[] original = cipher.doFinal(decodedBytes);
			byte[] originalWithoutIv = Arrays.copyOfRange(original, 16, original.length);
			return new String(originalWithoutIv);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	private static Cipher initCipherNoSecret(String salt, int mode) throws Exception {
		SecretKeyFactory factory = SecretKeyFactory.getInstance(factoryInstance);
		KeySpec spec = new PBEKeySpec("NoSecret".toCharArray(), salt.getBytes(), 65536, 256);
		SecretKey tmp = factory.generateSecret(spec);
		SecretKeySpec skeySpec = new SecretKeySpec(tmp.getEncoded(), secretKeyType);
		Cipher cipher = Cipher.getInstance(cipherInstance);
		// Generating random IV
		SecureRandom random = new SecureRandom();
		random.nextBytes(ivCode);
		cipher.init(mode, skeySpec, new IvParameterSpec(ivCode));
		return cipher;
	}

	private static Cipher initCipher(String salt, int mode) throws Exception {
		String secretKey = System.getProperty("meveo.security.secret", "NoDefaultKey");

		SecretKeyFactory factory = SecretKeyFactory.getInstance(factoryInstance);
		KeySpec spec = new PBEKeySpec(secretKey.toCharArray(), salt.getBytes(), 65536, 256);
		SecretKey tmp = factory.generateSecret(spec);
		SecretKeySpec skeySpec = new SecretKeySpec(tmp.getEncoded(), secretKeyType);
		Cipher cipher = Cipher.getInstance(cipherInstance);
		// Generating random IV
		SecureRandom random = new SecureRandom();
		random.nextBytes(ivCode);
		cipher.init(mode, skeySpec, new IvParameterSpec(ivCode));
		return cipher;
	}

	private static byte[] addIVToCipher(byte[] encrypted) {
		byte[] cipherWithIv = new byte[ivCode.length + encrypted.length];
		System.arraycopy(ivCode, 0, cipherWithIv, 0, ivCode.length);
		System.arraycopy(encrypted, 0, cipherWithIv, ivCode.length, encrypted.length);
		return cipherWithIv;
	}
}