package com.microtech.aidexx.ui.pair

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.os.Message
import com.microtech.aidexx.R
import com.microtech.aidexx.ble.MessageDistributor
import com.microtech.aidexx.ble.MessageObserver
import com.microtech.aidexx.ble.device.TransmitterManager
import com.microtech.aidexx.common.date2ymdhm
import com.microtech.aidexx.utils.ByteUtils
import com.microtech.aidexx.utils.LogUtil
import com.microtech.aidexx.utils.eventbus.EventBusKey
import com.microtech.aidexx.utils.eventbus.EventBusManager
import com.microtech.aidexx.widget.dialog.Dialogs
import com.microtechmd.blecomm.constant.AidexXOperation
import com.microtechmd.blecomm.constant.CgmOperation
import com.microtechmd.blecomm.controller.BleControllerInfo
import com.microtechmd.blecomm.entity.BleMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

object PairUtil {
    enum class Operation { PAIR, UNPAIR }

    private var operation: Operation? = null

    private const
    val DISMISS_DIALOG = 1
    private const val TIMEOUT_MILLIS = 30 * 1000L

    val handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            Dialogs.dismissWait()
        }
    }

    private var isForceUnpair: Boolean = false

    fun fail() {
        handler.removeMessages(DISMISS_DIALOG)
        when (operation) {
            Operation.PAIR -> {
                EventBusManager.send(EventBusKey.EVENT_PAIR_RESULT, false)
            }
            else -> {}
        }
    }

    fun observeMessage(context: Context, scope: CoroutineScope) {
        MessageDistributor.instance().observerAndIntercept(object : MessageObserver {
            override fun onMessage(message: BleMessage) {
                val success = message.isSuccess
                val default = TransmitterManager.instance().getDefault()
                if (default != null) {
                    when (message.operation) {
                        AidexXOperation.DISCOVER -> {
                            if (!success) {
                                fail()
                                Dialogs.showError(context.getString(R.string.Search_Timeout))
                            }
                        }
                        AidexXOperation.CONNECT -> {
                            LogUtil.eAiDEX("Pair ----> connect:$success")
                            if (success) {
                                Dialogs.showWait(context.getString(R.string.Connecting))
                            } else {
                                fail()
                                Dialogs.showError(context.getString(R.string.Connecting_Failed))
                            }
                        }
                        CgmOperation.BOND -> {
                            LogUtil.eAiDEX("Pair ----> bond:$success")
                            if (!success) {
                                fail()
                                Dialogs.showError(context.getString(R.string.failure))
                            }
                        }
                        CgmOperation.PAIR -> {
                            LogUtil.eAiDEX("Pair ----> pair:$success")
                            if (!success) {
                                fail()
                                pairFailedTips(context)
                            }
                        }
                        AidexXOperation.DELETE_BOND -> {
                            LogUtil.eAiDEX("Pair ----> delete bond:$success")
                            if (!isForceUnpair) {
                                if (success) {
                                    scope.launch {
                                        default.deletePair()
                                    }
                                } else {
                                    Dialogs.showError(context.getString(R.string.Unpair_fail))
                                }
                            }
                        }
                        CgmOperation.UNPAIR -> {
                            LogUtil.eAiDEX("Pair ----> unpair:$success")
                            if (success) {
                                scope.launch {
                                    default.deletePair()
                                }
                            } else {
                                Dialogs.showError(context.getString(R.string.Unpair_fail))
                            }
                        }
                        AidexXOperation.GET_START_TIME -> {
                            val data = message.data
                            val sensorStartTime = ByteUtils.toDate(data)
                            LogUtil.eAiDEX("Pair ----> Start time :" + sensorStartTime.date2ymdhm())
                            default.entity.sensorStartTime = sensorStartTime
                            scope.launch {
                                default.savePair()
                            }
                        }
                        AidexXOperation.GET_DEVICE_INFO -> {}

                        AidexXOperation.DISCONNECT -> {
                            LogUtil.eAiDEX("Pair ----> disconnect:$success")
                            Dialogs.dismissWait()
                        }
                        else -> {}
                    }
                }
            }
        })
    }

    fun startPair(context: Context, controllerInfo: BleControllerInfo) {
        operation = Operation.PAIR
        Dialogs.showWait(context.getString(R.string.pairing))
        handler.sendEmptyMessageDelayed(DISMISS_DIALOG, TIMEOUT_MILLIS)
        val buildModel =
            TransmitterManager.instance().buildModel(controllerInfo.sn, controllerInfo.address)
        buildModel.getController().pair()
        buildModel.getController().startTime()
    }

    fun startUnpair(context: Context, isForce: Boolean) {
        operation = Operation.UNPAIR
        isForceUnpair = isForce
        handler.sendEmptyMessageDelayed(DISMISS_DIALOG, TIMEOUT_MILLIS)
        val model = TransmitterManager.instance().getDefault()
        model?.let {
            Dialogs.showWait(context.getString(R.string.Connecting))
            it.getController().clearPair()
        }
    }

    private fun pairFailedTips(context: Context) {
//        Dialogs.showBottom(object : OnBindView<BottomDialog?>(R.layout.dialog_with_one_btn) {
//            override fun onBind(dialog: BottomDialog?, v: View?) {
//                v?.let {
//                    val bind = DialogWithOneBtnBinding.bind(it)
//                    bind.tvContent.text = context.getString(R.string.Pairing_Failed)
//                    bind.tvDesc.text = context.getString(R.string.Bluetooth_Pair_Denied_Tip)
//                    bind.btOk.text = context.getString(R.string.Button_Reset)
//                    bind.btOk.setOnClickListener {
//                        dialog?.dismiss()
//                    }
//                }
//            }
//        })
    }
}