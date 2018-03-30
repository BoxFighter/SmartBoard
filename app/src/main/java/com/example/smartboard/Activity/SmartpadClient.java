package com.example.smartboard.Activity;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.net.DhcpInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Toast;

import com.example.smartboard.MyDialog.ColorPickerDialog;
import com.example.smartboard.MyMenu.ArcMenu;
import com.example.smartboard.MyNet.SocketMessage;
import com.example.smartboard.MyView.SmartpadView;
import com.example.smartboard.R;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

public class SmartpadClient extends Activity{

    private ArcMenu mArcMenu;
    private static SmartpadView mSmartpadView;

    private boolean isStartRecieveMsg;
    private Socket mSocket;
    protected BufferedReader mReader;
    protected static BufferedWriter mWriter;
    private SocketHandler mHandler;

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_smartpad);
        ininview();
        try {
            WifiManager wifiManage = (WifiManager) getSystemService(Context.WIFI_SERVICE);
            DhcpInfo info = wifiManage.getDhcpInfo();
            WifiInfo wifiinfo = wifiManage.getConnectionInfo();
            String ip = intToIp(wifiinfo.getIpAddress());
            String serverip = intToIp(info.serverAddress);
            System.out.println(ip);//
            System.out.println(serverip);//
            //Toast.makeText(getApplicationContext(), serverip, Toast.LENGTH_SHORT).show();
            mHandler = new SocketHandler();
            initSocket(serverip, 6666);
            Toast.makeText(getApplicationContext(), "加入会议成功！", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void ininview() {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        mSmartpadView=(SmartpadView)findViewById(R.id.smartpadview);
        mSmartpadView.SC=false;
        mArcMenu = (ArcMenu) findViewById(R.id.arcmenu);
        mArcMenu.setOnMenuItemClickListener(new ArcMenu.OnMenuItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                // TODO Auto-generated method stub
                switch (position) {
                    case 0:
                        mSmartpadView.setStrokePen();
                        mSmartpadView.initAction();
                        break;
                    case 1:
                        mSmartpadView.setStrokeEraser();
                        break;
                    case 2:
                        setStrokeColor();
                        break;
                    case 3:
                        setStrokeSize();
                        break;
                    case 4:
                        drawgeometric();
                        break;
                    case 5:
                        quitMeeting();
                        break;
                    default:
                        break;
                }
            }
        });
    }

    protected void onResume() {
        if (getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
        super.onResume();
    }

    private void initSocket(final String ip, final int port) {
        Thread thread = new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    isStartRecieveMsg = true;
                    mSocket = new Socket(ip, port);

                    // 客户端socket在接收数据时，有两种超时：1. 连接服务器超时，即连接超时；2. 连接服务器成功后，接收服务器数据超时，即接收超时
                    // 设置 socket 读取数据流的超时时间
                    mSocket.setSoTimeout(5000);
                    // 发送数据包，默认为 false，即客户端发送数据采用 Nagle 算法；
                    // 但是对于实时交互性高的程序，建议其改为 true，即关闭 Nagle 算法，客户端每发送一次数据，无论数据包大小都会将这些数据发送出去
                    mSocket.setTcpNoDelay(true);
                    // 设置客户端 socket 关闭时，close() 方法起作用时延迟 30 秒关闭，如果 30 秒内尽量将未发送的数据包发送出去
                    mSocket.setSoLinger(true,15);
                    // 设置输出流和输入流的发送缓冲区大小，默认是40KB，即40960字节
                    mSocket.setSendBufferSize(40960);
                    mSocket.setReceiveBufferSize(40960);
                    mSocket.setOOBInline(true);
                    // 作用：每隔一段时间检查服务器是否处于活动状态，如果服务器端长时间没响应，自动关闭客户端socket
                    // 防止服务器端无效时，客户端长时间处于连接状态
                    mSocket.setKeepAlive(true);

                    System.out.println("连接成功");
                    mReader = new BufferedReader(new InputStreamReader(mSocket.getInputStream(), "utf-8"));
                    mWriter = new BufferedWriter(new OutputStreamWriter(mSocket.getOutputStream(), "utf-8"));


                    while(isStartRecieveMsg) {
                        try{
                            if(mReader.ready()) {
                                mHandler.obtainMessage(0, mReader.readLine()).sendToTarget();
                            }
                        }catch (EOFException e){
                            continue;
                        }
                        //Thread.sleep(200);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        thread.start();
    }
    public static void send() {
        new AsyncTask<String, Integer, String>() {
            @Override
            protected String doInBackground(String... params) {
                sendDP();
                return null;
            }
        }.execute();
    }

    public static void sendDP() {
        try {
            JSONObject json = new JSONObject();
            json.put("status",mSmartpadView.PAINT_STATUS);
            if(mSmartpadView.PAINT_STATUS==0||mSmartpadView.PAINT_STATUS==1) {
                json.put("x", mSmartpadView.x);
                json.put("y", mSmartpadView.y);
            }else{
                json.put("x",mSmartpadView.getStartX());
                json.put("y",mSmartpadView.getStartY());
                json.put("a",mSmartpadView.getStopX());
                json.put("b",mSmartpadView.getStopY());
            }
            mWriter.write(json.toString()+"\n");
            mWriter.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void sendMsg() {
        new AsyncTask<String, Integer, String>() {
            @Override
            protected String doInBackground(String... params) {
                sendPMsg();
                return null;
            }
        }.execute();
    }

    public void sendPMsg() {
        try {
            JSONObject json = new JSONObject();
            json.put("status",7);
            json.put("color",SmartpadView.smartPaint.getColor());
            json.put("size",SmartpadView.smartPaint.getStrokeWidth());
            mWriter.write(json.toString()+"\n");
            mWriter.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendRe() {
        new AsyncTask<String, Integer, String>() {
            @Override
            protected String doInBackground(String... params) {
                sendReMsg();
                return null;
            }
        }.execute();
    }

    public void sendReMsg() {
        try {
            JSONObject json = new JSONObject();
            json.put("status",9);
            mWriter.write(json.toString()+"\n");
            mWriter.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendUn() {
        new AsyncTask<String, Integer, String>() {
            @Override
            protected String doInBackground(String... params) {
                sendUnMsg();
                return null;
            }
        }.execute();
    }

    public void sendUnMsg() {
        try {
            JSONObject json = new JSONObject();
            json.put("status",8);
            mWriter.write(json.toString()+"\n");
            mWriter.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    class SocketHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    try {
                        JSONObject json = new JSONObject((String)msg.obj);
                        int status=json.getInt("status");
                        if(status==0||status==1) {
                            SocketMessage s=new SocketMessage();
                            s.status = status;
                            s.startX = Float.valueOf(json.get("x").toString());
                            s.startY = Float.valueOf(json.get("y").toString());;
                            mSmartpadView.receiveDrawPath(s);
                        }else if(status<7){
                            SocketMessage s=new SocketMessage();
                            s.status = status;
                            s.startX = Float.valueOf(json.get("x").toString());
                            s.startY = Float.valueOf(json.get("y").toString());
                            s.stopX = Float.valueOf(json.get("a").toString());
                            s.stopY = Float.valueOf(json.get("b").toString());
                            mSmartpadView.receiveDrawPath(s);
                        }else if(status==7){
                            SocketMessage s=new SocketMessage();
                            s.color = json.getInt("color");
                            s.size = json.getInt("size");
                            mSmartpadView.receiveDrawPath(s);
                        }else if(status==8){
                            mSmartpadView.undo();
                        }else if(status==9){
                            mSmartpadView.redo();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                default:
                    break;
            }
        }
    }

    public void onBackPressed() {
        super.onBackPressed();
        isStartRecieveMsg = false;
    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    private String intToIp(int ip) {
        return (ip & 0xFF) + "." + ((ip >> 8) & 0xFF) + "." + ((ip >> 16) & 0xFF) + "." + ((ip >> 24) & 0xFF);
    }

    public void setStrokeColor(){
        SmartpadView.smartPaint.setXfermode(null);
        ColorPickerDialog colorPicker = new ColorPickerDialog(SmartpadClient.this,SmartpadView.smartPaint.getColor());
        colorPicker.show();
    }

    public void setStrokeSize(){
        final LinearLayout vii = (LinearLayout) getLayoutInflater().inflate(R.layout.view_strokesize_dialog, null);
        new AlertDialog.Builder(SmartpadClient.this)
                .setIcon(R.mipmap.logo)
                .setTitle("设置画笔粗细")
                .setView(vii)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SeekBar seekBar=(SeekBar) vii.findViewById(R.id.strokesize_bar);
                        SmartpadView.smartPaint.setStrokeWidth(seekBar.getProgress());
                        sendMsg();
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .create()
                .show();
    }

    public void drawgeometric(){
        final LinearLayout vi = (LinearLayout) getLayoutInflater().inflate(R.layout.view_geometric, null);
        new AlertDialog.Builder(SmartpadClient.this)
                .setIcon(R.mipmap.logo)
                .setTitle("插入图形")
                .setView(vi)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        RadioGroup rg=(RadioGroup)vi.findViewById(R.id.geometricgroup);
                        switch (rg.getCheckedRadioButtonId()){
                            case R.id.drawline:
                                mSmartpadView.drawMyLine();
                                break;
                            case R.id.drawcircle:
                                mSmartpadView.drawMyCircle();
                                break;
                            case R.id.drawoval:
                                mSmartpadView.drawMyOval();
                                break;
                            case R.id.drawrect:
                                mSmartpadView.drawMyRect();
                                break;
                            case R.id.drawtable:
                                mSmartpadView.drawMyTable();
                                break;
                            default:break;
                        }
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .create()
                .show();
    }

    public void quitMeeting(){
        final LinearLayout vi = (LinearLayout) getLayoutInflater().inflate(R.layout.view_quit_meeting, null);
        new AlertDialog.Builder(SmartpadClient.this)
                .setIcon(R.mipmap.logo)
                .setTitle("退出会议")
                .setView(vi)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            if(mSocket!=null){
                                mSocket.close();
                            }
                            mWriter.close();
                            mReader.close();
                            mSocket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        getWindow().setFlags(0, WindowManager.LayoutParams.FLAG_FULLSCREEN);
                        mSmartpadView.saveBitmap();
                        Intent intent = new Intent();
                        intent.setAction("StartActivity");
                        intent.addCategory("android.intent.category.DEFAULT");
                        startActivity(intent);
                        SmartpadClient.this.finish();
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .create()
                .show();
    }

    public void undoPath(View view){
        mSmartpadView.undo();
        sendUn();
    }

    public void redoPath(View view){
        mSmartpadView.redo();
        sendRe();
    }

    public void pageTurning(View view){
        mSmartpadView.page_turning();
    }

}
