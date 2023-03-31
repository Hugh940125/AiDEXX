package com.microtech.aidexx.ui.pair

import android.content.Context
import android.view.View
import com.microtech.aidexx.R
import com.microtech.aidexx.ble.MessageDispatcher
import com.microtech.aidexx.ble.device.TransmitterManager
import com.microtech.aidexx.common.date2ymdhm
import com.microtech.aidexx.databinding.DialogWithOneBtnBinding
import com.microtech.aidexx.utils.ByteUtils
import com.microtech.aidexx.utils.LogUtil
import com.microtech.aidexx.utils.eventbus.EventBusKey
import com.microtech.aidexx.utils.eventbus.EventBusManager
import com.microtech.aidexx.widget.dialog.Dialogs
import com.microtech.aidexx.widget.dialog.x.bottom.BottomDialog
import com.microtech.aidexx.widget.dialog.x.bottom.NoSlideBottomDialog
import com.microtech.aidexx.widget.dialog.x.interfaces.OnBindView
import com.microtechmd.blecomm.constant.AidexXOperation
import com.microtechmd.blecomm.constant.CgmOperation
import com.microtechmd.blecomm.controller.BleControllerInfo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

object PairUtil {
    fun observe(
        context: Context, scope: CoroutineScope, successCallback: ((success: Boolean) -> Unit)?
    ): Job {
        return MessageDispatcher.instance().observer(scope) { msg ->
            val success = msg.isSuccess
            val default = TransmitterManager.instance().getDefault()
            default?.let {
                when (msg.operation) {
                    AidexXOperation.DISCOVER -> {
                        LogUtil.eAiDEX("Pair ----> scan:$success")
                        if (success) {
                            Dialogs.showWait(context.getString(R.string.Connecting))
                        } else {
                            Dialogs.showError(context.getString(R.string.Search_Timeout))
                        }
                    }
                    AidexXOperation.CONNECT -> {
                        LogUtil.eAiDEX("Pair ----> connect:$success")
                        if (success) {
                            Dialogs.showWait(context.getString(R.string.Pairing))
                        } else {
                            Dialogs.showError(context.getString(R.string.Connecting_Failed))
                        }
                    }
                    CgmOperation.BOND -> {
                        LogUtil.eAiDEX("Pair ----> bond:$success")
                        if (success) {

                        } else {
                            Dialogs.showError(context.getString(R.string.failure))
                        }
                    }
                    CgmOperation.PAIR -> {
                        LogUtil.eAiDEX("Pair ----> pair:$success")
                        if (success) {

                        } else {
                            pairFailedTips(context)
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
                        val data = msg.data
                        val sensorStartTime = ByteUtils.toDate(data)
                        LogUtil.eAiDEX("Pair ----> Start time :" + sensorStartTime.date2ymdhm())
                        default.entity.sensorStartTime = sensorStartTime
                        scope.launch {
                            default.savePair(success = {
                                successCallback?.invoke(true)
                                this.launch(Dispatchers.Main) {
                                    Dialogs.showSuccess(context.getString(R.string.Pairing_Succeed))
                                    EventBusManager.send(EventBusKey.EVENT_PAIR_SUCCESS, true)
                                }
                            }, fail = {
                                this.launch(Dispatchers.Main) {
                                    Dialogs.showError(context.getString(R.string.Pairing_Failed))
                                }
                            })
                        }
                    }
                    AidexXOperation.GET_DEVICE_INFO -> {}

                    AidexXOperation.DISCONNECT -> {
                        LogUtil.eAiDEX("Pair ----> disconnect:$success")
                        Dialogs.dismissWait()
                    }
                }
            }
        }
    }

    fun startPair(context: Context, controllerInfo: BleControllerInfo) {
        Dialogs.showWait(context.getString(R.string.Searching))
        val buildModel =
            TransmitterManager.instance().buildModel(controllerInfo.sn, controllerInfo.address)
        buildModel.getController().pair()
        buildModel.getController().startTime()
    }

    fun startUnpair(context: Context) {
        Dialogs.showWait(context.getString(R.string.Searching))
        val model = TransmitterManager.instance().getDefault()
        model?.getController()?.unpair()
    }

    private fun pairFailedTips(context: Context) {
        NoSlideBottomDialog(object : OnBindView<BottomDialog?>(R.layout.dialog_with_one_btn) {
            override fun onBind(dialog: BottomDialog?, v: View?) {
                v?.let {
                    val bind = DialogWithOneBtnBinding.bind(it)
                    bind.tvContent.text = context.getString(R.string.Pairing_Failed)
                    bind.tvDesc.text = context.getString(R.string.Bluetooth_Pair_Denied_Tip)
                    bind.btOk.text = context.getString(R.string.Button_Reset)
                    bind.btOk.setOnClickListener {
                        dialog?.dismiss()
                    }
                }
            }
        })
    }
}