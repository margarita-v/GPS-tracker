package com.course_work.margo.gps_tracker.interfaces;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationSettingsRequest;

public interface LocationSettingsCallback {
    void onCheckLocationSettings(GoogleApiClient googleApiClient,
                                 LocationSettingsRequest locationSettingsRequest);
}
