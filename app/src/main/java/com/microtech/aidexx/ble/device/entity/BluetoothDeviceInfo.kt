package com.microtech.aidexx.ble.device.entity

import android.bluetooth.BluetoothDevice

/**
 *@date 2023/3/8
 *@author Hugh
 *@desc
 */
data class BluetoothDeviceInfo(
    var name: String? = null,
    var sn: String? = null,
    var rssi: Int = 0,
    var device: BluetoothDevice? = null
)

