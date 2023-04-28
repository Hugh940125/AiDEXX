package com.microtech.aidexx.ble.device.model

/**
 *@date 2023/4/28
 *@author Hugh
 *@desc
 */
data class DeviceRegisterInfo(val record: RecordInfo, val originRecord: RawRecordInfo) {
    data class RecordInfo(val timeOffset: Int = 0)
    data class RawRecordInfo(val timeOffset: Int = 0)
}