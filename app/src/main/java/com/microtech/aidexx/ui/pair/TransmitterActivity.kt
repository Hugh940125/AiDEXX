package com.microtech.aidexx.ui.pair

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.os.Build
import android.os.Bundle
import android.os.ParcelUuid
import android.view.View
import android.view.View.OnClickListener
import androidx.lifecycle.lifecycleScope
import com.microtech.aidexx.base.BaseActivity
import com.microtech.aidexx.base.BaseViewModel
import com.microtech.aidexx.ble.AidexBleAdapter
import com.microtech.aidexx.ble.device.TransmitterManager
import com.microtech.aidexx.ble.device.entity.TransmitterEntity
import com.microtech.aidexx.databinding.ActivityTransmitterBinding
import com.microtech.aidexx.utils.LogUtil
import com.microtech.aidexx.utils.StringUtils
import com.microtech.aidexx.utils.permission.PermissionsUtil
import com.microtechmd.blecomm.controller.BleControllerInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TransmitterActivity : BaseActivity<BaseViewModel, ActivityTransmitterBinding>(), OnClickListener {
    private lateinit var transmitterListAdapter: FindTransmitterAdapter
    private var transmitter: TransmitterEntity? = null
    private val mutableSharedFlow = MutableSharedFlow<Boolean>()
    private val transmitterList = mutableListOf<BleControllerInfo>()

    @OptIn(FlowPreview::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        transmitterListAdapter = FindTransmitterAdapter()
        lifecycleScope.launch {
            mutableSharedFlow.debounce(1500).collect {
                transmitterListAdapter.setList(transmitterList)
            }
        }
    }

    override fun getViewBinding(): ActivityTransmitterBinding {
        return ActivityTransmitterBinding.inflate(layoutInflater)
    }

    fun loadSavedTransmitter() {
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

    private val scanCallback: ScanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)
            //将扫描到的广播，传入处理数据。
            result.scanRecord?.let {
                val sn = AdvertisingParser.getSN(result.scanRecord?.bytes)
                val name = AdvertisingParser.getName(result.scanRecord?.bytes)
                LogUtil.eAiDEX(
                    "Find device" + result.device.address + "--$name--$sn--" + StringUtils.binaryToHexString(
                        result.scanRecord?.bytes!!
                    )
                )
                val device = result.device
                if (name.contains("AiDEX X")) {
                    val adapter = AidexBleAdapter.getInstance() as AidexBleAdapter
                    adapter.onScanBack(result)
                    val bleControllerInfo = BleControllerInfo(device.address, name, sn, result.rssi + 130)
                    if (sn != transmitter?.deviceSn && transmitterList.contains(bleControllerInfo)) {
                        transmitterList.add(bleControllerInfo)
                    }
                    lifecycleScope.launch {
                        mutableSharedFlow.emit(true)
                    }
                }
            }
        }

        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
//            LogUtils.e("扫描开启失败 errorCode :$errorCode")
        }
    }

    @SuppressLint("MissingPermission")
    fun startScan() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PermissionsUtil.checkAndRequestPermissions(this, arrayOf(Manifest.permission.BLUETOOTH_SCAN)) {
                getBleAdapter().bluetoothLeScanner?.startScan(
                    buildScanFilters(),
                    buildScanSettings(),
                    scanCallback
                )
            }
        } else {
            getBleAdapter().bluetoothLeScanner?.startScan(
                buildScanFilters(),
                buildScanSettings(),
                scanCallback
            )
        }
    }

    @SuppressLint("MissingPermission")
    fun stopScan() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PermissionsUtil.checkAndRequestPermissions(this, arrayOf(Manifest.permission.BLUETOOTH_SCAN)) {
                getBleAdapter().bluetoothLeScanner?.stopScan(scanCallback)
            }
        } else {
            getBleAdapter().bluetoothLeScanner?.stopScan(scanCallback)
        }
    }

    private fun getBleAdapter(): BluetoothAdapter {
        val bluetoothManager = getSystemService(BLUETOOTH_SERVICE) as BluetoothManager
        return bluetoothManager.adapter
    }

    private fun buildScanFilters(): List<ScanFilter> {
        val scanFilterList: MutableList<ScanFilter> = ArrayList()
        val scanFilterBuilder = ScanFilter.Builder()
        val parcelUuidMask = ParcelUuid.fromString("0000181F-0000-1000-8000-00805F9B34FB")
        val parcelUuid = ParcelUuid.fromString("00002AFE-0000-1000-8000-00805F9B34FB")
//        scanFilterBuilder.setServiceUuid(parcelUuid, parcelUuidMask)
        scanFilterList.add(scanFilterBuilder.build())
        return scanFilterList
    }

    private fun buildScanSettings(): ScanSettings? {
        val scanSettingBuilder = ScanSettings.Builder()
        scanSettingBuilder.setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
        scanSettingBuilder.setMatchMode(ScanSettings.MATCH_MODE_AGGRESSIVE)
        if (getBleAdapter().isOffloadedScanBatchingSupported) {
            scanSettingBuilder.setReportDelay(0L)
        }
        scanSettingBuilder.setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
        return scanSettingBuilder.build()
    }
}