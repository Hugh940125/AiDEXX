package com.microtech.aidexx.data

import com.google.gson.GsonBuilder
import com.microtech.aidexx.common.equal
import com.microtech.aidexx.common.net.entity.BaseList
import com.microtech.aidexx.common.net.entity.BaseResponse
import com.microtech.aidexx.common.net.entity.PAGE_SIZE
import com.microtech.aidexx.common.net.entity.RESULT_OK
import com.microtech.aidexx.common.net.repository.EventRepository
import com.microtech.aidexx.common.user.UserInfoManager
import com.microtech.aidexx.db.ObjectBox
import com.microtech.aidexx.db.entity.BaseEventEntity
import com.microtech.aidexx.utils.LogUtil
import io.objectbox.Box
import io.objectbox.Property
import io.objectbox.kotlin.awaitCallInTx
import io.objectbox.query.QueryBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
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

    open suspend fun syncDeleteData(json: String): BaseResponse<BaseList<T>>? {
        return null
    }

    abstract suspend fun getRemoteData(userId: String): List<T>?

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

    suspend fun deleteData() {
        val needDelete = getNeedDeleteList()
        needDelete?.let {
            withContext(Dispatchers.IO) {
                if (needDelete.isNotEmpty()) {
                    val list = mutableListOf<HashMap<String, String?>>()
                    for (event in needDelete) {
                        val hashMap = HashMap<String, String?>()
                        hashMap["id"] = event.id
                        hashMap["state"] = "1"
                        list.add(hashMap)
                    }
                    val json = GsonBuilder().create().toJson(list)
                    val syncDeleteData = syncDeleteData(json)
                    syncDeleteData?.let {
                        if (syncDeleteData.code == RESULT_OK) {
                            for (item in needDelete) {
                                item.deleteStatus = 2
                                item.state = 1
                            }
                            entityBox.put(needDelete)
                        }
                    }
                }
            }
        }
    }

    override suspend fun downloadData(userId: String): Boolean {
        if (canSync()) {
            val result = getRemoteData(userId)
            return result?.let {
                if (it.isNotEmpty()) {
                    replaceEventData(responseList = it, type = 3, userId = userId)
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

    private suspend fun getNeedDeleteList(): MutableList<T>? {
        return ObjectBox.store.awaitCallInTx {
            val find = entityBox.query().equal(deleteStatus, 1).notNull(id).equal(
                userId,
                UserInfoManager.instance().userId(),
                QueryBuilder.StringOrder.CASE_INSENSITIVE
            ).build().find()
            find
        }
    }

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
        suspend fun downloadRecentData(userId: String): Boolean = withContext(scope.coroutineContext) {
            var isSuccess = true
            fun updateStatus(ret: Boolean) {
                if (!ret) isSuccess = false
            }

            val tasks = listOf(
                async { updateStatus(EventRepository.getRecentCgmData(userId)) },
                async { updateStatus(EventRepository.getRecentBgData(userId)) },
                async { updateStatus(EventRepository.getRecentCalData(userId)) }
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
                    async { CloudBgHistorySync.upload() }
                    //...
                )
                tasks.awaitAll()
            }
        }
    }
}