package com.dawei.scdpm.parser;

import java.util.Arrays;

/**
 * Created by Dawei on 5/4/2017.
 */
public class Ratio0_1_0 implements Parser{

    @Override
    public byte[] getAccelBytes(byte[] value) {
        return Arrays.copyOf(value, 18);
    }

    @Override
    public byte[] getEcgBytes(byte[] value) {
        return new byte[0];
    }

    @Override
    public byte[] getVolBytes(byte[] value) {
        return new byte[0];
    }
}
