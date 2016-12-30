package com.letv.wallet.common.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.telephony.TelephonyManager;

import com.letv.wallet.common.BaseApplication;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Set;

/**
 * Created by linquan on 16-1-6.
 */
public class NetworkHelper {
    public final static String TAG = "NetworkHelper";

    private static NetworkHelper instance;
    private Hashtable<Integer, NetworkAvailableCallBack> callBackHashtable = new Hashtable<Integer, NetworkAvailableCallBack>();
    private final Object callBackHashLock = new Object();

    public NetworkHelper() {
    }


    public interface NetworkAvailableCallBack {
        void onNetworkAvailable();
    }

    public static NetworkHelper getInstance() {
        if (instance == null) {
            synchronized (NetworkHelper.class) {
                if (instance == null) {
                    instance = new NetworkHelper();
                }
            }
        }
        return instance;
    }

    public boolean containsCallBack() {
        return !callBackHashtable.isEmpty();
    }

    public void excuteCallBack() {
        ArrayList<Integer> keyArrayList = new ArrayList<Integer>();
        synchronized (callBackHashLock){
            Set<Integer> keySet = callBackHashtable.keySet();
            NetworkAvailableCallBack callBack;
            for (int hashCode : keySet) {
                callBack = callBackHashtable.get(hashCode);
                callBack.onNetworkAvailable();
                keyArrayList.add(hashCode);
            }
        }
        for (int key : keyArrayList) {
            callBackHashtable.remove(key);
        }
    }

    public void addCallBack(int hashCode, NetworkAvailableCallBack availableCallBack) {
        if (availableCallBack != null) {
            synchronized (callBackHashLock){
                callBackHashtable.put(hashCode, availableCallBack);
            }
        }
    }

    /**
     * 获取uid 对应的应用是否允许链接移动数据网络
     *
     * @param uid
     */
    public static boolean isEnableMobileNetwork(int uid) {
        try {
            Class clazz = Class.forName("android.net.NetworkPolicyManager");
            Method method = clazz.getMethod("from", Context.class);
            Object obj = method.invoke(null, BaseApplication.getApplication());
            method = clazz.getMethod("getUidPolicy", int.class);
            int uidPolicy = (int) method.invoke(obj, uid);
            Field field = clazz.getField("POLICY_REJECT_METERED_BACKGROUND");
            field.getInt(obj);
            return !((uidPolicy & field.getInt(obj)) != 0);
        } catch (Exception e) {
            LogHelper.e(e.toString());
        }
        return false;
    }

    /**
     * 获取 uid 对应的应用是否允许链接WIFI
     *
     * @param uid
     */
    public static boolean isEnableWifi(int uid) {
        try {
            Class clazz = Class.forName("android.net.NetworkPolicyManager");
            Method method = clazz.getMethod("from", Context.class);
            Object obj = method.invoke(null, BaseApplication.getApplication());
            method = clazz.getMethod("getFirewallUidChainRule", int.class);
            return !((Boolean) method.invoke(obj, uid));
        } catch (Exception e) {
            LogHelper.e(e.toString());
        }
        return false;
    }

    public static boolean isDataNetworkAvailable() {
        TelephonyManager telephonyManager = (TelephonyManager) BaseApplication.getApplication().getSystemService(Context.TELEPHONY_SERVICE);
        int state = telephonyManager.getDataState();
        if (state == TelephonyManager.DATA_CONNECTED) {
            return true;
        }
        return false;
    }

    public static boolean isNetworkAvailable() {
        LogHelper.w("[%s] isNetworkAvailable called ", TAG);

        final ConnectivityManager manager = (ConnectivityManager) BaseApplication.getApplication().getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo info = manager.getActiveNetworkInfo();
        if (info != null && info.isAvailable() && info.isConnected()) {
            LogHelper.w("[%s] isNetworkAvailable true ", TAG);
            return true;
        }
        LogHelper.w("[%s] isNetworkAvailable false ", TAG);
        return false;
    }

    public static boolean isWifiAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) BaseApplication.getApplication().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = connectivityManager.getActiveNetworkInfo();
        if (info != null && info.isAvailable()
                && info.isConnected() && info.getType() == ConnectivityManager.TYPE_WIFI) {
            return true;
        }
        return false;
    }

    /**
     * if wifi is connect return wifi ip address,otherwise return phone loacal ip address
     *
     * @return
     */
    public static String getIpAddress() {
        if (isWifiAvailable()) {
            return getWlanIp();
        } else {
            return getLocalIpAddress();
        }
    }

    private static String getWlanIp() {
        //获取wifi服务
        WifiManager wifiManager = (WifiManager) BaseApplication.getApplication().getSystemService(Context.WIFI_SERVICE);
        //判断wifi是否开启
        if (!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
        }
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int ipAddress = wifiInfo.getIpAddress();
        return (ipAddress & 0xFF) + "." +
                ((ipAddress >> 8) & 0xFF) + "." +
                ((ipAddress >> 16) & 0xFF) + "." +
                (ipAddress >> 24 & 0xFF);
    }

    private static String getLocalIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress() && !inetAddress.isLinkLocalAddress()) {

                        return inetAddress.getHostAddress().toString();
                    }
                }
            }
        } catch (SocketException ex) {
            LogHelper.e(ex);
        }
        return null;
    }
}
