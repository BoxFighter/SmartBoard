package com.example.smartboard.Activity;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.DhcpInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Toast;

import com.example.smartboard.MyDialog.ColorPickerDialog;
import com.example.smartboard.MyMenu.ArcMenu;
import com.example.smartboard.MyMenu.ArcMenu.OnMenuItemClickListener;
import com.example.smartboard.MyNet.SocketMessage;
import com.example.smartboard.MyNet.WifiServerThread;
import com.example.smartboard.MyView.SmartpadView;
import com.example.smartboard.R;
import com.example.smartboard.Wifi.WifiManageUtils;

import static com.example.smartboard.MyNet.WifiServerThread.mMsgList;

public class SmartpadServer extends Activity {

    private ArcMenu mArcMenu;
    private static SmartpadView mSmartpadView;
    private int userNum;
    private String mSSID;
    private String mPassword;
    public static Handler mMainHandler;

    Context context;
    WifiServerThread wifiServerThread;

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_smartpad);
        ininview();
        Toast.makeText(getApplicationContext(),"建立会议成功",Toast.LENGTH_SHORT).show();
        Intent intent=getIntent();
        userNum=Integer.parseInt(intent.getStringExtra("UserNum"));
        mSSID=intent.getStringExtra("MeetingName");
        mPassword=intent.getStringExtra("Password");
        startWifiHot(mSSID,mPassword);
        mMainHandler=new Handler(){
            public void handleMessage(Message msg) {
                // 接收子线程的消息
                switch (msg.what){
                    case 0x789:
                        try {
                            SocketMessage d=(SocketMessage)msg.obj;
                            if(d.status==7){
                                SmartpadView.smartPaint.setStrokeWidth(d.size);
                                SmartpadView.smartPaint.setColor(d.color);
                            }
                            if(d.status==8){
                                mSmartpadView.undo();
                            }else  if(d.status==9){
                                mSmartpadView.redo();
                            }else {
                                mSmartpadView.receiveDrawPath(d);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;
                    case 0x456:
                        Toast.makeText(getApplicationContext(),msg.obj.toString(),Toast.LENGTH_LONG).show();
                        break;
                    default:
                        break;
                }
            }
        };
    }
    public void ininview(){
        context=this;
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        mSmartpadView=(SmartpadView)findViewById(R.id.smartpadview);
        mSmartpadView.SC=true;
        mArcMenu = (ArcMenu) findViewById(R.id.arcmenu);
        mArcMenu.setOnMenuItemClickListener(new OnMenuItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                // TODO Auto-generated method stub
                switch (position){
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
    public void startWifiHot(String mSSID,String mPasswd) {
        WifiManager wifiManager=(WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        if (wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(false);
        }
        WifiManageUtils wifimanageutils=new WifiManageUtils(SmartpadServer.this);
        Boolean b = wifimanageutils.stratWifiAp(mSSID, mPasswd,3);
        if (b) {

            WifiManager wifiManage = (WifiManager) getSystemService(Context.WIFI_SERVICE);
            DhcpInfo info = wifiManage.getDhcpInfo();
            WifiInfo wifiinfo = wifiManage.getConnectionInfo();
            String ip = intToIp(wifiinfo.getIpAddress());
            String serverAddress = intToIp(info.serverAddress);
            System.out.println(serverAddress);

            WifiServerThread serverThread = new WifiServerThread(this,6666,userNum);
            serverThread.start();
            System.out.println("server 端启动成功"+ip);
        } else {
            Toast.makeText(context, "server 端失败，请重试", Toast.LENGTH_SHORT).show();
        }
    }

    private String intToIp(int paramInt) {
        return (paramInt & 0xFF) + "." + (0xFF & paramInt >> 8) + "." + (0xFF & paramInt >> 16) + "."
                + (0xFF & paramInt >> 24);
    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
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
            SocketMessage msg = new SocketMessage();
            msg.from=0;
            msg.status=7;
            msg.color=mSmartpadView.getStrokeColor();
            msg.size=mSmartpadView.getStrokeSize();
            mMsgList.add(msg);
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
            SocketMessage msg = new SocketMessage();
            msg.from=0;
            msg.status=9;
            mMsgList.add(msg);
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
            SocketMessage msg = new SocketMessage();
            msg.from=0;
            msg.status=8;
            mMsgList.add(msg);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void setStrokeColor(){
        SmartpadView.smartPaint.setXfermode(null);
        ColorPickerDialog colorPicker = new ColorPickerDialog(SmartpadServer.this,SmartpadView.smartPaint.getColor());
        colorPicker.show();
        sendMsg();
    }

    public void setStrokeSize(){
        final LinearLayout vii = (LinearLayout) getLayoutInflater().inflate(R.layout.view_strokesize_dialog, null);
        new AlertDialog.Builder(SmartpadServer.this)
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
        new AlertDialog.Builder(SmartpadServer.this)
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
        new AlertDialog.Builder(SmartpadServer.this)
                .setIcon(R.mipmap.logo)
                .setTitle("退出会议")
                .setView(vi)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(wifiServerThread!=null){
                            wifiServerThread.closeServer();
                        }
                        getWindow().setFlags(0, WindowManager.LayoutParams.FLAG_FULLSCREEN);
                        mSmartpadView.saveBitmap();
                        WifiManageUtils wifimanageutils=new WifiManageUtils(SmartpadServer.this);
                        wifimanageutils.closeWifiAp();
                        Intent intent = new Intent();
                        intent.setAction("StartActivity");
                        intent.addCategory("android.intent.category.DEFAULT");
                        startActivity(intent);
                        SmartpadServer.this.finish();
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .create()
                .show();
        System.out.println("会议结束");
    }

    public void undoPath(View view){
        sendUn();
        mSmartpadView.undo();
    }

    public void redoPath(View view){
        sendRe();
        mSmartpadView.redo();
    }

    public void pageTurning(View view){
        mSmartpadView.page_turning();
    }


}
