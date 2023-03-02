package com.microtech.aidexx.utils

import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Parcelable
import android.text.TextUtils
import java.io.Serializable

object ActivityUtil {
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