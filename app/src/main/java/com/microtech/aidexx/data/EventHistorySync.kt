package com.microtech.aidexx.data

import com.microtech.aidexx.common.net.ApiResult
import com.microtech.aidexx.common.net.entity.PAGE_SIZE
import com.microtech.aidexx.common.net.repository.EventRepository
import com.microtech.aidexx.db.entity.BaseEventEntity
import com.microtech.aidexx.utils.LogUtil

abstract class EventHistorySync<T : BaseEventEntity> : DataSyncController<T>() {


    private suspend fun saveOrUpload(data: List<T>): MutableList<T>? =
        when (val apiResult = EventRepository.saveOrUpdateRecords(data)) {
            is ApiResult.Success -> apiResult.result.data as MutableList<T>
            is ApiResult.Failure -> null
        }

    open suspend fun getRemoteData(userId: String): List<T>? =
        when (val apiResult = EventRepository.getEventRecordsByPageInfo(userId, PAGE_SIZE, getSyncMaxId(userId), tClazz)) {
            is ApiResult.Success -> apiResult.result.data as List<T>
            is ApiResult.Failure -> null
        }

    open suspend fun upload() {
        val needUploadData = getNeedUploadData()
        needUploadData?.ifEmpty { null }?.let {
            saveOrUpload(it)?.let { rspData ->
                updateEventDataAfterUpload(it, rspData)
            }
        }
    }

    override suspend fun downloadData(userId: String): Boolean {
        if (canSync()) {
            val result = getRemoteData(userId)
            return result?.let {
                if (it.isNotEmpty()) {
                    applyData(userId, it)
                }
                if (it.size >= PAGE_SIZE) {
                    LogUtil.d("===DATASYNC=== 开始下一页数据下载")
                    // todo 是否需要加个间隔 不然可能会很快
                    downloadData(userId)
                } else true
            } ?: false
        }
        return false
    }

    abstract suspend fun getNeedUploadData(): MutableList<T>?

    private suspend fun updateEventDataAfterUpload(
        origin: MutableList<T>,
        responseList: MutableList<T>
    ) {
        for (entity in origin) {
            entity.uploadState = 2
            if (entity.autoIncrementColumn == null) {
                responseList.find {
                    entity == it
                }?.let {
                    entity.autoIncrementColumn = it.autoIncrementColumn
                    responseList.remove(it)
                }
            }
        }
        insertToDb(origin, CloudDietHistorySync.tClazz)
    }

}