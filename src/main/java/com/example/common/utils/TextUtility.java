package com.example.common.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TextUtility {
    private TextUtility() {
    }

    public static String removeSpecialCharacters(String str) {
        if (str == null) return null;
        return str.replaceAll("[^A-Za-z0-9]", "");
    }

    public static boolean containsSpecialCharacter(String str) {
        if (str == null) return false;
        Pattern p = Pattern.compile("[^a-z0-9 ]", Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(str);
        return m.find();
    }
}
