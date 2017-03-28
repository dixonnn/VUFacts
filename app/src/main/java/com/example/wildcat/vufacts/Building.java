package com.example.wildcat.vufacts;

import android.location.Location;

/**
 * Created by wildcat on 3/21/2017.
 */

public class Building {

    // Building name, latitude, and longitude
    private String name;
    private Location loc;

    // Class constructor
    public Building (String name, Location loc) {

        this.name = name;
        this.loc = loc;

    }

    // Get methods
    public String getName() {
        return name;
    }

    public Location getLocation() {return loc; }
}
