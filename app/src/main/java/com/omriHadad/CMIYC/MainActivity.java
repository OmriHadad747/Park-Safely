package com.omriHadad.CMIYC;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.location.LocationManager;
import android.media.Image;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity
{
    final static private String TAG = "main-activity";
    final static private String FILE_NAME = "json_file.txt";
    final static private String permissions[] = { Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION};
    private Context context;
    private File file;
    private AccessPointInfo apInfo;
    private FileJobs fileJob;
    private WifiManager wfManager ;
    private WifiBroadcastReceiver wfBroadcastReceiver;
    private String accessPointName;
    private String accessPointPass;
    boolean detectionSwitch;
    ImageView wifi;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        this.context = getApplicationContext();
        this.detectionSwitch=true;
        wifi = findViewById(R.id.wifi_image);
        wfManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        wfBroadcastReceiver=new WifiBroadcastReceiver(wfManager, this.context);;


        android.support.v7.widget.Toolbar toolbar = findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Park-Safely");
        getSupportActionBar().setSubtitle("Find How Hit You'r Vehicle");

        this.apInfo = new AccessPointInfo(this.context);
        this.fileJob = new FileJobs(this.context, this.apInfo, this.FILE_NAME);

        if(!checkIfFileAlreadyExist())
        {
            File path = this.context.getFilesDir();
            this.file = new File(path, FILE_NAME);  //create file for the first time
            this.apInfo.setFileCreated(true);
            this.apInfo.setFirstEntered(true);
            fileJob.writeJsonFile(this.file);
        }
        {
            this.apInfo = fileJob.readJsonFile();
            this.accessPointName = this.apInfo.getAccessPointName();
            this.accessPointPass = this.apInfo.getAccessPointPass();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        IntentFilter intentFilter = new IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION);
        registerReceiver(wfBroadcastReceiver,intentFilter);
    }

    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(wfBroadcastReceiver);
    }

    //===========================logical functions==================================================

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

        wifiEnabling(wfManager);
        locationEnabling();
        registerReceiver(wfBroadcastReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        if(!isConnectedToPS()){
            showMessage("Failed to connect wireless access point : "+accessPointName,"A Wi-Fi connection cannot be established \nCheck if the wireless access point is turn on and connected and try again.");
        }

    }

    private void showMessage(String title, String msg)
    {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle(title);
        alert.setCancelable(false);
        alert.setMessage(msg);
        alert.setPositiveButton("OK", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {

            }
        });
        alert.create().show();
    }

    //===========================switches configuration=============================================

    public void configDetectionSwitch(View v)
    {
            ServerTask turnOnTask = new ServerTask();

            if (detectionSwitch)
            {
                try
                {
                    detectionSwitch = false;
                    String answer = turnOnTask.execute("http://192.168.4.1/start_detection").get();
                    if (answer.equals("OK"))
                        Toast.makeText(this.context, "Detection Enabled", Toast.LENGTH_SHORT).show();
                    else
                        Toast.makeText(this.context, "Device Not found", Toast.LENGTH_SHORT).show();
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
                    String answer = turnOnTask.execute("http://192.168.4.1/end_detection").get();
                    if (answer.equals("OK"))
                        Toast.makeText(this.context, "Detection Disabled", Toast.LENGTH_SHORT).show();
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

    private void locationEnabling()
    {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != (PackageManager.PERMISSION_GRANTED))
        {
            Log.d(TAG, "location permission is not granted");
            Log.d(TAG, "request location permission");
            ActivityCompat.requestPermissions(this, permissions, 123);
        }
        else
            Log.d(TAG, "location permissions is granted");

        LocationManager lm = (LocationManager)this.context.getSystemService(Context.LOCATION_SERVICE);
        try
        {
            if(lm.isProviderEnabled(LocationManager.GPS_PROVIDER))
                Log.d(TAG, "location is enabled");
            else
            {
                Log.d(TAG, "location is disabled");
                Log.d(TAG, "request for enable location");
                Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(myIntent);
            }
        }
        catch(Exception e){}
    }

    private boolean isConnectedToPS()
    {
        WifiManager wfManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        if (wfManager.isWifiEnabled())
        {
            WifiInfo wfInfo = wfManager.getConnectionInfo();
            if (wfInfo != null)
            {
                if (wfInfo.getSSID().equals(accessPointName))
                    return true;
            }
        }

        return false;
    }

    private void wifiEnabling(final WifiManager wfManager)
    {
        if(!wfManager.isWifiEnabled())
        {
            Log.d(TAG, "turns on the wifi");
            wfManager.setWifiEnabled(true);
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
            Log.d(TAG, "The wifi is already on");
    }

    private WifiConfiguration createConfig(String ap_name, String ap_pass)
    {
        WifiConfiguration wfConfig = new WifiConfiguration();
        wfConfig.SSID = String.format("\"%s\"", ap_name);
        wfConfig.preSharedKey = String.format("\"%s\"", ap_pass);
        wfConfig.hiddenSSID = true;
        wfConfig.status = WifiConfiguration.Status.ENABLED;
        wfConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
        wfConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
        wfConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
        wfConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
        wfConfig.allowedProtocols.set(WifiConfiguration.Protocol.RSN);

        return wfConfig;
    }

    //===========================broadcast receiver definition======================================

    public class WifiBroadcastReceiver extends BroadcastReceiver
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

            //wifi image change listener
            int wifiStateExtra = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE,WifiManager.WIFI_STATE_UNKNOWN);
            switch (wifiStateExtra){
                case WifiManager.WIFI_STATE_ENABLED:
                    Toast.makeText(this.context, "WIFI ON", Toast.LENGTH_LONG).show();
                    WifiInfo wfInfo = wfManager.getConnectionInfo();
                    if (isConnectedToPS())
                    {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            wifi.setImageDrawable(getDrawable(R.drawable.ic_wifi_24dp));
                        }
                    }
                    break;

                default:
                    Toast.makeText(this.context, "WIFI OFF", Toast.LENGTH_LONG).show();
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        wifi.setImageDrawable(getDrawable(R.drawable.ic_wifi_off));
                    }
                    break;

            }
            //end
            String action = intent.getAction();
            int loopCounter = 1;

            if(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(action))
            {
                Log.d(TAG, "start searching after CMIYC");
                List<ScanResult> srl = wfManager.getScanResults();
                Log.d(TAG, "scan result list size is: " + srl.size());

                if(srl.size() != 0)
                {
                    for(ScanResult sr : srl)
                    {
                        if(sr.SSID.equals(accessPointName))
                        {
                            Log.d(TAG, "CMIYC is found");
                            WifiConfiguration wfConfig = createConfig(accessPointName, accessPointPass);
                            int networkId = wfManager.addNetwork(wfConfig);
                            wfManager.disconnect();
                            wfManager.enableNetwork(networkId, true);
                            wfManager.reconnect();
                            unregisterReceiver(wfBroadcastReceiver);
                            break;
                        }

                        Log.d(TAG, "loop number " + loopCounter++ + " pass");
                    }

                    Log.d(TAG, "CMIYC is not in wifi scan area");
                    Toast.makeText(this.context, "Car device is not in wifi scan area", Toast.LENGTH_LONG).show();
                    unregisterReceiver(wfBroadcastReceiver);
                }
            }
        }
    }
}
