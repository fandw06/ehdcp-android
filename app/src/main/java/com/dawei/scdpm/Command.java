package com.dawei.scdpm;

/**
 * Created by Dawei on 2/12/2018.
 */

public interface Command {

    /**
     * Basic commands:
     * 0x00: Stream, 2B
     *      STREAM [START|STOP]
     *
     * 0x01: change sampling interval of a sensor, 4B
     *      CHANGE_SR [SENSOR_TYPE] [value(2B)]
     *
     * 0x02: change BLE connection interval, 3B
     *      CHANGE_CONN [value(2B)]
     *      the value range is [0x000f, 0x0526] which is [30(15)ms, 1331ms]
     *
     * 0x03: change data scheme, 2B
     *      CHANGE_SCHEME [value(1B)]
     *      the value is the scheme index
     *
     */
    byte STREAM = 0x00;
    byte CHANGE_SR = 0x01;
    byte CHANGE_CONN = 0x02;
    byte CHANGE_SCHEME = 0x03;

    /**
     * Sensor type:
     */
    byte ACCEL = 0x00;
    byte ECG = 0x01;
    byte VOL = 0x02;
    byte LIGHT = 0x03;
    byte TEMP = 0x04;

    byte START = 0x01;
    byte STOP = 0x00;

    int MAX_CONN = 0x0526;
    int MIN_CONN = 0x000f;
    int MAX_INTV = 60000;
    int MIN_INTV = 1;

    void sendCommand(String name, byte cmd[]);
}
