package com.course_work.margo.gps_tracker;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ExpandableListView;

import com.course_work.margo.gps_tracker.location.TrackEntity;
import com.course_work.margo.gps_tracker.location.TrackList;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class TracksActivity extends AppCompatActivity {

    List<String> parentItems;
    HashMap<String, List<String>> childItems;
    ExpandableListView elvTracks;
    ExpandableListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tracks);

        elvTracks = (ExpandableListView) findViewById(R.id.elvTracks);

        parentItems = new ArrayList<>();
        childItems = new HashMap<>();

        int i = 0;
        for (TrackEntity trackEntity : TrackList.getTrackList()) {
            parentItems.add(trackEntity.getName());
            childItems.put(parentItems.get(i), trackEntity.getItemsToString());
            i++;
        }

        adapter = new ExpandableListAdapter(this, parentItems, childItems);
        elvTracks.setAdapter(adapter);
    }
}
