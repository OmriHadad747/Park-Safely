package com.omriHadad.CMIYC;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
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
    private boolean connectionFlag = false;
    private int networkId;
    private String accessPointName;
    private String accessPointPass;
    private boolean detectionSwitch = true;
    private ImageView wifiImage;
    private android.support.v7.widget.Toolbar toolbar;

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
        wfBroadcastReceiver = new WifiBroadcastReceiver(wfManager, this.context);

        //read JSON file to get access point name & password
        fileHandler();
        //set Images depends on wifi connection
        setWifiImage();
        //set toolbar name
        setToolbar();
    }

    //===========================logical functions==================================================

    private void fileHandler()
    {
        this.apInfo = new AccessPointInfo(this.context);
        this.fileJob = new FileJobs(this.context, this.apInfo, this.FILE_NAME);
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

    private void wifiEnabling()
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

    private void locationEnabling()
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

    private WifiConfiguration createConfig()
    {
        WifiConfiguration wfConfig = new WifiConfiguration();
        wfConfig.SSID = String.format("\"%s\"", this.accessPointName);
        wfConfig.preSharedKey = String.format("\"%s\"", this.accessPointPass);
        wfConfig.hiddenSSID = true;
        wfConfig.status = WifiConfiguration.Status.ENABLED;
        wfConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
        wfConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
        wfConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
        wfConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
        wfConfig.allowedProtocols.set(WifiConfiguration.Protocol.RSN);

        return wfConfig;
    }

    private boolean isConnectedToPS()
    {
        if (this.wfManager.isWifiEnabled())
        {
            WifiInfo wfInfo = this.wfManager.getConnectionInfo();
            if (wfInfo != null)
            {
                String ssid = wfInfo.getSSID().toString();
                String tmpAccessPointName = "\"" + this.accessPointName + "\"";
                if (ssid.equals(tmpAccessPointName))
                    return true;
            }
        }

        return false;
    }

    private void setWifiImage()
    {
        this.wifiImage = findViewById(R.id.wifi_image);
        if(isConnectedToPS())
        {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                this.wifiImage.setImageDrawable(getDrawable(R.drawable.ic_wifi_on));
        }
        else
        {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                this.wifiImage.setImageDrawable(getDrawable(R.drawable.ic_wifi_off));
        }
    }

    private void setToolbar()
    {
        this.toolbar = findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Park-Safely");
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
        if(!this.connectionFlag)
        {
            wifiEnabling();
            locationEnabling();
            registerReceiver(wfBroadcastReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        }
        else
        {
            wfManager.disconnect();
            wfManager.disableNetwork(networkId);
            this.connectionFlag = false;
            registerReceiver(wfBroadcastReceiver, new IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION));
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
            String action = intent.getAction();

            if(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(action))
            {
                List<ScanResult> srl = wfManager.getScanResults();
                if(srl.size() != 0)
                {
                    for(ScanResult sr : srl)
                    {
                        if(sr.SSID.equals(accessPointName))  //if find the desirable access point
                        {
                            WifiConfiguration wfConfig = createConfig();
                            networkId = wfManager.addNetwork(wfConfig);
                            wfManager.disconnect();
                            wfManager.enableNetwork(networkId, true);
                            wfManager.reconnect();
                            unregisterReceiver(wfBroadcastReceiver);  //remove scan result event listener

                            while(!connectionFlag)  //system update on new connection
                            {
                                if(isConnectedToPS())
                                {
                                    ServerTask task = new ServerTask();
                                    try
                                    {
                                        String answer = task.execute("http://192.168.4.1/set_is_client_connected").get();
                                        if(answer.equals("DONE\n"))
                                            connectionFlag = true;
                                        else
                                            continue;
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
                                else continue;
                            }

                            registerReceiver(wfBroadcastReceiver, new IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION));
                            Toast.makeText(context, "Connected To Park-Safely", Toast.LENGTH_LONG);
                            return;
                        }
                    }

                    Toast.makeText(this.context, "Park-Safely AP Is Not Found In Wifi Scan Area, Try Again", Toast.LENGTH_LONG).show();
                    unregisterReceiver(wfBroadcastReceiver);
                }
            }
            else if(WifiManager.WIFI_STATE_CHANGED_ACTION.equals(action))
            {
                if (isConnectedToPS())
                {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                        wifiImage.setImageDrawable(getDrawable(R.drawable.ic_wifi_on));
                }
                else
                {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                        wifiImage.setImageDrawable(getDrawable(R.drawable.ic_wifi_off));
                }
                unregisterReceiver(wfBroadcastReceiver);
            }
        }
    }
}
