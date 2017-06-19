package com.course_work.margo.gps_tracker.models;

import android.location.Location;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = TrackItem.TABLE_NAME_LOCATIONS)
public class TrackItem {
    static final String TABLE_NAME_LOCATIONS = "locations";

    private static final String FIELD_NAME_ID        = "id";
    public  static final String FIELD_NAME_LATITUDE  = "latitude";
    public  static final String FIELD_NAME_LONGITUDE = "longitude";
    public  static final String FIELD_NAME_TIME      = "time";
    public  static final String FIELD_NAME_SPEED     = "speed";
    private static final String FIELD_NAME_TRACK     = "track";

    @DatabaseField(columnName = FIELD_NAME_ID, generatedId = true)
    private int id;

    @DatabaseField(columnName = FIELD_NAME_LATITUDE, canBeNull = false)
    private double latitude;

    @DatabaseField(columnName = FIELD_NAME_LONGITUDE, canBeNull = false)
    private double longitude;

    @DatabaseField(columnName = FIELD_NAME_TIME, canBeNull = false)
    private long time;

    @DatabaseField(columnName = FIELD_NAME_SPEED)
    private float speed;

    @DatabaseField(columnName = FIELD_NAME_TRACK, foreign = true, foreignAutoRefresh = true,
            canBeNull = false, index = true,
            columnDefinition = "INTEGER CONSTRAINT FK_TRACK REFERENCES tracks(id) ON DELETE CASCADE")
    private Track track;

    public TrackItem() { }

    public TrackItem(Location location, Track track) {
        this.latitude   = location.getLatitude();
        this.longitude  = location.getLongitude();
        this.speed      = location.getSpeed();
        this.time       = location.getTime();
        this.track      = track;
    }

    @Override
    public String toString() {
        return "Latitude: " + this.latitude + ", longitude: " + this.longitude;
    }

    double getLatitude() {
        return this.latitude;
    }

    double getLongitude() {
        return this.longitude;
    }
}
