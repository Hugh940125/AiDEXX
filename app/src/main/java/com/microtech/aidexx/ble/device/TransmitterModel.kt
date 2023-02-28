package com.microtech.aidexx.ble.device

import android.content.Context
import com.microtech.aidexx.ble.device.entity.TransmitterEntity
import com.microtech.aidexx.common.millisToMinutes
import com.microtech.aidexx.common.millisToSeconds
import com.microtech.aidexx.common.user.UserInfoManager
import com.microtech.aidexx.db.ObjectBox
import com.microtech.aidexx.db.ObjectBox.cgmHistoryBox
import com.microtech.aidexx.db.ObjectBox.transmitterBox
import com.microtech.aidexx.db.entity.CgmHistoryEntity_
import com.microtech.aidexx.utils.*
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

    enum class GlucoseLevel { LOW, NORMAL, HIGH }
    enum class GlucoseTrend { SUPER_FAST_DOWN, FAST_DOWN, DOWN, STEADY, UP, FAST_UP, SUPER_FAST_UP }

    var isHistoryValid: Boolean = false
    var isDeviceFault: Boolean = false
    var faultType = 0 // 1.异常状态，可恢复 2.需要更换
    var latestHistory: AidexXHistoryEntity? = null
    private var lastAlertDeviceTime: Long = 0L
    val controller = AidexXController()
    private val cgmHistories: MutableList<CgmHistoryEntity> = ArrayList()
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
    var briefIndex: Int = 0
    var newestIndex: Int = 0
    var rawIndex: Int = 0

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
        entity.error4TimesWithin2Hours = insetError
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
            initSensorStartTime(entity.id)
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

    fun initSensorStartTime(deviceId: String?) {
        if (deviceId.isNullOrEmpty()) {
            return
        }
        val item = cgmHistoryBox!!.query().equal(
            CgmHistoryEntity_.eventIndex, 1
        ).and().equal(
            CgmHistoryEntity_.deviceId, deviceId, QueryBuilder.StringOrder.CASE_INSENSITIVE
        ).equal(
            CgmHistoryEntity_.authorizationId,
            UserInfoManager.instance().userId(),
            QueryBuilder.StringOrder.CASE_INSENSITIVE
        ).equal(CgmHistoryEntity_.sensorIndex, entity.sensorIndex)
            .orderDesc(CgmHistoryEntity_.idx).build().findFirst()
        if (item != null) {
            sensorStartTime = item.deviceTime
        }
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

    fun handleAdvertisement(context: Context, data: ByteArray) {
        if (FastUtil.isFastGet1()) {
            return
        }
//        if (data.size < 20) {
//            LogUtils.data("扫描到的字节数组太短")
//            return
//        }
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
        val histories = broadcast.history
        var history: AidexXHistoryEntity? = null

        LogUtils.eAiDex("收到广播----> $broadcast")
        if (histories.isNotEmpty()) {
            history = histories[0]
            latestHistory = histories[0]
        } else {
            return
        }
        isHistoryValid = latestHistory?.isValid == 1 && latestHistory?.status == History.STATUS_OK
        for ((index, his) in histories.withIndex()) {
            LogUtils.eAiDex("${index} ----> timeOffset:${his.timeOffset} , glucose:${his.glucose}")
        }
//        if (history.eventType == History.HISTORY_SENSOR_NEW && history.eventData == -2f) {
//            recentAdv = null
//            return
//        }
//        //过滤解析错误
//        if (broadcast.primary > 2 || broadcast.primary < 0) {
//            return
//        }
        val now = Date().time / 1000
        latestAdTime = now
        Constant.lastAdvertiseTime = latestAdTime
        Constant.hasAdvertise = true
        recentAdvertise = broadcast
//
//        primary = broadcast.primary
        if (UserManager.shareUserEntity != null) {
            LogUtils.data("查看分享中，不更新数据")
            return
        }
//
//        if (isNeedTipInsertErrorDialog && Constant.sensorInsetErrorSuper) {
//            LogUtils.data("传感器植入失败，弹框提示")
//            isNeedTipInsertErrorDialog = false
//            val time = Date().dateHourMinute()
//            notify?.invoke("$time", Constant.MESSAGE_TYPE_SENROR_EMBEDDING_SUPER)
//        }
//
////        LogUtils.debug("发射器高低血糖值 high : ${controller.hyper} , low : ${controller.hypo}")
//        if (history.eventIndex == 0) return
//        //更新高低血糖阈值
////        entity?.let {
////
////            if (isMainDevice()) {
////                if (it.hyperThreshold != controller.hyper) {
////
////                    it.hyperThreshold = controller.hyper
////                    UserManager.instance()
////                        .updateHealthAlert(Constant.ALERT_HIGH_UPDATE, it.hyperThreshold)
////                    LiveEventBus.get(EventKey.ALERT_UPDATE)
////                        .post(AlertEntity(Constant.ALERT_HIGH_UPDATE, it.hyperThreshold))
////                }
////
////                if (it.hypoThreshold != controller.hypo) {
////                    UserManager.instance()
////                        .updateHealthAlert(Constant.ALERT_LOW_UPDATE, it.hypoThreshold)
////                    it.hypoThreshold = controller.hypo
////                    LiveEventBus.get(EventKey.ALERT_UPDATE)
////                       .post(AlertEntity(Constant.ALERT_LOW_UPDATE, it.hypoThreshold))
////                }
////            } else {
////
////                if (it.hyperThreshold != controller.hyper || it.hypoThreshold != controller.hypo) {
////                    LiveEventBus.get(EventKey.ALERT_TIP)
////                        .post(AlertEvent())
////                }
////            }
////        }
//
//        when (history.eventType) {
//            History.HISTORY_SENSOR_NEW -> {
//                isNewSensorAd = true
//                LogUtils.data("广播检测到 NEW SENSOR")
//                if (history.eventData!! == -1f && isFirstInsertSensor()) {
//                    LogUtils.data("广播检测到 isNeedRequestNewSensor $isNeedRequestNewSensor lastRequestNewSensorTime $lastRequestNewSensorTime lastRequestNewSensorNum $lastRequestNewSensorNum")
//
//                    if (Date().time - lastRequestNewSensorTime > REQUEST_NEWSENSOR_DUCTION) {
//                        LogUtils.data(" NEW SENSOR  发送消息次数 $lastRequestNewSensorNum")
//                        lastRequestNewSensorNum++
//                        if (lastRequestNewSensorNum < 5) {
//                            lastRequestNewSensorTime = Date().time
//                            LiveEventBus.get<Boolean>(EventKey.CGM_NEW_SENSOR).post(true)
//                        }
//                    }
//                }
//
//                if (now - lastNewSenorNotifyTime > 15 * 60) {
//                    if (history.eventData != null && history.eventData!! < 0f) {
//                        val time = (history.deviceTime).dateHourMinute()
//                        lastNewSenorNotifyTime = now
//                        notify?.invoke(
//                            "$time", Constant.MESSAGE_TYPE_NEWSENROR
//                        )
//                    }
//                }
//            }
//            else -> {
//                isNewSensorAd = false
//            }
//        }
//
//        if (!isNewSensor(history)) {
//            if (isMainDevice()) {
//                if ((broadcast.datetime - now).absoluteValue > 60) {
//                    if (broadcast.datetime == lastDeviceTime) {
//                        errTimeNum++
//                    } else {
//                        errTimeNum = 0
//                    }
//                    LogUtils.eAiDex("errTimeNum $errTimeNum")
//                    if (errTimeNum >= 10) {
//                        LogUtils.eAiDex("=================")
//                        LogUtils.eAiDex("=====系统重启=====")
//                        LogUtils.eAiDex("=================")
//                        val model = TransmitterManager.instance().getDefaultModel()
//                        AidexBle.getInstance(CgmsApplication.instance)
//                            .executeOperation(
//                                model?.entity?.deviceMac,
//                                AidexBle.OPERATION_REBOOT
//                            )
//                        errTimeNum = 0
//                    } else {
//                        if (now - lastDevicUpdateTime > 60) {
//                            LogUtils.eAiDex("修改时间")
////                            controller.setDatetime(now)
//                            lastDevicUpdateTime = now  //上一次修改设备时间
//                        }
//                    }
//                    lastDeviceTime = broadcast.datetime
//
//                    return
//                }
//                errTimeNum = 0
//            } else {
//                if ((broadcast.datetime - now).absoluteValue > 60) {
//                    if (lastTipDeviceTimeError == 0L || now - lastTipDeviceTimeError > 60 * 60) {
//                        val time = Date().dateHourMinute()
//                        notify?.invoke(time!!, Constant.MESSAGE_TYPE_DEVICEERROR)
//                        lastTipDeviceTimeError = now
//                    }
//                }
//            }
//        }
//        state = broadcast.state
        val temp = broadcast.history[0].glucose
        glucose =
            if (Constant.sensorInsetError || Constant.sensorInsetErrorSuper || isSensorExpired || temp < 0) null else roundOffDecimal(
                temp / 18f
            )
        glucoseLevel = glucoseLevel(glucose)
//
//        if (!FastUtil.isFastGet1()) {
        if (entity.sensorStartTime == null) {
            controller.startTime
            return
        }
//        }
        val historyDate = getHistoryDate(broadcast.history[0].timeOffset)
        if (glucose != null && lastHistoryTime != historyDate) {
            lastHistoryTime = historyDate
        }
//
//        targetSensorIndex = history.sensorIndex
//        LogUtils.data("sensorIndex : $targetSensorIndex  nextEventIndex $nextEventIndex nextFullEventIndex $nextFullEventIndex")
//        if (targetSensorIndex == 0) return
//
        targetEventIndex = history?.timeOffset ?: 0
//
//        val lastRecord =
//            RecordManager.recordBox.query().orderDesc(DeviceUpdateRecord_.id).build().findFirst()
//        val needReset =
//            lastRecord?.ota1 == 1 && lastRecord.ota2 == 1 && lastRecord.needSetSensorIndex && targetSensorIndex == 2 && entity.sensorIndex == 2
//
//        if (fullSensorIndex != targetSensorIndex) {
//            nextFullEventIndex = 1
//        }
//
//        //新的传感器
//        if (targetSensorIndex != entity.sensorIndex || targetEventIndex + 1 < nextEventIndex || needReset) {
//            if (lastRecord?.needSetSensorIndex == true) {
//                lastRecord.needSetSensorIndex = false
//                RecordManager.recordBox.put(lastRecord)
//            }
//            LogUtils.eAiDex("新传感器 targetEventIndex $targetEventIndex nextEventIndex${nextEventIndex}")
//            nextEventIndex = 1
//            nextFullEventIndex = 1
//            lastRequestNewSensorTime = 0
//            lastRequestNewSensorNum = 0
//            Constant.sensorInsetErrorSuper = false
//            Constant.sensorInsetError = false
//            updateSensorStatus(false)
//        } else {
//            //否则下一个时间的 之前的index+1
//            nextEventIndex = entity.eventIndex + 1
//            nextFullEventIndex = entity.fullEventIndex + 1
//        }
//        if (!isNewSensor(history)) {
//            LogUtils.data("不是新旧传感器页面，同步数据")
        //当最新的事件等于下一个事件，则直接保存
        LogUtils.eAiDex("开始获取简要数据1 打印 newestIndex ${newestIndex} targetEventIndex ${targetEventIndex} nextEventIndex ${nextEventIndex}")
        if (nextEventIndex <= targetEventIndex) {
            val nextInBroadcast = isNextInBroadcast(nextEventIndex, histories)
            LogUtils.eAiDex("开始获取 nextInBroadcast $nextInBroadcast")
            if (nextInBroadcast) {
                LogUtils.eAiDex("开始获取简要数据1 在广播中获取 newestIndex ${newestIndex} targetEventIndex ${targetEventIndex} nextEventIndex ${nextEventIndex}")
//            if (!FastUtil.isFastGet2()) {
                val historiesFromBroadcast = getHistoriesFromBroadcast(nextEventIndex, histories)
                LogUtils.eAiDex("广播中取出数组 $historiesFromBroadcast")
                saveHistories(historiesFromBroadcast.reversed())
//            }
            } else {
//            if (!FastUtil.isFastGet3()) {
                LogUtils.eAiDex("开始获取 histories[0].timeOffset ${histories[0].timeOffset}")
                if (histories[0].timeOffset > nextEventIndex) {
                    LogUtils.eAiDex("开始获取简要数据1 newestIndex ${newestIndex} targetEventIndex ${targetEventIndex} nextEventIndex ${nextEventIndex}")
                    CgmsApplication.isCgmPairing = false
                    if (newestIndex == targetEventIndex) {
                        if (nextEventIndex < briefIndex) {
                            nextEventIndex = briefIndex
                        }
                        LogUtils.eAiDex("开始获取简要数据2 expired ${isSensorExpired} targetIndex ${targetEventIndex} nextEventIndex ${nextEventIndex}")
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
        if (targetEventIndex >= nextFullEventIndex + numGetHistory || ((targetEventIndex >= nextFullEventIndex) && isSensorExpired)) {
            if (newestIndex == targetEventIndex) {
                if (nextFullEventIndex < rawIndex) {
                    nextFullEventIndex = rawIndex
                }
                controller.getRawHistories(nextFullEventIndex)
            } else {
                controller.historyRange
            }
            LogUtils.data("开始获取原始数据 expired ${isSensorExpired} targetIndex ${targetEventIndex} nextFullIndex ${nextFullEventIndex}")
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

    fun isNextInBroadcast(next: Int, histories: List<AidexXHistoryEntity>): Boolean {
        return next in histories[histories.size - 1].timeOffset..histories[0].timeOffset
    }

    fun getHistoriesFromBroadcast(
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

    fun saveHistoriesAndContinueSync(data: ByteArray) {
        LogUtils.eAiDex("[收到简要数据] -- ${data.contentToString()}")
        val histories = AidexXParser.getHistories<AidexXHistoryEntity>(data)
        LogUtils.eAiDex("[收到简要数据] -- $histories")
        if (histories.isEmpty()) return

//        for (history in histories) {
//            history.sensorIndex = targetSensorIndex
//        }
        if (histories.first().timeOffset == nextEventIndex) {
            saveHistories(histories)
            //最新事件 大于 下一个事件  则获取
            if (targetEventIndex >= nextEventIndex) {
                LogUtils.data("开始获取简要数据3 expired ${isSensorExpired} targetIndex ${targetEventIndex} nextEventIndex ${nextEventIndex}")

                controller.getHistories(nextEventIndex)
            } else if (targetEventIndex >= nextFullEventIndex) {
                if (lastHistoryTime != null) {
                    updateGlucoseTrend(lastHistoryTime!!)
                }
                LogUtils.data("开始获取原始数据 expired ${isSensorExpired} targetIndex ${targetEventIndex} nextFullIndex ${nextFullEventIndex}")
                if (nextFullEventIndex < rawIndex) {
                    nextFullEventIndex = rawIndex
                }
                controller.getRawHistories(nextFullEventIndex)
            } else {
                LogUtils.data("获取血糖数据断开连接")
                controller.disconnect()
            }
        }
    }

    fun saveFullHistoriesAndContinueSync(data: ByteArray) {
        val histories = AidexXParser.getRawHistory<AidexXRawHistoryEntity>(data)
        if (histories.isEmpty()) return
        LogUtils.debug("[收到原始数据 数目 ${histories.size} - ] - ${histories.toString()}")
//        for (history in histories) {
//            history.sensorIndex = targetSensorIndex
//        }
        if (histories.first().timeOffset == nextFullEventIndex) {
            saveFullHistories(histories)
            if (targetEventIndex > nextFullEventIndex) {
                controller.getRawHistories(nextFullEventIndex)
            } else {
                LogUtils.data("同步原始数据到最新，断开连接")
                controller.disconnect()
            }
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
    private fun saveHistories(cgmHistories: List<AidexXHistoryEntity>) {
        entity.eventIndex = cgmHistories.last().timeOffset
        nextEventIndex = entity.eventIndex + 1
        val mutableListOf = mutableListOf<CgmHistoryEntity>()
        CgmsApplication.boxStore.runInTxAsync({
            val deviceId = TransmitterManager.instance().getDefaultModel()?.getDeviceID()
                ?: return@runInTxAsync
            val now = Date().time / 1000
            for (history in cgmHistories) {
                val oldHistory = cgmHistoryBox.query()
                    .equal(CgmHistoryEntity_.sensorIndex, entity.sensorStartTime?.time!!.toInt())
                    .equal(CgmHistoryEntity_.eventIndex, history.timeOffset).equal(
                        CgmHistoryEntity_.deviceId,
                        entity.id ?: "",
                        QueryBuilder.StringOrder.CASE_INSENSITIVE
                    ).equal(
                        CgmHistoryEntity_.authorizationId,
                        UserManager.instance().getUserId(),
                        QueryBuilder.StringOrder.CASE_INSENSITIVE
                    ).orderDesc(CgmHistoryEntity_.idx).build().findFirst()

                if (oldHistory != null) {
                    LogUtils.eAiDex("History exist,need not update ${oldHistory.recordIndex} -- ${oldHistory.deviceId}")
                    continue
                }

                val time = getHistoryDate(history.timeOffset).dateHourMinute()

                val cgmHistoryEntity = CgmHistoryEntity()
                cgmHistoryEntity.deviceId = deviceId
                if (history.isValid == 0) {
                    cgmHistoryEntity.eventType = History.HISTORY_INVALID
                } else {
                    when (history.status) {
                        History.STATUS_OK -> {
                            cgmHistoryEntity.eventType = History.HISTORY_GLUCOSE
                        }
                        History.STATUS_INVALID -> {
                            cgmHistoryEntity.eventType = History.HISTORY_GLUCOSE_INVALID
                        }
                        History.STATUS_ERROR -> {
                            cgmHistoryEntity.eventType = History.HISTORY_SENSOR_ERROR
                        }
                    }
                }
                cgmHistoryEntity.eventData = roundOffDecimal(history.glucose / 18f)
                cgmHistoryEntity.eventIndex = history.timeOffset
                cgmHistoryEntity.time = getHistoryDate(history.timeOffset)
                cgmHistoryEntity.deviceTime = getHistoryDate(history.timeOffset)
                cgmHistoryEntity.sensorIndex = (entity.sensorStartTime?.time!! / 1000).toInt()
                if (history.timeOffset <= 60) {
                    cgmHistoryEntity.eventWarning = -1
                }
                when (cgmHistoryEntity.eventType) {
                    History.HISTORY_GLUCOSE,
                    -> {
                        if (Constant.sensorInsetError || Constant.sensorInsetErrorSuper || cgmHistoryEntity.eventWarning == -1) {
                            cgmHistoryEntity.eventWarning = -1
                        } else {
                            if (cgmHistoryEntity.isHighOrLowGlucose()) {
                                val build = cgmHistoryBox.query()
//                                        .equal(CgmHistoryEntity_.sensorIndex, entity.sensorIndex)
                                    .equal(
                                        CgmHistoryEntity_.deviceId,
                                        entity.id ?: "",
                                        QueryBuilder.StringOrder.CASE_INSENSITIVE
                                    ).equal(
                                        CgmHistoryEntity_.authorizationId,
                                        UserManager.instance().getUserId(),
                                        QueryBuilder.StringOrder.CASE_INSENSITIVE
                                    )
                                when {
                                    cgmHistoryEntity.getHighOrLowGlucoseType() == 2 -> build.equal(
                                        CgmHistoryEntity_.eventWarning, History.HISTORY_LOCAL_HYPER
                                    )
                                    cgmHistoryEntity.getHighOrLowGlucoseType() == 1 -> build.equal(
                                        CgmHistoryEntity_.eventWarning, History.HISTORY_LOCAL_HYPO
                                    )
                                    cgmHistoryEntity.getHighOrLowGlucoseType() == 3 -> {
                                        build.equal(
                                            CgmHistoryEntity_.eventWarning,
                                            History.HISTORY_LOCAL_URGENT_HYPO
                                        )
                                    }
                                }
                                val findFirst =
                                    build.orderDesc(CgmHistoryEntity_.idx).build().findFirst()
                                if (findFirst != null) {
                                    lastAlertDeviceTime = findFirst.deviceTime.time
                                    if (abs(cgmHistoryEntity.deviceTime.time - lastAlertDeviceTime) / 1000 >= calculateFrequency(
                                            if ((cgmHistoryEntity.eventData
                                                    ?: 0F) > URGENT_HYPO
                                            ) MMKV.defaultMMKV().decodeInt(
                                                LocalPreference.NOTICE_FREQUENCY, 2
                                            ) else MMKV.defaultMMKV().decodeInt(
                                                LocalPreference.URGENT_NOTICE_FREQUENCY, 0
                                            )
                                        )
                                    ) {
                                        cgmHistoryEntity.updateEventWarning()
                                        lastAlertDeviceTime = cgmHistoryEntity.deviceTime.time
                                    }
                                } else {
                                    cgmHistoryEntity.updateEventWarning()
                                    lastAlertDeviceTime = cgmHistoryEntity.deviceTime.time
                                }
                            }

                            if (now - cgmHistoryEntity.deviceTime.time / 1000 <= calculateFrequency(
                                    MMKV.defaultMMKV()
                                        .decodeInt(LocalPreference.NOTICE_FREQUENCY, 2)
                                )
                            ) {
                                if (cgmHistoryEntity.eventWarning == History.HISTORY_LOCAL_HYPER) {
                                    if (MMKV.defaultMMKV()
                                            .decodeBool(LocalPreference.HIGH_NOTICE_ENABLE, true)
                                    ) {
                                        alert?.invoke(
                                            "$time", Constant.MESSAGE_TYPE_GLUCOSEHIGH
                                        )
                                    }
                                }

                                if (cgmHistoryEntity.eventWarning == History.HISTORY_LOCAL_HYPO) {
                                    if (MMKV.defaultMMKV()
                                            .decodeBool(LocalPreference.LOW_NOTICE_ENABLE, true)
                                    ) {
                                        alert?.invoke(
                                            "$time", Constant.MESSAGE_TYPE_GLUCOSELOW
                                        )
                                    }
                                }

                                if (cgmHistoryEntity.eventWarning == History.HISTORY_LOCAL_URGENT_HYPO) {
                                    if (MMKV.defaultMMKV()
                                            .decodeBool(LocalPreference.URGENT_NOTICE_ENABLE, true)
                                    ) {
                                        alert?.invoke(
                                            "$time", Constant.MESSAGE_TYPE_GLUCOSELOWALERT
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
                        if (now - cgmHistoryEntity.deviceTime.time / 1000 < 60 * 30 && (Constant.sensorInsetError || Constant.sensorInsetErrorSuper)) {
                            alert?.invoke(
                                "$time", Constant.MESSAGE_TYPE_SENRORERROR
                            )
                        }
                    }
                }
                cgmHistoryEntity.updateRecordUUID()
                cgmHistoryEntity.authorizationId = UserManager.instance().getUserId()
                cgmHistoryBox.put(cgmHistoryEntity)
                mutableListOf.add(cgmHistoryEntity)
                BroadCastManager.getInstance().dataReceivedFromTransmitter(
                    CgmsApplication.instance,
                    cgmHistoryEntity,
                    if (now - cgmHistoryEntity.deviceTime.time / 1000 < 6 * 60) glucose
                        ?: 0f else cgmHistoryEntity.eventData ?: 0f
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


    private fun sensorError() {
        if (Constant.sensorInsetErrorSuper) {
            return
        }
        if (sensorStart4HourLast()) {
            val historyLast = cgmHistoryBox.query().equal(
                CgmHistoryEntity_.eventIndex, 1
            ).and().equal(CgmHistoryEntity_.sensorIndex, sensorStartIndex!!).equal(
                CgmHistoryEntity_.deviceId,
                getDeviceID() ?: "",
                QueryBuilder.StringOrder.CASE_INSENSITIVE
            ).equal(
                CgmHistoryEntity_.authorizationId,
                UserManager.instance().getUserId(),
                QueryBuilder.StringOrder.CASE_INSENSITIVE
            ).orderDesc(CgmHistoryEntity_.deviceTime).build().findFirst()
            val historyError =
                cgmHistoryBox.query().equal(CgmHistoryEntity_.sensorIndex, sensorStartIndex!!)
                    .equal(
                        CgmHistoryEntity_.deviceId,
                        getDeviceID() ?: "",
                        QueryBuilder.StringOrder.CASE_INSENSITIVE
                    ).equal(
                        CgmHistoryEntity_.eventType, History.HISTORY_SENSOR_ERROR
                    ).equal(
                        CgmHistoryEntity_.authorizationId,
                        UserManager.instance().getUserId(),
                        QueryBuilder.StringOrder.CASE_INSENSITIVE
                    ).greater(CgmHistoryEntity_.idx, historyLast?.idx ?: 0L)
                    .orderDesc(CgmHistoryEntity_.idx).build().find()

            LogUtils.data("查询传感器故障次数 ${historyError.size} ， $historyError")
            if (historyError.size >= 4) {
                for ((index, item) in historyError.withIndex()) {
                    if (index >= 3) {
                        val durationTime =
                            item.deviceTime.time - historyError[index - 3].deviceTime.time
                        if (!Constant.sensorInsetErrorSuper && abs(durationTime) <= 2 * 60 * 60 * 1000) {
                            updateSensorState(true)
                            isNeedTipInsertErrorDialog = true //需要植入失败弹框，下一次接收广播提示
                        }
                    }
                }
            }
        } else {
            LogUtils.data("查询传感器故障 时间在4小时候内")
        }

    }


    fun errorInsertError() {
        //保证时间在22分钟到4小时范围内
        if (sensorStartTime == null) {
            LogUtils.eAiDex("获取的sensor start time是空的")
            return
        }
        Constant.sensorInsetError = false
        CgmsApplication.boxStore.runInTxAsync({
            val impedanceList = cgmHistoryBox.query().equal(
                CgmHistoryEntity_.authorizationId,
                UserManager.instance().getUserId(),
                QueryBuilder.StringOrder.CASE_INSENSITIVE
            ).and().equal(
                CgmHistoryEntity_.deviceId,
                getDeviceID() ?: "",
                QueryBuilder.StringOrder.CASE_INSENSITIVE
            ).and().equal(CgmHistoryEntity_.sensorIndex, targetSensorIndex).and()
                .equal(CgmHistoryEntity_.eventType, History.HISTORY_IMPENDANCE).between(
                    CgmHistoryEntity_.deviceTime,
                    sensorStartTime!!.time + 22 * 60 * 1000,
                    sensorStartTime!!.time + 4 * 60 * 60 * 1000
                ).notNull(CgmHistoryEntity_.rawData2).greater(CgmHistoryEntity_.rawData2, 300)
                .order(CgmHistoryEntity_.eventIndex).build().find()
            if (impedanceList.isNotEmpty()) {
                out@ for (entity in impedanceList) {
                    val histories = cgmHistoryBox.query().equal(
                        CgmHistoryEntity_.authorizationId,
                        UserManager.instance().getUserId(),
                        QueryBuilder.StringOrder.CASE_INSENSITIVE
                    ).equal(
                        CgmHistoryEntity_.deviceId,
                        getDeviceID() ?: "",
                        QueryBuilder.StringOrder.CASE_INSENSITIVE
                    ).equal(CgmHistoryEntity_.sensorIndex, targetSensorIndex)
                        .between(CgmHistoryEntity_.eventType, 7, 9)
                        .notNull(CgmHistoryEntity_.rawData5).less(CgmHistoryEntity_.rawData5, 3)
                        .notNull(CgmHistoryEntity_.rawData6).less(CgmHistoryEntity_.rawData6, 3)
                        .notNull(CgmHistoryEntity_.rawData7).less(CgmHistoryEntity_.rawData7, 3)
                        .notNull(CgmHistoryEntity_.rawData8).less(CgmHistoryEntity_.rawData8, 3)
                        .notNull(CgmHistoryEntity_.rawData9).less(CgmHistoryEntity_.rawData9, 3)
                        .between(
                            CgmHistoryEntity_.deviceTime,
                            if (entity.deviceTime.time - 15 * 60 * 1000 < sensorStartTime!!.time + 22 * 60 * 1000) sensorStartTime!!.time + 22 * 60 * 1000
                            else entity.deviceTime.time - 15 * 60 * 1000,
                            if (entity.deviceTime.time + 15 * 60 * 1000 > sensorStartTime!!.time + 4 * 60 * 60 * 1000) sensorStartTime!!.time + 4 * 60 * 60 * 1000
                            else entity.deviceTime.time + 15 * 60 * 1000
                        ).order(CgmHistoryEntity_.deviceTime).build().find()
                    if (histories.isNotEmpty()) {
                        var lastHis = histories[0]
                        for (his in histories) {
                            if (his.deviceTime.time - lastHis.deviceTime.time < 6 * 60 * 1000) {
                                val time = Date().dateHourMinute()
                                //传感器植入失败
                                Constant.sensorInsetError = true
                                updateSensorState(true)
                                alert?.invoke("$time", Constant.MESSAGE_TYPE_SENROR_EMBEDDING)
                                break@out
                            } else {
                                lastHis = his
                            }
                        }
                    }
                }
            }
        }) { _, _ ->

        }
    }


    fun errorSensorTimeRange(): Boolean {
        LogUtils.data("$sensorStartTime  recentAdv ${recentAdvertise?.datetime}")
        return sensorStartTime != null && recentAdvertise != null && ((recentAdvertise!!.datetime - sensorStartTime!!.time / 1000 >= 22 * 60) && (recentAdvertise!!.datetime - sensorStartTime!!.time / 1000 <= 4 * 60 * 60))
    }

    //点击新旧传感器4小时候之后
    fun sensorStart4HourLast(): Boolean {
        return sensorStartTime != null && recentAdvertise != null && (recentAdvertise!!.datetime - sensorStartTime!!.time / 1000 >= 4 * 60 * 60)
    }

    private fun saveFullHistories(cgmHistories: List<AidexXRawHistoryEntity>) {
        val initlast = entity.fullEventIndex
        fullSensorIndex = targetSensorIndex
        entity.fullSensorIndex = fullSensorIndex
        entity.fullEventIndex = cgmHistories.last().timeOffset
        nextFullEventIndex = entity.fullEventIndex + 1

        CgmsApplication.boxStore.runInTxAsync({
            for (history in cgmHistories) {
                val oldHistory = cgmHistoryBox.query()
//                    .equal(CgmHistoryEntity_.sensorIndex, history.sensorIndex)
                    .equal(CgmHistoryEntity_.eventIndex, history.timeOffset)
//                    .equal(CgmHistoryEntity_.deviceTime, history.deviceTime)
                    .equal(
                        CgmHistoryEntity_.deviceId,
                        getDeviceID() ?: "",
                        QueryBuilder.StringOrder.CASE_INSENSITIVE
                    ).equal(
                        CgmHistoryEntity_.authorizationId,
                        UserManager.instance().getUserId(),
                        QueryBuilder.StringOrder.CASE_INSENSITIVE
                    ).orderDesc(CgmHistoryEntity_.idx).build().findFirst()

                val cgmHistoryEntity = CgmHistoryEntity()
                cgmHistoryEntity.eventIndex = history.timeOffset
                cgmHistoryEntity.deviceTime = getHistoryDate(history.timeOffset)
                cgmHistoryEntity.rawData1 = history.i1
                cgmHistoryEntity.rawData2 = history.i2
                cgmHistoryEntity.rawData3 = history.vc
                cgmHistoryEntity.dataStatus = 1
                if (oldHistory != null) {
                    LogUtils.eAiDex("History exist,full ${oldHistory.recordIndex} -- ${oldHistory.deviceId}")
                    cgmHistoryEntity.recordIndex = oldHistory.recordIndex
                    cgmHistoryEntity.id = oldHistory.id
                    cgmHistoryEntity.idx = oldHistory.idx
                    cgmHistoryEntity.eventWarning = oldHistory.eventWarning
                    cgmHistoryEntity.eventData = oldHistory.eventData
                    cgmHistoryEntity.eventType = oldHistory.eventType
                }
                cgmHistoryEntity.updateRecordUUID()
                cgmHistoryEntity.deviceId = getDeviceID()
                cgmHistoryEntity.authorizationId = UserManager.instance().getUserId()
                LogUtils.debug("save full history :$history")
                cgmHistoryBox.put(cgmHistoryEntity)
            }
        }) { _, error ->
            if (error == null && UserManager.instance().isLogin()) {
                transmitterBox.put(entity)
                LogUtils.data("errorSensorTimeRange ," + errorSensorTimeRange())
//                if (!Constant.sensorInsetErrorSuper) {
//                    errorInsertError()
//                }
            } else {
                if (error != null) {
                    nextFullEventIndex = initlast
                    entity.fullEventIndex = initlast
                }
            }
        }
    }

    fun getGlucoseLevel(glucose: Float?): GlucoseLevel? {
        return when {
            glucose == null -> null
            glucose > ThresholdManager.hyper -> GlucoseLevel.HIGH
            glucose < ThresholdManager.hypo -> GlucoseLevel.LOW
            else -> GlucoseLevel.NORMAL
        }
    }

    //"5分钟", "15分钟", "30分钟", "45分钟", "60分钟"
    fun calculateFrequency(index: Int): Long {
        return when (index) {
            0 -> 5 * 60
            1 -> 15 * 60
            2 -> 30 * 60
            3 -> 45 * 60
            4 -> 60 * 60
            else -> 30 * 60
        }
    }

    /***
     *
     * @param sNeedToast (是否需要弹框提示，再切换语言重新加载历史数据的时候不需要)
     *
     * **/
    private fun updateGlucoseTrend(dateTime: Date, isNeedToast: Boolean = true) {
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