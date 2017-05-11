package com.dawei.assist_ble.parser;

/**
 * Parse data from raw bytes.
 * Ratio is ecg:acc:vol
 */
public interface Parser {
    byte[] getAccelBytes(byte[] value);
    byte[] getEcgBytes(byte[] value);
    byte[] getVolBytes(byte[] value);
}
