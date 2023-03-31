package com.microtech.aidexx.data

import com.google.gson.GsonBuilder
import com.jeremyliao.liveeventbus.LiveEventBus
import com.microtech.aidexx.common.createWithDateFormat
import com.microtech.aidexx.common.equal
import com.microtech.aidexx.common.net.ApiService
import com.microtech.aidexx.common.net.entity.BaseList
import com.microtech.aidexx.common.net.entity.BasePageList
import com.microtech.aidexx.common.net.entity.BaseResponse
import com.microtech.aidexx.common.net.entity.RESULT_OK
import com.microtech.aidexx.common.user.UserInfoManager
import com.microtech.aidexx.db.ObjectBox
import com.microtech.aidexx.db.entity.EventEntity
import com.microtech.aidexx.db.entity.RealCgmHistoryEntity
import io.objectbox.Box
import io.objectbox.Property
import io.objectbox.kotlin.awaitCallInTx
import io.objectbox.query.QueryBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.lang.reflect.ParameterizedType
import java.util.*
import kotlin.collections.HashMap

abstract class CloudHistorySync<T : EventEntity> {
    abstract val idx: Property<T>
    abstract val id: Property<T>
    abstract val url: String //api请求路径
    abstract val recordUuid: Property<T>
    abstract val authorizationId: Property<T>
    abstract val deleteStatus: Property<T>
    abstract val recordIndex: Property<T>
    abstract val recordId: Property<T>
    abstract val uuidValue: (T) -> String?
    private val clazz =
        (javaClass.genericSuperclass as ParameterizedType).actualTypeArguments[0] as Class<T>
    val entityBox: Box<T> = ObjectBox.store.boxFor(clazz)

    abstract suspend fun postLocalData(json: String): BaseResponse<BaseList<T>>?

    open suspend fun syncDeleteData(json: String): BaseResponse<BaseList<T>>? {
        return null
    }

    abstract suspend fun getRemoteData(authorizationId: String? = null): BaseResponse<BasePageList<T>>?

    open suspend fun upload() {
        val needUploadData = getNeedUploadData()
        if (needUploadData.size > 0) {
            val json = GsonBuilder().createWithDateFormat().toJson(needUploadData)
            withContext(Dispatchers.IO) {
                val result = postLocalData(json)
                result?.let { response ->
                    if (response.info.code == RESULT_OK) {
                        response.content?.let {
                            replaceEventData(needUploadData, it.records)
                        }
                    } else {
                        downloadData()
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
                        if (syncDeleteData.info.code == RESULT_OK) {
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

    suspend fun downloadData(authorizationId: String? = null) {
        val result = getRemoteData(authorizationId)
        result?.let { result ->
            if (result.info.code == RESULT_OK) {
                result.content?.let {
                    replaceEventData(
                        responseList = it.records, cgmStatus = 3, authorId = authorizationId
                    ) //下载的数据
                    val page = it.pageInfo
                    if (page.currentPage * page.pageSize < page.totalCount) {
                        downloadData(authorizationId)
                    }
                }
            }
        }
    }

    suspend fun getNeedDeleteList(): MutableList<T>? {
        return ObjectBox.store.awaitCallInTx {
            val find = entityBox.query().equal(deleteStatus, 1).notNull(id).equal(
                authorizationId,
                UserInfoManager.instance().userId(),
                QueryBuilder.StringOrder.CASE_INSENSITIVE
            ).build().find()
            find
        }
    }

    suspend fun getNeedUploadData(): MutableList<T> {
        val userId = UserInfoManager.instance().userId()
        val mutableList = ObjectBox.store.awaitCallInTx {
            entityBox.query().isNull(recordId).equal(
                authorizationId, userId
            ).order(idx).build().find()
        }
        mutableList?.let {
            if (mutableList.size > 0) {
                val indexList = ObjectBox.store.awaitCallInTx {
                    entityBox.query().notNull(recordIndex).equal(
                        authorizationId, userId, QueryBuilder.StringOrder.CASE_INSENSITIVE
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
        responseList: MutableList<T>,
        cgmStatus: Int = 0,
        authorId: String? = null,
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
                val existRecord = entityBox.query().equal(recordUuid, uuidValue(res) ?: "")
                    .equal(authorizationId, authorId ?: userId).build().find()
                if (existRecord.isEmpty()) {
                    res.authorizationId = authorId ?: userId
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
}