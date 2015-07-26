package android.datatheorem.com.ecnryptiondemo.Utils;

import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.util.Random;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * Created by alexeyreznik on 26/07/15.
 */
public class SecurityUtils {

    private static final int MAX_LENGTH = 16;

    public static byte[] generateSecretKey(String keyAlias) {
        try {
            //Generate Secret key using Android KeyGenerator
            byte[] keyStart = "this is a key".getBytes();
            KeyGenerator kg = KeyGenerator.getInstance("AES");
            SecureRandom sr = SecureRandom.getInstance("SHA1PRNG");
            sr.setSeed(keyStart);
            kg.init(128, sr);
            SecretKey skey = kg.generateKey();

            //Save secret key using Android KeyStore
            KeyStore ks = KeyStore.getInstance("AndroidKeyStore");
            ks.load(null, null);
            KeyStore.SecretKeyEntry entry = new KeyStore.SecretKeyEntry(skey);
            ks.setEntry(keyAlias, entry, null);
            return skey.getEncoded();
        } catch (NoSuchAlgorithmException | KeyStoreException | CertificateException | IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static byte[] retrieveSecretKey(String keyAlias) {
        try {
            //Retrieve secret key from KeyStore
            KeyStore ks = KeyStore.getInstance("AndroidKeyStore");
            return ((KeyStore.SecretKeyEntry) ks.getEntry(keyAlias, null)).getSecretKey().getEncoded();
        } catch (UnrecoverableEntryException | NoSuchAlgorithmException | KeyStoreException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static byte[] encrypt(byte[] key, byte[] clear) throws Exception {
        SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, skeySpec);
        byte[] encrypted = cipher.doFinal(clear);
        return encrypted;
    }

    public static byte[] decrypt(byte[] key, byte[] encrypted) throws Exception {
        SecretKeySpec skeySpec = new SecretKeySpec(key, "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, skeySpec);
        byte[] decrypted = cipher.doFinal(encrypted);
        return decrypted;
    }

    public static String generateRandomString() {
        Random generator = new Random();
        StringBuilder randomStringBuilder = new StringBuilder();
        int randomLength = generator.nextInt(MAX_LENGTH);
        char tempChar;
        for (int i = 0; i < randomLength; i++){
            tempChar = (char) (generator.nextInt(96) + 32);
            randomStringBuilder.append(tempChar);
        }
        return randomStringBuilder.toString();
    }
}
