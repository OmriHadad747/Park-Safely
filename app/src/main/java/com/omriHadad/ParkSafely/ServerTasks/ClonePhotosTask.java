package com.omriHadad.ParkSafely.ServerTasks;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.URL;

public class ClonePhotosTask extends AsyncTask <String, Void, String>
{
    final static private String TAG = "parkSafelyLog";

    private String convertStreamToString(InputStream is)
    {

        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();
        String line = null;
        try
        {
            while ((line = reader.readLine()) != null)
            {
                sb.append(line + "\n");
            }

            is.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        return sb.toString();
    }

    private String execTask(String url_)
    {
        try
        {
            URL url = new URL(url_);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();
            return convertStreamToString(connection.getInputStream());
        }
        catch (MalformedURLException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        return "ERR";
    }

    @Override
    protected String doInBackground(String... strings)
    {
        return execTask(strings[0]);
    }
}
