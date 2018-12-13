package com.omriHadad.ParkSafely;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

public class AccessPointInfo
{
    private String accessPointName;
    private String accessPointPass;

    public AccessPointInfo()
    {
        this.accessPointName = "Park-Safely AP";
        this.accessPointPass =  "01234567";
    }

    public String getAccessPointName(){return this.accessPointName;}

    public String getAccessPointPass(){return this.accessPointPass;}

    public void setAccessPointName(String accessPointName)
    {
        this.accessPointName = accessPointName;
    }

    public void setAccessPointPass(String accessPointPass)
    {
        this.accessPointPass = accessPointPass;
    }

    public boolean isConnectedToParkSafely(WifiManager wfManager, Context context)
    {
        if (wfManager.isWifiEnabled())
        {
            ConnectivityManager connectionManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo nwInfo = connectionManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);

            if(nwInfo != null && nwInfo.isConnected())
            {
                WifiInfo wfInfo = wfManager.getConnectionInfo();
                if (wfInfo != null)
                {
                    String ssid = wfInfo.getSSID().toString();
                    if (ssid.contains(this.accessPointName))
                        return true;
                }
            }
        }

        return false;
    }
}
