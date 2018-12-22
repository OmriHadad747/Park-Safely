package com.omriHadad.ParkSafely.Utilities;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

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
}
