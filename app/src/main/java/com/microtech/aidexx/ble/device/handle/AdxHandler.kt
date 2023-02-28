package com.microtech.aidexx.ble.device.handle

import com.microtech.aidexx.ble.device.*
import com.microtech.aidexx.common.user.UserInfoManager
import com.microtech.aidexx.db.ObjectBox
import com.microtech.aidexx.db.entity.CgmHistoryEntity
import com.microtech.aidexx.db.entity.CgmHistoryEntity_
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
import com.tencent.mmkv.MMKV
import io.objectbox.kotlin.equal
import io.objectbox.query.QueryBuilder
import okhttp3.internal.notify
import java.util.*

/**
 *@date 2023/2/28
 *@author Hugh
 *@desc
 */
class AdxHandler {

    private lateinit var transmitterModel: TransmitterModel
    private var lastAlertTime: Long? = null
    private var lastUrgentAlertTime: Long? = null
    private var typeAlert = 1
    private var typeUrgentAlert = 2

    fun handleAdvertise(model: TransmitterModel, data: ByteArray) {
        transmitterModel = model
        if (model.entity.sensorStartTime == null) {
            model.controller.startTime
            return
        }
        val broadcast = AidexXParser.getBroadcast<AidexXBroadcastEntity>(data)
        model.isSensorExpired =
            (broadcast.status == History.SESSION_STOPPED && broadcast.calTemp != History.TIME_SYNCHRONIZATION_REQUIRED)
        model.isDeviceFault = broadcast.status == History.SENSOR_MALFUNCTION
                || broadcast.status == History.DEVICE_SPECIFIC_ALERT
                || broadcast.status == History.GENERAL_DEVICE_FAULT
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
            LogUtil.eAiDEX("")
            return
        }
        val temp = broadcast.history[0].glucose
        model.glucose =
            if (model.isDeviceFault || model.isSensorExpired || temp < 0) null
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
//            if (!FastUtil.isFastGet3()) {
                LogUtils.eAiDex("开始获取 histories[0].timeOffset ${adHistories[0].timeOffset}")
                if (adHistories[0].timeOffset > nextEventIndex) {
                    LogUtils.eAiDex("开始获取简要数据1 newestIndex ${newestIndex} targetEventIndex ${targetEventIndex} nextEventIndex ${nextEventIndex}")
                    CgmsApplication.isCgmPairing = false
                    if (newestIndex == targetEventIndex) {
                        if (nextEventIndex < briefIndex) {
                            nextEventIndex = briefIndex
                        }
                        LogUtils.eAiDex("开始获取简要数据2 expired ${expired} targetIndex ${targetEventIndex} nextEventIndex ${nextEventIndex}")
                        controller.getHistories(nextEventIndex)
                    } else {
                        controller.historyRange
                    }
                }
            }
            return
        }
//
        val numGetHistory = 30//if (errorSensorTimeRange()) 6 else 40
//            when {
//                //当最新的事件 大于  下一个完整数据事件 则获取下一个完整事件
//        if (!FastUtil.isFastGet4()) {
        if (
            targetEventIndex >= nextFullEventIndex + numGetHistory || ((targetEventIndex >= nextFullEventIndex) && expired)
        ) {
            if (newestIndex == targetEventIndex) {
                if (nextFullEventIndex < rawIndex) {
                    nextFullEventIndex = rawIndex
                }
                controller.getRawHistories(nextFullEventIndex)
            } else {
                controller.historyRange
            }
            LogUtils.data("开始获取原始数据 expired ${expired} targetIndex ${targetEventIndex} nextFullIndex ${nextFullEventIndex}")
        }
