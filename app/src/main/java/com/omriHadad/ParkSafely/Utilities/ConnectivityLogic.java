package com.omriHadad.ParkSafely.Utilities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;
import com.omriHadad.ParkSafely.Activities.MainActivity;
import com.omriHadad.ParkSafely.ServerTasks.UpdateOnConnectionTask;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class ConnectivityLogic
{
    final static private String TAG = "parkSafelyLog";
    final static private String SERVER_ADDRS = "http://192.168.4.1/";
    final static private int CONN_ATTEMPTS = 10;
    final static private int DISCONN_ATTEMPTS = 5;
    private MainActivity ma;
    private FilesLogic fl;
    private WifiManager wfManager ;
    private WifiBroadcastReceiver scanResultBroadcast;
    private WifiBroadcastReceiver wifiConnectivityBroadcast;
    private AccessPointInfo apInfo;
    private int accessPointId;
    private String accessPointName;
    private String accessPointPass;

    public ConnectivityLogic(MainActivity ma)
    {
        this.ma = ma;
        this.wfManager = (WifiManager) ma.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        this.scanResultBroadcast = new WifiBroadcastReceiver(this.wfManager);
        this.wifiConnectivityBroadcast = new WifiBroadcastReceiver(this.wfManager);
        ma.registerReceiver(this.wifiConnectivityBroadcast, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
        this.fl = new FilesLogic(this.ma.getContext());
        apInfo = this.fl.fileHandler();
        this.accessPointName = apInfo.getAccessPointName();
        this.accessPointPass = apInfo.getAccessPointPassword();
        this.setIsConnected(); /*check if connected to park safely and sets the variable isConnected*/
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

    public boolean updateServerAboutConnection(boolean state)
    {
        try
        {
            UpdateOnConnectionTask task = new UpdateOnConnectionTask(state);
            String answer = task.execute(SERVER_ADDRS + "connected_on_off").get();
            if(answer.equals("DONE\n"))
            {
                if(state)
                {
                    ma.setIsConnected(true);
                    ma.setWifiImg(FromWhere.updateConnectionOn); /*update wifi img*/
                    ma.setCloneImg(FromWhere.updateConnectionOn); /*update clone img*/
                    ma.setDetectionBtnColor(FromWhere.updateConnectionOn); /*update start end detection color*/
                    Toast.makeText(ma.getContext(), "Connected To Park-Safely", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "connected");
                    return true;
                }
                else
                {
                    ma.setIsConnected(false);
                    ma.setWifiImg(FromWhere.updateConnectionOff); /*update wifi img*/
                    ma.setCloneImg(FromWhere.updateConnectionOff); /*update clone img*/
                    ma.setDetectionBtnColor(FromWhere.updateConnectionOff); /*update start end detection color*/
                    this.wfManager.disconnect();
                    this.wfManager.disableNetwork(this.accessPointId);
                    this.wfManager.setWifiEnabled(false);
                    Toast.makeText(ma.getContext(), "Disconnected From Park-Safely", Toast.LENGTH_SHORT).show();
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

    /*===========================setters & getters================================================*/

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

    protected AccessPointInfo getApInfo()
    {
        return apInfo;
    }

    protected void setAccessPointId(int id)
    {
        this.accessPointId = id;
    }

    private void setIsConnected()
    {
        if(apInfo.isConnectedToParkSafely(this.wfManager, ma.getContext()))
        {
            WifiInfo wfInfo = this.wfManager.getConnectionInfo();
            if(wfInfo != null)
                this.accessPointId = wfInfo.getNetworkId();
            ma.setIsConnected(true);
        }
        else
            ma.setIsConnected(false);
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
        int attempts = DISCONN_ATTEMPTS;
        while(!updateServerAboutConnection(false) &&  attempts-- > 0);
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
                        ma.unregisterReceiver(scanResultBroadcast);  /*remove scan result event listener*/
                        return;
                    }
                }

                Toast.makeText(ma.getContext(), getAccessPointName() + " AP Is Not Found In Wifi Scan Area, Try Again", Toast.LENGTH_LONG).show();
                ma.unregisterReceiver(scanResultBroadcast);  //remove scan result event listener
            }
            else if(srl.size() == 0)  /*for the case that there is zero access-point in the scan area*/
                Toast.makeText(ma.getContext(), "There Is No Access-Point In The Area", Toast.LENGTH_SHORT).show();
        }

        private void handleConnectivityEvents()
        {
            if(!getApInfo().isConnectedToParkSafely(wfManager, ma.getContext()) && ma.getIsConnected())
            {
                ma.setIsConnected(false);
                ma.setWifiImg(FromWhere.onReceive);
            }
            else if(getApInfo().isConnectedToParkSafely(wfManager, ma.getContext()) && !ma.getIsConnected())
            {
                int attempts = CONN_ATTEMPTS;
                while(!updateServerAboutConnection(true) && attempts-- > 0)
                    Log.d(TAG, "connect - attempt number: " + attempts + " from " + CONN_ATTEMPTS);
            }
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
                handleConnectivityEvents();
            }
        }
    }
}
