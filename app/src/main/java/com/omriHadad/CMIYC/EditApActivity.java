package com.omriHadad.CMIYC;

import android.content.Context;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.util.concurrent.ExecutionException;

public class EditApActivity extends AppCompatActivity
{
    private EditText newName, newPassword, confirmNewPassword;
    private Context context;
    private String accessPointName;
    final static private String TAG = "EditAp-Activity";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_ap);

        this.context = getApplicationContext();

        accessPointName = getApName();
        if(accessPointName.equals("error"))
            Log.d(TAG, "getApName return error");
    }

    //===========================onClick functions==================================================

    public void applyEditConfig(View v)
    {
        getValues();
        if(isConnectionAvailable())
        {
            if (approvePasswords())
            {
                //TODO
            }
        }
    }

    public void cancelEditConfig(View v)
    {
        //TODO
    }

    //==============================================================================================

    private boolean isConnectionAvailable()
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

    private boolean approvePasswords()
    {
        if(newPassword.equals(confirmNewPassword))
            return true;

        return false;
    }

    private void getValues()
    {
        newName = findViewById(R.id.editText);
        newPassword = findViewById(R.id.editPass);
        confirmNewPassword = findViewById(R.id.confirmPass);
    }

    private String getApName()
    {
        if(isConnectionAvailable())
        {
            try
            {
                return new ServerTask().execute("http://192.168.4.1/get_access_point_name").get();
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
            return "no connection";

        return "error";
    }
}
