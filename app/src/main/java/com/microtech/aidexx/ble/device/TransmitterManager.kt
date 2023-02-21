package com.microtech.aidexx.ble.device

import com.microtech.aidexx.ble.device.entity.TransmitterEntity
import com.microtech.aidexx.ble.device.entity.TransmitterEntity_
import com.microtech.aidexx.db.ObjectBox
import com.microtech.aidexx.db.ObjectBox.transmitterBox
import com.microtech.aidexx.db.entity.CgmHistoryEntity
import java.util.*

class TransmitterManager private constructor() {

    companion object {
        private var instance: TransmitterManager? = null
            get() {
                if (field == null) {
                    field = TransmitterManager()
                }
                return field
            }

        @Synchronized
        fun instance(): TransmitterManager {
            return instance!!
        }
    }

    private var defaultModel: TransmitterModel? = null
    private val transmitters: MutableMap<String, TransmitterModel> = HashMap()

    var cgmHistories: MutableList<CgmHistoryEntity> = ArrayList()
        private set

    var onLoadTransmitterListener: ((TransmitterModel) -> Unit)? = null
    var onLoadHistoriesListener: ((List<CgmHistoryEntity>) -> Unit)? = null
    var onUpdateHistoriesListener: ((List<CgmHistoryEntity>) -> Unit)? = null


    fun loadTransmitter(): TransmitterEntity? {
        return transmitterBox!!.query().orderDesc(TransmitterEntity_.idx)
            .build()
            .findFirst()
    }

    fun clearDb() {
        ObjectBox.store.runInTxAsync({
            transmitterBox!!.removeAll()
        }) { _, error ->
            if (error == null) {
                onLoadHistoriesListener?.invoke(cgmHistories)
            }
        }
    }

//    fun loadHistory() {
//        CgmsApplication.boxStore.runInTxAsync({
//            val builder = cgmHistoryBox.query()
//            if (UserManager.shareUserEntity == null) builder.equal(
//                CgmHistoryEntity_.authorizationId,
//                UserManager.instance().getUserId(),
//                QueryBuilder.StringOrder.CASE_INSENSITIVE
//            )
//            else builder.equal(
//                CgmHistoryEntity_.authorizationId, UserManager.shareUserEntity?.id ?: "",
//                QueryBuilder.StringOrder.CASE_INSENSITIVE
//            )
//            cgmHistories = builder
//                .equal(CgmHistoryEntity_.type, 0)
//                .isNull(CgmHistoryEntity_.eventWarning).or()
//                .notEqual(CgmHistoryEntity_.eventWarning, -1)
//                .order(CgmHistoryEntity_.idx)
//                .build()
//                .find()
//        }) { _, error ->
//            if (error == null) {
//                onLoadHistoriesListener?.invoke(cgmHistories)
//            }
//        }
//    }

    fun getDefault(): TransmitterModel? {
        return when {
            defaultModel != null -> defaultModel
            transmitters.isNotEmpty() -> {
                transmitters.values.last()
            }
            else -> null
        }
    }


    fun removeDefaultModel() {
        defaultModel = null
    }

//    fun getModel(sn: String): CgmModel {
//        if (sn != defaultModel?.entity?.deviceSn) {
//            val entity = TransmitterEntity(sn)
//            entity.hyperThreshold = Constant.hyper_value
//            entity.hypoThreshold = Constant.hypo_value
//            LogUtils.data("getModel HI ${entity.hyperThreshold} LO ${entity.hypoThreshold}")
//            defaultModel = transmitters[sn] ?: CgmModel(
//                entity
//            )
//        }
//        LogUtils.data("getModel HI ${defaultModel?.entity?.hyperThreshold} LO ${defaultModel?.entity?.hypoThreshold}")
//        return defaultModel!!
//    }

    fun set(model: TransmitterModel) {
        defaultModel = model
        model.controller.register()
        onLoadTransmitterListener?.invoke(model)
    }

    fun update(model: TransmitterModel) {
        if (model.entity.idx != null) {
            transmitterBox!!.put(model.entity)
        }
    }


//    fun remove(model: CgmModel) {
//
//        model.controller.unregister()
//        transmitters.remove(model.entity.deviceSn)
//
////        onLoadTransmitterListener?.invoke(model)
//    }

    fun updateHistories(cgmHistories: List<CgmHistoryEntity>) {
        this.cgmHistories.addAll(cgmHistories)
        onUpdateHistoriesListener?.invoke(cgmHistories)
    }

    fun getIdleTime(): Long {
        var idleTime = 300L
        var found = false
        val now = Date().time / 1000
        for (transmitter in transmitters.values) {
            if (now - transmitter.lastAdvertiseTime < 10) {
                found = true
            }
            if (transmitter.nextHistoryTime - now < idleTime) {
                idleTime = transmitter.nextHistoryTime - now
            }
        }
        return if (!found) {
            -1L
        } else if (idleTime < 0 || idleTime > 300) {
            0L
        } else {
            idleTime
        }
    }
}