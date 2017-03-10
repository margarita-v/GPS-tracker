package com.course_work.margo.gps_tracker;

import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ExpandableListView;

import com.course_work.margo.gps_tracker.location.Track;
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

        //prepareListData();

        parentItems = new ArrayList<>();
        childItems = new HashMap<>();

        /*Intent intent = getIntent();
        String trackName = intent.getStringExtra("trackName");
        ArrayList<Location> trackItems = intent.getParcelableArrayListExtra("trackItems");
        parentItems.add(trackName);
        int size = parentItems.size() - 1;
        List<String> items = new ArrayList<>();
        for (Location location: trackItems) {
            String item = "Latitude: " + location.getLatitude() +
                            ", Longitude: " + location.getLongitude();
            items.add(item);
        }
        childItems.put(parentItems.get(size), items);*/

        int i = parentItems.size();
        for (Track track: TrackList.getTrackList()) {
            parentItems.add(track.getName());
            childItems.put(parentItems.get(i), track.getItemsToString());
        }

        adapter = new ExpandableListAdapter(this, parentItems, childItems);
        elvTracks.setAdapter(adapter);
    }

    private void prepareListData() {
        parentItems = new ArrayList<>();
        childItems = new HashMap<>();

        parentItems.add("Top 250");
        parentItems.add("Now Showing");
        parentItems.add("Coming Soon..");

        // Adding child data
        List<String> top250 = new ArrayList<>();
        top250.add("The Shawshank Redemption");
        top250.add("The Godfather");
        top250.add("The Godfather: Part II");
        top250.add("Pulp Fiction");
        top250.add("The Good, the Bad and the Ugly");
        top250.add("The Dark Knight");
        top250.add("12 Angry Men");

        List<String> nowShowing = new ArrayList<>();
        nowShowing.add("The Conjuring");
        nowShowing.add("Despicable Me 2");
        nowShowing.add("Turbo");
        nowShowing.add("Grown Ups 2");
        nowShowing.add("Red 2");
        nowShowing.add("The Wolverine");

        List<String> comingSoon = new ArrayList<>();
        comingSoon.add("2 Guns");
        comingSoon.add("The Smurfs 2");
        comingSoon.add("The Spectacular Now");
        comingSoon.add("The Canyons");
        comingSoon.add("Europa Report");

        childItems.put(parentItems.get(0), top250);
        childItems.put(parentItems.get(1), nowShowing);
        childItems.put(parentItems.get(2), comingSoon);
    }
}
