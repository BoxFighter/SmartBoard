package com.example.smartboard.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import com.example.smartboard.R;

public class MainActivity extends AppCompatActivity{

    private ImageView welcome_image;
    private Handler handler=new Handler();
    int alpha=255;
    int judge=0;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        welcome_image=(ImageView) this.findViewById(R.id.welcome_image);
        welcome_image.setAlpha(alpha);
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (judge<2){
                    try{
                        if(judge==0){
                            Thread.sleep(1000);
                            judge=1;
                        }else {
                            Thread.sleep(50);
                        }
                        loadAPP();
                    }catch (InterruptedException e){
                        e.printStackTrace();
                    }
                }
            }
        }).start();
        handler=new Handler(){
            public void handleMessage(Message msg){
                super.handleMessage(msg);
                welcome_image.setAlpha(alpha);
                welcome_image.invalidate();
            }
        };
    }
    public void loadAPP(){
        alpha -= 5;
        if(alpha<=0){
            judge=2;
            Intent in=new Intent(this,StartActivity.class);
            startActivity(in);
            this.finish();
        }
        handler.sendMessage(handler.obtainMessage());
    }
}