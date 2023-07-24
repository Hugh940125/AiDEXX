package com.microtech.aidexx.data

import com.microtech.aidexx.common.net.ApiResult
import com.microtech.aidexx.common.net.ApiService
import com.microtech.aidexx.common.net.entity.BaseResponse
import com.microtech.aidexx.db.entity.CalibrateEntity
import com.microtech.aidexx.db.entity.CalibrateEntity_
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


    override suspend fun postLocalData(map: HashMap<String, MutableList<CalibrateEntity>>): BaseResponse<List<CalibrateEntity>>? {
        if (!canDoHttpRequest(DataTaskType.Upload)) return null
        return when (val postCalHistory = ApiService.instance.postCalHistory(map)) {
            is ApiResult.Success -> {
                onHttpRequestSuccess(DataTaskType.Upload)
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