package com.microtech.aidexx.ble.device.entity

import java.util.*

/**
 *@date 2023/4/28
 *@author Hugh
 *@desc
 */
data class DeviceRegisterInfo(
    val record: RecordInfo?,
    val originRecord: RawRecordInfo?,
    val calibrationRecord: CalibrationRecord?,
    val deviceId: String?
)

data class RecordInfo(val timeOffset: Int = 0)

data class RawRecordInfo(val timeOffset: Int = 0)

data class CalibrationRecord(val timeOffset: Int = 0)

data class CloudDeviceInfo(
    val deviceInfo: DeviceInfo?,
    val record: RecordInfo?,
    val originRecord: RawRecordInfo?,
    val calibrationRecord: CalibrationRecord?
)

data class DeviceInfo(
    val deviceId: String?,
    val userId: String?,
    val sensorId: String?,
    val sensorIndex: Int = 0,
    val sensorStartUp: Date?,
    val startUpTimeZone: String?,
    val deviceModel: Int = 0,
    val deviceSn: String?,
    val deviceMac: String?,
    val deviceKey: String?,
    val registerTime: Date?,
    val unregisterTime: Date?,
    val et: Int = 0
)