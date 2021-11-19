package com.example.common.utils;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;
import org.jetbrains.annotations.NotNull;

import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Validator {
    private Validator() {
    }

    public static boolean nullOrZero(Object object) {
        return object == null || object.equals(0);
    }

    public static boolean nullOrEmpty(String object) {
        return object == null || object.isEmpty();
    }

    public static boolean NOT_NULL_NOT_EMPTY(String text) {
        return text != null && !text.isEmpty();
    }

    public static boolean isValidPhoneNumber(@NotNull String region, @NotNull String phone) {
        PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();

        Phonenumber.PhoneNumber phoneNumber = null;
        try {
            //BD is default country code for Bangladesh (used for number without 880 at the begginning)
            phoneNumber = phoneUtil.parse(phone, region);
        } catch (NumberParseException e) {
            System.err.println("NumberParseException was thrown: " + e.toString());
        }
        return phoneUtil.isValidNumber(phoneNumber); // returns true
    }

    public static boolean isValidEmail(@NotNull String email) {
        return isValid(email, "^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@((\\[[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\.[0-9]{1,3}\\])|(([a-zA-Z\\-0-9]+\\.)+[a-zA-Z]{2,}))$");
    }

    public static boolean isValidUsername(String username) {
        return isValid(username, "^[a-zA-Z0-9]([._-](?![._-])|[a-zA-Z0-9]){3,18}[a-zA-Z0-9]$");
    }

    private static boolean isValid(String input, String regex) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);
        return matcher.matches();
    }

    public static boolean isValidUrl(String url) {
        try {
            new URL(url).toURI();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
