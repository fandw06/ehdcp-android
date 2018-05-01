package com.dawei.scdpm.calibrate;
import com.dawei.scdpm.Util;

/**
 * Created by Dawei on 5/5/2017.
 */
public class CalibrateTemp implements Calibrate {

    private int bytes;

    public CalibrateTemp(int b) {
        bytes = b;
    }

    @Override
    public double calibrate(byte[] rawData, int from) {

        if (bytes == 1) {
            if (rawData.length != 1)
                return -1;
            int value = Util.byte2int(rawData[from]) * 16;
            if (value > 2047)
                value = value - 4096;
            return (double)value * 0.0625;
        } else if (bytes == 2) {
            if (rawData.length != 2)
                return -1;
            int value = Util.byte2int(rawData[from]) * 16 + Util.byte2int(rawData[from + 1])/16;
            if (value > 2047)
                value = value - 4096;
            return (double)value * 0.0625;
        } else {
            return -1;
        }

    }

    @Override
    public int getBytes() {
        return bytes;
    }
}
