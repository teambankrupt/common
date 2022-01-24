package com.example.common.security;

import com.example.common.models.AsymmetricKeyCredentials;
import com.example.common.models.CertIdentityInfo;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.OperatorCreationException;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
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

    public AsymmetricKeyCredentials getKeyCredentials(@NotNull String alias, @NotNull String password) throws KeyStoreException, UnrecoverableKeyException, NoSuchAlgorithmException {
        Certificate cert = keyStore.getCertificate(alias);
        return new AsymmetricKeyCredentials(
                Base64.getEncoder().encodeToString(keyStore.getKey(alias, password.toCharArray()).getEncoded()),
                Base64.getEncoder().encodeToString(cert.getPublicKey().getEncoded()),
                cert
        );
    }

    public String retrieveSymmetricKey(@NotNull String alias, @NotNull String password) throws UnrecoverableKeyException, KeyStoreException, NoSuchAlgorithmException {
        Key key = keyStore.getKey(alias, password.toCharArray());
        if (key == null) return null;
//        return Base64.getEncoder().encodeToString(key.getEncoded());
        return new String(key.getEncoded());
    }

    public KeyPair generateKeyPair() throws NoSuchAlgorithmException, NoSuchProviderException {
        // generate a key pair
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(PUBLIC_KEY_ALGORITHM, "BC");
        keyPairGenerator.initialize(4096, new SecureRandom());
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
                + ", UID="+info.getUserId()
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

    public String sign(@NotNull String keyAlias, @NotNull String keyPassword, @NotNull String message) throws NoSuchAlgorithmException, UnrecoverableKeyException, KeyStoreException, InvalidKeyException, SignatureException {
        byte[] data = message.getBytes(StandardCharsets.UTF_8);

        Signature sig = Signature.getInstance(SIGNATURE_ALGORITHM);
        sig.initSign((PrivateKey) keyStore.getKey(keyAlias, keyPassword.toCharArray()));
        sig.update(data);
        byte[] signatureBytes = sig.sign();
        String signature = Base64.getEncoder().encodeToString(signatureBytes);
        logger.debug("Signature:" + signature);

        Certificate cert = keyStore.getCertificate(keyAlias);
        sig.initVerify(cert.getPublicKey());
        sig.update(data);

        return sig.verify(signatureBytes) ? signature : null;
    }

    public boolean verifySign(@NotNull String keyAlias, @NotNull String signature, @NotNull String message) throws KeyStoreException, NoSuchAlgorithmException, InvalidKeyException, SignatureException, IOException {
        Certificate cert = keyStore.getCertificate(keyAlias);
        Signature sig = Signature.getInstance(SIGNATURE_ALGORITHM);
        sig.initVerify(cert.getPublicKey());
        sig.update(message.getBytes(StandardCharsets.UTF_8));
        byte[] signatureBytes = Base64.getDecoder().decode(signature);
        return sig.verify(signatureBytes);
    }

}
