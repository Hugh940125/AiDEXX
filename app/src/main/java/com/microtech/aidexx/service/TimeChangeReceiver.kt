package com.microtech.aidexx.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.microtech.aidexx.ble.AidexBleAdapter
import com.microtech.aidexx.utils.LogUtil
import com.microtech.aidexx.utils.Throttle

private const val ON_TIME_CHANGE = 2

class TimeChangeReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        Throttle.instance().emit(3000, ON_TIME_CHANGE) {
            LogUtil.eAiDEX("System time modified, restart ble scan")
            AidexBleAdapter.getInstance().executeStopScan()
        }
    }
}