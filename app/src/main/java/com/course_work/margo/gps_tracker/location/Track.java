package com.course_work.margo.gps_tracker.location;

import android.location.Location;

import java.util.ArrayList;
import java.util.List;

public class Track {
    private String name;
    private List<Location> items;

    public Track(String name) {
        this.name = name;
        this.items = new ArrayList<>();
    }

    public List<Location> getItems() {
        return items;
    }

    public List<String> getItemsToString() {
        List<String> result = new ArrayList<>();
        for (Location location: items) {
            String item = "Latitude: " + location.getLatitude() +
                            ", Longitude: " + location.getLongitude();
            result.add(item);
        }
        return result;
    }

    public String getName() {
        return name;
    }

    public void addLocation(Location location) {
        items.add(location);
    }
}
