package com.microtech.aidexx.data

import com.microtech.aidexx.common.net.ApiResult
import com.microtech.aidexx.common.net.entity.PAGE_SIZE
import com.microtech.aidexx.common.net.repository.EventRepository
import com.microtech.aidexx.db.entity.BaseEventEntity
import com.microtech.aidexx.db.repository.EventDbRepository
import com.microtech.aidexx.utils.LogUtil

abstract class EventHistorySync<T : BaseEventEntity> : DataSyncController<T>() {

    companion object {
        private const val TAG = "EventHistorySync"
    }
    private suspend fun saveOrUpload(data: List<T>): MutableList<T>? {
        if (!canDoHttpRequest(DataTaskType.Upload)) return null
        return when (val apiResult = EventRepository.saveOrUpdateRecords(data)) {
            is ApiResult.Success -> {
                onHttpRequestSuccess(DataTaskType.Upload)
                apiResult.result.data as MutableList<T>
            }
            is ApiResult.Failure -> null
        }
    }


    open suspend fun getRemoteData(userId: String, syncTaskItem: SyncTaskItem): List<T>? {
        if (!canDoHttpRequest(DataTaskType.Download)) return null
        return when (val apiResult = EventRepository.getEventRecordsByPageInfo(
            userId,
            PAGE_SIZE,
            startAutoIncrementColumn = syncTaskItem.startAutoIncrementColumn,
            endAutoIncrementColumn = syncTaskItem.endAutoIncrementColumn,
            tClazz)
        ) {
            is ApiResult.Success -> {
                onHttpRequestSuccess(DataTaskType.Download)
                apiResult.result.data as List<T>
            }
            is ApiResult.Failure -> null
        }
    }


    open suspend fun upload() {
        if (!canSync("上传数据")) return
        val needUploadData = getNeedUploadData()
        needUploadData?.ifEmpty { null }?.let {
            saveOrUpload(it)?.let { rspData ->
                updateEventDataAfterUpload(it, rspData)
            }
        }
    }

    override suspend fun downloadData(userId: String): Boolean {
        if (canSync("下载数据-$userId")) {

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
                true
//                delay(DOWNLOAD_INTERVAL)
//                LogUtil.d("===DATASYNC=== 开始下一页数据下载")
//                downloadData(userId)
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

    // region 同步删除
    open suspend fun uploadDeletedData(data: List<String>): Boolean {
        if (!canDoHttpRequest(DataTaskType.UploadDel)) return false
        return if (EventRepository.deleteEventByIds(data, tClazz)) {
            onHttpRequestSuccess(DataTaskType.UploadDel)
            true
        } else false
    }

    override suspend fun uploadDeletedData(userId: String): Boolean {
        if (canSync("上传删除数据")) {
            val data = EventDbRepository.queryDeletedData(tClazz)
            return data?.ifEmpty {
                LogUtil.d("DELETE $tClazz EMPTY", TAG)
                null
            }?.let {
                if (uploadDeletedData(data)) {
                    EventDbRepository.updateDeleteStatusByIds(data, tClazz)
                } else false
            } ?: true
        }
        return false
    }

    //endregion



}