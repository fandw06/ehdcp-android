package com.dawei.scdpm.parser;

/**
 * Created by Dawei on 5/4/2017.
 */
public class Ratio10_3_1 implements Parser{

    @Override
    public byte[] getAccelBytes(byte[] value) {
        byte[] accelData = new byte[9];
        accelData[0] = value[3];
        accelData[1] = value[4];
        accelData[2] = value[5];
        accelData[3] = value[9];
        accelData[4] = value[10];
        accelData[5] = value[11];
        accelData[6] = value[15];
        accelData[7] = value[16];
        accelData[8] = value[17];
        return accelData;
    }

    @Override
    public byte[] getEcgBytes(byte[] value) {
        byte[] ecgData = new byte[10];
        ecgData[0] = value[0];
        ecgData[1] = value[1];
        ecgData[2] = value[2];
        ecgData[3] = value[6];
        ecgData[4] = value[7];
        ecgData[5] = value[8];
        ecgData[6] = value[12];
        ecgData[7] = value[13];
        ecgData[8] = value[14];
        ecgData[9] = value[18];
        return ecgData;
    }

    @Override
    public byte[] getVolBytes(byte[] value) {
        byte[] volData = new byte[1];
        volData[0] = value[19];
        return volData;
    }
}
