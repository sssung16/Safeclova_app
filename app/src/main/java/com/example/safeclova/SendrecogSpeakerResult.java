package com.example.safeclova;

/**
 * Created by 조성재 on 2018-05-07.
 */


import android.os.AsyncTask;
import android.util.Log;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class SendrecogSpeakerResult {

    public String sendMessageToServer(String determine)
    {
        String result = null;
        String stringUrl = "https://7002a842.ngrok.io/auth/recogSpeaker";

        try{
            //Setup the request
            HttpURLConnection httpUrlConnection = null;
            URL url = new URL(stringUrl);
            httpUrlConnection = (HttpURLConnection) url.openConnection();

            httpUrlConnection.setUseCaches(false);
            httpUrlConnection.setDoOutput(true);
            httpUrlConnection.setDoInput(true);

            httpUrlConnection.setRequestMethod("POST");
            httpUrlConnection.setRequestProperty("Connection", "Keep-Alive");
            httpUrlConnection.setRequestProperty("Content-Type", "application/json");
            httpUrlConnection.setRequestProperty("Cache-Control", "no-cache");

            // Start content wrapper
            JSONObject msg = new JSONObject();
            msg.put("Bool", determine);
            OutputStream os = httpUrlConnection.getOutputStream();
            os.write(msg.toString().getBytes());
            os.flush();

            BufferedReader rd = null;
            rd = new BufferedReader(new InputStreamReader(httpUrlConnection.getInputStream(), "UTF-8"));
            String line = null;
            while ((line = rd.readLine()) != null) {
                result = line;
                Log.i("Lifeclue", line);
            }
            httpUrlConnection.disconnect();

        }catch (Exception e) {
            Log.e("TAG","AndroidUploader.writeFormField: got: " + e.getMessage());
        }
        return result;
    }
    public class NetworkTask extends AsyncTask<String, Void, String>
    {
        String result;
        @Override
        protected String doInBackground(String... params) {
            result = sendMessageToServer(params[0]);
            return result;
        }

        protected void onPostExecute(String feed) {

        }
    }
}
