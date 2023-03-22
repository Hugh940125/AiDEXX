package com.microtech.aidexx.ui.pair

import android.os.*
import android.view.View
import android.view.View.OnClickListener
import androidx.lifecycle.lifecycleScope
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
import com.microtech.aidexx.utils.*
import com.microtech.aidexx.utils.permission.PermissionGroups
import com.microtech.aidexx.utils.permission.PermissionsUtil
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

class TransmitterActivity : BaseActivity<BaseViewModel, ActivityTransmitterBinding>(), OnClickListener {
    private var scanStarted = false
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
//                LogUtil.eAiDEX(
//                    "Find device" + result.device.address + "--$name - $sn--" + StringUtils.binaryToHexString(
//                        result.scanRecord?.bytes!!
//                    )
//                )
                if (name.length < 6) {
                    LogUtil.eAiDEX("Device name length less than 6")
                    return
                }
                val sn = name.substring(name.length - 6)
                val bleControllerInfo =
                    BleControllerInfo(device.address, name, sn, result.rssi + 130)
                if (transmitter != null && sn == transmitter?.deviceSn) {
                    continue
                }
                if (!transmitterList.contains(bleControllerInfo)) {
                    transmitterList.add(bleControllerInfo)
                }
            }
        }
        transmitterAdapter.setList(transmitterList)
        transmitterHandler.sendEmptyMessageDelayed(REFRESH_TRANS_LIST, 1500)
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
        initView()
        observeMessage()
    }

    private fun checkEnvironment(controllerInfo: BleControllerInfo) {
        if (!BleUtil.isBleEnable(this)) {
            enableBluetooth()
            return
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PermissionsUtil.checkPermissions(this, PermissionGroups.Bluetooth) {
                requestPermission()
                return@checkPermissions
            }
        } else {
            PermissionsUtil.checkPermissions(this, PermissionGroups.Location) {
                requestPermission()
                return@checkPermissions
            }
        }
        if (!LocationUtils.isLocationServiceEnable(this) && Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            enableLocation()
            return
        }
        if (!NetUtil.isNetAvailable(this)) {
            Dialogs.showError(getString(R.string.net_error))
            return
        }
        startPair(controllerInfo)
    }

    private fun startPair(controllerInfo: BleControllerInfo) {
        Dialogs.showWait(getString(R.string.Searching))
//        if (transmitter != null && transmitter?.accessId != null) {
////            unpairOld()
//        } else {
        val buildModel = TransmitterManager.instance().buildModel(controllerInfo.sn)
        buildModel.controller.mac = controllerInfo.address
        buildModel.getController().pair()
        buildModel.getController().startTime()
//        }
    }

//    private fun unpairOld() {
//        TODO("Not yet implemented")
//    }

    private fun requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PermissionsUtil.requestPermissions(this, PermissionGroups.Bluetooth)
        } else {
            PermissionsUtil.requestPermissions(this, PermissionGroups.Location)
        }
    }

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
        binding.rvOtherTrans.layoutManager = LinearLayoutManager(this)
        transmitterAdapter = TransmitterAdapter()
        transmitterAdapter.onPairClick = {
            checkEnvironment(it)
        }
        binding.layoutMyTrans.buttonPair.setOnClickListener(this)
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
                transmitter = TransmitterManager.instance().loadTransmitter()
            }
            if (transmitter == null) {
                binding.tvPlsSelectTrans.visibility = View.VISIBLE
                binding.layoutMyTrans.root.visibility = View.GONE
            } else {
                binding.tvPlsSelectTrans.visibility = View.GONE
                binding.layoutMyTrans.root.visibility = View.VISIBLE
                binding.layoutMyTrans.tvSn.text = transmitter!!.deviceSn
                binding.layoutMyTrans.buttonDelete.setOnClickListener(this@TransmitterActivity)
                if (transmitter!!.accessId == null) {
                    binding.layoutMyTrans.buttonPair.visibility = View.VISIBLE
                    binding.layoutMyTrans.buttonUnpair.visibility = View.GONE
                } else {
                    binding.layoutMyTrans.buttonPair.visibility = View.GONE
                    binding.layoutMyTrans.buttonUnpair.visibility = View.VISIBLE
                }
            }
        }
    }

    override fun onClick(v: View?) {
        when (v) {
            binding.layoutMyTrans.buttonDelete -> {

            }
            binding.layoutMyTrans.buttonPair -> {
                checkEnvironment(BleControllerInfo(transmitter?.deviceMac, "", transmitter?.deviceSn, 130))
            }
        }
    }
}