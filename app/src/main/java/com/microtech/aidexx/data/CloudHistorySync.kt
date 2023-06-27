package com.microtech.aidexx.data

import com.microtech.aidexx.common.equal
import com.microtech.aidexx.common.net.ApiResult
import com.microtech.aidexx.common.net.entity.BG_RECENT_COUNT
import com.microtech.aidexx.common.net.entity.BaseResponse
import com.microtech.aidexx.common.net.entity.CAL_RECENT_COUNT
import com.microtech.aidexx.common.net.entity.CGM_RECENT_COUNT
import com.microtech.aidexx.common.net.entity.EVENT_RECENT_COUNT
import com.microtech.aidexx.common.net.entity.PAGE_SIZE
import com.microtech.aidexx.common.net.repository.EventRepository
import com.microtech.aidexx.common.user.UserInfoManager
import com.microtech.aidexx.db.ObjectBox
import com.microtech.aidexx.db.entity.BaseEventEntity
import com.microtech.aidexx.db.entity.BloodGlucoseEntity
import com.microtech.aidexx.db.entity.CalibrateEntity
import com.microtech.aidexx.db.entity.RealCgmHistoryEntity
import com.microtech.aidexx.db.entity.event.DietEntity
import com.microtech.aidexx.db.entity.event.ExerciseEntity
import com.microtech.aidexx.db.entity.event.InsulinEntity
import com.microtech.aidexx.db.entity.event.MedicationEntity
import com.microtech.aidexx.db.entity.event.OthersEntity
import com.microtech.aidexx.db.repository.EventDbRepository
import com.microtech.aidexx.ui.setting.SettingsManager
import com.microtech.aidexx.utils.LogUtil
import io.objectbox.Box
import io.objectbox.Property
import io.objectbox.kotlin.awaitCallInTx
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.util.concurrent.CountDownLatch

abstract class CloudHistorySync<T : BaseEventEntity> : DataSyncController<T>() {
    abstract val idx: Property<T>
    abstract val id: Property<T>
    abstract val frontRecordId: Property<T>
    abstract val userId: Property<T>
    abstract val deleteStatus: Property<T>
    abstract val uploadState: Property<T>
    val entityBox: Box<T> = ObjectBox.store.boxFor(tClazz)

    abstract suspend fun postLocalData(map: HashMap<String, MutableList<T>>): BaseResponse<List<T>>?

    open suspend fun upload() {
        val needUploadData = getNeedUploadData()
        needUploadData?.let {
            if (needUploadData.size > 0) {
                withContext(Dispatchers.IO) {
                    val result = postLocalData(hashMapOf("records" to needUploadData))
                    result?.let {
                        replaceEventData(needUploadData, it.data)
                    }
                }
            }
        }
    }


    //region 下载数据
    open suspend fun getRemoteData(userId: String, syncTaskItem: SyncTaskItem): List<Any>? =
        when (val apiResult = EventRepository.getEventRecordsByPageInfo(
            userId,
            PAGE_SIZE,
            startAutoIncrementColumn = syncTaskItem.startAutoIncrementColumn,
            endAutoIncrementColumn = syncTaskItem.endAutoIncrementColumn,
            tClazz
        )
        ) {
            is ApiResult.Success -> apiResult.result.data
            is ApiResult.Failure -> null
        }

