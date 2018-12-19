package com.omriHadad.ParkSafely.Utilities;

import android.content.Context;
import android.util.Log;
import com.alibaba.fastjson.JSON;
import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class FilesLogic
{
    final static private String TAG = "parkSafelyLog";
    final static private String FILE_NAME = "json_file.txt";
    private Context context;
    private Gson gson;

    public FilesLogic(Context context)
    {
        this.context = context;
        this.gson = new Gson();
    }

    public AccessPointInfo readJsonFile()
    {
        String json = "";
        try
        {
            InputStream inStream = this.context.openFileInput(FILE_NAME);
            if(inStream != null)
            {
                InputStreamReader inStreamReader = new InputStreamReader(inStream);
                BufferedReader buffer = new BufferedReader(inStreamReader);
                String line = "";
                StringBuilder sb = new StringBuilder();

                while ((line = buffer.readLine()) != null)
                    sb.append(line);

                inStream.close();
                json = sb.toString();
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

        return gson.fromJson(json, AccessPointInfo.class);
    }

    public void writeJsonFile(AccessPointInfo apInfo, File file)
    {
        try
        {
            FileOutputStream fOutStream = new FileOutputStream(file);
            fOutStream.write(JSON.toJSONString(apInfo).getBytes());
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    public AccessPointInfo fileHandler()
    {
        AccessPointInfo apInfo = new AccessPointInfo();

        if(!checkIfFileAlreadyExist())
        {
            Log.d(TAG, "its the first time for the user in the app");
            File path = this.context.getFilesDir();
            writeJsonFile(apInfo, new File(path, FILE_NAME));
        }
        else
        {
            Log.d(TAG, "its not the first time for the user in the app");
            apInfo = readJsonFile();
        }

        return apInfo;
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
}
