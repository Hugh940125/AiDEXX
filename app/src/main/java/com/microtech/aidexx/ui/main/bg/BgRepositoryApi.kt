package com.microtech.aidexx.ui.main.bg

import com.microtech.aidexx.common.user.UserInfoManager
import com.microtech.aidexx.db.ObjectBox
import com.microtech.aidexx.db.entity.BloodGlucoseEntity
import com.microtech.aidexx.db.entity.BloodGlucoseEntity_
import io.objectbox.Box
import io.objectbox.query.QueryBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*

/**
 *@date 2023/3/13
 *@author Hugh
 *@desc
 */
object BgRepositoryApi {
    suspend fun getLastGlucoseHistory(): BloodGlucoseEntity? {
        val mutableList = ObjectBox.bgHistoryBox!!.query()
            .equal(
                BloodGlucoseEntity_.userId,
                UserInfoManager.instance().userId(),
                QueryBuilder.StringOrder.CASE_INSENSITIVE
            )
            .equal(BloodGlucoseEntity_.deleteStatus, 0)
            .order(BloodGlucoseEntity_.idx)
            .build()
            .find()
        return if (mutableList.isEmpty()) {
            null
        } else {
            mutableList.last()
        }
    }

    suspend fun getBloodGlucoseHistory(
        timeFrom: Date,
        timeTo: Date,
        authorId: String = UserInfoManager.instance().userId()
    ): MutableList<BloodGlucoseEntity> {
        return withContext(Dispatchers.IO) {
            val list = mutableListOf<BloodGlucoseEntity>()
            val mutableList = ObjectBox.bgHistoryBox!!.query()
                .equal(
                    BloodGlucoseEntity_.userId,
                    authorId,
                    QueryBuilder.StringOrder.CASE_INSENSITIVE
                )
                .equal(BloodGlucoseEntity_.deleteStatus, 0)
                .notEqual(BloodGlucoseEntity_.state, 1)
                .between(BloodGlucoseEntity_.testTime, timeFrom, timeTo)
                .orderDesc(BloodGlucoseEntity_.testTime)
                .build()
                .find()
            list.addAll(mutableList)
            list
        }
    }
}