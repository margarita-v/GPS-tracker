package com.course_work.margo.gps_tracker;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.course_work.margo.gps_tracker.models.Track;
import com.course_work.margo.gps_tracker.models.TrackItem;
import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;

import java.sql.SQLException;

class DatabaseHelper extends OrmLiteSqliteOpenHelper{
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
            Log.e(DatabaseHelper.class.getName(), "Unable to create databases", e);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase database, ConnectionSource connectionSource, int oldVersion, int newVersion) {
        try {
            TableUtils.dropTable(connectionSource, Track.class, true);
            TableUtils.dropTable(connectionSource, TrackItem.class, true);
            onCreate(database, connectionSource);
        } catch (SQLException e) {
            Log.e(DatabaseHelper.class.getName(), "Unable to upgrade database from version " + oldVersion + " to new "
                    + newVersion, e);
        }
    }

    public Dao<Track, Integer> getTeacherDao() throws SQLException {
        if (trackDao == null) {
            trackDao = getDao(Track.class);
        }
        return trackDao;
    }

    public Dao<TrackItem, Integer> getStudentDao() throws SQLException {
        if (locationDao == null) {
            locationDao = getDao(TrackItem.class);
        }
        return locationDao;
    }

    @Override
    public void close() {
        trackDao = null;
        locationDao = null;
        super.close();
    }
}
