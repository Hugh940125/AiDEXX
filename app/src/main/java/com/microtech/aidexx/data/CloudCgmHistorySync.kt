package com.microtech.aidexx.data

import com.google.gson.GsonBuilder
import com.microtech.aidexx.common.createWithDateFormat
import com.microtech.aidexx.common.equal
import com.microtech.aidexx.common.net.ApiResult
import com.microtech.aidexx.common.net.ApiService
import com.microtech.aidexx.common.net.UPLOAD_CGM_RECORD
import com.microtech.aidexx.common.net.entity.BaseList
import com.microtech.aidexx.common.net.entity.BasePageList
import com.microtech.aidexx.common.net.entity.BaseResponse
import com.microtech.aidexx.common.net.entity.RESULT_OK
import com.microtech.aidexx.common.net.repository.EventRepository
import com.microtech.aidexx.common.user.UserInfoManager
import com.microtech.aidexx.db.ObjectBox
import com.microtech.aidexx.db.entity.RealCgmHistoryEntity
import com.microtech.aidexx.db.entity.RealCgmHistoryEntity_
import com.microtech.aidexx.db.repository.CgmCalibBgRepository
import com.microtech.aidexx.db.repository.CgmCalibBgRepository
import com.microtech.aidexx.utils.LogUtil
import com.microtech.aidexx.utils.mmkv.MmkvManager
import com.microtech.aidexx.utils.mmkv.MmkvManager
import io.objectbox.Property
import io.objectbox.kotlin.awaitCallInTx
import io.objectbox.query.QueryBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val TYPE_BRIEF = 0
private const val TYPE_RAW = 1
private const val TYPE_CAL = 2
private const val HISTORY_ONCE_UPLOAD_NUMBER = 150L

object CloudCgmHistorySync : CloudHistorySync<RealCgmHistoryEntity>() {
    override val idx: Property<RealCgmHistoryEntity> = RealCgmHistoryEntity_.idx
    override val id: Property<RealCgmHistoryEntity> = RealCgmHistoryEntity_.id
    override val url: String = UPLOAD_CGM_BRIEF
    override val frontRecordId: Property<RealCgmHistoryEntity> = RealCgmHistoryEntity_.frontRecordId
    override val userId: Property<RealCgmHistoryEntity> =
        RealCgmHistoryEntity_.userId
    private val briefUploadState: Property<RealCgmHistoryEntity> =
        RealCgmHistoryEntity_.briefUploadState
    private val rawUploadState: Property<RealCgmHistoryEntity> =
        RealCgmHistoryEntity_.rawUploadState
    private val calUploadState: Property<RealCgmHistoryEntity> =
        RealCgmHistoryEntity_.calUploadState
    override val deleteStatus: Property<RealCgmHistoryEntity> = RealCgmHistoryEntity_.deleteStatus
    override val recordIndex: Property<RealCgmHistoryEntity> = RealCgmHistoryEntity_.recordIndex
    override val recordId: Property<RealCgmHistoryEntity> = RealCgmHistoryEntity_.recordId
    override val uuidValue: (RealCgmHistoryEntity) -> String? = { it.frontRecordId }
    val eventWarning: Property<RealCgmHistoryEntity> = RealCgmHistoryEntity_.eventWarning

    override suspend fun postLocalData(json: String): BaseResponse<BaseList<RealCgmHistoryEntity>>? {
        return null
    }

    private suspend fun postBriefData(map: HashMap<String, MutableList<RealCgmHistoryEntity>>): BaseResponse<BaseList<RealCgmHistoryEntity>>? {
        return when (val postHistory = ApiService.instance.postBriefHistory(map)) {
            is ApiResult.Success -> {
                postHistory.result
            }
            is ApiResult.Failure -> {
                null
            }
        }
    }

    override suspend fun getRemoteData(authorizationId: String): List<RealCgmHistoryEntity>? =
        when (val apiResult = EventRepository.getCgmRecordsByPageInfo(
            userId = authorizationId,
            startAutoIncrementColumn = MmkvManager.getEventDataMinId(getShareDataMinIdKey(authorizationId)))) {

            is ApiResult.Success -> apiResult.result.data

            is ApiResult.Failure -> null

        }

    override suspend fun upload() {
        //简要数据
        val needUploadBriefData = getNeedUploadData(TYPE_BRIEF)
        needUploadBriefData?.let { list ->
            if (list.size > 0) {
                LogUtil.eAiDEX("Upload brief History: size:${list.size}")
                withContext(Dispatchers.IO) {
                    val briefResponse = postBriefData(hashMapOf("records" to list))
                    briefResponse?.let { response ->
                        if (response.code == RESULT_OK) {
                            response.data?.let {
                                replaceEventData(list, it.records)
                            }
                        }
                    }
                }
            }
        }
        //原始数据
        val needUploadRawData = getNeedUploadData(TYPE_RAW)
        needUploadRawData?.let {
            if (needUploadRawData.size > 0) {
                LogUtil.eAiDEX("Upload raw history: size:${it.size}")
                withContext(Dispatchers.IO) {
                    val rawResponse = updateHistory(hashMapOf("records" to it))
                    rawResponse?.let { response ->
                        response.data?.let { data ->
                            replaceEventData(needUploadRawData, data.records)
                        }
                    }
                }
            }
        }
    }

