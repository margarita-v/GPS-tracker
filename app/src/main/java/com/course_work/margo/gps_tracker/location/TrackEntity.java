package com.course_work.margo.gps_tracker.location;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.List;

public class TrackEntity {
    // name of the track
    private String name;
    // location items of the track
    private List<Location> items;

    public TrackEntity(String name) {
        this.name = name;
        this.items = new ArrayList<>();
    }

    // get all location items in string format
    public List<String> getItemsToString() {
        List<String> result = new ArrayList<>();
        for (Location location: items) {
            String item = "Latitude: " + location.getLatitude() +
                            ", Longitude: " + location.getLongitude();
            result.add(item);
        }
        return result;
    }

    // get all location items as LatLng points
    public List<LatLng> getItemsAsLatLng() {
        List<LatLng> result = new ArrayList<>();
        for (Location location: items) {
            result.add(new LatLng(location.getLatitude(), location.getLongitude()));
        }
        return result;
    }

    // return name of the track
    public String getName() {
        return name;
    }

    public int size() {
        return items.size();
    }

    public void addLocation(Location location) {
        items.add(location);
    }
}
