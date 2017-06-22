package com.course_work.margo.gps_tracker.models;

import com.google.android.gms.maps.model.LatLng;
import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.ArrayList;
import java.util.List;

@DatabaseTable(tableName = Track.TABLE_NAME_TRACKS)
public class Track {
    static final String TABLE_NAME_TRACKS = "tracks";

    private static final String FIELD_NAME_ID        = "id";
    public  static final String FIELD_NAME_NAME      = "name";
    private static final String FIELD_NAME_LOCATIONS = "locations";

    public static final int MAX_COUNT_OF_LOCATIONS     = 5000;
    public static final int COUNT_OF_DELETED_LOCATIONS = 200;

    @DatabaseField(columnName = FIELD_NAME_ID, generatedId = true)
    private int id;

    @DatabaseField(columnName = FIELD_NAME_NAME, unique = true)
    private String name;

    @ForeignCollectionField(columnName = FIELD_NAME_LOCATIONS, eager = true)
    private ForeignCollection<TrackItem> locations;

    public ForeignCollection<TrackItem> getLocations() {
        return locations;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getLocationsToString() {
        List<String> result = new ArrayList<>();
        for (TrackItem location: locations) {
            result.add(location.toString());
        }
        return result;
    }

    public List<LatLng> getLocationsAsLatLng() {
        List<LatLng> result = new ArrayList<>();
        for (TrackItem location: locations) {
            result.add(new LatLng(location.getLatitude(), location.getLongitude()));
        }
        return result;
    }
}
