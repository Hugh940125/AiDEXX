package com.microtech.aidexx.db.entity

import android.content.res.Resources
import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import java.util.*

/**
 * 校准记录
 *
 * */
@Entity
class CalibrateEntity : EventEntity {
    @Id
    override var idx: Long? = null
    override var state: Int = 0
    override var userId: String? = null
    var calTime: Date = Date()
    override var createTime: Date = Date()
    override var recordId: String? = null

    @Transient
    override var language: String = ""
    override var uploadState: Int = 0

    @Transient
    override var time: Date = calTime
        get() {
            return calTime
        }
        set(time) {
            field = time
            calTime = time
        }
    override var id: String? = null
    var deviceId: String = ""
    var eventIndex = 0
    override var recordIndex: Long? = 0L
    override var deleteStatus: Int = 0
    var recordUuid: String? = UUID.randomUUID().toString().replace("-", "")
    var sensorIndex: Int = 0

    var referenceGlucose: Float = 0f
    var indexBeforeCal: Int = 0
    var indexAfterCal: Int = 0
    var calFactor: Float = 1f //校准系数
    var calOffset: Float = 0f //校准偏移量
    var isValid = false
    override fun getEventDescription(res: Resources): String {
        return ""
    }

    override fun getValueDescription(res: Resources): String {
        return ""
    }

    override fun toString(): String {
        return "CalibrateEntity(idx=$idx, authorizationId=$userId, calTime=$calTime, id=$id, deviceId='$deviceId', recordIndex=$recordIndex, recordUuid=$recordUuid, sensorIndex=$sensorIndex, referenceGlucose=$referenceGlucose, indexBeforeCal=$indexBeforeCal, indexAfterCal=$indexAfterCal, calFactor=$calFactor, calOffset=$calOffset)"
    }
}