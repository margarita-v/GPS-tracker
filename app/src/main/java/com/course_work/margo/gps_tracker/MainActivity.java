package com.course_work.margo.gps_tracker;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    Button btnStart, btnPause, btnStop, btnViewTracks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnStart = (Button) findViewById(R.id.btnStart);
        btnStart.setOnClickListener(this);

        btnPause = (Button) findViewById(R.id.btnPause);
        btnPause.setOnClickListener(this);

        btnStop = (Button) findViewById(R.id.btnStop);
        btnStop.setOnClickListener(this);

        btnViewTracks = (Button) findViewById(R.id.btnViewTracks);
        btnViewTracks.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnViewTracks:
                Intent intent = new Intent(this, TracksActivity.class);
                startActivity(intent);
                break;
            default:
                break;
        }
    }
}
