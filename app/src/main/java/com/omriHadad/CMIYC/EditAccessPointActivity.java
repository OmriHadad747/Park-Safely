package com.omriHadad.CMIYC;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;
import java.util.concurrent.ExecutionException;

public class EditAccessPointActivity extends AppCompatActivity
{
    final static private String ERR_CONNECTION = "In order to set device username and password you must be connected to the PS-system, please connect to the device to continue";
    final static private String ERR_PASS = "Something with your inputted passwords was wrong, please do it again";
    final static private String ERR_CONNECTION_TITLE = "Device Is Not Connected";
    final static private String ERR_PASS_TITLE = "Passwords Was Wrong";
    final static private String FILE_NAME = "json_file.txt";
    private Context context;
    private FileJobs fileJob;
    private AccessPointInfo apInfo;
    private EditText newName, newPassword, confirmNewPassword;
    private String currentName;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_access_point);
        context = getApplicationContext();

        this.apInfo = new AccessPointInfo(this.context);
        this.fileJob = new FileJobs(this.context, this.apInfo, this.FILE_NAME);
        this.apInfo = fileJob.readJsonFile();
        this.currentName = "\"" + apInfo.getAccessPointName() + "\"";

        if(!isConnectedToPS())
            showMessage(ERR_CONNECTION_TITLE);
    }

    //===========================logical functions==================================================

    private boolean isConnectedToPS()
    {
        WifiManager wfManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        if (wfManager.isWifiEnabled())
        {
            WifiInfo wfInfo = wfManager.getConnectionInfo();
            if (wfInfo != null)
            {
                String ssid = wfInfo.getSSID();
                if (ssid.equals(currentName))
                    return true;
            }
        }

        return false;
    }

    private boolean confirmPasswords()
    {
        if(this.newPassword.getText().toString().equals(this.confirmNewPassword.getText().toString()))
            return true;

        return false;
    }

    private void getViewValues()
    {
        newName = (EditText)findViewById(R.id.editText);
        newPassword = (EditText)findViewById(R.id.editPass);
        confirmNewPassword = (EditText)findViewById(R.id.confirmPass);
    }

    //===========================onClick functions==================================================

    public void checkButton(View v)
    {
        getViewValues();
        if(!confirmPasswords())
            showMessage(ERR_PASS_TITLE);
        else
        {
            this.apInfo.setAccessPointName(this.newName.getText().toString());
            this.apInfo.setAccessPointPass(this.newPassword.getText().toString());
            this.fileJob.writeJsonFile(new File(this.FILE_NAME));

            ServerTask task = new ServerTask();
            try
            {
                String s1 = "omri";
                String s2 = "omri2";
                String answer = task.execute("http://192.168.4.1/update_access_point_details", s1, s2).get();
                if(answer.equals("DONE\n"))
                {
                    Toast.makeText(this.context, "User Name & Password Saved Successfully", Toast.LENGTH_LONG).show();
                    startActivity(new Intent(EditAccessPointActivity.this, MainActivity.class));
                }
                else
                {
                    Toast.makeText(this.context, "User Name & Password NOT Saved Successfully, Try Again", Toast.LENGTH_LONG).show();
                    startActivity(new Intent(EditAccessPointActivity.this, EditAccessPointActivity.class));
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
    }

    public void cancelButton(View v)
    {
        Toast.makeText(this.context, "Edit Access Point Configuration Was Canceled", Toast.LENGTH_LONG).show();
        startActivity(new Intent(EditAccessPointActivity.this, SettingsActivity.class));
    }

    //===========================messages function==================================================

    private void showMessage(String title)
    {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle(title);
        alert.setCancelable(false);
        if(title.equals(ERR_CONNECTION_TITLE))
        {
            alert.setMessage(ERR_CONNECTION);

            alert.setPositiveButton("OK", new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialog, int which)
                {
                    startActivity(new Intent(EditAccessPointActivity.this, MainActivity.class));
                }
            });
        }
        else if(title.equals(ERR_PASS_TITLE))
        {
            alert.setMessage(ERR_PASS);
            alert.setPositiveButton("OK", new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialog, int which)
                {
                    startActivity(new Intent(EditAccessPointActivity.this, EditAccessPointActivity.class));
                }
            });
        }
        alert.create().show();
    }
}
