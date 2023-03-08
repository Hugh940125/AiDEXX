package com.microtech.aidexx.ui.pair

import android.os.*
import android.view.View
import android.view.View.OnClickListener
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.microtech.aidexx.R
import com.microtech.aidexx.base.BaseActivity
import com.microtech.aidexx.base.BaseViewModel
import com.microtech.aidexx.ble.AidexBleAdapter
import com.microtech.aidexx.ble.MessageDispatcher
import com.microtech.aidexx.ble.device.TransmitterManager
import com.microtech.aidexx.ble.device.entity.TransmitterEntity
import com.microtech.aidexx.databinding.ActivityTransmitterBinding
import com.microtech.aidexx.utils.*
import com.microtech.aidexx.utils.permission.PermissionGroups
import com.microtech.aidexx.utils.permission.PermissionsUtil
import com.microtech.aidexx.widget.dialog.Dialogs
import com.microtechmd.blecomm.constant.AidexXOperation
import com.microtechmd.blecomm.controller.BleControllerInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.ref.WeakReference

private const val DISMISS_LOADING = 2002
private const val REFRESH_TRANS_LIST = 2003

class TransmitterActivity : BaseActivity<BaseViewModel, ActivityTransmitterBinding>(), OnClickListener {
    private var scanStarted = false
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
            val sn = AdvertisingParser.getSN(result.scanRecord?.bytes)
            val name = AdvertisingParser.getName(result.scanRecord?.bytes)
            val device = result.device
            if (name.contains("AiDEX X")) {
                LogUtil.eAiDEX(
                    "Find device" + result.device.address + "--$name - $sn--" + StringUtils.binaryToHexString(
                        result.scanRecord?.bytes!!
                    )
                )
                val bleControllerInfo = BleControllerInfo(device.address, name, sn, result.rssi + 130)
                if (transmitter != null && sn == transmitter?.deviceSn) {
                    continue
                }
                if (!transmitterList.contains(bleControllerInfo)) {
                    transmitterList.add(bleControllerInfo)
                }
            }
        }
        transmitterAdapter.setList(transmitterList)
        transmitterHandler.sendEmptyMessageDelayed(REFRESH_TRANS_LIST, 1000)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        transmitterHandler = TransmitterHandler(this)
        loadSavedTransmitter()
        if (transmitter == null || transmitter?.accessId == null) {
            AidexBleAdapter.getInstance().startBtScan(false)
            scanStarted = true
            Dialogs.showWait(getString(R.string.loading))
            transmitterHandler.sendEmptyMessageDelayed(DISMISS_LOADING, 2 * 1000)
        }
        initView()
        observeMessage()
    }

    fun pair() {
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
        }
        if (!NetUtil.isNetAvailable(this)) {
            Dialogs.showError(getString(R.string.net_error))
            return
        }
    }

    private fun requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PermissionsUtil.requestPermissions(this, PermissionGroups.Bluetooth)
        } else {
            PermissionsUtil.requestPermissions(this, PermissionGroups.Location)
        }
    }

    private fun observeMessage() {
        MessageDispatcher.instance().observer(lifecycleScope) {
            val success = it.isSuccess
            when (it.operation) {
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
                AidexXOperation.DISCONNECT -> {
                    LogUtil.eAiDEX("Pair ----> disconnect:$success")
                    Dialogs.dismissWait()
                }
            }
        }
    }

    private fun initView() {
        binding.rvOtherTrans.layoutManager = LinearLayoutManager(this)
        transmitterAdapter = TransmitterAdapter()
        binding.rvOtherTrans.adapter = transmitterAdapter
        transmitterHandler.sendEmptyMessage(REFRESH_TRANS_LIST)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (scanStarted) {
            AidexBleAdapter.getInstance().stopBtScan(false)
        }
        transmitterHandler.removeCallbacksAndMessages(null)
    }

    override fun getViewBinding(): ActivityTransmitterBinding {
        return ActivityTransmitterBinding.inflate(layoutInflater)
    }

    private fun loadSavedTransmitter() {
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
        }
    }
}