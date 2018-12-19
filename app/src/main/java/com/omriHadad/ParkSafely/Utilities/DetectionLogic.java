package com.omriHadad.ParkSafely.Utilities;

import android.util.Log;
import android.widget.Toast;
import com.omriHadad.ParkSafely.Activities.MainActivity;
import com.omriHadad.ParkSafely.ServerTasks.StartEndDetectionTask;
import java.util.concurrent.ExecutionException;

public class DetectionLogic
{
    final static private String TAG = "parkSafelyLog";
    final static private String SERVER_ADDRS = "http://192.168.4.1/";
    private  MainActivity ma;

    public DetectionLogic(MainActivity ma)
    {
        this.ma = ma;
    }

    public void startDetection()
    {
        try
        {
            StartEndDetectionTask task = new StartEndDetectionTask(this.ma.getIsDetect());
            String answer = task.execute(SERVER_ADDRS + "start_end_detection").get();
            if (answer.equals("DONE\n"))
            {
                this.ma.setIsDetect(false);
                this.ma.setDetectionBtnColor(FromWhere.startDetection);
                Toast.makeText(this.ma.getContext(), "Detection turned Off", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "end detection");
            }
            else if(answer.equals("ERROR"))
                Toast.makeText(this.ma.getContext(), "Detection Was Not Enabled/Disabled, Try Again", Toast.LENGTH_LONG).show();
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

    public void endDetection()
    {
        try
        {
            StartEndDetectionTask task = new StartEndDetectionTask(this.ma.getIsDetect());
            String answer = task.execute(SERVER_ADDRS + "start_end_detection").get();
            if (answer.equals("DONE\n"))
            {
                this.ma.setIsDetect(true);
                this.ma.setDetectionBtnColor(FromWhere.endDetection);
                Toast.makeText(this.ma.getContext(), "Detection turned On", Toast.LENGTH_SHORT).show();
                Log.d(TAG, "start detection");
            }
            else if(answer.equals("ERROR"))
                Toast.makeText(this.ma.getContext(), "Detection Was Not Enabled/Disabled, Try Again", Toast.LENGTH_LONG).show();
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
