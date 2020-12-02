package com.kruserg.labo3;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.renderscript.Allocation;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static final int DEFAULT_UPDATE_INTERVAL = 30;
    public static final int FAST_UPDATE_INTERVAL = 5;
    //views
    TextView tv_long, tv_lat, tv_alt, tv_acc, tv_speed, tv_address;
    SwitchCompat sw_updates, sw_sensor;

    //config file of the api
    LocationRequest locationRequest;

    //Google location API
    FusedLocationProviderClient fusedLocationProviderClient;

    //location callback
    LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            super.onLocationResult(locationResult);
            updateViews(locationResult.getLastLocation());
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //view finder
        tv_long = findViewById(R.id.tv_long);
        tv_lat = findViewById(R.id.tv_lat);
        tv_alt = findViewById(R.id.tv_alt);
        tv_acc = findViewById(R.id.tv_acc);
        tv_speed = findViewById(R.id.tv_speed);
        tv_address = findViewById(R.id.tv_address);
        sw_updates = findViewById(R.id.sw_updates);
        sw_sensor = findViewById(R.id.sw_sensor);

        locationRequest = new LocationRequest();
        locationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        locationRequest.setInterval(1000 * DEFAULT_UPDATE_INTERVAL);
        locationRequest.setFastestInterval(1000 * FAST_UPDATE_INTERVAL);

        sw_sensor.setOnClickListener(v -> switchMode(locationRequest));
        sw_updates.setOnClickListener(v -> activateTracking(locationRequest));

        updateGPS();

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                updateGPS();
            } else {
                Toast.makeText(this, "OK BYE!", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private void switchMode(LocationRequest locationRequest) {
        if (sw_sensor.isChecked()) {
            locationRequest.setPriority(locationRequest.PRIORITY_HIGH_ACCURACY);
            Toast.makeText(this, "High Accuracy is now ON", Toast.LENGTH_SHORT).show();
        } else {
            locationRequest.setPriority(locationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
            Toast.makeText(this, "High Accuracy is now OFF", Toast.LENGTH_SHORT).show();
        }
        updateGPS();
    }

    private void updateGPS() {
       fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(MainActivity.this);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    updateViews(location);
                }
            });
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private void updateViews(Location location) {
        tv_long.setText("Longitude : " + location.getLongitude());
        tv_lat.setText("Latitude : " + location.getLatitude());
        tv_alt.setText("Altitude : " + location.getAltitude());
        tv_acc.setText("Accuracy : " + location.getAccuracy());
        tv_speed.setText("Speed : " + location.getSpeed());

        Geocoder geocoder = new Geocoder(MainActivity.this);
        try{
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(),location.getLatitude(),1);
            tv_address.setText("Address : " + addresses.get(0).getAddressLine(0));
        }catch (Exception e){
            tv_address.setText("Unable to get street address.");
        }
    }


    @SuppressLint("SetTextI18n")
    private void activateTracking(LocationRequest locationRequest) {
        if (sw_updates.isChecked()) {
            Toast.makeText(this, "Tracking is now ON", Toast.LENGTH_SHORT).show();
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
            {
                fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(MainActivity.this);
                fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);
                updateGPS();
                return;
            }



        }else{
            Toast.makeText(this, "Tracking is now OFF", Toast.LENGTH_SHORT).show();
            tv_long.setText("Longitude : "+"OFF");
            tv_lat.setText("Latitude : "+"OFF");
            tv_alt.setText("Altitude : "+"OFF");
            tv_acc.setText("Accuracy : "+"OFF");
            tv_speed.setText("Speed : "+"OFF");
            tv_address.setText("Address : "+"OFF");
            fusedLocationProviderClient.removeLocationUpdates(locationCallback);

        }
    }


}