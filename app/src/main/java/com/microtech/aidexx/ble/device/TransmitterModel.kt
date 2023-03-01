package com.microtech.aidexx.ble.device

import android.content.Context
import com.microtech.aidexx.ble.device.entity.TransmitterEntity
import com.microtech.aidexx.common.millisToMinutes
import com.microtech.aidexx.common.millisToSeconds
import com.microtech.aidexx.common.user.UserInfoManager
import com.microtech.aidexx.db.ObjectBox
import com.microtech.aidexx.db.ObjectBox.cgmHistoryBox
import com.microtech.aidexx.db.ObjectBox.transmitterBox
import com.microtech.aidexx.db.entity.CgmHistoryEntity
import com.microtech.aidexx.db.entity.CgmHistoryEntity_
import com.microtech.aidexx.ui.alert.AlertManager
import com.microtech.aidexx.ui.alert.AlertType
import com.microtech.aidexx.utils.*
import com.microtech.aidexx.utils.TimeUtils.dateHourMinute
import com.microtech.aidexx.utils.mmkv.MmkvManager
import com.microtechmd.blecomm.constant.History
import com.microtechmd.blecomm.controller.AidexXController
import com.microtechmd.blecomm.controller.BleController
import com.microtechmd.blecomm.entity.BleMessage
import com.microtechmd.blecomm.parser.*
import com.tencent.mmkv.MMKV
import io.objectbox.kotlin.equal
import io.objectbox.query.QueryBuilder
import java.math.RoundingMode
import java.nio.charset.Charset
import java.text.DecimalFormat
import java.util.*
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.roundToInt


/**
 * APP-SRC-A-2-7-2
 */
class TransmitterModel(val entity: TransmitterEntity) {
    companion object {
        const val devicePrimary = 2  //主设备
        const val deviceOther = 1  //从设备
        var error4TimesWithin2Hours: Boolean = false
        var messageCallBack: ((msg: BleMessage) -> Unit)? = null
        var alert: ((time: String, type: Int) -> Unit)? = null
        var notifyNotification: (() -> Unit)? = null
    }

    private val typeHyperAlert = 1
    private val typeHypoAlert = 2
    private val typeUrgentAlert = 3
    private var lastHyperAlertTime: Long? = null
    private var lastHypoAlertTime: Long? = null
    private var lastUrgentAlertTime: Long? = null

    enum class GlucoseLevel { LOW, NORMAL, HIGH }
    enum class GlucoseTrend { SUPER_FAST_DOWN, FAST_DOWN, DOWN, STEADY, UP, FAST_UP, SUPER_FAST_UP }

    private val tempList = mutableListOf<CgmHistoryEntity>()
    private var briefRangeStartIndex: Int = 0
    private var newestIndex: Int = 0
    private var rawRangeStartIndex: Int = 0
    var isHistoryValid: Boolean = false
    var isDeviceFault: Boolean = false
    var faultType = 0 // 1.异常状态，可恢复 2.需要更换
    var latestHistory: AidexXHistoryEntity? = null
    var lastAlertDeviceTime: Long = 0L
    val controller = AidexXController()
    val cgmHistories: MutableList<CgmHistoryEntity> = ArrayList()
    var sensorStartTime: Date? = null
    var targetSensorIndex = 0
    var targetEventIndex = 0
    var nextEventIndex = 0
    var nextFullEventIndex = 0
    var isNeedTipInsertErrorDialog = false //是否弹框
    var recentAdvertise: AidexXBroadcastEntity? = null //记录最新收到的广播
    var latestAdTime = 0L
    var lastHistoryTime: Date? = null
    var minutesAgo: Int? = null
        private set
        get() {
            if (lastHistoryTime == null) {
                field = null
            } else {
                field = (TimeUtils.currentTimeMillis - lastHistoryTime!!.time).millisToMinutes()
            }
            return field
        }
    var state: Int? = null
        private set
    var primary: Int = 2  //主从设备字段
        private set
    var isSensorExpired: Boolean = false
    var glucose: Float? = null
    var glucoseLevel: GlucoseLevel? = null
    var glucoseTrend: GlucoseTrend? = null

