package com.example.common.utils;

public class TextUtility {
    private TextUtility() {
    }

    public static String removeSpecialCharacters(String str) {
        if (str == null) return null;
        return str.replaceAll("[^A-Za-z0-9]", "");
    }
}
