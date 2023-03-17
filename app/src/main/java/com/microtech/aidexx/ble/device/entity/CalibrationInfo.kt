package com.microtech.aidexx.ble.device.entity

/**
 *@date 2023/3/17
 *@author Hugh
 *@desc
 */
class CalibrationInfo {
    var floatValue: Float? = 0F
    var intValue: Int = 0
    var time: Long = 0L
    var timeOffset: Int = 0

    constructor(floatValue: Float?, time: Long) {
        this.floatValue = floatValue
        this.time = time
    }

    constructor(intValue: Int, timeOffset: Int) {
        this.intValue = intValue
        this.timeOffset = timeOffset
    }
}
