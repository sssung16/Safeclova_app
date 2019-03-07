package com.example.safeclova;


import android.content.Context;
import android.media.MediaRecorder;
import android.os.CountDownTimer;
import android.util.Log;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

import java.io.FileNotFoundException;

public class SpeakerRecognition {
    private static final String BASE_URL = "https://api.voiceit.io";
    private AsyncHttpClient client;
    private String apiKey;
    private String apiToken;

    public SpeakerRecognition(String apiKey,String apiToken){
        this.apiKey = apiKey;
        this.apiToken = apiToken;
        client = new AsyncHttpClient();
        this.client.removeAllHeaders();
        this.client.setTimeout(15 * 1000);
        this.client.setBasicAuth(apiKey, apiToken);
    }

    public void createVoiceEnrollment(String userId, String contentLanguage, File recording, AsyncHttpResponseHandler responseHandler) {
        if(!userIdFormatted(userId)) {
            JSONObject json = new JSONObject();
            try {
                json.put("message", "Incorrectly formatted argument");
            } catch(JSONException e) {
                System.out.println("JSON Exception : " + e.getMessage());
            }
            responseHandler.sendFailureMessage(200, null, json.toString().getBytes(), new Throwable());
            return;
        }
        RequestParams params = new RequestParams();
        params.put("userId", userId);
        params.put("contentLanguage", contentLanguage);
        try {
            params.put("recording", recording);
        } catch (FileNotFoundException e) {
            Log.d("error: ", e.getMessage());
        }

        client.post(getAbsoluteUrl("/enrollments"), params, responseHandler);
    }

    public void createVoiceEnrollment(final String userId, final String contentLanguage, final AsyncHttpResponseHandler responseHandler) {
        if(!userIdFormatted(userId)) {
            JSONObject json = new JSONObject();
            try {
                json.put("message", "Incorrectly formatted argument");
            } catch(JSONException e) {
                System.out.println("JSON Exception : " + e.getMessage());
            }
            responseHandler.sendFailureMessage(200, null, json.toString().getBytes(), new Throwable());
            return;
        }
        try{
            final File recordingFile =  File.createTempFile("tempEnrollmentFile", ".wav");
            final MediaRecorder myRecorder = new MediaRecorder();
            myRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            myRecorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
            myRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
            myRecorder.setAudioSamplingRate(44100);
            myRecorder.setAudioChannels(1);
            myRecorder.setAudioEncodingBitRate(16000);
            myRecorder.setOutputFile(recordingFile.getAbsolutePath());
            myRecorder.prepare();
            myRecorder.start();
            CountDownTimer countDowntimer = new CountDownTimer(4800, 1000) {
                public void onTick(long millisUntilFinished) {}
                public void onFinish() {
                    try{
                        myRecorder.stop();
                        myRecorder.reset();
                        myRecorder.release();
                        createVoiceEnrollment(userId, contentLanguage, recordingFile, responseHandler);
                    } catch(Exception ex){
                        System.out.println("Exception Error:"+ex.getMessage());
                    }
                }
            };
            countDowntimer.start();
        }
        catch(Exception ex)
        {
            System.out.println("Recording Error:" + ex.getMessage());
        }
    }

    public void voiceVerification(String userId, String contentLanguage, File recording, AsyncHttpResponseHandler responseHandler) {
        if(!userIdFormatted(userId)) {
            JSONObject json = new JSONObject();
            try {
                json.put("message", "Incorrectly formatted argument");
            } catch(JSONException e) {
                System.out.println("JSON Exception : " + e.getMessage());
            }
            responseHandler.sendFailureMessage(200, null, json.toString().getBytes(), new Throwable());
            return;
        }
        RequestParams params = new RequestParams();
        params.put("userId", userId);
        params.put("contentLanguage", contentLanguage);
        try {
            params.put("recording", recording);
        } catch (FileNotFoundException e) {
            Log.d("error: ", e.getMessage());
        }
        client.post(getAbsoluteUrl("/verification"), params, responseHandler);
    }

    public void voiceVerification(final String userId, final String contentLanguage, final AsyncHttpResponseHandler responseHandler) {
        if(!userIdFormatted(userId)) {
            JSONObject json = new JSONObject();
            try {
                json.put("message", "Incorrectly formatted argument");
            } catch(JSONException e) {
                System.out.println("JSON Exception : " + e.getMessage());
            }
            responseHandler.sendFailureMessage(200, null, json.toString().getBytes(), new Throwable());
            return;
        }
        try{
            final File recordingFile =  File.createTempFile("tempEnrollmentFile", ".wav");
            final MediaRecorder myRecorder = new MediaRecorder();
            myRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            myRecorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
            myRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
            myRecorder.setAudioSamplingRate(44100);
            myRecorder.setAudioChannels(1);
            myRecorder.setAudioEncodingBitRate(16000);
            myRecorder.setOutputFile(recordingFile.getAbsolutePath());
            myRecorder.prepare();
            myRecorder.start();
            CountDownTimer countDowntimer = new CountDownTimer(4800, 1000) {
                public void onTick(long millisUntilFinished) {}
                public void onFinish() {
                    try{
                        myRecorder.stop();
                        myRecorder.reset();
                        myRecorder.release();
                        voiceVerification(userId, contentLanguage, recordingFile, responseHandler);
                    } catch(Exception ex){
                        System.out.println("Exception Error:"+ex.getMessage());
                    }
                }
            };
            countDowntimer.start();
        }
        catch(Exception ex)
        {
            System.out.println("Recording Error:" + ex.getMessage());
        }
    }

    private boolean userIdFormatted(String arg) {
        String id = arg.substring(arg.lastIndexOf('_') + 1);
        if (!id.matches("[A-Za-z0-9]+")
                || !arg.substring(0, 3).equals("usr")
                || id.length() != 32) {
            System.out.println("UserId is invalid!");
            return false;
        }
        return true;
    }

    private static String getAbsoluteUrl(String relativeUrl) {
        return BASE_URL + relativeUrl;
    }
}
