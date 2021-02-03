package com.example.common.utils;

import org.apache.commons.lang3.RandomStringUtils;

import java.math.BigInteger;
import java.security.SecureRandom;

public class SessionIdentifierGenerator {
    private static final SecureRandom random = new SecureRandom();

    public static String nextSessionId() {
        return new BigInteger(130, random).toString(32);
    }

    public String nextPassword() {
        return new BigInteger(130, random).toString(32);
    }

    public static int generateOTP() {
        return 100000 + random.nextInt(89999);
    }

    public static String alphanumeric(int size) {
        return RandomStringUtils.randomAlphanumeric(size);
    }

}