    init {
        controller.mac = entity.deviceMac
        controller.sn = entity.deviceSn
        controller.name = "AiDEX X-${entity.deviceSn}"
        val userId = UserInfoManager.instance().userId()
        val getBytes = userId.toByteArray(Charset.forName("UTF-8"))
        controller.hostAddress = getBytes
        controller.id = entity.accessId
        controller.key = entity.encryptionKey
        controller.setMessageCallback { operation, success, data ->
            LogUtil.eAiDEX(
                "operation $operation , success $success message ${
                    StringUtils.binaryToHexString(data)
                }"
            )
            if (operation in 1..3) {
                messageCallBack?.invoke(BleMessage(operation, success, data))
            } else {
                val result = ByteUtils.subByte(data, 1, data.size - 1);
                messageCallBack?.invoke(BleMessage(operation, success, result))
            }
        }
    }

    fun deviceId(): String? {
        return entity.id
    }

    //更新传感器状态
    fun updateSensorState(insetError: Boolean) {
        entity.needReplace = insetError
        error4TimesWithin2Hours = insetError
        ObjectBox.runAsync({ transmitterBox?.put(entity) })
    }

    fun isDataValid(): Boolean {
        return (lastHistoryTime != null && minutesAgo != null && minutesAgo in 0..15 && glucose != null && !isDeviceFault && isHistoryValid)
    }

    fun isDeviceFault(): Boolean {
        return isDeviceFault
    }

    fun getSensorRemaining(): Int? {
        val days = entity.expirationTime
        return when {
            isSensorExpired -> 0
            entity.sensorStartTime == null || latestAdTime == 0L || recentAdvertise == null -> null
            else -> {
                (days * TimeUtils.oneDayMillis - (TimeUtils.currentTimeMillis - entity.sensorStartTime!!.time)).millisToMinutes()
            }
        }
    }

    fun saveDeviceMode(expirationTime: Int) {
        entity.expirationTime = expirationTime
        transmitterBox!!.put(entity)
    }

    fun clearAccessId() {
        entity.accessId = null
        transmitterBox!!.put(entity)
    }

    suspend fun savePair(deviceModel: Int, version: String) {
        entity.deviceMac = controller.mac
        entity.accessId = controller.id
        entity.deviceModel = deviceModel
        entity.version = version
        entity.updateDeviceKey()
        entity.deviceSn?.let {
            val transmitter = TransmitterManager.instance().loadTransmitterFromDb(it)
            transmitter?.let { trans ->
                entity.fullSensorIndex = trans.fullSensorIndex
                entity.sensorIndex = trans.sensorIndex
                entity.eventIndex = trans.eventIndex
                entity.fullEventIndex = trans.fullEventIndex
                entity.id = trans.id
                entity.idx = trans.idx
            }
        }
        targetSensorIndex = entity.sensorIndex
        targetEventIndex = entity.eventIndex
        nextEventIndex = entity.eventIndex + 1
        nextFullEventIndex = entity.fullEventIndex + 1
        entity.id = null
        //向服务器请求注册设备
        DeviceApi.deviceRegister(entity, success = { it ->
            entity.accessId = controller.id
            entity.id = it.id
            entity.sensorIndex = it.sensorIndex
            entity.eventIndex = it.eventIndex
            entity.fullEventIndex = it.fullEventIndex
            entity.deviceSn = it.deviceSn
            targetSensorIndex = entity.sensorIndex
            targetEventIndex = entity.eventIndex
            nextEventIndex = entity.eventIndex + 1
            nextFullEventIndex = entity.fullEventIndex + 1
            ObjectBox.runAsync({ transmitterBox!!.put(entity) })
            TransmitterManager.instance().set(this@TransmitterModel)
//            LiveEventBus.get<Register>(EventKey.EVENT_REGISTER_SUCCESS)
//                .post(Register(true)) //匹配成功以后，发送信息到匹配页面 关闭页面
        }, failure = {
            TransmitterManager.instance().removeDefault()
//            LiveEventBus.get<Register>(EventKey.EVENT_REGISTER_SUCCESS)
//                .post(Register(false)) //匹配失败以后，发送信息到匹配页面 关闭页面
        })
    }

    fun resetIndex() {
        entity.eventIndex = 0
        entity.fullEventIndex = 0
        nextEventIndex = entity.eventIndex + 1
        nextFullEventIndex = entity.fullEventIndex + 1
    }

    //是否第一次植入传感器
    fun isFirstInsertSensor(): Boolean {
        if (sensorStartTime != null) {
            if ((TimeUtils.currentTimeMillis.millisToSeconds() - sensorStartTime!!.time.millisToSeconds() > 15 * TimeUtils.oneDaySeconds)) {
                return true
            }
        } else {
            if (entity.sensorStartTime != null
                && TimeUtils.currentTimeMillis.millisToSeconds() - entity.sensorStartTime!!.time.millisToSeconds() > 15 * TimeUtils.oneDaySeconds
            ) {
                return true
            }
        }
        return false
    }

    suspend fun deletePair() {
        entity.accessId = null
        controller.unregister()
        ObjectBox.runAsync({
            transmitterBox!!.put(entity)
        }, onSuccess = {
            entity.id?.let {
                val map = hashMapOf("id" to it)
                suspend {
                    DeviceApi.deviceUnregister(map,
                        success = {
                            controller.sn = null
                            controller.mac = null
                            controller.id = null
                            BleController.stopScan() //停止扫描
                            ObjectBox.runAsync({
                                transmitterBox!!.removeAll()
                            })
                            TransmitterManager.instance().removeTransmitterFromDb()
                            TransmitterManager.instance().removeDefault()
//                    LiveEventBus.get<String>(EventKey.EVENT_UNPAIR_SUCCESS)
//                        .post("unPairSuccess") //解配成功
                        },
                        failure = {
                            ToastUtil.showShort(it)
                        })
                }
            }
        }, onError = {

        })
    }


    /***
     *
     * 是否为主设备
     *
     * **/
    fun isPrimary(): Boolean {
        return primary == devicePrimary
    }

    fun getHistoryDate(timeOffset: Int): Date {
        val timeLong = entity.sensorStartTime?.time?.plus(timeOffset * 60 * 1000)
        return Date(timeLong!!)
    }

    fun handleAdvertisement(data: ByteArray) {
        if (entity.sensorStartTime == null) {
            controller.startTime
            return
        }
        val broadcast = AidexXParser.getBroadcast<AidexXBroadcastEntity>(data)
        isSensorExpired =
            (broadcast.status == History.SESSION_STOPPED && broadcast.calTemp != History.TIME_SYNCHRONIZATION_REQUIRED)
        isDeviceFault =
            broadcast.status == History.SENSOR_MALFUNCTION || broadcast.status == History.DEVICE_SPECIFIC_ALERT || broadcast.status == History.GENERAL_DEVICE_FAULT
        if (broadcast.status == History.SENSOR_MALFUNCTION) {
            faultType = 1
        } else if (broadcast.status == History.GENERAL_DEVICE_FAULT) {
            faultType = 2
        }
        val adHistories = broadcast.history
        if (adHistories.isNotEmpty()) {
            latestHistory = adHistories[0]
        } else {
            return
        }
        isHistoryValid =
            latestHistory?.isValid == 1 && latestHistory?.status == History.STATUS_OK
        val now = TimeUtils.currentTimeMillis
        latestAdTime = now
        if (UserInfoManager.shareUserInfo != null) {
            LogUtil.eAiDEX("view sharing")
            return
        }
        val temp = broadcast.history[0].glucose
        glucose = if (isDeviceFault || isSensorExpired || temp < 0) null
        else com.microtech.aidexx.widget.dialog.x.util.roundOffDecimal(temp / 18f)
        glucoseLevel = getGlucoseLevel(glucose)
        latestHistory?.let {
            val historyDate = getHistoryDate(it.timeOffset)
            if (glucose != null && lastHistoryTime != historyDate) {
                lastHistoryTime = historyDate
            }
        }
        targetEventIndex = latestHistory?.timeOffset ?: 0
        if (nextEventIndex <= targetEventIndex) {
            val broadcastContainsNext = isNextInBroadcast(nextEventIndex, adHistories)
            if (broadcastContainsNext) {
                val historiesFromBroadcast =
                    getHistoriesFromBroadcast(nextEventIndex, adHistories)
                saveBriefHistory(historiesFromBroadcast.reversed())
            } else {
                latestHistory?.let {
                    if (targetEventIndex > nextEventIndex) {
                        if (newestIndex == targetEventIndex) {
                            if (nextEventIndex < briefRangeStartIndex) {
                                nextEventIndex = briefRangeStartIndex
                            }
                            controller.getHistories(nextEventIndex)
                        } else {
                            controller.historyRange
                        }
                    }
                }
            }
            return
        }
        val numGetHistory = 40
        if (targetEventIndex >= nextFullEventIndex + numGetHistory
            || ((targetEventIndex >= nextFullEventIndex) && isSensorExpired)
        ) {
            if (newestIndex == targetEventIndex) {
                if (nextFullEventIndex < rawRangeStartIndex) {
                    nextFullEventIndex = rawRangeStartIndex
                }
                controller.getRawHistories(nextFullEventIndex)
            } else {
                controller.historyRange
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

    fun saveBriefHistoryFromConnect(data: ByteArray) {
        val histories = AidexXParser.getHistories<AidexXHistoryEntity>(data)
        if (histories.isNullOrEmpty()) return
        if (histories.first().timeOffset == nextEventIndex) {
            saveBriefHistory(histories)
        }
    }

    fun saveRawHistoryFromConnect(data: ByteArray) {
        val histories = AidexXParser.getRawHistory<AidexXRawHistoryEntity>(data)
        if (histories.isEmpty()) return
        if (histories.first().timeOffset == nextFullEventIndex) {
            saveRawHistory(histories)
        }
    }

    /***
     *
     * 配对获取第一条历史记录保存
     * */
    fun saveHistoriesSimple(data: ByteArray) {

        val histories = CgmParser.getHistories<CgmHistoryEntity>(data)

        if (histories.isEmpty()) return

        val history = histories.get(0)

        LogUtils.error("配对获取历史 $history")

        CgmsApplication.isCgmPairing = false //更改配对状态
        entity.sensorStartTime = history.deviceTime
        transmitterBox.put(entity)
    }

    fun roundOffDecimal(number: Float): Float {
        val df = DecimalFormat("#.#")
        df.roundingMode = RoundingMode.CEILING
        return df.format(number).toFloat()
    }

    // 保存数据
    private fun saveBriefHistory(histories: List<AidexXHistoryEntity>) {
        tempList.clear()
        val deviceId = TransmitterManager.instance().getDefault().deviceId() ?: return
        val userId = UserInfoManager.instance().userId()
        if (userId.isEmpty()) {
            return
        }
        ObjectBox.runAsync({
            val now = TimeUtils.currentTimeMillis
            for (history in histories) {
                val oldHistory = cgmHistoryBox!!.query().equal(
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
                val historyEntity = com.microtech.aidexx.db.entity.CgmHistoryEntity()
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
                historyEntity.eventData =
                    com.microtech.aidexx.widget.dialog.x.util.roundOffDecimal(history.glucose / 18f)
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
                        if (isDeviceFault || historyEntity.eventWarning == -1) {
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
                historyEntity.updateRecordUUID()
                historyEntity.authorizationId = userId
                cgmHistoryBox!!.put(historyEntity)
                tempList.add(historyEntity)
            }
        }, onSuccess = {
            if (UserInfoManager.instance().isLogin()) {
                cgmHistories.addAll(tempList)
                if (UserInfoManager.shareUserInfo == null) {
                    TransmitterManager.instance().updateHistories(tempList)
                }
                tempList.sortBy { it.eventIndex }
                entity.eventIndex = tempList.last().eventIndex
                nextEventIndex = entity.eventIndex + 1
                transmitterBox!!.put(entity)
                updateGlucoseTrend(tempList.last().deviceTime)
                continueBriefFetch()
            }
        })
    }

    private fun continueBriefFetch() {
        if (targetEventIndex >= nextEventIndex) {
            if (nextEventIndex < briefRangeStartIndex) {
                nextEventIndex = briefRangeStartIndex
            }
            controller.getHistories(nextEventIndex)
        } else if (targetEventIndex >= nextFullEventIndex) {
            if (nextFullEventIndex < rawRangeStartIndex) {
                nextFullEventIndex = rawRangeStartIndex
            }
            controller.getRawHistories(nextFullEventIndex)
        } else {
            controller.disconnect()
        }
    }

    private fun continueRawFetch() {
        if (targetEventIndex > nextFullEventIndex) {
            controller.getRawHistories(nextFullEventIndex)
        } else {
            controller.disconnect()
        }
    }

    private fun getLastAlert(deviceId: String, userId: String, type: Int): Long? {
        val build = cgmHistoryBox!!.query().equal(
            CgmHistoryEntity_.sensorIndex, entity.sensorIndex
        ).equal(
            CgmHistoryEntity_.deviceId, deviceId, QueryBuilder.StringOrder.CASE_INSENSITIVE
        ).equal(
            CgmHistoryEntity_.authorizationId, userId, QueryBuilder.StringOrder.CASE_INSENSITIVE
        )
        when (type) {
            History.HISTORY_LOCAL_HYPER -> build.equal(
                CgmHistoryEntity_.eventWarning, History.HISTORY_LOCAL_HYPER
            )
            History.HISTORY_LOCAL_HYPO -> build.equal(
                CgmHistoryEntity_.eventWarning, History.HISTORY_LOCAL_HYPO
            )
            History.HISTORY_LOCAL_URGENT_HYPO -> build.equal(
                CgmHistoryEntity_.eventWarning, History.HISTORY_LOCAL_URGENT_HYPO
            )
        }
        val lastAlert = build.orderDesc(CgmHistoryEntity_.idx).build().findFirst()
        return lastAlert?.deviceTime?.time
    }

    private fun saveRawHistory(rawHistories: List<AidexXRawHistoryEntity>) {
        val deviceId = TransmitterManager.instance().getDefault().deviceId() ?: return
        val userId = UserInfoManager.instance().userId()
        if (userId.isEmpty()) {
            return
        }
        ObjectBox.runAsync({
            for (rawHistory in rawHistories) {
                val existHistory = cgmHistoryBox!!.query()
                    .equal(CgmHistoryEntity_.eventIndex, rawHistory.timeOffset)
                    .equal(
                        CgmHistoryEntity_.deviceId,
                        deviceId,
                        QueryBuilder.StringOrder.CASE_INSENSITIVE
                    ).equal(
                        CgmHistoryEntity_.authorizationId,
                        userId,
                        QueryBuilder.StringOrder.CASE_INSENSITIVE
                    ).orderDesc(CgmHistoryEntity_.idx).build().findFirst()
                val historyEntity = CgmHistoryEntity()

                historyEntity.eventIndex = rawHistory.timeOffset
                historyEntity.deviceTime = getHistoryDate(rawHistory.timeOffset)
                historyEntity.rawData1 = rawHistory.i1
                historyEntity.rawData2 = rawHistory.i2
                historyEntity.rawData3 = rawHistory.vc
                historyEntity.dataStatus = 1
                if (existHistory != null) {
                    historyEntity.recordIndex = existHistory.recordIndex
                    historyEntity.id = existHistory.id
                    historyEntity.idx = existHistory.idx
                    historyEntity.eventWarning = existHistory.eventWarning
                    historyEntity.eventData = existHistory.eventData
                    historyEntity.eventType = existHistory.eventType
                }
                historyEntity.deviceId = deviceId
                historyEntity.authorizationId = userId
                historyEntity.updateRecordUUID()
                cgmHistoryBox!!.put(historyEntity)
            }
        }, onSuccess = {
            entity.fullEventIndex = rawHistories.last().timeOffset
            nextFullEventIndex = entity.fullEventIndex + 1
            transmitterBox!!.put(entity)
            continueRawFetch()
        })
    }

    fun getGlucoseLevel(glucose: Float?): GlucoseLevel? {
        return when {
            glucose == null -> null
            glucose > ThresholdManager.hyper -> GlucoseLevel.HIGH
            glucose < ThresholdManager.hypo -> GlucoseLevel.LOW
            else -> GlucoseLevel.NORMAL
        }
    }

    /***
     *
     * @param sNeedToast (是否需要弹框提示，再切换语言重新加载历史数据的时候不需要)
     *
     * **/
    fun updateGlucoseTrend(dateTime: Date, isNeedToast: Boolean = true) {
        CgmsApplication.boxStore.runInTxAsync({
            val size = 5
            val glucoseArray = FloatArray(size)
            var isEventCalibration = false //是否最近有校准事件
//            val builder = cgmHistoryBox.query()
//            builder.equal(
//                CgmHistoryEntity_.authorizationId,
//                UserManager.instance().getUserId(),
//                QueryBuilder.StringOrder.CASE_INSENSITIVE
//            )
//            builder.equal(
//                CgmHistoryEntity_.sensorIndex,
//                (entity.sensorStartTime?.time!! / 1000).toInt(),
//            )
//            val cgmHistories = builder
//                .equal(CgmHistoryEntity_.type, 0)
//                .orderDesc(CgmHistoryEntity_.eventIndex)
//                .build()
//                .find()
            LogUtils.eAiDex("趋势 cgmHistories.size:${cgmHistories.size}")
            loop@ for (index in cgmHistories.size - 1 downTo 0 step 4) {
                val history = cgmHistories[index]
//                if (history.eventType == History.HISTORY_GLUCOSE) {
                if (history.eventWarning == -1) {
                    LogUtils.eAiDex("趋势 history.eventWarning == -1")
                    continue@loop
                }
                val i =
                    ((dateTime.time - history.deviceTime.time).toDouble() / 1000 / 300f).roundToInt()
                val glu = history.eventData ?: 0f
                if (i < 0) {
                    continue@loop
                } else if (i >= size) {
                    break@loop
                } else {
                    if (history.eventType == History.HISTORY_GLUCOSE) {
                        glucoseArray[i] = glu
                    } else {
                        isEventCalibration = true
                        break@loop
                    }
                }
//                }
            }
            LogUtils.eAiDex("趋势 isEventCalibration:$isEventCalibration")
            LogUtils.eAiDex("趋势 glucoseArray:${glucoseArray.contentToString()}")
            val roc = if (isEventCalibration) null else {
                if (glucoseArray.any { it < GLUCOSE_LOWER || it > GLUCOSE_UPPER }) null else {
                    val fn = (1 - exp(
                        (abs(glucoseArray[0] + glucoseArray[2] - 2 * glucoseArray[1]) + abs(
                            glucoseArray[1] + glucoseArray[3] - 2 * glucoseArray[2]
                        ) + abs(glucoseArray[2] + glucoseArray[4] - 2 * glucoseArray[3])) / 5 - 1
                    )) * 1.5
                    if (fn > 0) {
                        val trend =
                            (fn * (glucoseArray[0] - glucoseArray[1]) / 5 + fn * (glucoseArray[0] - glucoseArray[2]) / 10 + (glucoseArray[0] - glucoseArray[3]) / 15) / (1 + 2 * fn);
                        if ((trend > 0.06 && (glucoseArray[0] - glucoseArray[1]) / 5 <= 0) || (trend < -0.06 && (glucoseArray[0] - glucoseArray[1]) / 5 >= 0)) {
                            null
                        } else trend
                    } else null
                }
            }
            when {
                roc == null -> null
                roc > SUPER_FAST_UP -> GlucoseTrend.SUPER_FAST_UP
                FAST_UP < roc && roc <= SUPER_FAST_UP -> GlucoseTrend.FAST_UP
                SLOW_UP < roc && roc <= FAST_UP -> GlucoseTrend.UP
                roc in SLOW_DOWN..SLOW_UP -> GlucoseTrend.STEADY
                roc >= FAST_DOWN && roc < SLOW_DOWN -> GlucoseTrend.DOWN
                roc >= SUPER_FAST_DOWN && roc < FAST_DOWN -> GlucoseTrend.FAST_DOWN
                roc < SUPER_FAST_DOWN -> GlucoseTrend.SUPER_FAST_DOWN
                else -> GlucoseTrend.STEADY
            }.also { glucoseTrend = it }
        }) { _, err ->
            if (err == null) {
                BroadcastData.setTrendSlope(glucoseTrend)
                val time = dateTime.dateHourMinute()
                if (isNeedToast) {
                    if (Date().time / 1000 - dateTime.time / 1000 > 60 * 30) {
                        return@runInTxAsync
                    }
                }
                LogUtils.eAiDex("趋势 glucoseTrend:$glucoseTrend")
                when (glucoseTrend) {
                    GlucoseTrend.SUPER_FAST_DOWN -> {
                        if (MMKV.defaultMMKV().decodeBool(LocalPreference.FALL_ALERT, true)) {
                            val lastFastDown: Long =
                                MMKVUtil.decodeLong(EventKey.LAST_FAST_DOWN_TIME, 0L)
                            if (lastFastDown != 0L) {
                                if ((dateTime.time - lastFastDown) / 1000 >= calculateFrequency(
                                        MMKV.defaultMMKV()
                                            .decodeInt(LocalPreference.NOTICE_FREQUENCY, 2)
                                    )
                                ) {
                                    alert?.invoke("$time", Constant.MESSAGE_TYPE_GLUCOSEDOWN)
                                    MMKVUtil.encodeLong(EventKey.LAST_FAST_DOWN_TIME, dateTime.time)
                                }
                            } else {
                                alert?.invoke("$time", Constant.MESSAGE_TYPE_GLUCOSEDOWN)
                                MMKVUtil.encodeLong(EventKey.LAST_FAST_DOWN_TIME, dateTime.time)
                            }
                        }
                    }
                    GlucoseTrend.SUPER_FAST_UP -> {
                        if (MMKV.defaultMMKV().decodeBool(LocalPreference.RAISE_ALERT, true)) {
                            val lastFastUp: Long =
                                MMKVUtil.decodeLong(EventKey.LAST_FAST_UP_TIME, 0L)
                            if (lastFastUp != 0L) {
                                if ((dateTime.time - lastFastUp) / 1000 >= calculateFrequency(
                                        MMKV.defaultMMKV()
                                            .decodeInt(LocalPreference.NOTICE_FREQUENCY, 2)
                                    )
                                ) {
                                    alert?.invoke("$time", Constant.MESSAGE_TYPE_GLUCOSEUP)
                                    MMKVUtil.encodeLong(EventKey.LAST_FAST_UP_TIME, dateTime.time)
                                }
                            } else {
                                alert?.invoke("$time", Constant.MESSAGE_TYPE_GLUCOSEUP)
                                MMKVUtil.encodeLong(EventKey.LAST_FAST_UP_TIME, dateTime.time)
                            }
                        }
                    }
                    else -> {
                    }
                }
            }
        }
    }

    fun updateNotification() {
        notifyNotification?.invoke()
    }

    /***
     * 信号丢失提示
     *
     * */
    fun messageLoss(context: Context) {
        if (MMKV.defaultMMKV().decodeBool(LocalPreference.LOSS_ALERT, true)) {
            val time = Date().dateHourMinute()
            alert?.invoke("${time}", Constant.MESSAGE_TYPE_SIGNLOST)
        }
    }

    fun updateStartTime(sensorStartTime: Date?) {
        entity.sensorStartTime = sensorStartTime
        transmitterBox.put(entity)
    }
}