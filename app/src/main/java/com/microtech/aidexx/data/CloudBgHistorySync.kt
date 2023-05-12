package com.microtech.aidexx.data

import com.microtech.aidexx.common.formatWithZone
import com.microtech.aidexx.common.net.ApiResult
import com.microtech.aidexx.common.net.entity.BaseList
import com.microtech.aidexx.common.net.entity.BaseResponse
import com.microtech.aidexx.common.net.repository.EventRepository
import com.microtech.aidexx.db.entity.BloodGlucoseEntity
import com.microtech.aidexx.db.repository.CgmCalibBgRepository
import com.microtech.aidexx.utils.mmkv.MmkvManager
import io.objectbox.Property

object CloudBgHistorySync: CloudHistorySync<BloodGlucoseEntity>() {
    override val idx: Property<BloodGlucoseEntity>
        get() = TODO("Not yet implemented")
    override val id: Property<BloodGlucoseEntity>
        get() = TODO("Not yet implemented")
    override val url: String
        get() = TODO("Not yet implemented")
    override val frontRecordId: Property<BloodGlucoseEntity>
        get() = TODO("Not yet implemented")
    override val userId: Property<BloodGlucoseEntity>
        get() = TODO("Not yet implemented")
    override val deleteStatus: Property<BloodGlucoseEntity>
        get() = TODO("Not yet implemented")
    override val recordIndex: Property<BloodGlucoseEntity>
        get() = TODO("Not yet implemented")
    override val recordId: Property<BloodGlucoseEntity>
        get() = TODO("Not yet implemented")
    override val uuidValue: (BloodGlucoseEntity) -> String?
        get() = TODO("Not yet implemented")

    override suspend fun postLocalData(json: String): BaseResponse<BaseList<BloodGlucoseEntity>>? {
        TODO("Not yet implemented")
    }

    override suspend fun getRemoteData(userId: String): List<BloodGlucoseEntity>? =
        when (val apiResult = EventRepository.getBgRecordsByPageInfo(
            userId = userId,
            date = MmkvManager.getEventDataMinId<String>(getDataSyncFlagKey(userId)),
        )) {
            is ApiResult.Success -> apiResult.result.data
            is ApiResult.Failure -> null
        }

    override suspend fun replaceEventData(
        origin: MutableList<BloodGlucoseEntity>,
        responseList: List<BloodGlucoseEntity>,
        type: Int,
        userId: String?
    ) {
        if (type == 3) { //下载
            if (responseList.isNotEmpty()) {
                CgmCalibBgRepository.insertBg(responseList)
                MmkvManager.setEventDataMinId(
                    getDataSyncFlagKey(userId!!),
                    responseList.last().createTime.formatWithZone()
                )
            }
            return
        }
    }

}