//        }
//                expired -> {
//                    nextHistoryTime = lastAdvertiseTime + 300L
//                }
//                else -> {
//                    val waitSeconds = history.deviceTime.time / 1000 + 300 - Date().time / 1000
//                    if (waitSeconds > 10) {
//                        nextHistoryTime = history.deviceTime.time / 1000 + 300L
//                    }
//                }
//            }
//        }
    }

    private fun isNextInBroadcast(next: Int, histories: List<AidexXHistoryEntity>): Boolean {
        return next in histories[histories.size - 1].timeOffset..histories[0].timeOffset
    }

    private fun getHistoriesFromBroadcast(
        next: Int,
        list: MutableList<AidexXHistoryEntity>
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
        transmitterModel.entity.eventIndex = histories.last().timeOffset
        transmitterModel.nextEventIndex = transmitterModel.entity.eventIndex + 1
        val mutableListOf = mutableListOf<CgmHistoryEntity>()
        val deviceId = TransmitterManager.instance().getDefault().deviceId() ?: return
        val userId = UserInfoManager.instance().userId()
        if (userId.isEmpty()) {
            return
        }
        CgmsApplication.boxStore.runInTxAsync({
            val now = TimeUtils.currentTimeMillis
            for (history in histories) {
                val oldHistory = ObjectBox.cgmHistoryBox!!.query()
                    .equal(
                        CgmHistoryEntity_.sensorIndex,
                        transmitterModel.entity.sensorStartTime!!.time.toInt()
                    )
                    .equal(CgmHistoryEntity_.eventIndex, history.timeOffset)
                    .equal(
                        CgmHistoryEntity_.deviceId,
                        deviceId,
                        QueryBuilder.StringOrder.CASE_INSENSITIVE
                    )
                    .equal(
                        CgmHistoryEntity_.authorizationId,
                        userId,
                        QueryBuilder.StringOrder.CASE_INSENSITIVE
                    )
                    .orderDesc(CgmHistoryEntity_.idx)
                    .build()
                    .findFirst()

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
                    (transmitterModel.entity.sensorStartTime?.time!!).toInt()
                if (history.timeOffset <= 60) {
                    historyEntity.eventWarning = -1
                }
                when (historyEntity.eventType) {
                    History.HISTORY_GLUCOSE,
                    -> {
                        if (transmitterModel.isDeviceFault || historyEntity.eventWarning == -1) {
                            historyEntity.eventWarning = -1
                        } else {
                            if (historyEntity.isHighOrLow()) {
                                when {
                                    historyEntity.getHighOrLowGlucoseType() == History.HISTORY_LOCAL_HYPER ||
                                            historyEntity.getHighOrLowGlucoseType() == History.HISTORY_LOCAL_HYPO -> {
                                        if (lastAlertTime == null) {
                                            lastAlertTime = getLastAlert(
                                                deviceId,
                                                userId,
                                                typeAlert
                                            )
                                        }
                                        if (lastAlertTime == null
                                            || historyEntity.deviceTime.time - lastAlertTime!!
                                            > transmitterModel.calculateFrequency(MmkvManager.getAlertFrequency())
                                        ) {
                                            historyEntity.updateEventWarning()
                                            if (historyEntity.getHighOrLowGlucoseType() == History.HISTORY_LOCAL_HYPER && MmkvManager.isHighAlertEnable()) {
                                                TransmitterModel.alert?.invoke(
                                                    "$time", AlertType.MESSAGE_TYPE_GLUCOSEHIGH
                                                )
                                            } else if (historyEntity.getHighOrLowGlucoseType() == History.HISTORY_LOCAL_HYPO && MmkvManager.isLowAlertEnable()) {
                                                TransmitterModel.alert?.invoke(
                                                    "$time", AlertType.MESSAGE_TYPE_GLUCOSELOW
                                                )
                                            }
                                            lastAlertTime = historyEntity.deviceTime.time
                                        }
                                    }
                                    historyEntity.getHighOrLowGlucoseType() == History.HISTORY_LOCAL_URGENT_HYPO -> {
                                        if (lastUrgentAlertTime == null) {
                                            lastUrgentAlertTime = getLastAlert(
                                                deviceId,
                                                userId,
                                                typeUrgentAlert
                                            )
                                        }
                                        if (lastUrgentAlertTime == null
                                            || historyEntity.deviceTime.time - lastUrgentAlertTime!!
                                            > transmitterModel.calculateFrequency(MmkvManager.getUrgentAlertFrequency())
                                        ) {
                                            historyEntity.updateEventWarning()
                                            TransmitterModel.alert?.invoke(
                                                "$time", AlertType.MESSAGE_TYPE_GLUCOSEHIGH
                                            )
                                            lastUrgentAlertTime = historyEntity.deviceTime.time
                                        }
                                    }
                                }
                                val findFirst =
                                    build.orderDesc(CgmHistoryEntity_.idx).build().findFirst()
                                if (findFirst != null) {
                                    lastAlertDeviceTime = findFirst.deviceTime.time
                                    if (abs(historyEntity.deviceTime.time - lastAlertDeviceTime) / 1000 >= calculateFrequency(
                                            if ((historyEntity.eventData
                                                    ?: 0F) > URGENT_HYPO
                                            ) MMKV.defaultMMKV()
                                                .decodeInt(
                                                    LocalPreference.NOTICE_FREQUENCY,
                                                    2
                                                ) else MMKV.defaultMMKV()
                                                .decodeInt(
                                                    LocalPreference.URGENT_NOTICE_FREQUENCY,
                                                    0
                                                )
                                        )
                                    ) {
                                        historyEntity.updateEventWarning()
                                        lastAlertDeviceTime = historyEntity.deviceTime.time
                                    }
                                } else {
                                    historyEntity.updateEventWarning()
                                    lastAlertDeviceTime = historyEntity.deviceTime.time
                                }
                            }

                            if (now - historyEntity.deviceTime.time / 1000 <= calculateFrequency(
                                    MMKV.defaultMMKV()
                                        .decodeInt(LocalPreference.NOTICE_FREQUENCY, 2)
                                )
                            ) {
                                if (historyEntity.eventWarning == History.HISTORY_LOCAL_HYPER) {
                                    if (MMKV.defaultMMKV()
                                            .decodeBool(LocalPreference.HIGH_NOTICE_ENABLE, true)
                                    ) {
                                        notify?.invoke(
                                            "$time", Constant.MESSAGE_TYPE_GLUCOSEHIGH
                                        )
                                    }
                                }

                                if (historyEntity.eventWarning == History.HISTORY_LOCAL_HYPO) {
                                    if (MMKV.defaultMMKV()
                                            .decodeBool(LocalPreference.LOW_NOTICE_ENABLE, true)
                                    ) {
                                        notify?.invoke(
                                            "$time", Constant.MESSAGE_TYPE_GLUCOSELOW
                                        )
                                    }
                                }

                                if (historyEntity.eventWarning == History.HISTORY_LOCAL_URGENT_HYPO) {
                                    if (MMKV.defaultMMKV()
                                            .decodeBool(LocalPreference.URGENT_NOTICE_ENABLE, true)
                                    ) {
                                        notify?.invoke(
                                            "$time",
                                            Constant.MESSAGE_TYPE_GLUCOSELOWALERT
                                        )
                                    }
                                }
                            }
                        }
                    }
//                    History.HISTORY_HYPO, History.HISTORY_HYPER -> {
//                        history.eventWarning = -1
//                    }
                    //传感器故障
                    History.HISTORY_SENSOR_ERROR -> {
                        if (now - historyEntity.deviceTime.time / 1000 < 60 * 30 && (Constant.sensorInsetError || Constant.sensorInsetErrorSuper)) {
                            notify?.invoke(
                                "$time", Constant.MESSAGE_TYPE_SENRORERROR
                            )
                        }
                    }
                }
                historyEntity.updateRecordUUID()
                historyEntity.authorizationId = UserManager.instance().getUserId()
                cgmHistoryBox.put(historyEntity)
                mutableListOf.add(historyEntity)
                BroadCastManager.getInstance().dataReceivedFromTransmitter(
                    CgmsApplication.instance,
                    historyEntity,
                    if (now - historyEntity.deviceTime.time / 1000 < 6 * 60) glucose
                        ?: 0f else historyEntity.eventData
                        ?: 0f
                )
            }
        }) { _, error ->
            if (error == null && UserManager.instance().isLogin()) {
                this.cgmHistories.addAll(mutableListOf)
                if (UserManager.shareUserEntity == null) {
                    TransmitterManager.instance().updateHistories(mutableListOf)
                }
                entity.id?.let {
//                    initSensorStartTime(it)
//                    sensorError()
                }
                transmitterBox.put(entity)
                updateGlucoseTrend(mutableListOf.last().deviceTime)
            }
        }
    }

    private fun getLastAlert(deviceId: String, userId: String, type: Int): Long? {
        val build = ObjectBox.cgmHistoryBox!!.query()
            .equal(
                CgmHistoryEntity_.sensorIndex,
                transmitterModel.entity.sensorIndex
            )
            .equal(
                CgmHistoryEntity_.deviceId, deviceId,
                QueryBuilder.StringOrder.CASE_INSENSITIVE
            )
            .equal(
                CgmHistoryEntity_.authorizationId,
                userId,
                QueryBuilder.StringOrder.CASE_INSENSITIVE
            )
        when (type) {
            1 -> build.equal(
                CgmHistoryEntity_.eventWarning,
                History.HISTORY_LOCAL_HYPO
            ).or().equal(
                CgmHistoryEntity_.eventWarning,
                History.HISTORY_LOCAL_HYPER
            )
            2 -> {
                build.equal(
                    CgmHistoryEntity_.eventWarning,
                    History.HISTORY_LOCAL_URGENT_HYPO
                )
            }
        }
        val lastAlert = build.orderDesc(CgmHistoryEntity_.idx).build().findFirst()
        return lastAlert?.deviceTime?.time
    }

    private fun getHistoryDate(timeOffset: Int): Date {
        val timeLong = transmitterModel.entity.sensorStartTime?.time?.plus(timeOffset * 60 * 1000)
        return Date(timeLong!!)
    }
}