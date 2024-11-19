package com.dulcian.face.utils;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.http.MediaType;
public class EncryptionUtils {
    private static final String ALGORITHM = "AES";
    private static final String CIPHER_ALGORITHM = "AES/CBC/PKCS5Padding";
    private static final String STATIC_KEY = "Z35w4eLfRTJPePVTt18RWb2ThXUBepXh";
    private static final String STATIC_IV = "Ew12Bbo4KtvLqcx4";
    private static final byte[] PNG_MAGIC_NUMBER = {(byte) 0x89, (byte) 0x50, (byte) 0x4E, (byte) 0x47};
    private static final byte[] JPEG_MAGIC_NUMBER = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF};
    private static final SecretKeySpec secretKey = new SecretKeySpec(STATIC_KEY.getBytes(), ALGORITHM);
    private static final IvParameterSpec iv = new IvParameterSpec(STATIC_IV.getBytes());
    private static final Cipher encryptCipher;
    private static final Cipher decryptCipher;

    static {
        try {
            encryptCipher = Cipher.getInstance(CIPHER_ALGORITHM);
            encryptCipher.init(Cipher.ENCRYPT_MODE, secretKey, iv);

            decryptCipher = Cipher.getInstance(CIPHER_ALGORITHM);
            decryptCipher.init(Cipher.DECRYPT_MODE, secretKey, iv);
        } catch (Exception e) {
            throw new RuntimeException("Error initializing encryption/decryption ciphers", e);
        }
    }

    public static byte[] encrypt(byte[] plainObject){
        try {
            return encryptCipher.doFinal(plainObject);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static byte[] decrypt(byte[] encryptedObject) {
        try {
            return decryptCipher.doFinal(encryptedObject);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static MediaType getType(byte[] imageData){
        if (isPng(imageData)) {
            return MediaType.IMAGE_PNG;
        }

        if(isJpeg(imageData)){
            return MediaType.IMAGE_JPEG;
        }

        throw  new RuntimeException("Image format is unknown");
    }

    public static boolean isPng(byte[] imageData){
        return startsWith(imageData, PNG_MAGIC_NUMBER);
    }

    public static boolean isJpeg(byte[] imageData){
        return startsWith(imageData, JPEG_MAGIC_NUMBER);
    }

    public static boolean startsWith(byte[] data, byte[] prefix) {
        if (data.length < prefix.length) return false;
        for (int i = 0; i < prefix.length; i++) {
            if (data[i] != prefix[i]) {
                return false;
            }
        }
        return true;
    }
}
