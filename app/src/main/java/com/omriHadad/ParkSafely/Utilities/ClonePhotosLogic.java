package com.omriHadad.ParkSafely.Utilities;

import android.util.Log;
import com.omriHadad.ParkSafely.Activities.MainActivity;
import com.omriHadad.ParkSafely.ServerTasks.ClonePhotosTask;
import com.omriHadad.ParkSafely.ServerTasks.HasNewPhotosTask;
import org.json.JSONException;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.concurrent.ExecutionException;

public class ClonePhotosLogic
{
    final static private String TAG = "parkSafelyLog";
    final static private String SERVER_ADDRS = "http://192.168.4.1/";
    private MainActivity ma;

    public ClonePhotosLogic(MainActivity ma)
    {
        this.ma = ma;
    }

    public boolean hasNewPhotosToClone()
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

    public void startClone()
    {
        try
        {
            ClonePhotosTask task = new ClonePhotosTask();
            String answer = task.execute(SERVER_ADDRS + "clone_photos").get();
            Log.d(TAG, answer);
            if(answer.equals("OK\n"))
            {

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
