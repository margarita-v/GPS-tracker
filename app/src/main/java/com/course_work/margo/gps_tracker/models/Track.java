package com.course_work.margo.gps_tracker.models;

import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = Track.TABLE_NAME_TRACKS)
public class Track {
    static final String TABLE_NAME_TRACKS = "tracks";

    private static final String FIELD_NAME_ID        = "id";
    private static final String FIELD_NAME_NAME      = "name";
    private static final String FIELD_NAME_LOCATIONS = "locations";

    @DatabaseField(columnName = FIELD_NAME_ID, generatedId = true)
    private int id;

    @DatabaseField(columnName = FIELD_NAME_NAME, canBeNull = false)
    private String name;

    @ForeignCollectionField(columnName = FIELD_NAME_LOCATIONS, eager = true)
    private ForeignCollection<TrackItem> locations;

    public ForeignCollection<TrackItem> getLocations() {
        return locations;
    }

    public String getName() {
        return name;
    }
}
