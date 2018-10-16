package com.omriHadad.CMIYC;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.util.List;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity
{
    final static private String TAG = "MY_CHECK";
    ToggleButton detectionSwitch;
    //Switch ReceiveDataSwitch;
    WifiBroadcastReceiver wfBroadcastReceiver;
    private Context context;
    final static private String ap_name = "CMIYC_AP";
    final static private String ap_pass = "01234567";
    final private String permissions[] = { Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION};

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.context = getApplicationContext();

        configDetectionSwitch();
        //configReceiveDataSwitch();
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
        WifiManager wfManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        wifiEnabling(wfManager);
        locationEnabling();

        this.wfBroadcastReceiver = new WifiBroadcastReceiver(wfManager);
        registerReceiver(wfBroadcastReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
    }

    //===========================switches configuration=============================================

    public void configDetectionSwitch()
    {
        this.detectionSwitch = findViewById(R.id.detectionToggle);
        this.detectionSwitch.setChecked(false);
        this.detectionSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                ServerTask turnOnTask = new ServerTask();

                if (detectionSwitch.isChecked())
                {
                    try
                    {
                        String answer=turnOnTask.execute("http://192.168.4.1/start_detection").get();
                        if(answer.equals("OK"))
                            Toast.makeText(context,"Detection Enabled",Toast.LENGTH_SHORT).show();
                        else{
                            Toast.makeText(context,"Device Not found",Toast.LENGTH_SHORT).show();
                        }
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
                else
                {
                    try
                    {
                        String answer=turnOnTask.execute("http://192.168.4.1/end_detection").get();
                        if(answer.equals("OK"))
                            Toast.makeText(getApplicationContext(),"Detection Disabled",Toast.LENGTH_SHORT).show();
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
        });
    }

    //    public void configReceiveDataSwitch()
//    {
//        this.ReceiveDataSwitch = findViewById(R.id.ReceiveDataSwitch);
//        this.ReceiveDataSwitch.setChecked(false);
//        this.ReceiveDataSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
//        {
//            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
//            {
//                ServerTask turnOnTask = new ServerTask();
//
//                if (ReceiveDataSwitch.isChecked())
//                {
//                    try
//                    {
//                        String answer = turnOnTask.execute("http://192.168.4.1/send_to_app").get();
//                        Log.d("MY_CHECK", "data received: " + answer);
//                    }
//                    catch (ExecutionException e)
//                    {
//                        e.printStackTrace();
//                    }
//                    catch (InterruptedException e)
//                    {
//                        e.printStackTrace();
//                    }
//                }
//            }
//        });
//    }

    //===========================general functions==================================================

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

        public WifiBroadcastReceiver(WifiManager wfManager)
        {
            this.wfManager = wfManager;
        }

        @Override
        public void onReceive(Context context, Intent intent)
        {
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
                        if(sr.SSID.equals(ap_name))
                        {
                            Log.d(TAG, "CMIYC is found");
                            WifiConfiguration wfConfig = createConfig(ap_name, ap_pass);
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
                    Toast.makeText(context, "CMIYC is not in wifi scan area", Toast.LENGTH_LONG).show();
                    unregisterReceiver(wfBroadcastReceiver);
                }
            }
        }
    }
}
