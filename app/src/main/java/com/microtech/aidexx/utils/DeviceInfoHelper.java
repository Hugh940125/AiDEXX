package com.microtech.aidexx.utils;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.text.TextUtils;

import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.Locale;

public class DeviceInfoHelper {

    private static String getMacByJavaAPI() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface netInterface = interfaces.nextElement();
                if ("wlan0".equals(netInterface.getName()) || "eth0".equals(netInterface.getName())) {
                    byte[] addr = netInterface.getHardwareAddress();
                    if (addr == null || addr.length == 0) {
                        return null;
                    }
                    StringBuilder buf = new StringBuilder();
                    for (byte b : addr) {
                        buf.append(String.format("%02X:", b));
                    }
                    if (buf.length() > 0) {
                        buf.deleteCharAt(buf.length() - 1);
                    }
                    return buf.toString().toLowerCase(Locale.getDefault());
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String osVersion() {
        return Build.VERSION.RELEASE;
    }

    public static String deviceName() {
        return Build.BRAND +" "+ Build.MODEL;
    }

    public static String installVersion(Context context) {
        return getPackageInfo(context).versionName;
    }

    public static PackageInfo getPackageInfo(Context context) {
        PackageInfo info = null;
        try {
            info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        if (info == null)
            info = new PackageInfo();
        return info;
    }

    public static String installVersionCode(Context context) {
        return String.valueOf(getPackageInfo(context).versionCode);
    }

    @SuppressLint("HardwareIds")
    public static String SerialNumber() {
        return Build.SERIAL;
    }
}
