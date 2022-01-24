package com.example.common.security;

import com.example.common.models.AsymmetricKeyCredentials;
import com.example.common.models.CertIdentityInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

public class EncExample {
    private static final Logger logger = LoggerFactory.getLogger(EncExample.class);

    private static final String KEYSTORE_FILE = "keystore.PKCS12";
    private static final String KEYSTORE_PASSWORD = "keystore_pass";

    public EncExample() {

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
            logger.info("Generating asymmetric key credentials.");
            X509Certificate cert = this.generateCertificate(credentialAlias, credentialPassword);
            logger.info("Credentials generated. Creating certificate file..");
            File file = this.getCertificateFile(credentialAlias, credentialPassword, certFileName);
            logger.info("Cert path: " + file.getAbsolutePath());

            // get certificate from keystore
            logger.info("Fetching credentials from KeyStore to check if it's working.");
            AsymmetricKeyCredentials credentials = this.getAsymmetricKeyCredentials(credentialAlias, credentialPassword);
            logger.info("---Credentials---");
            logger.info("\n" + credentials.toString());
            logger.info("------------------------------------------------------------------------------");
        } catch (KeyStoreException | CertificateException | NoSuchAlgorithmException | SignatureException | IOException | NoSuchProviderException | InvalidKeyException | UnrecoverableKeyException e) {
            e.printStackTrace();
        }

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
        EncryptionUtil
                .with(KEYSTORE_FILE, KEYSTORE_PASSWORD)
                .storeSymmetricKey(alias, key, password);
    }

    /*
     * Retrieve a symmetric key from keystore
     */
    private String retrieveSymmetricKey(String alias, String password) throws KeyStoreException, UnrecoverableKeyException, NoSuchAlgorithmException {
        return EncryptionUtil
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
    private X509Certificate generateCertificate(String alias, String password) throws KeyStoreException, CertificateException, NoSuchAlgorithmException, SignatureException, IOException, NoSuchProviderException, InvalidKeyException {
        CertIdentityInfo info = new CertIdentityInfo(
                "XYZ Limited",
                "XYZ Org",
                "XYZ Department",
                "Dhaka",
                "Dhaka",
                "Bangladesh"
        );
        return EncryptionUtil
                .with(KEYSTORE_FILE, KEYSTORE_PASSWORD)
                .generateCertificate(alias, password, info, 365);

    }

    /*
     * Fetch an existing credential from keystore, containing private key, public key and cert
     */
    private AsymmetricKeyCredentials getAsymmetricKeyCredentials(String alias, String password) throws KeyStoreException, UnrecoverableKeyException, NoSuchAlgorithmException {
        return EncryptionUtil
                .with(KEYSTORE_FILE, KEYSTORE_PASSWORD)
                .getKeyCredentials(alias, password);
    }


    /*
     * Get certificate file for a key
     */
    public File getCertificateFile(String alias, String password, String fileName) throws KeyStoreException, UnrecoverableKeyException, NoSuchAlgorithmException, CertificateEncodingException, IOException {
        AsymmetricKeyCredentials credentials = EncryptionUtil
                .with(KEYSTORE_FILE, KEYSTORE_PASSWORD)
                .getKeyCredentials(alias, password);
        return EncryptionUtil.writeCertToFile(credentials.getCert(), fileName);
    }
}
