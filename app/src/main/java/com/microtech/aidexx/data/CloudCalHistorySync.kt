package com.microtech.aidexx.data

import com.microtech.aidexx.common.net.ApiResult
import com.microtech.aidexx.common.net.ApiService
import com.microtech.aidexx.common.net.entity.BaseResponse
import com.microtech.aidexx.common.net.repository.EventRepository
import com.microtech.aidexx.db.entity.CalibrateEntity
import com.microtech.aidexx.db.entity.CalibrateEntity_
import com.microtech.aidexx.utils.mmkv.MmkvManager
import io.objectbox.Property

object CloudCalHistorySync : CloudHistorySync<CalibrateEntity>() {
    override val idx: Property<CalibrateEntity>
        get() = CalibrateEntity_.idx
    override val id: Property<CalibrateEntity>
        get() = CalibrateEntity_.id
    override val frontRecordId: Property<CalibrateEntity>
        get() = CalibrateEntity_.calibrationId
    override val userId: Property<CalibrateEntity>
        get() = CalibrateEntity_.userId
    override val deleteStatus: Property<CalibrateEntity>
        get() = CalibrateEntity_.deleteStatus
    override val uploadState: Property<CalibrateEntity>
        get() = CalibrateEntity_.uploadState

    override suspend fun getRemoteData(userId: String): List<CalibrateEntity>? =
        when (val apiResult = EventRepository.getCalRecordsByPageInfo(
            userId = userId,
            downAutoIncrementColumn = MmkvManager.getEventDataMinId<Long>(
                getDataSyncFlagKey(userId)
            )?.let { it - 1 },
        )) {
            is ApiResult.Success -> apiResult.result.data
            is ApiResult.Failure -> null
        }


    override suspend fun postLocalData(map: HashMap<String, MutableList<CalibrateEntity>>): BaseResponse<List<CalibrateEntity>>? {
        return when (val postCalHistory = ApiService.instance.postCalHistory(map)) {
            is ApiResult.Success -> {
                postCalHistory.result
            }
            is ApiResult.Failure -> {
                null
            }
        }
    }

    override suspend fun replaceEventData(
        origin: MutableList<CalibrateEntity>,
        responseList: List<CalibrateEntity>?,
        type: Int,
        userId: String?
    ) {
        responseList?.let {
            for ((index, entity) in origin.withIndex()) {
                entity.uploadState = 2
                entity.autoIncrementColumn = responseList[index].autoIncrementColumn
            }
            entityBox.put(origin)
        }
    }
}