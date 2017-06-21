package com.course_work.margo.gps_tracker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
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
import java.util.Objects;

public class TracksActivity extends AppCompatActivity {

    List<String> parentItems;
    HashMap<String, List<String>> childItems;
    ExpandableListView elvTracks;
    ExpandableListAdapter adapter;

    private LocationReceiver locationReceiver;
    private IntentFilter intentFilter;

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

        locationReceiver = new LocationReceiver();
        intentFilter = new IntentFilter(getString(R.string.intent_broadcast));
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(locationReceiver, intentFilter);
        // If maps activity was closed, then the running track was changed and we should rewrite it
        String trackName = MainActivity.getLocationName();
        try {
            Track track = getHelper().getTrackByName(trackName);
            // If track is running now and locations were added, we should update list element
            if (!Objects.equals(trackName, "")
                    && childItems.get(trackName).size() != track.getLocations().size()) {
                childItems.put(trackName, track.getLocationsToString());
                adapter.notifyDataSetChanged();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(locationReceiver);
    }

    // Receiver for results of LocationService
    private class LocationReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Location location = intent.getParcelableExtra(getString(R.string.intent_location_changed));
            childItems.get(MainActivity.getLocationName()).add(locationToString(location));
            adapter.notifyDataSetChanged();
        }
    }

    private String locationToString(Location location) {
        return "Latitude: " + location.getLatitude() + ", longitude: " + location.getLongitude();
    }
}
