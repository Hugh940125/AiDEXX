package com.microtech.aidexx.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.microtech.aidexx.ble.MessageDispatcher
import com.microtech.aidexx.ble.device.TransmitterManager
import com.microtech.aidexx.ble.device.model.DeviceModel
import com.microtechmd.blecomm.constant.CgmOperation
import com.microtechmd.blecomm.entity.BleMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

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
        model?.messageCallBack = {

        }
    }

    private fun onMessage(model: DeviceModel, message: BleMessage) {
        when (message.operation) {
            CgmOperation.DISCOVER -> {
                if (message.isSuccess) {
                    model.handleAdvertisement(message.data)
                }
            }
            CgmOperation.GET_DATETIME -> {
                model.disconnect()
            }

            CgmOperation.CALIBRATION -> {
//                serviceMainScope.launch {
//                    withContext(Dispatchers.Main) {
//                        if (message.isSuccess && message.data != null
//                            && message.data.isNotEmpty() && message.data[0].toInt() == 1
//                        ) {
//                            //发送到MainActivity,弹出对话框
//                            LiveEventBus.get<Boolean>(EventKey.EVENT_CALIBRATION)
//                                .post(true)
//                        } else {
//                            LiveEventBus.get<Calibration>(EventKey.EVENT_CALIBRATION)
//                                .post(Calibration(false))
//                        }
//                    }
//                }
            }

            CgmOperation.CONFIG_INFO -> {
//                val entity = CgmParser.getDeviceConfig<CgmConfEntity>(message.data)
//                LogUtils.data("Main Server  :$entity")
//                val defaultModel = TransmitterManager.instance().getDefault()
//                defaultModel?.saveDeviceMode(entity.expirationTime.toInt())
//                MMKVUtil.encodeBoolean(EventKey.not_register, true)
//                LiveEventBus.get<Boolean>(EventKey.RE_REGISTER_DEVICE).post(true)
            }

            CgmOperation.BOND -> {}
//                if (!message.isSuccess) {
//                LogUtils.eAiDex("BOND ERROR Controller ID :" + String(model.controller.id) + "numBondError $numBondError")
//                numBondError++
//                if (numBondError > 2) {
//                    numBondError = 0
//                    model.clearPair()
//                }
//            } else {
//                numBondError = 0
//                LogUtils.data("Controller ID :" + String(model.controller.id))
//            }
            CgmOperation.UNPAIR -> {
//                if (message.isSuccess) {
//                    model.deletePair()
//                }
            }
            CgmOperation.GET_HISTORIES -> {
//                if (UserInfoManager.instance().isLogin()) {
//                    if (CgmsApplication.isCgmPairing) {
//                        model.saveHistoriesSimple(message.data)
//                    } else {
//                        model.saveHistoriesAndContinueSync(message.data)
//                        updateNotification()
//                    }
//                }
            }
            CgmOperation.GET_HISTORIES_FULL -> {
//                if (UserInfoManager.instance().isLogin()) {
//                    model.saveFullHistoriesAndContinueSync(message.data)
//                }
            }
            else -> {
            }
        }
        serviceMainScope.launch {
            MessageDispatcher.instance().dispatch(message)
        }
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