    override suspend fun downloadData(userId: String): Boolean {
        if (canSync()) {

            val syncTaskItem = getFirstTaskItem(userId) ?: let {
                LogUtil.d("SyncTaskItemList=empty ${tClazz.simpleName}", TAG)
                return true
            }

            val result = getRemoteData(userId, syncTaskItem)
            return result?.let {
                if (it.isNotEmpty()) {
                    applyData(userId, it as List<T>)
                }

                if (it.size >= PAGE_SIZE) {
                    // 更新第一条任务的起始点
                    syncTaskItem.endAutoIncrementColumn = (it as List<T>).last().autoIncrementColumn
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

    //endregion

    // region 同步删除
    open suspend fun uploadDeletedData(data: List<String>): Boolean =
        EventRepository.deleteEventByIds(data, tClazz)

    override suspend fun uploadDeletedData(userId: String): Boolean {
        if (canSync()) {
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

    private suspend fun getNeedUploadData(): MutableList<T>? {
        val id = UserInfoManager.instance().userId()
        val mutableList = ObjectBox.store.awaitCallInTx {
            entityBox.query().equal(uploadState, 1).equal(
                userId, id
            ).order(idx).build().find()
        }
        return mutableList
    }

    open suspend fun replaceEventData(
        origin: MutableList<T> = mutableListOf(),
        responseList: List<T>? = mutableListOf(),
        type: Int = 0,
        userId: String? = null,
    ) {
        for (entity in origin) {
            entity.uploadState = 2
        }
        entityBox.put(origin)
    }

    companion object {

        private const val TAG = "CloudHistorySync"

        suspend fun downloadRecentData(userId: String): Boolean = withContext(scope.coroutineContext) {
            var isSuccess = true
            fun updateStatus(ret: Boolean) {
                if (!ret) isSuccess = false
            }

            val tasks = listOf(
                async { SettingsManager.downloadSettings(userId) },
                async { updateStatus(EventRepository.getRecentData<RealCgmHistoryEntity>(userId, CGM_RECENT_COUNT)) },
                async { updateStatus(EventRepository.getRecentData<BloodGlucoseEntity>(userId, BG_RECENT_COUNT)) },
                async { updateStatus(EventRepository.getRecentData<CalibrateEntity>(userId, CAL_RECENT_COUNT)) },
                async { updateStatus(EventRepository.getRecentData<DietEntity>(userId, EVENT_RECENT_COUNT)) },
                async { updateStatus(EventRepository.getRecentData<ExerciseEntity>(userId, EVENT_RECENT_COUNT)) },
                async { updateStatus(EventRepository.getRecentData<MedicationEntity>(userId, EVENT_RECENT_COUNT)) },
                async { updateStatus(EventRepository.getRecentData<InsulinEntity>(userId, EVENT_RECENT_COUNT)) },
                async { updateStatus(EventRepository.getRecentData<OthersEntity>(userId, EVENT_RECENT_COUNT)) }
                //...
            )
            tasks.awaitAll()
            isSuccess
        }

        suspend fun downloadAllData(userId: String? = null, needWait: Boolean = false): SyncStatus {

            var isSuccess = true
            var taskLatch: CountDownLatch? = null

            fun updateRet(status: SyncStatus?) {
                isSuccess = when (status) {
                    is SyncStatus.Failure -> {
                        taskLatch?.countDown()
                        false
                    }

                    is SyncStatus.Success -> {
                        taskLatch?.countDown()
                        isSuccess
                    }

                    else -> isSuccess
                }
            }

            val callback: ((SyncStatus?) -> Unit)? = if (needWait) {
                { updateRet(it) }
            } else null

            val tasks = listOf(
                { CloudCgmHistorySync.startDownload(userId = userId, cb = callback) },
                { CloudBgHistorySync.startDownload(userId = userId, cb = callback) },
                { CloudCalHistorySync.startDownload(userId = userId, cb = callback) },
                { CloudDietHistorySync.startDownload(userId = userId, cb = callback) },
                { CloudExerciseHistorySync.startDownload(userId = userId, cb = callback) },
                { CloudMedicineHistorySync.startDownload(userId = userId, cb = callback) },
                { CloudInsulinHistorySync.startDownload(userId = userId, cb = callback) },
                { CloudOthersHistorySync.startDownload(userId = userId, cb = callback) },
                //...
            )

            withContext(Dispatchers.IO) {
                taskLatch = if (needWait) {
                    CountDownLatch(tasks.size)
                } else null
                tasks.forEach {
                    if (!it.invoke()) {
                        taskLatch?.countDown()
                    }
                }
                taskLatch?.await() // todo 考虑加个超时
            }

            return if (isSuccess) {
                if (needWait) SyncStatus.Success else SyncStatus.Loading()
            } else SyncStatus.Failure()
        }


        suspend fun uploadHistoryData() {
            withContext(scope.coroutineContext) {
                val tasks = listOf(
                    async { CloudCgmHistorySync.upload() },
                    async { CloudBgHistorySync.upload() },
                    async { CloudCalHistorySync.upload() },
                    async { CloudDietHistorySync.upload() },
                    async { CloudExerciseHistorySync.upload() },
                    async { CloudMedicineHistorySync.upload() },
                    async { CloudInsulinHistorySync.upload() },
                    async { CloudOthersHistorySync.upload() },
                    //...
                )
                tasks.awaitAll()
            }
        }

        fun uploadDeletedData() {
            CloudBgHistorySync.startUploadDeletedData()
            CloudDietHistorySync.startUploadDeletedData()
            CloudExerciseHistorySync.startUploadDeletedData()
            CloudMedicineHistorySync.startUploadDeletedData()
            CloudInsulinHistorySync.startUploadDeletedData()
            CloudOthersHistorySync.startUploadDeletedData()
        }

    }
}