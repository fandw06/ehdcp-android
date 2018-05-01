package com.dawei.scdpm;

/**
 * Created by Dawei on 4/18/2018.
 */

public class Util {
    /**
     * Convert byte to unsigned int.
     * Eg: -1 -> 255
     * @param b
     * @return
     */
    public static int byte2int(byte b) {
         int i = b;
         if (i < 0)
             i += 256;
         return i;
    }
}
