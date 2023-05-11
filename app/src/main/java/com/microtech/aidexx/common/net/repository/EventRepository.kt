package com.microtech.aidexx.common.net.repository

import com.microtech.aidexx.common.net.ApiResult
import com.microtech.aidexx.common.net.ApiService
import com.microtech.aidexx.common.net.entity.CGM_RECENT_COUNT
import com.microtech.aidexx.common.net.entity.PAGE_SIZE
import com.microtech.aidexx.common.net.entity.ReqGetCgmByPage
import com.microtech.aidexx.common.net.entity.toQueryMap
import com.microtech.aidexx.common.user.UserInfoManager
import com.microtech.aidexx.data.CloudCgmHistorySync
import com.microtech.aidexx.data.DataSyncController.Companion.DATA_EMPTY_MIN_ID
import com.microtech.aidexx.db.repository.CgmCalibBgRepository
import com.microtech.aidexx.utils.mmkv.MmkvManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object EventRepository {

    private val dispatcher = Dispatchers.IO

    /**
     *   @param pageNum: Int = 1,//	是 1 分页参数 页数(Integer)
     *   @param pageSize: Int = [PAGE_SIZE],//	是 100 分页参数 条数(Integer)
     *   @param userId: String = [UserInfoManager.instance().userId()],//	是 String (String)
     *   @param startAutoIncrementColumn: Long?,//	否 0 自增列(Long).序号 有值的话是返回 大于等于 startAutoIncrementColumn 的数据
     *   @param endAutoIncrementColumn: Long?,// 有值的话是返回 小于等于 endAutoIncrementColumn 的数据
     *   @param orderStrategy: String? //	否 ASC 枚举值.排序规则 默认DESC
     */
    suspend fun getCgmRecordsByPageInfo(
        pageNum: Int = 1,
        pageSize: Int = PAGE_SIZE,
        userId: String = UserInfoManager.instance().userId(),
        startAutoIncrementColumn: Long? = null,
        endAutoIncrementColumn: Long? = null,
        orderStrategy: String? = null
    ) = withContext(dispatcher) {

        val req = ReqGetCgmByPage(
            pageNum,
            pageSize,
            userId,
            startAutoIncrementColumn,
            endAutoIncrementColumn,
            orderStrategy
        )

        ApiService.instance.getCgmRecordsByPageInfo(req.toQueryMap())
    }

    suspend fun getRecentCgmData(userId: String, count: Int = CGM_RECENT_COUNT) = withContext(dispatcher) {
        (0 until count).chunked(PAGE_SIZE).all { list ->
            val curMinId = MmkvManager.getEventDataMinId(CloudCgmHistorySync.getDataMinIdKey(userId))?.let { it - 1 }

            when (val apiResult = getCgmRecordsByPageInfo(userId = userId, pageSize = list.size, endAutoIncrementColumn = curMinId)) {
                is ApiResult.Success -> {
                    apiResult.result.data?.ifEmpty { null }?.let {
                        CgmCalibBgRepository.insert(it)
                        MmkvManager.setEventDataMinId(
                            CloudCgmHistorySync.getDataMinIdKey(userId), it.last().autoIncrementColumn)
                    } ?:let {
                        if (list[0] == 0) {
                            MmkvManager.setEventDataMinId(
                                CloudCgmHistorySync.getDataMinIdKey(userId), DATA_EMPTY_MIN_ID)
                        }
                        return@all true
                    }
                    true
                }
                is ApiResult.Failure -> {
                    return@all false
                }
            }
        }
    }


}