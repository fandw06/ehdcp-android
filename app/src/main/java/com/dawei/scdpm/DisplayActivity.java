package com.dawei.scdpm;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;


import com.dawei.scdpm.context.CalendarEvent;
import com.dawei.scdpm.context.ContextManager;
import com.dawei.scdpm.plot.CalibrateADC;
import com.dawei.scdpm.plot.CalibrateAccel;
import com.dawei.scdpm.plot.CalibrateVol;
import com.dawei.scdpm.plot.DataPlot;
import com.dawei.scdpm.plot.PlotConfig;

import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Point;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class DisplayActivity extends AppCompatActivity {

    private static final String TAG = "DISPLAY";
    private static final int REQUEST_EXTERNAL_STORAGE_RW = 0x01;
    private static final int REQUEST_READ_CALENDAR = 0x02;
    /** Location permission is required when scan BLE devices.*/
    private static final int REQUEST_LOCATION = 0x03;
    public BLEUtil ble;

    public DataPlot accelPlot;
    public DataPlot ecgPlot;
    public DataPlot volPlot;

    // Components
    public Button bScan;
    public Button bStream;
    public TextView tInfo;
    public CheckBox cbCloud;
    public CheckBox cbLocal;

    public RadioButton rbAmazon;
    public RadioButton rbLab;

    public EditText etConn;
    public EditText etSample;
    public Button bChangeConn;
    public Button bChangeSample;

    public TextView tSensor;

    // status
    public boolean isStreaming = false;
    public boolean enabledInfluxDB = false;
    public boolean enabledLocal = false;

    private String serverIP = LAB_IP;
    // Lab PC IP
    private static final String LAB_IP = "128.143.74.10";
    // Amazon cloud IP
    private static final String AMAZON_IP = "34.207.78.17";

    private String dbName = "scdpm";
    public InfluxDB influxDB;

    private static final String DIR = "scdpm";
    private PrintWriter accelWriter = null;
    private PrintWriter ecgWriter = null;
    private PrintWriter volWriter = null;

    //Sensor
    private SensorManager mSensorManager;
    private LocationManager mLocationManager;
    private ContextManager mContextManager;
    private ContentResolver mContentResolver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            verifyPermissions();
        }

        ble = new BLEUtil(this);

        initializePlot();
        initializeComponents();
        initializeContext();
        influxDB = null;
        Log.d(TAG, Charset.defaultCharset().displayName());
    }

    private void initializePlot() {
        PlotConfig ecgConfig = PlotConfig.builder()
                .setBytesPerSample(1)
                .setName(new String[]{"ecg"})
                .setNumOfSeries(1)
                .setResID(R.id.plot_ecg)
                .setXmlID(new int[]{R.xml.ecg_line_point_formatter})
                .setRedrawFreq(30)
                .setDomainBoundary(new double[]{0, 200})
                .setDomainInc(40.0)
                .setRangeBoundary(new double[]{0, 1.0})
                .setRangeInc(0.2)
                .build();
        ecgPlot = new DataPlot(this, ecgConfig, new CalibrateADC());

        PlotConfig volConfig = PlotConfig.builder()
                .setBytesPerSample(1)
                .setName(new String[]{"vol"})
                .setNumOfSeries(1)
                .setResID(R.id.plot_vol)
                .setXmlID(new int[]{R.xml.vol_line_point_formatter})
                .setRedrawFreq(30)
                .setDomainBoundary(new double[]{0, 200})
                .setDomainInc(40.0)
                .setRangeBoundary(new double[]{0, 3.0})
                .setRangeInc(0.5)
                .build();
        volPlot = new DataPlot(this, volConfig, new CalibrateVol());

        PlotConfig accelConfig = PlotConfig.builder()
                .setBytesPerSample(1)
                .setName(new String[]{"x", "y", "z"})
                .setNumOfSeries(3)
                .setResID(R.id.plot_accel)
                .setXmlID(new int[]{R.xml.accel_x_line_point_formatter, R.xml.accel_y_line_point_formatter, R.xml.accel_z_line_point_formatter})
                .setRedrawFreq(30)
                .setDomainBoundary(new double[]{0, 50})
                .setDomainInc(10.0)
                .setRangeBoundary(new double[]{-2.0, 2.0})
                .setRangeInc(0.5)
                .build();
        accelPlot = new DataPlot(this, accelConfig, new CalibrateAccel());
    }

    private void initializeComponents() {
        bScan = (Button) this.findViewById(R.id.scan);
        bScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tInfo.setText("Scanning scdpm BLE device...");
                    }
                });
                ble.scanLeDevice(true);
                Log.d(TAG, "Scan complete");
            }
        });

        bStream = (Button) this.findViewById(R.id.stream);
        bStream.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isStreaming) {
                    isStreaming = false;
                    ble.stopStreaming();
                    accelPlot.redrawer.pause();
                    ecgPlot.redrawer.pause();
                    volPlot.redrawer.pause();
                    bStream.setText("Stream");
                    if (cbLocal.isChecked())
                        closeWriters();
                }
                else {
                    isStreaming = true;
                    ble.startStreaming();
                    accelPlot.redrawer.start();
                    ecgPlot.redrawer.start();
                    volPlot.redrawer.start();
                    bStream.setText("Stop");
                    if (cbLocal.isChecked())
                        createWriters();
                }
            }
        });

        cbCloud = (CheckBox) this.findViewById(R.id.cb_cloud);
        cbCloud.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                if (!cbCloud.isChecked()) {
                    enabledInfluxDB = false;
                    Log.d(TAG, "disconnected!");
                }
                else {
                    enabledInfluxDB = true;
                    Log.d(TAG, "Connecting Influxdb database...");
                    influxDB = InfluxDBFactory.connect("http://" + serverIP +":8086", "root", "root");
                    Log.d(TAG, "Influxdb database is connected!");
                }
            }
        });

        cbLocal = (CheckBox) this.findViewById(R.id.cb_local);
        cbLocal.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                if (!cbLocal.isChecked()) {
                    enabledLocal = false;
                }
                else {
                    enabledLocal = true;
                }
            }
        });

        tInfo = (TextView) this.findViewById(R.id.txt_info);

        rbLab = (RadioButton) this.findViewById(R.id.r_lab);
        rbLab.setChecked(true);
        rbLab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setToLabIp();
            }
        });

        rbAmazon = (RadioButton) this.findViewById(R.id.r_amazon);
        rbAmazon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setToAmazonIp();
            }
        });


        //debug
        etConn = (EditText) this.findViewById(R.id.txt_conn);
        etSample = (EditText) this.findViewById(R.id.txt_sample);
        bChangeConn = (Button) this.findViewById(R.id.change_conn);
        bChangeSample = (Button) this.findViewById(R.id.change_sample);
        bChangeConn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int val = Integer.valueOf(etConn.getText().toString());
                ble.changeConnPara(val);
            }
        });
        bChangeSample.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int val = Integer.valueOf(etSample.getText().toString());
                ble.changeSamplingRate((byte)1, val);
            }
        });

        tSensor = (TextView) this.findViewById(R.id.txt_sensor);
    }

    private void initializeContext() {
        mSensorManager = (SensorManager) this.getSystemService(Context.SENSOR_SERVICE);
        mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        mContentResolver = this.getContentResolver();
        mContextManager = new ContextManager(mSensorManager, mLocationManager, mContentResolver);

        List<CalendarEvent> list = mContextManager.getCalendarToday();

        List<Sensor> sensors = mSensorManager.getSensorList(Sensor.TYPE_ALL);
        for (Sensor s: sensors)
            Log.d(TAG, s.getName() + " " + s.getVendor() + " " + s.getType() + " " +s.getReportingMode());

        /**
         * Print all required sensors:
         * 1, Significant motion.
         * 2, Temperature.
         * 3, Light
         *
         */

        if (mSensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE) != null){
            Log.d(TAG, "Temperature Sensor: " + mSensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE).getName());
        }
        else {
            Log.d(TAG, "No temperature sensor");
        }

        if (mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT) != null){
            Log.d(TAG, "Light Sensor: " + mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT).getName());
        }
        else {
            Log.d(TAG, "No light sensor");
        }

        if (mSensorManager.getDefaultSensor(Sensor.TYPE_SIGNIFICANT_MOTION) != null){
            Log.d(TAG, "Significant motion Sensor: " + mSensorManager.getDefaultSensor(Sensor.TYPE_SIGNIFICANT_MOTION).getName());
        }
        else {
            Log.d(TAG, "No significant motion sensor");
        }

        if (mSensorManager.getDefaultSensor(Sensor.TYPE_STATIONARY_DETECT) != null){
            Log.d(TAG, "Significant motion Sensor: " + mSensorManager.getDefaultSensor(Sensor.TYPE_STATIONARY_DETECT).getName());
        }
        else {
            Log.d(TAG, "No stationary detect sensor");
        }

    }

    private void verifyPermissions() {
        if (ContextCompat.checkSelfPermission(DisplayActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(DisplayActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION)) {
                Log.d(TAG, "Request user to grant coarse location permission");
            }
            else {
                ActivityCompat.requestPermissions(DisplayActivity.this,
                        new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                        REQUEST_LOCATION);
            }
        }
        if (ContextCompat.checkSelfPermission(DisplayActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(DisplayActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                Log.d(TAG, "Request user to grant write permission");
            }
            else {
                ActivityCompat.requestPermissions(DisplayActivity.this,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        REQUEST_EXTERNAL_STORAGE_RW);
            }
        }
        if (ContextCompat.checkSelfPermission(DisplayActivity.this, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(DisplayActivity.this, Manifest.permission.READ_CALENDAR)) {
                Log.d(TAG, "Request user to grant write permission");
            }
            else {
                ActivityCompat.requestPermissions(DisplayActivity.this,
                        new String[]{Manifest.permission.READ_CALENDAR},
                        REQUEST_READ_CALENDAR);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_LOCATION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "Location permission is granted.");
                } else {
                    Toast.makeText(getApplicationContext(), "Location permission is denied.", Toast.LENGTH_LONG).show();
                    Log.d(TAG, "Location permissions is denied!");
                }
                break;
            }
            case REQUEST_EXTERNAL_STORAGE_RW: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "Write and read permissions are granted.");
                } else {
                    Log.d(TAG, "Write and read permissions are denied!");
                }
                break;
            }
            case REQUEST_READ_CALENDAR: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.d(TAG, "Calendar permissions are granted.");
                } else {
                    Log.d(TAG, "Calendar permissions are denied!");
                }
                break;
            }
            default:
                Log.d(TAG, "Invalid permission request code.");
        }
    }

    public void uploadCloud(byte[] accel, byte[] ecg, byte[] vol) {
        long timestamp = System.currentTimeMillis();
        double calAccel[][] = new double[accel.length/3][3];
        double calEcg[] = new double[ecg.length];
        double calVol[] = new double[vol.length];
        long tsAccel[] = new long[accel.length/3];
        long tsEcg[] = new long[ecg.length];
        long tsVol[] = new long[vol.length];

        long iAccel = 40;
        long iEcg = 133;
        long iVol = 400;
        /**
         * Assign sensor value.
         */
        for (int i = 0; i<accel.length/3; i++) {
            calAccel[i] = new double[3];
            calAccel[i][0] = new CalibrateAccel().calibrate(accel[i*3]);
            calAccel[i][1] = new CalibrateAccel().calibrate(accel[i*3 + 1]);
            calAccel[i][2] = new CalibrateAccel().calibrate(accel[i*3 + 2]);

            if (i == 0)
                tsAccel[i] = timestamp;
            else
                tsAccel[i] = tsAccel[i-1] +iAccel;
            Log.d(TAG, "TS: " + tsAccel[i] + " Value: " + calAccel[i][0] + " " + calAccel[i][1] + " " + calAccel[i][2]);
        }
        for (int i = 0; i<ecg.length; i++) {
            calEcg[i] = new CalibrateADC().calibrate(ecg[i]);
            if (i == 0)
                tsEcg[i] = timestamp;
            else
                tsEcg[i] = tsEcg[i-1] +iEcg;
        }
        for (int i = 0; i<vol.length; i++) {
            calVol[i] = new CalibrateADC().calibrate(vol[i]);
            if (i == 0)
                tsVol[i] = timestamp;
            else
                tsVol[i] = tsVol[i-1] +iVol;
        }
        /**
         * Create data points and write to InfluxDB
         *
         */
        final BatchPoints batchPoints = BatchPoints
                .database(dbName)
                .tag("async", "true")
                .retentionPolicy("autogen")
                .consistency(InfluxDB.ConsistencyLevel.ALL)
                .build();
        Point point[] = new Point[14];

        for (int i = 0; i<accel.length/3; i++) {
            Map<String, Object> fields = new HashMap<>();

            fields.put("acc-x", calAccel[i][0]);
            fields.put("acc-y", calAccel[i][1]);
            fields.put("acc-z", calAccel[i][2]);
            point[i] = Point.measurement("ACCEL")
                    .time(tsAccel[i], TimeUnit.MILLISECONDS)
                    .fields(fields)
                    .build();
            batchPoints.point(point[i]);
        }

        for (int i = 0; i<ecg.length; i++) {
            Map<String, Object> fields = new HashMap<>();
            fields.put("v", calEcg[i]);
            point[i + accel.length/3] = Point.measurement("ECG")
                    .time(tsEcg[i], TimeUnit.MILLISECONDS)
                    .fields(fields)
                    .build();
            batchPoints.point(point[i + accel.length/3]);
        }

        for (int i = 0; i<vol.length; i++) {
            Map<String, Object> fields = new HashMap<>();
            fields.put("v", calVol[i]);
            point[i + accel.length/3 + ecg.length] = Point.measurement("VOL")
                    .time(tsVol[i], TimeUnit.MILLISECONDS)
                    .fields(fields)
                    .build();
            batchPoints.point(point[i + accel.length/3 + ecg.length]);
        }


        new Thread(new Runnable() {
            public void run() {
                influxDB.write(batchPoints);
            }
        }).start();
    }

    public void saveToFile(byte[] accel, byte[] ecg, byte[] vol) {
        long timestamp = System.currentTimeMillis();
        double calAccel[][] = new double[accel.length/3][3];
        double calEcg[] = new double[ecg.length];
        double calVol[] = new double[vol.length];
        long tsAccel[] = new long[accel.length/3];
        long tsEcg[] = new long[ecg.length];
        long tsVol[] = new long[vol.length];

        long iAccel = 40;
        long iEcg = 133;
        long iVol = 400;
        /**
         * Assign sensor value.
         */
        for (int i = 0; i<accel.length/3; i++) {
            calAccel[i] = new double[3];
            calAccel[i][0] = new CalibrateAccel().calibrate(accel[i*3]);
            calAccel[i][1] = new CalibrateAccel().calibrate(accel[i*3 + 1]);
            calAccel[i][2] = new CalibrateAccel().calibrate(accel[i*3 + 2]);

            if (i == 0)
                tsAccel[i] = timestamp;
            else
                tsAccel[i] = tsAccel[i-1] +iAccel;
            Log.d(TAG, "TS: " + tsAccel[i] + " Value: " + calAccel[i][0] + " " + calAccel[i][1] + " " + calAccel[i][2]);
        }
        for (int i = 0; i<ecg.length; i++) {
            calEcg[i] = new CalibrateADC().calibrate(ecg[i]);
            if (i == 0)
                tsEcg[i] = timestamp;
            else
                tsEcg[i] = tsEcg[i-1] +iEcg;
        }
        for (int i = 0; i<vol.length; i++) {
            calVol[i] = new CalibrateADC().calibrate(vol[i]);
            if (i == 0)
                tsVol[i] = timestamp;
            else
                tsVol[i] = tsVol[i-1] +iVol;
        }
        /**
         * Create data points and write to files
         *
         */
        for (int i = 0; i<accel.length/3; i++)
            accelWriter.println(tsAccel[i] + ", " + calAccel[i][0] + ", " + calAccel[i][1] + ", " + calAccel[i][2]);

        for (int i = 0; i<ecg.length; i++)
            ecgWriter.println(tsEcg[i] + ", " + calEcg[i]);

        for (int i = 0; i<vol.length; i++)
            volWriter.println(tsVol[i] + ", " + calVol[i]);
    }

    private void createWriters() {
        File FILES_DIR = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DOCUMENTS), DIR);
        if (!FILES_DIR.exists()) {
            boolean created = FILES_DIR.mkdirs();
            if (created)
                Log.d(TAG, "Create a new dir.");
            else
                Log.d(TAG, "Cannot create " + FILES_DIR.toString());
        }
        Log.d(TAG, "Creating local directory...");
        File local = new File(FILES_DIR, Long.toString(System.currentTimeMillis()));
        if (!local.mkdir()) {
            Log.w("LocalDir", "Local directory is not created!");
        }
        Log.d(TAG, "Creating local directory for current session...");
        try {
            accelWriter = new PrintWriter(
                    new BufferedWriter(
                            new FileWriter(
                                    new File(local, "accel.csv"), true)));
            ecgWriter = new PrintWriter(
                    new BufferedWriter(
                            new FileWriter(
                                    new File(local, "ecg.csv"), true)));
            volWriter = new PrintWriter(
                    new BufferedWriter(
                            new FileWriter(
                                    new File(local, "vol.csv"), true)));
        } catch (Exception e) {
            System.out.println(e);
        }

        Log.d(TAG, "Files are created!");
    }

    private void closeWriters() {
        if (accelWriter != null)
            accelWriter.close();
        if (volWriter != null)
            volWriter.close();
        if (ecgWriter != null)
            ecgWriter.close();
    }

    public void writeInfluxDB(final String name, final byte data[]) {
        new Thread(new Runnable() {

            public void run() {
                Map<String, Object> fields = new HashMap<>();
                for (int i = 0; i< data.length; i++) {
                    fields.put("ch" + i, data[i]);
                }
                Point point = Point.measurement(name)
                        .time(System.currentTimeMillis(), TimeUnit.MILLISECONDS)
                        .fields(fields)
                        .build();
                influxDB.write(dbName, "autogen", point);
            }
        }).start();
    }

    private void setToAmazonIp() {
        this.serverIP = AMAZON_IP;
    }

    private void setToLabIp() {
        this.serverIP = LAB_IP;
    }

}
