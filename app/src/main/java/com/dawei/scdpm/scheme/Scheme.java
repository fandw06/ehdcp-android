package com.dawei.scdpm.scheme;

import com.dawei.scdpm.calibrate.Calibrate;

/**
 * Parse data from raw received bytes.
 *
 */
public abstract class Scheme {

    public static final String TAG = "SCHEME";
    public static final int ECG = 0;
    public static final int ACCEL = 1;
    public static final int VOL = 2;
    public static final int TEMP = 3;
    public static final int LIGHT = 4;

    // Calibrate methods for each sensor.
    // Order: Ecg, accel, vol, temperature, light
    public Calibrate calibrates[];

    // The number of data points of each sensor in a packet.
    public int points[];

    // Raw data received from BLE.
    public byte[] raw;

    public Scheme() {
        this.calibrates = new Calibrate[5];
        this.points = new int[5];
    }

    public void processData(byte[] r) {
        this.raw = r;
    }

    private double[] getSensor(byte[] r, Calibrate c) {
        if (c == null)
            return new double[]{};
        int width = c.getBytes();
        int samples = r.length/width;
        double val[] = new double[samples];
        for (int i = 0; i < samples; i++)
            val[i] = c.calibrate(r, i * width);
        return val;
    }


    public SensorData getSensorData() {
        SensorData sd = new SensorData();
        sd.ecg = getSensor(getEcgBytes(), calibrates[ECG]);
        sd.accel = getSensor(getAccelBytes(), calibrates[ACCEL]);
        sd.vol = getSensor(getVolBytes(), calibrates[VOL]);
        sd.temp = getSensor(getTempBytes(), calibrates[TEMP]);
        sd.light = getSensor(getLightBytes(), calibrates[LIGHT]);
        return sd;
    }
    public double[] getEcg() {
        return getSensor(getEcgBytes(), calibrates[ECG]);
    }
    public double[] getAccel() {
        return getSensor(getAccelBytes(), calibrates[ACCEL]);
    }
    public double[] getVol() {
        return getSensor(getVolBytes(), calibrates[VOL]);
    }
    public double[] getTemp() {
        return getSensor(getTempBytes(), calibrates[TEMP]);
    }
    public double[] getLight() {
        return getSensor(getLightBytes(), calibrates[LIGHT]);
    }

    public int[] getPoints() {
        return points;
    }

    // Private methods, get raw bytes for each sensor.
    abstract protected byte[] getEcgBytes();
    abstract protected byte[] getAccelBytes();
    abstract protected byte[] getVolBytes();
    abstract protected byte[] getTempBytes();
    abstract protected byte[] getLightBytes();
}
