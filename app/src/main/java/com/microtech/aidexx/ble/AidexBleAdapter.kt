package com.microtech.aidexx.ble

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.*
import android.bluetooth.BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanFilter
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.os.Message
import android.os.ParcelUuid
import androidx.core.app.ActivityCompat
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.microtech.aidexx.AidexxApp
import com.microtech.aidexx.ble.device.TransmitterManager.Companion.instance
import com.microtech.aidexx.ble.device.work.StartScanWorker
import com.microtech.aidexx.ble.device.work.StopScanWorker
import com.microtech.aidexx.common.toIntBigEndian
import com.microtech.aidexx.common.toUuid
import com.microtech.aidexx.utils.LogUtil.Companion.eAiDEX
import com.microtech.aidexx.utils.StringUtils
import com.microtech.aidexx.utils.StringUtils.binaryToHexString
import com.microtech.aidexx.utils.TimeUtils.currentTimeMillis
import com.microtech.aidexx.utils.eventbus.EventBusKey
import com.microtech.aidexx.utils.eventbus.EventBusManager.send
import com.microtechmd.blecomm.BleAdapter
import com.microtechmd.blecomm.BluetoothDeviceStore
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * APP-SRC-A-105
 */
class AidexBleAdapter private constructor() : BleAdapter() {
    var retryNum = 0
    var workHandler: Handler? = null
    var lastDisConnectTime: Long = 0
    var mCharacteristic: BluetoothGattCharacteristic? = null
    private lateinit var mContext: Context

    //蓝牙设备
    private var mBluetoothDevice: BluetoothDevice? = null

    //蓝牙服务
    private var mBluetoothGatt: BluetoothGatt? = null

    //设置蓝牙扫描过滤器集合
    private var scanFilterList: MutableList<ScanFilter>? = null

    //设置蓝牙扫描过滤器
    private var scanFilterBuilder: ScanFilter.Builder? = null

    //设置蓝牙扫描设置
    private var scanSettingBuilder: ScanSettings.Builder? = null
    private val bluetoothDeviceStore = BluetoothDeviceStore()
    var isScaning = false
    private var isOnConnectState = false
    private var timerTask: TimerTask? = null
    private var characteristicsMap = hashMapOf<Int, BluetoothGattCharacteristic>()
    private val bluetoothAdapter: BluetoothAdapter
        get() {
            val bluetoothManager = mContext.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
            return bluetoothManager.adapter
        }

    override fun getDeviceStore(): BluetoothDeviceStore {
        return bluetoothDeviceStore
    }

