package com.example.common.models;

import java.security.cert.Certificate;

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
}
