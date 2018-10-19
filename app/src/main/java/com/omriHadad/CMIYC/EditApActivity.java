package com.omriHadad.CMIYC;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.EditText;

public class EditApActivity extends AppCompatActivity
{
    private EditText newName, newPassword, confirmNewPassword;
    private Context context;
    final static private String ap_name = "CMIYC_AP";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_ap);
        context = getApplicationContext();
        if(!isConnectionAvailable()){
            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setTitle("Device Is Not Connected");
            alert.setMessage("In order to set device username and password you must be connected to the device, please connect to the device to continue");
            alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    startActivity(new Intent(EditApActivity.this, MainActivity.class));
                }
            });
            alert.create().show();
        }
        /*
        getValues();
        if(isConnectionAvailable())
        {
            if (passwordsIntactness())
            {
                //TODO
            }
        }*/
    }

    private boolean isConnectionAvailable()
    {
        WifiManager wfManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        if (wfManager.isWifiEnabled())
        {
            WifiInfo wfInfo = wfManager.getConnectionInfo();
            if (wfInfo != null)
            {
                if (wfInfo.getSSID().equals(ap_name))
                    return true;
            }
        }

        return false;
    }

    private boolean passwordsIntactness()
    {
        if(newPassword.equals(confirmNewPassword))
            return true;

        return false;
    }

    /*private void getValues()
    {
        newName = findViewById(R.id.editText);
        newPassword = findViewById(R.id.editpass);
        confirmNewPassword = findViewById(R.id.confirmpass);
    }*/
}
