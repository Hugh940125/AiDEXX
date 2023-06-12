package com.microtech.aidexx.data

import com.microtech.aidexx.common.net.ApiResult
import com.microtech.aidexx.common.net.entity.PAGE_SIZE
import com.microtech.aidexx.common.net.repository.EventRepository
import com.microtech.aidexx.db.entity.BaseEventEntity
import com.microtech.aidexx.utils.LogUtil
import kotlinx.coroutines.delay

abstract class EventHistorySync<T : BaseEventEntity> : DataSyncController<T>() {

    companion object {
        private const val TAG = "EventHistorySync"
    }
    private suspend fun saveOrUpload(data: List<T>): MutableList<T>? =
        when (val apiResult = EventRepository.saveOrUpdateRecords(data)) {
            is ApiResult.Success -> apiResult.result.data as MutableList<T>
            is ApiResult.Failure -> null
        }

    open suspend fun getRemoteData(userId: String, syncTaskItem: SyncTaskItem): List<T>? =
        when (val apiResult = EventRepository.getEventRecordsByPageInfo(
            userId,
            PAGE_SIZE,
            startAutoIncrementColumn = syncTaskItem.startAutoIncrementColumn,
            endAutoIncrementColumn = syncTaskItem.endAutoIncrementColumn,
            tClazz)
        ) {
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

            val syncTaskItem = getFirstTaskItem(userId) ?:let {
                LogUtil.d("SyncTaskItemList=empty ${tClazz.simpleName}", TAG)
                return true
            }

            val result = getRemoteData(userId, syncTaskItem)
            return result?.let {
                if (it.isNotEmpty()) {
                    applyData(userId, it)
                }
                if (it.size >= PAGE_SIZE) {
                    // 更新第一条任务的起始点
                    syncTaskItem.endAutoIncrementColumn = it.last().autoIncrementColumn
                    updateFirstTaskItem(userId, syncTaskItem)
                } else {
                    // 数据量小于页大小 说明这个区间下载完毕 移除这条任务
                    removeFirstTaskItem(userId)
                }

                delay(DOWNLOAD_INTERVAL)
                LogUtil.d("===DATASYNC=== 开始下一页数据下载")
                downloadData(userId)
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