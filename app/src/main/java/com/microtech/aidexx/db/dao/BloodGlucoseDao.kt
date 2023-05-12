package com.microtech.aidexx.db.dao

import com.microtech.aidexx.db.ObjectBox
import com.microtech.aidexx.db.ObjectBox.awaitCallInTx
import com.microtech.aidexx.db.entity.BloodGlucoseEntity
import com.microtech.aidexx.db.entity.BloodGlucoseEntity_
import io.objectbox.kotlin.boxFor
import io.objectbox.query.QueryBuilder
import java.util.Date

object BloodGlucoseDao {

    private val box by lazy { ObjectBox.store.boxFor<BloodGlucoseEntity>() }

    suspend fun queryByUid(id: String): MutableList<BloodGlucoseEntity>? =
        awaitCallInTx {
            box.query()
                .equal(BloodGlucoseEntity_.authorizationId, id, QueryBuilder.StringOrder.CASE_SENSITIVE)
                .build()
                .find()
        }


    suspend fun query(
        startDate: Date,
        endDate: Date,
        authorId: String
    ): MutableList<BloodGlucoseEntity>? =
        awaitCallInTx {
            box.query()
                .between(
                    BloodGlucoseEntity_.testTime,
                    startDate,
                    endDate
                )
                .equal( BloodGlucoseEntity_.authorizationId, authorId, QueryBuilder.StringOrder.CASE_SENSITIVE )
                .equal(BloodGlucoseEntity_.deleteStatus, 0)
                .build().find()
        }

    suspend fun insert(list: List<BloodGlucoseEntity>) =
        awaitCallInTx {
            box.put(list)
        }
}