package com.omriHadad.ParkSafely;

import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

public class UpdateAccessPointTask extends AsyncTask<String, Void, String>
{

    private static JSONObject buildJsonObject(String accessPointName, String accessPointPass)
    {
        JSONObject json = new JSONObject();
        try
        {
            json.accumulate("accessPointName", accessPointName);
            json.accumulate("accessPointPass", accessPointPass);
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

    private String execTask(String url_, String accessPointName, String accessPointPass)
    {
        try
        {
            URL url = new URL(url_);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            //TODO - check if the next line is necessary
            connection.setRequestProperty("Content-Type", "application/json; charset = utf-8");
            JSONObject json = buildJsonObject(accessPointName, accessPointPass);
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
        return execTask(strings[0], strings[1], strings[2]);
    }
}
