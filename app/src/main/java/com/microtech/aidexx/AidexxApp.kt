package com.microtech.aidexx

import android.app.Activity
import android.app.Application
import android.hardware.display.DisplayManager
import android.os.Bundle
import android.view.Display
import com.microtech.aidexx.ble.AidexBleAdapter
import com.microtech.aidexx.db.ObjectBox
import com.microtech.aidexx.ui.setting.alert.AlertUtil
import com.microtech.aidexx.utils.ContextUtil
import com.microtech.aidexx.utils.CrashHandler
import com.microtech.aidexx.utils.ProcessUtil
import com.microtech.aidexx.widget.dialog.lib.DialogX
import com.microtechmd.blecomm.controller.BleController
import com.tencent.mars.xlog.Log
import com.tencent.mars.xlog.Xlog
import com.tencent.mmkv.MMKV
import io.objectbox.android.Admin
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import java.util.concurrent.atomic.AtomicInteger

class AidexxApp : Application() {
    var activityStack = mutableListOf<Activity>()
    private var activityAliveCount: AtomicInteger = AtomicInteger(0)

    companion object {
        var isPairing: Boolean = false
        lateinit var instance: AidexxApp
        lateinit var mainScope: CoroutineScope
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        mainScope = MainScope()
        //全局捕捉错误
        CrashHandler.instance?.init()
        initSdks()
        if (ProcessUtil.isMainProcess(this)) {
            if (BuildConfig.DEBUG) {
                Admin(ObjectBox.store).start(this)
            }
        }
        registerActivityLifecycleCallbacks(activityLifecycleCallbacks)
    }

    private fun initSdks() {
        initXlog()
        AlertUtil.init(this)
        ContextUtil.init(this)
        MMKV.initialize(this)
        ObjectBox.init(this)
        DialogX.init(this)
        AidexBleAdapter.init(this)
        BleController.setBleAdapter(AidexBleAdapter.getInstance())
        AidexBleAdapter.getInstance().setDiscoverCallback()
    }

    private fun initXlog() {
        val cacheDays = 15
        val namePrefix = "AiDEX"
        System.loadLibrary("c++_shared")
        System.loadLibrary("marsxlog")
        val root = externalCacheDir?.absolutePath
        val logPath = "$root/aidex/log"
        val cachePath = "${this.filesDir}/xlog"
        val logConfig = Xlog.XLogConfig()
        logConfig.mode = Xlog.AppednerModeAsync
        logConfig.logdir = logPath
        logConfig.nameprefix = namePrefix
        logConfig.pubkey = ""
        logConfig.compressmode = Xlog.ZLIB_MODE
        logConfig.compresslevel = 0
        logConfig.cachedir = cachePath
        logConfig.cachedays = cacheDays
        val xlog = Xlog()
        Log.setLogImp(xlog)
        if (ProcessUtil.isMainProcess(this)) {
            if (BuildConfig.DEBUG) {
                Log.setConsoleLogOpen(true)
                Log.appenderOpen(
                    Xlog.LEVEL_DEBUG,
                    Xlog.AppednerModeAsync,
                    "",
                    logPath,
                    namePrefix,
                    0
                )
            } else {
                Log.setConsoleLogOpen(false)
                Log.appenderOpen(
                    Xlog.LEVEL_DEBUG,
                    Xlog.AppednerModeAsync,
                    "",
                    logPath,
                    namePrefix,
                    cacheDays
                )
            }
        }
    }

    fun isDisplayOn(): Boolean {
        val displayManager = this.getSystemService(DISPLAY_SERVICE) as DisplayManager
        for (display in displayManager.displays) {
            if (display.state != Display.STATE_OFF) {
                return true
            }
        }
        return false
    }

    fun isForeground(): Boolean {
        if (activityAliveCount.get() != 0) {
            return true
        }
        return false
    }

    private val activityLifecycleCallbacks: ActivityLifecycleCallbacks =
        object : ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                activityStack.add(activity)
            }

            override fun onActivityStarted(activity: Activity) {
                activityAliveCount.incrementAndGet()
            }

            override fun onActivityResumed(activity: Activity) {}
            override fun onActivityPaused(activity: Activity) {}
            override fun onActivityStopped(activity: Activity) {
                activityAliveCount.decrementAndGet()
            }

            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
            override fun onActivityDestroyed(activity: Activity) {
                activityStack.remove(activity)
            }
        }
}