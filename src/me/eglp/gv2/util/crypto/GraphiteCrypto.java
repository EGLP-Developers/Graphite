package me.eglp.gv2.util.crypto;

import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.EncodedKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import me.mrletsplay.mrcore.misc.FriendlyException;

public class GraphiteCrypto {
	
	public static KeyPair generateKeyPair() {
		try {
			KeyPairGenerator k = KeyPairGenerator.getInstance("RSA");
			k.initialize(1024);
			return k.generateKeyPair();
		}catch(NoSuchAlgorithmException e) {
			throw new FriendlyException(e);
		}
	}
	
	public static SecretKey generateSymmetricKey() {
		try {
			KeyGenerator k = KeyGenerator.getInstance("AES");
			k.init(256);
			return k.generateKey();
		}catch(NoSuchAlgorithmException e) {
			throw new FriendlyException(e);
		}
	}
	
	public static PrivateKey decodePrivateKey(byte[] encoded) {
		try {
			KeyFactory f = KeyFactory.getInstance("RSA");
			EncodedKeySpec ks = new PKCS8EncodedKeySpec(encoded);
			return f.generatePrivate(ks);
		}catch(NoSuchAlgorithmException | InvalidKeySpecException e) {
			return null;
		}
	}
	
	public static PublicKey decodePublicKey(byte[] encoded) {
		try {
			KeyFactory f = KeyFactory.getInstance("RSA");
			EncodedKeySpec ks = new X509EncodedKeySpec(encoded);
			return f.generatePublic(ks);
		}catch(NoSuchAlgorithmException | InvalidKeySpecException e) {
			return null;
		}
	}
	
	public static SecretKey decryptAESKey(byte[] encryptedMessageKey, PrivateKey decryptionKey) {
		SecretKey aesKey;
		try {
			Cipher keyDecryptionCipher = Cipher.getInstance("RSA");
			keyDecryptionCipher.init(Cipher.DECRYPT_MODE, decryptionKey);
			byte[] messageKeyData = keyDecryptionCipher.doFinal(encryptedMessageKey);
			aesKey = new SecretKeySpec(messageKeyData, "AES");
			return aesKey;
		}catch(NoSuchAlgorithmException | NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException | InvalidKeyException e) {
			throw new FriendlyException(e);
		}
	}

}
