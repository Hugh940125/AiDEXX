package com.microtech.aidexx.ui.pair

import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Handler
import android.os.Looper
import android.os.Message
import com.microtech.aidexx.R
import com.microtech.aidexx.ble.AidexBleAdapter
import com.microtech.aidexx.ble.MessageDistributor
import com.microtech.aidexx.ble.MessageObserver
import com.microtech.aidexx.ble.device.TransmitterManager
import com.microtech.aidexx.common.toastShort
import com.microtech.aidexx.db.ObjectBox
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

    enum class Operation { PAIR, UNPAIR }

    private var operation: Operation? = null
    private const val DISMISS_DIALOG = 1
    private const val TIMEOUT_MILLIS = 40 * 1000L

    val handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            Dialogs.dismissWait()
            TransmitterManager.instance().removePair()
        }
    }
    private var isForceUnpair: Boolean = false
    private var receiver: BroadcastReceiver? = null

    class BondStateReceiver : BroadcastReceiver() {
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

    fun pairFail() {
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
                val instance = TransmitterManager.instance()
                val model = if (operation == Operation.PAIR) instance.getPair()
                else instance.getDefault()
                if (model != null) {
                    when (message.operation) {
                        AidexXOperation.DISCOVER -> {
                            if (!success) {
                                pairFail()
                                Dialogs.showError(context.getString(R.string.Search_Timeout))
                            }
                        }

                        AidexXOperation.CONNECT -> {
                            LogUtil.eAiDEX("Pair ----> connect:$success")
                            if (success) {
                                Dialogs.showWait(context.getString(R.string.Connecting))
                            } else {
                                pairFail()
                                if (AidexBleAdapter.getInstance().connectStatus == 22) {
                                    Dialogs.showError("请确认传感器是否被其他设备配对", 2500L)
                                } else {
                                    Dialogs.showError(context.getString(R.string.Connecting_Failed))
                                }
                            }
                        }

                        CgmOperation.BOND -> {
                            LogUtil.eAiDEX("Pair ----> bond:$success")
                            if (!success) {
                                pairFail()
                                Dialogs.showError(context.getString(R.string.failure))
                            }
                        }

                        CgmOperation.PAIR -> {
                            LogUtil.eAiDEX("Pair ----> pair:$success")
                            if (!success) {
                                pairFail()
                            }
                        }

                        AidexXOperation.DELETE_BOND -> {
                            LogUtil.eAiDEX("Pair ----> delete bond:$success")
                            if (!isForceUnpair) {
                                if (success) {
                                    scope.launch {
                                        model.deletePair()
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
                                    model.deletePair()
                                }
                            } else {
                                Dialogs.showError(context.getString(R.string.Unpair_fail))
                            }
                        }

                        AidexXOperation.GET_START_TIME -> {
                            val data = message.data
                            ObjectBox.runAsync({
                                val startTimePair = ByteUtils.checkToDate(data)
                                startTimePair?.let {
                                    model.updateStart(startTimePair)
                                }
                                model.savePair()
                            }, {
                                scope.launch {
                                    instance.set(model)
                                    EventBusManager.send(EventBusKey.EVENT_PAIR_RESULT, true)
                                }
                            }, {
                                scope.launch {
                                    EventBusManager.send(EventBusKey.EVENT_PAIR_RESULT, false)
                                }
                            })
                        }

                        AidexXOperation.GET_DEVICE_INFO -> {
                            val data = message.data
                            val deviceSoftVersion = ByteUtils.getDeviceSoftVersion(data)
                            val deviceType = ByteUtils.getDeviceType(data)
                            model.entity.version = deviceSoftVersion
                            model.entity.deviceModel = deviceType
                        }

                        AidexXOperation.DISCONNECT -> {
                            LogUtil.eAiDEX("Pair ----> disconnect:$success")
                            when (operation) {
                                Operation.PAIR -> {
                                    if (!model.isPaired()) {
                                        pairFail()
                                    }
                                }

                                Operation.UNPAIR -> {
                                    if (model.isPaired()) {
                                        pairFail()
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
        val pairModel =
            TransmitterManager.instance().buildModel(controllerInfo.sn, controllerInfo.address)
        pairModel?.let {
            it.getController().pair()
            it.getController().getTransInfo()
            it.getController().startTime()
        }
    }

    fun startUnpair(context: Context, isForce: Boolean) {
        if (!NetUtil.isNetAvailable(context)) {
            context.getString(R.string.net_error).toastShort()
            return
        }
        if (!isForce) {
            Dialogs.showWait(context.getString(R.string.unpairing))
        }
        handler.sendEmptyMessageDelayed(DISMISS_DIALOG, TIMEOUT_MILLIS)
        operation = Operation.UNPAIR
        isForceUnpair = isForce
        val model = TransmitterManager.instance().getDefault()
        model?.getController()?.clearPair()
    }

    fun registerBondStateChangeReceiver(context: Context) {
        receiver = BondStateReceiver()
        val filter = IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
        context.registerReceiver(receiver, filter)
    }

    fun unregisterBondStateChangeReceiver(context: Context) {
        receiver?.let {
            context.unregisterReceiver(it)
        }
    }
}