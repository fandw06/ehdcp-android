package com.dawei.assist_ble;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.dawei.assist_ble.parser.Ratio0_1_0;
import com.dawei.assist_ble.parser.Ratio10_3_1;
import com.dawei.assist_ble.plot.CalibrateADC;
import com.dawei.assist_ble.plot.CalibrateAccel;
import com.dawei.assist_ble.plot.CalibrateVol;
import com.dawei.assist_ble.plot.DataPlot;
import com.dawei.assist_ble.plot.PlotConfig;

import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Point;

import java.nio.charset.Charset;
import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class DisplayActivity extends AppCompatActivity {

    private static final String TAG = "DISPLAY";
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
    public RadioButton rbSolar;
    public RadioButton rbString;
    public RadioButton rbTEG;

    public RadioButton rbAmazon;
    public RadioButton rbLab;

    // status
    public boolean isStreaming = false;
    public boolean enabledInfluxDB = false;

    private String serverIP = LAB_IP;
    // Lab PC IP
    private static final String LAB_IP = "128.143.74.10";
    // Amazon cloud IP
    private static final String AMAZON_IP = "34.207.78.17";

    private String dbName = "assist";
    public InfluxDB influxDB;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            verifyLocationPermissions();
        }

        ble = new BLEUtil(this);

        initializePlot();
        initializeComponents();
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

                // Disable type selection.
                rbSolar.setEnabled(false);
                rbString.setEnabled(false);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        tInfo.setText("Scanning ASSIST BLE device...");
                    }
                });
                ble.scanLeDevice(true);
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
                }
                else {
                    isStreaming = true;
                    ble.startStreaming();
                    accelPlot.redrawer.start();
                    ecgPlot.redrawer.start();
                    volPlot.redrawer.start();
                    bStream.setText("Stop");
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

        tInfo = (TextView) this.findViewById(R.id.txt_info);

        rbSolar = (RadioButton) this.findViewById(R.id.r_solar);
        rbSolar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ble.setDemoType(BLEUtil.DEMO.SOLAR);
                cbCloud.setEnabled(true);
                ble.parser = new Ratio10_3_1();
            }
        });

        rbString = (RadioButton) this.findViewById(R.id.r_string);
        rbString.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ble.setDemoType(BLEUtil.DEMO.STRING);
                ble.parser = new Ratio10_3_1();
                cbCloud.setChecked(false);
                cbCloud.setEnabled(false);
            }
        });

        rbTEG = (RadioButton) this.findViewById(R.id.r_teg);
        rbTEG.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ble.setDemoType(BLEUtil.DEMO.TEG);
                ble.parser = new Ratio0_1_0();
                cbCloud.setChecked(false);
                cbCloud.setEnabled(false);
            }
        });

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
    }

    private void verifyLocationPermissions() {
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
