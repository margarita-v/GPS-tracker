package com.course_work.margo.gps_tracker;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ListView;

import com.course_work.margo.gps_tracker.models.Track;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class TracksActivity extends AppCompatActivity {

    List<String> parentItems;
    ListView lvTracks;
    TracksAdapter adapter;

    //region Using a database helper
    private DatabaseHelper databaseHelper = null;

    private DatabaseHelper getHelper() {
        if (databaseHelper == null) {
            databaseHelper = OpenHelperManager.getHelper(this, DatabaseHelper.class);
        }
        return databaseHelper;
    }
    //endregion

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracks);
        lvTracks = (ListView) findViewById(R.id.lvTracks);
        parentItems = new ArrayList<>();
        try {
            Dao<Track, Integer> trackDao = getHelper().getTrackDao();
            final List<Track> trackList = trackDao.queryForAll();
            for (Track track : trackList) {
                String trackName = track.getName();
                parentItems.add(trackName);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        adapter = new TracksAdapter(this, parentItems);
        lvTracks.setAdapter(adapter);
    }
}
