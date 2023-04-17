package com.microtech.aidexx.db.repository

import com.microtech.aidexx.common.user.UserInfoManager
import com.microtech.aidexx.db.dao.CgmHistoryDao
import java.util.Date

object DbRepository {

    suspend fun queryCgmByUid(
        uid: String = UserInfoManager.instance().userId()
    ) = CgmHistoryDao.queryByUid(uid)

    suspend fun queryCgmByPage(
        startDate: Date,
        endDate: Date,
        uid: String = UserInfoManager.instance().userId()
    ) = CgmHistoryDao.query(startDate,endDate,uid)

    suspend fun queryLatestOne(
        uid: String = UserInfoManager.instance().userId()
    ) = CgmHistoryDao.queryLatest(uid)

}