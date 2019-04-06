package com.omriHadad.ParkSafely.Utilities;

import android.graphics.Bitmap;
import android.os.Environment;
import android.util.Base64;
import android.util.Log;
import com.omriHadad.ParkSafely.Activities.MainActivity;
import com.omriHadad.ParkSafely.ServerTasks.ClonePhotosTask;
import com.omriHadad.ParkSafely.ServerTasks.HasNewPhotosTask;
import org.json.JSONException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Random;
import java.util.concurrent.ExecutionException;

public class ClonePhotosLogic
{
    final static private String TAG = "parkSafelyLog";
    final static private String SERVER_ADDRS = "http://192.168.4.1/";
    private MainActivity ma;
    private StringBuilder photo = new StringBuilder();
    private ImageEncoderDecoder imgUtil;

    public ClonePhotosLogic(MainActivity ma)
    {
        this.ma = ma;
        this.imgUtil = new ImageEncoderDecoder();
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
            String answer = "init state";
            while(!answer.equals("DONE\n"))
            {
                ClonePhotosTask task = new ClonePhotosTask();
                answer = task.execute(SERVER_ADDRS + "clone_photos").get();
                if(answer.equals("ERR\n"))
                {
                    Log.d(TAG, answer);
                    break;
                }
                Log.d(TAG, answer);
                this.photo.append(answer);
                this.photo.deleteCharAt(this.photo.length()-1);
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
