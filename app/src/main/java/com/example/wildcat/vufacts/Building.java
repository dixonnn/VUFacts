package com.example.wildcat.vufacts;

/**
 * Created by wildcat on 3/21/2017.
 */

public class Building {

    // Building name, latitude, and longitude
    public String name;
    public double lat;
    public double lon;

    // Class constructor
    public Building (String name, double lat, double lon) {

        this.name = name;
        this.lat = lat;
        this.lon = lon;

    }

    // Get methods
    public String getName() {
        return name;
    }

    public double getLat() {
        return lat;
    }

    public double getLong() {
        return lon;
    }
}
