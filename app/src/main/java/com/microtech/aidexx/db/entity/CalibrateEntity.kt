package com.microtech.aidexx.db.entity

import android.content.res.Resources
import com.microtech.aidexx.utils.EncryptUtils
import io.objectbox.annotation.*
import java.util.*
import kotlin.jvm.Transient

/**
 * 校准记录
 *
 * */
@Entity
class CalibrateEntity : BaseEventEntity() {
    var calTime: Date = Date()

    @Transient
    override var language: String = ""

    var deviceId: String = ""
    var eventIndex = 0

    @Unique(onConflict = ConflictStrategy.REPLACE)
    var calibrationId: String? = null
    var sensorIndex: Int = 0

    @Index(type = IndexType.HASH)
    var sensorId: String? = null
    var referenceGlucose: Float = 0f
    var indexBeforeCal: Int = 0
    var indexAfterCal: Int = 0
    var cf: Float = 1f //校准系数
    var offset: Float = 0f //校准偏移量
    var isValid = 0
    var index = 0
    var autoIncrementColumn = 0

    @Index
    var timeOffset = 0

    fun updateCalibrationId(): String {
        val uuidStr = StringBuffer()
        uuidStr.append(sensorId)
            .append(index)
        return EncryptUtils.md5(uuidStr.toString())
    }

    override fun getEventDescription(res: Resources): String {
        return ""
    }

    override fun getValueDescription(res: Resources): String {
        return ""
    }

    override fun toString(): String {
        return "CalibrateEntity(idx=$idx, authorizationId=$userId, calTime=$calTime, id=$id, deviceId='$deviceId', recordIndex=$recordIndex, calibrationId=$calibrationId, sensorIndex=$sensorIndex, referenceGlucose=$referenceGlucose, indexBeforeCal=$indexBeforeCal, indexAfterCal=$indexAfterCal, calFactor=$cf, calOffset=$offset)"
    }
}