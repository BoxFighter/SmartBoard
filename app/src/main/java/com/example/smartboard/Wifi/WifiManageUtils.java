package com.example.smartboard.Wifi;

import android.content.Context;
import android.net.DhcpInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

/**
 * Created by 刘宸睿 on 2016/8/14.
 */
public class WifiManageUtils {
    private static WifiManager wifiManager;
    private static WifiInfo wifiInfo;
    private static List<WifiConfiguration> wifiConfigurationlist;
    private static DhcpInfo wifiDhcpInfo;

    public WifiManageUtils(Context context) {
        // 取得WifiManager对象
        wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
    }

    public WifiInfo getWifiConnectInfo() {
        wifiInfo = wifiManager.getConnectionInfo();
        return wifiInfo;
    }

    public List<WifiConfiguration> getConfiguration() {
        wifiConfigurationlist = wifiManager.getConfiguredNetworks();
        return wifiConfigurationlist;
    }

    public DhcpInfo getDhcpInfo() {
        wifiDhcpInfo = wifiManager.getDhcpInfo();
        return wifiDhcpInfo;
    }

    /**
     * 开启热点作为服务端的配置
     *
     * @param ssid
     * @param passwd
     * @param type
     * @return
     */
    public WifiConfiguration getCustomeWifiConfiguration(String ssid, String passwd, int type) {
        WifiConfiguration config = new WifiConfiguration();
        config.allowedAuthAlgorithms.clear();
        config.allowedGroupCiphers.clear();
        config.allowedKeyManagement.clear();
        config.allowedPairwiseCiphers.clear();
        config.allowedProtocols.clear();
        config.SSID = ssid;
        if (type == 1) // NOPASS
        {
            config.wepKeys[0] = "";
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            config.wepTxKeyIndex = 0;
        }
        if (type == 2) // WEP
        {
            config.hiddenSSID = true;
            config.wepKeys[0] = passwd;
            config.allowedAuthAlgorithms
                    .set(WifiConfiguration.AuthAlgorithm.SHARED);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
            config.allowedGroupCiphers
                    .set(WifiConfiguration.GroupCipher.WEP104);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            config.wepTxKeyIndex = 0;
        }
        if (type == 3) // WPA
        {
            config.preSharedKey = passwd;
            config.hiddenSSID = true;
            config.allowedAuthAlgorithms
                    .set(WifiConfiguration.AuthAlgorithm.OPEN);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            config.allowedPairwiseCiphers
                    .set(WifiConfiguration.PairwiseCipher.TKIP);
            // config.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedPairwiseCiphers
                    .set(WifiConfiguration.PairwiseCipher.CCMP);
            config.status = WifiConfiguration.Status.ENABLED;
        }
        if (type == 4) // WPA2psk test
        {
            config.preSharedKey = passwd;
            config.hiddenSSID = true;

            config.status = WifiConfiguration.Status.ENABLED;
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            config.allowedPairwiseCiphers
                    .set(WifiConfiguration.PairwiseCipher.TKIP);
            config.allowedPairwiseCiphers
                    .set(WifiConfiguration.PairwiseCipher.CCMP);
            config.allowedProtocols.set(WifiConfiguration.Protocol.RSN);

        }
        return config;

    }

    /**
     * 客户端添加配置，作为连接认证配置
     * ssid、passwd 配置前后必须加上双引号“
     * @param ssid
     * @param passwd
     * @param type
     * @return
     */
    public WifiConfiguration getCustomeWifiClientConfiguration(String ssid, String passwd, int type) {
        WifiConfiguration config = new WifiConfiguration();
        config.allowedAuthAlgorithms.clear();
        config.allowedGroupCiphers.clear();
        config.allowedKeyManagement.clear();
        config.allowedPairwiseCiphers.clear();
        config.allowedProtocols.clear();
        //双引号必须
        config.SSID = "\"" + ssid + "\"";
        if (type == 1) // WIFICIPHER_NOPASS
        {
            config.wepKeys[0] = "";
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            config.wepTxKeyIndex = 0;
        }
        if (type == 2) // WIFICIPHER_WEP
        {
            config.hiddenSSID = true;
            config.wepKeys[0] = "\"" + passwd + "\"";
            config.allowedAuthAlgorithms
                    .set(WifiConfiguration.AuthAlgorithm.SHARED);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
            config.allowedGroupCiphers
                    .set(WifiConfiguration.GroupCipher.WEP104);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            config.wepTxKeyIndex = 0;
        }
        if (type == 3) // WIFICIPHER_WPA
        {
            config.preSharedKey = "\"" + passwd + "\"";
            config.hiddenSSID = true;
            config.allowedAuthAlgorithms
                    .set(WifiConfiguration.AuthAlgorithm.OPEN);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            config.allowedPairwiseCiphers
                    .set(WifiConfiguration.PairwiseCipher.TKIP);
            // config.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedPairwiseCiphers
                    .set(WifiConfiguration.PairwiseCipher.CCMP);
            config.status = WifiConfiguration.Status.ENABLED;
        }
        if (type == 4) // WPA2psk test
        {
            config.preSharedKey = "\"" + passwd + "\"";
            config.hiddenSSID = true;

            config.status = WifiConfiguration.Status.ENABLED;
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            config.allowedPairwiseCiphers
                    .set(WifiConfiguration.PairwiseCipher.TKIP);
            config.allowedPairwiseCiphers
                    .set(WifiConfiguration.PairwiseCipher.CCMP);
            config.allowedProtocols.set(WifiConfiguration.Protocol.RSN);

        }
        return config;

    }

    public Boolean stratWifiAp(String ssid, String psd, int type) {
        Method method1 = null;
        try
        {
            method1 = wifiManager.getClass().getMethod("setWifiApEnabled",
                    WifiConfiguration.class, boolean.class);
            WifiConfiguration netConfig = getCustomeWifiConfiguration(ssid,
                    psd, type);

            method1.invoke(wifiManager, netConfig, true);
            return true;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            return false;
        }
    }

    public void closeWifiAp() {
        if (isWifiApEnabled())
        {
            try
            {
                Method method = wifiManager.getClass().getMethod(
                        "getWifiApConfiguration");
                method.setAccessible(true);

                WifiConfiguration config = (WifiConfiguration) method
                        .invoke(wifiManager);

                Method method2 = wifiManager.getClass().getMethod(
                        "setWifiApEnabled", WifiConfiguration.class,
                        boolean.class);
                method2.invoke(wifiManager, config, false);
            }
            catch (NoSuchMethodException e)
            {
                e.printStackTrace();
            }
            catch (IllegalArgumentException e)
            {
                e.printStackTrace();
            }
            catch (IllegalAccessException e)
            {
                e.printStackTrace();
            }
            catch (InvocationTargetException e)
            {
                e.printStackTrace();
            }
        }
    }

    public boolean isWifiApEnabled() {
        try
        {
            Method method = wifiManager.getClass().getMethod("isWifiApEnabled");
            method.setAccessible(true);
            return (Boolean) method.invoke(wifiManager);

        }
        catch (NoSuchMethodException e)
        {
            e.printStackTrace();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return false;
    }
}
