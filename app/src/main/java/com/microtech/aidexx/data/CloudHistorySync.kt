package com.microtech.aidexx.data

import com.google.gson.GsonBuilder
import com.microtech.aidexx.common.createWithDateFormat
import com.microtech.aidexx.common.equal
import com.microtech.aidexx.common.net.entity.BaseList
import com.microtech.aidexx.common.net.entity.BaseResponse
import com.microtech.aidexx.common.net.entity.PAGE_SIZE
import com.microtech.aidexx.common.net.entity.RESULT_OK
import com.microtech.aidexx.common.net.repository.EventRepository
import com.microtech.aidexx.common.user.UserInfoManager
import com.microtech.aidexx.db.ObjectBox
import com.microtech.aidexx.db.entity.EventEntity
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

abstract class CloudHistorySync<T : EventEntity>: DataSyncController<T>() {
    abstract val idx: Property<T>
    abstract val id: Property<T>
    abstract val url: String //api请求路径
    abstract val frontRecordId: Property<T>
    abstract val userId: Property<T>
    abstract val deleteStatus: Property<T>
    abstract val recordIndex: Property<T>
    abstract val recordId: Property<T>
    abstract val uuidValue: (T) -> String?
    val entityBox: Box<T> = ObjectBox.store.boxFor(tClazz)

    abstract suspend fun postLocalData(json: String): BaseResponse<BaseList<T>>?

    open suspend fun syncDeleteData(json: String): BaseResponse<BaseList<T>>? {
        return null
    }

    abstract suspend fun getRemoteData(userId: String): List<T>?

    open suspend fun upload() {
        val needUploadData = getNeedUploadData()
        if (needUploadData.size > 0) {
            val json = GsonBuilder().createWithDateFormat().toJson(needUploadData)
            withContext(Dispatchers.IO) {
                val result = postLocalData(json)
                result?.let { response ->
                    if (response.code == RESULT_OK) {
                        response.data?.let {
                            replaceEventData(needUploadData, it.records)
                        }
                    } else {
//                        downloadData()
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
        val result = getRemoteData(userId)
        return result?.let {
            if (it.isNotEmpty()) {
                replaceEventData( responseList = it, type = 3, userId = userId )
            }
            if (it.size >= PAGE_SIZE) {
                LogUtil.d("===DATASYNC=== 开始下一页数据下载")
                // todo 是否需要加个间隔 不然可能会很快
                downloadData(userId)
            } else true
        } ?: false
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

    private suspend fun getNeedUploadData(): MutableList<T> {
        val userId = UserInfoManager.instance().userId()
        val mutableList = ObjectBox.store.awaitCallInTx {
            entityBox.query().isNull(recordId).equal(
                this.userId, userId
            ).order(idx).build().find()
        }
        mutableList?.let {
            if (mutableList.size > 0) {
                val indexList = ObjectBox.store.awaitCallInTx {
                    entityBox.query().notNull(recordIndex).equal(
                        this.userId, userId, QueryBuilder.StringOrder.CASE_INSENSITIVE
                    ).build().property(recordIndex).findLongs()
                }
                val recordIndex =
                    if (indexList == null || indexList.isEmpty()) 0 else indexList.max()
                val item = mutableList[0]
                item.recordIndex = (recordIndex.plus(1))
            }
            return mutableList
        }
        return mutableListOf()
    }

    open suspend fun replaceEventData(
        origin: MutableList<T> = mutableListOf(),
        responseList: List<T>,
        type: Int = 0,
        userId: String? = null,
    ) {

        val userId = UserInfoManager.instance().userId()
        if (origin.isNotEmpty()) {
            for ((index, entity) in origin.withIndex()) {
                entity.recordIndex = responseList[index].recordIndex
                entity.id = responseList[index].id
            }
            entityBox.put(origin)
        } else {
            val temp = mutableListOf<T>()
            for (res in responseList) {
                val existRecord = entityBox.query().equal(frontRecordId, uuidValue(res) ?: "")
                    .equal(this.userId, userId ?: userId).build().find()
                if (existRecord.isEmpty()) {
                    res.authorizationId = userId ?: userId
                    temp.add(res)
                } else {
                    for (record in existRecord) {
                        record.recordIndex = res.recordIndex
                        record.id = res.id
                        temp.add(record)
                    }
                }
            }
            entityBox.put(temp)
        }
    }

    companion object {

        suspend fun downloadRecentData(userId: String): Boolean = withContext(scope.coroutineContext) {
            var isSuccess = true
            fun updateStatus(ret: Boolean) {
                if (!ret) isSuccess = false
            }
            val tasks = listOf(
                async { updateStatus(EventRepository.getRecentCgmData(userId)) },
                async { updateStatus(EventRepository.getRecentBgData(userId)) }
                //...
            )
            tasks.awaitAll()
            isSuccess
        }

        suspend fun downloadAllData(userId: String? = null, needWait: Boolean = false): SyncStatus{

            var isSuccess = true
            var taskLatch : CountDownLatch? = null

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

            val callback: ((SyncStatus?)->Unit)? = if (needWait) { { updateRet(it) } } else null

            val tasks = listOf (
                { CloudCgmHistorySync.startDownload(userId = userId, cb = callback) },
                { CloudBgHistorySync.startDownload(userId = userId, cb = callback) },
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
    }
}