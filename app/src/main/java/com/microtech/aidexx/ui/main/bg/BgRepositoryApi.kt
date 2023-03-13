package com.microtech.aidexx.ui.main.bg

import com.microtech.aidexx.common.user.UserInfoManager
import com.microtech.aidexx.db.ObjectBox
import com.microtech.aidexx.db.entity.BloodGlucoseEntity
import com.microtech.aidexx.db.entity.BloodGlucoseEntity_
import io.objectbox.query.QueryBuilder

/**
 *@date 2023/3/13
 *@author Hugh
 *@desc
 */
object BgRepositoryApi {
    fun getLastGlucoseHistory(): BloodGlucoseEntity? {
        val entityClass = BloodGlucoseEntity::class.java
        val mutableList = ObjectBox.bgHistoryBox!!.query()
            .equal(
                BloodGlucoseEntity_.authorizationId,
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
}