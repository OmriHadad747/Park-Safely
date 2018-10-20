package com.omriHadad.CMIYC;

import android.content.Context;
import android.widget.Toast;

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
    private AccessPointInfo sf;
    private String fileName;
    private Gson gson;

    public FileJobs(Context context, AccessPointInfo sf, String fileName)
    {
        this.context = context;
        this.sf = sf;
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

    public boolean writeJsonFile(File file)
    {
        try
        {
            FileOutputStream streamOut = new FileOutputStream(file);
            streamOut.write(JSON.toJSONString(this.sf).getBytes());
            return true;
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        return false;
    }
}
