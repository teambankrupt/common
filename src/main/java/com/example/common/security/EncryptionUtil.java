package com.example.common.security;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.Locale;

public class EncryptionUtil implements Serializable {
    private static final long serialVersionUID = 1L;

    private static final Logger logger = LoggerFactory.getLogger(EncryptionUtil.class);

    private static KeyStore keyStore;
    private static String ksFile;
    private static String ksPassword;

    private static class EncryptionUtilLoader {
        private static final EncryptionUtil INSTANCE = new EncryptionUtil();
    }

    public static EncryptionUtil with(String keyStoreFile, String keyStorePassword) throws KeyStoreException {
        synchronized (EncryptionUtil.class) {
            EncryptionUtil.ksFile = keyStoreFile;
            EncryptionUtil.ksPassword = keyStorePassword;
            keyStore = getKeyStore(keyStoreFile, keyStorePassword);
        }
        return EncryptionUtilLoader.INSTANCE;
    }

    // Protect from serialization should return only one instance
    @SuppressWarnings("unused")
    private EncryptionUtil readResolve() {
        return EncryptionUtilLoader.INSTANCE;
    }

    private EncryptionUtil() {
    }

    private static KeyStore getKeyStore(String file, String password) throws KeyStoreException {
        if (file == null) throw new RuntimeException("Must provide keystore file path!");
        if (password == null) throw new RuntimeException("Keystore password can't be null!");
        if (!FilenameUtils.getExtension(file).toUpperCase(Locale.ROOT).equals("PKCS12"))
            throw new RuntimeException("Invalid keystore file. file must be a PKCS12.");

        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        char[] pwdArray = password.toCharArray();

        File keyStoreFile = new File(file);
        if (!keyStoreFile.exists())
            writeKeyStore(keyStore, file, pwdArray, false);

        try (InputStream is = new FileInputStream(file)) {
            keyStore.load(is, pwdArray);
        } catch (CertificateException | IOException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return keyStore;
    }

    private static void writeKeyStore(KeyStore keyStore, String file, char[] pwdArray, boolean update) {
        try (FileOutputStream fos = new FileOutputStream(file)) {
            if (!update)
                keyStore.load(null, pwdArray);
            keyStore.store(fos, pwdArray);
        } catch (IOException | CertificateException | KeyStoreException | NoSuchAlgorithmException e) {
            logger.debug("Could not create keystore!");
            e.printStackTrace();
        }
    }

    public void storeSymmetricKey(String alias, String key, String password) {
        if (password == null) throw new RuntimeException("Key password can't be null!");

        byte[] keyBytes = key.getBytes();
        SecretKey secretKey = new SecretKeySpec(keyBytes, "AES");
        KeyStore.SecretKeyEntry secret
                = new KeyStore.SecretKeyEntry(secretKey);
        KeyStore.ProtectionParameter pwd
                = new KeyStore.PasswordProtection(password.toCharArray());
        try {
            logger.info("Storing key entry for alias: " + alias + " | secret: " + secret + " | Passphrase: " + pwd);
            keyStore.setEntry(alias, secret, pwd);
            writeKeyStore(keyStore, ksFile, ksPassword.toCharArray(), true);
        } catch (KeyStoreException e) {
            e.printStackTrace();
        }
    }

    public String retrieveSymmetricKey(String alias, String password) throws UnrecoverableKeyException, KeyStoreException, NoSuchAlgorithmException {
        Key key = keyStore.getKey(alias, password.toCharArray());
        if (key == null) return null;
        return new String(key.getEncoded());
    }


}
