package com.microtech.aidexx.db.entity

import android.content.res.Resources
import com.microtech.aidexx.R
import com.microtech.aidexx.ble.device.TransmitterManager
import com.microtech.aidexx.common.user.UserInfoManager
import com.microtech.aidexx.utils.EncryptUtils
import com.microtech.aidexx.utils.ThresholdManager
import com.microtech.aidexx.utils.UnitManager
import com.microtech.aidexx.widget.dialog.x.util.toGlucoseValue
import com.microtechmd.blecomm.constant.History
import com.microtechmd.blecomm.parser.CgmHistoryEntity
import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import java.util.*

@Entity
class CgmHistoryEntity : EventEntity,
    CgmHistoryEntity {
    var eventWarning: Int? = null

    @Id
    override var idx: Long? = null
    override var state: Int = 0
    override var id: String? = null
    var deviceSn: String? = null
    var deviceTime = Date()
    var eventIndex = 0
    var sensorIndex = 0
    var dataStatus = 0 // 0，原始数据，1代表待上传 2代表已上传
    override var recordIndex: Long? = null
    override var deleteStatus: Int = 0
    var eventType: Int = History.HISTORY_INVALID
    var eventData: Float? = null
    var deviceId: String? = null
    var type = 0 // type为0正常数据，1代表占位数据
    override var authorizationId: String? = null
    var recordUuid: String? = null

    fun updateRecordUUID() {
        val uuidStr = StringBuffer()
        uuidStr.append(authorizationId)
            .append(deviceId)
            .append(deviceTime.time / 1000)
            .append(sensorIndex)
            .append(eventIndex)
            .append(eventType)
        recordUuid = EncryptUtils.md5(uuidStr.toString())
    }

    override fun _setDatetime(datetime: Long) {
        this.deviceTime = Date(datetime * 1000)
    }

    override fun _setEventIndex(eventIndex: Int) {
        this.eventIndex = eventIndex
    }

    override fun _setSensorIndex(sensorIndex: Int) {
        this.sensorIndex = sensorIndex
    }

    override fun _setEventType(eventType: Int) {
        this.eventType = eventType
    }

    override fun _setEventValue(eventValue: Float) {
        this.eventData = eventValue
    }

    var rawData1: Float? = null
    var rawData2: Float? = null
    var rawData3: Float? = null
    var rawData4: Float? = null
    var rawData5: Float? = null
    var rawData6: Float? = null
    var rawData7: Float? = null
    var rawData8: Float? = null
    var rawData9: Float? = null

    override fun _setRawValue(rawValue: FloatArray) {
        rawData1 = rawValue[0]
        rawData2 = rawValue[1]
        rawData3 = rawValue[2]
        rawData4 = rawValue[3]
        rawData5 = rawValue[4]
        rawData6 = rawValue[5]
        rawData7 = rawValue[6]
        rawData8 = rawValue[7]
        rawData9 = rawValue[8]

    }


    @Transient
    override var time: Date = deviceTime
        get() {
            return deviceTime
        }
        set(time) {
            field = time
            deviceTime = time
        }

    override fun getEventDescription(res: Resources): String {
        return when (eventWarning) {
            History.HISTORY_LOCAL_HYPER -> res.getString(R.string.hyper_item)
            History.HISTORY_LOCAL_HYPO -> res.getString(R.string.hypo_item)
            History.HISTORY_BLOOD_GLUCOSE -> res.getString(R.string.bg_title)
            History.HISTORY_LOCAL_URGENT_HYPO -> res.getString(R.string.Urgent_Low_Alarm)
            0, null -> when (eventType) {
                History.HISTORY_HYPER -> res.getString(R.string.hyper_item)
                History.HISTORY_HYPO -> res.getString(
                    R.string.hypo_item
                )
                else -> ""
            }
            else -> ""
        }
    }


    override fun getValueDescription(res: Resources): String {
        return when (eventWarning) {
            History.HISTORY_LOCAL_HYPER -> res.getString(
                R.string.hyper_description,
                eventData?.toGlucoseValue()
            ) + UnitManager.glucoseUnit.text
            History.HISTORY_LOCAL_HYPO -> res.getString(
                R.string.hypo_description,
                eventData?.toGlucoseValue()
            ) + UnitManager.glucoseUnit.text
            History.HISTORY_BLOOD_GLUCOSE -> res.getString(
                R.string.bg_description,
                eventData?.toGlucoseValue()
            ) + UnitManager.glucoseUnit.text
            0, null -> when (eventType) {
                History.HISTORY_HYPER -> res.getString(
                    R.string.hyper_description,
                    eventData?.toGlucoseValue()
                ) + UnitManager.glucoseUnit.text
                History.HISTORY_HYPO -> res.getString(
                    R.string.hypo_description,
                    eventData?.toGlucoseValue()
                ) + UnitManager.glucoseUnit.text
                else -> ""
            }
            else -> ""
        }
    }

    fun isHighOrLow(): Boolean {
        eventData?.let {
            if (it > ThresholdManager.hyper) {
                return true
            } else if (it < ThresholdManager.hypo) {
                return true
            }
        }
        return false
    }


    fun getHighOrLowGlucoseType(): Int {
        eventData?.let {
            if (it > ThresholdManager.hyper) {
                return 2
            } else if (it < ThresholdManager.hypo && it >= ThresholdManager.URGENT_HYPO) {
                return 1
            } else if (it < ThresholdManager.URGENT_HYPO) {
                return 3
            }
        }
        return 0
    }


    fun updateEventWarning() {
        eventData?.let {
            eventWarning = History.HISTORY_LOCAL_NORMAL
            if (it > ThresholdManager.hyper) {
                eventWarning = History.HISTORY_LOCAL_HYPER
            } else if (it < ThresholdManager.hypo && it >= ThresholdManager.URGENT_HYPO) {
                eventWarning = History.HISTORY_LOCAL_HYPO
            } else if (it < ThresholdManager.URGENT_HYPO) {
                eventWarning = History.HISTORY_LOCAL_URGENT_HYPO
            }
        }
    }

    override fun toString(): String {
        return "CgmHistoryEntity(eventWarning=$eventWarning, idx=$idx, state=$state, id=$id, deviceSn=$deviceSn, deviceTime=$deviceTime, eventIndex=$eventIndex, sensorIndex=$sensorIndex, dataStatus=$dataStatus, recordIndex=$recordIndex, deleteStatus=$deleteStatus, eventType=$eventType, eventData=$eventData, deviceId=$deviceId, type=$type, authorizationId=$authorizationId, recordUuid=$recordUuid, rawData1=$rawData1, rawData2=$rawData2, rawData3=$rawData3, rawData4=$rawData4, rawData5=$rawData5, rawData6=$rawData6, rawData7=$rawData7, rawData8=$rawData8, rawData9=$rawData9)"
    }
}