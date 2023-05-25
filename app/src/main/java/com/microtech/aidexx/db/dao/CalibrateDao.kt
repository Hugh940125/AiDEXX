package com.microtech.aidexx.db.dao

import com.microtech.aidexx.db.ObjectBox
import com.microtech.aidexx.db.ObjectBox.awaitCallInTx
import com.microtech.aidexx.db.entity.CalibrateEntity
import com.microtech.aidexx.db.entity.CalibrateEntity_
import io.objectbox.kotlin.boxFor
import io.objectbox.query.QueryBuilder
import java.util.Date

object CalibrateDao {

    private val box by lazy { ObjectBox.store.boxFor<CalibrateEntity>() }

    suspend fun queryByUid(id: String): MutableList<CalibrateEntity>? =
        awaitCallInTx {
            box.query()
                .equal(CalibrateEntity_.userId, id, QueryBuilder.StringOrder.CASE_SENSITIVE)
                .build()
                .find()
        }

    suspend fun query(
        startDate: Date,
        endDate: Date,
        authorId: String
    ): MutableList<CalibrateEntity>? =
        awaitCallInTx {
            box.query()
                .between(
                    CalibrateEntity_.timestamp,
                    startDate.time,
                    endDate.time
                )
                .equal( CalibrateEntity_.userId, authorId, QueryBuilder.StringOrder.CASE_SENSITIVE )
                .equal(CalibrateEntity_.deleteStatus, 0)
                .equal(CalibrateEntity_.isValid, 1)
                .build().find()
        }

    suspend fun insert(list: List<CalibrateEntity>) =
        awaitCallInTx {
            box.put(list)
        }

}