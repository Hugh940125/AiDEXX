package com.microtech.aidexx.utils

import android.content.Context
import android.os.Looper
import android.os.Process
import kotlin.system.exitProcess


/**
 *@date 2023/2/15
 *@author Hugh
 *@desc
 */
class CrashHandler : Thread.UncaughtExceptionHandler {
    private var mDefaultHandler: Thread.UncaughtExceptionHandler? = null
    @Volatile
    private var isKilled = false

    fun init() {
        // 获取系统默认的UncaughtException处理类
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler()
        // 设置 CrashHandler为系统默认的处理器
        Thread.setDefaultUncaughtExceptionHandler(this)
    }

    override fun uncaughtException(thread: Thread, ex: Throwable) {
        if (!handleException(ex) && mDefaultHandler != null) {
            // 如果用户没有处理让系统默认的来处理
            mDefaultHandler?.uncaughtException(thread, ex)
        } else {
            isKilled = true
            try {
                Thread.sleep(2000)
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
            // 退出程序
            Process.killProcess(Process.myPid())
            //非正常退出
            exitProcess(1)
        }
    }

    private fun handleException(ex: Throwable?): Boolean {
        if (ex == null || isKilled) {
            return false
        }
        LogUtil.eAiDEX(ex.printStackTrace().toString())
        // Toast来显示提示信息
        object : Thread() {
            override fun run() {
                Looper.prepare()
                ToastUtil.showShort("程序出现异常，即将退出！")
                Looper.loop()
            }
        }.start()
        return true
    }

    companion object {
        var instance: CrashHandler? = null
            get() {
                if (field == null) {
                    field = CrashHandler()
                }
                return field
            }
    }
}
