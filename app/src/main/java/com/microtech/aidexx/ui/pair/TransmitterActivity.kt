package com.microtech.aidexx.ui.pair

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.view.View
import android.view.View.OnClickListener
import android.view.animation.Animation
import android.view.animation.LinearInterpolator
import android.view.animation.RotateAnimation
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import com.microtech.aidexx.AidexxApp
import com.microtech.aidexx.R
import com.microtech.aidexx.base.BaseActivity
import com.microtech.aidexx.base.BaseViewModel
import com.microtech.aidexx.ble.AidexBleAdapter
import com.microtech.aidexx.ble.MessageDispatcher
import com.microtech.aidexx.ble.device.TransmitterManager
import com.microtech.aidexx.common.date2ymdhm
import com.microtech.aidexx.databinding.ActivityTransmitterBinding
import com.microtech.aidexx.databinding.DialogWithOneBtnBinding
import com.microtech.aidexx.db.ObjectBox
import com.microtech.aidexx.db.entity.TransmitterEntity
import com.microtech.aidexx.utils.ByteUtils
import com.microtech.aidexx.utils.LogUtil
import com.microtech.aidexx.utils.adapter.BaseQuickAdapter
import com.microtech.aidexx.utils.adapter.OnItemClickListener
import com.microtech.aidexx.widget.dialog.Dialogs
import com.microtech.aidexx.widget.dialog.x.bottom.BottomDialog
import com.microtech.aidexx.widget.dialog.x.bottom.NoSlideBottomDialog
import com.microtech.aidexx.widget.dialog.x.interfaces.OnBindView
import com.microtechmd.blecomm.constant.AidexXOperation
import com.microtechmd.blecomm.constant.CgmOperation
import com.microtechmd.blecomm.controller.BleControllerInfo
import io.objectbox.reactive.DataObserver
import io.objectbox.reactive.DataSubscription
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.ref.WeakReference

private const val DISMISS_LOADING = 2004
private const val REFRESH_TRANS_LIST = 2005

const val OPERATION_TYPE_PAIR: Int = 1
const val OPERATION_TYPE_UNPAIR: Int = 2
const val OPERATION_TYPE = "type"
const val BLE_INFO = "info"

class TransmitterActivity : BaseActivity<BaseViewModel, ActivityTransmitterBinding>(), OnClickListener {
    private var scanStarted = false
    private lateinit var rotateAnimation: RotateAnimation
    private var subscription: DataSubscription? = null
    private lateinit var transmitterHandler: TransmitterHandler
    private lateinit var transmitterAdapter: TransmitterAdapter
    private var transmitter: TransmitterEntity? = null
    private val transmitterList = mutableListOf<BleControllerInfo>()

    class TransmitterHandler(val activity: TransmitterActivity) : Handler(Looper.getMainLooper()) {
        private val reference = WeakReference(activity)
        override fun handleMessage(msg: Message) {
            reference.get()?.let {
                if (!it.isFinishing) {
                    when (msg.what) {
                        DISMISS_LOADING -> {
                            Dialogs.dismissWait()
                        }
                        REFRESH_TRANS_LIST -> {
                            activity.refreshList()
                        }
                    }
                }
            }
        }
    }

