package com.omriHadad.ParkSafely;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

enum fromWhere{MAIN_ACTIVITY_onCreate, ConnectionTask, WIFI_BROADCAST_RECEIVER_onReceive};

public class MainActivity extends AppCompatActivity
{
    final static private String TAG = "main-activity";
    final static private String FILE_NAME = "json_file.txt";
    final static private String permissions[] = {Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION};
    private Context context;
    private AccessPointInfo apInfo;
    private File file;
    private FileJobs fileJob;
    private WifiManager wfManager ;
    private WifiBroadcastReceiver scanResultBroadcast;
    private WifiBroadcastReceiver wifiBroadcast;
    private int accessPointId;
    private String accessPointName;
    private String accessPointPass;
    private boolean detectionSwitch = true;
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
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //initialize important variables
        this.context = getApplicationContext();
        this.wfManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        this.scanResultBroadcast = new WifiBroadcastReceiver(this.wfManager, this.context);
        this.wifiBroadcast = new WifiBroadcastReceiver(this.wfManager, this.context);
        this.apInfo = new AccessPointInfo(this.context);
        this.fileJob = new FileJobs(this.context, this.apInfo, this.FILE_NAME);

        if(this.apInfo.isConnectedToParkSafely(this.wfManager, this.context))
        {
            WifiInfo wfInfo = this.wfManager.getConnectionInfo();
            if(wfInfo != null)
                this.accessPointId = wfInfo.getNetworkId();
            this.isConnected = true;
        }
        else
            this.isConnected = false;

