package com.microtech.aidexx.data

import com.google.gson.ExclusionStrategy
import com.google.gson.FieldAttributes
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.microtech.aidexx.common.equal
import com.microtech.aidexx.common.net.ApiResult
import com.microtech.aidexx.common.net.ApiService
import com.microtech.aidexx.common.net.entity.BaseResponse
import com.microtech.aidexx.common.net.repository.EventRepository
import com.microtech.aidexx.common.user.UserInfoManager
import com.microtech.aidexx.db.ObjectBox
import com.microtech.aidexx.db.entity.RealCgmHistoryEntity
import com.microtech.aidexx.db.entity.RealCgmHistoryEntity_
import com.microtech.aidexx.db.repository.CgmCalibBgRepository
import com.microtech.aidexx.utils.LogUtil
import com.microtech.aidexx.utils.mmkv.MmkvManager
import io.objectbox.Property
import io.objectbox.kotlin.awaitCallInTx
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody

private const val TYPE_BRIEF = 0
private const val TYPE_RAW = 1
private const val HISTORY_ONCE_UPLOAD_NUMBER = 500L

object CloudCgmHistorySync : CloudHistorySync<RealCgmHistoryEntity>() {
    override val idx: Property<RealCgmHistoryEntity> = RealCgmHistoryEntity_.idx
    override val id: Property<RealCgmHistoryEntity> = RealCgmHistoryEntity_.id
    override val frontRecordId: Property<RealCgmHistoryEntity> = RealCgmHistoryEntity_.frontRecordId
    override val userId: Property<RealCgmHistoryEntity> =
        RealCgmHistoryEntity_.userId
    private val briefUploadState: Property<RealCgmHistoryEntity> =
        RealCgmHistoryEntity_.briefUploadState
    private val rawUploadState: Property<RealCgmHistoryEntity> =
        RealCgmHistoryEntity_.rawUploadState
    override val deleteStatus: Property<RealCgmHistoryEntity> = RealCgmHistoryEntity_.deleteStatus
    override val uploadState: Property<RealCgmHistoryEntity>
        get() = RealCgmHistoryEntity_.uploadState
    val eventWarning: Property<RealCgmHistoryEntity> = RealCgmHistoryEntity_.eventWarning
    private var gsonBuilder: Gson? = null

    override suspend fun postLocalData(map: HashMap<String, MutableList<RealCgmHistoryEntity>>): BaseResponse<List<RealCgmHistoryEntity>>? {
        return null
    }

    private suspend fun postBriefData(body: RequestBody): BaseResponse<List<RealCgmHistoryEntity>>? {
        return when (val postHistory = ApiService.instance.postBriefHistory(body)) {
            is ApiResult.Success -> {
                postHistory.result
            }
            is ApiResult.Failure -> {
                null
            }
        }
    }

    override suspend fun getRemoteData(userId: String): List<RealCgmHistoryEntity>? =
        when (val apiResult = EventRepository.getCgmRecordsByPageInfo(
            userId = userId,
            endAutoIncrementColumn = MmkvManager.getEventDataMinId<Long>(getDataSyncFlagKey(userId))?.let { it - 1 },
        )) {
            is ApiResult.Success -> apiResult.result.data
            is ApiResult.Failure -> null
        }

    private suspend fun getNeedUploadData(type: Int = 0): MutableList<RealCgmHistoryEntity>? {
        val userId = UserInfoManager.instance().userId()
        val query = entityBox.query()
        when (type) {
            TYPE_BRIEF -> query
                .equal(briefUploadState, 1)
                .equal(
                    this.userId,
                    userId,
                )

            TYPE_RAW -> query
                .equal(briefUploadState, 2)
                .equal(this.userId, userId)
                .equal(rawUploadState, 1)
        }
        return ObjectBox.store.awaitCallInTx {
            query.order(idx).build().find(0, HISTORY_ONCE_UPLOAD_NUMBER)
        }
    }

