package com.course_work.margo.gps_tracker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
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
import java.util.Objects;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private Track currentTrack;
    private LatLng lastPoint;

    private LocationReceiver locationReceiver;
    private IntentFilter intentFilter;

    private boolean isTrackingRoute;
    private static final int LINE_WIDTH = 5;
    private static final int ZOOM_LEVEL = 17;

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

        locationReceiver = new LocationReceiver();
        intentFilter = new IntentFilter(getString(R.string.intent_broadcast));
    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent intent = getIntent();
        String trackName = intent.getStringExtra(getString(R.string.intent_track_name));
        try {
            currentTrack = getHelper().getTrackByName(trackName);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        setTitle(trackName);
        // Receive location updates from location service
        isTrackingRoute = Objects.equals(MainActivity.getLocationName(), trackName);
        if (isTrackingRoute)
            registerReceiver(locationReceiver, intentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isTrackingRoute)
            unregisterReceiver(locationReceiver);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // get track items as LatLng points
        List<LatLng> items = currentTrack.getLocationsAsLatLng();

        // draw line between track's points
        PolylineOptions trackOptions = new PolylineOptions()
                .addAll(items)
                .width(LINE_WIDTH)
                .geodesic(true);
        mMap.addPolyline(trackOptions);

        // add marker in each point pf track
        for (LatLng latLng: items) {
            mMap.addMarker(new MarkerOptions().position(latLng));
        }
        lastPoint = items.get(items.size() - 1);

        // move camera to beginning of the track
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(items.get(0), ZOOM_LEVEL));
    }

    // Receiver for results of LocationService
    private class LocationReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Location location = intent.getParcelableExtra(getString(R.string.intent_location_changed));
            LatLng newPoint = new LatLng(location.getLatitude(), location.getLongitude());
            PolylineOptions trackOptions = new PolylineOptions()
                    .add(lastPoint, newPoint)
                    .width(LINE_WIDTH)
                    .geodesic(true);
            mMap.addPolyline(trackOptions);
            mMap.addMarker(new MarkerOptions().position(newPoint));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(newPoint, ZOOM_LEVEL));
            lastPoint = newPoint;
        }
    }
}
