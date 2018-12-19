package com.omriHadad.ParkSafely;

import android.Manifest;
import android.content.*;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.wifi.*;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.*;
import android.widget.*;
import android.widget.Toast;
import com.omriHadad.ParkSafely.ServerTasks.*;
import com.omriHadad.ParkSafely.Utilities.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.ExecutionException;

enum fromWhere{onCreate,
    onReceive,
    updateConnectionOn,
    updateConnectionOff,
    startEndDetectionOn,
    startEndDetectionOff,
    setWifiImg}

public class MainActivity extends AppCompatActivity
{
    final static private String TAG = "parkSafelyLog";
    final static private String FILE_NAME = "json_file.txt";
    final static private String SERVER_ADDRS = "http://192.168.4.1/";
    final static private int DISCONN_ATTEMPTS = 5;
    final static private int CONN_ATTEMPTS = 10;
    final static private String permissions[] = {Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION};
    private Context context;
    private static AccessPointInfo apInfo;
    private FileJobs fileJob;
    private WifiManager wfManager ;
    private WifiBroadcastReceiver scanResultBroadcast;
    private WifiBroadcastReceiver wifiConnectivityBroadcast;
    private int accessPointId;
    private String accessPointName;
    private String accessPointPass;
    private boolean isDetect = false;
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
        Log.d(TAG, "on create start work");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        /*initialize important variables*/
        this.context = getApplicationContext();
        this.wfManager = (WifiManager) this.context.getSystemService(Context.WIFI_SERVICE);
        this.scanResultBroadcast = new WifiBroadcastReceiver(this.wfManager);
        this.wifiConnectivityBroadcast = new WifiBroadcastReceiver(this.wfManager);
        registerReceiver(this.wifiConnectivityBroadcast, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        apInfo = new AccessPointInfo();
        this.fileJob = new FileJobs(this.context, FILE_NAME);

        requestPermissions();
        this.fileHandler(); /*read or write JSON file to get/set access point name & password*/
        this.setIsConnected(); /*check if connected to park safely and sets the variable isConnected*/
        this.setWifiImg(fromWhere.onCreate); /*set Images depends on wifi connection status*/
        this.setToolbar();  /*set toolbar name*/
        Log.d(TAG, "on create finish work");
    }

    /*===========================logical functions================================================*/

