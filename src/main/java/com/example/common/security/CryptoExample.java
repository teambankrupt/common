package com.example.common.security;

import com.example.common.models.AsymmetricKeyCredentials;
import com.example.common.models.CertIdentityInfo;
import org.bouncycastle.operator.OperatorCreationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.util.Base64;

public class CryptoExample {
    private static final Logger logger = LoggerFactory.getLogger(CryptoExample.class);

    private static final String KEYSTORE_FILE = "keystore.PKCS12";
    private static final String KEYSTORE_PASSWORD = "keystore_pass";

    public CryptoExample() {

        // Asymmetric key encryption
        String secretKeyAlias = "secret_key_alias";
        String secretKey = "secret_key";
        String secretKeyPassword = "secret_key_password";

        try {
            logger.info("------------------------------------------------------------------------------");
            logger.info("Storing symmetric key: " + secretKey);
            this.storeSymmetricKey(secretKeyAlias, secretKey, secretKeyPassword);
            logger.info("Stored key. Retrieving from keystore to check if it's working.");
            String key = retrieveSymmetricKey(secretKeyAlias, secretKeyPassword);
            logger.info("Secret Key from KeyStore: " + key + "\n\n");

        } catch (KeyStoreException | UnrecoverableKeyException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        // Asymmetric key encryption
        String credentialAlias = "xyz_credentials";
        String credentialPassword = "xyz_credential_password";
        String certFileName = credentialAlias + "_cert.pem";

        try {
            // generate private key and certificate
            logger.info("*** Generating asymmetric key credentials. ***");
            Certificate cert = this.generateCertificate(credentialAlias, credentialPassword);
            logger.info("Cert Generated. \nPublic Key: " + Base64.getEncoder().encodeToString(cert.getPublicKey().getEncoded()) + "\nEncoded: " + Base64.getEncoder().encodeToString(cert.getEncoded()));
            logger.info("Credentials generated. Creating certificate file..");
            File file = this.getCertificateFile(credentialAlias, credentialPassword, certFileName);
            logger.info("Cert path: " + file.getAbsolutePath());

            // get credentials from keystore
            logger.info("*** Fetching credentials from KeyStore to check if it's working. ***");
            AsymmetricKeyCredentials credentials = this.getAsymmetricKeyCredentials(credentialAlias, credentialPassword);
            logger.info("---Credentials---");
            logger.info("\n" + credentials.toString() + "\n\nSigning a message for test purpose.");

            // Digital signature
            String message = "This message should be signed.";
            logger.info("Message: " + message);
            String signature = this.sign(credentialAlias, credentialPassword, message);
            logger.info("Signature for message: " + signature);
            logger.info("Initiate signature verification..");
            boolean verified = this.verifySign(credentialAlias, signature, message);
            logger.info("Verified: " + verified);

            // Public Key Encryption
            logger.info("*** Testing public key encryption ***");
            message = "This is the message to be encrypted!";
            logger.info("Message to encrypt: " + message);
            byte[] encrypted = encrypt(credentialAlias, message.getBytes(StandardCharsets.UTF_8));
            logger.info("Encrypted message: " + Base64.getEncoder().encodeToString(encrypted));
            logger.info("Decrypting..");
            byte[] decrypted = decrypt(credentialAlias, credentialPassword, encrypted);
            logger.info("Decrypted message: " + new String(decrypted));
            logger.info("------------------------------------------------------------------------------");


        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    private byte[] encrypt(String alias, byte[] dataToEncrypt) throws Exception {
        CryptoUtility cryptoUtility = CryptoUtility.with(KEYSTORE_FILE, KEYSTORE_PASSWORD);
        Certificate cert = cryptoUtility.getCert(alias);
        return cryptoUtility
                .encrypt(cert.getPublicKey().getEncoded(), dataToEncrypt);
    }

    private byte[] decrypt(String alias, String password, byte[] encryptedData) throws Exception {
        CryptoUtility cryptoUtility = CryptoUtility.with(KEYSTORE_FILE, KEYSTORE_PASSWORD);
        Key key = cryptoUtility.getKey(alias, password);
        return cryptoUtility
                .decrypt(key.getEncoded(), encryptedData);
    }

    /*
    SYMMETRIC KEY ENCRYPTION
     */

    /*
     * Store symmetric key to keystore
     * method with(file,pass) initializes keystore and use it for operations
     * Creates new keystore if not exists
     */
    private void storeSymmetricKey(String alias, String key, String password) throws KeyStoreException {
        CryptoUtility
                .with(KEYSTORE_FILE, KEYSTORE_PASSWORD)
                .storeSymmetricKey(alias, key, password);
    }

    /*
     * Retrieve a symmetric key from keystore
     */
    private String retrieveSymmetricKey(String alias, String password) throws KeyStoreException, UnrecoverableKeyException, NoSuchAlgorithmException {
        return CryptoUtility
                .with(KEYSTORE_FILE, KEYSTORE_PASSWORD)
                .retrieveSymmetricKey(alias, password);
    }


    /*
    ASYMMETRIC KEY ENCRYPTION
     */

    /*
     * Generates a private key, certificate containing the public key
     * Stores private key and certificate in keystore
     */
    private Certificate generateCertificate(String alias, String password) throws KeyStoreException, CertificateException, IOException, NoSuchAlgorithmException, OperatorCreationException, NoSuchProviderException {
        CertIdentityInfo info = new CertIdentityInfo("www.xyz.com", "XYZ Limited", "XYZ Department", "321/3, XYZ Avenue", "Dhaka", "Dhaka", "Bangladesh", "userid");
        return CryptoUtility
                .with(KEYSTORE_FILE, KEYSTORE_PASSWORD)
                .generateCert(alias, password, 12, info);

    }

    /*
     * Fetch an existing credential from keystore, containing private key, public key and cert
     */
    private AsymmetricKeyCredentials getAsymmetricKeyCredentials(String alias, String password) throws KeyStoreException, UnrecoverableKeyException, NoSuchAlgorithmException {
        return CryptoUtility
                .with(KEYSTORE_FILE, KEYSTORE_PASSWORD)
                .getKeyCredentials(alias, password);
    }


    /*
     * Get certificate file for a key
     */
    private File getCertificateFile(String alias, String password, String fileName) throws KeyStoreException, UnrecoverableKeyException, NoSuchAlgorithmException, CertificateEncodingException, IOException {
        AsymmetricKeyCredentials credentials = CryptoUtility.with(KEYSTORE_FILE, KEYSTORE_PASSWORD).getKeyCredentials(alias, password);
        return CryptoUtility
                .with(KEYSTORE_FILE, KEYSTORE_PASSWORD)
                .writeCertToFile(credentials.getCert(), fileName);
    }

    /*
     * Digitally sign a message with private key
     */
    private String sign(String alias, String password, String message) throws KeyStoreException, UnrecoverableKeyException, NoSuchAlgorithmException, SignatureException, InvalidKeyException {
        byte[] signature = CryptoUtility
                .with(KEYSTORE_FILE, KEYSTORE_PASSWORD)
                .sign(alias, password, message.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(signature);
    }

    /*
     * Digitally sign a message with private key
     */
    private boolean verifySign(String alias, String signature, String message) throws KeyStoreException, NoSuchAlgorithmException, SignatureException, IOException, InvalidKeyException {
        return CryptoUtility
                .with(KEYSTORE_FILE, KEYSTORE_PASSWORD)
                .verifySignature(alias, Base64.getDecoder().decode(signature), message.getBytes(StandardCharsets.UTF_8));
    }


}
