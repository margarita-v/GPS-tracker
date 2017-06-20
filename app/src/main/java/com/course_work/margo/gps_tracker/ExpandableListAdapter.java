package com.course_work.margo.gps_tracker;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.course_work.margo.gps_tracker.models.Track;
import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

class ExpandableListAdapter extends BaseExpandableListAdapter {

    private Context context;
    private List<String> trackHeaders;
    private HashMap<String, List<String>> trackItems;

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

    ExpandableListAdapter(Context context,
                          List<String> headers,
                          HashMap<String, List<String>> items) {
        this.context = context;
        this.trackHeaders = headers;
        this.trackItems = items;
        try {
            trackDao = getHelper().getTrackDao();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getGroupCount() {
        return this.trackHeaders.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return this.trackItems.get(this.trackHeaders.get(groupPosition))
                .size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return this.trackHeaders.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return this.trackItems.get(this.trackHeaders.get(groupPosition))
                .get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(final int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        final String headerTitle = (String) getGroup(groupPosition);
        if (convertView == null) {
            LayoutInflater inflater= (LayoutInflater) this.context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.list_group, null);
        }

        TextView lblListHeader = (TextView) convertView.findViewById(R.id.trackName);
        lblListHeader.setText(headerTitle);

        ImageButton imgBtnMap = (ImageButton) convertView.findViewById(R.id.imgBtnMap);
        ImageButton imgBtnDelete = (ImageButton) convertView.findViewById(R.id.imgBtnDelete);

        // View track on map
        imgBtnMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Track track = getHelper().getTrackByName(headerTitle);
                    // Checking if the current track is empty
                    if (track.getLocations().size() > 0) {
                        Intent intent = new Intent(context, MapsActivity.class);
                        intent.putExtra(context.getString(R.string.intent_track_name), headerTitle);
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
        imgBtnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle(R.string.delete_confirm_title).setMessage(R.string.delete_confirm_message);

                builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        if (!Objects.equals(MainActivity.getLocationName(), headerTitle)) {
                            try {
                                trackHeaders.remove(headerTitle);
                                getHelper().deleteTrackByName(headerTitle);

                                // Checking if the track list is empty
                                long countOfTracks = trackDao.countOf();
                                if (countOfTracks == 0) {
                                    Toast.makeText(context, R.string.alert_empty_list_title, Toast.LENGTH_SHORT).show();
                                    ((Activity)context).finish();
                                } else
                                    notifyDataSetChanged();
                            } catch (SQLException e) {
                                e.printStackTrace();
                            }
                        }
                        else
                            MainActivity.createAlertDialog(context, R.string.delete_error_title, R.string.delete_error_message);
                    }
                });
                builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) { }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });

        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        final String childText = (String) getChild(groupPosition, childPosition);
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) this.context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.list_item, null);
        }
        TextView txtListChild = (TextView) convertView.findViewById(R.id.trackItem);
        txtListChild.setText(childText);
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}
