package com.course_work.margo.gps_tracker.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.course_work.margo.gps_tracker.models.Track;
import com.course_work.margo.gps_tracker.models.TrackItem;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.QueryBuilder;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends OrmLiteSqliteOpenHelper{
    private static final String DATABASE_NAME = "tracks.db";
    private static final int DATABASE_VERSION = 1;

    private Dao<Track, Integer> trackDao = null;
    private Dao<TrackItem, Integer> locationDao = null;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database, ConnectionSource connectionSource) {
        try {
            TableUtils.createTable(connectionSource, Track.class);
            TableUtils.createTable(connectionSource, TrackItem.class);
        } catch (SQLException e) {
            Log.e(DatabaseHelper.class.getName(), "Unable to create tables", e);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, ConnectionSource connectionSource, int oldVersion, int newVersion) {
        try {
            TableUtils.dropTable(connectionSource, Track.class, true);
            TableUtils.dropTable(connectionSource, TrackItem.class, true);
            onCreate(database, connectionSource);
        } catch (SQLException e) {
            Log.e(DatabaseHelper.class.getName(), "Unable to upgrade database from version " + oldVersion +
                    " to new " + newVersion, e);
        }
    }

    @Override
    public void close() {
        trackDao = null;
        locationDao = null;
        super.close();
    }

    public Dao<Track, Integer> getTrackDao() throws SQLException {
        if (trackDao == null) {
            trackDao = getDao(Track.class);
        }
        return trackDao;
    }

    public Dao<TrackItem, Integer> getLocationDao() throws SQLException {
        if (locationDao == null) {
            locationDao = getDao(TrackItem.class);
        }
        return locationDao;
    }

    public Track getTrackByName(String name) throws SQLException {
        QueryBuilder<Track, Integer> queryBuilder = getTrackDao().queryBuilder();
        queryBuilder.where().eq(Track.FIELD_NAME_NAME, name);
        return getTrackDao().queryForFirst(queryBuilder.prepare());
    }

    public void deleteTrackByName(String name) throws SQLException {
        Track track = getTrackByName(name);
        // Delete all locations for this track
        getLocationDao().delete(track.getLocations());
        getTrackDao().delete(track);
    }

    public void deleteFirstLocations(String trackName) throws SQLException {
        int i = 0;
        Track track = getTrackByName(trackName);
        List<TrackItem> deletedLocations = new ArrayList<>();
        for (TrackItem location: track.getLocations()) {
            deletedLocations.add(location);
            if (++i == Track.COUNT_OF_DELETED_LOCATIONS) break;
        }
        getLocationDao().delete(deletedLocations);
    }
}
