package com.course_work.margo.gps_tracker;

import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;

import com.course_work.margo.gps_tracker.location.Track;
import com.course_work.margo.gps_tracker.location.TrackList;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.List;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private Track currentTrack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        Intent intent = getIntent();
        int trackNumber = intent.getIntExtra("trackNumber", TrackList.size());
        currentTrack = TrackList.getTrack(trackNumber);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
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
                .addAll(items);
        mMap.addPolyline(trackOptions);

        // add marker in each point pf track
        for (LatLng latLng: items) {
            mMap.addMarker(new MarkerOptions().position(latLng));
        }

        // move camera to beginning of the track
        mMap.moveCamera(CameraUpdateFactory.newLatLng(items.get(0)));
        mMap.animateCamera(CameraUpdateFactory.zoomTo(14));
    }
}
