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

public class UpdateOnConnectionTask extends AsyncTask<String, Void, String>
{
    private static boolean isConnected;

    public UpdateOnConnectionTask(boolean bool)
    {
        this.isConnected = bool;
    }

    private static JSONObject buildJsonObject()
    {
        JSONObject json = new JSONObject();
        try
        {
            if (isConnected)
                json.accumulate("state", "true");
            else
                json.accumulate("state", "false");
        }
        catch (JSONException e)
        {
            e.printStackTrace();
        }

        return json;
    }

    private void setPostRequestContent(HttpURLConnection connection, JSONObject json) throws IOException
    {
        OutputStream os = connection.getOutputStream();
        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os, "UTF-8"));
        writer.write(json.toString());
        writer.flush();
        writer.close();
        os.close();
    }

    private String execTask(String url_)
    {
        try
        {
            URL url = new URL(url_);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            //TODO - check if the next line is necessary
            connection.setRequestProperty("Content-Type", "application/json; charset = utf-8");
            JSONObject json = buildJsonObject();
            setPostRequestContent(connection, json);
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
