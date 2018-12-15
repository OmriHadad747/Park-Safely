package com.omriHadad.ParkSafely.ServerTasks;

import android.os.AsyncTask;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class HasNewPhotosTask extends AsyncTask<String, Void, String>
{
    private String execTask(String url_)
    {
        try
        {
            URL url = new URL(url_);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();
            return connection.getResponseMessage();
        }
        catch (MalformedURLException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        return "ERROR";
    }

    @Override
    protected String doInBackground(String... strings)
    {
        return execTask(strings[0]);
    }
}
