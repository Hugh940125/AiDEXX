package com.microtech.aidexx.db.repository

import com.microtech.aidexx.common.user.UserInfoManager
import com.microtech.aidexx.db.dao.BloodGlucoseDao
import com.microtech.aidexx.db.dao.CgmHistoryDao
import com.microtech.aidexx.db.entity.BloodGlucoseEntity
import com.microtech.aidexx.db.entity.RealCgmHistoryEntity
import java.util.Date

object CgmCalibBgRepository {

    //region CGM
    suspend fun queryAllCgm(
        uid: String = UserInfoManager.instance().userId()
    ) = CgmHistoryDao.queryByUid(uid)

    suspend fun queryCgmByPage(
        startDate: Date,
        endDate: Date,
        uid: String = UserInfoManager.instance().userId()
    ) = CgmHistoryDao.query(startDate,endDate,uid)

    suspend fun queryNextByTargetDate(
        uid: String = UserInfoManager.instance().userId(),
        targetDate: Date
    ) = CgmHistoryDao.queryNextByTargetDate(uid, targetDate)

    suspend fun insertCgm(list: List<RealCgmHistoryEntity>) =
        CgmHistoryDao.insert(list)

    //endregion

    //region BG
    suspend fun queryBgByPage(
        startDate: Date,
        endDate: Date,
        uid: String = UserInfoManager.instance().userId()
    ) = BloodGlucoseDao.query(startDate, endDate, uid)

    suspend fun insertBg(list: List<BloodGlucoseEntity>) =
        BloodGlucoseDao.insert(list)

    //endregion
}