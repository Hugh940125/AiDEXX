package com.microtech.aidexx

import android.app.Application
import com.microtech.aidexx.ble.AidexBleAdapter
import com.microtech.aidexx.db.ObjectBox
import com.microtech.aidexx.utils.CrashHandler
import com.microtech.aidexx.utils.ProcessUtil
import com.microtech.aidexx.widget.dialog.x.DialogX
import com.microtechmd.blecomm.controller.BleController
import com.tencent.mmkv.MMKV
import io.objectbox.android.Admin
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope

class AidexxApp : Application() {
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

    }

    private fun initSdks() {
        MMKV.initialize(this)
        ObjectBox.init(this)
        DialogX.init(this)
        AidexBleAdapter.init(this)
        BleController.setBleAdapter(AidexBleAdapter.getInstance())
    }
}