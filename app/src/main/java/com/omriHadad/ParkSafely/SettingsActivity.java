package com.omriHadad.ParkSafely;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;

public class SettingsActivity extends AppCompatActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Toolbar toolbar = findViewById(R.id.tool_bar);
        //setSupportActionBar(toolbar);
        //getSupportActionBar().setTitle("Settings");
    }

    public void editApButtonOnClick(View v)
    {
        startActivity(new Intent(SettingsActivity.this, EditAccessPointActivity.class));
    }
    public void calibrationOnClick(View v)
    {
        startActivity(new Intent(SettingsActivity.this, CalibrationActivity.class));
    }
}
