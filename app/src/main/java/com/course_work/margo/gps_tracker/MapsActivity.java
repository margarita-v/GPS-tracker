package com.course_work.margo.gps_tracker;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.course_work.margo.gps_tracker.location.TrackEntity;
import com.course_work.margo.gps_tracker.location.TrackList;
import com.course_work.margo.gps_tracker.models.Track;
import com.course_work.margo.gps_tracker.models.TrackItem;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;

import java.util.List;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private TrackEntity currentTrack;

    //region Using a database helper
    private DatabaseHelper databaseHelper = null;
    private Dao<Track, Integer> trackDao;
    private Dao<TrackItem, Integer> locationDao;

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
        int trackNumber = intent.getIntExtra("trackNumber", TrackList.size());
        currentTrack = TrackList.getTrack(trackNumber);
        setTitle(currentTrack.getName());
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // get track items as LatLng points
        List<LatLng> items = currentTrack.getItemsAsLatLng();

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
