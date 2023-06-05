/*
 * @(#)ArbitraryNumber.java   16/01/07
 *
 * Copyright (c) 2010-2011 Actelion Pharmaceuticals Ltd.
 *
 *  Gewerbestrasse 16, CH-4123 Allschwil, Switzerland
 *  All rights reserved.
 *
 *  This software is the confidential and proprietary information
 *  of Actelion Pharmaceuticals Ltd. ("Confidential Information").  You
 *  shall not disclose such Confidential Information and shall use
 *  it only in accordance with the terms of the license agreement
 *  you entered into with Actelion Pharmaceuticals Ltd.
 *
 *  Author: finkt
 */



package com.actelion.research.knime.utils;

import java.util.Arrays;

public class ArbitraryNumber {
    private int   decimalValue = 0;
    private int[] base;

    //~--- constructors -------------------------------------------------------

    public ArbitraryNumber(int... base) {
        this.base = base;
    }

    //~--- methods ------------------------------------------------------------

    public static void main(String[] args) {
        ArbitraryNumber arbitraryNumber = new ArbitraryNumber(2, 2, 2);
        int             maxNumber       = arbitraryNumber.getMaxNumber();

        System.out.println(maxNumber);

        for (int dec = 0; dec <= maxNumber; dec++) {
            arbitraryNumber.setDecimalValue(dec);
            System.out.println(arbitraryNumber.getDecimalValue() + ": " + Arrays.toString(arbitraryNumber.getInMixedBase()));
        }
    }

    //~--- get methods --------------------------------------------------------

    public int getDecimalValue() {
        return decimalValue;
    }

    public int[] getInMixedBase() {
        int   v = decimalValue;
        int[] r = new int[base.length];

        for (int i = 0; i < base.length; i++) {
            int b = base[i];

            r[i] = v % b;
            v    = (int) Math.floor(v / b);
        }

        return r;
    }

    public int getMaxNumber() {
        int product = 1;

        for (int b : base) {
            product *= b;
        }

        return product - 1;
    }

    //~--- set methods --------------------------------------------------------

    public void setDecimalValue(int decimalValue) {
        this.decimalValue = decimalValue;
    }
}
