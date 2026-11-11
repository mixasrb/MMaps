package com.mm.mmaps;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;

public class MainActivity extends FragmentActivity
        implements OnMapReadyCallback, LocationListener, SensorEventListener {

    private static final String TAG = "Map";
    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    private Sensor magnetometer;
    private Sensor accelerometer;

    Location oldLocation;
    Location location;

    // 1. Get the Sensor Manager

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize location client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null)
            mapFragment.getMapAsync(this);

        SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        // 2. Access the hardware sensors
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void updateThirdPersonCamera(GoogleMap googleMap, LatLng currentUserLocation, float currentHeading) {
        // Build the 3D perspective camera position
        CameraPosition thirdPersonPosition = new CameraPosition.Builder()
                .target(currentUserLocation) // The current latitude and longitude of the user/vehicle
                .zoom(18f)                   // High zoom level for close-up detail
                .tilt(65f)                   // Pitch the camera (0 = top-down flat, 90 = horizon)
                .bearing(currentHeading)     // Rotate the map based on user's compass heading (0 to 360)
                .build();

        // Smoothly animate the camera to the new position
        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(thirdPersonPosition), 1000, null);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // You can leave this completely empty.
        // It just needs to exist to satisfy the interface compiler contract.
    }

    // 4. Handle updates
    @Override
    public void onSensorChanged(SensorEvent event) {
        CompassView compassView = findViewById(R.id.compassView);

        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            float bearing = event.values[0];
            compassView.setBearing(bearing);

            if (mMap != null) {
                if (location != null) {
                    Log.i(TAG,"Third Person Camera Updated");
                    LatLng currentLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f));
                    updateThirdPersonCamera(mMap, currentLatLng, bearing);
                }
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // Check and request location permissions
        enableUserLocation();

        // Enable default zoom buttons (+ and -) provided by Google Maps
        mMap.getUiSettings().setZoomControlsEnabled(true);
        /*
        // Find compass and pan buttons as before
        CompassView compassView = findViewById(R.id.compassView);
        mMap.setOnCameraMoveListener(() -> {
            float bearing = mMap.getCameraPosition().bearing;
            compassView.setBearing(bearing);
        });
         */

        // Optional: Setup your pan buttons here
        setupPanButtons();
    }

    @Override
    public void onLocationChanged(Location newLocation) {
        Log.i(TAG,"Location Changed");
    }

    private void enableUserLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            // Permission is already granted: turn on the blue location dot layer & my location button
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);

            // Zoom camera directly to the user's current last known location
            fusedLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location newLocation) {
                    location = newLocation;
                }
            });

        } else {
            // Request permission from the user
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, enable location layer safely
                enableUserLocation();
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void setupPanButtons() {
        Button btnPanUp = findViewById(R.id.btnPanUp);
        Button btnPanDown = findViewById(R.id.btnPanDown);
        Button btnPanLeft = findViewById(R.id.btnPanLeft);
        Button btnPanRight = findViewById(R.id.btnPanRight);
        int panAmount = 200;

        if (btnPanUp != null) {
            btnPanUp.setOnClickListener(v -> mMap.animateCamera(CameraUpdateFactory.scrollBy(0, -panAmount)));
            btnPanDown.setOnClickListener(v -> mMap.animateCamera(CameraUpdateFactory.scrollBy(0, panAmount)));
            btnPanLeft.setOnClickListener(v -> mMap.animateCamera(CameraUpdateFactory.scrollBy(-panAmount, 0)));
            btnPanRight.setOnClickListener(v -> mMap.animateCamera(CameraUpdateFactory.scrollBy(panAmount, 0)));
        }
    }
}