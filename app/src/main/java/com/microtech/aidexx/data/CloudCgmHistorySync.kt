package com.microtech.aidexx.data

import com.microtech.aidexx.common.equal
import com.microtech.aidexx.common.net.ApiResult
import com.microtech.aidexx.common.net.ApiService
import com.microtech.aidexx.common.net.entity.BaseList
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

private const val TYPE_BRIEF = 0
private const val TYPE_RAW = 1
private const val TYPE_CAL = 2
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
    private val calUploadState: Property<RealCgmHistoryEntity> =
        RealCgmHistoryEntity_.calUploadState
    override val deleteStatus: Property<RealCgmHistoryEntity> = RealCgmHistoryEntity_.deleteStatus
    override val uploadState: Property<RealCgmHistoryEntity>
        get() = RealCgmHistoryEntity_.uploadState
    val eventWarning: Property<RealCgmHistoryEntity> = RealCgmHistoryEntity_.eventWarning

    override suspend fun postLocalData(map: HashMap<String, MutableList<RealCgmHistoryEntity>>): BaseResponse<Nothing>? {
        return null
    }

    private suspend fun postBriefData(map: HashMap<String, MutableList<RealCgmHistoryEntity>>): BaseResponse<List<RealCgmHistoryEntity>>? {
        return when (val postHistory = ApiService.instance.postBriefHistory(map)) {
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
        //简要数据
        val needUploadBriefData = getNeedUploadData(TYPE_BRIEF)
        needUploadBriefData?.let { list ->
            if (list.size > 0) {
                LogUtil.eAiDEX("Upload brief History: size:${list.size}")
                withContext(Dispatchers.IO) {
                    val briefResponse = postBriefData(hashMapOf("records" to list))
                    briefResponse?.let { response ->
                        response.data?.let {
                            replaceEventData(list, it)
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
                            replaceEventData(needUploadRawData, data)
                        }
                    }
                }
            }
        }
    }

    override suspend fun replaceEventData(
        origin: MutableList<RealCgmHistoryEntity>,
        responseList: List<RealCgmHistoryEntity>,
        type: Int,
        authorId: String?,
    ) {
        if (type == 3) { //下载
            if (responseList.isNotEmpty()) {
                CgmCalibBgRepository.insertCgm(responseList)
                MmkvManager.setEventDataMinId(
                    getDataSyncFlagKey(authorId!!),
                    responseList.last().autoIncrementColumn
                )
            }
            return
        }
        if (origin.isNotEmpty()) {
            for ((index, old) in origin.withIndex()) {
                if (old.autoIncrementColumn == 0L)
                    old.autoIncrementColumn = responseList[index].autoIncrementColumn
                if (old.cgmRecordId == null)
                    old.cgmRecordId = responseList[index].cgmRecordId
                if (old.rawUploadState == 1)
                    old.briefUploadState = 2
                if (old.rawUploadState == 1)
                    old.rawUploadState = 2
                if (old.calUploadState == 1)
                    old.calUploadState = 2
            }
            entityBox.put(origin)
        }
    }
}
