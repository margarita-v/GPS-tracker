package com.course_work.margo.gps_tracker;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ExpandableListView;

import com.course_work.margo.gps_tracker.models.Track;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TracksActivity extends AppCompatActivity {

    List<String> parentItems;
    HashMap<String, List<String>> childItems;
    ExpandableListView elvTracks;
    ExpandableListAdapter adapter;

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

        elvTracks = (ExpandableListView) findViewById(R.id.elvTracks);

        parentItems = new ArrayList<>();
        childItems = new HashMap<>();

        try {
            Dao<Track, Integer> trackDao = getHelper().getTrackDao();
            final List<Track> trackList = trackDao.queryForAll();
            for (Track track: trackList) {
                String trackName = track.getName();
                parentItems.add(trackName);
                childItems.put(trackName, track.getLocationsToString());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        adapter = new ExpandableListAdapter(this, parentItems, childItems);
        elvTracks.setAdapter(adapter);
    }
}
