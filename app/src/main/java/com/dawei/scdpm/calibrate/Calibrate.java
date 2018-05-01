package com.dawei.scdpm.calibrate;

/**
 * Created by Dawei on 2/15/2017.
 *
 * Convert raw bytes data to sensor value.
 */
public interface Calibrate {
    // [from, to)
    double calibrate(byte[] rawData, int from);
    // Bytes per sample
    int getBytes();
}