    private val scanCallback: ScanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            super.onScanResult(callbackType, result)
            val scanRecord = result.scanRecord!!.bytes
            val address = result.device.address
            val rssi = result.rssi + 130
            onScanRespond(address, rssi, scanRecord)
            onAdvertise(address, rssi, scanRecord)
            bluetoothDeviceStore.add(result)
        }

        override fun onScanFailed(errorCode: Int) {
            super.onScanFailed(errorCode)
            eAiDEX("onScanFailed errorCode:$errorCode")
        }
    }

    fun refreshConnectState(isConnect: Boolean) {
        isOnConnectState = isConnect
    }

    fun initialize(context: Context): AidexBleAdapter {
        mContext = context
        setDiscoverTimeoutSeconds(DISCOVER_TIME_OUT_SECONDS)
        workHandler = object : Handler(context.mainLooper) {
            override fun handleMessage(msg: Message) {
                super.handleMessage(msg)
                val arg1 = msg.arg1
                when (msg.what) {
                    BLE_CONNECT_TIME_OUT, CONNECT_FAILURE -> {
                        workHandler!!.removeMessages(BLE_CONNECT_TIME_OUT)
                        refreshConnectState(false)
                        onConnectFailure()
                    }
                    DISCONNECT_GATT -> {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            if (ActivityCompat.checkSelfPermission(
                                    AidexxApp.instance,
                                    Manifest.permission.BLUETOOTH_CONNECT
                                ) != PackageManager.PERMISSION_GRANTED
                            ) {
                                eAiDEX("permission denied ----> Manifest.permission.BLUETOOTH_CONNECT")
                                return
                            }
                        }
                        if (mBluetoothGatt != null) {
                            mBluetoothGatt!!.disconnect()
                        }
                    }
                    CONNECT_DISCONNECTED -> {
                        refreshConnectState(false)
                        onDisconnected()
                    }
                    CONNECT_SUCCESS -> {
                        workHandler!!.removeMessages(BLE_CONNECT_TIME_OUT)
                        onConnectSuccess()
                    }
                    START_SCAN -> {
                        startBtScan(true)
                    }
                    FOUND_SERVER -> {
                        val status = msg.obj as Int
                        if (status == BluetoothGatt.GATT_SUCCESS) {
                            //根据指定的服务uuid获取指定的服务
                            val gattService = mBluetoothGatt!!.getService(serviceUUID.toUuid())
                            //根据指定特征值uuid获取指定的特征值
                            if (gattService != null) {
                                val normalCharacteristic = gattService.getCharacteristic(characteristicUUID.toUuid())
                                if (normalCharacteristic != null) {
                                    characteristicsMap[characteristicUUID] = normalCharacteristic
                                }
                                val privateCharacteristic =
                                    gattService.getCharacteristic(privateCharacteristicUUID.toUuid())
                                if (privateCharacteristic != null) {
                                    characteristicsMap[privateCharacteristicUUID] = privateCharacteristic
                                }
                                if (normalCharacteristic == null || privateCharacteristic == null) {
                                    eAiDEX("specific characteristic not found")
                                    return
                                }
                                //设置特征值通知,即设备的值有变化时会通知该特征值，即回调方法onCharacteristicChanged会有该通知
                                for ((key, characteristic) in characteristicsMap) {
                                    if (key == characteristicUUID) {
                                        mCharacteristic = characteristic
                                        continue
                                    }
                                    eAiDEX("enable notify ----> ${characteristic.uuid}")
                                    setNotify(characteristic)
                                }
                                mCharacteristic?.let {
                                    eAiDEX("enable notify ----> ${mCharacteristic?.uuid}")
//                                    setNotify(mCharacteristic!!)
                                }
                            }
                            return
                        }
                        if (status == 133) { //需要清除Gatt缓存并断开连接和关闭Gatt，然后重新连接
                            workHandler!!.sendEmptyMessage(CLOSE_GATT)
                            retry()
                        }
                    }
                    CONNECT_GATT -> {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            if (ActivityCompat.checkSelfPermission(
                                    AidexxApp.instance,
                                    Manifest.permission.BLUETOOTH_CONNECT
                                ) != PackageManager.PERMISSION_GRANTED
                            ) {
                                eAiDEX("permission denied ----> Manifest.permission.BLUETOOTH_CONNECT")
                                return
                            }
                        }
                        workHandler!!.removeMessages(BLE_CONNECT_TIME_OUT)
                        workHandler!!.sendEmptyMessageDelayed(BLE_CONNECT_TIME_OUT, BLE_CONNECT_TIME_LIMIT)
                        eAiDEX("Start connect " + mBluetoothDevice!!.address)
                        closeGatt()
                        mBluetoothGatt = mBluetoothDevice!!.connectGatt(
                            context,
                            false,
                            bluetoothGattCallback,
                            BluetoothDevice.TRANSPORT_LE
                        )
                    }
                    DISCOVER_SERVICES -> if (mBluetoothGatt != null) {
                        mBluetoothGatt!!.discoverServices()
                    }
                    CLOSE_GATT -> {
                        refreshConnectState(false)
                        closeGatt()
                    }
                    SEND_DATA -> sendData(arg1, msg.obj as ByteArray)
                    RECEIVER_DATA -> if (arg1 == 0) onReceiveData(msg.obj as ByteArray) else onReceiveData(
                        arg1,
                        msg.obj as ByteArray
                    )
                    READ_CHARACTERISTIC -> if (arg1 != 0) mBluetoothGatt!!.readCharacteristic(characteristicsMap[arg1])
                }
            }
        }
        return this
    }

    @SuppressLint("MissingPermission")
    private fun setNotify(characteristic: BluetoothGattCharacteristic) {
        val enable = mBluetoothGatt!!.setCharacteristicNotification(characteristic, true)
        if (enable) {
            for (descriptor in characteristic.descriptors) {
                if (characteristic.properties and BluetoothGattCharacteristic.PROPERTY_NOTIFY != 0) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        mBluetoothGatt!!.writeDescriptor(
                            descriptor,
                            BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                        )
                    } else {
                        descriptor.value = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE
                        mBluetoothGatt!!.writeDescriptor(descriptor)
                    }
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun closeGatt() {
        if (mBluetoothGatt != null) {
            mBluetoothGatt!!.close()
            mBluetoothGatt = null
        }
    }

    private fun sleep() {
        try {
            Thread.sleep(20)
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }

    @SuppressLint("MissingPermission")
    @Synchronized
    private fun sendData(arg1: Int, data: ByteArray) {
        try {
            val bluetoothGattCharacteristic = characteristicsMap[arg1]
            if (data.size <= 20) {
                if (bluetoothGattCharacteristic == null) {
                    eAiDEX("send data error ----> characteristic is null")
                    return
                }
                if (mBluetoothGatt == null) {
                    eAiDEX("send data error ----> gatt is null")
                    return
                }
                sleep()
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    mBluetoothGatt!!.writeCharacteristic(bluetoothGattCharacteristic, data, WRITE_TYPE_DEFAULT)
                } else {
                    bluetoothGattCharacteristic.value = data
                    bluetoothGattCharacteristic.writeType = WRITE_TYPE_DEFAULT
                    mBluetoothGatt!!.writeCharacteristic(bluetoothGattCharacteristic)
                    eAiDEX("send data ----> ${StringUtils.binaryToHexString(data)}")
                }
                if (timerTask != null) {
                    timerTask!!.cancel()
                    timerTask = null
                }
                val timer = Timer()
                timerTask = object : TimerTask() {
                    override fun run() {
                        executeDisconnect()
                    }
                }
                timer.schedule(timerTask, 1000)
            } else {
                val b1 = ByteArray(20)
                val b2 = ByteArray(data.size - 20)
                System.arraycopy(data, 0, b1, 0, 20)
                System.arraycopy(data, 20, b2, 0, data.size - 20)
                sendData(arg1, b1)
                sendData(arg1, b2)
            }
        } catch (e: Exception) {
            eAiDEX("send data error ----> $e")
            e.printStackTrace()
        }
    }

    private fun buildScanFilters(): List<ScanFilter> {
        if (scanFilterList == null) {
            scanFilterList = ArrayList()
            // 通过服务 uuid 过滤自己要连接的设备过滤器搜索GATT服务UUID
            scanFilterBuilder = ScanFilter.Builder()
            val parcelUuidMask = ParcelUuid.fromString("0000181F-0000-1000-8000-00805F9B34FB")
            val parcelUuid = ParcelUuid.fromString("00002AFF-0000-1000-8000-00805F9B34FB")
            //        scanFilterBuilder.setServiceUuid(parcelUuid, parcelUuidMask);
            scanFilterList?.add(scanFilterBuilder!!.build())
        }
        return scanFilterList!!
    }

    private fun buildScanSettings(): ScanSettings {
        if (scanSettingBuilder == null) {
            scanSettingBuilder = ScanSettings.Builder()
            //设置蓝牙LE扫描的扫描模式。
            //使用最高占空比进行扫描。建议只在应用程序处于此模式时使用此模式在前台运行
            scanSettingBuilder!!.setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
            //设置蓝牙LE扫描滤波器硬件匹配的匹配模式
            //在主动模式下，即使信号强度较弱，hw也会更快地确定匹配.在一段时间内很少有目击/匹配。
            scanSettingBuilder!!.setMatchMode(ScanSettings.MATCH_MODE_AGGRESSIVE)
            //设置蓝牙LE扫描的回调类型
            //为每一个匹配过滤条件的蓝牙广告触发一个回调。如果没有过滤器是活动的，所有的广告包被报告
            if (bluetoothAdapter.isOffloadedScanBatchingSupported) {
                //设置蓝牙LE扫描的报告延迟的时间（以毫秒为单位）
                //设置为0以立即通知结果
                scanSettingBuilder!!.setReportDelay(0L)
            }
            scanSettingBuilder!!.setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
        }
        return scanSettingBuilder!!.build()
    }

    override fun executeStartScan() {
        startBtScan(true)
    }

    override fun startBtScan(isPeriodic: Boolean) {
        val bluetoothAdapter = bluetoothAdapter
        if (bluetoothAdapter.bluetoothLeScanner == null) {
            return
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ActivityCompat.checkSelfPermission(
                    AidexxApp.instance,
                    Manifest.permission.BLUETOOTH_SCAN
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                eAiDEX("permission denied ----> Manifest.permission.BLUETOOTH_SCAN")
                return
            }
        }
        bluetoothAdapter.bluetoothLeScanner.startScan(buildScanFilters(), buildScanSettings(), scanCallback)
        if (!isOnConnectState && isPeriodic) {
            WorkManager.getInstance(AidexxApp.instance).cancelAllWorkByTag(STOP_SCAN.toString())
            if (instance().getDefault() != null) {
                val scanWorker: OneTimeWorkRequest =
                    OneTimeWorkRequest.Builder(StopScanWorker::class.java).setInitialDelay(30, TimeUnit.SECONDS).addTag(
                        STOP_SCAN.toString()
                    ).build() //30s以后停止扫描
                WorkManager.getInstance(AidexxApp.instance).enqueue(scanWorker)
            }
        }
    }

    override fun stopBtScan(isPeriodic: Boolean) {
        val bluetoothLeScanner = bluetoothAdapter.bluetoothLeScanner
        if (bluetoothLeScanner != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (ActivityCompat.checkSelfPermission(
                        AidexxApp.instance,
                        Manifest.permission.BLUETOOTH_SCAN
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    eAiDEX("permission denied---->Manifest.permission.BLUETOOTH_SCAN")
                    return
                }
            }
            bluetoothLeScanner.stopScan(scanCallback)
        }
        WorkManager.getInstance(AidexxApp.instance).cancelAllWorkByTag(START_SCAN.toString())
        val aDefault = instance().getDefault()
        if (aDefault != null && aDefault.entity.accessId != null && !isOnConnectState && isPeriodic) {
            val scanWorker: OneTimeWorkRequest =
                OneTimeWorkRequest.Builder(StartScanWorker::class.java).setInitialDelay(2, TimeUnit.SECONDS).addTag(
                    START_SCAN.toString()
                ).build() //2S后开启扫描
            WorkManager.getInstance(AidexxApp.instance).enqueue(scanWorker)
        }
    }

    override fun executeStopScan() {
        WorkManager.getInstance(AidexxApp.instance).cancelAllWorkByTag(STOP_SCAN.toString())
        WorkManager.getInstance(AidexxApp.instance).cancelAllWorkByTag(START_SCAN.toString())
        stopBtScan(true)
    }

    override fun isReadyToConnect(mac: String): Boolean {
        val result = bluetoothDeviceStore.deviceMap[mac]
        var bluetoothLeDevice: BluetoothDevice? = null
        if (result != null) {
            bluetoothLeDevice = result.device
        }
        eAiDEX("Device " + mac + " isReadyToConnect: " + (bluetoothLeDevice != null))
        return bluetoothLeDevice != null
    }

    override fun executeConnect(mac: String) {
        refreshConnectState(true)
        eAiDEX("Connecting to $mac")
        WorkManager.getInstance(AidexxApp.instance).cancelAllWorkByTag(START_SCAN.toString())
        val result = bluetoothDeviceStore.deviceMap[mac]
        if (result != null) {
            mBluetoothDevice = result.device
        }
        val duration = currentTimeMillis / 1000 - lastDisConnectTime
        if (duration >= TIME_BETWEEN_CONNECT) {
            workHandler!!.sendEmptyMessage(CONNECT_GATT)
        } else {
            workHandler!!.sendEmptyMessageDelayed(CONNECT_GATT, (TIME_BETWEEN_CONNECT - duration) * 1000)
        }
    }

    override fun executeDisconnect() {
        eAiDEX("Disconnecting")
        if (mBluetoothGatt != null) {
            workHandler!!.sendEmptyMessage(DISCONNECT_GATT)
        } else {
            eAiDEX("Gatt is null,call onDisconnected directly")
            onDisconnected()
        }
    }

    override fun executeWrite(data: ByteArray) {
        val message = Message.obtain()
        message.what = SEND_DATA
        message.obj = data
        workHandler!!.sendMessage(message)
    }

    override fun executeWriteCharacteristic(uuid: Int, data: ByteArray) {
        val message = Message.obtain()
        message.what = SEND_DATA
        message.obj = data
        message.arg1 = uuid
        workHandler!!.sendMessage(message)
    }

    override fun executeReadCharacteristic(uuid: Int) {
        val message = Message.obtain()
        message.what = READ_CHARACTERISTIC
        message.arg1 = uuid
        workHandler!!.sendMessage(message)
    }

    /**
     * 回调都是在子线程中，不可做更新 UI 操作
     */
    private val bluetoothGattCallback: BluetoothGattCallback = object : BluetoothGattCallback() {
        override fun onPhyUpdate(gatt: BluetoothGatt, txPhy: Int, rxPhy: Int, status: Int) {
            super.onPhyUpdate(gatt, txPhy, rxPhy, status)
        }

        override fun onPhyRead(gatt: BluetoothGatt, txPhy: Int, rxPhy: Int, status: Int) {
            super.onPhyRead(gatt, txPhy, rxPhy, status)
        }

        //status-->操作是否成功，如连接成功这个操作是否成功。会返回异常码
        //newState-->新的连接的状态。共四种：STATE_DISCONNECTED，STATE_CONNECTING，STATE_CONNECTED，STATE_DISCONNECTING
        override fun onConnectionStateChange(gatt: BluetoothGatt, status: Int, newState: Int) {
            super.onConnectionStateChange(gatt, status, newState)
            if (status == BluetoothGatt.GATT_SUCCESS) {
                when (newState) {
                    BluetoothProfile.STATE_DISCONNECTED -> {
                        workHandler!!.sendEmptyMessage(CLOSE_GATT)
                        lastDisConnectTime = currentTimeMillis / 1000
                        workHandler!!.sendEmptyMessageDelayed(CONNECT_DISCONNECTED, 2000)
                    }
                    BluetoothProfile.STATE_CONNECTED -> workHandler!!.sendEmptyMessage(DISCOVER_SERVICES)
                }
                retryNum = 0
                return
            }
            if (status == 257) {
                workHandler!!.sendEmptyMessage(CLOSE_GATT)
                workHandler!!.sendEmptyMessage(CONNECT_DISCONNECTED)
                send(EventBusKey.EVENT_RESTART_BLUETOOTH, true)
            }
            if (status == 133) {
                if (retryNum < 2) { //需要清除Gatt缓存并断开连接和关闭Gatt，然后重新连接
                    workHandler!!.sendEmptyMessage(CLOSE_GATT)
                    retry()
                } else {
                    retryNum = 0
                    workHandler!!.sendEmptyMessage(CLOSE_GATT)
                    workHandler!!.sendEmptyMessage(CONNECT_FAILURE)
                }
                return
            }
            workHandler!!.sendEmptyMessage(CLOSE_GATT)
            workHandler!!.sendEmptyMessage(CONNECT_FAILURE)
        }

        //发现服务成功后，会触发该回调方法。status：远程设备探索是否成功
        override fun onServicesDiscovered(gatt: BluetoothGatt, status: Int) {
            super.onServicesDiscovered(gatt, status)
            mBluetoothGatt = gatt
            if (status == BluetoothGatt.GATT_SUCCESS) {
                val message = Message.obtain()
                message.what = FOUND_SERVER
                message.obj = status
                workHandler!!.sendMessage(message)
            } else {
                workHandler!!.sendEmptyMessage(CONNECT_FAILURE)
            }
        }

        override fun onCharacteristicChanged(gatt: BluetoothGatt?, characteristic: BluetoothGattCharacteristic?) {
            super.onCharacteristicChanged(gatt, characteristic)
            eAiDEX("onCharacteristicChanged --> " + binaryToHexString(characteristic?.value))
            if (mBluetoothGatt == null) {
                eAiDEX("onCharacteristicChanged --> Gatt is null")
                return
            }
            val message = Message.obtain()
            message.what = RECEIVER_DATA
            message.obj = characteristic?.value
            message.arg1 = characteristic?.uuid?.toIntBigEndian()!!
            workHandler!!.sendMessage(message)
        }

        @SuppressLint("MissingPermission")
        override fun onCharacteristicWrite(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            super.onCharacteristicWrite(gatt, characteristic, status)
            if (status == BluetoothGatt.GATT_SUCCESS) {
                eAiDEX("Characteristic write success --> uuid:${characteristic.uuid}")
            } else eAiDEX("Send data fail")
        }

        override fun onCharacteristicChanged(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray
        ) {
            super.onCharacteristicChanged(gatt, characteristic, value)
            eAiDEX("onCharacteristicChanged --> " + binaryToHexString(value))
            if (mBluetoothGatt == null) {
                eAiDEX("onCharacteristicChanged --> Gatt is null")
                return
            }
            val message = Message.obtain()
            message.what = RECEIVER_DATA
            message.obj = value
            message.arg1 = characteristic.uuid.toIntBigEndian()
            workHandler!!.sendMessage(message)
        }

        override fun onCharacteristicRead(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            value: ByteArray,
            status: Int
        ) {
            super.onCharacteristicRead(gatt, characteristic, value, status)
            eAiDEX("onDescriptorRead -->" + "status:" + status + " uuid" + characteristic.uuid)
            val message = Message.obtain()
            message.what = RECEIVER_DATA
            message.obj = value
            message.arg1 = characteristic.uuid.toIntBigEndian()
            workHandler!!.sendMessage(message)
        }

        @Deprecated("Deprecated in Java")
        override fun onCharacteristicRead(
            gatt: BluetoothGatt?,
            characteristic: BluetoothGattCharacteristic?,
            status: Int
        ) {
            super.onCharacteristicRead(gatt, characteristic, status)
            eAiDEX("onDescriptorRead -->" + "status:" + status + " uuid" + characteristic?.uuid)
            val message = Message.obtain()
            message.what = RECEIVER_DATA
            message.obj = characteristic?.value
            message.arg1 = characteristic?.uuid?.toIntBigEndian() ?: 0
            workHandler!!.sendMessage(message)
        }

        //设置Descriptor后回调
        override fun onDescriptorWrite(gatt: BluetoothGatt, descriptor: BluetoothGattDescriptor, status: Int) {
            super.onDescriptorWrite(gatt, descriptor, status)
            if (status == BluetoothGatt.GATT_SUCCESS) {
                eAiDEX("onDescriptorWrite -->" + "Descriptor enable success. uuid:" + descriptor.characteristic.uuid)
//                if (descriptor.characteristic == mCharacteristic) {
                workHandler!!.sendEmptyMessage(CONNECT_SUCCESS)
//                }
            } else {
                workHandler!!.sendEmptyMessage(CLOSE_GATT)
                workHandler!!.sendEmptyMessage(CONNECT_FAILURE)
                eAiDEX("onDescriptorWrite -->" + "Descriptor enable fail")
            }
        }

        override fun onReliableWriteCompleted(gatt: BluetoothGatt, status: Int) {
            super.onReliableWriteCompleted(gatt, status)
        }

        override fun onReadRemoteRssi(gatt: BluetoothGatt, rssi: Int, status: Int) {
            super.onReadRemoteRssi(gatt, rssi, status)
        }

        override fun onMtuChanged(gatt: BluetoothGatt, mtu: Int, status: Int) {
            super.onMtuChanged(gatt, mtu, status)
        }
    }

    private fun retry() {
        retryNum++
        workHandler!!.sendEmptyMessage(CONNECT_GATT)
    }

    companion object {
        private const val TIME_BETWEEN_CONNECT = 2L //断开到连接的时间间隔
        private const val DISCOVER_TIME_OUT_SECONDS = 30
        private val UUID_SERVICE = UUID.fromString("0000181F-0000-1000-8000-00805F9B34FB")
        private val UUID_CHARACTERISTIC = UUID.fromString("00002AFF-0000-1000-8000-00805F9B34FB")
        private const val LINK_TIME_OUT = 1000
        const val START_SCAN = 1001
        private const val STOP_SCAN = 1002
        private const val CONNECT_GATT = 1003
        private const val DISCOVER_SERVICES = 1004
        private const val DISCONNECT_GATT = 1005
        private const val CLOSE_GATT = 1006
        private const val SEND_DATA = 1007
        private const val CLOSE_GATT_ONLY = 1011
        private const val RECEIVER_DATA = 2000 //处理返回的数据
        private const val CONNECT_DISCONNECTED = 1008
        private const val CONNECT_FAILURE = 1009
        private const val CONNECT_SUCCESS = 1010
        private const val FOUND_SERVER = 1012
        private const val READ_CHARACTERISTIC = 1013
        private const val BLE_CONNECT_TIME_OUT = 1100 //连接超时
        private const val BLE_CONNECT_TIME_LIMIT = (30 * 1000 //连接超时30S
                ).toLong()

        fun init(context: Context) {
            if (instance == null) {
                instance = AidexBleAdapter().initialize(context)
            }
        }

        fun getInstance(): BleAdapter {
            return instance
        }
    }
}