package com.example.android_security_hm1;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.location.LocationManager;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.core.app.ActivityCompat;

public class MainActivity extends AppCompatActivity {
    private AppCompatEditText passwordEditText;
    private boolean isFlashOn = false;
    private static final int PERMISSION_REQUEST_LOCATION = 1001;
    boolean isLocationOn = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        passwordEditText = findViewById(R.id.passwordEditText);

        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = this.registerReceiver(null, intentFilter);
        int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        float batteryPct = level / (float) scale;
        int batteryPercentage = (int) (batteryPct * 100);

        // Set password to battery level percentage
        String password = String.valueOf(batteryPercentage);

        //flash
        CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);

        // Check if the device has a flash unit
        if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
            try {
                // Get the ID of the back-facing camera
                String cameraId = cameraManager.getCameraIdList()[0];

                cameraManager.registerTorchCallback(new CameraManager.TorchCallback() {
                    @Override
                    public void onTorchModeChanged(String cameraId, boolean enabled) {
                        super.onTorchModeChanged(cameraId, enabled);

                        // Check the current state of the torch mode
                        isFlashOn = enabled;
                    }
                }, new Handler());

            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }

        // Check for location permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission not granted, request it
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSION_REQUEST_LOCATION);
        } else {
            // Permission already granted, check location service status
            checkLocationService();
        }

        // Button click listener
        findViewById(R.id.loginButton).setOnClickListener(view -> {
            String enteredPassword = passwordEditText.getText().toString();
            if (enteredPassword.equals(password) && isFlashOn && isLocationOn) {
                Toast.makeText(this, "Login successful!!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                intent.putExtra("password", enteredPassword);
                startActivity(intent);
            } else {
                Toast.makeText(this, "Invalid password or flashOFF or Location is off", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Callback for the result of requesting permissions

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, check location service status
                checkLocationService();
            } else {
                // Permission denied
               // Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }


    // Method to check if location service is enabled
    private void checkLocationService() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        boolean isLocationEnabled = locationManager != null &&
                locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        if (isLocationEnabled) {
            // Location service is enabled
            isLocationOn = true;
            Toast.makeText(this, "Location is enabled", Toast.LENGTH_SHORT).show();
        } else {
            // Location service is disabled
            isLocationOn = false;
            Toast.makeText(this, "Location is disabled", Toast.LENGTH_SHORT).show();
        }
    }
}