    fun refreshList() {
        val deviceStore = AidexBleAdapter.getInstance().deviceStore
        for (result in deviceStore.deviceMap.values) {
            val name = AdvertisingParser.getName(result.scanRecord?.bytes)
            val device = result.device
            if (name.contains("AiDEX X")) {
                if (name.length < 6) {
                    LogUtil.eAiDEX("Device name length less than 6")
                    return
                }
                val sn = name.substring(name.length - 6)
                val address = device.address
                val bleControllerInfo =
                    BleControllerInfo(address, name, sn, result.rssi + 130)
                if (transmitter != null && address == transmitter?.deviceMac) {
                    continue
                }
                if (!transmitterList.contains(bleControllerInfo)) {
                    transmitterList.add(bleControllerInfo)
                }
            }
        }
        transmitterAdapter.setList(transmitterList)
        transmitterHandler.sendEmptyMessageDelayed(REFRESH_TRANS_LIST, 3000)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        AidexxApp.isPairing = true
        transmitterHandler = TransmitterHandler(this)
        loadSavedTransmitter()
        if (transmitter == null || transmitter?.accessId == null) {
            AidexBleAdapter.getInstance().startBtScan(false)
            scanStarted = true
            Dialogs.showWait(getString(R.string.loading))
            transmitterHandler.sendEmptyMessageDelayed(DISMISS_LOADING, 3 * 1000)
        }
        initAnim()
        initView()
        observeMessage()
    }

    private fun initAnim() {
        rotateAnimation =
            RotateAnimation(
                0f, 360f, RotateAnimation.RELATIVE_TO_SELF,
                0.5f, RotateAnimation.RELATIVE_TO_SELF, 0.5f
            )
        rotateAnimation.fillAfter = true
        rotateAnimation.interpolator = LinearInterpolator()
        rotateAnimation.repeatCount = Animation.INFINITE
        rotateAnimation.repeatMode = Animation.RESTART
        rotateAnimation.duration = 1500
    }

    override fun onResume() {
        super.onResume()
        binding.ivRefreshScan.startAnimation(rotateAnimation)
    }

    override fun onPause() {
        super.onPause()
        binding.ivRefreshScan.clearAnimation()
    }

    private fun startPair(controllerInfo: BleControllerInfo) {
        Dialogs.showWait(getString(R.string.Searching))
//        if (transmitter != null && transmitter?.accessId != null) {
////            unpairOld()
//        } else {
        val buildModel = TransmitterManager.instance().buildModel(controllerInfo.sn, controllerInfo.address)
        buildModel.getController().pair()
//        }
    }

//    private fun unpairOld() {
//        TODO("Not yet implemented")
//    }

