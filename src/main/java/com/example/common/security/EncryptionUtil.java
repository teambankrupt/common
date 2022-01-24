package com.example.common.security;

import com.example.common.models.AsymmetricKeyCredentials;
import com.example.common.models.CertIdentityInfo;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.security.tools.keytool.CertAndKeyGen;
import sun.security.x509.X500Name;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.Date;
import java.util.Locale;

public class EncryptionUtil implements Serializable {
    private static final long serialVersionUID = 1L;

    private static final Logger logger = LoggerFactory.getLogger(EncryptionUtil.class);
    public static final String BEGIN_CERT = "-----BEGIN CERTIFICATE-----";
    public static final String END_CERT = "-----END CERTIFICATE-----";
    public final static String LINE_SEPARATOR = System.getProperty("line.separator");

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

    public EncryptionUtil storeSymmetricKey(String alias, String key, String password) {
        if (key == null) throw new RuntimeException("Key can't be null");
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
        return EncryptionUtilLoader.INSTANCE;
    }

    public AsymmetricKeyCredentials getKeyCredentials(@NotNull String alias, @NotNull String password) throws KeyStoreException, UnrecoverableKeyException, NoSuchAlgorithmException {
        Certificate cert = keyStore.getCertificate(alias);
        return new AsymmetricKeyCredentials(
                Base64.getEncoder().encodeToString(keyStore.getKey(alias, password.toCharArray()).getEncoded()),
                Base64.getEncoder().encodeToString(cert.getPublicKey().getEncoded()),
                cert
        );
    }

    public String retrieveSymmetricKey(String alias, String password) throws UnrecoverableKeyException, KeyStoreException, NoSuchAlgorithmException {
        Key key = keyStore.getKey(alias, password.toCharArray());
        if (key == null) return null;
        return new String(key.getEncoded());
    }

    public X509Certificate generateCertificate(
            @NotNull String alias, @NotNull String passPhrase,
            @NotNull CertIdentityInfo info, long validityInDays
    )
            throws NoSuchAlgorithmException, NoSuchProviderException, InvalidKeyException, CertificateException, SignatureException, KeyStoreException, IOException {
        CertAndKeyGen keypair = new CertAndKeyGen("RSA", "SHA1WithRSA", null);
        X500Name x500Name = new X500Name(
                info.getCommonName(),
                info.getOrganizationalUnit(),
                info.getOrganization(),
                info.getCity(),
                info.getState(),
                info.getCountry()
        );

        keypair.generate(2048);
        PrivateKey privateKey = keypair.getPrivateKey();

        X509Certificate[] chain = new X509Certificate[1];

        chain[0] = keypair.getSelfCertificate(x500Name, new Date(), validityInDays * 24 * 60 * 60);

        keyStore.setKeyEntry(alias, privateKey, passPhrase.toCharArray(), chain);
        writeKeyStore(keyStore, ksFile, ksPassword.toCharArray(), true);

        return chain[0];
    }

    public static File writeCertToFile(@NotNull final Certificate certificate, @NotNull String fileName) throws CertificateEncodingException, IOException {
        final Base64.Encoder encoder = Base64.getMimeEncoder(64, LINE_SEPARATOR.getBytes());

        final byte[] rawCrtText = certificate.getEncoded();
        final String encodedCertText = new String(encoder.encode(rawCrtText));
        String certStr = BEGIN_CERT + LINE_SEPARATOR + encodedCertText + LINE_SEPARATOR + END_CERT;
        File file = new File(fileName);
        if (!file.exists()) file.createNewFile();
        FileUtils.writeStringToFile(file,certStr, StandardCharsets.UTF_8);
        return file;
    }

}
