package com.example.smartboard.MyNet;

import android.content.Context;

import com.example.smartboard.Activity.SmartpadServer;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

/**
 * Created by 刘宸睿 on 2016/8/13.
 */
public class WifiServerThread extends Thread
{
    public Context context;
    private boolean isStartServer;
    private ServerSocket mServer;
    public static ArrayList<SocketMessage> mMsgList = new ArrayList<SocketMessage>();
    private ArrayList<SocketThread> mThreadList = new ArrayList<SocketThread>();
    private int port;
    private int userNum;

    public class SocketThread extends Thread {

        public int socketID;
        public Socket socket;
        public BufferedWriter writer;
        public BufferedReader reader;

        public SocketThread(Socket socket, int count) {
            socketID = count;
            this.socket = socket;
            SmartpadServer.mMainHandler.obtainMessage(0x456,"新加入一个用户").sendToTarget();
            System.out.println("新增一台客户机，socketID："+socketID);
        }

        @Override
        public void run() {
            super.run();

            try {

                reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), "utf-8"));
                writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), "utf-8"));
                while(isStartServer) {
                    if(reader.ready()) {
                        String data = reader.readLine();
                        JSONObject json = new JSONObject(data);
                        SocketMessage msg = new SocketMessage();
                        msg.status= json.getInt("status");
                        msg.from = socketID;
                        if(msg.status==0||msg.status==1) {
                            msg.startX = Float.valueOf(json.get("x").toString());
                            msg.startY = Float.valueOf(json.get("y").toString());
                        }else if(msg.status<7){
                            msg.startX = Float.valueOf(json.get("x").toString());
                            msg.startY = Float.valueOf(json.get("y").toString());
                            msg.stopX = Float.valueOf(json.get("a").toString()) ;
                            msg.stopY = Float.valueOf(json.get("b").toString()) ;
                        }else if(msg.status==7){
                            msg.color = json.getInt("color");
                            msg.size = json.getInt("size");
                        }
                        SmartpadServer.mMainHandler.obtainMessage(0x789,msg).sendToTarget();
                        mMsgList.add(msg);
                        System.out.println("收到一条消息：socketID"+msg.from);
                    }
                }
                } catch (UnsupportedEncodingException e1) {
                e1.printStackTrace();
            } catch (IOException e1) {
                e1.printStackTrace();
            } catch (JSONException e1) {
                e1.printStackTrace();
            }

        }
    }
    public WifiServerThread(Context context,int port,int userNum) {
        this.context = context;
        this.port=port;
        this.userNum=userNum;
    }

    public void run(){
        try {
            if(mServer==null){
                try {
                    mServer = new ServerSocket(port);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            isStartServer = true;
            System.out.println("启动server,端口："+port);
            Socket socket;
            int socketID = 1;
            startMessageThread();
            //socketID<userNum
            while(isStartServer) {
                try {
                    socket = mServer.accept();
                    System.out.println("监听到一个连接");
                    SocketThread thread = new SocketThread(socket, socketID++);
                    thread.start();
                    mThreadList.add(thread);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            System.out.println("监听连接结束");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void startMessageThread() {
        new Thread(new Runnable(){
            @Override
            public void run() {
                try {
                    while(isStartServer) {
                        if(mMsgList.size() > 0) {
                            SocketMessage from = mMsgList.get(0);
                            for(int i=0;i<mThreadList.size();i++) {
                                SocketThread to=mThreadList.get(i);
                                if((!isServerClose(to.socket)&&(to.socket!=null)&&(to.socketID!=from.from))||from.from==0) {
                                    BufferedWriter writer = to.writer;
                                    JSONObject json = new JSONObject();
                                    json.put("status", from.status);
                                    if(from.status==0||from.status==1) {
                                        json.put("x", from.startX);
                                        json.put("y", from.startY);
                                    }else if(from.status<7){
                                        json.put("x", from.startX);
                                        json.put("y", from.startY);
                                        json.put("a", from.stopX);
                                        json.put("b", from.stopY);
                                    }else if(from.status==7){
                                        json.put("color",from.color);
                                        json.put("size",from.size);
                                    }
                                    writer.write(json.toString()+"\n");
                                    writer.flush();
                                    System.out.println("转发消息成功："+from.status+">> to socketID:"+to.socketID);
                                    break;
                                }
                            }
                            mMsgList.remove(0);
                        }
                        //Thread.sleep(100);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
    public Boolean isServerClose(Socket socket){
        try{
            //发送1个字节的紧急数据，默认情况下，服务器端没有开启紧急数据处理，不影响正常通信
            socket.sendUrgentData(0);
            return false;
        }catch(Exception se){
            return true;
        }
    }
    public  void closeServer(){
        try {
            for (SocketThread s:mThreadList){
                if (s.socket!=null){
                    s.socket.close();
                }
                s.reader.close();
                s.writer.close();
            }
            if(mServer!=null){
                mServer.close();
                System.out.println("server关闭");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}