        fileHandler();  //read or write JSON file to get/set access point name & password
        setWifiImage(fromWhere.MAIN_ACTIVITY_onCreate);  //set Images depends on wifi connection status
        setToolbar();  //set toolbar name

    }

    //===========================logical functions==================================================

    private void fileHandler()
    {
        if(!checkIfFileAlreadyExist())
        {
            File path = this.context.getFilesDir();
            this.file = new File(path, FILE_NAME);  //create file for the first time
            this.apInfo.setFileCreated(true);
            this.apInfo.setFirstEntered(true);
            this.accessPointName = this.apInfo.getAccessPointName();
            this.accessPointPass = this.apInfo.getAccessPointPass();
            fileJob.writeJsonFile(this.file);
        }
        else
        {
            this.apInfo = fileJob.readJsonFile();
            this.accessPointName = this.apInfo.getAccessPointName();
            this.accessPointPass = this.apInfo.getAccessPointPass();
        }
    }

    private boolean checkIfFileAlreadyExist()
    {
        FileInputStream streamIn = null;
        try
        {
            streamIn = this.context.openFileInput(FILE_NAME);
            if (streamIn != null)
            {
                try
                {
                    streamIn.close();
                    return true;
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
            return false;
        }

        return false;
    }

    private void enableWifi()
    {
        if(!this.wfManager.isWifiEnabled())
        {
            this.wfManager.setWifiEnabled(true);
            /*AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

            builder.setMessage("You have to enable your WIFI before continue");
            builder.setCancelable(false);

            builder.setPositiveButton("Turn On", new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialog, int id)
                {
                    wfManager.setWifiEnabled(true);
                    dialog.cancel();
                }
            });

            AlertDialog alert = builder.create();
            alert.show();*/
        }
        else
        {
            this.wfManager.setWifiEnabled(false);
            this.wfManager.setWifiEnabled(true);
        }
    }

    private void enableLocation()
    {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != (PackageManager.PERMISSION_GRANTED))
        {
            ActivityCompat.requestPermissions(this, permissions, 123);
        }

        LocationManager lm = (LocationManager)this.context.getSystemService(Context.LOCATION_SERVICE);
        try
        {
            if(!lm.isProviderEnabled(LocationManager.GPS_PROVIDER))
            {
                Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(myIntent);
            }
        }
        catch(Exception e){}
    }

    //===========================onClick functions==================================================

    public void photoGalleryButtonOnClick(View v)
    {
        startActivity(new Intent(MainActivity.this, ImageGallery.class));
    }

    public void settingButtonOnClick(View v)
    {
        startActivity(new Intent(MainActivity.this, SettingsActivity.class));
    }

    public void wifiButtonOnClick(View v)
    {
        if(!this.isConnected)
        {
            enableWifi();
            enableLocation();
            registerReceiver(this.scanResultBroadcast, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        }
        else
        {
            this.wfManager.disconnect();
            this.wfManager.disableNetwork(this.accessPointId);
            this.wfManager.setWifiEnabled(false);
            this.isConnected = false;
        }
    }

    //===========================switches configuration=============================================

    public void configDetectionSwitch(View v)
    {
            ServerTask task = new ServerTask();

            if (detectionSwitch)
            {
                try
                {
                    detectionSwitch = false;
                    String answer = task.execute("http://192.168.4.1/start_detection").get();
                    if (answer.equals("DONE\n"))
                        Toast.makeText(this.context, "Detection Enabled", Toast.LENGTH_LONG).show();
                    else
                        Toast.makeText(this.context, "Device Not found", Toast.LENGTH_LONG).show();
                }
                catch (ExecutionException e)
                {
                    //e.printStackTrace();
                }
                catch (InterruptedException e)
                {
                    //e.printStackTrace();
                }
            }
            else
            {
                try
                {
                    detectionSwitch = true;
                    String answer = task.execute("http://192.168.4.1/end_detection").get();
                    if (answer.equals("DONE\n"))
                        Toast.makeText(this.context, "Detection Disabled", Toast.LENGTH_LONG).show();
                }
                catch (ExecutionException e)
                {
                    e.printStackTrace();
                }
                catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
            }
    }

    //===========================setters & getters======================================

    protected void setWifiImage(fromWhere howCallMe)
    {
        ImageView wifiImg = findViewById(R.id.wifi_image);
        TextView wifiText = findViewById(R.id.wifi_text);

        switch(howCallMe)
        {
            case MAIN_ACTIVITY_onCreate:
            {
                /*Log.d(TAG, "onCreate: ");*/
                if(isConnected)
                {
                    /*Log.d(TAG, "isConnected: true");*/
                    /*TODO - need to update the server on a connection*/
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                        wifiImg.setImageDrawable(getDrawable(R.drawable.ic_wifi_on));
                    wifiText.setText("Tap To Disconnect");
                    registerReceiver(this.wifiBroadcast, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
                }
                else
                {
                    /*Log.d(TAG, "isConnected: false");*/
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                        wifiImg.setImageDrawable(getDrawable(R.drawable.ic_wifi_off));
                    wifiText.setText("Tap To Connect");
                }
                break;
            }
            case ConnectionTask:
            {
                /*Log.d(TAG, "onReceive_scanResult: ");*/
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                    wifiImg.setImageDrawable(getDrawable(R.drawable.ic_wifi_on));
                wifiText.setText("Tap To Disconnect");
                registerReceiver(this.wifiBroadcast, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
                break;
            }
            case WIFI_BROADCAST_RECEIVER_onReceive:
            {
                /*Log.d(TAG, "onReceive_connectivity: ");*/
                if(!this.wfManager.isWifiEnabled())
                {
                    Log.d(TAG, "isWifiEnabled: false");
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                        wifiImg.setImageDrawable(getDrawable(R.drawable.ic_wifi_off));
                    wifiText.setText("Tap To Connect");
                    unregisterReceiver(this.wifiBroadcast);
                }
                break;
            }
        }
    }

    private void setToolbar()
    {
        android.support.v7.widget.Toolbar toolbar;

        toolbar = findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Park-Safely");
    }

    public MainActivity getThis()
    {
        return this;
    }

    protected void setIsConnected(boolean bool)
    {
        isConnected = bool;
    }

    protected void setAccessPointId(int id)
    {
        accessPointId = id;
    }

    protected int getAccessPointId()
    {
        return accessPointId;
    }

    protected Context getContext()
    {
        return this.context;
    }

    //===========================broadcast receiver definition======================================

    public class WifiBroadcastReceiver extends BroadcastReceiver  //this class implements broadcast receiver
    {
        WifiManager wfManager;
        Context context;

        public WifiBroadcastReceiver(WifiManager wfManager, Context context)
        {
            this.wfManager = wfManager;
            this.context = context;
        }

        @Override
        public void onReceive(Context context, Intent intent)
        {
            String action = intent.getAction();

            if (WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(action))
            {
                /*Log.d(TAG, "scan result");*/
                ConnectionTask task = new ConnectionTask(getThis(), this.wfManager);// creation of async task that will perform the connection action
                try
                {
                    boolean result = task.execute(accessPointName, accessPointPass).get();
                    if(result)
                        Toast.makeText(this.context, "Connected To Park-Safely", Toast.LENGTH_LONG).show();
                    else
                        Toast.makeText(this.context, "Park-Safely AP Is Not Found In Wifi Scan Area, Try Again", Toast.LENGTH_LONG).show();
                }
                catch (ExecutionException e)
                {
                    e.printStackTrace();
                }
                catch (InterruptedException e)
                {
                    e.printStackTrace();
                }
                unregisterReceiver(scanResultBroadcast);  //remove scan result event listener
            }
            else if (ConnectivityManager.CONNECTIVITY_ACTION.equals(action))
            {
                setWifiImage(fromWhere.WIFI_BROADCAST_RECEIVER_onReceive);
                return;
            }
        }
    }
}
