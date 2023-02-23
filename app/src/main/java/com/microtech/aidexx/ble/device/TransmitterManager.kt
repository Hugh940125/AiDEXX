package com.microtech.aidexx.ble.device

import com.microtech.aidexx.ble.device.entity.TransmitterEntity
import com.microtech.aidexx.ble.device.entity.TransmitterEntity_
import com.microtech.aidexx.db.ObjectBox
import com.microtech.aidexx.db.ObjectBox.transmitterBox
import com.microtech.aidexx.db.entity.CgmHistoryEntity
import com.microtech.aidexx.utils.LogUtil
import io.objectbox.kotlin.awaitCallInTx
import io.objectbox.query.QueryBuilder

class TransmitterManager private constructor() {

    private var default: TransmitterModel? = null
    private val transmitters: HashMap<String, TransmitterModel> = hashMapOf()

    var cgmHistories: MutableList<CgmHistoryEntity> = ArrayList()
    var onTransmitterLoaded: ((TransmitterModel) -> Unit)? = null
    var onLoadHistoriesListener: ((List<CgmHistoryEntity>) -> Unit)? = null
    var onUpdateHistoriesListener: ((List<CgmHistoryEntity>) -> Unit)? = null

//    fun loadTransmitter(): TransmitterEntity? {
//        val transmitter = transmitterBox!!.query().orderDesc(TransmitterEntity_.idx)
//            .build()
//            .findFirst()
//        default = transmitter
//        return
//    }

    suspend fun getTransmitterFromDb(sn: String): TransmitterEntity? {
        return ObjectBox.store.awaitCallInTx {
            var findFirst: TransmitterEntity? = null
            try {
                findFirst = transmitterBox!!.query()
                    .equal(
                        TransmitterEntity_.deviceSn, sn,
                        QueryBuilder.StringOrder.CASE_INSENSITIVE
                    )
                    .build()
                    .findFirst()
            } catch (e: Exception) {
                LogUtil.eAiDEX("Failed to load transmitter from db")
            }
            findFirst
        }
    }

    fun clearDb() {
        ObjectBox.runAsync({
            transmitterBox!!.removeAll()
        }, onSuccess = {
            onLoadHistoriesListener?.invoke(cgmHistories)
        })
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
            default != null -> default
            transmitters.isNotEmpty() -> {
                transmitters.values.last()
            }
            else -> null
        }
    }


    fun removeDefaultModel() {
        default = null
    }

//    fun get(sn: String): TransmitterModel {
//        if (sn != default?.entity?.deviceSn) {
//            val entity = TransmitterEntity(sn)
//            entity.hyperThreshold = ThresholdManager.hyper
//            entity.hypoThreshold = ThresholdManager.hypo
//            default = transmitters[sn] ?: TransmitterModel(entity)
//        }
//        return default
//    }

    fun set(model: TransmitterModel) {
        default = model
        model.controller.register()
        onTransmitterLoaded?.invoke(model)
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
}