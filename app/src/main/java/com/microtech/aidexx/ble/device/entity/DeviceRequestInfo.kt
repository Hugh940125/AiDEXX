package com.microtech.aidexx.ble.device.entity

import com.microtech.aidexx.db.entity.HistoryDeviceInfo

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

data class CalibrationRecord(val index: Int = 0)

data class CloudDeviceInfo(
    val deviceInfo: HistoryDeviceInfo?,
    val record: RecordInfo?,
    val originRecord: RawRecordInfo?,
    val calibrationRecord: CalibrationRecord?
)