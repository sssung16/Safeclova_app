package com.example.safeclova;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.firebase.iid.FirebaseInstanceId;

import com.loopj.android.http.JsonHttpResponseHandler;
import org.json.JSONException;
import org.json.JSONObject;
import cz.msebera.android.httpclient.Header;
import com.voiceit.voiceit2.VoiceItAPI2;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private ImageButton VoiceEnrollmentBtn;
    private Button Authentication;
    private Button fingerprint;
    public final int PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 1;
    public final int PERMISSIONS_REQUEST_RECORD_AUDIO = 2;
    public VoiceItAPI2 myVoiceIt2;
    public Activity mActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        myVoiceIt2 = new VoiceItAPI2("key_37e62332c3dc4de583efd2bfc7c36ca4","tok_c6adb2241ebd453f95f706324ec6a582");
        mActivity=this;


        //firebase token 발급
        FirebaseInstanceId.getInstance().getToken();
        String token = FirebaseInstanceId.getInstance().getToken();
        Log.d("token", token);

        init();

        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiverLoadTodays, new IntentFilter("update-message"));

        //Permission check
        if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE );
        }
        if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]{android.Manifest.permission.RECORD_AUDIO},
                    PERMISSIONS_REQUEST_RECORD_AUDIO);
        }
    }

    //Push message를 받기위해 broadcastReceive
    private BroadcastReceiver broadcastReceiverLoadTodays = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, final Intent intent) {
            SpeakerRecognition speakerRecognition = new SpeakerRecognition("key_37e62332c3dc4de583efd2bfc7c36ca4","tok_c6adb2241ebd453f95f706324ec6a582");

            AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
            alert.setMessage(intent.getExtras().getString("message"));
            Log.d("intentmessage",intent.getExtras().getString("message"));

            //지문인증일 경우 FingerprintActivity로 넘어감
            if(intent.getExtras().getString("message").equals("지문 인증을 해주세요")){
                startActivity(new Intent(MainActivity.this,FingerprintActivity.class));
            }else{
                //그 외 경우는 alert dialog로 푸시 메세지 내용을 띄운다.(목소리 인증을 해주세요)
                //Speaker recognition
                alert.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss(); //확인 버튼을 누르면 꺼지도록
                    }
                });
                alert.show(); //창을 띄운다.

                //push message 내용이 "목소리 인증을 해주세요"일 경우 authentication 수행
                if(intent.getExtras().getString("message").equals("목소리 인증을 해주세요")){
                    speakerRecognition.voiceVerification("usr_7194daaa8b464b71b7ee62e159f1e4ab", "en-US", new JsonHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                            System.out.println("JSONResult : " + response.toString());
                            try {
                                String Result=response.getString("responseCode");
                                String Score=response.getString("confidence");
                                if(Result.contains("SUCC")){
                                    Toast.makeText(getApplicationContext(), "화자 인증 성공 / 일치율 : " + Score, Toast.LENGTH_LONG).show();
                                    SendrecogSpeakerResult.NetworkTask task = new SendrecogSpeakerResult().new NetworkTask();
                                    task.execute("YES");
                                }else{
                                    Toast.makeText(getApplicationContext(), "화자 인증 실패 / 일치율 : " + Score, Toast.LENGTH_LONG).show();
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                        @Override
                        public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                            if (errorResponse != null) {
                                System.out.println("JSONResult : " + errorResponse.toString());
                            }
                        }});
                }
            }
        }
    };
    //Button들 초기화 및 onclicklistener 설정 -> 테스트 이후엔 VoiceEnrollmentBtn 빼고 삭제 예정
    private void init(){
        VoiceEnrollmentBtn = (ImageButton)findViewById(R.id.VoiceEnrollment);
        VoiceEnrollmentBtn.setOnClickListener(this);

        Authentication = (Button)findViewById(R.id.Authentication);
        Authentication.setOnClickListener(this);

        //fingerprint = (Button)findViewById(R.id.fingerprint);
        //fingerprint.setOnClickListener(this);
    }
    @Override
    public void onClick(View v) {
        SpeakerRecognition speakerRecognition = new SpeakerRecognition("key_37e62332c3dc4de583efd2bfc7c36ca4","tok_c6adb2241ebd453f95f706324ec6a582");

        switch (v.getId()){
            //목소리 등록 버튼을 누를 경우 ( 코 )
            case R.id.VoiceEnrollment:
                speakerRecognition.createVoiceEnrollment("usr_7194daaa8b464b71b7ee62e159f1e4ab", "en-US", new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        System.out.println("JSONResult : " + response.toString());
                        try {
                            String Result=response.getString("responseCode");
                            if(Result.contains("SUCC")){
                                Toast.makeText(getApplicationContext(), "목소리 등록 성공", Toast.LENGTH_LONG).show();
                            }else{
                                Toast.makeText(getApplicationContext(), "목소리 등록 실패", Toast.LENGTH_LONG).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                        if (errorResponse != null) {
                            System.out.println("JSONResult : " + errorResponse.toString());
                        }
                    }
                });
               break;

            //목소리 인증 버튼(테스트 이후 삭제)
            case R.id.Authentication:
                speakerRecognition.voiceVerification("usr_7194daaa8b464b71b7ee62e159f1e4ab", "en-US", new JsonHttpResponseHandler() {
                    @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    System.out.println("JSONResult : " + response.toString());
                        try {
                            String Result=response.getString("responseCode");
                            String Score=response.getString("confidence");
                            if(Result.contains("SUCC")){
                                Toast.makeText(getApplicationContext(), "화자 인증 성공 / 일치율 : " + Score, Toast.LENGTH_LONG).show();
                            }else{
                                Toast.makeText(getApplicationContext(), "화자 인증 실패 / 일치율 : " + Score, Toast.LENGTH_LONG).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    @Override
                    public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                        if (errorResponse != null) {
                            System.out.println("JSONResult : " + errorResponse.toString());
                        }
                    }});
                break;
            //지문 인증 버튼(테스트 이후 삭제)
            //case R.id.fingerprint:
               // startActivity(new Intent(MainActivity.this,FingerprintActivity.class));
               // break;
        }
    }

    //permission request and result
    @Override
    public void onRequestPermissionsResult(int requestCode,String[] permissions,int[] grantResults){
        super.onRequestPermissionsResult(requestCode,permissions,grantResults);
        switch(requestCode){
            case PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE:
                for (int i = 0; i < permissions.length; i++) {
                    String permission = permissions[i];
                    int grantResult = grantResults[i];
                    if (permission.equals(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                        if(grantResult == PackageManager.PERMISSION_GRANTED) {
                            // resultText.setText("Write external storage permission authorized");
                        } else {
                            // resultText.setText("Write external storage permission denied");
                        }
                    }
                }
                break;
            case PERMISSIONS_REQUEST_RECORD_AUDIO:
                for (int i = 0; i < permissions.length; i++) {
                    String permission = permissions[i];
                    int grantResult = grantResults[i];
                    if (permission.equals(Manifest.permission.RECORD_AUDIO)) {
                        if(grantResult == PackageManager.PERMISSION_GRANTED) {
                            // resultText.setText("Record audio permission authorized");
                        } else {
                            // resultText.setText("Record audio permission denied");
                        }
                    }
                }
                break;
        }
    }
}
