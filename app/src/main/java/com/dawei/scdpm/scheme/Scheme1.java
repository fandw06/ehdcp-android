package com.dawei.scdpm.scheme;

import com.dawei.scdpm.calibrate.CalibrateADC;
import com.dawei.scdpm.calibrate.CalibrateAccel;
import com.dawei.scdpm.calibrate.CalibrateLight;
import com.dawei.scdpm.calibrate.CalibrateTemp;

/**
 * Created by Dawei on 04/25/2018.
 *
 * Total bytes: 20
 * Sample time(byte order):
 * 0      ecg
 * 1      ecg
 * 2      ecg
 * 3      ecg  ax-ay-az
 * 4(7)   ecg
 * 5(8)   ecg
 * 6(9)   ecg
 * 7(10)  ecg  ax-ay-az vol temp(2B) light ecg-LSB(2B)
 *
 * ECG is 10b long, and the previous 8 samples use 1B, appending all 2b*8 = 2B LSB in the end.
 *
 */

public class Scheme1 extends Scheme {

    public Scheme1() {
        super();
        // Initialize calibrators
        calibrates[Scheme.ECG] = new CalibrateADC(10, false);
        calibrates[Scheme.ACCEL] = new CalibrateAccel();
        calibrates[Scheme.VOL] = new CalibrateADC(8, true);
        calibrates[Scheme.TEMP] = new CalibrateTemp(2);
        calibrates[Scheme.LIGHT] = new CalibrateLight(1);

        // Ratio
        points[Scheme.ECG] = 8;
        points[Scheme.ACCEL] = 2;
        points[Scheme.VOL] = 1;
        points[Scheme.TEMP] = 1;
        points[Scheme.LIGHT] = 1;
    }

    @Override
    protected byte[] getAccelBytes() {
        byte[] accelData = new byte[6];
        accelData[0] = raw[4];
        accelData[1] = raw[5];
        accelData[2] = raw[6];
        accelData[3] = raw[11];
        accelData[4] = raw[12];
        accelData[5] = raw[13];
        return accelData;
    }

    @Override
    protected byte[] getEcgBytes() {
        byte[] ecgData = new byte[16];
        byte low = raw[18];
        byte high = raw[19];
        ecgData[0] = raw[0];
        ecgData[1] = (byte)(low & 0x03);
        ecgData[2] = raw[1];
        ecgData[3] = (byte)((low >> 2) & 0x03);
        ecgData[4] = raw[2];
        ecgData[5] = (byte)((low >> 4) & 0x03);
        ecgData[6] = raw[3];
        ecgData[7] = (byte)((low >> 6) & 0x03);
        ecgData[8] = raw[7];
        ecgData[9] = (byte)(high & 0x03);
        ecgData[10] = raw[8];
        ecgData[11] = (byte)((high >> 2) & 0x03);
        ecgData[12] = raw[9];
        ecgData[13] = (byte)((high >> 4) & 0x03);
        ecgData[14] = raw[10];
        ecgData[15] = (byte)((high >> 6) & 0x03);
        return ecgData;
    }

    @Override
    protected byte[] getVolBytes() {
        return new byte[]{raw[14]};
    }

    @Override
    protected byte[] getTempBytes() {
        return new byte[]{raw[15], raw[16]};
    }

    @Override
    protected byte[] getLightBytes() {
        return new byte[]{raw[17]};
    }
}
