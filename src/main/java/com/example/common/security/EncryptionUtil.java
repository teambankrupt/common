package com.example.common.security;

import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.util.Locale;

public class EncryptionUtil {

    private static final EncryptionUtil INSTANCE = new EncryptionUtil();
    private static final Logger logger = LoggerFactory.getLogger(EncryptionUtil.class);

    public EncryptionUtil getInstance() {
        return INSTANCE;
    }

    private EncryptionUtil() {
    }

    public static KeyStore getKeyStore(String file, String password) throws KeyStoreException, CertificateException, IOException, NoSuchAlgorithmException {
        if (file == null) throw new RuntimeException("Must provide keystore file path!");
        if (password == null) throw new RuntimeException("Keystore password can't be null!");
        if (!FilenameUtils.getExtension(file).toUpperCase(Locale.ROOT).equals("PKCS12"))
            throw new RuntimeException("Invalid keystore file. file must be a PKCS12.");

        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        char[] pwdArray = password.toCharArray();

        File keyStoreFile = new File(file);
        if (!keyStoreFile.exists())
            EncryptionUtil.createNewKeystore(keyStore, file, pwdArray);

        InputStream is = new FileInputStream(file);

        keyStore.load(is, pwdArray);
        return keyStore;
    }

    private static void createNewKeystore(KeyStore keyStore, String file, char[] pwdArray) throws CertificateException, IOException, NoSuchAlgorithmException {
        keyStore.load(null, pwdArray);
        try (FileOutputStream fos = new FileOutputStream(file)) {
            keyStore.store(fos, pwdArray);
        } catch (IOException | CertificateException | KeyStoreException | NoSuchAlgorithmException e) {
            logger.debug("Could not create keystore!");
            e.printStackTrace();
        }
    }

}
