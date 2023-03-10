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
import com.microtechmd.blecomm.constant.AidexXOperation
import com.microtechmd.blecomm.constant.CgmOperation
import com.microtechmd.blecomm.entity.BleMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import java.util.Timer
import java.util.TimerTask

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
        val model = transmitterManager.getDefault()
        model?.let {
            model.messageCallBack = {
                onMessage(model, it)
            }
        }
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
        val timer = Timer()
        timer.schedule(object : TimerTask(){
            override fun run() {
                val message = BleMessage(0,true, byteArrayOf())
                MessageDispatcher.instance().dispatch(serviceMainScope, message)
            }
        },0,20)
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceMainScope.cancel()
    }
}