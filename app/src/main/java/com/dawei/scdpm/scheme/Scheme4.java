package com.dawei.scdpm.scheme;

import com.dawei.scdpm.calibrate.CalibrateADC;
import com.dawei.scdpm.calibrate.CalibrateAccel;
import com.dawei.scdpm.calibrate.CalibrateLight;
import com.dawei.scdpm.calibrate.CalibrateTemp;

import java.util.Arrays;

/**
 * Created by Dawei on 04/25/2018.
 * ECG Tracker
 * Total bytes: 20
 * Sample time(byte order):
 * 0~16   ecg
 * 17~19  ecg-LSB(2B)
 *
 * ECG is 10b long, and the previous 16 samples use 32b, appending all 2b*16 = 4B LSB in the end.
 *
 */

public class Scheme4 extends Scheme {

    public Scheme4() {
        super();
        // Initialize calibrators
        calibrates[Scheme.ECG] = new CalibrateADC(10, false);
        calibrates[Scheme.ACCEL] = null;
        calibrates[Scheme.VOL] = null;
        calibrates[Scheme.TEMP] = null;
        calibrates[Scheme.LIGHT] = null;

        // Ratio
        points[Scheme.ECG] = 16;
        points[Scheme.ACCEL] = 0;
        points[Scheme.VOL] = 0;
        points[Scheme.TEMP] = 0;
        points[Scheme.LIGHT] = 0;
    }

    @Override
    protected byte[] getAccelBytes() {
        return new byte[]{};
    }

    @Override
    protected byte[] getEcgBytes() {
        byte[] ecgData = new byte[32];

        // MSB 8 bits
        for (int i = 0; i< 16; i++)
            ecgData[i * 2] = raw[i];

        // LSB 2 bits
        for (int i = 0; i< 4; i++) {
            // the raw bytes which store the LSB bits
            byte curr = raw[16+i];
            for (int j = 0; j< 4; j++) {
                // the index of ecg sample
                int index = (i*4+j)*2 + 1;
                // the offset need to move
                int o = 2*j;
                ecgData[index] = (byte)((curr >> o) & 0x03);
            }
        }
        return ecgData;
    }

    @Override
    protected byte[] getVolBytes() {
        return new byte[]{};
    }

    @Override
    protected byte[] getTempBytes() {
        return new byte[]{};
    }

    @Override
    protected byte[] getLightBytes() {
        return new byte[]{};
    }
}
