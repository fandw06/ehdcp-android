package com.dawei.scdpm;

import com.dawei.scdpm.context.Weather;
import com.dawei.scdpm.context.WeatherManager;

import org.junit.Test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static org.junit.Assert.*;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
public class ExampleUnitTest {

    public static final String GEO = "57473";

    //@Test
    public void buildURL() throws Exception {
        String geo = WeatherManager.getLocationFromGeo(39.913818f, 116.363625f);
        assertEquals("Test url", geo, GEO);
    }

    //@Test
    public void getWeatherHour() {
        Weather weather[] = WeatherManager.getWeatherHours12(GEO);
        for (Weather w : weather)
            System.out.println(w.toString());
    }

    //@Test
    public void getWeatherCurrent() {
        Weather weather = WeatherManager.getWeatherCurrent(GEO);
        System.out.println(weather.toString());
    }

    //@Test
    public void getWeatherDay() {
        Weather weather[] = WeatherManager.getWeatherDay5(GEO);
        for (Weather w : weather)
            System.out.println(w.toString());
    }

    @Test
    public void getCalendar() {
        /*
        // Get today's unix.
        long unix = System.currentTimeMillis();
        System.out.println(unix);
        Date d = new Date(unix);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        try {
            System.out.println(sdf.format(d));
            System.out.println(sdf.parse(sdf.format(d)).getTime());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        */
    }
}