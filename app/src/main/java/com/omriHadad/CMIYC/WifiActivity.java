package com.omriHadad.CMIYC;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


import java.util.List;

public class WifiActivity extends AppCompatActivity
{
    WifiManager wifiManager;
    BroadcastReceiver wifiReciver;
    StringBuilder sb;
    List<ScanResult> wifiList;
    TextView stat;
    ListView peerListView;
    String[] deviceNameArray;
    WifiConfiguration config;
    private String m_Text = "";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wifi);
        stat=findViewById(R.id.status);
        peerListView=findViewById(R.id.peerListView);


        //item click ob list view listener
        peerListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                ScanResult selectedDevice = wifiList.get(position);
                config = new WifiConfiguration();
                config.SSID =quoted(selectedDevice.SSID);
                config.status = WifiConfiguration.Status.ENABLED;
                config.preSharedKey = quoted("12345678");
                config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
                config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
                config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
                config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
                config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
                config.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
                config.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
                int netId = wifiManager.addNetwork(config);
                // mWifiManager.disconnect(); /* disconnect from whichever wifi you're connected to */
                wifiManager.enableNetwork(netId, true);
                //wifiManager.reconnect(); // todo?
                Toast.makeText(getApplicationContext(),"Wifi is Connected to"+selectedDevice.SSID,Toast.LENGTH_SHORT).show();

            }
        });

        wifiManager =(WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if(wifiManager.isWifiEnabled()==false){
            Toast.makeText(getApplicationContext(),"Wifi is disabled...enabling it",Toast.LENGTH_SHORT).show();
            wifiManager.setWifiEnabled(true);
        }
        wifiReciver=new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                sb=new StringBuilder();
                wifiList=wifiManager.getScanResults();

                deviceNameArray = new String[wifiList.size()]; //array of devices names for list view

                sb.append("\n wifi Connection :"+wifiList.size());
                for(int i=0;i<wifiList.size();i++){
                    deviceNameArray[i]=wifiList.get(i).SSID;
                    //sb.append(new Integer(i+1).toString()+"");
                    //sb.append(wifiList.get(i).toString());
                    //sb.append("\n\n");
                }
                ArrayAdapter<String> adapter = new ArrayAdapter<>(getApplicationContext(),android.R.layout.simple_list_item_1,deviceNameArray);
                stat.setText(sb);
                peerListView.setAdapter(adapter);
            }
        };
        registerReceiver(wifiReciver,new IntentFilter(wifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        wifiManager.startScan();
        stat.setText("Start Scan...");
        if(wifiList!=null && wifiList.size()==0) {
            Toast.makeText(getApplicationContext(), "No Device Found", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(wifiReciver,new IntentFilter(wifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(wifiReciver);
    }

    public static String quoted(String s) {
        return "\"" + s + "\"";
    }
}
