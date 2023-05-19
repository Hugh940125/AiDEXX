package com.microtech.aidexx.data

import com.microtech.aidexx.common.net.ApiResult
import com.microtech.aidexx.common.net.ApiService
import com.microtech.aidexx.common.net.entity.BaseResponse
import com.microtech.aidexx.common.net.repository.EventRepository
import com.microtech.aidexx.db.entity.BloodGlucoseEntity
import com.microtech.aidexx.db.entity.BloodGlucoseEntity_
import com.microtech.aidexx.db.repository.CgmCalibBgRepository
import com.microtech.aidexx.utils.mmkv.MmkvManager
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
        return when (val postHistory = ApiService.instance.postBgHistory(map)) {
            is ApiResult.Success -> {
                postHistory.result
            }
            is ApiResult.Failure -> {
                null
            }
        }
    }

    override suspend fun getRemoteData(userId: String): List<BloodGlucoseEntity>? =
        when (val apiResult = EventRepository.getBgRecordsByPageInfo(
            userId = userId,
            downAutoIncrementColumn = MmkvManager.getEventDataMinId<Long>(getDataSyncFlagKey(userId))?.let { it - 1 },
        )) {
            is ApiResult.Success -> apiResult.result.data
            is ApiResult.Failure -> null
        }

    override suspend fun replaceEventData(
        origin: MutableList<BloodGlucoseEntity>,
        responseList: List<BloodGlucoseEntity>?,
        type: Int,
        userId: String?
    ) {
        responseList?.let {
            if (type == 3) { //下载
                if (responseList.isNotEmpty()) {
                    CgmCalibBgRepository.insertBg(responseList)
                    MmkvManager.setEventDataMinId(
                        getDataSyncFlagKey(userId!!),
                        responseList.last().autoIncrementColumn
                    )
                }
                return
            }
            for ((index, entity) in origin.withIndex()) {
                entity.uploadState = 2
                entity.autoIncrementColumn = responseList[index].autoIncrementColumn
            }
            entityBox.put(origin)
        }
    }

}