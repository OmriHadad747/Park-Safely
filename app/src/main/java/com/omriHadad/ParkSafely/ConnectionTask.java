package com.omriHadad.ParkSafely;

import android.app.ProgressDialog;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.util.Log;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class ConnectionTask extends AsyncTask<String, Void, Boolean>
{
    private static final String TAG = "main-activity";
    private MainActivity ma;
    private WifiManager wfManager;
    private ProgressDialog progress;

    public ConnectionTask(MainActivity ma, WifiManager wfManager)
    {
        /*Log.d(TAG, "ctor");*/
        this.ma = ma;
        this.wfManager = wfManager;
        this.progress = new ProgressDialog(this.ma.getThis());
    }

    private WifiConfiguration createConfig(String accessPointName, String accessPointPass)
    {
        /*Log.d(TAG, "createConfig");*/
        WifiConfiguration wfConfig = new WifiConfiguration();
        wfConfig.SSID = String.format("\"%s\"", accessPointName);
        wfConfig.preSharedKey = String.format("\"%s\"", accessPointPass);
        wfConfig.hiddenSSID = true;
        wfConfig.status = WifiConfiguration.Status.ENABLED;
        wfConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
        wfConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
        wfConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
        wfConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
        wfConfig.allowedProtocols.set(WifiConfiguration.Protocol.RSN);

        return wfConfig;
    }

    private void enableNetwork(String accessPointName, String accessPointPass)
    {
        Log.d(TAG, "name: " + accessPointName +"\npass: " + accessPointPass);
        WifiConfiguration wfConfig = createConfig(accessPointName, accessPointPass);
        this.ma.setAccessPointId(this.wfManager.addNetwork(wfConfig));
        this.wfManager.disconnect();
        this.wfManager.enableNetwork(this.ma.getAccessPointId(), true);
        this.wfManager.reconnect();
    }

    @Override
    protected void onPreExecute()
    {
        /*Log.d(TAG, "onPreExecute");*/
        this.progress = ProgressDialog.show(
                this.ma.getThis(),
                "Connect To Park-Safely",
                "Please Wait While Connecting...",
                false,
                false);
    }

    @Override
    protected Boolean doInBackground(String... str)
    {
        /*Log.d(TAG, "doInBackground");*/
        List<ScanResult> srl = wfManager.getScanResults();
        if (srl.size() > 0)
        {
            for (ScanResult sr : srl)
            {
                if (sr.SSID.equals(str[0]))  //if find the desirable access point
                {
                    this.enableNetwork(str[0], str[1]);
                    for (int count = 10; count > 0; count--)//system updated on new connection
                    {
                        ServerTask task = new ServerTask();
                        try
                        {
                            String answer = task.execute("http://192.168.4.1/set_is_client_connected").get();
                            if (answer.equals("DONE\n"))
                            {
                                this.ma.setIsConnected(true);
                                break;
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

                    this.ma.setWifiImage(fromWhere.ConnectionTask); //update img
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    protected void onPostExecute(Boolean aBoolean)
    {
        this.progress.dismiss();
    }
}
