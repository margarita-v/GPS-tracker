package com.course_work.margo.gps_tracker;

import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.widget.Toast;

import com.course_work.margo.gps_tracker.interfaces.LocationSettingsCallback;
import com.course_work.margo.gps_tracker.interfaces.LocationSettingsSuccess;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;

public class LocationService extends Service implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener, LocationSettingsSuccess {

    private Location mLastLocation;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private LocationSettingsRequest mLocationSettingsRequest;
    private static LocationSettingsCallback locationSettingsCallback;

    private static final long UPDATE_INTERVAL = 2000;
    private static final long FASTEST_INTERVAL = UPDATE_INTERVAL / 2;

    private static final int    ACCURACY = 100;
    private static final double MIN_DIFFERENCE = 0.0001;
    private static final double MAX_DIFFERENCE = 0.01;
    private static final double DIFFERENCE_FOR_ONE_DIMENSION = 2*MIN_DIFFERENCE;

    public static void setLocationSettingsCallback(LocationSettingsCallback callback) {
        locationSettingsCallback = callback;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        buildGoogleApiClient();
        createLocationRequest();
        buildLocationSettingsRequest();
        MainActivity.setLocationSettingsSuccess(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mGoogleApiClient.connect();
        return Service.START_NOT_STICKY;
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    protected void buildLocationSettingsRequest() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        mLocationSettingsRequest = builder.build();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
        mGoogleApiClient.disconnect();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        // Callback to activity; before start receive location updates, we should check location settings
        locationSettingsCallback.onCheckLocationSettings(mGoogleApiClient, mLocationSettingsRequest);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    // Callback from activity: location settings are accepted
    @Override
    public void onAcceptLocationSettings() {
        if (ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
    }

    @Override
    public void onLocationChanged(Location location) {
        boolean check = checkDifference(location.getLatitude(), location.getLongitude());
        if (location.getAccuracy() < ACCURACY && check) {//checkDifference(location.getLatitude(), location.getLongitude())) {
            mLastLocation = location;
            Intent intent = new Intent(getString(R.string.intent_broadcast));
            intent.putExtra(getString(R.string.intent_location_changed), location);
            sendBroadcast(intent);
        }
        Toast.makeText(this,
                Boolean.toString(check) + "\n" +
                Float.toString(location.getAccuracy()) + "\n" +
                Double.toString(location.getLatitude()) + "\n" +
                Double.toString(location.getLongitude()), Toast.LENGTH_SHORT).show();
    }

    private boolean checkDifference(double latitude, double longitude) {
        if (mLastLocation == null)
            return true;
        double latDif = Math.abs(mLastLocation.getLatitude() - latitude);
        double longDif = Math.abs(mLastLocation.getLongitude() - longitude);
        return (latDif >= MIN_DIFFERENCE && longDif >= MIN_DIFFERENCE ||
                latDif >= DIFFERENCE_FOR_ONE_DIMENSION || longDif >= DIFFERENCE_FOR_ONE_DIMENSION) &&
                latDif <= MAX_DIFFERENCE && longDif <= MAX_DIFFERENCE;
    }
}
