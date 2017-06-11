package com.course_work.margo.gps_tracker;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.course_work.margo.gps_tracker.models.Track;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.j256.ormlite.android.apptools.OpenHelperManager;

import java.sql.SQLException;
import java.util.List;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private Track currentTrack;

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
        setContentView(R.layout.activity_maps);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent intent = getIntent();
        String trackName = intent.getStringExtra("trackName");
        try {
            currentTrack = getHelper().getTrackByName(trackName);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        setTitle(trackName);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // get track items as LatLng points
        List<LatLng> items = currentTrack.getLocationsAsLatLng();

        // draw line between track's points
        PolylineOptions trackOptions = new PolylineOptions()
                .addAll(items)
                .width(5)
                .geodesic(true);
        mMap.addPolyline(trackOptions);

        // add marker in each point pf track
        for (LatLng latLng: items) {
            mMap.addMarker(new MarkerOptions().position(latLng));
        }

        // move camera to beginning of the track
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(items.get(0), 17));
    }
}
