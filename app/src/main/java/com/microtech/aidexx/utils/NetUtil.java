package com.microtech.aidexx.utils;


import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;

public class NetUtil {

    // 判断网络是否可用
    public static boolean isNetAvailable(Context context) {
        // ConnectivityManager:网络连接管理器.
        // ①.Monitor network connections (Wi-Fi, GPRS, UMTS, etc.)
        // 监视网络的连接状态;
        // ②.Send broadcast intents when network connectivity changes.
        // 当网络连接状态发生改变后发送一个广播意图;
        // ③.Attempt to "fail over" to another network when connectivity to a
        // network is lost.
        // 当一个网络的连接丢失后,尝试着"故障转移"到另一个网络.
        // ④.Provide an API that allows applications to query the coarse-grained
        // or fine-grained state of the available networks.
        // 提供了一套API,供开发者用来查询网络的好与坏的状态;
        // ⑤.Provide an API that allows applications to request and select
        // networks for their data traffic.
        // 提供了一套API,供开发者在网络中进行数据的传递.
        if(context == null){
            return false;
        }
        ConnectivityManager manager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        // 获取当前激活的网络信息
        NetworkInfo info = manager.getActiveNetworkInfo();
        if (info != null) {
            info.isAvailable();
            return true;
        }

        return false;

    }

    // 判断网络是否已连接
    public static boolean isNetworkConnected(Context context) {

        if(context ==null){

            return false;
        }
        ConnectivityManager manager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = manager.getActiveNetworkInfo();
        if (info != null && info.isAvailable()) {
            return info.isConnected();
        }
        return false;
    }

    // 判断有没有连接WiFi网络
    public static boolean isWifiConnected(Context context) {
        ConnectivityManager manager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo networkInfo = manager
                .getNetworkInfo(ConnectivityManager.TYPE_WIFI);

        if (networkInfo != null && networkInfo.isConnected()) {
            return true;
        } else {
            return false;
        }
    }

    // 判断有没有连接Mobile网络
    public static boolean isMobileConnected(Context context) {
        ConnectivityManager manager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo networkInfo = manager
                .getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

        if (networkInfo != null && networkInfo.isConnected()) {
            return true;
        } else {
            return false;
        }
    }

    // 判断当前连接的网络类型
    public static String getNetworkType(Context context) {
        ConnectivityManager manager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = manager.getActiveNetworkInfo();
        if (info != null) {
            // 返回各种网络类型
            // int type = info.getType();
            // 返回一个人类可读的网络类型名称
            return info.getTypeName();
        }
        return null;
    }
    /**
     * 获取当前的网络状态 ：没有网络-0：WIFI网络1：4G网络-2：3G网络-3：2G网络-4
     * 自定义
     *
     * @param context
     * @return
     */
    public static int getAPNType(Context context) {
        //结果返回值
        int netType = 0;
        //获取手机所有连接管理对象
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        //获取NetworkInfo对象
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        //NetworkInfo对象为空 则代表没有网络
        if (networkInfo == null) {
            return netType;
        }
        //否则 NetworkInfo对象不为空 则获取该networkInfo的类型
        int nType = networkInfo.getType();
        if (nType == ConnectivityManager.TYPE_WIFI) {
            //WIFI
            netType = 1;
        } else if (nType == ConnectivityManager.TYPE_MOBILE) {
            int nSubType = networkInfo.getSubtype();
            TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            //4G
            if (nSubType == TelephonyManager.NETWORK_TYPE_LTE
                    && !telephonyManager.isNetworkRoaming()) {
                netType = 2;
                //3G   联通的3G为UMTS或HSDPA 电信的3G为EVDO
            } else if (nSubType == TelephonyManager.NETWORK_TYPE_UMTS
                    || nSubType == TelephonyManager.NETWORK_TYPE_HSDPA
                    || nSubType == TelephonyManager.NETWORK_TYPE_EVDO_0
                    && !telephonyManager.isNetworkRoaming()) {
                netType = 3;
                //2G 移动和联通的2G为GPRS或EGDE，电信的2G为CDMA
            } else if (nSubType == TelephonyManager.NETWORK_TYPE_GPRS
                    || nSubType == TelephonyManager.NETWORK_TYPE_EDGE
                    || nSubType == TelephonyManager.NETWORK_TYPE_CDMA
                    && !telephonyManager.isNetworkRoaming()) {
                netType = 4;
            } else {
                netType = 4;
            }
        }
        return netType;
    }

}

