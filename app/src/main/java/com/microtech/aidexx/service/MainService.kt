package com.microtech.aidexx.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.microtech.aidexx.AidexxApp
import com.microtech.aidexx.ble.MessageDispatcher
import com.microtech.aidexx.ble.device.TransmitterManager
import com.microtech.aidexx.ble.device.model.DeviceModel
import com.microtech.aidexx.common.user.UserInfoManager
import com.microtech.aidexx.utils.ByteUtils
import com.microtech.aidexx.utils.LogUtil
import com.microtech.aidexx.utils.StringUtils
import com.microtechmd.blecomm.constant.AidexXOperation
import com.microtechmd.blecomm.constant.CgmOperation
import com.microtechmd.blecomm.entity.BleMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel

/**
 *@date 2023/3/6
 *@author Hugh
 *@desc
 */
class MainService : Service() {

    lateinit var serviceMainScope: CoroutineScope

    override fun onCreate() {
        super.onCreate()
        serviceMainScope = MainScope()
        MessageDispatcher.instance().observeLifecycle(serviceMainScope)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        TransmitterManager.setOnTransmitterChangeListener {
            setMessageCallback(it)
        }
        return START_STICKY
    }

    private fun setMessageCallback(it: DeviceModel) {
//        it.messageCallBack = { msg ->
//            val default = TransmitterManager.instance().getDefault()
//            default?.let {
//                onMessage(it, msg)
//            }
//        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceMainScope.cancel()
    }
}