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
    lateinit var transmitterManager: TransmitterManager

    override fun onCreate() {
        super.onCreate()
        serviceMainScope = MainScope()
        transmitterManager = TransmitterManager.instance()
        MessageDispatcher.instance().observeLifecycle(serviceMainScope)
    }

    private fun onMessage(model: DeviceModel, message: BleMessage) {
        val data = message.data
        when (message.operation) {
            CgmOperation.DISCOVER -> {
                if (message.isSuccess) {
                    model.handleAdvertisement(message.data)
                }
            }
            AidexXOperation.GET_START_TIME -> {
                if (!AidexxApp.isPairing) {
                    val sensorStartTime = ByteUtils.toDate(data)
                    model.updateStartTime(sensorStartTime)
                }
            }
            CgmOperation.GET_DATETIME -> {
                model.disconnect()
            }

            CgmOperation.CALIBRATION -> {

            }

            CgmOperation.CONFIG_INFO -> {

            }

            CgmOperation.BOND -> {}

            CgmOperation.UNPAIR -> {

            }
            CgmOperation.GET_HISTORIES -> {
                if (UserInfoManager.instance().isLogin()) {
                    model.saveBriefHistoryFromConnect(message.data)
                }
            }
            CgmOperation.GET_HISTORIES_FULL -> {
                if (UserInfoManager.instance().isLogin()) {
                    model.saveRawHistoryFromConnect(message.data)
                }
            }
            else -> {
            }
        }
        MessageDispatcher.instance().dispatch(serviceMainScope, message)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        TransmitterManager.onModelChange = {
            it.let {
                it.messageCallBack = { msg ->
                    onMessage(it, msg)
                }
            }
        }
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceMainScope.cancel()
    }
}