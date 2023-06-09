package com.microtech.aidexx.ui.pair

import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Handler
import android.os.Looper
import android.os.Message
import com.microtech.aidexx.AidexxApp
import com.microtech.aidexx.R
import com.microtech.aidexx.ble.AidexBleAdapter
import com.microtech.aidexx.ble.MessageDistributor
import com.microtech.aidexx.ble.MessageObserver
import com.microtech.aidexx.ble.device.TransmitterManager
import com.microtech.aidexx.common.toastShort
import com.microtech.aidexx.utils.ByteUtils
import com.microtech.aidexx.utils.LogUtil
import com.microtech.aidexx.utils.NetUtil
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
    private var isBonding = false
    private var isBondListenerRegister = false

    enum class Operation { PAIR, UNPAIR }

    private var operation: Operation? = null
    private const val DISMISS_DIALOG = 1
    private const val TIMEOUT_MILLIS = 40 * 1000L

    val handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            Dialogs.dismissWait()
        }
    }

    private var isForceUnpair: Boolean = false

    private val receiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val action = intent?.action
            if (action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)) {
                when (intent!!.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, -1)) {
                    BluetoothDevice.BOND_BONDING -> {
                        isBonding = true
                    }
                    BluetoothDevice.BOND_BONDED -> {
                        isBonding = false
                    }
                    BluetoothDevice.BOND_NONE -> {
                        if (isBonding) {
                            AidexBleAdapter.getInstance().executeDisconnect()
                            LogUtil.eAiDEX("BluetoothDevice.BOND_NONE dismissWait")
                            isBonding = false
                        }
                    }
                    else -> {
                    }
                }
            }
        }
    }

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
                            val startTimePair = ByteUtils.checkToDate(data)
                            default.entity.sensorStartTime = startTimePair
                            scope.launch {
                                default.savePair()
                            }
                        }
                        AidexXOperation.GET_DEVICE_INFO -> {
                            val data = message.data
                            val deviceSoftVersion = ByteUtils.getDeviceSoftVersion(data)
                            val deviceType = ByteUtils.getDeviceType(data)
                            default.entity.version = deviceSoftVersion
                            default.entity.deviceModel = deviceType
                            default.getController().startTime()
                        }

                        AidexXOperation.DISCONNECT -> {
                            LogUtil.eAiDEX("Pair ----> disconnect:$success")
                            when (operation) {
                                Operation.PAIR -> {
                                    if (!default.isPaired()) {
                                        fail()
                                    }
                                }
                                Operation.UNPAIR -> {
                                    if (default.isPaired()) {
                                        fail()
                                    }
                                }
                                else -> {}
                            }
                            Dialogs.dismissWait()
                        }
                        else -> {}
                    }
                }
            }
        })
    }

    fun startPair(context: Context, controllerInfo: BleControllerInfo) {
        if (!NetUtil.isNetAvailable(context)) {
            context.getString(R.string.net_error).toastShort()
            return
        }
        operation = Operation.PAIR
        Dialogs.showWait(context.getString(R.string.pairing))
        handler.sendEmptyMessageDelayed(DISMISS_DIALOG, TIMEOUT_MILLIS)
        val buildModel =
            TransmitterManager.instance().buildModel(controllerInfo.sn, controllerInfo.address)
        buildModel.getController().pair()
        buildModel.getController().getTransInfo()
    }

    fun startUnpair(context: Context, isForce: Boolean) {
        if (!NetUtil.isNetAvailable(context)) {
            context.getString(R.string.net_error).toastShort()
            return
        }
        handler.sendEmptyMessageDelayed(DISMISS_DIALOG, TIMEOUT_MILLIS)
        Dialogs.showWait(context.getString(R.string.unpairing))
        operation = Operation.UNPAIR
        isForceUnpair = isForce
        val model = TransmitterManager.instance().getDefault()
        model?.getController()?.clearPair()
    }

    fun registerBondStateChangeReceiver() {
        val filter = IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
        AidexxApp.instance.registerReceiver(receiver, filter)
        isBondListenerRegister = true
    }

    fun unregisterBondStateChangeReceiver() {
        if (isBondListenerRegister) {
            AidexxApp.instance.unregisterReceiver(receiver)
            isBondListenerRegister = false
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