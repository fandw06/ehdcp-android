package com.dawei.assist_ble.plot;

/**
 * Created by Dawei on 2/15/2017.
 */
public class CalibrateADC implements Calibrate {

    private static double FULL_SCALE = 3.0;
    private static double NUMBER_OF_LEVELS = 1024.0;

    @Override
    public double calibrate(byte[] rawData) {
        if (rawData.length != 2)
            return -1;
        int val = 0;
        if (rawData[0] < 0)
            val += 256 * (rawData[0] + 256);
        else
            val += 256 * rawData[0];
        if (rawData[1] < 0)
            val += rawData[1]+256;
        else
            val += rawData[1];
        return (double)val * FULL_SCALE/NUMBER_OF_LEVELS;
    }
}
