package com.course_work.margo.gps_tracker;

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

import com.course_work.margo.gps_tracker.location.TrackList;

import java.util.HashMap;
import java.util.List;

class ExpandableListAdapter extends BaseExpandableListAdapter {

    private Context context;
    private List<String> trackHeaders;
    private HashMap<String, List<String>> trackItems;
    private ImageButton imgBtnMap, imgBtnDelete;

    ExpandableListAdapter(Context context,
                          List<String> headers,
                          HashMap<String, List<String>> items) {
        this.context = context;
        this.trackHeaders = headers;
        this.trackItems = items;
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

        imgBtnMap = (ImageButton) convertView.findViewById(R.id.imgBtnMap);
        imgBtnDelete = (ImageButton) convertView.findViewById(R.id.imgBtnDelete);

        // View track on map
        imgBtnMap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TrackList.getTrack(groupPosition).size() > 0) {
                    Intent intent = new Intent(context, MapsActivity.class);
                    intent.putExtra("trackNumber", groupPosition);
                    context.startActivity(intent);
                }
                else
                    Toast.makeText(context, "This track is empty", Toast.LENGTH_SHORT).show();
            }
        });
        // Delete chosen track
        imgBtnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle(R.string.delete_confirm_title)
                        .setMessage(R.string.delete_confirm_message);

                builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User clicked OK button
                        trackHeaders.remove(headerTitle);
                        TrackList.removeTrack(groupPosition);
                        if (TrackList.size() == 0) {
                            Toast.makeText(context, "Track list is empty", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(context, MainActivity.class);
                            context.startActivity(intent);
                        }
                        else
                            notifyDataSetChanged();
                    }
                });

                builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                    }
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
