package com.course_work.margo.gps_tracker.location;

import java.util.ArrayList;
import java.util.List;

public class TrackList {
    private static List<Track> trackList = new ArrayList<>();

    public static int size() {
        return trackList.size();
    }

    public static List<Track> getTrackList() {
        return trackList;
    }

    public static Track getTrack(int index) {
        return trackList.get(index);
    }

    public static void addTrack(Track track) {
        trackList.add(track);
    }
}