    private suspend fun updateHistory(map: HashMap<String, MutableList<RealCgmHistoryEntity>>): BaseResponse<List<RealCgmHistoryEntity>>? {
        return when (val updateHistory = ApiService.instance.updateHistory(map)) {
            is ApiResult.Success -> {
                updateHistory.result
            }
            is ApiResult.Failure -> {
                null
            }
        }
    }

    override suspend fun upload() {
        if (gsonBuilder == null) {
            initGsonBuilder()
        }
        val needUploadBriefData = getNeedUploadData(TYPE_BRIEF)
        needUploadBriefData?.let { list ->
            if (list.size > 0) {
                val records = hashMapOf("records" to list)
                val toJson = gsonBuilder!!.toJson(records)
                val toRequestBody = toJson.toRequestBody("application/json".toMediaType())
                LogUtil.eAiDEX("Upload brief History: timeOffset: ${list[0].timeOffset} - ${list[list.size - 1].timeOffset}")
                withContext(Dispatchers.IO) {
                    val briefResponse = postBriefData(toRequestBody)
                    briefResponse?.let { response ->
                        response.data?.let {
                            replaceEventData(list, it, 1)
                        }
                    }
                }
            }
        }
        val needUploadRawData = getNeedUploadData(TYPE_RAW)
        needUploadRawData?.let { list ->
            if (list.size > 0) {
                LogUtil.eAiDEX("Upload raw history: timeOffset: ${list[0].timeOffset} - ${list[list.size - 1].timeOffset}")
                withContext(Dispatchers.IO) {
                    val rawResponse = updateHistory(hashMapOf("records" to list))
                    rawResponse?.let { response ->
                        response.data?.let { data ->
                            replaceEventData(list, data, 2)
                        }
                    }
                }
            }
        }
    }

    private fun initGsonBuilder() {
        gsonBuilder = GsonBuilder().addSerializationExclusionStrategy(object : ExclusionStrategy {
            override fun shouldSkipField(field: FieldAttributes?): Boolean {
                if ("rawOne" == field?.name) return true
                if ("rawTwo" == field?.name) return true
                if ("rawVc" == field?.name) return true
                if ("rawIsValid" == field?.name) return true
                if ("calibrationIsValid" == field?.name) return true
                if ("cf" == field?.name) return true
                if ("index" == field?.name) return true
                if ("offset" == field?.name) return true
                return false
            }

            override fun shouldSkipClass(clazz: Class<*>?): Boolean {
                return false
            }
        }).create()
    }

    override suspend fun replaceEventData(
        origin: MutableList<RealCgmHistoryEntity>,
        responseList: List<RealCgmHistoryEntity>?,
        type: Int,
        userId: String?,
    ) {
        responseList?.let {
            if (responseList.isNotEmpty()) {
                val tempList = mutableListOf<RealCgmHistoryEntity>()
                for (entity in responseList) {
                    entity.frontRecordId?.let { frontRecordId ->
                        val oldEntity = ObjectBox.cgmHistoryBox!!.query()
                            .equal(RealCgmHistoryEntity_.frontRecordId, frontRecordId)
                            .order(RealCgmHistoryEntity_.idx).build().findFirst()
                        when (type) {
                            1 -> {
                                oldEntity?.let { old ->
                                    old.briefUploadState = 2
                                    if (old.autoIncrementColumn == 0L)
                                        old.autoIncrementColumn = entity.autoIncrementColumn
                                    if (old.cgmRecordId == null)
                                        old.cgmRecordId = entity.cgmRecordId
                                    tempList.add(oldEntity)
                                }
                            }
                            2 -> {
                                oldEntity?.let { old ->
                                    old.rawUploadState = 2
                                    tempList.add(oldEntity)
                                }
                            }
                            else -> {}
                        }
                    }
                    3 -> { // 下载
                        CgmCalibBgRepository.insertCgm(responseList)
                        MmkvManager.setEventDataMinId(
                            getDataSyncFlagKey(userId!!),
                            responseList.last().autoIncrementColumn
                        )
                    }
                }
                entityBox.put(tempList)
            }
        }
    }
}
