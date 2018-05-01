package com.dawei.scdpm.calibrate;

import com.dawei.scdpm.Util;

/**
 * Created by Dawei on 5/5/2017.
 */
public class CalibrateLight implements Calibrate {

    private int bytes;

    public CalibrateLight(int b) {
        bytes = b;
    }

    @Override
    public double calibrate(byte[] rawData, int from) {

        if (bytes == 1) {
            if (rawData.length != 1)
                return -1;
            int val = Util.byte2int(rawData[from]);
            int exp = val / 16;
            int mantissa = (val & 0x0F);
            return mantissa * Math.pow(2, exp) * 0.72;
        } else if (bytes == 2) {
            if (rawData.length != 2)
                return -1;
            int low = Util.byte2int(rawData[from]);
            int high = Util.byte2int(rawData[from + 1]);
            int exp = high/16;
            int mantissa = (high & 0x0F)*16 + low;
            return mantissa * Math.pow(2, exp) * 0.045;
        } else {
            return -1;
        }
    }

    @Override
    public int getBytes() {
        return bytes;
    }
}
