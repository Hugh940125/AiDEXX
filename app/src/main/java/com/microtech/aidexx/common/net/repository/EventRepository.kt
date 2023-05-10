package com.microtech.aidexx.common.net.repository

import com.microtech.aidexx.common.net.ApiService
import com.microtech.aidexx.common.net.entity.PAGE_SIZE
import com.microtech.aidexx.common.net.entity.ReqGetCgmByPage
import com.microtech.aidexx.common.net.entity.toQueryMap
import com.microtech.aidexx.common.user.UserInfoManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object EventRepository {

    private val dispatcher = Dispatchers.IO

    /**
     *   @param pageNum: Int = 1,//	是 1 分页参数 页数(Integer)
     *   @param pageSize: Int = [PAGE_SIZE],//	是 100 分页参数 条数(Integer)
     *   @param userId: String = [UserInfoManager.instance().userId()],//	是 String (String)
     *   @param startAutoIncrementColumn: Long?,//	否 0 自增列(Long).序号
     *   @param endAutoIncrementColumn: Long?,//	否 0 自增列(Long).序号  结束点。闭区间
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



}