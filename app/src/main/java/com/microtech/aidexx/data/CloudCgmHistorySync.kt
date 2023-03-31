package com.microtech.aidexx.data

import com.google.gson.GsonBuilder
import com.jeremyliao.liveeventbus.LiveEventBus
import com.microtech.aidexx.common.createWithDateFormat
import com.microtech.aidexx.common.equal
import com.microtech.aidexx.common.net.ApiService
import com.microtech.aidexx.common.net.UPLOAD_CGM_RECORD
import com.microtech.aidexx.common.net.entity.BaseList
import com.microtech.aidexx.common.net.entity.BasePageList
import com.microtech.aidexx.common.net.entity.BaseResponse
import com.microtech.aidexx.common.net.entity.RESULT_OK
import com.microtech.aidexx.common.user.UserInfoManager
import com.microtech.aidexx.db.ObjectBox
import com.microtech.aidexx.db.entity.RealCgmHistoryEntity
import com.microtech.aidexx.db.entity.RealCgmHistoryEntity_
import com.microtech.aidexx.db.entity.RealCgmHistoryEntity_.dataStatus
import com.microtech.aidexx.utils.LogUtil
import io.objectbox.Property
import io.objectbox.kotlin.awaitCallInTx
import io.objectbox.query.QueryBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.await
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.*

private const val TYPE_BRIEF = 0
private const val TYPE_RAW = 1
private const val HISTORY_NUMBER_ONCE_UPLOAD = 150L

class CloudCgmHistorySync : CloudHistorySync<RealCgmHistoryEntity>() {
    override val idx: Property<RealCgmHistoryEntity> = RealCgmHistoryEntity_.idx
    override val id: Property<RealCgmHistoryEntity> = RealCgmHistoryEntity_.id
    override val url: String = UPLOAD_CGM_RECORD
    override val recordUuid: Property<RealCgmHistoryEntity> = RealCgmHistoryEntity_.recordUuid
    override val authorizationId: Property<RealCgmHistoryEntity> =
        RealCgmHistoryEntity_.authorizationId
    override val deleteStatus: Property<RealCgmHistoryEntity> = RealCgmHistoryEntity_.deleteStatus
    override val recordIndex: Property<RealCgmHistoryEntity> = RealCgmHistoryEntity_.recordIndex
    override val recordId: Property<RealCgmHistoryEntity> = RealCgmHistoryEntity_.recordId
    override val uuidValue: (RealCgmHistoryEntity) -> String? = { it.recordUuid }
    val eventWarning: Property<RealCgmHistoryEntity> = RealCgmHistoryEntity_.eventWarning

    override suspend fun postLocalData(json: String): BaseResponse<BaseList<RealCgmHistoryEntity>>? {
        return ApiService.instance.postHistory(json).execute().body()
    }

    override suspend fun getRemoteData(authorizationId: String?): BaseResponse<BasePageList<RealCgmHistoryEntity>>? {
        val userId = UserInfoManager.instance().userId()
        val indexList = ObjectBox.store.awaitCallInTx {
            entityBox
                .query()
                .notNull(recordIndex)
                .equal(
                    this.authorizationId,
                    authorizationId ?: userId,
                    QueryBuilder.StringOrder.CASE_INSENSITIVE
                )
                .build().property(recordIndex).findLongs()
        }
        val index = if (indexList == null || indexList.isEmpty()) 0
        else indexList.max()
        val map = if (authorizationId != null)
            "{'greaterThan':{'recordIndex':${index}},'pageSize' : 500,'authorizationId' : ${authorizationId}}"
        else "{'greaterThan':{'recordIndex':${index}},'pageSize' : 500}"
        return ApiService.instance.getRemoteHistory(map).execute().body()
    }

