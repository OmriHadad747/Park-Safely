package com.omriHadad.ParkSafely.Utilities;

import com.omriHadad.ParkSafely.Activities.MainActivity;
import com.omriHadad.ParkSafely.ServerTasks.HasNewPhotosTask;
import java.util.concurrent.ExecutionException;

public class ClonePhotosLogic
{
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
        //TODO
    }
}