    private void requestPermissions()
    {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)!=(PackageManager.PERMISSION_GRANTED))
            ActivityCompat.requestPermissions(this, permissions, 123);
    }

    private void fileHandler()
    {
        if(!checkIfFileAlreadyExist())
        {
            Log.d(TAG, "its the first time for the user in the app");
            File path = this.context.getFilesDir();
            fileJob.writeJsonFile(apInfo, new File(path, FILE_NAME));
            this.accessPointName = apInfo.getAccessPointName();
            this.accessPointPass = apInfo.getAccessPointPass();
        }
        else
        {
            Log.d(TAG, "its not the first time for the user in the app");
            apInfo = fileJob.readJsonFile();
            this.accessPointName = apInfo.getAccessPointName();
            this.accessPointPass = apInfo.getAccessPointPass();
        }
    }

    private boolean checkIfFileAlreadyExist()
    {
        try
        {
            FileInputStream fInStream = this.context.openFileInput(FILE_NAME);
            if (fInStream != null)
            {
                fInStream.close();
                return true;
            }
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
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
        LocationManager lm = (LocationManager)this.context.getSystemService(Context.LOCATION_SERVICE);
        boolean flag = false;
        try
        {
            Log.d(TAG, "waiting for location will be enabled by the user");
            while(!lm.isProviderEnabled(LocationManager.GPS_PROVIDER))
            {
                if(!flag)
                {
                    Toast.makeText(context, "You Have To Enable Location First", Toast.LENGTH_SHORT).show();
                    flag = true;
                    startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                }
                else continue;
            }
            Log.d(TAG, "location was enabled by the user");
        }
        catch(Exception ignored){}
    }

    private boolean updateServerAboutConnection(boolean state)
    {
        try
        {
            UpdateOnConnectionTask task = new UpdateOnConnectionTask(state);
            String answer = task.execute(SERVER_ADDRS + "connected_on_off").get();
            if(answer.equals("DONE\n"))
            {
                if(state)
                {
                    this.isConnected = true;
                    this.setWifiImg(fromWhere.updateConnectionOn); /*update wifi img*/
                    this.setCloneImg(fromWhere.updateConnectionOn); /*update clone img*/
                    this.setDetectionBtnColor(fromWhere.updateConnectionOn); /*update start end detection color*/
                    Toast.makeText(this.context, "Connected To Park-Safely", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "connected");
                    return true;
                }
                else if(!state)
                {
                    this.isConnected = false;
                    this.setWifiImg(fromWhere.updateConnectionOff); /*update wifi img*/
                    this.setCloneImg(fromWhere.updateConnectionOff); /*update clone img*/
                    this.setDetectionBtnColor(fromWhere.updateConnectionOff); /*update start end detection color*/
                    this.wfManager.disconnect();
                    this.wfManager.disableNetwork(this.accessPointId);
                    this.wfManager.setWifiEnabled(false);
                    Toast.makeText(this.context, "Disconnected From Park-Safely", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "disconnected");
                    return true;
                }
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

        return false;
    }

    private boolean hasNewPhotosToClone()
    {
        try
        {
            HasNewPhotosTask task = new HasNewPhotosTask();
            String answer = task.execute(SERVER_ADDRS + "has_new_photos").get();
            if(answer.equals("YES\n"))
                return true;
        }
        catch (ExecutionException e)
        {
            e.printStackTrace();
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }

        return false;
    }

    /*===========================onClick functions================================================*/

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
            Log.d(TAG, "try to connect");
            enableLocation();
            enableWifi();
            registerReceiver(this.scanResultBroadcast, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        }
        else
        {
            Log.d(TAG, "try to disconnect");
            int attempts = DISCONN_ATTEMPTS;
            while(!updateServerAboutConnection(false) &&  attempts-- > 0);
        }
    }

    public void startEndDetectionOnClick(View v)
    {
        try
        {
            if(this.isConnected)
            {
                StartEndDetectionTask task = new StartEndDetectionTask(this.isDetect);
                String answer = task.execute(SERVER_ADDRS + "start_end_detection").get();
                if (answer.equals("DONE\n"))
                {
                    if (this.isDetect)
                    {
                        this.isDetect = false;
                        this.setDetectionBtnColor(fromWhere.startEndDetectionOff);
                        Toast.makeText(this.context, "Detection turned Off", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "end detection");
                    }
                    else
                    {
                        this.isDetect = true;
                        this.setDetectionBtnColor(fromWhere.startEndDetectionOn);
                        Toast.makeText(this.context, "Detection turned On", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "start detection");
                    }
                }
                else if(answer.equals("ERROR"))
                    Toast.makeText(this.context, "Detection Was Not Enabled/Disabled, Try Again", Toast.LENGTH_LONG).show();
            }
            else
                Toast.makeText(this.context, "Device Is Not Connected To Park-Safely", Toast.LENGTH_SHORT).show();
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

    public void cloneOnClick(View v)
    {
        if(this.isConnected)
        {
            //TODO
        }
        else
            Toast.makeText(this.context, "Device Is Not Connected To Park-Safely", Toast.LENGTH_SHORT).show();
    }

    /*===========================setters & getters================================================*/

    protected void setDetectionBtnColor(fromWhere whoCallMe)
    {
        android.support.v7.widget.CardView startEndDetectionColor = findViewById(R.id.start_end_detection_btn);
        TextView startEndDetectionTxt = findViewById(R.id.start_end_detection_txt);

        switch(whoCallMe)
        {
            case updateConnectionOn:
            {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                    startEndDetectionColor.setCardBackgroundColor(Color.parseColor(("#3090A1")));  /*green color*/
                startEndDetectionTxt.setText("Start Detection");
                break;
            }
            case updateConnectionOff:
            {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                    startEndDetectionColor.setCardBackgroundColor(Color.parseColor("#FF848C8B")); /*gray color*/
                break;
            }
            case startEndDetectionOn:
            {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                    startEndDetectionColor.setCardBackgroundColor(Color.parseColor("#BC5148")); /*red color*/
                startEndDetectionTxt.setText("End Detection");
                break;
            }
            case startEndDetectionOff:
            {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                    startEndDetectionColor.setCardBackgroundColor(Color.parseColor("#3090A1")); /*green color*/
                startEndDetectionTxt.setText("Start Detection");
                break;
            }
            case setWifiImg:
            {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                    startEndDetectionColor.setCardBackgroundColor(Color.parseColor("#FF848C8B")); /*gray color*/
                startEndDetectionTxt.setText("Start Detection");
                break;
            }
        }
        Log.d(TAG, "setDetectionBtnColor done, call me: " + whoCallMe.toString());
    }

    protected void setCloneImg(fromWhere whoCallMe)
    {
        ImageView cloneImg = findViewById(R.id.clone_img);

        switch(whoCallMe)
        {
            case updateConnectionOn:
            {
                if(hasNewPhotosToClone())
                {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                        cloneImg.setImageDrawable(getDrawable(R.drawable.ic_file_download_red));

                    Toast.makeText(this.context, "You Have New Photos To Clone", Toast.LENGTH_SHORT).show();
                }
                break;
            }
            case updateConnectionOff:
            {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                    cloneImg.setImageDrawable(getDrawable(R.drawable.ic_file_download));
                break;
            }
            case setWifiImg:
            {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                    cloneImg.setImageDrawable(getDrawable(R.drawable.ic_file_download));
                break;
            }
        }
        Log.d(TAG, "setCloneImg done, call me: " + whoCallMe.toString());
    }

    protected void setWifiImg(fromWhere whoCallMe)
    {
        ImageView wifiImg = findViewById(R.id.wifi_img);
        TextView wifiTxt = findViewById(R.id.wifi_txt);

        switch(whoCallMe)
        {
            case onCreate:
            {
                if(this.isConnected)
                {
                    int attempts = 20;
                    while(!updateServerAboutConnection(true) &&  attempts-- > 0)
                        Log.d(TAG, "connect - attempt number: " + attempts);
                    return;
                }
                else
                {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                        wifiImg.setImageDrawable(getDrawable(R.drawable.ic_wifi_off));
                    wifiTxt.setText("Tap To Connect");
                    this.setCloneImg(fromWhere.setWifiImg); /*set clone img, red-if the is new photos to clone, white-the opposite*/
                    this.setDetectionBtnColor(fromWhere.setWifiImg); /*set color for detection button depend on the connection status*/
                }
                break;
            }
            case updateConnectionOn:
            {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                    wifiImg.setImageDrawable(getDrawable(R.drawable.ic_wifi_on));
                wifiTxt.setText("Tap To Disconnect");
                //unregisterReceiver(this.wifiConnectivityBroadcast);
                //registerReceiver(this.wifiConnectivityBroadcast, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
                break;
            }
            case updateConnectionOff:
            {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                    wifiImg.setImageDrawable(getDrawable(R.drawable.ic_wifi_off));
                wifiTxt.setText("Tap To Connect");
                //unregisterReceiver(this.wifiConnectivityBroadcast);
                break;
            }
            case onReceive: /*if the user turned off manually*/
            {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                    wifiImg.setImageDrawable(getDrawable(R.drawable.ic_wifi_off));
                wifiTxt.setText("Tap To Connect");
                this.isConnected = false;
                this.startEndDetectionOnClick(null);
                this.setCloneImg(fromWhere.setWifiImg);
                this.setDetectionBtnColor(fromWhere.setWifiImg);
                //unregisterReceiver(this.wifiConnectivityBroadcast);
                break;
            }
        }
        Log.d(TAG, "setWifiImg done, call me: " + whoCallMe.toString());
    }

    private void setToolbar()
    {
        android.support.v7.widget.Toolbar toolbar;
        toolbar = findViewById(R.id.tool_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Park-Safely");
    }

    private void setIsConnected()
    {
        if(apInfo.isConnectedToParkSafely(this.wfManager, this.context))
        {
            WifiInfo wfInfo = this.wfManager.getConnectionInfo();
            if(wfInfo != null)
                this.accessPointId = wfInfo.getNetworkId();
            this.isConnected = true;
        }
        else
            this.isConnected = false;
    }

    protected void setAccessPointId(int id)
    {
        this.accessPointId = id;
    }

    protected int getAccessPointId()
    {
        return this.accessPointId;
    }

    protected String getAccessPointName()
    {
        return this.accessPointName;
    }

    protected String getAccessPointPass()
    {
        return this.accessPointPass;
    }

    static public AccessPointInfo getApInfo()
    {
        return apInfo;
    }

    protected Context getContext()
    {
        return this.context;
    }

    //===========================broadcast receiver definition======================================

    public class WifiBroadcastReceiver extends BroadcastReceiver  /*this class implements broadcast receiver*/
    {
        private WifiManager wfManager;

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

        private void connectToParkSafelyAP()
        {
            List<ScanResult> srl = this.wfManager.getScanResults();
            if (srl.size() > 0)
            {
                for (ScanResult sr : srl)
                {
                    if (sr.SSID.equals(getAccessPointName()))  /*if the desirable access point founded*/
                    {
                        Log.d(TAG, "park-safely been found");
                        enableNetwork();
                        int attempts = CONN_ATTEMPTS;
                        while(!updateServerAboutConnection(true) && attempts-- > 0)
                            Log.d(TAG, "connect - attempt number: " + attempts + " from " + CONN_ATTEMPTS);
                        unregisterReceiver(scanResultBroadcast);  /*remove scan result event listener*/
                        return;
                    }
                }

                Toast.makeText(context, getAccessPointName() + " AP Is Not Found In Wifi Scan Area, Try Again", Toast.LENGTH_LONG).show();
                unregisterReceiver(scanResultBroadcast);  //remove scan result event listener
            }
            else if(srl.size() == 0)  /*for the case that there is zero access-point in the scan area*/
                Toast.makeText(context, "There Is No Access-Point In The Area", Toast.LENGTH_SHORT).show();
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

                if(!this.wfManager.isWifiEnabled())
                    setWifiImg(fromWhere.onReceive);

                if(getApInfo().isConnectedToParkSafely(wfManager, context))
                {
                    int attempts = CONN_ATTEMPTS;
                    while(!updateServerAboutConnection(true) && attempts-- > 0)
                        Log.d(TAG, "connect - attempt number: " + attempts + " from " + CONN_ATTEMPTS);
                }
            }
        }
    }
}
