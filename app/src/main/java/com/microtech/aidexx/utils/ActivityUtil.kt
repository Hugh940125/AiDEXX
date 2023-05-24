package com.microtech.aidexx.utils

import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.os.Process
import android.text.TextUtils
import java.io.Serializable
import java.lang.reflect.Method

object ActivityUtil {

    private const val HARMONY_OS = "harmony"

    fun isHarmonyOS(): Boolean {
        try {
            val clz = Class.forName("com.huawei.system.BuildEx")
            val method: Method = clz.getMethod("getOsBrand")
            return HARMONY_OS == method.invoke(clz)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return false
    }

    fun isMIUI(): Boolean {
        val manufacturer = Build.MANUFACTURER
        if ("Xiaomi".equals(manufacturer, true)) {
            return true
        }
        return false
    }

    fun isServiceRunning(context: Context?, serviceClass: Class<*>): Boolean {
        if (context == null) {
            return false
        }
        val appContext = context.applicationContext
        val manager = appContext.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val infos = manager.getRunningServices(Int.MAX_VALUE)
        if (infos != null && !infos.isEmpty()) {
            for (service in infos) {
                // 添加Uid验证, 防止服务重名, 当前服务无法启动
                if (getUid(context) == service.uid) {
                    if (serviceClass.name == service.service.className) {
                        return true
                    }
                }
            }
        }
        return false
    }

    private fun getUid(context: Context?): Int {
        if (context == null) {
            return -1
        }
        val pid = Process.myPid()
        val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val infos = manager.runningAppProcesses
        if (infos != null && !infos.isEmpty()) {
            for (processInfo in infos) {
                if (processInfo.pid == pid) {
                    return processInfo.uid
                }
            }
        }
        return -1
    }

    /**
     * 判断某个界面是否在前台
     *
     * @param activity 要判断的Activity
     * @return 是否在前台显示
     */
    fun isForeground(activity: Activity): Boolean {
        return isForeground(activity, activity.javaClass.name)
    }

    /**
     * 判断某个界面是否在前台
     * @param context   Context
     * @param className 界面的类名
     * @return 是否在前台显示
     */
    fun isForeground(context: Context?, className: String): Boolean {
        if (context == null || TextUtils.isEmpty(className)) return false
        val am = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val list = am.getRunningTasks(1)
        if (list != null && list.size > 0) {
            val cpn = list[0].topActivity
            return className == cpn!!.className
        }
        return false
    }

    fun toActivity(context: Context, clazz: Class<*>, vararg data: Pair<String, Any?>) {
        val intent = Intent(context, clazz)
        data.forEach {
            when (it.second) {
                is Boolean -> {
                    intent.putExtra(it.first, it.second as Boolean)
                }
                is Byte -> {
                    intent.putExtra(it.first, it.second as Byte)
                }
                is Int -> {
                    intent.putExtra(it.first, it.second as Int)
                }
                is Short -> {
                    intent.putExtra(it.first, it.second as Short)
                }
                is Long -> {
                    intent.putExtra(it.first, it.second as Long)
                }
                is Float -> {
                    intent.putExtra(it.first, it.second as Float)
                }
                is Double -> {
                    intent.putExtra(it.first, it.second as Double)
                }
                is Char -> {
                    intent.putExtra(it.first, it.second as Char)
                }
                is String -> {
                    intent.putExtra(it.first, it.second as String)
                }
                is Serializable -> {
                    intent.putExtra(it.first, it.second as Serializable)
                }
                is Parcelable -> {
                    intent.putExtra(it.first, it.second as Parcelable)
                }
            }
        }
        context.startActivity(intent)
    }

    fun toActivity(context: Context, bundle: Bundle, cls: Class<*>) {
        val intent = Intent(context, cls)
        intent.putExtras(bundle)
        context.startActivity(intent)
    }

    fun toActivity(context: Context, cls: Class<*>) {
        val intent = Intent(context, cls)
        context.startActivity(intent)
    }

    fun toSystemHome(context: Context) {
        val home = Intent(Intent.ACTION_MAIN)
        home.addCategory(Intent.CATEGORY_HOME)
        context.startActivity(home)
    }
}