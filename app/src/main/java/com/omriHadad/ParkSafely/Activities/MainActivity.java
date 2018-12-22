package com.omriHadad.ParkSafely.Activities;

import android.Manifest;
import android.content.*;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.*;
import android.widget.Toast;
import com.omriHadad.ParkSafely.Utilities.ClonePhotosLogic;
import com.omriHadad.ParkSafely.Utilities.DetectionLogic;
import com.omriHadad.ParkSafely.R;
import com.omriHadad.ParkSafely.Utilities.ConnectivityLogic;

public class MainActivity extends AppCompatActivity
{
    final static private String TAG = "parkSafelyLog";
    final static private String PERMISSIONS[] = {Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION};
    static private ConnectivityLogic cl;
    static private DetectionLogic dl;
    static private ClonePhotosLogic cpl;
    static private Context context;
    private boolean isDetect = false;

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        Log.d(TAG, "on create start work");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context = getApplicationContext();
        cl = new ConnectivityLogic(this);
        dl = new DetectionLogic(this);
        cpl = new ClonePhotosLogic(this);

        requestPermissions();
        //this.doDynamicDesign();
        this.setToolbar();
        Log.d(TAG, "on create finish work");
    }

    public void photoGalleryButtonOnClick(View v)
    {
        startActivity(new Intent(MainActivity.this, ImageGalleryActivity.class));
    }

    public void settingButtonOnClick(View v)
    {
        startActivity(new Intent(MainActivity.this, SettingsActivity.class));
    }

    public void wifiButtonOnClick(View v)
    {
        if(!ConnectivityLogic.isConnectedToParkSafely())
            cl.setConnected();
        else
            cl.setDisconnected();
    }

    public void startEndDetectionOnClick(View v)
    {
        if(ConnectivityLogic.isConnectedToParkSafely())
        {
            if (this.isDetect)
                dl.startDetection();
            else
                dl.endDetection();
        }
        else
            Toast.makeText(this.context, "Device Is Not Connected To Park-Safely", Toast.LENGTH_SHORT).show();
    }

    public void cloneOnClick(View v)
    {
        if(ConnectivityLogic.isConnectedToParkSafely())
            cpl.startClone();
        else
            Toast.makeText(context, "Device Is Not Connected To Park-Safely", Toast.LENGTH_SHORT).show();
    }

    private void requestPermissions()
    {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)!=(PackageManager.PERMISSION_GRANTED))
            ActivityCompat.requestPermissions(this, PERMISSIONS, 123);
    }

    public void doDynamicDesign()
    {
        ImageView wifiImg = findViewById(R.id.wifi_img);
        TextView wifiTxt = findViewById(R.id.wifi_txt);
        android.support.v7.widget.CardView startEndDetectionColor = findViewById(R.id.start_end_detection_btn);
        TextView startEndDetectionTxt = findViewById(R.id.start_end_detection_txt);
        ImageView cloneImg = findViewById(R.id.clone_img);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
        {
            if(ConnectivityLogic.isConnectedToParkSafely())
            {
                wifiImg.setImageDrawable(getDrawable(R.drawable.ic_wifi_on));
                wifiTxt.setText("Tap To Disconnect");
                Toast.makeText(context, "Connected To Park-Safely AP", Toast.LENGTH_SHORT).show();

                if(this.isDetect)
                {
                    startEndDetectionColor.setCardBackgroundColor(Color.parseColor("#BC5148")); /*red color*/
                    startEndDetectionTxt.setText("End Detection");
                }
                else
                {
                    startEndDetectionColor.setCardBackgroundColor(Color.parseColor(("#3090A1")));  /*green color*/
                    startEndDetectionTxt.setText("Start Detection");
                }

                if(cpl.hasNewPhotosToClone())
                {
                    cloneImg.setImageDrawable(getDrawable(R.drawable.ic_file_download_red));
                    Toast.makeText(context, "You Have New Photos To Clone", Toast.LENGTH_SHORT).show();
                }
                else
                    cloneImg.setImageDrawable(getDrawable(R.drawable.ic_file_download));
            }
            else
            {
                wifiImg.setImageDrawable(getDrawable(R.drawable.ic_wifi_off));
                wifiTxt.setText("Tap To Connect");
                Toast.makeText(context, "Not Connected To Park-Safely AP", Toast.LENGTH_SHORT).show();

                startEndDetectionColor.setCardBackgroundColor(Color.parseColor("#FF848C8B")); /*gray color*/
                startEndDetectionTxt.setText("Start Detection");

                cloneImg.setImageDrawable(getDrawable(R.drawable.ic_file_download));
            }
        }
        else
            Toast.makeText(context, "some message", Toast.LENGTH_SHORT).show();
    }

    private void setToolbar()
    {
        android.support.v7.widget.Toolbar toolbar;
        toolbar = findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Park-Safely");
    }

    public void setIsDetect(Boolean bool)
    {
        this.isDetect = bool;
    }

    public boolean getIsDetect()
    {
        return this.isDetect;
    }

    public static Context getContext()
    {
        return context;
    }
}
