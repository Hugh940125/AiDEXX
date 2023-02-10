package com.microtech.aidexx

import android.app.Application
import com.tencent.mmkv.MMKV

class AidexxApp : Application() {

    override fun onCreate() {
        super.onCreate()
        initSdks()
    }

    private fun initSdks() {
        MMKV.initialize(this, this.filesDir.absolutePath + "/mmkv")
    }
}