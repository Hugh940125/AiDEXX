package com.microtech.aidexx.db.dao

import com.microtech.aidexx.db.ObjectBox
import com.microtech.aidexx.db.ObjectBox.awaitCallInTx
import com.microtech.aidexx.db.entity.RealCgmHistoryEntity
import com.microtech.aidexx.db.entity.RealCgmHistoryEntity_
import com.microtechmd.blecomm.constant.History
import io.objectbox.kotlin.boxFor
import io.objectbox.kotlin.equal
import io.objectbox.query.QueryBuilder.StringOrder
import java.util.Date

object CgmHistoryDao {

    private val box by lazy { ObjectBox.store.boxFor<RealCgmHistoryEntity>() }

    suspend fun queryByUid(authorId: String): MutableList<RealCgmHistoryEntity>? =
        awaitCallInTx {
            box.query()
                .equal(RealCgmHistoryEntity_.eventType, History.HISTORY_CALIBRATION)
                .equal(RealCgmHistoryEntity_.authorizationId, authorId, StringOrder.CASE_SENSITIVE)
                .build()
                .find()
        }

    suspend fun query(
        startDate: Date,
        endDate: Date,
        authorId: String
    ): MutableList<RealCgmHistoryEntity>? =
        awaitCallInTx {
            box.query()
                .between(
                    RealCgmHistoryEntity_.deviceTime,
                    startDate,
                    endDate
                )
                .equal( RealCgmHistoryEntity_.authorizationId, authorId, StringOrder.CASE_SENSITIVE )
                .equal(RealCgmHistoryEntity_.deleteStatus, 0)
                .build().find()
        }

    suspend fun queryLatest(authorId: String, targetDate: Date): RealCgmHistoryEntity? =
        awaitCallInTx {
            box.query()
                .equal( RealCgmHistoryEntity_.authorizationId, authorId, StringOrder.CASE_SENSITIVE )
                .less(RealCgmHistoryEntity_.deviceTime, targetDate)
                .orderDesc(RealCgmHistoryEntity_.deviceTime)
                .build()
                .findFirst()
        }

    suspend fun insert(list: List<RealCgmHistoryEntity>) =
        awaitCallInTx {
            box.put(list)
        }
}