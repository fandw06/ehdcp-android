package com.dawei.scdpm.context;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.database.Cursor;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Dawei on 2/12/2018.
 */

public class ContextManager implements SensorEventListener, LocationListener{

    private static final String TAG = "ContextManager";
    private static final String ACCOUNT = "inertiauva@gmail.com";

    private SensorManager mSensorManager;
    private LocationManager mLocationManager;
    private ContentResolver mContentResolver;

    /**
     * Include following 4 sensors:
     * light
     * significant motion sensor
     * accel
     */
    private Sensor mLight;
    private Sensor mMotion;
    private Sensor mAccel;

    private Location currLocation;
    // two min
    private static final int TIME_INTV = 1000 * 60 * 2;

    // Context variables.
    private float light;
    private float[] accel;


    public ContextManager(SensorManager s, LocationManager l, ContentResolver c) {
        mSensorManager = s;
        mLocationManager = l;
        mContentResolver = c;
        mLight = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        mMotion = mSensorManager.getDefaultSensor(Sensor.TYPE_SIGNIFICANT_MOTION);
        mAccel = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        registerSensors();
    }

    public void registerSensors() {
        mSensorManager.registerListener(this, mLight, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mMotion, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mAccel, SensorManager.SENSOR_DELAY_NORMAL);

        try {
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    public void unregisterSensors() {
        mSensorManager.unregisterListener(this);
    }

    public float getLight() {
        return light;
    }

    public void getWeatherInfo() {

    }

    public void getRawGPS() {

    }

    public void getIsIndoor() {

    }

    public List<CalendarEvent> getCalendarToday() {
        List<CalendarEvent> events = new ArrayList<>();
        Cursor cur = null;

        long from = 0L, to = 0L;
        Date d = new Date(System.currentTimeMillis());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        try {
            from = sdf.parse(sdf.format(d)).getTime();
            to = from + 24 * 3600 * 1000;
        } catch (ParseException e) {
            e.printStackTrace();
        }

        // Query all today's events.
        Uri.Builder builder = CalendarContract.Instances.CONTENT_URI.buildUpon();
        ContentUris.appendId(builder, from);
        ContentUris.appendId(builder, to);

        //dummy selector
        String selection = CalendarContract.Instances.BEGIN + "> ?";
        String selectionArgs[] = new String[]{"0"};
        // Submit the query and get a Cursor object back.
        // Sort events according to begin time.
        try {
            cur = mContentResolver.query(
                    builder.build(),
                    CalendarEvent.EVENT_PROJECTION,
                    selection,
                    selectionArgs,
                    CalendarContract.Instances.BEGIN + " ASC");
        } catch (SecurityException e) {
            e.printStackTrace();
        }
        // Use the cursor to step through the returned records
        while (cur.moveToNext()) {
            CalendarEvent e = new CalendarEvent(cur);
        }
        return events;
    }


    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        Sensor source = sensorEvent.sensor;
        float values[] = sensorEvent.values;
        long ts = sensorEvent.timestamp;
        //Log.d(TAG, "Sensor: " + source.getName() + " " + "Values: " + Arrays.toString(values) + ".");
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    @Override
    public void onLocationChanged(Location location) {
        if (isBetterLocation(location, currLocation)) {
            currLocation = location;
            //Log.d(TAG, "Location " + currLocation.toString());
        }
        /*
        else {
            Log.d(TAG, "No update of location. " + location.toString());
        }
        */
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

    /** Determines whether one Location reading is better than the current Location fix
     * @param location  The new Location that you want to evaluate
     * @param currentBestLocation  The current Location fix, to which you want to compare the new one
     */
    protected boolean isBetterLocation(Location location, Location currentBestLocation) {
        if (currentBestLocation == null)
            return true;

        // Check whether the new location fix is newer or older
        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > TIME_INTV;
        boolean isSignificantlyOlder = timeDelta < TIME_INTV;
        boolean isNewer = timeDelta > 0;

        if (isSignificantlyNewer) {
            return true;
        } else if (isSignificantlyOlder) {
            return false;
        }

        // Check whether the new location fix is more or less accurate
        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;

        // Check if the old and new location are from the same provider
        boolean isFromSameProvider = isSameProvider(location.getProvider(),
                currentBestLocation.getProvider());

        // Determine location quality using a combination of timeliness and accuracy
        if (isMoreAccurate) {
            return true;
        } else if (isNewer && !isLessAccurate) {
            return true;
        } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
            return true;
        }
        return false;
    }

    /** Checks whether two providers are the same */
    private boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
            return provider2 == null;
        }
        return provider1.equals(provider2);
    }
}
