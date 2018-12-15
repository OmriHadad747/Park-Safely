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
            InputStream inputStream = this.context.openFileInput(fileName);
            if(inputStream != null)
            {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String line = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ((line = bufferedReader.readLine()) != null)
                    stringBuilder.append(line);

                inputStream.close();
                json = stringBuilder.toString();
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

    public boolean writeJsonFile(AccessPointInfo apInfo, File file)
    {
        try
        {
            FileOutputStream streamOut = new FileOutputStream(file);
            streamOut.write(JSON.toJSONString(apInfo).getBytes());
            return true;
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        return false;
    }
}
