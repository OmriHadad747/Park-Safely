package com.omriHadad.CMIYC;

import android.content.Context;

public class AccessPointInfo
{
    private Context context;
    private boolean fileCreated = false;
    private boolean firstEntered = false;
    private String accessPointName;
    private String accessPointPass;

    public AccessPointInfo(Context context)
    {
        this.context = context;
        this.accessPointName = "Park-Safely AP";
        this.accessPointPass =  "01234567";
    }

    public boolean isFirstEntered(){return this.firstEntered;}

    public void setFirstEntered(boolean b)
    {
        this.firstEntered = b;
    }

    public boolean isFileCreated(){return this.fileCreated;}

    public void setFileCreated(boolean b)
    {
        this.fileCreated = b;
    }

    public String getAccessPointName(){return this.accessPointName;}

    public void setAccessPointName(String accessPointName)
    {
        this.accessPointName = accessPointName;
    }

    public String getAccessPointPass(){return this.accessPointPass;}

    public void setAccessPointPass(String accessPointPass)
    {
        this.accessPointPass = accessPointPass;
    }
}
