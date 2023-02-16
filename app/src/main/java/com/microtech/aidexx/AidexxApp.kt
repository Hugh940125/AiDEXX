package com.microtech.aidexx

import android.app.Application
import com.microtech.aidexx.db.ObjectBox
import com.microtech.aidexx.utils.CrashHandler
import com.tencent.mmkv.MMKV

class AidexxApp : Application() {

    companion object {
        lateinit var instance: AidexxApp
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        //全局捕捉错误
        CrashHandler.instance?.init(this)
        initSdks()
    }

    private fun initSdks() {
        MMKV.initialize(this)
        ObjectBox.init(this)
    }
}