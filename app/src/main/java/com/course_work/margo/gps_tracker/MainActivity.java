package com.course_work.margo.gps_tracker;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.PowerManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.Manifest;

import com.course_work.margo.gps_tracker.location.Track;
import com.course_work.margo.gps_tracker.location.TrackList;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;

import java.text.DateFormat;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity implements
        View.OnClickListener, ConnectionCallbacks,
        OnConnectionFailedListener, LocationListener, ResultCallback<LocationSettingsResult>,
        ActivityCompat.OnRequestPermissionsResultCallback {

    Button btnStart, btnPause, btnStop, btnViewTracks;

    private Location mCurrentLocation;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    protected LocationSettingsRequest mLocationSettingsRequest;

    protected static final int REQUEST_CHECK_SETTINGS = 0x1;
    private static final long UPDATE_INTERVAL = 4000;
    private static final long FASTEST_INTERVAL = UPDATE_INTERVAL / 2;

    private static final String TAG = "myLog";
    private final String message = "Waiting for a GPS signal...\n";

    // blocking the transition to sleep
    private PowerManager.WakeLock wakeLock;

    WaitingProgressDialog dialogAsyncTask;
    int currentProgress;
    final int max = 30;
    TextView tvLocation;
    Track currentTrack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnStart = (Button) findViewById(R.id.btnStart);
        btnStart.setOnClickListener(this);

        btnPause = (Button) findViewById(R.id.btnPause);
        btnPause.setOnClickListener(this);
        btnPause.setEnabled(false);

        btnStop = (Button) findViewById(R.id.btnStop);
        btnStop.setOnClickListener(this);
        btnStop.setEnabled(false);

        btnViewTracks = (Button) findViewById(R.id.btnViewTracks);
        btnViewTracks.setOnClickListener(this);

        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "No sleep");
        wakeLock.acquire();

        tvLocation = (TextView) findViewById(R.id.tvLocation);
        TrackList.init();
        currentTrack = null;
        mCurrentLocation = null;
        Log.d(TAG, "Create");
        buildGoogleApiClient();
        createLocationRequest();
        buildLocationSettingsRequest();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnViewTracks:
                if (currentTrack == null && TrackList.size() == 0)
                    Toast.makeText(this, "Track list is empty", Toast.LENGTH_SHORT).show();
                else {
                    Intent intent = new Intent(this, TracksActivity.class);
                    startActivity(intent);
                }
                break;
            case R.id.btnStart:
                checkLocationSettings();
                break;
            case R.id.btnPause:
                btnStart.setEnabled(true);
                btnPause.setEnabled(false);
                btnStop.setEnabled(true);
                TrackList.setIsTrackingPaused(true);
                TrackList.setIsTrackingStopped(false);
                stopLocationUpdates();
                break;
            case R.id.btnStop:
                btnStart.setEnabled(true);
                btnPause.setEnabled(false);
                btnStop.setEnabled(false);
                TrackList.setIsTrackingPaused(false);
                TrackList.setIsTrackingStopped(true);
                currentTrack = null;
                if (dialogAsyncTask != null && !dialogAsyncTask.isCancelled()) {
                    dialogAsyncTask.cancel(true);
                    tvLocation.setText("");
                }
                stopLocationUpdates();
                break;
            default:
                break;
        }
    }

    //region Activity lifecycle methods
    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "Start");
        if (mGoogleApiClient != null)
            mGoogleApiClient.connect();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "Resume");
        if (mGoogleApiClient.isConnected() && !TrackList.isTrackingPaused() && !TrackList.isTrackingStopped())
            startLocationUpdates();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mGoogleApiClient.isConnected() && TrackList.isTrackingPaused())
            stopLocationUpdates();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected() && TrackList.isTrackingStopped())
            mGoogleApiClient.disconnect();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        wakeLock.release();
    }
    //endregion

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

    protected void checkLocationSettings() {
        // Check if permission is granted
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)

            ActivityCompat.requestPermissions(this,
                    new String[] { Manifest.permission.ACCESS_FINE_LOCATION },
                    REQUEST_CHECK_SETTINGS);
        else {
            // It will be always called for Android versions below 6.0
            PendingResult<LocationSettingsResult> result =
                    LocationServices.SettingsApi.checkLocationSettings(
                            mGoogleApiClient,
                            mLocationSettingsRequest
                    );
            // results provided through a PendingResult
            result.setResultCallback(this);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CHECK_SETTINGS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startTracking();
                }
            }
        }
    }

    @Override
    public void onResult(@NonNull LocationSettingsResult locationSettingsResult) {
        final Status status = locationSettingsResult.getStatus();
        switch (status.getStatusCode()) {
            case LocationSettingsStatusCodes.SUCCESS:
                startTracking();
                break;
            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                try {
                    // Show the dialog by calling startResolutionForResult(), and check the result
                    // in onActivityResult().
                    status.startResolutionForResult(MainActivity.this, REQUEST_CHECK_SETTINGS);
                } catch (IntentSender.SendIntentException e) {
                    e.printStackTrace();
                }
                break;
            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            // Check for the integer request code originally supplied to startResolutionForResult().
            case REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        startTracking();
                        break;
                    case Activity.RESULT_CANCELED:
                        break;
                }
                break;
        }
    }

    private void startTracking() {
        btnStart.setEnabled(false);
        btnPause.setEnabled(true);
        btnStop.setEnabled(true);
        // Use current date and time as a track's name
        DateFormat dateFormat = DateFormat.getDateTimeInstance();
        Calendar calendar = Calendar.getInstance();
        TrackList.setIsTrackingStopped(false);
        // if track wasn't paused, then we create new track, else track wil be resumed
        if (!TrackList.isTrackingPaused()) {
            // begin new track and save old track to list
            if (currentTrack != null && !TrackList.contains(currentTrack))
                TrackList.addTrack(currentTrack);
            currentTrack = new Track(dateFormat.format(calendar.getTime()));
            TrackList.addTrack(currentTrack);
        }
        else
            TrackList.setIsTrackingPaused(false);
        startLocationUpdates();
        if (mCurrentLocation == null) {
            tvLocation.setText(message);
            dialogAsyncTask = new WaitingProgressDialog();
            dialogAsyncTask.execute();
        }
        else
            Toast.makeText(this, "Start tracking", Toast.LENGTH_SHORT).show();
    }

    protected void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient,
                mLocationRequest,
                this
        ).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(@NonNull Status status) {
                Log.d(TAG, "Tracking goes");
            }
        });
    }

    protected void stopLocationUpdates() {
        // It is a good practice to remove location requests when the activity is in a paused or
        // stopped state. Doing so helps battery performance and is especially
        // recommended in applications that request frequent location updates.
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient,
                this
        ).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(@NonNull Status status) {

            }
        });
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (mCurrentLocation == null) {
            if (ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            Log.d(TAG, "Connected");
            //mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            //updateLocationUI();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }

    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
        currentTrack.addLocation(mCurrentLocation);
        updateLocationUI();
    }

    private void updateLocationUI() {
        if (mCurrentLocation != null) {
            String item = "Latitude: " + mCurrentLocation.getLatitude() +
                    " , Longitude: " + mCurrentLocation.getLongitude();
            tvLocation.setText(item);
        }
    }

    // Progress dialog for waiting for GPS signal
    private class WaitingProgressDialog extends AsyncTask<Void, Integer, Void> {
        ProgressDialog dialog;

        @Override
        protected void onPreExecute() {
            dialog = new ProgressDialog(MainActivity.this);
            dialog.setTitle("Start tracking");
            dialog.setMessage(message);
            dialog.setIndeterminate(false);
            dialog.setCancelable(false);
            dialog.setProgress(0);
            dialog.setMax(max);
            dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Close", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            dialog.show();
            super.onPreExecute();
        }

        @Override
        protected Void doInBackground(Void... params) {
            currentProgress = 1;
            while (currentProgress <= max && mCurrentLocation == null && !isCancelled()) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                publishProgress(currentProgress);
                currentProgress++;
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... params) {
            int progress = params[0];
            dialog.setProgress(progress);
            dialog.setMessage(message + Integer.toString(progress) + " seconds");
        }

        @Override
        protected void onPostExecute(Void result) {
            dialog.dismiss();
            super.onPostExecute(result);
        }
    }
}