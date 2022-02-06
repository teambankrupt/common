package com.example.common.security;

import com.example.common.models.AsymmetricKeyCredentials;
import com.example.common.models.CertIdentityInfo;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class CryptoUtility implements Serializable {
    private static final long serialVersionUID = 1L;

    private static final Logger logger = LoggerFactory.getLogger(CryptoUtility.class);
    private static final String BEGIN_CERT = "-----BEGIN CERTIFICATE-----";
    private static final String END_CERT = "-----END CERTIFICATE-----";
    private final static String LINE_SEPARATOR = System.getProperty("line.separator");
    private final static String SIGNATURE_ALGORITHM = "SHA1WithRSA";
    private final static String PUBLIC_KEY_ALGORITHM = "RSA";
    private final static int KEY_SIZE = 4096;

    private static KeyStore keyStore;
    private static String ksFile;
    private static String ksPassword;

    private static class CryptoUtilityLoader {
        private static final CryptoUtility INSTANCE = new CryptoUtility();
    }

    public static CryptoUtility with(String keyStoreFile, String keyStorePassword) throws KeyStoreException {
        synchronized (CryptoUtility.class) {
            CryptoUtility.ksFile = keyStoreFile;
            CryptoUtility.ksPassword = keyStorePassword;
            keyStore = getKeyStore(keyStoreFile, keyStorePassword);
        }
        return CryptoUtilityLoader.INSTANCE;
    }

    // Protect from serialization should return only one instance
    @SuppressWarnings("unused")
    private CryptoUtility readResolve() {
        return CryptoUtilityLoader.INSTANCE;
    }

    private CryptoUtility() {
    }

    /**
     * Fetch the KeyStore Object if keystore file is found
     * If not found, creates a new keystore and returns it.
     **/
    private static KeyStore getKeyStore(@NotNull String file, @NotNull String password) throws KeyStoreException {
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

    private static void writeKeyStore(@NotNull KeyStore keyStore, @NotNull String file, char[] pwdArray, boolean update) {
        try (FileOutputStream fos = new FileOutputStream(file)) {
            if (!update)
                keyStore.load(null, pwdArray);
            keyStore.store(fos, pwdArray);
        } catch (IOException | CertificateException | KeyStoreException | NoSuchAlgorithmException e) {
            logger.debug("Could not create keystore!");
            e.printStackTrace();
        }
    }

    /**
     * Symmetric Key Cryptography
     */
    public CryptoUtility storeSymmetricKey(@NotNull String alias, @NotNull String key, @NotNull String password) {
        if (key == null) throw new RuntimeException("Key can't be null");
        if (password == null) throw new RuntimeException("Key password can't be null!");

        byte[] keyBytes = key.getBytes();
        SecretKey secretKey = new SecretKeySpec(keyBytes, "AES");
        KeyStore.SecretKeyEntry secret
                = new KeyStore.SecretKeyEntry(secretKey);
        KeyStore.ProtectionParameter pwd
                = new KeyStore.PasswordProtection(password.toCharArray());
        try {
            logger.debug("Storing key entry for alias: " + alias + " | secret: " + secret + " | Passphrase: " + pwd);
            keyStore.setEntry(alias, secret, pwd);
            writeKeyStore(keyStore, ksFile, ksPassword.toCharArray(), true);
        } catch (KeyStoreException e) {
            e.printStackTrace();
        }
        return CryptoUtilityLoader.INSTANCE;
    }

    public String retrieveSymmetricKey(@NotNull String alias, @NotNull String password) throws UnrecoverableKeyException, KeyStoreException, NoSuchAlgorithmException {
        Key key = keyStore.getKey(alias, password.toCharArray());
        if (key == null) return null;
//        return Base64.getEncoder().encodeToString(key.getEncoded());
        return new String(key.getEncoded());
    }


    /**
     * Asymmetric Key Cryptography
     */
    public KeyPair generateKeyPair() throws NoSuchAlgorithmException, NoSuchProviderException {
        // generate a key pair
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(PUBLIC_KEY_ALGORITHM, "BC");
        keyPairGenerator.initialize(KEY_SIZE, new SecureRandom());
        return keyPairGenerator.generateKeyPair();
    }

    public Certificate generateCert(
            @NotNull String alias, @NotNull String passPhrase,
            int validityMonth,
            CertIdentityInfo info) throws OperatorCreationException, CertificateException, IOException, KeyStoreException, NoSuchAlgorithmException, NoSuchProviderException {
        Provider bcProvider = new BouncyCastleProvider();
        Security.addProvider(bcProvider);

        long now = System.currentTimeMillis();
        Date startDate = new Date(now);

        BigInteger certSerialNumber = new BigInteger(Long.toString(now)); // <-- Using the current timestamp as the certificate serial number

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startDate);
        calendar.add(Calendar.MONTH, validityMonth > 0 ? validityMonth : 12);

        Date endDate = calendar.getTime();

        KeyPair keyPair = this.generateKeyPair();
        ContentSigner contentSigner = new JcaContentSignerBuilder(SIGNATURE_ALGORITHM).build(keyPair.getPrivate());

        X500Name dnName = new X500Name(
                "CN=" + info.getCommonName()
                        + ", O=" + info.getOrganization()
                        + ", OU=" + info.getOrganizationalUnit()
                        + ", L=" + info.getCity()
                        + ", ST=" + info.getState()
                        + ", C=" + info.getCountry()
                        + ", UID=" + info.getUserId()
        );
        JcaX509v3CertificateBuilder certBuilder = new JcaX509v3CertificateBuilder(
                dnName, certSerialNumber, startDate, endDate, dnName, keyPair.getPublic()
        );

        // Extensions --------------------------

        // Basic Constraints
        BasicConstraints basicConstraints = new BasicConstraints(true); // <-- true for CA, false for EndEntity

        certBuilder.addExtension(new ASN1ObjectIdentifier("2.5.29.19"), true, basicConstraints); // Basic Constraints is usually marked as critical.

        // -------------------------------------
        Certificate[] certChain = new Certificate[1];
        certChain[0] = new JcaX509CertificateConverter()
                .setProvider(bcProvider)
                .getCertificate(certBuilder.build(contentSigner));

        // Store credentials to keystore
        keyStore.setKeyEntry(alias, keyPair.getPrivate(), passPhrase.toCharArray(), certChain);
        writeKeyStore(keyStore, ksFile, ksPassword.toCharArray(), true);

        return certChain[0];
    }

    public Key getKey(@NotNull String alias, @NotNull String password) throws UnrecoverableKeyException, KeyStoreException, NoSuchAlgorithmException {
        return keyStore.getKey(alias, password.toCharArray());
    }

    public Certificate getCert(String alias) throws KeyStoreException {
        return keyStore.getCertificate(alias);
    }

    public AsymmetricKeyCredentials getKeyCredentials(@NotNull String alias, @NotNull String password) throws KeyStoreException, UnrecoverableKeyException, NoSuchAlgorithmException {
        Certificate cert = keyStore.getCertificate(alias);
        return new AsymmetricKeyCredentials(
                Base64.getEncoder().encodeToString(keyStore.getKey(alias, password.toCharArray()).getEncoded()),
                Base64.getEncoder().encodeToString(cert.getPublicKey().getEncoded()),
                cert
        );
    }

    public File writeCertToFile(@NotNull final Certificate certificate, @NotNull String fileName) throws CertificateEncodingException, IOException {
        final Base64.Encoder encoder = Base64.getMimeEncoder(64, LINE_SEPARATOR.getBytes());

        final byte[] rawCrtText = certificate.getEncoded();
        final String encodedCertText = new String(encoder.encode(rawCrtText));
        String certStr = BEGIN_CERT + LINE_SEPARATOR + encodedCertText + LINE_SEPARATOR + END_CERT;
        File file = new File(fileName);
        if (!file.exists()) file.createNewFile();
        FileUtils.writeStringToFile(file, certStr, StandardCharsets.UTF_8);
        return file;
    }

    /**
     * Digital Signature
     */
    public byte[] sign(@NotNull String keyAlias, @NotNull String keyPassword, @NotNull byte[] data) throws NoSuchAlgorithmException, UnrecoverableKeyException, KeyStoreException, InvalidKeyException, SignatureException {

        Signature sig = Signature.getInstance(SIGNATURE_ALGORITHM);
        sig.initSign((PrivateKey) keyStore.getKey(keyAlias, keyPassword.toCharArray()));
        sig.update(data);
        byte[] signatureBytes = sig.sign();
        Certificate cert = keyStore.getCertificate(keyAlias);
        sig.initVerify(cert.getPublicKey());
        sig.update(data);

        return sig.verify(signatureBytes) ? signatureBytes : null;
    }

    public boolean verifySignature(@NotNull String keyAlias, @NotNull byte[] signature, @NotNull byte[] data) throws KeyStoreException, NoSuchAlgorithmException, InvalidKeyException, SignatureException, IOException {
        Certificate cert = keyStore.getCertificate(keyAlias);
        Signature sig = Signature.getInstance(SIGNATURE_ALGORITHM);
        sig.initVerify(cert.getPublicKey());
        sig.update(data);
        return sig.verify(signature);
    }

    /**
     * Encryption/Decryption
     */
    public byte[] encrypt(byte[] publicKey, byte[] dataToEncrypt)
            throws Exception {

        PublicKey key = KeyFactory.getInstance(PUBLIC_KEY_ALGORITHM)
                .generatePublic(new X509EncodedKeySpec(publicKey));

        Cipher cipher = Cipher.getInstance(PUBLIC_KEY_ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, key);

        return cipher.doFinal(dataToEncrypt);
    }

    public byte[] decrypt(byte[] privateKey, byte[] encryptedData)
            throws Exception {

        PrivateKey key = KeyFactory.getInstance(PUBLIC_KEY_ALGORITHM)
                .generatePrivate(new PKCS8EncodedKeySpec(privateKey));

        Cipher cipher = Cipher.getInstance(PUBLIC_KEY_ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, key);

        return cipher.doFinal(encryptedData);
    }


}
