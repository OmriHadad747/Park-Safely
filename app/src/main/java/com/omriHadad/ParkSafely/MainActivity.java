package com.omriHadad.ParkSafely;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

enum fromWhere{onCreate, onReceive, updateConnectionOn, updateConnectionOff};

public class MainActivity extends AppCompatActivity
{
    final static private String TAG = "main-activity";
    final static private String FILE_NAME = "json_file.txt";
    final static private String permissions[] = {Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION};
    private Context context;
    private static AccessPointInfo apInfo;
    private File file;
    private FileJobs fileJob;
    private WifiManager wfManager ;
    private WifiBroadcastReceiver scanResultBroadcast;
    private WifiBroadcastReceiver wifiBroadcast;
    private int accessPointId;
    private String accessPointName;
    private String accessPointPass;
    private boolean detectionSwitch = true;
    private boolean isConnected;

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //initialize important variables
        this.context = getApplicationContext();
        this.wfManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        this.scanResultBroadcast = new WifiBroadcastReceiver(this.wfManager);
        this.wifiBroadcast = new WifiBroadcastReceiver(this.wfManager);
        this.apInfo = new AccessPointInfo(this.context);
        this.fileJob = new FileJobs(this.context, this.FILE_NAME);

        if(this.apInfo.isConnectedToParkSafely(this.wfManager, this.context))
        {
            WifiInfo wfInfo = this.wfManager.getConnectionInfo();
            if(wfInfo != null)
                this.accessPointId = wfInfo.getNetworkId();
            this.isConnected = true;
        }
        else
            this.isConnected = false;

