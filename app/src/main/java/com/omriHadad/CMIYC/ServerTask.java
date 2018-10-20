package com.omriHadad.CMIYC;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class ServerTask extends AsyncTask<String, Void, String>
{
    public String responde;

    public String turnOn(String url)
    {
        String result="";
        HttpClient httpclient = new DefaultHttpClient();

        // Prepare a request object
        HttpGet httpget = new HttpGet(url);

        // Execute the request
        HttpResponse response;
        try
        {
            response = httpclient.execute(httpget);
            // Examine the response status
            //Log.v("Test", "StatusCode: " + response.getStatusLine().getStatusCode() + ", Entity: " + EntityUtils.toString(response.getEntity()));
            //Log.i("Praeda",response.getStatusLine().toString());

            // Get hold of the response entity
            HttpEntity entity = response.getEntity();
            // If the response does not enclose an entity, there is no need
            // to worry about connection release

            if (entity != null)
            {
                // A Simple JSON Response Read
                InputStream instream = entity.getContent();
                result= convertStreamToString(instream);
                // now you have the string representation of the HTML request
                instream.close();
                return result;
            }
        }
        catch (Exception e)
        {
            String se = e.getMessage();
            Log.d("myTag", e.getMessage());
        }
        return result;
    }

    public String connect(String url)
    {
        String result=null;
        HttpParams httpParameters = new BasicHttpParams();
        int timeoutConnection = 3000;
        HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
        int timeoutSocket = 5000;
        HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);
        HttpClient httpclient = new DefaultHttpClient(httpParameters);
        // Prepare a request object
        HttpGet httpget = new HttpGet(url);
        // Execute the request
        HttpResponse response;
        try
        {
            response = httpclient.execute(httpget);
            // Examine the response status
            //Log.v("Test", "StatusCode: " + response.getStatusLine().getStatusCode() + ", Entity: " + EntityUtils.toString(response.getEntity()));
            //Log.i("Praeda",response.getStatusLine().toString());

            // Get hold of the response entity
            HttpEntity entity = response.getEntity();
            // If the response does not enclose an entity, there is no need
            // to worry about connection release

            if (entity != null)
            {
                // A Simple JSON Response Read
                InputStream instream = entity.getContent();
                result= convertStreamToString(instream);
                // now you have the string representation of the HTML request
                instream.close();
                return result;
            }


        }
        catch (Exception e)
        {
            String se = e.getMessage();
            Log.d("myTag", se);
            return "Nok Error";
        }
        return result;
    }

    private String convertStreamToString(InputStream is)
    {
        /*
         * To convert the InputStream to String we use the BufferedReader.readLine()
         * method. We iterate until the BufferedReader return null which means
         * there's no more data to read. Each line will appended to a StringBuilder
         * and returned as String.
         */
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line = null;
        try
        {
            while ((line = reader.readLine()) != null)
                sb.append(line + "\n");

        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        finally
        {
            try
            {
                is.close();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }

    @Override
    protected String doInBackground(String... strings)
    {
        responde=connect(strings[0]);
        return responde;
    }
}
