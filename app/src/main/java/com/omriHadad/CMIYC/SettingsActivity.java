package com.omriHadad.CMIYC;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

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
        startActivity(new Intent(SettingsActivity.this, EditApActivity.class));
    }
}
