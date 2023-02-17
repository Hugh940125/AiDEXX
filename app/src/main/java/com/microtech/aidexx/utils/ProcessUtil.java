package com.microtech.aidexx.utils;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.webkit.WebView;

import java.util.List;

/**
 * @Description:
 * @Author: Hugh
 * @CreateDate: 2022/6/10 16:16
 */
public class ProcessUtil {
    // 包名判断是否为主进程
    public static boolean isMainProcess(Context context) {
        if (context == null) {
            return false;
        }
        return context.getPackageName().equals(getCurrentProcessName(context));
    }

    // 获取当前进程名
    public static String getCurrentProcessName(Context context) {
        if (context == null) return "";
        int pid = android.os.Process.myPid();
        String processName = "";
        ActivityManager manager = (ActivityManager) context.getSystemService(
                Context.ACTIVITY_SERVICE
        );
        for (ActivityManager.RunningAppProcessInfo process : manager.getRunningAppProcesses()) {
            if (process.pid == pid) {
                processName = process.processName;
            }
        }
        return processName;
    }

    // 异常出现情景:因为Android P行为变更，不可多进程使用同一个目录webView，需要为不同进程webView设置不同目录
    // 解决方式：重写项目Application，然后为其它进程webView设置目录
    public static void setWebDataDirectorySuffix(Application application) {
        if (application == null) {
            return;
        }
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                String processName = getProcessName(application);
                if (!application.getPackageName().equals(processName)) { // 判断不等于默认进程名称
                    //  为webView设置目录后缀
                    WebView.setDataDirectorySuffix(processName);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String getProcessName(Context context) {
        if (context == null) {
            return null;
        }
        ActivityManager manager = (ActivityManager) context.getSystemService(
                Context.ACTIVITY_SERVICE
        );
        List<ActivityManager.RunningAppProcessInfo> processInfoList = manager.getRunningAppProcesses();
        if (processInfoList != null && !processInfoList.isEmpty()) {
            for (ActivityManager.RunningAppProcessInfo processInfo : processInfoList) {
                if (processInfo.pid == android.os.Process.myPid()) {
                    return processInfo.processName;
                }
            }
        }
        return null;
    }

    public static boolean isInstalled(Context context, String pkg) {
        //默认不存在
        boolean exit = false;
        try {
            //不为空则存在
            exit = context.getPackageManager().getPackageInfo(pkg, PackageManager.GET_ACTIVITIES) != null;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return exit;
    }
}
