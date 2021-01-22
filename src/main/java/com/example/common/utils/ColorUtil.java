package com.example.common.utils;

import java.util.Random;

public class ColorUtil {
    private ColorUtil() {
    }

    public static String generateRandomColorCode(int offset) {
        Random obj = new Random();
        int randNum = obj.nextInt(0xffffff + offset);
        // format it as hexadecimal string
        return String.format("#%06x", randNum);
    }

}
