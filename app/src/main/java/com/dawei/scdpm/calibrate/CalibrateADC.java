package com.dawei.scdpm.calibrate;

import static com.dawei.scdpm.Util.byte2int;

/**
 * Created by Dawei on 2/15/2017.
 */
public class CalibrateADC implements Calibrate {

    private int bits;
    private boolean range;
    private double FULL_SCALE;
    private double NUMBER_OF_LEVELS;

    private static final double SCALE_12 = 1.2;
    private static final double SCALE_36 = 3.6;
    private static double BITS_8 = 256.0;
    /**
     * 10 bits format:
     * MSB       LSB
     * Byte0     Byte1
     * xxxxxxxx  000000xx
     */
    private static double BITS_10 = 1024.0;

    /**
     *
     * @param b 10 bits or 8 bits
     * @param full full range is 3.6V, otherwise 1.2V
     */
    public CalibrateADC(int b, boolean full) {
        this.bits = b;
        this.range = full;

        if (bits == 8)
            NUMBER_OF_LEVELS = BITS_8;
        else if (bits == 10)
            NUMBER_OF_LEVELS = BITS_10;
        else
            System.exit(0);

        if (range)
            FULL_SCALE = SCALE_36;
        else
            FULL_SCALE = SCALE_12;
    }

    @Override
    public double calibrate(byte[] rawData, int from) {
        int value;
        // Use 2B data
        if (bits == 10)
            value = byte2int(rawData[from]) * 4 + rawData[from + 1];
        else
            value = byte2int(rawData[from]);
        return (double)value * FULL_SCALE/NUMBER_OF_LEVELS;
    }

    @Override
    public int getBytes() {
        if (bits == 10)
            return 2;
        else
            return 1;
    }
}
