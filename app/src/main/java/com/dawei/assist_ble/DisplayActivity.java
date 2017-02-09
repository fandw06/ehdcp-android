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
import android.widget.TextView;
import android.widget.Toast;

public class DisplayActivity extends AppCompatActivity {


    private static final String TAG = "DISPLAY";
    /** Location permission is required when scan BLE devices.*/
    private static final int REQUEST_LOCATION = 0x03;
    public BLEUtil ble;
    public AccelPlot accelPlot;
    public ECGPlot ecgPlot;

    // Components
    public Button bScan;
    public Button bStream;
    public TextView tInfo;

    // status
    public boolean isStreaming = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            verifyLocationPermissions();
        }
        initializeComponents();
        ble = new BLEUtil(this);
        accelPlot = new AccelPlot(this);
        ecgPlot = new ECGPlot(this);
    }


    private void initializeComponents() {
        bScan = (Button) this.findViewById(R.id.scan);
        bScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
                    bStream.setText("Stream");
                }
                else {
                    isStreaming = true;
                    ble.startStreaming();
                    accelPlot.redrawer.start();
                    ecgPlot.redrawer.start();
                    bStream.setText("Stop");
                }
            }
        });
        tInfo = (TextView) this.findViewById(R.id.txt_info);
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
}
