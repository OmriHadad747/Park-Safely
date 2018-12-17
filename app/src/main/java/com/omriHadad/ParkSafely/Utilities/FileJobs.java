package com.omriHadad.ParkSafely.Utilities;

import android.content.Context;
import com.alibaba.fastjson.JSON;
import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class FileJobs
{
    private Context context;
    private String fileName;
    private Gson gson;

    public FileJobs(Context context, String fileName)
    {
        this.context = context;
        this.fileName = fileName;
        this.gson = new Gson();
    }

    public AccessPointInfo readJsonFile()
    {
        String json = "";
        try
        {
            InputStream inStream = this.context.openFileInput(fileName);
            if(inStream != null)
            {
                InputStreamReader inStreamReader = new InputStreamReader(inStream);
                BufferedReader buffered = new BufferedReader(inStreamReader);
                String line = "";
                StringBuilder sb = new StringBuilder();

                while ((line = buffered.readLine()) != null)
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
}
