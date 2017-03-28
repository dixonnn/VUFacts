package com.example.wildcat.vufacts;

import android.content.pm.PackageManager;
import android.hardware.SensorManager;
import android.location.Location;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.vision.text.Text;
import com.squareup.seismic.ShakeDetector;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

public class ShakeHome extends AppCompatActivity implements
        ShakeDetector.Listener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    // Variables for getting current location
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private int REQUEST_LOCATION = 1;
    public ArrayList<Building> buildList = new ArrayList<>();

    /*
    * onCreate
    *
    * Set TextView invisible, only should show once nearby building is recognized.
    * Prepare Google Play Services for finding current location.
    * Prepare shake listener.
    * Load all buildings and their locations into an ArrayList for access once shaken.
    */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shake_home);

        // Set youAre TextView to invisible
        TextView youAre = (TextView) findViewById(R.id.youAre);
        youAre.setVisibility(View.INVISIBLE);

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

        buildList = loadBuildings();
    }

    /*
    * loadJSON(String jsonFile)
    *
    * Uses inputStream to load json file from Assets to String object
    */
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


    /*
    * loadBuildings()
    *
    * Loads buildings into a list of Building objects from buildings.json
    * Called during activity creation
    */
    public ArrayList<Building> loadBuildings() {

        int count;
        String building;
        double latitude, longitude;
        ArrayList<Building> list = new ArrayList<>();

        try {
            // Load in Json list of buildings + locations
            JSONArray jArray = new JSONArray(loadJSON("buildings.json"));

            // Loop through Json Array parsing out Building objects into ArrayList<Building>
            for(count = 0; count < jArray.length(); count++) {

                // This declaration must be within the loop, otherwise
                // setLatitude() and setLongitude() methods update every
                // building in the ArrayList, not just the current element.
                Location buildingLocation = new Location("");

                building = jArray.getJSONObject(count).getString("Building");
                latitude = jArray.getJSONObject(count).getDouble("Latitude");
                longitude = jArray.getJSONObject(count).getDouble("Longitude");

                buildingLocation.setLatitude(latitude);
                buildingLocation.setLongitude(longitude);

                // Add new Building object to ArrayList
                list.add(new Building(building, buildingLocation));
            }

        } catch (JSONException j) {
            System.out.println("JSON Exception occurred...");
        }

        return list;
    }


    /*
    * hearShake()
    *
    * Connects to Google API Client when device is shaken
    */

    public void hearShake() {
        mGoogleApiClient.connect();
    }


    /*
    * onConnected(Bundle conHint)
    *
    * Handles permission requests and LocationServices to get current location
    */
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

            // If a location was properly found, send lat + long to fact fetching method
            if (mLastLocation != null) {
                getFact(mLastLocation);

                // If no location was detected, send toast
            } else {
                Toast.makeText(this, "No location detected.", Toast.LENGTH_SHORT).show();
            }

            mGoogleApiClient.disconnect();
        }
    }


    /*
    * >>>> getFact(Location curLoc)
    *
    * Calculate the distance between current location and each building.
    * If any are within 100 yards, add that building to a list.
    * Choose a building name from that list randomly.
    * Import facts.json, create new JSON Array of only facts about nearby building.
    * Choose random fact from that new array.
    * Set layout objects to proper string/image values
    */
    public void getFact(Location curLoc) {

        TextView youAre = (TextView) findViewById(R.id.youAre);
        TextView buildingName = (TextView) findViewById(R.id.buildingName);
        TextView fact = (TextView) findViewById(R.id.fact);

        // Declare Variables
        int ct1, ct2;
        float distance;
        ArrayList<String> nearNames = new ArrayList<>();
        String nearbyBuilding, buildingFact;

        // Calculate distance between current location and each building.
        // If dist <= 50, add the building's name to a list of Strings.
        for(ct1 = 0; ct1 < buildList.size(); ct1++) {
            distance = curLoc.distanceTo(buildList.get(ct1).getLocation());

            if(distance <= 50) {
                nearNames.add(buildList.get(ct1).getName());
            }
         }

        // If there is more than 1 name in the list, randomly pick one.
        // If there is only 1, set the string to that building's name.
        // If there are 0, throw toast, "None in range."
        if(nearNames.size() > 1) {
            int randBuilding = ThreadLocalRandom.current().nextInt(0, nearNames.size());
            nearbyBuilding = nearNames.get(randBuilding);
        } else if(nearNames.size() == 1){
            nearbyBuilding = nearNames.get(0);
        } else {
            Toast.makeText(this, "No valid buildings nearby!", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            JSONArray allFacts = new JSONArray(loadJSON("facts.json"));
            JSONArray relFacts = new JSONArray();

            // Parse through all facts, hold on to only those with tag_Building == nearbyBuilding
            for(ct2 = 0; ct2 < allFacts.length(); ct2++) {
                if(allFacts.getJSONObject(ct2).getString("Building").equals(nearbyBuilding)) {
                    relFacts.put(allFacts.getJSONObject(ct2));
                }
            }

            // Randomly pick fact from relevant facts
            int randFact = ThreadLocalRandom.current().nextInt(0, relFacts.length());
            buildingFact = relFacts.getJSONObject(randFact).getString("Fact");
            System.out.println("~~~ Finding random fact about " + nearbyBuilding + ": " + relFacts.length() + " facts found.");

            // Make youAre visible, set building name, fact, and image
            youAre.setVisibility(View.VISIBLE);
            buildingName.setText(nearbyBuilding);
            fact.setText(buildingFact);

        } catch (JSONException j) {
            System.out.println("JSON EXCEPTION: " + j);
        }
    }


    /*
    * onConnectionFailed(ConnectionResult result)
    *
    * Handles error when connection to GoogleApiClient fails
    */
    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.i("MainActivity", "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
    }


    /*
    * onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
    *
    * Handles response to permissions request
    */
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


    /*
    * onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
    *
    * Handles log message when connection suspended
    */
    @Override
    public void onConnectionSuspended(int cause) {
        Log.i("MainActivity", "Connection suspended");
    }

}















