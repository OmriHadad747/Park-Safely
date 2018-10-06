package com.omriHadad.CMIYC;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
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


public class MainActivity extends AppCompatActivity
{
    private Context context;

    final static private String AP_name = "CMIYC_AP";
    final static private String AP_pass = "01234567";
    final private String permissions[] = { Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_WIFI_STATE, Manifest.permission.CHANGE_WIFI_STATE,
            Manifest.permission.ACCESS_NETWORK_STATE} ;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.context = getApplicationContext();
    }

    public void WiFi_button_onClick(View v)
    {
        /*Intent wifi = new Intent(MainActivity.this, com.omriHadad.CMIYC.WifiActivity.class);
        startActivity(wifi);*/

        permissionCheck();
        connectToCMIYC(this.context, this.AP_name, this.AP_pass);
    }

    private void permissionCheck()
    {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                !=
                PackageManager.PERMISSION_GRANTED
                ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.CHANGE_WIFI_STATE)
                        != PackageManager.PERMISSION_GRANTED)
        {
            Log.d("MY_CHECK", "Requesting permissions");

            //Request permission
            ActivityCompat.requestPermissions(this, permissions, 123);
        }
        else
            Log.d("MY_CHECK", "Permissions already granted");

        Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        startActivity(myIntent);

        return;
    }

    private WifiConfiguration createConfig(String AP_name, String AP_pass, String securityMode)
    {
        Log.d("MY_CHECK", "found in Main Activity- createConfig()");

        WifiConfiguration wfConfig = new WifiConfiguration();
        wfConfig.SSID = String.format("\"%s\"", AP_name);

        if (securityMode.equalsIgnoreCase("OPEN") || securityMode.equalsIgnoreCase("WEP"))
        {
            wfConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
        }
        else if (securityMode.equalsIgnoreCase("PSK"))
        {
            Log.i("MY_CHECK", "security mode: " + securityMode);

            wfConfig.preSharedKey = String.format("\"%s\"", AP_pass);
            wfConfig.hiddenSSID = true;
            wfConfig.status = WifiConfiguration.Status.ENABLED;
            wfConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            wfConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            wfConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            wfConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            wfConfig.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
        }
        else
        {
            Log.i("MY_CHECK", "# Unsupported security mode: " + securityMode);

            return null;
        }

        return wfConfig;
    }

    private void wifiEnabling(final WifiManager wfManager)
    {
        Log.d("MY_CHECK", "found in Main Activity- wifiEnabling()");

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

    public String getScanResultSecurity(ScanResult sr)
    {
        final String cap = sr.capabilities;
        final String securityModes[] = {"WEP", "PSK", "EAP"};

        for (int i = securityModes.length - 1; i >= 0; i--)
        {
            if (cap.contains(securityModes[i]))
                return securityModes[i];
        }

        return "OPEN";
    }

    private void connectToCMIYC(Context context, String AP_name, String AP_pass)
    {
        WifiManager wfManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        wifiEnabling(wfManager);

        WifiBroadcastReceiver wfBroadReceiver = new WifiBroadcastReceiver(wfManager);
        registerReceiver(wfBroadReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));

        return;
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
            Log.d("MY_CHECK", "found in WifiBroadcastReceiver - onReceive()");

            String action = intent.getAction();

            if(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(action))
            {
                for(ScanResult sr : wfManager.getScanResults())
                {
                    if(sr.SSID.equals(AP_name))
                    {
                        String securityMode = getScanResultSecurity(sr);
                        WifiConfiguration wfConfig = createConfig(AP_name, AP_pass, securityMode);
                        int networkId = wfManager.addNetwork(wfConfig);
                        Log.d("MY_CHECK", "addNetwork() returned " + networkId);
                        wfManager.disconnect();
                        boolean a = wfManager.enableNetwork(networkId, true);
                        Log.d("MY_CHECK", "reconnect() return: " + a);
                        boolean b = wfManager.reconnect();
                        Log.d("MY_CHECK", "reconnect() return: " + b);

                        break;
                    }
                }

                return;
            }
        }
    }
}