        fileHandler();  //read or write JSON file to get/set access point name & password
        setWifiImage(fromWhere.onCreate);  //set Images depends on wifi connection status
        setToolbar();  //set toolbar name

    }

    //===========================logical functions==================================================

    private void fileHandler()
    {
        if(!checkIfFileAlreadyExist())
        {
            File path = this.context.getFilesDir();
            this.file = new File(path, FILE_NAME);  //create file for the first time
            this.accessPointName = this.apInfo.getAccessPointName();
            this.accessPointPass = this.apInfo.getAccessPointPass();
            fileJob.writeJsonFile(this.apInfo, this.file);
        }
        else
        {
            this.apInfo = fileJob.readJsonFile();
            this.accessPointName = this.apInfo.getAccessPointName();
            this.accessPointPass = this.apInfo.getAccessPointPass();
        }
    }

    private boolean checkIfFileAlreadyExist()
    {
        FileInputStream streamIn = null;
        try
        {
            streamIn = this.context.openFileInput(FILE_NAME);
            if (streamIn != null)
            {
                try
                {
                    streamIn.close();
                    return true;
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
            return false;
        }

        return false;
    }

    private void enableWifi()
    {
        if(!this.wfManager.isWifiEnabled())
            this.wfManager.setWifiEnabled(true);
        else
        {
            this.wfManager.setWifiEnabled(false);
            this.wfManager.setWifiEnabled(true);
        }
    }

    private void enableLocation()
    {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != (PackageManager.PERMISSION_GRANTED))
        {
            ActivityCompat.requestPermissions(this, permissions, 123);
        }

        LocationManager lm = (LocationManager)this.context.getSystemService(Context.LOCATION_SERVICE);
        try
        {
            if(!lm.isProviderEnabled(LocationManager.GPS_PROVIDER))
            {
                Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(myIntent);
            }
        }
        catch(Exception e){}
    }

    private boolean updateOnConnection(boolean state)
    {
        ServerTask task = new ServerTask();
        String ans = null;

        if(state)
        {
            try
            {
                ans = task.execute("http://192.168.4.1/connected_on").get();
                if(ans.equals("DONE\n"))
                {
                    this.isConnected = true;
                    setWifiImage(fromWhere.updateConnectionOn); //update img
                    Toast.makeText(getContext(), "Connected To Park-Safely", Toast.LENGTH_LONG).show();
                    unregisterReceiver(scanResultBroadcast);  //remove scan result event listener
                    return true;
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
        else if(!state)
        {
            try
            {
                ans = task.execute("http://192.168.4.1/connected_off").get();
                if(ans.equals("DONE\n"))
                {
                    this.isConnected = false;
                    setWifiImage(fromWhere.updateConnectionOff);
                    this.wfManager.disconnect();
                    this.wfManager.disableNetwork(this.accessPointId);
                    this.wfManager.setWifiEnabled(false);
                    Toast.makeText(getContext(), "Disconnected From Park-Safely", Toast.LENGTH_LONG).show();
                    return true;
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

        return false;
    }

    //===========================onClick functions==================================================

    public void photoGalleryButtonOnClick(View v)
    {
        startActivity(new Intent(MainActivity.this, ImageGallery.class));
    }

    public void settingButtonOnClick(View v)
    {
        startActivity(new Intent(MainActivity.this, SettingsActivity.class));
    }

    public void wifiButtonOnClick(View v)
    {
        if(!this.isConnected)
        {
            enableWifi();
            enableLocation();
            registerReceiver(this.scanResultBroadcast, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        }
        else
        {
            int updateAttempts = 20;
            while(!updateOnConnection(false) &&  updateAttempts-- > 0)
                Log.d(TAG, "disconnect - loop number: " + updateAttempts);
        }
    }

    //===========================switches configuration=============================================

    public void detection(View v)
    {
            ServerTask task = new ServerTask();
            String ans = null;
            if (detectionSwitch)
            {
                try
                {
                    detectionSwitch = false;
                    ans = task.execute("http://192.168.4.1/start_detection").get();
                    if (ans.equals("DONE\n"))
                        Toast.makeText(this.context, "Detection Enabled", Toast.LENGTH_LONG).show();
                    else
                        Toast.makeText(this.context, "Device Not found", Toast.LENGTH_LONG).show();
                }
                catch (ExecutionException e)
                {
                    //e.printStackTrace();
                }
                catch (InterruptedException e)
                {
                    //e.printStackTrace();
                }
            }
            else
            {
                try
                {
                    detectionSwitch = true;
                    ans = task.execute("http://192.168.4.1/end_detection").get();
                    if (ans.equals("DONE\n"))
                        Toast.makeText(this.context, "Detection Disabled", Toast.LENGTH_LONG).show();
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

    //===========================setters & getters======================================

    static public AccessPointInfo getApInfo()
    {
        return apInfo;
    }

    protected void setWifiImage(fromWhere howCallMe)
    {
        ImageView wifiImg = findViewById(R.id.wifi_image);
        TextView wifiText = findViewById(R.id.wifi_text);

        switch(howCallMe)
        {
            case onCreate:
            {
                if(isConnected)
                {
                    /*TODO - need to update the server on a connection*/
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                        wifiImg.setImageDrawable(getDrawable(R.drawable.ic_wifi_on));
                    wifiText.setText("Tap To Disconnect");
                    registerReceiver(this.wifiBroadcast, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
                }
                else
                {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                        wifiImg.setImageDrawable(getDrawable(R.drawable.ic_wifi_off));
                    wifiText.setText("Tap To Connect");
                }
                break;
            }
            case updateConnectionOn:
            {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                    wifiImg.setImageDrawable(getDrawable(R.drawable.ic_wifi_on));
                wifiText.setText("Tap To Disconnect");
                registerReceiver(this.wifiBroadcast, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
                break;
            }
            case updateConnectionOff:
            {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                    wifiImg.setImageDrawable(getDrawable(R.drawable.ic_wifi_off));
                wifiText.setText("Tap To Connect");
                unregisterReceiver(this.wifiBroadcast);
            }
            case onReceive:
            {
                if(!this.wfManager.isWifiEnabled())
                {
                    /*TODO - need to update the server on a disconnection*/
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                        wifiImg.setImageDrawable(getDrawable(R.drawable.ic_wifi_off));
                    wifiText.setText("Tap To Connect");
                    unregisterReceiver(this.wifiBroadcast);
                }
                break;
            }
        }
    }

    private void setToolbar()
    {
        android.support.v7.widget.Toolbar toolbar;

        toolbar = findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Park-Safely");
    }

    public MainActivity getThis()
    {
        return this;
    }

    protected void setIsConnected(boolean bool)
    {
        this.isConnected = bool;
    }

    protected void setAccessPointId(int id)
    {
        this.accessPointId = id;
    }

    protected int getAccessPointId()
    {
        return this.accessPointId;
    }

    protected Context getContext()
    {
        return this.context;
    }

    protected String getAccessPointName()
    {
        return this.accessPointName;
    }

    protected String getAccessPointPass()
    {
        return this.accessPointPass;
    }

    //===========================broadcast receiver definition======================================

    public class WifiBroadcastReceiver extends BroadcastReceiver  //this class implements broadcast receiver
    {
        WifiManager wfManager;

        public WifiBroadcastReceiver(WifiManager wfManager)
        {
            this.wfManager = wfManager;
        }

        private WifiConfiguration createConfig()
        {
            /*Log.d(TAG, "createConfig");*/
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

            return wfConfig;
        }

        private void enableNetwork()
        {
            WifiConfiguration wfConfig = createConfig();
            setAccessPointId(this.wfManager.addNetwork(wfConfig));
            this.wfManager.disconnect();
            this.wfManager.enableNetwork(getAccessPointId(), true);
            this.wfManager.reconnect();
        }

        private void connectToParkSafely()
        {
            List<ScanResult> srl = wfManager.getScanResults();
            if (srl.size() > 0)
            {
                for (ScanResult sr : srl)
                {
                    if (sr.SSID.equals(getAccessPointName()))  //if find the desirable access point
                    {
                        enableNetwork();
                        int updateAttempts = 20;
                        while(!updateOnConnection(true) &&  updateAttempts-- > 0)
                            Log.d(TAG, "connect - loop number: " + updateAttempts);
                        return;
                    }
                }
            }

            Toast.makeText(getContext(), "Park-Safely AP Is Not Found In Wifi Scan Area, Try Again", Toast.LENGTH_LONG).show();
            unregisterReceiver(scanResultBroadcast);  //remove scan result event listener
            return;
        }

        @Override
        public void onReceive(Context context, Intent intent)
        {
            String action = intent.getAction();

            if (WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(action))
            {
                connectToParkSafely();
                return;
            }
            else if (ConnectivityManager.CONNECTIVITY_ACTION.equals(action))
            {
                setWifiImage(fromWhere.onReceive);
                return;
            }
        }
    }
}
