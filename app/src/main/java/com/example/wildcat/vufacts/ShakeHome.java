package com.example.wildcat.vufacts;

import android.content.pm.PackageManager;
import android.hardware.SensorManager;
import android.location.Location;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.squareup.seismic.ShakeDetector;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

//import com.example.wildcat.vufacts.Building;

public class ShakeHome extends AppCompatActivity implements
        ShakeDetector.Listener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    public double currentLatitude, currentLongitude;
    private int REQUEST_LOCATION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shake_home);

        // Image of building to display only after coordinates gathered
        ImageView image = (ImageView) findViewById(R.id.imageView);

        // prep google play services
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        // prep shake function
        SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        ShakeDetector sd = new ShakeDetector(this);
        sd.start(sensorManager);

        // prep building locations
        int count = 0;
        String building, latitude, longitude;
        double latCast, longCast;
        ArrayList<Building> buildingList = new ArrayList<Building>();

        try {
            JSONArray jArray = new JSONArray(loadJSON("buildings.json"));

            while(count < jArray.length()) {
                JSONObject buildingObject = jArray.getJSONObject(count);
                building = buildingObject.getString("Building");
                latitude = buildingObject.getString("Latitude");
                longitude = buildingObject.getString("Longitude");

                try {
                    latCast = Double.parseDouble(latitude);
                    longCast = Double.parseDouble(longitude);
                    buildingList.add(new Building(building, latCast, longCast));

                } catch (NumberFormatException e) {
                    System.out.println("Number format exception occurred...");
                }

                count++;
            }

        } catch (JSONException j) {
            System.out.println("JSON Exception occurred...");
        }

    }

    public void hearShake() {
        Toast.makeText(this, "Shook!", Toast.LENGTH_SHORT).show();
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(Bundle conHint) {

        // If "Yes" hasn't been given by the user, request permissions
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION);
            mGoogleApiClient.disconnect();
        } else {
            // Permission has already been granted, we're good to go
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

            if (mLastLocation != null) {
                TextView building = (TextView) findViewById(R.id.buildingName);
                TextView fact = (TextView) findViewById(R.id.factText);

                currentLatitude = mLastLocation.getLatitude();
                currentLongitude = mLastLocation.getLongitude();



            } else {
                Toast.makeText(this, "No location detected.", Toast.LENGTH_SHORT).show();
            }

            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.i("MainActivity", "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == REQUEST_LOCATION) {
            if(grantResults.length == 1
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                mGoogleApiClient.connect();
            } else {
                // Permission was denied
            }
        }
    }

    @Override
    public void onConnectionSuspended(int cause) {
        Log.i("MainActivity", "Connection suspended");
    }

    public String loadJSON(String jsonFile) {
        String json;

        try {
            InputStream inputStream = getAssets().open(jsonFile);
            int size = inputStream.available();
            byte[] buffer = new byte[size];
            inputStream.read(buffer);
            inputStream.close();
            json = new String(buffer, "UTF-8");

        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }

        return json;
    }
}















