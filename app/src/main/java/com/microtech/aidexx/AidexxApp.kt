package com.microtech.aidexx

import android.app.Activity
import android.app.Application
import android.os.Bundle
import com.microtech.aidexx.ble.AidexBleAdapter
import com.microtech.aidexx.db.ObjectBox
import com.microtech.aidexx.ui.setting.alert.AlertUtil
import com.microtech.aidexx.utils.CrashHandler
import com.microtech.aidexx.utils.LogUtil
import com.microtech.aidexx.utils.ProcessUtil
import com.microtech.aidexx.widget.dialog.lib.DialogX
import com.microtechmd.blecomm.controller.BleController
import com.tencent.mmkv.MMKV
import io.objectbox.android.Admin
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import java.util.concurrent.atomic.AtomicInteger

class AidexxApp : Application() {
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
        AlertUtil.init(this)
        MMKV.initialize(this)
        ObjectBox.init(this)
        DialogX.init(this)
        AidexBleAdapter.init(this)
        BleController.setBleAdapter(AidexBleAdapter.getInstance())
        AidexBleAdapter.getInstance().setDiscoverCallback()
    }

    fun isForeground(): Boolean {
        if (activityAliveCount.get() != 0) {
            LogUtil.eAiDEX("前台")
            return true
        }
        LogUtil.eAiDEX("后台")
        return false
    }

    private val activityLifecycleCallbacks: ActivityLifecycleCallbacks =
        object : ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {}
            override fun onActivityStarted(activity: Activity) {
                activityAliveCount.incrementAndGet()
            }

            override fun onActivityResumed(activity: Activity) {}
            override fun onActivityPaused(activity: Activity) {}
            override fun onActivityStopped(activity: Activity) {
                activityAliveCount.decrementAndGet()
            }

            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
            override fun onActivityDestroyed(activity: Activity) {}
        }
}