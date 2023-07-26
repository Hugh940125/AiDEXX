package com.microtech.aidexx.db.entity

import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import java.util.Date

/**
 *@date 2023/7/26
 *@author Hugh
 *@desc
 */
@Entity
class HistoryDeviceInfo {
    @Id
    var idx: Long = 0
    var deviceId: String? = null
    var userId: String? = null
    var sensorId: String? = null
    var sensorIndex: Int = 0
    var sensorStartUp: Date? = null
    var startUpTimeZone: String? = null
    var deviceModel: Int = 0
    var deviceSn: String? = null
    var deviceMac: String? = null
    var deviceKey: String? = null
    var registerTime: Date? = null
    var unregisterTime: Date? = null
    var et: Int = 0
}