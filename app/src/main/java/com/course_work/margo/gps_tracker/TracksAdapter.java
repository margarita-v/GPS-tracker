package com.course_work.margo.gps_tracker;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.course_work.margo.gps_tracker.models.Track;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;

import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

class TracksAdapter extends ArrayAdapter<String> {

    private Context context;
    private List<String> tracks;

    //region Using a database helper
    private DatabaseHelper databaseHelper = null;
    private Dao<Track, Integer> trackDao;

    private DatabaseHelper getHelper() {
        if (databaseHelper == null) {
            databaseHelper = OpenHelperManager.getHelper(context, DatabaseHelper.class);
        }
        return databaseHelper;
    }
    //endregion

    TracksAdapter(Context context, List<String> objects) {
        super(context, -1, objects);
        this.context = context;
        this.tracks = objects;
        try {
            trackDao = getHelper().getTrackDao();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static class ViewHolder {
        TextView tvName;
        ImageButton imgBtnMap;
        ImageButton imgBtnDelete;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        final String trackName = getItem(position);
        ViewHolder viewHolder;

        if (convertView == null) {
            viewHolder = new ViewHolder();
            LayoutInflater layoutInflater = LayoutInflater.from(context);
            convertView = layoutInflater.inflate(R.layout.list_group, parent, false);
            viewHolder.tvName = (TextView) convertView.findViewById(R.id.trackName);
            viewHolder.imgBtnMap = (ImageButton) convertView.findViewById(R.id.imgBtnMap);
            viewHolder.imgBtnDelete = (ImageButton) convertView.findViewById(R.id.imgBtnDelete);
            convertView.setTag(viewHolder);
        }
        else
            viewHolder = (ViewHolder) convertView.getTag();
        viewHolder.tvName.setText(trackName);

        // View track on map
        viewHolder.imgBtnMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Track track = getHelper().getTrackByName(trackName);
                    // Checking if the current track is empty
                    if (track.getLocations().size() > 0) {
                        Intent intent = new Intent(context, MapsActivity.class);
                        intent.putExtra(context.getString(R.string.intent_track_name), trackName);
                        context.startActivity(intent);
                    }
                    else
                        MainActivity.createAlertDialog(context, R.string.alert_empty_track_title, R.string.alert_empty_track_message);

                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        });
        // Delete chosen track
        viewHolder.imgBtnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // if chosen route is tracking now
                if (Objects.equals(MainActivity.getTrackName(), trackName))
                    MainActivity.createAlertDialog(context, R.string.delete_error_title, R.string.delete_error_message);
                else {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle(R.string.delete_confirm_title).setMessage(R.string.delete_confirm_message);

                    builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            try {
                                tracks.remove(trackName);
                                getHelper().deleteTrackByName(trackName);

                                // Checking if the track list is empty
                                long countOfTracks = trackDao.countOf();
                                if (countOfTracks == 0) {
                                    Toast.makeText(context, R.string.alert_empty_list_title, Toast.LENGTH_SHORT).show();
                                    ((Activity) context).finish();
                                } else
                                    notifyDataSetChanged();
                            } catch (SQLException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) { }
                    });
                    AlertDialog dialog = builder.create();
                    dialog.show();
                } // Dialog builder for delete track
            } // onClick
        });
        return convertView;
    }
}
