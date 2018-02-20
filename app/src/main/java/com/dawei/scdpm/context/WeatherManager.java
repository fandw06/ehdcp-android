package com.dawei.scdpm.context;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.MalformedURLException;
import java.net.URL;



/**
 * Get weather information from AccuWeather.com
 */

public class WeatherManager {

    private static final String APP_KEY = "LbQ3I1hGAVbfGYszdduj1iYyexpMA7we";
    private static final String WEBSITE = "http://dataservice.accuweather.com/";

    public enum HORIZON{HOURLY, DAILY, CURRENT}

    public static String getLocationFromGeo(float lat, float lon) {
        // Example url:
        // "http://dataservice.accuweather.com/locations/v1/cities/geoposition/search?apikey=LbQ3I1hGAVbfGYszdduj1iYyexpMA7we&q=39.913818%2C116.363625"
        URL url = null;
        try {
            url = new URL(String.format("%slocations/v1/cities/geoposition/search?apikey=%s&q=%.5f%%2C%.5f", WEBSITE, APP_KEY, lat, lon));
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readTree(url);
            JsonNode geo = node.get("Key");
            return geo.asText();
        } catch (Exception e) {
            System.out.println("Cannot create json.");
            e.printStackTrace();
        }
        return "";
    }

    /**
     *  Weather[12]
     */
    public static Weather[] getWeatherHours12(String loc) {
        URL url = null;
        Weather weatherHourly[] = new Weather[12];
        try {
            url = new URL(buildForecastURL(HORIZON.HOURLY, 12, loc));
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(url);
            for (int i = 0; i < 12; i++) {
                int hour = Integer.valueOf(root.get(i).get("DateTime").asText().substring(11, 13));
                int condition = root.get(i).get("WeatherIcon").asInt();
                int temp = root.get(i).get("Temperature").get("Value").asInt();
                weatherHourly[i] = new Weather(hour, temp, condition);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return weatherHourly;
    }

    /**
     *  Weather
     */
    public static Weather getWeatherCurrent(String loc) {
        URL url = null;
        try {
            url = new URL(buildForecastURL(HORIZON.CURRENT, 0, loc));
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(url);
            int hour = Integer.valueOf(root.get(0).get("LocalObservationDateTime").asText().substring(11, 13));
            int condition = root.get(0).get("WeatherIcon").asInt();
            int temp = root.get(0).get("Temperature").get("Imperial").get("Value").asInt();
            return new Weather(hour, temp, condition);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public static Weather[] getWeatherDay5(String loc) {
        URL url = null;
        Weather weatherDayly[] = new Weather[5];
        try {
            url = new URL(buildForecastURL(HORIZON.DAILY, 5, loc));
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(url);
            for (int i = 0; i < 5; i++) {
                int hour = -1;
                int condition = root.get("DailyForecasts").get(i).get("Day").get("Icon").asInt();
                int temp = root.get("DailyForecasts").get(i).get("Temperature").get("Maximum").get("Value").asInt();
                weatherDayly[i] = new Weather(hour, temp, condition);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return weatherDayly;
    }

    private static String buildForecastURL(HORIZON h, int num, String loc) {
        // "http://dataservice.accuweather.com/forecasts/v1/hourly/12hour/57473?apikey=LbQ3I1hGAVbfGYszdduj1iYyexpMA7we"
        if (h == HORIZON.HOURLY)
            return String.format("%sforecasts/v1/hourly/%dhour/%s?apikey=%s", WEBSITE, num, loc ,APP_KEY);
        else if (h == HORIZON.DAILY)
            return String.format("%sforecasts/v1/daily/%dday/%s?apikey=%s", WEBSITE, num, loc, APP_KEY);
        else if (h == HORIZON.CURRENT)
            return String.format("%scurrentconditions/v1/%s?apikey=%s", WEBSITE, loc, APP_KEY);
        else
            return "";
    }
}
