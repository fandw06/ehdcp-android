package com.dawei.scdpm.scheme;

import com.dawei.scdpm.calibrate.CalibrateADC;
import com.dawei.scdpm.calibrate.CalibrateAccel;
import com.dawei.scdpm.calibrate.CalibrateLight;
import com.dawei.scdpm.calibrate.CalibrateTemp;

import java.util.Arrays;

/**
 * Created by Dawei on 04/25/2018.
 *
 * Motion tracker,
 * Total bytes: 18
 * Sample time(byte order):
 * 0      ax-ay-az
 * 1      ax-ay-az
 * 2      ax-ay-az
 * 3      ax-ay-az
 * 4      ax-ay-az
 * 5      ax-ay-az
 *
 */

public class Scheme3 extends Scheme {

    public Scheme3() {
        super();
        // Initialize calibrators
        calibrates[Scheme.ECG] = null;
        calibrates[Scheme.ACCEL] = new CalibrateAccel();
        calibrates[Scheme.VOL] = null;
        calibrates[Scheme.TEMP] = null;
        calibrates[Scheme.LIGHT] = null;

        // Ratio
        points[Scheme.ECG] = 0;
        points[Scheme.ACCEL] = 6;
        points[Scheme.VOL] = 0;
        points[Scheme.TEMP] = 0;
        points[Scheme.LIGHT] = 0;
    }

    @Override
    protected byte[] getAccelBytes() {
        return Arrays.copyOfRange(raw, 0, 18);
    }

    @Override
    protected byte[] getEcgBytes() {
        return new byte[]{};
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
