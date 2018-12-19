package com.omriHadad.ParkSafely.Activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.omriHadad.ParkSafely.R;

public class SettingsActivity extends AppCompatActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
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
