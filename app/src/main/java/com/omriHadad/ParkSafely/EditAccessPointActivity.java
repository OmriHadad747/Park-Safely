package com.omriHadad.ParkSafely;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.omriHadad.ParkSafely.ServerTasks.UpdateAccessPointTask;
import com.omriHadad.ParkSafely.Utilities.AccessPointInfo;
import com.omriHadad.ParkSafely.Utilities.FileJobs;

import java.io.File;
import java.util.concurrent.ExecutionException;

public class EditAccessPointActivity extends AppCompatActivity
{
    final static private String TAG = "ParkSafelyLog";
    final static private String FILE_NAME = "json_file.txt";
    final static private String SERVER_ADDRS = "http://192.168.4.1/";
    final static private String ERR_CONNECTION_TITLE = "Device Is Not Connected";
    final static private String ERR_CONNECTION = "In order to set new name and password to the system, " +
            "you have to be connected to Park-Safely first";
    final static private String ERR_PASS_TITLE = "Passwords Was Wrong";
    final static private String ERR_PASS = "Something with your inputted passwords was wrong, please do it again";
    final static private String ERR_INPUT_TITLE = "Invalid Input";
    final static private String ERR_INPUT = "The input is invalid, please do it again";
    private Context context;
    private FileJobs fileJob;
    private AccessPointInfo apInfo;
    private EditText newName, newPassword, confirmNewPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_access_point);

        context = getApplicationContext();
        this.apInfo = MainActivity.getApInfo();
        this.fileJob = new FileJobs(this.context, FILE_NAME);
        this.apInfo = fileJob.readJsonFile();

        WifiManager wfManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        if(!this.apInfo.isConnectedToParkSafely(wfManager, this.context))
            showMessage(ERR_CONNECTION_TITLE, ERR_CONNECTION);
    }

    //===========================logical functions==================================================

    private boolean confirmPasswords()
    {
        return this.newPassword.getText().toString().equals(this.confirmNewPassword.getText().toString());
    }

    private boolean checkViewValues()
    {
        String name = newName.getText().toString();
        String password = newPassword.getText().toString();
        String confirmPassword = confirmNewPassword.getText().toString();

        if(name.equals("") || password.equals("") || confirmPassword.equals(""))
            return false;

        return true;
    }

    private void getViewValues()
    {
        newName = findViewById(R.id.editText);
        newPassword = findViewById(R.id.editPass);
        confirmNewPassword = findViewById(R.id.confirmPass);
    }

    //===========================onClick functions==================================================

    public void checkButton(View v)
    {
        getViewValues();
        if(!checkViewValues())
            showMessage(ERR_INPUT_TITLE, ERR_INPUT);
        else
        {
            if(!confirmPasswords())
                showMessage(ERR_PASS_TITLE, ERR_PASS);
            else
            {
                this.apInfo.setAccessPointName(this.newName.getText().toString());
                this.apInfo.setAccessPointPass(this.newPassword.getText().toString());
                File path = this.context.getFilesDir();
                this.fileJob.writeJsonFile(this.apInfo, new File(path, FILE_NAME)); /*update json file with new values*/
                UpdateAccessPointTask task = new UpdateAccessPointTask(); /*update the server about changes*/
                try
                {
                    String answer = task.execute(
                            SERVER_ADDRS + "update_access_point_details",
                            this.apInfo.getAccessPointName(),
                            this.apInfo.getAccessPointPass()).get();
                    if(answer.equals("DONE\n"))
                    {
                        Toast.makeText(this.context, "User Name & Password Saved Successfully", Toast.LENGTH_LONG).show();
                        startActivity(new Intent(EditAccessPointActivity.this, MainActivity.class));
                    }
                    else if(answer.equals("ERROR"))
                    {
                        Toast.makeText(this.context, "User Name & Password Unsaved successfully, Try Again", Toast.LENGTH_LONG).show();
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
    }

    public void cancelButton(View v)
    {
        Toast.makeText(this.context, "Edit Access Point Configuration Was Canceled", Toast.LENGTH_SHORT).show();
        final Intent goSettingIntent = new Intent(EditAccessPointActivity.this, SettingsActivity.class);
        goSettingIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(goSettingIntent);
    }

    //===========================messages function==================================================

    private void showMessage(String title, String message)
    {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle(title);
        alert.setCancelable(false);

        if(title.equals(ERR_CONNECTION_TITLE))
        {
            final Intent goMainIntent = new Intent(EditAccessPointActivity.this, MainActivity.class);
            goMainIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            alert.setMessage(message);
            alert.setPositiveButton("OK", new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialog, int which)
                {
                    startActivity(goMainIntent);
                }
            });
        }
        else if(title.equals(ERR_PASS_TITLE) || title.equals(ERR_INPUT_TITLE))
        {
            final Intent goEditAccessPointIntent = new Intent(EditAccessPointActivity.this, EditAccessPointActivity.class);
            goEditAccessPointIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            alert.setMessage(message);
            alert.setPositiveButton("OK", new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialog, int which)
                {
                    startActivity(goEditAccessPointIntent);
                }
            });
        }
        alert.create().show();
    }
}
