package com.dawei.scdpm;

import com.dawei.scdpm.scheme.Scheme;
import com.dawei.scdpm.scheme.Scheme4;

import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
public class SchemeUnitTest {

    @Test
    public void testScheme4() throws Exception {
        byte raw[] = new byte[20];
        for(int i = 0; i< 16; i++)
            raw[i] = (byte)i;
        raw[16] = (byte)0b11001001;
        raw[17] = (byte)0b00101101;
        raw[18] = (byte)0b10000111;
        raw[19] = (byte)0b01001001;
        Scheme s = new Scheme4();

        s.processData(raw);
        double ecg[] = s.getEcg();
        System.out.println(Arrays.toString(ecg));

    }
}