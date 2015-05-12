package com.ty.hcl.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import java.util.Iterator;
import java.util.List;

/**
 * Created by huangchuanliang on 2015/3/11.
 * wifi工具类
 * 封装了Wifi的基础操作方法
 */
public class WifiUtil {
    private static final String TAG = "WifiUtil";
    private static WifiUtil wifiUtil;
    private WifiManager wifiManager;
    private WifiConfiguration wifiConfiguration;
    private WifiInfo wifiInfo;
    private NetworkInfo networkInfo;
    private DhcpInfo dhcpInfo;

    private WifiUtil(Context paramContext){
        wifiManager = (WifiManager)paramContext.getSystemService(Context.WIFI_SERVICE);
        wifiInfo = wifiManager.getConnectionInfo();//返回当前WiFi连接的动态信息
        networkInfo = ((ConnectivityManager)paramContext
                .getSystemService(Context.CONNECTIVITY_SERVICE))
                .getNetworkInfo(ConnectivityManager.TYPE_WIFI);
       dhcpInfo = wifiManager.getDhcpInfo();
    }

    public void setNewWifiManagerInfo(){
        wifiInfo = wifiManager.getConnectionInfo();
        dhcpInfo = wifiManager.getDhcpInfo();
    }

    //单例模式
    public static WifiUtil getInstance(Context paramContext){
        if (wifiUtil != null){
            wifiUtil = new WifiUtil(paramContext);
        }
        return wifiUtil;
    }

    private WifiConfiguration isExists(String paramString){
        Iterator<WifiConfiguration> wcfIterator=wifiManager.getConfiguredNetworks().iterator();
        WifiConfiguration localWifiConfig;
        do {
            if (!wcfIterator.hasNext())
                return null;
            localWifiConfig = wcfIterator.next();
        }while(!localWifiConfig.SSID.equals("\""+paramString+"\""));
        return localWifiConfig;
    }



}
