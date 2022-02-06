package com.example.common.models;

import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.util.Base64;

public class AsymmetricKeyCredentials {
    private final String privateKey;
    private final String publicKey;
    private final Certificate cert;

    public AsymmetricKeyCredentials(String privateKey, String publicKey, Certificate cert) {
        this.privateKey = privateKey;
        this.publicKey = publicKey;
        this.cert = cert;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public Certificate getCert() {
        return cert;
    }

    public String getCertEncoded() {
        final String LINE_SEPARATOR = System.getProperty("line.separator");
        final Base64.Encoder encoder = Base64.getMimeEncoder(64, LINE_SEPARATOR.getBytes());
        try {
            return encoder.encodeToString(this.cert.getEncoded());
        } catch (CertificateEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public String toString() {
        return "AsymmetricKeyCredentials{" +
                "\nprivateKey=\n" + privateKey + "\n\n" +
                "publicKey=\n" + publicKey + "\n\n" +
                "cert=\n" + this.getCertEncoded() +
                "\n}";
    }
}
