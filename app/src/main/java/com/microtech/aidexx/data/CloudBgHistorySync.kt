package com.microtech.aidexx.data

import com.microtech.aidexx.common.net.ApiResult
import com.microtech.aidexx.common.net.ApiService
import com.microtech.aidexx.common.net.entity.BaseResponse
import com.microtech.aidexx.db.entity.BloodGlucoseEntity
import com.microtech.aidexx.db.entity.BloodGlucoseEntity_
import io.objectbox.Property

object CloudBgHistorySync : CloudHistorySync<BloodGlucoseEntity>() {
    override val idx: Property<BloodGlucoseEntity>
        get() = BloodGlucoseEntity_.idx
    override val id: Property<BloodGlucoseEntity>
        get() = BloodGlucoseEntity_.id
    override val frontRecordId: Property<BloodGlucoseEntity>
        get() = BloodGlucoseEntity_.bloodGlucoseId
    override val userId: Property<BloodGlucoseEntity>
        get() = BloodGlucoseEntity_.userId
    override val deleteStatus: Property<BloodGlucoseEntity>
        get() = BloodGlucoseEntity_.deleteStatus
    override val uploadState: Property<BloodGlucoseEntity>
        get() = BloodGlucoseEntity_.uploadState


    override suspend fun postLocalData(map: HashMap<String, MutableList<BloodGlucoseEntity>>): BaseResponse<List<BloodGlucoseEntity>>? {
        if (!canDoHttpRequest(DataTaskType.Upload)) return null
        return when (val postHistory = ApiService.instance.postBgHistory(map)) {
            is ApiResult.Success -> {
                onHttpRequestSuccess(DataTaskType.Upload)
                postHistory.result
            }
            is ApiResult.Failure -> {
                null
            }
        }
    }

    override suspend fun replaceEventData(
        origin: MutableList<BloodGlucoseEntity>,
        responseList: List<BloodGlucoseEntity>?,
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