    private fun observeMessage() {
        MessageDispatcher.instance().observer(lifecycleScope) { msg ->
            val success = msg.isSuccess
            val default = TransmitterManager.instance().getDefault()
            default?.let {
                when (msg.operation) {
                    AidexXOperation.DISCOVER -> {
                        LogUtil.eAiDEX("Pair ----> scan:$success")
                        if (success) {
                            Dialogs.showWait(resources.getString(R.string.Connecting))
                        } else {
                            Dialogs.showError(resources.getString(R.string.Search_Timeout))
                        }
                    }
                    AidexXOperation.CONNECT -> {
                        LogUtil.eAiDEX("Pair ----> connect:$success")
                        if (success) {
                            Dialogs.showWait(resources.getString(R.string.Pairing))
                        } else {
                            Dialogs.showError(resources.getString(R.string.Connecting_Failed))
                        }
                    }
                    CgmOperation.BOND -> {
                        LogUtil.eAiDEX("Pair ----> bond:$success")
                        if (success) {
//                        default?.run {
//                            this.getController().getDefaultParam()
//                            this.getController().getTransInfo()
//                        }
                        } else {
                            Dialogs.showError(resources.getString(R.string.Pairing_Failed))
                        }
                    }
                    CgmOperation.PAIR -> {
                        LogUtil.eAiDEX("Pair ----> pair:$success")
                        if (success) {
                            default.getController().startTime()
                        } else {
                            pairFailedTips()
                        }
                    }
                    AidexXOperation.GET_START_TIME -> {
                        val data = msg.data
                        val sensorStartTime = ByteUtils.toDate(data)
                        LogUtil.eAiDEX("Pair ----> Start time :" + sensorStartTime.date2ymdhm())
                        default.entity.sensorStartTime = sensorStartTime
                        lifecycleScope.launch {
                            default.savePair(success = {
                                transmitterList.clear()
                                transmitterHandler.removeMessages(REFRESH_TRANS_LIST)
                                transmitterHandler.sendEmptyMessage(REFRESH_TRANS_LIST)
                                this.launch {
                                    Dialogs.showSuccess(resources.getString(R.string.Pairing_Succeed))
                                }
                            }, fail = {
                                this.launch {
                                    Dialogs.showError(resources.getString(R.string.Pairing_Failed))
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

    private fun pairFailedTips() {
        NoSlideBottomDialog(object : OnBindView<BottomDialog?>(R.layout.dialog_with_one_btn) {
            override fun onBind(dialog: BottomDialog?, v: View?) {
                v?.let {
                    val bind = DialogWithOneBtnBinding.bind(it)
                    bind.tvContent.text = getString(R.string.Pairing_Failed)
                    bind.tvDesc.text = getString(R.string.Bluetooth_Pair_Denied_Tip)
                    bind.btOk.text = getString(R.string.Button_Reset)
                    bind.btOk.setOnClickListener {
                        dialog?.dismiss()
                    }
                }
            }
        })
    }

    private fun initView() {
        binding.actionbarTransmitter.getLeftIcon().setOnClickListener { finish() }
        binding.rvOtherTrans.layoutManager = LinearLayoutManager(this)
        transmitterAdapter = TransmitterAdapter()
        transmitterAdapter.onPairClick = {
            checkEnvironment {
                startPair(it)
            }
        }
        binding.layoutMyTrans.root.setOnClickListener(this)
        binding.rvOtherTrans.adapter = transmitterAdapter
        transmitterHandler.sendEmptyMessage(REFRESH_TRANS_LIST)
    }

    override fun onDestroy() {
        super.onDestroy()
        AidexxApp.isPairing = false
        if (scanStarted) {
            AidexBleAdapter.getInstance().stopBtScan(false)
        }
        transmitterHandler.removeCallbacksAndMessages(null)
        subscription?.cancel()
    }

    override fun getViewBinding(): ActivityTransmitterBinding {
        return ActivityTransmitterBinding.inflate(layoutInflater)
    }

    private fun loadSavedTransmitter() {
        val observer = DataObserver<Class<TransmitterEntity>> { refreshMine() }
        subscription = ObjectBox.store.subscribe(TransmitterEntity::class.java).observer(observer)
    }

    private fun refreshMine() {
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                transmitter = TransmitterManager.instance().getDefault()?.entity
            }
            if (transmitter == null) {
                binding.tvPlsSelectTrans.visibility = View.VISIBLE
                binding.layoutMyTrans.root.visibility = View.GONE
            } else {
                binding.tvPlsSelectTrans.visibility = View.GONE
                binding.layoutMyTrans.root.visibility = View.VISIBLE
                binding.layoutMyTrans.tvSn.text = transmitter!!.deviceName
//                binding.layoutMyTrans.buttonDelete.setOnClickListener(this@TransmitterActivity)
                binding.layoutMyTrans.tvTransPairState.visibility = View.VISIBLE
                if (transmitter!!.accessId == null) {
                    binding.layoutMyTrans.tvTransPairState.text = "未配对"
                } else {
                    binding.layoutMyTrans.tvTransPairState.text = "已配对"
                }
            }
        }
    }

    override fun onClick(v: View?) {
        when (v) {
            binding.layoutMyTrans.root -> {
                val intent = Intent()
                intent.putExtra(
                    BLE_INFO,
                    BleControllerInfo(transmitter?.deviceMac, transmitter?.deviceName, transmitter?.deviceSn, 130)
                )
                if (transmitter!!.accessId == null) {
                    intent.putExtra(OPERATION_TYPE, OPERATION_TYPE_PAIR)
                } else {
                    intent.putExtra(OPERATION_TYPE, OPERATION_TYPE_UNPAIR)
                }
                startActivity(intent)
//                checkEnvironment {
//                    startPair()
//                }
            }
        }
    }
}