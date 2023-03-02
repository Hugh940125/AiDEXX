package com.microtech.aidexx.ble.device.handle

import com.microtech.aidexx.ble.device.*
import com.microtech.aidexx.common.user.UserInfoManager
import com.microtech.aidexx.db.ObjectBox
import com.microtech.aidexx.db.entity.CgmHistoryEntity
import com.microtech.aidexx.db.entity.CgmHistoryEntity_
import com.microtech.aidexx.ui.alert.AlertManager
import com.microtech.aidexx.ui.alert.AlertType
import com.microtech.aidexx.utils.LogUtil
import com.microtech.aidexx.utils.TimeUtils
import com.microtech.aidexx.utils.TimeUtils.dateHourMinute
import com.microtech.aidexx.utils.mmkv.MmkvManager
import com.microtech.aidexx.widget.dialog.x.util.roundOffDecimal
import com.microtechmd.blecomm.constant.History
import com.microtechmd.blecomm.parser.AidexXBroadcastEntity
import com.microtechmd.blecomm.parser.AidexXHistoryEntity
import com.microtechmd.blecomm.parser.AidexXParser
import io.objectbox.kotlin.equal
import io.objectbox.query.QueryBuilder
import java.util.*

/**
 *@date 2023/2/28
 *@author Hugh
 *@desc
 */
class AdxHandler {
    private var briefRangeStartIndex: Int = 0
    private var newestIndex: Int = 0
    private var rawRangeStartIndex: Int = 0
    private val typeHyperAlert = 1
    private val typeHypoAlert = 2
    private val typeUrgentAlert = 3
    private lateinit var transmitterModel: TransmitterModel
    private var lastHyperAlertTime: Long? = null
    private var lastHypoAlertTime: Long? = null
    private var lastUrgentAlertTime: Long? = null
    private val tempList = mutableListOf<CgmHistoryEntity>()

    fun handleAdvertise(model: TransmitterModel, data: ByteArray) {
        transmitterModel = model
        if (model.entity.sensorStartTime == null) {
            model.controller.startTime
            return
        }
        val broadcast = AidexXParser.getBroadcast<AidexXBroadcastEntity>(data)
        model.isSensorExpired =
            (broadcast.status == History.SESSION_STOPPED && broadcast.calTemp != History.TIME_SYNCHRONIZATION_REQUIRED)
        model.isMalfunction =
            broadcast.status == History.SENSOR_MALFUNCTION || broadcast.status == History.DEVICE_SPECIFIC_ALERT || broadcast.status == History.GENERAL_DEVICE_FAULT
        if (broadcast.status == History.SENSOR_MALFUNCTION) {
            model.faultType = 1
        } else if (broadcast.status == History.GENERAL_DEVICE_FAULT) {
            model.faultType = 2
        }
        val adHistories = broadcast.history
        if (adHistories.isNotEmpty()) {
            model.latestHistory = adHistories[0]
        } else {
            return
        }
        model.isHistoryValid =
            model.latestHistory?.isValid == 1 && model.latestHistory?.status == History.STATUS_OK
        val now = TimeUtils.currentTimeMillis
        model.latestAdTime = now
        if (UserInfoManager.shareUserInfo != null) {
            LogUtil.eAiDEX("view sharing")
            return
        }
        val temp = broadcast.history[0].glucose
        model.glucose = if (model.isMalfunction || model.isSensorExpired || temp < 0) null
        else roundOffDecimal(temp / 18f)
        model.glucoseLevel = model.getGlucoseLevel(model.glucose)
        model.latestHistory?.let {
            val historyDate = getHistoryDate(it.timeOffset)
            if (model.glucose != null && model.lastHistoryTime != historyDate) {
                model.lastHistoryTime = historyDate
            }
        }
        model.targetEventIndex = model.latestHistory?.timeOffset ?: 0
        if (model.nextEventIndex <= model.targetEventIndex) {
            val broadcastContainsNext = isNextInBroadcast(model.nextEventIndex, adHistories)
            if (broadcastContainsNext) {
                val historiesFromBroadcast =
                    getHistoriesFromBroadcast(model.nextEventIndex, adHistories)
                saveHistories(historiesFromBroadcast.reversed())
            } else {
                model.latestHistory?.let {
                    if (model.targetEventIndex > model.nextEventIndex) {
                        if (newestIndex == model.targetEventIndex) {
                            if (model.nextEventIndex < briefRangeStartIndex) {
                                model.nextEventIndex = briefRangeStartIndex
                            }
                            model.controller.getHistories(model.nextEventIndex)
                        } else {
                            model.controller.historyRange
                        }
                    }
                }
            }
            return
        }
        val numGetHistory = 40
        if (model.targetEventIndex >= model.nextFullEventIndex + numGetHistory
            || ((model.targetEventIndex >= model.nextFullEventIndex) && model.isSensorExpired)) {
            if (newestIndex == model.targetEventIndex) {
                if (model.nextFullEventIndex < rawRangeStartIndex) {
                    model.nextFullEventIndex = rawRangeStartIndex
                }
                model.controller.getRawHistories(model.nextFullEventIndex)
            } else {
                model.controller.historyRange
            }
        }
    }

