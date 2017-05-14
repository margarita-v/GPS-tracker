package com.course_work.margo.gps_tracker.location;

import java.util.ArrayList;
import java.util.List;

// this class presents all tracks in application
public class TrackList {
    private static List<Track> trackList;

    //region Flags
    // flag which be equal True if tracking was paused
    private static boolean isTrackingPaused;

    public static boolean isTrackingPaused() {
        return isTrackingPaused;
    }

    public static void setIsTrackingPaused(boolean isTrackingPaused) {
        TrackList.isTrackingPaused = isTrackingPaused;
    }

    // flag which be equal True if tracking was stopped
    private static boolean isTrackingStopped;

    public static boolean isTrackingStopped() {
        return isTrackingStopped;
    }

    public static void setIsTrackingStopped(boolean isTrackingStopped) {
        TrackList.isTrackingStopped = isTrackingStopped;
    }
    //endregion

    public static void init() {
        trackList = new ArrayList<>();
        isTrackingPaused = false;
        isTrackingStopped = true;
    }

    // return a count of track's items
    public static int size() {
        return trackList.size();
    }

    // get track list
    public static List<Track> getTrackList() {
        return trackList;
    }

    // get track by index
    public static Track getTrack(int index) {
        return trackList.get(index);
    }

    // add track to list
    public static void addTrack(Track track) {
        trackList.add(track);
    }

    // remove track by index
    public static void removeTrack(int index) {
        trackList.remove(index);
    }

    // check does track list contain track
    public static boolean contains(Track track) {
        return trackList.contains(track);
    }
}
