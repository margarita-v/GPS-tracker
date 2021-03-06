package com.course_work.margo.gps_tracker.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.Manifest;

import com.course_work.margo.gps_tracker.database.DatabaseHelper;
import com.course_work.margo.gps_tracker.service.LocationService;
import com.course_work.margo.gps_tracker.R;
import com.course_work.margo.gps_tracker.interfaces.LocationSettingsCallback;
import com.course_work.margo.gps_tracker.interfaces.LocationSettingsSuccess;
import com.course_work.margo.gps_tracker.models.Track;
import com.course_work.margo.gps_tracker.models.TrackItem;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;

import java.sql.SQLException;
import java.text.DateFormat;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity implements ResultCallback<LocationSettingsResult>,
        ActivityCompat.OnRequestPermissionsResultCallback, LocationSettingsCallback {

    private Button btnStart, btnPause, btnStop;
    private TextView tvLocation;

    private static LocationSettingsSuccess locationSettingsSuccess;
    private static String trackName = "";

    private Location mCurrentLocation;
    private Track currentTrack;
    private Intent serviceIntent;
    private int locationsCount;

    private static final int MAX = 30;
    private static final int REQUEST_CHECK_SETTINGS = 0x1;
    private static final String TAG = "myLog";

    private boolean isPaused  = false;
    private boolean isStopped = true;

    public static String getTrackName() {
        return trackName;
    }

    public static void setLocationSettingsSuccess(LocationSettingsSuccess callback) {
        locationSettingsSuccess = callback;
    }

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
        setContentView(R.layout.activity_main);

        // Start
        btnStart = (Button) findViewById(R.id.btnStart);
        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkLocationSettings();
            }
        });

        // Pause
        btnPause = (Button) findViewById(R.id.btnPause);
        btnPause.setEnabled(false);
        btnPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeState(true, false);
                tvLocation.setText(R.string.pause_tracking);
                stopService(serviceIntent);
            }
        });

        // Stop
        btnStop = (Button) findViewById(R.id.btnStop);
        btnStop.setEnabled(false);
        btnStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopLocationUpdates();
            }
        });

        // View tracks
        Button btnViewTracks = (Button) findViewById(R.id.btnViewTracks);
        btnViewTracks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                long countOfTracks = 0;
                try {
                    countOfTracks = trackDao.countOf();
                } catch (SQLException e) {
                    Log.d(TAG, "Track count exception");
                }
                if (currentTrack == null && countOfTracks == 0)
                    createAlertDialog(MainActivity.this, R.string.alert_empty_list_title, R.string.alert_empty_list_message);
                else {
                    Intent intent = new Intent(MainActivity.this, TracksActivity.class);
                    startActivity(intent);
                }
            }
        });

        tvLocation = (TextView) findViewById(R.id.tvLocation);
        currentTrack = null;
        LocationService.setLocationSettingsCallback(this);
        serviceIntent = new Intent(MainActivity.this, LocationService.class);

        // Receive location updates from location service
        LocationReceiver locationReceiver = new LocationReceiver();
        IntentFilter intentFilter = new IntentFilter(getString(R.string.intent_broadcast));
        registerReceiver(locationReceiver, intentFilter);

        try {
            trackDao = getHelper().getTrackDao();
            locationDao = getHelper().getLocationDao();
        } catch (SQLException e) {
            Log.d(TAG, "Get dao exception");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (databaseHelper != null) {
            OpenHelperManager.releaseHelper();
            databaseHelper = null;
        }
    }

    // Check permission for receive location updates and start service
    protected void checkLocationSettings() {
        // Check if permission is granted
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)

            ActivityCompat.requestPermissions(this,
                    new String[] { Manifest.permission.ACCESS_FINE_LOCATION },
                    REQUEST_CHECK_SETTINGS);
        else
            startService(serviceIntent);
    }

    // Result of checking permissions
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CHECK_SETTINGS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                    startService(serviceIntent);
            }
        }
    }

    // Callback from service; before start receive location updates, we should check location settings
    @Override
    public void onCheckLocationSettings(GoogleApiClient googleApiClient,
                                        LocationSettingsRequest locationSettingsRequest) {
        LocationServices.SettingsApi.checkLocationSettings(googleApiClient, locationSettingsRequest)
                .setResultCallback(this);
    }

    // Result of checking locations settings
    @Override
    public void onResult(@NonNull LocationSettingsResult locationSettingsResult) {
        final Status status = locationSettingsResult.getStatus();
        switch (status.getStatusCode()) {
            case LocationSettingsStatusCodes.SUCCESS:
                // Callback to service; location settings are enabled
                startTracking();
                break;
            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                try {
                    // Request location settings
                    status.startResolutionForResult(MainActivity.this, REQUEST_CHECK_SETTINGS);
                } catch (IntentSender.SendIntentException e) {
                    Log.d(TAG, "Check settings exception");
                }
                break;
            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                break;
        }
    }

    // Result for location settings request
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            // Check for the integer request code originally supplied to startResolutionForResult().
            case REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        // Callback to service; location settings are accepted
                        startTracking();
                        break;
                    case Activity.RESULT_CANCELED:
                        if (!isPaused)
                            stopLocationUpdates();
                        else
                            stopService(serviceIntent);
                        break;
                }
                break;
        }
    }

    private void startTracking() {
        // Use current date and time as a track's name
        DateFormat dateFormat = DateFormat.getDateTimeInstance();
        Calendar calendar = Calendar.getInstance();
        // if track wasn't paused, then we create new track, else track will be resumed
        if (!isPaused) {
            currentTrack = new Track();
            trackName = dateFormat.format(calendar.getTime());
            currentTrack.setName(trackName);
            locationsCount = 0;
            try {
                trackDao.create(currentTrack);
            } catch (SQLException e) {
                Log.d(TAG, "Can't create new track");
            }
        }
        else
            locationsCount = currentTrack.getLocations() != null ? currentTrack.getLocations().size() : 0;
        changeState(false, false);
        tvLocation.setText(R.string.alert_waiting_title);
        locationSettingsSuccess.onAcceptLocationSettings();
        // Print progress dialog while location hasn't received
        /*if (mCurrentLocation == null) {
            tvLocation.setText(message);
            WaitingProgressDialog dialogAsyncTask = new WaitingProgressDialog();
            dialogAsyncTask.execute();
        }
        else
            Toast.makeText(this, R.string.start_tracking_title, Toast.LENGTH_SHORT).show();*/
    }

    // Call when service for location updates is stopped
    private void stopLocationUpdates() {
        changeState(false, true);
        currentTrack = null;
        trackName = "";
        tvLocation.setText("");
        stopService(serviceIntent);
    }

    //region Functions for correct UI state
    private void changeState(boolean isPaused, boolean isStopped) {
        this.isPaused  = isPaused;
        this.isStopped = isStopped;
        btnStart.setEnabled(isPaused || isStopped);
        btnPause.setEnabled(!(btnStart.isEnabled()));
        btnStop.setEnabled(!isStopped);
    }

    private void printCurrentLocation() {
        if (mCurrentLocation != null) {
            String item = "Latitude: " + mCurrentLocation.getLatitude() +
                    ", Longitude: " + mCurrentLocation.getLongitude();
            tvLocation.setText(item);
        }
    }

    public static void createAlertDialog(Context context, int titleResource, int messageResource) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(titleResource)
                .setMessage(messageResource);
        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) { }
        });
        AlertDialog dialog = builder.create();
        dialog.setCancelable(false);
        dialog.show();
    }
    //endregion

    // Receiver for results of LocationService
    private class LocationReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            mCurrentLocation = intent.getParcelableExtra(getString(R.string.intent_location_changed));
            locationsCount++;
            try {
                if (locationsCount > Track.MAX_COUNT_OF_LOCATIONS) {
                    getHelper().deleteFirstLocations(trackName);
                    locationsCount = Track.MAX_COUNT_OF_LOCATIONS - Track.COUNT_OF_DELETED_LOCATIONS + 1;
                }
                locationDao.create(new TrackItem(mCurrentLocation, currentTrack));
            } catch (SQLException e) {
                Log.d(TAG, "Can't create locationDao");
            }
            printCurrentLocation();
        }
    }

    // Progress dialog for waiting for GPS signal
    private class WaitingProgressDialog extends AsyncTask<Void, Integer, Void> {
        private ProgressDialog dialog;
        private static final String message = "Waiting for a GPS signal...\n";

        @Override
        protected void onPreExecute() {
            dialog = new ProgressDialog(MainActivity.this);
            dialog.setTitle(R.string.start_tracking_title);
            dialog.setMessage(message);
            dialog.setIndeterminate(false);
            dialog.setCancelable(false);
            dialog.setProgress(0);
            dialog.setMax(MAX);
            dialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Close", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            dialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            int currentProgress = 1;
            while (currentProgress <= MAX && mCurrentLocation == null && !isCancelled()) {
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
            // Offer user to turn on the Internet
            createAlertDialog(MainActivity.this, R.string.alert_waiting_title, R.string.alert_waiting_message);
        }
    }
}