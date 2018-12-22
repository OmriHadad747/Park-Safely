package com.omriHadad.ParkSafely.Utilities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;
import com.omriHadad.ParkSafely.Activities.MainActivity;

import java.util.List;

public class ConnectivityLogic
{
    final static private String TAG = "parkSafelyLog";
    private MainActivity ma;
    private FilesLogic fl;
    private static WifiManager wfManager ;
    private WifiBroadcastReceiver scanResultBroadcast;
    private WifiBroadcastReceiver wifiConnectivityBroadcast;
    private AccessPointInfo apInfo;
    private int accessPointId;
    private static String accessPointName;
    private static String accessPointPass;

    public ConnectivityLogic(MainActivity ma)
    {
        this.ma = ma;
        wfManager = (WifiManager) ma.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        this.scanResultBroadcast = new WifiBroadcastReceiver();
        this.wifiConnectivityBroadcast = new WifiBroadcastReceiver();
        this.ma.registerReceiver(this.wifiConnectivityBroadcast, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        this.fl = new FilesLogic(MainActivity.getContext());
        this.apInfo = this.fl.fileHandler();
        accessPointName = this.apInfo.getAccessPointName();
        accessPointPass = this.apInfo.getAccessPointPass();
    }

    /*===========================logical functions================================================*/

    public void enableLocation()
    {
        LocationManager lm = (LocationManager)this.ma.getContext().getSystemService(Context.LOCATION_SERVICE);
        boolean flag = false;
        try
        {
            Log.d(TAG, "waiting for location will be enabled by the user");
            while(!lm.isProviderEnabled(LocationManager.GPS_PROVIDER))
            {
                if(!flag)
                {
                    Toast.makeText(ma.getContext(), "You Have To Enable Location First", Toast.LENGTH_SHORT).show();
                    flag = true;
                    this.ma.startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                }
                else continue;
            }
            Log.d(TAG, "location was enabled by the user");
        }
        catch(Exception ignored){}
    }

    public void enableWifi()
    {
        if(!this.wfManager.isWifiEnabled())
            this.wfManager.setWifiEnabled(true);
        else
        {
            this.wfManager.setWifiEnabled(false);
            this.wfManager.setWifiEnabled(true);
        }
    }

    public static boolean isConnectedToParkSafely()
    {
        if (wfManager.isWifiEnabled())
        {
            ConnectivityManager connectionManager = (ConnectivityManager) MainActivity.getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo nwInfo = connectionManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            if(nwInfo != null && nwInfo.isConnected())
            {
                WifiInfo wfInfo = wfManager.getConnectionInfo();
                if (wfInfo != null)
                {
                    String ssid = wfInfo.getSSID().toString();
                    return ssid.contains(accessPointName);
                }
            }
        }

        return false;
    }

    /*===========================setters & getters================================================*/

    protected int getAccessPointId()
    {
        return this.accessPointId;
    }

    protected String getAccessPointName()
    {
        return accessPointName;
    }

    protected String getAccessPointPass()
    {
        return accessPointPass;
    }

    protected void setAccessPointId(int id)
    {
        this.accessPointId = id;
    }

    public void setConnected()
    {
        Log.d(TAG, "try to connect");
        enableLocation();
        enableWifi();
        this.ma.registerReceiver(this.scanResultBroadcast, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
    }

    public void setDisconnected()
    {
        Log.d(TAG, "try to disconnect");
        wfManager.disconnect();
        wfManager.disableNetwork(this.accessPointId);
        wfManager.setWifiEnabled(false);
        this.ma.doDynamicDesign();
    }

    /*===========================broadcast receiver definition====================================*/

    public class WifiBroadcastReceiver extends BroadcastReceiver  /*this class implements broadcast receiver*/
    {

        private WifiConfiguration createConfig()
        {
            Log.d(TAG, "start create Configuration");
            WifiConfiguration wfConfig = new WifiConfiguration();
            wfConfig.SSID = String.format("\"%s\"", getAccessPointName());
            wfConfig.preSharedKey = String.format("\"%s\"", getAccessPointPass());
            wfConfig.hiddenSSID = true;
            wfConfig.status = WifiConfiguration.Status.ENABLED;
            wfConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            wfConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            wfConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            wfConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            wfConfig.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
            Log.d(TAG, "finished create Configuration");
            return wfConfig;
        }

        private void enableNetwork()
        {
            Log.d(TAG, "start enabling network");
            WifiConfiguration wfConfig = createConfig();
            setAccessPointId(wfManager.addNetwork(wfConfig));
            wfManager.disconnect();
            wfManager.enableNetwork(getAccessPointId(), true);
            wfManager.reconnect();
            Log.d(TAG, "finished enabling network");
        }

        private void connectToParkSafelyAP()
        {
            List<ScanResult> srl = wfManager.getScanResults();
            if (srl.size() > 0)
            {
                for (ScanResult sr : srl)
                {
                    if (sr.SSID.equals(getAccessPointName()))  /*if the desirable access point founded*/
                    {
                        Log.d(TAG, "PS-AP was found");
                        enableNetwork();
                        Log.d(TAG, "should be connected");
                        ma.unregisterReceiver(scanResultBroadcast);  /*remove scan result event listener*/
                        return;
                    }
                }

                Toast.makeText(MainActivity.getContext(), getAccessPointName() + " AP Is Not Found In Wifi Scan Area, Try Again", Toast.LENGTH_LONG).show();
                ma.unregisterReceiver(scanResultBroadcast);  //remove scan result event listener
            }
            else if(srl.size() == 0)  /*for the case that there is zero access-point in the scan area*/
                Toast.makeText(MainActivity.getContext(), "There Is No Access-Point In The Area", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onReceive(Context context, Intent intent)
        {
            String action = intent.getAction();

            if (WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(action))
            {
                connectToParkSafelyAP();
            }
            else if (ConnectivityManager.CONNECTIVITY_ACTION.equals(action))
            {
                Log.d(TAG, "connectivity event");
                ma.doDynamicDesign();
            }
        }
    }
}
