package com.dawei.scdpm.scheme;

import com.dawei.scdpm.calibrate.CalibrateADC;
import com.dawei.scdpm.calibrate.CalibrateAccel;
import com.dawei.scdpm.calibrate.CalibrateLight;
import com.dawei.scdpm.calibrate.CalibrateTemp;

/**
 * Created by Dawei on 04/25/2018.
 *
 * Total bytes: 19
 * Sample time(byte order):
 * 0      ecg
 * 1      ecg
 * 2      ecg
 * 3      ecg
 * 4      ecg  ax-ay-az
 * 5(8)   ecg
 * 6(9)   ecg
 * 7(10)  ecg
 * 8(11)  ecg
 * 9(12)  ecg  ax-ay-az vol temp(2B) light
 *
 *
 */
public class Scheme0 extends Scheme {

    public Scheme0() {
        super();
        // Initialize calibrators
        calibrates[Scheme.ECG] = new CalibrateADC(8, false);
        calibrates[Scheme.ACCEL] = new CalibrateAccel();
        calibrates[Scheme.VOL] = new CalibrateADC(8, true);
        calibrates[Scheme.TEMP] = new CalibrateTemp(2);
        calibrates[Scheme.LIGHT] = new CalibrateLight(1);

        // Ratio
        points[Scheme.ECG] = 10;
        points[Scheme.ACCEL] = 2;
        points[Scheme.VOL] = 1;
        points[Scheme.TEMP] = 1;
        points[Scheme.LIGHT] = 1;
    }

    @Override
    protected byte[] getAccelBytes() {
        byte[] accelData = new byte[6];
        accelData[0] = raw[5];
        accelData[1] = raw[6];
        accelData[2] = raw[7];
        accelData[3] = raw[13];
        accelData[4] = raw[14];
        accelData[5] = raw[15];
        return accelData;
    }

    @Override
    protected byte[] getEcgBytes() {
        byte[] ecgData = new byte[10];
        ecgData[0] = raw[0];
        ecgData[1] = raw[1];
        ecgData[2] = raw[2];
        ecgData[3] = raw[3];
        ecgData[4] = raw[4];
        ecgData[5] = raw[8];
        ecgData[6] = raw[9];
        ecgData[7] = raw[10];
        ecgData[8] = raw[11];
        ecgData[9] = raw[12];
        return ecgData;
    }

    @Override
    protected byte[] getVolBytes() {
        byte[] volData = new byte[1];
        volData[0] = raw[16];
        return volData;
    }

    @Override
    protected byte[] getTempBytes() {
        return new byte[]{raw[17], raw[18]};
    }

    @Override
    protected byte[] getLightBytes() {
        return new byte[]{raw[19]};
    }
}
