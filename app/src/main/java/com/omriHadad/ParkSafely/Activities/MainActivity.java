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
import com.omriHadad.ParkSafely.Utilities.FromWhere;

public class MainActivity extends AppCompatActivity
{
    final static private String TAG = "parkSafelyLog";

    final static private String permissions[] = {Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION};
    static private ConnectivityLogic cl;
    static private DetectionLogic dl;
    static private ClonePhotosLogic cpl;
    private Context context;
    private boolean isDetect = false;
    private boolean isConnected;

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

        /*initialize important variables*/
        this.context = getApplicationContext();
        cl = new ConnectivityLogic(this);
        dl = new DetectionLogic(this);
        cpl = new ClonePhotosLogic(this);

        requestPermissions();
        this.setWifiImg(FromWhere.onCreate); /*set Images depends on wifi connection status*/
        this.setToolbar();  /*set toolbar name*/
        Log.d(TAG, "on create finish work");
    }

    /*===========================logical functions================================================*/

    private void requestPermissions()
    {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)!=(PackageManager.PERMISSION_GRANTED))
            ActivityCompat.requestPermissions(this, permissions, 123);
    }

    /*===========================onClick functions================================================*/

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
        if(!this.isConnected)
            cl.setConnected();
        else
            cl.setDisconnected();
    }

    public void startEndDetectionOnClick(View v)
    {
        if(this.isConnected)
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
        if(this.isConnected)
            cpl.startClone();
        else
            Toast.makeText(this.context, "Device Is Not Connected To Park-Safely", Toast.LENGTH_SHORT).show();
    }

    /*===========================setters & getters================================================*/

    public void setDetectionBtnColor(FromWhere whoCallMe)
    {
        android.support.v7.widget.CardView startEndDetectionColor = findViewById(R.id.start_end_detection_btn);
        TextView startEndDetectionTxt = findViewById(R.id.start_end_detection_txt);

        switch(whoCallMe)
        {
            case updateConnectionOn:
            {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                    startEndDetectionColor.setCardBackgroundColor(Color.parseColor(("#3090A1")));  /*green color*/
                startEndDetectionTxt.setText("Start Detection");
                break;
            }
            case updateConnectionOff:
            {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                    startEndDetectionColor.setCardBackgroundColor(Color.parseColor("#FF848C8B")); /*gray color*/
                break;
            }
            case endDetection:
            {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                    startEndDetectionColor.setCardBackgroundColor(Color.parseColor("#BC5148")); /*red color*/
                startEndDetectionTxt.setText("End Detection");
                break;
            }
            case startDetection:
            {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                    startEndDetectionColor.setCardBackgroundColor(Color.parseColor("#3090A1")); /*green color*/
                startEndDetectionTxt.setText("Start Detection");
                break;
            }
            case setWifiImg:
            {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                    startEndDetectionColor.setCardBackgroundColor(Color.parseColor("#FF848C8B")); /*gray color*/
                startEndDetectionTxt.setText("Start Detection");
                break;
            }
        }
        /*Log.d(TAG, "setDetectionBtnColor done, call me: " + whoCallMe.toString());*/
    }

    public void setCloneImg(FromWhere whoCallMe)
    {
        ImageView cloneImg = findViewById(R.id.clone_img);

        switch(whoCallMe)
        {
            case updateConnectionOn:
            {
                if(cpl.hasNewPhotosToClone())
                {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                        cloneImg.setImageDrawable(getDrawable(R.drawable.ic_file_download_red));

                    Toast.makeText(this.context, "You Have New Photos To Clone", Toast.LENGTH_SHORT).show();
                }
                break;
            }
            case updateConnectionOff:
            {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                    cloneImg.setImageDrawable(getDrawable(R.drawable.ic_file_download));
                break;
            }
            case setWifiImg:
            {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                    cloneImg.setImageDrawable(getDrawable(R.drawable.ic_file_download));
                break;
            }
        }
        /*Log.d(TAG, "setCloneImg done, call me: " + whoCallMe.toString());*/
    }

    public void setWifiImg(FromWhere whoCallMe)
    {
        ImageView wifiImg = findViewById(R.id.wifi_img);
        TextView wifiTxt = findViewById(R.id.wifi_txt);

        switch(whoCallMe)
        {
            case onCreate:
            {
                if(this.isConnected)
                {
                    int attempts = 20;
                    while(!cl.updateServerAboutConnection(true) &&  attempts-- > 0)
                        Log.d(TAG, "connect - attempt number: " + attempts);
                    return;
                }
                else
                {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                        wifiImg.setImageDrawable(getDrawable(R.drawable.ic_wifi_off));
                    wifiTxt.setText("Tap To Connect");
                    this.setCloneImg(FromWhere.setWifiImg); /*set clone img, red-if the is new photos to clone, white-the opposite*/
                    this.setDetectionBtnColor(FromWhere.setWifiImg); /*set color for detection button depend on the connection status*/
                }
                break;
            }
            case updateConnectionOn:
            {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                    wifiImg.setImageDrawable(getDrawable(R.drawable.ic_wifi_on));
                wifiTxt.setText("Tap To Disconnect");
                break;
            }
            case updateConnectionOff:
            {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                    wifiImg.setImageDrawable(getDrawable(R.drawable.ic_wifi_off));
                wifiTxt.setText("Tap To Connect");
                break;
            }
            case onReceive: /*if the user turned off manually*/
            {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                    wifiImg.setImageDrawable(getDrawable(R.drawable.ic_wifi_off));
                wifiTxt.setText("Tap To Connect");
                this.isConnected = false;
                this.startEndDetectionOnClick(null);
                this.setCloneImg(FromWhere.setWifiImg);
                this.setDetectionBtnColor(FromWhere.setWifiImg);
                break;
            }
        }
        /*Log.d(TAG, "setWifiImg done, call me: " + whoCallMe.toString());*/
    }

    private void setToolbar()
    {
        android.support.v7.widget.Toolbar toolbar;
        toolbar = findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Park-Safely");
    }

    public void setIsConnected(boolean bool)
    {
        this.isConnected = bool;
    }

    public boolean getIsConnected()
    {
        return this.isConnected;
    }

    public void setIsDetect(Boolean bool)
    {
        this.isDetect = bool;
    }

    public boolean getIsDetect()
    {
        return this.isDetect;
    }

    public Context getContext()
    {
        return this.context;
    }
}
