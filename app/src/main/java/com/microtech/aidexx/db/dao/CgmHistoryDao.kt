package com.microtech.aidexx.db.dao

import com.microtech.aidexx.common.user.UserInfoManager
import com.microtech.aidexx.db.ObjectBox
import com.microtech.aidexx.db.ObjectBox.awaitCallInTx
import com.microtech.aidexx.db.entity.CgmHistoryEntity
import com.microtech.aidexx.db.entity.CgmHistoryEntity_
import com.microtechmd.blecomm.constant.History
import io.objectbox.kotlin.boxFor
import io.objectbox.kotlin.equal
import io.objectbox.query.QueryBuilder.StringOrder
import java.util.*

object CgmHistoryDao {

    private val box by lazy { ObjectBox.store.boxFor<CgmHistoryEntity>() }

    suspend fun queryByUid(id: String): MutableList<CgmHistoryEntity>? =
        awaitCallInTx {
            box.query()
                .equal(CgmHistoryEntity_.eventType, History.HISTORY_CALIBRATION)
                .equal(CgmHistoryEntity_.authorizationId, id, StringOrder.CASE_SENSITIVE)
                .build()
                .find()
        }

    suspend fun query(
        startDate: Date,
        endDate: Date,
        authorId: String
    ): MutableList<CgmHistoryEntity>? =
        awaitCallInTx {
            box.query()
                .between(
                    CgmHistoryEntity_.deviceTime,
                    startDate,
                    endDate
                )
                .equal( CgmHistoryEntity_.authorizationId, authorId, StringOrder.CASE_SENSITIVE )
                .equal(CgmHistoryEntity_.deleteStatus, 0)
                .build().find()
        }

}