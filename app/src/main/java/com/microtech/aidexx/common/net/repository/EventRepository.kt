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

    suspend fun getCgmRecordsByPageInfo(
        pageNum: Int = 1,
        pageSize: Int = PAGE_SIZE,
        userId: String = UserInfoManager.instance().userId(),
        autoIncrementColumn: Int? = null
    ) = withContext(dispatcher) {

        val req = ReqGetCgmByPage(
            pageNum,
            pageSize,
            userId,
            autoIncrementColumn)

        ApiService.instance.getCgmRecordsByPageInfo(req.toQueryMap())
    }



}