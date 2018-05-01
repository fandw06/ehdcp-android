package com.dawei.scdpm.calibrate;

/**
 * Created by Dawei on 2/15/2017.
 */
public class CalibrateAccel implements Calibrate {

    private static double FULL_SCALE = 2.0;
    private static double NUMBER_OF_LEVELS = 256;

    @Override
    public double calibrate(byte[] rawData, int from) {
        return (double)rawData[from] * FULL_SCALE * 2/NUMBER_OF_LEVELS;
    }

    @Override
    public int getBytes() {
        return 1;
    }
}
