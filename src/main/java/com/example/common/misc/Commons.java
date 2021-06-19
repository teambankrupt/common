package com.example.common.misc;

import java.io.File;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Commons {
    private Commons() {
    }

    public static List<String> deleteImageAndRebuildPaths(List<String> imagePaths, int imageIndex) {
        List<String> newPaths = new ArrayList<>();
        for (int i = 0; i < imagePaths.size(); i++) {
            if (i != imageIndex)
                newPaths.add(imagePaths.get(i));
            else {
                File file = new File(imagePaths.get(imageIndex));
                if (file.exists()) file.delete();
            }
        }
        return newPaths;
    }


    public static <T> T getLastElement(final Iterable<T> elements) {
        final Iterator<T> itr = elements.iterator();
        T lastElement = itr.next();

        while (itr.hasNext()) {
            lastElement = itr.next();
        }

        return lastElement;
    }

    /**
     * Calculates the similarity (a number within 0 and 1) between two strings.
     */
    public static double similarity(String s1, String s2) {
        String longer = s1, shorter = s2;
        if (s1.length() < s2.length()) { // longer should always have greater length
            longer = s2;
            shorter = s1;
        }
        int longerLength = longer.length();
        if (longerLength == 0) {
            return 1.0; /* both strings are zero length */
        }
    /* // If you have Apache Commons Text, you can use it to calculate the edit distance:
    LevenshteinDistance levenshteinDistance = new LevenshteinDistance();
    return (longerLength - levenshteinDistance.apply(longer, shorter)) / (double) longerLength; */
        return (longerLength - editDistance(longer, shorter)) / (double) longerLength;

    }

    // Example implementation of the Levenshtein Edit Distance
    // See http://rosettacode.org/wiki/Levenshtein_distance#Java
    private static int editDistance(String s1, String s2) {
        s1 = s1.toLowerCase();
        s2 = s2.toLowerCase();

        int[] costs = new int[s2.length() + 1];
        for (int i = 0; i <= s1.length(); i++) {
            int lastValue = i;
            for (int j = 0; j <= s2.length(); j++) {
                if (i == 0)
                    costs[j] = j;
                else {
                    if (j > 0) {
                        int newValue = costs[j - 1];
                        if (s1.charAt(i - 1) != s2.charAt(j - 1))
                            newValue = Math.min(Math.min(newValue, lastValue),
                                    costs[j]) + 1;
                        costs[j - 1] = lastValue;
                        lastValue = newValue;
                    }
                }
            }
            if (i > 0)
                costs[s2.length()] = lastValue;
        }
        return costs[s2.length()];
    }

    public static boolean containsSpecialCharacter(String str) {
        Pattern p = Pattern.compile("[^a-z0-9 ]", Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(str);
        return m.find();
    }

    public static String summary(String string, int length) {
        if (string == null) return null;
        return string.substring(0, Math.min(string.length(), length));
    }

    public static Set<String> matchPlaceholders(String text) {
        return matchPlaceholders("\\[(\\w*?)\\]", text);
    }

    public static Set<String> matchPlaceholders(String regex, String text) {
        Matcher m = Pattern.compile(regex).matcher(text);
        HashSet<String> elements = new HashSet<>();
        while (m.find()) {
            elements.add(m.group(1));
        }
        return elements;
    }
}
