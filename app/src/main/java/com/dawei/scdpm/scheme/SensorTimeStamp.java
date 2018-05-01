package com.dawei.scdpm.scheme;

public class SensorTimeStamp {

    public long tsEcg[];
    public long tsAccel[];
    public long tsVol[];
    public long tsTemp[];
    public long tsLight[];

    public SensorTimeStamp(SensorData sd, int points[], double sr) {
        long timestamp = System.currentTimeMillis();
        // timestamps
        tsEcg = new long[sd.ecg.length];
        tsAccel = new long[sd.accel.length/3];
        tsVol = new long[sd.vol.length];
        tsTemp = new long[sd.temp.length];
        tsLight = new long[sd.light.length];

        // interval between data points
        sr = sr * 10;
        long iEcg = (long)sr;
        long iAccel = (long)(sr*points[Scheme.ECG]/points[Scheme.ACCEL]);
        long iVol = (long)(sr*points[Scheme.ECG]/points[Scheme.VOL]);
        long iTemp = (long)(sr*points[Scheme.ECG]/points[Scheme.TEMP]);
        long iLight = (long)(sr*points[Scheme.ECG]/points[Scheme.LIGHT]);

        for (int i = 0; i<sd.accel.length/3; i++) {
            if (i == 0)
                tsAccel[i] = timestamp;
            else
                tsAccel[i] = tsAccel[i-1] +iAccel;
        }
        for (int i = 0; i<sd.ecg.length; i++) {
            if (i == 0)
                tsEcg[i] = timestamp;
            else
                tsEcg[i] = tsEcg[i-1] +iEcg;
        }
        for (int i = 0; i<sd.vol.length; i++) {
            if (i == 0)
                tsVol[i] = timestamp;
            else
                tsVol[i] = tsVol[i-1] +iVol;
        }
        for (int i = 0; i<sd.temp.length; i++) {
            if (i == 0)
                tsTemp[i] = timestamp;
            else
                tsTemp[i] = tsTemp[i-1] +iTemp;
        }
        for (int i = 0; i<sd.light.length; i++) {
            if (i == 0)
                tsLight[i] = timestamp;
            else
                tsLight[i] = tsLight[i-1] +iLight;
        }
    }
}