    private fun isNextInBroadcast(next: Int, histories: List<AidexXHistoryEntity>): Boolean {
        return next in histories[histories.size - 1].timeOffset..histories[0].timeOffset
    }

    private fun getHistoriesFromBroadcast(
        next: Int, list: MutableList<AidexXHistoryEntity>
    ): List<AidexXHistoryEntity> {
        var startIndex = 0
        for ((index, history) in list.withIndex()) {
            if (history.timeOffset == next) {
                startIndex = index + 1
                break
            }
        }
        return list.subList(0, startIndex)
    }

    private fun saveHistories(histories: List<AidexXHistoryEntity>) {
        tempList.clear()
        val entity = transmitterModel.entity
        val deviceId = TransmitterManager.instance().getDefault()?.deviceId() ?: return
        val userId = UserInfoManager.instance().userId()
        if (userId.isEmpty()) {
            return
        }
        ObjectBox.runAsync({
            val now = TimeUtils.currentTimeMillis
            for (history in histories) {
                val oldHistory = ObjectBox.cgmHistoryBox!!.query().equal(
                    CgmHistoryEntity_.sensorIndex,
                    entity.sensorStartTime!!.time.toInt()
                ).equal(CgmHistoryEntity_.eventIndex, history.timeOffset).equal(
                    CgmHistoryEntity_.deviceId,
                    deviceId,
                    QueryBuilder.StringOrder.CASE_INSENSITIVE
                ).equal(
                    CgmHistoryEntity_.authorizationId,
                    userId,
                    QueryBuilder.StringOrder.CASE_INSENSITIVE
                ).orderDesc(CgmHistoryEntity_.idx).build().findFirst()

                if (oldHistory != null) {
                    LogUtil.eAiDEX("History exist,need not update}")
                    continue
                }
                val time = getHistoryDate(history.timeOffset).dateHourMinute()
                val historyEntity = CgmHistoryEntity()
                historyEntity.deviceId = deviceId
                if (history.isValid == 0) {
                    historyEntity.eventType = History.HISTORY_INVALID
                } else {
                    when (history.status) {
                        History.STATUS_OK -> {
                            historyEntity.eventType = History.HISTORY_GLUCOSE
                        }
                        History.STATUS_INVALID -> {
                            historyEntity.eventType = History.HISTORY_GLUCOSE_INVALID
                        }
                        History.STATUS_ERROR -> {
                            historyEntity.eventType = History.HISTORY_SENSOR_ERROR
                        }
                    }
                }
                historyEntity.eventData = roundOffDecimal(history.glucose / 18f)
                historyEntity.eventIndex = history.timeOffset
                historyEntity.time = getHistoryDate(history.timeOffset)
                historyEntity.deviceTime = getHistoryDate(history.timeOffset)
                historyEntity.sensorIndex =
                    (entity.sensorStartTime?.time!!).toInt()
                if (history.timeOffset <= 60) {
                    historyEntity.eventWarning = -1
                }
                when (historyEntity.eventType) {
                    History.HISTORY_GLUCOSE,
                    -> {
                        if (transmitterModel.isMalfunction || historyEntity.eventWarning == -1) {
                            historyEntity.eventWarning = -1
                        } else {
                            if (historyEntity.isHighOrLow()) {
                                when {
                                    historyEntity.getHighOrLowGlucoseType() == History.HISTORY_LOCAL_HYPER -> {
                                        if (MmkvManager.isHyperAlertEnable()) if (lastHyperAlertTime == null) {
                                            lastHyperAlertTime = getLastAlert(
                                                deviceId, userId, typeHyperAlert
                                            )
                                        }
                                        if (lastHyperAlertTime == null || historyEntity.deviceTime.time - lastHyperAlertTime!!
                                            > AlertManager.calculateFrequency(MmkvManager.getAlertFrequency())
                                        ) {
                                            historyEntity.updateEventWarning()
                                            TransmitterModel.alert?.invoke(
                                                "$time", AlertType.MESSAGE_TYPE_GLUCOSEHIGH
                                            )
                                            lastHyperAlertTime = historyEntity.deviceTime.time
                                        }
                                    }
                                    historyEntity.getHighOrLowGlucoseType() == History.HISTORY_LOCAL_HYPO -> {
                                        if (MmkvManager.isHypoAlertEnable()) if (lastHypoAlertTime == null) {
                                            lastHypoAlertTime = getLastAlert(
                                                deviceId, userId, typeHypoAlert
                                            )
                                        }
                                        if (lastHypoAlertTime == null || historyEntity.deviceTime.time - lastHypoAlertTime!!
                                            > AlertManager.calculateFrequency(MmkvManager.getAlertFrequency())
                                        ) {
                                            historyEntity.updateEventWarning()
                                            TransmitterModel.alert?.invoke(
                                                "$time", AlertType.MESSAGE_TYPE_GLUCOSELOW
                                            )
                                            lastHypoAlertTime = historyEntity.deviceTime.time
                                        }
                                    }
                                    historyEntity.getHighOrLowGlucoseType() == History.HISTORY_LOCAL_URGENT_HYPO -> {
                                        if (MmkvManager.isHypoAlertEnable()) if (lastUrgentAlertTime == null) {
                                            lastUrgentAlertTime = getLastAlert(
                                                deviceId, userId, typeUrgentAlert
                                            )
                                        }
                                        if (lastUrgentAlertTime == null || historyEntity.deviceTime.time - lastUrgentAlertTime!!
                                            > AlertManager.calculateFrequency(MmkvManager.getUrgentAlertFrequency())
                                        ) {
                                            historyEntity.updateEventWarning()
                                            TransmitterModel.alert?.invoke(
                                                "$time", AlertType.MESSAGE_TYPE_GLUCOSELOWALERT
                                            )
                                            lastUrgentAlertTime = historyEntity.deviceTime.time
                                        }
                                    }
                                }
                            }
                        }
                    }
                    History.HISTORY_SENSOR_ERROR -> {
                        if (now - historyEntity.deviceTime.time < 1000 * 60 * 30 && entity.needReplace) {
                            TransmitterModel.alert?.invoke(
                                "$time", AlertType.MESSAGE_TYPE_SENRORERROR
                            )
                        }
                    }
                }
                historyEntity.authorizationId = userId
                historyEntity.updateRecordUUID()
                ObjectBox.cgmHistoryBox!!.put(historyEntity)
                tempList.add(historyEntity)
            }
        }, onSuccess = {
            if (UserInfoManager.instance().isLogin()) {
                transmitterModel.cgmHistories.addAll(tempList)
                if (UserInfoManager.shareUserInfo == null) {
                    TransmitterManager.instance().updateHistories(tempList)
                }
                entity.eventIndex = tempList.last().eventIndex
                transmitterModel.nextEventIndex = entity.eventIndex + 1
                ObjectBox.transmitterBox!!.put(entity)
                transmitterModel.updateGlucoseTrend(tempList.last().deviceTime)
            }
        })
    }

