package com.dawei.assist_ble;

import android.util.Log;

import java.util.Arrays;

/**
 * Created by Dawei on 2/9/2017.
 */
public class IntervalStat {

    private final String name;
    private long lastTs;
    private int numOfIntervals;
    private int[] interval;
    private boolean displayResults;
    private int NUMBER_OF_INV;
    private static final String TAG = "BLE_UTIL";
    /* Timeout 5s */
    private final int TIMEOUT = 5000;

    private long sum = 0;
    private int max = -1;
    private int min = TIMEOUT;

    public IntervalStat(String name, int num) {
        this.NUMBER_OF_INV = num;
        this.name = name;
        interval = new int[NUMBER_OF_INV];
        lastTs = -1;
        numOfIntervals = 0;
        displayResults = false;
    }

    public void update(long ts) {
        // The statistics is done.
        if (numOfIntervals >= NUMBER_OF_INV) {
            if (!displayResults) {
                displayResults = true;
                printResults();
            }
            return;
        }
        // Beginning point
        if (lastTs == -1 || ts - lastTs > TIMEOUT) {
            lastTs = ts;
        }
        else {
            int curr = (int)(ts - lastTs);
            interval[numOfIntervals++] = curr;
            lastTs = ts;
            sum += curr;
            if (curr > max)
                max = curr;
            if (curr < min)
                min = curr;
        }
    }

    public double sd() {
        if (NUMBER_OF_INV != numOfIntervals)
            return -1;
        double s = 0;
        double mean = (double)sum/NUMBER_OF_INV;
        for (int i : interval) {
            s += (i-mean)*(i-mean);
        }
        return Math.sqrt(s/(double)NUMBER_OF_INV);
    }
    public void printResults() {
        Log.d(TAG, "Session name: " + name);
        Log.d(TAG, "Raw intervals: " + Arrays.toString(interval));
        Log.d(TAG, "Average: " + (double)(sum)/NUMBER_OF_INV);
        Log.d(TAG, "Max: " + max);
        Log.d(TAG, "Min: " + min);
        Log.d(TAG, "Sd: " + sd());
    }

}
