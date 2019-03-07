package com.example.safeclova;

import android.content.Context;
import android.content.Intent;
import android.hardware.fingerprint.FingerprintManager;
import android.os.CancellationSignal;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.concurrent.CancellationException;

/**
 * Created by 조성재 on 2018-05-26.
 */

public class FingerPrintHandler extends FingerprintManager.AuthenticationCallback {
    private ImageView imageView;
    private TextView textView;
    private Context context;

    public FingerPrintHandler(Context context, ImageView imageView,TextView textView){
        this.context = context;
        this.imageView = imageView;
        this.textView = textView;
    }

    public void startAuth(FingerprintManager manager, FingerprintManager.CryptoObject cryptoObject){
        CancellationSignal cancellationSignal = new CancellationSignal();
        manager.authenticate(cryptoObject,cancellationSignal,0,this,null);
    }

    @Override
    public void onAuthenticationError(int errMsg,CharSequence helpString){
        Toast.makeText(context,helpString,Toast.LENGTH_LONG).show();
    }

    @Override
    public void onAuthenticationFailed(){
        Toast.makeText(context,"등록되지 않은 지문입니다.",Toast.LENGTH_LONG).show();
        Log.d("Finger","Authentication fail");
    }

    @Override
    public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result){
        //Toast.makeText(context,"지문인증 성공" ,Toast.LENGTH_LONG).show();
        SendrecogFingerResult.NetworkTask task = new SendrecogFingerResult().new NetworkTask();
        task.execute("YES");
        imageView.setImageResource(R.drawable.brownfinger2success);
        textView.setText("지문 인증에 성공하였습니다.");
        Log.d("Finger","Authentication Success");
        textView.setTextColor(textView.getResources().getColor(R.color.success_color));

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(500);
                    Intent intent = new Intent(context,MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        },500);
    }
}