    override suspend fun replaceEventData(
        origin: MutableList<RealCgmHistoryEntity>,
        responseList: MutableList<RealCgmHistoryEntity>,
        step: Int,
        authorId: String?,
    ) {
        if (step == 3) { //下载
            if (responseList.isNotEmpty()) {
                CgmCalibBgRepository.insert(responseList)
                MmkvManager.setEventDataMinId(
                    getShareDataMinIdKey(authorId!!),
                    responseList.last().autoIncrementColumn
                )
            }
            return
        }
        if (origin.isNotEmpty()) {
            for (old in origin) {
                if (old.rawUploadState == 1) {
                    old.briefUploadState = 2
                }
                if (old.rawUploadState == 1) {
                    old.rawUploadState = 2
                }
                if (old.calUploadState == 1) {
                    old.calUploadState = 2
                }
        }
            entityBox.put(origin)
    }

    private suspend fun updateHistory(map: HashMap<String, MutableList<RealCgmHistoryEntity>>): BaseResponse<BaseList<RealCgmHistoryEntity>>? {
        return when (val updateHistory = ApiService.instance.updateHistory(map)) {
            is ApiResult.Success -> {
                updateHistory.result
            }
            is ApiResult.Failure -> {
                null
            }
        }
    }

    private suspend fun getNeedUploadData(type: Int = 0): MutableList<RealCgmHistoryEntity>? {
        val userId = UserInfoManager.instance().userId()
        val query = entityBox.query()
        when (type) {
            TYPE_BRIEF -> query //post请求
                .equal(briefUploadState, 1)
                .equal(
                    this.userId,
                    userId,
                )

            TYPE_RAW -> query //put请求
                .equal(briefUploadState, 2)
                .equal(this.userId, userId)
                .equal(rawUploadState, 1)
                .or().equal(calUploadState, 1)
        }
        return ObjectBox.store.awaitCallInTx {
            query.order(idx).build().find(0, HISTORY_ONCE_UPLOAD_NUMBER)
        }
    }

    suspend fun downloadRecentCgmData(authorId: String? = null) {
        withContext(Dispatchers.IO) {
            val userid = authorId ?: UserInfoManager.instance().userId()
            //本地最大recordIndex
            val maxRecordIndex = getMaxRecordIndex(authorId)
            val result = getRecentCgmHistories(authorId, maxRecordIndex)
            if (result?.code == RESULT_OK) {
                //服务器最小的recordIndex
                val content = result.data
                content?.let {
                    val records = content.records
                    val minRecordIndex = if (records.isEmpty()) 1 else records[0].recordIndex ?: 1
                    if (maxRecordIndex < minRecordIndex) {
                        val min = maxRecordIndex + 1
                        val max = minRecordIndex - 1
                        if (min == max) {
                            val emptyHistory = RealCgmHistoryEntity()
                            emptyHistory.recordIndex = min
                            emptyHistory.type = 1
                            emptyHistory.authorizationId = userid
                            entityBox.put(emptyHistory) //设置空的占位历史记录
                        } else {
                            val historyList = mutableListOf<RealCgmHistoryEntity>()
                            for (index in min..max) {
                                val emptyHistory = RealCgmHistoryEntity()
                                emptyHistory.type = 1
                                emptyHistory.authorizationId = userid
                                emptyHistory.recordIndex = index
                                historyList.add(emptyHistory)
                            }
                            entityBox.put(historyList)
                        }
                    }
                    for (item in records) {
                        item.authorizationId = userid
                    }
                    entityBox.put(records)
                }
            }
        }
    }

    private suspend fun getMaxRecordIndex(authorId: String? = null): Long {
        val userId = UserInfoManager.instance().userId()
        val index: Long
        if (authorId != null) {
            val maxShare = ObjectBox.awaitCallInTx {
                entityBox
                    .query()
                    .equal(this.userId, authorId)
                    .notNull(recordIndex)
                    .build()
                    .property(recordIndex)
                    .max()
            }
            index = if (maxShare == Long.MIN_VALUE || maxShare == null) 1 else maxShare
        } else {
            val maxMine = ObjectBox.awaitCallInTx {
                entityBox.query()
                    .notNull(recordIndex)
                    .equal(this.userId, userId)
                    .build()
                    .property(recordIndex)
                    .max()
            }
            index = if (maxMine == Long.MIN_VALUE || maxMine == null) 1 else maxMine
        }
        return index
    }

    private suspend fun getRecentCgmHistories(
        authorId: String?,
        maxIndex: Long
    ): BaseResponse<BasePageList<RealCgmHistoryEntity>>? {
        if (authorId == null) {
            return ApiService.instance.getRecentHistories("recordIndex=$maxIndex").execute().body()
        }
        return ApiService.instance.getRecentHistories("authorizationId=$authorId&recordIndex=$maxIndex")
            .execute().body()
    }
}