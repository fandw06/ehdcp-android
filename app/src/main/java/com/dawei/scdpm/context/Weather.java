package com.dawei.scdpm.context;

/**
 * Created by Dawei on 2/13/2018.
 */

public class Weather {

    // Hourly number
    public int hour;
    // Unit: F
    public float temperature;
    // See: https://developer.accuweather.com/weather-icons
    public int condition;

    public Weather(int h, float t, int c) {
        this.hour = h;
        this.temperature = t;
        this.condition = c;
    }

    @Override
    public String toString() {
        return "Hour: " + hour + " Temperature: " + temperature + " Condition: " + condition;
    }
}
