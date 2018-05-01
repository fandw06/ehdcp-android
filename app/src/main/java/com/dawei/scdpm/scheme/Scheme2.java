package com.dawei.scdpm.scheme;

import com.dawei.scdpm.calibrate.CalibrateADC;
import com.dawei.scdpm.calibrate.CalibrateAccel;
import com.dawei.scdpm.calibrate.CalibrateLight;
import com.dawei.scdpm.calibrate.CalibrateTemp;

/**
 * Created by Dawei on 04/25/2018.
 * Profile, without ECG.
 *
 * Total bytes: 20
 * Sample time(byte order):
 * 0      ax-ay-az
 * 1(3)   ax-ay-az   vol(2B)(6, 7)
 * 2(8)   ax-ay-az
 * 3(11)  ax-ay-az   vol(2B)(14, 15)   temp(2B)(16, 17)   light(2B)(18, 19)
 *
 */

public class Scheme2 extends Scheme {

    public Scheme2() {
        super();
        // Initialize calibrators
        calibrates[Scheme.ECG] = null;
        calibrates[Scheme.ACCEL] = new CalibrateAccel();
        calibrates[Scheme.VOL] = new CalibrateADC(10, true);
        calibrates[Scheme.TEMP] = new CalibrateTemp(2);
        calibrates[Scheme.LIGHT] = new CalibrateLight(2);

        // Ratio
        points[Scheme.ECG] = 0;
        points[Scheme.ACCEL] = 4;
        points[Scheme.VOL] = 2;
        points[Scheme.TEMP] = 1;
        points[Scheme.LIGHT] = 1;
    }

    @Override
    protected byte[] getAccelBytes() {
        byte[] accelData = new byte[12];
        accelData[0] = raw[0];
        accelData[1] = raw[1];
        accelData[2] = raw[2];
        accelData[3] = raw[3];
        accelData[4] = raw[4];
        accelData[5] = raw[5];
        accelData[6] = raw[8];
        accelData[7] = raw[9];
        accelData[8] = raw[10];
        accelData[9] = raw[11];
        accelData[10] = raw[12];
        accelData[11] = raw[13];
        return accelData;
    }

    @Override
    protected byte[] getEcgBytes() {
        return new byte[]{};
    }

    @Override
    protected byte[] getVolBytes() {
        return new byte[]{raw[6], raw[7], raw[14], raw[15]};
    }

    @Override
    protected byte[] getTempBytes() {
        return new byte[]{raw[16], raw[17]};
    }

    @Override
    protected byte[] getLightBytes() {
        return new byte[]{raw[18], raw[19]};
    }
}