    private fun getLastAlert(deviceId: String, userId: String, type: Int): Long? {
        val build = ObjectBox.cgmHistoryBox!!.query().equal(
            CgmHistoryEntity_.sensorIndex, transmitterModel.entity.sensorIndex
        ).equal(
            CgmHistoryEntity_.deviceId, deviceId, QueryBuilder.StringOrder.CASE_INSENSITIVE
        ).equal(
            CgmHistoryEntity_.authorizationId, userId, QueryBuilder.StringOrder.CASE_INSENSITIVE
        )
        when (type) {
            1 -> build.equal(
                CgmHistoryEntity_.eventWarning, History.HISTORY_LOCAL_HYPER
            )
            2 -> build.equal(
                CgmHistoryEntity_.eventWarning, History.HISTORY_LOCAL_HYPO
            )

            3 -> build.equal(
                CgmHistoryEntity_.eventWarning, History.HISTORY_LOCAL_URGENT_HYPO
            )
        }
        val lastAlert = build.orderDesc(CgmHistoryEntity_.idx).build().findFirst()
        return lastAlert?.deviceTime?.time
    }

    private fun getHistoryDate(timeOffset: Int): Date {
        val timeLong = transmitterModel.entity.sensorStartTime?.time?.plus(timeOffset * 60 * 1000)
        return Date(timeLong!!)
    }
}