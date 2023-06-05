package com.actelion.research.knime.utils;

public class BitHelpers {


    public static String toBinaryString(int[] values) {
        StringBuilder sb = new StringBuilder();
        for (int i = values.length - 1; i >= 0; i--) {
            sb.append(Integer.toBinaryString(values[i]));
        }
        return sb.toString();
    }

    public static String toHexString(int[] values) {
        StringBuilder sb = new StringBuilder();
        for (int i = values.length - 1; i >= 0; i--) {
            sb.append(Integer.toHexString(values[i]));
        }
        return sb.toString();
    }
}