    override suspend fun upload() {
        //简要数据
        val needUploadBriefData = getNeedUploadData(TYPE_BRIEF)
        if (needUploadBriefData.size > 0) {
            val json = GsonBuilder().createWithDateFormat().toJson(needUploadBriefData)
            LogUtil.eAiDEX("Upload brief History: size:${needUploadBriefData.size} - $json")
            withContext(Dispatchers.IO) {
                val briefResponse = postLocalData(json)
                briefResponse?.let { response ->
                    if (response.info.code == RESULT_OK) {
                        response.content?.let {
                            replaceEventData(needUploadBriefData, it.records, 1)
                        }
                    } else {
                        downloadData()
                    }
                }
            }
        }

        //原始数据
        val needUploadRawData = getNeedUploadData(TYPE_RAW)
        if (needUploadRawData.size > 0) {
            val json = GsonBuilder().createWithDateFormat().toJson(needUploadRawData)
            LogUtil.eAiDEX("Upload raw history: size:${needUploadRawData.size} - $json")
            withContext(Dispatchers.IO) {
                val rawResponse = putLocalData(json)
                rawResponse?.let { response ->
                    if (response.info.code == RESULT_OK) {
                        response.content?.let {
                            replaceEventData(needUploadRawData, it.records, 2)
                        }
                    } else {
                        downloadData()
                    }
                }
            }
        }
    }

    override suspend fun replaceEventData(
        origin: MutableList<RealCgmHistoryEntity>,
        responseList: MutableList<RealCgmHistoryEntity>,
        cgmStatus: Int,
        authorId: String?,
    ) {
        val userId =
            if (authorId.isNullOrBlank()) UserInfoManager.instance().userId() else authorId
        if (origin.isNotEmpty()) {
            for ((index, old) in origin.withIndex()) {
                when (cgmStatus) {
                    1 -> { //POST请求
                    }
                    2 -> {//PUT请求
                        old.dataStatus = 2
                    }
                    3 -> {//下载请求
                        old.dataStatus =
                            if (responseList[index].rawData1 == null && responseList[index].rawData2 == null) 0 else 2
                    }
                }
                if (old.id == null && responseList[index].id != null) {
                    old.id = responseList[index].id
                }
                if (responseList[index].recordIndex != null) {
                    old.recordIndex = responseList[index].recordIndex
                }
                old.authorizationId = userId
            }
            entityBox.put(origin)
        }
    }

    private suspend fun putLocalData(json: String): BaseResponse<BaseList<RealCgmHistoryEntity>>? {
        return ApiService.instance.putHistory(json).execute().body()
    }

    suspend fun getNeedUploadData(status: Int = 0): MutableList<RealCgmHistoryEntity> {
        val list = mutableListOf<RealCgmHistoryEntity>()
        val userId = UserInfoManager.instance().userId()
        val query = entityBox.query()
        when (status) {
            TYPE_BRIEF -> query //post请求
                .isNull(recordIndex)
                .and().equal(
                    authorizationId,
                    userId,
                    QueryBuilder.StringOrder.CASE_INSENSITIVE
                )

            TYPE_RAW -> query //put请求
                .notNull(recordIndex)
                .and().equal(
                    authorizationId,
                    userId,
                    QueryBuilder.StringOrder.CASE_INSENSITIVE
                )
                .and().notNull(recordId)
                .and().equal(dataStatus, 1)
        }
        val mutableList = ObjectBox.store.awaitCallInTx {
            query.order(idx).build().find(0, HISTORY_NUMBER_ONCE_UPLOAD)
        }
        mutableList?.let { it ->
            for ((index, item) in it.withIndex()) {
                item?.eventData?.let {
                    item.eventData = BigDecimal("${item.eventData}").setScale(
                        2,
                        RoundingMode.HALF_UP
                    ).toFloat()
                }
                if (status == TYPE_RAW) {
                    item?.recordIndex = null
                } else {
                    if (index == 0) {
                        val findLongs = ObjectBox.store.awaitCallInTx {
                            entityBox.query().notNull(recordIndex)
                                .and()
                                .equal(
                                    authorizationId,
                                    userId,
                                    QueryBuilder.StringOrder.CASE_INSENSITIVE
                                )
                                .build().property(recordIndex)
                                .findLongs()
                        }
                        val recordIndex =
                            if (findLongs == null || findLongs.isEmpty()) 0 else findLongs.max()
                        item.recordIndex = (recordIndex.plus(1))
                    }
                }
                list.add(item)
            }
        }
        return list
    }

    suspend fun downloadRecentCgmData(authorId: String? = null) {
        withContext(Dispatchers.IO) {
            val userid = authorId ?: UserInfoManager.instance().userId()
            //本地最大recordIndex
            val maxRecordIndex = getMaxRecordIndex(authorId)
            val result = getRecentCgmHistories(authorId, maxRecordIndex)
            if (result?.code == RESULT_OK) {
                //服务器最小的recordIndex
                val content = result.content
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
                    .equal(authorizationId, authorId)
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
                    .equal(authorizationId, userId)
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