package com.mm.mmaps;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;

public class MainActivity extends FragmentActivity
        implements OnMapReadyCallback, SensorEventListener {

    private static final String TAG = "Map";
    private GoogleMap mMap;

    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private LocationRequest locationRequest;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000)
                .setMinUpdateIntervalMillis(2000)
                .build();

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                for (Location newLocation : locationResult.getLocations()) {
                    Log.i(TAG, "Lat: " + newLocation.getLatitude() + ", Lng: " + newLocation.getLongitude());
                    LatLng currentLatLng = new LatLng(newLocation.getLatitude(), newLocation.getLongitude());
                    float gpsBearing = newLocation.getBearing();
                    updateThirdPersonCamera(mMap, currentLatLng, gpsBearing);
                }
            }
        };

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Sensor magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        Sensor accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void updateThirdPersonCamera(GoogleMap googleMap, LatLng currentUserLocation, float currentHeading) {
        float zoom = mMap.getCameraPosition().zoom;
        CameraPosition thirdPersonPosition = new CameraPosition.Builder()
                .target(currentUserLocation)
                .zoom(zoom)
                .tilt(80f)
                .bearing(currentHeading)
                .build();

        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(thirdPersonPosition), 1000, null);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        CompassView compassView = findViewById(R.id.compassView);

        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            float bearing = event.values[0];
            if (compassView != null) {
                compassView.setBearing(bearing);
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        enableUserLocation();
        mMap.getUiSettings().setZoomControlsEnabled(true);
        setupPanButtons();
    }

    private void enableUserLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED) {

            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);

            // Start location updates ONLY after permission is confirmed
            fusedLocationClient.requestLocationUpdates(
                    locationRequest,
                    locationCallback,
                    Looper.getMainLooper()
            );

            fusedLocationClient.getLastLocation().addOnSuccessListener(this, newLocation -> {
                        if (newLocation != null) {
                            LatLng currentLatLng = new LatLng(newLocation.getLatitude(), newLocation.getLongitude());
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f),
                                    1000, null);
                        }
                    }
            );

        } else {
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
