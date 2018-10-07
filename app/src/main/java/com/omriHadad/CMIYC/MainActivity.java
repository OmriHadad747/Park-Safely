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
import java.util.List;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity
{
    Switch detectionSwitch;
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
    }

    public void configDetectionSwitch()
    {
        this.detectionSwitch = findViewById(R.id.detectionSwitch);
        this.detectionSwitch.setChecked(false);
        this.detectionSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
            {
                if (detectionSwitch.isChecked())
                {
                    ServerTask turnOnTask = new ServerTask();
                    try
                    {
                        String answer=turnOnTask.execute("http://192.168.4.1/start_detection").get();
                        if(answer.equals("OK"))
                            Toast.makeText(context,"Detection Enabled",Toast.LENGTH_SHORT).show();
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
                    ServerTask turnOnTask = new ServerTask();
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

    private void locationEnabling()
    {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != (PackageManager.PERMISSION_GRANTED))
        {
            Log.d("MY_CHECK", "location permission is not granted");
            Log.d("MY_CHECK", "request location permission");
            ActivityCompat.requestPermissions(this, permissions, 123);
        }
        else
        {
            Log.d("MY_CHECK", "location permissions is granted");
        }

        LocationManager lm = (LocationManager)this.context.getSystemService(Context.LOCATION_SERVICE);
        try
        {
            if(lm.isProviderEnabled(LocationManager.GPS_PROVIDER))
            {
                Log.d("MY_CHECK", "location is enabled");
            }
            else
            {
                Log.d("MY_CHECK", "location is disabled");
                Log.d("MY_CHECK", "request for enable location");
                Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(myIntent);
            }

            return;
        }
        catch(Exception e){}
    }

    private void wifiEnabling(final WifiManager wfManager)
    {
        if(wfManager.isWifiEnabled() == false)
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

            builder.setMessage("You have to enable your WIFI befor continue");
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
            alert.show();
        }

        return;
    }

    public void WiFi_button_onClick(View v)
    {
        locationEnabling();
        WifiManager wfManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        wifiEnabling(wfManager);

        this.wfBroadcastReceiver = new WifiBroadcastReceiver(wfManager);
        registerReceiver(wfBroadcastReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        Log.d("MY_CHECK", "connectToCmiyc passed");
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
            Log.d("MY_CHECK", "onReceive() function");

            String action = intent.getAction();

            if(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(action))
            {
                Log.d("MY_CHECK", "start searching after CMIYC");
                List<ScanResult> srl = wfManager.getScanResults();
                Log.d("MY_CHECK", "scan result list size is: " + srl.size());
                for(ScanResult sr : srl)
                {
                    if(sr.SSID.equals(ap_name))
                    {
                        Log.d("MY_CHECK", "CMIYC founded");
                        WifiConfiguration wfConfig = createConfig(ap_name, ap_pass);
                        int networkId = wfManager.addNetwork(wfConfig);
                        Log.d("MY_CHECK", "addNetwork() return: " + networkId);
                        wfManager.disconnect();
                        boolean a = wfManager.enableNetwork(networkId, true);
                        Log.d("MY_CHECK", "enableNetwork() return: " + a);
                        boolean b = wfManager.reconnect();
                        Log.d("MY_CHECK", "reconnect() return: " + b);
                        unregisterReceiver(wfBroadcastReceiver);
                        break;
                    }
                }
            }

            return;
        }
    }
}
