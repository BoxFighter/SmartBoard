package com.example.smartboard.Activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

import com.example.smartboard.Adapter.MyListViewAdapter;
import com.example.smartboard.R;
import com.example.smartboard.Interfaces.OnNetworkChangeListener;
import com.example.smartboard.MyView.MyListView;
import com.example.smartboard.MyDialog.WifiConnDialog;
import com.example.smartboard.MyDialog.WifiStatusDialog;
import com.example.smartboard.Wifi.WifiAdminUtils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class JoinMeetingActivity extends Activity {

    protected static final String TAG = "JoinMeetingActivity";

    private static final int REFRESH_CONN = 100;
    private static final int JOIN_MEETING = 200;
    // Wifi管理类
    private WifiAdminUtils mWifiAdmin;
    // 扫描结果列表
    private List<ScanResult> list = new ArrayList<ScanResult>();
    // 显示列表
    private MyListView listView;

    private MyListViewAdapter mAdapter;
    //下标
    private int mPosition;
    private WifiReceiver mReceiver;
    private Handler mHandler = new MyHandler(JoinMeetingActivity.this);
    protected boolean isUpdate = true;
    private int joinFlag;


    private OnNetworkChangeListener mOnNetworkChangeListener = new OnNetworkChangeListener() {

        @Override
        public void onNetWorkDisConnect() {
            getWifiListInfo();
            mAdapter.setDatas(list);
            mAdapter.notifyDataSetChanged();
        }

        @Override
        public void onNetWorkConnect() {
            getWifiListInfo();
            mAdapter.setDatas(list);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            mAdapter.notifyDataSetChanged();
            joinFlag=1;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_join_meeting);
        initData();
        initView();
        setListener();
        refreshWifiStatusOnTime();
    }

    /**
     * 初始化数据
     */
    private void initData() {
        mWifiAdmin = new WifiAdminUtils(this);
        // 获得Wifi列表信息
        getWifiListInfo();
        joinFlag=0;
    }

    /**
     * 初始化View
     */
    private void initView() {

        listView = (MyListView) this.findViewById(R.id.freelook_listview);
        mAdapter = new MyListViewAdapter(this, list);
        listView.setAdapter(mAdapter);
        //检查当前wifi状态
        int wifiState = mWifiAdmin.checkState();
        //WIFI_STATE_DISABLED  WIFI网卡不可用
        //WIFI_STATE_DISABLING  WIFI网卡正在关闭
        //WIFI_STATE_ENABLED  WIFI网卡状态未知
        if (wifiState == WifiManager.WIFI_STATE_DISABLED
                || wifiState == WifiManager.WIFI_STATE_DISABLING
                || wifiState == WifiManager.WIFI_STATE_UNKNOWN) {

        } else {
        }
    }

    private void registerReceiver() {
        mReceiver = new WifiReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        filter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        filter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(mReceiver, filter);
    }

    private void setListener() {
        // 设置刷新监听
        listView.setonRefreshListener(new MyListView.OnRefreshListener() {
            @Override
            public void onRefresh() {
                new AsyncTask<Void, Void, Void>() {
                    protected Void doInBackground(Void... params) {
                        try {
                            Thread.sleep(5000);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        getWifiListInfo();
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Void result) {
                        mAdapter.setDatas(list);
                        mAdapter.notifyDataSetChanged();
                        listView.onRefreshComplete();
                    }

                }.execute();
            }
        });


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int pos,long id) {
                mPosition = pos - 1;
                ScanResult scanResult = list.get(mPosition);
                String desc = "";
                String descOri = scanResult.capabilities;
                if (descOri.toUpperCase().contains("WPA-PSK")) {
                    desc = "WPA";
                }
                if (descOri.toUpperCase().contains("WPA2-PSK")) {
                    desc = "WPA2";
                }
                if (descOri.toUpperCase().contains("WPA-PSK")
                        && descOri.toUpperCase().contains("WPA2-PSK")) {
                    desc = "WPA/WPA2";
                }

                if (desc.equals("")) {
                    isConnectSelf(scanResult);
                    return;
                }
                isConnect(scanResult);
            }

            /**
             * 有密码验证连接
             * @param scanResult
             */
            private void isConnect(ScanResult scanResult) {
                if (mWifiAdmin.isConnect(scanResult)) {
                    // 已连接，显示连接状态对话框
                    WifiStatusDialog mStatusDialog = new WifiStatusDialog(
                            JoinMeetingActivity.this, R.style.defaultDialogStyle,
                            scanResult, mOnNetworkChangeListener);
                    mStatusDialog.show();
                } else {
                    // 未连接显示连接输入对话框
                    WifiConnDialog mDialog = new WifiConnDialog(
                            JoinMeetingActivity.this, R.style.defaultDialogStyle, listView, mPosition, mAdapter,
                            scanResult, list, mOnNetworkChangeListener);
                    mDialog.show();
                }
            }

            /**
             * 无密码直连
             * @param scanResult
             */
            private void isConnectSelf(ScanResult scanResult) {
                if (mWifiAdmin.isConnect(scanResult)) {
                    // 已连接，显示连接状态对话框
                    WifiStatusDialog mStatusDialog = new WifiStatusDialog(
                            JoinMeetingActivity.this, R.style.defaultDialogStyle,
                            scanResult, mOnNetworkChangeListener);
                    mStatusDialog.show();
                } else {
                    boolean iswifi = mWifiAdmin.connectSpecificAP(scanResult);
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (iswifi) {
                        Toast.makeText(JoinMeetingActivity.this, "连接成功！",Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(JoinMeetingActivity.this, "连接失败！", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
    }

    /**
     * 得到wifi的列表信息
     */
    private void getWifiListInfo() {
        Log.d(TAG, "getWifiListInfo");
        mWifiAdmin.startScan();
        List<ScanResult> tmpList = mWifiAdmin.getWifiList();
        if (tmpList == null) {
            list.clear();
        } else {
            list = tmpList;
        }
    }


    @Override
    public void onStart() {
        super.onStart();

    }

    @Override
    public void onStop() {
        super.onStop();

    }

    private class MyHandler extends Handler {

        private WeakReference<JoinMeetingActivity> reference;

        public MyHandler(JoinMeetingActivity activity) {
            this.reference = new WeakReference<JoinMeetingActivity>(activity);
        }

        @Override
        public void handleMessage(Message msg) {

            JoinMeetingActivity activity = reference.get();

            switch (msg.what) {
                case REFRESH_CONN:
                    activity.getWifiListInfo();
                    activity.mAdapter.setDatas(activity.list);
                    activity.mAdapter.notifyDataSetChanged();
                    break;
                case JOIN_MEETING:
                    Intent intent = new Intent();
                    // intent.putExtra("IP_Address",mWifiAdmin.ipIntToString(mWifiAdmin.getIpAddress()));
                    intent.setAction("SmartpadClient");
                    intent.addCategory("android.intent.category.DEFAULT");
                    startActivity(intent);
                default:
                    break;
            }

            super.handleMessage(msg);
        }
    }

    /**
     * 定时刷新Wifi列表信息
     *
     * @author Xiho
     */
    private void refreshWifiStatusOnTime() {
        new Thread() {
            public void run() {
                while (isUpdate) {
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    mHandler.sendEmptyMessage(REFRESH_CONN);
                    if(joinFlag==1){
                        mHandler.sendEmptyMessage(JOIN_MEETING);
                        joinFlag=0;
                        this.interrupt();
                    }

                }
            }
        }.start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isUpdate = false;
        unregisterReceiver();
    }

    /**
     * 取消广播
     */
    private void unregisterReceiver() {
        if (mReceiver != null) {
            unregisterReceiver(mReceiver);
        }
    }

    private class WifiReceiver extends BroadcastReceiver {
        protected static final String TAG = "JoinMeetingActivity";
        //记录网络断开的状态
        private boolean isDisConnected = false;
        //记录正在连接的状态
        private boolean isConnecting = false;

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {// wifi连接上与否
                Log.d(TAG, "网络已经改变");
                NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                if (info.getState().equals(NetworkInfo.State.DISCONNECTED)) {
                    if (!isDisConnected) {
                        Log.d(TAG, "wifi已经断开");
                        isDisConnected = true;
                    }
                } else if (info.getState().equals(NetworkInfo.State.CONNECTING)) {
                    if (!isConnecting) {
                        Log.d(TAG, "正在连接...");
                        isConnecting = true;
                    }
                } else if (info.getState().equals(NetworkInfo.State.CONNECTED)) {
                    WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                    WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                    Log.d(TAG, "连接到网络：" + wifiInfo.getBSSID());
                }

            } else if (intent.getAction().equals(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION)) {
                int error = intent.getIntExtra(WifiManager.EXTRA_SUPPLICANT_ERROR,
                        0);
                switch (error) {

                    case WifiManager.ERROR_AUTHENTICATING:
                        Log.d(TAG, "密码认证错误Code为：" + error);
                        Toast.makeText(getApplicationContext(), "wifi密码认证错误！", Toast.LENGTH_SHORT).show();
                        break;

                    default:
                        break;
                }

            } else if (intent.getAction().equals(WifiManager.WIFI_STATE_CHANGED_ACTION)) {
                int wifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, 0);
                Log.e("H3c", "wifiState" + wifiState);
                switch (wifiState) {
                    case WifiManager.WIFI_STATE_ENABLING:
                        Log.d(TAG, "wifi正在启用");
                        break;
                    case WifiManager.WIFI_STATE_ENABLED:
                        Log.d(TAG, "Wi-Fi已启用。");
                        break;

                }
            }
        }

    }
}
